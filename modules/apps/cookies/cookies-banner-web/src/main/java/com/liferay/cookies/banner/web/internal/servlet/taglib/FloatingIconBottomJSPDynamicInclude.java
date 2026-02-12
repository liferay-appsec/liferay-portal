package com.liferay.cookies.banner.web.internal.servlet.taglib;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.taglib.BaseJSPDynamicInclude;
import com.liferay.portal.kernel.servlet.taglib.DynamicInclude;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.IOException;

@Component(service = DynamicInclude.class)
public class FloatingIconBottomJSPDynamicInclude extends BaseJSPDynamicInclude {
	@Override
	protected String getJspPath() {
		return "/floating_icon/view.jsp";
	}

	@Override
	protected Log getLog() {
		return _log;
	}

	@Override
	protected ServletContext getServletContext() {
		return _servletContext;
	}

	@Override
	public void include(

		HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse, String key	)
		throws IOException {

		super.include(httpServletRequest, httpServletResponse, key);
	}

	@Override
	public void register(DynamicIncludeRegistry dynamicIncludeRegistry) {
		dynamicIncludeRegistry.register("/html/common/themes/bottom.jsp#post");
	}

	@Reference(
		target = "(osgi.web.symbolicname=com.liferay.cookies.banner.web)"
	)
	private ServletContext _servletContext;


	private static final Log _log = LogFactoryUtil.getLog(
		FloatingIconBottomJSPDynamicInclude.class);
}
