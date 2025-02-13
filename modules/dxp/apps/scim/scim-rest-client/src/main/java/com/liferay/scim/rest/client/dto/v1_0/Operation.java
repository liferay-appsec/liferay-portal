/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.scim.rest.client.dto.v1_0;

import com.liferay.scim.rest.client.function.UnsafeSupplier;
import com.liferay.scim.rest.client.serdes.v1_0.OperationSerDes;

import java.io.Serializable;

import java.util.Objects;

import javax.annotation.Generated;

/**
 * @author Olivér Kecskeméty
 * @generated
 */
@Generated("")
public class Operation implements Cloneable, Serializable {

	public static Operation toDTO(String json) {
		return OperationSerDes.toDTO(json);
	}

	public Op getOp() {
		return op;
	}

	public String getOpAsString() {
		if (op == null) {
			return null;
		}

		return op.toString();
	}

	public void setOp(Op op) {
		this.op = op;
	}

	public void setOp(UnsafeSupplier<Op, Exception> opUnsafeSupplier) {
		try {
			op = opUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Op op;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setPath(UnsafeSupplier<String, Exception> pathUnsafeSupplier) {
		try {
			path = pathUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String path;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setValue(
		UnsafeSupplier<String, Exception> valueUnsafeSupplier) {

		try {
			value = valueUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String value;

	@Override
	public Operation clone() throws CloneNotSupportedException {
		return (Operation)super.clone();
	}

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
		return OperationSerDes.toJSON(this);
	}

	public static enum Op {

		ADD("add"), REMOVE("remove"), REPLACE("replace");

		public static Op create(String value) {
			for (Op op : values()) {
				if (Objects.equals(op.getValue(), value) ||
					Objects.equals(op.name(), value)) {

					return op;
				}
			}

			return null;
		}

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

}