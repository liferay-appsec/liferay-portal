/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.saml.saas.application;

import static com.liferay.portal.kernel.security.auth.CompanyThreadLocal.getCompanyId;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.module.configuration.ConfigurationProviderUtil;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.saas.configuration.SaasConfiguration;
import com.liferay.saml.persistence.model.SamlSpIdpConnection;
import com.liferay.saml.persistence.service.SamlSpIdpConnectionLocalService;
import com.liferay.saml.runtime.configuration.SamlConfiguration;
import com.liferay.saml.runtime.configuration.SamlProviderConfigurationHelper;
import com.liferay.saml.runtime.credential.KeyStoreManager;
import com.liferay.saml.saas.constants.ExportImportKeys;
import com.liferay.saml.saas.util.SymmetricEntriptor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import java.security.KeyStore;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Marta Medio
 */
@Component(
	property = {
		"liferay.auth.verifier=false", "liferay.cors.annotation=true",
		"liferay.oauth2=false", "oauth2.scope.checker.type=none",
		"osgi.jaxrs.application.base=/saml-saas-import",
		"osgi.jaxrs.name=Liferay.Saas.SamlImport.Application"
	},
	service = Application.class
)
public class ImportSamlSaasApplication extends Application {

	@Override
	public Set<Object> getSingletons() {
		return Collections.singleton(this);
	}

	@Consumes(MediaType.TEXT_PLAIN)
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public String importSamlConfiguration(String data) {
		try {
			SaasConfiguration saasConfiguration =
				ConfigurationProviderUtil.getCompanyConfiguration(
					SaasConfiguration.class, getCompanyId());

			String preSharedKey = saasConfiguration.preSharedKey();

			if (!saasConfiguration.isProductionEnvironment() ||
				preSharedKey.isEmpty()) {

				throw new RuntimeException(
					"SaaS environment invalid for SAML data import");
			}

			String decryptedData = SymmetricEntriptor.decryptData(
				preSharedKey, data);

			JSONObject samlJsonObject = JSONFactoryUtil.createJSONObject(
				decryptedData);

			_generateSamlProviderConfiguration(
				(JSONObject)samlJsonObject.get(
					ExportImportKeys.SAML_PROVIDER_CONFIGURATION_KEY));

			_generateSamlSpIdpConnections(
				(JSONArray)samlJsonObject.get(
					ExportImportKeys.SAML_SP_IDP_CONNECTIONS));

			_generateKeystore(
				(String)samlJsonObject.get(ExportImportKeys.SAML_KEYSTORE));
		}
		catch (Exception exception) {
			_log.error(
				"Unable to import SAML data, check your Saas Configuration",
				exception);

			return JSONUtil.put(
				ExportImportKeys.SAML_RESULT, ExportImportKeys.ERROR_MESSAGE
			).toString();
		}

		return JSONUtil.put(
			ExportImportKeys.SAML_RESULT, ExportImportKeys.SUCCESS_MESSAGE
		).toString();
	}

	@Reference(
		name = "KeyStoreManager", target = "(default=true)", unbind = "-"
	)
	public void setKeyStoreManager(KeyStoreManager keyStoreManager) {
		_keyStoreManager = keyStoreManager;
	}

	private void _generateKeystore(String keyStoreBase64) throws Exception {
		SamlConfiguration samlConfiguration =
			ConfigurationProviderUtil.getSystemConfiguration(
				SamlConfiguration.class);

		String keyStorePassword = samlConfiguration.keyStorePassword();

		byte[] decodedKeyStore = Base64.decode(keyStoreBase64);

		InputStream keyStoreInputStream = new ByteArrayInputStream(
			decodedKeyStore);

		KeyStore keyStore = _keyStoreManager.getKeyStore();

		keyStore.load(keyStoreInputStream, keyStorePassword.toCharArray());

		_keyStoreManager.saveKeyStore(keyStore);
	}

