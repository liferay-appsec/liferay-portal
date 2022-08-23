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

package com.liferay.portal.security.ldap.persistence.service.persistence.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
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
import com.liferay.portal.security.ldap.persistence.exception.NoSuchServerAttributeRelException;
import com.liferay.portal.security.ldap.persistence.model.LDAPServerAttributeRel;
import com.liferay.portal.security.ldap.persistence.service.LDAPServerAttributeRelLocalServiceUtil;
import com.liferay.portal.security.ldap.persistence.service.persistence.LDAPServerAttributeRelPersistence;
import com.liferay.portal.security.ldap.persistence.service.persistence.LDAPServerAttributeRelUtil;
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
public class LDAPServerAttributeRelPersistenceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(), PersistenceTestRule.INSTANCE,
			new TransactionalTestRule(
				Propagation.REQUIRED,
				"com.liferay.portal.security.ldap.persistence.service"));

	@Before
	public void setUp() {
		_persistence = LDAPServerAttributeRelUtil.getPersistence();

		Class<?> clazz = _persistence.getClass();

		_dynamicQueryClassLoader = clazz.getClassLoader();
	}

	@After
	public void tearDown() throws Exception {
		Iterator<LDAPServerAttributeRel> iterator =
			_ldapServerAttributeRels.iterator();

		while (iterator.hasNext()) {
			_persistence.remove(iterator.next());

			iterator.remove();
		}
	}

	@Test
	public void testCreate() throws Exception {
		long pk = RandomTestUtil.nextLong();

		LDAPServerAttributeRel ldapServerAttributeRel = _persistence.create(pk);

		Assert.assertNotNull(ldapServerAttributeRel);

		Assert.assertEquals(ldapServerAttributeRel.getPrimaryKey(), pk);
	}

	@Test
	public void testRemove() throws Exception {
		LDAPServerAttributeRel newLDAPServerAttributeRel =
			addLDAPServerAttributeRel();

		_persistence.remove(newLDAPServerAttributeRel);

		LDAPServerAttributeRel existingLDAPServerAttributeRel =
			_persistence.fetchByPrimaryKey(
				newLDAPServerAttributeRel.getPrimaryKey());

		Assert.assertNull(existingLDAPServerAttributeRel);
	}

	@Test
	public void testUpdateNew() throws Exception {
		addLDAPServerAttributeRel();
	}

	@Test
	public void testUpdateExisting() throws Exception {
		long pk = RandomTestUtil.nextLong();

		LDAPServerAttributeRel newLDAPServerAttributeRel = _persistence.create(
			pk);

		newLDAPServerAttributeRel.setMvccVersion(RandomTestUtil.nextLong());

		newLDAPServerAttributeRel.setCompanyId(RandomTestUtil.nextLong());

		newLDAPServerAttributeRel.setLdapServerId(RandomTestUtil.nextLong());

		newLDAPServerAttributeRel.setClassNameId(RandomTestUtil.nextLong());

		newLDAPServerAttributeRel.setClassPK(RandomTestUtil.nextLong());

		_ldapServerAttributeRels.add(
			_persistence.update(newLDAPServerAttributeRel));

		LDAPServerAttributeRel existingLDAPServerAttributeRel =
			_persistence.findByPrimaryKey(
				newLDAPServerAttributeRel.getPrimaryKey());

		Assert.assertEquals(
			existingLDAPServerAttributeRel.getMvccVersion(),
			newLDAPServerAttributeRel.getMvccVersion());
		Assert.assertEquals(
			existingLDAPServerAttributeRel.getLdapServerAttributeRelId(),
			newLDAPServerAttributeRel.getLdapServerAttributeRelId());
		Assert.assertEquals(
			existingLDAPServerAttributeRel.getCompanyId(),
			newLDAPServerAttributeRel.getCompanyId());
		Assert.assertEquals(
			existingLDAPServerAttributeRel.getLdapServerId(),
			newLDAPServerAttributeRel.getLdapServerId());
		Assert.assertEquals(
			existingLDAPServerAttributeRel.getClassNameId(),
			newLDAPServerAttributeRel.getClassNameId());
		Assert.assertEquals(
			existingLDAPServerAttributeRel.getClassPK(),
			newLDAPServerAttributeRel.getClassPK());
	}

	@Test
	public void testCountByLdapServerId() throws Exception {
		_persistence.countByLdapServerId(RandomTestUtil.nextLong());

		_persistence.countByLdapServerId(0L);
	}

	@Test
	public void testCountByL_C() throws Exception {
		_persistence.countByL_C(
			RandomTestUtil.nextLong(), RandomTestUtil.nextLong());

		_persistence.countByL_C(0L, 0L);
	}

	@Test
	public void testCountByC_C() throws Exception {
		_persistence.countByC_C(
			RandomTestUtil.nextLong(), RandomTestUtil.nextLong());

		_persistence.countByC_C(0L, 0L);
	}

	@Test
	public void testCountByL_C_C() throws Exception {
		_persistence.countByL_C_C(
			RandomTestUtil.nextLong(), RandomTestUtil.nextLong(),
			RandomTestUtil.nextLong());

		_persistence.countByL_C_C(0L, 0L, 0L);
	}

	@Test
	public void testFindByPrimaryKeyExisting() throws Exception {
		LDAPServerAttributeRel newLDAPServerAttributeRel =
			addLDAPServerAttributeRel();

		LDAPServerAttributeRel existingLDAPServerAttributeRel =
			_persistence.findByPrimaryKey(
				newLDAPServerAttributeRel.getPrimaryKey());

		Assert.assertEquals(
			existingLDAPServerAttributeRel, newLDAPServerAttributeRel);
	}

	@Test(expected = NoSuchServerAttributeRelException.class)
	public void testFindByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		_persistence.findByPrimaryKey(pk);
	}

	@Test
	public void testFindAll() throws Exception {
		_persistence.findAll(
			QueryUtil.ALL_POS, QueryUtil.ALL_POS, getOrderByComparator());
	}

	protected OrderByComparator<LDAPServerAttributeRel> getOrderByComparator() {
		return OrderByComparatorFactoryUtil.create(
			"LDAPServerAttributeRel", "mvccVersion", true,
			"ldapServerAttributeRelId", true, "companyId", true, "ldapServerId",
			true, "classNameId", true, "classPK", true);
	}

	@Test
	public void testFetchByPrimaryKeyExisting() throws Exception {
		LDAPServerAttributeRel newLDAPServerAttributeRel =
			addLDAPServerAttributeRel();

		LDAPServerAttributeRel existingLDAPServerAttributeRel =
			_persistence.fetchByPrimaryKey(
				newLDAPServerAttributeRel.getPrimaryKey());

		Assert.assertEquals(
			existingLDAPServerAttributeRel, newLDAPServerAttributeRel);
	}

	@Test
	public void testFetchByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		LDAPServerAttributeRel missingLDAPServerAttributeRel =
			_persistence.fetchByPrimaryKey(pk);

		Assert.assertNull(missingLDAPServerAttributeRel);
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereAllPrimaryKeysExist()
		throws Exception {

		LDAPServerAttributeRel newLDAPServerAttributeRel1 =
			addLDAPServerAttributeRel();
		LDAPServerAttributeRel newLDAPServerAttributeRel2 =
			addLDAPServerAttributeRel();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newLDAPServerAttributeRel1.getPrimaryKey());
		primaryKeys.add(newLDAPServerAttributeRel2.getPrimaryKey());

		Map<Serializable, LDAPServerAttributeRel> ldapServerAttributeRels =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(2, ldapServerAttributeRels.size());
		Assert.assertEquals(
			newLDAPServerAttributeRel1,
			ldapServerAttributeRels.get(
				newLDAPServerAttributeRel1.getPrimaryKey()));
		Assert.assertEquals(
			newLDAPServerAttributeRel2,
			ldapServerAttributeRels.get(
				newLDAPServerAttributeRel2.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereNoPrimaryKeysExist()
		throws Exception {

		long pk1 = RandomTestUtil.nextLong();

		long pk2 = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(pk1);
		primaryKeys.add(pk2);

		Map<Serializable, LDAPServerAttributeRel> ldapServerAttributeRels =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(ldapServerAttributeRels.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereSomePrimaryKeysExist()
		throws Exception {

		LDAPServerAttributeRel newLDAPServerAttributeRel =
			addLDAPServerAttributeRel();

		long pk = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newLDAPServerAttributeRel.getPrimaryKey());
		primaryKeys.add(pk);

		Map<Serializable, LDAPServerAttributeRel> ldapServerAttributeRels =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, ldapServerAttributeRels.size());
		Assert.assertEquals(
			newLDAPServerAttributeRel,
			ldapServerAttributeRels.get(
				newLDAPServerAttributeRel.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithNoPrimaryKeys() throws Exception {
		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		Map<Serializable, LDAPServerAttributeRel> ldapServerAttributeRels =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(ldapServerAttributeRels.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithOnePrimaryKey() throws Exception {
		LDAPServerAttributeRel newLDAPServerAttributeRel =
			addLDAPServerAttributeRel();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newLDAPServerAttributeRel.getPrimaryKey());

		Map<Serializable, LDAPServerAttributeRel> ldapServerAttributeRels =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, ldapServerAttributeRels.size());
		Assert.assertEquals(
			newLDAPServerAttributeRel,
			ldapServerAttributeRels.get(
				newLDAPServerAttributeRel.getPrimaryKey()));
	}

	@Test
	public void testActionableDynamicQuery() throws Exception {
		final IntegerWrapper count = new IntegerWrapper();

		ActionableDynamicQuery actionableDynamicQuery =
			LDAPServerAttributeRelLocalServiceUtil.getActionableDynamicQuery();

		actionableDynamicQuery.setPerformActionMethod(
			new ActionableDynamicQuery.PerformActionMethod
				<LDAPServerAttributeRel>() {

				@Override
				public void performAction(
					LDAPServerAttributeRel ldapServerAttributeRel) {

					Assert.assertNotNull(ldapServerAttributeRel);

					count.increment();
				}

			});

		actionableDynamicQuery.performActions();

		Assert.assertEquals(count.getValue(), _persistence.countAll());
	}

	@Test
	public void testDynamicQueryByPrimaryKeyExisting() throws Exception {
		LDAPServerAttributeRel newLDAPServerAttributeRel =
			addLDAPServerAttributeRel();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			LDAPServerAttributeRel.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"ldapServerAttributeRelId",
				newLDAPServerAttributeRel.getLdapServerAttributeRelId()));

		List<LDAPServerAttributeRel> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		Assert.assertEquals(1, result.size());

		LDAPServerAttributeRel existingLDAPServerAttributeRel = result.get(0);

		Assert.assertEquals(
			existingLDAPServerAttributeRel, newLDAPServerAttributeRel);
	}

	@Test
	public void testDynamicQueryByPrimaryKeyMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			LDAPServerAttributeRel.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"ldapServerAttributeRelId", RandomTestUtil.nextLong()));

		List<LDAPServerAttributeRel> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testDynamicQueryByProjectionExisting() throws Exception {
		LDAPServerAttributeRel newLDAPServerAttributeRel =
			addLDAPServerAttributeRel();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			LDAPServerAttributeRel.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("ldapServerAttributeRelId"));

		Object newLdapServerAttributeRelId =
			newLDAPServerAttributeRel.getLdapServerAttributeRelId();

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"ldapServerAttributeRelId",
				new Object[] {newLdapServerAttributeRelId}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(1, result.size());

		Object existingLdapServerAttributeRelId = result.get(0);

		Assert.assertEquals(
			existingLdapServerAttributeRelId, newLdapServerAttributeRelId);
	}

	@Test
	public void testDynamicQueryByProjectionMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			LDAPServerAttributeRel.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("ldapServerAttributeRelId"));

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"ldapServerAttributeRelId",
				new Object[] {RandomTestUtil.nextLong()}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testResetOriginalValues() throws Exception {
		LDAPServerAttributeRel newLDAPServerAttributeRel =
			addLDAPServerAttributeRel();

		_persistence.clearCache();

		_assertOriginalValues(
			_persistence.findByPrimaryKey(
				newLDAPServerAttributeRel.getPrimaryKey()));
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

		LDAPServerAttributeRel newLDAPServerAttributeRel =
			addLDAPServerAttributeRel();

		if (clearSession) {
			Session session = _persistence.openSession();

			session.flush();

			session.clear();
		}

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			LDAPServerAttributeRel.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"ldapServerAttributeRelId",
				newLDAPServerAttributeRel.getLdapServerAttributeRelId()));

		List<LDAPServerAttributeRel> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		_assertOriginalValues(result.get(0));
	}

	private void _assertOriginalValues(
		LDAPServerAttributeRel ldapServerAttributeRel) {

		Assert.assertEquals(
			Long.valueOf(ldapServerAttributeRel.getLdapServerId()),
			ReflectionTestUtil.<Long>invoke(
				ldapServerAttributeRel, "getColumnOriginalValue",
				new Class<?>[] {String.class}, "ldapServerId"));
		Assert.assertEquals(
			Long.valueOf(ldapServerAttributeRel.getClassNameId()),
			ReflectionTestUtil.<Long>invoke(
				ldapServerAttributeRel, "getColumnOriginalValue",
				new Class<?>[] {String.class}, "classNameId"));
		Assert.assertEquals(
			Long.valueOf(ldapServerAttributeRel.getClassPK()),
			ReflectionTestUtil.<Long>invoke(
				ldapServerAttributeRel, "getColumnOriginalValue",
				new Class<?>[] {String.class}, "classPK"));
	}

	protected LDAPServerAttributeRel addLDAPServerAttributeRel()
		throws Exception {

		long pk = RandomTestUtil.nextLong();

		LDAPServerAttributeRel ldapServerAttributeRel = _persistence.create(pk);

		ldapServerAttributeRel.setMvccVersion(RandomTestUtil.nextLong());

		ldapServerAttributeRel.setCompanyId(RandomTestUtil.nextLong());

		ldapServerAttributeRel.setLdapServerId(RandomTestUtil.nextLong());

		ldapServerAttributeRel.setClassNameId(RandomTestUtil.nextLong());

		ldapServerAttributeRel.setClassPK(RandomTestUtil.nextLong());

		_ldapServerAttributeRels.add(
			_persistence.update(ldapServerAttributeRel));

		return ldapServerAttributeRel;
	}

	private List<LDAPServerAttributeRel> _ldapServerAttributeRels =
		new ArrayList<LDAPServerAttributeRel>();
	private LDAPServerAttributeRelPersistence _persistence;
	private ClassLoader _dynamicQueryClassLoader;

}