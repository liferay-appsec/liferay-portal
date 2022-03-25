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

package com.liferay.oauth.client.persistence.service.persistence;

import com.liferay.oauth.client.persistence.model.OAuthClientASMetadata;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.OrderByComparator;

import java.io.Serializable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The persistence utility for the o auth client as metadata service. This utility wraps <code>com.liferay.oauth.client.persistence.service.persistence.impl.OAuthClientASMetadataPersistenceImpl</code> and provides direct access to the database for CRUD operations. This utility should only be used by the service layer, as it must operate within a transaction. Never access this utility in a JSP, controller, model, or other front-end class.
 *
 * <p>
 * Caching information and settings can be found in <code>portal.properties</code>
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @see OAuthClientASMetadataPersistence
 * @generated
 */
public class OAuthClientASMetadataUtil {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify this class directly. Modify <code>service.xml</code> and rerun ServiceBuilder to regenerate this class.
	 */

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#clearCache()
	 */
	public static void clearCache() {
		getPersistence().clearCache();
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#clearCache(com.liferay.portal.kernel.model.BaseModel)
	 */
	public static void clearCache(OAuthClientASMetadata oAuthClientASMetadata) {
		getPersistence().clearCache(oAuthClientASMetadata);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#countWithDynamicQuery(DynamicQuery)
	 */
	public static long countWithDynamicQuery(DynamicQuery dynamicQuery) {
		return getPersistence().countWithDynamicQuery(dynamicQuery);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#fetchByPrimaryKeys(Set)
	 */
	public static Map<Serializable, OAuthClientASMetadata> fetchByPrimaryKeys(
		Set<Serializable> primaryKeys) {

		return getPersistence().fetchByPrimaryKeys(primaryKeys);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#findWithDynamicQuery(DynamicQuery)
	 */
	public static List<OAuthClientASMetadata> findWithDynamicQuery(
		DynamicQuery dynamicQuery) {

		return getPersistence().findWithDynamicQuery(dynamicQuery);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#findWithDynamicQuery(DynamicQuery, int, int)
	 */
	public static List<OAuthClientASMetadata> findWithDynamicQuery(
		DynamicQuery dynamicQuery, int start, int end) {

		return getPersistence().findWithDynamicQuery(dynamicQuery, start, end);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#findWithDynamicQuery(DynamicQuery, int, int, OrderByComparator)
	 */
	public static List<OAuthClientASMetadata> findWithDynamicQuery(
		DynamicQuery dynamicQuery, int start, int end,
		OrderByComparator<OAuthClientASMetadata> orderByComparator) {

		return getPersistence().findWithDynamicQuery(
			dynamicQuery, start, end, orderByComparator);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#update(com.liferay.portal.kernel.model.BaseModel)
	 */
	public static OAuthClientASMetadata update(
		OAuthClientASMetadata oAuthClientASMetadata) {

		return getPersistence().update(oAuthClientASMetadata);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#update(com.liferay.portal.kernel.model.BaseModel, ServiceContext)
	 */
	public static OAuthClientASMetadata update(
		OAuthClientASMetadata oAuthClientASMetadata,
		ServiceContext serviceContext) {

		return getPersistence().update(oAuthClientASMetadata, serviceContext);
	}

	/**
	 * Returns all the o auth client as metadatas where companyId = &#63;.
	 *
	 * @param companyId the company ID
	 * @return the matching o auth client as metadatas
	 */
	public static List<OAuthClientASMetadata> findByCompanyId(long companyId) {
		return getPersistence().findByCompanyId(companyId);
	}

	/**
	 * Returns a range of all the o auth client as metadatas where companyId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>OAuthClientASMetadataModelImpl</code>.
	 * </p>
	 *
	 * @param companyId the company ID
	 * @param start the lower bound of the range of o auth client as metadatas
	 * @param end the upper bound of the range of o auth client as metadatas (not inclusive)
	 * @return the range of matching o auth client as metadatas
	 */
	public static List<OAuthClientASMetadata> findByCompanyId(
		long companyId, int start, int end) {

		return getPersistence().findByCompanyId(companyId, start, end);
	}

	/**
	 * Returns an ordered range of all the o auth client as metadatas where companyId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>OAuthClientASMetadataModelImpl</code>.
	 * </p>
	 *
	 * @param companyId the company ID
	 * @param start the lower bound of the range of o auth client as metadatas
	 * @param end the upper bound of the range of o auth client as metadatas (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching o auth client as metadatas
	 */
	public static List<OAuthClientASMetadata> findByCompanyId(
		long companyId, int start, int end,
		OrderByComparator<OAuthClientASMetadata> orderByComparator) {

		return getPersistence().findByCompanyId(
			companyId, start, end, orderByComparator);
	}

	/**
	 * Returns an ordered range of all the o auth client as metadatas where companyId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>OAuthClientASMetadataModelImpl</code>.
	 * </p>
	 *
	 * @param companyId the company ID
	 * @param start the lower bound of the range of o auth client as metadatas
	 * @param end the upper bound of the range of o auth client as metadatas (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching o auth client as metadatas
	 */
	public static List<OAuthClientASMetadata> findByCompanyId(
		long companyId, int start, int end,
		OrderByComparator<OAuthClientASMetadata> orderByComparator,
		boolean useFinderCache) {

		return getPersistence().findByCompanyId(
			companyId, start, end, orderByComparator, useFinderCache);
	}

	/**
	 * Returns the first o auth client as metadata in the ordered set where companyId = &#63;.
	 *
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching o auth client as metadata
	 * @throws NoSuchASMetadataException if a matching o auth client as metadata could not be found
	 */
	public static OAuthClientASMetadata findByCompanyId_First(
			long companyId,
			OrderByComparator<OAuthClientASMetadata> orderByComparator)
		throws com.liferay.oauth.client.persistence.exception.
			NoSuchASMetadataException {

		return getPersistence().findByCompanyId_First(
			companyId, orderByComparator);
	}

	/**
	 * Returns the first o auth client as metadata in the ordered set where companyId = &#63;.
	 *
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching o auth client as metadata, or <code>null</code> if a matching o auth client as metadata could not be found
	 */
	public static OAuthClientASMetadata fetchByCompanyId_First(
		long companyId,
		OrderByComparator<OAuthClientASMetadata> orderByComparator) {

		return getPersistence().fetchByCompanyId_First(
			companyId, orderByComparator);
	}

	/**
	 * Returns the last o auth client as metadata in the ordered set where companyId = &#63;.
	 *
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching o auth client as metadata
	 * @throws NoSuchASMetadataException if a matching o auth client as metadata could not be found
	 */
	public static OAuthClientASMetadata findByCompanyId_Last(
			long companyId,
			OrderByComparator<OAuthClientASMetadata> orderByComparator)
		throws com.liferay.oauth.client.persistence.exception.
			NoSuchASMetadataException {

		return getPersistence().findByCompanyId_Last(
			companyId, orderByComparator);
	}

	/**
	 * Returns the last o auth client as metadata in the ordered set where companyId = &#63;.
	 *
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching o auth client as metadata, or <code>null</code> if a matching o auth client as metadata could not be found
	 */
	public static OAuthClientASMetadata fetchByCompanyId_Last(
		long companyId,
		OrderByComparator<OAuthClientASMetadata> orderByComparator) {

		return getPersistence().fetchByCompanyId_Last(
			companyId, orderByComparator);
	}

	/**
	 * Returns the o auth client as metadatas before and after the current o auth client as metadata in the ordered set where companyId = &#63;.
	 *
	 * @param oAuthClientASMetadataId the primary key of the current o auth client as metadata
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next o auth client as metadata
	 * @throws NoSuchASMetadataException if a o auth client as metadata with the primary key could not be found
	 */
	public static OAuthClientASMetadata[] findByCompanyId_PrevAndNext(
			long oAuthClientASMetadataId, long companyId,
			OrderByComparator<OAuthClientASMetadata> orderByComparator)
		throws com.liferay.oauth.client.persistence.exception.
			NoSuchASMetadataException {

		return getPersistence().findByCompanyId_PrevAndNext(
			oAuthClientASMetadataId, companyId, orderByComparator);
	}

	/**
	 * Removes all the o auth client as metadatas where companyId = &#63; from the database.
	 *
	 * @param companyId the company ID
	 */
	public static void removeByCompanyId(long companyId) {
		getPersistence().removeByCompanyId(companyId);
	}

	/**
	 * Returns the number of o auth client as metadatas where companyId = &#63;.
	 *
	 * @param companyId the company ID
	 * @return the number of matching o auth client as metadatas
	 */
	public static int countByCompanyId(long companyId) {
		return getPersistence().countByCompanyId(companyId);
	}

	/**
	 * Returns all the o auth client as metadatas where companyId = &#63; and userId = &#63;.
	 *
	 * @param companyId the company ID
	 * @param userId the user ID
	 * @return the matching o auth client as metadatas
	 */
	public static List<OAuthClientASMetadata> findByC_U(
		long companyId, long userId) {

		return getPersistence().findByC_U(companyId, userId);
	}

	/**
	 * Returns a range of all the o auth client as metadatas where companyId = &#63; and userId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>OAuthClientASMetadataModelImpl</code>.
	 * </p>
	 *
	 * @param companyId the company ID
	 * @param userId the user ID
	 * @param start the lower bound of the range of o auth client as metadatas
	 * @param end the upper bound of the range of o auth client as metadatas (not inclusive)
	 * @return the range of matching o auth client as metadatas
	 */
	public static List<OAuthClientASMetadata> findByC_U(
		long companyId, long userId, int start, int end) {

		return getPersistence().findByC_U(companyId, userId, start, end);
	}

	/**
	 * Returns an ordered range of all the o auth client as metadatas where companyId = &#63; and userId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>OAuthClientASMetadataModelImpl</code>.
	 * </p>
	 *
	 * @param companyId the company ID
	 * @param userId the user ID
	 * @param start the lower bound of the range of o auth client as metadatas
	 * @param end the upper bound of the range of o auth client as metadatas (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching o auth client as metadatas
	 */
	public static List<OAuthClientASMetadata> findByC_U(
		long companyId, long userId, int start, int end,
		OrderByComparator<OAuthClientASMetadata> orderByComparator) {

		return getPersistence().findByC_U(
			companyId, userId, start, end, orderByComparator);
	}

	/**
	 * Returns an ordered range of all the o auth client as metadatas where companyId = &#63; and userId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>OAuthClientASMetadataModelImpl</code>.
	 * </p>
	 *
	 * @param companyId the company ID
	 * @param userId the user ID
	 * @param start the lower bound of the range of o auth client as metadatas
	 * @param end the upper bound of the range of o auth client as metadatas (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching o auth client as metadatas
	 */
	public static List<OAuthClientASMetadata> findByC_U(
		long companyId, long userId, int start, int end,
		OrderByComparator<OAuthClientASMetadata> orderByComparator,
		boolean useFinderCache) {

		return getPersistence().findByC_U(
			companyId, userId, start, end, orderByComparator, useFinderCache);
	}

	/**
	 * Returns the first o auth client as metadata in the ordered set where companyId = &#63; and userId = &#63;.
	 *
	 * @param companyId the company ID
	 * @param userId the user ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching o auth client as metadata
	 * @throws NoSuchASMetadataException if a matching o auth client as metadata could not be found
	 */
	public static OAuthClientASMetadata findByC_U_First(
			long companyId, long userId,
			OrderByComparator<OAuthClientASMetadata> orderByComparator)
		throws com.liferay.oauth.client.persistence.exception.
			NoSuchASMetadataException {

		return getPersistence().findByC_U_First(
			companyId, userId, orderByComparator);
	}

	/**
	 * Returns the first o auth client as metadata in the ordered set where companyId = &#63; and userId = &#63;.
	 *
	 * @param companyId the company ID
	 * @param userId the user ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching o auth client as metadata, or <code>null</code> if a matching o auth client as metadata could not be found
	 */
	public static OAuthClientASMetadata fetchByC_U_First(
		long companyId, long userId,
		OrderByComparator<OAuthClientASMetadata> orderByComparator) {

		return getPersistence().fetchByC_U_First(
			companyId, userId, orderByComparator);
	}

	/**
	 * Returns the last o auth client as metadata in the ordered set where companyId = &#63; and userId = &#63;.
	 *
	 * @param companyId the company ID
	 * @param userId the user ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching o auth client as metadata
	 * @throws NoSuchASMetadataException if a matching o auth client as metadata could not be found
	 */
	public static OAuthClientASMetadata findByC_U_Last(
			long companyId, long userId,
			OrderByComparator<OAuthClientASMetadata> orderByComparator)
		throws com.liferay.oauth.client.persistence.exception.
			NoSuchASMetadataException {

		return getPersistence().findByC_U_Last(
			companyId, userId, orderByComparator);
	}

	/**
	 * Returns the last o auth client as metadata in the ordered set where companyId = &#63; and userId = &#63;.
	 *
	 * @param companyId the company ID
	 * @param userId the user ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching o auth client as metadata, or <code>null</code> if a matching o auth client as metadata could not be found
	 */
	public static OAuthClientASMetadata fetchByC_U_Last(
		long companyId, long userId,
		OrderByComparator<OAuthClientASMetadata> orderByComparator) {

		return getPersistence().fetchByC_U_Last(
			companyId, userId, orderByComparator);
	}

	/**
	 * Returns the o auth client as metadatas before and after the current o auth client as metadata in the ordered set where companyId = &#63; and userId = &#63;.
	 *
	 * @param oAuthClientASMetadataId the primary key of the current o auth client as metadata
	 * @param companyId the company ID
	 * @param userId the user ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next o auth client as metadata
	 * @throws NoSuchASMetadataException if a o auth client as metadata with the primary key could not be found
	 */
	public static OAuthClientASMetadata[] findByC_U_PrevAndNext(
			long oAuthClientASMetadataId, long companyId, long userId,
			OrderByComparator<OAuthClientASMetadata> orderByComparator)
		throws com.liferay.oauth.client.persistence.exception.
			NoSuchASMetadataException {

		return getPersistence().findByC_U_PrevAndNext(
			oAuthClientASMetadataId, companyId, userId, orderByComparator);
	}

	/**
	 * Removes all the o auth client as metadatas where companyId = &#63; and userId = &#63; from the database.
	 *
	 * @param companyId the company ID
	 * @param userId the user ID
	 */
	public static void removeByC_U(long companyId, long userId) {
		getPersistence().removeByC_U(companyId, userId);
	}

	/**
	 * Returns the number of o auth client as metadatas where companyId = &#63; and userId = &#63;.
	 *
	 * @param companyId the company ID
	 * @param userId the user ID
	 * @return the number of matching o auth client as metadatas
	 */
	public static int countByC_U(long companyId, long userId) {
		return getPersistence().countByC_U(companyId, userId);
	}

	/**
	 * Returns the o auth client as metadata where companyId = &#63; and issuer = &#63; or throws a <code>NoSuchASMetadataException</code> if it could not be found.
	 *
	 * @param companyId the company ID
	 * @param issuer the issuer
	 * @return the matching o auth client as metadata
	 * @throws NoSuchASMetadataException if a matching o auth client as metadata could not be found
	 */
	public static OAuthClientASMetadata findByC_I(long companyId, String issuer)
		throws com.liferay.oauth.client.persistence.exception.
			NoSuchASMetadataException {

		return getPersistence().findByC_I(companyId, issuer);
	}

	/**
	 * Returns the o auth client as metadata where companyId = &#63; and issuer = &#63; or returns <code>null</code> if it could not be found. Uses the finder cache.
	 *
	 * @param companyId the company ID
	 * @param issuer the issuer
	 * @return the matching o auth client as metadata, or <code>null</code> if a matching o auth client as metadata could not be found
	 */
	public static OAuthClientASMetadata fetchByC_I(
		long companyId, String issuer) {

		return getPersistence().fetchByC_I(companyId, issuer);
	}

	/**
	 * Returns the o auth client as metadata where companyId = &#63; and issuer = &#63; or returns <code>null</code> if it could not be found, optionally using the finder cache.
	 *
	 * @param companyId the company ID
	 * @param issuer the issuer
	 * @param useFinderCache whether to use the finder cache
	 * @return the matching o auth client as metadata, or <code>null</code> if a matching o auth client as metadata could not be found
	 */
	public static OAuthClientASMetadata fetchByC_I(
		long companyId, String issuer, boolean useFinderCache) {

		return getPersistence().fetchByC_I(companyId, issuer, useFinderCache);
	}

	/**
	 * Removes the o auth client as metadata where companyId = &#63; and issuer = &#63; from the database.
	 *
	 * @param companyId the company ID
	 * @param issuer the issuer
	 * @return the o auth client as metadata that was removed
	 */
	public static OAuthClientASMetadata removeByC_I(
			long companyId, String issuer)
		throws com.liferay.oauth.client.persistence.exception.
			NoSuchASMetadataException {

		return getPersistence().removeByC_I(companyId, issuer);
	}

	/**
	 * Returns the number of o auth client as metadatas where companyId = &#63; and issuer = &#63;.
	 *
	 * @param companyId the company ID
	 * @param issuer the issuer
	 * @return the number of matching o auth client as metadatas
	 */
	public static int countByC_I(long companyId, String issuer) {
		return getPersistence().countByC_I(companyId, issuer);
	}

	/**
	 * Caches the o auth client as metadata in the entity cache if it is enabled.
	 *
	 * @param oAuthClientASMetadata the o auth client as metadata
	 */
	public static void cacheResult(
		OAuthClientASMetadata oAuthClientASMetadata) {

		getPersistence().cacheResult(oAuthClientASMetadata);
	}

	/**
	 * Caches the o auth client as metadatas in the entity cache if it is enabled.
	 *
	 * @param oAuthClientASMetadatas the o auth client as metadatas
	 */
	public static void cacheResult(
		List<OAuthClientASMetadata> oAuthClientASMetadatas) {

		getPersistence().cacheResult(oAuthClientASMetadatas);
	}

	/**
	 * Creates a new o auth client as metadata with the primary key. Does not add the o auth client as metadata to the database.
	 *
	 * @param oAuthClientASMetadataId the primary key for the new o auth client as metadata
	 * @return the new o auth client as metadata
	 */
	public static OAuthClientASMetadata create(long oAuthClientASMetadataId) {
		return getPersistence().create(oAuthClientASMetadataId);
	}

	/**
	 * Removes the o auth client as metadata with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param oAuthClientASMetadataId the primary key of the o auth client as metadata
	 * @return the o auth client as metadata that was removed
	 * @throws NoSuchASMetadataException if a o auth client as metadata with the primary key could not be found
	 */
	public static OAuthClientASMetadata remove(long oAuthClientASMetadataId)
		throws com.liferay.oauth.client.persistence.exception.
			NoSuchASMetadataException {

		return getPersistence().remove(oAuthClientASMetadataId);
	}

	public static OAuthClientASMetadata updateImpl(
		OAuthClientASMetadata oAuthClientASMetadata) {

		return getPersistence().updateImpl(oAuthClientASMetadata);
	}

	/**
	 * Returns the o auth client as metadata with the primary key or throws a <code>NoSuchASMetadataException</code> if it could not be found.
	 *
	 * @param oAuthClientASMetadataId the primary key of the o auth client as metadata
	 * @return the o auth client as metadata
	 * @throws NoSuchASMetadataException if a o auth client as metadata with the primary key could not be found
	 */
	public static OAuthClientASMetadata findByPrimaryKey(
			long oAuthClientASMetadataId)
		throws com.liferay.oauth.client.persistence.exception.
			NoSuchASMetadataException {

		return getPersistence().findByPrimaryKey(oAuthClientASMetadataId);
	}

	/**
	 * Returns the o auth client as metadata with the primary key or returns <code>null</code> if it could not be found.
	 *
	 * @param oAuthClientASMetadataId the primary key of the o auth client as metadata
	 * @return the o auth client as metadata, or <code>null</code> if a o auth client as metadata with the primary key could not be found
	 */
	public static OAuthClientASMetadata fetchByPrimaryKey(
		long oAuthClientASMetadataId) {

		return getPersistence().fetchByPrimaryKey(oAuthClientASMetadataId);
	}

	/**
	 * Returns all the o auth client as metadatas.
	 *
	 * @return the o auth client as metadatas
	 */
	public static List<OAuthClientASMetadata> findAll() {
		return getPersistence().findAll();
	}

	/**
	 * Returns a range of all the o auth client as metadatas.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>OAuthClientASMetadataModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of o auth client as metadatas
	 * @param end the upper bound of the range of o auth client as metadatas (not inclusive)
	 * @return the range of o auth client as metadatas
	 */
	public static List<OAuthClientASMetadata> findAll(int start, int end) {
		return getPersistence().findAll(start, end);
	}

	/**
	 * Returns an ordered range of all the o auth client as metadatas.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>OAuthClientASMetadataModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of o auth client as metadatas
	 * @param end the upper bound of the range of o auth client as metadatas (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of o auth client as metadatas
	 */
	public static List<OAuthClientASMetadata> findAll(
		int start, int end,
		OrderByComparator<OAuthClientASMetadata> orderByComparator) {

		return getPersistence().findAll(start, end, orderByComparator);
	}

	/**
	 * Returns an ordered range of all the o auth client as metadatas.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>OAuthClientASMetadataModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of o auth client as metadatas
	 * @param end the upper bound of the range of o auth client as metadatas (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of o auth client as metadatas
	 */
	public static List<OAuthClientASMetadata> findAll(
		int start, int end,
		OrderByComparator<OAuthClientASMetadata> orderByComparator,
		boolean useFinderCache) {

		return getPersistence().findAll(
			start, end, orderByComparator, useFinderCache);
	}

	/**
	 * Removes all the o auth client as metadatas from the database.
	 */
	public static void removeAll() {
		getPersistence().removeAll();
	}

	/**
	 * Returns the number of o auth client as metadatas.
	 *
	 * @return the number of o auth client as metadatas
	 */
	public static int countAll() {
		return getPersistence().countAll();
	}

	public static OAuthClientASMetadataPersistence getPersistence() {
		return _persistence;
	}

	private static volatile OAuthClientASMetadataPersistence _persistence;

}