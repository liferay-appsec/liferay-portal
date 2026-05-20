/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.audit.rest.internal.graphql.servlet.v1_0;

import com.liferay.portal.kernel.util.ObjectValuePair;
import com.liferay.portal.security.audit.rest.internal.graphql.mutation.v1_0.Mutation;
import com.liferay.portal.security.audit.rest.internal.graphql.query.v1_0.Query;
import com.liferay.portal.security.audit.rest.internal.resource.v1_0.AuditEventResourceImpl;
import com.liferay.portal.security.audit.rest.resource.v1_0.AuditEventResource;
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
 * @author Rafael Praxedes
 * @generated
 */
@Component(service = ServletData.class)
@Generated("")
public class ServletDataImpl implements ServletData {

	@Activate
	public void activate(BundleContext bundleContext) {
		Mutation.setAuditEventResourceComponentServiceObjects(
			_auditEventResourceComponentServiceObjects);

		Query.setAuditEventResourceComponentServiceObjects(
			_auditEventResourceComponentServiceObjects);
	}

	public String getApplicationName() {
		return "Liferay.Audit.REST";
	}

	@Override
	public Mutation getMutation() {
		return new Mutation();
	}

	@Override
	public String getPath() {
		return "/audit-graphql/v1_0";
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
						"mutation#createAuditEventsPageExportBatch",
						new ObjectValuePair<>(
							AuditEventResourceImpl.class,
							"postAuditEventsPageExportBatch"));

					put(
						"query#auditEvent",
						new ObjectValuePair<>(
							AuditEventResourceImpl.class, "getAuditEvent"));
					put(
						"query#auditEvents",
						new ObjectValuePair<>(
							AuditEventResourceImpl.class,
							"getAuditEventsPage"));
				}
			};

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<AuditEventResource>
		_auditEventResourceComponentServiceObjects;

}
// LIFERAY-REST-BUILDER-HASH:248937522