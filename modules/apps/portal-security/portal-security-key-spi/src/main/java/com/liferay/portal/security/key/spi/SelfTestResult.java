/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.key.spi;

/**
 * The outcome of a {@link SecurityModuleProvider#performSelfTests()} run. When
 * a test fails, the failed mechanism is named so the failure can be isolated
 * and reported.
 *
 * @author Christopher Kian
 */
public class SelfTestResult {

	public SelfTestResult(String failedMechanism, boolean passed) {
		_failedMechanism = failedMechanism;
		_passed = passed;
	}

	public String getFailedMechanism() {
		return _failedMechanism;
	}

	public boolean isPassed() {
		return _passed;
	}

	private final String _failedMechanism;
	private final boolean _passed;

}