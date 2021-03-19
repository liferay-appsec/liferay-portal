<!DOCTYPE html>

<#include init />

<html class="${root_css_class}" dir="<@liferay.language key="lang.dir" />" lang="${w3c_language_id}">

<head>
	<title>${html_title}</title>

	<meta content="initial-scale=1.0, width=device-width" name="viewport" />

	<@liferay_util["include"] page=top_head_include />
</head>

<body class="${css_class}">

<@liferay_ui["quick-access"] contentId="#main-content" />

<@liferay_util["include"] page=body_top_include />

<#assign scope_group = theme_display.getScopeGroup() />

<div id="wrapper">
	<div id="content-wrapper">
		<div id="content">
			<#if selectable>
				<#if (request.getAttribute("contentServletContext"))??>
					<@liferay_util["include"] page=content_include servletContext=request.getAttribute("contentServletContext")/>
				<#else>
					<@liferay_util["include"] page=content_include />
				</#if>
			<#else>
				${portletDisplay.recycle()}

				${portletDisplay.setTitle(the_title)}

				<@liferay_theme["wrap-portlet"] page="portlet.ftl">
					<#if (request.getAttribute("contentServletContext"))??>
						<@liferay_util["include"] page=content_include servletContext=request.getAttribute("contentServletContext")/>
					<#else>
						<@liferay_util["include"] page=content_include />
					</#if>
				</@>
			</#if>

			<div class="clear"></div>
		</div>
	</div>
</div>

<@liferay_util["include"] page=body_bottom_include />

<@liferay_util["include"] page=bottom_include />

</body>

</html>