/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.saml.admin.rest.client.serdes.v1_0;

import com.liferay.saml.admin.rest.client.dto.v1_0.Error;
import com.liferay.saml.admin.rest.client.json.BaseJSONParser;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.Generated;

/**
 * @author Stian Sigvartsen
 * @generated
 */
@Generated("")
public class ErrorSerDes {

	public static Error toDTO(String json) {
		ErrorJSONParser errorJSONParser = new ErrorJSONParser();

		return errorJSONParser.parseToDTO(json);
	}

	public static Error[] toDTOs(String json) {
		ErrorJSONParser errorJSONParser = new ErrorJSONParser();

		return errorJSONParser.parseToDTOs(json);
	}

	public static String toJSON(Error error) {
		if (error == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (error.getDetail() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"detail\": ");

			sb.append("\"");

			sb.append(_escape(error.getDetail()));

			sb.append("\"");
		}

		if (error.getStatus() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"status\": ");

			sb.append("\"");

			sb.append(_escape(error.getStatus()));

			sb.append("\"");
		}

		if (error.getTitle() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"title\": ");

			sb.append("\"");

			sb.append(_escape(error.getTitle()));

			sb.append("\"");
		}

		if (error.getType() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"type\": ");

			sb.append("\"");

			sb.append(_escape(error.getType()));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		ErrorJSONParser errorJSONParser = new ErrorJSONParser();

		return errorJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(Error error) {
		if (error == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (error.getDetail() == null) {
			map.put("detail", null);
		}
		else {
			map.put("detail", String.valueOf(error.getDetail()));
		}

		if (error.getStatus() == null) {
			map.put("status", null);
		}
		else {
			map.put("status", String.valueOf(error.getStatus()));
		}

		if (error.getTitle() == null) {
			map.put("title", null);
		}
		else {
			map.put("title", String.valueOf(error.getTitle()));
		}

		if (error.getType() == null) {
			map.put("type", null);
		}
		else {
			map.put("type", String.valueOf(error.getType()));
		}

		return map;
	}

	public static class ErrorJSONParser extends BaseJSONParser<Error> {

		@Override
		protected Error createDTO() {
			return new Error();
		}

		@Override
		protected Error[] createDTOArray(int size) {
			return new Error[size];
		}

		@Override
		protected void setField(
			Error error, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "detail")) {
				if (jsonParserFieldValue != null) {
					error.setDetail((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "status")) {
				if (jsonParserFieldValue != null) {
					error.setStatus((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "title")) {
				if (jsonParserFieldValue != null) {
					error.setTitle((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "type")) {
				if (jsonParserFieldValue != null) {
					error.setType((String)jsonParserFieldValue);
				}
			}
		}

	}

	private static String _escape(Object object) {
		String string = String.valueOf(object);

		for (String[] strings : BaseJSONParser.JSON_ESCAPE_STRINGS) {
			string = string.replace(strings[0], strings[1]);
		}

		return string;
	}

	private static String _toJSON(Map<String, ?> map) {
		StringBuilder sb = new StringBuilder("{");

		@SuppressWarnings("unchecked")
		Set set = map.entrySet();

		@SuppressWarnings("unchecked")
		Iterator<Map.Entry<String, ?>> iterator = set.iterator();

		while (iterator.hasNext()) {
			Map.Entry<String, ?> entry = iterator.next();

			sb.append("\"");
			sb.append(entry.getKey());
			sb.append("\": ");

			Object value = entry.getValue();

			Class<?> valueClass = value.getClass();

			if (value instanceof Map) {
				sb.append(_toJSON((Map)value));
			}
			else if (valueClass.isArray()) {
				Object[] values = (Object[])value;

				sb.append("[");

				for (int i = 0; i < values.length; i++) {
					sb.append("\"");
					sb.append(_escape(values[i]));
					sb.append("\"");

					if ((i + 1) < values.length) {
						sb.append(", ");
					}
				}

				sb.append("]");
			}
			else if (value instanceof String) {
				sb.append("\"");
				sb.append(_escape(entry.getValue()));
				sb.append("\"");
			}
			else {
				sb.append(String.valueOf(entry.getValue()));
			}

			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}

		sb.append("}");

		return sb.toString();
	}

}