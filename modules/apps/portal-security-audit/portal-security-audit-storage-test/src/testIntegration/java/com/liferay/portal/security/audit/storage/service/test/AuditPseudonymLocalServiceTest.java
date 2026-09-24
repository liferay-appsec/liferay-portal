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
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.kernel.transaction.TransactionConfig;
import com.liferay.portal.kernel.transaction.TransactionInvokerUtil;
import com.liferay.portal.kernel.util.DigesterUtil;
import com.liferay.portal.security.audit.storage.model.AuditPseudonym;
import com.liferay.portal.security.audit.storage.service.AuditPseudonymLocalService;
import com.liferay.portal.test.log.LogCapture;
import com.liferay.portal.test.log.LogEntry;
import com.liferay.portal.test.log.LoggerTestUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;

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
		String value = RandomTestUtil.randomString();

		AuditPseudonym auditPseudonym = _getOrAddAuditPseudonym(value);

		Assert.assertEquals(
			_CONTEXT_NAME_DEFAULT, auditPseudonym.getContextName());
		Assert.assertEquals(
			DigesterUtil.digestHex(DigesterUtil.SHA_256, value),
			auditPseudonym.getValueHash());

		for (String contextName :
				new String[] {StringPool.BLANK, _CONTEXT_NAME_DEFAULT}) {

			AuditPseudonym curAuditPseudonym =
				_auditPseudonymLocalService.getOrAddAuditPseudonym(
					TestPropsValues.getCompanyId(), contextName,
					_FIELD_CATEGORY, value);

			Assert.assertEquals(
				auditPseudonym.getAuditPseudonymId(),
				curAuditPseudonym.getAuditPseudonymId());
		}

		String contextName = RandomTestUtil.randomString();

		AuditPseudonym contextAuditPseudonym =
			_auditPseudonymLocalService.getOrAddAuditPseudonym(
				TestPropsValues.getCompanyId(), contextName, _FIELD_CATEGORY,
				value);

		Assert.assertEquals(
			contextName, contextAuditPseudonym.getContextName());

		AuditPseudonym curContextAuditPseudonym =
			_auditPseudonymLocalService.getOrAddAuditPseudonym(
				TestPropsValues.getCompanyId(), contextName, _FIELD_CATEGORY,
				value);

		Assert.assertEquals(
			contextAuditPseudonym.getAuditPseudonymId(),
			curContextAuditPseudonym.getAuditPseudonymId());

		Assert.assertEquals(2, _getAuditPseudonymsCount(value));
	}

	@Test
	public void testGetOrAddAuditPseudonymConcurrently() throws Exception {
		ExecutorService executorService = Executors.newFixedThreadPool(
			_THREAD_COUNT);

		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				SqlExceptionHelper.class.getName(), LoggerTestUtil.WARN)) {

			CyclicBarrier cyclicBarrier = new CyclicBarrier(_THREAD_COUNT);
			List<Future<AuditPseudonym>> futures = new ArrayList<>();
			String value = RandomTestUtil.randomString();

			for (int i = 0; i < _THREAD_COUNT; i++) {
				futures.add(
					executorService.submit(
						new CompanyInheritableThreadLocalCallable<>(
							() -> {
								cyclicBarrier.await();

								return _invoke(
									() -> {
										AuditPseudonym auditPseudonym =
											_getOrAddAuditPseudonym(value);
										AuditPseudonym curAuditPseudonym =
											_getOrAddAuditPseudonym(value);

										Assert.assertEquals(
											auditPseudonym.
												getAuditPseudonymId(),
											curAuditPseudonym.
												getAuditPseudonymId());

										return auditPseudonym;
									});
							})));
			}

			Set<Long> auditPseudonymIds = new HashSet<>();

			for (Future<AuditPseudonym> future : futures) {
				AuditPseudonym auditPseudonym = future.get();

				auditPseudonymIds.add(auditPseudonym.getAuditPseudonymId());
			}

			Assert.assertEquals(
				auditPseudonymIds.toString(), 1, auditPseudonymIds.size());
			Assert.assertEquals(1, _getAuditPseudonymsCount(value));

			List<LogEntry> logEntries = logCapture.getLogEntries();

			Assert.assertTrue(logEntries.toString(), logEntries.isEmpty());
		}
		finally {
			executorService.shutdownNow();
		}
	}

	@Test
	public void testGetOrAddAuditPseudonymWithPendingUserUpdate()
		throws Exception {

		User user = UserTestUtil.addUser();

		String value = RandomTestUtil.randomString();

		AuditPseudonym auditPseudonym = _invoke(
			() -> {
				User detachedUser = _userLocalService.getUser(user.getUserId());

				detachedUser.setJobTitle(RandomTestUtil.randomString());

				_userLocalService.updateUser(detachedUser);

				AuditPseudonym curAuditPseudonym = _getOrAddAuditPseudonym(
					value);

				detachedUser.setJobTitle(RandomTestUtil.randomString());

				_userLocalService.updateUser(detachedUser);

				return curAuditPseudonym;
			});

		auditPseudonym = _auditPseudonymLocalService.fetchAuditPseudonym(
			auditPseudonym.getAuditPseudonymId());

		Assert.assertEquals(value, auditPseudonym.getValue());
	}

	@Test
	public void testGetOrAddAuditPseudonymWithSimilarValues() throws Exception {
		Set<Long> auditPseudonymIds = new HashSet<>();

		for (String value :
				new String[] {
					"CASEY", "Casey", "Jose", "José", "casey", "casey "
				}) {

			AuditPseudonym auditPseudonym = _getOrAddAuditPseudonym(value);

			Assert.assertTrue(
				auditPseudonymIds.add(auditPseudonym.getAuditPseudonymId()));

			auditPseudonym = _auditPseudonymLocalService.fetchAuditPseudonym(
				auditPseudonym.getAuditPseudonymId());

			Assert.assertEquals(value, auditPseudonym.getValue());
		}
	}

	private long _getAuditPseudonymsCount(String value) throws Exception {
		DynamicQuery dynamicQuery = _auditPseudonymLocalService.dynamicQuery();

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq(
				"companyId", TestPropsValues.getCompanyId()));
		dynamicQuery.add(
			RestrictionsFactoryUtil.eq("fieldCategory", _FIELD_CATEGORY));
		dynamicQuery.add(RestrictionsFactoryUtil.eq("value", value));

		return _auditPseudonymLocalService.dynamicQueryCount(dynamicQuery);
	}

	private AuditPseudonym _getOrAddAuditPseudonym(String value)
		throws Exception {

		return _auditPseudonymLocalService.getOrAddAuditPseudonym(
			TestPropsValues.getCompanyId(), null, _FIELD_CATEGORY, value);
	}

	private <T> T _invoke(Callable<T> callable) throws Exception {
		try {
			return TransactionInvokerUtil.invoke(_transactionConfig, callable);
		}
		catch (Throwable throwable) {
			return ReflectionUtil.throwException(throwable);
		}
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