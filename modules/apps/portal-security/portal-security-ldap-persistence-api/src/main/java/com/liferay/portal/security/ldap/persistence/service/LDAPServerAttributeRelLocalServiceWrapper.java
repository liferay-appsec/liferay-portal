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

import com.liferay.portal.kernel.service.ServiceWrapper;

/**
 * Provides a wrapper for {@link LDAPServerAttributeRelLocalService}.
 *
 * @author Brian Wing Shun Chan
 * @see LDAPServerAttributeRelLocalService
 * @generated
 */
public class LDAPServerAttributeRelLocalServiceWrapper
	implements LDAPServerAttributeRelLocalService,
			   ServiceWrapper<LDAPServerAttributeRelLocalService> {

	public LDAPServerAttributeRelLocalServiceWrapper() {
		this(null);
	}

	public LDAPServerAttributeRelLocalServiceWrapper(
		LDAPServerAttributeRelLocalService ldapServerAttributeRelLocalService) {

		_ldapServerAttributeRelLocalService =
			ldapServerAttributeRelLocalService;
	}

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
	@Override
	public
		com.liferay.portal.security.ldap.persistence.model.
			LDAPServerAttributeRel addLDAPServerAttributeRel(
				com.liferay.portal.security.ldap.persistence.model.
					LDAPServerAttributeRel ldapServerAttributeRel) {

		return _ldapServerAttributeRelLocalService.addLDAPServerAttributeRel(
			ldapServerAttributeRel);
	}

	@Override
	public
		com.liferay.portal.security.ldap.persistence.model.
			LDAPServerAttributeRel addLDAPServerAttributeRel(
					long ldapServerId, String className, long classPK)
				throws com.liferay.portal.kernel.exception.PortalException {

		return _ldapServerAttributeRelLocalService.addLDAPServerAttributeRel(
			ldapServerId, className, classPK);
	}

	/**
	 * Creates a new ldap server attribute rel with the primary key. Does not add the ldap server attribute rel to the database.
	 *
	 * @param ldapServerAttributeRelId the primary key for the new ldap server attribute rel
	 * @return the new ldap server attribute rel
	 */
	@Override
	public
		com.liferay.portal.security.ldap.persistence.model.
			LDAPServerAttributeRel createLDAPServerAttributeRel(
				long ldapServerAttributeRelId) {

		return _ldapServerAttributeRelLocalService.createLDAPServerAttributeRel(
			ldapServerAttributeRelId);
	}

	/**
	 * @throws PortalException
	 */
	@Override
	public com.liferay.portal.kernel.model.PersistedModel createPersistedModel(
			java.io.Serializable primaryKeyObj)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _ldapServerAttributeRelLocalService.createPersistedModel(
			primaryKeyObj);
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
	@Override
	public
		com.liferay.portal.security.ldap.persistence.model.
			LDAPServerAttributeRel deleteLDAPServerAttributeRel(
				com.liferay.portal.security.ldap.persistence.model.
					LDAPServerAttributeRel ldapServerAttributeRel) {

		return _ldapServerAttributeRelLocalService.deleteLDAPServerAttributeRel(
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
	@Override
	public
		com.liferay.portal.security.ldap.persistence.model.
			LDAPServerAttributeRel deleteLDAPServerAttributeRel(
					long ldapServerAttributeRelId)
				throws com.liferay.portal.kernel.exception.PortalException {

		return _ldapServerAttributeRelLocalService.deleteLDAPServerAttributeRel(
			ldapServerAttributeRelId);
	}

	@Override
	public void deleteLDAPServerAttributeRel(
			long ldapServerId, String className, long classPK)
		throws com.liferay.portal.kernel.exception.PortalException {

		_ldapServerAttributeRelLocalService.deleteLDAPServerAttributeRel(
			ldapServerId, className, classPK);
	}

	/**
	 * @throws PortalException
	 */
	@Override
	public com.liferay.portal.kernel.model.PersistedModel deletePersistedModel(
			com.liferay.portal.kernel.model.PersistedModel persistedModel)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _ldapServerAttributeRelLocalService.deletePersistedModel(
			persistedModel);
	}

	@Override
	public <T> T dslQuery(com.liferay.petra.sql.dsl.query.DSLQuery dslQuery) {
		return _ldapServerAttributeRelLocalService.dslQuery(dslQuery);
	}

	@Override
	public int dslQueryCount(
		com.liferay.petra.sql.dsl.query.DSLQuery dslQuery) {

		return _ldapServerAttributeRelLocalService.dslQueryCount(dslQuery);
	}

	@Override
	public com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery() {
		return _ldapServerAttributeRelLocalService.dynamicQuery();
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

		return _ldapServerAttributeRelLocalService.dynamicQuery(dynamicQuery);
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
	@Override
	public <T> java.util.List<T> dynamicQuery(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery, int start,
		int end) {

		return _ldapServerAttributeRelLocalService.dynamicQuery(
			dynamicQuery, start, end);
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
	@Override
	public <T> java.util.List<T> dynamicQuery(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery, int start,
		int end,
		com.liferay.portal.kernel.util.OrderByComparator<T> orderByComparator) {

		return _ldapServerAttributeRelLocalService.dynamicQuery(
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

		return _ldapServerAttributeRelLocalService.dynamicQueryCount(
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

		return _ldapServerAttributeRelLocalService.dynamicQueryCount(
			dynamicQuery, projection);
	}

	@Override
	public
		com.liferay.portal.security.ldap.persistence.model.
			LDAPServerAttributeRel fetchLDAPServerAttributeRel(
				long ldapServerAttributeRelId) {

		return _ldapServerAttributeRelLocalService.fetchLDAPServerAttributeRel(
			ldapServerAttributeRelId);
	}

	@Override
	public com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery
		getActionableDynamicQuery() {

		return _ldapServerAttributeRelLocalService.getActionableDynamicQuery();
	}

	@Override
	public com.liferay.portal.kernel.dao.orm.IndexableActionableDynamicQuery
		getIndexableActionableDynamicQuery() {

		return _ldapServerAttributeRelLocalService.
			getIndexableActionableDynamicQuery();
	}

	/**
	 * Returns the ldap server attribute rel with the primary key.
	 *
	 * @param ldapServerAttributeRelId the primary key of the ldap server attribute rel
	 * @return the ldap server attribute rel
	 * @throws PortalException if a ldap server attribute rel with the primary key could not be found
	 */
	@Override
	public
		com.liferay.portal.security.ldap.persistence.model.
			LDAPServerAttributeRel getLDAPServerAttributeRel(
					long ldapServerAttributeRelId)
				throws com.liferay.portal.kernel.exception.PortalException {

		return _ldapServerAttributeRelLocalService.getLDAPServerAttributeRel(
			ldapServerAttributeRelId);
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
	@Override
	public java.util.List
		<com.liferay.portal.security.ldap.persistence.model.
			LDAPServerAttributeRel> getLDAPServerAttributeRels(
				int start, int end) {

		return _ldapServerAttributeRelLocalService.getLDAPServerAttributeRels(
			start, end);
	}

	@Override
	public java.util.List
		<com.liferay.portal.security.ldap.persistence.model.
			LDAPServerAttributeRel> getLDAPServerAttributeRels(
				long ldapServerId, String className) {

		return _ldapServerAttributeRelLocalService.getLDAPServerAttributeRels(
			ldapServerId, className);
	}

	/**
	 * Returns the number of ldap server attribute rels.
	 *
	 * @return the number of ldap server attribute rels
	 */
	@Override
	public int getLDAPServerAttributeRelsCount() {
		return _ldapServerAttributeRelLocalService.
			getLDAPServerAttributeRelsCount();
	}

	/**
	 * Returns the OSGi service identifier.
	 *
	 * @return the OSGi service identifier
	 */
	@Override
	public String getOSGiServiceIdentifier() {
		return _ldapServerAttributeRelLocalService.getOSGiServiceIdentifier();
	}

	/**
	 * @throws PortalException
	 */
	@Override
	public com.liferay.portal.kernel.model.PersistedModel getPersistedModel(
			java.io.Serializable primaryKeyObj)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _ldapServerAttributeRelLocalService.getPersistedModel(
			primaryKeyObj);
	}

	@Override
	public boolean hasLDAPServerAttributeRel(
		long ldapServerId, String className, long classPK) {

		return _ldapServerAttributeRelLocalService.hasLDAPServerAttributeRel(
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
	@Override
	public
		com.liferay.portal.security.ldap.persistence.model.
			LDAPServerAttributeRel updateLDAPServerAttributeRel(
				com.liferay.portal.security.ldap.persistence.model.
					LDAPServerAttributeRel ldapServerAttributeRel) {

		return _ldapServerAttributeRelLocalService.updateLDAPServerAttributeRel(
			ldapServerAttributeRel);
	}

	@Override
	public LDAPServerAttributeRelLocalService getWrappedService() {
		return _ldapServerAttributeRelLocalService;
	}

	@Override
	public void setWrappedService(
		LDAPServerAttributeRelLocalService ldapServerAttributeRelLocalService) {

		_ldapServerAttributeRelLocalService =
			ldapServerAttributeRelLocalService;
	}

	private LDAPServerAttributeRelLocalService
		_ldapServerAttributeRelLocalService;

}