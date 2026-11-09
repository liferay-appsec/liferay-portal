/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.key.internal.secret;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.cache.PortalCache;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.portlet.PortletIdCodec;
import com.liferay.portal.kernel.security.fips.FIPSAuditEvent;
import com.liferay.portal.kernel.security.fips.FIPSAuditUtil;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.settings.CompanyServiceSettingsLocator;
import com.liferay.portal.kernel.settings.GroupServiceSettingsLocator;
import com.liferay.portal.kernel.settings.ParameterMapSettingsLocator;
import com.liferay.portal.kernel.settings.PortletInstanceSettingsLocator;
import com.liferay.portal.kernel.settings.SettingsLocatorHelper;
import com.liferay.portal.kernel.settings.definition.ConfigurationPidMapping;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.security.key.KeyReference;
import com.liferay.portal.security.key.KeyReferenceUtil;
import com.liferay.portal.security.key.internal.profile.configuration.KeyManagerConfiguration;
import com.liferay.portal.security.key.secret.Secret;
import com.liferay.portal.security.key.secret.SecretContext;
import com.liferay.portal.security.key.secret.SecretManager;
import com.liferay.portal.security.key.secret.exception.SecretException;
import com.liferay.portal.security.key.spi.profile.KeyManagerProfile;
import com.liferay.portal.security.key.spi.profile.KeyManagerProfileRegistry;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Collections;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * @author Pedro Victor Silvestre
 */
