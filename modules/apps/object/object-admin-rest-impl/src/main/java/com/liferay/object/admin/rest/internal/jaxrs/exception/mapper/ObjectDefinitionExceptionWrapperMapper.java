package com.liferay.object.admin.rest.internal.jaxrs.exception.mapper;

import com.liferay.object.exception.WrapperException;
import com.liferay.portal.vulcan.jaxrs.exception.mapper.BaseExceptionMapper;
import com.liferay.portal.vulcan.jaxrs.exception.mapper.Problem;

import jakarta.ws.rs.ext.ExceptionMapper;

import org.osgi.service.component.annotations.Component;

/**
 * @author Caio Farias
 */
@Component(
	property = {
		"osgi.jaxrs.application.select=(osgi.jaxrs.name=Liferay.Object.Admin.REST)",
		"osgi.jaxrs.extension=true",
		"osgi.jaxrs.name=Liferay.Object.Admin.REST.ObjectDefinitionExceptionWrapperMapper"
	},
	service = ExceptionMapper.class
)
public class ObjectDefinitionExceptionWrapperMapper
	extends BaseExceptionMapper<WrapperException> {

	@Override
	protected Problem getProblem(WrapperException wrapperException) {
		Problem problem = new Problem(wrapperException);

		problem.setDetail(wrapperException.getDetails());

		return problem;
	}

}