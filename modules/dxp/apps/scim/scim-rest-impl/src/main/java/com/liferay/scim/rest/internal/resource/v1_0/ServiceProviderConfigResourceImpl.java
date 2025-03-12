/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.scim.rest.internal.resource.v1_0;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.scim.rest.internal.util.ScimUtil;
import com.liferay.scim.rest.resource.v1_0.ServiceProviderConfigResource;

import javax.ws.rs.core.Response;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import org.wso2.charon3.core.exceptions.AbstractCharonException;
import org.wso2.charon3.core.exceptions.ConflictException;
import org.wso2.charon3.core.exceptions.NotFoundException;
import org.wso2.charon3.core.protocol.ResponseCodeConstants;
import org.wso2.charon3.core.protocol.endpoints.AbstractResourceManager;
import org.wso2.charon3.core.schema.SCIMConstants;

/**
 * @author Olivér Kecskeméty
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/service-provider-config.properties",
	scope = ServiceScope.PROTOTYPE,
	service = ServiceProviderConfigResource.class
)
public class ServiceProviderConfigResourceImpl
	extends BaseServiceProviderConfigResourceImpl {

	@Override
	public Object getV2ServiceProviderConfig() throws Exception {
		ServiceContext serviceContext =
			ServiceContextThreadLocal.getServiceContext();

		Response.ResponseBuilder responseBuilder = Response.status(
			ResponseCodeConstants.CODE_OK);

		responseBuilder.header(
			SCIMConstants.CONTENT_TYPE_HEADER, SCIMConstants.APPLICATION_JSON
		).header(
			SCIMConstants.LOCATION_HEADER,
			AbstractResourceManager.getResourceEndpointURL(
				SCIMConstants.SERVICE_PROVIDER_CONFIG_ENDPOINT)
		);

		try {
			ScimUtil.getScimClientOAuth2ApplicationConfiguration(
				serviceContext.getCompanyId(), _configurationAdmin);
		}
		catch (javax.ws.rs.NotFoundException notFoundException) {
			responseBuilder = Response.status(
				ResponseCodeConstants.CODE_RESOURCE_NOT_FOUND);

			responseBuilder.entity(
				JSONUtil.put(
					"detail", notFoundException.getMessage()
				).put(
					"schemas",
					_jsonFactory.createJSONArray(
						"[\"urn:ietf:params:scim:api:messages:2.0:Error\"]")
				).put(
					"status", "404"
				).toString());

			return responseBuilder.build();
		}

		try {
			responseBuilder.entity(_getServiceProviderConfig());

			return responseBuilder.build();
		}
		catch (AbstractCharonException abstractCharonException) {
			responseBuilder.entity(
				AbstractResourceManager.encodeSCIMException(
					abstractCharonException));

			return responseBuilder.build();
		}
		catch (Exception exception) {
			if (exception instanceof ConflictException) {
				responseBuilder.entity(
					AbstractResourceManager.encodeSCIMException(
						(ConflictException)exception));

				return responseBuilder.build();
			}

			throw exception;
		}
	}

	private String _getServiceProviderConfig()
		throws JSONException, NotFoundException {

		return JSONUtil.put(
			"authenticationSchemes",
			_jsonFactory.createJSONArray(
				StringBundler.concat(
					StringPool.OPEN_BRACKET,
					JSONUtil.put(
						"description",
						"Authentication scheme using the OAuth Bearer Token " +
							"Standard"
					).put(
						"documentationUri", "https://learn.liferay.com"
					).put(
						"name", "OAuth Bearer Token"
					).put(
						"primary", true
					).put(
						"specUri", "http://www.rfc-editor.org/info/rfc6750"
					).put(
						"type", "oauthbearertoken"
					),
					StringPool.CLOSE_BRACKET))
		).put(
			"bulk",
			JSONUtil.put(
				"maxOperations", 0
			).put(
				"maxPayloadSize", 0
			).put(
				"supported", false
			)
		).put(
			"changePassword", JSONUtil.put("supported", false)
		).put(
			"documentationUri",
			StringBundler.concat(
				"https://learn.liferay.com/w/dxp/installation-and-upgrades",
				"/securing-liferay",
				"/system-for-cross-domain-identity-management-scim")
		).put(
			"etag", JSONUtil.put("supported", false)
		).put(
			"filter",
			JSONUtil.put(
				"maxResults", 100
			).put(
				"supported", true
			)
		).put(
			"meta",
			JSONUtil.put(
				"created", "2025-03-13T00:00Z"
			).put(
				"lastModified", "2025-03-13T00:00Z"
			).put(
				"location",
				AbstractResourceManager.getResourceEndpointURL(
					SCIMConstants.SERVICE_PROVIDER_CONFIG_ENDPOINT)
			).put(
				"resourceType", "ServiceProviderConfig"
			).put(
				"version", "1"
			)
		).put(
			"patch", JSONUtil.put("supported", true)
		).put(
			"schemas",
			_jsonFactory.createJSONArray(
				"[\"urn:ietf:params:scim:schemas:core:2.0:" +
					"ServiceProviderConfig\"]")
		).put(
			"sort", JSONUtil.put("supported", false)
		).toString();
	}

	@Reference
	private ConfigurationAdmin _configurationAdmin;

	@Reference
	private JSONFactory _jsonFactory;

}