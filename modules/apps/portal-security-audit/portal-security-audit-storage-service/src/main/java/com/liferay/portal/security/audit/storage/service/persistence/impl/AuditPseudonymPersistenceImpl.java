/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.storage.service.persistence.impl;

import com.liferay.portal.kernel.configuration.Configuration;
import com.liferay.portal.kernel.dao.orm.EntityCache;
import com.liferay.portal.kernel.dao.orm.FinderCache;
import com.liferay.portal.kernel.dao.orm.Session;
import com.liferay.portal.kernel.dao.orm.SessionFactory;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.service.persistence.impl.BasePersistenceImpl;
import com.liferay.portal.kernel.service.persistence.impl.FinderColumn;
import com.liferay.portal.kernel.service.persistence.impl.UniquePersistenceFinder;
import com.liferay.portal.kernel.util.ProxyUtil;
import com.liferay.portal.security.audit.storage.exception.NoSuchPseudonymException;
import com.liferay.portal.security.audit.storage.model.AuditPseudonym;
import com.liferay.portal.security.audit.storage.model.AuditPseudonymTable;
import com.liferay.portal.security.audit.storage.model.impl.AuditPseudonymImpl;
import com.liferay.portal.security.audit.storage.model.impl.AuditPseudonymModelImpl;
import com.liferay.portal.security.audit.storage.service.persistence.AuditPseudonymPersistence;
import com.liferay.portal.security.audit.storage.service.persistence.AuditPseudonymUtil;
import com.liferay.portal.security.audit.storage.service.persistence.impl.constants.AuditPersistenceConstants;

import java.io.Serializable;

import java.lang.reflect.InvocationHandler;

import java.util.Date;
import java.util.Map;

import javax.sql.DataSource;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * The persistence implementation for the audit pseudonym service.
 *
 * <p>
 * Caching information and settings can be found in <code>portal.properties</code>
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @generated
 */
