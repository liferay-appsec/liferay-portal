/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.event.generators.user.management.internal.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.configuration.test.util.ConfigurationTemporarySwapper;
import com.liferay.portal.kernel.audit.AuditMessage;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.PortalPreferences;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.PrefsPropsUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.security.audit.event.generators.constants.EventTypes;
import com.liferay.portal.security.audit.event.generators.user.management.internal.model.listener.test.BaseModelListenerTestCase;
import com.liferay.portal.security.audit.router.configuration.LoggingAuditMessageProcessorConfiguration;
import com.liferay.portal.test.log.LogCapture;
import com.liferay.portal.test.log.LogEntry;
import com.liferay.portal.test.log.LoggerTestUtil;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

/**
 * @author Lucas Miranda
 */
@RunWith(Arquillian.class)
public class AuditPortalPreferencesLocalServiceWrapperTest
	extends BaseModelListenerTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		_authType = PrefsPropsUtil.getString(
			TestPropsValues.getCompanyId(),
			PropsKeys.COMPANY_SECURITY_AUTH_TYPE);
		_autoLogin = PrefsPropsUtil.getBoolean(
			TestPropsValues.getCompanyId(),
			PropsKeys.COMPANY_SECURITY_AUTO_LOGIN);
		_siteLogo = PrefsPropsUtil.getBoolean(
			TestPropsValues.getCompanyId(),
			PropsKeys.COMPANY_SECURITY_SITE_LOGO);
		_strangers = PrefsPropsUtil.getBoolean(
			TestPropsValues.getCompanyId(),
			PropsKeys.COMPANY_SECURITY_STRANGERS);
		_strangersVerify = PrefsPropsUtil.getBoolean(
			TestPropsValues.getCompanyId(),
			PropsKeys.COMPANY_SECURITY_STRANGERS_VERIFY);
		_strangersWithMx = PrefsPropsUtil.getBoolean(
			TestPropsValues.getCompanyId(),
			PropsKeys.COMPANY_SECURITY_STRANGERS_WITH_MX);

		super.setUp();
	}

	@After
	@Override
	public void tearDown() throws Exception {
		_companyLocalService.updateSecurity(
			TestPropsValues.getCompanyId(), _authType, _autoLogin, false,
			_strangers, _strangersWithMx, _strangersVerify, _siteLogo);

		super.tearDown();
	}

	@FeatureFlag("LPD-6417")
	@Test
	public void testUpdatePreferences() throws Exception {
		try (ConfigurationTemporarySwapper configurationTemporarySwapper =
				new ConfigurationTemporarySwapper(
					LoggingAuditMessageProcessorConfiguration.class.getName(),
					HashMapDictionaryBuilder.<String, Object>put(
						"enabled", true
					).put(
						"logMessageFormat", "JSON"
					).build());
			LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				"com.liferay.portal.security.audit.router.internal." +
					"LoggingAuditMessageProcessor",
				LoggerTestUtil.INFO)) {

			_companyLocalService.updateSecurity(
				TestPropsValues.getCompanyId(), _authType, _autoLogin, false,
				!_strangers, _strangersWithMx, _strangersVerify, _siteLogo);

			Assert.assertEquals(
				auditMessages.toString(), 1, auditMessages.size());

			AuditMessage auditMessage = fetchAuditMessage(
				PortalPreferences.class.getName(), EventTypes.UPDATE);

			Assert.assertEquals(
				TestPropsValues.getCompanyId(), auditMessage.getCompanyId());
			Assert.assertEquals(
				"instance.portalpreferences.update",
				auditMessage.getResourceAction());

			JSONAssert.assertEquals(
				JSONUtil.put(
					"attributes",
					JSONUtil.putAll(
						JSONUtil.put(
							"name", PropsKeys.COMPANY_SECURITY_STRANGERS
						).put(
							"newValue", ""
						).put(
							"oldValue", ""
						))
				).toString(),
				String.valueOf(auditMessage.getAdditionalInfo()),
				JSONCompareMode.NON_EXTENSIBLE);

			List<LogEntry> logEntries = logCapture.getLogEntries();

			Assert.assertEquals(logEntries.toString(), 1, logEntries.size());

			LogEntry logEntry = logEntries.get(0);

			JSONAssert.assertEquals(
				String.valueOf(auditMessage.toJSONObject()),
				logEntry.getMessage(), JSONCompareMode.NON_EXTENSIBLE);
		}
	}

	private String _authType;
	private boolean _autoLogin;

	@Inject
	private CompanyLocalService _companyLocalService;

	private boolean _siteLogo;
	private boolean _strangers;
	private boolean _strangersVerify;
	private boolean _strangersWithMx;

}