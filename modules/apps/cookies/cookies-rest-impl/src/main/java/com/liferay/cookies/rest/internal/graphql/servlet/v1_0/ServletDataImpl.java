/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.cookies.rest.internal.graphql.servlet.v1_0;

import com.liferay.cookies.rest.internal.graphql.mutation.v1_0.Mutation;
import com.liferay.cookies.rest.internal.graphql.query.v1_0.Query;
import com.liferay.cookies.rest.internal.resource.v1_0.ConsentPreferenceResourceImpl;
import com.liferay.cookies.rest.resource.v1_0.ConsentPreferenceResource;
import com.liferay.portal.kernel.util.ObjectValuePair;
import com.liferay.portal.vulcan.graphql.servlet.ServletData;

import jakarta.annotation.Generated;

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentServiceObjects;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;

/**
 * @author Christopher Kian
 * @generated
 */
@Component(service = ServletData.class)
@Generated("")
public class ServletDataImpl implements ServletData {

	@Activate
	public void activate(BundleContext bundleContext) {
		Mutation.setConsentPreferenceResourceComponentServiceObjects(
			_consentPreferenceResourceComponentServiceObjects);

		Query.setConsentPreferenceResourceComponentServiceObjects(
			_consentPreferenceResourceComponentServiceObjects);
	}

	public String getApplicationName() {
		return "Liferay.Cookies.REST";
	}

	@Override
	public Mutation getMutation() {
		return new Mutation();
	}

	@Override
	public String getPath() {
		return "/cookies-graphql/v1_0";
	}

	@Override
	public Query getQuery() {
		return new Query();
	}

	public ObjectValuePair<Class<?>, String> getResourceMethodObjectValuePair(
		String methodName, boolean mutation) {

		if (mutation) {
			return _resourceMethodObjectValuePairs.get(
				"mutation#" + methodName);
		}

		return _resourceMethodObjectValuePairs.get("query#" + methodName);
	}

	private static final Map<String, ObjectValuePair<Class<?>, String>>
		_resourceMethodObjectValuePairs =
			new HashMap<String, ObjectValuePair<Class<?>, String>>() {
				{
					put(
						"mutation#deleteConsentPreferenceByName",
						new ObjectValuePair<>(
							ConsentPreferenceResourceImpl.class,
							"deleteConsentPreferenceByName"));
					put(
						"mutation#deleteConsentPreferences",
						new ObjectValuePair<>(
							ConsentPreferenceResourceImpl.class,
							"deleteConsentPreferences"));
					put(
						"mutation#patchConsentPreference",
						new ObjectValuePair<>(
							ConsentPreferenceResourceImpl.class,
							"patchConsentPreference"));
					put(
						"mutation#createConsentPreference",
						new ObjectValuePair<>(
							ConsentPreferenceResourceImpl.class,
							"postConsentPreference"));
					put(
						"mutation#createConsentPreferenceBatch",
						new ObjectValuePair<>(
							ConsentPreferenceResourceImpl.class,
							"postConsentPreferenceBatch"));
					put(
						"mutation#createConsentPreferencesPageExportBatch",
						new ObjectValuePair<>(
							ConsentPreferenceResourceImpl.class,
							"postConsentPreferencesPageExportBatch"));
					put(
						"mutation#updateConsentPreference",
						new ObjectValuePair<>(
							ConsentPreferenceResourceImpl.class,
							"putConsentPreference"));
					put(
						"mutation#updateConsentPreferenceBatch",
						new ObjectValuePair<>(
							ConsentPreferenceResourceImpl.class,
							"putConsentPreferenceBatch"));

					put(
						"query#consentPreferenceByName",
						new ObjectValuePair<>(
							ConsentPreferenceResourceImpl.class,
							"getConsentPreferenceByName"));
					put(
						"query#consentPreferences",
						new ObjectValuePair<>(
							ConsentPreferenceResourceImpl.class,
							"getConsentPreferencesPage"));
				}
			};

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<ConsentPreferenceResource>
		_consentPreferenceResourceComponentServiceObjects;

}