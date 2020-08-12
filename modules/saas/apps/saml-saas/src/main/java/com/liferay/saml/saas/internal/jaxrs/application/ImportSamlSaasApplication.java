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

package com.liferay.saml.saas.internal.jaxrs.application;

import com.liferay.expando.kernel.model.ExpandoBridge;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.module.configuration.ConfigurationProviderUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.saml.persistence.model.SamlSpIdpConnection;
import com.liferay.saml.persistence.service.SamlSpIdpConnectionLocalService;
import com.liferay.saml.runtime.configuration.SamlConfiguration;
import com.liferay.saml.runtime.configuration.SamlProviderConfigurationHelper;
import com.liferay.saml.runtime.credential.KeyStoreManager;
import com.liferay.saml.saas.internal.configuration.SaasConfiguration;
import com.liferay.saml.saas.internal.constants.JSONKeys;
import com.liferay.saml.saas.internal.util.SymmetricEncryptor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;

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
		ServiceContext serviceContext =
			ServiceContextThreadLocal.getServiceContext();

		try {
			SaasConfiguration saasConfiguration =
				ConfigurationProviderUtil.getCompanyConfiguration(
					SaasConfiguration.class, serviceContext.getCompanyId());

			String preSharedKey = saasConfiguration.preSharedKey();

			if (!saasConfiguration.productionEnvironment()) {
				throw new RuntimeException(
					"Instance must be configured as a SAML SaaS production " +
						"environment to receive configuration data imports");
			}

			if (Validator.isBlank(preSharedKey)) {
				throw new RuntimeException(
					"Instance must be configured with a pre-shared key to " +
						"decrypt configuration data imports");
			}

			String decryptedData = SymmetricEncryptor.decryptData(
				preSharedKey, data);

			JSONObject jsonObject = JSONFactoryUtil.createJSONObject(
				decryptedData);

			_generateSamlProviderConfiguration(
				(JSONObject)jsonObject.get(
					JSONKeys.SAML_PROVIDER_CONFIGURATION));

			_generateSamlSpIdpConnections(
				serviceContext,
				(JSONArray)jsonObject.get(JSONKeys.SAML_SP_IDP_CONNECTIONS));

			_generateKeystore((String)jsonObject.get(JSONKeys.SAML_KEYSTORE));
		}
		catch (Exception exception) {
			_log.error("Unable to import SAML configuration data", exception);

			return JSONUtil.put(
				JSONKeys.RESULT, JSONKeys.RESULT_ERROR
			).toString();
		}

		return JSONUtil.put(
			JSONKeys.RESULT, JSONKeys.RESULT_SUCCESS
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
			ServiceContext serviceContext, JSONArray jsonSamlSpIdConnections)
		throws PortalException {

		List<SamlSpIdpConnection> samlSpIdpConnections =
			_samlSpIdpConnectionLocalService.getSamlSpIdpConnections(
				serviceContext.getCompanyId());

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

			SamlSpIdpConnection samlSpIdpConnection =
				_samlSpIdpConnectionLocalService.addSamlSpIdpConnection(
					samlIdpEntityId, assertionSignatureRequired, clockSkew,
					enabled, forceAuthn, ldapImportEnabled, metadataUrl,
					new ByteArrayInputStream(metadataXml.getBytes()), name,
					nameIdFormat, signAuthnRequest, unknownUsersAreStrangers,
					userAttributeMappings, serviceContext);

			JSONObject expandoValues = jsonSamlSpIdpConnection.getJSONObject(
				JSONKeys.EXPANDO_VALUES);

			ExpandoBridge expandoBridge =
				samlSpIdpConnection.getExpandoBridge();

			for (String key : expandoValues.keySet()) {
				expandoBridge.setAttribute(
					key, (Serializable)expandoValues.get(key), false);
			}
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