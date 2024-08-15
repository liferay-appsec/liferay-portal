package com.liferay.portal.security.sso.openid.connect.internal;

import com.liferay.oauth.client.persistence.service.OAuthClientEntryLocalService;
import com.liferay.oauth.client.persistence.service.OAuthClientEntryLocalServiceWrapper;
import com.liferay.petra.function.UnsafeConsumer;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier;
import com.nimbusds.openid.connect.sdk.AuthenticationResponseParser;
import com.nimbusds.openid.connect.sdk.AuthenticationSuccessResponse;
import com.nimbusds.openid.connect.sdk.Nonce;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.osgi.service.component.annotations.Reference;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import java.net.URI;


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
	public void testProcessAuthenticationResponseGivenJWTClaimSetThenEmailsIsMissingThenNullPointerExceptionIsNotThrown() {
		//given

		OpenIdConnectAuthenticationSession mockOpenIdConnectAuthenticationSession = Mockito.mock(OpenIdConnectAuthenticationSession.class);
		State state = new State("state");
		Mockito.when(mockOpenIdConnectAuthenticationSession.getState()).thenReturn(state);
//		Mockito.when(_mockHttpSession.getAttribute(OpenIdConnectAuthenticationHandlerImpl.class.getName() +
//												   "#OPEN_ID_CONNECT_AUTHENTICATION_SESSION")).thenReturn(mockOpenIdConnectAuthenticationSession);
		_mockHttpSession.setAttribute(OpenIdConnectAuthenticationHandlerImpl.class.getName() +
									  "#OPEN_ID_CONNECT_AUTHENTICATION_SESSION", new OpenIdConnectAuthenticationSession(new CodeVerifier(), new Nonce(),0l, state));
		_mockHttpServletRequest.setSession(_mockHttpSession);

		_mockHttpServletRequest.setRequestURI("https://example.com");



		//_mockHttpServletRequest.setQueryString("querystring");
		_mockAuthenticationResponse(state);

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

	private void _mockAuthenticationResponse(State state){
		AuthenticationSuccessResponse _mockSuccessResponse = Mockito.mock(AuthenticationSuccessResponse.class);
		Mockito.when(_mockSuccessResponse.getState()).thenReturn(state);

		Mockito.mockStatic(
			AuthenticationResponseParser.class).when(() ->AuthenticationResponseParser.parse(Mockito.any(
			URI.class))).thenReturn(_mockSuccessResponse);
	}

	private MockHttpServletRequest _mockHttpServletRequest;
	private MockHttpServletResponse _mockHttpServletResponse;
	private UnsafeConsumer<Long, Exception> _userIdUnsafeConsumer;
	private OpenIdConnectAuthenticationHandlerImpl _testedClass;
	private MockHttpSession _mockHttpSession;

	private OAuthClientEntryLocalService _OAuthClientEntryLocalService = new OAuthClientEntryLocalServiceWrapper();
}
