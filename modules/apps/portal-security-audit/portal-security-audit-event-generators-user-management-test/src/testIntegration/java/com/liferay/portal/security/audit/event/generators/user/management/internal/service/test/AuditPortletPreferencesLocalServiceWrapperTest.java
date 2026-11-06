/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.event.generators.user.management.internal.service.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.audit.AuditMessage;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.PortletPreferences;
import com.liferay.portal.kernel.service.PortletPreferencesLocalService;
import com.liferay.portal.kernel.settings.PortletPreferencesSettings;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.PortletKeys;
import com.liferay.portal.security.audit.event.generators.constants.EventTypes;
import com.liferay.portal.security.audit.event.generators.user.management.internal.model.listener.test.BaseModelListenerTestCase;
import com.liferay.portal.test.rule.Inject;

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
public class AuditPortletPreferencesLocalServiceWrapperTest
	extends BaseModelListenerTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		_settingsId = RandomTestUtil.randomString();

		super.setUp();
	}

	@After
	@Override
	public void tearDown() throws Exception {
		PortletPreferences portletPreferences =
			_portletPreferencesLocalService.fetchPortletPreferences(
				TestPropsValues.getCompanyId(),
				PortletKeys.PREFS_OWNER_TYPE_COMPANY, 0, _settingsId);

		if (portletPreferences != null) {
			_portletPreferencesLocalService.deletePortletPreferences(
				portletPreferences);
		}

		super.tearDown();
	}

	@Test
	public void testUpdatePreferences() throws Exception {
		PortletPreferencesSettings portletPreferencesSettings =
			new PortletPreferencesSettings(
				_portletPreferencesLocalService.getStrictPreferences(
					TestPropsValues.getCompanyId(),
					TestPropsValues.getCompanyId(),
					PortletKeys.PREFS_OWNER_TYPE_COMPANY, 0, _settingsId));

		String clientSecret = RandomTestUtil.randomString();

		portletPreferencesSettings.setValue("clientSecret", clientSecret);

		portletPreferencesSettings.store();

		Assert.assertEquals(auditMessages.toString(), 1, auditMessages.size());

		AuditMessage auditMessage = fetchAuditMessage(
			PortletPreferences.class.getName(), EventTypes.UPDATE);

		Assert.assertEquals(
			TestPropsValues.getCompanyId(), auditMessage.getCompanyId());

		JSONAssert.assertEquals(
			JSONUtil.put(
				"attributes",
				JSONUtil.putAll(
					JSONUtil.put(
						"name", "clientSecret"
					).put(
						"newValue", ""
					).put(
						"oldValue", ""
					))
			).put(
				"portletId", _settingsId
			).toString(),
			String.valueOf(auditMessage.getAdditionalInfo()),
			JSONCompareMode.NON_EXTENSIBLE);

		String json = String.valueOf(auditMessage.toJSONObject());

		Assert.assertFalse(json, json.contains(clientSecret));

		portletPreferencesSettings.store();

		Assert.assertEquals(auditMessages.toString(), 1, auditMessages.size());
	}

	@Inject
	private PortletPreferencesLocalService _portletPreferencesLocalService;

	private String _settingsId;

}