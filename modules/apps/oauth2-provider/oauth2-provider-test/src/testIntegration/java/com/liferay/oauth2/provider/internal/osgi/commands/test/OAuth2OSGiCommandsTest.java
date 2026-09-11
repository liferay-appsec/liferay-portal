/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth2.provider.internal.osgi.commands.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.oauth2.provider.internal.scheduler.test.BaseUnresolvedScopeAliasesTestCase;
import com.liferay.oauth2.provider.model.OAuth2Application;
import com.liferay.osgi.util.osgi.commands.OSGiCommands;
import com.liferay.petra.function.UnsafeRunnable;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.test.rule.Inject;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import java.nio.charset.StandardCharsets;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Christopher Kian
 */
@RunWith(Arquillian.class)
public class OAuth2OSGiCommandsTest extends BaseUnresolvedScopeAliasesTestCase {

	@Test
	public void testListUnresolvedScopes() throws Exception {
		_testListUnresolvedScopes();
		_testListUnresolvedScopesWithoutOAuth2Application();
	}

	@Test
	public void testReconcile() throws Exception {
		long companyId = TestPropsValues.getCompanyId();

		try (SafeCloseable safeCloseable = CompanyThreadLocal.lock(companyId)) {
			OAuth2Application oAuth2Application = addOAuth2Application(
				companyId);

			String scopeAlias = getResolvableScopeAlias(companyId);
			long oAuth2ApplicationId =
				oAuth2Application.getOAuth2ApplicationId();

			unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
				companyId, oAuth2ApplicationId,
				Collections.singletonList(scopeAlias));

			String output = _getOutput(
				() -> ReflectionTestUtil.invoke(
					_osgiCommands, "reconcile", new Class<?>[0]));

			Assert.assertTrue(hasScopeAlias(oAuth2ApplicationId, scopeAlias));
			Assert.assertTrue(
				output,
				output.contains(
					"Previously unresolved scope aliases were bound"));
		}
	}

	private String _getOutput(UnsafeRunnable<Exception> unsafeRunnable)
		throws Exception {

		ByteArrayOutputStream byteArrayOutputStream =
			new ByteArrayOutputStream();
		PrintStream printStream = System.out;

		System.setOut(new PrintStream(byteArrayOutputStream, true, "UTF-8"));

		try {
			unsafeRunnable.run();
		}
		finally {
			System.setOut(printStream);
		}

		return new String(
			byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8);
	}

	private void _testListUnresolvedScopes() throws Exception {
		long companyId = TestPropsValues.getCompanyId();

		try (SafeCloseable safeCloseable = CompanyThreadLocal.lock(companyId)) {
			OAuth2Application oAuth2Application = addOAuth2Application(
				companyId);

			long oAuth2ApplicationId =
				oAuth2Application.getOAuth2ApplicationId();

			String output = _getOutput(
				() -> ReflectionTestUtil.invoke(
					_osgiCommands, "listUnresolvedScopes", new Class<?>[0]));

			Assert.assertTrue(
				output, output.contains("application " + oAuth2ApplicationId));
			Assert.assertTrue(
				output, output.contains("has unresolved scope aliases"));
			Assert.assertTrue(
				output,
				output.contains(
					"named \"" + oAuth2Application.getName() + "\""));
		}
	}

	private void _testListUnresolvedScopesWithoutOAuth2Application()
		throws Exception {

		long companyId = TestPropsValues.getCompanyId();

		try (SafeCloseable safeCloseable = CompanyThreadLocal.lock(companyId)) {
			long oAuth2ApplicationId = RandomTestUtil.randomLong();

			unresolvedScopeAliasesRegistry.setUnresolvedScopeAliases(
				companyId, oAuth2ApplicationId,
				Collections.singletonList(RandomTestUtil.randomString()));

			try {
				String output = _getOutput(
					() -> ReflectionTestUtil.invoke(
						_osgiCommands, "listUnresolvedScopes",
						new Class<?>[0]));

				Assert.assertTrue(
					output,
					output.contains(
						"application " + oAuth2ApplicationId +
							" has unresolved scope aliases"));
			}
			finally {
				unresolvedScopeAliasesRegistry.removeUnresolvedScopeAliases(
					companyId, oAuth2ApplicationId);
			}
		}
	}

	@Inject(filter = "osgi.command.function=listUnresolvedScopes")
	private OSGiCommands _osgiCommands;

}