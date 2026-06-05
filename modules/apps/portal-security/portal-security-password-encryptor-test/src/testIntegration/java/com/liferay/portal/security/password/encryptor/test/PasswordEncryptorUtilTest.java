/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.password.encryptor.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.module.util.SystemBundleUtil;
import com.liferay.portal.kernel.security.pwd.PasswordEncryptor;
import com.liferay.portal.kernel.security.pwd.PasswordEncryptorUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.PropsValuesTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.Objects;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;

/**
 * @author Caio Farias
 */
@RunWith(Arquillian.class)
public class PasswordEncryptorUtilTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testEncrypt() throws Exception {
		_testEncrypt(PasswordEncryptor.TYPE_BCRYPT);
		_testEncrypt(PasswordEncryptor.TYPE_UFC_CRYPT);
	}

	@Test
	public void testPasswordEncryptorsInFIPSMode() throws Exception {
		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			_restartBundle();

			Assert.assertFalse(
				_isComponentEnabled(
					"com.liferay.portal.security.password.encryptor.internal." +
						"BCryptPasswordEncryptor"));
			Assert.assertFalse(
				_isComponentEnabled(
					"com.liferay.portal.security.password.encryptor.internal." +
						"CryptPasswordEncryptor"));
		}
		finally {
			_restartBundle();
		}

		Assert.assertTrue(
			_isComponentEnabled(
				"com.liferay.portal.security.password.encryptor.internal." +
					"BCryptPasswordEncryptor"));
		Assert.assertTrue(
			_isComponentEnabled(
				"com.liferay.portal.security.password.encryptor.internal." +
					"CryptPasswordEncryptor"));
	}

	private Bundle _getBundle() {
		BundleContext bundleContext = SystemBundleUtil.getBundleContext();

		for (Bundle bundle : bundleContext.getBundles()) {
			if (Objects.equals(
					bundle.getSymbolicName(),
					"com.liferay.portal.security.password.encryptor.impl")) {

				return bundle;
			}
		}

		return null;
	}

	private boolean _isComponentEnabled(String componentName) {
		Bundle bundle = _getBundle();

		Assert.assertNotNull(bundle);

		ComponentDescriptionDTO componentDescriptionDTO =
			_serviceComponentRuntime.getComponentDescriptionDTO(
				bundle, componentName);

		Assert.assertNotNull(componentDescriptionDTO);

		return _serviceComponentRuntime.isComponentEnabled(
			componentDescriptionDTO);
	}

	private void _restartBundle() throws Exception {
		Bundle bundle = _getBundle();

		if (bundle == null) {
			return;
		}

		bundle.stop();

		bundle.start();
	}

	private void _testEncrypt(String algorithm) throws Exception {
		String password = RandomTestUtil.randomString();

		String encryptedPassword = PasswordEncryptorUtil.encrypt(
			algorithm, password, null);

		Assert.assertEquals(
			encryptedPassword,
			PasswordEncryptorUtil.encrypt(
				algorithm, password, encryptedPassword));
	}

	@Inject
	private ServiceComponentRuntime _serviceComponentRuntime;

}