/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.storage.service;

import com.liferay.portal.kernel.service.ServiceWrapper;
import com.liferay.portal.kernel.service.persistence.BasePersistence;

/**
 * Provides a wrapper for {@link AuditPseudonymLocalService}.
 *
 * @author Brian Wing Shun Chan
 * @see AuditPseudonymLocalService
 * @generated
 */
public class AuditPseudonymLocalServiceWrapper
	implements AuditPseudonymLocalService,
			   ServiceWrapper<AuditPseudonymLocalService> {

	public AuditPseudonymLocalServiceWrapper() {
		this(null);
	}

	public AuditPseudonymLocalServiceWrapper(
		AuditPseudonymLocalService auditPseudonymLocalService) {

		_auditPseudonymLocalService = auditPseudonymLocalService;
	}

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
	@Override
	public com.liferay.portal.security.audit.storage.model.AuditPseudonym
		addAuditPseudonym(
			com.liferay.portal.security.audit.storage.model.AuditPseudonym
				auditPseudonym) {

		return _auditPseudonymLocalService.addAuditPseudonym(auditPseudonym);
	}

	/**
	 * Creates a new audit pseudonym with the primary key. Does not add the audit pseudonym to the database.
	 *
	 * @param auditPseudonymId the primary key for the new audit pseudonym
	 * @return the new audit pseudonym
	 */
	@Override
	public com.liferay.portal.security.audit.storage.model.AuditPseudonym
		createAuditPseudonym(long auditPseudonymId) {

		return _auditPseudonymLocalService.createAuditPseudonym(
			auditPseudonymId);
	}

	/**
	 * @throws PortalException
	 */
	@Override
	public com.liferay.portal.kernel.model.PersistedModel createPersistedModel(
			java.io.Serializable primaryKeyObj)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _auditPseudonymLocalService.createPersistedModel(primaryKeyObj);
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
	@Override
	public com.liferay.portal.security.audit.storage.model.AuditPseudonym
		deleteAuditPseudonym(
			com.liferay.portal.security.audit.storage.model.AuditPseudonym
				auditPseudonym) {

		return _auditPseudonymLocalService.deleteAuditPseudonym(auditPseudonym);
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
	@Override
	public com.liferay.portal.security.audit.storage.model.AuditPseudonym
			deleteAuditPseudonym(long auditPseudonymId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _auditPseudonymLocalService.deleteAuditPseudonym(
			auditPseudonymId);
	}

	/**
	 * @throws PortalException
	 */
	@Override
	public com.liferay.portal.kernel.model.PersistedModel deletePersistedModel(
			com.liferay.portal.kernel.model.PersistedModel persistedModel)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _auditPseudonymLocalService.deletePersistedModel(persistedModel);
	}

	@Override
	public <T> T dslQuery(com.liferay.petra.sql.dsl.query.DSLQuery dslQuery) {
		return _auditPseudonymLocalService.dslQuery(dslQuery);
	}

	@Override
	public int dslQueryCount(
		com.liferay.petra.sql.dsl.query.DSLQuery dslQuery) {

		return _auditPseudonymLocalService.dslQueryCount(dslQuery);
	}

	@Override
	public com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery() {
		return _auditPseudonymLocalService.dynamicQuery();
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

		return _auditPseudonymLocalService.dynamicQuery(dynamicQuery);
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
	@Override
	public <T> java.util.List<T> dynamicQuery(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery, int start,
		int end) {

		return _auditPseudonymLocalService.dynamicQuery(
			dynamicQuery, start, end);
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
	@Override
	public <T> java.util.List<T> dynamicQuery(
		com.liferay.portal.kernel.dao.orm.DynamicQuery dynamicQuery, int start,
		int end,
		com.liferay.portal.kernel.util.OrderByComparator<T> orderByComparator) {

		return _auditPseudonymLocalService.dynamicQuery(
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

		return _auditPseudonymLocalService.dynamicQueryCount(dynamicQuery);
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

		return _auditPseudonymLocalService.dynamicQueryCount(
			dynamicQuery, projection);
	}

	@Override
	public com.liferay.portal.security.audit.storage.model.AuditPseudonym
		fetchAuditPseudonym(long auditPseudonymId) {

		return _auditPseudonymLocalService.fetchAuditPseudonym(
			auditPseudonymId);
	}

	@Override
	public com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery
		getActionableDynamicQuery() {

		return _auditPseudonymLocalService.getActionableDynamicQuery();
	}

	/**
	 * Returns the audit pseudonym with the primary key.
	 *
	 * @param auditPseudonymId the primary key of the audit pseudonym
	 * @return the audit pseudonym
	 * @throws PortalException if a audit pseudonym with the primary key could not be found
	 */
	@Override
	public com.liferay.portal.security.audit.storage.model.AuditPseudonym
			getAuditPseudonym(long auditPseudonymId)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _auditPseudonymLocalService.getAuditPseudonym(auditPseudonymId);
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
	@Override
	public java.util.List
		<com.liferay.portal.security.audit.storage.model.AuditPseudonym>
			getAuditPseudonyms(int start, int end) {

		return _auditPseudonymLocalService.getAuditPseudonyms(start, end);
	}

	/**
	 * Returns the number of audit pseudonyms.
	 *
	 * @return the number of audit pseudonyms
	 */
	@Override
	public int getAuditPseudonymsCount() {
		return _auditPseudonymLocalService.getAuditPseudonymsCount();
	}

	@Override
	public com.liferay.portal.kernel.dao.orm.IndexableActionableDynamicQuery
		getIndexableActionableDynamicQuery() {

		return _auditPseudonymLocalService.getIndexableActionableDynamicQuery();
	}

	@Override
	public com.liferay.portal.security.audit.storage.model.AuditPseudonym
			getOrAddAuditPseudonym(
				long companyId, String contextName, String fieldCategory,
				String identityValue)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _auditPseudonymLocalService.getOrAddAuditPseudonym(
			companyId, contextName, fieldCategory, identityValue);
	}

	/**
	 * Returns the OSGi service identifier.
	 *
	 * @return the OSGi service identifier
	 */
	@Override
	public String getOSGiServiceIdentifier() {
		return _auditPseudonymLocalService.getOSGiServiceIdentifier();
	}

	/**
	 * @throws PortalException
	 */
	@Override
	public com.liferay.portal.kernel.model.PersistedModel getPersistedModel(
			java.io.Serializable primaryKeyObj)
		throws com.liferay.portal.kernel.exception.PortalException {

		return _auditPseudonymLocalService.getPersistedModel(primaryKeyObj);
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
	@Override
	public com.liferay.portal.security.audit.storage.model.AuditPseudonym
		updateAuditPseudonym(
			com.liferay.portal.security.audit.storage.model.AuditPseudonym
				auditPseudonym) {

		return _auditPseudonymLocalService.updateAuditPseudonym(auditPseudonym);
	}

	@Override
	public BasePersistence<?> getBasePersistence() {
		return _auditPseudonymLocalService.getBasePersistence();
	}

	@Override
	public AuditPseudonymLocalService getWrappedService() {
		return _auditPseudonymLocalService;
	}

	@Override
	public void setWrappedService(
		AuditPseudonymLocalService auditPseudonymLocalService) {

		_auditPseudonymLocalService = auditPseudonymLocalService;
	}

	private AuditPseudonymLocalService _auditPseudonymLocalService;

}
// LIFERAY-SERVICE-BUILDER-HASH:2118902039