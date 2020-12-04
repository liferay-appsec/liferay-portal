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

package com.liferay.portal.security.password.service.persistence;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.security.password.model.PasswordMeta;

import java.io.Serializable;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The persistence utility for the password meta service. This utility wraps <code>com.liferay.portal.security.password.service.persistence.impl.PasswordMetaPersistenceImpl</code> and provides direct access to the database for CRUD operations. This utility should only be used by the service layer, as it must operate within a transaction. Never access this utility in a JSP, controller, model, or other front-end class.
 *
 * <p>
 * Caching information and settings can be found in <code>portal.properties</code>
 * </p>
 *
 * @author Arthur Chan
 * @see PasswordMetaPersistence
 * @generated
 */
public class PasswordMetaUtil {

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
	public static void clearCache(PasswordMeta passwordMeta) {
		getPersistence().clearCache(passwordMeta);
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
	public static Map<Serializable, PasswordMeta> fetchByPrimaryKeys(
		Set<Serializable> primaryKeys) {

		return getPersistence().fetchByPrimaryKeys(primaryKeys);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#findWithDynamicQuery(DynamicQuery)
	 */
	public static List<PasswordMeta> findWithDynamicQuery(
		DynamicQuery dynamicQuery) {

		return getPersistence().findWithDynamicQuery(dynamicQuery);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#findWithDynamicQuery(DynamicQuery, int, int)
	 */
	public static List<PasswordMeta> findWithDynamicQuery(
		DynamicQuery dynamicQuery, int start, int end) {

		return getPersistence().findWithDynamicQuery(dynamicQuery, start, end);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#findWithDynamicQuery(DynamicQuery, int, int, OrderByComparator)
	 */
	public static List<PasswordMeta> findWithDynamicQuery(
		DynamicQuery dynamicQuery, int start, int end,
		OrderByComparator<PasswordMeta> orderByComparator) {

		return getPersistence().findWithDynamicQuery(
			dynamicQuery, start, end, orderByComparator);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#update(com.liferay.portal.kernel.model.BaseModel)
	 */
	public static PasswordMeta update(PasswordMeta passwordMeta) {
		return getPersistence().update(passwordMeta);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#update(com.liferay.portal.kernel.model.BaseModel, ServiceContext)
	 */
	public static PasswordMeta update(
		PasswordMeta passwordMeta, ServiceContext serviceContext) {

		return getPersistence().update(passwordMeta, serviceContext);
	}

	/**
	 * Returns all the password metas where uuid = &#63;.
	 *
	 * @param uuid the uuid
	 * @return the matching password metas
	 */
	public static List<PasswordMeta> findByUuid(String uuid) {
		return getPersistence().findByUuid(uuid);
	}

	/**
	 * Returns a range of all the password metas where uuid = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordMetaModelImpl</code>.
	 * </p>
	 *
	 * @param uuid the uuid
	 * @param start the lower bound of the range of password metas
	 * @param end the upper bound of the range of password metas (not inclusive)
	 * @return the range of matching password metas
	 */
	public static List<PasswordMeta> findByUuid(
		String uuid, int start, int end) {

		return getPersistence().findByUuid(uuid, start, end);
	}

	/**
	 * Returns an ordered range of all the password metas where uuid = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordMetaModelImpl</code>.
	 * </p>
	 *
	 * @param uuid the uuid
	 * @param start the lower bound of the range of password metas
	 * @param end the upper bound of the range of password metas (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching password metas
	 */
	public static List<PasswordMeta> findByUuid(
		String uuid, int start, int end,
		OrderByComparator<PasswordMeta> orderByComparator) {

		return getPersistence().findByUuid(uuid, start, end, orderByComparator);
	}

	/**
	 * Returns an ordered range of all the password metas where uuid = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordMetaModelImpl</code>.
	 * </p>
	 *
	 * @param uuid the uuid
	 * @param start the lower bound of the range of password metas
	 * @param end the upper bound of the range of password metas (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching password metas
	 */
	public static List<PasswordMeta> findByUuid(
		String uuid, int start, int end,
		OrderByComparator<PasswordMeta> orderByComparator,
		boolean useFinderCache) {

		return getPersistence().findByUuid(
			uuid, start, end, orderByComparator, useFinderCache);
	}

	/**
	 * Returns the first password meta in the ordered set where uuid = &#63;.
	 *
	 * @param uuid the uuid
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching password meta
	 * @throws NoSuchMetaException if a matching password meta could not be found
	 */
	public static PasswordMeta findByUuid_First(
			String uuid, OrderByComparator<PasswordMeta> orderByComparator)
		throws com.liferay.portal.security.password.exception.
			NoSuchMetaException {

		return getPersistence().findByUuid_First(uuid, orderByComparator);
	}

	/**
	 * Returns the first password meta in the ordered set where uuid = &#63;.
	 *
	 * @param uuid the uuid
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching password meta, or <code>null</code> if a matching password meta could not be found
	 */
	public static PasswordMeta fetchByUuid_First(
		String uuid, OrderByComparator<PasswordMeta> orderByComparator) {

		return getPersistence().fetchByUuid_First(uuid, orderByComparator);
	}

	/**
	 * Returns the last password meta in the ordered set where uuid = &#63;.
	 *
	 * @param uuid the uuid
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching password meta
	 * @throws NoSuchMetaException if a matching password meta could not be found
	 */
	public static PasswordMeta findByUuid_Last(
			String uuid, OrderByComparator<PasswordMeta> orderByComparator)
		throws com.liferay.portal.security.password.exception.
			NoSuchMetaException {

		return getPersistence().findByUuid_Last(uuid, orderByComparator);
	}

	/**
	 * Returns the last password meta in the ordered set where uuid = &#63;.
	 *
	 * @param uuid the uuid
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching password meta, or <code>null</code> if a matching password meta could not be found
	 */
	public static PasswordMeta fetchByUuid_Last(
		String uuid, OrderByComparator<PasswordMeta> orderByComparator) {

		return getPersistence().fetchByUuid_Last(uuid, orderByComparator);
	}

	/**
	 * Returns the password metas before and after the current password meta in the ordered set where uuid = &#63;.
	 *
	 * @param passwordMetaId the primary key of the current password meta
	 * @param uuid the uuid
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next password meta
	 * @throws NoSuchMetaException if a password meta with the primary key could not be found
	 */
	public static PasswordMeta[] findByUuid_PrevAndNext(
			long passwordMetaId, String uuid,
			OrderByComparator<PasswordMeta> orderByComparator)
		throws com.liferay.portal.security.password.exception.
			NoSuchMetaException {

		return getPersistence().findByUuid_PrevAndNext(
			passwordMetaId, uuid, orderByComparator);
	}

	/**
	 * Removes all the password metas where uuid = &#63; from the database.
	 *
	 * @param uuid the uuid
	 */
	public static void removeByUuid(String uuid) {
		getPersistence().removeByUuid(uuid);
	}

	/**
	 * Returns the number of password metas where uuid = &#63;.
	 *
	 * @param uuid the uuid
	 * @return the number of matching password metas
	 */
	public static int countByUuid(String uuid) {
		return getPersistence().countByUuid(uuid);
	}

	/**
	 * Returns all the password metas where uuid = &#63; and companyId = &#63;.
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @return the matching password metas
	 */
	public static List<PasswordMeta> findByUuid_C(String uuid, long companyId) {
		return getPersistence().findByUuid_C(uuid, companyId);
	}

	/**
	 * Returns a range of all the password metas where uuid = &#63; and companyId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordMetaModelImpl</code>.
	 * </p>
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param start the lower bound of the range of password metas
	 * @param end the upper bound of the range of password metas (not inclusive)
	 * @return the range of matching password metas
	 */
	public static List<PasswordMeta> findByUuid_C(
		String uuid, long companyId, int start, int end) {

		return getPersistence().findByUuid_C(uuid, companyId, start, end);
	}

	/**
	 * Returns an ordered range of all the password metas where uuid = &#63; and companyId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordMetaModelImpl</code>.
	 * </p>
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param start the lower bound of the range of password metas
	 * @param end the upper bound of the range of password metas (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching password metas
	 */
	public static List<PasswordMeta> findByUuid_C(
		String uuid, long companyId, int start, int end,
		OrderByComparator<PasswordMeta> orderByComparator) {

		return getPersistence().findByUuid_C(
			uuid, companyId, start, end, orderByComparator);
	}

	/**
	 * Returns an ordered range of all the password metas where uuid = &#63; and companyId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordMetaModelImpl</code>.
	 * </p>
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param start the lower bound of the range of password metas
	 * @param end the upper bound of the range of password metas (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching password metas
	 */
	public static List<PasswordMeta> findByUuid_C(
		String uuid, long companyId, int start, int end,
		OrderByComparator<PasswordMeta> orderByComparator,
		boolean useFinderCache) {

		return getPersistence().findByUuid_C(
			uuid, companyId, start, end, orderByComparator, useFinderCache);
	}

	/**
	 * Returns the first password meta in the ordered set where uuid = &#63; and companyId = &#63;.
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching password meta
	 * @throws NoSuchMetaException if a matching password meta could not be found
	 */
	public static PasswordMeta findByUuid_C_First(
			String uuid, long companyId,
			OrderByComparator<PasswordMeta> orderByComparator)
		throws com.liferay.portal.security.password.exception.
			NoSuchMetaException {

		return getPersistence().findByUuid_C_First(
			uuid, companyId, orderByComparator);
	}

	/**
	 * Returns the first password meta in the ordered set where uuid = &#63; and companyId = &#63;.
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching password meta, or <code>null</code> if a matching password meta could not be found
	 */
	public static PasswordMeta fetchByUuid_C_First(
		String uuid, long companyId,
		OrderByComparator<PasswordMeta> orderByComparator) {

		return getPersistence().fetchByUuid_C_First(
			uuid, companyId, orderByComparator);
	}

	/**
	 * Returns the last password meta in the ordered set where uuid = &#63; and companyId = &#63;.
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching password meta
	 * @throws NoSuchMetaException if a matching password meta could not be found
	 */
	public static PasswordMeta findByUuid_C_Last(
			String uuid, long companyId,
			OrderByComparator<PasswordMeta> orderByComparator)
		throws com.liferay.portal.security.password.exception.
			NoSuchMetaException {

		return getPersistence().findByUuid_C_Last(
			uuid, companyId, orderByComparator);
	}

	/**
	 * Returns the last password meta in the ordered set where uuid = &#63; and companyId = &#63;.
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching password meta, or <code>null</code> if a matching password meta could not be found
	 */
	public static PasswordMeta fetchByUuid_C_Last(
		String uuid, long companyId,
		OrderByComparator<PasswordMeta> orderByComparator) {

		return getPersistence().fetchByUuid_C_Last(
			uuid, companyId, orderByComparator);
	}

	/**
	 * Returns the password metas before and after the current password meta in the ordered set where uuid = &#63; and companyId = &#63;.
	 *
	 * @param passwordMetaId the primary key of the current password meta
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next password meta
	 * @throws NoSuchMetaException if a password meta with the primary key could not be found
	 */
	public static PasswordMeta[] findByUuid_C_PrevAndNext(
			long passwordMetaId, String uuid, long companyId,
			OrderByComparator<PasswordMeta> orderByComparator)
		throws com.liferay.portal.security.password.exception.
			NoSuchMetaException {

		return getPersistence().findByUuid_C_PrevAndNext(
			passwordMetaId, uuid, companyId, orderByComparator);
	}

	/**
	 * Removes all the password metas where uuid = &#63; and companyId = &#63; from the database.
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 */
	public static void removeByUuid_C(String uuid, long companyId) {
		getPersistence().removeByUuid_C(uuid, companyId);
	}

	/**
	 * Returns the number of password metas where uuid = &#63; and companyId = &#63;.
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @return the number of matching password metas
	 */
	public static int countByUuid_C(String uuid, long companyId) {
		return getPersistence().countByUuid_C(uuid, companyId);
	}

	/**
	 * Returns all the password metas where createDate &lt; &#63;.
	 *
	 * @param createDate the create date
	 * @return the matching password metas
	 */
	public static List<PasswordMeta> findByLtCreateDate(Date createDate) {
		return getPersistence().findByLtCreateDate(createDate);
	}

	/**
	 * Returns a range of all the password metas where createDate &lt; &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordMetaModelImpl</code>.
	 * </p>
	 *
	 * @param createDate the create date
	 * @param start the lower bound of the range of password metas
	 * @param end the upper bound of the range of password metas (not inclusive)
	 * @return the range of matching password metas
	 */
	public static List<PasswordMeta> findByLtCreateDate(
		Date createDate, int start, int end) {

		return getPersistence().findByLtCreateDate(createDate, start, end);
	}

	/**
	 * Returns an ordered range of all the password metas where createDate &lt; &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordMetaModelImpl</code>.
	 * </p>
	 *
	 * @param createDate the create date
	 * @param start the lower bound of the range of password metas
	 * @param end the upper bound of the range of password metas (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching password metas
	 */
	public static List<PasswordMeta> findByLtCreateDate(
		Date createDate, int start, int end,
		OrderByComparator<PasswordMeta> orderByComparator) {

		return getPersistence().findByLtCreateDate(
			createDate, start, end, orderByComparator);
	}

	/**
	 * Returns an ordered range of all the password metas where createDate &lt; &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordMetaModelImpl</code>.
	 * </p>
	 *
	 * @param createDate the create date
	 * @param start the lower bound of the range of password metas
	 * @param end the upper bound of the range of password metas (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching password metas
	 */
	public static List<PasswordMeta> findByLtCreateDate(
		Date createDate, int start, int end,
		OrderByComparator<PasswordMeta> orderByComparator,
		boolean useFinderCache) {

		return getPersistence().findByLtCreateDate(
			createDate, start, end, orderByComparator, useFinderCache);
	}

	/**
	 * Returns the first password meta in the ordered set where createDate &lt; &#63;.
	 *
	 * @param createDate the create date
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching password meta
	 * @throws NoSuchMetaException if a matching password meta could not be found
	 */
	public static PasswordMeta findByLtCreateDate_First(
			Date createDate, OrderByComparator<PasswordMeta> orderByComparator)
		throws com.liferay.portal.security.password.exception.
			NoSuchMetaException {

		return getPersistence().findByLtCreateDate_First(
			createDate, orderByComparator);
	}

	/**
	 * Returns the first password meta in the ordered set where createDate &lt; &#63;.
	 *
	 * @param createDate the create date
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching password meta, or <code>null</code> if a matching password meta could not be found
	 */
	public static PasswordMeta fetchByLtCreateDate_First(
		Date createDate, OrderByComparator<PasswordMeta> orderByComparator) {

		return getPersistence().fetchByLtCreateDate_First(
			createDate, orderByComparator);
	}

	/**
	 * Returns the last password meta in the ordered set where createDate &lt; &#63;.
	 *
	 * @param createDate the create date
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching password meta
	 * @throws NoSuchMetaException if a matching password meta could not be found
	 */
	public static PasswordMeta findByLtCreateDate_Last(
			Date createDate, OrderByComparator<PasswordMeta> orderByComparator)
		throws com.liferay.portal.security.password.exception.
			NoSuchMetaException {

		return getPersistence().findByLtCreateDate_Last(
			createDate, orderByComparator);
	}

	/**
	 * Returns the last password meta in the ordered set where createDate &lt; &#63;.
	 *
	 * @param createDate the create date
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching password meta, or <code>null</code> if a matching password meta could not be found
	 */
	public static PasswordMeta fetchByLtCreateDate_Last(
		Date createDate, OrderByComparator<PasswordMeta> orderByComparator) {

		return getPersistence().fetchByLtCreateDate_Last(
			createDate, orderByComparator);
	}

	/**
	 * Returns the password metas before and after the current password meta in the ordered set where createDate &lt; &#63;.
	 *
	 * @param passwordMetaId the primary key of the current password meta
	 * @param createDate the create date
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next password meta
	 * @throws NoSuchMetaException if a password meta with the primary key could not be found
	 */
	public static PasswordMeta[] findByLtCreateDate_PrevAndNext(
			long passwordMetaId, Date createDate,
			OrderByComparator<PasswordMeta> orderByComparator)
		throws com.liferay.portal.security.password.exception.
			NoSuchMetaException {

		return getPersistence().findByLtCreateDate_PrevAndNext(
			passwordMetaId, createDate, orderByComparator);
	}

	/**
	 * Removes all the password metas where createDate &lt; &#63; from the database.
	 *
	 * @param createDate the create date
	 */
	public static void removeByLtCreateDate(Date createDate) {
		getPersistence().removeByLtCreateDate(createDate);
	}

	/**
	 * Returns the number of password metas where createDate &lt; &#63;.
	 *
	 * @param createDate the create date
	 * @return the number of matching password metas
	 */
	public static int countByLtCreateDate(Date createDate) {
		return getPersistence().countByLtCreateDate(createDate);
	}

	/**
	 * Returns all the password metas where passwordEntryId = &#63;.
	 *
	 * @param passwordEntryId the password entry ID
	 * @return the matching password metas
	 */
	public static List<PasswordMeta> findByPasswordEntryId(
		long passwordEntryId) {

		return getPersistence().findByPasswordEntryId(passwordEntryId);
	}

	/**
	 * Returns a range of all the password metas where passwordEntryId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordMetaModelImpl</code>.
	 * </p>
	 *
	 * @param passwordEntryId the password entry ID
	 * @param start the lower bound of the range of password metas
	 * @param end the upper bound of the range of password metas (not inclusive)
	 * @return the range of matching password metas
	 */
	public static List<PasswordMeta> findByPasswordEntryId(
		long passwordEntryId, int start, int end) {

		return getPersistence().findByPasswordEntryId(
			passwordEntryId, start, end);
	}

	/**
	 * Returns an ordered range of all the password metas where passwordEntryId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordMetaModelImpl</code>.
	 * </p>
	 *
	 * @param passwordEntryId the password entry ID
	 * @param start the lower bound of the range of password metas
	 * @param end the upper bound of the range of password metas (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching password metas
	 */
	public static List<PasswordMeta> findByPasswordEntryId(
		long passwordEntryId, int start, int end,
		OrderByComparator<PasswordMeta> orderByComparator) {

		return getPersistence().findByPasswordEntryId(
			passwordEntryId, start, end, orderByComparator);
	}

	/**
	 * Returns an ordered range of all the password metas where passwordEntryId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordMetaModelImpl</code>.
	 * </p>
	 *
	 * @param passwordEntryId the password entry ID
	 * @param start the lower bound of the range of password metas
	 * @param end the upper bound of the range of password metas (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching password metas
	 */
	public static List<PasswordMeta> findByPasswordEntryId(
		long passwordEntryId, int start, int end,
		OrderByComparator<PasswordMeta> orderByComparator,
		boolean useFinderCache) {

		return getPersistence().findByPasswordEntryId(
			passwordEntryId, start, end, orderByComparator, useFinderCache);
	}

	/**
	 * Returns the first password meta in the ordered set where passwordEntryId = &#63;.
	 *
	 * @param passwordEntryId the password entry ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching password meta
	 * @throws NoSuchMetaException if a matching password meta could not be found
	 */
	public static PasswordMeta findByPasswordEntryId_First(
			long passwordEntryId,
			OrderByComparator<PasswordMeta> orderByComparator)
		throws com.liferay.portal.security.password.exception.
			NoSuchMetaException {

		return getPersistence().findByPasswordEntryId_First(
			passwordEntryId, orderByComparator);
	}

	/**
	 * Returns the first password meta in the ordered set where passwordEntryId = &#63;.
	 *
	 * @param passwordEntryId the password entry ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching password meta, or <code>null</code> if a matching password meta could not be found
	 */
	public static PasswordMeta fetchByPasswordEntryId_First(
		long passwordEntryId,
		OrderByComparator<PasswordMeta> orderByComparator) {

		return getPersistence().fetchByPasswordEntryId_First(
			passwordEntryId, orderByComparator);
	}

	/**
	 * Returns the last password meta in the ordered set where passwordEntryId = &#63;.
	 *
	 * @param passwordEntryId the password entry ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching password meta
	 * @throws NoSuchMetaException if a matching password meta could not be found
	 */
	public static PasswordMeta findByPasswordEntryId_Last(
			long passwordEntryId,
			OrderByComparator<PasswordMeta> orderByComparator)
		throws com.liferay.portal.security.password.exception.
			NoSuchMetaException {

		return getPersistence().findByPasswordEntryId_Last(
			passwordEntryId, orderByComparator);
	}

	/**
	 * Returns the last password meta in the ordered set where passwordEntryId = &#63;.
	 *
	 * @param passwordEntryId the password entry ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching password meta, or <code>null</code> if a matching password meta could not be found
	 */
	public static PasswordMeta fetchByPasswordEntryId_Last(
		long passwordEntryId,
		OrderByComparator<PasswordMeta> orderByComparator) {

		return getPersistence().fetchByPasswordEntryId_Last(
			passwordEntryId, orderByComparator);
	}

	/**
	 * Returns the password metas before and after the current password meta in the ordered set where passwordEntryId = &#63;.
	 *
	 * @param passwordMetaId the primary key of the current password meta
	 * @param passwordEntryId the password entry ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next password meta
	 * @throws NoSuchMetaException if a password meta with the primary key could not be found
	 */
	public static PasswordMeta[] findByPasswordEntryId_PrevAndNext(
			long passwordMetaId, long passwordEntryId,
			OrderByComparator<PasswordMeta> orderByComparator)
		throws com.liferay.portal.security.password.exception.
			NoSuchMetaException {

		return getPersistence().findByPasswordEntryId_PrevAndNext(
			passwordMetaId, passwordEntryId, orderByComparator);
	}

	/**
	 * Removes all the password metas where passwordEntryId = &#63; from the database.
	 *
	 * @param passwordEntryId the password entry ID
	 */
	public static void removeByPasswordEntryId(long passwordEntryId) {
		getPersistence().removeByPasswordEntryId(passwordEntryId);
	}

	/**
	 * Returns the number of password metas where passwordEntryId = &#63;.
	 *
	 * @param passwordEntryId the password entry ID
	 * @return the number of matching password metas
	 */
	public static int countByPasswordEntryId(long passwordEntryId) {
		return getPersistence().countByPasswordEntryId(passwordEntryId);
	}

	/**
	 * Caches the password meta in the entity cache if it is enabled.
	 *
	 * @param passwordMeta the password meta
	 */
	public static void cacheResult(PasswordMeta passwordMeta) {
		getPersistence().cacheResult(passwordMeta);
	}

	/**
	 * Caches the password metas in the entity cache if it is enabled.
	 *
	 * @param passwordMetas the password metas
	 */
	public static void cacheResult(List<PasswordMeta> passwordMetas) {
		getPersistence().cacheResult(passwordMetas);
	}

	/**
	 * Creates a new password meta with the primary key. Does not add the password meta to the database.
	 *
	 * @param passwordMetaId the primary key for the new password meta
	 * @return the new password meta
	 */
	public static PasswordMeta create(long passwordMetaId) {
		return getPersistence().create(passwordMetaId);
	}

	/**
	 * Removes the password meta with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param passwordMetaId the primary key of the password meta
	 * @return the password meta that was removed
	 * @throws NoSuchMetaException if a password meta with the primary key could not be found
	 */
	public static PasswordMeta remove(long passwordMetaId)
		throws com.liferay.portal.security.password.exception.
			NoSuchMetaException {

		return getPersistence().remove(passwordMetaId);
	}

	public static PasswordMeta updateImpl(PasswordMeta passwordMeta) {
		return getPersistence().updateImpl(passwordMeta);
	}

	/**
	 * Returns the password meta with the primary key or throws a <code>NoSuchMetaException</code> if it could not be found.
	 *
	 * @param passwordMetaId the primary key of the password meta
	 * @return the password meta
	 * @throws NoSuchMetaException if a password meta with the primary key could not be found
	 */
	public static PasswordMeta findByPrimaryKey(long passwordMetaId)
		throws com.liferay.portal.security.password.exception.
			NoSuchMetaException {

		return getPersistence().findByPrimaryKey(passwordMetaId);
	}

	/**
	 * Returns the password meta with the primary key or returns <code>null</code> if it could not be found.
	 *
	 * @param passwordMetaId the primary key of the password meta
	 * @return the password meta, or <code>null</code> if a password meta with the primary key could not be found
	 */
	public static PasswordMeta fetchByPrimaryKey(long passwordMetaId) {
		return getPersistence().fetchByPrimaryKey(passwordMetaId);
	}

	/**
	 * Returns all the password metas.
	 *
	 * @return the password metas
	 */
	public static List<PasswordMeta> findAll() {
		return getPersistence().findAll();
	}

	/**
	 * Returns a range of all the password metas.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordMetaModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of password metas
	 * @param end the upper bound of the range of password metas (not inclusive)
	 * @return the range of password metas
	 */
	public static List<PasswordMeta> findAll(int start, int end) {
		return getPersistence().findAll(start, end);
	}

	/**
	 * Returns an ordered range of all the password metas.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordMetaModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of password metas
	 * @param end the upper bound of the range of password metas (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of password metas
	 */
	public static List<PasswordMeta> findAll(
		int start, int end, OrderByComparator<PasswordMeta> orderByComparator) {

		return getPersistence().findAll(start, end, orderByComparator);
	}

	/**
	 * Returns an ordered range of all the password metas.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordMetaModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of password metas
	 * @param end the upper bound of the range of password metas (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of password metas
	 */
	public static List<PasswordMeta> findAll(
		int start, int end, OrderByComparator<PasswordMeta> orderByComparator,
		boolean useFinderCache) {

		return getPersistence().findAll(
			start, end, orderByComparator, useFinderCache);
	}

	/**
	 * Removes all the password metas from the database.
	 */
	public static void removeAll() {
		getPersistence().removeAll();
	}

	/**
	 * Returns the number of password metas.
	 *
	 * @return the number of password metas
	 */
	public static int countAll() {
		return getPersistence().countAll();
	}

	public static PasswordMetaPersistence getPersistence() {
		return _serviceTracker.getService();
	}

	private static ServiceTracker
		<PasswordMetaPersistence, PasswordMetaPersistence> _serviceTracker;

	static {
		Bundle bundle = FrameworkUtil.getBundle(PasswordMetaPersistence.class);

		ServiceTracker<PasswordMetaPersistence, PasswordMetaPersistence>
			serviceTracker =
				new ServiceTracker
					<PasswordMetaPersistence, PasswordMetaPersistence>(
						bundle.getBundleContext(),
						PasswordMetaPersistence.class, null);

		serviceTracker.open();

		_serviceTracker = serviceTracker;
	}

}