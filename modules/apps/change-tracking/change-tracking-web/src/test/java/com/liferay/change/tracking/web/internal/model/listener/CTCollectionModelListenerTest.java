/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.change.tracking.web.internal.model.listener;

import com.liferay.change.tracking.model.CTRemote;
import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.security.key.secret.SecretContext;
import com.liferay.portal.security.key.secret.SecretResolver;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Map;
import java.util.Objects;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * @author Caio Farias
 */
public class CTCollectionModelListenerTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testGetToken() throws Exception {
		CTCollectionModelListener ctCollectionModelListener =
			new CTCollectionModelListener();

		CTRemote ctRemote = Mockito.mock(CTRemote.class);

		String clientSecret = RandomTestUtil.randomString();

		Mockito.when(
			ctRemote.getClientSecret()
		).thenReturn(
			clientSecret
		);

		long companyId = RandomTestUtil.randomLong();

		Mockito.when(
			ctRemote.getCompanyId()
		).thenReturn(
			companyId
		);

		long primaryKey = RandomTestUtil.randomLong();

		Mockito.when(
			ctRemote.getPrimaryKey()
		).thenReturn(
			primaryKey
		);

		Http http = Mockito.mock(Http.class);

		Mockito.when(
			http.URLtoString(Mockito.any(Http.Options.class))
		).thenReturn(
			"{}"
		);

		ReflectionTestUtil.setFieldValue(
			ctCollectionModelListener, "_http", http);

		ReflectionTestUtil.setFieldValue(
			ctCollectionModelListener, "_jsonFactory", new JSONFactoryImpl());

		SecretResolver secretResolver = Mockito.mock(SecretResolver.class);

		String resolvedClientSecret = RandomTestUtil.randomString();

		Mockito.when(
			secretResolver.resolve(
				Mockito.eq(companyId),
				Mockito.argThat(
					(SecretContext secretContext) ->
						Objects.equals(
							secretContext.getClassName(),
							CTRemote.class.getName()) &&
						(secretContext.getCompanyId() == companyId) &&
						Objects.equals(
							secretContext.getKey(), "clientSecret") &&
						(secretContext.getPrimaryKey() == primaryKey)),
				Mockito.eq(clientSecret))
		).thenReturn(
			resolvedClientSecret
		);

		ReflectionTestUtil.setFieldValue(
			ctCollectionModelListener, "_secretResolver", secretResolver);

		ReflectionTestUtil.invoke(
			ctCollectionModelListener, "_getToken",
			new Class<?>[] {long.class, CTRemote.class}, companyId, ctRemote);

		ArgumentCaptor<Http.Options> argumentCaptor = ArgumentCaptor.forClass(
			Http.Options.class);

		Mockito.verify(
			http
		).URLtoString(
			argumentCaptor.capture()
		);

		Http.Options options = argumentCaptor.getValue();

		Map<String, String> parts = options.getParts();

		Assert.assertEquals(resolvedClientSecret, parts.get("client_secret"));
	}

}