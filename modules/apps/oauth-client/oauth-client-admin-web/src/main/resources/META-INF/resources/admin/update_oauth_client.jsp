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

long oAuthClientEntryId = 0;

String authServerWellKnownURI = ParamUtil.getString(request, "authServerWellKnownURI", "");

String clientId = ParamUtil.getString(request, "clientId", "");

String infoJSON = (String)request.getAttribute("infoTemplate");

String parametersJSON = (String)request.getAttribute("parametersTemplate");

if ((authServerWellKnownURI.length() > 0) && (clientId.length() > 0)) {
	OAuthClientEntry oAuthClientEntry = OAuthClientEntryServiceUtil.getOAuthClientEntry(companyId, authServerWellKnownURI, clientId);

	oAuthClientEntryId = oAuthClientEntry.getOAuthClientEntryId();

	infoJSON = oAuthClientEntry.getInfoJSON();

	parametersJSON = oAuthClientEntry.getParametersJSON();
}

portletDisplay.setShowBackIcon(true);
portletDisplay.setURLBack(ParamUtil.getString(request, "redirect"));
%>

<portlet:actionURL name="/oauth_client_admin/update_o_auth_client" var="updateOAuthClientEntryURL">
	<portlet:param name="backURL" value='<%= ParamUtil.getString(request, "redirect") %>' />
</portlet:actionURL>

<aui:form action="<%= updateOAuthClientEntryURL %>" id="oauth-client-fm" method="post" name="oauth-client-fm" onSubmit="event.preventDefault();">
	<clay:container-fluid
		cssClass="container-view"
	>
		<div class="sheet">
			<clay:row>
				<clay:col
					lg="12"
				>
					<aui:input helpMessage="oauth-client-as-well-known-uri-help" label="oauth-client-as-well-known-uri" name="authServerWellKnownURI" />

					<aui:input helpMessage="oauth-client-info-json-help" label="oauth-client-info-json" name="infoJSON" style="min-height: 600px;" type="textarea" />

					<aui:input helpMessage="oauth-client-parameters-json-help" label="oauth-client-parameters-json" name="parametersJSON" style="min-height: 200px;" type="textarea" />

					<aui:input name="oAuthClientEntryId" type="hidden" value="<%= oAuthClientEntryId %>" />
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
		document.getElementById(
			'<portlet:namespace />infoJSON'
		).value = JSON.stringify(
			JSON.parse(
				document.getElementById('<portlet:namespace />infoJSON').value
			),
			null,
			0
		);

		document.getElementById(
			'<portlet:namespace />parametersJSON'
		).value = JSON.stringify(
			JSON.parse(
				document.getElementById('<portlet:namespace />parametersJSON').value
			),
			null,
			0
		);

		submitForm(document.getElementById('<portlet:namespace />oauth-client-fm'));
	}

	function <portlet:namespace />init() {
		document.getElementById(
			'<portlet:namespace />infoJSON'
		).value = JSON.stringify(JSON.parse('<%= infoJSON %>'), null, 4);

		document.getElementById(
			'<portlet:namespace />parametersJSON'
		).value = JSON.stringify(JSON.parse('<%= parametersJSON %>'), null, 4);
	}
</aui:script>