/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.cell.internal.security;

import com.liferay.ai.hub.cell.configuration.AIHubCellSecretConfiguration;
import com.liferay.ai.hub.cell.security.JWTTokenUtil;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.SecureRandomUtil;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.Validator;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.util.Date;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Pedro Victor Silvestre
 */
@Component(service = JWTTokenUtil.class)
public class JWTTokenImpl implements JWTTokenUtil {

	@Override
	public String generateToken(
		long companyId, long expirationTime, String issuer, long userId) {

		Date now = new Date();

		SignedJWT signedJWT = new SignedJWT(
			new JWSHeader(JWSAlgorithm.HS256),
			new JWTClaimsSet.Builder(
			).expirationTime(
				new Date(now.getTime() + expirationTime)
			).issuer(
				issuer
			).issueTime(
				now
			).subject(
				String.valueOf(userId)
			).build());

		try {
			signedJWT.sign(new MACSigner(_getSecret(companyId)));
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug("Unable to generate a signed token", exception);
			}

			return null;
		}

		return signedJWT.serialize();
	}

	@Override
	public long getUserId(long companyId, String token) {
		JWTClaimsSet jwtClaimsSet = null;

		try {
			SignedJWT signedJWT = SignedJWT.parse(token);

			if (!signedJWT.verify(new MACVerifier(_getSecret(companyId)))) {
				if (_log.isDebugEnabled()) {
					_log.debug("Invalid JWT signature");
				}

				return 0;
			}

			jwtClaimsSet = signedJWT.getJWTClaimsSet();
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"Unable to parse and verify the JWT token", exception);
			}

			return 0;
		}

		Date expirationDate = jwtClaimsSet.getExpirationTime();

		if ((expirationDate == null) || expirationDate.before(new Date())) {
			if (_log.isDebugEnabled()) {
				_log.debug("The JWT token is expired");
			}

			return 0;
		}

		return GetterUtil.getLong(jwtClaimsSet.getSubject());
	}

	private byte[] _getSecret(long companyId) throws Exception {
		AIHubCellSecretConfiguration aiHubCellSecretConfiguration =
			_configurationProvider.getCompanyConfiguration(
				AIHubCellSecretConfiguration.class, companyId);

		String secret = aiHubCellSecretConfiguration.secret();

		if (Validator.isBlank(secret)) {
			int sha256BlockSize = 64;

			byte[] secretBytes = new byte[sha256BlockSize];

			for (int i = 0; i < secretBytes.length; i++) {
				secretBytes[i] = SecureRandomUtil.nextByte();
			}

			secret = Base64.encode(secretBytes);

			_configurationProvider.saveCompanyConfiguration(
				AIHubCellSecretConfiguration.class, companyId,
				HashMapDictionaryBuilder.<String, Object>put(
					"secret", secret
				).build());

			return secretBytes;
		}

		return Base64.decode(secret);
	}

	private static final Log _log = LogFactoryUtil.getLog(JWTTokenImpl.class);

	@Reference
	private ConfigurationProvider _configurationProvider;

}