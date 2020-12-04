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
import com.liferay.portal.security.password.exception.NoSuchMetaException;
import com.liferay.portal.security.password.model.PasswordMeta;

import java.util.Date;

import org.osgi.annotation.versioning.ProviderType;

/**
 * The persistence interface for the password meta service.
 *
 * <p>
 * Caching information and settings can be found in <code>portal.properties</code>
 * </p>
 *
 * @author Arthur Chan
 * @see PasswordMetaUtil
 * @generated
 */
@ProviderType
public interface PasswordMetaPersistence extends BasePersistence<PasswordMeta> {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this interface directly. Always use {@link PasswordMetaUtil} to access the password meta persistence. Modify <code>service.xml</code> and rerun ServiceBuilder to regenerate this interface.
	 */

	/**
	 * Returns all the password metas where uuid = &#63;.
	 *
	 * @param uuid the uuid
	 * @return the matching password metas
	 */
	public java.util.List<PasswordMeta> findByUuid(String uuid);

	/**
	 * Returns a range of all the password metas where uuid = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordMetaModelImpl</code>.
	 * </p>
	 *
	 * @param uuid the uuid
	 * @param start the lower bound of the range of password metas
	 * @param end the upper bound of the range of password metas (not inclusive)
	 * @return the range of matching password metas
	 */
	public java.util.List<PasswordMeta> findByUuid(
		String uuid, int start, int end);

	/**
	 * Returns an ordered range of all the password metas where uuid = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordMetaModelImpl</code>.
	 * </p>
	 *
	 * @param uuid the uuid
	 * @param start the lower bound of the range of password metas
	 * @param end the upper bound of the range of password metas (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching password metas
	 */
	public java.util.List<PasswordMeta> findByUuid(
		String uuid, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<PasswordMeta>
			orderByComparator);

	/**
	 * Returns an ordered range of all the password metas where uuid = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordMetaModelImpl</code>.
	 * </p>
	 *
	 * @param uuid the uuid
	 * @param start the lower bound of the range of password metas
	 * @param end the upper bound of the range of password metas (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching password metas
	 */
	public java.util.List<PasswordMeta> findByUuid(
		String uuid, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<PasswordMeta>
			orderByComparator,
		boolean useFinderCache);

	/**
	 * Returns the first password meta in the ordered set where uuid = &#63;.
	 *
	 * @param uuid the uuid
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching password meta
	 * @throws NoSuchMetaException if a matching password meta could not be found
	 */
	public PasswordMeta findByUuid_First(
			String uuid,
			com.liferay.portal.kernel.util.OrderByComparator<PasswordMeta>
				orderByComparator)
		throws NoSuchMetaException;

	/**
	 * Returns the first password meta in the ordered set where uuid = &#63;.
	 *
	 * @param uuid the uuid
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching password meta, or <code>null</code> if a matching password meta could not be found
	 */
	public PasswordMeta fetchByUuid_First(
		String uuid,
		com.liferay.portal.kernel.util.OrderByComparator<PasswordMeta>
			orderByComparator);

	/**
	 * Returns the last password meta in the ordered set where uuid = &#63;.
	 *
	 * @param uuid the uuid
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching password meta
	 * @throws NoSuchMetaException if a matching password meta could not be found
	 */
	public PasswordMeta findByUuid_Last(
			String uuid,
			com.liferay.portal.kernel.util.OrderByComparator<PasswordMeta>
				orderByComparator)
		throws NoSuchMetaException;

	/**
	 * Returns the last password meta in the ordered set where uuid = &#63;.
	 *
	 * @param uuid the uuid
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching password meta, or <code>null</code> if a matching password meta could not be found
	 */
	public PasswordMeta fetchByUuid_Last(
		String uuid,
		com.liferay.portal.kernel.util.OrderByComparator<PasswordMeta>
			orderByComparator);

