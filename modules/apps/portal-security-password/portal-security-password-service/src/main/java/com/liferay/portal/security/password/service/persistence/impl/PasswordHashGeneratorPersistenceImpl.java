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

package com.liferay.portal.security.password.service.persistence.impl;

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
import com.liferay.portal.kernel.service.persistence.impl.BasePersistenceImpl;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.ProxyUtil;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.uuid.PortalUUIDUtil;
import com.liferay.portal.security.password.exception.NoSuchHashGeneratorException;
import com.liferay.portal.security.password.model.PasswordHashGenerator;
import com.liferay.portal.security.password.model.PasswordHashGeneratorTable;
import com.liferay.portal.security.password.model.impl.PasswordHashGeneratorImpl;
import com.liferay.portal.security.password.model.impl.PasswordHashGeneratorModelImpl;
import com.liferay.portal.security.password.service.persistence.PasswordHashGeneratorPersistence;
import com.liferay.portal.security.password.service.persistence.impl.constants.PasswordPersistenceConstants;

import java.io.Serializable;

import java.lang.reflect.InvocationHandler;

import java.sql.Timestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.sql.DataSource;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * The persistence implementation for the password hash generator service.
 *
 * <p>
 * Caching information and settings can be found in <code>portal.properties</code>
 * </p>
 *
 * @author Arthur Chan
 * @generated
 */
