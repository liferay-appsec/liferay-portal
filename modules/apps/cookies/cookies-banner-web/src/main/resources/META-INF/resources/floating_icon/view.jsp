<%--
/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/init.jsp" %>

<aui:link hashedFile="<%= true %>" href="cookies-banner-web/floating_icon/css/main.css" rel="stylesheet" type="text/css" />

<clay:container-fluid
	cssClass="container-view"
>
	<clay:row>
		<clay:content-row
			cssClass="autofit-float-sm-down px-2 px-md-0"
			noGutters="true"
			verticalAlign="center"
		>
			<clay:content-col>
				<clay:button
					cssClass="d-none"
					displayType="link"
					id="floatingIconButton"
					label=""
				/>

				<label class="align-items-center cursor-pointer d-none floating-icon-button justify-content-center mb-0 ml-3 rounded-circle text-white" for="floatingIconButton" id="floatingIconLabel">
					<clay:icon
						symbol='<%= (String)request.getAttribute("floatingIcon") %>'
					/>
				</label>
			</clay:content-col>
		</clay:content-row>
	</clay:row>
</clay:container-fluid>

<liferay-frontend:component
	componentId="FloatingIcon"
	module="{FloatingIcon} from cookies-banner-web"
/>