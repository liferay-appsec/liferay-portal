/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.service.persistence.impl;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.orm.EntityCache;
import com.liferay.portal.kernel.dao.orm.EntityCacheUtil;
import com.liferay.portal.kernel.dao.orm.FinderCache;
import com.liferay.portal.kernel.dao.orm.FinderCacheUtil;
import com.liferay.portal.kernel.dao.orm.FinderPath;
import com.liferay.portal.kernel.dao.orm.Query;
import com.liferay.portal.kernel.dao.orm.QueryPos;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.dao.orm.Session;
import com.liferay.portal.kernel.exception.NoSuchRememberMeTokenException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.RememberMeToken;
import com.liferay.portal.kernel.model.RememberMeTokenTable;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.service.persistence.RememberMeTokenPersistence;
import com.liferay.portal.kernel.service.persistence.RememberMeTokenUtil;
import com.liferay.portal.kernel.service.persistence.impl.BasePersistenceImpl;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.ProxyUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.model.impl.RememberMeTokenImpl;
import com.liferay.portal.model.impl.RememberMeTokenModelImpl;

import java.io.Serializable;

import java.lang.reflect.InvocationHandler;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * The persistence implementation for the remember me token service.
 *
 * <p>
 * Caching information and settings can be found in <code>portal.properties</code>
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @generated
 */
