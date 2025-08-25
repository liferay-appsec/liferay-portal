/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.definition.util;

import com.liferay.object.entry.util.ObjectEntryThreadLocal;
import com.liferay.object.exception.ObjectEntryValidationException;
import com.liferay.object.model.ObjectField;
import com.liferay.petra.lang.CentralizedThreadLocal;
import com.liferay.petra.lang.SafeCloseable;

import java.util.List;

/**
 * @author Shuyang Zhou
 */
public class ObjectDefinitionThreadLocal {

	public static String getObjectDefinitionERC() {
		return _objectDefinitionERC.get();
	}

	public static List<ObjectField> getObjectDefinitionObjectFields() {
		return _objectDefinitionObjectFields.get();
	}

	public static List<ObjectEntryValidationException.ValidationError>
		getValidationErrors() {

		return _validationErrors.get();
	}

	public static void handleAsValidationError(
		Exception exception, String key) {

		ObjectEntryValidationException.ValidationError validationError =
			new ObjectEntryValidationException.ValidationError(
				exception.getMessage(), key,
				exception.getClass(
				).getName());

		List<ObjectEntryValidationException.ValidationError> validationErrors =
			_validationErrors.get();

		if (validationErrors == null) {
			return;
		}

		validationErrors.add(validationError);

		_validationErrors.set(validationErrors);
	}

	public static boolean hasValidationErrors() {
		List<ObjectEntryValidationException.ValidationError> validationErrors =
			_validationErrors.get();

		if (validationErrors == null) {
			return false;
		};

		return !_validationErrors.get(
		).isEmpty();
	}

	public static boolean isDeleteObjectDefinitionId(long objectDefinitionId) {
		Long deleteObjectDefinitionId = _deleteObjectDefinitionId.get();

		if ((deleteObjectDefinitionId != null) &&
			(deleteObjectDefinitionId == objectDefinitionId)) {

			return true;
		}

		return false;
	}

	public static boolean isSkipThrowException() {
		return _skipThrowException.get();
	}

	public static SafeCloseable setDeleteObjectDefinitionIdWithSafeCloseable(
		long id) {

		return _deleteObjectDefinitionId.setWithSafeCloseable(id);
	}

	public static void setObjectDefinitionERC(String externalReferenceCode) {
		_objectDefinitionERC.set(externalReferenceCode);
	}

	public static void setObjectDefinitionObjectFields(
		List<ObjectField> objectDefinitionObjectFields) {

		_objectDefinitionObjectFields.set(objectDefinitionObjectFields);
	}

	public static void setSkipThrowException(boolean skipThrowException) {
		_skipThrowException.set(skipThrowException);
	}

	public static void setValidationErrors(
		List<ObjectEntryValidationException.ValidationError> validationErrors) {

		_validationErrors.set(validationErrors);
	}

	private static final CentralizedThreadLocal<Long>
		_deleteObjectDefinitionId = new CentralizedThreadLocal<>(
			ObjectEntryThreadLocal.class + "._deleteObjectDefinitionId");
	private static final CentralizedThreadLocal<String> _objectDefinitionERC =
		new CentralizedThreadLocal<>(
			ObjectDefinitionThreadLocal.class + "._objectDefinitionERC");
	private static final CentralizedThreadLocal<List<ObjectField>>
		_objectDefinitionObjectFields = new CentralizedThreadLocal<>(
			ObjectDefinitionThreadLocal.class +
				"._objectDefinitionObjectFields");
	private static final CentralizedThreadLocal<Boolean> _skipThrowException =
		new CentralizedThreadLocal<>(
			ObjectDefinitionThreadLocal.class + "._skipThrowException",
			() -> Boolean.FALSE);
	private static final CentralizedThreadLocal
		<List<ObjectEntryValidationException.ValidationError>>
			_validationErrors = new CentralizedThreadLocal<>(
				ObjectDefinitionThreadLocal.class + "._validationErrors");

}