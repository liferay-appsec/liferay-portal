package com.liferay.user.scim.internal.jaxrs.application;

import org.wso2.charon3.core.protocol.SCIMResponse;

import javax.ws.rs.core.Response;
import java.util.Map;

public class AbstractResource {
	/*
	 * build the jaxrs response
	 * @param scimResponse
	 * @return
	 */
	public Response buildResponse(SCIMResponse scimResponse) {
		//create a response builder with the status code of the response to be returned.
		Response.ResponseBuilder responseBuilder = Response.status(scimResponse.getResponseStatus());
		//set the headers on the response
		Map<String, String> httpHeaders = scimResponse.getHeaderParamMap();
		if (httpHeaders != null && !httpHeaders.isEmpty()) {
			for (Map.Entry<String, String> entry : httpHeaders.entrySet()) {

				responseBuilder.header(entry.getKey(), entry.getValue());
			}
		}
		//set the payload of the response, if available.
		if (scimResponse.getResponseMessage() != null) {
			responseBuilder.entity(scimResponse.getResponseMessage());
		}
		return responseBuilder.build();
	}

}