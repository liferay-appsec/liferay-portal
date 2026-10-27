/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.fips.cbom.builder;

import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cyclonedx.model.Component;
import org.cyclonedx.model.Dependency;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

/**
 * @author Manuele Castro
 */
public class CycloneDXCBOMWriterTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@BeforeClass
	public static void setUpClass() {
		JSONFactoryUtil jsonFactoryUtil = new JSONFactoryUtil();

		jsonFactoryUtil.setJSONFactory(new JSONFactoryImpl());
	}

	@Test
	public void testWrite() throws Exception {
		Component component1 = new Component();

		component1.setBomRef(RandomTestUtil.randomString());

		Component component2 = new Component();

		component2.setBomRef(RandomTestUtil.randomString());

		List<Component> components = Arrays.asList(component1, component2);

		Dependency dependency1 = new Dependency(component1.getBomRef());

		dependency1.setDependencies(
			Collections.singletonList(new Dependency(component2.getBomRef())));

		Dependency dependency2 = new Dependency(component2.getBomRef());

		dependency2.setDependencies(Collections.emptyList());

		List<Dependency> dependencies = Arrays.asList(dependency1, dependency2);

		Map<String, Set<String>> provides =
			HashMapBuilder.<String, Set<String>>put(
				component2.getBomRef(),
				Collections.singleton(component1.getBomRef())
			).build();

		String configurationName = RandomTestUtil.randomString();
		String productName = RandomTestUtil.randomString();
		String productVersion = RandomTestUtil.randomString();

		String json = CycloneDXCBOMWriter.write(
			components, configurationName, dependencies, productName,
			productVersion, provides);

		Assert.assertEquals(
			json,
			CycloneDXCBOMWriter.write(
				components, configurationName, dependencies, productName,
				productVersion, provides));

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject(json);

		String serialNumber = jsonObject.getString("serialNumber");

		Assert.assertTrue(
			serialNumber.matches(
				"urn:uuid:[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-" +
					"[89ab][0-9a-f]{3}-[0-9a-f]{12}"));

		JSONAssert.assertEquals(
			JSONUtil.put(
				"bomFormat", "CycloneDX"
			).put(
				"components",
				JSONUtil.putAll(
					JSONUtil.put("bom-ref", component1.getBomRef()),
					JSONUtil.put("bom-ref", component2.getBomRef()))
			).put(
				"dependencies",
				JSONUtil.putAll(
					JSONUtil.put(
						"dependsOn", JSONUtil.putAll(component2.getBomRef())
					).put(
						"ref", component1.getBomRef()
					),
					JSONUtil.put(
						"dependsOn", JSONUtil.putAll()
					).put(
						"provides", JSONUtil.putAll(component1.getBomRef())
					).put(
						"ref", component2.getBomRef()
					))
			).put(
				"metadata",
				JSONUtil.put(
					"component",
					JSONUtil.put(
						"name", productName
					).put(
						"type", "application"
					).put(
						"version", productVersion
					)
				).put(
					"lifecycles",
					JSONUtil.putAll(JSONUtil.put("phase", "post-build"))
				).put(
					"properties",
					JSONUtil.putAll(
						JSONUtil.put(
							"name", "liferay:cbom:configuration"
						).put(
							"value", configurationName
						),
						JSONUtil.put(
							"name", "liferay:cbom:schema-version"
						).put(
							"value", "1.0"
						))
				)
			).put(
				"serialNumber", serialNumber
			).put(
				"specVersion", "1.6"
			).put(
				"version", 1
			).toString(),
			json, JSONCompareMode.STRICT);

		Assert.assertNotEquals(
			json,
			CycloneDXCBOMWriter.write(
				components, RandomTestUtil.randomString(), dependencies,
				productName, productVersion, provides));
		Assert.assertNotEquals(
			json,
			CycloneDXCBOMWriter.write(
				components, configurationName, dependencies, productName,
				RandomTestUtil.randomString(), provides));
	}

}