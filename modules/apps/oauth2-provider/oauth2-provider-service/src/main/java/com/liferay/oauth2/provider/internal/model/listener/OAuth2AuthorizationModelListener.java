/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth2.provider.internal.model.listener;

import com.liferay.oauth2.provider.constants.OAuth2AuthorizationConstants;
import com.liferay.oauth2.provider.model.OAuth2Authorization;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.uuid.PortalUUIDUtil;
import com.liferay.portal.security.key.KeyReference;
import com.liferay.portal.security.key.KeyReferenceUtil;
import com.liferay.portal.security.key.secret.Secret;
import com.liferay.portal.security.key.secret.SecretManager;

import java.util.Date;
import java.util.Objects;

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
		_deleteSecret(oAuth2Authorization);
	}

	@Override
	public void onAfterUpdate(
		OAuth2Authorization originalOAuth2Authorization,
		OAuth2Authorization oAuth2Authorization) {

		if (!Objects.equals(
				originalOAuth2Authorization.getAccessTokenContent(),
				oAuth2Authorization.getAccessTokenContent())) {

			_deleteSecret(originalOAuth2Authorization);
		}
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

	private void _deleteSecret(OAuth2Authorization oAuth2Authorization) {
		KeyReference keyReference = KeyReferenceUtil.parseKeyReference(
			oAuth2Authorization.getAccessTokenContent());

		if (keyReference == null) {
			return;
		}

		String identifier = keyReference.getIdentifier();

		if (!identifier.startsWith(_getIdentifierPrefix(oAuth2Authorization))) {
			return;
		}

		try {
			_secretManager.deleteSecret(
				oAuth2Authorization.getCompanyId(), keyReference);
		}
		catch (Exception exception) {
			_log.error(
				"Unable to delete the stored value for OAuth2 authorization " +
					oAuth2Authorization.getOAuth2AuthorizationId(),
				exception);
		}
	}

	private String _getIdentifierPrefix(
		OAuth2Authorization oAuth2Authorization) {

		return StringBundler.concat(
			OAuth2Authorization.class.getSimpleName(), StringPool.SLASH,
			oAuth2Authorization.getCompanyId(), StringPool.SLASH,
			oAuth2Authorization.getOAuth2AuthorizationId(), StringPool.SLASH);
	}

	private void _storeAccessTokenContent(
		OAuth2Authorization oAuth2Authorization) {

		if (!PropsValues.FIPS_ENABLED) {
			return;
		}

		String accessTokenContent = oAuth2Authorization.getAccessTokenContent();

		if (Validator.isNull(accessTokenContent) ||
			Objects.equals(
				accessTokenContent,
				OAuth2AuthorizationConstants.
					ACCESS_TOKEN_CONTENT_EXPIRED_TOKEN) ||
			KeyReferenceUtil.isKeyReference(accessTokenContent)) {

			return;
		}

		Date accessTokenCreateDate =
			oAuth2Authorization.getAccessTokenCreateDate();
		Date accessTokenExpirationDate =
			oAuth2Authorization.getAccessTokenExpirationDate();

		long accessTokenDuration =
			accessTokenExpirationDate.getTime() -
				accessTokenCreateDate.getTime();

		if (accessTokenDuration <= (Time.MINUTE * 10)) {
			return;
		}

		try {
			String identifier =
				_getIdentifierPrefix(oAuth2Authorization) +
					PortalUUIDUtil.generate();

			try (Secret secret = new Secret(
					new KeyReference(
						identifier, StringPool.STAR, KeyReference.Type.SECRET),
					accessTokenContent)) {

				oAuth2Authorization.setAccessTokenContent(
					KeyReferenceUtil.toKeyReferenceString(
						_secretManager.putSecret(
							oAuth2Authorization.getCompanyId(), secret)));
			}

			oAuth2Authorization.setAccessTokenContentHash(
				accessTokenContent.hashCode());
		}
		catch (Exception exception) {
			throw new ModelListenerException(exception);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		OAuth2AuthorizationModelListener.class);

	@Reference
	private SecretManager _secretManager;

}