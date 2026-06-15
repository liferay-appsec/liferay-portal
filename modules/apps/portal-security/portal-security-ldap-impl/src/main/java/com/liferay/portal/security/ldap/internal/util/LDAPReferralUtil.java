/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.ldap.internal.util;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.ReferralException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

/**
 * @author Lucas Miranda
 */
public class LDAPReferralUtil {

	public static void hardenReferralEnvironment(
		Map<? super String, ? super String> environment, String referral) {

		environment.put(
			"com.sun.jndi.cosnaming.object.trustURLCodebase", "false");
		environment.put("com.sun.jndi.ldap.object.trustURLCodebase", "false");
		environment.put("com.sun.jndi.rmi.object.trustURLCodebase", "false");

		if (isManagedFollow(referral)) {
			environment.put(Context.REFERRAL, "throw");
		}
		else {
			environment.put(Context.REFERRAL, referral);
		}
	}

	public static boolean isAllowedReferralURL(String url) {
		if (Validator.isNull(url)) {
			return false;
		}

		String scheme = StringUtil.toLowerCase(StringUtil.trim(url));

		if (scheme.startsWith("ldaps://") || scheme.startsWith("ldap://")) {
			return true;
		}

		return false;
	}

	public static boolean isManagedFollow(String referral) {
		return Objects.equals(referral, "follow");
	}

	public static NamingEnumeration<SearchResult> searchWithSafeReferrals(
			DirContext dirContext, Name name, String filter,
			Object[] filterArguments, SearchControls searchControls)
		throws NamingException {

		List<SearchResult> searchResults = new ArrayList<>();

		_collectSearchResults(
			dirContext, name, filter, filterArguments, searchControls,
			searchResults, 0);

		return new ListNamingEnumeration(searchResults);
	}

	private static void _collectSearchResults(
			DirContext dirContext, Name name, String filter,
			Object[] filterArguments, SearchControls searchControls,
			List<SearchResult> searchResults, int depth)
		throws NamingException {

		NamingEnumeration<SearchResult> enumeration = null;

		try {
			enumeration = dirContext.search(
				name, filter, filterArguments, searchControls);

			while (enumeration.hasMore()) {
				searchResults.add(enumeration.next());
			}
		}
		catch (ReferralException referralException) {
			if (depth >= _MAX_REFERRAL_DEPTH) {
				if (_log.isDebugEnabled()) {
					_log.debug(
						"Stopped following LDAP referrals after reaching the " +
							"maximum depth of " + _MAX_REFERRAL_DEPTH);
				}

				return;
			}

			while (true) {
				Object referralInfo = referralException.getReferralInfo();

				if ((referralInfo instanceof String) &&
					isAllowedReferralURL((String)referralInfo)) {

					Context referralContext =
						referralException.getReferralContext();

					try {
						_collectSearchResults(
							(DirContext)referralContext, name, filter,
							filterArguments, searchControls, searchResults,
							depth + 1);
					}
					finally {
						referralContext.close();
					}
				}
				else if (_log.isDebugEnabled()) {
					_log.debug(
						"Blocked an LDAP referral to a non-LDAP URL: " +
							referralInfo);
				}

				if (!referralException.skipReferral()) {
					return;
				}
			}
		}
		finally {
			if (enumeration != null) {
				enumeration.close();
			}
		}
	}

	private static final int _MAX_REFERRAL_DEPTH = 10;

	private static final Log _log = LogFactoryUtil.getLog(
		LDAPReferralUtil.class);

	private static class ListNamingEnumeration
		implements NamingEnumeration<SearchResult> {

		public ListNamingEnumeration(List<SearchResult> searchResults) {
			_iterator = searchResults.iterator();
		}

		@Override
		public void close() {
		}

		@Override
		public boolean hasMore() {
			return _iterator.hasNext();
		}

		@Override
		public boolean hasMoreElements() {
			return _iterator.hasNext();
		}

		@Override
		public SearchResult next() {
			return _iterator.next();
		}

		@Override
		public SearchResult nextElement() {
			if (!_iterator.hasNext()) {
				throw new NoSuchElementException();
			}

			return _iterator.next();
		}

		private final Iterator<SearchResult> _iterator;

	}

}