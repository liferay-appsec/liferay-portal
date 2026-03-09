/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.cookies.rest.resource.v1_0.test;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

import com.liferay.cookies.rest.client.dto.v1_0.ConsentPreference;
import com.liferay.cookies.rest.client.http.HttpInvoker;
import com.liferay.cookies.rest.client.pagination.Page;
import com.liferay.cookies.rest.client.resource.v1_0.ConsentPreferenceResource;
import com.liferay.cookies.rest.client.serdes.v1_0.ConsentPreferenceSerDes;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONDeserializer;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.DateUtil;
import com.liferay.portal.kernel.util.FastDateFormatFactoryUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.odata.entity.EntityField;
import com.liferay.portal.odata.entity.EntityModel;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.vulcan.resource.EntityModelResource;

import jakarta.annotation.Generated;

import jakarta.ws.rs.core.MultivaluedHashMap;

import java.lang.reflect.Method;

import java.text.Format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Christopher Kian
 * @generated
 */
@Generated("")
public abstract class BaseConsentPreferenceResourceTestCase {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@BeforeClass
	public static void setUpClass() throws Exception {
		_format = FastDateFormatFactoryUtil.getSimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss'Z'");
	}

	@Before
	public void setUp() throws Exception {
		irrelevantGroup = GroupTestUtil.addGroup();
		testGroup = GroupTestUtil.addGroup();

		testCompany = CompanyLocalServiceUtil.getCompany(
			testGroup.getCompanyId());

		_consentPreferenceResource.setContextCompany(testCompany);

		_testCompanyAdminUser = UserTestUtil.getAdminUser(
			testCompany.getCompanyId());

		consentPreferenceResource = ConsentPreferenceResource.builder(
		).authentication(
			_testCompanyAdminUser.getEmailAddress(),
			PropsValues.DEFAULT_ADMIN_PASSWORD
		).endpoint(
			testCompany.getVirtualHostname(), 8080, "http"
		).locale(
			LocaleUtil.getDefault()
		).build();
	}

	@After
	public void tearDown() throws Exception {
		GroupTestUtil.deleteGroup(irrelevantGroup);
		GroupTestUtil.deleteGroup(testGroup);
	}

	@Test
	public void testClientSerDesToDTO() throws Exception {
		ObjectMapper objectMapper = getClientSerDesObjectMapper();

		ConsentPreference consentPreference1 = randomConsentPreference();

		String json = objectMapper.writeValueAsString(consentPreference1);

		ConsentPreference consentPreference2 = ConsentPreferenceSerDes.toDTO(
			json);

		Assert.assertTrue(equals(consentPreference1, consentPreference2));
	}

	@Test
	public void testClientSerDesToJSON() throws Exception {
		ObjectMapper objectMapper = getClientSerDesObjectMapper();

		ConsentPreference consentPreference = randomConsentPreference();

		String json1 = objectMapper.writeValueAsString(consentPreference);
		String json2 = ConsentPreferenceSerDes.toJSON(consentPreference);

		Assert.assertEquals(
			objectMapper.readTree(json1), objectMapper.readTree(json2));
	}

