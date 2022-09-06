/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.analytics.settings.rest.internal.graphql.servlet.v1_0;

import com.liferay.analytics.settings.rest.internal.graphql.mutation.v1_0.Mutation;
import com.liferay.analytics.settings.rest.internal.graphql.query.v1_0.Query;
import com.liferay.analytics.settings.rest.internal.resource.v1_0.ChannelResourceImpl;
import com.liferay.analytics.settings.rest.internal.resource.v1_0.DataSourceResourceImpl;
import com.liferay.analytics.settings.rest.resource.v1_0.ChannelResource;
import com.liferay.analytics.settings.rest.resource.v1_0.DataSourceResource;
import com.liferay.portal.kernel.util.ObjectValuePair;
import com.liferay.portal.vulcan.graphql.servlet.ServletData;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentServiceObjects;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;

/**
 * @author Riccardo Ferrari
 * @generated
 */
@Component(service = ServletData.class)
@Generated("")
public class ServletDataImpl implements ServletData {

	@Activate
	public void activate(BundleContext bundleContext) {
		Mutation.setChannelResourceComponentServiceObjects(
			_channelResourceComponentServiceObjects);
		Mutation.setDataSourceResourceComponentServiceObjects(
			_dataSourceResourceComponentServiceObjects);

		Query.setChannelResourceComponentServiceObjects(
			_channelResourceComponentServiceObjects);
	}

	public String getApplicationName() {
		return "Liferay.Analytyics.Settings.REST";
	}

	@Override
	public Mutation getMutation() {
		return new Mutation();
	}

	@Override
	public String getPath() {
		return "/analytics-settings-rest-graphql/v1_0";
	}

	@Override
	public Query getQuery() {
		return new Query();
	}

	public ObjectValuePair<Class<?>, String> getResourceMethodPair(
		String methodName, boolean mutation) {

		if (mutation) {
			return _resourceMethodPairs.get("mutation#" + methodName);
		}

		return _resourceMethodPairs.get("query#" + methodName);
	}

	private static final Map<String, ObjectValuePair<Class<?>, String>>
		_resourceMethodPairs = new HashMap<>();

	static {
		_resourceMethodPairs.put(
			"mutation#createChannel",
			new ObjectValuePair<>(ChannelResourceImpl.class, "postChannel"));
		_resourceMethodPairs.put(
			"mutation#deleteDataSource",
			new ObjectValuePair<>(
				DataSourceResourceImpl.class, "deleteDataSource"));
		_resourceMethodPairs.put(
			"mutation#createDataSource",
			new ObjectValuePair<>(
				DataSourceResourceImpl.class, "postDataSource"));
		_resourceMethodPairs.put(
			"query#channels",
			new ObjectValuePair<>(
				ChannelResourceImpl.class, "getChannelsPage"));
	}

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<ChannelResource>
		_channelResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<DataSourceResource>
		_dataSourceResourceComponentServiceObjects;

}