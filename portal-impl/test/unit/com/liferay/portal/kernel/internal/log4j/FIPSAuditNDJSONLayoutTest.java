/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.internal.log4j;

import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.CodeCoverageAssertor;
import com.liferay.portal.kernel.util.LinkedHashMapBuilder;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.io.ByteArrayOutputStream;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.layout.ByteBufferDestination;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ObjectMessage;
import org.apache.logging.log4j.message.SimpleMessage;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Rafael Praxedes
 */
public class FIPSAuditNDJSONLayoutTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			CodeCoverageAssertor.INSTANCE, LiferayUnitTestRule.INSTANCE);

	@Test
	public void testEncode() {
		Map<String, Object> record = LinkedHashMapBuilder.<String, Object>put(
			"severity", "critical"
		).put(
			"event-type", "fips-state-transition"
		).build();

		FIPSAuditNDJSONLayout fipsAuditNDJSONLayout = _newLayout();

		TestByteBufferDestination testByteBufferDestination =
			new TestByteBufferDestination();

		fipsAuditNDJSONLayout.encode(
			_newLogEvent(new ObjectMessage(record)), testByteBufferDestination);

		Assert.assertEquals(
			_toSerializable(record), testByteBufferDestination.toString());
	}

	@Test
	public void testGetContentType() {
		FIPSAuditNDJSONLayout fipsAuditNDJSONLayout = _newLayout();

		Assert.assertEquals(
			"application/x-ndjson; charset=UTF-8",
			fipsAuditNDJSONLayout.getContentType());
	}

	@Test
	public void testToSerializable() {
		Assert.assertEquals(
			"{\"event-type\":\"fips-state-transition\",\"severity\":\"info\"," +
				"\"timestamp\":\"2026-08-04T12:00:00.000Z\"}\n",
			_toSerializable(
				LinkedHashMapBuilder.<String, Object>put(
					"timestamp", "2026-08-04T12:00:00.000Z"
				).put(
					"severity", "info"
				).put(
					"event-type", "fips-state-transition"
				).build()));
	}

	@Test
	public void testToSerializableEndsWithASingleNewLine() {
		String ndjson = _toSerializable(
			LinkedHashMapBuilder.<String, Object>put(
				"event-type", "fips-state-transition"
			).build());

		Assert.assertEquals(
			"{\"event-type\":\"fips-state-transition\"}\n", ndjson);
		Assert.assertEquals(ndjson.length() - 1, ndjson.indexOf('\n'));
	}

	@Test
	public void testToSerializableEscapesReservedCharacters() {
		Assert.assertEquals(
			"{\"provider-error-message\":\"a\\\\b\\\"c\\nd\\re\\tf\\u0001g" +
				"\\u2028h\\u2029i\"}\n",
			_toSerializable(
				LinkedHashMapBuilder.<String, Object>put(
					"provider-error-message",
					new String(
						new char[] {
							'a', '\\', 'b', '"', 'c', '\n', 'd', '\r', 'e',
							'\t', 'f', 0x01, 'g', 0x2028, 'h', 0x2029, 'i'
						})
				).build()));
	}

	@Test
	public void testToSerializableSortsKeysRecursively() {
		Assert.assertEquals(
			"{\"event-type\":\"fips-state-transition\",\"fields\":" +
				"{\"from-state\":\"SELF_TEST\",\"to-state\":\"OPERATIONAL\"}," +
					"\"severity\":\"info\"}\n",
			_toSerializable(
				LinkedHashMapBuilder.<String, Object>put(
					"severity", "info"
				).put(
					"fields",
					LinkedHashMapBuilder.<String, Object>put(
						"to-state", "OPERATIONAL"
					).put(
						"from-state", "SELF_TEST"
					).build()
				).put(
					"event-type", "fips-state-transition"
				).build()));
	}

	@Test
	public void testToSerializableWritesMessageKeyForUnreadableMessages() {
		FIPSAuditNDJSONLayout fipsAuditNDJSONLayout = _newLayout();

		Assert.assertEquals(
			"{\"message\":\"Unable to \\\"parse\\\" the record\"}\n",
			fipsAuditNDJSONLayout.toSerializable(
				_newLogEvent(
					new SimpleMessage("Unable to \"parse\" the record"))));

		Assert.assertEquals(
			"{\"message\":\"not-a-map\"}\n",
			fipsAuditNDJSONLayout.toSerializable(
				_newLogEvent(new ObjectMessage("not-a-map"))));
	}

	@Test
	public void testToSerializableWritesNonfiniteNumbersAsStrings() {
		Assert.assertEquals(
			"{\"double\":\"NaN\",\"float\":\"-Infinity\"}\n",
			_toSerializable(
				LinkedHashMapBuilder.<String, Object>put(
					"double", Double.NaN
				).put(
					"float", Float.NEGATIVE_INFINITY
				).build()));
	}

	@Test
	public void testToSerializableWritesNullValues() {
		Assert.assertEquals(
			"{\"provider-name\":null}\n",
			_toSerializable(Collections.singletonMap("provider-name", null)));
	}

	@Test
	public void testToSerializableWritesValueTypes() {
		Assert.assertEquals(
			"{\"array\":[\"first\",\"second\"],\"boolean\":true," +
				"\"decimal\":1.5,\"iterable\":[\"one\",2],\"number\":42," +
					"\"string\":\"text\"}\n",
			_toSerializable(
				LinkedHashMapBuilder.<String, Object>put(
					"array", new String[] {"first", "second"}
				).put(
					"boolean", Boolean.TRUE
				).put(
					"decimal", 1.5D
				).put(
					"iterable", Arrays.asList("one", 2)
				).put(
					"number", 42
				).put(
					"string", "text"
				).build()));
	}

	private FIPSAuditNDJSONLayout _newLayout() {
		FIPSAuditNDJSONLayout.Builder builder =
			FIPSAuditNDJSONLayout.newBuilder();

		return builder.build();
	}

	private LogEvent _newLogEvent(Message message) {
		Log4jLogEvent.Builder builder = Log4jLogEvent.newBuilder();

		builder.setLevel(Level.INFO);
		builder.setLoggerName("liferay.fips.audit");
		builder.setMessage(message);

		return builder.build();
	}

	private String _toSerializable(Map<String, Object> record) {
		FIPSAuditNDJSONLayout fipsAuditNDJSONLayout = _newLayout();

		return fipsAuditNDJSONLayout.toSerializable(
			_newLogEvent(new ObjectMessage(record)));
	}

	private static class TestByteBufferDestination
		implements ByteBufferDestination {

		@Override
		public ByteBuffer drain(ByteBuffer byteBuffer) {
			byteBuffer.flip();

			writeBytes(byteBuffer);

			byteBuffer.clear();

			return byteBuffer;
		}

		@Override
		public ByteBuffer getByteBuffer() {
			return _byteBuffer;
		}

		@Override
		public String toString() {
			drain(_byteBuffer);

			byte[] bytes = _byteArrayOutputStream.toByteArray();

			return new String(bytes, StandardCharsets.UTF_8);
		}

		@Override
		public void writeBytes(byte[] bytes, int offset, int length) {
			_byteArrayOutputStream.write(bytes, offset, length);
		}

		@Override
		public void writeBytes(ByteBuffer byteBuffer) {
			byte[] bytes = new byte[byteBuffer.remaining()];

			byteBuffer.get(bytes);

			writeBytes(bytes, 0, bytes.length);
		}

		private final ByteArrayOutputStream _byteArrayOutputStream =
			new ByteArrayOutputStream();
		private final ByteBuffer _byteBuffer = ByteBuffer.allocate(4096);

	}

}