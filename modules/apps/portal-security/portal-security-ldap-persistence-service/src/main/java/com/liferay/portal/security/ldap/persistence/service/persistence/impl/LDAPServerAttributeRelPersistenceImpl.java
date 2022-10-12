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

package com.liferay.portal.security.ldap.persistence.service.persistence.impl;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.configuration.Configuration;
import com.liferay.portal.kernel.dao.orm.EntityCache;
import com.liferay.portal.kernel.dao.orm.FinderCache;
import com.liferay.portal.kernel.dao.orm.FinderPath;
import com.liferay.portal.kernel.dao.orm.Query;
import com.liferay.portal.kernel.dao.orm.QueryPos;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.dao.orm.Session;
import com.liferay.portal.kernel.dao.orm.SessionFactory;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.persistence.BasePersistence;
import com.liferay.portal.kernel.service.persistence.impl.BasePersistenceImpl;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.ProxyUtil;
import com.liferay.portal.security.ldap.persistence.exception.NoSuchServerAttributeRelException;
import com.liferay.portal.security.ldap.persistence.model.LDAPServerAttributeRel;
import com.liferay.portal.security.ldap.persistence.model.LDAPServerAttributeRelTable;
import com.liferay.portal.security.ldap.persistence.model.impl.LDAPServerAttributeRelImpl;
import com.liferay.portal.security.ldap.persistence.model.impl.LDAPServerAttributeRelModelImpl;
import com.liferay.portal.security.ldap.persistence.service.persistence.LDAPServerAttributeRelPersistence;
import com.liferay.portal.security.ldap.persistence.service.persistence.LDAPServerAttributeRelUtil;
import com.liferay.portal.security.ldap.persistence.service.persistence.impl.constants.LDAPPersistenceConstants;

import java.io.Serializable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * The persistence implementation for the ldap server attribute rel service.
 *
 * <p>
 * Caching information and settings can be found in <code>portal.properties</code>
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @generated
 */
