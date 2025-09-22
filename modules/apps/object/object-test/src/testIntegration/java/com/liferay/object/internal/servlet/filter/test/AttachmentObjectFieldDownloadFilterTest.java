/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.internal.servlet.filter.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.service.DLAppLocalService;
import com.liferay.document.library.kernel.service.DLFileEntryLocalService;
import com.liferay.document.library.util.DLURLHelper;
import com.liferay.object.constants.ObjectEntryFolderConstants;
import com.liferay.object.constants.ObjectFieldConstants;
import com.liferay.object.constants.ObjectFieldSettingConstants;
import com.liferay.object.field.setting.builder.ObjectFieldSettingBuilder;
import com.liferay.object.field.util.ObjectFieldUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectField;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.test.util.ObjectDefinitionTestUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.TempFileEntryUtil;
import com.liferay.portal.security.permission.PermissionCacheUtil;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import jakarta.servlet.http.HttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.Serializable;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Manuele Castro
 */
@FeatureFlag("LPD-17564")
@RunWith(Arquillian.class)
public class AttachmentObjectFieldDownloadFilterTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_objectField = ObjectFieldUtil.createObjectField(
			ObjectFieldConstants.BUSINESS_TYPE_ATTACHMENT,
			ObjectFieldConstants.DB_TYPE_LONG, true, false, null,
			RandomTestUtil.randomString(), "attachment",
			Arrays.asList(
				new ObjectFieldSettingBuilder(
				).name(
					ObjectFieldSettingConstants.NAME_ACCEPTED_FILE_EXTENSIONS
				).value(
					"jpg, jpeg, png, svg, txt"
				).build(),
				new ObjectFieldSettingBuilder(
				).name(
					ObjectFieldSettingConstants.NAME_FILE_SOURCE
				).value(
					ObjectFieldSettingConstants.VALUE_USER_COMPUTER
				).build(),
				new ObjectFieldSettingBuilder(
				).name(
					ObjectFieldSettingConstants.NAME_MAX_FILE_SIZE
				).value(
					"100"
				).build()),
			false);

		_objectDefinition = ObjectDefinitionTestUtil.publishObjectDefinition(
			Collections.singletonList(_objectField));

		_company = _companyLocalService.getCompany(
			TestPropsValues.getCompanyId());

		_objectEntry = _objectEntryLocalService.addOrUpdateObjectEntry(
			RandomTestUtil.randomString(), 0, TestPropsValues.getUserId(),
			_objectDefinition.getObjectDefinitionId(),
			ObjectEntryFolderConstants.PARENT_OBJECT_ENTRY_FOLDER_ID_DEFAULT,
			HashMapBuilder.<String, Serializable>put(
				"attachment",
				() -> {
					DLFileEntry dlFileEntry = _addDLFileEntry();

					return String.valueOf(dlFileEntry.getFileEntryId());
				}
			).build(),
			ServiceContextTestUtil.getServiceContext());

		Map<String, Serializable> objectEntryValues = _objectEntry.getValues();

		_fileEntry = _dlAppLocalService.getFileEntry(
			(long)objectEntryValues.get("attachment"));

		_role = _roleLocalService.getRole(
			_company.getCompanyId(), RoleConstants.GUEST);

		_themeDisplay.setCompany(_company);

		_user = UserLocalServiceUtil.getGuestUser(_company.getCompanyId());

		_permissionChecker = PermissionCheckerFactoryUtil.create(_user);
	}

	@After
	public void tearDown() throws PortalException {
		_objectDefinitionLocalService.deleteObjectDefinition(_objectDefinition);
	}

	@Test
	public void testProcessFilter() throws Exception {
		String objectFieldActionId =
			ObjectFieldConstants.ATTACHMENT_FIELD_DOWNLOAD_ACTION_ID_PREFIX +
				_objectField.getName();

		Assert.assertFalse(
			_permissionChecker.hasPermission(
				_user.getGroupId(), _objectDefinition.getClassName(),
				_objectEntry.getObjectEntryId(), objectFieldActionId));

		_testHttpURLConnection(HttpServletResponse.SC_NOT_FOUND);

		_testProcessFilter(
			HttpServletResponse.SC_NOT_FOUND, new String[] {ActionKeys.VIEW});
		_testProcessFilter(
			HttpServletResponse.SC_NOT_FOUND,
			new String[] {objectFieldActionId});
		_testProcessFilter(
			HttpServletResponse.SC_OK,
			new String[] {ActionKeys.VIEW, objectFieldActionId});

		_resourcePermissionLocalService.setResourcePermissions(
			_company.getCompanyId(), DLFileEntry.class.getName(),
			ResourceConstants.SCOPE_INDIVIDUAL,
			String.valueOf(_fileEntry.getFileEntryId()), _role.getRoleId(),
			new String[0]);

		_testHttpURLConnection(HttpServletResponse.SC_NOT_FOUND);
	}

	private DLFileEntry _addDLFileEntry() throws Exception {
		FileEntry fileEntry = _dlAppLocalService.addFileEntry(
			null, TestPropsValues.getUserId(), _company.getGroupId(),
			DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			TempFileEntryUtil.getTempFileName("image.jpg"),
			ContentTypes.APPLICATION_TEXT, RandomTestUtil.randomString(),
			StringPool.BLANK, StringPool.BLANK, StringPool.BLANK,
			new ByteArrayInputStream(RandomTestUtil.randomBytes()), 67, null,
			null, null, ServiceContextTestUtil.getServiceContext());

		return _dlFileEntryLocalService.getFileEntry(
			fileEntry.getFileEntryId());
	}

	private HttpURLConnection _openHttpURLConnection(String urlString)
		throws Exception {

		URL url = new URL(urlString);

		HttpURLConnection httpURLConnection =
			(HttpURLConnection)url.openConnection();

		httpURLConnection.setRequestMethod("GET");

		return httpURLConnection;
	}

	private void _testHttpURLConnection(int expectedStatusCode)
		throws Exception {

		HttpURLConnection httpURLConnection = _openHttpURLConnection(
			StringBundler.concat(
				"http://", _company.getVirtualHostname(), ":8080",
				ObjectFieldUtil.getAttachmentDownloadURL(
					_dlURLHelper, _fileEntry, 0,
					_objectDefinition.getExternalReferenceCode(),
					_objectEntry.getExternalReferenceCode(), _themeDisplay,
					_objectField.getName())));

		httpURLConnection.connect();

		Assert.assertEquals(
			expectedStatusCode, httpURLConnection.getResponseCode());

		httpURLConnection.disconnect();
	}

	private void _testPermissions(String[] permissions) throws Exception {
		_resourcePermissionLocalService.setResourcePermissions(
			_company.getCompanyId(), _objectDefinition.getClassName(),
			ResourceConstants.SCOPE_INDIVIDUAL,
			String.valueOf(_objectEntry.getObjectEntryId()), _role.getRoleId(),
			permissions);

		PermissionCacheUtil.clearCache(_user.getUserId());

		for (String permission : permissions) {
			Assert.assertTrue(
				_permissionChecker.hasPermission(
					_user.getGroupId(), _objectDefinition.getClassName(),
					_objectEntry.getObjectEntryId(), permission));
		}
	}

	private void _testProcessFilter(
			int expectedStatusCode, String[] permissions)
		throws Exception {

		_testPermissions(permissions);

		_testHttpURLConnection(expectedStatusCode);
	}

	private Company _company;

	@Inject
	private CompanyLocalService _companyLocalService;

	@Inject
	private DLAppLocalService _dlAppLocalService;

	@Inject
	private DLFileEntryLocalService _dlFileEntryLocalService;

	@Inject
	private DLURLHelper _dlURLHelper;

	private FileEntry _fileEntry;
	private ObjectDefinition _objectDefinition;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	private ObjectEntry _objectEntry;

	@Inject
	private ObjectEntryLocalService _objectEntryLocalService;

	private ObjectField _objectField;
	private PermissionChecker _permissionChecker;

	@Inject
	private ResourcePermissionLocalService _resourcePermissionLocalService;

	private Role _role;

	@Inject
	private RoleLocalService _roleLocalService;

	private final ThemeDisplay _themeDisplay = new ThemeDisplay();
	private User _user;

}