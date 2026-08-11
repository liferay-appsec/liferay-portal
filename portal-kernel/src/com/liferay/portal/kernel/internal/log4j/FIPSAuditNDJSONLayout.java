/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.internal.log4j;

import com.liferay.petra.string.CharPool;

import java.lang.reflect.Array;

import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.layout.ByteBufferDestination;
import org.apache.logging.log4j.core.layout.Encoder;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ObjectMessage;

/**
 * @author Rafael Praxedes
 * @see    LiferayXmlLayout
 */
@Plugin(
	category = Node.CATEGORY, elementType = Layout.ELEMENT_TYPE,
	name = FIPSAuditNDJSONLayout.PLUGIN_NAME, printObject = true
)
public final class FIPSAuditNDJSONLayout extends AbstractStringLayout {

	public static final String PLUGIN_NAME = "FIPSAuditNDJSONLayout";

	@PluginBuilderFactory
	public static Builder newBuilder() {
		return new Builder();
	}

	@Override
	public void encode(
		LogEvent logEvent, ByteBufferDestination byteBufferDestination) {

		StringBuilder sb = getStringBuilder();

		_generateNDJSON(logEvent, sb);

		Encoder<StringBuilder> encoder = getStringBuilderEncoder();

		encoder.encode(sb, byteBufferDestination);
	}

	@Override
	public String getContentType() {
		return "application/x-ndjson; charset=UTF-8";
	}

	@Override
	public String toSerializable(LogEvent logEvent) {
		StringBuilder sb = getStringBuilder();

		_generateNDJSON(logEvent, sb);

		return sb.toString();
	}

	public static class Builder
		implements org.apache.logging.log4j.core.util.Builder
			<FIPSAuditNDJSONLayout> {

		@Override
		public FIPSAuditNDJSONLayout build() {
			return new FIPSAuditNDJSONLayout();
		}

	}

	private FIPSAuditNDJSONLayout() {
		super(StandardCharsets.UTF_8);
	}

	private void _appendJSONArray(StringBuilder sb, Iterable<?> iterable) {
		sb.append(CharPool.OPEN_BRACKET);

		boolean first = true;

		for (Object value : iterable) {
			if (first) {
				first = false;
			}
			else {
				sb.append(CharPool.COMMA);
			}

			_appendJSONValue(sb, value);
		}

		sb.append(CharPool.CLOSE_BRACKET);
	}

	private void _appendJSONNumber(StringBuilder sb, Number number) {
		if (number instanceof Double || number instanceof Float) {
			double doubleValue = number.doubleValue();

			if (Double.isInfinite(doubleValue) || Double.isNaN(doubleValue)) {
				_appendJSONString(sb, number.toString());

				return;
			}
		}

		sb.append(number);
	}

	private void _appendJSONObject(StringBuilder sb, Map<?, ?> map) {
		sb.append(CharPool.OPEN_CURLY_BRACE);

		Map<String, Object> sortedMap = new TreeMap<>();

		for (Map.Entry<?, ?> entry : map.entrySet()) {
			sortedMap.put(String.valueOf(entry.getKey()), entry.getValue());
		}

		boolean first = true;

		for (Map.Entry<String, Object> entry : sortedMap.entrySet()) {
			if (first) {
				first = false;
			}
			else {
				sb.append(CharPool.COMMA);
			}

			_appendJSONString(sb, entry.getKey());

			sb.append(CharPool.COLON);

			_appendJSONValue(sb, entry.getValue());
		}

		sb.append(CharPool.CLOSE_CURLY_BRACE);
	}

	private void _appendJSONString(StringBuilder sb, String string) {
		sb.append(CharPool.QUOTE);

		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);

			if (c == CharPool.BACK_SLASH) {
				sb.append("\\\\");
			}
			else if (c == CharPool.QUOTE) {
				sb.append("\\\"");
			}
			else if (c == CharPool.NEW_LINE) {
				sb.append("\\n");
			}
			else if (c == CharPool.RETURN) {
				sb.append("\\r");
			}
			else if (c == CharPool.TAB) {
				sb.append("\\t");
			}
			else if ((c < 0x20) || (c == _LINE_SEPARATOR) ||
					 (c == _PARAGRAPH_SEPARATOR)) {

				_appendUnicodeEscape(sb, c);
			}
			else {
				sb.append(c);
			}
		}

		sb.append(CharPool.QUOTE);
	}

	private void _appendJSONValue(StringBuilder sb, Object value) {
		if (value == null) {
			sb.append("null");

			return;
		}

		if (value instanceof Boolean) {
			sb.append(value);

			return;
		}

		if (value instanceof Number) {
			_appendJSONNumber(sb, (Number)value);

			return;
		}

		if (value instanceof Map) {
			_appendJSONObject(sb, (Map<?, ?>)value);

			return;
		}

		if (value instanceof Iterable) {
			_appendJSONArray(sb, (Iterable<?>)value);

			return;
		}

		Class<?> clazz = value.getClass();

		if (clazz.isArray()) {
			_appendJSONArray(sb, _toList(value));

			return;
		}

		_appendJSONString(sb, value.toString());
	}

	private void _appendUnicodeEscape(StringBuilder sb, char c) {
		sb.append("\\u");
		sb.append(_HEX_CHARS[(c >> 12) & 0x0f]);
		sb.append(_HEX_CHARS[(c >> 8) & 0x0f]);
		sb.append(_HEX_CHARS[(c >> 4) & 0x0f]);
		sb.append(_HEX_CHARS[c & 0x0f]);
	}

	private void _generateNDJSON(LogEvent logEvent, StringBuilder sb) {
		Message message = logEvent.getMessage();

		Object record = null;

		if (message instanceof ObjectMessage) {
			ObjectMessage objectMessage = (ObjectMessage)message;

			record = objectMessage.getParameter();
		}

		if (record instanceof Map) {
			_appendJSONObject(sb, (Map<?, ?>)record);
		}
		else {
			sb.append(CharPool.OPEN_CURLY_BRACE);

			_appendJSONString(sb, "message");

			sb.append(CharPool.COLON);

			_appendJSONString(sb, message.getFormattedMessage());

			sb.append(CharPool.CLOSE_CURLY_BRACE);
		}

		sb.append(CharPool.NEW_LINE);
	}

	private List<Object> _toList(Object array) {
		int length = Array.getLength(array);

		List<Object> values = new ArrayList<>(length);

		for (int i = 0; i < length; i++) {
			values.add(Array.get(array, i));
		}

		return values;
	}

	private static final char[] _HEX_CHARS = "0123456789abcdef".toCharArray();

	private static final char _LINE_SEPARATOR = 0x2028;

	private static final char _PARAGRAPH_SEPARATOR = 0x2029;

}