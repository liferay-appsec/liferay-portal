/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.internal.action.executor;

import com.liferay.object.model.ObjectAction;
import com.liferay.portal.json.JSONObjectImpl;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.UnicodePropertiesBuilder;
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
public class WebhookObjectActionExecutorImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testExecute() throws Exception {
		WebhookObjectActionExecutorImpl webhookObjectActionExecutorImpl =
			new WebhookObjectActionExecutorImpl();

		Http http = Mockito.mock(Http.class);

		ReflectionTestUtil.setFieldValue(
			webhookObjectActionExecutorImpl, "_http", http);

		SecretResolver secretResolver = Mockito.mock(SecretResolver.class);

		long companyId = RandomTestUtil.randomLong();
		long objectActionId = RandomTestUtil.randomLong();
		String secret = RandomTestUtil.randomString();

		String resolvedSecret = RandomTestUtil.randomString();

		Mockito.when(
			secretResolver.resolve(
				Mockito.eq(companyId),
				Mockito.argThat(
					(SecretContext secretContext) ->
						Objects.equals(
							secretContext.getClassName(),
							ObjectAction.class.getName()) &&
						(secretContext.getCompanyId() == companyId) &&
						Objects.equals(secretContext.getKey(), "secret") &&
						(secretContext.getPrimaryKey() == objectActionId)),
				Mockito.eq(secret))
		).thenReturn(
			resolvedSecret
		);

		ReflectionTestUtil.setFieldValue(
			webhookObjectActionExecutorImpl, "_secretResolver", secretResolver);

		webhookObjectActionExecutorImpl.execute(
			companyId, objectActionId,
			UnicodePropertiesBuilder.put(
				"secret", secret
			).put(
				"url", "https://" + RandomTestUtil.randomString()
			).put(
				"urlLocalNetworkAccessEnabled", "true"
			).build(),
			new JSONObjectImpl(), RandomTestUtil.randomLong());

		ArgumentCaptor<Http.Options> argumentCaptor = ArgumentCaptor.forClass(
			Http.Options.class);

		Mockito.verify(
			http
		).URLtoString(
			argumentCaptor.capture()
		);

		Http.Options options = argumentCaptor.getValue();

		Map<String, String> headers = options.getHeaders();

		Assert.assertEquals(resolvedSecret, headers.get("x-api-key"));
	}

}