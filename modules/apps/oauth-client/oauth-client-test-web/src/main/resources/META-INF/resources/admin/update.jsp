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
String redirect = ParamUtil.getString(request, "redirect");

portletDisplay.setShowBackIcon(true);
portletDisplay.setURLBack(redirect);
%>

<portlet:actionURL name="/oauth_client_test/update" var="updateURL">
	<portlet:param name="mvcRenderCommandName" value="/oauth_client_test/update" />
	<portlet:param name="redirect" value="<%= HtmlUtil.escape(redirect) %>" />
</portlet:actionURL>

<aui:form action="<%= updateURL %>" id="oauth-client-test-fm" method="post" name="oauth-client-test-fm">
	<aui:input name="redirect" type="hidden" value="<%= redirect %>" />

	<clay:container-fluid
		cssClass="container-view"
	>
		<div class="sheet">
			<aui:fieldset>
				<aui:input name="clientExternalReferenceCode" type="text" />

				<aui:button-row>
					<aui:button onClick='<%= liferayPortletResponse.getNamespace() + "doSubmit();" %>' type="submit" />
					<aui:button href="<%= HtmlUtil.escape(redirect) %>" type="cancel" />
				</aui:button-row>
			</aui:fieldset>
		</div>
	</clay:container-fluid>
</aui:form>