	/**
	 * Returns the password metas before and after the current password meta in the ordered set where uuid = &#63;.
	 *
	 * @param passwordMetaId the primary key of the current password meta
	 * @param uuid the uuid
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next password meta
	 * @throws NoSuchMetaException if a password meta with the primary key could not be found
	 */
	public PasswordMeta[] findByUuid_PrevAndNext(
			long passwordMetaId, String uuid,
			com.liferay.portal.kernel.util.OrderByComparator<PasswordMeta>
				orderByComparator)
		throws NoSuchMetaException;

	/**
	 * Removes all the password metas where uuid = &#63; from the database.
	 *
	 * @param uuid the uuid
	 */
	public void removeByUuid(String uuid);

	/**
	 * Returns the number of password metas where uuid = &#63;.
	 *
	 * @param uuid the uuid
	 * @return the number of matching password metas
	 */
	public int countByUuid(String uuid);

	/**
	 * Returns all the password metas where uuid = &#63; and companyId = &#63;.
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @return the matching password metas
	 */
	public java.util.List<PasswordMeta> findByUuid_C(
		String uuid, long companyId);

	/**
	 * Returns a range of all the password metas where uuid = &#63; and companyId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordMetaModelImpl</code>.
	 * </p>
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param start the lower bound of the range of password metas
	 * @param end the upper bound of the range of password metas (not inclusive)
	 * @return the range of matching password metas
	 */
	public java.util.List<PasswordMeta> findByUuid_C(
		String uuid, long companyId, int start, int end);

	/**
	 * Returns an ordered range of all the password metas where uuid = &#63; and companyId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordMetaModelImpl</code>.
	 * </p>
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param start the lower bound of the range of password metas
	 * @param end the upper bound of the range of password metas (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching password metas
	 */
	public java.util.List<PasswordMeta> findByUuid_C(
		String uuid, long companyId, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<PasswordMeta>
			orderByComparator);

	/**
	 * Returns an ordered range of all the password metas where uuid = &#63; and companyId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordMetaModelImpl</code>.
	 * </p>
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param start the lower bound of the range of password metas
	 * @param end the upper bound of the range of password metas (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching password metas
	 */
	public java.util.List<PasswordMeta> findByUuid_C(
		String uuid, long companyId, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<PasswordMeta>
			orderByComparator,
		boolean useFinderCache);

	/**
	 * Returns the first password meta in the ordered set where uuid = &#63; and companyId = &#63;.
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching password meta
	 * @throws NoSuchMetaException if a matching password meta could not be found
	 */
	public PasswordMeta findByUuid_C_First(
			String uuid, long companyId,
			com.liferay.portal.kernel.util.OrderByComparator<PasswordMeta>
				orderByComparator)
		throws NoSuchMetaException;

	/**
	 * Returns the first password meta in the ordered set where uuid = &#63; and companyId = &#63;.
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching password meta, or <code>null</code> if a matching password meta could not be found
	 */
	public PasswordMeta fetchByUuid_C_First(
		String uuid, long companyId,
		com.liferay.portal.kernel.util.OrderByComparator<PasswordMeta>
			orderByComparator);

	/**
	 * Returns the last password meta in the ordered set where uuid = &#63; and companyId = &#63;.
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching password meta
	 * @throws NoSuchMetaException if a matching password meta could not be found
	 */
	public PasswordMeta findByUuid_C_Last(
			String uuid, long companyId,
			com.liferay.portal.kernel.util.OrderByComparator<PasswordMeta>
				orderByComparator)
		throws NoSuchMetaException;

	/**
	 * Returns the last password meta in the ordered set where uuid = &#63; and companyId = &#63;.
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching password meta, or <code>null</code> if a matching password meta could not be found
	 */
	public PasswordMeta fetchByUuid_C_Last(
		String uuid, long companyId,
		com.liferay.portal.kernel.util.OrderByComparator<PasswordMeta>
			orderByComparator);

	/**
	 * Returns the password metas before and after the current password meta in the ordered set where uuid = &#63; and companyId = &#63;.
	 *
	 * @param passwordMetaId the primary key of the current password meta
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next password meta
	 * @throws NoSuchMetaException if a password meta with the primary key could not be found
	 */
	public PasswordMeta[] findByUuid_C_PrevAndNext(
			long passwordMetaId, String uuid, long companyId,
			com.liferay.portal.kernel.util.OrderByComparator<PasswordMeta>
				orderByComparator)
		throws NoSuchMetaException;

