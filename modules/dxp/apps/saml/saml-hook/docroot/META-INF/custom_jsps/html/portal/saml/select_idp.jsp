<%--
/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */
--%>

<%@ include file="/html/portal/init.jsp" %>

<%
String redirect = ParamUtil.getString(request, "redirect");

JSONObject samlSsoLoginContext = (JSONObject)request.getAttribute("SAML_SSO_LOGIN_CONTEXT");

String selectedName = samlSsoLoginContext.getString("selectedName");
String error = samlSsoLoginContext.getString("error");

JSONArray relevantIdpConnectionsJSONArray = samlSsoLoginContext.getJSONArray("relevantIdpConnections");

String selectedEntityId = StringPool.BLANK;
%>

<liferay-util:buffer
	var="selectIdpContent"
>
	<aui:form action='<%= PortalUtil.getPortalURL(request) + "/c/portal/login" %>' method="get" name="fm">
		<aui:input name="saveLastPath" type="hidden" value="<%= false %>" />
		<aui:input name="redirect" type="hidden" value="<%= redirect %>" />
	
		<c:choose>
			<c:when test="<%= relevantIdpConnectionsJSONArray.length() > 0 %>">
				<aui:select label="identity-provider" name="idpEntityId">
	
					<%
					for (int i = 0; i < relevantIdpConnectionsJSONArray.length(); i++) {
						JSONObject relevantIdpConnectionJSONObject = relevantIdpConnectionsJSONArray.getJSONObject(i);
	
						String entityId = relevantIdpConnectionJSONObject.getString("entityId");
						String name = relevantIdpConnectionJSONObject.getString("name");
	
						if (name.equals(selectedName)) {
							selectedEntityId = entityId;
	
							continue;
						}
					%>
	
						<aui:option label="<%= HtmlUtil.escape(name) %>" value="<%= HtmlUtil.escapeAttribute(entityId) %>" />
	
					<%
					}
					%>
	
				</aui:select>
	
				<aui:fieldset>
					<aui:button-row>
						<aui:button type="submit" value="sign-in" />
					</aui:button-row>
				</aui:fieldset>
			</c:when>
			<c:otherwise>
				<liferay-ui:message key="no-identity-provider-is-available-to-sign-you-in" />
			</c:otherwise>
		</c:choose>
	</aui:form>
</liferay-util:buffer>

<c:choose>
	<c:when test="<%= Validator.isNotNull(selectedEntityId) %>">
		<aui:form action='<%= PortalUtil.getPortalURL(request) + "/c/portal/login" %>' method="post" name="fm">
			<aui:input name="saveLastPath" type="hidden" value="<%= false %>" />
			<aui:input name="redirect" type="hidden" value="<%= redirect %>" />
			<aui:input name="forceAuthn" type="hidden" value="true" />
			<aui:input name="idpEntityId" type="hidden" value="<%= selectedEntityId %>" />
			<aui:input name="p_auth" type="hidden" value="<%= AuthTokenUtil.getToken(request) %>" />

			<%
			String key = StringPool.BLANK;
			boolean portletMsgError = true;

			if ("AuthnAgeException".equals(error)) {
				key = "you-authenticated-with-this-identity-provide-too-long-ago";
				portletMsgError = false;
			}
			else if ("ForceAuthnException".equals(error)) {
				key = "the-identity-provider-is-not-behaving-as-expected";
			}
			%>

			<div class="<%= portletMsgError ? "portlet-msg-error" : "portlet-msg-info" %>">
				<h2><%= selectedName %></h2>

				<liferay-ui:message key="<%= key %>" />

				<aui:fieldset>
					<aui:button-row>
						<aui:button type="submit" value="re-authenticate" />
					</aui:button-row>
				</aui:fieldset>
			</div>
		</aui:form>

		<c:if test="<%= relevantIdpConnectionsJSONArray.length() > 1 %>">
			<p><liferay-ui:message key="alternatively-you-can-select-a-different-identity-provider" /></p>
			<%= selectIdpContent %>
		</c:if>
	</c:when>
	<c:otherwise>
		<p><liferay-ui:message key="please-select-your-identity-provider" /></p>
		<%= selectIdpContent %>
	</c:otherwise>
</c:choose>