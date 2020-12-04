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

package com.liferay.portal.security.password.service.persistence;

import com.liferay.portal.kernel.service.persistence.BasePersistence;
import com.liferay.portal.security.password.exception.NoSuchHashGeneratorException;
import com.liferay.portal.security.password.model.PasswordHashGenerator;

import java.util.Date;

import org.osgi.annotation.versioning.ProviderType;

/**
 * The persistence interface for the password hash generator service.
 *
 * <p>
 * Caching information and settings can be found in <code>portal.properties</code>
 * </p>
 *
 * @author Arthur Chan
 * @see PasswordHashGeneratorUtil
 * @generated
 */
@ProviderType
public interface PasswordHashGeneratorPersistence
	extends BasePersistence<PasswordHashGenerator> {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this interface directly. Always use {@link PasswordHashGeneratorUtil} to access the password hash generator persistence. Modify <code>service.xml</code> and rerun ServiceBuilder to regenerate this interface.
	 */

	/**
	 * Returns all the password hash generators where uuid = &#63;.
	 *
	 * @param uuid the uuid
	 * @return the matching password hash generators
	 */
	public java.util.List<PasswordHashGenerator> findByUuid(String uuid);

	/**
	 * Returns a range of all the password hash generators where uuid = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordHashGeneratorModelImpl</code>.
	 * </p>
	 *
	 * @param uuid the uuid
	 * @param start the lower bound of the range of password hash generators
	 * @param end the upper bound of the range of password hash generators (not inclusive)
	 * @return the range of matching password hash generators
	 */
	public java.util.List<PasswordHashGenerator> findByUuid(
		String uuid, int start, int end);

	/**
	 * Returns an ordered range of all the password hash generators where uuid = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordHashGeneratorModelImpl</code>.
	 * </p>
	 *
	 * @param uuid the uuid
	 * @param start the lower bound of the range of password hash generators
	 * @param end the upper bound of the range of password hash generators (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching password hash generators
	 */
	public java.util.List<PasswordHashGenerator> findByUuid(
		String uuid, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<PasswordHashGenerator>
			orderByComparator);

	/**
	 * Returns an ordered range of all the password hash generators where uuid = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordHashGeneratorModelImpl</code>.
	 * </p>
	 *
	 * @param uuid the uuid
	 * @param start the lower bound of the range of password hash generators
	 * @param end the upper bound of the range of password hash generators (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching password hash generators
	 */
	public java.util.List<PasswordHashGenerator> findByUuid(
		String uuid, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<PasswordHashGenerator>
			orderByComparator,
		boolean useFinderCache);

	/**
	 * Returns the first password hash generator in the ordered set where uuid = &#63;.
	 *
	 * @param uuid the uuid
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching password hash generator
	 * @throws NoSuchHashGeneratorException if a matching password hash generator could not be found
	 */
	public PasswordHashGenerator findByUuid_First(
			String uuid,
			com.liferay.portal.kernel.util.OrderByComparator
				<PasswordHashGenerator> orderByComparator)
		throws NoSuchHashGeneratorException;

	/**
	 * Returns the first password hash generator in the ordered set where uuid = &#63;.
	 *
	 * @param uuid the uuid
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching password hash generator, or <code>null</code> if a matching password hash generator could not be found
	 */
	public PasswordHashGenerator fetchByUuid_First(
		String uuid,
		com.liferay.portal.kernel.util.OrderByComparator<PasswordHashGenerator>
			orderByComparator);

	/**
	 * Returns the last password hash generator in the ordered set where uuid = &#63;.
	 *
	 * @param uuid the uuid
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching password hash generator
	 * @throws NoSuchHashGeneratorException if a matching password hash generator could not be found
	 */
	public PasswordHashGenerator findByUuid_Last(
			String uuid,
			com.liferay.portal.kernel.util.OrderByComparator
				<PasswordHashGenerator> orderByComparator)
		throws NoSuchHashGeneratorException;

	/**
	 * Returns the last password hash generator in the ordered set where uuid = &#63;.
	 *
	 * @param uuid the uuid
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching password hash generator, or <code>null</code> if a matching password hash generator could not be found
	 */
	public PasswordHashGenerator fetchByUuid_Last(
		String uuid,
		com.liferay.portal.kernel.util.OrderByComparator<PasswordHashGenerator>
			orderByComparator);

	/**
	 * Returns the password hash generators before and after the current password hash generator in the ordered set where uuid = &#63;.
	 *
	 * @param passwordHashGeneratorId the primary key of the current password hash generator
	 * @param uuid the uuid
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next password hash generator
	 * @throws NoSuchHashGeneratorException if a password hash generator with the primary key could not be found
	 */
	public PasswordHashGenerator[] findByUuid_PrevAndNext(
			long passwordHashGeneratorId, String uuid,
			com.liferay.portal.kernel.util.OrderByComparator
				<PasswordHashGenerator> orderByComparator)
		throws NoSuchHashGeneratorException;

	/**
	 * Removes all the password hash generators where uuid = &#63; from the database.
	 *
	 * @param uuid the uuid
	 */
	public void removeByUuid(String uuid);

	/**
	 * Returns the number of password hash generators where uuid = &#63;.
	 *
	 * @param uuid the uuid
	 * @return the number of matching password hash generators
	 */
	public int countByUuid(String uuid);

	/**
	 * Returns all the password hash generators where uuid = &#63; and companyId = &#63;.
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @return the matching password hash generators
	 */
	public java.util.List<PasswordHashGenerator> findByUuid_C(
		String uuid, long companyId);

	/**
	 * Returns a range of all the password hash generators where uuid = &#63; and companyId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordHashGeneratorModelImpl</code>.
	 * </p>
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param start the lower bound of the range of password hash generators
	 * @param end the upper bound of the range of password hash generators (not inclusive)
	 * @return the range of matching password hash generators
	 */
	public java.util.List<PasswordHashGenerator> findByUuid_C(
		String uuid, long companyId, int start, int end);

	/**
	 * Returns an ordered range of all the password hash generators where uuid = &#63; and companyId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordHashGeneratorModelImpl</code>.
	 * </p>
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param start the lower bound of the range of password hash generators
	 * @param end the upper bound of the range of password hash generators (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching password hash generators
	 */
	public java.util.List<PasswordHashGenerator> findByUuid_C(
		String uuid, long companyId, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<PasswordHashGenerator>
			orderByComparator);

	/**
	 * Returns an ordered range of all the password hash generators where uuid = &#63; and companyId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordHashGeneratorModelImpl</code>.
	 * </p>
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param start the lower bound of the range of password hash generators
	 * @param end the upper bound of the range of password hash generators (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching password hash generators
	 */
	public java.util.List<PasswordHashGenerator> findByUuid_C(
		String uuid, long companyId, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<PasswordHashGenerator>
			orderByComparator,
		boolean useFinderCache);

	/**
	 * Returns the first password hash generator in the ordered set where uuid = &#63; and companyId = &#63;.
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching password hash generator
	 * @throws NoSuchHashGeneratorException if a matching password hash generator could not be found
	 */
	public PasswordHashGenerator findByUuid_C_First(
			String uuid, long companyId,
			com.liferay.portal.kernel.util.OrderByComparator
				<PasswordHashGenerator> orderByComparator)
		throws NoSuchHashGeneratorException;

	/**
	 * Returns the first password hash generator in the ordered set where uuid = &#63; and companyId = &#63;.
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching password hash generator, or <code>null</code> if a matching password hash generator could not be found
	 */
	public PasswordHashGenerator fetchByUuid_C_First(
		String uuid, long companyId,
		com.liferay.portal.kernel.util.OrderByComparator<PasswordHashGenerator>
			orderByComparator);

	/**
	 * Returns the last password hash generator in the ordered set where uuid = &#63; and companyId = &#63;.
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching password hash generator
	 * @throws NoSuchHashGeneratorException if a matching password hash generator could not be found
	 */
	public PasswordHashGenerator findByUuid_C_Last(
			String uuid, long companyId,
			com.liferay.portal.kernel.util.OrderByComparator
				<PasswordHashGenerator> orderByComparator)
		throws NoSuchHashGeneratorException;

	/**
	 * Returns the last password hash generator in the ordered set where uuid = &#63; and companyId = &#63;.
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching password hash generator, or <code>null</code> if a matching password hash generator could not be found
	 */
	public PasswordHashGenerator fetchByUuid_C_Last(
		String uuid, long companyId,
		com.liferay.portal.kernel.util.OrderByComparator<PasswordHashGenerator>
			orderByComparator);

	/**
	 * Returns the password hash generators before and after the current password hash generator in the ordered set where uuid = &#63; and companyId = &#63;.
	 *
	 * @param passwordHashGeneratorId the primary key of the current password hash generator
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next password hash generator
	 * @throws NoSuchHashGeneratorException if a password hash generator with the primary key could not be found
	 */
	public PasswordHashGenerator[] findByUuid_C_PrevAndNext(
			long passwordHashGeneratorId, String uuid, long companyId,
			com.liferay.portal.kernel.util.OrderByComparator
				<PasswordHashGenerator> orderByComparator)
		throws NoSuchHashGeneratorException;

	/**
	 * Removes all the password hash generators where uuid = &#63; and companyId = &#63; from the database.
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 */
	public void removeByUuid_C(String uuid, long companyId);

	/**
	 * Returns the number of password hash generators where uuid = &#63; and companyId = &#63;.
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @return the number of matching password hash generators
	 */
	public int countByUuid_C(String uuid, long companyId);

	/**
	 * Returns all the password hash generators where createDate &lt; &#63;.
	 *
	 * @param createDate the create date
	 * @return the matching password hash generators
	 */
	public java.util.List<PasswordHashGenerator> findByLtCreateDate(
		Date createDate);

	/**
	 * Returns a range of all the password hash generators where createDate &lt; &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordHashGeneratorModelImpl</code>.
	 * </p>
	 *
	 * @param createDate the create date
	 * @param start the lower bound of the range of password hash generators
	 * @param end the upper bound of the range of password hash generators (not inclusive)
	 * @return the range of matching password hash generators
	 */
	public java.util.List<PasswordHashGenerator> findByLtCreateDate(
		Date createDate, int start, int end);

	/**
	 * Returns an ordered range of all the password hash generators where createDate &lt; &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordHashGeneratorModelImpl</code>.
	 * </p>
	 *
	 * @param createDate the create date
	 * @param start the lower bound of the range of password hash generators
	 * @param end the upper bound of the range of password hash generators (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching password hash generators
	 */
	public java.util.List<PasswordHashGenerator> findByLtCreateDate(
		Date createDate, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<PasswordHashGenerator>
			orderByComparator);

	/**
	 * Returns an ordered range of all the password hash generators where createDate &lt; &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordHashGeneratorModelImpl</code>.
	 * </p>
	 *
	 * @param createDate the create date
	 * @param start the lower bound of the range of password hash generators
	 * @param end the upper bound of the range of password hash generators (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching password hash generators
	 */
	public java.util.List<PasswordHashGenerator> findByLtCreateDate(
		Date createDate, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<PasswordHashGenerator>
			orderByComparator,
		boolean useFinderCache);

	/**
	 * Returns the first password hash generator in the ordered set where createDate &lt; &#63;.
	 *
	 * @param createDate the create date
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching password hash generator
	 * @throws NoSuchHashGeneratorException if a matching password hash generator could not be found
	 */
	public PasswordHashGenerator findByLtCreateDate_First(
			Date createDate,
			com.liferay.portal.kernel.util.OrderByComparator
				<PasswordHashGenerator> orderByComparator)
		throws NoSuchHashGeneratorException;

	/**
	 * Returns the first password hash generator in the ordered set where createDate &lt; &#63;.
	 *
	 * @param createDate the create date
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching password hash generator, or <code>null</code> if a matching password hash generator could not be found
	 */
	public PasswordHashGenerator fetchByLtCreateDate_First(
		Date createDate,
		com.liferay.portal.kernel.util.OrderByComparator<PasswordHashGenerator>
			orderByComparator);

	/**
	 * Returns the last password hash generator in the ordered set where createDate &lt; &#63;.
	 *
	 * @param createDate the create date
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching password hash generator
	 * @throws NoSuchHashGeneratorException if a matching password hash generator could not be found
	 */
	public PasswordHashGenerator findByLtCreateDate_Last(
			Date createDate,
			com.liferay.portal.kernel.util.OrderByComparator
				<PasswordHashGenerator> orderByComparator)
		throws NoSuchHashGeneratorException;

	/**
	 * Returns the last password hash generator in the ordered set where createDate &lt; &#63;.
	 *
	 * @param createDate the create date
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching password hash generator, or <code>null</code> if a matching password hash generator could not be found
	 */
	public PasswordHashGenerator fetchByLtCreateDate_Last(
		Date createDate,
		com.liferay.portal.kernel.util.OrderByComparator<PasswordHashGenerator>
			orderByComparator);

	/**
	 * Returns the password hash generators before and after the current password hash generator in the ordered set where createDate &lt; &#63;.
	 *
	 * @param passwordHashGeneratorId the primary key of the current password hash generator
	 * @param createDate the create date
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next password hash generator
	 * @throws NoSuchHashGeneratorException if a password hash generator with the primary key could not be found
	 */
	public PasswordHashGenerator[] findByLtCreateDate_PrevAndNext(
			long passwordHashGeneratorId, Date createDate,
			com.liferay.portal.kernel.util.OrderByComparator
				<PasswordHashGenerator> orderByComparator)
		throws NoSuchHashGeneratorException;

	/**
	 * Removes all the password hash generators where createDate &lt; &#63; from the database.
	 *
	 * @param createDate the create date
	 */
	public void removeByLtCreateDate(Date createDate);

	/**
	 * Returns the number of password hash generators where createDate &lt; &#63;.
	 *
	 * @param createDate the create date
	 * @return the number of matching password hash generators
	 */
	public int countByLtCreateDate(Date createDate);

	/**
	 * Caches the password hash generator in the entity cache if it is enabled.
	 *
	 * @param passwordHashGenerator the password hash generator
	 */
	public void cacheResult(PasswordHashGenerator passwordHashGenerator);

	/**
	 * Caches the password hash generators in the entity cache if it is enabled.
	 *
	 * @param passwordHashGenerators the password hash generators
	 */
	public void cacheResult(
		java.util.List<PasswordHashGenerator> passwordHashGenerators);

	/**
	 * Creates a new password hash generator with the primary key. Does not add the password hash generator to the database.
	 *
	 * @param passwordHashGeneratorId the primary key for the new password hash generator
	 * @return the new password hash generator
	 */
	public PasswordHashGenerator create(long passwordHashGeneratorId);

	/**
	 * Removes the password hash generator with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param passwordHashGeneratorId the primary key of the password hash generator
	 * @return the password hash generator that was removed
	 * @throws NoSuchHashGeneratorException if a password hash generator with the primary key could not be found
	 */
	public PasswordHashGenerator remove(long passwordHashGeneratorId)
		throws NoSuchHashGeneratorException;

	public PasswordHashGenerator updateImpl(
		PasswordHashGenerator passwordHashGenerator);

	/**
	 * Returns the password hash generator with the primary key or throws a <code>NoSuchHashGeneratorException</code> if it could not be found.
	 *
	 * @param passwordHashGeneratorId the primary key of the password hash generator
	 * @return the password hash generator
	 * @throws NoSuchHashGeneratorException if a password hash generator with the primary key could not be found
	 */
	public PasswordHashGenerator findByPrimaryKey(long passwordHashGeneratorId)
		throws NoSuchHashGeneratorException;

	/**
	 * Returns the password hash generator with the primary key or returns <code>null</code> if it could not be found.
	 *
	 * @param passwordHashGeneratorId the primary key of the password hash generator
	 * @return the password hash generator, or <code>null</code> if a password hash generator with the primary key could not be found
	 */
	public PasswordHashGenerator fetchByPrimaryKey(
		long passwordHashGeneratorId);

	/**
	 * Returns all the password hash generators.
	 *
	 * @return the password hash generators
	 */
	public java.util.List<PasswordHashGenerator> findAll();

	/**
	 * Returns a range of all the password hash generators.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordHashGeneratorModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of password hash generators
	 * @param end the upper bound of the range of password hash generators (not inclusive)
	 * @return the range of password hash generators
	 */
	public java.util.List<PasswordHashGenerator> findAll(int start, int end);

	/**
	 * Returns an ordered range of all the password hash generators.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordHashGeneratorModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of password hash generators
	 * @param end the upper bound of the range of password hash generators (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of password hash generators
	 */
	public java.util.List<PasswordHashGenerator> findAll(
		int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<PasswordHashGenerator>
			orderByComparator);

	/**
	 * Returns an ordered range of all the password hash generators.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordHashGeneratorModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of password hash generators
	 * @param end the upper bound of the range of password hash generators (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of password hash generators
	 */
	public java.util.List<PasswordHashGenerator> findAll(
		int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<PasswordHashGenerator>
			orderByComparator,
		boolean useFinderCache);

	/**
	 * Removes all the password hash generators from the database.
	 */
	public void removeAll();

	/**
	 * Returns the number of password hash generators.
	 *
	 * @return the number of password hash generators
	 */
	public int countAll();

}