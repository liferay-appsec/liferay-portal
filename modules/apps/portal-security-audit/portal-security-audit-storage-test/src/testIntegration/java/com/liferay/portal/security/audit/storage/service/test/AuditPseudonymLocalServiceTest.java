/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.storage.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.test.AssertUtils;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.kernel.util.DigesterUtil;
import com.liferay.portal.security.audit.storage.exception.AuditPseudonymValueException;
import com.liferay.portal.security.audit.storage.model.AuditPseudonym;
import com.liferay.portal.security.audit.storage.service.AuditPseudonymLocalService;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.TransactionalTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Christian Moura
 */
@RunWith(Arquillian.class)
public class AuditPseudonymLocalServiceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			new TransactionalTestRule(
				Propagation.REQUIRED,
				"com.liferay.portal.security.audit.storage.service"));

	@Test
	public void testAddAuditPseudonym() throws Exception {
		_testAddAuditPseudonym(StringPool.BLANK);
		_testAddAuditPseudonym("INSTANCE");
		_testAddAuditPseudonym(null);

		String contextName = RandomTestUtil.randomString();
		String value = RandomTestUtil.randomString();

		AuditPseudonym auditPseudonym =
			_auditPseudonymLocalService.addAuditPseudonym(
				TestPropsValues.getCompanyId(), contextName, _FIELD_CATEGORY,
				value);

		Assert.assertEquals(contextName, auditPseudonym.getContextName());

		AuditPseudonym curAuditPseudonym =
			_auditPseudonymLocalService.addAuditPseudonym(
				TestPropsValues.getCompanyId(), contextName, _FIELD_CATEGORY,
				value);

		Assert.assertEquals(
			auditPseudonym.getAuditPseudonymId(),
			curAuditPseudonym.getAuditPseudonymId());

		_addAuditPseudonym(value);

		DynamicQuery dynamicQuery = _auditPseudonymLocalService.dynamicQuery();

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"companyId", TestPropsValues.getCompanyId()));
		dynamicQuery.add(
			RestrictionsFactoryUtil.eq("fieldCategory", _FIELD_CATEGORY));
		dynamicQuery.add(RestrictionsFactoryUtil.eq("value", value));

		Assert.assertEquals(
			2, _auditPseudonymLocalService.dynamicQueryCount(dynamicQuery));

		_testAddAuditPseudonymWithInvalidValue(
			"Maximum length of value exceeded",
			RandomTestUtil.randomString(256));
		_testAddAuditPseudonymWithInvalidValue(
			"Value is blank", StringPool.BLANK);
		_testAddAuditPseudonymWithInvalidValue("Value is blank", null);

		_testAddAuditPseudonymWithSimilarValues("CASEY");
		_testAddAuditPseudonymWithSimilarValues("Casey");
		_testAddAuditPseudonymWithSimilarValues("Jose");
		_testAddAuditPseudonymWithSimilarValues("José");
		_testAddAuditPseudonymWithSimilarValues("casey");
		_testAddAuditPseudonymWithSimilarValues("casey ");
	}

	private AuditPseudonym _addAuditPseudonym(String value) throws Exception {
		return _auditPseudonymLocalService.addAuditPseudonym(
			TestPropsValues.getCompanyId(), null, _FIELD_CATEGORY, value);
	}

	private void _testAddAuditPseudonym(String contextName) throws Exception {
		String value = RandomTestUtil.randomString();

		AuditPseudonym auditPseudonym =
			_auditPseudonymLocalService.addAuditPseudonym(
				TestPropsValues.getCompanyId(), contextName, _FIELD_CATEGORY,
				value);

		Assert.assertEquals("INSTANCE", auditPseudonym.getContextName());
		Assert.assertEquals(
			DigesterUtil.digestHex(DigesterUtil.SHA_256, value),
			auditPseudonym.getValueHash());

		AuditPseudonym curAuditPseudonym = _addAuditPseudonym(value);

		Assert.assertEquals(
			auditPseudonym.getAuditPseudonymId(),
			curAuditPseudonym.getAuditPseudonymId());
	}

	private void _testAddAuditPseudonymWithInvalidValue(
		String expectedMessage, String value) {

		AssertUtils.assertFailure(
			AuditPseudonymValueException.class, expectedMessage,
			() -> _addAuditPseudonym(value));
	}

	private void _testAddAuditPseudonymWithSimilarValues(String value)
		throws Exception {

		AuditPseudonym auditPseudonym = _addAuditPseudonym(value);

		Assert.assertEquals(value, auditPseudonym.getValue());
	}

	private static final String _FIELD_CATEGORY = RandomTestUtil.randomString();

	@Inject
	private AuditPseudonymLocalService _auditPseudonymLocalService;

}