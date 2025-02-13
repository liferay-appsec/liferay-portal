/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.scim.rest.dto.v1_0;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import com.liferay.petra.function.UnsafeSupplier;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLField;
import com.liferay.portal.vulcan.graphql.annotation.GraphQLName;
import com.liferay.portal.vulcan.util.ObjectMapperUtil;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Generated;

import javax.validation.Valid;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Olivér Kecskeméty
 * @generated
 */
@Generated("")
@GraphQLName("Operation")
@JsonFilter("Liferay.Vulcan")
@XmlRootElement(name = "Operation")
public class Operation implements Serializable {

	public static Operation toDTO(String json) {
		return ObjectMapperUtil.readValue(Operation.class, json);
	}

	public static Operation unsafeToDTO(String json) {
		return ObjectMapperUtil.unsafeReadValue(Operation.class, json);
	}

	@JsonGetter("op")
	@Schema(
		description = "The method that should be used in the operation. Possible values add remove replace"
	)
	@Valid
	public Op getOp() {
		if (_opSupplier != null) {
			op = _opSupplier.get();

			_opSupplier = null;
		}

		return op;
	}

	@JsonIgnore
	public String getOpAsString() {
		Op op = getOp();

		if (op == null) {
			return null;
		}

		return op.toString();
	}

	public void setOp(Op op) {
		this.op = op;

		_opSupplier = null;
	}

	@JsonIgnore
	public void setOp(UnsafeSupplier<Op, Exception> opUnsafeSupplier) {
		_opSupplier = () -> {
			try {
				return opUnsafeSupplier.get();
			}
			catch (RuntimeException runtimeException) {
				throw runtimeException;
			}
			catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		};
	}

	@GraphQLField(
		description = "The method that should be used in the operation. Possible values add remove replace"
	)
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected Op op;

	@JsonIgnore
	private Supplier<Op> _opSupplier;

	@Schema(
		description = "Add the path to specify the attribute/sub-attribute that should be updated."
	)
	public String getPath() {
		if (_pathSupplier != null) {
			path = _pathSupplier.get();

			_pathSupplier = null;
		}

		return path;
	}

	public void setPath(String path) {
		this.path = path;

		_pathSupplier = null;
	}

	@JsonIgnore
	public void setPath(UnsafeSupplier<String, Exception> pathUnsafeSupplier) {
		_pathSupplier = () -> {
			try {
				return pathUnsafeSupplier.get();
			}
			catch (RuntimeException runtimeException) {
				throw runtimeException;
			}
			catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		};
	}

	@GraphQLField(
		description = "Add the path to specify the attribute/sub-attribute that should be updated."
	)
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected String path;

	@JsonIgnore
	private Supplier<String> _pathSupplier;

	@Schema(description = "The value that should be updated.")
	public String getValue() {
		if (_valueSupplier != null) {
			value = _valueSupplier.get();

			_valueSupplier = null;
		}

		return value;
	}

	public void setValue(String value) {
		this.value = value;

		_valueSupplier = null;
	}

	@JsonIgnore
	public void setValue(
		UnsafeSupplier<String, Exception> valueUnsafeSupplier) {

		_valueSupplier = () -> {
			try {
				return valueUnsafeSupplier.get();
			}
			catch (RuntimeException runtimeException) {
				throw runtimeException;
			}
			catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		};
	}

	@GraphQLField(description = "The value that should be updated.")
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	protected String value;

	@JsonIgnore
	private Supplier<String> _valueSupplier;

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof Operation)) {
			return false;
		}

		Operation operation = (Operation)object;

		return Objects.equals(toString(), operation.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		StringBundler sb = new StringBundler();

		sb.append("{");

		Op op = getOp();

		if (op != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"op\": ");

			sb.append("\"");

			sb.append(op);

			sb.append("\"");
		}

		String path = getPath();

		if (path != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"path\": ");

			sb.append("\"");

			sb.append(_escape(path));

			sb.append("\"");
		}

		String value = getValue();

		if (value != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"value\": ");

			sb.append("\"");

			sb.append(_escape(value));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	@Schema(
		accessMode = Schema.AccessMode.READ_ONLY,
		defaultValue = "com.liferay.scim.rest.dto.v1_0.Operation",
		name = "x-class-name"
	)
	public String xClassName;

	@GraphQLName("Op")
	public static enum Op {

		ADD("add"), REMOVE("remove"), REPLACE("replace");

		@JsonCreator
		public static Op create(String value) {
			if ((value == null) || value.equals("")) {
				return null;
			}

			for (Op op : values()) {
				if (Objects.equals(op.getValue(), value)) {
					return op;
				}
			}

			throw new IllegalArgumentException("Invalid enum value: " + value);
		}

		@JsonValue
		public String getValue() {
			return _value;
		}

		@Override
		public String toString() {
			return _value;
		}

		private Op(String value) {
			_value = value;
		}

		private final String _value;

	}

	private static String _escape(Object object) {
		return StringUtil.replace(
			String.valueOf(object), _JSON_ESCAPE_STRINGS[0],
			_JSON_ESCAPE_STRINGS[1]);
	}

	private static boolean _isArray(Object value) {
		if (value == null) {
			return false;
		}

		Class<?> clazz = value.getClass();

		return clazz.isArray();
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
			sb.append(_escape(entry.getKey()));
			sb.append("\": ");

			Object value = entry.getValue();

			if (_isArray(value)) {
				sb.append("[");

				Object[] valueArray = (Object[])value;

				for (int i = 0; i < valueArray.length; i++) {
					if (valueArray[i] instanceof Map) {
						sb.append(_toJSON((Map<String, ?>)valueArray[i]));
					}
					else if (valueArray[i] instanceof String) {
						sb.append("\"");
						sb.append(valueArray[i]);
						sb.append("\"");
					}
					else {
						sb.append(valueArray[i]);
					}

					if ((i + 1) < valueArray.length) {
						sb.append(", ");
					}
				}

				sb.append("]");
			}
			else if (value instanceof Map) {
				sb.append(_toJSON((Map<String, ?>)value));
			}
			else if (value instanceof String) {
				sb.append("\"");
				sb.append(_escape(value));
				sb.append("\"");
			}
			else {
				sb.append(value);
			}

			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}

		sb.append("}");

		return sb.toString();
	}

	private static final String[][] _JSON_ESCAPE_STRINGS = {
		{"\\", "\"", "\b", "\f", "\n", "\r", "\t"},
		{"\\\\", "\\\"", "\\b", "\\f", "\\n", "\\r", "\\t"}
	};

	private Map<String, Serializable> _extendedProperties;

}