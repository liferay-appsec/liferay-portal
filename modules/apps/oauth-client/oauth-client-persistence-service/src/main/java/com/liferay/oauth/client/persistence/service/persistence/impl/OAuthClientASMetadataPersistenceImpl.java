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

package com.liferay.oauth.client.persistence.service.persistence.impl;

import com.liferay.oauth.client.persistence.exception.NoSuchASMetadataException;
import com.liferay.oauth.client.persistence.model.OAuthClientASMetadata;
import com.liferay.oauth.client.persistence.model.OAuthClientASMetadataTable;
import com.liferay.oauth.client.persistence.model.impl.OAuthClientASMetadataImpl;
import com.liferay.oauth.client.persistence.model.impl.OAuthClientASMetadataModelImpl;
import com.liferay.oauth.client.persistence.service.persistence.OAuthClientASMetadataPersistence;
import com.liferay.oauth.client.persistence.service.persistence.OAuthClientASMetadataUtil;
import com.liferay.oauth.client.persistence.service.persistence.impl.constants.OAuthClientPersistenceConstants;
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
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.service.persistence.BasePersistence;
import com.liferay.portal.kernel.service.persistence.impl.BasePersistenceImpl;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.ProxyUtil;

import java.io.Serializable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;

import java.util.Date;
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
 * The persistence implementation for the o auth client as metadata service.
 *
 * <p>
 * Caching information and settings can be found in <code>portal.properties</code>
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @generated
 */
