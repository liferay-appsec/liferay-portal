/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.journal.web.internal.portlet.configuration.icon;

import com.liferay.journal.web.internal.configuration.FFImportStructureConfiguration;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.util.ResourceBundleUtil;

import javax.portlet.PortletRequest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mockito;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Marcela Cunha
 */
@PrepareForTest({ResourceBundleUtil.class, LanguageUtil.class})
@RunWith(PowerMockRunner.class)
public class ImportStructurePortletConfigurationIconTest {

	@Before
	public void setUp() {
		ReflectionTestUtil.setFieldValue(
			_importStructurePortletConfigurationIcon,
			"_ffImportStructureConfiguration", _ffImportStructureConfiguration);
	}

	@Test
	public void testGetJspPath() {
		Assert.assertEquals(
			"/configuration/icon/import_structure.jsp",
			_importStructurePortletConfigurationIcon.getJspPath());
	}

	@Test
	public void testIsShowWithFFImportStructureConfigurationDisabled() {
		mockFFImportStructureConfiguration(false);

		Assert.assertFalse(
			_importStructurePortletConfigurationIcon.isShow(_portletRequest));
	}

	@Test
	public void testIsShowWithFFImportStructureConfigurationEnabled() {
		mockFFImportStructureConfiguration(true);

		Assert.assertTrue(
			_importStructurePortletConfigurationIcon.isShow(_portletRequest));
	}

	protected void mockFFImportStructureConfiguration(boolean enabled) {
		Mockito.when(
			_ffImportStructureConfiguration.enabled()
		).thenReturn(
			enabled
		);
	}

	private final FFImportStructureConfiguration
		_ffImportStructureConfiguration = Mockito.mock(
			FFImportStructureConfiguration.class);
	private final ImportStructurePortletConfigurationIcon
		_importStructurePortletConfigurationIcon =
			new ImportStructurePortletConfigurationIcon();
	private final PortletRequest _portletRequest = Mockito.mock(
		PortletRequest.class);

}