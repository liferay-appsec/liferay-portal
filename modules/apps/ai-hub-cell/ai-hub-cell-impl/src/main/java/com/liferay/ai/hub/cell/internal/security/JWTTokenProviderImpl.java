/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.cell.internal.security;

import com.liferay.ai.hub.cell.configuration.AIHubCellConfiguration;
import com.liferay.ai.hub.cell.security.JWTTokenProvider;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.GetterUtil;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Rafael Praxedes
 */
@Component(service = JWTTokenProvider.class)
public class JWTTokenProviderImpl implements JWTTokenProvider {

	@Override
	public String generateToken() {
		try {
			Company company = _companyLocalService.getCompany(
				CompanyThreadLocal.getCompanyId());

			return _generateToken(
				TimeUnit.MINUTES.toMillis(10), company.getVirtualHostname(),
				PrincipalThreadLocal.getUserId());
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug("Unable to generate a signed token", exception);
			}

			return null;
		}
	}

	@Override
	public long getUserId(String token) {
		JWTClaimsSet jwtClaimsSet = null;

		try {
			SignedJWT signedJWT = SignedJWT.parse(token);

			if (!signedJWT.verify(new MACVerifier(_getSecret()))) {
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

	private String _generateToken(
		long expirationTime, String issuer, long userId) {

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
			signedJWT.sign(new MACSigner(_getSecret()));
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug("Unable to generate a signed token", exception);
			}

			return null;
		}

		return signedJWT.serialize();
	}

	private byte[] _getSecret() throws Exception {
		AIHubCellConfiguration aiHubCellConfiguration =
			_configurationProvider.getCompanyConfiguration(
				AIHubCellConfiguration.class,
				CompanyThreadLocal.getCompanyId());

		return Base64.decode(aiHubCellConfiguration.secret());
	}

	private static final Log _log = LogFactoryUtil.getLog(
		JWTTokenProviderImpl.class);

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private ConfigurationProvider _configurationProvider;

}