@Component(
	service = {LDAPServerAttributeRelPersistence.class, BasePersistence.class}
)
public class LDAPServerAttributeRelPersistenceImpl
	extends BasePersistenceImpl<LDAPServerAttributeRel>
	implements LDAPServerAttributeRelPersistence {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this class directly. Always use <code>LDAPServerAttributeRelUtil</code> to access the ldap server attribute rel persistence. Modify <code>service.xml</code> and rerun ServiceBuilder to regenerate this class.
	 */
	public static final String FINDER_CLASS_NAME_ENTITY =
		LDAPServerAttributeRelImpl.class.getName();

	public static final String FINDER_CLASS_NAME_LIST_WITH_PAGINATION =
		FINDER_CLASS_NAME_ENTITY + ".List1";

	public static final String FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION =
		FINDER_CLASS_NAME_ENTITY + ".List2";

	private FinderPath _finderPathWithPaginationFindAll;
	private FinderPath _finderPathWithoutPaginationFindAll;
	private FinderPath _finderPathCountAll;
	private FinderPath _finderPathWithPaginationFindByLdapServerId;
	private FinderPath _finderPathWithoutPaginationFindByLdapServerId;
	private FinderPath _finderPathCountByLdapServerId;

	/**
	 * Returns all the ldap server attribute rels where ldapServerId = &#63;.
	 *
	 * @param ldapServerId the ldap server ID
	 * @return the matching ldap server attribute rels
	 */
	@Override
	public List<LDAPServerAttributeRel> findByLdapServerId(long ldapServerId) {
		return findByLdapServerId(
			ldapServerId, QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
	}

	/**
	 * Returns a range of all the ldap server attribute rels where ldapServerId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>LDAPServerAttributeRelModelImpl</code>.
	 * </p>
	 *
	 * @param ldapServerId the ldap server ID
	 * @param start the lower bound of the range of ldap server attribute rels
	 * @param end the upper bound of the range of ldap server attribute rels (not inclusive)
	 * @return the range of matching ldap server attribute rels
	 */
	@Override
	public List<LDAPServerAttributeRel> findByLdapServerId(
		long ldapServerId, int start, int end) {

		return findByLdapServerId(ldapServerId, start, end, null);
	}

	/**
	 * Returns an ordered range of all the ldap server attribute rels where ldapServerId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>LDAPServerAttributeRelModelImpl</code>.
	 * </p>
	 *
	 * @param ldapServerId the ldap server ID
	 * @param start the lower bound of the range of ldap server attribute rels
	 * @param end the upper bound of the range of ldap server attribute rels (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching ldap server attribute rels
	 */
	@Override
	public List<LDAPServerAttributeRel> findByLdapServerId(
		long ldapServerId, int start, int end,
		OrderByComparator<LDAPServerAttributeRel> orderByComparator) {

		return findByLdapServerId(
			ldapServerId, start, end, orderByComparator, true);
	}

	/**
	 * Returns an ordered range of all the ldap server attribute rels where ldapServerId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>LDAPServerAttributeRelModelImpl</code>.
	 * </p>
	 *
	 * @param ldapServerId the ldap server ID
	 * @param start the lower bound of the range of ldap server attribute rels
	 * @param end the upper bound of the range of ldap server attribute rels (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching ldap server attribute rels
	 */
	@Override
	public List<LDAPServerAttributeRel> findByLdapServerId(
		long ldapServerId, int start, int end,
		OrderByComparator<LDAPServerAttributeRel> orderByComparator,
		boolean useFinderCache) {

		FinderPath finderPath = null;
		Object[] finderArgs = null;

		if ((start == QueryUtil.ALL_POS) && (end == QueryUtil.ALL_POS) &&
			(orderByComparator == null)) {

			if (useFinderCache) {
				finderPath = _finderPathWithoutPaginationFindByLdapServerId;
				finderArgs = new Object[] {ldapServerId};
			}
		}
		else if (useFinderCache) {
			finderPath = _finderPathWithPaginationFindByLdapServerId;
			finderArgs = new Object[] {
				ldapServerId, start, end, orderByComparator
			};
		}

		List<LDAPServerAttributeRel> list = null;

		if (useFinderCache) {
			list = (List<LDAPServerAttributeRel>)finderCache.getResult(
				finderPath, finderArgs);

			if ((list != null) && !list.isEmpty()) {
				for (LDAPServerAttributeRel ldapServerAttributeRel : list) {
					if (ldapServerId !=
							ldapServerAttributeRel.getLdapServerId()) {

						list = null;

						break;
					}
				}
			}
		}

		if (list == null) {
			StringBundler sb = null;

			if (orderByComparator != null) {
				sb = new StringBundler(
					3 + (orderByComparator.getOrderByFields().length * 2));
			}
			else {
				sb = new StringBundler(3);
			}

			sb.append(_SQL_SELECT_LDAPSERVERATTRIBUTEREL_WHERE);

			sb.append(_FINDER_COLUMN_LDAPSERVERID_LDAPSERVERID_2);

			if (orderByComparator != null) {
				appendOrderByComparator(
					sb, _ORDER_BY_ENTITY_ALIAS, orderByComparator);
			}
			else {
				sb.append(LDAPServerAttributeRelModelImpl.ORDER_BY_JPQL);
			}

			String sql = sb.toString();

			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(sql);

				QueryPos queryPos = QueryPos.getInstance(query);

				queryPos.add(ldapServerId);

				list = (List<LDAPServerAttributeRel>)QueryUtil.list(
					query, getDialect(), start, end);

				cacheResult(list);

				if (useFinderCache) {
					finderCache.putResult(finderPath, finderArgs, list);
				}
			}
			catch (Exception exception) {
				throw processException(exception);
			}
			finally {
				closeSession(session);
			}
		}

		return list;
	}

	/**
	 * Returns the first ldap server attribute rel in the ordered set where ldapServerId = &#63;.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching ldap server attribute rel
	 * @throws NoSuchServerAttributeRelException if a matching ldap server attribute rel could not be found
	 */
	@Override
	public LDAPServerAttributeRel findByLdapServerId_First(
			long ldapServerId,
			OrderByComparator<LDAPServerAttributeRel> orderByComparator)
		throws NoSuchServerAttributeRelException {

		LDAPServerAttributeRel ldapServerAttributeRel =
			fetchByLdapServerId_First(ldapServerId, orderByComparator);

		if (ldapServerAttributeRel != null) {
			return ldapServerAttributeRel;
		}

		StringBundler sb = new StringBundler(4);

		sb.append(_NO_SUCH_ENTITY_WITH_KEY);

		sb.append("ldapServerId=");
		sb.append(ldapServerId);

		sb.append("}");

		throw new NoSuchServerAttributeRelException(sb.toString());
	}

	/**
	 * Returns the first ldap server attribute rel in the ordered set where ldapServerId = &#63;.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching ldap server attribute rel, or <code>null</code> if a matching ldap server attribute rel could not be found
	 */
	@Override
	public LDAPServerAttributeRel fetchByLdapServerId_First(
		long ldapServerId,
		OrderByComparator<LDAPServerAttributeRel> orderByComparator) {

		List<LDAPServerAttributeRel> list = findByLdapServerId(
			ldapServerId, 0, 1, orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the last ldap server attribute rel in the ordered set where ldapServerId = &#63;.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching ldap server attribute rel
	 * @throws NoSuchServerAttributeRelException if a matching ldap server attribute rel could not be found
	 */
	@Override
	public LDAPServerAttributeRel findByLdapServerId_Last(
			long ldapServerId,
			OrderByComparator<LDAPServerAttributeRel> orderByComparator)
		throws NoSuchServerAttributeRelException {

		LDAPServerAttributeRel ldapServerAttributeRel =
			fetchByLdapServerId_Last(ldapServerId, orderByComparator);

		if (ldapServerAttributeRel != null) {
			return ldapServerAttributeRel;
		}

		StringBundler sb = new StringBundler(4);

		sb.append(_NO_SUCH_ENTITY_WITH_KEY);

		sb.append("ldapServerId=");
		sb.append(ldapServerId);

		sb.append("}");

		throw new NoSuchServerAttributeRelException(sb.toString());
	}

	/**
	 * Returns the last ldap server attribute rel in the ordered set where ldapServerId = &#63;.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching ldap server attribute rel, or <code>null</code> if a matching ldap server attribute rel could not be found
	 */
	@Override
	public LDAPServerAttributeRel fetchByLdapServerId_Last(
		long ldapServerId,
		OrderByComparator<LDAPServerAttributeRel> orderByComparator) {

		int count = countByLdapServerId(ldapServerId);

		if (count == 0) {
			return null;
		}

		List<LDAPServerAttributeRel> list = findByLdapServerId(
			ldapServerId, count - 1, count, orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the ldap server attribute rels before and after the current ldap server attribute rel in the ordered set where ldapServerId = &#63;.
	 *
	 * @param ldapServerAttributeRelId the primary key of the current ldap server attribute rel
	 * @param ldapServerId the ldap server ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next ldap server attribute rel
	 * @throws NoSuchServerAttributeRelException if a ldap server attribute rel with the primary key could not be found
	 */
	@Override
	public LDAPServerAttributeRel[] findByLdapServerId_PrevAndNext(
			long ldapServerAttributeRelId, long ldapServerId,
			OrderByComparator<LDAPServerAttributeRel> orderByComparator)
		throws NoSuchServerAttributeRelException {

		LDAPServerAttributeRel ldapServerAttributeRel = findByPrimaryKey(
			ldapServerAttributeRelId);

		Session session = null;

		try {
			session = openSession();

			LDAPServerAttributeRel[] array = new LDAPServerAttributeRelImpl[3];

			array[0] = getByLdapServerId_PrevAndNext(
				session, ldapServerAttributeRel, ldapServerId,
				orderByComparator, true);

			array[1] = ldapServerAttributeRel;

			array[2] = getByLdapServerId_PrevAndNext(
				session, ldapServerAttributeRel, ldapServerId,
				orderByComparator, false);

			return array;
		}
		catch (Exception exception) {
			throw processException(exception);
		}
		finally {
			closeSession(session);
		}
	}

	protected LDAPServerAttributeRel getByLdapServerId_PrevAndNext(
		Session session, LDAPServerAttributeRel ldapServerAttributeRel,
		long ldapServerId,
		OrderByComparator<LDAPServerAttributeRel> orderByComparator,
		boolean previous) {

		StringBundler sb = null;

		if (orderByComparator != null) {
			sb = new StringBundler(
				4 + (orderByComparator.getOrderByConditionFields().length * 3) +
					(orderByComparator.getOrderByFields().length * 3));
		}
		else {
			sb = new StringBundler(3);
		}

		sb.append(_SQL_SELECT_LDAPSERVERATTRIBUTEREL_WHERE);

		sb.append(_FINDER_COLUMN_LDAPSERVERID_LDAPSERVERID_2);

		if (orderByComparator != null) {
			String[] orderByConditionFields =
				orderByComparator.getOrderByConditionFields();

			if (orderByConditionFields.length > 0) {
				sb.append(WHERE_AND);
			}

			for (int i = 0; i < orderByConditionFields.length; i++) {
				sb.append(_ORDER_BY_ENTITY_ALIAS);
				sb.append(orderByConditionFields[i]);

				if ((i + 1) < orderByConditionFields.length) {
					if (orderByComparator.isAscending() ^ previous) {
						sb.append(WHERE_GREATER_THAN_HAS_NEXT);
					}
					else {
						sb.append(WHERE_LESSER_THAN_HAS_NEXT);
					}
				}
				else {
					if (orderByComparator.isAscending() ^ previous) {
						sb.append(WHERE_GREATER_THAN);
					}
					else {
						sb.append(WHERE_LESSER_THAN);
					}
				}
			}

			sb.append(ORDER_BY_CLAUSE);

			String[] orderByFields = orderByComparator.getOrderByFields();

			for (int i = 0; i < orderByFields.length; i++) {
				sb.append(_ORDER_BY_ENTITY_ALIAS);
				sb.append(orderByFields[i]);

				if ((i + 1) < orderByFields.length) {
					if (orderByComparator.isAscending() ^ previous) {
						sb.append(ORDER_BY_ASC_HAS_NEXT);
					}
					else {
						sb.append(ORDER_BY_DESC_HAS_NEXT);
					}
				}
				else {
					if (orderByComparator.isAscending() ^ previous) {
						sb.append(ORDER_BY_ASC);
					}
					else {
						sb.append(ORDER_BY_DESC);
					}
				}
			}
		}
		else {
			sb.append(LDAPServerAttributeRelModelImpl.ORDER_BY_JPQL);
		}

		String sql = sb.toString();

		Query query = session.createQuery(sql);

		query.setFirstResult(0);
		query.setMaxResults(2);

		QueryPos queryPos = QueryPos.getInstance(query);

		queryPos.add(ldapServerId);

		if (orderByComparator != null) {
			for (Object orderByConditionValue :
					orderByComparator.getOrderByConditionValues(
						ldapServerAttributeRel)) {

				queryPos.add(orderByConditionValue);
			}
		}

		List<LDAPServerAttributeRel> list = query.list();

		if (list.size() == 2) {
			return list.get(1);
		}
		else {
			return null;
		}
	}

	/**
	 * Removes all the ldap server attribute rels where ldapServerId = &#63; from the database.
	 *
	 * @param ldapServerId the ldap server ID
	 */
	@Override
	public void removeByLdapServerId(long ldapServerId) {
		for (LDAPServerAttributeRel ldapServerAttributeRel :
				findByLdapServerId(
					ldapServerId, QueryUtil.ALL_POS, QueryUtil.ALL_POS, null)) {

			remove(ldapServerAttributeRel);
		}
	}

	/**
	 * Returns the number of ldap server attribute rels where ldapServerId = &#63;.
	 *
	 * @param ldapServerId the ldap server ID
	 * @return the number of matching ldap server attribute rels
	 */
	@Override
	public int countByLdapServerId(long ldapServerId) {
		FinderPath finderPath = _finderPathCountByLdapServerId;

		Object[] finderArgs = new Object[] {ldapServerId};

		Long count = (Long)finderCache.getResult(finderPath, finderArgs);

		if (count == null) {
			StringBundler sb = new StringBundler(2);

			sb.append(_SQL_COUNT_LDAPSERVERATTRIBUTEREL_WHERE);

			sb.append(_FINDER_COLUMN_LDAPSERVERID_LDAPSERVERID_2);

			String sql = sb.toString();

			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(sql);

				QueryPos queryPos = QueryPos.getInstance(query);

				queryPos.add(ldapServerId);

				count = (Long)query.uniqueResult();

				finderCache.putResult(finderPath, finderArgs, count);
			}
			catch (Exception exception) {
				throw processException(exception);
			}
			finally {
				closeSession(session);
			}
		}

		return count.intValue();
	}

	private static final String _FINDER_COLUMN_LDAPSERVERID_LDAPSERVERID_2 =
		"ldapServerAttributeRel.ldapServerId = ?";

	private FinderPath _finderPathWithPaginationFindByL_C;
	private FinderPath _finderPathWithoutPaginationFindByL_C;
	private FinderPath _finderPathCountByL_C;

	/**
	 * Returns all the ldap server attribute rels where ldapServerId = &#63; and classNameId = &#63;.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @return the matching ldap server attribute rels
	 */
	@Override
	public List<LDAPServerAttributeRel> findByL_C(
		long ldapServerId, long classNameId) {

		return findByL_C(
			ldapServerId, classNameId, QueryUtil.ALL_POS, QueryUtil.ALL_POS,
			null);
	}

	/**
	 * Returns a range of all the ldap server attribute rels where ldapServerId = &#63; and classNameId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>LDAPServerAttributeRelModelImpl</code>.
	 * </p>
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @param start the lower bound of the range of ldap server attribute rels
	 * @param end the upper bound of the range of ldap server attribute rels (not inclusive)
	 * @return the range of matching ldap server attribute rels
	 */
	@Override
	public List<LDAPServerAttributeRel> findByL_C(
		long ldapServerId, long classNameId, int start, int end) {

		return findByL_C(ldapServerId, classNameId, start, end, null);
	}

	/**
	 * Returns an ordered range of all the ldap server attribute rels where ldapServerId = &#63; and classNameId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>LDAPServerAttributeRelModelImpl</code>.
	 * </p>
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @param start the lower bound of the range of ldap server attribute rels
	 * @param end the upper bound of the range of ldap server attribute rels (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching ldap server attribute rels
	 */
	@Override
	public List<LDAPServerAttributeRel> findByL_C(
		long ldapServerId, long classNameId, int start, int end,
		OrderByComparator<LDAPServerAttributeRel> orderByComparator) {

		return findByL_C(
			ldapServerId, classNameId, start, end, orderByComparator, true);
	}

	/**
	 * Returns an ordered range of all the ldap server attribute rels where ldapServerId = &#63; and classNameId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>LDAPServerAttributeRelModelImpl</code>.
	 * </p>
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @param start the lower bound of the range of ldap server attribute rels
	 * @param end the upper bound of the range of ldap server attribute rels (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching ldap server attribute rels
	 */
	@Override
	public List<LDAPServerAttributeRel> findByL_C(
		long ldapServerId, long classNameId, int start, int end,
		OrderByComparator<LDAPServerAttributeRel> orderByComparator,
		boolean useFinderCache) {

		FinderPath finderPath = null;
		Object[] finderArgs = null;

		if ((start == QueryUtil.ALL_POS) && (end == QueryUtil.ALL_POS) &&
			(orderByComparator == null)) {

			if (useFinderCache) {
				finderPath = _finderPathWithoutPaginationFindByL_C;
				finderArgs = new Object[] {ldapServerId, classNameId};
			}
		}
		else if (useFinderCache) {
			finderPath = _finderPathWithPaginationFindByL_C;
			finderArgs = new Object[] {
				ldapServerId, classNameId, start, end, orderByComparator
			};
		}

		List<LDAPServerAttributeRel> list = null;

		if (useFinderCache) {
			list = (List<LDAPServerAttributeRel>)finderCache.getResult(
				finderPath, finderArgs);

			if ((list != null) && !list.isEmpty()) {
				for (LDAPServerAttributeRel ldapServerAttributeRel : list) {
					if ((ldapServerId !=
							ldapServerAttributeRel.getLdapServerId()) ||
						(classNameId !=
							ldapServerAttributeRel.getClassNameId())) {

						list = null;

						break;
					}
				}
			}
		}

		if (list == null) {
			StringBundler sb = null;

			if (orderByComparator != null) {
				sb = new StringBundler(
					4 + (orderByComparator.getOrderByFields().length * 2));
			}
			else {
				sb = new StringBundler(4);
			}

			sb.append(_SQL_SELECT_LDAPSERVERATTRIBUTEREL_WHERE);

			sb.append(_FINDER_COLUMN_L_C_LDAPSERVERID_2);

			sb.append(_FINDER_COLUMN_L_C_CLASSNAMEID_2);

			if (orderByComparator != null) {
				appendOrderByComparator(
					sb, _ORDER_BY_ENTITY_ALIAS, orderByComparator);
			}
			else {
				sb.append(LDAPServerAttributeRelModelImpl.ORDER_BY_JPQL);
			}

			String sql = sb.toString();

			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(sql);

				QueryPos queryPos = QueryPos.getInstance(query);

				queryPos.add(ldapServerId);

				queryPos.add(classNameId);

				list = (List<LDAPServerAttributeRel>)QueryUtil.list(
					query, getDialect(), start, end);

				cacheResult(list);

				if (useFinderCache) {
					finderCache.putResult(finderPath, finderArgs, list);
				}
			}
			catch (Exception exception) {
				throw processException(exception);
			}
			finally {
				closeSession(session);
			}
		}

		return list;
	}

	/**
	 * Returns the first ldap server attribute rel in the ordered set where ldapServerId = &#63; and classNameId = &#63;.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching ldap server attribute rel
	 * @throws NoSuchServerAttributeRelException if a matching ldap server attribute rel could not be found
	 */
	@Override
	public LDAPServerAttributeRel findByL_C_First(
			long ldapServerId, long classNameId,
			OrderByComparator<LDAPServerAttributeRel> orderByComparator)
		throws NoSuchServerAttributeRelException {

		LDAPServerAttributeRel ldapServerAttributeRel = fetchByL_C_First(
			ldapServerId, classNameId, orderByComparator);

		if (ldapServerAttributeRel != null) {
			return ldapServerAttributeRel;
		}

		StringBundler sb = new StringBundler(6);

		sb.append(_NO_SUCH_ENTITY_WITH_KEY);

		sb.append("ldapServerId=");
		sb.append(ldapServerId);

		sb.append(", classNameId=");
		sb.append(classNameId);

		sb.append("}");

		throw new NoSuchServerAttributeRelException(sb.toString());
	}

	/**
	 * Returns the first ldap server attribute rel in the ordered set where ldapServerId = &#63; and classNameId = &#63;.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching ldap server attribute rel, or <code>null</code> if a matching ldap server attribute rel could not be found
	 */
	@Override
	public LDAPServerAttributeRel fetchByL_C_First(
		long ldapServerId, long classNameId,
		OrderByComparator<LDAPServerAttributeRel> orderByComparator) {

		List<LDAPServerAttributeRel> list = findByL_C(
			ldapServerId, classNameId, 0, 1, orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the last ldap server attribute rel in the ordered set where ldapServerId = &#63; and classNameId = &#63;.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching ldap server attribute rel
	 * @throws NoSuchServerAttributeRelException if a matching ldap server attribute rel could not be found
	 */
	@Override
	public LDAPServerAttributeRel findByL_C_Last(
			long ldapServerId, long classNameId,
			OrderByComparator<LDAPServerAttributeRel> orderByComparator)
		throws NoSuchServerAttributeRelException {

		LDAPServerAttributeRel ldapServerAttributeRel = fetchByL_C_Last(
			ldapServerId, classNameId, orderByComparator);

		if (ldapServerAttributeRel != null) {
			return ldapServerAttributeRel;
		}

		StringBundler sb = new StringBundler(6);

		sb.append(_NO_SUCH_ENTITY_WITH_KEY);

		sb.append("ldapServerId=");
		sb.append(ldapServerId);

		sb.append(", classNameId=");
		sb.append(classNameId);

		sb.append("}");

		throw new NoSuchServerAttributeRelException(sb.toString());
	}

	/**
	 * Returns the last ldap server attribute rel in the ordered set where ldapServerId = &#63; and classNameId = &#63;.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching ldap server attribute rel, or <code>null</code> if a matching ldap server attribute rel could not be found
	 */
	@Override
	public LDAPServerAttributeRel fetchByL_C_Last(
		long ldapServerId, long classNameId,
		OrderByComparator<LDAPServerAttributeRel> orderByComparator) {

		int count = countByL_C(ldapServerId, classNameId);

		if (count == 0) {
			return null;
		}

		List<LDAPServerAttributeRel> list = findByL_C(
			ldapServerId, classNameId, count - 1, count, orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the ldap server attribute rels before and after the current ldap server attribute rel in the ordered set where ldapServerId = &#63; and classNameId = &#63;.
	 *
	 * @param ldapServerAttributeRelId the primary key of the current ldap server attribute rel
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next ldap server attribute rel
	 * @throws NoSuchServerAttributeRelException if a ldap server attribute rel with the primary key could not be found
	 */
	@Override
	public LDAPServerAttributeRel[] findByL_C_PrevAndNext(
			long ldapServerAttributeRelId, long ldapServerId, long classNameId,
			OrderByComparator<LDAPServerAttributeRel> orderByComparator)
		throws NoSuchServerAttributeRelException {

		LDAPServerAttributeRel ldapServerAttributeRel = findByPrimaryKey(
			ldapServerAttributeRelId);

		Session session = null;

		try {
			session = openSession();

			LDAPServerAttributeRel[] array = new LDAPServerAttributeRelImpl[3];

			array[0] = getByL_C_PrevAndNext(
				session, ldapServerAttributeRel, ldapServerId, classNameId,
				orderByComparator, true);

			array[1] = ldapServerAttributeRel;

			array[2] = getByL_C_PrevAndNext(
				session, ldapServerAttributeRel, ldapServerId, classNameId,
				orderByComparator, false);

			return array;
		}
		catch (Exception exception) {
			throw processException(exception);
		}
		finally {
			closeSession(session);
		}
	}

	protected LDAPServerAttributeRel getByL_C_PrevAndNext(
		Session session, LDAPServerAttributeRel ldapServerAttributeRel,
		long ldapServerId, long classNameId,
		OrderByComparator<LDAPServerAttributeRel> orderByComparator,
		boolean previous) {

		StringBundler sb = null;

		if (orderByComparator != null) {
			sb = new StringBundler(
				5 + (orderByComparator.getOrderByConditionFields().length * 3) +
					(orderByComparator.getOrderByFields().length * 3));
		}
		else {
			sb = new StringBundler(4);
		}

		sb.append(_SQL_SELECT_LDAPSERVERATTRIBUTEREL_WHERE);

		sb.append(_FINDER_COLUMN_L_C_LDAPSERVERID_2);

		sb.append(_FINDER_COLUMN_L_C_CLASSNAMEID_2);

		if (orderByComparator != null) {
			String[] orderByConditionFields =
				orderByComparator.getOrderByConditionFields();

			if (orderByConditionFields.length > 0) {
				sb.append(WHERE_AND);
			}

			for (int i = 0; i < orderByConditionFields.length; i++) {
				sb.append(_ORDER_BY_ENTITY_ALIAS);
				sb.append(orderByConditionFields[i]);

				if ((i + 1) < orderByConditionFields.length) {
					if (orderByComparator.isAscending() ^ previous) {
						sb.append(WHERE_GREATER_THAN_HAS_NEXT);
					}
					else {
						sb.append(WHERE_LESSER_THAN_HAS_NEXT);
					}
				}
				else {
					if (orderByComparator.isAscending() ^ previous) {
						sb.append(WHERE_GREATER_THAN);
					}
					else {
						sb.append(WHERE_LESSER_THAN);
					}
				}
			}

			sb.append(ORDER_BY_CLAUSE);

			String[] orderByFields = orderByComparator.getOrderByFields();

			for (int i = 0; i < orderByFields.length; i++) {
				sb.append(_ORDER_BY_ENTITY_ALIAS);
				sb.append(orderByFields[i]);

				if ((i + 1) < orderByFields.length) {
					if (orderByComparator.isAscending() ^ previous) {
						sb.append(ORDER_BY_ASC_HAS_NEXT);
					}
					else {
						sb.append(ORDER_BY_DESC_HAS_NEXT);
					}
				}
				else {
					if (orderByComparator.isAscending() ^ previous) {
						sb.append(ORDER_BY_ASC);
					}
					else {
						sb.append(ORDER_BY_DESC);
					}
				}
			}
		}
		else {
			sb.append(LDAPServerAttributeRelModelImpl.ORDER_BY_JPQL);
		}

		String sql = sb.toString();

		Query query = session.createQuery(sql);

		query.setFirstResult(0);
		query.setMaxResults(2);

		QueryPos queryPos = QueryPos.getInstance(query);

		queryPos.add(ldapServerId);

		queryPos.add(classNameId);

		if (orderByComparator != null) {
			for (Object orderByConditionValue :
					orderByComparator.getOrderByConditionValues(
						ldapServerAttributeRel)) {

				queryPos.add(orderByConditionValue);
			}
		}

		List<LDAPServerAttributeRel> list = query.list();

		if (list.size() == 2) {
			return list.get(1);
		}
		else {
			return null;
		}
	}

	/**
	 * Removes all the ldap server attribute rels where ldapServerId = &#63; and classNameId = &#63; from the database.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 */
	@Override
	public void removeByL_C(long ldapServerId, long classNameId) {
		for (LDAPServerAttributeRel ldapServerAttributeRel :
				findByL_C(
					ldapServerId, classNameId, QueryUtil.ALL_POS,
					QueryUtil.ALL_POS, null)) {

			remove(ldapServerAttributeRel);
		}
	}

	/**
	 * Returns the number of ldap server attribute rels where ldapServerId = &#63; and classNameId = &#63;.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @return the number of matching ldap server attribute rels
	 */
	@Override
	public int countByL_C(long ldapServerId, long classNameId) {
		FinderPath finderPath = _finderPathCountByL_C;

		Object[] finderArgs = new Object[] {ldapServerId, classNameId};

		Long count = (Long)finderCache.getResult(finderPath, finderArgs);

		if (count == null) {
			StringBundler sb = new StringBundler(3);

			sb.append(_SQL_COUNT_LDAPSERVERATTRIBUTEREL_WHERE);

			sb.append(_FINDER_COLUMN_L_C_LDAPSERVERID_2);

			sb.append(_FINDER_COLUMN_L_C_CLASSNAMEID_2);

			String sql = sb.toString();

			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(sql);

				QueryPos queryPos = QueryPos.getInstance(query);

				queryPos.add(ldapServerId);

				queryPos.add(classNameId);

				count = (Long)query.uniqueResult();

				finderCache.putResult(finderPath, finderArgs, count);
			}
			catch (Exception exception) {
				throw processException(exception);
			}
			finally {
				closeSession(session);
			}
		}

		return count.intValue();
	}

	private static final String _FINDER_COLUMN_L_C_LDAPSERVERID_2 =
		"ldapServerAttributeRel.ldapServerId = ? AND ";

	private static final String _FINDER_COLUMN_L_C_CLASSNAMEID_2 =
		"ldapServerAttributeRel.classNameId = ?";

	private FinderPath _finderPathWithPaginationFindByC_C;
	private FinderPath _finderPathWithoutPaginationFindByC_C;
	private FinderPath _finderPathCountByC_C;

	/**
	 * Returns all the ldap server attribute rels where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the matching ldap server attribute rels
	 */
	@Override
	public List<LDAPServerAttributeRel> findByC_C(
		long classNameId, long classPK) {

		return findByC_C(
			classNameId, classPK, QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
	}

	/**
	 * Returns a range of all the ldap server attribute rels where classNameId = &#63; and classPK = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>LDAPServerAttributeRelModelImpl</code>.
	 * </p>
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param start the lower bound of the range of ldap server attribute rels
	 * @param end the upper bound of the range of ldap server attribute rels (not inclusive)
	 * @return the range of matching ldap server attribute rels
	 */
	@Override
	public List<LDAPServerAttributeRel> findByC_C(
		long classNameId, long classPK, int start, int end) {

		return findByC_C(classNameId, classPK, start, end, null);
	}

	/**
	 * Returns an ordered range of all the ldap server attribute rels where classNameId = &#63; and classPK = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>LDAPServerAttributeRelModelImpl</code>.
	 * </p>
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param start the lower bound of the range of ldap server attribute rels
	 * @param end the upper bound of the range of ldap server attribute rels (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching ldap server attribute rels
	 */
	@Override
	public List<LDAPServerAttributeRel> findByC_C(
		long classNameId, long classPK, int start, int end,
		OrderByComparator<LDAPServerAttributeRel> orderByComparator) {

		return findByC_C(
			classNameId, classPK, start, end, orderByComparator, true);
	}

	/**
	 * Returns an ordered range of all the ldap server attribute rels where classNameId = &#63; and classPK = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>LDAPServerAttributeRelModelImpl</code>.
	 * </p>
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param start the lower bound of the range of ldap server attribute rels
	 * @param end the upper bound of the range of ldap server attribute rels (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching ldap server attribute rels
	 */
	@Override
	public List<LDAPServerAttributeRel> findByC_C(
		long classNameId, long classPK, int start, int end,
		OrderByComparator<LDAPServerAttributeRel> orderByComparator,
		boolean useFinderCache) {

		FinderPath finderPath = null;
		Object[] finderArgs = null;

		if ((start == QueryUtil.ALL_POS) && (end == QueryUtil.ALL_POS) &&
			(orderByComparator == null)) {

			if (useFinderCache) {
				finderPath = _finderPathWithoutPaginationFindByC_C;
				finderArgs = new Object[] {classNameId, classPK};
			}
		}
		else if (useFinderCache) {
			finderPath = _finderPathWithPaginationFindByC_C;
			finderArgs = new Object[] {
				classNameId, classPK, start, end, orderByComparator
			};
		}

		List<LDAPServerAttributeRel> list = null;

		if (useFinderCache) {
			list = (List<LDAPServerAttributeRel>)finderCache.getResult(
				finderPath, finderArgs);

			if ((list != null) && !list.isEmpty()) {
				for (LDAPServerAttributeRel ldapServerAttributeRel : list) {
					if ((classNameId !=
							ldapServerAttributeRel.getClassNameId()) ||
						(classPK != ldapServerAttributeRel.getClassPK())) {

						list = null;

						break;
					}
				}
			}
		}

		if (list == null) {
			StringBundler sb = null;

			if (orderByComparator != null) {
				sb = new StringBundler(
					4 + (orderByComparator.getOrderByFields().length * 2));
			}
			else {
				sb = new StringBundler(4);
			}

			sb.append(_SQL_SELECT_LDAPSERVERATTRIBUTEREL_WHERE);

			sb.append(_FINDER_COLUMN_C_C_CLASSNAMEID_2);

			sb.append(_FINDER_COLUMN_C_C_CLASSPK_2);

			if (orderByComparator != null) {
				appendOrderByComparator(
					sb, _ORDER_BY_ENTITY_ALIAS, orderByComparator);
			}
			else {
				sb.append(LDAPServerAttributeRelModelImpl.ORDER_BY_JPQL);
			}

			String sql = sb.toString();

			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(sql);

				QueryPos queryPos = QueryPos.getInstance(query);

				queryPos.add(classNameId);

				queryPos.add(classPK);

				list = (List<LDAPServerAttributeRel>)QueryUtil.list(
					query, getDialect(), start, end);

				cacheResult(list);

				if (useFinderCache) {
					finderCache.putResult(finderPath, finderArgs, list);
				}
			}
			catch (Exception exception) {
				throw processException(exception);
			}
			finally {
				closeSession(session);
			}
		}

		return list;
	}

	/**
	 * Returns the first ldap server attribute rel in the ordered set where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching ldap server attribute rel
	 * @throws NoSuchServerAttributeRelException if a matching ldap server attribute rel could not be found
	 */
	@Override
	public LDAPServerAttributeRel findByC_C_First(
			long classNameId, long classPK,
			OrderByComparator<LDAPServerAttributeRel> orderByComparator)
		throws NoSuchServerAttributeRelException {

		LDAPServerAttributeRel ldapServerAttributeRel = fetchByC_C_First(
			classNameId, classPK, orderByComparator);

		if (ldapServerAttributeRel != null) {
			return ldapServerAttributeRel;
		}

		StringBundler sb = new StringBundler(6);

		sb.append(_NO_SUCH_ENTITY_WITH_KEY);

		sb.append("classNameId=");
		sb.append(classNameId);

		sb.append(", classPK=");
		sb.append(classPK);

		sb.append("}");

		throw new NoSuchServerAttributeRelException(sb.toString());
	}

	/**
	 * Returns the first ldap server attribute rel in the ordered set where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching ldap server attribute rel, or <code>null</code> if a matching ldap server attribute rel could not be found
	 */
	@Override
	public LDAPServerAttributeRel fetchByC_C_First(
		long classNameId, long classPK,
		OrderByComparator<LDAPServerAttributeRel> orderByComparator) {

		List<LDAPServerAttributeRel> list = findByC_C(
			classNameId, classPK, 0, 1, orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the last ldap server attribute rel in the ordered set where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching ldap server attribute rel
	 * @throws NoSuchServerAttributeRelException if a matching ldap server attribute rel could not be found
	 */
	@Override
	public LDAPServerAttributeRel findByC_C_Last(
			long classNameId, long classPK,
			OrderByComparator<LDAPServerAttributeRel> orderByComparator)
		throws NoSuchServerAttributeRelException {

		LDAPServerAttributeRel ldapServerAttributeRel = fetchByC_C_Last(
			classNameId, classPK, orderByComparator);

		if (ldapServerAttributeRel != null) {
			return ldapServerAttributeRel;
		}

		StringBundler sb = new StringBundler(6);

		sb.append(_NO_SUCH_ENTITY_WITH_KEY);

		sb.append("classNameId=");
		sb.append(classNameId);

		sb.append(", classPK=");
		sb.append(classPK);

		sb.append("}");

		throw new NoSuchServerAttributeRelException(sb.toString());
	}

	/**
	 * Returns the last ldap server attribute rel in the ordered set where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching ldap server attribute rel, or <code>null</code> if a matching ldap server attribute rel could not be found
	 */
	@Override
	public LDAPServerAttributeRel fetchByC_C_Last(
		long classNameId, long classPK,
		OrderByComparator<LDAPServerAttributeRel> orderByComparator) {

		int count = countByC_C(classNameId, classPK);

		if (count == 0) {
			return null;
		}

		List<LDAPServerAttributeRel> list = findByC_C(
			classNameId, classPK, count - 1, count, orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the ldap server attribute rels before and after the current ldap server attribute rel in the ordered set where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param ldapServerAttributeRelId the primary key of the current ldap server attribute rel
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next ldap server attribute rel
	 * @throws NoSuchServerAttributeRelException if a ldap server attribute rel with the primary key could not be found
	 */
	@Override
	public LDAPServerAttributeRel[] findByC_C_PrevAndNext(
			long ldapServerAttributeRelId, long classNameId, long classPK,
			OrderByComparator<LDAPServerAttributeRel> orderByComparator)
		throws NoSuchServerAttributeRelException {

		LDAPServerAttributeRel ldapServerAttributeRel = findByPrimaryKey(
			ldapServerAttributeRelId);

		Session session = null;

		try {
			session = openSession();

			LDAPServerAttributeRel[] array = new LDAPServerAttributeRelImpl[3];

			array[0] = getByC_C_PrevAndNext(
				session, ldapServerAttributeRel, classNameId, classPK,
				orderByComparator, true);

			array[1] = ldapServerAttributeRel;

			array[2] = getByC_C_PrevAndNext(
				session, ldapServerAttributeRel, classNameId, classPK,
				orderByComparator, false);

			return array;
		}
		catch (Exception exception) {
			throw processException(exception);
		}
		finally {
			closeSession(session);
		}
	}

	protected LDAPServerAttributeRel getByC_C_PrevAndNext(
		Session session, LDAPServerAttributeRel ldapServerAttributeRel,
		long classNameId, long classPK,
		OrderByComparator<LDAPServerAttributeRel> orderByComparator,
		boolean previous) {

		StringBundler sb = null;

		if (orderByComparator != null) {
			sb = new StringBundler(
				5 + (orderByComparator.getOrderByConditionFields().length * 3) +
					(orderByComparator.getOrderByFields().length * 3));
		}
		else {
			sb = new StringBundler(4);
		}

		sb.append(_SQL_SELECT_LDAPSERVERATTRIBUTEREL_WHERE);

		sb.append(_FINDER_COLUMN_C_C_CLASSNAMEID_2);

		sb.append(_FINDER_COLUMN_C_C_CLASSPK_2);

		if (orderByComparator != null) {
			String[] orderByConditionFields =
				orderByComparator.getOrderByConditionFields();

			if (orderByConditionFields.length > 0) {
				sb.append(WHERE_AND);
			}

			for (int i = 0; i < orderByConditionFields.length; i++) {
				sb.append(_ORDER_BY_ENTITY_ALIAS);
				sb.append(orderByConditionFields[i]);

				if ((i + 1) < orderByConditionFields.length) {
					if (orderByComparator.isAscending() ^ previous) {
						sb.append(WHERE_GREATER_THAN_HAS_NEXT);
					}
					else {
						sb.append(WHERE_LESSER_THAN_HAS_NEXT);
					}
				}
				else {
					if (orderByComparator.isAscending() ^ previous) {
						sb.append(WHERE_GREATER_THAN);
					}
					else {
						sb.append(WHERE_LESSER_THAN);
					}
				}
			}

			sb.append(ORDER_BY_CLAUSE);

			String[] orderByFields = orderByComparator.getOrderByFields();

			for (int i = 0; i < orderByFields.length; i++) {
				sb.append(_ORDER_BY_ENTITY_ALIAS);
				sb.append(orderByFields[i]);

				if ((i + 1) < orderByFields.length) {
					if (orderByComparator.isAscending() ^ previous) {
						sb.append(ORDER_BY_ASC_HAS_NEXT);
					}
					else {
						sb.append(ORDER_BY_DESC_HAS_NEXT);
					}
				}
				else {
					if (orderByComparator.isAscending() ^ previous) {
						sb.append(ORDER_BY_ASC);
					}
					else {
						sb.append(ORDER_BY_DESC);
					}
				}
			}
		}
		else {
			sb.append(LDAPServerAttributeRelModelImpl.ORDER_BY_JPQL);
		}

		String sql = sb.toString();

		Query query = session.createQuery(sql);

		query.setFirstResult(0);
		query.setMaxResults(2);

		QueryPos queryPos = QueryPos.getInstance(query);

		queryPos.add(classNameId);

		queryPos.add(classPK);

		if (orderByComparator != null) {
			for (Object orderByConditionValue :
					orderByComparator.getOrderByConditionValues(
						ldapServerAttributeRel)) {

				queryPos.add(orderByConditionValue);
			}
		}

		List<LDAPServerAttributeRel> list = query.list();

		if (list.size() == 2) {
			return list.get(1);
		}
		else {
			return null;
		}
	}

	/**
	 * Removes all the ldap server attribute rels where classNameId = &#63; and classPK = &#63; from the database.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 */
	@Override
	public void removeByC_C(long classNameId, long classPK) {
		for (LDAPServerAttributeRel ldapServerAttributeRel :
				findByC_C(
					classNameId, classPK, QueryUtil.ALL_POS, QueryUtil.ALL_POS,
					null)) {

			remove(ldapServerAttributeRel);
		}
	}

	/**
	 * Returns the number of ldap server attribute rels where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the number of matching ldap server attribute rels
	 */
	@Override
	public int countByC_C(long classNameId, long classPK) {
		FinderPath finderPath = _finderPathCountByC_C;

		Object[] finderArgs = new Object[] {classNameId, classPK};

		Long count = (Long)finderCache.getResult(finderPath, finderArgs);

		if (count == null) {
			StringBundler sb = new StringBundler(3);

			sb.append(_SQL_COUNT_LDAPSERVERATTRIBUTEREL_WHERE);

			sb.append(_FINDER_COLUMN_C_C_CLASSNAMEID_2);

			sb.append(_FINDER_COLUMN_C_C_CLASSPK_2);

			String sql = sb.toString();

			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(sql);

				QueryPos queryPos = QueryPos.getInstance(query);

				queryPos.add(classNameId);

				queryPos.add(classPK);

				count = (Long)query.uniqueResult();

				finderCache.putResult(finderPath, finderArgs, count);
			}
			catch (Exception exception) {
				throw processException(exception);
			}
			finally {
				closeSession(session);
			}
		}

		return count.intValue();
	}

	private static final String _FINDER_COLUMN_C_C_CLASSNAMEID_2 =
		"ldapServerAttributeRel.classNameId = ? AND ";

	private static final String _FINDER_COLUMN_C_C_CLASSPK_2 =
		"ldapServerAttributeRel.classPK = ?";

	private FinderPath _finderPathFetchByL_C_C;
	private FinderPath _finderPathCountByL_C_C;

	/**
	 * Returns the ldap server attribute rel where ldapServerId = &#63; and classNameId = &#63; and classPK = &#63; or throws a <code>NoSuchServerAttributeRelException</code> if it could not be found.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the matching ldap server attribute rel
	 * @throws NoSuchServerAttributeRelException if a matching ldap server attribute rel could not be found
	 */
	@Override
	public LDAPServerAttributeRel findByL_C_C(
			long ldapServerId, long classNameId, long classPK)
		throws NoSuchServerAttributeRelException {

		LDAPServerAttributeRel ldapServerAttributeRel = fetchByL_C_C(
			ldapServerId, classNameId, classPK);

		if (ldapServerAttributeRel == null) {
			StringBundler sb = new StringBundler(8);

			sb.append(_NO_SUCH_ENTITY_WITH_KEY);

			sb.append("ldapServerId=");
			sb.append(ldapServerId);

			sb.append(", classNameId=");
			sb.append(classNameId);

			sb.append(", classPK=");
			sb.append(classPK);

			sb.append("}");

			if (_log.isDebugEnabled()) {
				_log.debug(sb.toString());
			}

			throw new NoSuchServerAttributeRelException(sb.toString());
		}

		return ldapServerAttributeRel;
	}

	/**
	 * Returns the ldap server attribute rel where ldapServerId = &#63; and classNameId = &#63; and classPK = &#63; or returns <code>null</code> if it could not be found. Uses the finder cache.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the matching ldap server attribute rel, or <code>null</code> if a matching ldap server attribute rel could not be found
	 */
	@Override
	public LDAPServerAttributeRel fetchByL_C_C(
		long ldapServerId, long classNameId, long classPK) {

		return fetchByL_C_C(ldapServerId, classNameId, classPK, true);
	}

	/**
	 * Returns the ldap server attribute rel where ldapServerId = &#63; and classNameId = &#63; and classPK = &#63; or returns <code>null</code> if it could not be found, optionally using the finder cache.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param useFinderCache whether to use the finder cache
	 * @return the matching ldap server attribute rel, or <code>null</code> if a matching ldap server attribute rel could not be found
	 */
	@Override
	public LDAPServerAttributeRel fetchByL_C_C(
		long ldapServerId, long classNameId, long classPK,
		boolean useFinderCache) {

		Object[] finderArgs = null;

		if (useFinderCache) {
			finderArgs = new Object[] {ldapServerId, classNameId, classPK};
		}

		Object result = null;

		if (useFinderCache) {
			result = finderCache.getResult(_finderPathFetchByL_C_C, finderArgs);
		}

		if (result instanceof LDAPServerAttributeRel) {
			LDAPServerAttributeRel ldapServerAttributeRel =
				(LDAPServerAttributeRel)result;

			if ((ldapServerId != ldapServerAttributeRel.getLdapServerId()) ||
				(classNameId != ldapServerAttributeRel.getClassNameId()) ||
				(classPK != ldapServerAttributeRel.getClassPK())) {

				result = null;
			}
		}

		if (result == null) {
			StringBundler sb = new StringBundler(5);

			sb.append(_SQL_SELECT_LDAPSERVERATTRIBUTEREL_WHERE);

			sb.append(_FINDER_COLUMN_L_C_C_LDAPSERVERID_2);

			sb.append(_FINDER_COLUMN_L_C_C_CLASSNAMEID_2);

			sb.append(_FINDER_COLUMN_L_C_C_CLASSPK_2);

			String sql = sb.toString();

			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(sql);

				QueryPos queryPos = QueryPos.getInstance(query);

				queryPos.add(ldapServerId);

				queryPos.add(classNameId);

				queryPos.add(classPK);

				List<LDAPServerAttributeRel> list = query.list();

				if (list.isEmpty()) {
					if (useFinderCache) {
						finderCache.putResult(
							_finderPathFetchByL_C_C, finderArgs, list);
					}
				}
				else {
					LDAPServerAttributeRel ldapServerAttributeRel = list.get(0);

					result = ldapServerAttributeRel;

					cacheResult(ldapServerAttributeRel);
				}
			}
			catch (Exception exception) {
				throw processException(exception);
			}
			finally {
				closeSession(session);
			}
		}

		if (result instanceof List<?>) {
			return null;
		}
		else {
			return (LDAPServerAttributeRel)result;
		}
	}

	/**
	 * Removes the ldap server attribute rel where ldapServerId = &#63; and classNameId = &#63; and classPK = &#63; from the database.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the ldap server attribute rel that was removed
	 */
	@Override
	public LDAPServerAttributeRel removeByL_C_C(
			long ldapServerId, long classNameId, long classPK)
		throws NoSuchServerAttributeRelException {

		LDAPServerAttributeRel ldapServerAttributeRel = findByL_C_C(
			ldapServerId, classNameId, classPK);

		return remove(ldapServerAttributeRel);
	}

	/**
	 * Returns the number of ldap server attribute rels where ldapServerId = &#63; and classNameId = &#63; and classPK = &#63;.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the number of matching ldap server attribute rels
	 */
	@Override
	public int countByL_C_C(long ldapServerId, long classNameId, long classPK) {
		FinderPath finderPath = _finderPathCountByL_C_C;

		Object[] finderArgs = new Object[] {ldapServerId, classNameId, classPK};

		Long count = (Long)finderCache.getResult(finderPath, finderArgs);

		if (count == null) {
			StringBundler sb = new StringBundler(4);

			sb.append(_SQL_COUNT_LDAPSERVERATTRIBUTEREL_WHERE);

			sb.append(_FINDER_COLUMN_L_C_C_LDAPSERVERID_2);

			sb.append(_FINDER_COLUMN_L_C_C_CLASSNAMEID_2);

			sb.append(_FINDER_COLUMN_L_C_C_CLASSPK_2);

			String sql = sb.toString();

			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(sql);

				QueryPos queryPos = QueryPos.getInstance(query);

				queryPos.add(ldapServerId);

				queryPos.add(classNameId);

				queryPos.add(classPK);

				count = (Long)query.uniqueResult();

				finderCache.putResult(finderPath, finderArgs, count);
			}
			catch (Exception exception) {
				throw processException(exception);
			}
			finally {
				closeSession(session);
			}
		}

		return count.intValue();
	}

	private static final String _FINDER_COLUMN_L_C_C_LDAPSERVERID_2 =
		"ldapServerAttributeRel.ldapServerId = ? AND ";

	private static final String _FINDER_COLUMN_L_C_C_CLASSNAMEID_2 =
		"ldapServerAttributeRel.classNameId = ? AND ";

	private static final String _FINDER_COLUMN_L_C_C_CLASSPK_2 =
		"ldapServerAttributeRel.classPK = ?";

	public LDAPServerAttributeRelPersistenceImpl() {
		setModelClass(LDAPServerAttributeRel.class);

		setModelImplClass(LDAPServerAttributeRelImpl.class);
		setModelPKClass(long.class);

		setTable(LDAPServerAttributeRelTable.INSTANCE);
	}

	/**
	 * Caches the ldap server attribute rel in the entity cache if it is enabled.
	 *
	 * @param ldapServerAttributeRel the ldap server attribute rel
	 */
	@Override
	public void cacheResult(LDAPServerAttributeRel ldapServerAttributeRel) {
		entityCache.putResult(
			LDAPServerAttributeRelImpl.class,
			ldapServerAttributeRel.getPrimaryKey(), ldapServerAttributeRel);

		finderCache.putResult(
			_finderPathFetchByL_C_C,
			new Object[] {
				ldapServerAttributeRel.getLdapServerId(),
				ldapServerAttributeRel.getClassNameId(),
				ldapServerAttributeRel.getClassPK()
			},
			ldapServerAttributeRel);
	}

	private int _valueObjectFinderCacheListThreshold;

	/**
	 * Caches the ldap server attribute rels in the entity cache if it is enabled.
	 *
	 * @param ldapServerAttributeRels the ldap server attribute rels
	 */
	@Override
	public void cacheResult(
		List<LDAPServerAttributeRel> ldapServerAttributeRels) {

		if ((_valueObjectFinderCacheListThreshold == 0) ||
			((_valueObjectFinderCacheListThreshold > 0) &&
			 (ldapServerAttributeRels.size() >
				 _valueObjectFinderCacheListThreshold))) {

			return;
		}

		for (LDAPServerAttributeRel ldapServerAttributeRel :
				ldapServerAttributeRels) {

			if (entityCache.getResult(
					LDAPServerAttributeRelImpl.class,
					ldapServerAttributeRel.getPrimaryKey()) == null) {

				cacheResult(ldapServerAttributeRel);
			}
		}
	}

	/**
	 * Clears the cache for all ldap server attribute rels.
	 *
	 * <p>
	 * The <code>EntityCache</code> and <code>FinderCache</code> are both cleared by this method.
	 * </p>
	 */
	@Override
	public void clearCache() {
		entityCache.clearCache(LDAPServerAttributeRelImpl.class);

		finderCache.clearCache(LDAPServerAttributeRelImpl.class);
	}

	/**
	 * Clears the cache for the ldap server attribute rel.
	 *
	 * <p>
	 * The <code>EntityCache</code> and <code>FinderCache</code> are both cleared by this method.
	 * </p>
	 */
	@Override
	public void clearCache(LDAPServerAttributeRel ldapServerAttributeRel) {
		entityCache.removeResult(
			LDAPServerAttributeRelImpl.class, ldapServerAttributeRel);
	}

	@Override
	public void clearCache(
		List<LDAPServerAttributeRel> ldapServerAttributeRels) {

		for (LDAPServerAttributeRel ldapServerAttributeRel :
				ldapServerAttributeRels) {

			entityCache.removeResult(
				LDAPServerAttributeRelImpl.class, ldapServerAttributeRel);
		}
	}

	@Override
	public void clearCache(Set<Serializable> primaryKeys) {
		finderCache.clearCache(LDAPServerAttributeRelImpl.class);

		for (Serializable primaryKey : primaryKeys) {
			entityCache.removeResult(
				LDAPServerAttributeRelImpl.class, primaryKey);
		}
	}

	protected void cacheUniqueFindersCache(
		LDAPServerAttributeRelModelImpl ldapServerAttributeRelModelImpl) {

		Object[] args = new Object[] {
			ldapServerAttributeRelModelImpl.getLdapServerId(),
			ldapServerAttributeRelModelImpl.getClassNameId(),
			ldapServerAttributeRelModelImpl.getClassPK()
		};

		finderCache.putResult(_finderPathCountByL_C_C, args, Long.valueOf(1));
		finderCache.putResult(
			_finderPathFetchByL_C_C, args, ldapServerAttributeRelModelImpl);
	}

	/**
	 * Creates a new ldap server attribute rel with the primary key. Does not add the ldap server attribute rel to the database.
	 *
	 * @param ldapServerAttributeRelId the primary key for the new ldap server attribute rel
	 * @return the new ldap server attribute rel
	 */
	@Override
	public LDAPServerAttributeRel create(long ldapServerAttributeRelId) {
		LDAPServerAttributeRel ldapServerAttributeRel =
			new LDAPServerAttributeRelImpl();

		ldapServerAttributeRel.setNew(true);
		ldapServerAttributeRel.setPrimaryKey(ldapServerAttributeRelId);

		ldapServerAttributeRel.setCompanyId(CompanyThreadLocal.getCompanyId());

		return ldapServerAttributeRel;
	}

	/**
	 * Removes the ldap server attribute rel with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param ldapServerAttributeRelId the primary key of the ldap server attribute rel
	 * @return the ldap server attribute rel that was removed
	 * @throws NoSuchServerAttributeRelException if a ldap server attribute rel with the primary key could not be found
	 */
	@Override
	public LDAPServerAttributeRel remove(long ldapServerAttributeRelId)
		throws NoSuchServerAttributeRelException {

		return remove((Serializable)ldapServerAttributeRelId);
	}

	/**
	 * Removes the ldap server attribute rel with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param primaryKey the primary key of the ldap server attribute rel
	 * @return the ldap server attribute rel that was removed
	 * @throws NoSuchServerAttributeRelException if a ldap server attribute rel with the primary key could not be found
	 */
	@Override
	public LDAPServerAttributeRel remove(Serializable primaryKey)
		throws NoSuchServerAttributeRelException {

		Session session = null;

		try {
			session = openSession();

			LDAPServerAttributeRel ldapServerAttributeRel =
				(LDAPServerAttributeRel)session.get(
					LDAPServerAttributeRelImpl.class, primaryKey);

			if (ldapServerAttributeRel == null) {
				if (_log.isDebugEnabled()) {
					_log.debug(_NO_SUCH_ENTITY_WITH_PRIMARY_KEY + primaryKey);
				}

				throw new NoSuchServerAttributeRelException(
					_NO_SUCH_ENTITY_WITH_PRIMARY_KEY + primaryKey);
			}

			return remove(ldapServerAttributeRel);
		}
		catch (NoSuchServerAttributeRelException noSuchEntityException) {
			throw noSuchEntityException;
		}
		catch (Exception exception) {
			throw processException(exception);
		}
		finally {
			closeSession(session);
		}
	}

	@Override
	protected LDAPServerAttributeRel removeImpl(
		LDAPServerAttributeRel ldapServerAttributeRel) {

		Session session = null;

		try {
			session = openSession();

			if (!session.contains(ldapServerAttributeRel)) {
				ldapServerAttributeRel = (LDAPServerAttributeRel)session.get(
					LDAPServerAttributeRelImpl.class,
					ldapServerAttributeRel.getPrimaryKeyObj());
			}

			if (ldapServerAttributeRel != null) {
				session.delete(ldapServerAttributeRel);
			}
		}
		catch (Exception exception) {
			throw processException(exception);
		}
		finally {
			closeSession(session);
		}

		if (ldapServerAttributeRel != null) {
			clearCache(ldapServerAttributeRel);
		}

		return ldapServerAttributeRel;
	}

	@Override
	public LDAPServerAttributeRel updateImpl(
		LDAPServerAttributeRel ldapServerAttributeRel) {

		boolean isNew = ldapServerAttributeRel.isNew();

		if (!(ldapServerAttributeRel instanceof
				LDAPServerAttributeRelModelImpl)) {

			InvocationHandler invocationHandler = null;

			if (ProxyUtil.isProxyClass(ldapServerAttributeRel.getClass())) {
				invocationHandler = ProxyUtil.getInvocationHandler(
					ldapServerAttributeRel);

				throw new IllegalArgumentException(
					"Implement ModelWrapper in ldapServerAttributeRel proxy " +
						invocationHandler.getClass());
			}

			throw new IllegalArgumentException(
				"Implement ModelWrapper in custom LDAPServerAttributeRel implementation " +
					ldapServerAttributeRel.getClass());
		}

		LDAPServerAttributeRelModelImpl ldapServerAttributeRelModelImpl =
			(LDAPServerAttributeRelModelImpl)ldapServerAttributeRel;

		Session session = null;

		try {
			session = openSession();

			if (isNew) {
				session.save(ldapServerAttributeRel);
			}
			else {
				ldapServerAttributeRel = (LDAPServerAttributeRel)session.merge(
					ldapServerAttributeRel);
			}
		}
		catch (Exception exception) {
			throw processException(exception);
		}
		finally {
			closeSession(session);
		}

		entityCache.putResult(
			LDAPServerAttributeRelImpl.class, ldapServerAttributeRelModelImpl,
			false, true);

		cacheUniqueFindersCache(ldapServerAttributeRelModelImpl);

		if (isNew) {
			ldapServerAttributeRel.setNew(false);
		}

		ldapServerAttributeRel.resetOriginalValues();

		return ldapServerAttributeRel;
	}

	/**
	 * Returns the ldap server attribute rel with the primary key or throws a <code>com.liferay.portal.kernel.exception.NoSuchModelException</code> if it could not be found.
	 *
	 * @param primaryKey the primary key of the ldap server attribute rel
	 * @return the ldap server attribute rel
	 * @throws NoSuchServerAttributeRelException if a ldap server attribute rel with the primary key could not be found
	 */
	@Override
	public LDAPServerAttributeRel findByPrimaryKey(Serializable primaryKey)
		throws NoSuchServerAttributeRelException {

		LDAPServerAttributeRel ldapServerAttributeRel = fetchByPrimaryKey(
			primaryKey);

		if (ldapServerAttributeRel == null) {
			if (_log.isDebugEnabled()) {
				_log.debug(_NO_SUCH_ENTITY_WITH_PRIMARY_KEY + primaryKey);
			}

			throw new NoSuchServerAttributeRelException(
				_NO_SUCH_ENTITY_WITH_PRIMARY_KEY + primaryKey);
		}

		return ldapServerAttributeRel;
	}

	/**
	 * Returns the ldap server attribute rel with the primary key or throws a <code>NoSuchServerAttributeRelException</code> if it could not be found.
	 *
	 * @param ldapServerAttributeRelId the primary key of the ldap server attribute rel
	 * @return the ldap server attribute rel
	 * @throws NoSuchServerAttributeRelException if a ldap server attribute rel with the primary key could not be found
	 */
	@Override
	public LDAPServerAttributeRel findByPrimaryKey(
			long ldapServerAttributeRelId)
		throws NoSuchServerAttributeRelException {

		return findByPrimaryKey((Serializable)ldapServerAttributeRelId);
	}

	/**
	 * Returns the ldap server attribute rel with the primary key or returns <code>null</code> if it could not be found.
	 *
	 * @param ldapServerAttributeRelId the primary key of the ldap server attribute rel
	 * @return the ldap server attribute rel, or <code>null</code> if a ldap server attribute rel with the primary key could not be found
	 */
	@Override
	public LDAPServerAttributeRel fetchByPrimaryKey(
		long ldapServerAttributeRelId) {

		return fetchByPrimaryKey((Serializable)ldapServerAttributeRelId);
	}

	/**
	 * Returns all the ldap server attribute rels.
	 *
	 * @return the ldap server attribute rels
	 */
	@Override
	public List<LDAPServerAttributeRel> findAll() {
		return findAll(QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
	}

	/**
	 * Returns a range of all the ldap server attribute rels.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>LDAPServerAttributeRelModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of ldap server attribute rels
	 * @param end the upper bound of the range of ldap server attribute rels (not inclusive)
	 * @return the range of ldap server attribute rels
	 */
	@Override
	public List<LDAPServerAttributeRel> findAll(int start, int end) {
		return findAll(start, end, null);
	}

	/**
	 * Returns an ordered range of all the ldap server attribute rels.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>LDAPServerAttributeRelModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of ldap server attribute rels
	 * @param end the upper bound of the range of ldap server attribute rels (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of ldap server attribute rels
	 */
	@Override
	public List<LDAPServerAttributeRel> findAll(
		int start, int end,
		OrderByComparator<LDAPServerAttributeRel> orderByComparator) {

		return findAll(start, end, orderByComparator, true);
	}

	/**
	 * Returns an ordered range of all the ldap server attribute rels.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>LDAPServerAttributeRelModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of ldap server attribute rels
	 * @param end the upper bound of the range of ldap server attribute rels (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of ldap server attribute rels
	 */
	@Override
	public List<LDAPServerAttributeRel> findAll(
		int start, int end,
		OrderByComparator<LDAPServerAttributeRel> orderByComparator,
		boolean useFinderCache) {

		FinderPath finderPath = null;
		Object[] finderArgs = null;

		if ((start == QueryUtil.ALL_POS) && (end == QueryUtil.ALL_POS) &&
			(orderByComparator == null)) {

			if (useFinderCache) {
				finderPath = _finderPathWithoutPaginationFindAll;
				finderArgs = FINDER_ARGS_EMPTY;
			}
		}
		else if (useFinderCache) {
			finderPath = _finderPathWithPaginationFindAll;
			finderArgs = new Object[] {start, end, orderByComparator};
		}

		List<LDAPServerAttributeRel> list = null;

		if (useFinderCache) {
			list = (List<LDAPServerAttributeRel>)finderCache.getResult(
				finderPath, finderArgs);
		}

		if (list == null) {
			StringBundler sb = null;
			String sql = null;

			if (orderByComparator != null) {
				sb = new StringBundler(
					2 + (orderByComparator.getOrderByFields().length * 2));

				sb.append(_SQL_SELECT_LDAPSERVERATTRIBUTEREL);

				appendOrderByComparator(
					sb, _ORDER_BY_ENTITY_ALIAS, orderByComparator);

				sql = sb.toString();
			}
			else {
				sql = _SQL_SELECT_LDAPSERVERATTRIBUTEREL;

				sql = sql.concat(LDAPServerAttributeRelModelImpl.ORDER_BY_JPQL);
			}

			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(sql);

				list = (List<LDAPServerAttributeRel>)QueryUtil.list(
					query, getDialect(), start, end);

				cacheResult(list);

				if (useFinderCache) {
					finderCache.putResult(finderPath, finderArgs, list);
				}
			}
			catch (Exception exception) {
				throw processException(exception);
			}
			finally {
				closeSession(session);
			}
		}

		return list;
	}

	/**
	 * Removes all the ldap server attribute rels from the database.
	 *
	 */
	@Override
	public void removeAll() {
		for (LDAPServerAttributeRel ldapServerAttributeRel : findAll()) {
			remove(ldapServerAttributeRel);
		}
	}

	/**
	 * Returns the number of ldap server attribute rels.
	 *
	 * @return the number of ldap server attribute rels
	 */
	@Override
	public int countAll() {
		Long count = (Long)finderCache.getResult(
			_finderPathCountAll, FINDER_ARGS_EMPTY);

		if (count == null) {
			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(
					_SQL_COUNT_LDAPSERVERATTRIBUTEREL);

				count = (Long)query.uniqueResult();

				finderCache.putResult(
					_finderPathCountAll, FINDER_ARGS_EMPTY, count);
			}
			catch (Exception exception) {
				throw processException(exception);
			}
			finally {
				closeSession(session);
			}
		}

		return count.intValue();
	}

	@Override
	protected EntityCache getEntityCache() {
		return entityCache;
	}

	@Override
	protected String getPKDBName() {
		return "ldapServerAttributeRelId";
	}

	@Override
	protected String getSelectSQL() {
		return _SQL_SELECT_LDAPSERVERATTRIBUTEREL;
	}

	@Override
	protected Map<String, Integer> getTableColumnsMap() {
		return LDAPServerAttributeRelModelImpl.TABLE_COLUMNS_MAP;
	}

	/**
	 * Initializes the ldap server attribute rel persistence.
	 */
	@Activate
	public void activate() {
		_valueObjectFinderCacheListThreshold = GetterUtil.getInteger(
			PropsUtil.get(PropsKeys.VALUE_OBJECT_FINDER_CACHE_LIST_THRESHOLD));

		_finderPathWithPaginationFindAll = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITH_PAGINATION, "findAll", new String[0],
			new String[0], true);

		_finderPathWithoutPaginationFindAll = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "findAll", new String[0],
			new String[0], true);

		_finderPathCountAll = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countAll",
			new String[0], new String[0], false);

		_finderPathWithPaginationFindByLdapServerId = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITH_PAGINATION, "findByLdapServerId",
			new String[] {
				Long.class.getName(), Integer.class.getName(),
				Integer.class.getName(), OrderByComparator.class.getName()
			},
			new String[] {"ldapServerId"}, true);

		_finderPathWithoutPaginationFindByLdapServerId = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "findByLdapServerId",
			new String[] {Long.class.getName()}, new String[] {"ldapServerId"},
			true);

		_finderPathCountByLdapServerId = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countByLdapServerId",
			new String[] {Long.class.getName()}, new String[] {"ldapServerId"},
			false);

		_finderPathWithPaginationFindByL_C = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITH_PAGINATION, "findByL_C",
			new String[] {
				Long.class.getName(), Long.class.getName(),
				Integer.class.getName(), Integer.class.getName(),
				OrderByComparator.class.getName()
			},
			new String[] {"ldapServerId", "classNameId"}, true);

		_finderPathWithoutPaginationFindByL_C = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "findByL_C",
			new String[] {Long.class.getName(), Long.class.getName()},
			new String[] {"ldapServerId", "classNameId"}, true);

		_finderPathCountByL_C = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countByL_C",
			new String[] {Long.class.getName(), Long.class.getName()},
			new String[] {"ldapServerId", "classNameId"}, false);

		_finderPathWithPaginationFindByC_C = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITH_PAGINATION, "findByC_C",
			new String[] {
				Long.class.getName(), Long.class.getName(),
				Integer.class.getName(), Integer.class.getName(),
				OrderByComparator.class.getName()
			},
			new String[] {"classNameId", "classPK"}, true);

		_finderPathWithoutPaginationFindByC_C = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "findByC_C",
			new String[] {Long.class.getName(), Long.class.getName()},
			new String[] {"classNameId", "classPK"}, true);

		_finderPathCountByC_C = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countByC_C",
			new String[] {Long.class.getName(), Long.class.getName()},
			new String[] {"classNameId", "classPK"}, false);

		_finderPathFetchByL_C_C = new FinderPath(
			FINDER_CLASS_NAME_ENTITY, "fetchByL_C_C",
			new String[] {
				Long.class.getName(), Long.class.getName(), Long.class.getName()
			},
			new String[] {"ldapServerId", "classNameId", "classPK"}, true);

		_finderPathCountByL_C_C = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countByL_C_C",
			new String[] {
				Long.class.getName(), Long.class.getName(), Long.class.getName()
			},
			new String[] {"ldapServerId", "classNameId", "classPK"}, false);

		_setLDAPServerAttributeRelUtilPersistence(this);
	}

	@Deactivate
	public void deactivate() {
		_setLDAPServerAttributeRelUtilPersistence(null);

		entityCache.removeCache(LDAPServerAttributeRelImpl.class.getName());
	}

	private void _setLDAPServerAttributeRelUtilPersistence(
		LDAPServerAttributeRelPersistence ldapServerAttributeRelPersistence) {

		try {
			Field field = LDAPServerAttributeRelUtil.class.getDeclaredField(
				"_persistence");

			field.setAccessible(true);

			field.set(null, ldapServerAttributeRelPersistence);
		}
		catch (ReflectiveOperationException reflectiveOperationException) {
			throw new RuntimeException(reflectiveOperationException);
		}
	}

	@Override
	@Reference(
		target = LDAPPersistenceConstants.SERVICE_CONFIGURATION_FILTER,
		unbind = "-"
	)
	public void setConfiguration(Configuration configuration) {
	}

	@Override
	@Reference(
		target = LDAPPersistenceConstants.ORIGIN_BUNDLE_SYMBOLIC_NAME_FILTER,
		unbind = "-"
	)
	public void setDataSource(DataSource dataSource) {
		super.setDataSource(dataSource);
	}

	@Override
	@Reference(
		target = LDAPPersistenceConstants.ORIGIN_BUNDLE_SYMBOLIC_NAME_FILTER,
		unbind = "-"
	)
	public void setSessionFactory(SessionFactory sessionFactory) {
		super.setSessionFactory(sessionFactory);
	}

	@Reference
	protected EntityCache entityCache;

	@Reference
	protected FinderCache finderCache;

	private static final String _SQL_SELECT_LDAPSERVERATTRIBUTEREL =
		"SELECT ldapServerAttributeRel FROM LDAPServerAttributeRel ldapServerAttributeRel";

	private static final String _SQL_SELECT_LDAPSERVERATTRIBUTEREL_WHERE =
		"SELECT ldapServerAttributeRel FROM LDAPServerAttributeRel ldapServerAttributeRel WHERE ";

	private static final String _SQL_COUNT_LDAPSERVERATTRIBUTEREL =
		"SELECT COUNT(ldapServerAttributeRel) FROM LDAPServerAttributeRel ldapServerAttributeRel";

	private static final String _SQL_COUNT_LDAPSERVERATTRIBUTEREL_WHERE =
		"SELECT COUNT(ldapServerAttributeRel) FROM LDAPServerAttributeRel ldapServerAttributeRel WHERE ";

	private static final String _ORDER_BY_ENTITY_ALIAS =
		"ldapServerAttributeRel.";

	private static final String _NO_SUCH_ENTITY_WITH_PRIMARY_KEY =
		"No LDAPServerAttributeRel exists with the primary key ";

	private static final String _NO_SUCH_ENTITY_WITH_KEY =
		"No LDAPServerAttributeRel exists with the key {";

	private static final Log _log = LogFactoryUtil.getLog(
		LDAPServerAttributeRelPersistenceImpl.class);

	@Override
	protected FinderCache getFinderCache() {
		return finderCache;
	}

}