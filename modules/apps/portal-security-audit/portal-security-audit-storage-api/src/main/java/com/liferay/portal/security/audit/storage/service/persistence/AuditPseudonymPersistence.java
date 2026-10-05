/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.storage.service.persistence;

import com.liferay.portal.kernel.service.persistence.BasePersistence;
import com.liferay.portal.security.audit.storage.exception.NoSuchPseudonymException;
import com.liferay.portal.security.audit.storage.model.AuditPseudonym;

import org.osgi.annotation.versioning.ProviderType;

/**
 * The persistence interface for the audit pseudonym service.
 *
 * <p>
 * Caching information and settings can be found in <code>portal.properties</code>
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @see AuditPseudonymUtil
 * @generated
 */
@ProviderType
public interface AuditPseudonymPersistence
	extends BasePersistence<AuditPseudonym> {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this interface directly. Always use {@link AuditPseudonymUtil} to access the audit pseudonym persistence. Modify <code>service.xml</code> and rerun ServiceBuilder to regenerate this interface.
	 */

	/**
	 * Returns the audit pseudonym where companyId = &#63; and contextName = &#63; and fieldCategory = &#63; and valueHash = &#63; or throws a <code>NoSuchPseudonymException</code> if it could not be found.
	 *
	 * @param companyId the company ID
	 * @param contextName the context name
	 * @param fieldCategory the field category
	 * @param valueHash the value hash
	 * @return the matching audit pseudonym
	 * @throws NoSuchPseudonymException if a matching audit pseudonym could not be found
	 */
	public AuditPseudonym findByC_CN_FC_VH(
			long companyId, String contextName, String fieldCategory,
			String valueHash)
		throws NoSuchPseudonymException;

	/**
	 * Returns the audit pseudonym where companyId = &#63; and contextName = &#63; and fieldCategory = &#63; and valueHash = &#63; or returns <code>null</code> if it could not be found, optionally using the finder cache.
	 *
	 * @param companyId the company ID
	 * @param contextName the context name
	 * @param fieldCategory the field category
	 * @param valueHash the value hash
	 * @param useFinderCache whether to use the finder cache
	 * @return the matching audit pseudonym, or <code>null</code> if a matching audit pseudonym could not be found
	 */
	public AuditPseudonym fetchByC_CN_FC_VH(
		long companyId, String contextName, String fieldCategory,
		String valueHash, boolean useFinderCache);

	/**
	 * Removes the audit pseudonym where companyId = &#63; and contextName = &#63; and fieldCategory = &#63; and valueHash = &#63; from the database.
	 *
	 * @param companyId the company ID
	 * @param contextName the context name
	 * @param fieldCategory the field category
	 * @param valueHash the value hash
	 * @return the audit pseudonym that was removed
	 */
	public AuditPseudonym removeByC_CN_FC_VH(
			long companyId, String contextName, String fieldCategory,
			String valueHash)
		throws NoSuchPseudonymException;

	/**
	 * Returns the number of audit pseudonyms where companyId = &#63; and contextName = &#63; and fieldCategory = &#63; and valueHash = &#63;.
	 *
	 * @param companyId the company ID
	 * @param contextName the context name
	 * @param fieldCategory the field category
	 * @param valueHash the value hash
	 * @return the number of matching audit pseudonyms
	 */
	public int countByC_CN_FC_VH(
		long companyId, String contextName, String fieldCategory,
		String valueHash);

	/**
	 * Creates a new audit pseudonym with the primary key. Does not add the audit pseudonym to the database.
	 *
	 * @param auditPseudonymId the primary key for the new audit pseudonym
	 * @return the new audit pseudonym
	 */
	public AuditPseudonym create(long auditPseudonymId);

	/**
	 * Removes the audit pseudonym with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param auditPseudonymId the primary key of the audit pseudonym
	 * @return the audit pseudonym that was removed
	 * @throws NoSuchPseudonymException if a audit pseudonym with the primary key could not be found
	 */
	public AuditPseudonym remove(long auditPseudonymId)
		throws NoSuchPseudonymException;

	public AuditPseudonym updateImpl(AuditPseudonym auditPseudonym);

	/**
	 * Returns the audit pseudonym with the primary key or throws a <code>NoSuchPseudonymException</code> if it could not be found.
	 *
	 * @param auditPseudonymId the primary key of the audit pseudonym
	 * @return the audit pseudonym
	 * @throws NoSuchPseudonymException if a audit pseudonym with the primary key could not be found
	 */
	public AuditPseudonym findByPrimaryKey(long auditPseudonymId)
		throws NoSuchPseudonymException;

	/**
	 * Returns the audit pseudonym with the primary key or returns <code>null</code> if it could not be found.
	 *
	 * @param auditPseudonymId the primary key of the audit pseudonym
	 * @return the audit pseudonym, or <code>null</code> if a audit pseudonym with the primary key could not be found
	 */
	public AuditPseudonym fetchByPrimaryKey(long auditPseudonymId);

	/**
	 * Returns the audit pseudonym where companyId = &#63; and contextName = &#63; and fieldCategory = &#63; and valueHash = &#63; or returns <code>null</code> if it could not be found. Uses the finder cache.
	 *
	 * @param companyId the company ID
	 * @param contextName the context name
	 * @param fieldCategory the field category
	 * @param valueHash the value hash
	 * @return the matching audit pseudonym, or <code>null</code> if a matching audit pseudonym could not be found
	 */
	public default AuditPseudonym fetchByC_CN_FC_VH(
		long companyId, String contextName, String fieldCategory,
		String valueHash) {

		return fetchByC_CN_FC_VH(
			companyId, contextName, fieldCategory, valueHash, true);
	}

}
// LIFERAY-SERVICE-BUILDER-HASH:1735589835