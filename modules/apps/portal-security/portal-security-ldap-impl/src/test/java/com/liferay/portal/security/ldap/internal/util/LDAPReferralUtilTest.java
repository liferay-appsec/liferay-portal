/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.ldap.internal.util;

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
	public void testHardenReferralEnvironment() {
		Map<String, String> environment = new HashMap<>();

		LDAPReferralUtil.hardenReferralEnvironment(environment, "follow");

		Assert.assertEquals("throw", environment.get(Context.REFERRAL));

		_assertTrustURLCodebaseDisabled(environment);

		LDAPReferralUtil.hardenReferralEnvironment(environment, "ignore");

		Assert.assertEquals("ignore", environment.get(Context.REFERRAL));

		_assertTrustURLCodebaseDisabled(environment);

		LDAPReferralUtil.hardenReferralEnvironment(environment, "throw");

		Assert.assertEquals("throw", environment.get(Context.REFERRAL));

		_assertTrustURLCodebaseDisabled(environment);
	}

	@Test
	public void testIsAllowedReferralURL() {
		Assert.assertTrue(
			LDAPReferralUtil.isAllowedReferralURL("ldap://host:389"));
		Assert.assertTrue(
			LDAPReferralUtil.isAllowedReferralURL("ldaps://host:636"));
		Assert.assertTrue(
			LDAPReferralUtil.isAllowedReferralURL("LDAP://host:389"));
		Assert.assertTrue(
			LDAPReferralUtil.isAllowedReferralURL("  ldap://host:389  "));

		Assert.assertFalse(
			LDAPReferralUtil.isAllowedReferralURL("rmi://host:1099/exploit"));
		Assert.assertFalse(
			LDAPReferralUtil.isAllowedReferralURL("corba://host/exploit"));
		Assert.assertFalse(LDAPReferralUtil.isAllowedReferralURL("dns://host"));
		Assert.assertFalse(
			LDAPReferralUtil.isAllowedReferralURL("http://host/exploit"));
		Assert.assertFalse(
			LDAPReferralUtil.isAllowedReferralURL("ldapx://host"));
		Assert.assertFalse(LDAPReferralUtil.isAllowedReferralURL("host:389"));
		Assert.assertFalse(LDAPReferralUtil.isAllowedReferralURL(""));
		Assert.assertFalse(LDAPReferralUtil.isAllowedReferralURL(null));
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
				dirContext, Mockito.mock(Name.class), "(cn=*)", new Object[0],
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
			dirContext, Mockito.mock(Name.class), "(cn=*)", new Object[0],
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
			environment.get("com.sun.jndi.rmi.object.trustURLCodebase"));
		Assert.assertEquals(
			"false",
			environment.get("com.sun.jndi.cosnaming.object.trustURLCodebase"));
		Assert.assertEquals(
			"false",
			environment.get("com.sun.jndi.ldap.object.trustURLCodebase"));
	}

}