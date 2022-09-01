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

package com.liferay.oauth.client.persistence.service.persistence.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.oauth.client.persistence.exception.NoSuchOAuthClientOIDCException;
import com.liferay.oauth.client.persistence.model.OAuthClientOIDC;
import com.liferay.oauth.client.persistence.service.OAuthClientOIDCLocalServiceUtil;
import com.liferay.oauth.client.persistence.service.persistence.OAuthClientOIDCPersistence;
import com.liferay.oauth.client.persistence.service.persistence.OAuthClientOIDCUtil;
import com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.ProjectionFactoryUtil;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.dao.orm.Session;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.kernel.util.IntegerWrapper;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.OrderByComparatorFactoryUtil;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PersistenceTestRule;
import com.liferay.portal.test.rule.TransactionalTestRule;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @generated
 */
@RunWith(Arquillian.class)
public class OAuthClientOIDCPersistenceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(), PersistenceTestRule.INSTANCE,
			new TransactionalTestRule(
				Propagation.REQUIRED,
				"com.liferay.oauth.client.persistence.service"));

	@Before
	public void setUp() {
		_persistence = OAuthClientOIDCUtil.getPersistence();

		Class<?> clazz = _persistence.getClass();

		_dynamicQueryClassLoader = clazz.getClassLoader();
	}

	@After
	public void tearDown() throws Exception {
		Iterator<OAuthClientOIDC> iterator = _oAuthClientOIDCs.iterator();

		while (iterator.hasNext()) {
			_persistence.remove(iterator.next());

			iterator.remove();
		}
	}

	@Test
	public void testCreate() throws Exception {
		long pk = RandomTestUtil.nextLong();

		OAuthClientOIDC oAuthClientOIDC = _persistence.create(pk);

		Assert.assertNotNull(oAuthClientOIDC);

		Assert.assertEquals(oAuthClientOIDC.getPrimaryKey(), pk);
	}

	@Test
	public void testRemove() throws Exception {
		OAuthClientOIDC newOAuthClientOIDC = addOAuthClientOIDC();

		_persistence.remove(newOAuthClientOIDC);

		OAuthClientOIDC existingOAuthClientOIDC =
			_persistence.fetchByPrimaryKey(newOAuthClientOIDC.getPrimaryKey());

		Assert.assertNull(existingOAuthClientOIDC);
	}

	@Test
	public void testUpdateNew() throws Exception {
		addOAuthClientOIDC();
	}

	@Test
	public void testUpdateExisting() throws Exception {
		long pk = RandomTestUtil.nextLong();

		OAuthClientOIDC newOAuthClientOIDC = _persistence.create(pk);

		newOAuthClientOIDC.setMvccVersion(RandomTestUtil.nextLong());

		newOAuthClientOIDC.setCompanyId(RandomTestUtil.nextLong());

		newOAuthClientOIDC.setUserId(RandomTestUtil.nextLong());

		newOAuthClientOIDC.setModifiedDate(RandomTestUtil.nextDate());

		newOAuthClientOIDC.setOAuthClientEntryId(RandomTestUtil.nextLong());

		newOAuthClientOIDC.setUserInfoMapperJSON(RandomTestUtil.randomString());

		_oAuthClientOIDCs.add(_persistence.update(newOAuthClientOIDC));

		OAuthClientOIDC existingOAuthClientOIDC = _persistence.findByPrimaryKey(
			newOAuthClientOIDC.getPrimaryKey());

		Assert.assertEquals(
			existingOAuthClientOIDC.getMvccVersion(),
			newOAuthClientOIDC.getMvccVersion());
		Assert.assertEquals(
			existingOAuthClientOIDC.getOAuthClientOIDCId(),
			newOAuthClientOIDC.getOAuthClientOIDCId());
		Assert.assertEquals(
			existingOAuthClientOIDC.getCompanyId(),
			newOAuthClientOIDC.getCompanyId());
		Assert.assertEquals(
			existingOAuthClientOIDC.getUserId(),
			newOAuthClientOIDC.getUserId());
		Assert.assertEquals(
			Time.getShortTimestamp(existingOAuthClientOIDC.getModifiedDate()),
			Time.getShortTimestamp(newOAuthClientOIDC.getModifiedDate()));
		Assert.assertEquals(
			existingOAuthClientOIDC.getOAuthClientEntryId(),
			newOAuthClientOIDC.getOAuthClientEntryId());
		Assert.assertEquals(
			existingOAuthClientOIDC.getUserInfoMapperJSON(),
			newOAuthClientOIDC.getUserInfoMapperJSON());
	}

	@Test
	public void testCountByOAuthClientEntryId() throws Exception {
		_persistence.countByOAuthClientEntryId(RandomTestUtil.nextLong());

		_persistence.countByOAuthClientEntryId(0L);
	}

	@Test
	public void testFindByPrimaryKeyExisting() throws Exception {
		OAuthClientOIDC newOAuthClientOIDC = addOAuthClientOIDC();

		OAuthClientOIDC existingOAuthClientOIDC = _persistence.findByPrimaryKey(
			newOAuthClientOIDC.getPrimaryKey());

		Assert.assertEquals(existingOAuthClientOIDC, newOAuthClientOIDC);
	}

	@Test(expected = NoSuchOAuthClientOIDCException.class)
	public void testFindByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		_persistence.findByPrimaryKey(pk);
	}

	@Test
	public void testFindAll() throws Exception {
		_persistence.findAll(
			QueryUtil.ALL_POS, QueryUtil.ALL_POS, getOrderByComparator());
	}

	protected OrderByComparator<OAuthClientOIDC> getOrderByComparator() {
		return OrderByComparatorFactoryUtil.create(
			"OAuthClientOIDC", "mvccVersion", true, "OAuthClientOIDCId", true,
			"companyId", true, "userId", true, "modifiedDate", true,
			"oAuthClientEntryId", true, "userInfoMapperJSON", true);
	}

	@Test
	public void testFetchByPrimaryKeyExisting() throws Exception {
		OAuthClientOIDC newOAuthClientOIDC = addOAuthClientOIDC();

		OAuthClientOIDC existingOAuthClientOIDC =
			_persistence.fetchByPrimaryKey(newOAuthClientOIDC.getPrimaryKey());

		Assert.assertEquals(existingOAuthClientOIDC, newOAuthClientOIDC);
	}

	@Test
	public void testFetchByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		OAuthClientOIDC missingOAuthClientOIDC = _persistence.fetchByPrimaryKey(
			pk);

		Assert.assertNull(missingOAuthClientOIDC);
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereAllPrimaryKeysExist()
		throws Exception {

		OAuthClientOIDC newOAuthClientOIDC1 = addOAuthClientOIDC();
		OAuthClientOIDC newOAuthClientOIDC2 = addOAuthClientOIDC();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newOAuthClientOIDC1.getPrimaryKey());
		primaryKeys.add(newOAuthClientOIDC2.getPrimaryKey());

		Map<Serializable, OAuthClientOIDC> oAuthClientOIDCs =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(2, oAuthClientOIDCs.size());
		Assert.assertEquals(
			newOAuthClientOIDC1,
			oAuthClientOIDCs.get(newOAuthClientOIDC1.getPrimaryKey()));
		Assert.assertEquals(
			newOAuthClientOIDC2,
			oAuthClientOIDCs.get(newOAuthClientOIDC2.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereNoPrimaryKeysExist()
		throws Exception {

		long pk1 = RandomTestUtil.nextLong();

		long pk2 = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(pk1);
		primaryKeys.add(pk2);

		Map<Serializable, OAuthClientOIDC> oAuthClientOIDCs =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(oAuthClientOIDCs.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereSomePrimaryKeysExist()
		throws Exception {

		OAuthClientOIDC newOAuthClientOIDC = addOAuthClientOIDC();

		long pk = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newOAuthClientOIDC.getPrimaryKey());
		primaryKeys.add(pk);

		Map<Serializable, OAuthClientOIDC> oAuthClientOIDCs =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, oAuthClientOIDCs.size());
		Assert.assertEquals(
			newOAuthClientOIDC,
			oAuthClientOIDCs.get(newOAuthClientOIDC.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithNoPrimaryKeys() throws Exception {
		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		Map<Serializable, OAuthClientOIDC> oAuthClientOIDCs =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(oAuthClientOIDCs.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithOnePrimaryKey() throws Exception {
		OAuthClientOIDC newOAuthClientOIDC = addOAuthClientOIDC();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newOAuthClientOIDC.getPrimaryKey());

		Map<Serializable, OAuthClientOIDC> oAuthClientOIDCs =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, oAuthClientOIDCs.size());
		Assert.assertEquals(
			newOAuthClientOIDC,
			oAuthClientOIDCs.get(newOAuthClientOIDC.getPrimaryKey()));
	}

	@Test
	public void testActionableDynamicQuery() throws Exception {
		final IntegerWrapper count = new IntegerWrapper();

		ActionableDynamicQuery actionableDynamicQuery =
			OAuthClientOIDCLocalServiceUtil.getActionableDynamicQuery();

		actionableDynamicQuery.setPerformActionMethod(
			new ActionableDynamicQuery.PerformActionMethod<OAuthClientOIDC>() {

				@Override
				public void performAction(OAuthClientOIDC oAuthClientOIDC) {
					Assert.assertNotNull(oAuthClientOIDC);

					count.increment();
				}

			});

		actionableDynamicQuery.performActions();

		Assert.assertEquals(count.getValue(), _persistence.countAll());
	}

	@Test
	public void testDynamicQueryByPrimaryKeyExisting() throws Exception {
		OAuthClientOIDC newOAuthClientOIDC = addOAuthClientOIDC();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			OAuthClientOIDC.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"OAuthClientOIDCId",
				newOAuthClientOIDC.getOAuthClientOIDCId()));

		List<OAuthClientOIDC> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		Assert.assertEquals(1, result.size());

		OAuthClientOIDC existingOAuthClientOIDC = result.get(0);

		Assert.assertEquals(existingOAuthClientOIDC, newOAuthClientOIDC);
	}

	@Test
	public void testDynamicQueryByPrimaryKeyMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			OAuthClientOIDC.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"OAuthClientOIDCId", RandomTestUtil.nextLong()));

		List<OAuthClientOIDC> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testDynamicQueryByProjectionExisting() throws Exception {
		OAuthClientOIDC newOAuthClientOIDC = addOAuthClientOIDC();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			OAuthClientOIDC.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("OAuthClientOIDCId"));

		Object newOAuthClientOIDCId = newOAuthClientOIDC.getOAuthClientOIDCId();

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"OAuthClientOIDCId", new Object[] {newOAuthClientOIDCId}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(1, result.size());

		Object existingOAuthClientOIDCId = result.get(0);

		Assert.assertEquals(existingOAuthClientOIDCId, newOAuthClientOIDCId);
	}

	@Test
	public void testDynamicQueryByProjectionMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			OAuthClientOIDC.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("OAuthClientOIDCId"));

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"OAuthClientOIDCId", new Object[] {RandomTestUtil.nextLong()}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testResetOriginalValues() throws Exception {
		OAuthClientOIDC newOAuthClientOIDC = addOAuthClientOIDC();

		_persistence.clearCache();

		_assertOriginalValues(
			_persistence.findByPrimaryKey(newOAuthClientOIDC.getPrimaryKey()));
	}

	@Test
	public void testResetOriginalValuesWithDynamicQueryLoadFromDatabase()
		throws Exception {

		_testResetOriginalValuesWithDynamicQuery(true);
	}

	@Test
	public void testResetOriginalValuesWithDynamicQueryLoadFromSession()
		throws Exception {

		_testResetOriginalValuesWithDynamicQuery(false);
	}

	private void _testResetOriginalValuesWithDynamicQuery(boolean clearSession)
		throws Exception {

		OAuthClientOIDC newOAuthClientOIDC = addOAuthClientOIDC();

		if (clearSession) {
			Session session = _persistence.openSession();

			session.flush();

			session.clear();
		}

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			OAuthClientOIDC.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"OAuthClientOIDCId",
				newOAuthClientOIDC.getOAuthClientOIDCId()));

		List<OAuthClientOIDC> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		_assertOriginalValues(result.get(0));
	}

	private void _assertOriginalValues(OAuthClientOIDC oAuthClientOIDC) {
		Assert.assertEquals(
			Long.valueOf(oAuthClientOIDC.getOAuthClientEntryId()),
			ReflectionTestUtil.<Long>invoke(
				oAuthClientOIDC, "getColumnOriginalValue",
				new Class<?>[] {String.class}, "oAuthClientEntryId"));
	}

	protected OAuthClientOIDC addOAuthClientOIDC() throws Exception {
		long pk = RandomTestUtil.nextLong();

		OAuthClientOIDC oAuthClientOIDC = _persistence.create(pk);

		oAuthClientOIDC.setMvccVersion(RandomTestUtil.nextLong());

		oAuthClientOIDC.setCompanyId(RandomTestUtil.nextLong());

		oAuthClientOIDC.setUserId(RandomTestUtil.nextLong());

		oAuthClientOIDC.setModifiedDate(RandomTestUtil.nextDate());

		oAuthClientOIDC.setOAuthClientEntryId(RandomTestUtil.nextLong());

		oAuthClientOIDC.setUserInfoMapperJSON(RandomTestUtil.randomString());

		_oAuthClientOIDCs.add(_persistence.update(oAuthClientOIDC));

		return oAuthClientOIDC;
	}

	private List<OAuthClientOIDC> _oAuthClientOIDCs =
		new ArrayList<OAuthClientOIDC>();
	private OAuthClientOIDCPersistence _persistence;
	private ClassLoader _dynamicQueryClassLoader;

}