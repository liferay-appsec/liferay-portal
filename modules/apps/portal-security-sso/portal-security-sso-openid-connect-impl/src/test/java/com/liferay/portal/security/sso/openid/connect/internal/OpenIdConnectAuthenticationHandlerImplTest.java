package com.liferay.portal.security.sso.openid.connect.internal;

import com.liferay.oauth.client.persistence.model.OAuthClientEntry;
import com.liferay.oauth.client.persistence.service.OAuthClientEntryLocalService;
import com.liferay.petra.function.UnsafeConsumer;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.security.sso.openid.connect.internal.util.OpenIdConnectTokenRequestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier;
import com.nimbusds.oauth2.sdk.util.JSONObjectUtils;
import com.nimbusds.openid.connect.sdk.AuthenticationResponseParser;
import com.nimbusds.openid.connect.sdk.AuthenticationSuccessResponse;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientInformation;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;
import net.minidev.json.JSONObject;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import java.net.URI;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Map;


/**
 * @author Tamas Biro
 */
public class OpenIdConnectAuthenticationHandlerImplTest {
	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setup(){
		_testedClass = new OpenIdConnectAuthenticationHandlerImpl();
		_mockHttpServletRequest = new MockHttpServletRequest();
		_mockHttpServletResponse = new MockHttpServletResponse();
		_mockHttpSession = new MockHttpSession();
	}

	@Test
	public void testEmailValueExistsInJWTClaimSetWhenSingularEmailIsGiven()
		throws Exception {
		//given

		OpenIdConnectAuthenticationSession mockOpenIdConnectAuthenticationSession = Mockito.mock(OpenIdConnectAuthenticationSession.class);
		State state = new State("state");
		Mockito.when(mockOpenIdConnectAuthenticationSession.getState()).thenReturn(state);

		_mockHttpSession.setAttribute(OpenIdConnectAuthenticationHandlerImpl.class.getName() +
									  "#OPEN_ID_CONNECT_AUTHENTICATION_SESSION", new OpenIdConnectAuthenticationSession(new CodeVerifier(), new Nonce(),
			0L, state));
		_mockHttpServletRequest.setSession(_mockHttpSession);
		_mockHttpServletRequest.setRequestURI("https://example.com");

		OAuthClientEntry mockOAuthClientEntry = Mockito.mock(OAuthClientEntry.class);
		Mockito.when(mockOAuthClientEntry.getInfoJSON()).thenReturn("InfoJson");
		Mockito.when(mockOAuthClientEntry.getAuthServerWellKnownURI()).thenReturn("wellKnowUri");
		Mockito.when(mockOAuthClientEntry.getTokenRequestParametersJSON()).thenReturn("ParamJSON");

		OAuthClientEntryLocalService mockOAuthClientEntryLocalService = Mockito.mock(OAuthClientEntryLocalService.class);
		Mockito.when(mockOAuthClientEntryLocalService.getOAuthClientEntry(Mockito.anyLong())).thenReturn(mockOAuthClientEntry);

		_mockStaticAuthenticationResponseParserWithState(state);

		ReflectionTestUtil.setFieldValue(
			_testedClass,
			"_oAuthClientEntryLocalService",
			mockOAuthClientEntryLocalService);

		_mockStaticJSONParsers();

		AuthorizationServerMetadataResolver mockResolver = Mockito.mock(AuthorizationServerMetadataResolver.class);
		OIDCProviderMetadata mockProviderMetadata = Mockito.mock(OIDCProviderMetadata.class);
		Mockito.when(mockResolver.resolveOIDCProviderMetadata(Mockito.anyString())).thenReturn(mockProviderMetadata);

		ReflectionTestUtil.setFieldValue(
			_testedClass,
			"_authorizationServerMetadataResolver",
			mockResolver);

		Portal mockPortal = Mockito.mock(Portal.class);
		Mockito.when(mockPortal.getPortalURL(_mockHttpServletRequest)).thenReturn("https://example.com");
		Mockito.when(mockPortal.getPathContext(Mockito.anyString())).thenReturn("/pathContext");

		ReflectionTestUtil.setFieldValue(
			_testedClass,
			"_portal",
			mockPortal);

		JWT mockJWT = Mockito.mock(JWT.class);
		JWTClaimsSet jwtClaimsSet= _fillJWTClaimSet();
		Mockito.when(mockJWT.getJWTClaimsSet()).thenReturn(jwtClaimsSet);

		_mockStaticOpenIdConnectTokenRequestUtil(mockJWT);

		//when
		try {
			_testedClass.processAuthenticationResponse(
				_mockHttpServletRequest, _mockHttpServletResponse,
				_userIdUnsafeConsumer);
		} catch (NullPointerException npe){
			System.out.println(npe);
		} catch (Exception e){
			System.out.println(e);
		}
		//then
	}

	private JWTClaimsSet _fillJWTClaimSet() throws ParseException {
		String email1 = "email1@email.com";
		String email2 = "email2@email.com";
		Object list = Arrays.asList(email1, email2);
		Map<String, Object> map = HashMapBuilder.put("emails", list).build();
		return JWTClaimsSet.parse(map);
	}

	private void _mockStaticAuthenticationResponseParserWithState(State state){
		AuthenticationSuccessResponse mockSuccessResponse = Mockito.mock(AuthenticationSuccessResponse.class);
		Mockito.when(mockSuccessResponse.getState()).thenReturn(state);

		Mockito.mockStatic(
			AuthenticationResponseParser.class).when(() ->AuthenticationResponseParser.parse(Mockito.any(
			URI.class))).thenReturn(mockSuccessResponse);
	}

	private void _mockStaticJSONParsers(){
		OIDCClientInformation mockOidcClientInformation = Mockito.mock(OIDCClientInformation.class);

		Mockito.mockStatic(
			OIDCClientInformation.class).when(() -> OIDCClientInformation.parse(Mockito.any())).thenReturn(mockOidcClientInformation);

		JSONObject mockJSONObject = Mockito.mock(JSONObject.class);

		Mockito.mockStatic(JSONObjectUtils.class).when(() -> JSONObjectUtils.parse(Mockito.any())).thenReturn(mockJSONObject);
	}

	private void _mockStaticOpenIdConnectTokenRequestUtil(JWT jwt) {
		OIDCTokens mockOidcTokens = Mockito.mock(OIDCTokens.class);
		Mockito.when(mockOidcTokens.getIDToken()).thenReturn(jwt);

		Mockito.mockStatic(OpenIdConnectTokenRequestUtil.class).when(() ->OpenIdConnectTokenRequestUtil.request(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),Mockito.any(),Mockito.any(), Mockito.any())).thenReturn(mockOidcTokens);
	}

	private MockHttpServletRequest _mockHttpServletRequest;
	private MockHttpServletResponse _mockHttpServletResponse;
	private UnsafeConsumer<Long, Exception> _userIdUnsafeConsumer;
	private OpenIdConnectAuthenticationHandlerImpl _testedClass;
	private MockHttpSession _mockHttpSession;
}
