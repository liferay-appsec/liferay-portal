<%--
/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/dynamic_include/init.jsp" %>

<liferay-theme:defineObjects />

<%
String homeURL = themeDisplay.getURLHome();
%>

<div class="alert alert-info align-items-center d-flex justify-content-between mb-3">
	<span>
		<liferay-ui:message key="pressing-the-preview-button-shows-the-cookie-banner-with-the-current-configurations-only-to-you" />
	</span>

	<button class="btn btn-info" id="<portlet:namespace />cookiesPreviewButton" type="button">
		<liferay-ui:message key="preview" />
	</button>
</div>

<aui:script>
	var previewButton = document.getElementById(
		'<portlet:namespace />cookiesPreviewButton'
	);

	if (previewButton) {
		previewButton.addEventListener('click', function (event) {
			event.preventDefault();

			Liferay.Util.openModal({
				id: 'cookiesBannerPreviewModal',
				size: 'full-screen',
				title: Liferay.Language.get('preview'),
				url: '<%= homeURL + (homeURL.contains("?") ? "&" : "?") + CookiesBannerWebKeys.COOKIES_PREVIEW + "=1" %>',
			});
		});
	}
</aui:script>