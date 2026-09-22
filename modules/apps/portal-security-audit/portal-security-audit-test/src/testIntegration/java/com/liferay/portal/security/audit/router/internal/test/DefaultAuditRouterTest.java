/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.router.internal.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.test.util.CompanyConfigurationTemporarySwapper;
import com.liferay.portal.configuration.test.util.ConfigurationTemporarySwapper;
import com.liferay.portal.kernel.audit.AuditMessage;
import com.liferay.portal.kernel.audit.AuditRouter;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.security.audit.AuditMessageProcessor;
import com.liferay.portal.security.audit.configuration.AuditConfiguration;
import com.liferay.portal.security.audit.storage.constants.AuditPseudonymConstants;
import com.liferay.portal.security.audit.storage.model.AuditPseudonym;
import com.liferay.portal.security.audit.storage.service.AuditPseudonymLocalService;
import com.liferay.portal.test.log.LogCapture;
import com.liferay.portal.test.log.LogEntry;
import com.liferay.portal.test.log.LoggerTestUtil;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

/**
 * @author Christian Moura
 */
@RunWith(Arquillian.class)
public class DefaultAuditRouterTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@BeforeClass
	public static void setUpClass() throws Exception {
		_company = CompanyTestUtil.addCompany();
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		CompanyLocalServiceUtil.deleteCompany(_company.getCompanyId());
	}

	@Before
	public void setUp() throws Exception {
		Bundle bundle = FrameworkUtil.getBundle(getClass());

		BundleContext bundleContext = bundle.getBundleContext();

		_serviceRegistration = bundleContext.registerService(
			AuditMessageProcessor.class, _auditMessages::add,
			HashMapDictionaryBuilder.<String, Object>put(
				"eventTypes", "*"
			).build());
	}

	@After
	public void tearDown() throws Exception {
		_serviceRegistration.unregister();
	}

	@FeatureFlag("LPD-6417")
	@Test
	public void testRoute() throws Exception {
		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					new CompanyConfigurationTemporarySwapper(
						_company.getCompanyId(),
						AuditConfiguration.class.getName(),
						HashMapDictionaryBuilder.<String, Object>put(
							"enabled", false
						).build())) {

			Assert.assertFalse(
				_auditMessages.contains(_route(_company.getCompanyId())));
			Assert.assertTrue(
				_auditMessages.contains(
					_route(TestPropsValues.getCompanyId())));
		}

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper1 =
					new CompanyConfigurationTemporarySwapper(
						_company.getCompanyId(),
						AuditConfiguration.class.getName(),
						HashMapDictionaryBuilder.<String, Object>put(
							"enabled", true
						).build());
			CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper2 =
					new CompanyConfigurationTemporarySwapper(
						TestPropsValues.getCompanyId(),
						AuditConfiguration.class.getName(),
						HashMapDictionaryBuilder.<String, Object>put(
							"enabled", false
						).build())) {

			Assert.assertFalse(
				_auditMessages.contains(
					_route(TestPropsValues.getCompanyId())));
			Assert.assertTrue(
				_auditMessages.contains(_route(_company.getCompanyId())));
		}
	}

	@FeatureFlag("LPD-6417")
	@Test
	public void testRouteWhenAuditMessageIsPseudonymized() throws Exception {
		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					_getCompanyConfigurationTemporarySwapper()) {

			AuditMessage auditMessage = _createAuditMessage(
				_company.getCompanyId());

			_auditRouter.route(auditMessage);

			String json = String.valueOf(auditMessage.toJSONObject());

			long count = _getAuditPseudonymsCount(_company.getCompanyId());

			_auditRouter.route(auditMessage);

			Assert.assertEquals(
				json, String.valueOf(auditMessage.toJSONObject()));
			Assert.assertEquals(
				count, _getAuditPseudonymsCount(_company.getCompanyId()));
		}
	}

	@FeatureFlag(enable = false, value = "LPD-6417")
	@Test
	public void testRouteWhenFeatureFlagIsDisabled() throws Exception {
		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					new CompanyConfigurationTemporarySwapper(
						_company.getCompanyId(),
						AuditConfiguration.class.getName(),
						HashMapDictionaryBuilder.<String, Object>put(
							"enabled", false
						).build())) {

			AuditMessage auditMessage = _route(_company.getCompanyId());

			Assert.assertTrue(_auditMessages.contains(auditMessage));
			Assert.assertFalse(auditMessage.isPseudonymized());
		}

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					new CompanyConfigurationTemporarySwapper(
						_company.getCompanyId(),
						AuditConfiguration.class.getName(),
						HashMapDictionaryBuilder.<String, Object>put(
							"enabled", true
						).build());
			ConfigurationTemporarySwapper configurationTemporarySwapper =
				new ConfigurationTemporarySwapper(
					AuditConfiguration.class.getName(),
					HashMapDictionaryBuilder.<String, Object>put(
						"enabled", false
					).build())) {

			Assert.assertFalse(
				_auditMessages.contains(_route(_company.getCompanyId())));
		}
	}

	@FeatureFlag("LPD-6417")
	@Test
	public void testRouteWhenPseudonymizationFails() throws Exception {
		Bundle bundle = FrameworkUtil.getBundle(getClass());

		BundleContext bundleContext = bundle.getBundleContext();

		ServiceRegistration<?> serviceRegistration =
			bundleContext.registerService(
				ModelListener.class,
				new BaseModelListener<AuditPseudonym>() {

					@Override
					public void onBeforeCreate(AuditPseudonym auditPseudonym)
						throws ModelListenerException {

						throw new ModelListenerException();
					}

				},
				null);

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					_getCompanyConfigurationTemporarySwapper();
			LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				_CLASS_NAME_AUDIT_MESSAGE_PSEUDONYMIZER,
				LoggerTestUtil.ERROR)) {

			long count = _getAuditPseudonymsCount(_company.getCompanyId());

			AuditMessage auditMessage = _createAuditMessage(
				_company.getCompanyId());

			String classPK = String.valueOf(RandomTestUtil.randomLong());

			AuditMessage resourceAuditMessage = _createAuditMessage(
				_company.getCompanyId());

			resourceAuditMessage.setClassPK(classPK);

			_auditRouter.route(auditMessage);
			_auditRouter.route(resourceAuditMessage);

			Assert.assertEquals(StringPool.BLANK, auditMessage.getClassPK());
			Assert.assertEquals(classPK, resourceAuditMessage.getClassPK());

			for (AuditMessage curAuditMessage :
					new AuditMessage[] {auditMessage, resourceAuditMessage}) {

				Assert.assertTrue(_auditMessages.contains(curAuditMessage));
				Assert.assertEquals(
					StringPool.BLANK, curAuditMessage.getClientHost());
				Assert.assertEquals(
					StringPool.BLANK, curAuditMessage.getClientIP());
				Assert.assertEquals(
					StringPool.BLANK, curAuditMessage.getClientIPReference());
				Assert.assertEquals(
					StringPool.BLANK,
					curAuditMessage.getImpersonatedUserEmailAddress());
				Assert.assertEquals(0, curAuditMessage.getImpersonatedUserId());
				Assert.assertEquals(
					StringPool.BLANK,
					curAuditMessage.getImpersonatedUserName());
				Assert.assertEquals(
					StringPool.BLANK, curAuditMessage.getObjectName());
				Assert.assertTrue(curAuditMessage.isPseudonymizationFailed());
				Assert.assertTrue(curAuditMessage.isPseudonymized());
				Assert.assertEquals(
					StringPool.BLANK, curAuditMessage.getUserEmailAddress());
				Assert.assertEquals(0, curAuditMessage.getUserId());
				Assert.assertEquals(
					StringPool.BLANK, curAuditMessage.getUserLogin());
				Assert.assertEquals(
					StringPool.BLANK, curAuditMessage.getUserName());
			}

			Assert.assertEquals(
				count, _getAuditPseudonymsCount(_company.getCompanyId()));

			List<LogEntry> logEntries = logCapture.getLogEntries();

			Assert.assertEquals(logEntries.toString(), 2, logEntries.size());
		}
		finally {
			serviceRegistration.unregister();
		}
	}

	@FeatureFlag("LPD-6417")
	@Test
	public void testRouteWhenPseudonymizationIsEnabled() throws Exception {
		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					_getCompanyConfigurationTemporarySwapper()) {

			AuditMessage auditMessage = _createAuditMessage(
				_company.getCompanyId());

			AuditMessage originalAuditMessage = new AuditMessage(
				String.valueOf(auditMessage.toJSONObject()));

			long count = _getAuditPseudonymsCount(_company.getCompanyId());

			_auditRouter.route(auditMessage);

			Assert.assertTrue(_auditMessages.contains(auditMessage));
			Assert.assertEquals(
				String.valueOf(auditMessage.getUserId()),
				auditMessage.getClassPK());
			Assert.assertEquals(
				auditMessage.getClientIP(), auditMessage.getClientHost());

			String clientIP = originalAuditMessage.getClientIP();

			Assert.assertEquals(
				clientIP.substring(0, clientIP.lastIndexOf(CharPool.PERIOD)) +
					".0/24",
				auditMessage.getClientIP());

			Assert.assertFalse(auditMessage.isPseudonymizationFailed());
			Assert.assertTrue(auditMessage.isPseudonymized());
			Assert.assertEquals(
				auditMessage.getUserEmailAddress(),
				auditMessage.getUserLogin());

			_assertAuditPseudonym(
				AuditPseudonymConstants.FIELD_CATEGORY_CLIENT_IP, clientIP,
				auditMessage.getClientIPReference());
			_assertAuditPseudonym(
				AuditPseudonymConstants.FIELD_CATEGORY_OBJECT_NAME,
				originalAuditMessage.getObjectName(),
				auditMessage.getObjectName());
			_assertAuditPseudonym(
				AuditPseudonymConstants.FIELD_CATEGORY_USER_EMAIL_ADDRESS,
				originalAuditMessage.getImpersonatedUserEmailAddress(),
				auditMessage.getImpersonatedUserEmailAddress());
			_assertAuditPseudonym(
				AuditPseudonymConstants.FIELD_CATEGORY_USER_EMAIL_ADDRESS,
				originalAuditMessage.getUserEmailAddress(),
				auditMessage.getUserEmailAddress());
			_assertAuditPseudonym(
				AuditPseudonymConstants.FIELD_CATEGORY_USER_ID,
				String.valueOf(originalAuditMessage.getImpersonatedUserId()),
				String.valueOf(auditMessage.getImpersonatedUserId()));
			_assertAuditPseudonym(
				AuditPseudonymConstants.FIELD_CATEGORY_USER_ID,
				String.valueOf(originalAuditMessage.getUserId()),
				String.valueOf(auditMessage.getUserId()));
			_assertAuditPseudonym(
				AuditPseudonymConstants.FIELD_CATEGORY_USER_NAME,
				originalAuditMessage.getImpersonatedUserName(),
				auditMessage.getImpersonatedUserName());
			_assertAuditPseudonym(
				AuditPseudonymConstants.FIELD_CATEGORY_USER_NAME,
				originalAuditMessage.getUserName(), auditMessage.getUserName());

			Assert.assertEquals(
				count + 8, _getAuditPseudonymsCount(_company.getCompanyId()));

			_auditRouter.route(originalAuditMessage);

			Assert.assertEquals(
				String.valueOf(auditMessage.toJSONObject()),
				String.valueOf(originalAuditMessage.toJSONObject()));
			Assert.assertEquals(
				count + 8, _getAuditPseudonymsCount(_company.getCompanyId()));

			AuditMessage impersonatedAuditMessage = _createAuditMessage(
				_company.getCompanyId());

			impersonatedAuditMessage.setClassPK(
				String.valueOf(
					impersonatedAuditMessage.getImpersonatedUserId()));
			impersonatedAuditMessage.setUserLogin(
				String.valueOf(impersonatedAuditMessage.getUserId()));

			_auditRouter.route(impersonatedAuditMessage);

			Assert.assertEquals(
				String.valueOf(
					impersonatedAuditMessage.getImpersonatedUserId()),
				impersonatedAuditMessage.getClassPK());
			Assert.assertEquals(
				String.valueOf(impersonatedAuditMessage.getUserId()),
				impersonatedAuditMessage.getUserLogin());
		}
	}

	@FeatureFlag("LPD-6417")
	@Test
	public void testRouteWhenPseudonymizationIsEnabledWithClientIP()
		throws Exception {

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					_getCompanyConfigurationTemporarySwapper()) {

			_testRouteWhenPseudonymizationIsEnabledWithClientIP(
				StringPool.BLANK, StringPool.BLANK);
			_testRouteWhenPseudonymizationIsEnabledWithClientIP(
				" 2001:db8::1 ", "2001:db8:0:0:0:0:0:0/64");
			_testRouteWhenPseudonymizationIsEnabledWithClientIP(
				"192.168.1.10", "192.168.1.0/24");
			_testRouteWhenPseudonymizationIsEnabledWithClientIP(
				"2001:db8:1:2:3:4:5:6", "2001:db8:1:2:0:0:0:0/64");
			_testRouteWhenPseudonymizationIsEnabledWithClientIP(
				"::ffff:192.168.1.10", "192.168.1.0/24");
			_testRouteWhenPseudonymizationIsEnabledWithClientIP(
				"::ffff:192.168.1.10%x", "192.168.1.0/24");
			_testRouteWhenPseudonymizationIsEnabledWithClientIP(
				"[ 2001:db8::1 ]", "2001:db8:0:0:0:0:0:0/64");
			_testRouteWhenPseudonymizationIsEnabledWithClientIP(
				"[2001:db8::1]", "2001:db8:0:0:0:0:0:0/64");
			_testRouteWhenPseudonymizationIsEnabledWithClientIP(
				"[fe80::1%x]", "fe80:0:0:0:0:0:0:0/64");
			_testRouteWhenPseudonymizationIsEnabledWithClientIP(
				"example.com:80", StringPool.BLANK);
			_testRouteWhenPseudonymizationIsEnabledWithClientIP(
				"fe80::1%x", "fe80:0:0:0:0:0:0:0/64");
			_testRouteWhenPseudonymizationIsEnabledWithClientIP(
				"not an address", StringPool.BLANK);
			_testRouteWhenPseudonymizationIsEnabledWithClientIP(null, null);
		}
	}

	@FeatureFlag("LPD-6417")
	@Test
	public void testRouteWhenPseudonymizationIsEnabledWithUnmatchedValues()
		throws Exception {

		try (CompanyConfigurationTemporarySwapper
				companyConfigurationTemporarySwapper =
					_getCompanyConfigurationTemporarySwapper()) {

			AuditMessage auditMessage = new AuditMessage(
				_company.getCompanyId(), 0, StringPool.BLANK,
				RandomTestUtil.randomString());

			String classPK = String.valueOf(RandomTestUtil.randomLong());

			auditMessage.setClassPK(classPK);

			auditMessage.setClientHost(RandomTestUtil.randomString());
			auditMessage.setClientIP(_randomClientIP());
			auditMessage.setUserLogin(RandomTestUtil.randomString());

			long count = _getAuditPseudonymsCount(_company.getCompanyId());

			_auditRouter.route(auditMessage);

			Assert.assertEquals(classPK, auditMessage.getClassPK());
			Assert.assertEquals(StringPool.BLANK, auditMessage.getClientHost());
			Assert.assertEquals(
				count + 1, _getAuditPseudonymsCount(_company.getCompanyId()));
			Assert.assertEquals(0, auditMessage.getUserId());
			Assert.assertEquals(StringPool.BLANK, auditMessage.getUserLogin());
			Assert.assertEquals(StringPool.BLANK, auditMessage.getUserName());

			for (String userName : new String[] {" ", "null"}) {
				AuditMessage userNameAuditMessage = new AuditMessage(
					_company.getCompanyId(), 0, userName,
					RandomTestUtil.randomString());

				_auditRouter.route(userNameAuditMessage);

				_assertAuditPseudonym(
					AuditPseudonymConstants.FIELD_CATEGORY_USER_NAME, userName,
					userNameAuditMessage.getUserName());
			}
		}
	}

	private void _assertAuditPseudonym(
		String fieldCategory, String value, String token) {

		AuditPseudonym auditPseudonym =
			_auditPseudonymLocalService.fetchAuditPseudonym(
				GetterUtil.getLong(token));

		Assert.assertEquals(
			_company.getCompanyId(), auditPseudonym.getCompanyId());
		Assert.assertEquals(fieldCategory, auditPseudonym.getFieldCategory());
		Assert.assertEquals(value, auditPseudonym.getValue());
	}

	private AuditMessage _createAuditMessage(long companyId) {
		long userId = RandomTestUtil.randomLong();

		AuditMessage auditMessage = new AuditMessage(
			companyId, userId, RandomTestUtil.randomString(),
			RandomTestUtil.randomString());

		auditMessage.setClassName(RandomTestUtil.randomString());
		auditMessage.setClassPK(String.valueOf(userId));

		String clientIP = _randomClientIP();

		auditMessage.setClientHost(clientIP);
		auditMessage.setClientIP(clientIP);

		auditMessage.setImpersonatedUserEmailAddress(
			RandomTestUtil.randomString());
		auditMessage.setImpersonatedUserId(RandomTestUtil.randomLong());
		auditMessage.setImpersonatedUserName(RandomTestUtil.randomString());
		auditMessage.setMessage(RandomTestUtil.randomString());
		auditMessage.setObjectName(RandomTestUtil.randomString());

		String userEmailAddress = RandomTestUtil.randomString();

		auditMessage.setUserEmailAddress(userEmailAddress);
		auditMessage.setUserLogin(userEmailAddress);

		return auditMessage;
	}

	private long _getAuditPseudonymsCount(long companyId) {
		DynamicQuery dynamicQuery = _auditPseudonymLocalService.dynamicQuery();

		dynamicQuery.add(RestrictionsFactoryUtil.eq("companyId", companyId));

		return _auditPseudonymLocalService.dynamicQueryCount(dynamicQuery);
	}

	private CompanyConfigurationTemporarySwapper
			_getCompanyConfigurationTemporarySwapper()
		throws Exception {

		return new CompanyConfigurationTemporarySwapper(
			_company.getCompanyId(), AuditConfiguration.class.getName(),
			HashMapDictionaryBuilder.<String, Object>put(
				"enabled", true
			).put(
				"pseudonymizationEnabled", true
			).build());
	}

	private String _randomClientIP() {
		return StringBundler.concat(
			RandomTestUtil.randomInt(1, 223), StringPool.PERIOD,
			RandomTestUtil.randomInt(0, 255), StringPool.PERIOD,
			RandomTestUtil.randomInt(0, 255), StringPool.PERIOD,
			RandomTestUtil.randomInt(1, 254));
	}

	private AuditMessage _route(long companyId) throws Exception {
		AuditMessage auditMessage = new AuditMessage(
			companyId, RandomTestUtil.randomLong(),
			RandomTestUtil.randomString(), RandomTestUtil.randomString());

		_auditRouter.route(auditMessage);

		return auditMessage;
	}

	private void _testRouteWhenPseudonymizationIsEnabledWithClientIP(
			String clientIP, String expectedClientIP)
		throws Exception {

		AuditMessage auditMessage = _createAuditMessage(
			_company.getCompanyId());

		auditMessage.setClientHost(clientIP);
		auditMessage.setClientIP(clientIP);

		_auditRouter.route(auditMessage);

		Assert.assertEquals(expectedClientIP, auditMessage.getClientHost());
		Assert.assertEquals(expectedClientIP, auditMessage.getClientIP());
		Assert.assertFalse(auditMessage.isPseudonymizationFailed());
	}

	private static final String _CLASS_NAME_AUDIT_MESSAGE_PSEUDONYMIZER =
		"com.liferay.portal.security.audit.router.internal." +
			"AuditMessagePseudonymizer";

	private static Company _company;

	private final List<AuditMessage> _auditMessages = new ArrayList<>();

	@Inject
	private AuditPseudonymLocalService _auditPseudonymLocalService;

	@Inject
	private AuditRouter _auditRouter;

	private ServiceRegistration<AuditMessageProcessor> _serviceRegistration;

}