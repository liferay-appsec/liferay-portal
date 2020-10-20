package com.liferay.user.scim.internal.jaxrs.application;

import com.liferay.user.scim.internal.constants.SCIMProviderConstants;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.osgi.service.component.annotations.Component;

import org.wso2.charon3.core.exceptions.CharonException;
import org.wso2.charon3.core.exceptions.FormatNotSupportedException;
import org.wso2.charon3.core.extensions.UserManager;
import org.wso2.charon3.core.protocol.SCIMResponse;
import org.wso2.charon3.core.protocol.endpoints.UserResourceManager;
import org.wso2.charon3.utils.DefaultCharonManager;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Component(
	immediate = true, property = {
		"osgi.jaxrs.resource=true",
		"osgi.jaxrs.application.select=(osgi.jaxrs.name=Liferay.SCIM.Application)"
	},
	service = Object.class
)
@OpenAPIDefinition(
	info = @Info(
		title = "/Users Endpoint Swagger Definition", version = "1.0",
		description = "SCIM 2.0 /Users endpoint",
		license = @License(name = "Apache 2.0", url = "http://www.apache.org/licenses/LICENSE-2.0"))
)
@Path("/v2/Users")
public class UserResource extends AbstractResource {

	@GET
	@Path("/{id}")
	@Produces({"application/json", "application/scim+json"})

