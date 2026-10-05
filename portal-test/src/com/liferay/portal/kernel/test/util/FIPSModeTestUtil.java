/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.test.util;

import com.liferay.petra.function.UnsafeConsumer;
import com.liferay.petra.function.UnsafeRunnable;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.security.crypto.CryptoFunctionApprovalType;
import com.liferay.portal.kernel.security.crypto.CryptoUtil;

import java.lang.reflect.Method;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.function.ThrowingRunnable;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author Caio Farias
 * @author Lucas Miranda
 */
public class FIPSModeTestUtil {

	public static void assertAlgorithm(
			String algorithm, UnsafeRunnable<Exception> unsafeRunnable)
		throws Exception {

		_assertAlgorithm(algorithm, unsafeRunnable);

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			_assertAlgorithm(algorithm, unsafeRunnable);
		}
	}

	public static <T> void assertAlgorithmSwitch(
			String algorithm, Class<T> classToMock, String fipsAlgorithm,
			UnsafeConsumer<String, Exception> unsafeConsumer,
			UnsafeRunnable<Exception> unsafeRunnable)
		throws Exception {

		try (MockedStatic<T> mockedStatic = Mockito.mockStatic(
				classToMock, Mockito.CALLS_REAL_METHODS)) {

			unsafeRunnable.run();

			mockedStatic.verify(
				() -> unsafeConsumer.accept(algorithm), Mockito.atLeastOnce());
			mockedStatic.verify(
				() -> unsafeConsumer.accept(fipsAlgorithm), Mockito.never());
		}

		try (MockedStatic<T> mockedStatic = Mockito.mockStatic(
				classToMock, Mockito.CALLS_REAL_METHODS);
			SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			unsafeRunnable.run();

			mockedStatic.verify(
				() -> unsafeConsumer.accept(algorithm), Mockito.never());
			mockedStatic.verify(
				() -> unsafeConsumer.accept(fipsAlgorithm),
				Mockito.atLeastOnce());
		}
	}

	public static void assertApprovalType(
			CryptoFunctionApprovalType approvalType,
			UnsafeRunnable<Exception> unsafeRunnable)
		throws Exception {

		_assertApprovalType(approvalType, unsafeRunnable);

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"FIPS_ENABLED", true)) {

			_assertApprovalType(approvalType, unsafeRunnable);
		}
	}

	public static void assertSecurityException(
		String expectedMessage, ThrowingRunnable throwingRunnable) {

		SecurityException securityException = Assert.assertThrows(
			SecurityException.class, throwingRunnable);

		String message = securityException.getMessage();

		Assert.assertTrue(message, message.contains(expectedMessage));
	}

	private static void _assertAlgorithm(
			String algorithm, UnsafeRunnable<Exception> unsafeRunnable)
		throws Exception {

		Set<String> algorithms = new HashSet<>();

		try (MockedStatic<CryptoUtil> mockedStatic = Mockito.mockStatic(
				CryptoUtil.class,
				invocationOnMock -> {
					Method method = invocationOnMock.getMethod();

					String methodName = method.getName();

					if (methodName.startsWith("create")) {
						for (Object argument :
								invocationOnMock.getArguments()) {

							if (argument instanceof String) {
								algorithms.add((String)argument);
							}
						}
					}

					return invocationOnMock.callRealMethod();
				})) {

			unsafeRunnable.run();
		}

		Assert.assertTrue(
			algorithms.toString(), algorithms.contains(algorithm));
	}

	private static void _assertApprovalType(
			CryptoFunctionApprovalType approvalType,
			UnsafeRunnable<Exception> unsafeRunnable)
		throws Exception {

		Set<CryptoFunctionApprovalType> approvalTypes = new HashSet<>();

		try (MockedStatic<CryptoUtil> mockedStatic = Mockito.mockStatic(
				CryptoUtil.class,
				invocationOnMock -> {
					for (Object argument : invocationOnMock.getArguments()) {
						if (argument instanceof CryptoFunctionApprovalType) {
							approvalTypes.add(
								(CryptoFunctionApprovalType)argument);
						}
					}

					return invocationOnMock.callRealMethod();
				})) {

			unsafeRunnable.run();
		}

		Assert.assertTrue(
			approvalTypes.toString(), approvalTypes.contains(approvalType));
	}

}