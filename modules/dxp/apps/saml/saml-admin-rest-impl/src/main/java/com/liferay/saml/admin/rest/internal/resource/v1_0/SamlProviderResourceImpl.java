/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.saml.admin.rest.internal.resource.v1_0;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.UnicodePropertiesBuilder;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import com.liferay.saml.admin.rest.dto.v1_0.Idp;
import com.liferay.saml.admin.rest.dto.v1_0.SamlProvider;
import com.liferay.saml.admin.rest.dto.v1_0.Sp;
import com.liferay.saml.admin.rest.resource.v1_0.SamlProviderResource;
import com.liferay.saml.constants.SamlProviderConfigurationKeys;
import com.liferay.saml.runtime.configuration.SamlConfiguration;
import com.liferay.saml.runtime.configuration.SamlProviderConfiguration;
import com.liferay.saml.runtime.configuration.SamlProviderConfigurationHelper;
import com.liferay.saml.runtime.exception.CredentialException;
import com.liferay.saml.runtime.exception.EntityIdException;
import com.liferay.saml.runtime.metadata.LocalEntityManager;

import java.io.Serializable;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Map;
import java.util.Objects;

import javax.ws.rs.core.Response;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Stian Sigvartsen
 */
@Component(
	configurationPid = "com.liferay.saml.runtime.configuration.SamlConfiguration",
	properties = "OSGI-INF/liferay/rest/v1_0/saml-provider.properties",
	scope = ServiceScope.PROTOTYPE, service = SamlProviderResource.class
)
public class SamlProviderResourceImpl extends BaseSamlProviderResourceImpl {

	@Override
	public Object getRole(String roleId) throws Exception {
		_checkPermission();

		SamlProviderConfiguration samlProviderConfiguration =
			_samlProviderConfigurationHelper.getSamlProviderConfiguration();

		if (Objects.equals(SamlProvider.Role.SP.getValue(), roleId)) {
			return _getSp(samlProviderConfiguration);
		}
		else if (Objects.equals(SamlProvider.Role.IDP.getValue(), roleId)) {
			return _getIdp(samlProviderConfiguration);
		}

		throw new PortalException("Unsupported role: " + roleId);
	}

	@Override
	public SamlProvider getSamlProvider() throws Exception {
		_checkPermission();

		SamlProviderConfiguration samlProviderConfiguration =
			_samlProviderConfigurationHelper.getSamlProviderConfiguration();

		SamlProvider samlProvider = new SamlProvider();

		samlProvider.setEnabled(samlProviderConfiguration.enabled());
		samlProvider.setEntityId(samlProviderConfiguration.entityId());
		samlProvider.setSignMetadata(samlProviderConfiguration.signMetadata());
		samlProvider.setSslRequired(samlProviderConfiguration.sslRequired());

		String role = samlProviderConfiguration.role();

		if (SamlProviderConfigurationKeys.SAML_ROLE_SP.equals(role)) {
			samlProvider.setRole(SamlProvider.Role.SP);
			samlProvider.setSp(_getSp(samlProviderConfiguration));
		}
		else if (SamlProviderConfigurationKeys.SAML_ROLE_IDP.equals(role)) {
			samlProvider.setIdp(_getIdp(samlProviderConfiguration));
			samlProvider.setRole(SamlProvider.Role.IDP);
		}

		return samlProvider;
	}

	@Override
	public Response patchRole(String roleId, Object object) throws Exception {
		_checkPermission();

		if (Objects.equals(SamlProvider.Role.IDP.getValue(), roleId) &&
			(object instanceof Idp)) {

			_setIdpProperties((Idp)object, new UnicodeProperties(), false);
		}
		else if (Objects.equals(SamlProvider.Role.SP.getValue(), roleId) &&
				 (object instanceof Sp)) {

			SamlProviderConfiguration samlProviderConfiguration =
				_samlProviderConfigurationHelper.getSamlProviderConfiguration();

			_setSpProperties(
				samlProviderConfiguration.entityId(), (Sp)object,
				new UnicodeProperties(), false);
		}
		else {
			throw new PortalException("Unsupported role or invalid data");
		}

		return super.patchRole(roleId, object);
	}

