/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.saml.persistence.internal.model.listener;

import com.liferay.portal.kernel.bean.BeanProperties;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.saml.persistence.model.SamlIdpSpConnection;
import com.liferay.saml.persistence.model.SamlPeerBinding;
import com.liferay.saml.persistence.model.SamlSpIdpConnection;
import com.liferay.saml.persistence.service.SamlIdpSpConnectionLocalService;
import com.liferay.saml.persistence.service.SamlPeerBindingLocalService;
import com.liferay.saml.persistence.service.SamlSpIdpConnectionLocalService;
import com.liferay.saml.runtime.configuration.SamlProviderConfigurationHelper;

import java.util.List;
import java.util.Objects;

import org.opensaml.saml.saml2.core.NameIDType;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Christopher Kian
 */
@Component(service = ModelListener.class)
public class UserModelListener extends BaseModelListener<User> {

	@Override
	public void onAfterUpdate(User originalUser, User user)
		throws ModelListenerException {

		try {
			if (!_samlProviderConfigurationHelper.isEnabled()) {
				return;
			}

			List<SamlPeerBinding> samlPeerBindings =
				_samlPeerBindingLocalService.getUserSamlPeerBindings(
					originalUser.getCompanyId(), originalUser.getUserId(),
					false);

			for (SamlPeerBinding samlPeerBinding : samlPeerBindings) {
				String nameIdAttribute = "emailAddress";
				String samlPeerEntityId = samlPeerBinding.getSamlPeerEntityId();

				if (_samlProviderConfigurationHelper.isRoleIdp()) {
					SamlIdpSpConnection samlIdpSpConnection =
						_samlIdpSpConnectionLocalService.getSamlIdpSpConnection(
							user.getCompanyId(), samlPeerEntityId);

					nameIdAttribute = samlIdpSpConnection.getNameIdAttribute();
				}
				else {
					SamlSpIdpConnection samlSpIdpConnection =
						_samlSpIdpConnectionLocalService.getSamlSpIdpConnection(
							user.getCompanyId(), samlPeerEntityId);

					String userIdentifierExpression =
						samlSpIdpConnection.getUserIdentifierExpression();

					if (Validator.isNull(userIdentifierExpression) ||
						userIdentifierExpression.equals("none")) {

						continue;
					}
					else if (userIdentifierExpression.equals("dynamic")) {
						if (!Objects.equals(
								samlSpIdpConnection.getNameIdFormat(),
								NameIDType.EMAIL)) {

							nameIdAttribute = "screenName";
						}
					}
					else {
						nameIdAttribute = userIdentifierExpression.substring(
							"attribute:".length());
					}
				}

				if (nameIdAttribute.startsWith("static:") ||
					nameIdAttribute.startsWith("expando:")) {

					continue;
				}

				if (!Objects.equals(
						_beanProperties.getObject(user, nameIdAttribute),
						_beanProperties.getObject(
							originalUser, nameIdAttribute))) {

					samlPeerBinding.setDeleted(true);

					_samlPeerBindingLocalService.updateSamlPeerBinding(
						samlPeerBinding);
				}
			}
		}
		catch (Exception exception) {
			throw new ModelListenerException(exception);
		}
	}

	@Reference
	private BeanProperties _beanProperties;

	@Reference
	private SamlIdpSpConnectionLocalService _samlIdpSpConnectionLocalService;

	@Reference
	private SamlPeerBindingLocalService _samlPeerBindingLocalService;

	@Reference
	private SamlProviderConfigurationHelper _samlProviderConfigurationHelper;

	@Reference
	private SamlSpIdpConnectionLocalService _samlSpIdpConnectionLocalService;

}