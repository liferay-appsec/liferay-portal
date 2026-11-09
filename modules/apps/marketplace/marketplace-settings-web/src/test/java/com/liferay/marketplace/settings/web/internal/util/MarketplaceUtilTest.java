/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.marketplace.settings.web.internal.util;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.security.fips.FIPSAuditEvent;
import com.liferay.portal.kernel.security.fips.FIPSAuditUtil;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.PrefsProps;
import com.liferay.portal.kernel.util.PrefsPropsUtil;
import com.liferay.portal.security.key.KeyReference;
import com.liferay.portal.security.key.KeyReferenceUtil;
import com.liferay.portal.security.key.secret.SecretContext;
import com.liferay.portal.security.key.secret.SecretResolver;
import com.liferay.portal.security.key.secret.exception.SecretException;
import com.liferay.portal.test.log.LogCapture;
import com.liferay.portal.test.log.LogEntry;
import com.liferay.portal.test.log.LoggerTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Caio Farias
 */
public class MarketplaceUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		_prefsProps = ReflectionTestUtil.getAndSetFieldValue(
			PrefsPropsUtil.class, "_prefsProps", _mockPrefsProps);
		_secretResolverSnapshot = ReflectionTestUtil.getAndSetFieldValue(
			MarketplaceUtil.class, "_secretResolverSnapshot",
			new Snapshot<SecretResolver>(
				MarketplaceUtil.class, SecretResolver.class) {

				@Override
				public SecretResolver get() {
					return _secretResolver;
				}

			});
	}

	@After
	public void tearDown() {
		ReflectionTestUtil.setFieldValue(
			MarketplaceUtil.class, "_secretResolverSnapshot",
			_secretResolverSnapshot);
		ReflectionTestUtil.setFieldValue(
			PrefsPropsUtil.class, "_prefsProps", _prefsProps);
	}

	@Test
	public void testDeleteTokens() throws Exception {
		long companyId = RandomTestUtil.randomLong();

		String accessToken = KeyReferenceUtil.toKeyReferenceString(
			_getKeyReference());

		_mockPreference(companyId, "marketplaceAccessToken", accessToken);

		_mockPreference(
			companyId, "marketplaceRefreshToken",
			RandomTestUtil.randomString());

		MarketplaceUtil.deleteTokens(companyId);

		_verifyDelete(companyId, "marketplaceAccessToken", accessToken);

		Mockito.verifyNoMoreInteractions(_secretResolver);

		Mockito.clearInvocations(_secretResolver);

		String refreshToken = KeyReferenceUtil.toKeyReferenceString(
			_getKeyReference());

		_mockPreference(companyId, "marketplaceRefreshToken", refreshToken);

		MarketplaceUtil.deleteTokens(companyId);

		_verifyDelete(companyId, "marketplaceAccessToken", accessToken);
		_verifyDelete(companyId, "marketplaceRefreshToken", refreshToken);

		Mockito.clearInvocations(_secretResolver);

		Mockito.doAnswer(
			invocationOnMock -> {
				throw new SecretException();
			}
		).when(
			_secretResolver
		).delete(
			Mockito.eq(companyId),
			Mockito.argThat(
				(SecretContext secretContext) -> _isCompanySecretContext(
					companyId, "marketplaceAccessToken", secretContext)),
			Mockito.eq(accessToken)
		);

		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				MarketplaceUtil.class.getName(), LoggerTestUtil.ERROR);
			MockedStatic<FIPSAuditUtil> fipsAuditUtilMockedStatic =
				Mockito.mockStatic(FIPSAuditUtil.class)) {

			MarketplaceUtil.deleteTokens(companyId);

			ArgumentCaptor<FIPSAuditEvent> argumentCaptor =
				ArgumentCaptor.forClass(FIPSAuditEvent.class);

			fipsAuditUtilMockedStatic.verify(
				() -> FIPSAuditUtil.write(argumentCaptor.capture()));

			FIPSAuditEvent fipsAuditEvent = argumentCaptor.getValue();

			Assert.assertEquals(
				"marketplace-token-deletion-failure",
				fipsAuditEvent.getEventType());

			Map<String, Object> fields = fipsAuditEvent.getFields();

			Assert.assertEquals(companyId, fields.get("company-id"));
			Assert.assertEquals(
				"marketplaceAccessToken", fields.get("preference-name"));

			List<LogEntry> logEntries = logCapture.getLogEntries();

			Assert.assertEquals(logEntries.toString(), 1, logEntries.size());

			LogEntry logEntry = logEntries.get(0);

			Assert.assertEquals(
				"Unable to delete the stored Marketplace token " +
					"\"marketplaceAccessToken\" for company " + companyId,
				logEntry.getMessage());
		}

		_verifyDelete(companyId, "marketplaceRefreshToken", refreshToken);
	}

	@Test
	public void testGetToken() throws Exception {
		long companyId = RandomTestUtil.randomLong();

		String keyReferenceString = KeyReferenceUtil.toKeyReferenceString(
			_getKeyReference());

		String token = RandomTestUtil.randomString();

		Mockito.when(
			_secretResolver.resolve(
				Mockito.eq(companyId),
				Mockito.argThat(
					(SecretContext secretContext) -> _isCompanySecretContext(
						companyId, "marketplaceAccessToken", secretContext)),
				Mockito.eq(keyReferenceString))
		).thenReturn(
			token
		);

		_testGetToken(companyId, token, keyReferenceString);

		_testGetToken(companyId, token, token);
	}

	@Test
	public void testStoreToken() throws Exception {
		long companyId = RandomTestUtil.randomLong();
		String keyReferenceString = RandomTestUtil.randomString();
		String token = RandomTestUtil.randomString();

		Mockito.when(
			_secretResolver.store(
				Mockito.eq(companyId),
				Mockito.argThat(
					(SecretContext secretContext) -> _isCompanySecretContext(
						companyId, "marketplaceRefreshToken", secretContext)),
				Mockito.eq(token))
		).thenReturn(
			keyReferenceString
		);

		Assert.assertEquals(
			keyReferenceString,
			_storeToken(companyId, "marketplaceRefreshToken", token));

		PortalException portalException = Assert.assertThrows(
			PortalException.class,
			() -> _storeToken(
				companyId, "marketplaceRefreshToken",
				KeyReferenceUtil.toKeyReferenceString(
					new KeyReference(
						RandomTestUtil.randomString(), StringPool.STAR,
						KeyReference.Type.SECRET))));

		Assert.assertEquals(
			"Token cannot begin with a reserved key reference prefix",
			portalException.getMessage());
	}

	private KeyReference _getKeyReference() {
		return new KeyReference(
			RandomTestUtil.randomString(), RandomTestUtil.randomString(),
			KeyReference.Type.SECRET);
	}

	private boolean _isCompanySecretContext(
		long companyId, String key, SecretContext secretContext) {

		if ((secretContext != null) &&
			(secretContext.getType() == SecretContext.Type.COMPANY) &&
			(secretContext.getCompanyId() == companyId) &&
			Objects.equals(secretContext.getKey(), key)) {

			return true;
		}

		return false;
	}

	private void _mockPreference(long companyId, String name, String value) {
		Mockito.when(
			_mockPrefsProps.getString(companyId, name)
		).thenReturn(
			value
		);
	}

	private String _storeToken(long companyId, String name, String token)
		throws Exception {

		return ReflectionTestUtil.invoke(
			MarketplaceUtil.class, "_storeToken",
			new Class<?>[] {long.class, String.class, String.class}, companyId,
			name, token);
	}

	private void _testGetToken(
			long companyId, String expectedToken, String storedToken)
		throws Exception {

		_mockPreference(companyId, "marketplaceAccessToken", storedToken);

		Assert.assertEquals(
			expectedToken,
			MarketplaceUtil.getToken(companyId, "marketplaceAccessToken"));
	}

	private void _verifyDelete(long companyId, String name, String token) {
		Mockito.verify(
			_secretResolver
		).delete(
			Mockito.eq(companyId),
			Mockito.argThat(
				(SecretContext secretContext) -> _isCompanySecretContext(
					companyId, name, secretContext)),
			Mockito.eq(token)
		);
	}

	private final PrefsProps _mockPrefsProps = Mockito.mock(PrefsProps.class);
	private PrefsProps _prefsProps;
	private final SecretResolver _secretResolver = Mockito.mock(
		SecretResolver.class);
	private Snapshot<SecretResolver> _secretResolverSnapshot;

}