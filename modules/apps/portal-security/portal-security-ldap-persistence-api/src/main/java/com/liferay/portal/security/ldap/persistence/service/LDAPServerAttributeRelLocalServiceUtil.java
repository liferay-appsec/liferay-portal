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

package com.liferay.portal.security.ldap.persistence.service;

import com.liferay.petra.sql.dsl.query.DSLQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.PersistedModel;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.security.ldap.persistence.model.LDAPServerAttributeRel;

import java.io.Serializable;

import java.util.List;

/**
 * Provides the local service utility for LDAPServerAttributeRel. This utility wraps
 * <code>com.liferay.portal.security.ldap.persistence.service.impl.LDAPServerAttributeRelLocalServiceImpl</code> and
 * is an access point for service operations in application layer code running
 * on the local server. Methods of this service will not have security checks
 * based on the propagated JAAS credentials because this service can only be
 * accessed from within the same VM.
 *
 * @author Brian Wing Shun Chan
 * @see LDAPServerAttributeRelLocalService
 * @generated
 */
public class LDAPServerAttributeRelLocalServiceUtil {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify this class directly. Add custom service methods to <code>com.liferay.portal.security.ldap.persistence.service.impl.LDAPServerAttributeRelLocalServiceImpl</code> and rerun ServiceBuilder to regenerate this class.
	 */

	/**
	 * Adds the ldap server attribute rel to the database. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect LDAPServerAttributeRelLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param ldapServerAttributeRel the ldap server attribute rel
	 * @return the ldap server attribute rel that was added
	 */
	public static LDAPServerAttributeRel addLDAPServerAttributeRel(
		LDAPServerAttributeRel ldapServerAttributeRel) {

		return getService().addLDAPServerAttributeRel(ldapServerAttributeRel);
	}

	public static LDAPServerAttributeRel addLDAPServerAttributeRel(
			long ldapServerId, String className, long classPK)
		throws PortalException {

		return getService().addLDAPServerAttributeRel(
			ldapServerId, className, classPK);
	}

	/**
	 * Creates a new ldap server attribute rel with the primary key. Does not add the ldap server attribute rel to the database.
	 *
	 * @param ldapServerAttributeRelId the primary key for the new ldap server attribute rel
	 * @return the new ldap server attribute rel
	 */
	public static LDAPServerAttributeRel createLDAPServerAttributeRel(
		long ldapServerAttributeRelId) {

		return getService().createLDAPServerAttributeRel(
			ldapServerAttributeRelId);
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
	 * Deletes the ldap server attribute rel from the database. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect LDAPServerAttributeRelLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param ldapServerAttributeRel the ldap server attribute rel
	 * @return the ldap server attribute rel that was removed
	 */
	public static LDAPServerAttributeRel deleteLDAPServerAttributeRel(
		LDAPServerAttributeRel ldapServerAttributeRel) {

		return getService().deleteLDAPServerAttributeRel(
			ldapServerAttributeRel);
	}

	/**
	 * Deletes the ldap server attribute rel with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect LDAPServerAttributeRelLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param ldapServerAttributeRelId the primary key of the ldap server attribute rel
	 * @return the ldap server attribute rel that was removed
	 * @throws PortalException if a ldap server attribute rel with the primary key could not be found
	 */
	public static LDAPServerAttributeRel deleteLDAPServerAttributeRel(
			long ldapServerAttributeRelId)
		throws PortalException {

		return getService().deleteLDAPServerAttributeRel(
			ldapServerAttributeRelId);
	}

	public static void deleteLDAPServerAttributeRel(
			long ldapServerId, String className, long classPK)
		throws PortalException {

		getService().deleteLDAPServerAttributeRel(
			ldapServerId, className, classPK);
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
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.portal.security.ldap.persistence.model.impl.LDAPServerAttributeRelModelImpl</code>.
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
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.portal.security.ldap.persistence.model.impl.LDAPServerAttributeRelModelImpl</code>.
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

	public static LDAPServerAttributeRel fetchLDAPServerAttributeRel(
		long ldapServerAttributeRelId) {

		return getService().fetchLDAPServerAttributeRel(
			ldapServerAttributeRelId);
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
	 * Returns the ldap server attribute rel with the primary key.
	 *
	 * @param ldapServerAttributeRelId the primary key of the ldap server attribute rel
	 * @return the ldap server attribute rel
	 * @throws PortalException if a ldap server attribute rel with the primary key could not be found
	 */
	public static LDAPServerAttributeRel getLDAPServerAttributeRel(
			long ldapServerAttributeRelId)
		throws PortalException {

		return getService().getLDAPServerAttributeRel(ldapServerAttributeRelId);
	}

	/**
	 * Returns a range of all the ldap server attribute rels.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.portal.security.ldap.persistence.model.impl.LDAPServerAttributeRelModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of ldap server attribute rels
	 * @param end the upper bound of the range of ldap server attribute rels (not inclusive)
	 * @return the range of ldap server attribute rels
	 */
	public static List<LDAPServerAttributeRel> getLDAPServerAttributeRels(
		int start, int end) {

		return getService().getLDAPServerAttributeRels(start, end);
	}

	public static List<LDAPServerAttributeRel> getLDAPServerAttributeRels(
		long ldapServerId, String className) {

		return getService().getLDAPServerAttributeRels(ldapServerId, className);
	}

	/**
	 * Returns the number of ldap server attribute rels.
	 *
	 * @return the number of ldap server attribute rels
	 */
	public static int getLDAPServerAttributeRelsCount() {
		return getService().getLDAPServerAttributeRelsCount();
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

	public static boolean hasLDAPServerAttributeRel(
		long ldapServerId, String className, long classPK) {

		return getService().hasLDAPServerAttributeRel(
			ldapServerId, className, classPK);
	}

	/**
	 * Updates the ldap server attribute rel in the database or adds it if it does not yet exist. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect LDAPServerAttributeRelLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param ldapServerAttributeRel the ldap server attribute rel
	 * @return the ldap server attribute rel that was updated
	 */
	public static LDAPServerAttributeRel updateLDAPServerAttributeRel(
		LDAPServerAttributeRel ldapServerAttributeRel) {

		return getService().updateLDAPServerAttributeRel(
			ldapServerAttributeRel);
	}

	public static LDAPServerAttributeRelLocalService getService() {
		return _service;
	}

	private static volatile LDAPServerAttributeRelLocalService _service;

}