	@Operation(description = "Return the user with the given id")
	@Parameters(value = {
		@Parameter(in = ParameterIn.PATH, description = SCIMProviderConstants.ID_DESC, name="id", required = true),
		@Parameter(in = ParameterIn.QUERY, description = SCIMProviderConstants.ATTRIBUTES_DESC, name="attribute", required = false),
		@Parameter(in = ParameterIn.QUERY, description = SCIMProviderConstants.EXCLUDED_ATTRIBUTES_DESC, name="excludedAttributes", required = false)})
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Valid user is found"),
		@ApiResponse(responseCode = "404", description = "Valid user is not found")})

	public Response getUser(
		@Parameter(hidden = true) @PathParam(SCIMProviderConstants.ID) String id,
		@Parameter(hidden = true) @QueryParam(SCIMProviderConstants.ATTRIBUTES) String attribute,
		@Parameter(hidden = true) @QueryParam(SCIMProviderConstants.EXCLUDE_ATTRIBUTES) String excludedAttributes)
		throws FormatNotSupportedException, CharonException {

		try {
			// obtain the user store manager
			UserManager userManager =
				DefaultCharonManager.getInstance().getUserManager();

			// create charon-SCIM user endpoint and hand-over the request.
			UserResourceManager userResourceManager = new UserResourceManager();

			SCIMResponse scimResponse =
				userResourceManager.get(id, userManager, attribute,
					excludedAttributes);
			// needs to check the code of the response and return 200 0k or other error codes
			// appropriately.
			return buildResponse(scimResponse);

		}
		catch (CharonException e) {
			throw new CharonException(e.getDetail(), e);
		}
	}

	@Operation(description = "Return the user which was created")
	@POST
	@Produces({"application/json", "application/scim+json"})
	@Consumes("application/scim+json")
	@Parameters(value = {
		@Parameter(in = ParameterIn.QUERY, description = SCIMProviderConstants.ATTRIBUTES_DESC, name="resourceString", required = false),
		@Parameter(in = ParameterIn.QUERY, description = SCIMProviderConstants.EXCLUDED_ATTRIBUTES_DESC, name="excludedAttributes", required = false)})

	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Valid user is created"),
		@ApiResponse(responseCode = "404", description = "User is not found")})

	public Response createUser(
		@QueryParam(SCIMProviderConstants.ATTRIBUTES) String attribute,
		@QueryParam(SCIMProviderConstants.EXCLUDE_ATTRIBUTES) String excludedAttributes,
		String resourceString)
		throws CharonException, FormatNotSupportedException {

		try {
			// obtain the user store manager
			UserManager userManager =
				DefaultCharonManager.getInstance().getUserManager();

			// create charon-SCIM user endpoint and hand-over the request.
			UserResourceManager userResourceManager = new UserResourceManager();

			SCIMResponse response =
				userResourceManager.create(resourceString, userManager,
					attribute, excludedAttributes);

			return buildResponse(response);

		}
		catch (CharonException e) {
			throw new CharonException(e.getDetail(), e);
		}

	}

	@DELETE
	@Path("/{id}")
	@Produces({"application/json", "application/scim+json"})
	@Operation(description = "Delete the user with the given id")
	@Parameters(
		@Parameter(in = ParameterIn.PATH, description = SCIMProviderConstants.ID_DESC, name="id", required = true)
	)

	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "User is deleted"),
		@ApiResponse(responseCode = "404", description = "Valid user is not found")})

	public Response deleteUser(
		@PathParam(SCIMProviderConstants.ID) String id)
		throws FormatNotSupportedException, CharonException {

		try {
			// obtain the user store manager
			UserManager userManager =
				DefaultCharonManager.getInstance().getUserManager();

			// create charon-SCIM user resource manager and hand-over the request.
			UserResourceManager userResourceManager = new UserResourceManager();

			SCIMResponse scimResponse =
				userResourceManager.delete(id, userManager);
			// needs to check the code of the response and return 200 0k or other error codes
			// appropriately.
			return buildResponse(scimResponse);

		}
		catch (CharonException e) {
			throw new CharonException(e.getDetail(), e);
		}
	}

	@GET
	@Produces({"application/json", "application/scim+json"})
	@Operation(description = "Return users according to the filter, sort and pagination parameters")
	@Parameters(value = {
		@Parameter(in = ParameterIn.QUERY, description = SCIMProviderConstants.ATTRIBUTES_DESC, name = "attribute", required = false),
		@Parameter(in = ParameterIn.QUERY, description = SCIMProviderConstants.EXCLUDED_ATTRIBUTES_DESC, name = "excludedAttributes", required = false),
		@Parameter(in = ParameterIn.QUERY, description = SCIMProviderConstants.FILTER_DESC, name = "filter", required = false),
		@Parameter(in = ParameterIn.QUERY, description = SCIMProviderConstants.START_INDEX_DESC, name = "startIndex", required = false),
		@Parameter(in = ParameterIn.QUERY, description = SCIMProviderConstants.COUNT_DESC, name = "count", required = false),
		@Parameter(in = ParameterIn.QUERY, description = SCIMProviderConstants.SORT_BY_DESC, name = "sortBy", required = false),
		@Parameter(in = ParameterIn.QUERY, description = SCIMProviderConstants.SORT_ORDER_DESC, name = "sortOrder", required = false),
		@Parameter(in = ParameterIn.QUERY, description = SCIMProviderConstants.DOMAIN_DESC, name = "domainName", required = false)})

	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Valid users are found"),
		@ApiResponse(responseCode = "404", description = "Valid users are not found")})

	public Response getUser(
		@QueryParam(SCIMProviderConstants.ATTRIBUTES) String attribute,
		@QueryParam(SCIMProviderConstants.EXCLUDE_ATTRIBUTES) String excludedAttributes,
		@QueryParam(SCIMProviderConstants.FILTER) String filter,
		@QueryParam(SCIMProviderConstants.START_INDEX) int startIndex,
		@QueryParam(SCIMProviderConstants.COUNT) int count,
		@QueryParam(SCIMProviderConstants.SORT_BY) String sortBy,
		@QueryParam(SCIMProviderConstants.SORT_ORDER) String sortOrder,
		@QueryParam(SCIMProviderConstants.DOMAIN) String domainName)
		throws FormatNotSupportedException, CharonException {

		try {
			// obtain the user store manager
			UserManager userManager =
				DefaultCharonManager.getInstance().getUserManager();

			// create charon-SCIM user resource manager and hand-over the request.
			UserResourceManager userResourceManager = new UserResourceManager();

			SCIMResponse scimResponse =
				userResourceManager.listWithGET(userManager, filter, startIndex,
					count,
					sortBy, sortOrder, domainName, attribute,
					excludedAttributes);

			return buildResponse(scimResponse);

		}
		catch (CharonException e) {
			throw new CharonException(e.getDetail(), e);
		}
	}

	@POST
	@Path("/.search")
	@Produces({"application/json", "application/scim+json"})
	@Consumes("application/scim+json")
	@Operation(description = "Return users according to the filter, sort and pagination parameters")

	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Valid users are found"),
		@ApiResponse(responseCode = "404", description = "Valid users are not found")})

	public Response getUsersByPost(String resourceString)
		throws FormatNotSupportedException, CharonException {

		try {
			// obtain the user store manager
			UserManager userManager =
				DefaultCharonManager.getInstance().getUserManager();

			// create charon-SCIM user resource manager and hand-over the request.
			UserResourceManager userResourceManager = new UserResourceManager();

			SCIMResponse scimResponse =
				userResourceManager.listWithPOST(resourceString, userManager);

			return buildResponse(scimResponse);

		}
		catch (CharonException e) {
			throw new CharonException(e.getDetail(), e);
		}
	}

	@PUT
	@Path("{id}")
	@Produces({"application/json", "application/scim+json"})
	@Consumes("application/scim+json")
	@Operation(description = "Return the updated user")
	@Parameters(value = {
		@Parameter(in = ParameterIn.PATH, description = SCIMProviderConstants.ID_DESC, name = "id", required = true),
		@Parameter(in = ParameterIn.QUERY, description = SCIMProviderConstants.ATTRIBUTES_DESC, name = "attribute", required = false),
		@Parameter(in = ParameterIn.QUERY, description = SCIMProviderConstants.EXCLUDED_ATTRIBUTES_DESC, name = "excludedAttributes", required = false)})

	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "User is updated"),
		@ApiResponse(responseCode = "404", description = "Valid user is not found")})

	public Response updateUser(
		@PathParam(SCIMProviderConstants.ID) String id,
		@QueryParam(SCIMProviderConstants.ATTRIBUTES) String attribute,
		@QueryParam(SCIMProviderConstants.EXCLUDE_ATTRIBUTES) String excludedAttributes,
		String resourceString)
		throws FormatNotSupportedException, CharonException {

		try {
			// obtain the user store manager
			UserManager userManager =
				DefaultCharonManager.getInstance().getUserManager();

			// create charon-SCIM user endpoint and hand-over the request.
			UserResourceManager userResourceManager = new UserResourceManager();

			SCIMResponse response = userResourceManager.updateWithPUT(
				id, resourceString, userManager, attribute, excludedAttributes);

			return buildResponse(response);

		}
		catch (CharonException e) {
			throw new CharonException(e.getDetail(), e);
		}
	}
}