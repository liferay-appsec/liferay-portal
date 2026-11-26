/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.vulcan.internal.jaxrs.message.body;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.liferay.portal.kernel.servlet.MaxRequestLengthUtil;
import com.liferay.portal.kernel.util.WebKeys;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.io.InputStream;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import java.util.Objects;

import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;

/**
 * @author Javier Gamarra
 * @author Ivica Cardic
 */
@Consumes(MediaType.APPLICATION_JSON)
@Provider
public class JSONMessageBodyReader extends BaseMessageBodyReader {

	public JSONMessageBodyReader() {
		super(ObjectMapper.class, MediaType.APPLICATION_JSON_TYPE);
	}

	@Override
	public Object readFrom(
			Class clazz, Type type, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap multivaluedMap,
			InputStream inputStream)
		throws IOException {

		if ((clazz != Object.class) || !_isBatch()) {
			return super.readFrom(
				clazz, type, annotations, mediaType, multivaluedMap,
				inputStream);
		}

		_httpServletRequest.setAttribute(
			WebKeys.MAX_REQUEST_LENGTH_BATCH, Boolean.TRUE);

		byte[] bytes = MaxRequestLengthUtil.read(
			_httpServletRequest, inputStream);

		try (JsonParser jsonParser = _jsonFactory.createParser(bytes)) {
			if (jsonParser.nextToken() == null) {
				throw new JsonParseException(
					jsonParser, "Request body is empty");
			}

			jsonParser.skipChildren();
		}

		return bytes;
	}

	private boolean _isBatch() {
		Message message = PhaseInterceptorChain.getCurrentMessage();

		if (message == null) {
			return false;
		}

		Exchange exchange = message.getExchange();

		OperationResourceInfo operationResourceInfo = exchange.get(
			OperationResourceInfo.class);

		if (operationResourceInfo == null) {
			return false;
		}

		Method annotatedMethod = operationResourceInfo.getAnnotatedMethod();

		for (Annotation[] parameterAnnotations :
				annotatedMethod.getParameterAnnotations()) {

			for (Annotation annotation : parameterAnnotations) {
				if (!(annotation instanceof QueryParam)) {
					continue;
				}

				QueryParam queryParam = (QueryParam)annotation;

				if (Objects.equals(queryParam.value(), "callbackURL")) {
					return true;
				}
			}
		}

		return false;
	}

	private static final JsonFactory _jsonFactory = new JsonFactory();

	@Context
	private HttpServletRequest _httpServletRequest;

}