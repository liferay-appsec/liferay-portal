/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.storage.service.persistence.test;

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
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.security.audit.storage.exception.NoSuchPseudonymException;
import com.liferay.portal.security.audit.storage.model.AuditPseudonym;
import com.liferay.portal.security.audit.storage.service.AuditPseudonymLocalServiceUtil;
import com.liferay.portal.security.audit.storage.service.persistence.AuditPseudonymPersistence;
import com.liferay.portal.security.audit.storage.service.persistence.AuditPseudonymUtil;
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
public class AuditPseudonymPersistenceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(), PersistenceTestRule.INSTANCE,
			new TransactionalTestRule(
				Propagation.REQUIRED,
				"com.liferay.portal.security.audit.storage.service"));

	@Before
	public void setUp() {
		_persistence = AuditPseudonymUtil.getPersistence();

		Class<?> clazz = _persistence.getClass();

		_dynamicQueryClassLoader = clazz.getClassLoader();
	}

	@After
	public void tearDown() throws Exception {
		Iterator<AuditPseudonym> iterator = _auditPseudonyms.iterator();

		while (iterator.hasNext()) {
			_persistence.remove(iterator.next());

			iterator.remove();
		}
	}

	@Test
	public void testCreate() throws Exception {
		long pk = RandomTestUtil.nextLong();

		AuditPseudonym auditPseudonym = _persistence.create(pk);

		Assert.assertNotNull(auditPseudonym);

		Assert.assertEquals(auditPseudonym.getPrimaryKey(), pk);
	}

	@Test
	public void testRemove() throws Exception {
		AuditPseudonym newAuditPseudonym = addAuditPseudonym();

		_persistence.remove(newAuditPseudonym);

		AuditPseudonym existingAuditPseudonym = _persistence.fetchByPrimaryKey(
			newAuditPseudonym.getPrimaryKey());

		Assert.assertNull(existingAuditPseudonym);
	}

	@Test
	public void testUpdateNew() throws Exception {
		addAuditPseudonym();
	}

	@Test
	public void testUpdateExisting() throws Exception {
		AuditPseudonym newAuditPseudonym = addAuditPseudonym();

		newAuditPseudonym.setCompanyId(RandomTestUtil.nextLong());

		newAuditPseudonym.setCreateDate(RandomTestUtil.nextDate());

		newAuditPseudonym.setContextName(RandomTestUtil.randomString());

		newAuditPseudonym.setFieldCategory(RandomTestUtil.randomString());

		newAuditPseudonym.setValue(RandomTestUtil.randomString());

		newAuditPseudonym.setValueHash(RandomTestUtil.randomString());

		newAuditPseudonym = _persistence.update(newAuditPseudonym);

		_auditPseudonyms.add(newAuditPseudonym);

		AuditPseudonym existingAuditPseudonym = _persistence.findByPrimaryKey(
			newAuditPseudonym.getPrimaryKey());

		Assert.assertEquals(
			existingAuditPseudonym.getAuditPseudonymId(),
			newAuditPseudonym.getAuditPseudonymId());
		Assert.assertEquals(
			existingAuditPseudonym.getCompanyId(),
			newAuditPseudonym.getCompanyId());
		Assert.assertEquals(
			Time.getShortTimestamp(existingAuditPseudonym.getCreateDate()),
			Time.getShortTimestamp(newAuditPseudonym.getCreateDate()));
		Assert.assertEquals(
			existingAuditPseudonym.getContextName(),
			newAuditPseudonym.getContextName());
		Assert.assertEquals(
			existingAuditPseudonym.getFieldCategory(),
			newAuditPseudonym.getFieldCategory());
		Assert.assertEquals(
			existingAuditPseudonym.getValue(), newAuditPseudonym.getValue());
		Assert.assertEquals(
			existingAuditPseudonym.getValueHash(),
			newAuditPseudonym.getValueHash());
	}

	@Test
	public void testCountByC_C_FC_VH() throws Exception {
		_persistence.countByC_C_FC_VH(RandomTestUtil.nextLong(), "", "", "");

		_persistence.countByC_C_FC_VH(0L, "null", "null", "null");

		_persistence.countByC_C_FC_VH(
			0L, (String)null, (String)null, (String)null);
	}

	@Test
	public void testFindByPrimaryKeyExisting() throws Exception {
		AuditPseudonym newAuditPseudonym = addAuditPseudonym();

		AuditPseudonym existingAuditPseudonym = _persistence.findByPrimaryKey(
			newAuditPseudonym.getPrimaryKey());

		Assert.assertEquals(existingAuditPseudonym, newAuditPseudonym);
	}

	@Test(expected = NoSuchPseudonymException.class)
	public void testFindByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		_persistence.findByPrimaryKey(pk);
	}

	@Test
	public void testFindAll() throws Exception {
		_persistence.findAll(
			QueryUtil.ALL_POS, QueryUtil.ALL_POS, getOrderByComparator());
	}

	protected OrderByComparator<AuditPseudonym> getOrderByComparator() {
		return OrderByComparatorFactoryUtil.create(
			"Audit_AuditPseudonym", "auditPseudonymId", true, "companyId", true,
			"createDate", true, "contextName", true, "fieldCategory", true,
			"value", true, "valueHash", true);
	}

	@Test
	public void testFetchByPrimaryKeyExisting() throws Exception {
		AuditPseudonym newAuditPseudonym = addAuditPseudonym();

		AuditPseudonym existingAuditPseudonym = _persistence.fetchByPrimaryKey(
			newAuditPseudonym.getPrimaryKey());

		Assert.assertEquals(existingAuditPseudonym, newAuditPseudonym);
	}

	@Test
	public void testFetchByPrimaryKeyMissing() throws Exception {
		long pk = RandomTestUtil.nextLong();

		AuditPseudonym missingAuditPseudonym = _persistence.fetchByPrimaryKey(
			pk);

		Assert.assertNull(missingAuditPseudonym);
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereAllPrimaryKeysExist()
		throws Exception {

		AuditPseudonym newAuditPseudonym1 = addAuditPseudonym();
		AuditPseudonym newAuditPseudonym2 = addAuditPseudonym();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newAuditPseudonym1.getPrimaryKey());
		primaryKeys.add(newAuditPseudonym2.getPrimaryKey());

		Map<Serializable, AuditPseudonym> auditPseudonyms =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(2, auditPseudonyms.size());
		Assert.assertEquals(
			newAuditPseudonym1,
			auditPseudonyms.get(newAuditPseudonym1.getPrimaryKey()));
		Assert.assertEquals(
			newAuditPseudonym2,
			auditPseudonyms.get(newAuditPseudonym2.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereNoPrimaryKeysExist()
		throws Exception {

		long pk1 = RandomTestUtil.nextLong();

		long pk2 = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(pk1);
		primaryKeys.add(pk2);

		Map<Serializable, AuditPseudonym> auditPseudonyms =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(auditPseudonyms.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithMultiplePrimaryKeysWhereSomePrimaryKeysExist()
		throws Exception {

		AuditPseudonym newAuditPseudonym = addAuditPseudonym();

		long pk = RandomTestUtil.nextLong();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newAuditPseudonym.getPrimaryKey());
		primaryKeys.add(pk);

		Map<Serializable, AuditPseudonym> auditPseudonyms =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, auditPseudonyms.size());
		Assert.assertEquals(
			newAuditPseudonym,
			auditPseudonyms.get(newAuditPseudonym.getPrimaryKey()));
	}

	@Test
	public void testFetchByPrimaryKeysWithNoPrimaryKeys() throws Exception {
		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		Map<Serializable, AuditPseudonym> auditPseudonyms =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertTrue(auditPseudonyms.isEmpty());
	}

	@Test
	public void testFetchByPrimaryKeysWithOnePrimaryKey() throws Exception {
		AuditPseudonym newAuditPseudonym = addAuditPseudonym();

		Set<Serializable> primaryKeys = new HashSet<Serializable>();

		primaryKeys.add(newAuditPseudonym.getPrimaryKey());

		Map<Serializable, AuditPseudonym> auditPseudonyms =
			_persistence.fetchByPrimaryKeys(primaryKeys);

		Assert.assertEquals(1, auditPseudonyms.size());
		Assert.assertEquals(
			newAuditPseudonym,
			auditPseudonyms.get(newAuditPseudonym.getPrimaryKey()));
	}

	@Test
	public void testActionableDynamicQuery() throws Exception {
		final IntegerWrapper count = new IntegerWrapper();

		ActionableDynamicQuery actionableDynamicQuery =
			AuditPseudonymLocalServiceUtil.getActionableDynamicQuery();

		actionableDynamicQuery.setPerformActionMethod(
			new ActionableDynamicQuery.PerformActionMethod<AuditPseudonym>() {

				@Override
				public void performAction(AuditPseudonym auditPseudonym) {
					Assert.assertNotNull(auditPseudonym);

					count.increment();
				}

			});

		actionableDynamicQuery.performActions();

		Assert.assertEquals(count.getValue(), _persistence.countAll());
	}

	@Test
	public void testDynamicQueryByPrimaryKeyExisting() throws Exception {
		AuditPseudonym newAuditPseudonym = addAuditPseudonym();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			AuditPseudonym.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"auditPseudonymId", newAuditPseudonym.getAuditPseudonymId()));

		List<AuditPseudonym> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		Assert.assertEquals(1, result.size());

		AuditPseudonym existingAuditPseudonym = result.get(0);

		Assert.assertEquals(existingAuditPseudonym, newAuditPseudonym);
	}

	@Test
	public void testDynamicQueryByPrimaryKeyMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			AuditPseudonym.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"auditPseudonymId", RandomTestUtil.nextLong()));

		List<AuditPseudonym> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testDynamicQueryByProjectionExisting() throws Exception {
		AuditPseudonym newAuditPseudonym = addAuditPseudonym();

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			AuditPseudonym.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("auditPseudonymId"));

		Object newAuditPseudonymId = newAuditPseudonym.getAuditPseudonymId();

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"auditPseudonymId", new Object[] {newAuditPseudonymId}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(1, result.size());

		Object existingAuditPseudonymId = result.get(0);

		Assert.assertEquals(existingAuditPseudonymId, newAuditPseudonymId);
	}

	@Test
	public void testDynamicQueryByProjectionMissing() throws Exception {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			AuditPseudonym.class, _dynamicQueryClassLoader);

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("auditPseudonymId"));

		dynamicQuery.add(
			RestrictionsFactoryUtil.in(
				"auditPseudonymId", new Object[] {RandomTestUtil.nextLong()}));

		List<Object> result = _persistence.findWithDynamicQuery(dynamicQuery);

		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testResetOriginalValues() throws Exception {
		AuditPseudonym newAuditPseudonym = addAuditPseudonym();

		_persistence.clearCache();

		_assertOriginalValues(
			_persistence.findByPrimaryKey(newAuditPseudonym.getPrimaryKey()));
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

		AuditPseudonym newAuditPseudonym = addAuditPseudonym();

		if (clearSession) {
			Session session = _persistence.openSession();

			session.flush();

			session.clear();
		}

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			AuditPseudonym.class, _dynamicQueryClassLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"auditPseudonymId", newAuditPseudonym.getAuditPseudonymId()));

		List<AuditPseudonym> result = _persistence.findWithDynamicQuery(
			dynamicQuery);

		_assertOriginalValues(result.get(0));
	}

	private void _assertOriginalValues(AuditPseudonym auditPseudonym) {
		Assert.assertEquals(
			Long.valueOf(auditPseudonym.getCompanyId()),
			ReflectionTestUtil.<Long>invoke(
				auditPseudonym, "getColumnOriginalValue",
				new Class<?>[] {String.class}, "companyId"));
		Assert.assertEquals(
			auditPseudonym.getContextName(),
			ReflectionTestUtil.invoke(
				auditPseudonym, "getColumnOriginalValue",
				new Class<?>[] {String.class}, "contextName"));
		Assert.assertEquals(
			auditPseudonym.getFieldCategory(),
			ReflectionTestUtil.invoke(
				auditPseudonym, "getColumnOriginalValue",
				new Class<?>[] {String.class}, "fieldCategory"));
		Assert.assertEquals(
			auditPseudonym.getValueHash(),
			ReflectionTestUtil.invoke(
				auditPseudonym, "getColumnOriginalValue",
				new Class<?>[] {String.class}, "valueHash"));
	}

	protected AuditPseudonym addAuditPseudonym() throws Exception {
		long pk = RandomTestUtil.nextLong();

		AuditPseudonym auditPseudonym = _persistence.create(pk);

		auditPseudonym.setCompanyId(RandomTestUtil.nextLong());

		auditPseudonym.setCreateDate(RandomTestUtil.nextDate());

		auditPseudonym.setContextName(RandomTestUtil.randomString());

		auditPseudonym.setFieldCategory(RandomTestUtil.randomString());

		auditPseudonym.setValue(RandomTestUtil.randomString());

		auditPseudonym.setValueHash(RandomTestUtil.randomString());

		_auditPseudonyms.add(_persistence.update(auditPseudonym));

		return auditPseudonym;
	}

	private List<AuditPseudonym> _auditPseudonyms =
		new ArrayList<AuditPseudonym>();
	private AuditPseudonymPersistence _persistence;
	private ClassLoader _dynamicQueryClassLoader;

}
// LIFERAY-SERVICE-BUILDER-HASH:-1645036872