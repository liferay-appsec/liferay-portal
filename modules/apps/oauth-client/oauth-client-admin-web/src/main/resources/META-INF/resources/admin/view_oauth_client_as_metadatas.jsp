<%--
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
--%>

<%@ include file="/init.jsp" %>

<%
List<OAuthClientASMetadata> oAuthClientASMetadatas = OAuthClientASMetadataLocalServiceUtil.getOAuthClientASMetadatas(themeDisplay.getCompanyId(), searchContainer.getStart(), searchContainer.getEnd());
%>

<clay:container-fluid
	cssClass="closed"
>
	<liferay-ui:search-container
		emptyResultsMessage="no-oauth-client-as-metadata-found"
		id="oAuthClientASMetadatasSearchContainer"
		iteratorURL="<%= currentURLObj %>"
		rowChecker="<%= new EmptyOnClickRowChecker(renderResponse) %>"
		total="<%= oAuthClientASMetadatas.size() %>"
	>
		<liferay-ui:search-container-results
			results="<%= oAuthClientASMetadatas %>"
		/>

		<liferay-ui:search-container-row
			className="com.liferay.oauth.client.persistence.model.OAuthClientASMetadata"
			escapedModel="<%= true %>"
			keyProperty="issuer"
			modelVar="oAuthClientASMetadata"
		>
			<portlet:renderURL var="editURL">
				<portlet:param name="mvcRenderCommandName" value="/oauth_client_admin/edit_oauth_client_as_metadata" />
				<portlet:param name="issuer" value="<%= oAuthClientASMetadata.getIssuer() %>" />
				<portlet:param name="redirect" value="<%= currentURL %>" />
			</portlet:renderURL>

			<liferay-ui:search-container-column-text
				href="<%= editURL %>"
				property="issuer"
			/>

			<liferay-ui:search-container-column-text
				property="discoveryEndpoint"
			/>

			<liferay-ui:search-container-column-jsp
				align="right"
				path="/admin/application_actions.jsp"
			/>
		</liferay-ui:search-container-row>

		<liferay-ui:search-iterator
			displayStyle="descriptive"
			markupView="lexicon"
		/>
	</liferay-ui:search-container>
</clay:container-fluid>