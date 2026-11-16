/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.fips.internal.osgi.commands;

import com.liferay.osgi.util.osgi.commands.OSGiCommands;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.security.fips.FIPSModeValidator;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

import org.apache.felix.service.command.Descriptor;

import org.osgi.service.component.annotations.Component;

/**
 * @author Jorge García Jiménez
 */
@Component(
	property = {"osgi.command.function=readiness", "osgi.command.scope=fips"},
	service = OSGiCommands.class
)
public class FIPSOSGiCommands implements OSGiCommands {

	@Descriptor("List the FIPS boot blockers before switching to FIPS mode.")
	public String readiness() {
		List<String> violations = new ArrayList<>(
			FIPSModeValidator.getPortalPropertiesViolations());

		violations.addAll(FIPSModeValidator.getKeyEncryptionKeyViolations());

		List<String> lines = new ArrayList<>();

		if (violations.isEmpty()) {
			lines.add("No FIPS boot blockers were found.");
		}
		else {
			lines.add("FIPS boot blockers:");

			for (int i = 0; i < violations.size(); i++) {
				lines.add(StringBundler.concat(i + 1, ". ", violations.get(i)));
			}
		}

		lines.add(StringPool.BLANK);
		lines.add(
			"Only the FIPS boot checks the JVM: the security providers, the " +
				"SecureRandom provider, and the TLS and certificate " +
					"revocation properties.");

		return StringUtil.merge(lines, StringPool.NEW_LINE);
	}

}