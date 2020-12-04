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

package com.liferay.portal.security.password.service;

import com.liferay.portal.kernel.service.ServiceWrapper;

/**
 * Provides a wrapper for {@link PasswordMetaLocalService}.
 *
 * @author Arthur Chan
 * @see PasswordMetaLocalService
 * @generated
 */
public class PasswordMetaLocalServiceWrapper
	implements PasswordMetaLocalService,
			   ServiceWrapper<PasswordMetaLocalService> {

	public PasswordMetaLocalServiceWrapper(
		PasswordMetaLocalService passwordMetaLocalService) {

		_passwordMetaLocalService = passwordMetaLocalService;
	}

	/**
	 * Add a passwordMeta for the given passwordEntry during passwordEntry generation.
	 *
	 * @param PasswordEntryId the passwordEntryId
	 * @param salt the password salt
	 * @return Generated meta for passwordEntry
	 * @throws PortalException
	 */
	@Override
	public com.liferay.portal.security.password.model.PasswordMeta
			addPasswordMeta(long passwordEntryId, byte[] salt, String pepperId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _passwordMetaLocalService.addPasswordMeta(
			passwordEntryId, salt, pepperId);
	}

	/**
	 * Adds the password meta to the database. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect PasswordMetaLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param passwordMeta the password meta
	 * @return the password meta that was added
	 */
	@Override
	public com.liferay.portal.security.password.model.PasswordMeta
		addPasswordMeta(
			com.liferay.portal.security.password.model.PasswordMeta
				passwordMeta) {

		return _passwordMetaLocalService.addPasswordMeta(passwordMeta);
	}

	/**
	 * Creates a new password meta with the primary key. Does not add the password meta to the database.
	 *
	 * @param passwordMetaId the primary key for the new password meta
	 * @return the new password meta
	 */
	@Override
	public com.liferay.portal.security.password.model.PasswordMeta
		createPasswordMeta(long passwordMetaId) {

		return _passwordMetaLocalService.createPasswordMeta(passwordMetaId);
	}

	/**
	 * Deletes the password meta with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect PasswordMetaLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param passwordMetaId the primary key of the password meta
	 * @return the password meta that was removed
	 * @throws PortalException if a password meta with the primary key could not be found
	 */
	@Override
	public com.liferay.portal.security.password.model.PasswordMeta
			deletePasswordMeta(long passwordMetaId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _passwordMetaLocalService.deletePasswordMeta(passwordMetaId);
	}

	/**
	 * Deletes the password meta from the database. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect PasswordMetaLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param passwordMeta the password meta
	 * @return the password meta that was removed
	 */
	@Override
	public com.liferay.portal.security.password.model.PasswordMeta
		deletePasswordMeta(
			com.liferay.portal.security.password.model.PasswordMeta
				passwordMeta) {

		return _passwordMetaLocalService.deletePasswordMeta(passwordMeta);
	}

	/**
	 * Delete all password metas associated with the given passwordEntry
	 *
	 * @param PasswordEntryId the passwordEntryId
	 */
	@Override
	public void deletePasswordMetasByPasswordEntry(
		com.liferay.portal.security.password.model.PasswordEntry
			passwordEntry) {

		_passwordMetaLocalService.deletePasswordMetasByPasswordEntry(
			passwordEntry);
	}

	/**
	 * Delete all password metas associated with the given passwordEntry Id
	 *
	 * @param PasswordEntryId the passwordEntryId
	 */
	@Override
	public void deletePasswordMetasByPasswordEntryId(long passwordEntryId) {
		_passwordMetaLocalService.deletePasswordMetasByPasswordEntryId(
			passwordEntryId);
	}

	/**
	 * @throws PortalException
	 */
	@Override
	public com.liferay.portal.kernel.model.PersistedModel deletePersistedModel(
			com.liferay.portal.kernel.model.PersistedModel persistedModel)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _passwordMetaLocalService.deletePersistedModel(persistedModel);
	}

	@Override
	public <T> T dslQuery(com.liferay.petra.sql.dsl.query.DSLQuery dslQuery) {
		return _passwordMetaLocalService.dslQuery(dslQuery);
	}

	@Override
	public com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery() {
		return _passwordMetaLocalService.dynamicQuery();
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

		return _passwordMetaLocalService.dynamicQuery(dynamicQuery);
	}

	/**
	 * Performs a dynamic query on the database and returns a range of the matching rows.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.portal.security.password.model.impl.PasswordMetaModelImpl</code>.
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

		return _passwordMetaLocalService.dynamicQuery(dynamicQuery, start, end);
	}

	/**
	 * Performs a dynamic query on the database and returns an ordered range of the matching rows.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.portal.security.password.model.impl.PasswordMetaModelImpl</code>.
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

		return _passwordMetaLocalService.dynamicQuery(
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

		return _passwordMetaLocalService.dynamicQueryCount(dynamicQuery);
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

		return _passwordMetaLocalService.dynamicQueryCount(
			dynamicQuery, projection);
	}

	@Override
	public com.liferay.portal.security.password.model.PasswordMeta
		fetchPasswordMeta(long passwordMetaId) {

		return _passwordMetaLocalService.fetchPasswordMeta(passwordMetaId);
	}

	/**
	 * Returns the password meta with the matching UUID and company.
	 *
	 * @param uuid the password meta's UUID
	 * @param companyId the primary key of the company
	 * @return the matching password meta, or <code>null</code> if a matching password meta could not be found
	 */
	@Override
	public com.liferay.portal.security.password.model.PasswordMeta
		fetchPasswordMetaByUuidAndCompanyId(String uuid, long companyId) {

		return _passwordMetaLocalService.fetchPasswordMetaByUuidAndCompanyId(
			uuid, companyId);
	}

	@Override
	public com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery
		getActionableDynamicQuery() {

		return _passwordMetaLocalService.getActionableDynamicQuery();
	}

	@Override
	public com.liferay.portal.kernel.dao.orm.IndexableActionableDynamicQuery
		getIndexableActionableDynamicQuery() {

		return _passwordMetaLocalService.getIndexableActionableDynamicQuery();
	}

	/**
	 * Returns the OSGi service identifier.
	 *
	 * @return the OSGi service identifier
	 */
	@Override
	public String getOSGiServiceIdentifier() {
		return _passwordMetaLocalService.getOSGiServiceIdentifier();
	}

	/**
	 * Returns the password meta with the primary key.
	 *
	 * @param passwordMetaId the primary key of the password meta
	 * @return the password meta
	 * @throws PortalException if a password meta with the primary key could not be found
	 */
	@Override
	public com.liferay.portal.security.password.model.PasswordMeta
			getPasswordMeta(long passwordMetaId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _passwordMetaLocalService.getPasswordMeta(passwordMetaId);
	}

	/**
	 * Returns the password meta with the matching UUID and company.
	 *
	 * @param uuid the password meta's UUID
	 * @param companyId the primary key of the company
	 * @return the matching password meta
	 * @throws PortalException if a matching password meta could not be found
	 */
	@Override
	public com.liferay.portal.security.password.model.PasswordMeta
			getPasswordMetaByUuidAndCompanyId(String uuid, long companyId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _passwordMetaLocalService.getPasswordMetaByUuidAndCompanyId(
			uuid, companyId);
	}

	/**
	 * Returns a range of all the password metas.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.portal.security.password.model.impl.PasswordMetaModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of password metas
	 * @param end the upper bound of the range of password metas (not inclusive)
	 * @return the range of password metas
	 */
	@Override
	public java.util.List
		<com.liferay.portal.security.password.model.PasswordMeta>
			getPasswordMetas(int start, int end) {

		return _passwordMetaLocalService.getPasswordMetas(start, end);
	}

	/**
	 * Return a list of metas of PasswordEntry, asc ordered by create date
	 *
	 * @param PasswordEntry the passwordEntry
	 * @return a list of metas of PasswordEntry
	 * @throws PortalException
	 */
	@Override
	public java.util.List
		<com.liferay.portal.security.password.model.PasswordMeta>
				getPasswordMetasByPasswordEntry(
					com.liferay.portal.security.password.model.PasswordEntry
						passwordEntry)
			throws com.liferay.portal.kernel.exception.PortalException {

		return _passwordMetaLocalService.getPasswordMetasByPasswordEntry(
			passwordEntry);
	}

	/**
	 * Return a list of metas of PasswordEntry, asc ordered by create date
	 *
	 * @param PasswordEntryId the ID of passwordEntry
	 * @return a list of metas of PasswordEntry
	 * @throws PortalException
	 */
	@Override
	public java.util.List
		<com.liferay.portal.security.password.model.PasswordMeta>
				getPasswordMetasByPasswordEntryId(long passwordEntryId)
			throws com.liferay.portal.kernel.exception.PortalException {

		return _passwordMetaLocalService.getPasswordMetasByPasswordEntryId(
			passwordEntryId);
	}

	/**
	 * Returns the number of password metas.
	 *
	 * @return the number of password metas
	 */
	@Override
	public int getPasswordMetasCount() {
		return _passwordMetaLocalService.getPasswordMetasCount();
	}

	@Override
	public int getPasswordMetasCountByPasswordEntry(
			com.liferay.portal.security.password.model.PasswordEntry
				passwordEntry)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _passwordMetaLocalService.getPasswordMetasCountByPasswordEntry(
			passwordEntry);
	}

	@Override
	public int getPasswordMetasCountByPasswordEntryId(long passwordEntryId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _passwordMetaLocalService.getPasswordMetasCountByPasswordEntryId(
			passwordEntryId);
	}

	/**
	 * @throws PortalException
	 */
	@Override
	public com.liferay.portal.kernel.model.PersistedModel getPersistedModel(
			java.io.Serializable primaryKeyObj)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _passwordMetaLocalService.getPersistedModel(primaryKeyObj);
	}

	/**
	 * Updates the password meta in the database or adds it if it does not yet exist. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect PasswordMetaLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param passwordMeta the password meta
	 * @return the password meta that was updated
	 */
	@Override
	public com.liferay.portal.security.password.model.PasswordMeta
		updatePasswordMeta(
			com.liferay.portal.security.password.model.PasswordMeta
				passwordMeta) {

		return _passwordMetaLocalService.updatePasswordMeta(passwordMeta);
	}

	@Override
	public PasswordMetaLocalService getWrappedService() {
		return _passwordMetaLocalService;
	}

	@Override
	public void setWrappedService(
		PasswordMetaLocalService passwordMetaLocalService) {

		_passwordMetaLocalService = passwordMetaLocalService;
	}

	private PasswordMetaLocalService _passwordMetaLocalService;

}