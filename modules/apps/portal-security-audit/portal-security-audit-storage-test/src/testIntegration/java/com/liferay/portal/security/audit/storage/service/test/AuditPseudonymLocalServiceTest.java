/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.storage.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.counter.kernel.service.CounterLocalService;
import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.dao.orm.hibernate.VerifySessionFactoryWrapper;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.auth.CompanyInheritableThreadLocalCallable;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.AssertUtils;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.kernel.transaction.TransactionConfig;
import com.liferay.portal.kernel.transaction.TransactionInvokerUtil;
import com.liferay.portal.kernel.util.DigesterUtil;
import com.liferay.portal.kernel.util.ProxyUtil;
import com.liferay.portal.security.audit.storage.exception.AuditPseudonymIdentityValueException;
import com.liferay.portal.security.audit.storage.model.AuditPseudonym;
import com.liferay.portal.security.audit.storage.service.AuditPseudonymLocalService;
import com.liferay.portal.security.audit.storage.service.persistence.AuditPseudonymPersistence;
import com.liferay.portal.spring.aop.AopInvocationHandler;
import com.liferay.portal.test.log.LogCapture;
import com.liferay.portal.test.log.LogEntry;
import com.liferay.portal.test.log.LoggerTestUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;

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

		_testGetOrAddAuditPseudonymWithInvalidIdentityValue(
			"Maximum length of identity value exceeded",
			RandomTestUtil.randomString(256));
		_testGetOrAddAuditPseudonymWithInvalidIdentityValue(
			_IDENTITY_VALUE_BLANK_MESSAGE, StringPool.BLANK);
		_testGetOrAddAuditPseudonymWithInvalidIdentityValue(
			_IDENTITY_VALUE_BLANK_MESSAGE, null);

		_testGetOrAddAuditPseudonymWithSimilarIdentityValues("CASEY");
		_testGetOrAddAuditPseudonymWithSimilarIdentityValues("Casey");
		_testGetOrAddAuditPseudonymWithSimilarIdentityValues("Jose");
		_testGetOrAddAuditPseudonymWithSimilarIdentityValues("José");
		_testGetOrAddAuditPseudonymWithSimilarIdentityValues("casey");
		_testGetOrAddAuditPseudonymWithSimilarIdentityValues("casey ");
	}

	@Test
	public void testGetOrAddAuditPseudonymConcurrently() throws Exception {
		ExecutorService executorService = Executors.newFixedThreadPool(
			_THREAD_COUNT);

		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				SqlExceptionHelper.class.getName(), LoggerTestUtil.WARN)) {

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
	public void testGetOrAddAuditPseudonymWithRaceCondition() throws Exception {
		AtomicReference<AuditPseudonym> auditPseudonymAtomicReference =
			new AtomicReference<>();
		Class<?> clazz = _auditPseudonymPersistence.getClass();
		String identityValue = RandomTestUtil.randomString();

		AuditPseudonymPersistence auditPseudonymPersistence =
			ProxyUtil.newDelegateProxyInstance(
				clazz.getClassLoader(), AuditPseudonymPersistence.class,
				new Object() {

					public AuditPseudonym fetchByC_CN_FC_IVH(
							long companyId, String contextName,
							String fieldCategory, String identityValueHash,
							boolean useFinderCache)
						throws Exception {

						if (auditPseudonymAtomicReference.get() != null) {
							return _auditPseudonymPersistence.
								fetchByC_CN_FC_IVH(
									companyId, contextName, fieldCategory,
									identityValueHash, useFinderCache);
						}

						FutureTask<AuditPseudonym> futureTask =
							new FutureTask<>(
								new CompanyInheritableThreadLocalCallable<>(
									() -> {
										AuditPseudonym auditPseudonym =
											_auditPseudonymLocalService.
												createAuditPseudonym(
													_counterLocalService.
														increment());

										auditPseudonym.setCompanyId(companyId);
										auditPseudonym.setCreateDate(
											new Date());
										auditPseudonym.setContextName(
											contextName);
										auditPseudonym.setFieldCategory(
											fieldCategory);
										auditPseudonym.setIdentityValue(
											identityValue);
										auditPseudonym.setIdentityValueHash(
											identityValueHash);

										return _auditPseudonymLocalService.
											addAuditPseudonym(auditPseudonym);
									}));

						Thread thread = new Thread(futureTask);

						thread.start();

						auditPseudonymAtomicReference.set(futureTask.get());

						return null;
					}

				},
				_auditPseudonymPersistence);

		AopInvocationHandler aopInvocationHandler =
			ProxyUtil.fetchInvocationHandler(
				_auditPseudonymLocalService, AopInvocationHandler.class);

		Object auditPseudonymLocalServiceImpl =
			aopInvocationHandler.getTarget();

		try (AutoCloseable autoCloseable =
				ReflectionTestUtil.setFieldValueWithAutoCloseable(
					auditPseudonymLocalServiceImpl, "auditPseudonymPersistence",
					auditPseudonymPersistence);
			LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				SqlExceptionHelper.class.getName(), LoggerTestUtil.ERROR)) {

			AuditPseudonym curAuditPseudonym = _getOrAddAuditPseudonym(
				identityValue);

			AuditPseudonym auditPseudonym = auditPseudonymAtomicReference.get();

			Assert.assertEquals(
				auditPseudonym.getAuditPseudonymId(),
				curAuditPseudonym.getAuditPseudonymId());

			Assert.assertEquals(1, _getAuditPseudonymsCount(identityValue));

			List<LogEntry> logEntries = logCapture.getLogEntries();

			Assert.assertFalse(logEntries.isEmpty());
		}
	}

	@Test
	public void testGetOrAddAuditPseudonymWithRollback() throws Exception {
		AtomicReference<AuditPseudonym> auditPseudonymAtomicReference =
			new AtomicReference<>();
		String identityValue = RandomTestUtil.randomString();
		String message = RandomTestUtil.randomString();

		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				VerifySessionFactoryWrapper.class.getName(),
				LoggerTestUtil.ERROR)) {

			AssertUtils.assertFailure(
				PortalException.class, message,
				() -> _invoke(
					() -> {
						auditPseudonymAtomicReference.set(
							_getOrAddAuditPseudonym(identityValue));

						throw new PortalException(message);
					}));

			List<LogEntry> logEntries = logCapture.getLogEntries();

			Assert.assertTrue(logEntries.isEmpty());
		}

		AuditPseudonym auditPseudonym = auditPseudonymAtomicReference.get();

		auditPseudonym = _auditPseudonymLocalService.fetchAuditPseudonym(
			auditPseudonym.getAuditPseudonymId());

		Assert.assertEquals(identityValue, auditPseudonym.getIdentityValue());
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

	private static final String _IDENTITY_VALUE_BLANK_MESSAGE =
		"Identity value is blank";

	private static final int _THREAD_COUNT = 16;

	private static final TransactionConfig _transactionConfig =
		TransactionConfig.Factory.create(
			Propagation.REQUIRED, new Class<?>[] {Exception.class});

	@Inject
	private AuditPseudonymLocalService _auditPseudonymLocalService;

	@Inject
	private AuditPseudonymPersistence _auditPseudonymPersistence;

	@Inject
	private CounterLocalService _counterLocalService;

	@Inject
	private UserLocalService _userLocalService;

}