/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.key.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;
import com.liferay.portal.configuration.test.util.ConfigurationTestUtil;
import com.liferay.portal.kernel.module.util.SystemBundleUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.security.key.KeyReference;
import com.liferay.portal.security.key.KeyReferenceUtil;
import com.liferay.portal.security.key.secret.Secret;
import com.liferay.portal.security.key.secret.SecretManager;
import com.liferay.portal.security.key.spi.secret.SecretProvider;
import com.liferay.portal.security.key.test.util.TestSecretProvider;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.Dictionary;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedService;

/**
 * @author Pedro Victor Silvestre
 */
@RunWith(Arquillian.class)
public class SecretReferenceConfigurationPluginImplTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_identifier = RandomTestUtil.randomString();
		_pid = RandomTestUtil.randomString();
		_secretValue = RandomTestUtil.randomString();

		_registerTestSecretProvider();

		ConfigurationTestUtil.saveConfiguration(
			_KEY_MANAGER_CUSTOM_PROFILE_CONFIGURATION_PID,
			HashMapDictionaryBuilder.<String, Object>put(
				"companySecretProviderId", _SECRET_PROVIDER_ID
			).put(
				"systemSecretProviderId", _SECRET_PROVIDER_ID
			).build());

		_secretManager.putSecret(
			TestPropsValues.getCompanyId(),
			new Secret(
				new KeyReference(
					_identifier, _SECRET_PROVIDER_ID, KeyReference.Type.SECRET),
				_secretValue));
	}

	@After
	public void tearDown() throws Exception {
		if (_managedServiceRegistration != null) {
			_managedServiceRegistration.unregister();
		}

		if (_configuration != null) {
			ConfigurationTestUtil.deleteConfiguration(_configuration);
		}

		ConfigurationTestUtil.deleteConfiguration(
			_KEY_MANAGER_CUSTOM_PROFILE_CONFIGURATION_PID);

		if (_secretProviderServiceRegistration != null) {
			_secretProviderServiceRegistration.unregister();
		}
	}

	@Test
	public void testModifyConfiguration() throws Exception {
		Assert.assertEquals(
			_secretValue,
			_getProcessedProperty("password", _toSecretReference(_identifier)));
		Assert.assertEquals(
			_secretValue,
			_getProcessedProperty(
				"password", "${secretRef:*:" + _identifier + "}"));
	}

	@Test
	public void testModifyConfigurationDoesNotPersistSecret() throws Exception {
		String secretReference = _toSecretReference(_identifier);

		_getProcessedProperty("password", secretReference);

		Dictionary<String, Object> properties = _configuration.getProperties();

		Assert.assertEquals(secretReference, properties.get("password"));
	}

	@Test
	public void testModifyConfigurationWhenManagedServiceIsRegistered()
		throws Exception {

		_registerManagedService();

		_saveConfiguration("password", _toSecretReference(_identifier));

		Assert.assertEquals(_secretValue, _takeDeliveredPassword());
	}

	@Test
	public void testModifyConfigurationWhenProviderAppearsLate()
		throws Exception {

		_secretProviderServiceRegistration.unregister();

		_secretProviderServiceRegistration = null;

		_registerManagedService();

		String secretReference = _toSecretReference(_identifier);

		_saveConfiguration("password", secretReference);

		Assert.assertEquals(secretReference, _takeDeliveredPassword());

		_registerTestSecretProvider();

		Assert.assertEquals(_secretValue, _takeDeliveredPassword());
	}

	@Test
	public void testModifyConfigurationWhenReferenceIsNotResolved()
		throws Exception {

		String keyReference = KeyReferenceUtil.toKeyReferenceString(
			new KeyReference(
				_identifier, _SECRET_PROVIDER_ID, KeyReference.Type.CRYPTO));

		Assert.assertEquals(
			keyReference, _getProcessedProperty("password", keyReference));

		String secretReference = _toSecretReference(
			RandomTestUtil.randomString());

		Assert.assertEquals(
			secretReference,
			_getProcessedProperty("password", secretReference));
	}

	@Test
	public void testModifyConfigurationWhenScopeIsGroup() throws Exception {
		_configuration = _configurationAdmin.getConfiguration(_pid);

		ConfigurationTestUtil.saveConfiguration(
			_configuration,
			HashMapDictionaryBuilder.<String, Object>put(
				ExtendedObjectClassDefinition.Scope.GROUP.getPropertyKey(),
				TestPropsValues.getGroupId()
			).put(
				"password", _toSecretReference(_identifier)
			).build());

		Dictionary<String, Object> properties =
			_configuration.getProcessedProperties(null);

		Assert.assertEquals(_secretValue, properties.get("password"));
	}

	@Test
	public void testModifyConfigurationWhenValueIsStringArray()
		throws Exception {

		String literal = RandomTestUtil.randomString();

		_saveConfiguration(
			"passwords",
			new String[] {literal, _toSecretReference(_identifier)});

		Dictionary<String, Object> properties =
			_configuration.getProcessedProperties(null);

		Assert.assertArrayEquals(
			new String[] {literal, _secretValue},
			(String[])properties.get("passwords"));
	}

	private String _getProcessedProperty(String key, Object value)
		throws Exception {

		_saveConfiguration(key, value);

		Dictionary<String, Object> properties =
			_configuration.getProcessedProperties(null);

		return (String)properties.get(key);
	}

	private void _registerManagedService() {
		_managedServiceRegistration = _bundleContext.registerService(
			ManagedService.class,
			properties -> {
				if (properties != null) {
					_propertiesBlockingQueue.add(properties);
				}
			},
			HashMapDictionaryBuilder.<String, Object>put(
				Constants.SERVICE_PID, _pid
			).build());
	}

	private void _registerTestSecretProvider() {
		_secretProviderServiceRegistration = _bundleContext.registerService(
			SecretProvider.class, _testSecretProvider,
			HashMapDictionaryBuilder.<String, Object>put(
				"secret.provider.id", _SECRET_PROVIDER_ID
			).build());
	}

	private void _saveConfiguration(String key, Object value) throws Exception {
		_configuration = _configurationAdmin.getConfiguration(_pid);

		ConfigurationTestUtil.saveConfiguration(
			_configuration,
			HashMapDictionaryBuilder.<String, Object>put(
				ExtendedObjectClassDefinition.Scope.COMPANY.getPropertyKey(),
				TestPropsValues.getCompanyId()
			).put(
				key, value
			).build());
	}

	private String _takeDeliveredPassword() throws Exception {
		Dictionary<String, ?> properties = _propertiesBlockingQueue.poll(
			1, TimeUnit.MINUTES);

		Assert.assertNotNull("No configuration was delivered", properties);

		return (String)properties.get("password");
	}

	private String _toSecretReference(String identifier) {
		return KeyReferenceUtil.toKeyReferenceString(
			new KeyReference(
				identifier, _SECRET_PROVIDER_ID, KeyReference.Type.SECRET));
	}

	private static final String _KEY_MANAGER_CUSTOM_PROFILE_CONFIGURATION_PID =
		"com.liferay.portal.security.key.internal.profile.configuration." +
			"KeyManagerCustomProfileConfiguration";

	private static final String _SECRET_PROVIDER_ID = "test-key-secret";

	private static final BundleContext _bundleContext =
		SystemBundleUtil.getBundleContext();

	private Configuration _configuration;

	@Inject
	private ConfigurationAdmin _configurationAdmin;

	private String _identifier;
	private ServiceRegistration<ManagedService> _managedServiceRegistration;
	private String _pid;
	private final BlockingQueue<Dictionary<String, ?>>
		_propertiesBlockingQueue = new LinkedBlockingQueue<>();

	@Inject
	private SecretManager _secretManager;

	private ServiceRegistration<SecretProvider>
		_secretProviderServiceRegistration;
	private String _secretValue;
	private final TestSecretProvider _testSecretProvider =
		new TestSecretProvider(_SECRET_PROVIDER_ID);

}