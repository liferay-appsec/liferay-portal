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
	public void testGetKey() {
		long companyId = RandomTestUtil.randomLong();
		String keyReferenceString = RandomTestUtil.randomString();

		Assert.assertEquals(
			companyId + StringPool.POUND + keyReferenceString,
			SecretResolverImpl.getKey(companyId, keyReferenceString));
	}

	@Test
	public void testResolve() throws Exception {
		_testResolve();
		_testResolveWhenKeyReferenceIsCrypto();
		_testResolveWhenKeyReferenceIsInvalid();
		_testResolveWhenProviderIsSystem();
		_testResolveWhenSecretManagerFails();
		_testResolveWhenValueIsCached();
		_testResolveWhenValueIsNotKeyReference();
	}

	@Test
	public void testResolveWithIdentifier() throws Exception {
		_testResolveWithIdentifier();
		_testResolveWithIdentifierWhenValueIsNotKeyReference();
		_testResolveWithIdentifierWhenValueReferencesAnotherIdentifier();
	}

	@Test
	public void testResolveWithSecretContext() throws Exception {
		_testResolveWithSecretContext();
		_testResolveWithSecretContextUnderAnotherCompany();
		_testResolveWithSecretContextWhenConfigurationPidIsMapped();
		_testResolveWithSecretContextWhenGroupIsStaged();
		_testResolveWithSecretContextWhenGroupIsStagingGroup();
		_testResolveWithSecretContextWhenPortletIsEmbedded();
		_testResolveWithSecretContextWhenPortletIsUserPortlet();
		_testResolveWithSecretContextWhenSettingsLocatorIsGroup();
		_testResolveWithSecretContextWhenSettingsLocatorIsPortletInstance();
		_testResolveWithSecretContextWhenSettingsLocatorIsUnsupported();
		_testResolveWithSecretContextWhenValueReferencesAnotherContext();
	}

	@Test
	public void testStore() throws Exception {
		try (AutoCloseable autoCloseable =
				ReflectionTestUtil.setFieldValueWithAutoCloseable(
					PropsValues.class, "FIPS_ENABLED", true)) {

			_testStore();

			Mockito.clearInvocations(_secretManager);

			_testStoreWhenFIPSIsDisabled();
			_testStoreWhenIdentifierNamespaceIsUnsupported();
			_testStoreWhenKeyReferenceIsInvalid();
			_testStoreWhenSecretManagerFails();

			Mockito.reset(_secretManager);

			_testStoreWhenValueIsBlank();
			_testStoreWhenValueReferencesAnotherKey();
			_testStoreWhenValueReferencesAnotherNamespace();
			_testStoreWhenValueReferencesSameKeyInAnotherScope();
			_testStoreWhenValueReferencesSameSlot();
		}
	}

	@Test
	public void testStoreWithSecretContext() throws Exception {
		try (AutoCloseable autoCloseable =
				ReflectionTestUtil.setFieldValueWithAutoCloseable(
					PropsValues.class, "FIPS_ENABLED", true)) {

			_testStoreWithSecretContext();
			_testStoreWithSecretContextWhenFIPSIsDisabled();
			_testStoreWithSecretContextWhenValueReferencesAnotherContext();
			_testStoreWithSecretContextWhenValueReferencesItsChain();
			_testStoreWithSecretContextWhenValueReferencesSameContext();
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

		String keyReferenceString = KeyReferenceUtil.toKeyReferenceString(
			keyReference);

		Assert.assertEquals(
			value,
			_secretResolverImpl.resolve(
				companyId, secretContext, keyReferenceString));

		Mockito.verify(
			_portalCache
		).put(
			scopeCompanyId + StringPool.POUND + keyReferenceString, value, 600
		);
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

	private void _assertResolveFails(
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

	private void _assertStore(String identifier) throws Exception {
		long companyId = RandomTestUtil.randomLong();

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
				companyId, identifier, RandomTestUtil.randomString()));

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

	private void _assertStoreFails(
		String identifier, String keyReferenceIdentifier) {

		Assert.assertThrows(
			SecretException.class,
			() -> _secretResolverImpl.store(
				RandomTestUtil.randomLong(), identifier,
				_toKeyReferenceString(keyReferenceIdentifier)));

		Mockito.verifyNoInteractions(_secretManager);
	}

	private void _assertStoreKeepsValue(
		String identifier, String keyReferenceIdentifier) {

		String value = _toKeyReferenceString(keyReferenceIdentifier);

		Assert.assertEquals(
			value,
			_secretResolverImpl.store(
				RandomTestUtil.randomLong(), identifier, value));

		Mockito.verifyNoInteractions(_secretManager);
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

	private Layout _setUpLayout(long companyId, long groupId, long plid) {
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

		Mockito.when(
			layout.getPlid()
		).thenReturn(
			plid
		);

		return layout;
	}

	private void _testResolve() throws Exception {
		_assertResolve(RandomTestUtil.randomString());
		_assertResolve(StringPool.STAR);
	}

	private void _testResolveWhenKeyReferenceIsCrypto() {
		Mockito.clearInvocations(_secretManager);

		Assert.assertThrows(
			SecretException.class,
			() -> _secretResolverImpl.resolve(
				RandomTestUtil.randomLong(), "${keyRef:provider:identifier}"));

		Mockito.verifyNoInteractions(_secretManager);
	}

	private void _testResolveWhenKeyReferenceIsInvalid() {
		Mockito.clearInvocations(_secretManager);

		Assert.assertThrows(
			SecretException.class,
			() -> _secretResolverImpl.resolve(
				RandomTestUtil.randomLong(), "${secretRef:provider}"));

		Mockito.verifyNoInteractions(_secretManager);
	}

	private void _testResolveWhenProviderIsSystem() throws Exception {
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

	private void _testResolveWhenSecretManagerFails() throws Exception {
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

	private void _testResolveWhenValueIsCached() {
		Mockito.clearInvocations(_secretManager);

		long companyId = RandomTestUtil.randomLong();
		String keyReferenceString = _toKeyReferenceString(
			RandomTestUtil.randomString());

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

	private void _testResolveWhenValueIsNotKeyReference() {
		Mockito.clearInvocations(_secretManager);

		Assert.assertNull(
			_secretResolverImpl.resolve(RandomTestUtil.randomLong(), null));

		String value = RandomTestUtil.randomString();

		Assert.assertSame(
			value,
			_secretResolverImpl.resolve(RandomTestUtil.randomLong(), value));

		Mockito.verifyNoInteractions(_secretManager);
	}

	private void _testResolveWithIdentifier() throws Exception {
		long companyId = RandomTestUtil.randomLong();

		KeyReference keyReference = new KeyReference(
			RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			KeyReference.Type.SECRET);
		String value = RandomTestUtil.randomString();

		Mockito.when(
			_secretManager.getSecret(companyId, keyReference)
		).thenReturn(
			new Secret(keyReference, value)
		);

		Assert.assertEquals(
			value,
			_secretResolverImpl.resolve(
				companyId, keyReference.getIdentifier(),
				KeyReferenceUtil.toKeyReferenceString(keyReference)));
	}

	private void _testResolveWithIdentifierWhenValueIsNotKeyReference() {
		Mockito.clearInvocations(_secretManager);

		Assert.assertNull(
			_secretResolverImpl.resolve(
				RandomTestUtil.randomLong(), RandomTestUtil.randomString(),
				null));

		String value = RandomTestUtil.randomString();

		Assert.assertSame(
			value,
			_secretResolverImpl.resolve(
				RandomTestUtil.randomLong(), RandomTestUtil.randomString(),
				value));

		Mockito.verifyNoInteractions(_secretManager);
	}

	private void _testResolveWithIdentifierWhenValueReferencesAnotherIdentifier() {
		Mockito.clearInvocations(_secretManager);

		String value = _toKeyReferenceString(RandomTestUtil.randomString());

		Assert.assertSame(
			value,
			_secretResolverImpl.resolve(
				RandomTestUtil.randomLong(), RandomTestUtil.randomString(),
				value));

		Mockito.verifyNoInteractions(_secretManager);
	}

	private void _testResolveWithSecretContext() throws Exception {
		long companyId = RandomTestUtil.randomLong();
		String key = RandomTestUtil.randomString();
		String pid = RandomTestUtil.randomString();

		_assertResolve(
			companyId,
			StringBundler.concat(
				"config/", pid, "/company/", companyId, StringPool.SLASH, key),
			companyId,
			SecretContext.fromConfiguration(
				key,
				HashMapBuilder.<String, Object>put(
					ConfigurationAdmin.SERVICE_FACTORYPID, pid + ".scoped"
				).put(
					Constants.SERVICE_PID,
					pid + ".scoped~" + RandomTestUtil.randomString()
				).put(
					"companyId", companyId
				).build()));

		String name = RandomTestUtil.randomString();

		_assertResolve(
			companyId,
			StringBundler.concat(
				"config/", pid, StringPool.SLASH, name, "/company/", companyId,
				StringPool.SLASH, key),
			companyId,
			SecretContext.fromConfiguration(
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
			SecretContext.fromConfiguration(
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
			SecretContext.fromConfiguration(
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
			SecretContext.fromConfiguration(
				key,
				HashMapBuilder.<String, Object>put(
					Constants.SERVICE_PID, pid
				).build()));
	}

	private void _testResolveWithSecretContextUnderAnotherCompany() {
		long companyId = RandomTestUtil.randomLong();
		String key = RandomTestUtil.randomString();
		String pid = RandomTestUtil.randomString();

		_assertResolveFails(
			RandomTestUtil.randomLong(),
			StringBundler.concat(
				"config/", pid, "/company/", companyId, StringPool.SLASH, key),
			SecretContext.fromConfiguration(
				key,
				HashMapBuilder.<String, Object>put(
					Constants.SERVICE_PID, pid
				).put(
					"companyId", companyId
				).build()));
	}

	private void _testResolveWithSecretContextWhenConfigurationPidIsMapped()
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

		SecretContext secretContext = SecretContext.fromSettings(
			key, new CompanyServiceSettingsLocator(companyId, settingsId));

		_assertResolve(
			companyId,
			StringBundler.concat(
				"config/", KeyManagerConfiguration.class.getName(),
				"/system/0/", key),
			CompanyConstants.SYSTEM, secretContext);
		_assertResolveFails(
			companyId,
			StringBundler.concat("config/", settingsId, "/system/0/", key),
			secretContext);
	}

	private void _testResolveWithSecretContextWhenGroupIsStaged()
		throws Exception {

		long companyId = RandomTestUtil.randomLong();
		long groupId = RandomTestUtil.randomLong();

		Group group = _setUpGroup(companyId, groupId);

		Group stagingGroup = Mockito.mock(Group.class);

		long stagingGroupId = RandomTestUtil.randomLong();

		Mockito.when(
			stagingGroup.getGroupId()
		).thenReturn(
			stagingGroupId
		);

		Mockito.when(
			group.getStagingGroup()
		).thenReturn(
			stagingGroup
		);

		String key = RandomTestUtil.randomString();
		String settingsId = RandomTestUtil.randomString();

		_assertResolve(
			companyId,
			StringBundler.concat(
				"preference/", settingsId, "/group/", stagingGroupId,
				StringPool.SLASH, key),
			companyId,
			SecretContext.fromSettings(
				key, new GroupServiceSettingsLocator(groupId, settingsId)));
	}

	private void _testResolveWithSecretContextWhenGroupIsStagingGroup()
		throws Exception {

		long companyId = RandomTestUtil.randomLong();
		long groupId = RandomTestUtil.randomLong();

		Group group = _setUpGroup(companyId, groupId);

		Mockito.when(
			group.isStagingGroup()
		).thenReturn(
			true
		);

		long liveGroupId = RandomTestUtil.randomLong();

		Mockito.when(
			group.getLiveGroupId()
		).thenReturn(
			liveGroupId
		);

		String key = RandomTestUtil.randomString();
		String settingsId = RandomTestUtil.randomString();

		SecretContext secretContext = SecretContext.fromSettings(
			key, new GroupServiceSettingsLocator(groupId, settingsId));

		_assertResolve(
			companyId,
			StringBundler.concat(
				"config/", settingsId, "/group/", liveGroupId, StringPool.SLASH,
				key),
			companyId, secretContext);
		_assertResolve(
			companyId,
			StringBundler.concat(
				"preference/", settingsId, "/group/", liveGroupId,
				StringPool.SLASH, key),
			companyId, secretContext);
	}

	private void _testResolveWithSecretContextWhenPortletIsEmbedded()
		throws Exception {

		long companyId = RandomTestUtil.randomLong();
		long groupId = RandomTestUtil.randomLong();

		_setUpGroup(companyId, groupId);

		Layout layout = _setUpLayout(
			companyId, groupId, RandomTestUtil.randomLong());

		Mockito.when(
			layout.isSupportsEmbeddedPortlets()
		).thenReturn(
			true
		);

		String portletId = PortletIdCodec.encode(
			RandomTestUtil.randomString(), RandomTestUtil.randomString());

		Mockito.when(
			layout.isPortletEmbedded(portletId, groupId)
		).thenReturn(
			true
		);

		String key = RandomTestUtil.randomString();

		_assertResolve(
			companyId,
			StringBundler.concat(
				"preference/", portletId, "/portlet-instance/", groupId, "/0/",
				key),
			companyId,
			SecretContext.fromSettings(
				key, new PortletInstanceSettingsLocator(layout, portletId)));
	}

	private void _testResolveWithSecretContextWhenPortletIsUserPortlet()
		throws Exception {

		long companyId = RandomTestUtil.randomLong();
		long groupId = RandomTestUtil.randomLong();

		_setUpGroup(companyId, groupId);

		long plid = RandomTestUtil.randomLong();

		long userId = RandomTestUtil.randomLong();

		String portletId = PortletIdCodec.encode(
			RandomTestUtil.randomString(), userId,
			RandomTestUtil.randomString());

		String key = RandomTestUtil.randomString();

		_assertResolve(
			companyId,
			StringBundler.concat(
				"preference/", portletId, "/portlet-instance/", userId,
				StringPool.SLASH, plid, StringPool.SLASH, key),
			companyId,
			SecretContext.fromSettings(
				key,
				new PortletInstanceSettingsLocator(
					_setUpLayout(companyId, groupId, plid), portletId)));
	}

	private void _testResolveWithSecretContextWhenSettingsLocatorIsGroup()
		throws Exception {

		long companyId = RandomTestUtil.randomLong();
		long groupId = RandomTestUtil.randomLong();

		_setUpGroup(companyId, groupId);

		String key = RandomTestUtil.randomString();
		String settingsId = RandomTestUtil.randomString();

		SecretContext secretContext = SecretContext.fromSettings(
			key, new GroupServiceSettingsLocator(groupId, settingsId));

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

		String identifier = StringBundler.concat(
			"preference/", settingsId, "/group/", groupId, StringPool.SLASH,
			key);

		_assertResolve(companyId, identifier, companyId, secretContext);

		_assertResolveFails(
			RandomTestUtil.randomLong(), identifier, secretContext);

		_assertResolveFails(
			companyId,
			StringBundler.concat(
				"config/", RandomTestUtil.randomString(), "/system/0/", key),
			secretContext);
	}

	private void _testResolveWithSecretContextWhenSettingsLocatorIsPortletInstance()
		throws Exception {

		long companyId = RandomTestUtil.randomLong();
		long groupId = RandomTestUtil.randomLong();

		_setUpGroup(companyId, groupId);

		long plid = RandomTestUtil.randomLong();

		Layout layout = _setUpLayout(companyId, groupId, plid);

		String portletName = RandomTestUtil.randomString();

		String portletId = PortletIdCodec.encode(
			portletName, RandomTestUtil.randomString());

		String key = RandomTestUtil.randomString();

		SecretContext secretContext = SecretContext.fromSettings(
			key, new PortletInstanceSettingsLocator(layout, portletId));

		_assertResolve(
			companyId,
			StringBundler.concat(
				"config/", portletName, "/portlet-instance/", portletId,
				StringPool.SLASH, key),
			CompanyConstants.SYSTEM, secretContext);
		_assertResolve(
			companyId,
			StringBundler.concat(
				"preference/", portletId, "/portlet-instance/0/", plid,
				StringPool.SLASH, key),
			companyId, secretContext);
		_assertResolve(
			companyId,
			StringBundler.concat(
				"preference/", portletName, "/company/", companyId,
				StringPool.SLASH, key),
			companyId, secretContext);
		_assertResolve(
			companyId,
			StringBundler.concat(
				"preference/", portletName, "/group/", groupId,
				StringPool.SLASH, key),
			companyId, secretContext);
		_assertResolveFails(
			companyId,
			StringBundler.concat(
				"preference/", portletId, "/portlet-instance/0/",
				RandomTestUtil.randomLong(), StringPool.SLASH, key),
			secretContext);
	}

	private void _testResolveWithSecretContextWhenSettingsLocatorIsUnsupported() {
		long companyId = RandomTestUtil.randomLong();

		SecretContext secretContext = SecretContext.fromSettings(
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

	private void _testResolveWithSecretContextWhenValueReferencesAnotherContext() {
		long companyId = RandomTestUtil.randomLong();
		String key = RandomTestUtil.randomString();
		String pid = RandomTestUtil.randomString();

		_assertResolveFails(
			companyId, StringBundler.concat("config/", pid, "/system/0/", key),
			SecretContext.fromConfiguration(
				key,
				HashMapBuilder.<String, Object>put(
					Constants.SERVICE_PID, pid
				).put(
					"companyId", companyId
				).build()));
	}

	private void _testStore() throws Exception {
		_assertStore(
			StringBundler.concat(
				"config/", RandomTestUtil.randomString(), StringPool.SLASH,
				RandomTestUtil.randomString()));
		_assertStore(
			StringBundler.concat(
				"preference/", RandomTestUtil.randomString(), StringPool.SLASH,
				RandomTestUtil.randomString()));
	}

	private void _testStoreWhenFIPSIsDisabled() throws Exception {
		try (AutoCloseable autoCloseable =
				ReflectionTestUtil.setFieldValueWithAutoCloseable(
					PropsValues.class, "FIPS_ENABLED", false)) {

			String value = RandomTestUtil.randomString();

			Assert.assertEquals(
				value,
				_secretResolverImpl.store(
					RandomTestUtil.randomLong(), RandomTestUtil.randomString(),
					value));

			Mockito.verifyNoInteractions(_secretManager);
		}
	}

	private void _testStoreWhenIdentifierNamespaceIsUnsupported() {
		Assert.assertThrows(
			IllegalArgumentException.class,
			() -> _secretResolverImpl.store(
				RandomTestUtil.randomLong(),
				StringBundler.concat(
					RandomTestUtil.randomString(), StringPool.SLASH,
					RandomTestUtil.randomString()),
				RandomTestUtil.randomString()));

		Mockito.verifyNoInteractions(_secretManager);
	}

	private void _testStoreWhenKeyReferenceIsInvalid() {
		Assert.assertThrows(
			SecretException.class,
			() -> _secretResolverImpl.store(
				RandomTestUtil.randomLong(),
				StringBundler.concat(
					"config/", RandomTestUtil.randomString(), StringPool.SLASH,
					RandomTestUtil.randomString()),
				"${secretRef:provider}"));

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

		Assert.assertSame(
			secretException,
			Assert.assertThrows(
				SecretException.class,
				() -> _secretResolverImpl.store(
					companyId,
					StringBundler.concat(
						"config/", RandomTestUtil.randomString(),
						StringPool.SLASH, RandomTestUtil.randomString()),
					RandomTestUtil.randomString())));
	}

	private void _testStoreWhenValueIsBlank() {
		Assert.assertEquals(
			StringPool.BLANK,
			_secretResolverImpl.store(
				RandomTestUtil.randomLong(), RandomTestUtil.randomString(),
				StringPool.BLANK));

		Mockito.verifyNoInteractions(_secretManager);
	}

	private void _testStoreWhenValueReferencesAnotherKey() {
		String scope = RandomTestUtil.randomString();

		_assertStoreFails(
			StringBundler.concat(
				"config/", scope, StringPool.SLASH,
				RandomTestUtil.randomString()),
			StringBundler.concat(
				"config/", scope, StringPool.SLASH,
				RandomTestUtil.randomString()));
		_assertStoreFails(
			StringBundler.concat(
				"preference/", scope, StringPool.SLASH,
				RandomTestUtil.randomString()),
			StringBundler.concat(
				"preference/", scope, StringPool.SLASH,
				RandomTestUtil.randomString()));
	}

	private void _testStoreWhenValueReferencesAnotherNamespace() {
		String key = RandomTestUtil.randomString();
		String scope = RandomTestUtil.randomString();

		_assertStoreFails(
			StringBundler.concat("config/", scope, StringPool.SLASH, key),
			StringBundler.concat("preference/", scope, StringPool.SLASH, key));
		_assertStoreFails(
			StringBundler.concat("preference/", scope, StringPool.SLASH, key),
			StringBundler.concat("config/", scope, StringPool.SLASH, key));
	}

	private void _testStoreWhenValueReferencesSameKeyInAnotherScope() {
		String key = RandomTestUtil.randomString();

		_assertStoreFails(
			StringBundler.concat(
				"config/", RandomTestUtil.randomString(), StringPool.SLASH,
				key),
			StringBundler.concat(
				"config/", RandomTestUtil.randomString(), StringPool.SLASH,
				key));
		_assertStoreKeepsValue(
			StringBundler.concat(
				"preference/", RandomTestUtil.randomString(), StringPool.SLASH,
				key),
			StringBundler.concat(
				"preference/", RandomTestUtil.randomString(), StringPool.SLASH,
				key));
	}

	private void _testStoreWhenValueReferencesSameSlot() {
		String configurationIdentifier = StringBundler.concat(
			"config/", RandomTestUtil.randomString(), StringPool.SLASH,
			RandomTestUtil.randomString());

		_assertStoreKeepsValue(
			configurationIdentifier, configurationIdentifier);

		String preferenceIdentifier = StringBundler.concat(
			"preference/", RandomTestUtil.randomString(), StringPool.SLASH,
			RandomTestUtil.randomString());

		_assertStoreKeepsValue(preferenceIdentifier, preferenceIdentifier);
	}

	private void _testStoreWithSecretContext() throws Exception {
		long companyId = RandomTestUtil.randomLong();
		String key = RandomTestUtil.randomString();
		String pid = RandomTestUtil.randomString();

		_assertStore(
			companyId,
			StringBundler.concat(
				"config/", pid, "/company/", companyId, StringPool.SLASH, key),
			SecretContext.fromConfiguration(
				key,
				HashMapBuilder.<String, Object>put(
					Constants.SERVICE_PID, pid
				).put(
					"companyId", companyId
				).build()));

		long groupId = RandomTestUtil.randomLong();

		_setUpGroup(companyId, groupId);

		String settingsId = RandomTestUtil.randomString();

		_assertStore(
			companyId,
			StringBundler.concat(
				"preference/", settingsId, "/group/", groupId, StringPool.SLASH,
				key),
			SecretContext.fromSettings(
				key, new GroupServiceSettingsLocator(groupId, settingsId)));
	}

	private void _testStoreWithSecretContextWhenFIPSIsDisabled()
		throws Exception {

		Mockito.clearInvocations(_secretManager);

		try (AutoCloseable autoCloseable =
				ReflectionTestUtil.setFieldValueWithAutoCloseable(
					PropsValues.class, "FIPS_ENABLED", false)) {

			String value = RandomTestUtil.randomString();

			Assert.assertEquals(
				value,
				_secretResolverImpl.store(
					RandomTestUtil.randomLong(),
					SecretContext.fromConfiguration(
						RandomTestUtil.randomString(), Collections.emptyMap()),
					value));
		}

		Mockito.verifyNoInteractions(_secretManager);
	}

	private void _testStoreWithSecretContextWhenValueReferencesAnotherContext() {
		Mockito.clearInvocations(_secretManager);

		try (MockedStatic<FIPSAuditUtil> fipsAuditUtilMockedStatic =
				Mockito.mockStatic(FIPSAuditUtil.class)) {

			long companyId = RandomTestUtil.randomLong();
			String key = RandomTestUtil.randomString();
			String pid = RandomTestUtil.randomString();

			SecretContext secretContext = SecretContext.fromConfiguration(
				key,
				HashMapBuilder.<String, Object>put(
					Constants.SERVICE_PID, pid
				).put(
					"companyId", companyId
				).build());

			String identifier = StringBundler.concat(
				"config/", pid, "/company/", RandomTestUtil.randomLong(),
				StringPool.SLASH, key);

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

	private void _testStoreWithSecretContextWhenValueReferencesItsChain() {
		Mockito.clearInvocations(_secretManager);

		long companyId = RandomTestUtil.randomLong();

		String key = RandomTestUtil.randomString();
		String settingsId = RandomTestUtil.randomString();

		String value = _toKeyReferenceString(
			StringBundler.concat("config/", settingsId, "/system/0/", key));

		Assert.assertEquals(
			value,
			_secretResolverImpl.store(
				companyId,
				SecretContext.fromSettings(
					key,
					new CompanyServiceSettingsLocator(companyId, settingsId)),
				value));

		Mockito.verifyNoInteractions(_secretManager);
	}

	private void _testStoreWithSecretContextWhenValueReferencesSameContext() {
		Mockito.clearInvocations(_secretManager);

		long companyId = RandomTestUtil.randomLong();
		String key = RandomTestUtil.randomString();
		String pid = RandomTestUtil.randomString();

		String value = _toKeyReferenceString(
			StringBundler.concat(
				"config/", pid, "/company/", companyId, StringPool.SLASH, key));

		Assert.assertEquals(
			value,
			_secretResolverImpl.store(
				companyId,
				SecretContext.fromConfiguration(
					key,
					HashMapBuilder.<String, Object>put(
						Constants.SERVICE_PID, pid
					).put(
						"companyId", companyId
					).build()),
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