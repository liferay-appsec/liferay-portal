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

import com.liferay.oauth.client.persistence.exception.NoSuchASMetadataException;
import com.liferay.oauth.client.persistence.model.OAuthClientASMetadata;
import com.liferay.portal.kernel.service.persistence.BasePersistence;

import org.osgi.annotation.versioning.ProviderType;

/**
 * The persistence interface for the o auth client as metadata service.
 *
 * <p>
 * Caching information and settings can be found in <code>portal.properties</code>
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @see OAuthClientASMetadataUtil
 * @generated
 */
@ProviderType
public interface OAuthClientASMetadataPersistence
	extends BasePersistence<OAuthClientASMetadata> {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this interface directly. Always use {@link OAuthClientASMetadataUtil} to access the o auth client as metadata persistence. Modify <code>service.xml</code> and rerun ServiceBuilder to regenerate this interface.
	 */

	/**
	 * Returns all the o auth client as metadatas where companyId = &#63;.
	 *
	 * @param companyId the company ID
	 * @return the matching o auth client as metadatas
	 */
	public java.util.List<OAuthClientASMetadata> findByCompanyId(
		long companyId);

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
	public java.util.List<OAuthClientASMetadata> findByCompanyId(
		long companyId, int start, int end);

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
	public java.util.List<OAuthClientASMetadata> findByCompanyId(
		long companyId, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<OAuthClientASMetadata>
			orderByComparator);

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
	public java.util.List<OAuthClientASMetadata> findByCompanyId(
		long companyId, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<OAuthClientASMetadata>
			orderByComparator,
		boolean useFinderCache);

	/**
	 * Returns the first o auth client as metadata in the ordered set where companyId = &#63;.
	 *
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching o auth client as metadata
	 * @throws NoSuchASMetadataException if a matching o auth client as metadata could not be found
	 */
	public OAuthClientASMetadata findByCompanyId_First(
			long companyId,
			com.liferay.portal.kernel.util.OrderByComparator
				<OAuthClientASMetadata> orderByComparator)
		throws NoSuchASMetadataException;

	/**
	 * Returns the first o auth client as metadata in the ordered set where companyId = &#63;.
	 *
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching o auth client as metadata, or <code>null</code> if a matching o auth client as metadata could not be found
	 */
	public OAuthClientASMetadata fetchByCompanyId_First(
		long companyId,
		com.liferay.portal.kernel.util.OrderByComparator<OAuthClientASMetadata>
			orderByComparator);

	/**
	 * Returns the last o auth client as metadata in the ordered set where companyId = &#63;.
	 *
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching o auth client as metadata
	 * @throws NoSuchASMetadataException if a matching o auth client as metadata could not be found
	 */
	public OAuthClientASMetadata findByCompanyId_Last(
			long companyId,
			com.liferay.portal.kernel.util.OrderByComparator
				<OAuthClientASMetadata> orderByComparator)
		throws NoSuchASMetadataException;

	/**
	 * Returns the last o auth client as metadata in the ordered set where companyId = &#63;.
	 *
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching o auth client as metadata, or <code>null</code> if a matching o auth client as metadata could not be found
	 */
	public OAuthClientASMetadata fetchByCompanyId_Last(
		long companyId,
		com.liferay.portal.kernel.util.OrderByComparator<OAuthClientASMetadata>
			orderByComparator);

	/**
	 * Returns the o auth client as metadatas before and after the current o auth client as metadata in the ordered set where companyId = &#63;.
	 *
	 * @param oAuthClientASMetadataId the primary key of the current o auth client as metadata
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next o auth client as metadata
	 * @throws NoSuchASMetadataException if a o auth client as metadata with the primary key could not be found
	 */
	public OAuthClientASMetadata[] findByCompanyId_PrevAndNext(
			long oAuthClientASMetadataId, long companyId,
			com.liferay.portal.kernel.util.OrderByComparator
				<OAuthClientASMetadata> orderByComparator)
		throws NoSuchASMetadataException;

	/**
	 * Removes all the o auth client as metadatas where companyId = &#63; from the database.
	 *
	 * @param companyId the company ID
	 */
	public void removeByCompanyId(long companyId);

	/**
	 * Returns the number of o auth client as metadatas where companyId = &#63;.
	 *
	 * @param companyId the company ID
	 * @return the number of matching o auth client as metadatas
	 */
	public int countByCompanyId(long companyId);

	/**
	 * Returns all the o auth client as metadatas where companyId = &#63; and userId = &#63;.
	 *
	 * @param companyId the company ID
	 * @param userId the user ID
	 * @return the matching o auth client as metadatas
	 */
	public java.util.List<OAuthClientASMetadata> findByC_U(
		long companyId, long userId);

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
	public java.util.List<OAuthClientASMetadata> findByC_U(
		long companyId, long userId, int start, int end);

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
	public java.util.List<OAuthClientASMetadata> findByC_U(
		long companyId, long userId, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<OAuthClientASMetadata>
			orderByComparator);

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
	public java.util.List<OAuthClientASMetadata> findByC_U(
		long companyId, long userId, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<OAuthClientASMetadata>
			orderByComparator,
		boolean useFinderCache);

	/**
	 * Returns the first o auth client as metadata in the ordered set where companyId = &#63; and userId = &#63;.
	 *
	 * @param companyId the company ID
	 * @param userId the user ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching o auth client as metadata
	 * @throws NoSuchASMetadataException if a matching o auth client as metadata could not be found
	 */
	public OAuthClientASMetadata findByC_U_First(
			long companyId, long userId,
			com.liferay.portal.kernel.util.OrderByComparator
				<OAuthClientASMetadata> orderByComparator)
		throws NoSuchASMetadataException;

	/**
	 * Returns the first o auth client as metadata in the ordered set where companyId = &#63; and userId = &#63;.
	 *
	 * @param companyId the company ID
	 * @param userId the user ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching o auth client as metadata, or <code>null</code> if a matching o auth client as metadata could not be found
	 */
	public OAuthClientASMetadata fetchByC_U_First(
		long companyId, long userId,
		com.liferay.portal.kernel.util.OrderByComparator<OAuthClientASMetadata>
			orderByComparator);

	/**
	 * Returns the last o auth client as metadata in the ordered set where companyId = &#63; and userId = &#63;.
	 *
	 * @param companyId the company ID
	 * @param userId the user ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching o auth client as metadata
	 * @throws NoSuchASMetadataException if a matching o auth client as metadata could not be found
	 */
	public OAuthClientASMetadata findByC_U_Last(
			long companyId, long userId,
			com.liferay.portal.kernel.util.OrderByComparator
				<OAuthClientASMetadata> orderByComparator)
		throws NoSuchASMetadataException;

	/**
	 * Returns the last o auth client as metadata in the ordered set where companyId = &#63; and userId = &#63;.
	 *
	 * @param companyId the company ID
	 * @param userId the user ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching o auth client as metadata, or <code>null</code> if a matching o auth client as metadata could not be found
	 */
	public OAuthClientASMetadata fetchByC_U_Last(
		long companyId, long userId,
		com.liferay.portal.kernel.util.OrderByComparator<OAuthClientASMetadata>
			orderByComparator);

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
	public OAuthClientASMetadata[] findByC_U_PrevAndNext(
			long oAuthClientASMetadataId, long companyId, long userId,
			com.liferay.portal.kernel.util.OrderByComparator
				<OAuthClientASMetadata> orderByComparator)
		throws NoSuchASMetadataException;

	/**
	 * Removes all the o auth client as metadatas where companyId = &#63; and userId = &#63; from the database.
	 *
	 * @param companyId the company ID
	 * @param userId the user ID
	 */
	public void removeByC_U(long companyId, long userId);

	/**
	 * Returns the number of o auth client as metadatas where companyId = &#63; and userId = &#63;.
	 *
	 * @param companyId the company ID
	 * @param userId the user ID
	 * @return the number of matching o auth client as metadatas
	 */
	public int countByC_U(long companyId, long userId);

	/**
	 * Returns the o auth client as metadata where companyId = &#63; and issuer = &#63; or throws a <code>NoSuchASMetadataException</code> if it could not be found.
	 *
	 * @param companyId the company ID
	 * @param issuer the issuer
	 * @return the matching o auth client as metadata
	 * @throws NoSuchASMetadataException if a matching o auth client as metadata could not be found
	 */
	public OAuthClientASMetadata findByC_I(long companyId, String issuer)
		throws NoSuchASMetadataException;

	/**
	 * Returns the o auth client as metadata where companyId = &#63; and issuer = &#63; or returns <code>null</code> if it could not be found. Uses the finder cache.
	 *
	 * @param companyId the company ID
	 * @param issuer the issuer
	 * @return the matching o auth client as metadata, or <code>null</code> if a matching o auth client as metadata could not be found
	 */
	public OAuthClientASMetadata fetchByC_I(long companyId, String issuer);

	/**
	 * Returns the o auth client as metadata where companyId = &#63; and issuer = &#63; or returns <code>null</code> if it could not be found, optionally using the finder cache.
	 *
	 * @param companyId the company ID
	 * @param issuer the issuer
	 * @param useFinderCache whether to use the finder cache
	 * @return the matching o auth client as metadata, or <code>null</code> if a matching o auth client as metadata could not be found
	 */
	public OAuthClientASMetadata fetchByC_I(
		long companyId, String issuer, boolean useFinderCache);

	/**
	 * Removes the o auth client as metadata where companyId = &#63; and issuer = &#63; from the database.
	 *
	 * @param companyId the company ID
	 * @param issuer the issuer
	 * @return the o auth client as metadata that was removed
	 */
	public OAuthClientASMetadata removeByC_I(long companyId, String issuer)
		throws NoSuchASMetadataException;

	/**
	 * Returns the number of o auth client as metadatas where companyId = &#63; and issuer = &#63;.
	 *
	 * @param companyId the company ID
	 * @param issuer the issuer
	 * @return the number of matching o auth client as metadatas
	 */
	public int countByC_I(long companyId, String issuer);

	/**
	 * Caches the o auth client as metadata in the entity cache if it is enabled.
	 *
	 * @param oAuthClientASMetadata the o auth client as metadata
	 */
	public void cacheResult(OAuthClientASMetadata oAuthClientASMetadata);

	/**
	 * Caches the o auth client as metadatas in the entity cache if it is enabled.
	 *
	 * @param oAuthClientASMetadatas the o auth client as metadatas
	 */
	public void cacheResult(
		java.util.List<OAuthClientASMetadata> oAuthClientASMetadatas);

	/**
	 * Creates a new o auth client as metadata with the primary key. Does not add the o auth client as metadata to the database.
	 *
	 * @param oAuthClientASMetadataId the primary key for the new o auth client as metadata
	 * @return the new o auth client as metadata
	 */
	public OAuthClientASMetadata create(long oAuthClientASMetadataId);

	/**
	 * Removes the o auth client as metadata with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param oAuthClientASMetadataId the primary key of the o auth client as metadata
	 * @return the o auth client as metadata that was removed
	 * @throws NoSuchASMetadataException if a o auth client as metadata with the primary key could not be found
	 */
	public OAuthClientASMetadata remove(long oAuthClientASMetadataId)
		throws NoSuchASMetadataException;

	public OAuthClientASMetadata updateImpl(
		OAuthClientASMetadata oAuthClientASMetadata);

	/**
	 * Returns the o auth client as metadata with the primary key or throws a <code>NoSuchASMetadataException</code> if it could not be found.
	 *
	 * @param oAuthClientASMetadataId the primary key of the o auth client as metadata
	 * @return the o auth client as metadata
	 * @throws NoSuchASMetadataException if a o auth client as metadata with the primary key could not be found
	 */
	public OAuthClientASMetadata findByPrimaryKey(long oAuthClientASMetadataId)
		throws NoSuchASMetadataException;

	/**
	 * Returns the o auth client as metadata with the primary key or returns <code>null</code> if it could not be found.
	 *
	 * @param oAuthClientASMetadataId the primary key of the o auth client as metadata
	 * @return the o auth client as metadata, or <code>null</code> if a o auth client as metadata with the primary key could not be found
	 */
	public OAuthClientASMetadata fetchByPrimaryKey(
		long oAuthClientASMetadataId);

	/**
	 * Returns all the o auth client as metadatas.
	 *
	 * @return the o auth client as metadatas
	 */
	public java.util.List<OAuthClientASMetadata> findAll();

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
	public java.util.List<OAuthClientASMetadata> findAll(int start, int end);

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
	public java.util.List<OAuthClientASMetadata> findAll(
		int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<OAuthClientASMetadata>
			orderByComparator);

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
	public java.util.List<OAuthClientASMetadata> findAll(
		int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<OAuthClientASMetadata>
			orderByComparator,
		boolean useFinderCache);

	/**
	 * Removes all the o auth client as metadatas from the database.
	 */
	public void removeAll();

	/**
	 * Returns the number of o auth client as metadatas.
	 *
	 * @return the number of o auth client as metadatas
	 */
	public int countAll();

}