	@Override
	public SamlProvider patchSamlProvider(SamlProvider samlProvider)
		throws Exception {

		_checkPermission();

		return _updateSamlProvider(samlProvider, false);
	}

	@Override
	public SamlProvider postSamlProvider(SamlProvider samlProvider)
		throws Exception {

		_checkPermission();

		return _updateSamlProvider(samlProvider, true);
	}

	@Override
	public Response putRole(String roleId, Object object) throws Exception {
		_checkPermission();

		if (Objects.equals(SamlProvider.Role.IDP.getValue(), roleId) &&
			(object instanceof Idp)) {

			_setIdpProperties((Idp)object, new UnicodeProperties(), true);
		}
		else if (Objects.equals(SamlProvider.Role.SP.getValue(), roleId) &&
				 (object instanceof Sp)) {

			SamlProviderConfiguration samlProviderConfiguration =
				_samlProviderConfigurationHelper.getSamlProviderConfiguration();

			_setSpProperties(
				samlProviderConfiguration.entityId(), (Sp)object,
				new UnicodeProperties(), true);
		}
		else {
			throw new PortalException("Unsupported role or invalid data");
		}

		return super.putRole(roleId, object);
	}

	@Override
	public Page<SamlProvider> read(
			Filter filter, Pagination pagination, Sort[] sorts,
			Map<String, Serializable> parameters, String search)
		throws Exception {

		_checkPermission();

		return Page.of(Collections.singleton(getSamlProvider()));
	}

	@Activate
	protected void activate(
		BundleContext bundleContext, Map<String, Object> properties) {

		_samlConfiguration = ConfigurableUtil.createConfigurable(
			SamlConfiguration.class, properties);

		_serviceRegistration = bundleContext.registerService(
			ManagedServiceFactory.class,
			new SystemConfigurationManagedServiceFactory(),
			HashMapDictionaryBuilder.put(
				Constants.SERVICE_PID,
				"com.liferay.saml.runtime.configuration." +
					"SamlProviderConfiguration"
			).build());
	}

	@Deactivate
	protected void deactivate() {
		if (_serviceRegistration != null) {
			_serviceRegistration.unregister();
		}
	}

