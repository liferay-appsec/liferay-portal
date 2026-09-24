/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.storage.service.impl;

import com.liferay.petra.concurrent.NoticeableExecutorService;
import com.liferay.petra.concurrent.NoticeableFuture;
import com.liferay.petra.concurrent.ThreadPoolHandlerAdapter;
import com.liferay.petra.executor.PortalExecutorConfig;
import com.liferay.petra.executor.PortalExecutorManager;
import com.liferay.petra.lang.CentralizedThreadLocal;
import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.portal.aop.AopService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.model.ModelHintsUtil;
import com.liferay.portal.kernel.security.auth.CompanyInheritableThreadLocalCallable;
import com.liferay.portal.kernel.service.SQLStateAcceptor;
import com.liferay.portal.kernel.spring.aop.Property;
import com.liferay.portal.kernel.spring.aop.Retry;
import com.liferay.portal.kernel.util.DigesterUtil;
import com.liferay.portal.kernel.util.NamedThreadFactory;
import com.liferay.portal.kernel.util.PortalClassLoaderUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.security.audit.storage.exception.AuditPseudonymIdentityValueException;
import com.liferay.portal.security.audit.storage.model.AuditPseudonym;
import com.liferay.portal.security.audit.storage.service.base.AuditPseudonymLocalServiceBaseImpl;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Christian Moura
 */
@Component(
	property = "model.class.name=com.liferay.portal.security.audit.storage.model.AuditPseudonym",
	service = AopService.class
)
public class AuditPseudonymLocalServiceImpl
	extends AuditPseudonymLocalServiceBaseImpl {

	@Override
	@Retry(
		acceptor = SQLStateAcceptor.class,
		properties = {
			@Property(
				name = SQLStateAcceptor.SQLSTATE,
				value = SQLStateAcceptor.SQLSTATE_INTEGRITY_CONSTRAINT_VIOLATION
			)
		}
	)
	public AuditPseudonym addAuditPseudonym(
			long companyId, String contextName, String fieldCategory,
			String identityValue)
		throws PortalException {

		_validate(identityValue);

		if (Validator.isBlank(contextName)) {
			contextName = "INSTANCE";
		}

		String identityValueHash = DigesterUtil.digestHex(
			DigesterUtil.SHA_256, identityValue);

		AuditPseudonym auditPseudonym =
			auditPseudonymPersistence.fetchByC_CN_FC_IVH(
				companyId, contextName, fieldCategory, identityValueHash,
				false);

		if (auditPseudonym != null) {
			return auditPseudonym;
		}

		long auditPseudonymId = counterLocalService.increment();

		auditPseudonym = auditPseudonymPersistence.create(auditPseudonymId);

		auditPseudonym.setCompanyId(companyId);
		auditPseudonym.setCreateDate(new Date());
		auditPseudonym.setContextName(contextName);
		auditPseudonym.setFieldCategory(fieldCategory);
		auditPseudonym.setIdentityValue(identityValue);
		auditPseudonym.setIdentityValueHash(identityValueHash);

		return auditPseudonymPersistence.update(auditPseudonym);
	}

	@Override
	public AuditPseudonym getOrAddAuditPseudonym(
			long companyId, String contextName, String fieldCategory,
			String identityValue)
		throws PortalException {

		_validate(identityValue);

		if (Validator.isBlank(contextName)) {
			contextName = "INSTANCE";
		}

		String identityValueHash = DigesterUtil.digestHex(
			DigesterUtil.SHA_256, identityValue);

		AuditPseudonym auditPseudonym =
			auditPseudonymPersistence.fetchByC_CN_FC_IVH(
				companyId, contextName, fieldCategory, identityValueHash);

		if (auditPseudonym == null) {
			auditPseudonym = _addAuditPseudonym(
				companyId, contextName, fieldCategory, identityValue);

			auditPseudonymPersistence.cacheResult(auditPseudonym);
		}

		return auditPseudonym;
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_registerPortalExecutorConfig(bundleContext);

		_noticeableExecutorService = _portalExecutorManager.getPortalExecutor(
			AuditPseudonymLocalServiceImpl.class.getName());
	}

	@Deactivate
	@Override
	protected void deactivate() {
		super.deactivate();

		_noticeableExecutorService.shutdown();

		_serviceRegistration.unregister();
	}

	private AuditPseudonym _addAuditPseudonym(
		long companyId, String contextName, String fieldCategory,
		String identityValue) {

		NoticeableFuture<AuditPseudonym> noticeableFuture =
			_noticeableExecutorService.submit(
				new CompanyInheritableThreadLocalCallable<>(
					() -> auditPseudonymLocalService.addAuditPseudonym(
						companyId, contextName, fieldCategory, identityValue)));

		try {
			return noticeableFuture.get(2, TimeUnit.SECONDS);
		}
		catch (ExecutionException executionException) {
			return ReflectionUtil.throwException(executionException.getCause());
		}
		catch (InterruptedException interruptedException) {
			noticeableFuture.cancel(false);

			Thread thread = Thread.currentThread();

			thread.interrupt();

			throw new SystemException(interruptedException);
		}
		catch (TimeoutException timeoutException) {
			noticeableFuture.cancel(false);

			throw new SystemException(timeoutException);
		}
	}

	private void _registerPortalExecutorConfig(BundleContext bundleContext) {
		PortalExecutorConfig portalExecutorConfig = new PortalExecutorConfig(
			AuditPseudonymLocalServiceImpl.class.getName(), 1, 1, 60,
			TimeUnit.SECONDS, 200,
			new NamedThreadFactory(
				AuditPseudonymLocalServiceImpl.class.getName(),
				Thread.NORM_PRIORITY, PortalClassLoaderUtil.getClassLoader()),
			new ThreadPoolExecutor.AbortPolicy(),
			new ThreadPoolHandlerAdapter() {

				@Override
				public void afterExecute(
					Runnable runnable, Throwable throwable) {

					CentralizedThreadLocal.
						clearShortLivedCentralizedThreadLocals();
				}

			});

		_serviceRegistration = bundleContext.registerService(
			PortalExecutorConfig.class, portalExecutorConfig, null);
	}

	private void _validate(String identityValue) throws PortalException {
		if (Validator.isBlank(identityValue)) {
			throw new AuditPseudonymIdentityValueException(
				"Identity value is blank");
		}

		int identityValueMaxLength = ModelHintsUtil.getMaxLength(
			AuditPseudonym.class.getName(), "identityValue");

		if (identityValue.length() > identityValueMaxLength) {
			throw new AuditPseudonymIdentityValueException(
				"Maximum length of identity value exceeded");
		}
	}

	private NoticeableExecutorService _noticeableExecutorService;

	@Reference
	private PortalExecutorManager _portalExecutorManager;

	private ServiceRegistration<PortalExecutorConfig> _serviceRegistration;

}