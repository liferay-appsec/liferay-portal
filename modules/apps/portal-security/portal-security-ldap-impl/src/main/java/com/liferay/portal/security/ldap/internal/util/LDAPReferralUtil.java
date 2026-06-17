/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.ldap.internal.util;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.security.ldap.constants.LDAPConstants;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Queue;

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

	public static void addSafeReferralEnvironmentProperties(
		Map<? super String, ? super String> environment, String referral) {

		environment.put(_TRUST_URL_CODEBASE_COSNAMING, "false");
		environment.put(_TRUST_URL_CODEBASE_LDAP, "false");
		environment.put(_TRUST_URL_CODEBASE_RMI, "false");

		if (Objects.equals(referral, LDAPConstants.REFERRAL_FOLLOW)) {
			environment.put(Context.REFERRAL, LDAPConstants.REFERRAL_THROWS);
		}
		else {
			environment.put(Context.REFERRAL, referral);
		}
	}

	public static NamingEnumeration<SearchResult> searchWithSafeReferrals(
			DirContext dirContext, String filter, Object[] filterArguments,
			Name name, SearchControls searchControls)
		throws NamingException {

		List<SearchResult> searchResults = new ArrayList<>();

		Queue<DirContext> dirContexts = new ArrayDeque<>();

		dirContexts.add(dirContext);

		int referralCount = 0;

		while (!dirContexts.isEmpty()) {
			DirContext currentDirContext = dirContexts.poll();

			NamingEnumeration<SearchResult> enumeration = null;

			try {
				enumeration = currentDirContext.search(
					name, filter, filterArguments, searchControls);

				while (enumeration.hasMore()) {
					searchResults.add(enumeration.next());
				}
			}
			catch (ReferralException referralException) {
				boolean hasReferral = true;

				while (hasReferral) {
					Object referralInfo = referralException.getReferralInfo();

					if ((referralInfo instanceof String) &&
						_isAllowedReferralURL((String)referralInfo)) {

						if (referralCount >= _MAX_REFERRAL_COUNT) {
							if (_log.isDebugEnabled()) {
								_log.debug(
									"Stopped following LDAP referrals after " +
										"reaching the maximum count of " +
											_MAX_REFERRAL_COUNT);
							}
						}
						else {
							referralCount++;

							dirContexts.add(
								(DirContext)
									referralException.getReferralContext());
						}
					}
					else if (_log.isDebugEnabled()) {
						_log.debug(
							"Blocked an LDAP referral to a non-LDAP URL: " +
								referralInfo);
					}

					hasReferral = referralException.skipReferral();
				}
			}
			finally {
				if (enumeration != null) {
					enumeration.close();
				}

				if (currentDirContext != dirContext) {
					currentDirContext.close();
				}
			}
		}

		return new ListNamingEnumeration(searchResults);
	}

	private static boolean _isAllowedReferralURL(String url) {
		if (Validator.isNull(url)) {
			return false;
		}

		String normalizedURL = StringUtil.toLowerCase(StringUtil.trim(url));

		if (normalizedURL.startsWith("ldap://") ||
			normalizedURL.startsWith("ldaps://")) {

			return true;
		}

		return false;
	}

	private static final int _MAX_REFERRAL_COUNT = 10;

	private static final String _TRUST_URL_CODEBASE_COSNAMING =
		"com.sun.jndi.cosnaming.object.trustURLCodebase";

	private static final String _TRUST_URL_CODEBASE_LDAP =
		"com.sun.jndi.ldap.object.trustURLCodebase";

	private static final String _TRUST_URL_CODEBASE_RMI =
		"com.sun.jndi.rmi.object.trustURLCodebase";

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