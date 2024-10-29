<%--
/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/init.jsp" %>

<liferay-portlet:resourceURL id="/audit/export_audit_events" useNamespace="<%= false %>" var="exportURL">
	<portlet:param name="endDateAmPm" value="<%= String.valueOf(auditDisplayContext.getEndDateAmPm()) %>" />
	<portlet:param name="endDateDay" value="<%= String.valueOf(auditDisplayContext.getEndDateDay()) %>" />
	<portlet:param name="endDateHour" value="<%= String.valueOf(auditDisplayContext.getEndDateHour()) %>" />
	<portlet:param name="endDateMinute" value="<%= String.valueOf(auditDisplayContext.getEndDateMinute()) %>" />
	<portlet:param name="endDateMonth" value="<%= String.valueOf(auditDisplayContext.getEndDateMonth()) %>" />
	<portlet:param name="endDateYear" value="<%= String.valueOf(auditDisplayContext.getEndDateYear()) %>" />
	<portlet:param name="startDateAmPm" value="<%= String.valueOf(auditDisplayContext.getStartDateAmPm()) %>" />
	<portlet:param name="startDateDay" value="<%= String.valueOf(auditDisplayContext.getStartDateDay()) %>" />
	<portlet:param name="startDateHour" value="<%= String.valueOf(auditDisplayContext.getStartDateHour()) %>" />
	<portlet:param name="startDateMinute" value="<%= String.valueOf(auditDisplayContext.getStartDateMinute()) %>" />
	<portlet:param name="startDateMonth" value="<%= String.valueOf(auditDisplayContext.getStartDateMonth()) %>" />
	<portlet:param name="startDateYear" value="<%= String.valueOf(auditDisplayContext.getStartDateYear()) %>" />
</liferay-portlet:resourceURL>

<aui:script>
	Liferay.Util.setPortletConfigurationIconAction(
		'<portlet:namespace />exportAuditEvents',
		() => {
			Liferay.Util.openConfirmModal({
				message:
					'<liferay-ui:message key="warning-this-csv-file-contains-user-supplied-inputs" unicode="<%= true %>" />',
				onConfirm: (isConfirmed) => {
					if (isConfirmed) {
						submitForm(
							document.hrefFm,
							'<%= exportURL + "&compress=0&etag=0&strip=0" %>'
						);
					}
				},
			});
		}
	);
</aui:script>