	protected ObjectMapper getClientSerDesObjectMapper() {
		return new ObjectMapper() {
			{
				configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
				configure(
					SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
				enable(SerializationFeature.INDENT_OUTPUT);
				setDateFormat(new ISO8601DateFormat());
				setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
				setSerializationInclusion(JsonInclude.Include.NON_NULL);
				setVisibility(
					PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
				setVisibility(
					PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
			}
		};
	}

	@Test
	public void testEscapeRegexInStringFields() throws Exception {
		String regex = "^[0-9]+(\\.[0-9]{1,2})\"?";

		ConsentPreference consentPreference = randomConsentPreference();

		consentPreference.setDomain(regex);
		consentPreference.setName(regex);
		consentPreference.setValue(regex);

		String json = ConsentPreferenceSerDes.toJSON(consentPreference);

		Assert.assertFalse(json.contains(regex));

		consentPreference = ConsentPreferenceSerDes.toDTO(json);

		Assert.assertEquals(regex, consentPreference.getDomain());
		Assert.assertEquals(regex, consentPreference.getName());
		Assert.assertEquals(regex, consentPreference.getValue());
	}

	@Test
	public void testDeleteConsentPreferenceByName() throws Exception {
		@SuppressWarnings("PMD.UnusedLocalVariable")
		ConsentPreference consentPreference =
			testDeleteConsentPreferenceByName_addConsentPreference();

		assertHttpResponseStatusCode(
			204,
			consentPreferenceResource.deleteConsentPreferenceByNameHttpResponse(
				consentPreference.getName()));

		assertHttpResponseStatusCode(
			404,
			consentPreferenceResource.getConsentPreferenceByNameHttpResponse(
				consentPreference.getName()));
		assertHttpResponseStatusCode(
			404,
			consentPreferenceResource.getConsentPreferenceByNameHttpResponse(
				consentPreference.getName()));
	}

	protected ConsentPreference
			testDeleteConsentPreferenceByName_addConsentPreference()
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	@Test
	public void testGraphQLDeleteConsentPreferenceByName() throws Exception {

		// No namespace

		ConsentPreference consentPreference1 =
			testGraphQLDeleteConsentPreferenceByName_addConsentPreference();

		Assert.assertTrue(
			JSONUtil.getValueAsBoolean(
				invokeGraphQLMutation(
					new GraphQLField(
						"deleteConsentPreferenceByName",
						new HashMap<String, Object>() {
							{
								put(
									"name",
									"\"" + consentPreference1.getName() + "\"");
							}
						})),
				"JSONObject/data", "Object/deleteConsentPreferenceByName"));

		JSONArray errorsJSONArray1 = JSONUtil.getValueAsJSONArray(
			invokeGraphQLQuery(
				new GraphQLField(
					"consentPreferenceByName",
					new HashMap<String, Object>() {
						{
							put(
								"name",
								"\"" + consentPreference1.getName() + "\"");
						}
					},
					getGraphQLFields())),
			"JSONArray/errors");

		Assert.assertTrue(errorsJSONArray1.length() > 0);

		// Using the namespace cookies_v1_0

		ConsentPreference consentPreference2 =
			testGraphQLDeleteConsentPreferenceByName_addConsentPreference();

		Assert.assertTrue(
			JSONUtil.getValueAsBoolean(
				invokeGraphQLMutation(
					new GraphQLField(
						"cookies_v1_0",
						new GraphQLField(
							"deleteConsentPreferenceByName",
							new HashMap<String, Object>() {
								{
									put(
										"name",
										"\"" + consentPreference2.getName() +
											"\"");
								}
							}))),
				"JSONObject/data", "JSONObject/cookies_v1_0",
				"Object/deleteConsentPreferenceByName"));

		JSONArray errorsJSONArray2 = JSONUtil.getValueAsJSONArray(
			invokeGraphQLQuery(
				new GraphQLField(
					"cookies_v1_0",
					new GraphQLField(
						"consentPreferenceByName",
						new HashMap<String, Object>() {
							{
								put(
									"name",
									"\"" + consentPreference2.getName() + "\"");
							}
						},
						getGraphQLFields()))),
			"JSONArray/errors");

		Assert.assertTrue(errorsJSONArray2.length() > 0);
	}

	protected ConsentPreference
			testGraphQLDeleteConsentPreferenceByName_addConsentPreference()
		throws Exception {

		return testGraphQLConsentPreference_addConsentPreference();
	}

	@Test
	public void testDeleteConsentPreferences() throws Exception {
		@SuppressWarnings("PMD.UnusedLocalVariable")
		ConsentPreference consentPreference =
			testDeleteConsentPreferences_addConsentPreference();

		assertHttpResponseStatusCode(
			204,
			consentPreferenceResource.deleteConsentPreferencesHttpResponse());
	}

	protected ConsentPreference
			testDeleteConsentPreferences_addConsentPreference()
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	@Test
	public void testGraphQLDeleteConsentPreferences() throws Exception {
		Assert.assertTrue(false);
	}

	@Test
	public void testGetConsentPreferenceByName() throws Exception {
		ConsentPreference postConsentPreference =
			testGetConsentPreferenceByName_addConsentPreference();

		ConsentPreference getConsentPreference =
			consentPreferenceResource.getConsentPreferenceByName(
				postConsentPreference.getName());

		assertEquals(postConsentPreference, getConsentPreference);
		assertValid(getConsentPreference);
	}

	protected ConsentPreference
			testGetConsentPreferenceByName_addConsentPreference()
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	@Test
	public void testGraphQLGetConsentPreferenceByName() throws Exception {
		ConsentPreference consentPreference =
			testGraphQLGetConsentPreferenceByName_addConsentPreference();

		// No namespace

		Assert.assertTrue(
			equals(
				consentPreference,
				ConsentPreferenceSerDes.toDTO(
					JSONUtil.getValueAsString(
						invokeGraphQLQuery(
							new GraphQLField(
								"consentPreferenceByName",
								new HashMap<String, Object>() {
									{
										put(
											"name",
											"\"" + consentPreference.getName() +
												"\"");
									}
								},
								getGraphQLFields())),
						"JSONObject/data", "Object/consentPreferenceByName"))));

		// Using the namespace cookies_v1_0

		Assert.assertTrue(
			equals(
				consentPreference,
				ConsentPreferenceSerDes.toDTO(
					JSONUtil.getValueAsString(
						invokeGraphQLQuery(
							new GraphQLField(
								"cookies_v1_0",
								new GraphQLField(
									"consentPreferenceByName",
									new HashMap<String, Object>() {
										{
											put(
												"name",
												"\"" +
													consentPreference.
														getName() + "\"");
										}
									},
									getGraphQLFields()))),
						"JSONObject/data", "JSONObject/cookies_v1_0",
						"Object/consentPreferenceByName"))));
	}

	@Test
	public void testGraphQLGetConsentPreferenceByNameNotFound()
		throws Exception {

		String irrelevantName = "\"" + RandomTestUtil.randomString() + "\"";

		// No namespace

		Assert.assertEquals(
			"Not Found",
			JSONUtil.getValueAsString(
				invokeGraphQLQuery(
					new GraphQLField(
						"consentPreferenceByName",
						new HashMap<String, Object>() {
							{
								put("name", irrelevantName);
							}
						},
						getGraphQLFields())),
				"JSONArray/errors", "Object/0", "JSONObject/extensions",
				"Object/code"));

		// Using the namespace cookies_v1_0

		Assert.assertEquals(
			"Not Found",
			JSONUtil.getValueAsString(
				invokeGraphQLQuery(
					new GraphQLField(
						"cookies_v1_0",
						new GraphQLField(
							"consentPreferenceByName",
							new HashMap<String, Object>() {
								{
									put("name", irrelevantName);
								}
							},
							getGraphQLFields()))),
				"JSONArray/errors", "Object/0", "JSONObject/extensions",
				"Object/code"));
	}

	protected ConsentPreference
			testGraphQLGetConsentPreferenceByName_addConsentPreference()
		throws Exception {

		return testGraphQLConsentPreference_addConsentPreference();
	}

	@Test
	public void testGetConsentPreferencesPage() throws Exception {
		Page<ConsentPreference> page =
			consentPreferenceResource.getConsentPreferencesPage();

		long totalCount = page.getTotalCount();

		ConsentPreference consentPreference1 =
			testGetConsentPreferencesPage_addConsentPreference(
				randomConsentPreference());

		ConsentPreference consentPreference2 =
			testGetConsentPreferencesPage_addConsentPreference(
				randomConsentPreference());

		page = consentPreferenceResource.getConsentPreferencesPage();

		Assert.assertEquals(totalCount + 2, page.getTotalCount());

		assertContains(
			consentPreference1, (List<ConsentPreference>)page.getItems());
		assertContains(
			consentPreference2, (List<ConsentPreference>)page.getItems());
		assertValid(page, testGetConsentPreferencesPage_getExpectedActions());
	}

	protected Map<String, Map<String, String>>
			testGetConsentPreferencesPage_getExpectedActions()
		throws Exception {

		Map<String, Map<String, String>> expectedActions = new HashMap<>();

		return expectedActions;
	}

	protected ConsentPreference
			testGetConsentPreferencesPage_addConsentPreference(
				ConsentPreference consentPreference)
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	@Test
	public void testGraphQLGetConsentPreferencesPage() throws Exception {
		GraphQLField graphQLField = new GraphQLField(
			"consentPreferences",
			new HashMap<String, Object>() {
				{
				}
			},
			new GraphQLField("items", getGraphQLFields()),
			new GraphQLField("page"), new GraphQLField("totalCount"));

		// No namespace

		JSONObject consentPreferencesJSONObject = JSONUtil.getValueAsJSONObject(
			invokeGraphQLQuery(graphQLField), "JSONObject/data",
			"JSONObject/consentPreferences");

		long totalCount = consentPreferencesJSONObject.getLong("totalCount");

		ConsentPreference consentPreference1 =
			testGraphQLConsentPreference_addConsentPreference(
				randomConsentPreference());

		ConsentPreference consentPreference2 =
			testGraphQLConsentPreference_addConsentPreference(
				randomConsentPreference());

		consentPreferencesJSONObject = JSONUtil.getValueAsJSONObject(
			invokeGraphQLQuery(graphQLField), "JSONObject/data",
			"JSONObject/consentPreferences");

		Assert.assertEquals(
			totalCount + 2, consentPreferencesJSONObject.getLong("totalCount"));

		assertContains(
			consentPreference1,
			Arrays.asList(
				ConsentPreferenceSerDes.toDTOs(
					consentPreferencesJSONObject.getString("items"))));
		assertContains(
			consentPreference2,
			Arrays.asList(
				ConsentPreferenceSerDes.toDTOs(
					consentPreferencesJSONObject.getString("items"))));

		// Using the namespace cookies_v1_0

		consentPreferencesJSONObject = JSONUtil.getValueAsJSONObject(
			invokeGraphQLQuery(new GraphQLField("cookies_v1_0", graphQLField)),
			"JSONObject/data", "JSONObject/cookies_v1_0",
			"JSONObject/consentPreferences");

		Assert.assertEquals(
			totalCount + 2, consentPreferencesJSONObject.getLong("totalCount"));

		assertContains(
			consentPreference1,
			Arrays.asList(
				ConsentPreferenceSerDes.toDTOs(
					consentPreferencesJSONObject.getString("items"))));
		assertContains(
			consentPreference2,
			Arrays.asList(
				ConsentPreferenceSerDes.toDTOs(
					consentPreferencesJSONObject.getString("items"))));
	}

	@Test
	public void testPatchConsentPreference() throws Exception {
		Assert.assertTrue(false);
	}

	@Test
	public void testPostConsentPreference() throws Exception {
		ConsentPreference randomConsentPreference = randomConsentPreference();

		ConsentPreference postConsentPreference =
			testPostConsentPreference_addConsentPreference(
				randomConsentPreference);

		assertEquals(randomConsentPreference, postConsentPreference);
		assertValid(postConsentPreference);
	}

	protected ConsentPreference testPostConsentPreference_addConsentPreference(
			ConsentPreference consentPreference)
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	@Test
	public void testGraphQLPostConsentPreference() throws Exception {
		ConsentPreference randomConsentPreference = randomConsentPreference();

		ConsentPreference consentPreference =
			testGraphQLConsentPreference_addConsentPreference(
				randomConsentPreference);

		Assert.assertTrue(equals(randomConsentPreference, consentPreference));
	}

	@Test
	public void testPutConsentPreference() throws Exception {
		ConsentPreference postConsentPreference =
			testPutConsentPreference_addConsentPreference();

		ConsentPreference randomConsentPreference = randomConsentPreference();

		ConsentPreference putConsentPreference =
			consentPreferenceResource.putConsentPreference(
				randomConsentPreference);

		assertEquals(randomConsentPreference, putConsentPreference);
		assertValid(putConsentPreference);

		ConsentPreference getConsentPreference =
			testPutConsentPreference_getConsentPreference();

		assertEquals(randomConsentPreference, getConsentPreference);
		assertValid(getConsentPreference);
	}

	protected ConsentPreference
		testPutConsentPreference_getConsentPreference() {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected ConsentPreference testPutConsentPreference_addConsentPreference()
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	@Test
	public void testBatchEngineDeleteImportTask() throws Exception {
		Assert.assertTrue(true);
	}

	protected ConsentPreference
			testGraphQLConsentPreference_addConsentPreference()
		throws Exception {

		return testGraphQLConsentPreference_addConsentPreference(
			randomConsentPreference());
	}

	protected ConsentPreference
			testGraphQLConsentPreference_addConsentPreference(
				ConsentPreference consentPreference)
		throws Exception {

		JSONDeserializer<ConsentPreference> jsonDeserializer =
			JSONFactoryUtil.createJSONDeserializer();

		StringBuilder sb = new StringBuilder("{");

		for (java.lang.reflect.Field field :
				getDeclaredFields(ConsentPreference.class)) {

			if (getGraphQLValue(field.get(consentPreference)) != null) {
				if (sb.length() > 1) {
					sb.append(", ");
				}

				sb.append(field.getName());
				sb.append(": ");
				sb.append(getGraphQLValue(field.get(consentPreference)));
			}
		}

		sb.append("}");

		List<GraphQLField> graphQLFields = getGraphQLFields();

		return jsonDeserializer.deserialize(
			JSONUtil.getValueAsString(
				invokeGraphQLMutation(
					new GraphQLField(
						"createConsentPreference",
						new HashMap<String, Object>() {
							{
								put("consentPreference", sb.toString());
							}
						},
						graphQLFields)),
				"JSONObject/data", "JSONObject/createConsentPreference"),
			ConsentPreference.class);
	}

	protected String getGraphQLValue(Object value) throws Exception {
		if (value == null) {
			return null;
		}
		else if (value instanceof Boolean || value instanceof Number) {
			return value.toString();
		}
		else if (value instanceof Date date) {
			return "\"" +
				DateUtil.getDate(
					date, "yyyy-MM-dd'T'HH:mm:ss'Z'", LocaleUtil.getDefault(),
					TimeZone.getTimeZone("UTC")) + "\"";
		}
		else if (value instanceof Enum<?> enm) {
			return enm.name();
		}
		else if (value instanceof Map<?, ?> map) {
			List<String> entries = new ArrayList<>();

			for (Map.Entry<?, ?> entry : map.entrySet()) {
				String graphQLValue = getGraphQLValue(entry.getValue());

				if (graphQLValue != null) {
					entries.add(entry.getKey() + ": " + graphQLValue);
				}
			}

			return "{" + String.join(", ", entries) + "}";
		}
		else if (value instanceof Object[] array) {
			List<String> entries = new ArrayList<>();

			for (Object entry : array) {
				String graphQLValue = getGraphQLValue(entry);

				if (graphQLValue != null) {
					entries.add(graphQLValue);
				}
			}

			return "[" + String.join(", ", entries) + "]";
		}
		else if (value instanceof String) {
			return "\"" + value + "\"";
		}
		else {
			List<String> entries = new ArrayList<>();

			Class<?> clazz = value.getClass();
			java.lang.reflect.Field[] declaredFields = getDeclaredFields(clazz);

			if (declaredFields.length == 0) {
				declaredFields = getDeclaredFields(clazz.getSuperclass());
			}

			for (java.lang.reflect.Field field : declaredFields) {
				String graphQLValue = getGraphQLValue(field.get(value));

				if (graphQLValue != null) {
					entries.add(field.getName() + ": " + graphQLValue);
				}
			}

			return "{" + String.join(", ", entries) + "}";
		}
	}

	protected void assertContains(
		ConsentPreference consentPreference,
		List<ConsentPreference> consentPreferences) {

		boolean contains = false;

		for (ConsentPreference item : consentPreferences) {
			if (equals(consentPreference, item)) {
				contains = true;

				break;
			}
		}

		Assert.assertTrue(
			consentPreferences + " does not contain " + consentPreference,
			contains);
	}

	protected void assertHttpResponseStatusCode(
		int expectedHttpResponseStatusCode,
		HttpInvoker.HttpResponse actualHttpResponse) {

		Assert.assertEquals(
			expectedHttpResponseStatusCode, actualHttpResponse.getStatusCode());
	}

	protected void assertEquals(
		ConsentPreference consentPreference1,
		ConsentPreference consentPreference2) {

		Assert.assertTrue(
			consentPreference1 + " does not equal " + consentPreference2,
			equals(consentPreference1, consentPreference2));
	}

	protected void assertEquals(
		List<ConsentPreference> consentPreferences1,
		List<ConsentPreference> consentPreferences2) {

		Assert.assertEquals(
			consentPreferences1.size(), consentPreferences2.size());

		for (int i = 0; i < consentPreferences1.size(); i++) {
			ConsentPreference consentPreference1 = consentPreferences1.get(i);
			ConsentPreference consentPreference2 = consentPreferences2.get(i);

			assertEquals(consentPreference1, consentPreference2);
		}
	}

	protected void assertEqualsIgnoringOrder(
		List<ConsentPreference> consentPreferences1,
		List<ConsentPreference> consentPreferences2) {

		Assert.assertEquals(
			consentPreferences1.size(), consentPreferences2.size());

		for (ConsentPreference consentPreference1 : consentPreferences1) {
			boolean contains = false;

			for (ConsentPreference consentPreference2 : consentPreferences2) {
				if (equals(consentPreference1, consentPreference2)) {
					contains = true;

					break;
				}
			}

			Assert.assertTrue(
				consentPreferences2 + " does not contain " + consentPreference1,
				contains);
		}
	}

	protected void assertValid(ConsentPreference consentPreference)
		throws Exception {

		boolean valid = true;

		if (consentPreference.getId() == null) {
			valid = false;
		}

		for (String additionalAssertFieldName :
				getAdditionalAssertFieldNames()) {

			if (Objects.equals("domain", additionalAssertFieldName)) {
				if (consentPreference.getDomain() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("expirationDate", additionalAssertFieldName)) {
				if (consentPreference.getExpirationDate() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("name", additionalAssertFieldName)) {
				if (consentPreference.getName() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("userId", additionalAssertFieldName)) {
				if (consentPreference.getUserId() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("value", additionalAssertFieldName)) {
				if (consentPreference.getValue() == null) {
					valid = false;
				}

				continue;
			}

			throw new IllegalArgumentException(
				"Invalid additional assert field name " +
					additionalAssertFieldName);
		}

		Assert.assertTrue(valid);
	}

	protected void assertValid(Page<ConsentPreference> page) {
		assertValid(page, Collections.emptyMap());
	}

	protected void assertValid(
		Page<ConsentPreference> page,
		Map<String, Map<String, String>> expectedActions) {

		boolean valid = false;

		java.util.Collection<ConsentPreference> consentPreferences =
			page.getItems();

		int size = consentPreferences.size();

		if ((page.getLastPage() > 0) && (page.getPage() > 0) &&
			(page.getPageSize() > 0) && (page.getTotalCount() > 0) &&
			(size > 0)) {

			valid = true;
		}

		Assert.assertTrue(valid);

		assertValid(page.getActions(), expectedActions);
	}

	protected void assertValid(
		Map<String, Map<String, String>> actions1,
		Map<String, Map<String, String>> actions2) {

		for (String key : actions2.keySet()) {
			Map action = actions1.get(key);

			Assert.assertNotNull(key + " does not contain an action", action);

			Map<String, String> expectedAction = actions2.get(key);

			Assert.assertEquals(
				expectedAction.get("method"), action.get("method"));
			Assert.assertEquals(expectedAction.get("href"), action.get("href"));
		}
	}

	protected String[] getAdditionalAssertFieldNames() {
		return new String[0];
	}

	protected List<GraphQLField> getGraphQLFields() throws Exception {
		List<GraphQLField> graphQLFields = new ArrayList<>();

		graphQLFields.add(new GraphQLField("id"));

		for (java.lang.reflect.Field field :
				getDeclaredFields(
					com.liferay.cookies.rest.dto.v1_0.ConsentPreference.
						class)) {

			if (!ArrayUtil.contains(
					getAdditionalAssertFieldNames(), field.getName())) {

				continue;
			}

			graphQLFields.addAll(getGraphQLFields(field));
		}

		return graphQLFields;
	}

	protected List<GraphQLField> getGraphQLFields(
			java.lang.reflect.Field... fields)
		throws Exception {

		List<GraphQLField> graphQLFields = new ArrayList<>();

		for (java.lang.reflect.Field field : fields) {
			com.liferay.portal.vulcan.graphql.annotation.GraphQLField
				vulcanGraphQLField = field.getAnnotation(
					com.liferay.portal.vulcan.graphql.annotation.GraphQLField.
						class);

			if (vulcanGraphQLField != null) {
				Class<?> clazz = field.getType();

				if (clazz.isArray()) {
					clazz = clazz.getComponentType();
				}

				List<GraphQLField> childrenGraphQLFields = getGraphQLFields(
					getDeclaredFields(clazz));

				graphQLFields.add(
					new GraphQLField(field.getName(), childrenGraphQLFields));
			}
		}

		return graphQLFields;
	}

	protected String[] getIgnoredEntityFieldNames() {
		return new String[0];
	}

	protected boolean equals(
		ConsentPreference consentPreference1,
		ConsentPreference consentPreference2) {

		if (consentPreference1 == consentPreference2) {
			return true;
		}

		for (String additionalAssertFieldName :
				getAdditionalAssertFieldNames()) {

			if (Objects.equals("domain", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						consentPreference1.getDomain(),
						consentPreference2.getDomain())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("expirationDate", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						consentPreference1.getExpirationDate(),
						consentPreference2.getExpirationDate())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("id", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						consentPreference1.getId(),
						consentPreference2.getId())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("name", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						consentPreference1.getName(),
						consentPreference2.getName())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("userId", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						consentPreference1.getUserId(),
						consentPreference2.getUserId())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("value", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						consentPreference1.getValue(),
						consentPreference2.getValue())) {

					return false;
				}

				continue;
			}

			throw new IllegalArgumentException(
				"Invalid additional assert field name " +
					additionalAssertFieldName);
		}

		return true;
	}

	protected boolean equals(
		Map<String, Object> map1, Map<String, Object> map2) {

		if (Objects.equals(map1.keySet(), map2.keySet())) {
			for (Map.Entry<String, Object> entry : map1.entrySet()) {
				if (entry.getValue() instanceof Map) {
					if (!equals(
							(Map)entry.getValue(),
							(Map)map2.get(entry.getKey()))) {

						return false;
					}
				}
				else if (!Objects.deepEquals(
							entry.getValue(), map2.get(entry.getKey()))) {

					return false;
				}
			}

			return true;
		}

		return false;
	}

	protected java.lang.reflect.Field[] getDeclaredFields(Class clazz)
		throws Exception {

		if (clazz.getClassLoader() == null) {
			return new java.lang.reflect.Field[0];
		}

		return TransformUtil.transform(
			ReflectionUtil.getDeclaredFields(clazz),
			field -> {
				if (field.isSynthetic()) {
					return null;
				}

				return field;
			},
			java.lang.reflect.Field.class);
	}

	protected java.util.Collection<EntityField> getEntityFields()
		throws Exception {

		if (!(_consentPreferenceResource instanceof EntityModelResource)) {
			throw new UnsupportedOperationException(
				"Resource is not an instance of EntityModelResource");
		}

		EntityModelResource entityModelResource =
			(EntityModelResource)_consentPreferenceResource;

		EntityModel entityModel = entityModelResource.getEntityModel(
			new MultivaluedHashMap());

		if (entityModel == null) {
			return Collections.emptyList();
		}

		Map<String, EntityField> entityFieldsMap =
			entityModel.getEntityFieldsMap();

		return entityFieldsMap.values();
	}

	protected List<EntityField> getEntityFields(EntityField.Type type)
		throws Exception {

		return TransformUtil.transform(
			getEntityFields(),
			entityField -> {
				if (!Objects.equals(entityField.getType(), type) ||
					ArrayUtil.contains(
						getIgnoredEntityFieldNames(), entityField.getName())) {

					return null;
				}

				return entityField;
			});
	}

	protected String getFilterString(
		EntityField entityField, String operator,
		ConsentPreference consentPreference) {

		StringBundler sb = new StringBundler();

		String entityFieldName = entityField.getName();

		sb.append(entityFieldName);

		sb.append(" ");
		sb.append(operator);
		sb.append(" ");

		if (entityFieldName.equals("domain")) {
			Object object = consentPreference.getDomain();

			String value = String.valueOf(object);

			if (operator.equals("contains")) {
				sb = new StringBundler();

				sb.append("contains(");
				sb.append(entityFieldName);
				sb.append(",'");

				if ((object != null) && (value.length() > 2)) {
					sb.append(value.substring(1, value.length() - 1));
				}
				else {
					sb.append(value);
				}

				sb.append("')");
			}
			else if (operator.equals("startswith")) {
				sb = new StringBundler();

				sb.append("startswith(");
				sb.append(entityFieldName);
				sb.append(",'");

				if ((object != null) && (value.length() > 1)) {
					sb.append(value.substring(0, value.length() - 1));
				}
				else {
					sb.append(value);
				}

				sb.append("')");
			}
			else {
				sb.append("'");
				sb.append(value);
				sb.append("'");
			}

			return sb.toString();
		}

		if (entityFieldName.equals("expirationDate")) {
			if (operator.equals("between")) {
				Date date = consentPreference.getExpirationDate();

				sb = new StringBundler();

				sb.append("(");
				sb.append(entityFieldName);
				sb.append(" gt ");
				sb.append(_format.format(date.getTime() - (2 * Time.SECOND)));
				sb.append(" and ");
				sb.append(entityFieldName);
				sb.append(" lt ");
				sb.append(_format.format(date.getTime() + (2 * Time.SECOND)));
				sb.append(")");
			}
			else {
				sb.append(entityFieldName);

				sb.append(" ");
				sb.append(operator);
				sb.append(" ");

				sb.append(
					_format.format(consentPreference.getExpirationDate()));
			}

			return sb.toString();
		}

		if (entityFieldName.equals("id")) {
			throw new IllegalArgumentException(
				"Invalid entity field " + entityFieldName);
		}

		if (entityFieldName.equals("name")) {
			Object object = consentPreference.getName();

			String value = String.valueOf(object);

			if (operator.equals("contains")) {
				sb = new StringBundler();

				sb.append("contains(");
				sb.append(entityFieldName);
				sb.append(",'");

				if ((object != null) && (value.length() > 2)) {
					sb.append(value.substring(1, value.length() - 1));
				}
				else {
					sb.append(value);
				}

				sb.append("')");
			}
			else if (operator.equals("startswith")) {
				sb = new StringBundler();

				sb.append("startswith(");
				sb.append(entityFieldName);
				sb.append(",'");

				if ((object != null) && (value.length() > 1)) {
					sb.append(value.substring(0, value.length() - 1));
				}
				else {
					sb.append(value);
				}

				sb.append("')");
			}
			else {
				sb.append("'");
				sb.append(value);
				sb.append("'");
			}

			return sb.toString();
		}

		if (entityFieldName.equals("userId")) {
			throw new IllegalArgumentException(
				"Invalid entity field " + entityFieldName);
		}

		if (entityFieldName.equals("value")) {
			Object object = consentPreference.getValue();

			String value = String.valueOf(object);

			if (operator.equals("contains")) {
				sb = new StringBundler();

				sb.append("contains(");
				sb.append(entityFieldName);
				sb.append(",'");

				if ((object != null) && (value.length() > 2)) {
					sb.append(value.substring(1, value.length() - 1));
				}
				else {
					sb.append(value);
				}

				sb.append("')");
			}
			else if (operator.equals("startswith")) {
				sb = new StringBundler();

				sb.append("startswith(");
				sb.append(entityFieldName);
				sb.append(",'");

				if ((object != null) && (value.length() > 1)) {
					sb.append(value.substring(0, value.length() - 1));
				}
				else {
					sb.append(value);
				}

				sb.append("')");
			}
			else {
				sb.append("'");
				sb.append(value);
				sb.append("'");
			}

			return sb.toString();
		}

		throw new IllegalArgumentException(
			"Invalid entity field " + entityFieldName);
	}

	protected String invoke(String query) throws Exception {
		HttpInvoker httpInvoker = HttpInvoker.newHttpInvoker();

		httpInvoker.body(
			JSONUtil.put(
				"query", query
			).toString(),
			"application/json");
		httpInvoker.httpMethod(HttpInvoker.HttpMethod.POST);
		httpInvoker.path("http://localhost:8080/o/graphql");
		httpInvoker.userNameAndPassword(
			"test@liferay.com:" + PropsValues.DEFAULT_ADMIN_PASSWORD);

		HttpInvoker.HttpResponse httpResponse = httpInvoker.invoke();

		return httpResponse.getContent();
	}

	protected JSONObject invokeGraphQLMutation(GraphQLField graphQLField)
		throws Exception {

		GraphQLField mutationGraphQLField = new GraphQLField(
			"mutation", graphQLField);

		return JSONFactoryUtil.createJSONObject(
			invoke(mutationGraphQLField.toString()));
	}

	protected JSONObject invokeGraphQLQuery(GraphQLField graphQLField)
		throws Exception {

		GraphQLField queryGraphQLField = new GraphQLField(
			"query", graphQLField);

		return JSONFactoryUtil.createJSONObject(
			invoke(queryGraphQLField.toString()));
	}

	protected ConsentPreference randomConsentPreference() throws Exception {
		return new ConsentPreference() {
			{
				domain = StringUtil.toLowerCase(RandomTestUtil.randomString());
				expirationDate = RandomTestUtil.nextDate();
				id = RandomTestUtil.randomLong();
				name = StringUtil.toLowerCase(RandomTestUtil.randomString());
				userId = RandomTestUtil.randomLong();
				value = StringUtil.toLowerCase(RandomTestUtil.randomString());
			}
		};
	}

	protected ConsentPreference randomIrrelevantConsentPreference()
		throws Exception {

		ConsentPreference randomIrrelevantConsentPreference =
			randomConsentPreference();

		return randomIrrelevantConsentPreference;
	}

	protected ConsentPreference randomPatchConsentPreference()
		throws Exception {

		return randomConsentPreference();
	}

	protected ConsentPreferenceResource consentPreferenceResource;
	protected com.liferay.portal.kernel.model.Group irrelevantGroup;
	protected com.liferay.portal.kernel.model.Company testCompany;
	protected com.liferay.portal.kernel.model.Group testGroup;

	protected static class BeanTestUtil {

		public static void copyProperties(Object source, Object target)
			throws Exception {

			Class<?> sourceClass = source.getClass();

			Class<?> targetClass = target.getClass();

			for (java.lang.reflect.Field field :
					_getAllDeclaredFields(sourceClass)) {

				if (field.isSynthetic()) {
					continue;
				}

				Method getMethod = _getMethod(
					sourceClass, field.getName(), "get");

				try {
					Method setMethod = _getMethod(
						targetClass, field.getName(), "set",
						getMethod.getReturnType());

					setMethod.invoke(target, getMethod.invoke(source));
				}
				catch (Exception e) {
					continue;
				}
			}
		}

		public static boolean hasProperty(Object bean, String name) {
			Method setMethod = _getMethod(
				bean.getClass(), "set" + StringUtil.upperCaseFirstLetter(name));

			if (setMethod != null) {
				return true;
			}

			return false;
		}

		public static void setProperty(Object bean, String name, Object value)
			throws Exception {

			Class<?> clazz = bean.getClass();

			Method setMethod = _getMethod(
				clazz, "set" + StringUtil.upperCaseFirstLetter(name));

			if (setMethod == null) {
				throw new NoSuchMethodException();
			}

			Class<?>[] parameterTypes = setMethod.getParameterTypes();

			setMethod.invoke(bean, _translateValue(parameterTypes[0], value));
		}

		private static List<java.lang.reflect.Field> _getAllDeclaredFields(
			Class<?> clazz) {

			List<java.lang.reflect.Field> fields = new ArrayList<>();

			while ((clazz != null) && (clazz != Object.class)) {
				for (java.lang.reflect.Field field :
						clazz.getDeclaredFields()) {

					fields.add(field);
				}

				clazz = clazz.getSuperclass();
			}

			return fields;
		}

		private static Method _getMethod(Class<?> clazz, String name) {
			for (Method method : clazz.getMethods()) {
				if (name.equals(method.getName()) &&
					(method.getParameterCount() == 1) &&
					_parameterTypes.contains(method.getParameterTypes()[0])) {

					return method;
				}
			}

			return null;
		}

		private static Method _getMethod(
				Class<?> clazz, String fieldName, String prefix,
				Class<?>... parameterTypes)
			throws Exception {

			return clazz.getMethod(
				prefix + StringUtil.upperCaseFirstLetter(fieldName),
				parameterTypes);
		}

		private static Object _translateValue(
			Class<?> parameterType, Object value) {

			if ((value instanceof Integer) &&
				parameterType.equals(Long.class)) {

				Integer intValue = (Integer)value;

				return intValue.longValue();
			}

			return value;
		}

		private static final Set<Class<?>> _parameterTypes = new HashSet<>(
			Arrays.asList(
				Boolean.class, Date.class, Double.class, Integer.class,
				Long.class, Map.class, String.class));

	}

	protected class GraphQLField {

		public GraphQLField(String key, GraphQLField... graphQLFields) {
			this(key, new HashMap<>(), graphQLFields);
		}

		public GraphQLField(String key, List<GraphQLField> graphQLFields) {
			this(key, new HashMap<>(), graphQLFields);
		}

		public GraphQLField(
			String key, Map<String, Object> parameterMap,
			GraphQLField... graphQLFields) {

			_key = key;
			_parameterMap = parameterMap;
			_graphQLFields = Arrays.asList(graphQLFields);
		}

		public GraphQLField(
			String key, Map<String, Object> parameterMap,
			List<GraphQLField> graphQLFields) {

			_key = key;
			_parameterMap = parameterMap;
			_graphQLFields = graphQLFields;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(_key);

			if (!_parameterMap.isEmpty()) {
				sb.append("(");

				for (Map.Entry<String, Object> entry :
						_parameterMap.entrySet()) {

					sb.append(entry.getKey());
					sb.append(": ");
					sb.append(entry.getValue());
					sb.append(", ");
				}

				sb.setLength(sb.length() - 2);

				sb.append(")");
			}

			if (!_graphQLFields.isEmpty()) {
				sb.append("{");

				for (GraphQLField graphQLField : _graphQLFields) {
					sb.append(graphQLField.toString());
					sb.append(", ");
				}

				sb.setLength(sb.length() - 2);

				sb.append("}");
			}

			return sb.toString();
		}

		private final List<GraphQLField> _graphQLFields;
		private final String _key;
		private final Map<String, Object> _parameterMap;

	}

	private static final com.liferay.portal.kernel.log.Log _log =
		LogFactoryUtil.getLog(BaseConsentPreferenceResourceTestCase.class);

	private static Format _format;

	private com.liferay.portal.kernel.model.User _testCompanyAdminUser;

	@Inject
	private com.liferay.cookies.rest.resource.v1_0.ConsentPreferenceResource
		_consentPreferenceResource;

}