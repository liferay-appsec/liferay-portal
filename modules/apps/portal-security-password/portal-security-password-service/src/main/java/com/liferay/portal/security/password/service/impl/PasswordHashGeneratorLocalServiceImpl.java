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

package com.liferay.portal.security.password.service.impl;

import com.liferay.portal.aop.AopService;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.crypto.hash.generation.context.HashGenerationContext;
import com.liferay.portal.crypto.hash.generation.context.salt.SaltCommand;
import com.liferay.portal.crypto.hash.processor.HashProcessor;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.HashMapDictionary;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.security.password.hash.generator.configuration.PasswordHashGeneratorConfiguration;
import com.liferay.portal.security.password.model.PasswordHashGenerator;
import com.liferay.portal.security.password.model.PasswordMeta;
import com.liferay.portal.security.password.service.base.PasswordHashGeneratorLocalServiceBaseImpl;
import com.liferay.portal.security.password.service.persistence.PasswordMetaPersistence;

import java.util.Date;
import java.util.Map;

import org.json.JSONObject;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * The implementation of the hash algorithm entry local service.
 *
 * <p>
 * All custom service methods should be put in this class. Whenever methods are added, rerun ServiceBuilder to copy their definitions into the <code>PasswordHashGeneratorLocalService</code> interface.
 *
 * <p>
 * This is a local service. Methods of this service will not have security checks based on the propagated JAAS credentials because this service can only be accessed from within the same VM.
 * </p>
 *
 * @author Arthur Chan
 * @see PasswordHashGeneratorLocalServiceBaseImpl
 */