@Component(service = PasswordHashGeneratorPersistence.class)
public class PasswordHashGeneratorPersistenceImpl
	extends BasePersistenceImpl<PasswordHashGenerator>
	implements PasswordHashGeneratorPersistence {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this class directly. Always use <code>PasswordHashGeneratorUtil</code> to access the password hash generator persistence. Modify <code>service.xml</code> and rerun ServiceBuilder to regenerate this class.
	 */
	public static final String FINDER_CLASS_NAME_ENTITY =
		PasswordHashGeneratorImpl.class.getName();

	public static final String FINDER_CLASS_NAME_LIST_WITH_PAGINATION =
		FINDER_CLASS_NAME_ENTITY + ".List1";

	public static final String FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION =
		FINDER_CLASS_NAME_ENTITY + ".List2";

	private FinderPath _finderPathWithPaginationFindAll;
	private FinderPath _finderPathWithoutPaginationFindAll;
	private FinderPath _finderPathCountAll;
	private FinderPath _finderPathWithPaginationFindByUuid;
	private FinderPath _finderPathWithoutPaginationFindByUuid;
	private FinderPath _finderPathCountByUuid;

	/**
	 * Returns all the password hash generators where uuid = &#63;.
	 *
	 * @param uuid the uuid
	 * @return the matching password hash generators
	 */
	@Override
	public List<PasswordHashGenerator> findByUuid(String uuid) {
		return findByUuid(uuid, QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
	}

	/**
	 * Returns a range of all the password hash generators where uuid = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordHashGeneratorModelImpl</code>.
	 * </p>
	 *
	 * @param uuid the uuid
	 * @param start the lower bound of the range of password hash generators
	 * @param end the upper bound of the range of password hash generators (not inclusive)
	 * @return the range of matching password hash generators
	 */
	@Override
	public List<PasswordHashGenerator> findByUuid(
		String uuid, int start, int end) {

		return findByUuid(uuid, start, end, null);
	}

	/**
	 * Returns an ordered range of all the password hash generators where uuid = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordHashGeneratorModelImpl</code>.
	 * </p>
	 *
	 * @param uuid the uuid
	 * @param start the lower bound of the range of password hash generators
	 * @param end the upper bound of the range of password hash generators (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching password hash generators
	 */
	@Override
	public List<PasswordHashGenerator> findByUuid(
		String uuid, int start, int end,
		OrderByComparator<PasswordHashGenerator> orderByComparator) {

		return findByUuid(uuid, start, end, orderByComparator, true);
	}

	/**
	 * Returns an ordered range of all the password hash generators where uuid = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordHashGeneratorModelImpl</code>.
	 * </p>
	 *
	 * @param uuid the uuid
	 * @param start the lower bound of the range of password hash generators
	 * @param end the upper bound of the range of password hash generators (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching password hash generators
	 */
	@Override
	public List<PasswordHashGenerator> findByUuid(
		String uuid, int start, int end,
		OrderByComparator<PasswordHashGenerator> orderByComparator,
		boolean useFinderCache) {

		uuid = Objects.toString(uuid, "");

		FinderPath finderPath = null;
		Object[] finderArgs = null;

		if ((start == QueryUtil.ALL_POS) && (end == QueryUtil.ALL_POS) &&
			(orderByComparator == null)) {

			if (useFinderCache) {
				finderPath = _finderPathWithoutPaginationFindByUuid;
				finderArgs = new Object[] {uuid};
			}
		}
		else if (useFinderCache) {
			finderPath = _finderPathWithPaginationFindByUuid;
			finderArgs = new Object[] {uuid, start, end, orderByComparator};
		}

		List<PasswordHashGenerator> list = null;

		if (useFinderCache) {
			list = (List<PasswordHashGenerator>)finderCache.getResult(
				finderPath, finderArgs, this);

			if ((list != null) && !list.isEmpty()) {
				for (PasswordHashGenerator passwordHashGenerator : list) {
					if (!uuid.equals(passwordHashGenerator.getUuid())) {
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

			sb.append(_SQL_SELECT_PASSWORDHASHGENERATOR_WHERE);

			boolean bindUuid = false;

			if (uuid.isEmpty()) {
				sb.append(_FINDER_COLUMN_UUID_UUID_3);
			}
			else {
				bindUuid = true;

				sb.append(_FINDER_COLUMN_UUID_UUID_2);
			}

			if (orderByComparator != null) {
				appendOrderByComparator(
					sb, _ORDER_BY_ENTITY_ALIAS, orderByComparator);
			}
			else {
				sb.append(PasswordHashGeneratorModelImpl.ORDER_BY_JPQL);
			}

			String sql = sb.toString();

			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(sql);

				QueryPos queryPos = QueryPos.getInstance(query);

				if (bindUuid) {
					queryPos.add(uuid);
				}

				list = (List<PasswordHashGenerator>)QueryUtil.list(
					query, getDialect(), start, end);

				cacheResult(list);

				if (useFinderCache) {
					finderCache.putResult(finderPath, finderArgs, list);
				}
			}
			catch (Exception exception) {
				if (useFinderCache) {
					finderCache.removeResult(finderPath, finderArgs);
				}

				throw processException(exception);
			}
			finally {
				closeSession(session);
			}
		}

		return list;
	}

	/**
	 * Returns the first password hash generator in the ordered set where uuid = &#63;.
	 *
	 * @param uuid the uuid
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching password hash generator
	 * @throws NoSuchHashGeneratorException if a matching password hash generator could not be found
	 */
	@Override
	public PasswordHashGenerator findByUuid_First(
			String uuid,
			OrderByComparator<PasswordHashGenerator> orderByComparator)
		throws NoSuchHashGeneratorException {

		PasswordHashGenerator passwordHashGenerator = fetchByUuid_First(
			uuid, orderByComparator);

		if (passwordHashGenerator != null) {
			return passwordHashGenerator;
		}

		StringBundler sb = new StringBundler(4);

		sb.append(_NO_SUCH_ENTITY_WITH_KEY);

		sb.append("uuid=");
		sb.append(uuid);

		sb.append("}");

		throw new NoSuchHashGeneratorException(sb.toString());
	}

	/**
	 * Returns the first password hash generator in the ordered set where uuid = &#63;.
	 *
	 * @param uuid the uuid
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching password hash generator, or <code>null</code> if a matching password hash generator could not be found
	 */
	@Override
	public PasswordHashGenerator fetchByUuid_First(
		String uuid,
		OrderByComparator<PasswordHashGenerator> orderByComparator) {

		List<PasswordHashGenerator> list = findByUuid(
			uuid, 0, 1, orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the last password hash generator in the ordered set where uuid = &#63;.
	 *
	 * @param uuid the uuid
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching password hash generator
	 * @throws NoSuchHashGeneratorException if a matching password hash generator could not be found
	 */
	@Override
	public PasswordHashGenerator findByUuid_Last(
			String uuid,
			OrderByComparator<PasswordHashGenerator> orderByComparator)
		throws NoSuchHashGeneratorException {

		PasswordHashGenerator passwordHashGenerator = fetchByUuid_Last(
			uuid, orderByComparator);

		if (passwordHashGenerator != null) {
			return passwordHashGenerator;
		}

		StringBundler sb = new StringBundler(4);

		sb.append(_NO_SUCH_ENTITY_WITH_KEY);

		sb.append("uuid=");
		sb.append(uuid);

		sb.append("}");

		throw new NoSuchHashGeneratorException(sb.toString());
	}

	/**
	 * Returns the last password hash generator in the ordered set where uuid = &#63;.
	 *
	 * @param uuid the uuid
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching password hash generator, or <code>null</code> if a matching password hash generator could not be found
	 */
	@Override
	public PasswordHashGenerator fetchByUuid_Last(
		String uuid,
		OrderByComparator<PasswordHashGenerator> orderByComparator) {

		int count = countByUuid(uuid);

		if (count == 0) {
			return null;
		}

		List<PasswordHashGenerator> list = findByUuid(
			uuid, count - 1, count, orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the password hash generators before and after the current password hash generator in the ordered set where uuid = &#63;.
	 *
	 * @param passwordHashGeneratorId the primary key of the current password hash generator
	 * @param uuid the uuid
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next password hash generator
	 * @throws NoSuchHashGeneratorException if a password hash generator with the primary key could not be found
	 */
	@Override
	public PasswordHashGenerator[] findByUuid_PrevAndNext(
			long passwordHashGeneratorId, String uuid,
			OrderByComparator<PasswordHashGenerator> orderByComparator)
		throws NoSuchHashGeneratorException {

		uuid = Objects.toString(uuid, "");

		PasswordHashGenerator passwordHashGenerator = findByPrimaryKey(
			passwordHashGeneratorId);

		Session session = null;

		try {
			session = openSession();

			PasswordHashGenerator[] array = new PasswordHashGeneratorImpl[3];

			array[0] = getByUuid_PrevAndNext(
				session, passwordHashGenerator, uuid, orderByComparator, true);

			array[1] = passwordHashGenerator;

			array[2] = getByUuid_PrevAndNext(
				session, passwordHashGenerator, uuid, orderByComparator, false);

			return array;
		}
		catch (Exception exception) {
			throw processException(exception);
		}
		finally {
			closeSession(session);
		}
	}

	protected PasswordHashGenerator getByUuid_PrevAndNext(
		Session session, PasswordHashGenerator passwordHashGenerator,
		String uuid, OrderByComparator<PasswordHashGenerator> orderByComparator,
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

		sb.append(_SQL_SELECT_PASSWORDHASHGENERATOR_WHERE);

		boolean bindUuid = false;

		if (uuid.isEmpty()) {
			sb.append(_FINDER_COLUMN_UUID_UUID_3);
		}
		else {
			bindUuid = true;

			sb.append(_FINDER_COLUMN_UUID_UUID_2);
		}

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
			sb.append(PasswordHashGeneratorModelImpl.ORDER_BY_JPQL);
		}

		String sql = sb.toString();

		Query query = session.createQuery(sql);

		query.setFirstResult(0);
		query.setMaxResults(2);

		QueryPos queryPos = QueryPos.getInstance(query);

		if (bindUuid) {
			queryPos.add(uuid);
		}

		if (orderByComparator != null) {
			for (Object orderByConditionValue :
					orderByComparator.getOrderByConditionValues(
						passwordHashGenerator)) {

				queryPos.add(orderByConditionValue);
			}
		}

		List<PasswordHashGenerator> list = query.list();

		if (list.size() == 2) {
			return list.get(1);
		}
		else {
			return null;
		}
	}

	/**
	 * Removes all the password hash generators where uuid = &#63; from the database.
	 *
	 * @param uuid the uuid
	 */
	@Override
	public void removeByUuid(String uuid) {
		for (PasswordHashGenerator passwordHashGenerator :
				findByUuid(uuid, QueryUtil.ALL_POS, QueryUtil.ALL_POS, null)) {

			remove(passwordHashGenerator);
		}
	}

	/**
	 * Returns the number of password hash generators where uuid = &#63;.
	 *
	 * @param uuid the uuid
	 * @return the number of matching password hash generators
	 */
	@Override
	public int countByUuid(String uuid) {
		uuid = Objects.toString(uuid, "");

		FinderPath finderPath = _finderPathCountByUuid;

		Object[] finderArgs = new Object[] {uuid};

		Long count = (Long)finderCache.getResult(finderPath, finderArgs, this);

		if (count == null) {
			StringBundler sb = new StringBundler(2);

			sb.append(_SQL_COUNT_PASSWORDHASHGENERATOR_WHERE);

			boolean bindUuid = false;

			if (uuid.isEmpty()) {
				sb.append(_FINDER_COLUMN_UUID_UUID_3);
			}
			else {
				bindUuid = true;

				sb.append(_FINDER_COLUMN_UUID_UUID_2);
			}

			String sql = sb.toString();

			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(sql);

				QueryPos queryPos = QueryPos.getInstance(query);

				if (bindUuid) {
					queryPos.add(uuid);
				}

				count = (Long)query.uniqueResult();

				finderCache.putResult(finderPath, finderArgs, count);
			}
			catch (Exception exception) {
				finderCache.removeResult(finderPath, finderArgs);

				throw processException(exception);
			}
			finally {
				closeSession(session);
			}
		}

		return count.intValue();
	}

	private static final String _FINDER_COLUMN_UUID_UUID_2 =
		"passwordHashGenerator.uuid = ?";

	private static final String _FINDER_COLUMN_UUID_UUID_3 =
		"(passwordHashGenerator.uuid IS NULL OR passwordHashGenerator.uuid = '')";

	private FinderPath _finderPathWithPaginationFindByUuid_C;
	private FinderPath _finderPathWithoutPaginationFindByUuid_C;
	private FinderPath _finderPathCountByUuid_C;

	/**
	 * Returns all the password hash generators where uuid = &#63; and companyId = &#63;.
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @return the matching password hash generators
	 */
	@Override
	public List<PasswordHashGenerator> findByUuid_C(
		String uuid, long companyId) {

		return findByUuid_C(
			uuid, companyId, QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
	}

	/**
	 * Returns a range of all the password hash generators where uuid = &#63; and companyId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordHashGeneratorModelImpl</code>.
	 * </p>
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param start the lower bound of the range of password hash generators
	 * @param end the upper bound of the range of password hash generators (not inclusive)
	 * @return the range of matching password hash generators
	 */
	@Override
	public List<PasswordHashGenerator> findByUuid_C(
		String uuid, long companyId, int start, int end) {

		return findByUuid_C(uuid, companyId, start, end, null);
	}

	/**
	 * Returns an ordered range of all the password hash generators where uuid = &#63; and companyId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordHashGeneratorModelImpl</code>.
	 * </p>
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param start the lower bound of the range of password hash generators
	 * @param end the upper bound of the range of password hash generators (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching password hash generators
	 */
	@Override
	public List<PasswordHashGenerator> findByUuid_C(
		String uuid, long companyId, int start, int end,
		OrderByComparator<PasswordHashGenerator> orderByComparator) {

		return findByUuid_C(
			uuid, companyId, start, end, orderByComparator, true);
	}

	/**
	 * Returns an ordered range of all the password hash generators where uuid = &#63; and companyId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordHashGeneratorModelImpl</code>.
	 * </p>
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param start the lower bound of the range of password hash generators
	 * @param end the upper bound of the range of password hash generators (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching password hash generators
	 */
	@Override
	public List<PasswordHashGenerator> findByUuid_C(
		String uuid, long companyId, int start, int end,
		OrderByComparator<PasswordHashGenerator> orderByComparator,
		boolean useFinderCache) {

		uuid = Objects.toString(uuid, "");

		FinderPath finderPath = null;
		Object[] finderArgs = null;

		if ((start == QueryUtil.ALL_POS) && (end == QueryUtil.ALL_POS) &&
			(orderByComparator == null)) {

			if (useFinderCache) {
				finderPath = _finderPathWithoutPaginationFindByUuid_C;
				finderArgs = new Object[] {uuid, companyId};
			}
		}
		else if (useFinderCache) {
			finderPath = _finderPathWithPaginationFindByUuid_C;
			finderArgs = new Object[] {
				uuid, companyId, start, end, orderByComparator
			};
		}

		List<PasswordHashGenerator> list = null;

		if (useFinderCache) {
			list = (List<PasswordHashGenerator>)finderCache.getResult(
				finderPath, finderArgs, this);

			if ((list != null) && !list.isEmpty()) {
				for (PasswordHashGenerator passwordHashGenerator : list) {
					if (!uuid.equals(passwordHashGenerator.getUuid()) ||
						(companyId != passwordHashGenerator.getCompanyId())) {

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

			sb.append(_SQL_SELECT_PASSWORDHASHGENERATOR_WHERE);

			boolean bindUuid = false;

			if (uuid.isEmpty()) {
				sb.append(_FINDER_COLUMN_UUID_C_UUID_3);
			}
			else {
				bindUuid = true;

				sb.append(_FINDER_COLUMN_UUID_C_UUID_2);
			}

			sb.append(_FINDER_COLUMN_UUID_C_COMPANYID_2);

			if (orderByComparator != null) {
				appendOrderByComparator(
					sb, _ORDER_BY_ENTITY_ALIAS, orderByComparator);
			}
			else {
				sb.append(PasswordHashGeneratorModelImpl.ORDER_BY_JPQL);
			}

			String sql = sb.toString();

			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(sql);

				QueryPos queryPos = QueryPos.getInstance(query);

				if (bindUuid) {
					queryPos.add(uuid);
				}

				queryPos.add(companyId);

				list = (List<PasswordHashGenerator>)QueryUtil.list(
					query, getDialect(), start, end);

				cacheResult(list);

				if (useFinderCache) {
					finderCache.putResult(finderPath, finderArgs, list);
				}
			}
			catch (Exception exception) {
				if (useFinderCache) {
					finderCache.removeResult(finderPath, finderArgs);
				}

				throw processException(exception);
			}
			finally {
				closeSession(session);
			}
		}

		return list;
	}

	/**
	 * Returns the first password hash generator in the ordered set where uuid = &#63; and companyId = &#63;.
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching password hash generator
	 * @throws NoSuchHashGeneratorException if a matching password hash generator could not be found
	 */
	@Override
	public PasswordHashGenerator findByUuid_C_First(
			String uuid, long companyId,
			OrderByComparator<PasswordHashGenerator> orderByComparator)
		throws NoSuchHashGeneratorException {

		PasswordHashGenerator passwordHashGenerator = fetchByUuid_C_First(
			uuid, companyId, orderByComparator);

		if (passwordHashGenerator != null) {
			return passwordHashGenerator;
		}

		StringBundler sb = new StringBundler(6);

		sb.append(_NO_SUCH_ENTITY_WITH_KEY);

		sb.append("uuid=");
		sb.append(uuid);

		sb.append(", companyId=");
		sb.append(companyId);

		sb.append("}");

		throw new NoSuchHashGeneratorException(sb.toString());
	}

	/**
	 * Returns the first password hash generator in the ordered set where uuid = &#63; and companyId = &#63;.
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching password hash generator, or <code>null</code> if a matching password hash generator could not be found
	 */
	@Override
	public PasswordHashGenerator fetchByUuid_C_First(
		String uuid, long companyId,
		OrderByComparator<PasswordHashGenerator> orderByComparator) {

		List<PasswordHashGenerator> list = findByUuid_C(
			uuid, companyId, 0, 1, orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the last password hash generator in the ordered set where uuid = &#63; and companyId = &#63;.
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching password hash generator
	 * @throws NoSuchHashGeneratorException if a matching password hash generator could not be found
	 */
	@Override
	public PasswordHashGenerator findByUuid_C_Last(
			String uuid, long companyId,
			OrderByComparator<PasswordHashGenerator> orderByComparator)
		throws NoSuchHashGeneratorException {

		PasswordHashGenerator passwordHashGenerator = fetchByUuid_C_Last(
			uuid, companyId, orderByComparator);

		if (passwordHashGenerator != null) {
			return passwordHashGenerator;
		}

		StringBundler sb = new StringBundler(6);

		sb.append(_NO_SUCH_ENTITY_WITH_KEY);

		sb.append("uuid=");
		sb.append(uuid);

		sb.append(", companyId=");
		sb.append(companyId);

		sb.append("}");

		throw new NoSuchHashGeneratorException(sb.toString());
	}

	/**
	 * Returns the last password hash generator in the ordered set where uuid = &#63; and companyId = &#63;.
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching password hash generator, or <code>null</code> if a matching password hash generator could not be found
	 */
	@Override
	public PasswordHashGenerator fetchByUuid_C_Last(
		String uuid, long companyId,
		OrderByComparator<PasswordHashGenerator> orderByComparator) {

		int count = countByUuid_C(uuid, companyId);

		if (count == 0) {
			return null;
		}

		List<PasswordHashGenerator> list = findByUuid_C(
			uuid, companyId, count - 1, count, orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the password hash generators before and after the current password hash generator in the ordered set where uuid = &#63; and companyId = &#63;.
	 *
	 * @param passwordHashGeneratorId the primary key of the current password hash generator
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next password hash generator
	 * @throws NoSuchHashGeneratorException if a password hash generator with the primary key could not be found
	 */
	@Override
	public PasswordHashGenerator[] findByUuid_C_PrevAndNext(
			long passwordHashGeneratorId, String uuid, long companyId,
			OrderByComparator<PasswordHashGenerator> orderByComparator)
		throws NoSuchHashGeneratorException {

		uuid = Objects.toString(uuid, "");

		PasswordHashGenerator passwordHashGenerator = findByPrimaryKey(
			passwordHashGeneratorId);

		Session session = null;

		try {
			session = openSession();

			PasswordHashGenerator[] array = new PasswordHashGeneratorImpl[3];

			array[0] = getByUuid_C_PrevAndNext(
				session, passwordHashGenerator, uuid, companyId,
				orderByComparator, true);

			array[1] = passwordHashGenerator;

			array[2] = getByUuid_C_PrevAndNext(
				session, passwordHashGenerator, uuid, companyId,
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

	protected PasswordHashGenerator getByUuid_C_PrevAndNext(
		Session session, PasswordHashGenerator passwordHashGenerator,
		String uuid, long companyId,
		OrderByComparator<PasswordHashGenerator> orderByComparator,
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

		sb.append(_SQL_SELECT_PASSWORDHASHGENERATOR_WHERE);

		boolean bindUuid = false;

		if (uuid.isEmpty()) {
			sb.append(_FINDER_COLUMN_UUID_C_UUID_3);
		}
		else {
			bindUuid = true;

			sb.append(_FINDER_COLUMN_UUID_C_UUID_2);
		}

		sb.append(_FINDER_COLUMN_UUID_C_COMPANYID_2);

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
			sb.append(PasswordHashGeneratorModelImpl.ORDER_BY_JPQL);
		}

		String sql = sb.toString();

		Query query = session.createQuery(sql);

		query.setFirstResult(0);
		query.setMaxResults(2);

		QueryPos queryPos = QueryPos.getInstance(query);

		if (bindUuid) {
			queryPos.add(uuid);
		}

		queryPos.add(companyId);

		if (orderByComparator != null) {
			for (Object orderByConditionValue :
					orderByComparator.getOrderByConditionValues(
						passwordHashGenerator)) {

				queryPos.add(orderByConditionValue);
			}
		}

		List<PasswordHashGenerator> list = query.list();

		if (list.size() == 2) {
			return list.get(1);
		}
		else {
			return null;
		}
	}

	/**
	 * Removes all the password hash generators where uuid = &#63; and companyId = &#63; from the database.
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 */
	@Override
	public void removeByUuid_C(String uuid, long companyId) {
		for (PasswordHashGenerator passwordHashGenerator :
				findByUuid_C(
					uuid, companyId, QueryUtil.ALL_POS, QueryUtil.ALL_POS,
					null)) {

			remove(passwordHashGenerator);
		}
	}

	/**
	 * Returns the number of password hash generators where uuid = &#63; and companyId = &#63;.
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @return the number of matching password hash generators
	 */
	@Override
	public int countByUuid_C(String uuid, long companyId) {
		uuid = Objects.toString(uuid, "");

		FinderPath finderPath = _finderPathCountByUuid_C;

		Object[] finderArgs = new Object[] {uuid, companyId};

		Long count = (Long)finderCache.getResult(finderPath, finderArgs, this);

		if (count == null) {
			StringBundler sb = new StringBundler(3);

			sb.append(_SQL_COUNT_PASSWORDHASHGENERATOR_WHERE);

			boolean bindUuid = false;

			if (uuid.isEmpty()) {
				sb.append(_FINDER_COLUMN_UUID_C_UUID_3);
			}
			else {
				bindUuid = true;

				sb.append(_FINDER_COLUMN_UUID_C_UUID_2);
			}

			sb.append(_FINDER_COLUMN_UUID_C_COMPANYID_2);

			String sql = sb.toString();

			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(sql);

				QueryPos queryPos = QueryPos.getInstance(query);

				if (bindUuid) {
					queryPos.add(uuid);
				}

				queryPos.add(companyId);

				count = (Long)query.uniqueResult();

				finderCache.putResult(finderPath, finderArgs, count);
			}
			catch (Exception exception) {
				finderCache.removeResult(finderPath, finderArgs);

				throw processException(exception);
			}
			finally {
				closeSession(session);
			}
		}

		return count.intValue();
	}

	private static final String _FINDER_COLUMN_UUID_C_UUID_2 =
		"passwordHashGenerator.uuid = ? AND ";

	private static final String _FINDER_COLUMN_UUID_C_UUID_3 =
		"(passwordHashGenerator.uuid IS NULL OR passwordHashGenerator.uuid = '') AND ";

	private static final String _FINDER_COLUMN_UUID_C_COMPANYID_2 =
		"passwordHashGenerator.companyId = ?";

	private FinderPath _finderPathWithPaginationFindByLtCreateDate;
	private FinderPath _finderPathWithPaginationCountByLtCreateDate;

	/**
	 * Returns all the password hash generators where createDate &lt; &#63;.
	 *
	 * @param createDate the create date
	 * @return the matching password hash generators
	 */
	@Override
	public List<PasswordHashGenerator> findByLtCreateDate(Date createDate) {
		return findByLtCreateDate(
			createDate, QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
	}

	/**
	 * Returns a range of all the password hash generators where createDate &lt; &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordHashGeneratorModelImpl</code>.
	 * </p>
	 *
	 * @param createDate the create date
	 * @param start the lower bound of the range of password hash generators
	 * @param end the upper bound of the range of password hash generators (not inclusive)
	 * @return the range of matching password hash generators
	 */
	@Override
	public List<PasswordHashGenerator> findByLtCreateDate(
		Date createDate, int start, int end) {

		return findByLtCreateDate(createDate, start, end, null);
	}

	/**
	 * Returns an ordered range of all the password hash generators where createDate &lt; &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordHashGeneratorModelImpl</code>.
	 * </p>
	 *
	 * @param createDate the create date
	 * @param start the lower bound of the range of password hash generators
	 * @param end the upper bound of the range of password hash generators (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching password hash generators
	 */
	@Override
	public List<PasswordHashGenerator> findByLtCreateDate(
		Date createDate, int start, int end,
		OrderByComparator<PasswordHashGenerator> orderByComparator) {

		return findByLtCreateDate(
			createDate, start, end, orderByComparator, true);
	}

	/**
	 * Returns an ordered range of all the password hash generators where createDate &lt; &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordHashGeneratorModelImpl</code>.
	 * </p>
	 *
	 * @param createDate the create date
	 * @param start the lower bound of the range of password hash generators
	 * @param end the upper bound of the range of password hash generators (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching password hash generators
	 */
	@Override
	public List<PasswordHashGenerator> findByLtCreateDate(
		Date createDate, int start, int end,
		OrderByComparator<PasswordHashGenerator> orderByComparator,
		boolean useFinderCache) {

		FinderPath finderPath = null;
		Object[] finderArgs = null;

		finderPath = _finderPathWithPaginationFindByLtCreateDate;
		finderArgs = new Object[] {
			_getTime(createDate), start, end, orderByComparator
		};

		List<PasswordHashGenerator> list = null;

		if (useFinderCache) {
			list = (List<PasswordHashGenerator>)finderCache.getResult(
				finderPath, finderArgs, this);

			if ((list != null) && !list.isEmpty()) {
				for (PasswordHashGenerator passwordHashGenerator : list) {
					if (createDate.getTime() <=
							passwordHashGenerator.getCreateDate(
							).getTime()) {

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

			sb.append(_SQL_SELECT_PASSWORDHASHGENERATOR_WHERE);

			boolean bindCreateDate = false;

			if (createDate == null) {
				sb.append(_FINDER_COLUMN_LTCREATEDATE_CREATEDATE_1);
			}
			else {
				bindCreateDate = true;

				sb.append(_FINDER_COLUMN_LTCREATEDATE_CREATEDATE_2);
			}

			if (orderByComparator != null) {
				appendOrderByComparator(
					sb, _ORDER_BY_ENTITY_ALIAS, orderByComparator);
			}
			else {
				sb.append(PasswordHashGeneratorModelImpl.ORDER_BY_JPQL);
			}

			String sql = sb.toString();

			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(sql);

				QueryPos queryPos = QueryPos.getInstance(query);

				if (bindCreateDate) {
					queryPos.add(new Timestamp(createDate.getTime()));
				}

				list = (List<PasswordHashGenerator>)QueryUtil.list(
					query, getDialect(), start, end);

				cacheResult(list);

				if (useFinderCache) {
					finderCache.putResult(finderPath, finderArgs, list);
				}
			}
			catch (Exception exception) {
				if (useFinderCache) {
					finderCache.removeResult(finderPath, finderArgs);
				}

				throw processException(exception);
			}
			finally {
				closeSession(session);
			}
		}

		return list;
	}

	/**
	 * Returns the first password hash generator in the ordered set where createDate &lt; &#63;.
	 *
	 * @param createDate the create date
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching password hash generator
	 * @throws NoSuchHashGeneratorException if a matching password hash generator could not be found
	 */
	@Override
	public PasswordHashGenerator findByLtCreateDate_First(
			Date createDate,
			OrderByComparator<PasswordHashGenerator> orderByComparator)
		throws NoSuchHashGeneratorException {

		PasswordHashGenerator passwordHashGenerator = fetchByLtCreateDate_First(
			createDate, orderByComparator);

		if (passwordHashGenerator != null) {
			return passwordHashGenerator;
		}

		StringBundler sb = new StringBundler(4);

		sb.append(_NO_SUCH_ENTITY_WITH_KEY);

		sb.append("createDate<");
		sb.append(createDate);

		sb.append("}");

		throw new NoSuchHashGeneratorException(sb.toString());
	}

	/**
	 * Returns the first password hash generator in the ordered set where createDate &lt; &#63;.
	 *
	 * @param createDate the create date
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching password hash generator, or <code>null</code> if a matching password hash generator could not be found
	 */
	@Override
	public PasswordHashGenerator fetchByLtCreateDate_First(
		Date createDate,
		OrderByComparator<PasswordHashGenerator> orderByComparator) {

		List<PasswordHashGenerator> list = findByLtCreateDate(
			createDate, 0, 1, orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the last password hash generator in the ordered set where createDate &lt; &#63;.
	 *
	 * @param createDate the create date
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching password hash generator
	 * @throws NoSuchHashGeneratorException if a matching password hash generator could not be found
	 */
	@Override
	public PasswordHashGenerator findByLtCreateDate_Last(
			Date createDate,
			OrderByComparator<PasswordHashGenerator> orderByComparator)
		throws NoSuchHashGeneratorException {

		PasswordHashGenerator passwordHashGenerator = fetchByLtCreateDate_Last(
			createDate, orderByComparator);

		if (passwordHashGenerator != null) {
			return passwordHashGenerator;
		}

		StringBundler sb = new StringBundler(4);

		sb.append(_NO_SUCH_ENTITY_WITH_KEY);

		sb.append("createDate<");
		sb.append(createDate);

		sb.append("}");

		throw new NoSuchHashGeneratorException(sb.toString());
	}

	/**
	 * Returns the last password hash generator in the ordered set where createDate &lt; &#63;.
	 *
	 * @param createDate the create date
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching password hash generator, or <code>null</code> if a matching password hash generator could not be found
	 */
	@Override
	public PasswordHashGenerator fetchByLtCreateDate_Last(
		Date createDate,
		OrderByComparator<PasswordHashGenerator> orderByComparator) {

		int count = countByLtCreateDate(createDate);

		if (count == 0) {
			return null;
		}

		List<PasswordHashGenerator> list = findByLtCreateDate(
			createDate, count - 1, count, orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the password hash generators before and after the current password hash generator in the ordered set where createDate &lt; &#63;.
	 *
	 * @param passwordHashGeneratorId the primary key of the current password hash generator
	 * @param createDate the create date
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next password hash generator
	 * @throws NoSuchHashGeneratorException if a password hash generator with the primary key could not be found
	 */
	@Override
	public PasswordHashGenerator[] findByLtCreateDate_PrevAndNext(
			long passwordHashGeneratorId, Date createDate,
			OrderByComparator<PasswordHashGenerator> orderByComparator)
		throws NoSuchHashGeneratorException {

		PasswordHashGenerator passwordHashGenerator = findByPrimaryKey(
			passwordHashGeneratorId);

		Session session = null;

		try {
			session = openSession();

			PasswordHashGenerator[] array = new PasswordHashGeneratorImpl[3];

			array[0] = getByLtCreateDate_PrevAndNext(
				session, passwordHashGenerator, createDate, orderByComparator,
				true);

			array[1] = passwordHashGenerator;

			array[2] = getByLtCreateDate_PrevAndNext(
				session, passwordHashGenerator, createDate, orderByComparator,
				false);

			return array;
		}
		catch (Exception exception) {
			throw processException(exception);
		}
		finally {
			closeSession(session);
		}
	}

	protected PasswordHashGenerator getByLtCreateDate_PrevAndNext(
		Session session, PasswordHashGenerator passwordHashGenerator,
		Date createDate,
		OrderByComparator<PasswordHashGenerator> orderByComparator,
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

		sb.append(_SQL_SELECT_PASSWORDHASHGENERATOR_WHERE);

		boolean bindCreateDate = false;

		if (createDate == null) {
			sb.append(_FINDER_COLUMN_LTCREATEDATE_CREATEDATE_1);
		}
		else {
			bindCreateDate = true;

			sb.append(_FINDER_COLUMN_LTCREATEDATE_CREATEDATE_2);
		}

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
			sb.append(PasswordHashGeneratorModelImpl.ORDER_BY_JPQL);
		}

		String sql = sb.toString();

		Query query = session.createQuery(sql);

		query.setFirstResult(0);
		query.setMaxResults(2);

		QueryPos queryPos = QueryPos.getInstance(query);

		if (bindCreateDate) {
			queryPos.add(new Timestamp(createDate.getTime()));
		}

		if (orderByComparator != null) {
			for (Object orderByConditionValue :
					orderByComparator.getOrderByConditionValues(
						passwordHashGenerator)) {

				queryPos.add(orderByConditionValue);
			}
		}

		List<PasswordHashGenerator> list = query.list();

		if (list.size() == 2) {
			return list.get(1);
		}
		else {
			return null;
		}
	}

	/**
	 * Removes all the password hash generators where createDate &lt; &#63; from the database.
	 *
	 * @param createDate the create date
	 */
	@Override
	public void removeByLtCreateDate(Date createDate) {
		for (PasswordHashGenerator passwordHashGenerator :
				findByLtCreateDate(
					createDate, QueryUtil.ALL_POS, QueryUtil.ALL_POS, null)) {

			remove(passwordHashGenerator);
		}
	}

	/**
	 * Returns the number of password hash generators where createDate &lt; &#63;.
	 *
	 * @param createDate the create date
	 * @return the number of matching password hash generators
	 */
	@Override
	public int countByLtCreateDate(Date createDate) {
		FinderPath finderPath = _finderPathWithPaginationCountByLtCreateDate;

		Object[] finderArgs = new Object[] {_getTime(createDate)};

		Long count = (Long)finderCache.getResult(finderPath, finderArgs, this);

		if (count == null) {
			StringBundler sb = new StringBundler(2);

			sb.append(_SQL_COUNT_PASSWORDHASHGENERATOR_WHERE);

			boolean bindCreateDate = false;

			if (createDate == null) {
				sb.append(_FINDER_COLUMN_LTCREATEDATE_CREATEDATE_1);
			}
			else {
				bindCreateDate = true;

				sb.append(_FINDER_COLUMN_LTCREATEDATE_CREATEDATE_2);
			}

			String sql = sb.toString();

			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(sql);

				QueryPos queryPos = QueryPos.getInstance(query);

				if (bindCreateDate) {
					queryPos.add(new Timestamp(createDate.getTime()));
				}

				count = (Long)query.uniqueResult();

				finderCache.putResult(finderPath, finderArgs, count);
			}
			catch (Exception exception) {
				finderCache.removeResult(finderPath, finderArgs);

				throw processException(exception);
			}
			finally {
				closeSession(session);
			}
		}

		return count.intValue();
	}

	private static final String _FINDER_COLUMN_LTCREATEDATE_CREATEDATE_1 =
		"passwordHashGenerator.createDate IS NULL";

	private static final String _FINDER_COLUMN_LTCREATEDATE_CREATEDATE_2 =
		"passwordHashGenerator.createDate < ?";

	public PasswordHashGeneratorPersistenceImpl() {
		Map<String, String> dbColumnNames = new HashMap<String, String>();

		dbColumnNames.put("uuid", "uuid_");

		setDBColumnNames(dbColumnNames);

		setModelClass(PasswordHashGenerator.class);

		setModelImplClass(PasswordHashGeneratorImpl.class);
		setModelPKClass(long.class);

		setTable(PasswordHashGeneratorTable.INSTANCE);
	}

	/**
	 * Caches the password hash generator in the entity cache if it is enabled.
	 *
	 * @param passwordHashGenerator the password hash generator
	 */
	@Override
	public void cacheResult(PasswordHashGenerator passwordHashGenerator) {
		entityCache.putResult(
			entityCacheEnabled, PasswordHashGeneratorImpl.class,
			passwordHashGenerator.getPrimaryKey(), passwordHashGenerator);

		passwordHashGenerator.resetOriginalValues();
	}

	/**
	 * Caches the password hash generators in the entity cache if it is enabled.
	 *
	 * @param passwordHashGenerators the password hash generators
	 */
	@Override
	public void cacheResult(
		List<PasswordHashGenerator> passwordHashGenerators) {

		for (PasswordHashGenerator passwordHashGenerator :
				passwordHashGenerators) {

			if (entityCache.getResult(
					entityCacheEnabled, PasswordHashGeneratorImpl.class,
					passwordHashGenerator.getPrimaryKey()) == null) {

				cacheResult(passwordHashGenerator);
			}
			else {
				passwordHashGenerator.resetOriginalValues();
			}
		}
	}

	/**
	 * Clears the cache for all password hash generators.
	 *
	 * <p>
	 * The <code>EntityCache</code> and <code>FinderCache</code> are both cleared by this method.
	 * </p>
	 */
	@Override
	public void clearCache() {
		entityCache.clearCache(PasswordHashGeneratorImpl.class);

		finderCache.clearCache(FINDER_CLASS_NAME_ENTITY);
		finderCache.clearCache(FINDER_CLASS_NAME_LIST_WITH_PAGINATION);
		finderCache.clearCache(FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION);
	}

	/**
	 * Clears the cache for the password hash generator.
	 *
	 * <p>
	 * The <code>EntityCache</code> and <code>FinderCache</code> are both cleared by this method.
	 * </p>
	 */
	@Override
	public void clearCache(PasswordHashGenerator passwordHashGenerator) {
		entityCache.removeResult(
			entityCacheEnabled, PasswordHashGeneratorImpl.class,
			passwordHashGenerator.getPrimaryKey());

		finderCache.clearCache(FINDER_CLASS_NAME_LIST_WITH_PAGINATION);
		finderCache.clearCache(FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION);
	}

	@Override
	public void clearCache(List<PasswordHashGenerator> passwordHashGenerators) {
		finderCache.clearCache(FINDER_CLASS_NAME_LIST_WITH_PAGINATION);
		finderCache.clearCache(FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION);

		for (PasswordHashGenerator passwordHashGenerator :
				passwordHashGenerators) {

			entityCache.removeResult(
				entityCacheEnabled, PasswordHashGeneratorImpl.class,
				passwordHashGenerator.getPrimaryKey());
		}
	}

	public void clearCache(Set<Serializable> primaryKeys) {
		finderCache.clearCache(FINDER_CLASS_NAME_ENTITY);
		finderCache.clearCache(FINDER_CLASS_NAME_LIST_WITH_PAGINATION);
		finderCache.clearCache(FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION);

		for (Serializable primaryKey : primaryKeys) {
			entityCache.removeResult(
				entityCacheEnabled, PasswordHashGeneratorImpl.class,
				primaryKey);
		}
	}

	/**
	 * Creates a new password hash generator with the primary key. Does not add the password hash generator to the database.
	 *
	 * @param passwordHashGeneratorId the primary key for the new password hash generator
	 * @return the new password hash generator
	 */
	@Override
	public PasswordHashGenerator create(long passwordHashGeneratorId) {
		PasswordHashGenerator passwordHashGenerator =
			new PasswordHashGeneratorImpl();

		passwordHashGenerator.setNew(true);
		passwordHashGenerator.setPrimaryKey(passwordHashGeneratorId);

		String uuid = PortalUUIDUtil.generate();

		passwordHashGenerator.setUuid(uuid);

		passwordHashGenerator.setCompanyId(CompanyThreadLocal.getCompanyId());

		return passwordHashGenerator;
	}

	/**
	 * Removes the password hash generator with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param passwordHashGeneratorId the primary key of the password hash generator
	 * @return the password hash generator that was removed
	 * @throws NoSuchHashGeneratorException if a password hash generator with the primary key could not be found
	 */
	@Override
	public PasswordHashGenerator remove(long passwordHashGeneratorId)
		throws NoSuchHashGeneratorException {

		return remove((Serializable)passwordHashGeneratorId);
	}

	/**
	 * Removes the password hash generator with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param primaryKey the primary key of the password hash generator
	 * @return the password hash generator that was removed
	 * @throws NoSuchHashGeneratorException if a password hash generator with the primary key could not be found
	 */
	@Override
	public PasswordHashGenerator remove(Serializable primaryKey)
		throws NoSuchHashGeneratorException {

		Session session = null;

		try {
			session = openSession();

			PasswordHashGenerator passwordHashGenerator =
				(PasswordHashGenerator)session.get(
					PasswordHashGeneratorImpl.class, primaryKey);

			if (passwordHashGenerator == null) {
				if (_log.isDebugEnabled()) {
					_log.debug(_NO_SUCH_ENTITY_WITH_PRIMARY_KEY + primaryKey);
				}

				throw new NoSuchHashGeneratorException(
					_NO_SUCH_ENTITY_WITH_PRIMARY_KEY + primaryKey);
			}

			return remove(passwordHashGenerator);
		}
		catch (NoSuchHashGeneratorException noSuchEntityException) {
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
	protected PasswordHashGenerator removeImpl(
		PasswordHashGenerator passwordHashGenerator) {

		Session session = null;

		try {
			session = openSession();

			if (!session.contains(passwordHashGenerator)) {
				passwordHashGenerator = (PasswordHashGenerator)session.get(
					PasswordHashGeneratorImpl.class,
					passwordHashGenerator.getPrimaryKeyObj());
			}

			if (passwordHashGenerator != null) {
				session.delete(passwordHashGenerator);
			}
		}
		catch (Exception exception) {
			throw processException(exception);
		}
		finally {
			closeSession(session);
		}

		if (passwordHashGenerator != null) {
			clearCache(passwordHashGenerator);
		}

		return passwordHashGenerator;
	}

	@Override
	public PasswordHashGenerator updateImpl(
		PasswordHashGenerator passwordHashGenerator) {

		boolean isNew = passwordHashGenerator.isNew();

		if (!(passwordHashGenerator instanceof
				PasswordHashGeneratorModelImpl)) {

			InvocationHandler invocationHandler = null;

			if (ProxyUtil.isProxyClass(passwordHashGenerator.getClass())) {
				invocationHandler = ProxyUtil.getInvocationHandler(
					passwordHashGenerator);

				throw new IllegalArgumentException(
					"Implement ModelWrapper in passwordHashGenerator proxy " +
						invocationHandler.getClass());
			}

			throw new IllegalArgumentException(
				"Implement ModelWrapper in custom PasswordHashGenerator implementation " +
					passwordHashGenerator.getClass());
		}

		PasswordHashGeneratorModelImpl passwordHashGeneratorModelImpl =
			(PasswordHashGeneratorModelImpl)passwordHashGenerator;

		if (Validator.isNull(passwordHashGenerator.getUuid())) {
			String uuid = PortalUUIDUtil.generate();

			passwordHashGenerator.setUuid(uuid);
		}

		Session session = null;

		try {
			session = openSession();

			if (isNew) {
				session.save(passwordHashGenerator);

				passwordHashGenerator.setNew(false);
			}
			else {
				passwordHashGenerator = (PasswordHashGenerator)session.merge(
					passwordHashGenerator);
			}
		}
		catch (Exception exception) {
			throw processException(exception);
		}
		finally {
			closeSession(session);
		}

		finderCache.clearCache(FINDER_CLASS_NAME_LIST_WITH_PAGINATION);

		if (!_columnBitmaskEnabled) {
			finderCache.clearCache(FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION);
		}
		else if (isNew) {
			Object[] args = new Object[] {
				passwordHashGeneratorModelImpl.getUuid()
			};

			finderCache.removeResult(_finderPathCountByUuid, args);
			finderCache.removeResult(
				_finderPathWithoutPaginationFindByUuid, args);

			args = new Object[] {
				passwordHashGeneratorModelImpl.getUuid(),
				passwordHashGeneratorModelImpl.getCompanyId()
			};

			finderCache.removeResult(_finderPathCountByUuid_C, args);
			finderCache.removeResult(
				_finderPathWithoutPaginationFindByUuid_C, args);

			finderCache.removeResult(_finderPathCountAll, FINDER_ARGS_EMPTY);
			finderCache.removeResult(
				_finderPathWithoutPaginationFindAll, FINDER_ARGS_EMPTY);
		}
		else {
			if ((passwordHashGeneratorModelImpl.getColumnBitmask() &
				 _finderPathWithoutPaginationFindByUuid.getColumnBitmask()) !=
					 0) {

				Object[] args = new Object[] {
					passwordHashGeneratorModelImpl.getOriginalUuid()
				};

				finderCache.removeResult(_finderPathCountByUuid, args);
				finderCache.removeResult(
					_finderPathWithoutPaginationFindByUuid, args);

				args = new Object[] {passwordHashGeneratorModelImpl.getUuid()};

				finderCache.removeResult(_finderPathCountByUuid, args);
				finderCache.removeResult(
					_finderPathWithoutPaginationFindByUuid, args);
			}

			if ((passwordHashGeneratorModelImpl.getColumnBitmask() &
				 _finderPathWithoutPaginationFindByUuid_C.getColumnBitmask()) !=
					 0) {

				Object[] args = new Object[] {
					passwordHashGeneratorModelImpl.getOriginalUuid(),
					passwordHashGeneratorModelImpl.getOriginalCompanyId()
				};

				finderCache.removeResult(_finderPathCountByUuid_C, args);
				finderCache.removeResult(
					_finderPathWithoutPaginationFindByUuid_C, args);

				args = new Object[] {
					passwordHashGeneratorModelImpl.getUuid(),
					passwordHashGeneratorModelImpl.getCompanyId()
				};

				finderCache.removeResult(_finderPathCountByUuid_C, args);
				finderCache.removeResult(
					_finderPathWithoutPaginationFindByUuid_C, args);
			}
		}

		entityCache.putResult(
			entityCacheEnabled, PasswordHashGeneratorImpl.class,
			passwordHashGenerator.getPrimaryKey(), passwordHashGenerator,
			false);

		passwordHashGenerator.resetOriginalValues();

		return passwordHashGenerator;
	}

	/**
	 * Returns the password hash generator with the primary key or throws a <code>com.liferay.portal.kernel.exception.NoSuchModelException</code> if it could not be found.
	 *
	 * @param primaryKey the primary key of the password hash generator
	 * @return the password hash generator
	 * @throws NoSuchHashGeneratorException if a password hash generator with the primary key could not be found
	 */
	@Override
	public PasswordHashGenerator findByPrimaryKey(Serializable primaryKey)
		throws NoSuchHashGeneratorException {

		PasswordHashGenerator passwordHashGenerator = fetchByPrimaryKey(
			primaryKey);

		if (passwordHashGenerator == null) {
			if (_log.isDebugEnabled()) {
				_log.debug(_NO_SUCH_ENTITY_WITH_PRIMARY_KEY + primaryKey);
			}

			throw new NoSuchHashGeneratorException(
				_NO_SUCH_ENTITY_WITH_PRIMARY_KEY + primaryKey);
		}

		return passwordHashGenerator;
	}

	/**
	 * Returns the password hash generator with the primary key or throws a <code>NoSuchHashGeneratorException</code> if it could not be found.
	 *
	 * @param passwordHashGeneratorId the primary key of the password hash generator
	 * @return the password hash generator
	 * @throws NoSuchHashGeneratorException if a password hash generator with the primary key could not be found
	 */
	@Override
	public PasswordHashGenerator findByPrimaryKey(long passwordHashGeneratorId)
		throws NoSuchHashGeneratorException {

		return findByPrimaryKey((Serializable)passwordHashGeneratorId);
	}

	/**
	 * Returns the password hash generator with the primary key or returns <code>null</code> if it could not be found.
	 *
	 * @param passwordHashGeneratorId the primary key of the password hash generator
	 * @return the password hash generator, or <code>null</code> if a password hash generator with the primary key could not be found
	 */
	@Override
	public PasswordHashGenerator fetchByPrimaryKey(
		long passwordHashGeneratorId) {

		return fetchByPrimaryKey((Serializable)passwordHashGeneratorId);
	}

	/**
	 * Returns all the password hash generators.
	 *
	 * @return the password hash generators
	 */
	@Override
	public List<PasswordHashGenerator> findAll() {
		return findAll(QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
	}

	/**
	 * Returns a range of all the password hash generators.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordHashGeneratorModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of password hash generators
	 * @param end the upper bound of the range of password hash generators (not inclusive)
	 * @return the range of password hash generators
	 */
	@Override
	public List<PasswordHashGenerator> findAll(int start, int end) {
		return findAll(start, end, null);
	}

	/**
	 * Returns an ordered range of all the password hash generators.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordHashGeneratorModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of password hash generators
	 * @param end the upper bound of the range of password hash generators (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of password hash generators
	 */
	@Override
	public List<PasswordHashGenerator> findAll(
		int start, int end,
		OrderByComparator<PasswordHashGenerator> orderByComparator) {

		return findAll(start, end, orderByComparator, true);
	}

	/**
	 * Returns an ordered range of all the password hash generators.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordHashGeneratorModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of password hash generators
	 * @param end the upper bound of the range of password hash generators (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of password hash generators
	 */
	@Override
	public List<PasswordHashGenerator> findAll(
		int start, int end,
		OrderByComparator<PasswordHashGenerator> orderByComparator,
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

		List<PasswordHashGenerator> list = null;

		if (useFinderCache) {
			list = (List<PasswordHashGenerator>)finderCache.getResult(
				finderPath, finderArgs, this);
		}

		if (list == null) {
			StringBundler sb = null;
			String sql = null;

			if (orderByComparator != null) {
				sb = new StringBundler(
					2 + (orderByComparator.getOrderByFields().length * 2));

				sb.append(_SQL_SELECT_PASSWORDHASHGENERATOR);

				appendOrderByComparator(
					sb, _ORDER_BY_ENTITY_ALIAS, orderByComparator);

				sql = sb.toString();
			}
			else {
				sql = _SQL_SELECT_PASSWORDHASHGENERATOR;

				sql = sql.concat(PasswordHashGeneratorModelImpl.ORDER_BY_JPQL);
			}

			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(sql);

				list = (List<PasswordHashGenerator>)QueryUtil.list(
					query, getDialect(), start, end);

				cacheResult(list);

				if (useFinderCache) {
					finderCache.putResult(finderPath, finderArgs, list);
				}
			}
			catch (Exception exception) {
				if (useFinderCache) {
					finderCache.removeResult(finderPath, finderArgs);
				}

				throw processException(exception);
			}
			finally {
				closeSession(session);
			}
		}

		return list;
	}

	/**
	 * Removes all the password hash generators from the database.
	 *
	 */
	@Override
	public void removeAll() {
		for (PasswordHashGenerator passwordHashGenerator : findAll()) {
			remove(passwordHashGenerator);
		}
	}

	/**
	 * Returns the number of password hash generators.
	 *
	 * @return the number of password hash generators
	 */
	@Override
	public int countAll() {
		Long count = (Long)finderCache.getResult(
			_finderPathCountAll, FINDER_ARGS_EMPTY, this);

		if (count == null) {
			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(
					_SQL_COUNT_PASSWORDHASHGENERATOR);

				count = (Long)query.uniqueResult();

				finderCache.putResult(
					_finderPathCountAll, FINDER_ARGS_EMPTY, count);
			}
			catch (Exception exception) {
				finderCache.removeResult(
					_finderPathCountAll, FINDER_ARGS_EMPTY);

				throw processException(exception);
			}
			finally {
				closeSession(session);
			}
		}

		return count.intValue();
	}

	@Override
	public Set<String> getBadColumnNames() {
		return _badColumnNames;
	}

	@Override
	protected EntityCache getEntityCache() {
		return entityCache;
	}

	@Override
	protected String getPKDBName() {
		return "passwordHashGeneratorId";
	}

	@Override
	protected String getSelectSQL() {
		return _SQL_SELECT_PASSWORDHASHGENERATOR;
	}

	@Override
	protected Map<String, Integer> getTableColumnsMap() {
		return PasswordHashGeneratorModelImpl.TABLE_COLUMNS_MAP;
	}

	/**
	 * Initializes the password hash generator persistence.
	 */
	@Activate
	public void activate() {
		PasswordHashGeneratorModelImpl.setEntityCacheEnabled(
			entityCacheEnabled);
		PasswordHashGeneratorModelImpl.setFinderCacheEnabled(
			finderCacheEnabled);

		_finderPathWithPaginationFindAll = new FinderPath(
			entityCacheEnabled, finderCacheEnabled,
			PasswordHashGeneratorImpl.class,
			FINDER_CLASS_NAME_LIST_WITH_PAGINATION, "findAll", new String[0]);

		_finderPathWithoutPaginationFindAll = new FinderPath(
			entityCacheEnabled, finderCacheEnabled,
			PasswordHashGeneratorImpl.class,
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "findAll",
			new String[0]);

		_finderPathCountAll = new FinderPath(
			entityCacheEnabled, finderCacheEnabled, Long.class,
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countAll",
			new String[0]);

		_finderPathWithPaginationFindByUuid = new FinderPath(
			entityCacheEnabled, finderCacheEnabled,
			PasswordHashGeneratorImpl.class,
			FINDER_CLASS_NAME_LIST_WITH_PAGINATION, "findByUuid",
			new String[] {
				String.class.getName(), Integer.class.getName(),
				Integer.class.getName(), OrderByComparator.class.getName()
			});

		_finderPathWithoutPaginationFindByUuid = new FinderPath(
			entityCacheEnabled, finderCacheEnabled,
			PasswordHashGeneratorImpl.class,
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "findByUuid",
			new String[] {String.class.getName()},
			PasswordHashGeneratorModelImpl.UUID_COLUMN_BITMASK |
			PasswordHashGeneratorModelImpl.CREATEDATE_COLUMN_BITMASK);

		_finderPathCountByUuid = new FinderPath(
			entityCacheEnabled, finderCacheEnabled, Long.class,
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countByUuid",
			new String[] {String.class.getName()});

		_finderPathWithPaginationFindByUuid_C = new FinderPath(
			entityCacheEnabled, finderCacheEnabled,
			PasswordHashGeneratorImpl.class,
			FINDER_CLASS_NAME_LIST_WITH_PAGINATION, "findByUuid_C",
			new String[] {
				String.class.getName(), Long.class.getName(),
				Integer.class.getName(), Integer.class.getName(),
				OrderByComparator.class.getName()
			});

		_finderPathWithoutPaginationFindByUuid_C = new FinderPath(
			entityCacheEnabled, finderCacheEnabled,
			PasswordHashGeneratorImpl.class,
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "findByUuid_C",
			new String[] {String.class.getName(), Long.class.getName()},
			PasswordHashGeneratorModelImpl.UUID_COLUMN_BITMASK |
			PasswordHashGeneratorModelImpl.COMPANYID_COLUMN_BITMASK |
			PasswordHashGeneratorModelImpl.CREATEDATE_COLUMN_BITMASK);

		_finderPathCountByUuid_C = new FinderPath(
			entityCacheEnabled, finderCacheEnabled, Long.class,
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countByUuid_C",
			new String[] {String.class.getName(), Long.class.getName()});

		_finderPathWithPaginationFindByLtCreateDate = new FinderPath(
			entityCacheEnabled, finderCacheEnabled,
			PasswordHashGeneratorImpl.class,
			FINDER_CLASS_NAME_LIST_WITH_PAGINATION, "findByLtCreateDate",
			new String[] {
				Date.class.getName(), Integer.class.getName(),
				Integer.class.getName(), OrderByComparator.class.getName()
			});

		_finderPathWithPaginationCountByLtCreateDate = new FinderPath(
			entityCacheEnabled, finderCacheEnabled, Long.class,
			FINDER_CLASS_NAME_LIST_WITH_PAGINATION, "countByLtCreateDate",
			new String[] {Date.class.getName()});
	}

	@Deactivate
	public void deactivate() {
		entityCache.removeCache(PasswordHashGeneratorImpl.class.getName());

		finderCache.removeCache(FINDER_CLASS_NAME_ENTITY);
		finderCache.removeCache(FINDER_CLASS_NAME_LIST_WITH_PAGINATION);
		finderCache.removeCache(FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION);
	}

	@Override
	@Reference(
		target = PasswordPersistenceConstants.SERVICE_CONFIGURATION_FILTER,
		unbind = "-"
	)
	public void setConfiguration(Configuration configuration) {
		super.setConfiguration(configuration);

		_columnBitmaskEnabled = GetterUtil.getBoolean(
			configuration.get(
				"value.object.column.bitmask.enabled.com.liferay.portal.security.password.model.PasswordHashGenerator"),
			true);
	}

	@Override
	@Reference(
		target = PasswordPersistenceConstants.ORIGIN_BUNDLE_SYMBOLIC_NAME_FILTER,
		unbind = "-"
	)
	public void setDataSource(DataSource dataSource) {
		super.setDataSource(dataSource);
	}

	@Override
	@Reference(
		target = PasswordPersistenceConstants.ORIGIN_BUNDLE_SYMBOLIC_NAME_FILTER,
		unbind = "-"
	)
	public void setSessionFactory(SessionFactory sessionFactory) {
		super.setSessionFactory(sessionFactory);
	}

	private boolean _columnBitmaskEnabled;

	@Reference
	protected EntityCache entityCache;

	@Reference
	protected FinderCache finderCache;

	private static Long _getTime(Date date) {
		if (date == null) {
			return null;
		}

		return date.getTime();
	}

	private static final String _SQL_SELECT_PASSWORDHASHGENERATOR =
		"SELECT passwordHashGenerator FROM PasswordHashGenerator passwordHashGenerator";

	private static final String _SQL_SELECT_PASSWORDHASHGENERATOR_WHERE =
		"SELECT passwordHashGenerator FROM PasswordHashGenerator passwordHashGenerator WHERE ";

	private static final String _SQL_COUNT_PASSWORDHASHGENERATOR =
		"SELECT COUNT(passwordHashGenerator) FROM PasswordHashGenerator passwordHashGenerator";

	private static final String _SQL_COUNT_PASSWORDHASHGENERATOR_WHERE =
		"SELECT COUNT(passwordHashGenerator) FROM PasswordHashGenerator passwordHashGenerator WHERE ";

	private static final String _ORDER_BY_ENTITY_ALIAS =
		"passwordHashGenerator.";

	private static final String _NO_SUCH_ENTITY_WITH_PRIMARY_KEY =
		"No PasswordHashGenerator exists with the primary key ";

	private static final String _NO_SUCH_ENTITY_WITH_KEY =
		"No PasswordHashGenerator exists with the key {";

	private static final Log _log = LogFactoryUtil.getLog(
		PasswordHashGeneratorPersistenceImpl.class);

	private static final Set<String> _badColumnNames = SetUtil.fromArray(
		new String[] {"uuid"});

	static {
		try {
			Class.forName(PasswordPersistenceConstants.class.getName());
		}
		catch (ClassNotFoundException classNotFoundException) {
			throw new ExceptionInInitializerError(classNotFoundException);
		}
	}

}