	/**
	 * Removes all the password metas where uuid = &#63; and companyId = &#63; from the database.
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 */
	public void removeByUuid_C(String uuid, long companyId);

	/**
	 * Returns the number of password metas where uuid = &#63; and companyId = &#63;.
	 *
	 * @param uuid the uuid
	 * @param companyId the company ID
	 * @return the number of matching password metas
	 */
	public int countByUuid_C(String uuid, long companyId);

	/**
	 * Returns all the password metas where createDate &lt; &#63;.
	 *
	 * @param createDate the create date
	 * @return the matching password metas
	 */
	public java.util.List<PasswordMeta> findByLtCreateDate(Date createDate);

	/**
	 * Returns a range of all the password metas where createDate &lt; &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordMetaModelImpl</code>.
	 * </p>
	 *
	 * @param createDate the create date
	 * @param start the lower bound of the range of password metas
	 * @param end the upper bound of the range of password metas (not inclusive)
	 * @return the range of matching password metas
	 */
	public java.util.List<PasswordMeta> findByLtCreateDate(
		Date createDate, int start, int end);

	/**
	 * Returns an ordered range of all the password metas where createDate &lt; &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordMetaModelImpl</code>.
	 * </p>
	 *
	 * @param createDate the create date
	 * @param start the lower bound of the range of password metas
	 * @param end the upper bound of the range of password metas (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching password metas
	 */
	public java.util.List<PasswordMeta> findByLtCreateDate(
		Date createDate, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<PasswordMeta>
			orderByComparator);

	/**
	 * Returns an ordered range of all the password metas where createDate &lt; &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordMetaModelImpl</code>.
	 * </p>
	 *
	 * @param createDate the create date
	 * @param start the lower bound of the range of password metas
	 * @param end the upper bound of the range of password metas (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching password metas
	 */
	public java.util.List<PasswordMeta> findByLtCreateDate(
		Date createDate, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<PasswordMeta>
			orderByComparator,
		boolean useFinderCache);

	/**
	 * Returns the first password meta in the ordered set where createDate &lt; &#63;.
	 *
	 * @param createDate the create date
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching password meta
	 * @throws NoSuchMetaException if a matching password meta could not be found
	 */
	public PasswordMeta findByLtCreateDate_First(
			Date createDate,
			com.liferay.portal.kernel.util.OrderByComparator<PasswordMeta>
				orderByComparator)
		throws NoSuchMetaException;

	/**
	 * Returns the first password meta in the ordered set where createDate &lt; &#63;.
	 *
	 * @param createDate the create date
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching password meta, or <code>null</code> if a matching password meta could not be found
	 */
	public PasswordMeta fetchByLtCreateDate_First(
		Date createDate,
		com.liferay.portal.kernel.util.OrderByComparator<PasswordMeta>
			orderByComparator);

	/**
	 * Returns the last password meta in the ordered set where createDate &lt; &#63;.
	 *
	 * @param createDate the create date
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching password meta
	 * @throws NoSuchMetaException if a matching password meta could not be found
	 */
	public PasswordMeta findByLtCreateDate_Last(
			Date createDate,
			com.liferay.portal.kernel.util.OrderByComparator<PasswordMeta>
				orderByComparator)
		throws NoSuchMetaException;

	/**
	 * Returns the last password meta in the ordered set where createDate &lt; &#63;.
	 *
	 * @param createDate the create date
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching password meta, or <code>null</code> if a matching password meta could not be found
	 */
	public PasswordMeta fetchByLtCreateDate_Last(
		Date createDate,
		com.liferay.portal.kernel.util.OrderByComparator<PasswordMeta>
			orderByComparator);