	private void _authenticateLocalEntityCertificate(
			String certificateKeyPassword,
			LocalEntityManager.CertificateUsage certificateUsage,
			String entityId)
		throws Exception {

		try {
			_localEntityManager.authenticateLocalEntityCertificate(
				certificateKeyPassword, certificateUsage, entityId);
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) {
				_log.warn(exception);
			}

			throw new CredentialException(
				StringBundler.concat(
					"Error occurred when authenticating the ",
					certificateUsage.name(), " certificate. Please verify ",
					"that the SAML KeyStore contains a certificate for the ",
					"Entity ID and that it is protected by the provided key ",
					"credential password"));
		}
	}

	private void _checkPermission() throws Exception {
		PermissionChecker permissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		if (!permissionChecker.isCompanyAdmin(
				CompanyThreadLocal.getCompanyId())) {

			throw new PrincipalException.MustBeCompanyAdmin(
				permissionChecker.getUserId());
		}
	}

	private Idp _getIdp(SamlProviderConfiguration samlProviderConfiguration)
		throws Exception {

		Idp idp = new Idp();

		idp.setAuthnRequestSignatureRequired(
			samlProviderConfiguration.authnRequestSignatureRequired());
		idp.setDefaultAssertionLifetime(
			samlProviderConfiguration.defaultAssertionLifetime());
		idp.setSessionMaximumAge(samlProviderConfiguration.sessionMaximumAge());
		idp.setSessionTimeout(samlProviderConfiguration.sessionTimeout());

		return idp;
	}

	private Sp _getSp(SamlProviderConfiguration samlProviderConfiguration)
		throws Exception {

		Sp sp = new Sp();

		sp.setAllowShowingTheLoginPortlet(
			samlProviderConfiguration.allowShowingTheLoginPortlet());
		sp.setAssertionSignatureRequired(
			samlProviderConfiguration.assertionSignatureRequired());
		sp.setClockSkew(samlProviderConfiguration.clockSkew());
		sp.setLdapImportEnabled(samlProviderConfiguration.ldapImportEnabled());
		sp.setSignAuthnRequest(samlProviderConfiguration.signAuthnRequest());

		return sp;
	}

	private void _setIdpProperties(
		Idp idp, UnicodeProperties unicodeProperties,
		boolean useSystemDefaultsForNulls) {

		_setProperty(
			unicodeProperties, "saml.idp.assertion.lifetime",
			_toNullableString(idp.getDefaultAssertionLifetime()),
			useSystemDefaultsForNulls);
		_setProperty(
			unicodeProperties, "saml.idp.authn.request.signature.required",
			_toNullableString(idp.getAuthnRequestSignatureRequired()),
			useSystemDefaultsForNulls);
		_setProperty(
			unicodeProperties, "saml.idp.session.maximum.age",
			_toNullableString(idp.getSessionMaximumAge()),
			useSystemDefaultsForNulls);
		_setProperty(
			unicodeProperties, "saml.idp.session.timeout",
			_toNullableString(idp.getSessionTimeout()),
			useSystemDefaultsForNulls);

		unicodeProperties.put(
			"saml.role", SamlProviderConfigurationKeys.SAML_ROLE_IDP);
	}

	private void _setProperty(
		UnicodeProperties unicodeProperties, String key, String value,
		boolean useSystemDefaultsForNulls) {

		if (value == null) {
			if (useSystemDefaultsForNulls && (_systemProperties != null)) {
				unicodeProperties.put(
					key, _toNullableString(_systemProperties.get(key)));
			}

			return;
		}

		unicodeProperties.put(key, value);
	}

	private void _setSamlProviderProperties(
		SamlProvider samlProvider, UnicodeProperties unicodeProperties,
		boolean useSystemDefaultsForNulls) {

		_setProperty(
			unicodeProperties, "saml.enabled",
			_toNullableString(samlProvider.getEnabled()),
			useSystemDefaultsForNulls);

		_setProperty(
			unicodeProperties, "saml.entity.id", samlProvider.getEntityId(),
			useSystemDefaultsForNulls);

		_setProperty(
			unicodeProperties, "saml.keystore.credential.password",
			_toNullableString(samlProvider.getKeyStoreCredentialPassword()),
			useSystemDefaultsForNulls);

		_setProperty(
			unicodeProperties, "saml.sign.metadata",
			_toNullableString(samlProvider.getSignMetadata()),
			useSystemDefaultsForNulls);

		_setProperty(
			unicodeProperties, "saml.ssl.required",
			_toNullableString(samlProvider.getSslRequired()),
			useSystemDefaultsForNulls);
	}

	private void _setSpProperties(
			String entityId, Sp sp, UnicodeProperties unicodeProperties,
			boolean useSystemDefaultsForNulls)
		throws Exception {

		if (sp.getKeyStoreEncryptionCredentialPassword() != null) {
			_authenticateLocalEntityCertificate(
				sp.getKeyStoreEncryptionCredentialPassword(),
				LocalEntityManager.CertificateUsage.ENCRYPTION, entityId);
		}

		_setProperty(
			unicodeProperties, "saml.sp.allow.showing.the.login.portlet",
			_toNullableString(sp.getAllowShowingTheLoginPortlet()),
			useSystemDefaultsForNulls);
		_setProperty(
			unicodeProperties, "saml.sp.assertion.signature.required",
			_toNullableString(sp.getAssertionSignatureRequired()),
			useSystemDefaultsForNulls);
		_setProperty(
			unicodeProperties, "saml.sp.clock.skew",
			_toNullableString(sp.getClockSkew()), useSystemDefaultsForNulls);
		_setProperty(
			unicodeProperties, "saml.sp.ldap.import.enabled",
			_toNullableString(sp.getLdapImportEnabled()),
			useSystemDefaultsForNulls);
		_setProperty(
			unicodeProperties, "saml.keystore.encryption.credential.password",
			_toNullableString(sp.getKeyStoreEncryptionCredentialPassword()),
			useSystemDefaultsForNulls);
		_setProperty(
			unicodeProperties, "saml.sp.sign.authn.request",
			_toNullableString(sp.getSignAuthnRequest()),
			useSystemDefaultsForNulls);

		unicodeProperties.put(
			"saml.role", SamlProviderConfigurationKeys.SAML_ROLE_SP);
	}

	private String _toNullableString(Object value) {
		if (value == null) {
			return null;
		}

		return String.valueOf(value);
	}

	private SamlProvider _updateSamlProvider(
			SamlProvider samlProvider, boolean useSystemDefaultsForNulls)
		throws Exception {

		UnicodeProperties unicodeProperties = UnicodePropertiesBuilder.create(
			false
		).build();

		_setSamlProviderProperties(
			samlProvider, unicodeProperties, useSystemDefaultsForNulls);

		String entityId = samlProvider.getEntityId();

		if (Validator.isNotNull(entityId)) {
			if (entityId.length() > 1024) {
				throw new EntityIdException(
					"EntityID too long (Max 1024 characters)");
			}
		}
		else {
			SamlProviderConfiguration samlProviderConfiguration =
				_samlProviderConfigurationHelper.getSamlProviderConfiguration();

			entityId = samlProviderConfiguration.entityId();
		}

		if (GetterUtil.getBoolean(samlProvider.getEnabled()) ||
			!Validator.isBlank(samlProvider.getKeyStoreCredentialPassword())) {

			_authenticateLocalEntityCertificate(
				samlProvider.getKeyStoreCredentialPassword(),
				LocalEntityManager.CertificateUsage.SIGNING, entityId);
		}

		SamlProvider currentSamlProvider = getSamlProvider();

		if (samlProvider.getIdp() != null) {
			if (!_validateRoleSelection(
					samlProvider.getEnabled(),
					SamlProvider.Role.IDP.getValue())) {

				throw new ConfigurationException(
					"The Identity Provider role has been disabled. It can be " +
						"re-enabled in system settings.");
			}

			if ((samlProvider.getSp() != null) ||
				(!useSystemDefaultsForNulls &&
				 (currentSamlProvider.getSp() != null))) {

				throw new ConfigurationException(
					"Can only configure one of sp & idp roles");
			}

			_setIdpProperties(
				samlProvider.getIdp(), unicodeProperties,
				useSystemDefaultsForNulls);
		}
		else if (samlProvider.getSp() != null) {
			if (!useSystemDefaultsForNulls &&
				(currentSamlProvider.getIdp() != null)) {

				throw new ConfigurationException(
					"Can only configure one of sp & idp roles");
			}

			_setSpProperties(
				entityId, samlProvider.getSp(), unicodeProperties,
				useSystemDefaultsForNulls);
		}
		else if (GetterUtil.getBoolean(samlProvider.getEnabled()) &&
				 (currentSamlProvider.getRole() == null)) {

			throw new ConfigurationException(
				"Cannot enable the provider without configuring its role");
		}

		_samlProviderConfigurationHelper.updateProperties(unicodeProperties);

		return getSamlProvider();
	}

	private boolean _validateRoleSelection(boolean enabled, String samlRole) {
		if (_samlConfiguration.idpRoleConfigurationEnabled()) {
			return true;
		}

		if (!_samlProviderConfigurationHelper.isRoleIdp() &&
			samlRole.equals(SamlProviderConfigurationKeys.SAML_ROLE_IDP)) {

			return false;
		}

		if (!_samlProviderConfigurationHelper.isEnabled() && enabled &&
			samlRole.equals(SamlProviderConfigurationKeys.SAML_ROLE_IDP)) {

			return false;
		}

		return true;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		SamlProviderResourceImpl.class);

	@Reference
	private LocalEntityManager _localEntityManager;

	private SamlConfiguration _samlConfiguration;

	@Reference
	private SamlProviderConfigurationHelper _samlProviderConfigurationHelper;

	private ServiceRegistration<?> _serviceRegistration;
	private Dictionary<String, ?> _systemProperties;

	private class SystemConfigurationManagedServiceFactory
		implements ManagedServiceFactory {

		@Override
		public void deleted(String pid) {
			if ((_systemProperties != null) &&
				Objects.equals(
					_systemProperties.get(Constants.SERVICE_PID), pid)) {

				_systemProperties = null;
			}
		}

		@Override
		public String getName() {
			return SystemConfigurationManagedServiceFactory.class.getName();
		}

		@Override
		public void updated(String pid, Dictionary<String, ?> properties) {
			if (GetterUtil.getLong(properties.get("companyId")) ==
					CompanyConstants.SYSTEM) {

				_systemProperties = properties;
			}
		}

	}

}