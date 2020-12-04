/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.security.password.model;

import com.liferay.portal.kernel.annotation.ImplementationClassName;
import com.liferay.portal.kernel.model.PersistedModel;
import com.liferay.portal.kernel.util.Accessor;

import org.osgi.annotation.versioning.ProviderType;

/**
 * The extended model interface for the PasswordHashGenerator service. Represents a row in the &quot;PasswordHashGenerator&quot; database table, with each column mapped to a property of this class.
 *
 * @author Arthur Chan
 * @see PasswordHashGeneratorModel
 * @generated
 */
@ImplementationClassName(
	"com.liferay.portal.security.password.model.impl.PasswordHashGeneratorImpl"
)
@ProviderType
public interface PasswordHashGenerator
	extends PasswordHashGeneratorModel, PersistedModel {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify this interface directly. Add methods to <code>com.liferay.portal.security.password.model.impl.PasswordHashGeneratorImpl</code> and rerun ServiceBuilder to automatically copy the method declarations to this interface.
	 */
	public static final Accessor<PasswordHashGenerator, Long>
		PASSWORD_HASH_GENERATOR_ID_ACCESSOR =
			new Accessor<PasswordHashGenerator, Long>() {

				@Override
				public Long get(PasswordHashGenerator passwordHashGenerator) {
					return passwordHashGenerator.getPasswordHashGeneratorId();
				}

				@Override
				public Class<Long> getAttributeClass() {
					return Long.class;
				}

				@Override
				public Class<PasswordHashGenerator> getTypeClass() {
					return PasswordHashGenerator.class;
				}

			};

}