	/**
	 * Returns the password metas before and after the current password meta in the ordered set where createDate &lt; &#63;.
	 *
	 * @param passwordMetaId the primary key of the current password meta
	 * @param createDate the create date
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next password meta
	 * @throws NoSuchMetaException if a password meta with the primary key could not be found
	 */
	public PasswordMeta[] findByLtCreateDate_PrevAndNext(
			long passwordMetaId, Date createDate,
			com.liferay.portal.kernel.util.OrderByComparator<PasswordMeta>
				orderByComparator)
		throws NoSuchMetaException;

	/**
	 * Removes all the password metas where createDate &lt; &#63; from the database.
	 *
	 * @param createDate the create date
	 */
	public void removeByLtCreateDate(Date createDate);

	/**
	 * Returns the number of password metas where createDate &lt; &#63;.
	 *
	 * @param createDate the create date
	 * @return the number of matching password metas
	 */
	public int countByLtCreateDate(Date createDate);

	/**
	 * Returns all the password metas where passwordEntryId = &#63;.
	 *
	 * @param passwordEntryId the password entry ID
	 * @return the matching password metas
	 */
	public java.util.List<PasswordMeta> findByPasswordEntryId(
		long passwordEntryId);

	/**
	 * Returns a range of all the password metas where passwordEntryId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordMetaModelImpl</code>.
	 * </p>
	 *
	 * @param passwordEntryId the password entry ID
	 * @param start the lower bound of the range of password metas
	 * @param end the upper bound of the range of password metas (not inclusive)
	 * @return the range of matching password metas
	 */
	public java.util.List<PasswordMeta> findByPasswordEntryId(
		long passwordEntryId, int start, int end);

	/**
	 * Returns an ordered range of all the password metas where passwordEntryId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordMetaModelImpl</code>.
	 * </p>
	 *
	 * @param passwordEntryId the password entry ID
	 * @param start the lower bound of the range of password metas
	 * @param end the upper bound of the range of password metas (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching password metas
	 */
	public java.util.List<PasswordMeta> findByPasswordEntryId(
		long passwordEntryId, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<PasswordMeta>
			orderByComparator);

	/**
	 * Returns an ordered range of all the password metas where passwordEntryId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordMetaModelImpl</code>.
	 * </p>
	 *
	 * @param passwordEntryId the password entry ID
	 * @param start the lower bound of the range of password metas
	 * @param end the upper bound of the range of password metas (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching password metas
	 */
	public java.util.List<PasswordMeta> findByPasswordEntryId(
		long passwordEntryId, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<PasswordMeta>
			orderByComparator,
		boolean useFinderCache);

	/**
	 * Returns the first password meta in the ordered set where passwordEntryId = &#63;.
	 *
	 * @param passwordEntryId the password entry ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching password meta
	 * @throws NoSuchMetaException if a matching password meta could not be found
	 */
	public PasswordMeta findByPasswordEntryId_First(
			long passwordEntryId,
			com.liferay.portal.kernel.util.OrderByComparator<PasswordMeta>
				orderByComparator)
		throws NoSuchMetaException;

	/**
	 * Returns the first password meta in the ordered set where passwordEntryId = &#63;.
	 *
	 * @param passwordEntryId the password entry ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching password meta, or <code>null</code> if a matching password meta could not be found
	 */
	public PasswordMeta fetchByPasswordEntryId_First(
		long passwordEntryId,
		com.liferay.portal.kernel.util.OrderByComparator<PasswordMeta>
			orderByComparator);

	/**
	 * Returns the last password meta in the ordered set where passwordEntryId = &#63;.
	 *
	 * @param passwordEntryId the password entry ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching password meta
	 * @throws NoSuchMetaException if a matching password meta could not be found
	 */
	public PasswordMeta findByPasswordEntryId_Last(
			long passwordEntryId,
			com.liferay.portal.kernel.util.OrderByComparator<PasswordMeta>
				orderByComparator)
		throws NoSuchMetaException;

	/**
	 * Returns the last password meta in the ordered set where passwordEntryId = &#63;.
	 *
	 * @param passwordEntryId the password entry ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching password meta, or <code>null</code> if a matching password meta could not be found
	 */
	public PasswordMeta fetchByPasswordEntryId_Last(
		long passwordEntryId,
		com.liferay.portal.kernel.util.OrderByComparator<PasswordMeta>
			orderByComparator);

