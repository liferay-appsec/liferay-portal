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
long companyId = themeDisplay.getCompanyId();

long oAuthClientASLocalMetadataId = 0;

String localWellKnownURI = ParamUtil.getString(request, "localWellKnownURI", "");

String metadataJSON = (String)request.getAttribute("metadataTemplate");

if (localWellKnownURI.length() > 0) {
	OAuthClientASLocalMetadata oAuthClientASLocalMetadata = OAuthClientASLocalMetadataServiceUtil.getOAuthClientASLocalMetadata(companyId, localWellKnownURI);

	oAuthClientASLocalMetadataId = oAuthClientASLocalMetadata.getOAuthClientASLocalMetadataId();

	metadataJSON = oAuthClientASLocalMetadata.getMetadataJSON();
}

portletDisplay.setShowBackIcon(true);
portletDisplay.setURLBack(ParamUtil.getString(request, "redirect"));
%>

<portlet:actionURL name="/oauth_client_admin/update_o_auth_client_as_local_metadata" var="updateOAuthClientASLocalMetadataURL">
	<portlet:param name="backURL" value='<%= ParamUtil.getString(request, "redirect") %>' />
</portlet:actionURL>

<aui:form action="<%= updateOAuthClientASLocalMetadataURL %>" id="oauth-client-as-fm" method="post" name="oauth-client-as-fm" onSubmit="event.preventDefault();">
	<clay:container-fluid
		cssClass="container-view"
	>
		<div class="sheet">
			<clay:row>
				<clay:col
					lg="12"
				>
					<aui:input helpMessage="oauth-client-as-local-well-known-uri-help" label="oauth-client-as-local-well-known-uri" name="oAuthClientASLocalWellKnowURI" readonly="true" value="<%= localWellKnownURI %>" />

					<aui:input helpMessage="oauth-client-as-local-well-known-uri-suffix-help" label="oauth-client-as-local-well-known-uri-suffix" name="oAuthClientASLocalWellKnowURI" readonly="true" value="openid-configuration" />

					<aui:input helpMessage="oauth-client-as-local-metadata-json-help" label="oauth-client-as-local-metadata-json" name="metadataJSON" style="min-height: 600px;" type="textarea" />

					<aui:input name="oAuthClientASLocalMetadataId" type="hidden" value="<%= oAuthClientASLocalMetadataId %>" />
				</clay:col>
			</clay:row>

			<clay:row>
				<clay:col
					lg="12"
				>
					<aui:button-row>
						<aui:button onClick='<%= liferayPortletResponse.getNamespace() + "doSubmit();" %>' type="submit" />
						<aui:button href='<%= ParamUtil.getString(request, "redirect") %>' type="cancel" />
					</aui:button-row>
				</clay:col>
			</clay:row>
		</div>
	</clay:container-fluid>
</aui:form>

<aui:script>
	<portlet:namespace />init();

	function <portlet:namespace />doSubmit() {
		var form = document.getElementById(
			'<portlet:namespace />oauth-client-as-fm'
		);

		document.getElementById(
			'<portlet:namespace />metadataJSON'
		).value = JSON.stringify(
			JSON.parse(
				document.getElementById('<portlet:namespace />metadataJSON').value
			),
			null,
			0
		);
		submitForm(form);
	}

	function <portlet:namespace />init() {
		document.getElementById(
			'<portlet:namespace />metadataJSON'
		).value = JSON.stringify(JSON.parse('<%= metadataJSON %>'), null, 4);
	}
</aui:script>