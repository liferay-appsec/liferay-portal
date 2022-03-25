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

package com.liferay.oauth.client.persistence.service;

import com.liferay.oauth.client.persistence.model.OAuthClientASMetadata;
import com.liferay.petra.sql.dsl.query.DSLQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.PersistedModel;
import com.liferay.portal.kernel.util.OrderByComparator;

import java.io.Serializable;

import java.util.List;

/**
 * Provides the local service utility for OAuthClientASMetadata. This utility wraps
 * <code>com.liferay.oauth.client.persistence.service.impl.OAuthClientASMetadataLocalServiceImpl</code> and
 * is an access point for service operations in application layer code running
 * on the local server. Methods of this service will not have security checks
 * based on the propagated JAAS credentials because this service can only be
 * accessed from within the same VM.
 *
 * @author Brian Wing Shun Chan
 * @see OAuthClientASMetadataLocalService
 * @generated
 */
public class OAuthClientASMetadataLocalServiceUtil {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify this class directly. Add custom service methods to <code>com.liferay.oauth.client.persistence.service.impl.OAuthClientASMetadataLocalServiceImpl</code> and rerun ServiceBuilder to regenerate this class.
	 */

	/**
	 * Adds the o auth client as metadata to the database. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect OAuthClientASMetadataLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param oAuthClientASMetadata the o auth client as metadata
	 * @return the o auth client as metadata that was added
	 */
	public static OAuthClientASMetadata addOAuthClientASMetadata(
		OAuthClientASMetadata oAuthClientASMetadata) {

		return getService().addOAuthClientASMetadata(oAuthClientASMetadata);
	}

	/**
	 * Creates a new o auth client as metadata with the primary key. Does not add the o auth client as metadata to the database.
	 *
	 * @param oAuthClientASMetadataId the primary key for the new o auth client as metadata
	 * @return the new o auth client as metadata
	 */
	public static OAuthClientASMetadata createOAuthClientASMetadata(
		long oAuthClientASMetadataId) {

		return getService().createOAuthClientASMetadata(
			oAuthClientASMetadataId);
	}

	/**
	 * @throws PortalException
	 */
	public static PersistedModel createPersistedModel(
			Serializable primaryKeyObj)
		throws PortalException {

		return getService().createPersistedModel(primaryKeyObj);
	}

	/**
	 * Deletes the o auth client as metadata with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect OAuthClientASMetadataLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param oAuthClientASMetadataId the primary key of the o auth client as metadata
	 * @return the o auth client as metadata that was removed
	 * @throws PortalException if a o auth client as metadata with the primary key could not be found
	 */
	public static OAuthClientASMetadata deleteOAuthClientASMetadata(
			long oAuthClientASMetadataId)
		throws PortalException {

		return getService().deleteOAuthClientASMetadata(
			oAuthClientASMetadataId);
	}

	/**
	 * Deletes the o auth client as metadata from the database. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect OAuthClientASMetadataLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param oAuthClientASMetadata the o auth client as metadata
	 * @return the o auth client as metadata that was removed
	 */
	public static OAuthClientASMetadata deleteOAuthClientASMetadata(
		OAuthClientASMetadata oAuthClientASMetadata) {

		return getService().deleteOAuthClientASMetadata(oAuthClientASMetadata);
	}

	/**
	 * @throws PortalException
	 */
	public static PersistedModel deletePersistedModel(
			PersistedModel persistedModel)
		throws PortalException {

		return getService().deletePersistedModel(persistedModel);
	}

	public static <T> T dslQuery(DSLQuery dslQuery) {
		return getService().dslQuery(dslQuery);
	}

	public static int dslQueryCount(DSLQuery dslQuery) {
		return getService().dslQueryCount(dslQuery);
	}

	public static DynamicQuery dynamicQuery() {
		return getService().dynamicQuery();
	}

	/**
	 * Performs a dynamic query on the database and returns the matching rows.
	 *
	 * @param dynamicQuery the dynamic query
	 * @return the matching rows
	 */
	public static <T> List<T> dynamicQuery(DynamicQuery dynamicQuery) {
		return getService().dynamicQuery(dynamicQuery);
	}

	/**
	 * Performs a dynamic query on the database and returns a range of the matching rows.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.oauth.client.persistence.model.impl.OAuthClientASMetadataModelImpl</code>.
	 * </p>
	 *
	 * @param dynamicQuery the dynamic query
	 * @param start the lower bound of the range of model instances
	 * @param end the upper bound of the range of model instances (not inclusive)
	 * @return the range of matching rows
	 */
	public static <T> List<T> dynamicQuery(
		DynamicQuery dynamicQuery, int start, int end) {

		return getService().dynamicQuery(dynamicQuery, start, end);
	}

