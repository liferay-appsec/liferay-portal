/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.login.web.internal.servlet.taglib.include;

import com.liferay.login.web.constants.LoginPortletKeys;
import com.liferay.layout.utility.page.kernel.constants.LayoutUtilityPageEntryConstants;
import com.liferay.layout.utility.page.kernel.provider.LayoutUtilityPageEntryLayoutProvider;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManager;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.JavaConstants;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.taglib.include.PageInclude;
import com.liferay.taglib.ui.IconTag;

import java.util.Objects;

import javax.portlet.PortletConfig;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Shuyang Zhou
 */
@Component(
	property = {
		"login.web.navigation.position=post", "service.ranking:Integer=200"
	},
	service = PageInclude.class
)
public class CreateAccountNavigationPostPageInclude implements PageInclude {

	@Override
	public void include(PageContext pageContext) throws JspException {
		HttpServletRequest httpServletRequest =
			(HttpServletRequest)pageContext.getRequest();

		String mvcRenderCommandName = httpServletRequest.getParameter(
			"mvcRenderCommandName");

		if (_featureFlagManager.isEnabled("LPD-6378")) {
			PortletConfig portletConfig =
				(PortletConfig)httpServletRequest.getAttribute(
					JavaConstants.JAVAX_PORTLET_CONFIG);

			String portletName = portletConfig.getPortletName();

			if (portletName.equals(LoginPortletKeys.CREATE_ACCOUNT) &&
				Validator.isNull(mvcRenderCommandName)) {

				return;
			}
		}

		if (Objects.equals(mvcRenderCommandName, "/login/create_account")) {
			return;
		}

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		Company company = themeDisplay.getCompany();

		if (!company.isStrangers()) {
			return;
		}

		IconTag iconTag = new IconTag();

		iconTag.setCssClass("text-4");
		iconTag.setMessage("create-account");

		try {
			String url = StringPool.BLANK;

			Layout layout =
				_layoutUtilityPageEntryLayoutProvider.
					getDefaultLayoutUtilityPageEntryLayout(
						themeDisplay.getScopeGroupId(),
						LayoutUtilityPageEntryConstants.TYPE_CREATE_ACCOUNT);

			if (layout != null) {
				url = _portal.getLayoutURL(layout, themeDisplay);
			}
			else {
				url = _portal.getCreateAccountURL(
					httpServletRequest, themeDisplay);
			}

			iconTag.setUrl(url);
		}
		catch (Exception exception) {
			throw new JspException(exception);
		}

		iconTag.doTag(pageContext);
	}

	@Reference
	private FeatureFlagManager _featureFlagManager;

	@Reference
	private LayoutUtilityPageEntryLayoutProvider
		_layoutUtilityPageEntryLayoutProvider;

	@Reference
	private Portal _portal;

}