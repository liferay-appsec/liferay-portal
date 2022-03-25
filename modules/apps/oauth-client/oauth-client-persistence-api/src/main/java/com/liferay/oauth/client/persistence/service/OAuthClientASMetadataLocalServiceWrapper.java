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

import com.liferay.portal.kernel.service.ServiceWrapper;

/**
 * Provides a wrapper for {@link OAuthClientASMetadataLocalService}.
 *
 * @author Brian Wing Shun Chan
 * @see OAuthClientASMetadataLocalService
 * @generated
 */
public class OAuthClientASMetadataLocalServiceWrapper
	implements OAuthClientASMetadataLocalService,
			   ServiceWrapper<OAuthClientASMetadataLocalService> {

	public OAuthClientASMetadataLocalServiceWrapper() {
		this(null);
	}

	public OAuthClientASMetadataLocalServiceWrapper(
		OAuthClientASMetadataLocalService oAuthClientASMetadataLocalService) {

		_oAuthClientASMetadataLocalService = oAuthClientASMetadataLocalService;
	}

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
	@Override
	public com.liferay.oauth.client.persistence.model.OAuthClientASMetadata
		addOAuthClientASMetadata(
			com.liferay.oauth.client.persistence.model.OAuthClientASMetadata
				oAuthClientASMetadata) {

		return _oAuthClientASMetadataLocalService.addOAuthClientASMetadata(
			oAuthClientASMetadata);
	}

	/**
	 * Creates a new o auth client as metadata with the primary key. Does not add the o auth client as metadata to the database.
	 *
	 * @param oAuthClientASMetadataId the primary key for the new o auth client as metadata
	 * @return the new o auth client as metadata
	 */
	@Override
	public com.liferay.oauth.client.persistence.model.OAuthClientASMetadata
		createOAuthClientASMetadata(long oAuthClientASMetadataId) {

		return _oAuthClientASMetadataLocalService.createOAuthClientASMetadata(
			oAuthClientASMetadataId);
	}

	/**
	 * @throws PortalException
	 */
	@Override
	public com.liferay.portal.kernel.model.PersistedModel createPersistedModel(
			java.io.Serializable primaryKeyObj)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _oAuthClientASMetadataLocalService.createPersistedModel(
			primaryKeyObj);
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
	@Override
	public com.liferay.oauth.client.persistence.model.OAuthClientASMetadata
			deleteOAuthClientASMetadata(long oAuthClientASMetadataId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _oAuthClientASMetadataLocalService.deleteOAuthClientASMetadata(
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
	@Override
	public com.liferay.oauth.client.persistence.model.OAuthClientASMetadata
		deleteOAuthClientASMetadata(
			com.liferay.oauth.client.persistence.model.OAuthClientASMetadata
				oAuthClientASMetadata) {

		return _oAuthClientASMetadataLocalService.deleteOAuthClientASMetadata(
			oAuthClientASMetadata);
	}

	/**
	 * @throws PortalException
	 */
	@Override
	public com.liferay.portal.kernel.model.PersistedModel deletePersistedModel(
			com.liferay.portal.kernel.model.PersistedModel persistedModel)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _oAuthClientASMetadataLocalService.deletePersistedModel(
			persistedModel);
	}

	@Override
	public <T> T dslQuery(com.liferay.petra.sql.dsl.query.DSLQuery dslQuery) {
		return _oAuthClientASMetadataLocalService.dslQuery(dslQuery);
	}

	@Override
	public int dslQueryCount(
		com.liferay.petra.sql.dsl.query.DSLQuery dslQuery) {

		return _oAuthClientASMetadataLocalService.dslQueryCount(dslQuery);
	}

	@Override
	public com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery() {
		return _oAuthClientASMetadataLocalService.dynamicQuery();
	}

	/**
	 * Performs a dynamic query on the database and returns the matching rows.
	 *
	 * @param dynamicQuery the dynamic query
	 * @return the matching rows
	 */
	@Override
	public <T> java.util.List<T> dynamicQuery(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery) {

		return _oAuthClientASMetadataLocalService.dynamicQuery(dynamicQuery);
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
	@Override
	public <T> java.util.List<T> dynamicQuery(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery, int start,
		int end) {

		return _oAuthClientASMetadataLocalService.dynamicQuery(
			dynamicQuery, start, end);
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
	@Override
	public <T> java.util.List<T> dynamicQuery(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery, int start,
		int end,
		com.liferay.portal.kernel.util.OrderByComparator<T> orderByComparator) {

		return _oAuthClientASMetadataLocalService.dynamicQuery(
			dynamicQuery, start, end, orderByComparator);
	}

	/**
	 * Returns the number of rows matching the dynamic query.
	 *
	 * @param dynamicQuery the dynamic query
	 * @return the number of rows matching the dynamic query
	 */
	@Override
	public long dynamicQueryCount(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery) {

		return _oAuthClientASMetadataLocalService.dynamicQueryCount(
			dynamicQuery);
	}

	/**
	 * Returns the number of rows matching the dynamic query.
	 *
	 * @param dynamicQuery the dynamic query
	 * @param projection the projection to apply to the query
	 * @return the number of rows matching the dynamic query
	 */
	@Override
	public long dynamicQueryCount(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery,
		com.liferay.portal.kernel.dao.orm.Projection projection) {

		return _oAuthClientASMetadataLocalService.dynamicQueryCount(
			dynamicQuery, projection);
	}

	@Override
	public com.liferay.oauth.client.persistence.model.OAuthClientASMetadata
		fetchOAuthClientASMetadata(long oAuthClientASMetadataId) {

		return _oAuthClientASMetadataLocalService.fetchOAuthClientASMetadata(
			oAuthClientASMetadataId);
	}

	@Override
	public com.liferay.oauth.client.persistence.model.OAuthClientASMetadata
		fetchOAuthClientASMetadata(long companyId, String issuer) {

		return _oAuthClientASMetadataLocalService.fetchOAuthClientASMetadata(
			companyId, issuer);
	}

	@Override
	public com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery
		getActionableDynamicQuery() {

		return _oAuthClientASMetadataLocalService.getActionableDynamicQuery();
	}

	@Override
	public com.liferay.portal.kernel.dao.orm.IndexableActionableDynamicQuery
		getIndexableActionableDynamicQuery() {

		return _oAuthClientASMetadataLocalService.
			getIndexableActionableDynamicQuery();
	}

	/**
	 * Returns the o auth client as metadata with the primary key.
	 *
	 * @param oAuthClientASMetadataId the primary key of the o auth client as metadata
	 * @return the o auth client as metadata
	 * @throws PortalException if a o auth client as metadata with the primary key could not be found
	 */
	@Override
	public com.liferay.oauth.client.persistence.model.OAuthClientASMetadata
			getOAuthClientASMetadata(long oAuthClientASMetadataId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _oAuthClientASMetadataLocalService.getOAuthClientASMetadata(
			oAuthClientASMetadataId);
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
	@Override
	public java.util.List
		<com.liferay.oauth.client.persistence.model.OAuthClientASMetadata>
			getOAuthClientASMetadatas(int start, int end) {

		return _oAuthClientASMetadataLocalService.getOAuthClientASMetadatas(
			start, end);
	}

	@Override
	public java.util.List
		<com.liferay.oauth.client.persistence.model.OAuthClientASMetadata>
			getOAuthClientASMetadatas(long companyId) {

		return _oAuthClientASMetadataLocalService.getOAuthClientASMetadatas(
			companyId);
	}

	@Override
	public java.util.List
		<com.liferay.oauth.client.persistence.model.OAuthClientASMetadata>
			getOAuthClientASMetadatas(long companyId, long userId) {

		return _oAuthClientASMetadataLocalService.getOAuthClientASMetadatas(
			companyId, userId);
	}

	/**
	 * Returns the number of o auth client as metadatas.
	 *
	 * @return the number of o auth client as metadatas
	 */
	@Override
	public int getOAuthClientASMetadatasCount() {
		return _oAuthClientASMetadataLocalService.
			getOAuthClientASMetadatasCount();
	}

	/**
	 * Returns the OSGi service identifier.
	 *
	 * @return the OSGi service identifier
	 */
	@Override
	public String getOSGiServiceIdentifier() {
		return _oAuthClientASMetadataLocalService.getOSGiServiceIdentifier();
	}

	/**
	 * @throws PortalException
	 */
	@Override
	public com.liferay.portal.kernel.model.PersistedModel getPersistedModel(
			java.io.Serializable primaryKeyObj)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _oAuthClientASMetadataLocalService.getPersistedModel(
			primaryKeyObj);
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
	@Override
	public com.liferay.oauth.client.persistence.model.OAuthClientASMetadata
		updateOAuthClientASMetadata(
			com.liferay.oauth.client.persistence.model.OAuthClientASMetadata
				oAuthClientASMetadata) {

		return _oAuthClientASMetadataLocalService.updateOAuthClientASMetadata(
			oAuthClientASMetadata);
	}

	@Override
	public OAuthClientASMetadataLocalService getWrappedService() {
		return _oAuthClientASMetadataLocalService;
	}

	@Override
	public void setWrappedService(
		OAuthClientASMetadataLocalService oAuthClientASMetadataLocalService) {

		_oAuthClientASMetadataLocalService = oAuthClientASMetadataLocalService;
	}

	private OAuthClientASMetadataLocalService
		_oAuthClientASMetadataLocalService;

}