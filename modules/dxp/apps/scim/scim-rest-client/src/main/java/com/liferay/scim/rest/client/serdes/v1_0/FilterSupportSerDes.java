/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.scim.rest.client.serdes.v1_0;

import com.liferay.scim.rest.client.dto.v1_0.FilterSupport;
import com.liferay.scim.rest.client.json.BaseJSONParser;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.Generated;

/**
 * @author Olivér Kecskeméty
 * @generated
 */
@Generated("")
public class FilterSupportSerDes {

	public static FilterSupport toDTO(String json) {
		FilterSupportJSONParser filterSupportJSONParser =
			new FilterSupportJSONParser();

		return filterSupportJSONParser.parseToDTO(json);
	}

	public static FilterSupport[] toDTOs(String json) {
		FilterSupportJSONParser filterSupportJSONParser =
			new FilterSupportJSONParser();

		return filterSupportJSONParser.parseToDTOs(json);
	}

	public static String toJSON(FilterSupport filterSupport) {
		if (filterSupport == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (filterSupport.getMaxResults() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"maxResults\": ");

			sb.append(filterSupport.getMaxResults());
		}

		if (filterSupport.getSupported() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"supported\": ");

			sb.append(filterSupport.getSupported());
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		FilterSupportJSONParser filterSupportJSONParser =
			new FilterSupportJSONParser();

		return filterSupportJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(FilterSupport filterSupport) {
		if (filterSupport == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (filterSupport.getMaxResults() == null) {
			map.put("maxResults", null);
		}
		else {
			map.put(
				"maxResults", String.valueOf(filterSupport.getMaxResults()));
		}

		if (filterSupport.getSupported() == null) {
			map.put("supported", null);
		}
		else {
			map.put("supported", String.valueOf(filterSupport.getSupported()));
		}

		return map;
	}

	public static class FilterSupportJSONParser
		extends BaseJSONParser<FilterSupport> {

		@Override
		protected FilterSupport createDTO() {
			return new FilterSupport();
		}

		@Override
		protected FilterSupport[] createDTOArray(int size) {
			return new FilterSupport[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(jsonParserFieldName, "maxResults")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "supported")) {
				return false;
			}

			return false;
		}

		@Override
		protected void setField(
			FilterSupport filterSupport, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "maxResults")) {
				if (jsonParserFieldValue != null) {
					filterSupport.setMaxResults(
						Integer.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "supported")) {
				if (jsonParserFieldValue != null) {
					filterSupport.setSupported((Boolean)jsonParserFieldValue);
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

			sb.append(_toJSON(value));

			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}

		sb.append("}");

		return sb.toString();
	}

	private static String _toJSON(Object value) {
		if (value == null) {
			return "null";
		}

		if (value instanceof Map) {
			return _toJSON((Map)value);
		}

		Class<?> clazz = value.getClass();

		if (clazz.isArray()) {
			StringBuilder sb = new StringBuilder("[");

			Object[] values = (Object[])value;

			for (int i = 0; i < values.length; i++) {
				sb.append(_toJSON(values[i]));

				if ((i + 1) < values.length) {
					sb.append(", ");
				}
			}

			sb.append("]");

			return sb.toString();
		}

		if (value instanceof String) {
			return "\"" + _escape(value) + "\"";
		}

		return String.valueOf(value);
	}

}