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

package com.liferay.dynamic.data.mapping.internal.upgrade.v2_0_0;

import com.liferay.portal.kernel.model.ResourceAction;
import com.liferay.portal.kernel.service.ResourceActionLocalService;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.model.impl.ResourceActionImpl;

import java.util.List;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Matchers;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Pedro Queiroz
 */
@RunWith(PowerMockRunner.class)
public class DDMFormInstanceUpgradeProcessTest extends PowerMockito {

	@Before
	public void setUp() throws Exception {
		setUpDDMFormInstanceUpgradeProcess();
	}

	@Test
	public void testGetNewActionIds1() throws Exception {
		mockResourceActionLocalService(
			"RecordSet", this::createRecorSetResourceActionList, "FormInstance",
			this::createFormInstanceResourceActionList);

		// VIEW, ADD_RECORD

		long currentNewActionIds = 9;

		Assert.assertEquals(
			17,
			_ddmFormInstanceUpgradeProcess.getNewActionIds(
				"RecordSet", "FormInstance", 0, currentNewActionIds));
	}

	@Test
	public void testGetNewActionIds2() throws Exception {
		mockResourceActionLocalService(
			"RecordSet", this::createRecorSetResourceActionList, "FormInstance",
			this::createFormInstanceResourceActionList);

		// VIEW, UPDATE, ADD_RECORD

		long currentNewActionIds = 25;

		Assert.assertEquals(
			25,
			_ddmFormInstanceUpgradeProcess.getNewActionIds(
				"RecordSet", "FormInstance", 0, currentNewActionIds));
	}

	protected List<ResourceAction> createFormInstanceResourceActionList() {
		return ListUtil.fromArray(
			createResourceAction("VIEW", 1), createResourceAction("DELETE", 2),
			createResourceAction("PERMISSIONS", 4),
			createResourceAction("UPDATE", 8),
			createResourceAction("ADD_FORM_INSTANCE_RECORD", 16));
	}

	protected List<ResourceAction> createRecorSetResourceActionList() {
		return ListUtil.fromArray(
			createResourceAction("VIEW", 1), createResourceAction("DELETE", 2),
			createResourceAction("PERMISSIONS", 4),
			createResourceAction("ADD_RECORD", 8),
			createResourceAction("UPDATE", 16));
	}

	protected ResourceAction createResourceAction(
		String actionId, long bitwiseValue) {

		ResourceAction resourceAction = new ResourceActionImpl();

		resourceAction.setActionId(actionId);
		resourceAction.setBitwiseValue(bitwiseValue);

		return resourceAction;
	}

	protected void mockResourceActionLocalService(
			String oldName,
			Supplier<List<ResourceAction>> oldResourceActionListSupplier,
			String newName,
			Supplier<List<ResourceAction>> newResourceActionListSupplier)
		throws Exception {

		ResourceActionLocalService resourceActionLocalService = mock(
			ResourceActionLocalService.class);

		when(
			resourceActionLocalService.getResourceActions(Matchers.eq(oldName))
		).thenReturn(
			oldResourceActionListSupplier.get()
		);

		when(
			resourceActionLocalService.getResourceActions(Matchers.eq(newName))
		).thenReturn(
			newResourceActionListSupplier.get()
		);

		field(
			DDMFormInstanceUpgradeProcess.class, "_resourceActionLocalService"
		).set(
			_ddmFormInstanceUpgradeProcess, resourceActionLocalService
		);
	}

	protected void setUpDDMFormInstanceUpgradeProcess() throws Exception {
		_ddmFormInstanceUpgradeProcess = new DDMFormInstanceUpgradeProcess(
			null, null, null, null, null);
	}

	private DDMFormInstanceUpgradeProcess _ddmFormInstanceUpgradeProcess;

}