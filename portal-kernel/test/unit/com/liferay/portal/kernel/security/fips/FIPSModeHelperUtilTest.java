/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.security.fips;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.test.util.RandomTestUtil;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import org.w3c.dom.Element;

/**
 * @author Caio Farias
 */
public class FIPSModeHelperUtilTest {

	@Test
	public void testGetElementsMap() {
		Map<String, Element> elements = FIPSModeTestUtil.getElements(
			StringBundler.concat(
				"<config>", FIPSModeTestUtil.XML_ASYM_ENCRYPT,
				FIPSModeTestUtil.XML_AUTH, "</config>"));

		Assert.assertEquals(
			List.of("ASYM_ENCRYPT", "AUTH"), List.copyOf(elements.keySet()));

		Element asymEncryptElement = elements.get("ASYM_ENCRYPT");

		Assert.assertEquals(
			"2048", asymEncryptElement.getAttribute("asym_keylength"));
		Assert.assertEquals(
			FIPSModeTestUtil.TRANSFORMATION_SYM,
			asymEncryptElement.getAttribute("sym_algorithm"));

		Element authElement = elements.get("AUTH");

		Assert.assertEquals(
			FIPSModeTestUtil.AUTH_CLASS,
			authElement.getAttribute("auth_class"));

		Assert.assertEquals(
			Collections.emptyMap(),
			FIPSModeTestUtil.getElements("<config><UDP /></config>"));

		elements = FIPSModeTestUtil.getElements(
			StringBundler.concat(
				"<config><!--", FIPSModeTestUtil.XML_SYM_ENCRYPT, "-->",
				FIPSModeTestUtil.XML_ASYM_ENCRYPT, "</config>"));

		Assert.assertEquals(
			List.of("ASYM_ENCRYPT"), List.copyOf(elements.keySet()));
	}

	@Test
	public void testGetJGroupsProfileElements() throws Exception {
		Path path = Files.createTempFile(null, ".xml");

		try {
			String channelPropertiesXML = StringBundler.concat(
				"<config>", FIPSModeTestUtil.XML_AUTH,
				FIPSModeTestUtil.XML_SYM_ENCRYPT, "</config>");

			Files.write(path, channelPropertiesXML.getBytes());

			Map<String, Element> elements =
				FIPSModeHelperUtil.getJGroupsProfileElements(
					String.valueOf(path));

			Assert.assertEquals(
				List.of("AUTH", "SYM_ENCRYPT"), List.copyOf(elements.keySet()));

			Element authElement = elements.get("AUTH");

			Assert.assertEquals(
				FIPSModeTestUtil.AUTH_CLASS,
				authElement.getAttribute("auth_class"));

			Files.write(path, "<config><AUTH".getBytes());

			FIPSModeTestUtil.assertSecurityException(
				"Unable to parse the JGroups channel properties",
				() -> FIPSModeHelperUtil.getJGroupsProfileElements(
					String.valueOf(path)));
		}
		finally {
			Files.delete(path);
		}

		FIPSModeTestUtil.assertSecurityException(
			"Unable to read the JGroups channel properties",
			() -> FIPSModeHelperUtil.getJGroupsProfileElements(
				RandomTestUtil.randomString()));
	}

}