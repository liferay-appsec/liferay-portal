/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.security.fips;

import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.PropsValuesTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.PropsUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ObjectMessage;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Jorge García Jiménez
 * @author Rafael Praxedes
 */
public class FIPSApplicationStateMachineUtilTest {

	@BeforeClass
	public static void setUpClass() {
		PropsUtil.get("fips.enabled");

		_logger = Mockito.mock(Logger.class);

		_logManagerMockedStatic = Mockito.mockStatic(
			LogManager.class, Mockito.CALLS_REAL_METHODS);

		_logManagerMockedStatic.when(
			() -> LogManager.getLogger(FIPSAuditEventEmitterUtil.class)
		).thenReturn(
			_logger
		);
	}

	@AfterClass
	public static void tearDownClass() {
		_logManagerMockedStatic.close();
	}

	@Before
	public void setUp() {
		Mockito.reset(_logger);

		_safeCloseable = PropsValuesTestUtil.swapWithSafeCloseable(
			"FIPS_AUDIT_DEPLOYMENT_INSTANCE_ID", RandomTestUtil.randomString());

		Mockito.doAnswer(
			invocation -> {
				ObjectMessage objectMessage = invocation.getArgument(1);

				_records.add((Map<String, Object>)objectMessage.getParameter());

				return null;
			}
		).when(
			_logger
		).log(
			Mockito.any(Level.class), Mockito.any(Message.class)
		);

		_setFIPSApplicationState(FIPSApplicationState.INITIALIZING);
	}

	@After
	public void tearDown() {
		_safeCloseable.close();
	}

	@Test
	public void testError() {
		_setFIPSApplicationState(FIPSApplicationState.OPERATIONAL);

		String failedStep = RandomTestUtil.randomString();

		FIPSApplicationStateMachineUtil.error(
			failedStep, new SecurityException("The provider is unhappy"));

		Assert.assertEquals(
			FIPSApplicationState.ERROR,
			FIPSApplicationStateMachineUtil.getFIPSApplicationState());

		Map<String, Object> record = _getRecord();

		_assertEnvelope(record, "event-type", "fips-state-transition");
		_assertEnvelope(record, "severity", "critical");

		_assertField(record, "failed-step", failedStep);
		_assertField(record, "from-state", "Operational");
		_assertField(
			record, "provider-error-message", "The provider is unhappy");
		_assertField(record, "to-state", "Error");
	}

	@Test
	public void testKeyCSPEntry() {
		_setFIPSApplicationState(FIPSApplicationState.OPERATIONAL);

		String cryptoOfficerUserId = RandomTestUtil.randomString();
		String operationType = RandomTestUtil.randomString();

		FIPSApplicationStateMachineUtil.keyCSPEntry(
			cryptoOfficerUserId, operationType,
			() -> {
			});

		Assert.assertEquals(
			FIPSApplicationState.OPERATIONAL,
			FIPSApplicationStateMachineUtil.getFIPSApplicationState());

		Assert.assertEquals(_records.toString(), 2, _records.size());

		_assertEnvelope(_records.get(0), "severity", "info");

		_assertField(
			_records.get(0), "crypto-officer-user-id", cryptoOfficerUserId);
		_assertField(_records.get(0), "operation-type", operationType);
		_assertField(_records.get(0), "to-state", "Key/CSP Entry");

		_assertField(_records.get(1), "from-state", "Key/CSP Entry");
		_assertField(
			_records.get(1), "message",
			"The operation was completed successfully");
		_assertField(_records.get(1), "to-state", "Operational");
	}

	@Test
	public void testKeyCSPEntryWithFailedOperation() {
		_setFIPSApplicationState(FIPSApplicationState.OPERATIONAL);

		Assert.assertThrows(
			SecurityException.class,
			() -> FIPSApplicationStateMachineUtil.keyCSPEntry(
				RandomTestUtil.randomString(), RandomTestUtil.randomString(),
				() -> {
					throw new SecurityException("The key is unusable");
				}));

		Assert.assertEquals(
			FIPSApplicationState.ERROR,
			FIPSApplicationStateMachineUtil.getFIPSApplicationState());

		Assert.assertEquals(_records.toString(), 2, _records.size());

		_assertEnvelope(_records.get(1), "severity", "critical");

		_assertField(_records.get(1), "failed-step", "Key or CSP entry");
		_assertField(
			_records.get(1), "provider-error-message", "The key is unusable");
		_assertField(_records.get(1), "to-state", "Error");
	}

