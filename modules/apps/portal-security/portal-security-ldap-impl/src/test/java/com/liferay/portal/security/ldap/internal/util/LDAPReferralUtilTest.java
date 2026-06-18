/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.ldap.internal.util;

import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.ReferralException;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Lucas Miranda
 */
public class LDAPReferralUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testAddSafeReferralEnvironmentProperties() {
		Map<String, String> environment = new HashMap<>();

		LDAPReferralUtil.addSafeReferralEnvironmentProperties(
			environment, "follow");

		Assert.assertEquals("throw", environment.get(Context.REFERRAL));

		_assertTrustURLCodebaseDisabled(environment);

		LDAPReferralUtil.addSafeReferralEnvironmentProperties(
			environment, "ignore");

		Assert.assertEquals("ignore", environment.get(Context.REFERRAL));

		_assertTrustURLCodebaseDisabled(environment);

		LDAPReferralUtil.addSafeReferralEnvironmentProperties(
			environment, "throw");

		Assert.assertEquals("throw", environment.get(Context.REFERRAL));

		_assertTrustURLCodebaseDisabled(environment);
	}

	@Test
	public void testIsAllowedReferralURL() {
		Assert.assertFalse(_isAllowedReferralURL(""));
		Assert.assertTrue(_isAllowedReferralURL("  ldap://host:389  "));
		Assert.assertFalse(_isAllowedReferralURL("corba://host/exploit"));
		Assert.assertFalse(_isAllowedReferralURL("dns://host"));
		Assert.assertFalse(_isAllowedReferralURL("host:389"));
		Assert.assertFalse(_isAllowedReferralURL("http://host/exploit"));
		Assert.assertTrue(
			_isAllowedReferralURL("ldap://host1:389 ldap://host2:389"));
		Assert.assertFalse(
			_isAllowedReferralURL("ldap://host1:389 rmi://host2:1099/exploit"));
		Assert.assertTrue(_isAllowedReferralURL("LDAP://host:389"));
		Assert.assertTrue(_isAllowedReferralURL("ldap://host:389"));
		Assert.assertTrue(_isAllowedReferralURL("ldaps://host:636"));
		Assert.assertFalse(_isAllowedReferralURL("ldapx://host"));
		Assert.assertFalse(_isAllowedReferralURL("rmi://host:1099/exploit"));
		Assert.assertFalse(_isAllowedReferralURL(null));
	}

	@Test
	public void testSearchWithSafeReferrals() throws Exception {
		DirContext dirContext = Mockito.mock(DirContext.class);

		ReferralException referralException = Mockito.mock(
			ReferralException.class);

		Mockito.when(
			referralException.getReferralInfo()
		).thenReturn(
			"rmi://attacker:1099/exploit"
		);

		Mockito.when(
			referralException.skipReferral()
		).thenReturn(
			false
		);

		NamingEnumeration<SearchResult> enumeration = Mockito.mock(
			NamingEnumeration.class);

		Mockito.when(
			enumeration.hasMore()
		).thenThrow(
			referralException
		);

		Mockito.when(
			dirContext.search(
				Mockito.any(Name.class), Mockito.anyString(),
				Mockito.any(Object[].class), Mockito.any(SearchControls.class))
		).thenReturn(
			enumeration
		);

		NamingEnumeration<SearchResult> resultEnumeration =
			LDAPReferralUtil.searchWithSafeReferrals(
				dirContext, "(cn=*)", new Object[0], Mockito.mock(Name.class),
				new SearchControls());

		Assert.assertFalse(resultEnumeration.hasMore());

		Mockito.verify(
			referralException, Mockito.never()
		).getReferralContext();

		dirContext = Mockito.mock(DirContext.class);

		referralException = Mockito.mock(ReferralException.class);

		Mockito.when(
			referralException.getReferralInfo()
		).thenReturn(
			"ldap://other:389"
		);

		Mockito.when(
			referralException.skipReferral()
		).thenReturn(
			false
		);

		enumeration = Mockito.mock(NamingEnumeration.class);

		Mockito.when(
			enumeration.hasMore()
		).thenThrow(
			referralException
		);

		Mockito.when(
			dirContext.search(
				Mockito.any(Name.class), Mockito.anyString(),
				Mockito.any(Object[].class), Mockito.any(SearchControls.class))
		).thenReturn(
			enumeration
		);

		DirContext referralContext = Mockito.mock(DirContext.class);

		Mockito.when(
			referralException.getReferralContext()
		).thenReturn(
			referralContext
		);

		SearchResult searchResult = new SearchResult(
			"cn=test", null, new BasicAttributes());

		NamingEnumeration<SearchResult> referralEnumeration = Mockito.mock(
			NamingEnumeration.class);

		Mockito.when(
			referralEnumeration.hasMore()
		).thenReturn(
			true, false
		);

		Mockito.when(
			referralEnumeration.next()
		).thenReturn(
			searchResult
		);

		Mockito.when(
			referralContext.search(
				Mockito.any(Name.class), Mockito.anyString(),
				Mockito.any(Object[].class), Mockito.any(SearchControls.class))
		).thenReturn(
			referralEnumeration
		);

		resultEnumeration = LDAPReferralUtil.searchWithSafeReferrals(
			dirContext, "(cn=*)", new Object[0], Mockito.mock(Name.class),
			new SearchControls());

		Assert.assertTrue(resultEnumeration.hasMore());
		Assert.assertSame(searchResult, resultEnumeration.next());
		Assert.assertFalse(resultEnumeration.hasMore());

		Mockito.verify(
			referralException, Mockito.times(1)
		).getReferralContext();
	}

	private void _assertTrustURLCodebaseDisabled(
		Map<String, String> environment) {

		Assert.assertEquals(
			"false",
			environment.get(
				ReflectionTestUtil.getFieldValue(
					LDAPReferralUtil.class, "_TRUST_URL_CODEBASE_COSNAMING")));
		Assert.assertEquals(
			"false",
			environment.get(
				ReflectionTestUtil.getFieldValue(
					LDAPReferralUtil.class, "_TRUST_URL_CODEBASE_LDAP")));
		Assert.assertEquals(
			"false",
			environment.get(
				ReflectionTestUtil.getFieldValue(
					LDAPReferralUtil.class, "_TRUST_URL_CODEBASE_RMI")));
	}

	private boolean _isAllowedReferralURL(String url) {
		return ReflectionTestUtil.invoke(
			LDAPReferralUtil.class, "_isAllowedReferralURL",
			new Class<?>[] {String.class}, url);
	}

}