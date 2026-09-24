/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.storage.service.persistence;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.security.audit.storage.model.AuditPseudonym;

import java.io.Serializable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The persistence utility for the audit pseudonym service. This utility wraps <code>com.liferay.portal.security.audit.storage.service.persistence.impl.AuditPseudonymPersistenceImpl</code> and provides direct access to the database for CRUD operations. This utility should only be used by the service layer, as it must operate within a transaction. Never access this utility in a JSP, controller, model, or other front-end class.
 *
 * <p>
 * Caching information and settings can be found in <code>portal.properties</code>
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @see AuditPseudonymPersistence
 * @generated
 */
public class AuditPseudonymUtil {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify this class directly. Modify <code>service.xml</code> and rerun ServiceBuilder to regenerate this class.
	 */

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#cacheResult(List)
	 */
	public static void cacheResult(List<AuditPseudonym> auditPseudonyms) {
		getPersistence().cacheResult(auditPseudonyms);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#cacheResult(com.liferay.portal.kernel.model.BaseModel)
	 */
	public static void cacheResult(AuditPseudonym auditPseudonym) {
		getPersistence().cacheResult(auditPseudonym);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#clearCache()
	 */
	public static void clearCache() {
		getPersistence().clearCache();
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#clearCache(com.liferay.portal.kernel.model.BaseModel)
	 */
	public static void clearCache(AuditPseudonym auditPseudonym) {
		getPersistence().clearCache(auditPseudonym);
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
	public static Map<Serializable, AuditPseudonym> fetchByPrimaryKeys(
		Set<Serializable> primaryKeys) {

		return getPersistence().fetchByPrimaryKeys(primaryKeys);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#findWithDynamicQuery(DynamicQuery)
	 */
	public static List<AuditPseudonym> findWithDynamicQuery(
		DynamicQuery dynamicQuery) {

		return getPersistence().findWithDynamicQuery(dynamicQuery);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#findWithDynamicQuery(DynamicQuery, int, int)
	 */
	public static List<AuditPseudonym> findWithDynamicQuery(
		DynamicQuery dynamicQuery, int start, int end) {

		return getPersistence().findWithDynamicQuery(dynamicQuery, start, end);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#findWithDynamicQuery(DynamicQuery, int, int, OrderByComparator)
	 */
	public static List<AuditPseudonym> findWithDynamicQuery(
		DynamicQuery dynamicQuery, int start, int end,
		OrderByComparator<AuditPseudonym> orderByComparator) {

		return getPersistence().findWithDynamicQuery(
			dynamicQuery, start, end, orderByComparator);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#update(com.liferay.portal.kernel.model.BaseModel)
	 */
	public static AuditPseudonym update(AuditPseudonym auditPseudonym) {
		return getPersistence().update(auditPseudonym);
	}

	/**
	 * @see com.liferay.portal.kernel.service.persistence.BasePersistence#update(com.liferay.portal.kernel.model.BaseModel, ServiceContext)
	 */
	public static AuditPseudonym update(
		AuditPseudonym auditPseudonym, ServiceContext serviceContext) {

		return getPersistence().update(auditPseudonym, serviceContext);
	}

	/**
	 * Returns the audit pseudonym where companyId = &#63; and contextName = &#63; and fieldCategory = &#63; and identityValueHash = &#63; or throws a <code>NoSuchPseudonymException</code> if it could not be found.
	 *
	 * @param companyId the company ID
	 * @param contextName the context name
	 * @param fieldCategory the field category
	 * @param identityValueHash the identity value hash
	 * @return the matching audit pseudonym
	 * @throws NoSuchPseudonymException if a matching audit pseudonym could not be found
	 */
	public static AuditPseudonym findByC_CN_FC_IVH(
			long companyId, String contextName, String fieldCategory,
			String identityValueHash)
		throws com.liferay.portal.security.audit.storage.exception.
			NoSuchPseudonymException {

		return getPersistence().findByC_CN_FC_IVH(
			companyId, contextName, fieldCategory, identityValueHash);
	}

	/**
	 * Returns the audit pseudonym where companyId = &#63; and contextName = &#63; and fieldCategory = &#63; and identityValueHash = &#63; or returns <code>null</code> if it could not be found, optionally using the finder cache.
	 *
	 * @param companyId the company ID
	 * @param contextName the context name
	 * @param fieldCategory the field category
	 * @param identityValueHash the identity value hash
	 * @param useFinderCache whether to use the finder cache
	 * @return the matching audit pseudonym, or <code>null</code> if a matching audit pseudonym could not be found
	 */
	public static AuditPseudonym fetchByC_CN_FC_IVH(
		long companyId, String contextName, String fieldCategory,
		String identityValueHash, boolean useFinderCache) {

		return getPersistence().fetchByC_CN_FC_IVH(
			companyId, contextName, fieldCategory, identityValueHash,
			useFinderCache);
	}

	/**
	 * Removes the audit pseudonym where companyId = &#63; and contextName = &#63; and fieldCategory = &#63; and identityValueHash = &#63; from the database.
	 *
	 * @param companyId the company ID
	 * @param contextName the context name
	 * @param fieldCategory the field category
	 * @param identityValueHash the identity value hash
	 * @return the audit pseudonym that was removed
	 */
	public static AuditPseudonym removeByC_CN_FC_IVH(
			long companyId, String contextName, String fieldCategory,
			String identityValueHash)
		throws com.liferay.portal.security.audit.storage.exception.
			NoSuchPseudonymException {

		return getPersistence().removeByC_CN_FC_IVH(
			companyId, contextName, fieldCategory, identityValueHash);
	}

	/**
	 * Returns the number of audit pseudonyms where companyId = &#63; and contextName = &#63; and fieldCategory = &#63; and identityValueHash = &#63;.
	 *
	 * @param companyId the company ID
	 * @param contextName the context name
	 * @param fieldCategory the field category
	 * @param identityValueHash the identity value hash
	 * @return the number of matching audit pseudonyms
	 */
	public static int countByC_CN_FC_IVH(
		long companyId, String contextName, String fieldCategory,
		String identityValueHash) {

		return getPersistence().countByC_CN_FC_IVH(
			companyId, contextName, fieldCategory, identityValueHash);
	}

	/**
	 * Creates a new audit pseudonym with the primary key. Does not add the audit pseudonym to the database.
	 *
	 * @param auditPseudonymId the primary key for the new audit pseudonym
	 * @return the new audit pseudonym
	 */
	public static AuditPseudonym create(long auditPseudonymId) {
		return getPersistence().create(auditPseudonymId);
	}

	/**
	 * Removes the audit pseudonym with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param auditPseudonymId the primary key of the audit pseudonym
	 * @return the audit pseudonym that was removed
	 * @throws NoSuchPseudonymException if a audit pseudonym with the primary key could not be found
	 */
	public static AuditPseudonym remove(long auditPseudonymId)
		throws com.liferay.portal.security.audit.storage.exception.
			NoSuchPseudonymException {

		return getPersistence().remove(auditPseudonymId);
	}

	public static AuditPseudonym updateImpl(AuditPseudonym auditPseudonym) {
		return getPersistence().updateImpl(auditPseudonym);
	}

	/**
	 * Returns the audit pseudonym with the primary key or throws a <code>NoSuchPseudonymException</code> if it could not be found.
	 *
	 * @param auditPseudonymId the primary key of the audit pseudonym
	 * @return the audit pseudonym
	 * @throws NoSuchPseudonymException if a audit pseudonym with the primary key could not be found
	 */
	public static AuditPseudonym findByPrimaryKey(long auditPseudonymId)
		throws com.liferay.portal.security.audit.storage.exception.
			NoSuchPseudonymException {

		return getPersistence().findByPrimaryKey(auditPseudonymId);
	}

	/**
	 * Returns the audit pseudonym with the primary key or returns <code>null</code> if it could not be found.
	 *
	 * @param auditPseudonymId the primary key of the audit pseudonym
	 * @return the audit pseudonym, or <code>null</code> if a audit pseudonym with the primary key could not be found
	 */
	public static AuditPseudonym fetchByPrimaryKey(long auditPseudonymId) {
		return getPersistence().fetchByPrimaryKey(auditPseudonymId);
	}

	/**
	 * Returns the audit pseudonym where companyId = &#63; and contextName = &#63; and fieldCategory = &#63; and identityValueHash = &#63; or returns <code>null</code> if it could not be found. Uses the finder cache.
	 *
	 * @param companyId the company ID
	 * @param contextName the context name
	 * @param fieldCategory the field category
	 * @param identityValueHash the identity value hash
	 * @return the matching audit pseudonym, or <code>null</code> if a matching audit pseudonym could not be found
	 */
	public static AuditPseudonym fetchByC_CN_FC_IVH(
		long companyId, String contextName, String fieldCategory,
		String identityValueHash) {

		return getPersistence().fetchByC_CN_FC_IVH(
			companyId, contextName, fieldCategory, identityValueHash);
	}

	public static AuditPseudonymPersistence getPersistence() {
		return _persistence;
	}

	public static void setPersistence(AuditPseudonymPersistence persistence) {
		_persistence = persistence;
	}

	private static volatile AuditPseudonymPersistence _persistence;

}
// LIFERAY-SERVICE-BUILDER-HASH:146108336