	@Test
	public void testPowerOff() {
		_setFIPSApplicationState(FIPSApplicationState.OPERATIONAL);

		String initiatingActor = RandomTestUtil.randomString();

		FIPSApplicationStateMachineUtil.powerOff(initiatingActor);

		Assert.assertEquals(
			FIPSApplicationState.POWER_OFF,
			FIPSApplicationStateMachineUtil.getFIPSApplicationState());

		Map<String, Object> record = _getRecord();

		_assertEnvelope(record, "severity", "info");

		_assertField(record, "from-state", "Operational");
		_assertField(record, "initiating-actor", initiatingActor);
		_assertField(record, "to-state", "Power-off");
	}

	@Test
	public void testQuiescent() {
		_setFIPSApplicationState(FIPSApplicationState.OPERATIONAL);

		String cryptoOfficerUserId = RandomTestUtil.randomString();
		String reason = RandomTestUtil.randomString();

		FIPSApplicationStateMachineUtil.quiescent(cryptoOfficerUserId, reason);

		Assert.assertEquals(
			FIPSApplicationState.QUIESCENT,
			FIPSApplicationStateMachineUtil.getFIPSApplicationState());

		FIPSApplicationStateMachineUtil.operational(
			cryptoOfficerUserId, reason);

		Assert.assertEquals(
			FIPSApplicationState.OPERATIONAL,
			FIPSApplicationStateMachineUtil.getFIPSApplicationState());

		Assert.assertEquals(_records.toString(), 2, _records.size());

		_assertField(
			_records.get(0), "crypto-officer-user-id", cryptoOfficerUserId);
		_assertField(_records.get(0), "reason", reason);
		_assertField(_records.get(0), "to-state", "Quiescent");

		_assertField(_records.get(1), "from-state", "Quiescent");
		_assertField(_records.get(1), "reason", reason);
		_assertField(_records.get(1), "to-state", "Operational");
	}

	@Test
	public void testSelfTest() {
		FIPSApplicationStateMachineUtil.selfTest(
			() -> {
			});

		Assert.assertEquals(
			FIPSApplicationState.OPERATIONAL,
			FIPSApplicationStateMachineUtil.getFIPSApplicationState());

		Assert.assertEquals(_records.toString(), 2, _records.size());

		_assertField(_records.get(0), "from-state", "Initializing");
		_assertField(
			_records.get(0), "message", "The integrity checks were started");
		_assertField(_records.get(0), "to-state", "Self-Test");

		_assertField(
			_records.get(1), "message",
			"All checks and the validated provider self tests passed");
		_assertField(_records.get(1), "to-state", "Operational");

		_testSelfTest(new RuntimeException());
		_testSelfTest(new SecurityException());
	}

	@Test
	public void testSelfTestWithRecovery() {
		_setFIPSApplicationState(FIPSApplicationState.ERROR);

		String cryptoOfficerUserId = RandomTestUtil.randomString();
		String recoveryAction = RandomTestUtil.randomString();

		FIPSApplicationStateMachineUtil.selfTest(
			cryptoOfficerUserId, recoveryAction,
			() -> {
			});

		Assert.assertEquals(
			FIPSApplicationState.OPERATIONAL,
			FIPSApplicationStateMachineUtil.getFIPSApplicationState());

		Assert.assertEquals(_records.toString(), 2, _records.size());

		_assertField(
			_records.get(0), "crypto-officer-user-id", cryptoOfficerUserId);
		_assertField(_records.get(0), "from-state", "Error");
		_assertField(_records.get(0), "recovery-action", recoveryAction);
		_assertField(_records.get(0), "to-state", "Self-Test");

		_assertField(_records.get(1), "to-state", "Operational");
	}

