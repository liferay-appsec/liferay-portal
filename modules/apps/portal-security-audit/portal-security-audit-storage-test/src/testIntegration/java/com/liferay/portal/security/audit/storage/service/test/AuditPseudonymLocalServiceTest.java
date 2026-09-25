/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.storage.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.auth.CompanyInheritableThreadLocalCallable;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.AssertUtils;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.kernel.transaction.TransactionConfig;
import com.liferay.portal.kernel.transaction.TransactionInvokerUtil;
import com.liferay.portal.kernel.util.DigesterUtil;
import com.liferay.portal.security.audit.storage.exception.AuditPseudonymIdentityValueException;
import com.liferay.portal.security.audit.storage.model.AuditPseudonym;
import com.liferay.portal.security.audit.storage.service.AuditPseudonymLocalService;
import com.liferay.portal.test.log.LogCapture;
import com.liferay.portal.test.log.LogEntry;
import com.liferay.portal.test.log.LoggerTestUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
		new LiferayIntegrationTestRule();

	@Test
	public void testGetOrAddAuditPseudonym() throws Exception {
		_testGetOrAddAuditPseudonym(StringPool.BLANK);
		_testGetOrAddAuditPseudonym(_CONTEXT_NAME_DEFAULT);
		_testGetOrAddAuditPseudonym(null);

		String contextName = RandomTestUtil.randomString();
		String identityValue = RandomTestUtil.randomString();

		AuditPseudonym auditPseudonym =
			_auditPseudonymLocalService.getOrAddAuditPseudonym(
				TestPropsValues.getCompanyId(), contextName, _FIELD_CATEGORY,
				identityValue);

		Assert.assertEquals(contextName, auditPseudonym.getContextName());

		AuditPseudonym curAuditPseudonym =
			_auditPseudonymLocalService.getOrAddAuditPseudonym(
				TestPropsValues.getCompanyId(), contextName, _FIELD_CATEGORY,
				identityValue);

		Assert.assertEquals(
			auditPseudonym.getAuditPseudonymId(),
			curAuditPseudonym.getAuditPseudonymId());

		_getOrAddAuditPseudonym(identityValue);

		Assert.assertEquals(2, _getAuditPseudonymsCount(identityValue));
	}

	@Test
	public void testGetOrAddAuditPseudonymConcurrently() throws Exception {
		ExecutorService executorService = Executors.newFixedThreadPool(
			_THREAD_COUNT);

		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				"org.hibernate.engine.jdbc.spi.SqlExceptionHelper",
				LoggerTestUtil.WARN)) {

			CyclicBarrier cyclicBarrier = new CyclicBarrier(_THREAD_COUNT);
			List<Future<AuditPseudonym>> futures = new ArrayList<>();
			String identityValue = RandomTestUtil.randomString();

			for (int i = 0; i < _THREAD_COUNT; i++) {
				futures.add(
					executorService.submit(
						new CompanyInheritableThreadLocalCallable<>(
							() -> {
								cyclicBarrier.await();

								return _invoke(
									() -> {
										AuditPseudonym auditPseudonym =
											_getOrAddAuditPseudonym(
												identityValue);
										AuditPseudonym curAuditPseudonym =
											_getOrAddAuditPseudonym(
												identityValue);

										Assert.assertEquals(
											auditPseudonym.
												getAuditPseudonymId(),
											curAuditPseudonym.
												getAuditPseudonymId());

										return auditPseudonym;
									});
							})));
			}

			Future<AuditPseudonym> future = futures.get(0);

			AuditPseudonym auditPseudonym = future.get();

			for (Future<AuditPseudonym> curFuture : futures) {
				AuditPseudonym curAuditPseudonym = curFuture.get();

				Assert.assertEquals(
					auditPseudonym.getAuditPseudonymId(),
					curAuditPseudonym.getAuditPseudonymId());
			}

			Assert.assertEquals(1, _getAuditPseudonymsCount(identityValue));

			List<LogEntry> logEntries = logCapture.getLogEntries();

			Assert.assertTrue(logEntries.isEmpty());
		}
		finally {
			executorService.shutdownNow();
		}
	}

	@Test
	public void testGetOrAddAuditPseudonymWithInvalidIdentityValue()
		throws Exception {

		_testGetOrAddAuditPseudonymWithInvalidIdentityValue(
			"Identity value is blank", StringPool.BLANK);
		_testGetOrAddAuditPseudonymWithInvalidIdentityValue(
			"Identity value is blank", null);
		_testGetOrAddAuditPseudonymWithInvalidIdentityValue(
			"Maximum length of identity value exceeded",
			RandomTestUtil.randomString(256));
	}

	@Test
	public void testGetOrAddAuditPseudonymWithPendingUserUpdate()
		throws Exception {

		User user = UserTestUtil.addUser();

		String identityValue = RandomTestUtil.randomString();

		AuditPseudonym auditPseudonym = _invoke(
			() -> {
				User detachedUser = _userLocalService.getUser(user.getUserId());

				detachedUser.setJobTitle(RandomTestUtil.randomString());

				_userLocalService.updateUser(detachedUser);

				AuditPseudonym curAuditPseudonym = _getOrAddAuditPseudonym(
					identityValue);

				detachedUser.setJobTitle(RandomTestUtil.randomString());

				_userLocalService.updateUser(detachedUser);

				return curAuditPseudonym;
			});

		auditPseudonym = _auditPseudonymLocalService.fetchAuditPseudonym(
			auditPseudonym.getAuditPseudonymId());

		Assert.assertEquals(identityValue, auditPseudonym.getIdentityValue());
	}

	@Test
	public void testGetOrAddAuditPseudonymWithSimilarIdentityValues()
		throws Exception {

		_testGetOrAddAuditPseudonymWithSimilarIdentityValues("CASEY");
		_testGetOrAddAuditPseudonymWithSimilarIdentityValues("Casey");
		_testGetOrAddAuditPseudonymWithSimilarIdentityValues("Jose");
		_testGetOrAddAuditPseudonymWithSimilarIdentityValues("José");
		_testGetOrAddAuditPseudonymWithSimilarIdentityValues("casey");
		_testGetOrAddAuditPseudonymWithSimilarIdentityValues("casey ");
	}

	private long _getAuditPseudonymsCount(String identityValue)
		throws Exception {

		DynamicQuery dynamicQuery = _auditPseudonymLocalService.dynamicQuery();

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"companyId", TestPropsValues.getCompanyId()));
		dynamicQuery.add(
			RestrictionsFactoryUtil.eq("fieldCategory", _FIELD_CATEGORY));
		dynamicQuery.add(
			RestrictionsFactoryUtil.eq("identityValue", identityValue));

		return _auditPseudonymLocalService.dynamicQueryCount(dynamicQuery);
	}

	private AuditPseudonym _getOrAddAuditPseudonym(String identityValue)
		throws Exception {

		return _auditPseudonymLocalService.getOrAddAuditPseudonym(
			TestPropsValues.getCompanyId(), null, _FIELD_CATEGORY,
			identityValue);
	}

	private <T> T _invoke(Callable<T> callable) throws Exception {
		try {
			return TransactionInvokerUtil.invoke(_transactionConfig, callable);
		}
		catch (Throwable throwable) {
			return ReflectionUtil.throwException(throwable);
		}
	}

	private void _testGetOrAddAuditPseudonym(String contextName)
		throws Exception {

		String identityValue = RandomTestUtil.randomString();

		AuditPseudonym auditPseudonym =
			_auditPseudonymLocalService.getOrAddAuditPseudonym(
				TestPropsValues.getCompanyId(), contextName, _FIELD_CATEGORY,
				identityValue);

		Assert.assertEquals(
			_CONTEXT_NAME_DEFAULT, auditPseudonym.getContextName());
		Assert.assertEquals(
			DigesterUtil.digestHex(DigesterUtil.SHA_256, identityValue),
			auditPseudonym.getIdentityValueHash());

		AuditPseudonym curAuditPseudonym = _getOrAddAuditPseudonym(
			identityValue);

		Assert.assertEquals(
			auditPseudonym.getAuditPseudonymId(),
			curAuditPseudonym.getAuditPseudonymId());
	}

	private void _testGetOrAddAuditPseudonymWithInvalidIdentityValue(
		String expectedMessage, String identityValue) {

		AssertUtils.assertFailure(
			AuditPseudonymIdentityValueException.class, expectedMessage,
			() -> _getOrAddAuditPseudonym(identityValue));
	}

	private void _testGetOrAddAuditPseudonymWithSimilarIdentityValues(
			String identityValue)
		throws Exception {

		AuditPseudonym auditPseudonym = _getOrAddAuditPseudonym(identityValue);

		auditPseudonym = _auditPseudonymLocalService.fetchAuditPseudonym(
			auditPseudonym.getAuditPseudonymId());

		Assert.assertEquals(identityValue, auditPseudonym.getIdentityValue());
	}

	private static final String _CONTEXT_NAME_DEFAULT = "INSTANCE";

	private static final String _FIELD_CATEGORY = RandomTestUtil.randomString();

	private static final int _THREAD_COUNT = 16;

	private static final TransactionConfig _transactionConfig =
		TransactionConfig.Factory.create(
			Propagation.REQUIRED, new Class<?>[] {Exception.class});

	@Inject
	private AuditPseudonymLocalService _auditPseudonymLocalService;

	@Inject
	private UserLocalService _userLocalService;

}