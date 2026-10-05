/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.scim.configuration.web.internal.model.listener;

import com.liferay.oauth2.provider.constants.OAuth2AuthorizationConstants;
import com.liferay.oauth2.provider.model.OAuth2Application;
import com.liferay.oauth2.provider.model.OAuth2Authorization;
import com.liferay.oauth2.provider.service.OAuth2ApplicationLocalService;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.transaction.TransactionCommitCallbackUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.uuid.PortalUUIDUtil;
import com.liferay.portal.security.key.KeyReference;
import com.liferay.portal.security.key.KeyReferenceUtil;
import com.liferay.portal.security.key.secret.Secret;
import com.liferay.portal.security.key.secret.SecretManager;
import com.liferay.scim.rest.util.ScimClientUtil;

import java.util.Dictionary;
import java.util.Objects;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Caio Farias
 */
@Component(service = ModelListener.class)
public class OAuth2AuthorizationModelListener
	extends BaseModelListener<OAuth2Authorization> {

	@Override
	public void onAfterRemove(OAuth2Authorization oAuth2Authorization) {
		KeyReference keyReference = _fetchKeyReference(
			oAuth2Authorization.getAccessTokenContent(), oAuth2Authorization);

		if (keyReference == null) {
			return;
		}

		_deleteSecret(keyReference, oAuth2Authorization);
	}

	@Override
	public void onAfterUpdate(
		OAuth2Authorization originalOAuth2Authorization,
		OAuth2Authorization oAuth2Authorization) {

		KeyReference keyReference = _fetchKeyReference(
			originalOAuth2Authorization.getAccessTokenContent(),
			originalOAuth2Authorization);

		if ((keyReference == null) ||
			Objects.equals(
				originalOAuth2Authorization.getAccessTokenContent(),
				oAuth2Authorization.getAccessTokenContent())) {

			return;
		}

		TransactionCommitCallbackUtil.registerCallback(
			() -> {
				_deleteSecret(keyReference, oAuth2Authorization);

				return null;
			});
	}

	@Override
	public void onBeforeCreate(OAuth2Authorization oAuth2Authorization) {
		_storeAccessTokenContent(oAuth2Authorization);
	}

	@Override
	public void onBeforeUpdate(
		OAuth2Authorization originalOAuth2Authorization,
		OAuth2Authorization oAuth2Authorization) {

		_storeAccessTokenContent(oAuth2Authorization);
	}

	private void _deleteSecret(
		KeyReference keyReference, OAuth2Authorization oAuth2Authorization) {

		try {
			SecretManager secretManager = _getSecretManager();

			secretManager.deleteSecret(
				oAuth2Authorization.getCompanyId(), keyReference);
		}
		catch (Exception exception) {
			_log.error(
				"Unable to delete the stored value for OAuth2 authorization " +
					oAuth2Authorization.getOAuth2AuthorizationId(),
				exception);
		}
	}

	private KeyReference _fetchKeyReference(
		String accessTokenContent, OAuth2Authorization oAuth2Authorization) {

		KeyReference keyReference = KeyReferenceUtil.parseKeyReference(
			accessTokenContent);

		if (keyReference == null) {
			return null;
		}

		String identifier = keyReference.getIdentifier();

		if (!identifier.startsWith(_getIdentifierPrefix(oAuth2Authorization))) {
			return null;
		}

		return keyReference;
	}

	private String _getIdentifierPrefix(
		OAuth2Authorization oAuth2Authorization) {

		return StringBundler.concat(
			OAuth2Authorization.class.getSimpleName(), StringPool.SLASH,
			oAuth2Authorization.getCompanyId(), StringPool.SLASH,
			oAuth2Authorization.getOAuth2AuthorizationId(), StringPool.SLASH);
	}

	private SecretManager _getSecretManager() {
		SecretManager secretManager = _secretManagerSnapshot.get();

		if (secretManager == null) {
			throw new IllegalStateException("Secret manager is unavailable");
		}

		return secretManager;
	}

	private boolean _isScimClient(OAuth2Authorization oAuth2Authorization)
		throws Exception {

		OAuth2Application oAuth2Application =
			_oAuth2ApplicationLocalService.fetchOAuth2Application(
				oAuth2Authorization.getOAuth2ApplicationId());

		if ((oAuth2Application == null) ||
			!StringUtil.startsWith(
				oAuth2Application.getClientId(), _SCIM_CLIENT_ID_PREFIX)) {

			return false;
		}

		Configuration[] configurations = _configurationAdmin.listConfigurations(
			StringBundler.concat(
				"(&(", ConfigurationAdmin.SERVICE_FACTORYPID,
				"=com.liferay.scim.rest.internal.configuration.",
				"ScimClientOAuth2ApplicationConfiguration)(companyId=",
				oAuth2Application.getCompanyId(), "))"));

		if (ArrayUtil.isEmpty(configurations)) {
			return false;
		}

		Configuration configuration = configurations[0];

		Dictionary<String, Object> properties = configuration.getProperties();

		return Objects.equals(
			oAuth2Application.getClientId(),
			ScimClientUtil.generateScimClientId(
				GetterUtil.getString(properties.get("oAuth2ApplicationName"))));
	}

	private void _storeAccessTokenContent(
		OAuth2Authorization oAuth2Authorization) {

		String accessTokenContent = oAuth2Authorization.getAccessTokenContent();

		try {
			if (PropsValues.FIPS_ENABLED &&
				Validator.isNotNull(accessTokenContent) &&
				!Objects.equals(
					accessTokenContent,
					OAuth2AuthorizationConstants.
						ACCESS_TOKEN_CONTENT_EXPIRED_TOKEN) &&
				!KeyReferenceUtil.isKeyReference(accessTokenContent) &&
				_isScimClient(oAuth2Authorization)) {

				SecretManager secretManager = _getSecretManager();

				try (Secret secret = new Secret(
						new KeyReference(
							_getIdentifierPrefix(oAuth2Authorization) +
								PortalUUIDUtil.generate(),
							StringPool.STAR, KeyReference.Type.SECRET),
						accessTokenContent)) {

					oAuth2Authorization.setAccessTokenContent(
						KeyReferenceUtil.toKeyReferenceString(
							secretManager.putSecret(
								oAuth2Authorization.getCompanyId(), secret)));
				}

				oAuth2Authorization.setAccessTokenContentHash(
					accessTokenContent.hashCode());
			}
		}
		catch (Exception exception) {
			throw new ModelListenerException(exception);
		}
	}

	private static final String _SCIM_CLIENT_ID_PREFIX =
		ScimClientUtil.generateScimClientId(StringPool.BLANK);

	private static final Log _log = LogFactoryUtil.getLog(
		OAuth2AuthorizationModelListener.class);

	private static final Snapshot<SecretManager> _secretManagerSnapshot =
		new Snapshot<>(
			OAuth2AuthorizationModelListener.class, SecretManager.class, null,
			true);

	@Reference
	private ConfigurationAdmin _configurationAdmin;

	@Reference
	private OAuth2ApplicationLocalService _oAuth2ApplicationLocalService;

}