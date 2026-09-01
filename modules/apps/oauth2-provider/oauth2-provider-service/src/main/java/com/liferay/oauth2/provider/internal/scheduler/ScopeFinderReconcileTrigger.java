/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth2.provider.internal.scheduler;

import com.liferay.oauth2.provider.scope.liferay.UnresolvedScopeAliasReconciler;
import com.liferay.oauth2.provider.scope.liferay.UnresolvedScopeAliasesRegistry;
import com.liferay.oauth2.provider.scope.spi.scope.finder.ScopeFinder;
import com.liferay.petra.executor.PortalExecutorManager;
import com.liferay.portal.kernel.cluster.ClusterMasterExecutor;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.module.framework.ModuleServiceLifecycle;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * Reconciles unresolved scope aliases as soon as a {@link ScopeFinder}
 * registers, so an alias bound to a late-registering scope source is granted
 * immediately rather than waiting for the periodic scheduler.
 *
 * <p>
 * Each registration requests a reconcile. Requests are coalesced through the
 * {@code _reconcilePending} / {@code _reconcileRunning} handshake and drained on
 * a portal executor thread, so a burst of registrations at startup collapses
 * into as few passes as possible without dropping a request that arrives while a
 * pass is running. Because a finder's aliases can lag its service registration,
 * a drain retries a few times; it stops as soon as a pass reports it bound
 * something, so an unrelated alias that never resolves does not keep every
 * registration retrying. Reconciling runs only on the cluster master, matching
 * the clustered scheduler that owns the fallback pass.
 * </p>
 *
 * @author Allen Ziegenfus
 */
@Component(service = {})
public class ScopeFinderReconcileTrigger {

	@Activate
	protected void activate(BundleContext bundleContext) {
		_scopeFinderServiceTracker = new ServiceTracker<>(
			bundleContext, ScopeFinder.class,
			new ScopeFinderServiceTrackerCustomizer());

		_scopeFinderServiceTracker.open();
	}

	@Deactivate
	protected void deactivate() {
		_deactivated = true;

		if (_scopeFinderServiceTracker != null) {
			_scopeFinderServiceTracker.close();
		}
	}

	private void _drainReconcile() {
		do {
			try {
				if (!_reconcilePending.compareAndSet(true, false)) {
					return;
				}

				_retryReconcile();
			}
			finally {
				_reconcileRunning.set(false);
			}
		}
		while (_reconcilePending.get() &&
			   _reconcileRunning.compareAndSet(false, true));
	}

	private void _requestReconcile() {
		if (!_clusterMasterExecutor.isMaster() ||
			_unresolvedScopeAliasesRegistry.isEmpty()) {

			return;
		}

		_reconcilePending.set(true);

		if (!_reconcileRunning.compareAndSet(false, true)) {
			return;
		}

		try {
			ExecutorService executorService =
				_portalExecutorManager.getPortalExecutor(
					ScopeFinderReconcileTrigger.class.getName());

			executorService.submit(this::_drainReconcile);
		}
		catch (Throwable throwable) {
			_reconcileRunning.set(false);

			if (_log.isWarnEnabled()) {
				_log.warn("Unable to submit the reconcile task", throwable);
			}
		}
	}

	private void _retryReconcile() {
		for (int i = 0; i < _RECONCILE_ATTEMPT_COUNT; i++) {
			if (_deactivated) {
				return;
			}

			boolean bound = false;

			try {
				bound = _unresolvedScopeAliasReconciler.reconcile();
			}
			catch (Exception exception) {
				if (_log.isWarnEnabled()) {
					_log.warn(
						"Unable to reconcile unresolved scope aliases",
						exception);
				}
			}

			if (bound || _unresolvedScopeAliasesRegistry.isEmpty() ||
				(i == (_RECONCILE_ATTEMPT_COUNT - 1))) {

				return;
			}

			try {
				Thread.sleep(_RECONCILE_ATTEMPT_DELAY);
			}
			catch (InterruptedException interruptedException) {
				if (_log.isDebugEnabled()) {
					_log.debug(
						"Interrupted while waiting to retry the reconcile",
						interruptedException);
				}

				Thread.currentThread(
				).interrupt();

				return;
			}
		}
	}

	private static final int _RECONCILE_ATTEMPT_COUNT = 3;

	private static final long _RECONCILE_ATTEMPT_DELAY = 250;

	private static final Log _log = LogFactoryUtil.getLog(
		ScopeFinderReconcileTrigger.class);

	@Reference
	private ClusterMasterExecutor _clusterMasterExecutor;

	private volatile boolean _deactivated;

	@Reference(target = ModuleServiceLifecycle.PORTAL_INITIALIZED)
	private ModuleServiceLifecycle _moduleServiceLifecycle;

	@Reference
	private PortalExecutorManager _portalExecutorManager;

	private final AtomicBoolean _reconcilePending = new AtomicBoolean();
	private final AtomicBoolean _reconcileRunning = new AtomicBoolean();
	private ServiceTracker<ScopeFinder, Boolean> _scopeFinderServiceTracker;

	@Reference
	private UnresolvedScopeAliasesRegistry _unresolvedScopeAliasesRegistry;

	@Reference
	private UnresolvedScopeAliasReconciler _unresolvedScopeAliasReconciler;

	private class ScopeFinderServiceTrackerCustomizer
		implements ServiceTrackerCustomizer<ScopeFinder, Boolean> {

		@Override
		public Boolean addingService(
			ServiceReference<ScopeFinder> serviceReference) {

			_requestReconcile();

			return Boolean.TRUE;
		}

		@Override
		public void modifiedService(
			ServiceReference<ScopeFinder> serviceReference, Boolean present) {
		}

		@Override
		public void removedService(
			ServiceReference<ScopeFinder> serviceReference, Boolean present) {
		}

	}

}