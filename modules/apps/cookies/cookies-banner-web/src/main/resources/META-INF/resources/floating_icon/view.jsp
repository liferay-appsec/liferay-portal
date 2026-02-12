<%--
/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/init.jsp" %>

<clay:container-fluid
	cssClass="container-view"
>

	<clay:row>

		<clay:content-col>
			<clay:button
                displayType="link"
                cssClass="js-floating-icon-btn"
                label="TEST"
                small="<%= true %>"
            />
		</clay:content-col>
	</clay:row>


</clay:container-fluid>

<aui:script>
    if (!window.floatingIconInitialized) {

        document.addEventListener('click', (event) => {

            const button = event.target.closest('.js-floating-icon-btn');

            if (button) {
                event.preventDefault();
                console.log("Floating icon button clicked via delegation!");

            }
        });

        window.floatingIconInitialized = true;
    }
</aui:script>