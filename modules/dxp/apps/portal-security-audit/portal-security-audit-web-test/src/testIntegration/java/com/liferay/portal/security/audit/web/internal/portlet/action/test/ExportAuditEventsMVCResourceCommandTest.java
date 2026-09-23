/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.web.internal.portlet.action.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.test.util.ConfigurationTemporarySwapper;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.HashMapDictionary;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.ProgressTracker;
import com.liferay.portal.kernel.util.ProxyUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.security.audit.AuditEvent;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Christian Moura
 */
@FeatureFlag("LPD-6417")
@RunWith(Arquillian.class)
public class ExportAuditEventsMVCResourceCommandTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testBuildCSV() throws Exception {
		AuditEvent auditEvent = _createAuditEvent(
			RandomTestUtil.randomBoolean(), false, null, 0);

		String csv = ReflectionTestUtil.invoke(
			_mvcResourceCommand, "_buildCSV",
			new Class<?>[] {List.class, String[].class, ProgressTracker.class},
			ListUtil.fromArray(auditEvent),
			_getColumns(TestPropsValues.getCompanyId()), null);

		String[] lines = StringUtil.split(csv, CharPool.NEW_LINE);

		Assert.assertEquals(Arrays.toString(lines), 2, lines.length);
		Assert.assertEquals(
			StringUtil.merge(_COLUMNS_DEFAULT, StringPool.COMMA), lines[0]);
	}

	@Test
	public void testGetColumns() throws Exception {
		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				new ConfigurationTemporarySwapper(
					_CONFIGURATION_PID,
					HashMapDictionaryBuilder.<String, Object>put(
						"columns", new String[] {RandomTestUtil.randomString()}
					).build())) {

			Assert.assertArrayEquals(
				_COLUMNS_DEFAULT, _getColumns(TestPropsValues.getCompanyId()));
		}
	}

	@FeatureFlag(enable = false, value = "LPD-6417")
	@Test
	public void testGetColumnsWhenFeatureFlagIsDisabled() throws Exception {
		String[] columns = {
			RandomTestUtil.randomString(), RandomTestUtil.randomString()
		};

		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				new ConfigurationTemporarySwapper(
					_CONFIGURATION_PID,
					HashMapDictionaryBuilder.<String, Object>put(
						"columns", columns
					).build())) {

			Assert.assertArrayEquals(
				columns, _getColumns(TestPropsValues.getCompanyId()));
		}

		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				new ConfigurationTemporarySwapper(
					_CONFIGURATION_PID, new HashMapDictionary<>())) {

			Assert.assertArrayEquals(
				ArrayUtil.remove(_COLUMNS_DEFAULT, "pseudonymizationFailed"),
				_getColumns(TestPropsValues.getCompanyId()));
		}
	}

	@Test
	public void testGetEmailAddress() throws Exception {
		User user = TestPropsValues.getUser();
		String userEmailAddress = RandomTestUtil.randomString();

		AuditEvent auditEvent = _createAuditEvent(
			RandomTestUtil.randomBoolean(), false, userEmailAddress,
			user.getUserId());

		Assert.assertEquals(
			user.getEmailAddress(), _getEmailAddress(auditEvent));

		auditEvent = _createAuditEvent(
			RandomTestUtil.randomBoolean(), true, userEmailAddress,
			user.getUserId());

		Assert.assertEquals(userEmailAddress, _getEmailAddress(auditEvent));
	}

	@Test
	public void testGetPseudonymizationFailed() {
		AuditEvent auditEvent = _createAuditEvent(
			true, false, null, RandomTestUtil.randomLong());

		Assert.assertEquals(
			StringPool.BLANK, _getPseudonymizationFailed(auditEvent));

		auditEvent = _createAuditEvent(
			false, true, null, RandomTestUtil.randomLong());

		Assert.assertEquals("false", _getPseudonymizationFailed(auditEvent));

		auditEvent = _createAuditEvent(
			true, true, null, RandomTestUtil.randomLong());

		Assert.assertEquals("true", _getPseudonymizationFailed(auditEvent));
	}

	@Test
	public void testGetScreenName() throws Exception {
		User user = TestPropsValues.getUser();

		AuditEvent auditEvent = _createAuditEvent(
			RandomTestUtil.randomBoolean(), false, null, user.getUserId());

		Assert.assertEquals(user.getScreenName(), _getScreenName(auditEvent));

		auditEvent = _createAuditEvent(
			RandomTestUtil.randomBoolean(), true, null, user.getUserId());

		Assert.assertEquals(StringPool.BLANK, _getScreenName(auditEvent));
	}

	private AuditEvent _createAuditEvent(
		boolean pseudonymizationFailed, boolean pseudonymized,
		String userEmailAddress, long userId) {

		return (AuditEvent)ProxyUtil.newProxyInstance(
			AuditEvent.class.getClassLoader(),
			new Class<?>[] {AuditEvent.class},
			(proxy, method, arguments) -> {
				String methodName = method.getName();

				if (methodName.equals("getUserEmailAddress")) {
					return userEmailAddress;
				}

				if (methodName.equals("getUserId")) {
					return userId;
				}

				if (methodName.equals("isPseudonymizationFailed")) {
					return pseudonymizationFailed;
				}

				if (methodName.equals("isPseudonymized")) {
					return pseudonymized;
				}

				Class<?> returnType = method.getReturnType();

				if (returnType == int.class) {
					return 0;
				}

				if (returnType == long.class) {
					return 0L;
				}

				return null;
			});
	}

	private String[] _getColumns(long companyId) {
		return ReflectionTestUtil.invoke(
			_mvcResourceCommand, "_getColumns", new Class<?>[] {long.class},
			companyId);
	}

	private String _getEmailAddress(AuditEvent auditEvent) {
		return ReflectionTestUtil.invoke(
			_mvcResourceCommand, "_getEmailAddress",
			new Class<?>[] {AuditEvent.class}, auditEvent);
	}

	private String _getPseudonymizationFailed(AuditEvent auditEvent) {
		return ReflectionTestUtil.invoke(
			_mvcResourceCommand, "_getPseudonymizationFailed",
			new Class<?>[] {AuditEvent.class}, auditEvent);
	}

	private String _getScreenName(AuditEvent auditEvent) {
		return ReflectionTestUtil.invoke(
			_mvcResourceCommand, "_getScreenName",
			new Class<?>[] {AuditEvent.class}, auditEvent);
	}

	private static final String[] _COLUMNS_DEFAULT = {
		"additionalInfo", "className", "classPK", "clientHost", "clientIP",
		"companyId", "eventType", "message", "pseudonymizationFailed",
		"serverName", "serverPort", "sessionID", "timestamp",
		"userEmailAddress", "userId", "userLogin", "userName"
	};

	private static final String _CONFIGURATION_PID =
		"com.liferay.portal.security.audit.router.configuration." +
			"CSVLogMessageFormatterConfiguration";

	@Inject(filter = "mvc.command.name=/audit/export_audit_events")
	private MVCResourceCommand _mvcResourceCommand;

}