public class SecretResolverImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);

		ReflectionTestUtil.setFieldValue(
			_secretResolverImpl, "_groupLocalService", _groupLocalService);
		ReflectionTestUtil.setFieldValue(
			_secretResolverImpl, "_keyManagerProfileRegistry",
			_keyManagerProfileRegistry);
		ReflectionTestUtil.setFieldValue(
			_secretResolverImpl, "_portalCache", _portalCache);
		ReflectionTestUtil.setFieldValue(
			_secretResolverImpl, "_secretManager", _secretManager);
		ReflectionTestUtil.setFieldValue(
			_secretResolverImpl, "_settingsLocatorHelper",
			_settingsLocatorHelper);
	}

	@Test
	public void testDelete() throws Exception {
		long companyId = RandomTestUtil.randomLong();
		String key = RandomTestUtil.randomString();

		KeyReference keyReference = new KeyReference(
			StringBundler.concat(
				"preference/portal/", companyId, StringPool.SLASH, key),
			RandomTestUtil.randomString(), KeyReference.Type.SECRET);

		_secretResolverImpl.delete(
			companyId, SecretContext.createCompanySecretContext(companyId, key),
			KeyReferenceUtil.toKeyReferenceString(keyReference));

		Mockito.verify(
			_secretManager
		).deleteSecret(
			companyId, keyReference
		);

		Mockito.clearInvocations(_secretManager);

		_assertDeleteRejects(
			companyId, keyReference,
			SecretContext.createCompanySecretContext(
				RandomTestUtil.randomLong(), key));

		long groupId = RandomTestUtil.randomLong();

		_setUpGroup(companyId, groupId);

		SecretContext secretContext = SecretContext.createGroupSecretContext(
			groupId, key);

		_assertDeleteRejects(companyId, keyReference, secretContext);

		_secretResolverImpl.delete(
			companyId, secretContext, RandomTestUtil.randomString());

		Mockito.verifyNoInteractions(_secretManager);
	}

	@Test
	public void testGetKey() {
		long companyId = RandomTestUtil.randomLong();
		String keyReferenceString = RandomTestUtil.randomString();

		Assert.assertEquals(
			companyId + StringPool.POUND + keyReferenceString,
			SecretResolverImpl.getKey(companyId, keyReferenceString));
	}

	@Test
	public void testResolve() throws Exception {
		_assertResolve(RandomTestUtil.randomString());
		_assertResolve(StringPool.STAR);
	}

	@Test
	public void testResolveWhenKeyReferenceIsCrypto() throws Exception {
		Assert.assertThrows(
			SecretException.class,
			() -> _secretResolverImpl.resolve(
				RandomTestUtil.randomLong(), "${keyRef:provider:identifier}"));

		Mockito.verifyNoInteractions(_secretManager);
	}

	@Test
	public void testResolveWhenKeyReferenceIsInvalid() throws Exception {
		Assert.assertThrows(
			SecretException.class,
			() -> _secretResolverImpl.resolve(
				RandomTestUtil.randomLong(), "${secretRef:provider}"));

		Mockito.verifyNoInteractions(_secretManager);
	}

	@Test
	public void testResolveWhenProviderIsSystem() throws Exception {
		String systemSecretProviderId = RandomTestUtil.randomString();

		Mockito.when(
			_keyManagerProfile.getSystemSecretProviderId()
		).thenReturn(
			systemSecretProviderId
		);

		Mockito.when(
			_keyManagerProfileRegistry.getActiveKeyManagerProfile()
		).thenReturn(
			_keyManagerProfile
		);

		KeyReference keyReference = new KeyReference(
			RandomTestUtil.randomString(), systemSecretProviderId,
			KeyReference.Type.SECRET);
		String value = RandomTestUtil.randomString();

		Mockito.when(
			_secretManager.getSecret(CompanyConstants.SYSTEM, keyReference)
		).thenReturn(
			new Secret(keyReference, value)
		);

		Assert.assertEquals(
			value,
			_secretResolverImpl.resolve(
				RandomTestUtil.randomLong(),
				KeyReferenceUtil.toKeyReferenceString(keyReference)));

		Mockito.verify(
			_secretManager
		).getSecret(
			CompanyConstants.SYSTEM, keyReference
		);
	}

	@Test
	public void testResolveWhenSecretManagerFails() throws Exception {
		long companyId = RandomTestUtil.randomLong();

		KeyReference keyReference = new KeyReference(
			RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			KeyReference.Type.SECRET);
		SecretException secretException = new SecretException();

		Mockito.when(
			_secretManager.getSecret(companyId, keyReference)
		).thenThrow(
			secretException
		);

		Assert.assertSame(
			secretException,
			Assert.assertThrows(
				SecretException.class,
				() -> _secretResolverImpl.resolve(
					companyId,
					KeyReferenceUtil.toKeyReferenceString(keyReference))));
	}

	@Test
	public void testResolveWhenValueIsCached() throws Exception {
		long companyId = RandomTestUtil.randomLong();

		KeyReference keyReference = new KeyReference(
			RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			KeyReference.Type.SECRET);

		String keyReferenceString = KeyReferenceUtil.toKeyReferenceString(
			keyReference);

		String value = RandomTestUtil.randomString();

		Mockito.when(
			_portalCache.get(companyId + StringPool.POUND + keyReferenceString)
		).thenReturn(
			value
		);

		Assert.assertEquals(
			value, _secretResolverImpl.resolve(companyId, keyReferenceString));

		Mockito.verifyNoInteractions(_secretManager);
	}

	@Test
	public void testResolveWhenValueIsNotKeyReference() throws Exception {
		String value = RandomTestUtil.randomString();

		Assert.assertNull(
			_secretResolverImpl.resolve(RandomTestUtil.randomLong(), null));
		Assert.assertSame(
			value,
			_secretResolverImpl.resolve(RandomTestUtil.randomLong(), value));

		Mockito.verifyNoInteractions(_secretManager);
	}

	@Test
	public void testResolveWithIdentifier() throws Exception {
		long companyId = RandomTestUtil.randomLong();

		KeyReference keyReference = new KeyReference(
			RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			KeyReference.Type.SECRET);

		String keyReferenceString = KeyReferenceUtil.toKeyReferenceString(
			keyReference);

		Assert.assertSame(
			keyReferenceString,
			_secretResolverImpl.resolve(
				companyId, RandomTestUtil.randomString(), keyReferenceString));

		String plainValue = RandomTestUtil.randomString();

		Assert.assertSame(
			plainValue,
			_secretResolverImpl.resolve(
				companyId, keyReference.getIdentifier(), plainValue));

		Assert.assertNull(
			_secretResolverImpl.resolve(
				companyId, keyReference.getIdentifier(), null));

		Mockito.verifyNoInteractions(_secretManager);

		String value = RandomTestUtil.randomString();

		Mockito.when(
			_secretManager.getSecret(companyId, keyReference)
		).thenReturn(
			new Secret(keyReference, value)
		);

		Assert.assertEquals(
			value,
			_secretResolverImpl.resolve(
				companyId, keyReference.getIdentifier(), keyReferenceString));
	}

	@Test
	public void testResolveWithSecretContext() throws Exception {
		_testResolveWithCompanySecretContext();
		_testResolveWithConfigurationSecretContext();
		_testResolveWithGroupSecretContext();
		_testResolveWithSettingsSecretContext();
		_testResolveWithSettingsSecretContextWhenConfigurationPidIsMapped();
		_testResolveWithSettingsSecretContextWhenSettingsLocatorIsPortletInstance();
		_testResolveWithSettingsSecretContextWhenSettingsLocatorIsUnsupported();
	}

	@Test
	public void testStore() throws Exception {
		try (AutoCloseable autoCloseable =
				ReflectionTestUtil.setFieldValueWithAutoCloseable(
					PropsValues.class, "FIPS_ENABLED", true)) {

			_testStore();
			_testStoreWhenFIPSIsDisabled();
			_testStoreWhenKeyReferenceIsInvalid();
			_testStoreWhenSecretManagerFails();
			_testStoreWhenValueIsBlank();
			_testStoreWhenValueReferencesAnotherContext();
			_testStoreWhenValueReferencesItsChain();
		}
	}

	private void _assertDeleteRejects(
		long companyId, KeyReference keyReference,
		SecretContext secretContext) {

		try (MockedStatic<FIPSAuditUtil> fipsAuditUtilMockedStatic =
				Mockito.mockStatic(FIPSAuditUtil.class)) {

			Assert.assertThrows(
				SecretException.class,
				() -> _secretResolverImpl.delete(
					companyId, secretContext,
					KeyReferenceUtil.toKeyReferenceString(keyReference)));

			_assertFIPSAuditEvent(
				companyId, fipsAuditUtilMockedStatic,
				keyReference.getIdentifier(), "delete");
		}
	}

	private void _assertFIPSAuditEvent(
		long companyId, MockedStatic<FIPSAuditUtil> fipsAuditUtilMockedStatic,
		String identifier, String operation) {

		ArgumentCaptor<FIPSAuditEvent> argumentCaptor = ArgumentCaptor.forClass(
			FIPSAuditEvent.class);

		fipsAuditUtilMockedStatic.verify(
			() -> FIPSAuditUtil.write(argumentCaptor.capture()));

		FIPSAuditEvent fipsAuditEvent = argumentCaptor.getValue();

		Assert.assertEquals(
			"secret-reference-refused", fipsAuditEvent.getEventType());
		Assert.assertEquals(
			HashMapBuilder.<String, Object>put(
				"company-id", companyId
			).put(
				"identifier", identifier
			).put(
				"operation", operation
			).build(),
			fipsAuditEvent.getFields());
	}

	private void _assertResolve(
			long companyId, String identifier, long scopeCompanyId,
			SecretContext secretContext)
		throws Exception {

		KeyReference keyReference = new KeyReference(
			identifier, RandomTestUtil.randomString(),
			KeyReference.Type.SECRET);
		String value = RandomTestUtil.randomString();

		Mockito.when(
			_secretManager.getSecret(scopeCompanyId, keyReference)
		).thenReturn(
			new Secret(keyReference, value)
		);

		Assert.assertEquals(
			value,
			_secretResolverImpl.resolve(
				companyId, secretContext,
				KeyReferenceUtil.toKeyReferenceString(keyReference)));
	}

	private void _assertResolve(String providerId) throws Exception {
		long companyId = RandomTestUtil.randomLong();

		KeyReference keyReference = new KeyReference(
			RandomTestUtil.randomString(), providerId,
			KeyReference.Type.SECRET);
		String value = RandomTestUtil.randomString();

		Secret secret = new Secret(keyReference, value);

		Mockito.when(
			_secretManager.getSecret(companyId, keyReference)
		).thenReturn(
			secret
		);

		Assert.assertEquals(
			value,
			_secretResolverImpl.resolve(
				companyId,
				KeyReferenceUtil.toKeyReferenceString(keyReference)));
		Assert.assertTrue(secret.isDestroyed());

		Mockito.verify(
			_secretManager
		).getSecret(
			companyId, keyReference
		);

		Mockito.verify(
			_portalCache
		).put(
			companyId + StringPool.POUND +
				KeyReferenceUtil.toKeyReferenceString(keyReference),
			value, 600
		);
	}

	private void _assertResolveRejects(
		long companyId, String identifier, SecretContext secretContext) {

		Mockito.clearInvocations(_secretManager);

		try (MockedStatic<FIPSAuditUtil> fipsAuditUtilMockedStatic =
				Mockito.mockStatic(FIPSAuditUtil.class)) {

			Assert.assertThrows(
				SecretException.class,
				() -> _secretResolverImpl.resolve(
					companyId, secretContext,
					_toKeyReferenceString(identifier)));

			_assertFIPSAuditEvent(
				companyId, fipsAuditUtilMockedStatic, identifier, "resolve");
		}

		Mockito.verifyNoInteractions(_secretManager);
	}

	private void _assertStore(
			long companyId, String identifier, SecretContext secretContext)
		throws Exception {

		Mockito.clearInvocations(_secretManager);

		KeyReference keyReference = new KeyReference(
			RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			KeyReference.Type.SECRET);

		Mockito.when(
			_secretManager.putSecret(Mockito.eq(companyId), Mockito.any())
		).thenReturn(
			keyReference
		);

		Assert.assertEquals(
			KeyReferenceUtil.toKeyReferenceString(keyReference),
			_secretResolverImpl.store(
				companyId, secretContext, RandomTestUtil.randomString()));

		ArgumentCaptor<Secret> argumentCaptor = ArgumentCaptor.forClass(
			Secret.class);

		Mockito.verify(
			_secretManager
		).putSecret(
			Mockito.eq(companyId), argumentCaptor.capture()
		);

		Secret secret = argumentCaptor.getValue();

		KeyReference secretKeyReference = secret.getKeyReference();

		Assert.assertEquals(identifier, secretKeyReference.getIdentifier());
		Assert.assertEquals(
			StringPool.STAR, secretKeyReference.getProviderId());

		Assert.assertTrue(secret.isDestroyed());
	}

	private Group _setUpGroup(long companyId, long groupId) throws Exception {
		Group group = Mockito.mock(Group.class);

		Mockito.when(
			group.getCompanyId()
		).thenReturn(
			companyId
		);

		Mockito.when(
			group.getGroupId()
		).thenReturn(
			groupId
		);

		Mockito.when(
			_groupLocalService.getGroup(groupId)
		).thenReturn(
			group
		);

		return group;
	}

	private void _testResolveWithCompanySecretContext() throws Exception {
		long companyId = RandomTestUtil.randomLong();
		String key = RandomTestUtil.randomString();

		String identifier = StringBundler.concat(
			"preference/portal/", companyId, StringPool.SLASH, key);
		SecretContext secretContext = SecretContext.createCompanySecretContext(
			companyId, key);

		_assertResolve(companyId, identifier, companyId, secretContext);
		_assertResolveRejects(
			RandomTestUtil.randomLong(), identifier, secretContext);

		String otherCompanyIdentifier = StringBundler.concat(
			"preference/portal/", RandomTestUtil.randomLong(), StringPool.SLASH,
			key);

		_assertResolveRejects(companyId, otherCompanyIdentifier, secretContext);
	}

	private void _testResolveWithConfigurationSecretContext() throws Exception {
		long companyId = RandomTestUtil.randomLong();
		String key = RandomTestUtil.randomString();
		String pid = RandomTestUtil.randomString();

		SecretContext secretContext =
			SecretContext.createConfigurationSecretContext(
				key,
				HashMapBuilder.<String, Object>put(
					ConfigurationAdmin.SERVICE_FACTORYPID, pid + ".scoped"
				).put(
					Constants.SERVICE_PID,
					pid + ".scoped~" + RandomTestUtil.randomString()
				).put(
					"companyId", companyId
				).build());

		_assertResolve(
			companyId,
			StringBundler.concat(
				"config/", pid, "/company/", companyId, StringPool.SLASH, key),
			companyId, secretContext);
		_assertResolveRejects(
			companyId, StringBundler.concat("config/", pid, "/system/0/", key),
			secretContext);

		String name = RandomTestUtil.randomString();

		_assertResolve(
			companyId,
			StringBundler.concat(
				"config/", pid, StringPool.SLASH, name, "/company/", companyId,
				StringPool.SLASH, key),
			companyId,
			SecretContext.createConfigurationSecretContext(
				key,
				HashMapBuilder.<String, Object>put(
					ConfigurationAdmin.SERVICE_FACTORYPID, pid
				).put(
					Constants.SERVICE_PID, pid + "~" + name
				).put(
					"companyId", companyId
				).build()));

		long groupId = RandomTestUtil.randomLong();

		_assertResolve(
			companyId,
			StringBundler.concat(
				"config/", pid, "/group/", groupId, StringPool.SLASH, key),
			companyId,
			SecretContext.createConfigurationSecretContext(
				key,
				HashMapBuilder.<String, Object>put(
					ConfigurationAdmin.SERVICE_FACTORYPID, pid + ".scoped"
				).put(
					Constants.SERVICE_PID,
					pid + ".scoped~" + RandomTestUtil.randomString()
				).put(
					"companyId", companyId
				).put(
					"groupId", groupId
				).build()));

		String portletInstanceId = RandomTestUtil.randomString();

		_assertResolve(
			companyId,
			StringBundler.concat(
				"config/", pid, "/portlet-instance/", portletInstanceId,
				StringPool.SLASH, key),
			CompanyConstants.SYSTEM,
			SecretContext.createConfigurationSecretContext(
				key,
				HashMapBuilder.<String, Object>put(
					ConfigurationAdmin.SERVICE_FACTORYPID, pid + ".scoped"
				).put(
					Constants.SERVICE_PID,
					pid + ".scoped~" + RandomTestUtil.randomString()
				).put(
					"portletInstanceId", portletInstanceId
				).build()));

		_assertResolve(
			companyId, StringBundler.concat("config/", pid, "/system/0/", key),
			CompanyConstants.SYSTEM,
			SecretContext.createConfigurationSecretContext(
				key,
				HashMapBuilder.<String, Object>put(
					Constants.SERVICE_PID, pid
				).build()));
	}

	private void _testResolveWithGroupSecretContext() throws Exception {
		long companyId = RandomTestUtil.randomLong();
		long groupId = RandomTestUtil.randomLong();

		Group group = _setUpGroup(companyId, groupId);

		long liveGroupId = RandomTestUtil.randomLong();

		Mockito.when(
			group.getLiveGroupId()
		).thenReturn(
			liveGroupId
		);

		Mockito.when(
			group.isStagingGroup()
		).thenReturn(
			true
		);

		String key = RandomTestUtil.randomString();

		String identifier = StringBundler.concat(
			"typesettings/group/", groupId, StringPool.SLASH, key);
		SecretContext secretContext = SecretContext.createGroupSecretContext(
			groupId, key);

		_assertResolve(companyId, identifier, companyId, secretContext);

		_assertResolve(
			companyId,
			StringBundler.concat(
				"preference/portal/", companyId, StringPool.SLASH, key),
			companyId, secretContext);
		_assertResolve(
			companyId,
			StringBundler.concat(
				"typesettings/group/", liveGroupId, StringPool.SLASH, key),
			companyId, secretContext);
		_assertResolveRejects(
			RandomTestUtil.randomLong(), identifier, secretContext);

		String otherGroupIdentifier = StringBundler.concat(
			"typesettings/group/", RandomTestUtil.randomLong(),
			StringPool.SLASH, key);

		_assertResolveRejects(companyId, otherGroupIdentifier, secretContext);
	}

	private void _testResolveWithSettingsSecretContext() throws Exception {
		long companyId = RandomTestUtil.randomLong();
		long groupId = RandomTestUtil.randomLong();

		_setUpGroup(companyId, groupId);

		String key = RandomTestUtil.randomString();
		String settingsId = RandomTestUtil.randomString();

		String identifier = StringBundler.concat(
			"preference/", settingsId, "/group/", groupId, StringPool.SLASH,
			key);
		SecretContext secretContext = SecretContext.createSettingsSecretContext(
			key, new GroupServiceSettingsLocator(groupId, settingsId));

		_assertResolve(companyId, identifier, companyId, secretContext);

		_assertResolve(
			companyId, "property/" + key, CompanyConstants.SYSTEM,
			secretContext);
		_assertResolve(
			companyId,
			StringBundler.concat(
				"config/", settingsId, "/company/", companyId, StringPool.SLASH,
				key),
			companyId, secretContext);
		_assertResolve(
			companyId,
			StringBundler.concat(
				"config/", settingsId, "/group/", groupId, StringPool.SLASH,
				key),
			companyId, secretContext);
		_assertResolve(
			companyId,
			StringBundler.concat("config/", settingsId, "/system/0/", key),
			CompanyConstants.SYSTEM, secretContext);
		_assertResolve(
			companyId,
			StringBundler.concat(
				"preference/", settingsId, "/company/", companyId,
				StringPool.SLASH, key),
			companyId, secretContext);
		_assertResolve(
			companyId,
			StringBundler.concat(
				"preference/portal/", companyId, StringPool.SLASH, key),
			companyId, secretContext);
		_assertResolveRejects(
			RandomTestUtil.randomLong(), identifier, secretContext);

		String otherConfigurationIdentifier = StringBundler.concat(
			"config/", RandomTestUtil.randomString(), "/system/0/", key);

		_assertResolveRejects(
			companyId, otherConfigurationIdentifier, secretContext);
	}

	private void _testResolveWithSettingsSecretContextWhenConfigurationPidIsMapped()
		throws Exception {

		ConfigurationPidMapping configurationPidMapping = Mockito.mock(
			ConfigurationPidMapping.class);

		Mockito.doReturn(
			KeyManagerConfiguration.class
		).when(
			configurationPidMapping
		).getConfigurationBeanClass();

		String settingsId = RandomTestUtil.randomString();

		Mockito.when(
			_settingsLocatorHelper.getConfigurationPidMapping(settingsId)
		).thenReturn(
			configurationPidMapping
		);

		long companyId = RandomTestUtil.randomLong();
		String key = RandomTestUtil.randomString();

		SecretContext secretContext = SecretContext.createSettingsSecretContext(
			key, new CompanyServiceSettingsLocator(companyId, settingsId));

		_assertResolve(
			companyId,
			StringBundler.concat(
				"config/com.liferay.portal.security.key.internal.profile.",
				"configuration.KeyManagerConfiguration/system/0/", key),
			CompanyConstants.SYSTEM, secretContext);
		_assertResolveRejects(
			companyId,
			StringBundler.concat("config/", settingsId, "/system/0/", key),
			secretContext);
	}

	private void _testResolveWithSettingsSecretContextWhenSettingsLocatorIsPortletInstance()
		throws Exception {

		long companyId = RandomTestUtil.randomLong();
		long groupId = RandomTestUtil.randomLong();

		_setUpGroup(companyId, groupId);

		Layout layout = Mockito.mock(Layout.class);

		Mockito.when(
			layout.getCompanyId()
		).thenReturn(
			companyId
		);

		Mockito.when(
			layout.getGroupId()
		).thenReturn(
			groupId
		);

		long plid = RandomTestUtil.randomLong();

		Mockito.when(
			layout.getPlid()
		).thenReturn(
			plid
		);

		String key = RandomTestUtil.randomString();

		String portletName = RandomTestUtil.randomString();

		String portletId = PortletIdCodec.encode(
			portletName, RandomTestUtil.randomString());

		SecretContext secretContext = SecretContext.createSettingsSecretContext(
			key, new PortletInstanceSettingsLocator(layout, portletId));

		_assertResolve(
			companyId,
			StringBundler.concat(
				"preference/", portletId, "/portlet-instance/0/", plid,
				StringPool.SLASH, key),
			companyId, secretContext);
		_assertResolve(
			companyId,
			StringBundler.concat(
				"config/", portletName, "/portlet-instance/", portletId,
				StringPool.SLASH, key),
			CompanyConstants.SYSTEM, secretContext);
		_assertResolve(
			companyId,
			StringBundler.concat(
				"preference/", portletName, "/group/", groupId,
				StringPool.SLASH, key),
			companyId, secretContext);
		_assertResolve(
			companyId,
			StringBundler.concat(
				"preference/", portletName, "/company/", companyId,
				StringPool.SLASH, key),
			companyId, secretContext);

		String otherPortletIdentifier = StringBundler.concat(
			"preference/", portletId, "/portlet-instance/0/",
			RandomTestUtil.randomLong(), StringPool.SLASH, key);

		_assertResolveRejects(companyId, otherPortletIdentifier, secretContext);
	}

	private void _testResolveWithSettingsSecretContextWhenSettingsLocatorIsUnsupported() {
		long companyId = RandomTestUtil.randomLong();

		SecretContext secretContext = SecretContext.createSettingsSecretContext(
			RandomTestUtil.randomString(),
			new ParameterMapSettingsLocator(
				new HashMap<>(),
				new CompanyServiceSettingsLocator(
					companyId, RandomTestUtil.randomString())));

		Assert.assertThrows(
			IllegalArgumentException.class,
			() -> _secretResolverImpl.resolve(
				companyId, secretContext,
				_toKeyReferenceString(RandomTestUtil.randomString())));
	}

	private void _testStore() throws Exception {
		long companyId = RandomTestUtil.randomLong();
		String key = RandomTestUtil.randomString();

		_assertStore(
			companyId,
			StringBundler.concat(
				"preference/portal/", companyId, StringPool.SLASH, key),
			SecretContext.createCompanySecretContext(companyId, key));

		String pid = RandomTestUtil.randomString();

		_assertStore(
			companyId,
			StringBundler.concat(
				"config/", pid, "/company/", companyId, StringPool.SLASH, key),
			SecretContext.createConfigurationSecretContext(
				key,
				HashMapBuilder.<String, Object>put(
					Constants.SERVICE_PID, pid
				).put(
					"companyId", companyId
				).build()));

		long groupId = RandomTestUtil.randomLong();

		_setUpGroup(companyId, groupId);

		_assertStore(
			companyId,
			StringBundler.concat(
				"typesettings/group/", groupId, StringPool.SLASH, key),
			SecretContext.createGroupSecretContext(groupId, key));

		String settingsId = RandomTestUtil.randomString();

		_assertStore(
			companyId,
			StringBundler.concat(
				"preference/", settingsId, "/group/", groupId, StringPool.SLASH,
				key),
			SecretContext.createSettingsSecretContext(
				key, new GroupServiceSettingsLocator(groupId, settingsId)));
	}

	private void _testStoreWhenFIPSIsDisabled() throws Exception {
		Mockito.clearInvocations(_secretManager);

		try (AutoCloseable autoCloseable =
				ReflectionTestUtil.setFieldValueWithAutoCloseable(
					PropsValues.class, "FIPS_ENABLED", false)) {

			String value = RandomTestUtil.randomString();

			Assert.assertEquals(
				value,
				_secretResolverImpl.store(
					RandomTestUtil.randomLong(),
					SecretContext.createConfigurationSecretContext(
						RandomTestUtil.randomString(), Collections.emptyMap()),
					value));
		}

		Mockito.verifyNoInteractions(_secretManager);
	}

	private void _testStoreWhenKeyReferenceIsInvalid() {
		Mockito.clearInvocations(_secretManager);

		long companyId = RandomTestUtil.randomLong();

		SecretContext secretContext = SecretContext.createCompanySecretContext(
			companyId, RandomTestUtil.randomString());

		Assert.assertThrows(
			SecretException.class,
			() -> _secretResolverImpl.store(
				companyId, secretContext, "${secretRef:provider}"));

		Mockito.verifyNoInteractions(_secretManager);
	}

	private void _testStoreWhenSecretManagerFails() throws Exception {
		long companyId = RandomTestUtil.randomLong();
		SecretException secretException = new SecretException();

		Mockito.when(
			_secretManager.putSecret(Mockito.eq(companyId), Mockito.any())
		).thenThrow(
			secretException
		);

		SecretContext secretContext = SecretContext.createCompanySecretContext(
			companyId, RandomTestUtil.randomString());

		Assert.assertSame(
			secretException,
			Assert.assertThrows(
				SecretException.class,
				() -> _secretResolverImpl.store(
					companyId, secretContext, RandomTestUtil.randomString())));
	}

	private void _testStoreWhenValueIsBlank() {
		Mockito.clearInvocations(_secretManager);

		long companyId = RandomTestUtil.randomLong();

		Assert.assertEquals(
			StringPool.BLANK,
			_secretResolverImpl.store(
				companyId,
				SecretContext.createCompanySecretContext(
					companyId, RandomTestUtil.randomString()),
				StringPool.BLANK));

		Mockito.verifyNoInteractions(_secretManager);
	}

	private void _testStoreWhenValueReferencesAnotherContext() {
		Mockito.clearInvocations(_secretManager);

		try (MockedStatic<FIPSAuditUtil> fipsAuditUtilMockedStatic =
				Mockito.mockStatic(FIPSAuditUtil.class)) {

			long companyId = RandomTestUtil.randomLong();
			String key = RandomTestUtil.randomString();
			String pid = RandomTestUtil.randomString();

			String identifier = StringBundler.concat(
				"config/", pid, "/company/", RandomTestUtil.randomLong(),
				StringPool.SLASH, key);
			SecretContext secretContext =
				SecretContext.createConfigurationSecretContext(
					key,
					HashMapBuilder.<String, Object>put(
						Constants.SERVICE_PID, pid
					).put(
						"companyId", companyId
					).build());

			Assert.assertThrows(
				SecretException.class,
				() -> _secretResolverImpl.store(
					companyId, secretContext,
					_toKeyReferenceString(identifier)));

			_assertFIPSAuditEvent(
				companyId, fipsAuditUtilMockedStatic, identifier, "store");
		}

		Mockito.verifyNoInteractions(_secretManager);
	}

	private void _testStoreWhenValueReferencesItsChain() throws Exception {
		Mockito.clearInvocations(_secretManager);

		long companyId = RandomTestUtil.randomLong();
		long groupId = RandomTestUtil.randomLong();

		_setUpGroup(companyId, groupId);

		String key = RandomTestUtil.randomString();

		String value = _toKeyReferenceString(
			StringBundler.concat(
				"preference/portal/", companyId, StringPool.SLASH, key));

		Assert.assertEquals(
			value,
			_secretResolverImpl.store(
				companyId, SecretContext.createGroupSecretContext(groupId, key),
				value));

		Mockito.verifyNoInteractions(_secretManager);
	}

	private String _toKeyReferenceString(String identifier) {
		return KeyReferenceUtil.toKeyReferenceString(
			new KeyReference(
				identifier, RandomTestUtil.randomString(),
				KeyReference.Type.SECRET));
	}

	@Mock
	private GroupLocalService _groupLocalService;

	@Mock
	private KeyManagerProfile _keyManagerProfile;

	@Mock
	private KeyManagerProfileRegistry _keyManagerProfileRegistry;

	@Mock
	private PortalCache<String, String> _portalCache;

	@Mock
	private SecretManager _secretManager;

	private final SecretResolverImpl _secretResolverImpl =
		new SecretResolverImpl();

	@Mock
	private SettingsLocatorHelper _settingsLocatorHelper;

}