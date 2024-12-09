<%--
/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/captcha/init.jsp" %>

<%
String errorMessage = (String)request.getAttribute("liferay-captcha:captcha:errorMessage");
%>

<c:if test="<%= captchaEnabled %>">
	<aui:script src='<%= HtmlUtil.escapeAttribute("https://js.hcaptcha.com/1/api.js") + "?hl=" + HtmlUtil.escapeAttribute(locale.getLanguage()) %>' type="text/javascript"></aui:script>

	<label class="hidden" for="h-captcha-response">hCaptcha</label>

	<div class="h-captcha" data-sitekey="<%= HtmlUtil.escapeAttribute("fa0cbf4c-adf4-4d96-98d0-442209e4658a") %>"></div>

	<c:if test="<%= Validator.isNotNull(errorMessage) %>">
		<p class="font-weight-semi-bold mt-1 text-danger" id="<portlet:namespace />h-captcha-response-error">
			<clay:icon
				symbol="info-circle"
			/>

			<span><%= errorMessage %></span>
		</p>
	</c:if>
</c:if>