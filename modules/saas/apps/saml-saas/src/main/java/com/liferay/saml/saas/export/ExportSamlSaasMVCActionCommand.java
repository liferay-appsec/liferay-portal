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

package com.liferay.saml.saas.export;

import com.liferay.expando.kernel.model.ExpandoBridge;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.module.configuration.ConfigurationProviderUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.saas.configuration.SaasConfiguration;
import com.liferay.saml.constants.SamlAdminPortletKeys;
import com.liferay.saml.persistence.model.SamlSpIdpConnection;
import com.liferay.saml.persistence.service.SamlSpIdpConnectionLocalService;
import com.liferay.saml.runtime.configuration.SamlConfiguration;
import com.liferay.saml.runtime.configuration.SamlProviderConfiguration;
import com.liferay.saml.runtime.configuration.SamlProviderConfigurationHelper;
import com.liferay.saml.runtime.credential.KeyStoreManager;
import com.liferay.saml.saas.constants.JSONKeys;
import com.liferay.saml.saas.util.SymmetricEntriptor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import java.util.Enumeration;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.RuntimeDelegate;

import org.apache.cxf.jaxrs.client.spec.ClientBuilderImpl;
import org.apache.cxf.jaxrs.impl.RuntimeDelegateImpl;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Marta Medio
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + SamlAdminPortletKeys.SAML_ADMIN,
		"mvc.command.name=/admin/saas/saml/export"
	},
	service = MVCActionCommand.class
)
public class ExportSamlSaasMVCActionCommand extends BaseMVCActionCommand {

	@Reference(
		name = "KeyStoreManager", target = "(default=true)", unbind = "-"
	)
	public void setKeyStoreManager(KeyStoreManager keyStoreManager) {
		_keyStoreManager = keyStoreManager;
	}

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		long companyId = _portal.getCompanyId(actionRequest);

		SaasConfiguration saasConfiguration =
			ConfigurationProviderUtil.getCompanyConfiguration(
				SaasConfiguration.class, companyId);

		if (saasConfiguration.isProductionEnvironment() ||
			Validator.isBlank(saasConfiguration.targetInstanceImportURL()) ||
			Validator.isBlank(saasConfiguration.preSharedKey())) {

			return;
		}

