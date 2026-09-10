/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.fips.test.util;

import com.liferay.osgi.util.ServiceTrackerFactory;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.instance.lifecycle.PortalInstanceLifecycleListener;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.module.util.SystemBundleUtil;
import com.liferay.portal.kernel.test.util.PropsValuesTestUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;
import org.osgi.util.promise.Promise;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author Lucas Miranda
 */
public class FIPSTestUtil {

	public static SafeCloseable enableFIPSModeWithSafeCloseable()
		throws Exception {

		SafeCloseable safeCloseable = PropsValuesTestUtil.swapWithSafeCloseable(
			"FIPS_ENABLED", true);

		_setGatedComponentsEnabled(true);

		return () -> {
			try {
				_setGatedComponentsEnabled(false);
			}
			catch (Exception exception) {
				ReflectionUtil.throwException(exception);
			}
			finally {
				safeCloseable.close();
			}
		};
	}

	public static void portalInstanceRegistered(Company company)
		throws Exception {

		String filterString = StringBundler.concat(
			"(&(component.name=com.liferay.portal.security.fips.internal.",
			"instance.lifecycle.FIPSPortalInstanceLifecycleListener)",
			"(objectClass=", PortalInstanceLifecycleListener.class.getName(),
			"))");

		ServiceTracker
			<PortalInstanceLifecycleListener, PortalInstanceLifecycleListener>
				serviceTracker = ServiceTrackerFactory.open(
					SystemBundleUtil.getBundleContext(), filterString);

		try {
			int timeout = 10000;

			PortalInstanceLifecycleListener portalInstanceLifecycleListener =
				serviceTracker.waitForService(timeout);

			if (portalInstanceLifecycleListener == null) {
				throw new TimeoutException(
					StringBundler.concat(
						"Timeout on waiting for ", filterString, " after ",
						timeout, "ms"));
			}

			portalInstanceLifecycleListener.portalInstanceRegistered(company);
		}
		finally {
			serviceTracker.close();
		}
	}

	private static Bundle _getBundle(String symbolicName) {
		BundleContext bundleContext = SystemBundleUtil.getBundleContext();

		for (Bundle bundle : bundleContext.getBundles()) {
			if (Objects.equals(bundle.getSymbolicName(), symbolicName)) {
				return bundle;
			}
		}

		throw new IllegalStateException(
			"Unable to find bundle " + symbolicName);
	}

	private static List<ComponentDescriptionDTO>
		_getGatedComponentDescriptionDTOs() {

		List<ComponentDescriptionDTO> componentDescriptionDTOs =
			new ArrayList<>();

		ServiceComponentRuntime serviceComponentRuntime =
			_getServiceComponentRuntime();

		for (ComponentDescriptionDTO componentDescriptionDTO :
				serviceComponentRuntime.getComponentDescriptionDTOs(
					TransformUtil.transform(
						_BUNDLE_SYMBOLIC_NAMES, FIPSTestUtil::_getBundle,
						Bundle.class))) {

			if (!componentDescriptionDTO.defaultEnabled) {
				componentDescriptionDTOs.add(componentDescriptionDTO);
			}
		}

		if (componentDescriptionDTOs.isEmpty()) {
			throw new IllegalStateException(
				"No FIPS component is declared disabled by default");
		}

		return componentDescriptionDTOs;
	}

	private static ServiceComponentRuntime _getServiceComponentRuntime() {
		BundleContext bundleContext = SystemBundleUtil.getBundleContext();

		return bundleContext.getService(
			bundleContext.getServiceReference(ServiceComponentRuntime.class));
	}

	private static void _setGatedComponentsEnabled(boolean enabled)
		throws Exception {

		ServiceComponentRuntime serviceComponentRuntime =
			_getServiceComponentRuntime();

		for (ComponentDescriptionDTO componentDescriptionDTO :
				_getGatedComponentDescriptionDTOs()) {

			Promise<Void> promise = null;

			if (enabled) {
				promise = serviceComponentRuntime.enableComponent(
					componentDescriptionDTO);
			}
			else {
				promise = serviceComponentRuntime.disableComponent(
					componentDescriptionDTO);
			}

			promise.getValue();
		}
	}

	private static final String[] _BUNDLE_SYMBOLIC_NAMES = {
		"com.liferay.portal.security.fips.impl",
		"com.liferay.portal.security.fips.web"
	};

}