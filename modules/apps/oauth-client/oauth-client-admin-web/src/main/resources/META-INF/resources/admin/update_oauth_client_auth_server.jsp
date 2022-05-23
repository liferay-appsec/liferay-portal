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

long oAuthClientAuthServerId = 0;

String issuer = ParamUtil.getString(request, "issuer", "");

String discoveryEndpoint = "";

String metadataJSON = (String)request.getAttribute("metadataTemplate");

if (issuer.length() > 0) {
	OAuthClientAuthServer oAuthClientAuthServer = OAuthClientAuthServerServiceUtil.getOAuthClientAuthServer(companyId, issuer);

	oAuthClientAuthServerId = oAuthClientAuthServer.getOAuthClientAuthServerId();

	discoveryEndpoint = oAuthClientAuthServer.getDiscoveryEndpoint();

	metadataJSON = oAuthClientAuthServer.getMetadataJSON();
}

portletDisplay.setShowBackIcon(true);
portletDisplay.setURLBack(ParamUtil.getString(request, "redirect"));
%>

<portlet:actionURL name="/oauth_client_admin/update_o_auth_client_auth_server" var="updateOAuthClientAuthServerURL">
	<portlet:param name="backURL" value='<%= ParamUtil.getString(request, "redirect") %>' />
</portlet:actionURL>

<aui:form action="<%= updateOAuthClientAuthServerURL %>" id="oauth-client-auth-server-fm" method="post" name="oauth-client-auth-server-fm" onSubmit="event.preventDefault();">
	<clay:container-fluid
		cssClass="container-view"
	>
		<aui:input label="Use Discovery" name="useDiscovery" onClick='<%= liferayPortletResponse.getNamespace() + "displayDiscovery(this.checked);" %>' type="checkbox" value="<%= !((issuer.length() > 0) && (discoveryEndpoint.length() < 1)) %>" />

		<div class="sheet">
			<clay:row>
				<clay:col
					lg="12"
				>
					<div id="discoveryEndpointDiv">
						<aui:input helpMessage="oauth-client-auth-server-discovery-endpoint-help" label="Discovery Endpoint" name="discoveryEndpoint" type="url" />
					</div>

					<div id="metadataJSONDiv" style="display: none;">
						<aui:input helpMessage="oauth-client-auth-server-metadata-json-help" label="Authorization Server Metadata" name="metadataJSON" type="textarea" />
					</div>

					<aui:input name="oAuthClientAuthServerId" type="hidden" value="<%= oAuthClientAuthServerId %>" />
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
			'<portlet:namespace />oauth-client-auth-server-fm'
		);

		if (
			document.getElementById('<portlet:namespace />useDiscovery').value ===
			'false'
		) {
			document.getElementById(
				'<portlet:namespace />metadataJSON'
			).value = JSON.stringify(
				JSON.parse(
					document.getElementById('<portlet:namespace />metadataJSON')
						.value
				),
				null,
				0
			);
			submitForm(form);
			return;
		}

		fetch(
			document.getElementById('<portlet:namespace />discoveryEndpoint').value
		)
			.then((response) => response.json())
			.then((jsonData) => {
				document.getElementById(
					'<portlet:namespace />metadataJSON'
				).value = JSON.stringify(jsonData, null, 0);
				submitForm(form);
			})
			.catch(console.error);
	}

	function <portlet:namespace />init() {
		document.getElementById(
			'<portlet:namespace />metadataJSON'
		).value = JSON.stringify(JSON.parse('<%= metadataJSON %>'), null, 4);

		if (<%= issuer.length() < 1 %>) {
			return;
		}

		if (<%= discoveryEndpoint.length() > 0 %>) {
			document.getElementById(
				'<portlet:namespace />discoveryEndpoint'
			).value = '<%= discoveryEndpoint %>';

			<portlet:namespace />displayDiscovery(true);
		}
		else {
			<portlet:namespace />displayDiscovery(false);
		}
	}

	function <portlet:namespace />displayDiscovery(checked) {
		if (checked) {
			document.getElementById(
				'<portlet:namespace />useDiscovery'
			).value = true;
			document.getElementById('discoveryEndpointDiv').style = '';
			document.getElementById('metadataJSONDiv').style = 'display: none';
		}
		else {
			document.getElementById(
				'<portlet:namespace />useDiscovery'
			).value = false;
			document.getElementById('discoveryEndpointDiv').style = 'display: none';
			document.getElementById('metadataJSONDiv').style = '';
			document.getElementById('<portlet:namespace />metadataJSON').style =
				'min-height: 600px';
		}
	}
</aui:script>