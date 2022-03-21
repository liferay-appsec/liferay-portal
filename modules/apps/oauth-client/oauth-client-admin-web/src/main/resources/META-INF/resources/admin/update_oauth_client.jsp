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

String authServerIssuer = ParamUtil.getString(request, "authServerIssuer", "");

String clientId = ParamUtil.getString(request, "clientId", "");

String infoJSON = (String)request.getAttribute("infoTemplate");

String parametersJSON = (String)request.getAttribute("parametersTemplate");

List<OAuthClientAuthServer> oAuthClientAuthServers = (List<OAuthClientAuthServer>)request.getAttribute("oAuthClientAuthServers");

if ((authServerIssuer.length() > 0) && (clientId.length() > 0)) {
	OAuthClientEntry oAuthClientEntry = OAuthClientEntryServiceUtil.getOAuthClientEntry(companyId, authServerIssuer, clientId);

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
					<aui:select helpMessage="oauth-client-auth-server-select-help" label="Authorization Server" name="authServerIssuer" value="<%= authServerIssuer %>">

						<%
						for (OAuthClientAuthServer oAuthClientAuthServer : oAuthClientAuthServers) {
						%>

						<aui:option label="<%= oAuthClientAuthServer.getIssuer() %>" selected="<%= authServerIssuer.equals(oAuthClientAuthServer.getIssuer()) %>" value="<%= oAuthClientAuthServer.getIssuer() %>" />

						<%} %>
					</aui:select>

					<aui:input helpMessage="oauth-client-info-json-help" label="OAuth Client Information" name="infoJSON" style="min-height: 600px;" type="textarea" />

					<aui:input helpMessage="oauth-client-request-params-json-help" label="OAuth Client Request Parameters" name="parametersJSON" style="min-height: 200px;" type="textarea" />

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
		document.getElementById('<portlet:namespace />infoJSON').value = JSON.stringify(JSON.parse(document.getElementById('<portlet:namespace />infoJSON').value), null, 0);

		document.getElementById('<portlet:namespace />parametersJSON').value = JSON.stringify(JSON.parse(document.getElementById('<portlet:namespace />parametersJSON').value), null, 0);

		submitForm(document.getElementById('<portlet:namespace />oauth-client-fm'));
	}

	function <portlet:namespace />init() {
		document.getElementById('<portlet:namespace />infoJSON').value = JSON.stringify(JSON.parse('<%= infoJSON %>'), null, 4);

		document.getElementById('<portlet:namespace />parametersJSON').value = JSON.stringify(JSON.parse('<%= parametersJSON %>'), null, 4);
	}

</aui:script>