@Component(
	configurationPid = "com.liferay.portal.security.password.hash.generator.configuration.PasswordHashGeneratorConfiguration",
	property = "model.class.name=com.liferay.portal.security.password.model.PasswordHashGenerator",
	service = AopService.class
)
public class PasswordHashGeneratorLocalServiceImpl
	extends PasswordHashGeneratorLocalServiceBaseImpl {

	@Override
	public PasswordHashGenerator addPasswordHashGenerator(
		PasswordHashGenerator passwordHashGenerator) {

		try {
			return _addPasswordHashGenerator(
				passwordHashGenerator.getHashGeneratorName(),
				passwordHashGenerator.getHashGeneratorMeta());
		}
		catch (Exception exception) {
			_log.error(exception, exception);

			return null;
		}
	}

	/**
	 * Get the latest password hash generator ordered by created date
	 *
	 * @return the latest password hash generator ordered by created date
	 */
	@Override
	public PasswordHashGenerator getLastPasswordHashGenerator()
		throws PortalException {

		// OrderByComparator can be null, because nature order is
		// set to ascending by create date in service.xml

		return passwordHashGeneratorPersistence.findByLtCreateDate_Last(
			new Date(), null);
	}

	/**
	 * Get the latest password hash generator before a date, ordered by created date
	 *
	 * @param date the given date
	 * @return the latest password hash generator before a date,  ordered by created date
	 */
	@Override
	public PasswordHashGenerator getLastPasswordHashGeneratorBeforeDate(
			Date date)
		throws PortalException {

		// OrderByComparator can be null, because nature order is
		// set to ascending by create date in service.xml

		return passwordHashGeneratorPersistence.findByLtCreateDate_Last(
			date, null);
	}

	/**
	 * Whenever there is change to password hash generator configuration, we need to
	 * store it to the database.
	 * However we shall not add a configuration that is same as last one, for example
	 * because of component re-deployment or simply a same password hash generator
	 * is being configured.
	 * Also when adding a password hash generator to database, we need to delete
	 * previous password Hash Generator entry if it is not used.
	 *
	 * @param properties the properties
	 */
	@Activate
	protected void activate(
		BundleContext bundleContext, Map<String, Object> properties) {

		PasswordHashGeneratorConfiguration passwordHashGeneratorConfiguration =
			ConfigurableUtil.createConfigurable(
				PasswordHashGeneratorConfiguration.class, properties);

		try {
			PasswordHashGenerator passwordHashGenerator =
				_addPasswordHashGenerator(
					passwordHashGeneratorConfiguration.hashGeneratorName(),
					passwordHashGeneratorConfiguration.hashGeneratorMetaJSON());

			HashGenerationContext.Builder hashGenerationContextBuilder =
				_hashProcessor.createHashGenerationContextBuilder(
					passwordHashGenerator.getHashGeneratorName(),
					new JSONObject(
						passwordHashGenerator.getHashGeneratorMeta()));

			HashGenerationContext hashGenerationContext =
				hashGenerationContextBuilder.pepperApp(
					_pepperAppId
					).build(
					SaltCommand.generateDefaultSizeSalt());

			_serviceRegistration = bundleContext.registerService(
				HashGenerationContext.class, hashGenerationContext,
				new HashMapDictionary<>(properties));
		}
		catch (Exception exception) {
			_log.error(exception, exception);
		}
	}

	@Deactivate
	protected void deactivate() {
		_serviceRegistration.unregister();
	}

	/**
	 * Invoked whenever a change to Password Hash Generator configuration
	 *
	 * @param  name the password hash generator name
	 * @param  metaJSON the password hash generator meta in json format
	 * @return PasswordHashGenerator
	 */
	private PasswordHashGenerator _addPasswordHashGenerator(
			String name, String metaJSON)
		throws Exception {

		if (Validator.isNull(name)) {
			throw new PortalException(
				"Password hash generator name can not be empty");
		}

		JSONObject meta = null;

		if (Validator.isNull(metaJSON)) {
			meta = new JSONObject();
		}
		else {
			meta = new JSONObject(metaJSON);
		}

		PasswordHashGenerator lastPasswordHashGenerator =
			passwordHashGeneratorPersistence.fetchByLtCreateDate_Last(
				new Date(), null);

		if (lastPasswordHashGenerator == null) {
			return _addPasswordHashGeneratorCommon(name, meta);
		}

		if (!name.equals(lastPasswordHashGenerator.getHashGeneratorMeta())) {
			_deleteLastHashGeneratorIfNotUsed();

			return _addPasswordHashGeneratorCommon(name, meta);
		}

		JSONObject storedHashGeneratorMeta = new JSONObject(
			lastPasswordHashGenerator.getHashGeneratorMeta());

		if (!meta.similar(storedHashGeneratorMeta)) {
			_deleteLastHashGeneratorIfNotUsed();

			return _addPasswordHashGeneratorCommon(name, meta);
		}

		throw new PortalException(
			"The last Password Hash Generator has same configuration.");
	}

	/**
	 * Invoked whenever a change to Password Hash Generator configuration
	 *
	 * @param  name the password hash generator name
	 * @param  meta the password hash generator meta
	 * @return PasswordHashGenerator
	 */
	private PasswordHashGenerator _addPasswordHashGeneratorCommon(
		String name, JSONObject meta) {

		long passwordHashGeneratorId = counterLocalService.increment(
			PasswordHashGenerator.class.getName());

		PasswordHashGenerator passwordHashGenerator =
			passwordHashGeneratorPersistence.create(passwordHashGeneratorId);

		passwordHashGenerator.setHashGeneratorName(name);

		passwordHashGenerator.setHashGeneratorMeta(meta.toString());

		passwordHashGenerator.setNew(true);

		return passwordHashGeneratorPersistence.update(passwordHashGenerator);
	}

	/**
	 * Invoked when adding a new Password Hash Generator, we have to
	 * remove previous hash generator that is not being used, because
	 * only the last/current one will be used for generating passwordMeta
	 */
	private void _deleteLastHashGeneratorIfNotUsed() {
		PasswordHashGenerator lastPasswordHashGenerator =
			passwordHashGeneratorPersistence.fetchByLtCreateDate_Last(
				new Date(), null);

		if (lastPasswordHashGenerator == null) {
			return;
		}

		PasswordMeta passwordMeta =
			_passwordMetaPersistence.fetchByLtCreateDate_Last(new Date(), null);

		// No passwordMeta was created, meaning last passwordHashGenerator
		// is not being used

		if (passwordMeta == null) {
			passwordHashGeneratorPersistence.remove(lastPasswordHashGenerator);
		}

		Date lastPasswordHashGeneratorCreateDate =
			lastPasswordHashGenerator.getCreateDate();

		Date passwordMetaCreateDate = passwordMeta.getCreateDate();

		// Last passwordHashGenerator is not used

		if (lastPasswordHashGeneratorCreateDate.after(passwordMetaCreateDate)) {
			passwordHashGeneratorPersistence.remove(lastPasswordHashGenerator);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		PasswordHashGeneratorLocalServiceImpl.class);

	@Reference
	private HashProcessor _hashProcessor;

	@Reference
	private PasswordMetaPersistence _passwordMetaPersistence;

	private static final String _pepperAppId =
		"com.liferay.portal.security.password.service";

	private ServiceRegistration<HashGenerationContext> _serviceRegistration;

}