public class RememberMeTokenPersistenceImpl
	extends BasePersistenceImpl<RememberMeToken>
	implements RememberMeTokenPersistence {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this class directly. Always use <code>RememberMeTokenUtil</code> to access the remember me token persistence. Modify <code>service.xml</code> and rerun ServiceBuilder to regenerate this class.
	 */
	public static final String FINDER_CLASS_NAME_ENTITY =
		RememberMeTokenImpl.class.getName();

	public static final String FINDER_CLASS_NAME_LIST_WITH_PAGINATION =
		FINDER_CLASS_NAME_ENTITY + ".List1";

	public static final String FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION =
		FINDER_CLASS_NAME_ENTITY + ".List2";

	private FinderPath _finderPathWithPaginationFindAll;
	private FinderPath _finderPathWithoutPaginationFindAll;
	private FinderPath _finderPathCountAll;
	private FinderPath _finderPathFetchByAccessToken;
	private FinderPath _finderPathCountByAccessToken;

	/**
	 * Returns the remember me token where accessToken = &#63; or throws a <code>NoSuchRememberMeTokenException</code> if it could not be found.
	 *
	 * @param accessToken the access token
	 * @return the matching remember me token
	 * @throws NoSuchRememberMeTokenException if a matching remember me token could not be found
	 */
	@Override
	public RememberMeToken findByAccessToken(String accessToken)
		throws NoSuchRememberMeTokenException {

		RememberMeToken rememberMeToken = fetchByAccessToken(accessToken);

		if (rememberMeToken == null) {
			StringBundler sb = new StringBundler(4);

			sb.append(_NO_SUCH_ENTITY_WITH_KEY);

			sb.append("accessToken=");
			sb.append(accessToken);

			sb.append("}");

			if (_log.isDebugEnabled()) {
				_log.debug(sb.toString());
			}

			throw new NoSuchRememberMeTokenException(sb.toString());
		}

		return rememberMeToken;
	}

	/**
	 * Returns the remember me token where accessToken = &#63; or returns <code>null</code> if it could not be found. Uses the finder cache.
	 *
	 * @param accessToken the access token
	 * @return the matching remember me token, or <code>null</code> if a matching remember me token could not be found
	 */
	@Override
	public RememberMeToken fetchByAccessToken(String accessToken) {
		return fetchByAccessToken(accessToken, true);
	}

	/**
	 * Returns the remember me token where accessToken = &#63; or returns <code>null</code> if it could not be found, optionally using the finder cache.
	 *
	 * @param accessToken the access token
	 * @param useFinderCache whether to use the finder cache
	 * @return the matching remember me token, or <code>null</code> if a matching remember me token could not be found
	 */
	@Override
	public RememberMeToken fetchByAccessToken(
		String accessToken, boolean useFinderCache) {

		accessToken = Objects.toString(accessToken, "");

		Object[] finderArgs = null;

		if (useFinderCache) {
			finderArgs = new Object[] {accessToken};
		}

		Object result = null;

		if (useFinderCache) {
			result = FinderCacheUtil.getResult(
				_finderPathFetchByAccessToken, finderArgs, this);
		}

		if (result instanceof RememberMeToken) {
			RememberMeToken rememberMeToken = (RememberMeToken)result;

			if (!Objects.equals(
					accessToken, rememberMeToken.getAccessToken())) {

				result = null;
			}
		}

		if (result == null) {
			StringBundler sb = new StringBundler(3);

			sb.append(_SQL_SELECT_REMEMBERMETOKEN_WHERE);

			boolean bindAccessToken = false;

			if (accessToken.isEmpty()) {
				sb.append(_FINDER_COLUMN_ACCESSTOKEN_ACCESSTOKEN_3);
			}
			else {
				bindAccessToken = true;

				sb.append(_FINDER_COLUMN_ACCESSTOKEN_ACCESSTOKEN_2);
			}

			String sql = sb.toString();

			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(sql);

				QueryPos queryPos = QueryPos.getInstance(query);

				if (bindAccessToken) {
					queryPos.add(accessToken);
				}

				List<RememberMeToken> list = query.list();

				if (list.isEmpty()) {
					if (useFinderCache) {
						FinderCacheUtil.putResult(
							_finderPathFetchByAccessToken, finderArgs, list);
					}
				}
				else {
					if (list.size() > 1) {
						Collections.sort(list, Collections.reverseOrder());

						if (_log.isWarnEnabled()) {
							if (!useFinderCache) {
								finderArgs = new Object[] {accessToken};
							}

							_log.warn(
								"RememberMeTokenPersistenceImpl.fetchByAccessToken(String, boolean) with parameters (" +
									StringUtil.merge(finderArgs) +
										") yields a result set with more than 1 result. This violates the logical unique restriction. There is no order guarantee on which result is returned by this finder.");
						}
					}

					RememberMeToken rememberMeToken = list.get(0);

					result = rememberMeToken;

					cacheResult(rememberMeToken);
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
			return (RememberMeToken)result;
		}
	}

	/**
	 * Removes the remember me token where accessToken = &#63; from the database.
	 *
	 * @param accessToken the access token
	 * @return the remember me token that was removed
	 */
	@Override
	public RememberMeToken removeByAccessToken(String accessToken)
		throws NoSuchRememberMeTokenException {

		RememberMeToken rememberMeToken = findByAccessToken(accessToken);

		return remove(rememberMeToken);
	}

	/**
	 * Returns the number of remember me tokens where accessToken = &#63;.
	 *
	 * @param accessToken the access token
	 * @return the number of matching remember me tokens
	 */
	@Override
	public int countByAccessToken(String accessToken) {
		accessToken = Objects.toString(accessToken, "");

		FinderPath finderPath = _finderPathCountByAccessToken;

		Object[] finderArgs = new Object[] {accessToken};

		Long count = (Long)FinderCacheUtil.getResult(
			finderPath, finderArgs, this);

		if (count == null) {
			StringBundler sb = new StringBundler(2);

			sb.append(_SQL_COUNT_REMEMBERMETOKEN_WHERE);

			boolean bindAccessToken = false;

			if (accessToken.isEmpty()) {
				sb.append(_FINDER_COLUMN_ACCESSTOKEN_ACCESSTOKEN_3);
			}
			else {
				bindAccessToken = true;

				sb.append(_FINDER_COLUMN_ACCESSTOKEN_ACCESSTOKEN_2);
			}

			String sql = sb.toString();

			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(sql);

				QueryPos queryPos = QueryPos.getInstance(query);

				if (bindAccessToken) {
					queryPos.add(accessToken);
				}

				count = (Long)query.uniqueResult();

				FinderCacheUtil.putResult(finderPath, finderArgs, count);
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

	private static final String _FINDER_COLUMN_ACCESSTOKEN_ACCESSTOKEN_2 =
		"rememberMeToken.accessToken = ?";

	private static final String _FINDER_COLUMN_ACCESSTOKEN_ACCESSTOKEN_3 =
		"(rememberMeToken.accessToken IS NULL OR rememberMeToken.accessToken = '')";

	public RememberMeTokenPersistenceImpl() {
		setModelClass(RememberMeToken.class);

		setModelImplClass(RememberMeTokenImpl.class);
		setModelPKClass(long.class);

		setTable(RememberMeTokenTable.INSTANCE);
	}

	/**
	 * Caches the remember me token in the entity cache if it is enabled.
	 *
	 * @param rememberMeToken the remember me token
	 */
	@Override
	public void cacheResult(RememberMeToken rememberMeToken) {
		EntityCacheUtil.putResult(
			RememberMeTokenImpl.class, rememberMeToken.getPrimaryKey(),
			rememberMeToken);

		FinderCacheUtil.putResult(
			_finderPathFetchByAccessToken,
			new Object[] {rememberMeToken.getAccessToken()}, rememberMeToken);
	}

	private int _valueObjectFinderCacheListThreshold;

	/**
	 * Caches the remember me tokens in the entity cache if it is enabled.
	 *
	 * @param rememberMeTokens the remember me tokens
	 */
	@Override
	public void cacheResult(List<RememberMeToken> rememberMeTokens) {
		if ((_valueObjectFinderCacheListThreshold == 0) ||
			((_valueObjectFinderCacheListThreshold > 0) &&
			 (rememberMeTokens.size() >
				 _valueObjectFinderCacheListThreshold))) {

			return;
		}

		for (RememberMeToken rememberMeToken : rememberMeTokens) {
			if (EntityCacheUtil.getResult(
					RememberMeTokenImpl.class,
					rememberMeToken.getPrimaryKey()) == null) {

				cacheResult(rememberMeToken);
			}
		}
	}

	/**
	 * Clears the cache for all remember me tokens.
	 *
	 * <p>
	 * The <code>EntityCache</code> and <code>FinderCache</code> are both cleared by this method.
	 * </p>
	 */
	@Override
	public void clearCache() {
		EntityCacheUtil.clearCache(RememberMeTokenImpl.class);

		FinderCacheUtil.clearCache(RememberMeTokenImpl.class);
	}

	/**
	 * Clears the cache for the remember me token.
	 *
	 * <p>
	 * The <code>EntityCache</code> and <code>FinderCache</code> are both cleared by this method.
	 * </p>
	 */
	@Override
	public void clearCache(RememberMeToken rememberMeToken) {
		EntityCacheUtil.removeResult(
			RememberMeTokenImpl.class, rememberMeToken);
	}

	@Override
	public void clearCache(List<RememberMeToken> rememberMeTokens) {
		for (RememberMeToken rememberMeToken : rememberMeTokens) {
			EntityCacheUtil.removeResult(
				RememberMeTokenImpl.class, rememberMeToken);
		}
	}

	@Override
	public void clearCache(Set<Serializable> primaryKeys) {
		FinderCacheUtil.clearCache(RememberMeTokenImpl.class);

		for (Serializable primaryKey : primaryKeys) {
			EntityCacheUtil.removeResult(RememberMeTokenImpl.class, primaryKey);
		}
	}

	protected void cacheUniqueFindersCache(
		RememberMeTokenModelImpl rememberMeTokenModelImpl) {

		Object[] args = new Object[] {
			rememberMeTokenModelImpl.getAccessToken()
		};

		FinderCacheUtil.putResult(
			_finderPathCountByAccessToken, args, Long.valueOf(1));
		FinderCacheUtil.putResult(
			_finderPathFetchByAccessToken, args, rememberMeTokenModelImpl);
	}

	/**
	 * Creates a new remember me token with the primary key. Does not add the remember me token to the database.
	 *
	 * @param rememberMeTokenId the primary key for the new remember me token
	 * @return the new remember me token
	 */
	@Override
	public RememberMeToken create(long rememberMeTokenId) {
		RememberMeToken rememberMeToken = new RememberMeTokenImpl();

		rememberMeToken.setNew(true);
		rememberMeToken.setPrimaryKey(rememberMeTokenId);

		rememberMeToken.setCompanyId(CompanyThreadLocal.getCompanyId());

		return rememberMeToken;
	}

	/**
	 * Removes the remember me token with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param rememberMeTokenId the primary key of the remember me token
	 * @return the remember me token that was removed
	 * @throws NoSuchRememberMeTokenException if a remember me token with the primary key could not be found
	 */
	@Override
	public RememberMeToken remove(long rememberMeTokenId)
		throws NoSuchRememberMeTokenException {

		return remove((Serializable)rememberMeTokenId);
	}

	/**
	 * Removes the remember me token with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param primaryKey the primary key of the remember me token
	 * @return the remember me token that was removed
	 * @throws NoSuchRememberMeTokenException if a remember me token with the primary key could not be found
	 */
	@Override
	public RememberMeToken remove(Serializable primaryKey)
		throws NoSuchRememberMeTokenException {

		Session session = null;

		try {
			session = openSession();

			RememberMeToken rememberMeToken = (RememberMeToken)session.get(
				RememberMeTokenImpl.class, primaryKey);

			if (rememberMeToken == null) {
				if (_log.isDebugEnabled()) {
					_log.debug(_NO_SUCH_ENTITY_WITH_PRIMARY_KEY + primaryKey);
				}

				throw new NoSuchRememberMeTokenException(
					_NO_SUCH_ENTITY_WITH_PRIMARY_KEY + primaryKey);
			}

			return remove(rememberMeToken);
		}
		catch (NoSuchRememberMeTokenException noSuchEntityException) {
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
	protected RememberMeToken removeImpl(RememberMeToken rememberMeToken) {
		Session session = null;

		try {
			session = openSession();

			if (!session.contains(rememberMeToken)) {
				rememberMeToken = (RememberMeToken)session.get(
					RememberMeTokenImpl.class,
					rememberMeToken.getPrimaryKeyObj());
			}

			if (rememberMeToken != null) {
				session.delete(rememberMeToken);
			}
		}
		catch (Exception exception) {
			throw processException(exception);
		}
		finally {
			closeSession(session);
		}

		if (rememberMeToken != null) {
			clearCache(rememberMeToken);
		}

		return rememberMeToken;
	}

	@Override
	public RememberMeToken updateImpl(RememberMeToken rememberMeToken) {
		boolean isNew = rememberMeToken.isNew();

		if (!(rememberMeToken instanceof RememberMeTokenModelImpl)) {
			InvocationHandler invocationHandler = null;

			if (ProxyUtil.isProxyClass(rememberMeToken.getClass())) {
				invocationHandler = ProxyUtil.getInvocationHandler(
					rememberMeToken);

				throw new IllegalArgumentException(
					"Implement ModelWrapper in rememberMeToken proxy " +
						invocationHandler.getClass());
			}

			throw new IllegalArgumentException(
				"Implement ModelWrapper in custom RememberMeToken implementation " +
					rememberMeToken.getClass());
		}

		RememberMeTokenModelImpl rememberMeTokenModelImpl =
			(RememberMeTokenModelImpl)rememberMeToken;

		if (isNew && (rememberMeToken.getCreateDate() == null)) {
			ServiceContext serviceContext =
				ServiceContextThreadLocal.getServiceContext();

			Date date = new Date();

			if (serviceContext == null) {
				rememberMeToken.setCreateDate(date);
			}
			else {
				rememberMeToken.setCreateDate(
					serviceContext.getCreateDate(date));
			}
		}

		Session session = null;

		try {
			session = openSession();

			if (isNew) {
				session.save(rememberMeToken);
			}
			else {
				rememberMeToken = (RememberMeToken)session.merge(
					rememberMeToken);
			}
		}
		catch (Exception exception) {
			throw processException(exception);
		}
		finally {
			closeSession(session);
		}

		EntityCacheUtil.putResult(
			RememberMeTokenImpl.class, rememberMeTokenModelImpl, false, true);

		cacheUniqueFindersCache(rememberMeTokenModelImpl);

		if (isNew) {
			rememberMeToken.setNew(false);
		}

		rememberMeToken.resetOriginalValues();

		return rememberMeToken;
	}

	/**
	 * Returns the remember me token with the primary key or throws a <code>com.liferay.portal.kernel.exception.NoSuchModelException</code> if it could not be found.
	 *
	 * @param primaryKey the primary key of the remember me token
	 * @return the remember me token
	 * @throws NoSuchRememberMeTokenException if a remember me token with the primary key could not be found
	 */
	@Override
	public RememberMeToken findByPrimaryKey(Serializable primaryKey)
		throws NoSuchRememberMeTokenException {

		RememberMeToken rememberMeToken = fetchByPrimaryKey(primaryKey);

		if (rememberMeToken == null) {
			if (_log.isDebugEnabled()) {
				_log.debug(_NO_SUCH_ENTITY_WITH_PRIMARY_KEY + primaryKey);
			}

			throw new NoSuchRememberMeTokenException(
				_NO_SUCH_ENTITY_WITH_PRIMARY_KEY + primaryKey);
		}

		return rememberMeToken;
	}

	/**
	 * Returns the remember me token with the primary key or throws a <code>NoSuchRememberMeTokenException</code> if it could not be found.
	 *
	 * @param rememberMeTokenId the primary key of the remember me token
	 * @return the remember me token
	 * @throws NoSuchRememberMeTokenException if a remember me token with the primary key could not be found
	 */
	@Override
	public RememberMeToken findByPrimaryKey(long rememberMeTokenId)
		throws NoSuchRememberMeTokenException {

		return findByPrimaryKey((Serializable)rememberMeTokenId);
	}

	/**
	 * Returns the remember me token with the primary key or returns <code>null</code> if it could not be found.
	 *
	 * @param rememberMeTokenId the primary key of the remember me token
	 * @return the remember me token, or <code>null</code> if a remember me token with the primary key could not be found
	 */
	@Override
	public RememberMeToken fetchByPrimaryKey(long rememberMeTokenId) {
		return fetchByPrimaryKey((Serializable)rememberMeTokenId);
	}

	/**
	 * Returns all the remember me tokens.
	 *
	 * @return the remember me tokens
	 */
	@Override
	public List<RememberMeToken> findAll() {
		return findAll(QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
	}

	/**
	 * Returns a range of all the remember me tokens.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>RememberMeTokenModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of remember me tokens
	 * @param end the upper bound of the range of remember me tokens (not inclusive)
	 * @return the range of remember me tokens
	 */
	@Override
	public List<RememberMeToken> findAll(int start, int end) {
		return findAll(start, end, null);
	}

	/**
	 * Returns an ordered range of all the remember me tokens.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>RememberMeTokenModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of remember me tokens
	 * @param end the upper bound of the range of remember me tokens (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of remember me tokens
	 */
	@Override
	public List<RememberMeToken> findAll(
		int start, int end,
		OrderByComparator<RememberMeToken> orderByComparator) {

		return findAll(start, end, orderByComparator, true);
	}

	/**
	 * Returns an ordered range of all the remember me tokens.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>RememberMeTokenModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of remember me tokens
	 * @param end the upper bound of the range of remember me tokens (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of remember me tokens
	 */
	@Override
	public List<RememberMeToken> findAll(
		int start, int end,
		OrderByComparator<RememberMeToken> orderByComparator,
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

		List<RememberMeToken> list = null;

		if (useFinderCache) {
			list = (List<RememberMeToken>)FinderCacheUtil.getResult(
				finderPath, finderArgs, this);
		}

		if (list == null) {
			StringBundler sb = null;
			String sql = null;

			if (orderByComparator != null) {
				sb = new StringBundler(
					2 + (orderByComparator.getOrderByFields().length * 2));

				sb.append(_SQL_SELECT_REMEMBERMETOKEN);

				appendOrderByComparator(
					sb, _ORDER_BY_ENTITY_ALIAS, orderByComparator);

				sql = sb.toString();
			}
			else {
				sql = _SQL_SELECT_REMEMBERMETOKEN;

				sql = sql.concat(RememberMeTokenModelImpl.ORDER_BY_JPQL);
			}

			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(sql);

				list = (List<RememberMeToken>)QueryUtil.list(
					query, getDialect(), start, end);

				cacheResult(list);

				if (useFinderCache) {
					FinderCacheUtil.putResult(finderPath, finderArgs, list);
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
	 * Removes all the remember me tokens from the database.
	 *
	 */
	@Override
	public void removeAll() {
		for (RememberMeToken rememberMeToken : findAll()) {
			remove(rememberMeToken);
		}
	}

	/**
	 * Returns the number of remember me tokens.
	 *
	 * @return the number of remember me tokens
	 */
	@Override
	public int countAll() {
		Long count = (Long)FinderCacheUtil.getResult(
			_finderPathCountAll, FINDER_ARGS_EMPTY, this);

		if (count == null) {
			Session session = null;

			try {
				session = openSession();

				Query query = session.createQuery(_SQL_COUNT_REMEMBERMETOKEN);

				count = (Long)query.uniqueResult();

				FinderCacheUtil.putResult(
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
		return EntityCacheUtil.getEntityCache();
	}

	@Override
	protected String getPKDBName() {
		return "rememberMeTokenId";
	}

	@Override
	protected String getSelectSQL() {
		return _SQL_SELECT_REMEMBERMETOKEN;
	}

	@Override
	protected Map<String, Integer> getTableColumnsMap() {
		return RememberMeTokenModelImpl.TABLE_COLUMNS_MAP;
	}

	/**
	 * Initializes the remember me token persistence.
	 */
	public void afterPropertiesSet() {
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

		_finderPathFetchByAccessToken = new FinderPath(
			FINDER_CLASS_NAME_ENTITY, "fetchByAccessToken",
			new String[] {String.class.getName()}, new String[] {"accessToken"},
			true);

		_finderPathCountByAccessToken = new FinderPath(
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countByAccessToken",
			new String[] {String.class.getName()}, new String[] {"accessToken"},
			false);

		RememberMeTokenUtil.setPersistence(this);
	}

	public void destroy() {
		RememberMeTokenUtil.setPersistence(null);

		EntityCacheUtil.removeCache(RememberMeTokenImpl.class.getName());
	}

	private static final String _SQL_SELECT_REMEMBERMETOKEN =
		"SELECT rememberMeToken FROM RememberMeToken rememberMeToken";

	private static final String _SQL_SELECT_REMEMBERMETOKEN_WHERE =
		"SELECT rememberMeToken FROM RememberMeToken rememberMeToken WHERE ";

	private static final String _SQL_COUNT_REMEMBERMETOKEN =
		"SELECT COUNT(rememberMeToken) FROM RememberMeToken rememberMeToken";

	private static final String _SQL_COUNT_REMEMBERMETOKEN_WHERE =
		"SELECT COUNT(rememberMeToken) FROM RememberMeToken rememberMeToken WHERE ";

	private static final String _ORDER_BY_ENTITY_ALIAS = "rememberMeToken.";

	private static final String _NO_SUCH_ENTITY_WITH_PRIMARY_KEY =
		"No RememberMeToken exists with the primary key ";

	private static final String _NO_SUCH_ENTITY_WITH_KEY =
		"No RememberMeToken exists with the key {";

	private static final Log _log = LogFactoryUtil.getLog(
		RememberMeTokenPersistenceImpl.class);

	@Override
	protected FinderCache getFinderCache() {
		return FinderCacheUtil.getFinderCache();
	}

}