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

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Provides the local service utility for PasswordEntry. This utility wraps
 * <code>com.liferay.portal.security.password.service.impl.PasswordEntryLocalServiceImpl</code> and
 * is an access point for service operations in application layer code running
 * on the local server. Methods of this service will not have security checks
 * based on the propagated JAAS credentials because this service can only be
 * accessed from within the same VM.
 *
 * @author Arthur Chan
 * @see PasswordEntryLocalService
 * @generated
 */
public class PasswordEntryLocalServiceUtil {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify this class directly. Add custom service methods to <code>com.liferay.portal.security.password.service.impl.PasswordEntryLocalServiceImpl</code> and rerun ServiceBuilder to regenerate this class.
	 */

	/**
	 * If current hash generator is no longer considered secured, allow to
	 * apply a new and secured hash generator to the persisted password hash
	 * so that the persisted password will remain secured.
	 *
	 * @param UserId the userId
	 * @param PasswordHashGenerator the passwordHashGenerator
	 * @return Same passowrdEntry with different hash
	 * @throws PortalException
	 */
	public static com.liferay.portal.security.password.model.PasswordEntry
			addHashGeneratorToCurrentPasswordEntryByUserId(long userId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return getService().addHashGeneratorToCurrentPasswordEntryByUserId(
			userId);
	}

	/**
	 * Add a passwordEntry for user of userId
	 *
	 * @param UserId ID of user
	 * @param Password plain text password
	 * @return Generated PassowrdEntry
	 * @throws PortalException
	 */
	public static com.liferay.portal.security.password.model.PasswordEntry
			addPasswordEntry(long userId, String password)
		throws com.liferay.portal.kernel.exception.PortalException {

		return getService().addPasswordEntry(userId, password);
	}

	/**
	 * Adds the password entry to the database. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect PasswordEntryLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param passwordEntry the password entry
	 * @return the password entry that was added
	 */
	public static com.liferay.portal.security.password.model.PasswordEntry
		addPasswordEntry(
			com.liferay.portal.security.password.model.PasswordEntry
				passwordEntry) {

		return getService().addPasswordEntry(passwordEntry);
	}

	/**
	 * Creates a new password entry with the primary key. Does not add the password entry to the database.
	 *
	 * @param passwordEntryId the primary key for the new password entry
	 * @return the new password entry
	 */
	public static com.liferay.portal.security.password.model.PasswordEntry
		createPasswordEntry(long passwordEntryId) {

		return getService().createPasswordEntry(passwordEntryId);
	}

	/**
	 * Remove all password entries for given user, including current password and history passwords.
	 *
	 * @param UserId ID of user
	 */
	public static void deletePasswordEntriesByUserId(long userId) {
		getService().deletePasswordEntriesByUserId(userId);
	}

	/**
	 * Deletes the password entry with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect PasswordEntryLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param passwordEntryId the primary key of the password entry
	 * @return the password entry that was removed
	 * @throws PortalException if a password entry with the primary key could not be found
	 */
	public static com.liferay.portal.security.password.model.PasswordEntry
			deletePasswordEntry(long passwordEntryId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return getService().deletePasswordEntry(passwordEntryId);
	}

	/**
	 * Deletes the password entry from the database. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect PasswordEntryLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param passwordEntry the password entry
	 * @return the password entry that was removed
	 */
	public static com.liferay.portal.security.password.model.PasswordEntry
		deletePasswordEntry(
			com.liferay.portal.security.password.model.PasswordEntry
				passwordEntry) {

		return getService().deletePasswordEntry(passwordEntry);
	}

	/**
	 * @throws PortalException
	 */
	public static com.liferay.portal.kernel.model.PersistedModel
			deletePersistedModel(
				com.liferay.portal.kernel.model.PersistedModel persistedModel)
		throws com.liferay.portal.kernel.exception.PortalException {

		return getService().deletePersistedModel(persistedModel);
	}

	public static <T> T dslQuery(
		com.liferay.petra.sql.dsl.query.DSLQuery dslQuery) {

		return getService().dslQuery(dslQuery);
	}

	public static com.liferay.portal.kernel.dao.orm.DynamicQuery
		dynamicQuery() {

		return getService().dynamicQuery();
	}

	/**
	 * Performs a dynamic query on the database and returns the matching rows.
	 *
	 * @param dynamicQuery the dynamic query
	 * @return the matching rows
	 */
	public static <T> java.util.List<T> dynamicQuery(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery) {

		return getService().dynamicQuery(dynamicQuery);
	}

	/**
	 * Performs a dynamic query on the database and returns a range of the matching rows.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.portal.security.password.model.impl.PasswordEntryModelImpl</code>.
	 * </p>
	 *
	 * @param dynamicQuery the dynamic query
	 * @param start the lower bound of the range of model instances
	 * @param end the upper bound of the range of model instances (not inclusive)
	 * @return the range of matching rows
	 */
	public static <T> java.util.List<T> dynamicQuery(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery, int start,
		int end) {

		return getService().dynamicQuery(dynamicQuery, start, end);
	}

	/**
	 * Performs a dynamic query on the database and returns an ordered range of the matching rows.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.portal.security.password.model.impl.PasswordEntryModelImpl</code>.
	 * </p>
	 *
	 * @param dynamicQuery the dynamic query
	 * @param start the lower bound of the range of model instances
	 * @param end the upper bound of the range of model instances (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching rows
	 */
	public static <T> java.util.List<T> dynamicQuery(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery, int start,
		int end,
		com.liferay.portal.kernel.util.OrderByComparator<T> orderByComparator) {

		return getService().dynamicQuery(
			dynamicQuery, start, end, orderByComparator);
	}

