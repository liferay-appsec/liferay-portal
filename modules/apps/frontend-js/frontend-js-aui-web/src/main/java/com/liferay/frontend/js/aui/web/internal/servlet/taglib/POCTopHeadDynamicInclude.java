package com.liferay.frontend.js.aui.web.internal.servlet.taglib;

import com.liferay.portal.kernel.servlet.taglib.BaseDynamicInclude;
import com.liferay.portal.kernel.servlet.taglib.DynamicInclude;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.osgi.service.component.annotations.Component;

import java.io.IOException;
import java.io.PrintWriter;

@Component(service = DynamicInclude.class, property = {
	"service.ranking:Integer=1000"
})
public class POCTopHeadDynamicInclude extends BaseDynamicInclude {

	@Override
	public void include(
		HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse, String key)
		throws IOException {


		PrintWriter printWriter = httpServletResponse.getWriter();

		printWriter.println("<script id=\"Cookiebot\" src=\"https://consent.cookiebot.com/uc.js\" data-cbid=\"<your-domain-group-id-here>\" data-blockingmode=\"auto\" type=\"text/javascript\"></script>");
	}

	@Override
	public void register(DynamicIncludeRegistry dynamicIncludeRegistry) {
		dynamicIncludeRegistry.register(
			"/html/common/themes/top_head.jsp#pre");
	}
}