	private void _generateSamlProviderConfiguration(
			JSONObject jsonSamlProviderConfiguration)
		throws Exception {

		UnicodeProperties unicodeProperties = new UnicodeProperties();

		unicodeProperties.put(
			"saml.keystore.credential.password",
			String.valueOf(
				jsonSamlProviderConfiguration.get(
					"saml.keystore.credential.password")));
		unicodeProperties.put(
			"saml.keystore.encryption.credential.password",
			String.valueOf(
				jsonSamlProviderConfiguration.get(
					"saml.keystore.encryption.credential.password")));
		unicodeProperties.put(
			"saml.sp.assertion.signature.required",
			String.valueOf(
				jsonSamlProviderConfiguration.get(
					"saml.sp.assertion.signature.required")));
		unicodeProperties.put(
			"saml.idp.authn.request.signature.required",
			String.valueOf(
				jsonSamlProviderConfiguration.get(
					"saml.idp.authn.request.signature.required")));
		unicodeProperties.put(
			"saml.sp.clock.skew",
			String.valueOf(
				jsonSamlProviderConfiguration.get("saml.sp.clock.skew")));
		unicodeProperties.put(
			"saml.idp.assertion.lifetime",
			String.valueOf(
				jsonSamlProviderConfiguration.get(
					"saml.idp.assertion.lifetime")));
		unicodeProperties.put(
			"saml.entity.id",
			String.valueOf(
				jsonSamlProviderConfiguration.get("saml.entity.id")));
		unicodeProperties.put(
			"saml.sp.ldap.import.enabled",
			String.valueOf(
				jsonSamlProviderConfiguration.get(
					"saml.sp.ldap.import.enabled")));
		unicodeProperties.put(
			"saml.role",
			String.valueOf(jsonSamlProviderConfiguration.get("saml.role")));
		unicodeProperties.put(
			"saml.idp.session.maximum.age",
			String.valueOf(
				jsonSamlProviderConfiguration.get(
					"saml.idp.session.maximum.age")));
		unicodeProperties.put(
			"saml.idp.session.timeout",
			String.valueOf(
				jsonSamlProviderConfiguration.get("saml.idp.session.timeout")));
		unicodeProperties.put(
			"saml.sp.sign.authn.request",
			String.valueOf(
				jsonSamlProviderConfiguration.get(
					"saml.sp.sign.authn.request")));
		unicodeProperties.put(
			"saml.sign.metadata",
			String.valueOf(
				jsonSamlProviderConfiguration.get("saml.sign.metadata")));
		unicodeProperties.put(
			"saml.ssl.required",
			String.valueOf(
				jsonSamlProviderConfiguration.get("saml.ssl.required")));
		unicodeProperties.put(
			"saml.sp.allow.showing.the.login.portlet",
			String.valueOf(
				jsonSamlProviderConfiguration.get(
					"saml.sp.allow.showing.the.login.portlet")));

		_samlProviderConfigurationHelper.updateProperties(unicodeProperties);
	}

	private void _generateSamlSpIdpConnections(
			JSONArray jsonSamlSpIdConnections)
		throws PortalException {

		List<SamlSpIdpConnection> samlSpIdpConnections =
			_samlSpIdpConnectionLocalService.getSamlSpIdpConnections(
				getCompanyId());

		for (SamlSpIdpConnection samlSpIdpConnection : samlSpIdpConnections) {
			_samlSpIdpConnectionLocalService.deleteSamlSpIdpConnection(
				samlSpIdpConnection.getSamlSpIdpConnectionId());
		}

		for (Iterator<JSONObject> iterator = jsonSamlSpIdConnections.iterator();
			 iterator.hasNext();) {

			JSONObject jsonSamlSpIdpConnection = iterator.next();

			String samlIdpEntityId = GetterUtil.getString(
				jsonSamlSpIdpConnection.get("samlIdpEntityId"));
			boolean assertionSignatureRequired = GetterUtil.getBoolean(
				jsonSamlSpIdpConnection.get("assertionSignatureRequired"));
			long clockSkew = GetterUtil.getLong(
				jsonSamlSpIdpConnection.get("clockSkew"));

			boolean enabled = GetterUtil.getBoolean(
				jsonSamlSpIdpConnection.get("enabled"));

			boolean forceAuthn = GetterUtil.getBoolean(
				jsonSamlSpIdpConnection.get("forceAuthn"));

			boolean ldapImportEnabled = GetterUtil.getBoolean(
				jsonSamlSpIdpConnection.get("ldapImportEnabled"));

			String metadataUrl = GetterUtil.getString(
				jsonSamlSpIdpConnection.get("metadataUrl"));

			String metadataXml = GetterUtil.getString(
				jsonSamlSpIdpConnection.get("metadataXml"));

			String name = GetterUtil.getString(
				jsonSamlSpIdpConnection.get("name"));

			String nameIdFormat = GetterUtil.getString(
				jsonSamlSpIdpConnection.get("nameIdFormat"));

			boolean signAuthnRequest = GetterUtil.getBoolean(
				jsonSamlSpIdpConnection.get("signAuthnRequest"));

			boolean unknownUsersAreStrangers = GetterUtil.getBoolean(
				jsonSamlSpIdpConnection.get("unknownUsersAreStrangers"));

			String userAttributeMappings = GetterUtil.getString(
				jsonSamlSpIdpConnection.get("userAttributeMappings"));

			_samlSpIdpConnectionLocalService.addSamlSpIdpConnection(
				samlIdpEntityId, assertionSignatureRequired, clockSkew, enabled,
				forceAuthn, ldapImportEnabled, metadataUrl,
				new ByteArrayInputStream(metadataXml.getBytes()), name,
				nameIdFormat, signAuthnRequest, unknownUsersAreStrangers,
				userAttributeMappings,
				ServiceContextThreadLocal.getServiceContext());
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ImportSamlSaasApplication.class);

	private KeyStoreManager _keyStoreManager;

	@Reference
	private SamlProviderConfigurationHelper _samlProviderConfigurationHelper;

	@Reference
	private SamlSpIdpConnectionLocalService _samlSpIdpConnectionLocalService;

}