		try {
			ClientBuilder clientBuilder = new ClientBuilderImpl();

			Client client = clientBuilder.build();

			RuntimeDelegate runtimeDelegate = new RuntimeDelegateImpl();

			UriBuilder uriBuilder = runtimeDelegate.createUriBuilder();

			WebTarget target = client.target(
				uriBuilder.uri(saasConfiguration.targetInstanceImportURL()));

			String jsonResponse = target.request(
				MediaType.APPLICATION_JSON
			).post(
				Entity.entity(
					_getEncryptedJSONPayload(
						companyId, saasConfiguration.preSharedKey()),
					MediaType.TEXT_PLAIN),
				String.class
			);

			if (jsonResponse != null) {
				JSONObject response = JSONFactoryUtil.createJSONObject(
					jsonResponse);

				if (response.get(
						JSONKeys.RESULT).equals(
							JSONKeys.RESULT_ERROR)) {

					SessionErrors.add(actionRequest, "exportError");
				}
			}
		}
		catch (Exception exception) {
			_log.error("Unable to export SAML configuration data", exception);

			SessionErrors.add(actionRequest, "exportError");
		}
	}

	private String _getEncryptedJSONPayload(long companyId, String preSharedKey)
		throws Exception {

		JSONObject samlJsonObject;

		try {
			samlJsonObject = JSONUtil.put(
				JSONKeys.SAML_KEYSTORE, _getKeyStore());
		}
		catch (Exception exception) {
			_log.error(
				"Unable to export the SAML KeyStore for companyId " + companyId,
				exception);

			throw exception;
		}

		samlJsonObject.put(
			JSONKeys.SAML_PROVIDER_CONFIGURATION,
			_getSamlProviderConfiguration()
		).put(
			JSONKeys.SAML_SP_IDP_CONNECTIONS, _getSpIdpConnections(companyId)
		);

		try {
			return SymmetricEntriptor.encryptData(
				preSharedKey, samlJsonObject.toString());
		}
		catch (Exception exception) {
			_log.error("Unable to encrypt the JSON payload", exception);

			throw exception;
		}
	}

	private String _getKeyStore()
		throws CertificateException, ConfigurationException, IOException,
			   KeyStoreException, NoSuchAlgorithmException {

		SamlConfiguration samlConfiguration =
			ConfigurationProviderUtil.getSystemConfiguration(
				SamlConfiguration.class);

		String keyStorePassword = samlConfiguration.keyStorePassword();

		KeyStore keyStore = _keyStoreManager.getKeyStore();

		ByteArrayOutputStream byteArrayOutputStream =
			new ByteArrayOutputStream();

		keyStore.store(byteArrayOutputStream, keyStorePassword.toCharArray());

		return Base64.encode(byteArrayOutputStream.toByteArray());
	}

	private JSONObject _getSamlProviderConfiguration() {
		SamlProviderConfiguration samlProviderConfiguration =
			_samlProviderConfigurationHelper.getSamlProviderConfiguration();

		return JSONUtil.put(
			"saml.entity.id", samlProviderConfiguration.entityId()
		).put(
			"saml.idp.assertion.lifetime",
			samlProviderConfiguration.defaultAssertionLifetime()
		).put(
			"saml.idp.authn.request.signature.required",
			samlProviderConfiguration.authnRequestSignatureRequired()
		).put(
			"saml.idp.session.maximum.age",
			samlProviderConfiguration.sessionMaximumAge()
		).put(
			"saml.idp.session.timeout",
			samlProviderConfiguration.sessionTimeout()
		).put(
			"saml.keystore.credential.password",
			samlProviderConfiguration.keyStoreCredentialPassword()
		).put(
			"saml.keystore.encryption.credential.password",
			samlProviderConfiguration.keyStoreEncryptionCredentialPassword()
		).put(
			"saml.role", samlProviderConfiguration.role()
		).put(
			"saml.sign.metadata", samlProviderConfiguration.signMetadata()
		).put(
			"saml.sp.allow.showing.the.login.portlet",
			samlProviderConfiguration.allowShowingTheLoginPortlet()
		).put(
			"saml.sp.assertion.signature.required",
			samlProviderConfiguration.assertionSignatureRequired()
		).put(
			"saml.sp.clock.skew", samlProviderConfiguration.clockSkew()
		).put(
			"saml.sp.ldap.import.enabled",
			samlProviderConfiguration.ldapImportEnabled()
		).put(
			"saml.sp.sign.authn.request",
			samlProviderConfiguration.signAuthnRequest()
		).put(
			"saml.ssl.required", samlProviderConfiguration.sslRequired()
		);
	}

	private JSONObject _getSpIdpConnectionExpandoValues(
		SamlSpIdpConnection samlSpIdpConnection) {

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

		ExpandoBridge expandoBridge = samlSpIdpConnection.getExpandoBridge();

		Enumeration<String> enumeration = expandoBridge.getAttributeNames();

		while (enumeration.hasMoreElements()) {
			String attributeName = enumeration.nextElement();

			jsonObject.put(
				attributeName,
				expandoBridge.getAttribute(attributeName, false));
		}

		return jsonObject;
	}

	private JSONArray _getSpIdpConnections(long companyId) {
		JSONArray samlSpIdpConnections = JSONFactoryUtil.createJSONArray();

		List<SamlSpIdpConnection> samlSpIdpConnectionsList =
			_samlSpIdpConnectionLocalService.getSamlSpIdpConnections(companyId);

		for (SamlSpIdpConnection samlSpIdpConnection :
				samlSpIdpConnectionsList) {

			JSONObject jsonSamlSpIdpConnection = JSONUtil.put(
				JSONKeys.EXPANDO_VALUES,
				_getSpIdpConnectionExpandoValues(samlSpIdpConnection)
			).put(
				"assertionSignatureRequired",
				samlSpIdpConnection.isAssertionSignatureRequired()
			).put(
				"clockSkew", samlSpIdpConnection.getClockSkew()
			).put(
				"enabled", samlSpIdpConnection.isEnabled()
			).put(
				"forceAuthn", samlSpIdpConnection.isForceAuthn()
			).put(
				"ldapImportEnabled", samlSpIdpConnection.isLdapImportEnabled()
			).put(
				"metadataUrl", samlSpIdpConnection.getMetadataUrl()
			).put(
				"metadataXml", samlSpIdpConnection.getMetadataXml()
			).put(
				"name", samlSpIdpConnection.getName()
			).put(
				"nameIdFormat", samlSpIdpConnection.getNameIdFormat()
			).put(
				"samlIdpEntityId", samlSpIdpConnection.getSamlIdpEntityId()
			).put(
				"samlSpIdpConnectionId",
				samlSpIdpConnection.getSamlSpIdpConnectionId()
			).put(
				"signAuthnRequest", samlSpIdpConnection.isSignAuthnRequest()
			).put(
				"unknownUsersAreStrangers",
				samlSpIdpConnection.isUnknownUsersAreStrangers()
			).put(
				"userAttributeMappings",
				samlSpIdpConnection.getUserAttributeMappings()
			);

			samlSpIdpConnections.put(jsonSamlSpIdpConnection);
		}

		return samlSpIdpConnections;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ExportSamlSaasMVCActionCommand.class);

	private KeyStoreManager _keyStoreManager;

	@Reference
	private Portal _portal;

	@Reference
	private SamlProviderConfigurationHelper _samlProviderConfigurationHelper;

	@Reference
	private SamlSpIdpConnectionLocalService _samlSpIdpConnectionLocalService;

}