	/**
	 * Performs a dynamic query on the database and returns an ordered range of the matching rows.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.oauth.client.persistence.model.impl.OAuthClientASMetadataModelImpl</code>.
	 * </p>
	 *
	 * @param dynamicQuery the dynamic query
	 * @param start the lower bound of the range of model instances
	 * @param end the upper bound of the range of model instances (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching rows
	 */
	public static <T> List<T> dynamicQuery(
		DynamicQuery dynamicQuery, int start, int end,
		OrderByComparator<T> orderByComparator) {

		return getService().dynamicQuery(
			dynamicQuery, start, end, orderByComparator);
	}

	/**
	 * Returns the number of rows matching the dynamic query.
	 *
	 * @param dynamicQuery the dynamic query
	 * @return the number of rows matching the dynamic query
	 */
	public static long dynamicQueryCount(DynamicQuery dynamicQuery) {
		return getService().dynamicQueryCount(dynamicQuery);
	}

	/**
	 * Returns the number of rows matching the dynamic query.
	 *
	 * @param dynamicQuery the dynamic query
	 * @param projection the projection to apply to the query
	 * @return the number of rows matching the dynamic query
	 */
	public static long dynamicQueryCount(
		DynamicQuery dynamicQuery,
		com.liferay.portal.kernel.dao.orm.Projection projection) {

		return getService().dynamicQueryCount(dynamicQuery, projection);
	}

	public static OAuthClientASMetadata fetchOAuthClientASMetadata(
		long oAuthClientASMetadataId) {

		return getService().fetchOAuthClientASMetadata(oAuthClientASMetadataId);
	}

	public static OAuthClientASMetadata fetchOAuthClientASMetadata(
		long companyId, String issuer) {

		return getService().fetchOAuthClientASMetadata(companyId, issuer);
	}

	public static com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery
		getActionableDynamicQuery() {

		return getService().getActionableDynamicQuery();
	}

	public static
		com.liferay.portal.kernel.dao.orm.IndexableActionableDynamicQuery
			getIndexableActionableDynamicQuery() {

		return getService().getIndexableActionableDynamicQuery();
	}

	/**
	 * Returns the o auth client as metadata with the primary key.
	 *
	 * @param oAuthClientASMetadataId the primary key of the o auth client as metadata
	 * @return the o auth client as metadata
	 * @throws PortalException if a o auth client as metadata with the primary key could not be found
	 */
	public static OAuthClientASMetadata getOAuthClientASMetadata(
			long oAuthClientASMetadataId)
		throws PortalException {

		return getService().getOAuthClientASMetadata(oAuthClientASMetadataId);
	}

	/**
	 * Returns a range of all the o auth client as metadatas.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.oauth.client.persistence.model.impl.OAuthClientASMetadataModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of o auth client as metadatas
	 * @param end the upper bound of the range of o auth client as metadatas (not inclusive)
	 * @return the range of o auth client as metadatas
	 */
	public static List<OAuthClientASMetadata> getOAuthClientASMetadatas(
		int start, int end) {

		return getService().getOAuthClientASMetadatas(start, end);
	}

	public static List<OAuthClientASMetadata> getOAuthClientASMetadatas(
		long companyId, int start, int end) {

		return getService().getOAuthClientASMetadatas(companyId, start, end);
	}

	public static List<OAuthClientASMetadata> getOAuthClientASMetadatas(
		long companyId, long userId) {

		return getService().getOAuthClientASMetadatas(companyId, userId);
	}

	/**
	 * Returns the number of o auth client as metadatas.
	 *
	 * @return the number of o auth client as metadatas
	 */
	public static int getOAuthClientASMetadatasCount() {
		return getService().getOAuthClientASMetadatasCount();
	}

	/**
	 * Returns the OSGi service identifier.
	 *
	 * @return the OSGi service identifier
	 */
	public static String getOSGiServiceIdentifier() {
		return getService().getOSGiServiceIdentifier();
	}

	/**
	 * @throws PortalException
	 */
	public static PersistedModel getPersistedModel(Serializable primaryKeyObj)
		throws PortalException {

		return getService().getPersistedModel(primaryKeyObj);
	}

	/**
	 * Updates the o auth client as metadata in the database or adds it if it does not yet exist. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect OAuthClientASMetadataLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param oAuthClientASMetadata the o auth client as metadata
	 * @return the o auth client as metadata that was updated
	 */
	public static OAuthClientASMetadata updateOAuthClientASMetadata(
		OAuthClientASMetadata oAuthClientASMetadata) {

		return getService().updateOAuthClientASMetadata(oAuthClientASMetadata);
	}

	public static OAuthClientASMetadataLocalService getService() {
		return _service;
	}

	private static volatile OAuthClientASMetadataLocalService _service;

}