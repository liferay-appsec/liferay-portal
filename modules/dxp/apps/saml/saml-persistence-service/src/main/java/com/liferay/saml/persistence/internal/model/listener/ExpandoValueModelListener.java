/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.saml.persistence.internal.model.listener;

import com.liferay.expando.kernel.model.ExpandoColumn;
import com.liferay.expando.kernel.model.ExpandoTable;
import com.liferay.expando.kernel.model.ExpandoTableConstants;
import com.liferay.expando.kernel.model.ExpandoValue;
import com.liferay.expando.kernel.service.ExpandoColumnLocalService;
import com.liferay.expando.kernel.service.ExpandoTableLocalService;
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

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Christopher Kian
 */
@Component(service = ModelListener.class)
public class ExpandoValueModelListener extends BaseModelListener<ExpandoValue> {

	@Override
	public void onAfterUpdate(
			ExpandoValue originalExpandoValue, ExpandoValue expandoValue)
		throws ModelListenerException {

		try {
			if (!Objects.equals(
					expandoValue.getClassName(), User.class.getName()) ||
				!_samlProviderConfigurationHelper.isEnabled()) {

				return;
			}

			ExpandoTable expandoTable =
				_expandoTableLocalService.getExpandoTable(
					expandoValue.getTableId());

			if (!Objects.equals(
					expandoTable.getName(),
					ExpandoTableConstants.DEFAULT_TABLE_NAME)) {

				return;
			}

			ExpandoColumn expandoColumn = null;

			List<SamlPeerBinding> samlPeerBindings =
				_samlPeerBindingLocalService.getUserSamlPeerBindings(
					expandoValue.getCompanyId(), expandoValue.getClassPK(),
					false);

			for (SamlPeerBinding samlPeerBinding : samlPeerBindings) {
				String nameIdAttribute;
				String samlPeerEntityId = samlPeerBinding.getSamlPeerEntityId();

				if (_samlProviderConfigurationHelper.isRoleIdp()) {
					SamlIdpSpConnection samlIdpSpConnection =
						_samlIdpSpConnectionLocalService.getSamlIdpSpConnection(
							expandoValue.getCompanyId(), samlPeerEntityId);

					nameIdAttribute = samlIdpSpConnection.getNameIdAttribute();
				}
				else {
					SamlSpIdpConnection samlSpIdpConnection =
						_samlSpIdpConnectionLocalService.getSamlSpIdpConnection(
							expandoValue.getCompanyId(), samlPeerEntityId);

					String userIdentifierExpression =
						samlSpIdpConnection.getUserIdentifierExpression();

					if (Validator.isNull(userIdentifierExpression) ||
						!userIdentifierExpression.startsWith("attribute:")) {

						continue;
					}

					nameIdAttribute = userIdentifierExpression.substring(
						"attribute:".length());
				}

				if (nameIdAttribute.startsWith("expando:")) {
					String attributeName = nameIdAttribute.substring(
						"expando:".length());

					if (expandoColumn == null) {
						expandoColumn = _expandoColumnLocalService.getColumn(
							expandoValue.getColumnId());
					}

					if (Objects.equals(
							attributeName, expandoColumn.getName())) {

						samlPeerBinding.setDeleted(true);

						_samlPeerBindingLocalService.updateSamlPeerBinding(
							samlPeerBinding);
					}
				}
			}
		}
		catch (Exception exception) {
			throw new ModelListenerException(exception);
		}
	}

	@Reference
	private ExpandoColumnLocalService _expandoColumnLocalService;

	@Reference
	private ExpandoTableLocalService _expandoTableLocalService;

	@Reference
	private SamlIdpSpConnectionLocalService _samlIdpSpConnectionLocalService;

	@Reference
	private SamlPeerBindingLocalService _samlPeerBindingLocalService;

	@Reference
	private SamlProviderConfigurationHelper _samlProviderConfigurationHelper;

	@Reference
	private SamlSpIdpConnectionLocalService _samlSpIdpConnectionLocalService;

}