	@Test
	public void testTransition() {
		_testTransition(
			FIPSApplicationState.ERROR, FIPSApplicationState.POWER_OFF);
		_testTransition(
			FIPSApplicationState.ERROR, FIPSApplicationState.SELF_TEST);
		_testTransition(
			FIPSApplicationState.INITIALIZING, FIPSApplicationState.ERROR);
		_testTransition(
			FIPSApplicationState.INITIALIZING, FIPSApplicationState.POWER_OFF);
		_testTransition(
			FIPSApplicationState.INITIALIZING, FIPSApplicationState.SELF_TEST);
		_testTransition(
			FIPSApplicationState.KEY_CSP_ENTRY, FIPSApplicationState.ERROR);
		_testTransition(
			FIPSApplicationState.KEY_CSP_ENTRY,
			FIPSApplicationState.OPERATIONAL);
		_testTransition(
			FIPSApplicationState.KEY_CSP_ENTRY, FIPSApplicationState.POWER_OFF);
		_testTransition(
			FIPSApplicationState.OPERATIONAL, FIPSApplicationState.ERROR);
		_testTransition(
			FIPSApplicationState.OPERATIONAL,
			FIPSApplicationState.KEY_CSP_ENTRY);
		_testTransition(
			FIPSApplicationState.OPERATIONAL, FIPSApplicationState.POWER_OFF);
		_testTransition(
			FIPSApplicationState.OPERATIONAL, FIPSApplicationState.QUIESCENT);
		_testTransition(
			FIPSApplicationState.QUIESCENT, FIPSApplicationState.ERROR);
		_testTransition(
			FIPSApplicationState.QUIESCENT, FIPSApplicationState.KEY_CSP_ENTRY);
		_testTransition(
			FIPSApplicationState.QUIESCENT, FIPSApplicationState.OPERATIONAL);
		_testTransition(
			FIPSApplicationState.QUIESCENT, FIPSApplicationState.POWER_OFF);
		_testTransition(
			FIPSApplicationState.SELF_TEST, FIPSApplicationState.ERROR);
		_testTransition(
			FIPSApplicationState.SELF_TEST, FIPSApplicationState.OPERATIONAL);
		_testTransition(
			FIPSApplicationState.SELF_TEST, FIPSApplicationState.POWER_OFF);
	}

	@Test
	public void testTransitionWithIllegalState() {
		_testTransitionWithIllegalState(
			FIPSApplicationState.ERROR, FIPSApplicationState.ERROR);
		_testTransitionWithIllegalState(
			FIPSApplicationState.ERROR, FIPSApplicationState.INITIALIZING);
		_testTransitionWithIllegalState(
			FIPSApplicationState.ERROR, FIPSApplicationState.KEY_CSP_ENTRY);
		_testTransitionWithIllegalState(
			FIPSApplicationState.ERROR, FIPSApplicationState.OPERATIONAL);
		_testTransitionWithIllegalState(
			FIPSApplicationState.ERROR, FIPSApplicationState.QUIESCENT);
		_testTransitionWithIllegalState(
			FIPSApplicationState.INITIALIZING,
			FIPSApplicationState.INITIALIZING);
		_testTransitionWithIllegalState(
			FIPSApplicationState.INITIALIZING,
			FIPSApplicationState.KEY_CSP_ENTRY);
		_testTransitionWithIllegalState(
			FIPSApplicationState.INITIALIZING,
			FIPSApplicationState.OPERATIONAL);
		_testTransitionWithIllegalState(
			FIPSApplicationState.INITIALIZING, FIPSApplicationState.QUIESCENT);
		_testTransitionWithIllegalState(
			FIPSApplicationState.KEY_CSP_ENTRY,
			FIPSApplicationState.INITIALIZING);
		_testTransitionWithIllegalState(
			FIPSApplicationState.KEY_CSP_ENTRY,
			FIPSApplicationState.KEY_CSP_ENTRY);
		_testTransitionWithIllegalState(
			FIPSApplicationState.KEY_CSP_ENTRY, FIPSApplicationState.QUIESCENT);
		_testTransitionWithIllegalState(
			FIPSApplicationState.KEY_CSP_ENTRY, FIPSApplicationState.SELF_TEST);
		_testTransitionWithIllegalState(
			FIPSApplicationState.OPERATIONAL,
			FIPSApplicationState.INITIALIZING);
		_testTransitionWithIllegalState(
			FIPSApplicationState.OPERATIONAL, FIPSApplicationState.OPERATIONAL);
		_testTransitionWithIllegalState(
			FIPSApplicationState.OPERATIONAL, FIPSApplicationState.SELF_TEST);
		_testTransitionWithIllegalState(
			FIPSApplicationState.POWER_OFF, FIPSApplicationState.ERROR);
		_testTransitionWithIllegalState(
			FIPSApplicationState.POWER_OFF, FIPSApplicationState.INITIALIZING);
		_testTransitionWithIllegalState(
			FIPSApplicationState.POWER_OFF, FIPSApplicationState.KEY_CSP_ENTRY);
		_testTransitionWithIllegalState(
			FIPSApplicationState.POWER_OFF, FIPSApplicationState.OPERATIONAL);
		_testTransitionWithIllegalState(
			FIPSApplicationState.POWER_OFF, FIPSApplicationState.POWER_OFF);
		_testTransitionWithIllegalState(
			FIPSApplicationState.POWER_OFF, FIPSApplicationState.QUIESCENT);
		_testTransitionWithIllegalState(
			FIPSApplicationState.POWER_OFF, FIPSApplicationState.SELF_TEST);
		_testTransitionWithIllegalState(
			FIPSApplicationState.QUIESCENT, FIPSApplicationState.INITIALIZING);
		_testTransitionWithIllegalState(
			FIPSApplicationState.QUIESCENT, FIPSApplicationState.QUIESCENT);
		_testTransitionWithIllegalState(
			FIPSApplicationState.QUIESCENT, FIPSApplicationState.SELF_TEST);
		_testTransitionWithIllegalState(
			FIPSApplicationState.SELF_TEST, FIPSApplicationState.INITIALIZING);
		_testTransitionWithIllegalState(
			FIPSApplicationState.SELF_TEST, FIPSApplicationState.KEY_CSP_ENTRY);
		_testTransitionWithIllegalState(
			FIPSApplicationState.SELF_TEST, FIPSApplicationState.QUIESCENT);
		_testTransitionWithIllegalState(
			FIPSApplicationState.SELF_TEST, FIPSApplicationState.SELF_TEST);
	}

