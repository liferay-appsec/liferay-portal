/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.change.tracking.web.internal.model.listener;

import com.liferay.change.tracking.model.CTCollection;
import com.liferay.change.tracking.model.CTRemote;
import com.liferay.change.tracking.rest.client.resource.v1_0.CTCollectionResource;
import com.liferay.change.tracking.service.CTRemoteLocalService;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.security.key.secret.SecretContext;
import com.liferay.portal.security.key.secret.SecretResolver;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author David Truong
 */
@Component(service = ModelListener.class)
public class CTCollectionModelListener extends BaseModelListener<CTCollection> {

	@Override
	public void onAfterCreate(CTCollection ctCollection)
		throws ModelListenerException {

		if (ctCollection.getCtRemoteId() == 0) {
			return;
		}

		try {
			CTCollectionResource ctCollectionResource =
				_getCTCollectionResource(ctCollection);

			ctCollectionResource.postCTCollection(
				new com.liferay.change.tracking.rest.client.dto.v1_0.
					CTCollection() {

					{
						setDescription(ctCollection::getDescription);
						setExternalReferenceCode(
							ctCollection::getExternalReferenceCode);
						setName(ctCollection::getName);
					}
				});
		}
		catch (Exception exception) {
			throw new ModelListenerException(exception);
		}
	}

	@Override
	public void onAfterRemove(CTCollection ctCollection)
		throws ModelListenerException {

		if (ctCollection.getCtRemoteId() == 0) {
			return;
		}

		try {
			CTCollectionResource ctCollectionResource =
				_getCTCollectionResource(ctCollection);

			ctCollectionResource.deleteCTCollectionByExternalReferenceCode(
				ctCollection.getExternalReferenceCode());
		}
		catch (Exception exception) {
			throw new ModelListenerException(exception);
		}
	}

	@Override
	public void onAfterUpdate(
			CTCollection oldCTCollection, CTCollection ctCollection)
		throws ModelListenerException {

		if (ctCollection.getCtRemoteId() == 0) {
			return;
		}

		try {
			CTCollectionResource ctCollectionResource =
				_getCTCollectionResource(ctCollection);

			ctCollectionResource.patchCTCollectionByExternalReferenceCode(
				ctCollection.getExternalReferenceCode(),
				new com.liferay.change.tracking.rest.client.dto.v1_0.
					CTCollection() {

					{
						setDescription(ctCollection::getDescription);
						setExternalReferenceCode(
							ctCollection::getExternalReferenceCode);
						setName(ctCollection::getName);
					}
				});
		}
		catch (Exception exception) {
			throw new ModelListenerException(exception);
		}
	}

	private CTCollectionResource _getCTCollectionResource(
			CTCollection ctCollection)
		throws Exception {

		CTRemote ctRemote = _ctRemoteLocalService.fetchCTRemote(
			ctCollection.getCtRemoteId());

		CTCollectionResource.Builder builder = CTCollectionResource.builder();

		return builder.endpoint(
			ctRemote.getUrl(), "http"
		).bearerToken(
			_getToken(ctCollection.getCompanyId(), ctRemote)
		).build();
	}

	private String _getToken(long companyId, CTRemote ctRemote)
		throws Exception {

		Http.Options options = new Http.Options();

		options.setLocation(ctRemote.getUrl() + "/o/oauth2/token");
		options.setPost(true);

		SecretContext secretContext = SecretContext.createEntitySecretContext(
			CTRemote.class, ctRemote.getCompanyId(), "clientSecret",
			ctRemote.getPrimaryKey());

		options.addPart("client_id", ctRemote.getClientId());
		options.addPart(
			"client_secret",
			_secretResolver.resolve(
				companyId, secretContext, ctRemote.getClientSecret()));
		options.addPart("grant_type", "client_credentials");

		JSONObject jsonObject = _jsonFactory.createJSONObject(
			_http.URLtoString(options));

		return jsonObject.getString("access_token");
	}

	@Reference
	private CTRemoteLocalService _ctRemoteLocalService;

	@Reference
	private Http _http;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private SecretResolver _secretResolver;

}