@Component(
	service = {OAuthClientASMetadataPersistence.class, BasePersistence.class}
)
public class OAuthClientASMetadataPersistenceImpl
	extends BasePersistenceImpl<OAuthClientASMetadata>
	implements OAuthClientASMetadataPersistence {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this class directly. Always use <code>OAuthClientASMetadataUtil</code> to access the o auth client as metadata persistence. Modify <code>service.xml</code> and rerun ServiceBuilder to regenerate this class.
	 */
	public static final String FINDER_CLASS_NAME_ENTITY =
		OAuthClientASMetadataImpl.class.getName();

	public static final String FINDER_CLASS_NAME_LIST_WITH_PAGINATION =
		FINDER_CLASS_NAME_ENTITY + ".List1";

	public static final String FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION =
		FINDER_CLASS_NAME_ENTITY + ".List2";

	private FinderPath _finderPathWithPaginationFindAll;
	private FinderPath _finderPathWithoutPaginationFindAll;
	private FinderPath _finderPathCountAll;
	private FinderPath _finderPathWithPaginationFindByCompanyId;
	private FinderPath _finderPathWithoutPaginationFindByCompanyId;
	private FinderPath _finderPathCountByCompanyId;

	/**
	 * Returns all the o auth client as metadatas where companyId = &#63;.
	 *
	 * @param companyId the company ID
	 * @return the matching o auth client as metadatas
	 */
	@Override
	public List<OAuthClientASMetadata> findByCompanyId(long companyId) {
		return findByCompanyId(
			companyId, QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
	}

	/**
	 * Returns a range of all the o auth client as metadatas where companyId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>OAuthClientASMetadataModelImpl</code>.
	 * </p>
	 *
	 * @param companyId the company ID
	 * @param start the lower bound of the range of o auth client as metadatas
	 * @param end the upper bound of the range of o auth client as metadatas (not inclusive)
	 * @return the range of matching o auth client as metadatas
	 */
	@Override
	public List<OAuthClientASMetadata> findByCompanyId(
		long companyId, int start, int end) {

		return findByCompanyId(companyId, start, end, null);
	}

	/**
	 * Returns an ordered range of all the o auth client as metadatas where companyId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>OAuthClientASMetadataModelImpl</code>.
	 * </p>
	 *
	 * @param companyId the company ID
	 * @param start the lower bound of the range of o auth client as metadatas
	 * @param end the upper bound of the range of o auth client as metadatas (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching o auth client as metadatas
	 */
	@Override
	public List<OAuthClientASMetadata> findByCompanyId(
		long companyId, int start, int end,
		OrderByComparator<OAuthClientASMetadata> orderByComparator) {

		return findByCompanyId(companyId, start, end, orderByComparator, true);
	}

	/**
	 * Returns an ordered range of all the o auth client as metadatas where companyId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>OAuthClientASMetadataModelImpl</code>.
	 * </p>
	 *
	 * @param companyId the company ID
	 * @param start the lower bound of the range of o auth client as metadatas
	 * @param end the upper bound of the range of o auth client as metadatas (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching o auth client as metadatas
	 */
	@Override
	public List<OAuthClientASMetadata> findByCompanyId(
		long companyId, int start, int end,
		OrderByComparator<OAuthClientASMetadata> orderByComparator,
		boolean useFinderCache) {

		FinderPath finderPath = null;
		Object[] finderArgs = null;

		if ((start == QueryUtil.ALL_POS) && (end == QueryUtil.ALL_POS) &&
			(orderByComparator == null)) {

			if (useFinderCache) {
				finderPath = _finderPathWithoutPaginationFindByCompanyId;
				finderArgs = new Object[] {companyId};
			}
		}
		else if (useFinderCache) {
			finderPath = _finderPathWithPaginationFindByCompanyId;
			finderArgs = new Object[] {
				companyId, start, end, orderByComparator
			};
		}

		List<OAuthClientASMetadata> list = null;

		if (useFinderCache) {
			list = (List<OAuthClientASMetadata>)finderCache.getResult(
				finderPath, finderArgs);

			if ((list != null) && !list.isEmpty()) {
				for (OAuthClientASMetadata oAuthClientASMetadata : list) {
					if (companyId != oAuthClientASMetadata.getCompanyId()) {
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

			sb.append(_SQL_SELECT_OAUTHCLIENTASMETADATA_WHERE);

			sb.append(_FINDER_COLUMN_COMPANYID_COMPANYID_2);

			if (orderByComparator != null) {
				appendOrderByComparator(
					sb, _ORDER_BY_ENTITY_ALIAS, orderByComparator);
			}
			else {
				sb.append(OAuthClientASMetadataModelImpl.ORDER_BY_JPQL);
			}

			String sql = sb.toString();

			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(sql);

				QueryPos queryPos = QueryPos.getInstance(query);

				queryPos.add(companyId);

				list = (List<OAuthClientASMetadata>)QueryUtil.list(
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
	 * Returns the first o auth client as metadata in the ordered set where companyId = &#63;.
	 *
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching o auth client as metadata
	 * @throws NoSuchASMetadataException if a matching o auth client as metadata could not be found
	 */
	@Override
	public OAuthClientASMetadata findByCompanyId_First(
			long companyId,
			OrderByComparator<OAuthClientASMetadata> orderByComparator)
		throws NoSuchASMetadataException {

		OAuthClientASMetadata oAuthClientASMetadata = fetchByCompanyId_First(
			companyId, orderByComparator);

		if (oAuthClientASMetadata != null) {
			return oAuthClientASMetadata;
		}

		StringBundler sb = new StringBundler(4);

		sb.append(_NO_SUCH_ENTITY_WITH_KEY);

		sb.append("companyId=");
		sb.append(companyId);

		sb.append("}");

		throw new NoSuchASMetadataException(sb.toString());
	}

	/**
	 * Returns the first o auth client as metadata in the ordered set where companyId = &#63;.
	 *
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching o auth client as metadata, or <code>null</code> if a matching o auth client as metadata could not be found
	 */
	@Override
	public OAuthClientASMetadata fetchByCompanyId_First(
		long companyId,
		OrderByComparator<OAuthClientASMetadata> orderByComparator) {

		List<OAuthClientASMetadata> list = findByCompanyId(
			companyId, 0, 1, orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the last o auth client as metadata in the ordered set where companyId = &#63;.
	 *
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching o auth client as metadata
	 * @throws NoSuchASMetadataException if a matching o auth client as metadata could not be found
	 */
	@Override
	public OAuthClientASMetadata findByCompanyId_Last(
			long companyId,
			OrderByComparator<OAuthClientASMetadata> orderByComparator)
		throws NoSuchASMetadataException {

		OAuthClientASMetadata oAuthClientASMetadata = fetchByCompanyId_Last(
			companyId, orderByComparator);

		if (oAuthClientASMetadata != null) {
			return oAuthClientASMetadata;
		}

		StringBundler sb = new StringBundler(4);

		sb.append(_NO_SUCH_ENTITY_WITH_KEY);

		sb.append("companyId=");
		sb.append(companyId);

		sb.append("}");

		throw new NoSuchASMetadataException(sb.toString());
	}

	/**
	 * Returns the last o auth client as metadata in the ordered set where companyId = &#63;.
	 *
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching o auth client as metadata, or <code>null</code> if a matching o auth client as metadata could not be found
	 */
	@Override
	public OAuthClientASMetadata fetchByCompanyId_Last(
		long companyId,
		OrderByComparator<OAuthClientASMetadata> orderByComparator) {

		int count = countByCompanyId(companyId);

		if (count == 0) {
			return null;
		}

		List<OAuthClientASMetadata> list = findByCompanyId(
			companyId, count - 1, count, orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the o auth client as metadatas before and after the current o auth client as metadata in the ordered set where companyId = &#63;.
	 *
	 * @param oAuthClientASMetadataId the primary key of the current o auth client as metadata
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next o auth client as metadata
	 * @throws NoSuchASMetadataException if a o auth client as metadata with the primary key could not be found
	 */
	@Override
	public OAuthClientASMetadata[] findByCompanyId_PrevAndNext(
			long oAuthClientASMetadataId, long companyId,
			OrderByComparator<OAuthClientASMetadata> orderByComparator)
		throws NoSuchASMetadataException {

		OAuthClientASMetadata oAuthClientASMetadata = findByPrimaryKey(
			oAuthClientASMetadataId);

		Session session = null;

		try {
			session = openSession();

			OAuthClientASMetadata[] array = new OAuthClientASMetadataImpl[3];

			array[0] = getByCompanyId_PrevAndNext(
				session, oAuthClientASMetadata, companyId, orderByComparator,
				true);

			array[1] = oAuthClientASMetadata;

			array[2] = getByCompanyId_PrevAndNext(
				session, oAuthClientASMetadata, companyId, orderByComparator,
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

	protected OAuthClientASMetadata getByCompanyId_PrevAndNext(
		Session session, OAuthClientASMetadata oAuthClientASMetadata,
		long companyId,
		OrderByComparator<OAuthClientASMetadata> orderByComparator,
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

		sb.append(_SQL_SELECT_OAUTHCLIENTASMETADATA_WHERE);

		sb.append(_FINDER_COLUMN_COMPANYID_COMPANYID_2);

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
			sb.append(OAuthClientASMetadataModelImpl.ORDER_BY_JPQL);
		}

		String sql = sb.toString();

		Query query = session.createQuery(sql);

		query.setFirstResult(0);
		query.setMaxResults(2);

		QueryPos queryPos = QueryPos.getInstance(query);

		queryPos.add(companyId);

		if (orderByComparator != null) {
			for (Object orderByConditionValue :
					orderByComparator.getOrderByConditionValues(
						oAuthClientASMetadata)) {

				queryPos.add(orderByConditionValue);
			}
		}

		List<OAuthClientASMetadata> list = query.list();

		if (list.size() == 2) {
			return list.get(1);
		}
		else {
			return null;
		}
	}

	/**
	 * Removes all the o auth client as metadatas where companyId = &#63; from the database.
	 *
	 * @param companyId the company ID
	 */
	@Override
	public void removeByCompanyId(long companyId) {
		for (OAuthClientASMetadata oAuthClientASMetadata :
				findByCompanyId(
					companyId, QueryUtil.ALL_POS, QueryUtil.ALL_POS, null)) {

			remove(oAuthClientASMetadata);
		}
	}

	/**
	 * Returns the number of o auth client as metadatas where companyId = &#63;.
	 *
	 * @param companyId the company ID
	 * @return the number of matching o auth client as metadatas
	 */
	@Override
	public int countByCompanyId(long companyId) {
		FinderPath finderPath = _finderPathCountByCompanyId;

		Object[] finderArgs = new Object[] {companyId};

		Long count = (Long)finderCache.getResult(finderPath, finderArgs);

		if (count == null) {
			StringBundler sb = new StringBundler(2);

			sb.append(_SQL_COUNT_OAUTHCLIENTASMETADATA_WHERE);

			sb.append(_FINDER_COLUMN_COMPANYID_COMPANYID_2);

			String sql = sb.toString();

			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(sql);

				QueryPos queryPos = QueryPos.getInstance(query);

				queryPos.add(companyId);

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

	private static final String _FINDER_COLUMN_COMPANYID_COMPANYID_2 =
		"oAuthClientASMetadata.companyId = ?";

	private FinderPath _finderPathWithPaginationFindByC_U;
	private FinderPath _finderPathWithoutPaginationFindByC_U;
	private FinderPath _finderPathCountByC_U;

	/**
	 * Returns all the o auth client as metadatas where companyId = &#63; and userId = &#63;.
	 *
	 * @param companyId the company ID
	 * @param userId the user ID
	 * @return the matching o auth client as metadatas
	 */
	@Override
	public List<OAuthClientASMetadata> findByC_U(long companyId, long userId) {
		return findByC_U(
			companyId, userId, QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
	}

	/**
	 * Returns a range of all the o auth client as metadatas where companyId = &#63; and userId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>OAuthClientASMetadataModelImpl</code>.
	 * </p>
	 *
	 * @param companyId the company ID
	 * @param userId the user ID
	 * @param start the lower bound of the range of o auth client as metadatas
	 * @param end the upper bound of the range of o auth client as metadatas (not inclusive)
	 * @return the range of matching o auth client as metadatas
	 */
	@Override
	public List<OAuthClientASMetadata> findByC_U(
		long companyId, long userId, int start, int end) {

		return findByC_U(companyId, userId, start, end, null);
	}

	/**
	 * Returns an ordered range of all the o auth client as metadatas where companyId = &#63; and userId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>OAuthClientASMetadataModelImpl</code>.
	 * </p>
	 *
	 * @param companyId the company ID
	 * @param userId the user ID
	 * @param start the lower bound of the range of o auth client as metadatas
	 * @param end the upper bound of the range of o auth client as metadatas (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching o auth client as metadatas
	 */
	@Override
	public List<OAuthClientASMetadata> findByC_U(
		long companyId, long userId, int start, int end,
		OrderByComparator<OAuthClientASMetadata> orderByComparator) {

		return findByC_U(
			companyId, userId, start, end, orderByComparator, true);
	}

	/**
	 * Returns an ordered range of all the o auth client as metadatas where companyId = &#63; and userId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>OAuthClientASMetadataModelImpl</code>.
	 * </p>
	 *
	 * @param companyId the company ID
	 * @param userId the user ID
	 * @param start the lower bound of the range of o auth client as metadatas
	 * @param end the upper bound of the range of o auth client as metadatas (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching o auth client as metadatas
	 */
	@Override
	public List<OAuthClientASMetadata> findByC_U(
		long companyId, long userId, int start, int end,
		OrderByComparator<OAuthClientASMetadata> orderByComparator,
		boolean useFinderCache) {

		FinderPath finderPath = null;
		Object[] finderArgs = null;

		if ((start == QueryUtil.ALL_POS) && (end == QueryUtil.ALL_POS) &&
			(orderByComparator == null)) {

			if (useFinderCache) {
				finderPath = _finderPathWithoutPaginationFindByC_U;
				finderArgs = new Object[] {companyId, userId};
			}
		}
		else if (useFinderCache) {
			finderPath = _finderPathWithPaginationFindByC_U;
			finderArgs = new Object[] {
				companyId, userId, start, end, orderByComparator
			};
		}

		List<OAuthClientASMetadata> list = null;

		if (useFinderCache) {
			list = (List<OAuthClientASMetadata>)finderCache.getResult(
				finderPath, finderArgs);

			if ((list != null) && !list.isEmpty()) {
				for (OAuthClientASMetadata oAuthClientASMetadata : list) {
					if ((companyId != oAuthClientASMetadata.getCompanyId()) ||
						(userId != oAuthClientASMetadata.getUserId())) {

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

			sb.append(_SQL_SELECT_OAUTHCLIENTASMETADATA_WHERE);

			sb.append(_FINDER_COLUMN_C_U_COMPANYID_2);

			sb.append(_FINDER_COLUMN_C_U_USERID_2);

			if (orderByComparator != null) {
				appendOrderByComparator(
					sb, _ORDER_BY_ENTITY_ALIAS, orderByComparator);
			}
			else {
				sb.append(OAuthClientASMetadataModelImpl.ORDER_BY_JPQL);
			}

			String sql = sb.toString();

			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(sql);

				QueryPos queryPos = QueryPos.getInstance(query);

				queryPos.add(companyId);

				queryPos.add(userId);

				list = (List<OAuthClientASMetadata>)QueryUtil.list(
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
	 * Returns the first o auth client as metadata in the ordered set where companyId = &#63; and userId = &#63;.
	 *
	 * @param companyId the company ID
	 * @param userId the user ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching o auth client as metadata
	 * @throws NoSuchASMetadataException if a matching o auth client as metadata could not be found
	 */
	@Override
	public OAuthClientASMetadata findByC_U_First(
			long companyId, long userId,
			OrderByComparator<OAuthClientASMetadata> orderByComparator)
		throws NoSuchASMetadataException {

		OAuthClientASMetadata oAuthClientASMetadata = fetchByC_U_First(
			companyId, userId, orderByComparator);

		if (oAuthClientASMetadata != null) {
			return oAuthClientASMetadata;
		}

		StringBundler sb = new StringBundler(6);

		sb.append(_NO_SUCH_ENTITY_WITH_KEY);

		sb.append("companyId=");
		sb.append(companyId);

		sb.append(", userId=");
		sb.append(userId);

		sb.append("}");

		throw new NoSuchASMetadataException(sb.toString());
	}

	/**
	 * Returns the first o auth client as metadata in the ordered set where companyId = &#63; and userId = &#63;.
	 *
	 * @param companyId the company ID
	 * @param userId the user ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching o auth client as metadata, or <code>null</code> if a matching o auth client as metadata could not be found
	 */
	@Override
	public OAuthClientASMetadata fetchByC_U_First(
		long companyId, long userId,
		OrderByComparator<OAuthClientASMetadata> orderByComparator) {

		List<OAuthClientASMetadata> list = findByC_U(
			companyId, userId, 0, 1, orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the last o auth client as metadata in the ordered set where companyId = &#63; and userId = &#63;.
	 *
	 * @param companyId the company ID
	 * @param userId the user ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching o auth client as metadata
	 * @throws NoSuchASMetadataException if a matching o auth client as metadata could not be found
	 */
	@Override
	public OAuthClientASMetadata findByC_U_Last(
			long companyId, long userId,
			OrderByComparator<OAuthClientASMetadata> orderByComparator)
		throws NoSuchASMetadataException {

		OAuthClientASMetadata oAuthClientASMetadata = fetchByC_U_Last(
			companyId, userId, orderByComparator);

		if (oAuthClientASMetadata != null) {
			return oAuthClientASMetadata;
		}

		StringBundler sb = new StringBundler(6);

		sb.append(_NO_SUCH_ENTITY_WITH_KEY);

		sb.append("companyId=");
		sb.append(companyId);

		sb.append(", userId=");
		sb.append(userId);

		sb.append("}");

		throw new NoSuchASMetadataException(sb.toString());
	}

	/**
	 * Returns the last o auth client as metadata in the ordered set where companyId = &#63; and userId = &#63;.
	 *
	 * @param companyId the company ID
	 * @param userId the user ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching o auth client as metadata, or <code>null</code> if a matching o auth client as metadata could not be found
	 */
	@Override
	public OAuthClientASMetadata fetchByC_U_Last(
		long companyId, long userId,
		OrderByComparator<OAuthClientASMetadata> orderByComparator) {

		int count = countByC_U(companyId, userId);

		if (count == 0) {
			return null;
		}

		List<OAuthClientASMetadata> list = findByC_U(
			companyId, userId, count - 1, count, orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the o auth client as metadatas before and after the current o auth client as metadata in the ordered set where companyId = &#63; and userId = &#63;.
	 *
	 * @param oAuthClientASMetadataId the primary key of the current o auth client as metadata
	 * @param companyId the company ID
	 * @param userId the user ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next o auth client as metadata
	 * @throws NoSuchASMetadataException if a o auth client as metadata with the primary key could not be found
	 */
	@Override
	public OAuthClientASMetadata[] findByC_U_PrevAndNext(
			long oAuthClientASMetadataId, long companyId, long userId,
			OrderByComparator<OAuthClientASMetadata> orderByComparator)
		throws NoSuchASMetadataException {

		OAuthClientASMetadata oAuthClientASMetadata = findByPrimaryKey(
			oAuthClientASMetadataId);

		Session session = null;

		try {
			session = openSession();

			OAuthClientASMetadata[] array = new OAuthClientASMetadataImpl[3];

			array[0] = getByC_U_PrevAndNext(
				session, oAuthClientASMetadata, companyId, userId,
				orderByComparator, true);

			array[1] = oAuthClientASMetadata;

			array[2] = getByC_U_PrevAndNext(
				session, oAuthClientASMetadata, companyId, userId,
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

	protected OAuthClientASMetadata getByC_U_PrevAndNext(
		Session session, OAuthClientASMetadata oAuthClientASMetadata,
		long companyId, long userId,
		OrderByComparator<OAuthClientASMetadata> orderByComparator,
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

		sb.append(_SQL_SELECT_OAUTHCLIENTASMETADATA_WHERE);

		sb.append(_FINDER_COLUMN_C_U_COMPANYID_2);

		sb.append(_FINDER_COLUMN_C_U_USERID_2);

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
			sb.append(OAuthClientASMetadataModelImpl.ORDER_BY_JPQL);
		}

		String sql = sb.toString();

		Query query = session.createQuery(sql);

		query.setFirstResult(0);
		query.setMaxResults(2);

		QueryPos queryPos = QueryPos.getInstance(query);

		queryPos.add(companyId);

		queryPos.add(userId);

		if (orderByComparator != null) {
			for (Object orderByConditionValue :
					orderByComparator.getOrderByConditionValues(
						oAuthClientASMetadata)) {

				queryPos.add(orderByConditionValue);
			}
		}

		List<OAuthClientASMetadata> list = query.list();

		if (list.size() == 2) {
			return list.get(1);
		}
		else {
			return null;
		}
	}

	/**
	 * Removes all the o auth client as metadatas where companyId = &#63; and userId = &#63; from the database.
	 *
	 * @param companyId the company ID
	 * @param userId the user ID
	 */
	@Override
	public void removeByC_U(long companyId, long userId) {
		for (OAuthClientASMetadata oAuthClientASMetadata :
				findByC_U(
					companyId, userId, QueryUtil.ALL_POS, QueryUtil.ALL_POS,
					null)) {

			remove(oAuthClientASMetadata);
		}
	}

	/**
	 * Returns the number of o auth client as metadatas where companyId = &#63; and userId = &#63;.
	 *
	 * @param companyId the company ID
	 * @param userId the user ID
	 * @return the number of matching o auth client as metadatas
	 */
	@Override
	public int countByC_U(long companyId, long userId) {
		FinderPath finderPath = _finderPathCountByC_U;

		Object[] finderArgs = new Object[] {companyId, userId};

		Long count = (Long)finderCache.getResult(finderPath, finderArgs);

		if (count == null) {
			StringBundler sb = new StringBundler(3);

			sb.append(_SQL_COUNT_OAUTHCLIENTASMETADATA_WHERE);

			sb.append(_FINDER_COLUMN_C_U_COMPANYID_2);

			sb.append(_FINDER_COLUMN_C_U_USERID_2);

			String sql = sb.toString();

			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(sql);

				QueryPos queryPos = QueryPos.getInstance(query);

				queryPos.add(companyId);

				queryPos.add(userId);

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

	private static final String _FINDER_COLUMN_C_U_COMPANYID_2 =
		"oAuthClientASMetadata.companyId = ? AND ";

	private static final String _FINDER_COLUMN_C_U_USERID_2 =
		"oAuthClientASMetadata.userId = ?";

	private FinderPath _finderPathFetchByC_I;
	private FinderPath _finderPathCountByC_I;

	/**
	 * Returns the o auth client as metadata where companyId = &#63; and issuer = &#63; or throws a <code>NoSuchASMetadataException</code> if it could not be found.
	 *
	 * @param companyId the company ID
	 * @param issuer the issuer
	 * @return the matching o auth client as metadata
	 * @throws NoSuchASMetadataException if a matching o auth client as metadata could not be found
	 */
	@Override
	public OAuthClientASMetadata findByC_I(long companyId, String issuer)
		throws NoSuchASMetadataException {

		OAuthClientASMetadata oAuthClientASMetadata = fetchByC_I(
			companyId, issuer);

		if (oAuthClientASMetadata == null) {
			StringBundler sb = new StringBundler(6);

			sb.append(_NO_SUCH_ENTITY_WITH_KEY);

			sb.append("companyId=");
			sb.append(companyId);

			sb.append(", issuer=");
			sb.append(issuer);

			sb.append("}");

			if (_log.isDebugEnabled()) {
				_log.debug(sb.toString());
			}

			throw new NoSuchASMetadataException(sb.toString());
		}

		return oAuthClientASMetadata;
	}

	/**
	 * Returns the o auth client as metadata where companyId = &#63; and issuer = &#63; or returns <code>null</code> if it could not be found. Uses the finder cache.
	 *
	 * @param companyId the company ID
	 * @param issuer the issuer
	 * @return the matching o auth client as metadata, or <code>null</code> if a matching o auth client as metadata could not be found
	 */
	@Override
	public OAuthClientASMetadata fetchByC_I(long companyId, String issuer) {
		return fetchByC_I(companyId, issuer, true);
	}

	/**
	 * Returns the o auth client as metadata where companyId = &#63; and issuer = &#63; or returns <code>null</code> if it could not be found, optionally using the finder cache.
	 *
	 * @param companyId the company ID
	 * @param issuer the issuer
	 * @param useFinderCache whether to use the finder cache
	 * @return the matching o auth client as metadata, or <code>null</code> if a matching o auth client as metadata could not be found
	 */
	@Override
	public OAuthClientASMetadata fetchByC_I(
		long companyId, String issuer, boolean useFinderCache) {

		issuer = Objects.toString(issuer, "");

		Object[] finderArgs = null;

		if (useFinderCache) {
			finderArgs = new Object[] {companyId, issuer};
		}

		Object result = null;

		if (useFinderCache) {
			result = finderCache.getResult(_finderPathFetchByC_I, finderArgs);
		}

		if (result instanceof OAuthClientASMetadata) {
			OAuthClientASMetadata oAuthClientASMetadata =
				(OAuthClientASMetadata)result;

			if ((companyId != oAuthClientASMetadata.getCompanyId()) ||
				!Objects.equals(issuer, oAuthClientASMetadata.getIssuer())) {

				result = null;
			}
		}

		if (result == null) {
			StringBundler sb = new StringBundler(4);

			sb.append(_SQL_SELECT_OAUTHCLIENTASMETADATA_WHERE);

			sb.append(_FINDER_COLUMN_C_I_COMPANYID_2);

			boolean bindIssuer = false;

			if (issuer.isEmpty()) {
				sb.append(_FINDER_COLUMN_C_I_ISSUER_3);
			}
			else {
				bindIssuer = true;

				sb.append(_FINDER_COLUMN_C_I_ISSUER_2);
			}

			String sql = sb.toString();

			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(sql);

				QueryPos queryPos = QueryPos.getInstance(query);

				queryPos.add(companyId);

				if (bindIssuer) {
					queryPos.add(issuer);
				}

				List<OAuthClientASMetadata> list = query.list();

				if (list.isEmpty()) {
					if (useFinderCache) {
						finderCache.putResult(
							_finderPathFetchByC_I, finderArgs, list);
					}
				}
				else {
					OAuthClientASMetadata oAuthClientASMetadata = list.get(0);

					result = oAuthClientASMetadata;

					cacheResult(oAuthClientASMetadata);
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
			return (OAuthClientASMetadata)result;
		}
	}

	/**
	 * Removes the o auth client as metadata where companyId = &#63; and issuer = &#63; from the database.
	 *
	 * @param companyId the company ID
	 * @param issuer the issuer
	 * @return the o auth client as metadata that was removed
	 */
	@Override
	public OAuthClientASMetadata removeByC_I(long companyId, String issuer)
		throws NoSuchASMetadataException {

		OAuthClientASMetadata oAuthClientASMetadata = findByC_I(
			companyId, issuer);

		return remove(oAuthClientASMetadata);
	}

	/**
	 * Returns the number of o auth client as metadatas where companyId = &#63; and issuer = &#63;.
	 *
	 * @param companyId the company ID
	 * @param issuer the issuer
	 * @return the number of matching o auth client as metadatas
	 */
	@Override
	public int countByC_I(long companyId, String issuer) {
		issuer = Objects.toString(issuer, "");

		FinderPath finderPath = _finderPathCountByC_I;

		Object[] finderArgs = new Object[] {companyId, issuer};

		Long count = (Long)finderCache.getResult(finderPath, finderArgs);

		if (count == null) {
			StringBundler sb = new StringBundler(3);

			sb.append(_SQL_COUNT_OAUTHCLIENTASMETADATA_WHERE);

			sb.append(_FINDER_COLUMN_C_I_COMPANYID_2);

			boolean bindIssuer = false;

			if (issuer.isEmpty()) {
				sb.append(_FINDER_COLUMN_C_I_ISSUER_3);
			}
			else {
				bindIssuer = true;

				sb.append(_FINDER_COLUMN_C_I_ISSUER_2);
			}

			String sql = sb.toString();

			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(sql);

				QueryPos queryPos = QueryPos.getInstance(query);

				queryPos.add(companyId);

				if (bindIssuer) {
					queryPos.add(issuer);
				}

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

	private static final String _FINDER_COLUMN_C_I_COMPANYID_2 =
		"oAuthClientASMetadata.companyId = ? AND ";

	private static final String _FINDER_COLUMN_C_I_ISSUER_2 =
		"oAuthClientASMetadata.issuer = ?";

	private static final String _FINDER_COLUMN_C_I_ISSUER_3 =
		"(oAuthClientASMetadata.issuer IS NULL OR oAuthClientASMetadata.issuer = '')";

	public OAuthClientASMetadataPersistenceImpl() {
		setModelClass(OAuthClientASMetadata.class);

		setModelImplClass(OAuthClientASMetadataImpl.class);
		setModelPKClass(long.class);

		setTable(OAuthClientASMetadataTable.INSTANCE);
	}

	/**
	 * Caches the o auth client as metadata in the entity cache if it is enabled.
	 *
	 * @param oAuthClientASMetadata the o auth client as metadata
	 */
	@Override
	public void cacheResult(OAuthClientASMetadata oAuthClientASMetadata) {
		entityCache.putResult(
			OAuthClientASMetadataImpl.class,
			oAuthClientASMetadata.getPrimaryKey(), oAuthClientASMetadata);

		finderCache.putResult(
			_finderPathFetchByC_I,
			new Object[] {
				oAuthClientASMetadata.getCompanyId(),
				oAuthClientASMetadata.getIssuer()
			},
			oAuthClientASMetadata);
	}

	private int _valueObjectFinderCacheListThreshold;

	/**
	 * Caches the o auth client as metadatas in the entity cache if it is enabled.
	 *
	 * @param oAuthClientASMetadatas the o auth client as metadatas
	 */
	@Override
	public void cacheResult(
		List<OAuthClientASMetadata> oAuthClientASMetadatas) {

		if ((_valueObjectFinderCacheListThreshold == 0) ||
			((_valueObjectFinderCacheListThreshold > 0) &&
			 (oAuthClientASMetadatas.size() >
				 _valueObjectFinderCacheListThreshold))) {

			return;
		}

		for (OAuthClientASMetadata oAuthClientASMetadata :
				oAuthClientASMetadatas) {

			if (entityCache.getResult(
					OAuthClientASMetadataImpl.class,
					oAuthClientASMetadata.getPrimaryKey()) == null) {

				cacheResult(oAuthClientASMetadata);
			}
		}
	}

	/**
	 * Clears the cache for all o auth client as metadatas.
	 *
	 * <p>
	 * The <code>EntityCache</code> and <code>FinderCache</code> are both cleared by this method.
	 * </p>
	 */
	@Override
	public void clearCache() {
		entityCache.clearCache(OAuthClientASMetadataImpl.class);

		finderCache.clearCache(OAuthClientASMetadataImpl.class);
	}

	/**
	 * Clears the cache for the o auth client as metadata.
	 *
	 * <p>
	 * The <code>EntityCache</code> and <code>FinderCache</code> are both cleared by this method.
	 * </p>
	 */
	@Override
	public void clearCache(OAuthClientASMetadata oAuthClientASMetadata) {
		entityCache.removeResult(
			OAuthClientASMetadataImpl.class, oAuthClientASMetadata);
	}

	@Override
	public void clearCache(List<OAuthClientASMetadata> oAuthClientASMetadatas) {
		for (OAuthClientASMetadata oAuthClientASMetadata :
				oAuthClientASMetadatas) {

			entityCache.removeResult(
				OAuthClientASMetadataImpl.class, oAuthClientASMetadata);
		}
	}

	@Override
	public void clearCache(Set<Serializable> primaryKeys) {
		finderCache.clearCache(OAuthClientASMetadataImpl.class);

		for (Serializable primaryKey : primaryKeys) {
			entityCache.removeResult(
				OAuthClientASMetadataImpl.class, primaryKey);
		}
	}

	protected void cacheUniqueFindersCache(
		OAuthClientASMetadataModelImpl oAuthClientASMetadataModelImpl) {

		Object[] args = new Object[] {
			oAuthClientASMetadataModelImpl.getCompanyId(),
			oAuthClientASMetadataModelImpl.getIssuer()
		};

		finderCache.putResult(_finderPathCountByC_I, args, Long.valueOf(1));
		finderCache.putResult(
			_finderPathFetchByC_I, args, oAuthClientASMetadataModelImpl);
	}

	/**
	 * Creates a new o auth client as metadata with the primary key. Does not add the o auth client as metadata to the database.
	 *
	 * @param oAuthClientASMetadataId the primary key for the new o auth client as metadata
	 * @return the new o auth client as metadata
	 */
	@Override
	public OAuthClientASMetadata create(long oAuthClientASMetadataId) {
		OAuthClientASMetadata oAuthClientASMetadata =
			new OAuthClientASMetadataImpl();

		oAuthClientASMetadata.setNew(true);
		oAuthClientASMetadata.setPrimaryKey(oAuthClientASMetadataId);

		oAuthClientASMetadata.setCompanyId(CompanyThreadLocal.getCompanyId());

		return oAuthClientASMetadata;
	}

	/**
	 * Removes the o auth client as metadata with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param oAuthClientASMetadataId the primary key of the o auth client as metadata
	 * @return the o auth client as metadata that was removed
	 * @throws NoSuchASMetadataException if a o auth client as metadata with the primary key could not be found
	 */
	@Override
	public OAuthClientASMetadata remove(long oAuthClientASMetadataId)
		throws NoSuchASMetadataException {

		return remove((Serializable)oAuthClientASMetadataId);
	}

	/**
	 * Removes the o auth client as metadata with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param primaryKey the primary key of the o auth client as metadata
	 * @return the o auth client as metadata that was removed
	 * @throws NoSuchASMetadataException if a o auth client as metadata with the primary key could not be found
	 */
	@Override
	public OAuthClientASMetadata remove(Serializable primaryKey)
		throws NoSuchASMetadataException {

		Session session = null;

		try {
			session = openSession();

			OAuthClientASMetadata oAuthClientASMetadata =
				(OAuthClientASMetadata)session.get(
					OAuthClientASMetadataImpl.class, primaryKey);

			if (oAuthClientASMetadata == null) {
				if (_log.isDebugEnabled()) {
					_log.debug(_NO_SUCH_ENTITY_WITH_PRIMARY_KEY + primaryKey);
				}

				throw new NoSuchASMetadataException(
					_NO_SUCH_ENTITY_WITH_PRIMARY_KEY + primaryKey);
			}

			return remove(oAuthClientASMetadata);
		}
		catch (NoSuchASMetadataException noSuchEntityException) {
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
	protected OAuthClientASMetadata removeImpl(
		OAuthClientASMetadata oAuthClientASMetadata) {

		Session session = null;

		try {
			session = openSession();

			if (!session.contains(oAuthClientASMetadata)) {
				oAuthClientASMetadata = (OAuthClientASMetadata)session.get(
					OAuthClientASMetadataImpl.class,
					oAuthClientASMetadata.getPrimaryKeyObj());
			}

			if (oAuthClientASMetadata != null) {
				session.delete(oAuthClientASMetadata);
			}
		}
		catch (Exception exception) {
			throw processException(exception);
		}
		finally {
			closeSession(session);
		}

		if (oAuthClientASMetadata != null) {
			clearCache(oAuthClientASMetadata);
		}

		return oAuthClientASMetadata;
	}

	@Override
	public OAuthClientASMetadata updateImpl(
		OAuthClientASMetadata oAuthClientASMetadata) {

		boolean isNew = oAuthClientASMetadata.isNew();

		if (!(oAuthClientASMetadata instanceof
				OAuthClientASMetadataModelImpl)) {

			InvocationHandler invocationHandler = null;

			if (ProxyUtil.isProxyClass(oAuthClientASMetadata.getClass())) {
				invocationHandler = ProxyUtil.getInvocationHandler(
					oAuthClientASMetadata);

				throw new IllegalArgumentException(
					"Implement ModelWrapper in oAuthClientASMetadata proxy " +
						invocationHandler.getClass());
			}

			throw new IllegalArgumentException(
				"Implement ModelWrapper in custom OAuthClientASMetadata implementation " +
					oAuthClientASMetadata.getClass());
		}

		OAuthClientASMetadataModelImpl oAuthClientASMetadataModelImpl =
			(OAuthClientASMetadataModelImpl)oAuthClientASMetadata;

		ServiceContext serviceContext =
			ServiceContextThreadLocal.getServiceContext();

		Date date = new Date();

		if (isNew && (oAuthClientASMetadata.getCreateDate() == null)) {
			if (serviceContext == null) {
				oAuthClientASMetadata.setCreateDate(date);
			}
			else {
				oAuthClientASMetadata.setCreateDate(
					serviceContext.getCreateDate(date));
			}
		}

		if (!oAuthClientASMetadataModelImpl.hasSetModifiedDate()) {
			if (serviceContext == null) {
				oAuthClientASMetadata.setModifiedDate(date);
			}
			else {
				oAuthClientASMetadata.setModifiedDate(
					serviceContext.getModifiedDate(date));
			}
		}

		Session session = null;

		try {
			session = openSession();

			if (isNew) {
				session.save(oAuthClientASMetadata);
			}
			else {
				oAuthClientASMetadata = (OAuthClientASMetadata)session.merge(
					oAuthClientASMetadata);
			}
		}
		catch (Exception exception) {
			throw processException(exception);
		}
		finally {
			closeSession(session);
		}

		entityCache.putResult(
			OAuthClientASMetadataImpl.class, oAuthClientASMetadataModelImpl,
			false, true);

		cacheUniqueFindersCache(oAuthClientASMetadataModelImpl);

		if (isNew) {
			oAuthClientASMetadata.setNew(false);
		}

		oAuthClientASMetadata.resetOriginalValues();

		return oAuthClientASMetadata;
	}

	/**
	 * Returns the o auth client as metadata with the primary key or throws a <code>com.liferay.portal.kernel.exception.NoSuchModelException</code> if it could not be found.
	 *
	 * @param primaryKey the primary key of the o auth client as metadata
	 * @return the o auth client as metadata
	 * @throws NoSuchASMetadataException if a o auth client as metadata with the primary key could not be found
	 */
	@Override
	public OAuthClientASMetadata findByPrimaryKey(Serializable primaryKey)
		throws NoSuchASMetadataException {

		OAuthClientASMetadata oAuthClientASMetadata = fetchByPrimaryKey(
			primaryKey);

		if (oAuthClientASMetadata == null) {
			if (_log.isDebugEnabled()) {
				_log.debug(_NO_SUCH_ENTITY_WITH_PRIMARY_KEY + primaryKey);
			}

			throw new NoSuchASMetadataException(
				_NO_SUCH_ENTITY_WITH_PRIMARY_KEY + primaryKey);
		}

		return oAuthClientASMetadata;
	}

	/**
	 * Returns the o auth client as metadata with the primary key or throws a <code>NoSuchASMetadataException</code> if it could not be found.
	 *
	 * @param oAuthClientASMetadataId the primary key of the o auth client as metadata
	 * @return the o auth client as metadata
	 * @throws NoSuchASMetadataException if a o auth client as metadata with the primary key could not be found
	 */
	@Override
	public OAuthClientASMetadata findByPrimaryKey(long oAuthClientASMetadataId)
		throws NoSuchASMetadataException {

		return findByPrimaryKey((Serializable)oAuthClientASMetadataId);
	}

	/**
	 * Returns the o auth client as metadata with the primary key or returns <code>null</code> if it could not be found.
	 *
	 * @param oAuthClientASMetadataId the primary key of the o auth client as metadata
	 * @return the o auth client as metadata, or <code>null</code> if a o auth client as metadata with the primary key could not be found
	 */
	@Override
	public OAuthClientASMetadata fetchByPrimaryKey(
		long oAuthClientASMetadataId) {

		return fetchByPrimaryKey((Serializable)oAuthClientASMetadataId);
	}

	/**
	 * Returns all the o auth client as metadatas.
	 *
	 * @return the o auth client as metadatas
	 */
	@Override
	public List<OAuthClientASMetadata> findAll() {
		return findAll(QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
	}

	/**
	 * Returns a range of all the o auth client as metadatas.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>OAuthClientASMetadataModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of o auth client as metadatas
	 * @param end the upper bound of the range of o auth client as metadatas (not inclusive)
	 * @return the range of o auth client as metadatas
	 */
	@Override
	public List<OAuthClientASMetadata> findAll(int start, int end) {
		return findAll(start, end, null);
	}

	/**
	 * Returns an ordered range of all the o auth client as metadatas.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>OAuthClientASMetadataModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of o auth client as metadatas
	 * @param end the upper bound of the range of o auth client as metadatas (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of o auth client as metadatas
	 */
	@Override
	public List<OAuthClientASMetadata> findAll(
		int start, int end,
		OrderByComparator<OAuthClientASMetadata> orderByComparator) {

		return findAll(start, end, orderByComparator, true);
	}

	/**
	 * Returns an ordered range of all the o auth client as metadatas.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>OAuthClientASMetadataModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of o auth client as metadatas
	 * @param end the upper bound of the range of o auth client as metadatas (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of o auth client as metadatas
	 */
	@Override
	public List<OAuthClientASMetadata> findAll(
		int start, int end,
		OrderByComparator<OAuthClientASMetadata> orderByComparator,
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

		List<OAuthClientASMetadata> list = null;

		if (useFinderCache) {
			list = (List<OAuthClientASMetadata>)finderCache.getResult(
				finderPath, finderArgs);
		}

		if (list == null) {
			StringBundler sb = null;
			String sql = null;

			if (orderByComparator != null) {
				sb = new StringBundler(
					2 + (orderByComparator.getOrderByFields().length * 2));

				sb.append(_SQL_SELECT_OAUTHCLIENTASMETADATA);

				appendOrderByComparator(
					sb, _ORDER_BY_ENTITY_ALIAS, orderByComparator);

				sql = sb.toString();
			}
			else {
				sql = _SQL_SELECT_OAUTHCLIENTASMETADATA;

				sql = sql.concat(OAuthClientASMetadataModelImpl.ORDER_BY_JPQL);
			}

			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(sql);

				list = (List<OAuthClientASMetadata>)QueryUtil.list(
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
	 * Removes all the o auth client as metadatas from the database.
	 *
	 */
	@Override
	public void removeAll() {
		for (OAuthClientASMetadata oAuthClientASMetadata : findAll()) {
			remove(oAuthClientASMetadata);
		}
	}

	/**
	 * Returns the number of o auth client as metadatas.
	 *
	 * @return the number of o auth client as metadatas
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
					_SQL_COUNT_OAUTHCLIENTASMETADATA);

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
		return "oAuthClientASMetadataId";
	}

	@Override
	protected String getSelectSQL() {
		return _SQL_SELECT_OAUTHCLIENTASMETADATA;
	}

	@Override
	protected Map<String, Integer> getTableColumnsMap() {
		return OAuthClientASMetadataModelImpl.TABLE_COLUMNS_MAP;
	}

	/**
	 * Initializes the o auth client as metadata persistence.
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

		_finderPathWithPaginationFindByCompanyId = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITH_PAGINATION, "findByCompanyId",
			new String[] {
				Long.class.getName(), Integer.class.getName(),
				Integer.class.getName(), OrderByComparator.class.getName()
			},
			new String[] {"companyId"}, true);

		_finderPathWithoutPaginationFindByCompanyId = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "findByCompanyId",
			new String[] {Long.class.getName()}, new String[] {"companyId"},
			true);

		_finderPathCountByCompanyId = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countByCompanyId",
			new String[] {Long.class.getName()}, new String[] {"companyId"},
			false);

		_finderPathWithPaginationFindByC_U = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITH_PAGINATION, "findByC_U",
			new String[] {
				Long.class.getName(), Long.class.getName(),
				Integer.class.getName(), Integer.class.getName(),
				OrderByComparator.class.getName()
			},
			new String[] {"companyId", "userId"}, true);

		_finderPathWithoutPaginationFindByC_U = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "findByC_U",
			new String[] {Long.class.getName(), Long.class.getName()},
			new String[] {"companyId", "userId"}, true);

		_finderPathCountByC_U = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countByC_U",
			new String[] {Long.class.getName(), Long.class.getName()},
			new String[] {"companyId", "userId"}, false);

		_finderPathFetchByC_I = new FinderPath(
			FINDER_CLASS_NAME_ENTITY, "fetchByC_I",
			new String[] {Long.class.getName(), String.class.getName()},
			new String[] {"companyId", "issuer"}, true);

		_finderPathCountByC_I = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countByC_I",
			new String[] {Long.class.getName(), String.class.getName()},
			new String[] {"companyId", "issuer"}, false);

		_setOAuthClientASMetadataUtilPersistence(this);
	}

	@Deactivate
	public void deactivate() {
		_setOAuthClientASMetadataUtilPersistence(null);

		entityCache.removeCache(OAuthClientASMetadataImpl.class.getName());
	}

	private void _setOAuthClientASMetadataUtilPersistence(
		OAuthClientASMetadataPersistence oAuthClientASMetadataPersistence) {

		try {
			Field field = OAuthClientASMetadataUtil.class.getDeclaredField(
				"_persistence");

			field.setAccessible(true);

			field.set(null, oAuthClientASMetadataPersistence);
		}
		catch (ReflectiveOperationException reflectiveOperationException) {
			throw new RuntimeException(reflectiveOperationException);
		}
	}

	@Override
	@Reference(
		target = OAuthClientPersistenceConstants.SERVICE_CONFIGURATION_FILTER,
		unbind = "-"
	)
	public void setConfiguration(Configuration configuration) {
	}

	@Override
	@Reference(
		target = OAuthClientPersistenceConstants.ORIGIN_BUNDLE_SYMBOLIC_NAME_FILTER,
		unbind = "-"
	)
	public void setDataSource(DataSource dataSource) {
		super.setDataSource(dataSource);
	}

	@Override
	@Reference(
		target = OAuthClientPersistenceConstants.ORIGIN_BUNDLE_SYMBOLIC_NAME_FILTER,
		unbind = "-"
	)
	public void setSessionFactory(SessionFactory sessionFactory) {
		super.setSessionFactory(sessionFactory);
	}

	@Reference
	protected EntityCache entityCache;

	@Reference
	protected FinderCache finderCache;

	private static final String _SQL_SELECT_OAUTHCLIENTASMETADATA =
		"SELECT oAuthClientASMetadata FROM OAuthClientASMetadata oAuthClientASMetadata";

	private static final String _SQL_SELECT_OAUTHCLIENTASMETADATA_WHERE =
		"SELECT oAuthClientASMetadata FROM OAuthClientASMetadata oAuthClientASMetadata WHERE ";

	private static final String _SQL_COUNT_OAUTHCLIENTASMETADATA =
		"SELECT COUNT(oAuthClientASMetadata) FROM OAuthClientASMetadata oAuthClientASMetadata";

	private static final String _SQL_COUNT_OAUTHCLIENTASMETADATA_WHERE =
		"SELECT COUNT(oAuthClientASMetadata) FROM OAuthClientASMetadata oAuthClientASMetadata WHERE ";

	private static final String _ORDER_BY_ENTITY_ALIAS =
		"oAuthClientASMetadata.";

	private static final String _NO_SUCH_ENTITY_WITH_PRIMARY_KEY =
		"No OAuthClientASMetadata exists with the primary key ";

	private static final String _NO_SUCH_ENTITY_WITH_KEY =
		"No OAuthClientASMetadata exists with the key {";

	private static final Log _log = LogFactoryUtil.getLog(
		OAuthClientASMetadataPersistenceImpl.class);

	@Override
	protected FinderCache getFinderCache() {
		return finderCache;
	}

	@Reference
	private OAuthClientASMetadataModelArgumentsResolver
		_oAuthClientASMetadataModelArgumentsResolver;

}