	/**
	 * Returns the number of rows matching the dynamic query.
	 *
	 * @param dynamicQuery the dynamic query
	 * @return the number of rows matching the dynamic query
	 */
	public static long dynamicQueryCount(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery) {

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
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery,
		com.liferay.portal.kernel.dao.orm.Projection projection) {

		return getService().dynamicQueryCount(dynamicQuery, projection);
	}

	public static com.liferay.portal.security.password.model.PasswordEntry
		fetchPasswordEntry(long passwordEntryId) {

		return getService().fetchPasswordEntry(passwordEntryId);
	}

	/**
	 * Returns the password entry with the matching UUID and company.
	 *
	 * @param uuid the password entry's UUID
	 * @param companyId the primary key of the company
	 * @return the matching password entry, or <code>null</code> if a matching password entry could not be found
	 */
	public static com.liferay.portal.security.password.model.PasswordEntry
		fetchPasswordEntryByUuidAndCompanyId(String uuid, long companyId) {

		return getService().fetchPasswordEntryByUuidAndCompanyId(
			uuid, companyId);
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
	 * Return current password for given user.
	 *
	 * @param UserId ID of user
	 * @return PasswordEntry
	 * @throws PortalException
	 */
	public static com.liferay.portal.security.password.model.PasswordEntry
			getLastPasswordEntryByUserId(long userId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return getService().getLastPasswordEntryByUserId(userId);
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
	 * Returns a range of all the password entries.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.portal.security.password.model.impl.PasswordEntryModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of password entries
	 * @param end the upper bound of the range of password entries (not inclusive)
	 * @return the range of password entries
	 */
	public static java.util.List
		<com.liferay.portal.security.password.model.PasswordEntry>
			getPasswordEntries(int start, int end) {

		return getService().getPasswordEntries(start, end);
	}

	/**
	 * Return all password entries for given user, including current password and history passwords. Ordered by modified date.
	 *
	 * @param UserId ID of user
	 * @return A list of password entries
	 */
	public static java.util.List
		<com.liferay.portal.security.password.model.PasswordEntry>
			getPasswordEntriesByUserId(long userId) {

		return getService().getPasswordEntriesByUserId(userId);
	}

	/**
	 * Returns the number of password entries.
	 *
	 * @return the number of password entries
	 */
	public static int getPasswordEntriesCount() {
		return getService().getPasswordEntriesCount();
	}

	/**
	 * Returns the password entry with the primary key.
	 *
	 * @param passwordEntryId the primary key of the password entry
	 * @return the password entry
	 * @throws PortalException if a password entry with the primary key could not be found
	 */
	public static com.liferay.portal.security.password.model.PasswordEntry
			getPasswordEntry(long passwordEntryId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return getService().getPasswordEntry(passwordEntryId);
	}

	/**
	 * Returns the password entry with the matching UUID and company.
	 *
	 * @param uuid the password entry's UUID
	 * @param companyId the primary key of the company
	 * @return the matching password entry
	 * @throws PortalException if a matching password entry could not be found
	 */
	public static com.liferay.portal.security.password.model.PasswordEntry
			getPasswordEntryByUuidAndCompanyId(String uuid, long companyId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return getService().getPasswordEntryByUuidAndCompanyId(uuid, companyId);
	}

	/**
	 * @throws PortalException
	 */
	public static com.liferay.portal.kernel.model.PersistedModel
			getPersistedModel(java.io.Serializable primaryKeyObj)
		throws com.liferay.portal.kernel.exception.PortalException {

		return getService().getPersistedModel(primaryKeyObj);
	}

	/**
	 * When updating passwordEntries for a user, two things can happen:
	 * 1. when passwordPolicy is set to have password history,
	 *    remove extra history passwords that are over history count limit,
	 *    and return a newly added password entry for the user.
	 * 2. otherwise,
	 *    remove all passwords and return a newly added password entry.
	 *
	 * @param UserId ID of user
	 * @param Password plain text password
	 * @return Updated PassowrdEntry
	 * @throws PortalException
	 */
	public static com.liferay.portal.security.password.model.PasswordEntry
			updatePasswordEntriesByUserId(
				long userId, String password,
				com.liferay.portal.security.password.model.PasswordHashGenerator
					passwordHashGenerator)
		throws com.liferay.portal.kernel.exception.PortalException {

		return getService().updatePasswordEntriesByUserId(
			userId, password, passwordHashGenerator);
	}

	/**
	 * Updates the password entry in the database or adds it if it does not yet exist. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect PasswordEntryLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param passwordEntry the password entry
	 * @return the password entry that was updated
	 */
	public static com.liferay.portal.security.password.model.PasswordEntry
		updatePasswordEntry(
			com.liferay.portal.security.password.model.PasswordEntry
				passwordEntry) {

		return getService().updatePasswordEntry(passwordEntry);
	}

	public static PasswordEntryLocalService getService() {
		return _serviceTracker.getService();
	}

	private static ServiceTracker
		<PasswordEntryLocalService, PasswordEntryLocalService> _serviceTracker;

	static {
		Bundle bundle = FrameworkUtil.getBundle(
			PasswordEntryLocalService.class);

		ServiceTracker<PasswordEntryLocalService, PasswordEntryLocalService>
			serviceTracker =
				new ServiceTracker
					<PasswordEntryLocalService, PasswordEntryLocalService>(
						bundle.getBundleContext(),
						PasswordEntryLocalService.class, null);

		serviceTracker.open();

		_serviceTracker = serviceTracker;
	}

}