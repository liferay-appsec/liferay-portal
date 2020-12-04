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
 * Provides a wrapper for {@link PasswordHashGeneratorLocalService}.
 *
 * @author Arthur Chan
 * @see PasswordHashGeneratorLocalService
 * @generated
 */
public class PasswordHashGeneratorLocalServiceWrapper
	implements PasswordHashGeneratorLocalService,
			   ServiceWrapper<PasswordHashGeneratorLocalService> {

	public PasswordHashGeneratorLocalServiceWrapper(
		PasswordHashGeneratorLocalService passwordHashGeneratorLocalService) {

		_passwordHashGeneratorLocalService = passwordHashGeneratorLocalService;
	}

	/**
	 * Adds the password hash generator to the database. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect PasswordHashGeneratorLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param passwordHashGenerator the password hash generator
	 * @return the password hash generator that was added
	 */
	@Override
	public com.liferay.portal.security.password.model.PasswordHashGenerator
		addPasswordHashGenerator(
			com.liferay.portal.security.password.model.PasswordHashGenerator
				passwordHashGenerator) {

		return _passwordHashGeneratorLocalService.addPasswordHashGenerator(
			passwordHashGenerator);
	}

	/**
	 * Creates a new password hash generator with the primary key. Does not add the password hash generator to the database.
	 *
	 * @param passwordHashGeneratorId the primary key for the new password hash generator
	 * @return the new password hash generator
	 */
	@Override
	public com.liferay.portal.security.password.model.PasswordHashGenerator
		createPasswordHashGenerator(long passwordHashGeneratorId) {

		return _passwordHashGeneratorLocalService.createPasswordHashGenerator(
			passwordHashGeneratorId);
	}

	/**
	 * Deletes the password hash generator with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect PasswordHashGeneratorLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param passwordHashGeneratorId the primary key of the password hash generator
	 * @return the password hash generator that was removed
	 * @throws PortalException if a password hash generator with the primary key could not be found
	 */
	@Override
	public com.liferay.portal.security.password.model.PasswordHashGenerator
			deletePasswordHashGenerator(long passwordHashGeneratorId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _passwordHashGeneratorLocalService.deletePasswordHashGenerator(
			passwordHashGeneratorId);
	}

	/**
	 * Deletes the password hash generator from the database. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect PasswordHashGeneratorLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param passwordHashGenerator the password hash generator
	 * @return the password hash generator that was removed
	 */
	@Override
	public com.liferay.portal.security.password.model.PasswordHashGenerator
		deletePasswordHashGenerator(
			com.liferay.portal.security.password.model.PasswordHashGenerator
				passwordHashGenerator) {

		return _passwordHashGeneratorLocalService.deletePasswordHashGenerator(
			passwordHashGenerator);
	}

	/**
	 * @throws PortalException
	 */
	@Override
	public com.liferay.portal.kernel.model.PersistedModel deletePersistedModel(
			com.liferay.portal.kernel.model.PersistedModel persistedModel)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _passwordHashGeneratorLocalService.deletePersistedModel(
			persistedModel);
	}

	@Override
	public <T> T dslQuery(com.liferay.petra.sql.dsl.query.DSLQuery dslQuery) {
		return _passwordHashGeneratorLocalService.dslQuery(dslQuery);
	}

	@Override
	public com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery() {
		return _passwordHashGeneratorLocalService.dynamicQuery();
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

		return _passwordHashGeneratorLocalService.dynamicQuery(dynamicQuery);
	}

	/**
	 * Performs a dynamic query on the database and returns a range of the matching rows.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.portal.security.password.model.impl.PasswordHashGeneratorModelImpl</code>.
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

		return _passwordHashGeneratorLocalService.dynamicQuery(
			dynamicQuery, start, end);
	}

	/**
	 * Performs a dynamic query on the database and returns an ordered range of the matching rows.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.portal.security.password.model.impl.PasswordHashGeneratorModelImpl</code>.
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

		return _passwordHashGeneratorLocalService.dynamicQuery(
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

		return _passwordHashGeneratorLocalService.dynamicQueryCount(
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

		return _passwordHashGeneratorLocalService.dynamicQueryCount(
			dynamicQuery, projection);
	}

	@Override
	public com.liferay.portal.security.password.model.PasswordHashGenerator
		fetchPasswordHashGenerator(long passwordHashGeneratorId) {

		return _passwordHashGeneratorLocalService.fetchPasswordHashGenerator(
			passwordHashGeneratorId);
	}

	/**
	 * Returns the password hash generator with the matching UUID and company.
	 *
	 * @param uuid the password hash generator's UUID
	 * @param companyId the primary key of the company
	 * @return the matching password hash generator, or <code>null</code> if a matching password hash generator could not be found
	 */
	@Override
	public com.liferay.portal.security.password.model.PasswordHashGenerator
		fetchPasswordHashGeneratorByUuidAndCompanyId(
			String uuid, long companyId) {

		return _passwordHashGeneratorLocalService.
			fetchPasswordHashGeneratorByUuidAndCompanyId(uuid, companyId);
	}

	@Override
	public com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery
		getActionableDynamicQuery() {

		return _passwordHashGeneratorLocalService.getActionableDynamicQuery();
	}

	@Override
	public com.liferay.portal.kernel.dao.orm.IndexableActionableDynamicQuery
		getIndexableActionableDynamicQuery() {

		return _passwordHashGeneratorLocalService.
			getIndexableActionableDynamicQuery();
	}

	/**
	 * Get the latest password hash generator ordered by created date
	 *
	 * @return the latest password hash generator ordered by created date
	 */
	@Override
	public com.liferay.portal.security.password.model.PasswordHashGenerator
			getLastPasswordHashGenerator()
		throws com.liferay.portal.kernel.exception.PortalException {

		return _passwordHashGeneratorLocalService.
			getLastPasswordHashGenerator();
	}

	/**
	 * Get the latest password hash generator before a date, ordered by created date
	 *
	 * @param date the given date
	 * @return the latest password hash generator before a date,  ordered by created date
	 */
	@Override
	public com.liferay.portal.security.password.model.PasswordHashGenerator
			getLastPasswordHashGeneratorBeforeDate(java.util.Date date)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _passwordHashGeneratorLocalService.
			getLastPasswordHashGeneratorBeforeDate(date);
	}

	/**
	 * Returns the OSGi service identifier.
	 *
	 * @return the OSGi service identifier
	 */
	@Override
	public String getOSGiServiceIdentifier() {
		return _passwordHashGeneratorLocalService.getOSGiServiceIdentifier();
	}

	/**
	 * Returns the password hash generator with the primary key.
	 *
	 * @param passwordHashGeneratorId the primary key of the password hash generator
	 * @return the password hash generator
	 * @throws PortalException if a password hash generator with the primary key could not be found
	 */
	@Override
	public com.liferay.portal.security.password.model.PasswordHashGenerator
			getPasswordHashGenerator(long passwordHashGeneratorId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _passwordHashGeneratorLocalService.getPasswordHashGenerator(
			passwordHashGeneratorId);
	}

	/**
	 * Returns the password hash generator with the matching UUID and company.
	 *
	 * @param uuid the password hash generator's UUID
	 * @param companyId the primary key of the company
	 * @return the matching password hash generator
	 * @throws PortalException if a matching password hash generator could not be found
	 */
	@Override
	public com.liferay.portal.security.password.model.PasswordHashGenerator
			getPasswordHashGeneratorByUuidAndCompanyId(
				String uuid, long companyId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _passwordHashGeneratorLocalService.
			getPasswordHashGeneratorByUuidAndCompanyId(uuid, companyId);
	}

	/**
	 * Returns a range of all the password hash generators.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.portal.security.password.model.impl.PasswordHashGeneratorModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of password hash generators
	 * @param end the upper bound of the range of password hash generators (not inclusive)
	 * @return the range of password hash generators
	 */
	@Override
	public java.util.List
		<com.liferay.portal.security.password.model.PasswordHashGenerator>
			getPasswordHashGenerators(int start, int end) {

		return _passwordHashGeneratorLocalService.getPasswordHashGenerators(
			start, end);
	}

	/**
	 * Returns the number of password hash generators.
	 *
	 * @return the number of password hash generators
	 */
	@Override
	public int getPasswordHashGeneratorsCount() {
		return _passwordHashGeneratorLocalService.
			getPasswordHashGeneratorsCount();
	}

	/**
	 * @throws PortalException
	 */
	@Override
	public com.liferay.portal.kernel.model.PersistedModel getPersistedModel(
			java.io.Serializable primaryKeyObj)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _passwordHashGeneratorLocalService.getPersistedModel(
			primaryKeyObj);
	}

	/**
	 * Updates the password hash generator in the database or adds it if it does not yet exist. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect PasswordHashGeneratorLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param passwordHashGenerator the password hash generator
	 * @return the password hash generator that was updated
	 */
	@Override
	public com.liferay.portal.security.password.model.PasswordHashGenerator
		updatePasswordHashGenerator(
			com.liferay.portal.security.password.model.PasswordHashGenerator
				passwordHashGenerator) {

		return _passwordHashGeneratorLocalService.updatePasswordHashGenerator(
			passwordHashGenerator);
	}

	@Override
	public PasswordHashGeneratorLocalService getWrappedService() {
		return _passwordHashGeneratorLocalService;
	}

	@Override
	public void setWrappedService(
		PasswordHashGeneratorLocalService passwordHashGeneratorLocalService) {

		_passwordHashGeneratorLocalService = passwordHashGeneratorLocalService;
	}

	private PasswordHashGeneratorLocalService
		_passwordHashGeneratorLocalService;

}