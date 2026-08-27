/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.key.internal.configuration.plugin;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.security.key.secret.SecretResolver;
import com.liferay.portal.security.key.secret.exception.SecretException;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author Pedro Victor Silvestre
 */
public class SecretReferenceConfigurationPluginImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);

		ReflectionTestUtil.setFieldValue(
			_secretReferenceConfigurationPluginImpl,
			"_configurationAdminServiceTracker",
			_configurationAdminServiceTracker);
		ReflectionTestUtil.setFieldValue(
			_secretReferenceConfigurationPluginImpl,
			"_secretResolverServiceTracker", _secretResolverServiceTracker);

		Mockito.when(
			_configurationAdminServiceTracker.getService()
		).thenReturn(
			_configurationAdmin
		);

		Mockito.when(
			_secretResolverServiceTracker.getService()
		).thenReturn(
			_secretResolver
		);
	}

	@Test
	public void testModifyConfiguration() throws Exception {
		long companyId = RandomTestUtil.randomLong();

		_assertResolvedPassword(
			companyId,
			HashMapDictionaryBuilder.<String, Object>put(
				ExtendedObjectClassDefinition.Scope.COMPANY.getPropertyKey(),
				companyId
			).put(
				"password", _KEY_REFERENCE_SECRET
			).build());

		_assertResolvedPassword(
			CompanyConstants.SYSTEM,
			HashMapDictionaryBuilder.<String, Object>put(
				"password", _KEY_REFERENCE_SECRET
			).build());
	}

	@Test
	public void testModifyConfigurationWhenOneReferenceFails()
		throws Exception {

		Dictionary<String, Object> properties =
			HashMapDictionaryBuilder.<String, Object>put(
				Constants.SERVICE_PID, _TEST_CONFIGURATION_PID
			).put(
				"otherPassword", _KEY_REFERENCE_SECRET_OTHER
			).put(
				"password", _KEY_REFERENCE_SECRET
			).build();

		Mockito.when(
			_secretResolver.resolve(
				CompanyConstants.SYSTEM, _KEY_REFERENCE_SECRET_OTHER)
		).thenThrow(
			new SecretException()
		);

		String value = RandomTestUtil.randomString();

		Mockito.when(
			_secretResolver.resolve(
				CompanyConstants.SYSTEM, _KEY_REFERENCE_SECRET)
		).thenReturn(
			value
		);

		_secretReferenceConfigurationPluginImpl.modifyConfiguration(
			null, properties);

		Assert.assertEquals(
			_KEY_REFERENCE_SECRET_OTHER, properties.get("otherPassword"));
		Assert.assertEquals(value, properties.get("password"));

		Assert.assertTrue(_getPids().contains(_TEST_CONFIGURATION_PID));
	}

	@Test
	public void testModifyConfigurationWhenReferenceIsCrypto()
		throws Exception {

		Dictionary<String, Object> properties =
			HashMapDictionaryBuilder.<String, Object>put(
				Constants.SERVICE_PID, _TEST_CONFIGURATION_PID
			).put(
				"password", _KEY_REFERENCE_CRYPTO
			).build();

		_secretReferenceConfigurationPluginImpl.modifyConfiguration(
			null, properties);

		Assert.assertEquals(_KEY_REFERENCE_CRYPTO, properties.get("password"));

		Assert.assertTrue(_getPids().isEmpty());

		Mockito.verifyNoInteractions(_secretResolver);
	}

	@Test
	public void testModifyConfigurationWhenSecretResolverIsUnavailable()
		throws Exception {

		Mockito.when(
			_secretResolverServiceTracker.getService()
		).thenReturn(
			null
		);

		Dictionary<String, Object> properties =
			HashMapDictionaryBuilder.<String, Object>put(
				Constants.SERVICE_PID, _TEST_CONFIGURATION_PID
			).put(
				"password", _KEY_REFERENCE_SECRET
			).build();

		_secretReferenceConfigurationPluginImpl.modifyConfiguration(
			null, properties);

		Assert.assertEquals(_KEY_REFERENCE_SECRET, properties.get("password"));

		Assert.assertTrue(_getPids().contains(_TEST_CONFIGURATION_PID));
	}

	@Test
	public void testModifyConfigurationWhenThereAreNoReferences()
		throws Exception {

		_secretReferenceConfigurationPluginImpl.modifyConfiguration(
			null,
			HashMapDictionaryBuilder.<String, Object>put(
				"enabled", Boolean.TRUE
			).put(
				"name", RandomTestUtil.randomString()
			).put(
				"size", 42
			).build());

		Mockito.verifyNoInteractions(_secretResolver);
	}

	@Test
	public void testModifyConfigurationWhenValuesHaveDifferentTypes()
		throws Exception {

		String literal = RandomTestUtil.randomString();

		Dictionary<String, Object> properties =
			HashMapDictionaryBuilder.<String, Object>put(
				"enabled", Boolean.TRUE
			).put(
				"password", _KEY_REFERENCE_SECRET
			).put(
				"passwords", new String[] {literal, _KEY_REFERENCE_SECRET}
			).put(
				"size", 42
			).build();

		String value = RandomTestUtil.randomString();

		Mockito.when(
			_secretResolver.resolve(
				CompanyConstants.SYSTEM, _KEY_REFERENCE_SECRET)
		).thenReturn(
			value
		);

		_secretReferenceConfigurationPluginImpl.modifyConfiguration(
			null, properties);

		Assert.assertEquals(Boolean.TRUE, properties.get("enabled"));
		Assert.assertEquals(value, properties.get("password"));
		Assert.assertArrayEquals(
			new String[] {literal, value},
			(String[])properties.get("passwords"));
		Assert.assertEquals(42, properties.get("size"));
	}

	@Test
	public void testRedeliverWhenConfigurationAdminFails() throws Exception {
		_record();

		Mockito.when(
			_configurationAdmin.listConfigurations(
				_TEST_CONFIGURATION_PID_FILTER)
		).thenThrow(
			new IllegalStateException()
		);

		_redeliver();

		Assert.assertTrue(_getPids().contains(_TEST_CONFIGURATION_PID));
	}

	@Test
	public void testRedeliverWhenConfigurationAdminIsUnavailable()
		throws Exception {

		_record();

		Mockito.when(
			_configurationAdminServiceTracker.getService()
		).thenReturn(
			null
		);

		_redeliver();

		Assert.assertTrue(_getPids().contains(_TEST_CONFIGURATION_PID));

		Mockito.verifyNoInteractions(_configurationAdmin);
	}

	@Test
	public void testRedeliverWhenConfigurationIsMissing() throws Exception {
		_record();

		Mockito.when(
			_configurationAdmin.listConfigurations(
				_TEST_CONFIGURATION_PID_FILTER)
		).thenReturn(
			null
		);

		_redeliver();

		Assert.assertTrue(_getPids().isEmpty());
	}

	@Test
	public void testRedeliverWhenNothingIsRecorded() throws Exception {
		_redeliver();

		Mockito.verifyNoInteractions(_configurationAdmin);
	}

	@Test
	public void testRedeliverWhenPidIsRecorded() throws Exception {
		_record();

		Dictionary<String, Object> properties =
			HashMapDictionaryBuilder.<String, Object>put(
				"password", _KEY_REFERENCE_SECRET
			).build();

		Mockito.when(
			_configuration.getProperties()
		).thenReturn(
			properties
		);

		Mockito.when(
			_configurationAdmin.listConfigurations(
				_TEST_CONFIGURATION_PID_FILTER)
		).thenReturn(
			new Configuration[] {_configuration}
		);

		_redeliver();

		Mockito.verify(
			_configuration
		).update(
			properties
		);

		Assert.assertTrue(_getPids().isEmpty());
	}

	private void _assertResolvedPassword(
			long companyId, Dictionary<String, Object> properties)
		throws Exception {

		String value = RandomTestUtil.randomString();

		Mockito.when(
			_secretResolver.resolve(companyId, _KEY_REFERENCE_SECRET)
		).thenReturn(
			value
		);

		_secretReferenceConfigurationPluginImpl.modifyConfiguration(
			null, properties);

		Assert.assertEquals(value, properties.get("password"));
	}

	private Set<String> _getPids() {
		return ReflectionTestUtil.getFieldValue(
			_secretReferenceConfigurationPluginImpl, "_pids");
	}

	private void _record() {
		ReflectionTestUtil.invoke(
			_secretReferenceConfigurationPluginImpl, "_record",
			new Class<?>[] {List.class, String.class, Dictionary.class},
			Collections.singletonList("password"), _TEST_CONFIGURATION_PID,
			HashMapDictionaryBuilder.<String, Object>put(
				"password", _KEY_REFERENCE_SECRET
			).build());
	}

	private void _redeliver() {
		ReflectionTestUtil.invoke(
			_secretReferenceConfigurationPluginImpl, "_redeliver",
			new Class<?>[0]);
	}

	private static final String _KEY_REFERENCE_CRYPTO =
		"${keyRef:provider:identifier}";

	private static final String _KEY_REFERENCE_SECRET =
		"${secretRef:provider:identifier}";

	private static final String _KEY_REFERENCE_SECRET_OTHER =
		"${secretRef:provider:other}";

	private static final String _TEST_CONFIGURATION_PID =
		"com.liferay.test.Configuration";

	private static final String _TEST_CONFIGURATION_PID_FILTER =
		StringBundler.concat(
			"(", Constants.SERVICE_PID, "=", _TEST_CONFIGURATION_PID, ")");

	@Mock
	private Configuration _configuration;

	@Mock
	private ConfigurationAdmin _configurationAdmin;

	@Mock
	private ServiceTracker<ConfigurationAdmin, ConfigurationAdmin>
		_configurationAdminServiceTracker;

	private final SecretReferenceConfigurationPluginImpl
		_secretReferenceConfigurationPluginImpl =
			new SecretReferenceConfigurationPluginImpl(
				Mockito.mock(BundleContext.class));

	@Mock
	private SecretResolver _secretResolver;

	@Mock
	private ServiceTracker<SecretResolver, SecretResolver>
		_secretResolverServiceTracker;

}