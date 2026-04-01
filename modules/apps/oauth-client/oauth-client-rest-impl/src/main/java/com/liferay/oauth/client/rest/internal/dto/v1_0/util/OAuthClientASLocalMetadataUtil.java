/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth.client.rest.internal.dto.v1_0.util;

import com.liferay.headless.delivery.dto.v1_0.util.CreatorUtil;
import com.liferay.oauth.client.rest.dto.v1_0.OAuthClientASLocalMetadata;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.util.Portal;

/**
 * @author Manuele Castro
 */
public class OAuthClientASLocalMetadataUtil {

	public static OAuthClientASLocalMetadata toOAuthClientASLocalMetadata(
		Portal portal,
		com.liferay.oauth.client.persistence.model.OAuthClientASLocalMetadata
			serviceBuilderOAuthClientASLocalMetadata,
		User user) {

		return new OAuthClientASLocalMetadata() {
			{
				setCreator(() -> CreatorUtil.toCreator(null, portal, user));
				setDateCreated(
					serviceBuilderOAuthClientASLocalMetadata::getCreateDate);
				setDateModified(
					serviceBuilderOAuthClientASLocalMetadata::getModifiedDate);
				setExternalReferenceCode(
					serviceBuilderOAuthClientASLocalMetadata::
						getExternalReferenceCode);
				setIssuer(serviceBuilderOAuthClientASLocalMetadata::getIssuer);
				setLocalWellKnownEnabled(
					serviceBuilderOAuthClientASLocalMetadata::
						getLocalWellKnownEnabled);
				setLocalWellKnownURI(
					serviceBuilderOAuthClientASLocalMetadata::
						getLocalWellKnownURI);
				setMetadataJSON(
					serviceBuilderOAuthClientASLocalMetadata::getMetadataJSON);
				setOAuthASLocalWellKnownURI(
					serviceBuilderOAuthClientASLocalMetadata::
						getOAuthASLocalWellKnownURI);
				setOAuthASMetadataJSON(
					serviceBuilderOAuthClientASLocalMetadata::
						getOAuthASMetadataJSON);
			}
		};
	}

}