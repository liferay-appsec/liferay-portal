/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.fips.internal.osgi.commands;

import com.liferay.osgi.util.osgi.commands.OSGiCommands;
import com.liferay.petra.sql.dsl.DSLFunctionFactoryUtil;
import com.liferay.petra.sql.dsl.DSLQueryFactoryUtil;
import com.liferay.petra.sql.dsl.expression.Predicate;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.UserConstants;
import com.liferay.portal.kernel.model.UserTable;
import com.liferay.portal.kernel.security.fips.FIPSModeValidator;
import com.liferay.portal.kernel.security.pwd.PasswordEncryptor;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.service.command.Descriptor;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Jorge García Jiménez
 */
@Component(
	property = {"osgi.command.function=readiness", "osgi.command.scope=fips"},
	service = OSGiCommands.class
)
public class FIPSOSGiCommands implements OSGiCommands {

	@Descriptor("List the FIPS boot blockers and the stored hash formats.")
	public String readiness() throws Exception {
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

		Map<String, Integer> passwordHashCounts = _getPasswordHashCounts();

		if (!passwordHashCounts.isEmpty()) {
			lines.add(StringPool.BLANK);
			lines.add("Password hashes by format:");

			for (Map.Entry<String, Integer> entry :
					passwordHashCounts.entrySet()) {

				lines.add(
					StringBundler.concat(
						"- ", entry.getKey(), ": ", entry.getValue()));
			}

			lines.add(
				"In FIPS mode, a user whose stored hash is not PBKDF2 must " +
					"reset it. See \"Passwords\" in the FIPS migration guide.");
		}

		lines.add(StringPool.BLANK);
		lines.add(
			"Only the FIPS boot checks the JVM: the security providers, the " +
				"SecureRandom provider, and the TLS and certificate " +
					"revocation properties.");

		return StringUtil.merge(lines, StringPool.NEW_LINE);
	}

	private int _count(Predicate predicate) {
		return _userLocalService.dslQueryCount(
			DSLQueryFactoryUtil.count(
			).from(
				UserTable.INSTANCE
			).where(
				predicate
			));
	}

	private Map<String, Integer> _getPasswordHashCounts() throws Exception {
		Map<String, Integer> passwordHashCounts = new LinkedHashMap<>();

		_companyLocalService.forEachCompanyId(
			companyId -> {
				Predicate predicate = UserTable.INSTANCE.companyId.eq(
					companyId
				).and(
					UserTable.INSTANCE.type.neq(UserConstants.TYPE_GUEST)
				);

				Predicate hashedPredicate = predicate.and(
					UserTable.INSTANCE.passwordEncrypted.eq(true)
				).and(
					UserTable.INSTANCE.password.like("_%")
				);

				int otherPrefixCount = _count(
					hashedPredicate.and(
						UserTable.INSTANCE.password.like("{%")));

				for (String algorithm : _ALGORITHMS) {
					int count = _count(
						hashedPredicate.and(
							DSLFunctionFactoryUtil.lower(
								UserTable.INSTANCE.password
							).like(
								StringBundler.concat(
									"{", StringUtil.toLowerCase(algorithm),
									"}%")
							)));

					passwordHashCounts.merge(algorithm, count, Integer::sum);

					otherPrefixCount -= count;
				}

				passwordHashCounts.merge(
					"Another Prefix", otherPrefixCount, Integer::sum);
				passwordHashCounts.merge(
					"No Prefix",
					_count(
						hashedPredicate.and(
							UserTable.INSTANCE.password.notLike("{%"))),
					Integer::sum);
				passwordHashCounts.merge(
					"Not Hashed",
					_count(
						predicate.and(
							UserTable.INSTANCE.passwordEncrypted.eq(false))),
					Integer::sum);
			});

		Collection<Integer> values = passwordHashCounts.values();

		values.removeIf(count -> count == 0);

		return passwordHashCounts;
	}

	private static final String[] _ALGORITHMS = {
		PasswordEncryptor.TYPE_BCRYPT, PasswordEncryptor.TYPE_MD2,
		PasswordEncryptor.TYPE_MD5, PasswordEncryptor.TYPE_NONE,
		"PBKDF2WITHHMACSHA1", "PBKDF2WITHHMACSHA256", "PBKDF2WITHHMACSHA384",
		"PBKDF2WITHHMACSHA512", PasswordEncryptor.TYPE_SHA,
		PasswordEncryptor.TYPE_SHA_256, PasswordEncryptor.TYPE_SHA_384,
		PasswordEncryptor.TYPE_SSHA, PasswordEncryptor.TYPE_UFC_CRYPT
	};

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private UserLocalService _userLocalService;

}