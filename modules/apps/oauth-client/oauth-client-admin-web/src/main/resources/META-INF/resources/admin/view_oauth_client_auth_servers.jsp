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
int oAuthClientAuthServerCount = OAuthClientAuthServerLocalServiceUtil.getOAuthClientAuthServersCount();

OAuthClientAuthServersManagementToolbarDisplayContext oAuthClientAuthServersManagementToolbarDisplayContext = new OAuthClientAuthServersManagementToolbarDisplayContext(liferayPortletRequest, liferayPortletResponse, currentURLObj);
%>

<clay:management-toolbar
	actionDropdownItems="<%= oAuthClientAuthServersManagementToolbarDisplayContext.getActionDropdownItems() %>"
	additionalProps="<%= oAuthClientAuthServersManagementToolbarDisplayContext.getAdditionalProps() %>"
	creationMenu="<%= oAuthClientAuthServersManagementToolbarDisplayContext.getCreationMenu() %>"
	disabled="<%= oAuthClientAuthServerCount == 0 %>"
	filterDropdownItems="<%= oAuthClientAuthServersManagementToolbarDisplayContext.getFilterDropdownItems() %>"
	itemsTotal="<%= oAuthClientAuthServerCount %>"
	searchContainerId="oAuthClientAuthServerSearchContainer"
	selectable="<%= true %>"
	showCreationMenu="<%= true %>"
	showSearch="<%= false %>"
	sortingOrder="<%= oAuthClientAuthServersManagementToolbarDisplayContext.getOrderByType() %>"
	sortingURL="<%= String.valueOf(oAuthClientAuthServersManagementToolbarDisplayContext.getSortingURL()) %>"
	viewTypeItems="<%= oAuthClientAuthServersManagementToolbarDisplayContext.getViewTypes() %>"
/>

<clay:container-fluid
	cssClass="closed"
>
	<liferay-ui:search-container
		emptyResultsMessage="no-oauth-client-auth-servers-were-found"
		id="oAuthClientAuthServerSearchContainer"
		iteratorURL="<%= currentURLObj %>"
		rowChecker="<%= new EmptyOnClickRowChecker(renderResponse) %>"
		total="<%= oAuthClientAuthServerCount %>"
	>
		<liferay-ui:search-container-results
			results="<%= OAuthClientAuthServerServiceUtil.getOAuthClientAuthServers(themeDisplay.getCompanyId(), searchContainer.getStart(), searchContainer.getEnd()) %>"
		/>

		<liferay-ui:search-container-row
			className="com.liferay.oauth.client.persistence.model.OAuthClientAuthServer"
			escapedModel="<%= true %>"
			keyProperty="issuer"
			modelVar="oAuthClientAuthServer"
		>
			<portlet:renderURL var="editURL">
				<portlet:param name="mvcRenderCommandName" value="/oauth_client_admin/update_o_auth_client_auth_server" />
				<portlet:param name="issuer" value="<%= oAuthClientAuthServer.getIssuer() %>" />
				<portlet:param name="redirect" value="<%= currentURL %>" />
			</portlet:renderURL>

			<liferay-ui:search-container-column-text
				href="<%= editURL %>"
				name="oauth-client-auth-server-issuer"
				property="issuer"
			/>

			<liferay-ui:search-container-column-text
				name="user-id"
				property="userId"
			/>

			<liferay-ui:search-container-column-text
				name="company-id"
				property="companyId"
			/>

			<liferay-ui:search-container-column-text
				name="use-discovery-endpoint"
				value="<%= String.valueOf(Validator.isNotNull(oAuthClientAuthServer.getDiscoveryEndpoint())) %>"
			/>

			<liferay-ui:search-container-column-text
				name="type"
				property="type"
			/>

			<liferay-ui:search-container-column-jsp
				align="right"
				path="/admin/oauth_client_auth_server_actions.jsp"
			/>
		</liferay-ui:search-container-row>

		<liferay-ui:search-iterator
			displayStyle="list"
			markupView="lexicon"
		/>
	</liferay-ui:search-container>
</clay:container-fluid>