@Component(service = AuditPseudonymPersistence.class)
public class AuditPseudonymPersistenceImpl
	extends BasePersistenceImpl<AuditPseudonym, NoSuchPseudonymException>
	implements AuditPseudonymPersistence {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this class directly. Always use <code>AuditPseudonymUtil</code> to access the audit pseudonym persistence. Modify <code>service.xml</code> and rerun ServiceBuilder to regenerate this class.
	 */
	public static final String FINDER_CLASS_NAME_ENTITY =
		AuditPseudonymImpl.class.getName();

	public static final String FINDER_CLASS_NAME_LIST_WITH_PAGINATION =
		FINDER_CLASS_NAME_ENTITY + ".List1";

	public static final String FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION =
		FINDER_CLASS_NAME_ENTITY + ".List2";

	private UniquePersistenceFinder<AuditPseudonym, NoSuchPseudonymException>
		_uniquePersistenceFinderByC_CN_FC_VH;

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
	@Override
	public AuditPseudonym findByC_CN_FC_VH(
			long companyId, String contextName, String fieldCategory,
			String valueHash)
		throws NoSuchPseudonymException {

		return _uniquePersistenceFinderByC_CN_FC_VH.find(
			finderCache,
			new Object[] {companyId, contextName, fieldCategory, valueHash});
	}

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
	@Override
	public AuditPseudonym fetchByC_CN_FC_VH(
		long companyId, String contextName, String fieldCategory,
		String valueHash, boolean useFinderCache) {

		return _uniquePersistenceFinderByC_CN_FC_VH.fetch(
			finderCache,
			new Object[] {companyId, contextName, fieldCategory, valueHash},
			useFinderCache);
	}

	/**
	 * Removes the audit pseudonym where companyId = &#63; and contextName = &#63; and fieldCategory = &#63; and valueHash = &#63; from the database.
	 *
	 * @param companyId the company ID
	 * @param contextName the context name
	 * @param fieldCategory the field category
	 * @param valueHash the value hash
	 * @return the audit pseudonym that was removed
	 */
	@Override
	public AuditPseudonym removeByC_CN_FC_VH(
			long companyId, String contextName, String fieldCategory,
			String valueHash)
		throws NoSuchPseudonymException {

		AuditPseudonym auditPseudonym = findByC_CN_FC_VH(
			companyId, contextName, fieldCategory, valueHash);

		return remove(auditPseudonym);
	}

	/**
	 * Returns the number of audit pseudonyms where companyId = &#63; and contextName = &#63; and fieldCategory = &#63; and valueHash = &#63;.
	 *
	 * @param companyId the company ID
	 * @param contextName the context name
	 * @param fieldCategory the field category
	 * @param valueHash the value hash
	 * @return the number of matching audit pseudonyms
	 */
	@Override
	public int countByC_CN_FC_VH(
		long companyId, String contextName, String fieldCategory,
		String valueHash) {

		return _uniquePersistenceFinderByC_CN_FC_VH.count(
			finderCache,
			new Object[] {companyId, contextName, fieldCategory, valueHash});
	}

	public AuditPseudonymPersistenceImpl() {
		setModelClass(AuditPseudonym.class);

		setModelImplClass(AuditPseudonymImpl.class);
		setModelPKClass(long.class);

		setTable(AuditPseudonymTable.INSTANCE);
	}

	/**
	 * Creates a new audit pseudonym with the primary key. Does not add the audit pseudonym to the database.
	 *
	 * @param auditPseudonymId the primary key for the new audit pseudonym
	 * @return the new audit pseudonym
	 */
	@Override
	public AuditPseudonym create(long auditPseudonymId) {
		AuditPseudonym auditPseudonym = new AuditPseudonymImpl();

		auditPseudonym.setNew(true);
		auditPseudonym.setPrimaryKey(auditPseudonymId);

		auditPseudonym.setCompanyId(CompanyThreadLocal.getCompanyId());

		return auditPseudonym;
	}

	/**
	 * Removes the audit pseudonym with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param auditPseudonymId the primary key of the audit pseudonym
	 * @return the audit pseudonym that was removed
	 * @throws NoSuchPseudonymException if a audit pseudonym with the primary key could not be found
	 */
	@Override
	public AuditPseudonym remove(long auditPseudonymId)
		throws NoSuchPseudonymException {

		return remove((Serializable)auditPseudonymId);
	}

	@Override
	protected AuditPseudonym removeImpl(AuditPseudonym auditPseudonym) {
		Session session = null;

		try {
			session = openSession();

			if (!session.contains(auditPseudonym)) {
				auditPseudonym = (AuditPseudonym)session.get(
					AuditPseudonymImpl.class,
					auditPseudonym.getPrimaryKeyObj());
			}

			if (auditPseudonym != null) {
				session.delete(auditPseudonym);
			}
		}
		catch (Exception exception) {
			throw processException(exception);
		}
		finally {
			closeSession(session);
		}

		if (auditPseudonym != null) {
			clearCache(auditPseudonym);
		}

		return auditPseudonym;
	}

	@Override
	public AuditPseudonym updateImpl(AuditPseudonym auditPseudonym) {
		boolean isNew = auditPseudonym.isNew();

		if (!(auditPseudonym instanceof AuditPseudonymModelImpl)) {
			InvocationHandler invocationHandler = null;

			if (ProxyUtil.isProxyClass(auditPseudonym.getClass())) {
				invocationHandler = ProxyUtil.getInvocationHandler(
					auditPseudonym);

				throw new IllegalArgumentException(
					"Implement ModelWrapper in auditPseudonym proxy " +
						invocationHandler.getClass());
			}

			throw new IllegalArgumentException(
				"Implement ModelWrapper in custom AuditPseudonym implementation " +
					auditPseudonym.getClass());
		}

		AuditPseudonymModelImpl auditPseudonymModelImpl =
			(AuditPseudonymModelImpl)auditPseudonym;

		if (isNew && (auditPseudonym.getCreateDate() == null)) {
			ServiceContext serviceContext =
				ServiceContextThreadLocal.getServiceContext();

			Date date = new Date();

			if (serviceContext == null) {
				auditPseudonym.setCreateDate(date);
			}
			else {
				auditPseudonym.setCreateDate(
					serviceContext.getCreateDate(date));
			}
		}

		Session session = null;

		try {
			session = openSession();

			if (isNew) {
				session.save(auditPseudonym);
			}
			else {
				auditPseudonym = (AuditPseudonym)session.merge(auditPseudonym);
			}
		}
		catch (Exception exception) {
			throw processException(exception);
		}
		finally {
			closeSession(session);
		}

		cacheUniqueFindersResult(auditPseudonym, false);

		if (isNew) {
			auditPseudonym.setNew(false);
		}

		auditPseudonym.resetOriginalValues();

		return auditPseudonym;
	}

	/**
	 * Returns the audit pseudonym with the primary key or throws a <code>NoSuchPseudonymException</code> if it could not be found.
	 *
	 * @param auditPseudonymId the primary key of the audit pseudonym
	 * @return the audit pseudonym
	 * @throws NoSuchPseudonymException if a audit pseudonym with the primary key could not be found
	 */
	@Override
	public AuditPseudonym findByPrimaryKey(long auditPseudonymId)
		throws NoSuchPseudonymException {

		return findByPrimaryKey((Serializable)auditPseudonymId);
	}

	/**
	 * Returns the audit pseudonym with the primary key or returns <code>null</code> if it could not be found.
	 *
	 * @param auditPseudonymId the primary key of the audit pseudonym
	 * @return the audit pseudonym, or <code>null</code> if a audit pseudonym with the primary key could not be found
	 */
	@Override
	public AuditPseudonym fetchByPrimaryKey(long auditPseudonymId) {
		return fetchByPrimaryKey((Serializable)auditPseudonymId);
	}

	@Override
	protected EntityCache getEntityCache() {
		return entityCache;
	}

	@Override
	protected String getPKDBName() {
		return "auditPseudonymId";
	}

	@Override
	protected String getSelectSQL() {
		return _SQL_SELECT_AUDITPSEUDONYM;
	}

	@Override
	protected Map<String, Integer> getTableColumnsMap() {
		return AuditPseudonymModelImpl.TABLE_COLUMNS_MAP;
	}

	/**
	 * Initializes the audit pseudonym persistence.
	 */
	@Activate
	public void activate() {
		_uniquePersistenceFinderByC_CN_FC_VH = new UniquePersistenceFinder<>(
			this,
			createUniqueFinderPath(
				FINDER_CLASS_NAME_ENTITY, "fetchByC_CN_FC_VH",
				new String[] {
					Long.class.getName(), String.class.getName(),
					String.class.getName(), String.class.getName()
				},
				new String[] {
					"companyId", "contextName", "fieldCategory", "valueHash"
				},
				0, 14, false, AuditPseudonym::getCompanyId,
				convertNullFunction(AuditPseudonym::getContextName),
				convertNullFunction(AuditPseudonym::getFieldCategory),
				convertNullFunction(AuditPseudonym::getValueHash)),
			_SQL_SELECT_AUDITPSEUDONYM_WHERE, "",
			new FinderColumn<>(
				"auditPseudonym.", "companyId", FinderColumn.Type.LONG, "=",
				true, true, AuditPseudonym::getCompanyId),
			new FinderColumn<>(
				"auditPseudonym.", "contextName", FinderColumn.Type.STRING, "=",
				true, true, AuditPseudonym::getContextName),
			new FinderColumn<>(
				"auditPseudonym.", "fieldCategory", FinderColumn.Type.STRING,
				"=", true, true, AuditPseudonym::getFieldCategory),
			new FinderColumn<>(
				"auditPseudonym.", "valueHash", FinderColumn.Type.STRING, "=",
				true, true, AuditPseudonym::getValueHash));

		AuditPseudonymUtil.setPersistence(this);
	}

	@Deactivate
	public void deactivate() {
		AuditPseudonymUtil.setPersistence(null);

		entityCache.removeCache(AuditPseudonymImpl.class.getName());
	}

	@Override
	@Reference(
		target = AuditPersistenceConstants.SERVICE_CONFIGURATION_FILTER,
		unbind = "-"
	)
	public void setConfiguration(Configuration configuration) {
	}

	@Override
	@Reference(
		target = AuditPersistenceConstants.ORIGIN_BUNDLE_SYMBOLIC_NAME_FILTER,
		unbind = "-"
	)
	public void setDataSource(DataSource dataSource) {
		super.setDataSource(dataSource);
	}

	@Override
	@Reference(
		target = AuditPersistenceConstants.ORIGIN_BUNDLE_SYMBOLIC_NAME_FILTER,
		unbind = "-"
	)
	public void setSessionFactory(SessionFactory sessionFactory) {
		super.setSessionFactory(sessionFactory);
	}

	@Reference
	protected EntityCache entityCache;

	@Reference
	protected FinderCache finderCache;

	private static final String _SQL_SELECT_AUDITPSEUDONYM =
		"SELECT auditPseudonym FROM AuditPseudonym auditPseudonym";

	private static final String _SQL_SELECT_AUDITPSEUDONYM_WHERE =
		"SELECT auditPseudonym FROM AuditPseudonym auditPseudonym WHERE ";

	@Override
	protected FinderCache getFinderCache() {
		return finderCache;
	}

}
// LIFERAY-SERVICE-BUILDER-HASH:625119168