	private void _assertEnvelope(
		Map<String, Object> record, String key, String value) {

		Assert.assertEquals(String.valueOf(record), value, record.get(key));
	}

	private void _assertField(
		Map<String, Object> record, String key, String value) {

		Map<?, ?> fields = (Map<?, ?>)record.get("fields");

		Assert.assertEquals(String.valueOf(record), value, fields.get(key));
	}

	private Map<String, Object> _getRecord() {
		return _records.get(_records.size() - 1);
	}

	private void _setFIPSApplicationState(
		FIPSApplicationState fipsApplicationState) {

		AtomicReference<FIPSApplicationState>
			fipsApplicationStateAtomicReference =
				ReflectionTestUtil.getFieldValue(
					FIPSApplicationStateMachineUtil.class,
					"_fipsApplicationStateAtomicReference");

		fipsApplicationStateAtomicReference.set(fipsApplicationState);
	}

	private void _testSelfTest(RuntimeException runtimeException) {
		_records.clear();

		_setFIPSApplicationState(FIPSApplicationState.INITIALIZING);

		Assert.assertThrows(
			runtimeException.getClass(),
			() -> FIPSApplicationStateMachineUtil.selfTest(
				() -> {
					throw runtimeException;
				}));

		Assert.assertEquals(
			FIPSApplicationState.ERROR,
			FIPSApplicationStateMachineUtil.getFIPSApplicationState());

		Assert.assertEquals(_records.toString(), 2, _records.size());

		_assertEnvelope(_records.get(1), "severity", "critical");

		_assertField(_records.get(1), "failed-step", "Self test");
		_assertField(_records.get(1), "from-state", "Self-Test");
		_assertField(_records.get(1), "to-state", "Error");
	}

	private void _testTransition(
		FIPSApplicationState fromFIPSApplicationState,
		FIPSApplicationState toFIPSApplicationState) {

		_records.clear();

		_setFIPSApplicationState(fromFIPSApplicationState);

		_transition(toFIPSApplicationState);

		Assert.assertEquals(
			toFIPSApplicationState,
			FIPSApplicationStateMachineUtil.getFIPSApplicationState());

		Assert.assertEquals(_records.toString(), 1, _records.size());

		_assertEnvelope(_records.get(0), "event-type", "fips-state-transition");

		_assertField(
			_records.get(0), "from-state", fromFIPSApplicationState.getValue());
		_assertField(
			_records.get(0), "to-state", toFIPSApplicationState.getValue());
	}

	private void _testTransitionWithIllegalState(
		FIPSApplicationState fromFIPSApplicationState,
		FIPSApplicationState toFIPSApplicationState) {

		_records.clear();

		_setFIPSApplicationState(fromFIPSApplicationState);

		Assert.assertThrows(
			IllegalStateException.class,
			() -> _transition(toFIPSApplicationState));

		Assert.assertEquals(
			fromFIPSApplicationState,
			FIPSApplicationStateMachineUtil.getFIPSApplicationState());

		Assert.assertTrue(_records.toString(), _records.isEmpty());
	}

	private void _transition(FIPSApplicationState fipsApplicationState) {
		ReflectionTestUtil.invoke(
			FIPSApplicationStateMachineUtil.class, "_transition",
			new Class<?>[] {FIPSApplicationState.class, Consumer.class},
			fipsApplicationState,
			(Consumer<FIPSAuditEvent>)fipsAuditEvent -> {
			});
	}

	private static Logger _logger;

	private static MockedStatic<LogManager> _logManagerMockedStatic;

	private final List<Map<String, Object>> _records = new ArrayList<>();
	private SafeCloseable _safeCloseable;

}