	/**
	 * Returns the password metas before and after the current password meta in the ordered set where passwordEntryId = &#63;.
	 *
	 * @param passwordMetaId the primary key of the current password meta
	 * @param passwordEntryId the password entry ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next password meta
	 * @throws NoSuchMetaException if a password meta with the primary key could not be found
	 */
	public PasswordMeta[] findByPasswordEntryId_PrevAndNext(
			long passwordMetaId, long passwordEntryId,
			com.liferay.portal.kernel.util.OrderByComparator<PasswordMeta>
				orderByComparator)
		throws NoSuchMetaException;

	/**
	 * Removes all the password metas where passwordEntryId = &#63; from the database.
	 *
	 * @param passwordEntryId the password entry ID
	 */
	public void removeByPasswordEntryId(long passwordEntryId);

	/**
	 * Returns the number of password metas where passwordEntryId = &#63;.
	 *
	 * @param passwordEntryId the password entry ID
	 * @return the number of matching password metas
	 */
	public int countByPasswordEntryId(long passwordEntryId);

	/**
	 * Caches the password meta in the entity cache if it is enabled.
	 *
	 * @param passwordMeta the password meta
	 */
	public void cacheResult(PasswordMeta passwordMeta);

	/**
	 * Caches the password metas in the entity cache if it is enabled.
	 *
	 * @param passwordMetas the password metas
	 */
	public void cacheResult(java.util.List<PasswordMeta> passwordMetas);

	/**
	 * Creates a new password meta with the primary key. Does not add the password meta to the database.
	 *
	 * @param passwordMetaId the primary key for the new password meta
	 * @return the new password meta
	 */
	public PasswordMeta create(long passwordMetaId);

	/**
	 * Removes the password meta with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param passwordMetaId the primary key of the password meta
	 * @return the password meta that was removed
	 * @throws NoSuchMetaException if a password meta with the primary key could not be found
	 */
	public PasswordMeta remove(long passwordMetaId) throws NoSuchMetaException;

	public PasswordMeta updateImpl(PasswordMeta passwordMeta);

	/**
	 * Returns the password meta with the primary key or throws a <code>NoSuchMetaException</code> if it could not be found.
	 *
	 * @param passwordMetaId the primary key of the password meta
	 * @return the password meta
	 * @throws NoSuchMetaException if a password meta with the primary key could not be found
	 */
	public PasswordMeta findByPrimaryKey(long passwordMetaId)
		throws NoSuchMetaException;

	/**
	 * Returns the password meta with the primary key or returns <code>null</code> if it could not be found.
	 *
	 * @param passwordMetaId the primary key of the password meta
	 * @return the password meta, or <code>null</code> if a password meta with the primary key could not be found
	 */
	public PasswordMeta fetchByPrimaryKey(long passwordMetaId);

	/**
	 * Returns all the password metas.
	 *
	 * @return the password metas
	 */
	public java.util.List<PasswordMeta> findAll();

	/**
	 * Returns a range of all the password metas.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordMetaModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of password metas
	 * @param end the upper bound of the range of password metas (not inclusive)
	 * @return the range of password metas
	 */
	public java.util.List<PasswordMeta> findAll(int start, int end);

	/**
	 * Returns an ordered range of all the password metas.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordMetaModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of password metas
	 * @param end the upper bound of the range of password metas (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of password metas
	 */
	public java.util.List<PasswordMeta> findAll(
		int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<PasswordMeta>
			orderByComparator);

	/**
	 * Returns an ordered range of all the password metas.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>PasswordMetaModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of password metas
	 * @param end the upper bound of the range of password metas (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of password metas
	 */
	public java.util.List<PasswordMeta> findAll(
		int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<PasswordMeta>
			orderByComparator,
		boolean useFinderCache);

	/**
	 * Removes all the password metas from the database.
	 */
	public void removeAll();

	/**
	 * Returns the number of password metas.
	 *
	 * @return the number of password metas
	 */
	public int countAll();

}