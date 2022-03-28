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
ResultRow row = (ResultRow)request.getAttribute(WebKeys.SEARCH_CONTAINER_RESULT_ROW);

OAuthClientASMetadata oAuthClientASMetadata = (OAuthClientASMetadata)row.getObject();

String oAuthClientASMetadataId = String.valueOf(oAuthClientASMetadata.getOAuthClientASMetadataId());

int oAuthClientASMetadataCount = OAuthClientASMetadataLocalServiceUtil.getOAuthClientASMetadatasCount();
%>

<liferay-ui:icon-menu
	direction="left-side"
	icon="<%= StringPool.BLANK %>"
	markupView="lexicon"
	message="<%= StringPool.BLANK %>"
	showWhenSingleIcon="<%= true %>"
>
	<c:if test="<%= OAuthClientASMetadataDisplayContext.hasUpdatePermission(oAuthClientASMetadata) %>">
		<portlet:renderURL var="editURL">
			<portlet:param name="mvcRenderCommandName" value="/oauth_client_admin/edit_oauth_client_as_metadata" />
			<portlet:param name="issuer" value="<%= oAuthClientASMetadata.getIssuer() %>" />
			<portlet:param name="redirect" value="<%= currentURL %>" />
		</portlet:renderURL>

		<liferay-ui:icon
			message="edit"
			url="<%= editURL.toString() %>"
		/>
	</c:if>

	<c:if test="<%= OAuthClientASMetadataDisplayContext.hasPermissionsPermission(oAuthClientASMetadata) %>">
		<liferay-security:permissionsURL
			modelResource="<%= OAuthClientASMetadata.class.getName() %>"
			modelResourceDescription="<%= oAuthClientASMetadata.getIssuer() %>"
			resourcePrimKey="<%= oAuthClientASMetadataId %>"
			var="permissionsURL"
			windowState="<%= LiferayWindowState.POP_UP.toString() %>"
		/>

		<liferay-ui:icon
			message="permissions"
			method="get"
			url="<%= permissionsURL %>"
			useDialog="<%= true %>"
		/>
	</c:if>

	<c:if test="<%= OAuthClientASMetadataDisplayContext.hasDeletePermission(oAuthClientASMetadata) %>">
		<portlet:actionURL name="/oauth_client_admin/delete_oauth_client_as_metadata" var="deleteURL">
			<portlet:param name="issuer" value="<%= oAuthClientASMetadata.getIssuer() %>" />
		</portlet:actionURL>

		<liferay-ui:icon-delete
			message="delete"
			url="<%= deleteURL.toString() %>"
		/>
	</c:if>
</liferay-ui:icon-menu>