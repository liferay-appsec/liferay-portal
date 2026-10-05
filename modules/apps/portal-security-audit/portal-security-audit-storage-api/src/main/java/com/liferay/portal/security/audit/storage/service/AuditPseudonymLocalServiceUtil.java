/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.storage.service;

import com.liferay.petra.sql.dsl.query.DSLQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.PersistedModel;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.security.audit.storage.model.AuditPseudonym;

import java.io.Serializable;

import java.util.List;

/**
 * Provides the local service utility for AuditPseudonym. This utility wraps
 * <code>com.liferay.portal.security.audit.storage.service.impl.AuditPseudonymLocalServiceImpl</code> and
 * is an access point for service operations in application layer code running
 * on the local server. Methods of this service will not have security checks
 * based on the propagated JAAS credentials because this service can only be
 * accessed from within the same VM.
 *
 * @author Brian Wing Shun Chan
 * @see AuditPseudonymLocalService
 * @generated
 */
public class AuditPseudonymLocalServiceUtil {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify this class directly. Add custom service methods to <code>com.liferay.portal.security.audit.storage.service.impl.AuditPseudonymLocalServiceImpl</code> and rerun ServiceBuilder to regenerate this class.
	 */

	/**
	 * Adds the audit pseudonym to the database. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect AuditPseudonymLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param auditPseudonym the audit pseudonym
	 * @return the audit pseudonym that was added
	 */
	public static AuditPseudonym addAuditPseudonym(
		AuditPseudonym auditPseudonym) {

		return getService().addAuditPseudonym(auditPseudonym);
	}

	/**
	 * Creates a new audit pseudonym with the primary key. Does not add the audit pseudonym to the database.
	 *
	 * @param auditPseudonymId the primary key for the new audit pseudonym
	 * @return the new audit pseudonym
	 */
	public static AuditPseudonym createAuditPseudonym(long auditPseudonymId) {
		return getService().createAuditPseudonym(auditPseudonymId);
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
	 * Deletes the audit pseudonym from the database. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect AuditPseudonymLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param auditPseudonym the audit pseudonym
	 * @return the audit pseudonym that was removed
	 */
	public static AuditPseudonym deleteAuditPseudonym(
		AuditPseudonym auditPseudonym) {

		return getService().deleteAuditPseudonym(auditPseudonym);
	}

	/**
	 * Deletes the audit pseudonym with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect AuditPseudonymLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param auditPseudonymId the primary key of the audit pseudonym
	 * @return the audit pseudonym that was removed
	 * @throws PortalException if a audit pseudonym with the primary key could not be found
	 */
	public static AuditPseudonym deleteAuditPseudonym(long auditPseudonymId)
		throws PortalException {

		return getService().deleteAuditPseudonym(auditPseudonymId);
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
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.portal.security.audit.storage.model.impl.AuditPseudonymModelImpl</code>.
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
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.portal.security.audit.storage.model.impl.AuditPseudonymModelImpl</code>.
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

	public static AuditPseudonym fetchAuditPseudonym(long auditPseudonymId) {
		return getService().fetchAuditPseudonym(auditPseudonymId);
	}

	public static com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery
		getActionableDynamicQuery() {

		return getService().getActionableDynamicQuery();
	}

	/**
	 * Returns the audit pseudonym with the primary key.
	 *
	 * @param auditPseudonymId the primary key of the audit pseudonym
	 * @return the audit pseudonym
	 * @throws PortalException if a audit pseudonym with the primary key could not be found
	 */
	public static AuditPseudonym getAuditPseudonym(long auditPseudonymId)
		throws PortalException {

		return getService().getAuditPseudonym(auditPseudonymId);
	}

	/**
	 * Returns a range of all the audit pseudonyms.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>com.liferay.portal.security.audit.storage.model.impl.AuditPseudonymModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of audit pseudonyms
	 * @param end the upper bound of the range of audit pseudonyms (not inclusive)
	 * @return the range of audit pseudonyms
	 */
	public static List<AuditPseudonym> getAuditPseudonyms(int start, int end) {
		return getService().getAuditPseudonyms(start, end);
	}

	/**
	 * Returns the number of audit pseudonyms.
	 *
	 * @return the number of audit pseudonyms
	 */
	public static int getAuditPseudonymsCount() {
		return getService().getAuditPseudonymsCount();
	}

	public static
		com.liferay.portal.kernel.dao.orm.IndexableActionableDynamicQuery
			getIndexableActionableDynamicQuery() {

		return getService().getIndexableActionableDynamicQuery();
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

	/**
	 * Updates the audit pseudonym in the database or adds it if it does not yet exist. Also notifies the appropriate model listeners.
	 *
	 * <p>
	 * <strong>Important:</strong> Inspect AuditPseudonymLocalServiceImpl for overloaded versions of the method. If provided, use these entry points to the API, as the implementation logic may require the additional parameters defined there.
	 * </p>
	 *
	 * @param auditPseudonym the audit pseudonym
	 * @return the audit pseudonym that was updated
	 */
	public static AuditPseudonym updateAuditPseudonym(
		AuditPseudonym auditPseudonym) {

		return getService().updateAuditPseudonym(auditPseudonym);
	}

	public static AuditPseudonymLocalService getService() {
		return _serviceSnapshot.get();
	}

	private static final Snapshot<AuditPseudonymLocalService> _serviceSnapshot =
		new Snapshot<>(
			AuditPseudonymLocalServiceUtil.class,
			AuditPseudonymLocalService.class);

}
// LIFERAY-SERVICE-BUILDER-HASH:-1771739895