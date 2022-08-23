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

package com.liferay.portal.security.ldap.persistence.service.persistence;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.security.ldap.persistence.model.LDAPServerAttributeRel;

import java.io.Serializable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The persistence utility for the ldap server attribute rel service. This utility wraps <code>com.liferay.portal.security.ldap.persistence.service.persistence.impl.LDAPServerAttributeRelPersistenceImpl</code> and provides direct access to the database for CRUD operations. This utility should only be used by the service layer, as it must operate within a transaction. Never access this utility in a JSP, controller, model, or other front-end class.
 *
 * <p>
 * Caching information and settings can be found in <code>portal.properties</code>
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @see LDAPServerAttributeRelPersistence
 * @generated
 */
public class LDAPServerAttributeRelUtil {

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
	public static void clearCache(
		LDAPServerAttributeRel ldapServerAttributeRel) {

		getPersistence().clearCache(ldapServerAttributeRel);
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
	public static Map<Serializable, LDAPServerAttributeRel> fetchByPrimaryKeys(
		Set<Serializable> primaryKeys) {

		return getPersistence().fetchByPrimaryKeys(primaryKeys);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#findWithDynamicQuery(DynamicQuery)
	 */
	public static List<LDAPServerAttributeRel> findWithDynamicQuery(
		DynamicQuery dynamicQuery) {

		return getPersistence().findWithDynamicQuery(dynamicQuery);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#findWithDynamicQuery(DynamicQuery, int, int)
	 */
	public static List<LDAPServerAttributeRel> findWithDynamicQuery(
		DynamicQuery dynamicQuery, int start, int end) {

		return getPersistence().findWithDynamicQuery(dynamicQuery, start, end);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#findWithDynamicQuery(DynamicQuery, int, int, OrderByComparator)
	 */
	public static List<LDAPServerAttributeRel> findWithDynamicQuery(
		DynamicQuery dynamicQuery, int start, int end,
		OrderByComparator<LDAPServerAttributeRel> orderByComparator) {

		return getPersistence().findWithDynamicQuery(
			dynamicQuery, start, end, orderByComparator);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#update(com.liferay.portal.kernel.model.BaseModel)
	 */
	public static LDAPServerAttributeRel update(
		LDAPServerAttributeRel ldapServerAttributeRel) {

		return getPersistence().update(ldapServerAttributeRel);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#update(com.liferay.portal.kernel.model.BaseModel, ServiceContext)
	 */
	public static LDAPServerAttributeRel update(
		LDAPServerAttributeRel ldapServerAttributeRel,
		ServiceContext serviceContext) {

		return getPersistence().update(ldapServerAttributeRel, serviceContext);
	}

	/**
	 * Returns all the ldap server attribute rels where ldapServerId = &#63;.
	 *
	 * @param ldapServerId the ldap server ID
	 * @return the matching ldap server attribute rels
	 */
	public static List<LDAPServerAttributeRel> findByLdapServerId(
		long ldapServerId) {

		return getPersistence().findByLdapServerId(ldapServerId);
	}

	/**
	 * Returns a range of all the ldap server attribute rels where ldapServerId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>LDAPServerAttributeRelModelImpl</code>.
	 * </p>
	 *
	 * @param ldapServerId the ldap server ID
	 * @param start the lower bound of the range of ldap server attribute rels
	 * @param end the upper bound of the range of ldap server attribute rels (not inclusive)
	 * @return the range of matching ldap server attribute rels
	 */
	public static List<LDAPServerAttributeRel> findByLdapServerId(
		long ldapServerId, int start, int end) {

		return getPersistence().findByLdapServerId(ldapServerId, start, end);
	}

	/**
	 * Returns an ordered range of all the ldap server attribute rels where ldapServerId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>LDAPServerAttributeRelModelImpl</code>.
	 * </p>
	 *
	 * @param ldapServerId the ldap server ID
	 * @param start the lower bound of the range of ldap server attribute rels
	 * @param end the upper bound of the range of ldap server attribute rels (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching ldap server attribute rels
	 */
	public static List<LDAPServerAttributeRel> findByLdapServerId(
		long ldapServerId, int start, int end,
		OrderByComparator<LDAPServerAttributeRel> orderByComparator) {

		return getPersistence().findByLdapServerId(
			ldapServerId, start, end, orderByComparator);
	}

	/**
	 * Returns an ordered range of all the ldap server attribute rels where ldapServerId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>LDAPServerAttributeRelModelImpl</code>.
	 * </p>
	 *
	 * @param ldapServerId the ldap server ID
	 * @param start the lower bound of the range of ldap server attribute rels
	 * @param end the upper bound of the range of ldap server attribute rels (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching ldap server attribute rels
	 */
	public static List<LDAPServerAttributeRel> findByLdapServerId(
		long ldapServerId, int start, int end,
		OrderByComparator<LDAPServerAttributeRel> orderByComparator,
		boolean useFinderCache) {

		return getPersistence().findByLdapServerId(
			ldapServerId, start, end, orderByComparator, useFinderCache);
	}

	/**
	 * Returns the first ldap server attribute rel in the ordered set where ldapServerId = &#63;.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching ldap server attribute rel
	 * @throws NoSuchServerAttributeRelException if a matching ldap server attribute rel could not be found
	 */
	public static LDAPServerAttributeRel findByLdapServerId_First(
			long ldapServerId,
			OrderByComparator<LDAPServerAttributeRel> orderByComparator)
		throws com.liferay.portal.security.ldap.persistence.exception.
			NoSuchServerAttributeRelException {

		return getPersistence().findByLdapServerId_First(
			ldapServerId, orderByComparator);
	}

	/**
	 * Returns the first ldap server attribute rel in the ordered set where ldapServerId = &#63;.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching ldap server attribute rel, or <code>null</code> if a matching ldap server attribute rel could not be found
	 */
	public static LDAPServerAttributeRel fetchByLdapServerId_First(
		long ldapServerId,
		OrderByComparator<LDAPServerAttributeRel> orderByComparator) {

		return getPersistence().fetchByLdapServerId_First(
			ldapServerId, orderByComparator);
	}

	/**
	 * Returns the last ldap server attribute rel in the ordered set where ldapServerId = &#63;.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching ldap server attribute rel
	 * @throws NoSuchServerAttributeRelException if a matching ldap server attribute rel could not be found
	 */
	public static LDAPServerAttributeRel findByLdapServerId_Last(
			long ldapServerId,
			OrderByComparator<LDAPServerAttributeRel> orderByComparator)
		throws com.liferay.portal.security.ldap.persistence.exception.
			NoSuchServerAttributeRelException {

		return getPersistence().findByLdapServerId_Last(
			ldapServerId, orderByComparator);
	}

	/**
	 * Returns the last ldap server attribute rel in the ordered set where ldapServerId = &#63;.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching ldap server attribute rel, or <code>null</code> if a matching ldap server attribute rel could not be found
	 */
	public static LDAPServerAttributeRel fetchByLdapServerId_Last(
		long ldapServerId,
		OrderByComparator<LDAPServerAttributeRel> orderByComparator) {

		return getPersistence().fetchByLdapServerId_Last(
			ldapServerId, orderByComparator);
	}

	/**
	 * Returns the ldap server attribute rels before and after the current ldap server attribute rel in the ordered set where ldapServerId = &#63;.
	 *
	 * @param ldapServerAttributeRelId the primary key of the current ldap server attribute rel
	 * @param ldapServerId the ldap server ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next ldap server attribute rel
	 * @throws NoSuchServerAttributeRelException if a ldap server attribute rel with the primary key could not be found
	 */
	public static LDAPServerAttributeRel[] findByLdapServerId_PrevAndNext(
			long ldapServerAttributeRelId, long ldapServerId,
			OrderByComparator<LDAPServerAttributeRel> orderByComparator)
		throws com.liferay.portal.security.ldap.persistence.exception.
			NoSuchServerAttributeRelException {

		return getPersistence().findByLdapServerId_PrevAndNext(
			ldapServerAttributeRelId, ldapServerId, orderByComparator);
	}

	/**
	 * Removes all the ldap server attribute rels where ldapServerId = &#63; from the database.
	 *
	 * @param ldapServerId the ldap server ID
	 */
	public static void removeByLdapServerId(long ldapServerId) {
		getPersistence().removeByLdapServerId(ldapServerId);
	}

	/**
	 * Returns the number of ldap server attribute rels where ldapServerId = &#63;.
	 *
	 * @param ldapServerId the ldap server ID
	 * @return the number of matching ldap server attribute rels
	 */
	public static int countByLdapServerId(long ldapServerId) {
		return getPersistence().countByLdapServerId(ldapServerId);
	}

	/**
	 * Returns all the ldap server attribute rels where ldapServerId = &#63; and classNameId = &#63;.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @return the matching ldap server attribute rels
	 */
	public static List<LDAPServerAttributeRel> findByL_C(
		long ldapServerId, long classNameId) {

		return getPersistence().findByL_C(ldapServerId, classNameId);
	}

	/**
	 * Returns a range of all the ldap server attribute rels where ldapServerId = &#63; and classNameId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>LDAPServerAttributeRelModelImpl</code>.
	 * </p>
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @param start the lower bound of the range of ldap server attribute rels
	 * @param end the upper bound of the range of ldap server attribute rels (not inclusive)
	 * @return the range of matching ldap server attribute rels
	 */
	public static List<LDAPServerAttributeRel> findByL_C(
		long ldapServerId, long classNameId, int start, int end) {

		return getPersistence().findByL_C(
			ldapServerId, classNameId, start, end);
	}

	/**
	 * Returns an ordered range of all the ldap server attribute rels where ldapServerId = &#63; and classNameId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>LDAPServerAttributeRelModelImpl</code>.
	 * </p>
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @param start the lower bound of the range of ldap server attribute rels
	 * @param end the upper bound of the range of ldap server attribute rels (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching ldap server attribute rels
	 */
	public static List<LDAPServerAttributeRel> findByL_C(
		long ldapServerId, long classNameId, int start, int end,
		OrderByComparator<LDAPServerAttributeRel> orderByComparator) {

		return getPersistence().findByL_C(
			ldapServerId, classNameId, start, end, orderByComparator);
	}

	/**
	 * Returns an ordered range of all the ldap server attribute rels where ldapServerId = &#63; and classNameId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>LDAPServerAttributeRelModelImpl</code>.
	 * </p>
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @param start the lower bound of the range of ldap server attribute rels
	 * @param end the upper bound of the range of ldap server attribute rels (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching ldap server attribute rels
	 */
	public static List<LDAPServerAttributeRel> findByL_C(
		long ldapServerId, long classNameId, int start, int end,
		OrderByComparator<LDAPServerAttributeRel> orderByComparator,
		boolean useFinderCache) {

		return getPersistence().findByL_C(
			ldapServerId, classNameId, start, end, orderByComparator,
			useFinderCache);
	}

	/**
	 * Returns the first ldap server attribute rel in the ordered set where ldapServerId = &#63; and classNameId = &#63;.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching ldap server attribute rel
	 * @throws NoSuchServerAttributeRelException if a matching ldap server attribute rel could not be found
	 */
	public static LDAPServerAttributeRel findByL_C_First(
			long ldapServerId, long classNameId,
			OrderByComparator<LDAPServerAttributeRel> orderByComparator)
		throws com.liferay.portal.security.ldap.persistence.exception.
			NoSuchServerAttributeRelException {

		return getPersistence().findByL_C_First(
			ldapServerId, classNameId, orderByComparator);
	}

	/**
	 * Returns the first ldap server attribute rel in the ordered set where ldapServerId = &#63; and classNameId = &#63;.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching ldap server attribute rel, or <code>null</code> if a matching ldap server attribute rel could not be found
	 */
	public static LDAPServerAttributeRel fetchByL_C_First(
		long ldapServerId, long classNameId,
		OrderByComparator<LDAPServerAttributeRel> orderByComparator) {

		return getPersistence().fetchByL_C_First(
			ldapServerId, classNameId, orderByComparator);
	}

	/**
	 * Returns the last ldap server attribute rel in the ordered set where ldapServerId = &#63; and classNameId = &#63;.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching ldap server attribute rel
	 * @throws NoSuchServerAttributeRelException if a matching ldap server attribute rel could not be found
	 */
	public static LDAPServerAttributeRel findByL_C_Last(
			long ldapServerId, long classNameId,
			OrderByComparator<LDAPServerAttributeRel> orderByComparator)
		throws com.liferay.portal.security.ldap.persistence.exception.
			NoSuchServerAttributeRelException {

		return getPersistence().findByL_C_Last(
			ldapServerId, classNameId, orderByComparator);
	}

	/**
	 * Returns the last ldap server attribute rel in the ordered set where ldapServerId = &#63; and classNameId = &#63;.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching ldap server attribute rel, or <code>null</code> if a matching ldap server attribute rel could not be found
	 */
	public static LDAPServerAttributeRel fetchByL_C_Last(
		long ldapServerId, long classNameId,
		OrderByComparator<LDAPServerAttributeRel> orderByComparator) {

		return getPersistence().fetchByL_C_Last(
			ldapServerId, classNameId, orderByComparator);
	}

	/**
	 * Returns the ldap server attribute rels before and after the current ldap server attribute rel in the ordered set where ldapServerId = &#63; and classNameId = &#63;.
	 *
	 * @param ldapServerAttributeRelId the primary key of the current ldap server attribute rel
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next ldap server attribute rel
	 * @throws NoSuchServerAttributeRelException if a ldap server attribute rel with the primary key could not be found
	 */
	public static LDAPServerAttributeRel[] findByL_C_PrevAndNext(
			long ldapServerAttributeRelId, long ldapServerId, long classNameId,
			OrderByComparator<LDAPServerAttributeRel> orderByComparator)
		throws com.liferay.portal.security.ldap.persistence.exception.
			NoSuchServerAttributeRelException {

		return getPersistence().findByL_C_PrevAndNext(
			ldapServerAttributeRelId, ldapServerId, classNameId,
			orderByComparator);
	}

	/**
	 * Removes all the ldap server attribute rels where ldapServerId = &#63; and classNameId = &#63; from the database.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 */
	public static void removeByL_C(long ldapServerId, long classNameId) {
		getPersistence().removeByL_C(ldapServerId, classNameId);
	}

	/**
	 * Returns the number of ldap server attribute rels where ldapServerId = &#63; and classNameId = &#63;.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @return the number of matching ldap server attribute rels
	 */
	public static int countByL_C(long ldapServerId, long classNameId) {
		return getPersistence().countByL_C(ldapServerId, classNameId);
	}

	/**
	 * Returns all the ldap server attribute rels where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the matching ldap server attribute rels
	 */
	public static List<LDAPServerAttributeRel> findByC_C(
		long classNameId, long classPK) {

		return getPersistence().findByC_C(classNameId, classPK);
	}

	/**
	 * Returns a range of all the ldap server attribute rels where classNameId = &#63; and classPK = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>LDAPServerAttributeRelModelImpl</code>.
	 * </p>
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param start the lower bound of the range of ldap server attribute rels
	 * @param end the upper bound of the range of ldap server attribute rels (not inclusive)
	 * @return the range of matching ldap server attribute rels
	 */
	public static List<LDAPServerAttributeRel> findByC_C(
		long classNameId, long classPK, int start, int end) {

		return getPersistence().findByC_C(classNameId, classPK, start, end);
	}

	/**
	 * Returns an ordered range of all the ldap server attribute rels where classNameId = &#63; and classPK = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>LDAPServerAttributeRelModelImpl</code>.
	 * </p>
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param start the lower bound of the range of ldap server attribute rels
	 * @param end the upper bound of the range of ldap server attribute rels (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching ldap server attribute rels
	 */
	public static List<LDAPServerAttributeRel> findByC_C(
		long classNameId, long classPK, int start, int end,
		OrderByComparator<LDAPServerAttributeRel> orderByComparator) {

		return getPersistence().findByC_C(
			classNameId, classPK, start, end, orderByComparator);
	}

	/**
	 * Returns an ordered range of all the ldap server attribute rels where classNameId = &#63; and classPK = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>LDAPServerAttributeRelModelImpl</code>.
	 * </p>
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param start the lower bound of the range of ldap server attribute rels
	 * @param end the upper bound of the range of ldap server attribute rels (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching ldap server attribute rels
	 */
	public static List<LDAPServerAttributeRel> findByC_C(
		long classNameId, long classPK, int start, int end,
		OrderByComparator<LDAPServerAttributeRel> orderByComparator,
		boolean useFinderCache) {

		return getPersistence().findByC_C(
			classNameId, classPK, start, end, orderByComparator,
			useFinderCache);
	}

	/**
	 * Returns the first ldap server attribute rel in the ordered set where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching ldap server attribute rel
	 * @throws NoSuchServerAttributeRelException if a matching ldap server attribute rel could not be found
	 */
	public static LDAPServerAttributeRel findByC_C_First(
			long classNameId, long classPK,
			OrderByComparator<LDAPServerAttributeRel> orderByComparator)
		throws com.liferay.portal.security.ldap.persistence.exception.
			NoSuchServerAttributeRelException {

		return getPersistence().findByC_C_First(
			classNameId, classPK, orderByComparator);
	}

	/**
	 * Returns the first ldap server attribute rel in the ordered set where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching ldap server attribute rel, or <code>null</code> if a matching ldap server attribute rel could not be found
	 */
	public static LDAPServerAttributeRel fetchByC_C_First(
		long classNameId, long classPK,
		OrderByComparator<LDAPServerAttributeRel> orderByComparator) {

		return getPersistence().fetchByC_C_First(
			classNameId, classPK, orderByComparator);
	}

	/**
	 * Returns the last ldap server attribute rel in the ordered set where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching ldap server attribute rel
	 * @throws NoSuchServerAttributeRelException if a matching ldap server attribute rel could not be found
	 */
	public static LDAPServerAttributeRel findByC_C_Last(
			long classNameId, long classPK,
			OrderByComparator<LDAPServerAttributeRel> orderByComparator)
		throws com.liferay.portal.security.ldap.persistence.exception.
			NoSuchServerAttributeRelException {

		return getPersistence().findByC_C_Last(
			classNameId, classPK, orderByComparator);
	}

	/**
	 * Returns the last ldap server attribute rel in the ordered set where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching ldap server attribute rel, or <code>null</code> if a matching ldap server attribute rel could not be found
	 */
	public static LDAPServerAttributeRel fetchByC_C_Last(
		long classNameId, long classPK,
		OrderByComparator<LDAPServerAttributeRel> orderByComparator) {

		return getPersistence().fetchByC_C_Last(
			classNameId, classPK, orderByComparator);
	}

	/**
	 * Returns the ldap server attribute rels before and after the current ldap server attribute rel in the ordered set where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param ldapServerAttributeRelId the primary key of the current ldap server attribute rel
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next ldap server attribute rel
	 * @throws NoSuchServerAttributeRelException if a ldap server attribute rel with the primary key could not be found
	 */
	public static LDAPServerAttributeRel[] findByC_C_PrevAndNext(
			long ldapServerAttributeRelId, long classNameId, long classPK,
			OrderByComparator<LDAPServerAttributeRel> orderByComparator)
		throws com.liferay.portal.security.ldap.persistence.exception.
			NoSuchServerAttributeRelException {

		return getPersistence().findByC_C_PrevAndNext(
			ldapServerAttributeRelId, classNameId, classPK, orderByComparator);
	}

	/**
	 * Removes all the ldap server attribute rels where classNameId = &#63; and classPK = &#63; from the database.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 */
	public static void removeByC_C(long classNameId, long classPK) {
		getPersistence().removeByC_C(classNameId, classPK);
	}

	/**
	 * Returns the number of ldap server attribute rels where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the number of matching ldap server attribute rels
	 */
	public static int countByC_C(long classNameId, long classPK) {
		return getPersistence().countByC_C(classNameId, classPK);
	}

	/**
	 * Returns the ldap server attribute rel where ldapServerId = &#63; and classNameId = &#63; and classPK = &#63; or throws a <code>NoSuchServerAttributeRelException</code> if it could not be found.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the matching ldap server attribute rel
	 * @throws NoSuchServerAttributeRelException if a matching ldap server attribute rel could not be found
	 */
	public static LDAPServerAttributeRel findByL_C_C(
			long ldapServerId, long classNameId, long classPK)
		throws com.liferay.portal.security.ldap.persistence.exception.
			NoSuchServerAttributeRelException {

		return getPersistence().findByL_C_C(ldapServerId, classNameId, classPK);
	}

	/**
	 * Returns the ldap server attribute rel where ldapServerId = &#63; and classNameId = &#63; and classPK = &#63; or returns <code>null</code> if it could not be found. Uses the finder cache.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the matching ldap server attribute rel, or <code>null</code> if a matching ldap server attribute rel could not be found
	 */
	public static LDAPServerAttributeRel fetchByL_C_C(
		long ldapServerId, long classNameId, long classPK) {

		return getPersistence().fetchByL_C_C(
			ldapServerId, classNameId, classPK);
	}

	/**
	 * Returns the ldap server attribute rel where ldapServerId = &#63; and classNameId = &#63; and classPK = &#63; or returns <code>null</code> if it could not be found, optionally using the finder cache.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param useFinderCache whether to use the finder cache
	 * @return the matching ldap server attribute rel, or <code>null</code> if a matching ldap server attribute rel could not be found
	 */
	public static LDAPServerAttributeRel fetchByL_C_C(
		long ldapServerId, long classNameId, long classPK,
		boolean useFinderCache) {

		return getPersistence().fetchByL_C_C(
			ldapServerId, classNameId, classPK, useFinderCache);
	}

	/**
	 * Removes the ldap server attribute rel where ldapServerId = &#63; and classNameId = &#63; and classPK = &#63; from the database.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the ldap server attribute rel that was removed
	 */
	public static LDAPServerAttributeRel removeByL_C_C(
			long ldapServerId, long classNameId, long classPK)
		throws com.liferay.portal.security.ldap.persistence.exception.
			NoSuchServerAttributeRelException {

		return getPersistence().removeByL_C_C(
			ldapServerId, classNameId, classPK);
	}

	/**
	 * Returns the number of ldap server attribute rels where ldapServerId = &#63; and classNameId = &#63; and classPK = &#63;.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the number of matching ldap server attribute rels
	 */
	public static int countByL_C_C(
		long ldapServerId, long classNameId, long classPK) {

		return getPersistence().countByL_C_C(
			ldapServerId, classNameId, classPK);
	}

	/**
	 * Caches the ldap server attribute rel in the entity cache if it is enabled.
	 *
	 * @param ldapServerAttributeRel the ldap server attribute rel
	 */
	public static void cacheResult(
		LDAPServerAttributeRel ldapServerAttributeRel) {

		getPersistence().cacheResult(ldapServerAttributeRel);
	}

	/**
	 * Caches the ldap server attribute rels in the entity cache if it is enabled.
	 *
	 * @param ldapServerAttributeRels the ldap server attribute rels
	 */
	public static void cacheResult(
		List<LDAPServerAttributeRel> ldapServerAttributeRels) {

		getPersistence().cacheResult(ldapServerAttributeRels);
	}

	/**
	 * Creates a new ldap server attribute rel with the primary key. Does not add the ldap server attribute rel to the database.
	 *
	 * @param ldapServerAttributeRelId the primary key for the new ldap server attribute rel
	 * @return the new ldap server attribute rel
	 */
	public static LDAPServerAttributeRel create(long ldapServerAttributeRelId) {
		return getPersistence().create(ldapServerAttributeRelId);
	}

	/**
	 * Removes the ldap server attribute rel with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param ldapServerAttributeRelId the primary key of the ldap server attribute rel
	 * @return the ldap server attribute rel that was removed
	 * @throws NoSuchServerAttributeRelException if a ldap server attribute rel with the primary key could not be found
	 */
	public static LDAPServerAttributeRel remove(long ldapServerAttributeRelId)
		throws com.liferay.portal.security.ldap.persistence.exception.
			NoSuchServerAttributeRelException {

		return getPersistence().remove(ldapServerAttributeRelId);
	}

	public static LDAPServerAttributeRel updateImpl(
		LDAPServerAttributeRel ldapServerAttributeRel) {

		return getPersistence().updateImpl(ldapServerAttributeRel);
	}

	/**
	 * Returns the ldap server attribute rel with the primary key or throws a <code>NoSuchServerAttributeRelException</code> if it could not be found.
	 *
	 * @param ldapServerAttributeRelId the primary key of the ldap server attribute rel
	 * @return the ldap server attribute rel
	 * @throws NoSuchServerAttributeRelException if a ldap server attribute rel with the primary key could not be found
	 */
	public static LDAPServerAttributeRel findByPrimaryKey(
			long ldapServerAttributeRelId)
		throws com.liferay.portal.security.ldap.persistence.exception.
			NoSuchServerAttributeRelException {

		return getPersistence().findByPrimaryKey(ldapServerAttributeRelId);
	}

	/**
	 * Returns the ldap server attribute rel with the primary key or returns <code>null</code> if it could not be found.
	 *
	 * @param ldapServerAttributeRelId the primary key of the ldap server attribute rel
	 * @return the ldap server attribute rel, or <code>null</code> if a ldap server attribute rel with the primary key could not be found
	 */
	public static LDAPServerAttributeRel fetchByPrimaryKey(
		long ldapServerAttributeRelId) {

		return getPersistence().fetchByPrimaryKey(ldapServerAttributeRelId);
	}

	/**
	 * Returns all the ldap server attribute rels.
	 *
	 * @return the ldap server attribute rels
	 */
	public static List<LDAPServerAttributeRel> findAll() {
		return getPersistence().findAll();
	}

	/**
	 * Returns a range of all the ldap server attribute rels.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>LDAPServerAttributeRelModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of ldap server attribute rels
	 * @param end the upper bound of the range of ldap server attribute rels (not inclusive)
	 * @return the range of ldap server attribute rels
	 */
	public static List<LDAPServerAttributeRel> findAll(int start, int end) {
		return getPersistence().findAll(start, end);
	}

	/**
	 * Returns an ordered range of all the ldap server attribute rels.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>LDAPServerAttributeRelModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of ldap server attribute rels
	 * @param end the upper bound of the range of ldap server attribute rels (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of ldap server attribute rels
	 */
	public static List<LDAPServerAttributeRel> findAll(
		int start, int end,
		OrderByComparator<LDAPServerAttributeRel> orderByComparator) {

		return getPersistence().findAll(start, end, orderByComparator);
	}

	/**
	 * Returns an ordered range of all the ldap server attribute rels.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>LDAPServerAttributeRelModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of ldap server attribute rels
	 * @param end the upper bound of the range of ldap server attribute rels (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of ldap server attribute rels
	 */
	public static List<LDAPServerAttributeRel> findAll(
		int start, int end,
		OrderByComparator<LDAPServerAttributeRel> orderByComparator,
		boolean useFinderCache) {

		return getPersistence().findAll(
			start, end, orderByComparator, useFinderCache);
	}

	/**
	 * Removes all the ldap server attribute rels from the database.
	 */
	public static void removeAll() {
		getPersistence().removeAll();
	}

	/**
	 * Returns the number of ldap server attribute rels.
	 *
	 * @return the number of ldap server attribute rels
	 */
	public static int countAll() {
		return getPersistence().countAll();
	}

	public static LDAPServerAttributeRelPersistence getPersistence() {
		return _persistence;
	}

	private static volatile LDAPServerAttributeRelPersistence _persistence;

}