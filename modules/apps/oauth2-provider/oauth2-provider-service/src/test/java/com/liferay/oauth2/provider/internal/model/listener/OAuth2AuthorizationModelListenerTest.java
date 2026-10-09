/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth2.provider.internal.model.listener;

import com.liferay.oauth2.provider.constants.OAuth2AuthorizationConstants;
import com.liferay.oauth2.provider.model.OAuth2Authorization;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.security.key.KeyReference;
import com.liferay.portal.security.key.KeyReferenceUtil;
import com.liferay.portal.security.key.secret.Secret;
import com.liferay.portal.security.key.secret.SecretManager;
import com.liferay.portal.test.log.LogCapture;
import com.liferay.portal.test.log.LogEntry;
import com.liferay.portal.test.log.LoggerTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Caio Farias
 */
public class OAuth2AuthorizationModelListenerTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		ReflectionTestUtil.setFieldValue(
			_oAuth2AuthorizationModelListener, "_secretManager",
			_secretManager);
	}

	@Test
	public void testOnAfterRemove() throws Exception {
		_oAuth2AuthorizationModelListener.onAfterRemove(
			_getOAuth2Authorization(
				KeyReferenceUtil.toKeyReferenceString(
					new KeyReference(
						RandomTestUtil.randomString(),
						RandomTestUtil.randomString(),
						KeyReference.Type.SECRET)),
				_ACCESS_TOKEN_DURATION_LONG_LIVED));

		Mockito.verifyNoInteractions(_secretManager);

		KeyReference keyReference = _getKeyReference();

		OAuth2Authorization oAuth2Authorization = _getOAuth2Authorization(
			KeyReferenceUtil.toKeyReferenceString(keyReference),
			_ACCESS_TOKEN_DURATION_LONG_LIVED);

		_oAuth2AuthorizationModelListener.onAfterRemove(oAuth2Authorization);

		Mockito.verify(
			_secretManager
		).deleteSecret(
			_COMPANY_ID, keyReference
		);

		Mockito.doThrow(
			new IllegalStateException()
		).when(
			_secretManager
		).deleteSecret(
			_COMPANY_ID, keyReference
		);

		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				OAuth2AuthorizationModelListener.class.getName(),
				LoggerTestUtil.ERROR)) {

			_oAuth2AuthorizationModelListener.onAfterRemove(
				oAuth2Authorization);

			List<LogEntry> logEntries = logCapture.getLogEntries();

			Assert.assertEquals(logEntries.toString(), 1, logEntries.size());

			LogEntry logEntry = logEntries.get(0);

			Assert.assertEquals(
				"Unable to delete the stored value for OAuth2 authorization " +
					_OAUTH2_AUTHORIZATION_ID,
				logEntry.getMessage());
		}
	}

	@Test
	public void testOnAfterUpdate() throws Exception {
		KeyReference keyReference = _getKeyReference();

		String accessTokenContent = KeyReferenceUtil.toKeyReferenceString(
			keyReference);

		OAuth2Authorization originalOAuth2Authorization =
			_getOAuth2Authorization(
				accessTokenContent, _ACCESS_TOKEN_DURATION_LONG_LIVED);

		_oAuth2AuthorizationModelListener.onAfterUpdate(
			originalOAuth2Authorization,
			_getOAuth2Authorization(
				KeyReferenceUtil.toKeyReferenceString(_getKeyReference()),
				_ACCESS_TOKEN_DURATION_LONG_LIVED));

		Mockito.verify(
			_secretManager
		).deleteSecret(
			_COMPANY_ID, keyReference
		);

		Mockito.clearInvocations(_secretManager);

		_oAuth2AuthorizationModelListener.onAfterUpdate(
			originalOAuth2Authorization,
			_getOAuth2Authorization(
				OAuth2AuthorizationConstants.ACCESS_TOKEN_CONTENT_EXPIRED_TOKEN,
				_ACCESS_TOKEN_DURATION_LONG_LIVED));

		Mockito.verify(
			_secretManager
		).deleteSecret(
			_COMPANY_ID, keyReference
		);

		Mockito.clearInvocations(_secretManager);

		_oAuth2AuthorizationModelListener.onAfterUpdate(
			originalOAuth2Authorization,
			_getOAuth2Authorization(
				accessTokenContent, _ACCESS_TOKEN_DURATION_LONG_LIVED));

		Mockito.verifyNoInteractions(_secretManager);
	}

	@Test
	public void testOnBeforeCreate() throws Exception {
		String accessTokenContent = RandomTestUtil.randomString();

		OAuth2Authorization oAuth2Authorization = _getOAuth2Authorization(
			accessTokenContent, _ACCESS_TOKEN_DURATION_LONG_LIVED);

		_oAuth2AuthorizationModelListener.onBeforeCreate(oAuth2Authorization);

		Mockito.verify(
			oAuth2Authorization, Mockito.never()
		).setAccessTokenContent(
			Mockito.anyString()
		);

		Mockito.verifyNoInteractions(_secretManager);

		KeyReference keyReference = _getKeyReference();

		Mockito.when(
			_secretManager.putSecret(
				Mockito.eq(_COMPANY_ID), Mockito.any(Secret.class))
		).thenReturn(
			keyReference
		);

		try (AutoCloseable autoCloseable =
				ReflectionTestUtil.setFieldValueWithAutoCloseable(
					PropsValues.class, "FIPS_ENABLED", true)) {

			_oAuth2AuthorizationModelListener.onBeforeCreate(
				oAuth2Authorization);

			Mockito.verify(
				oAuth2Authorization
			).setAccessTokenContent(
				KeyReferenceUtil.toKeyReferenceString(keyReference)
			);

			Mockito.verify(
				oAuth2Authorization
			).setAccessTokenContentHash(
				accessTokenContent.hashCode()
			);

			OAuth2Authorization otherOAuth2Authorization =
				_getOAuth2Authorization(
					RandomTestUtil.randomString(),
					_ACCESS_TOKEN_DURATION_SHORT_LIVED);

			_oAuth2AuthorizationModelListener.onBeforeCreate(
				otherOAuth2Authorization);

			Mockito.verify(
				otherOAuth2Authorization, Mockito.never()
			).setAccessTokenContent(
				Mockito.anyString()
			);
		}
	}

	@Test
	public void testOnBeforeUpdate() throws Exception {
		KeyReference keyReference = _getKeyReference();

		Mockito.when(
			_secretManager.putSecret(
				Mockito.eq(_COMPANY_ID), Mockito.any(Secret.class))
		).thenReturn(
			keyReference
		);

		try (AutoCloseable autoCloseable =
				ReflectionTestUtil.setFieldValueWithAutoCloseable(
					PropsValues.class, "FIPS_ENABLED", true)) {

			String accessTokenContent = KeyReferenceUtil.toKeyReferenceString(
				_getKeyReference());

			OAuth2Authorization originalOAuth2Authorization =
				_getOAuth2Authorization(
					accessTokenContent, _ACCESS_TOKEN_DURATION_LONG_LIVED);

			OAuth2Authorization oAuth2Authorization = _getOAuth2Authorization(
				RandomTestUtil.randomString(),
				_ACCESS_TOKEN_DURATION_LONG_LIVED);

			_oAuth2AuthorizationModelListener.onBeforeUpdate(
				originalOAuth2Authorization, oAuth2Authorization);

			Mockito.verify(
				oAuth2Authorization
			).setAccessTokenContent(
				KeyReferenceUtil.toKeyReferenceString(keyReference)
			);

			Mockito.verify(
				_secretManager, Mockito.never()
			).deleteSecret(
				Mockito.anyLong(), Mockito.any(KeyReference.class)
			);

			Mockito.clearInvocations(_secretManager);

			oAuth2Authorization = _getOAuth2Authorization(
				OAuth2AuthorizationConstants.ACCESS_TOKEN_CONTENT_EXPIRED_TOKEN,
				_ACCESS_TOKEN_DURATION_LONG_LIVED);

			_oAuth2AuthorizationModelListener.onBeforeUpdate(
				originalOAuth2Authorization, oAuth2Authorization);

			Mockito.verify(
				oAuth2Authorization, Mockito.never()
			).setAccessTokenContent(
				Mockito.anyString()
			);

			_oAuth2AuthorizationModelListener.onBeforeUpdate(
				originalOAuth2Authorization,
				_getOAuth2Authorization(
					accessTokenContent, _ACCESS_TOKEN_DURATION_LONG_LIVED));

			Mockito.verifyNoInteractions(_secretManager);
		}
	}

	private KeyReference _getKeyReference() {
		return new KeyReference(
			StringBundler.concat(
				OAuth2Authorization.class.getSimpleName(), StringPool.SLASH,
				_COMPANY_ID, StringPool.SLASH, _OAUTH2_AUTHORIZATION_ID,
				StringPool.SLASH, RandomTestUtil.randomString()),
			RandomTestUtil.randomString(), KeyReference.Type.SECRET);
	}

	private OAuth2Authorization _getOAuth2Authorization(
		String accessTokenContent, long accessTokenDuration) {

		OAuth2Authorization oAuth2Authorization = Mockito.mock(
			OAuth2Authorization.class);

		Mockito.when(
			oAuth2Authorization.getAccessTokenContent()
		).thenReturn(
			accessTokenContent
		);

		Date accessTokenCreateDate = new Date();

		Mockito.when(
			oAuth2Authorization.getAccessTokenCreateDate()
		).thenReturn(
			accessTokenCreateDate
		);

		Mockito.when(
			oAuth2Authorization.getAccessTokenExpirationDate()
		).thenReturn(
			new Date(accessTokenCreateDate.getTime() + accessTokenDuration)
		);

		Mockito.when(
			oAuth2Authorization.getCompanyId()
		).thenReturn(
			_COMPANY_ID
		);

		Mockito.when(
			oAuth2Authorization.getOAuth2AuthorizationId()
		).thenReturn(
			_OAUTH2_AUTHORIZATION_ID
		);

		return oAuth2Authorization;
	}

	private static final long _ACCESS_TOKEN_DURATION_LONG_LIVED =
		(Time.MINUTE * 10) + Time.SECOND;

	private static final long _ACCESS_TOKEN_DURATION_SHORT_LIVED =
		Time.MINUTE * 10;

	private static final long _COMPANY_ID = RandomTestUtil.randomLong();

	private static final long _OAUTH2_AUTHORIZATION_ID =
		RandomTestUtil.randomLong();

	private final OAuth2AuthorizationModelListener
		_oAuth2AuthorizationModelListener =
			new OAuth2AuthorizationModelListener();
	private final SecretManager _secretManager = Mockito.mock(
		SecretManager.class);

}