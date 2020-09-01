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
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.module.configuration.ConfigurationProviderUtil;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.saml.persistence.model.SamlSpIdpConnection;
import com.liferay.saml.persistence.service.SamlSpIdpConnectionLocalService;
import com.liferay.saml.runtime.configuration.SamlConfiguration;
import com.liferay.saml.runtime.configuration.SamlProviderConfigurationHelper;
import com.liferay.saml.runtime.credential.KeyStoreManager;
import com.liferay.saml.saas.internal.configuration.SamlSaasConfiguration;
import com.liferay.saml.saas.internal.constants.JSONKeys;
import com.liferay.saml.saas.internal.util.SymmetricEncryptor;

import java.io.ByteArrayInputStream;
import java.io.Serializable;

import java.security.KeyStore;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Marta Medio
 */
@Component(
	configurationPid = "com.liferay.saml.runtime.configuration.SamlConfiguration",
	configurationPolicy = ConfigurationPolicy.OPTIONAL,
	property = {
		"liferay.auth.verifier=false", "liferay.oauth2=false",
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
	public String importSamlConfiguration(
		String data, @Context HttpServletRequest httpServletRequest) {

		long companyId = _portal.getCompanyId(httpServletRequest);

		try {
			SamlSaasConfiguration samlSaasConfiguration =
				ConfigurationProviderUtil.getCompanyConfiguration(
					SamlSaasConfiguration.class, companyId);

			String preSharedKey = samlSaasConfiguration.preSharedKey();

			if (!samlSaasConfiguration.productionEnvironment()) {
				_log.error(
					"Instance must be configured as a SAML SaaS production " +
						"environment to receive configuration data imports");

				throw new WebApplicationException(Response.Status.NOT_FOUND);
			}

			if (Validator.isBlank(preSharedKey)) {
				_log.error(
					"Instance must be configured with a pre-shared key to " +
						"decrypt configuration data imports");

				throw new WebApplicationException(Response.Status.NOT_FOUND);
			}

			String decryptedData = SymmetricEncryptor.decryptData(
				preSharedKey, data);

			JSONObject jsonObject = JSONFactoryUtil.createJSONObject(
				decryptedData);

			_generateSamlProviderConfiguration(
				(JSONObject)jsonObject.get(
					JSONKeys.SAML_PROVIDER_CONFIGURATION));

			_generateSamlSpIdpConnections(
				httpServletRequest,
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

	@Activate
	protected void activate(Map<String, Object> properties) {
		_samlConfiguration = ConfigurableUtil.createConfigurable(
			SamlConfiguration.class, properties);
	}

	private void _generateKeystore(String keyStoreBase64) throws Exception {
		KeyStore keyStore = _keyStoreManager.getKeyStore();

		String keyStorePassword = _samlConfiguration.keyStorePassword();

		keyStore.load(
			new ByteArrayInputStream(Base64.decode(keyStoreBase64)),
			keyStorePassword.toCharArray());

		_keyStoreManager.saveKeyStore(keyStore);
	}

	private void _generateSamlProviderConfiguration(
			JSONObject jsonSamlProviderConfiguration)
		throws Exception {

		UnicodeProperties unicodeProperties = new UnicodeProperties();

		unicodeProperties.put(
			"saml.entity.id",
			String.valueOf(
				jsonSamlProviderConfiguration.get("saml.entity.id")));
		unicodeProperties.put(
			"saml.idp.assertion.lifetime",
			String.valueOf(
				jsonSamlProviderConfiguration.get(
					"saml.idp.assertion.lifetime")));
		unicodeProperties.put(
			"saml.idp.authn.request.signature.required",
			String.valueOf(
				jsonSamlProviderConfiguration.get(
					"saml.idp.authn.request.signature.required")));
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
			"saml.role",
			String.valueOf(jsonSamlProviderConfiguration.get("saml.role")));
		unicodeProperties.put(
			"saml.sign.metadata",
			String.valueOf(
				jsonSamlProviderConfiguration.get("saml.sign.metadata")));
		unicodeProperties.put(
			"saml.sp.allow.showing.the.login.portlet",
			String.valueOf(
				jsonSamlProviderConfiguration.get(
					"saml.sp.allow.showing.the.login.portlet")));
		unicodeProperties.put(
			"saml.sp.assertion.signature.required",
			String.valueOf(
				jsonSamlProviderConfiguration.get(
					"saml.sp.assertion.signature.required")));
		unicodeProperties.put(
			"saml.sp.clock.skew",
			String.valueOf(
				jsonSamlProviderConfiguration.get("saml.sp.clock.skew")));
		unicodeProperties.put(
			"saml.sp.ldap.import.enabled",
			String.valueOf(
				jsonSamlProviderConfiguration.get(
					"saml.sp.ldap.import.enabled")));
		unicodeProperties.put(
			"saml.sp.sign.authn.request",
			String.valueOf(
				jsonSamlProviderConfiguration.get(
					"saml.sp.sign.authn.request")));
		unicodeProperties.put(
			"saml.ssl.required",
			String.valueOf(
				jsonSamlProviderConfiguration.get("saml.ssl.required")));

		_samlProviderConfigurationHelper.updateProperties(unicodeProperties);
	}

	private void _generateSamlSpIdpConnections(
			HttpServletRequest httpServletRequest,
			JSONArray jsonSamlSpIdConnections)
		throws PortalException {

		List<SamlSpIdpConnection> samlSpIdpConnections =
			_samlSpIdpConnectionLocalService.getSamlSpIdpConnections(
				_portal.getCompanyId(httpServletRequest));

		for (SamlSpIdpConnection samlSpIdpConnection : samlSpIdpConnections) {
			_samlSpIdpConnectionLocalService.deleteSamlSpIdpConnection(
				samlSpIdpConnection.getSamlSpIdpConnectionId());
		}

		for (JSONObject jsonSamlSpIdpConnection :
				(Iterable<JSONObject>)jsonSamlSpIdConnections) {

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
					userAttributeMappings,
					ServiceContextFactory.getInstance(
						SamlSpIdpConnection.class.getName(),
						httpServletRequest));

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

	@Reference(name = "KeyStoreManager")
	private KeyStoreManager _keyStoreManager;

	@Reference
	private Portal _portal;

	private SamlConfiguration _samlConfiguration;

	@Reference
	private SamlProviderConfigurationHelper _samlProviderConfigurationHelper;

	@Reference
	private SamlSpIdpConnectionLocalService _samlSpIdpConnectionLocalService;

}