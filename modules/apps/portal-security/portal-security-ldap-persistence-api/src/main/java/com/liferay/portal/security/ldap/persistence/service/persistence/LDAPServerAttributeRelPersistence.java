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

package com.liferay.portal.security.ldap.persistence.service.persistence;

import com.liferay.portal.kernel.service.persistence.BasePersistence;
import com.liferay.portal.security.ldap.persistence.exception.NoSuchServerAttributeRelException;
import com.liferay.portal.security.ldap.persistence.model.LDAPServerAttributeRel;

import org.osgi.annotation.versioning.ProviderType;

/**
 * The persistence interface for the ldap server attribute rel service.
 *
 * <p>
 * Caching information and settings can be found in <code>portal.properties</code>
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @see LDAPServerAttributeRelUtil
 * @generated
 */
@ProviderType
public interface LDAPServerAttributeRelPersistence
	extends BasePersistence<LDAPServerAttributeRel> {

	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this interface directly. Always use {@link LDAPServerAttributeRelUtil} to access the ldap server attribute rel persistence. Modify <code>service.xml</code> and rerun ServiceBuilder to regenerate this interface.
	 */

	/**
	 * Returns all the ldap server attribute rels where ldapServerId = &#63;.
	 *
	 * @param ldapServerId the ldap server ID
	 * @return the matching ldap server attribute rels
	 */
	public java.util.List<LDAPServerAttributeRel> findByLdapServerId(
		long ldapServerId);

	/**
	 * Returns a range of all the ldap server attribute rels where ldapServerId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>LDAPServerAttributeRelModelImpl</code>.
	 * </p>
	 *
	 * @param ldapServerId the ldap server ID
	 * @param start the lower bound of the range of ldap server attribute rels
	 * @param end the upper bound of the range of ldap server attribute rels (not inclusive)
	 * @return the range of matching ldap server attribute rels
	 */
	public java.util.List<LDAPServerAttributeRel> findByLdapServerId(
		long ldapServerId, int start, int end);

	/**
	 * Returns an ordered range of all the ldap server attribute rels where ldapServerId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>LDAPServerAttributeRelModelImpl</code>.
	 * </p>
	 *
	 * @param ldapServerId the ldap server ID
	 * @param start the lower bound of the range of ldap server attribute rels
	 * @param end the upper bound of the range of ldap server attribute rels (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching ldap server attribute rels
	 */
	public java.util.List<LDAPServerAttributeRel> findByLdapServerId(
		long ldapServerId, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<LDAPServerAttributeRel>
			orderByComparator);

	/**
	 * Returns an ordered range of all the ldap server attribute rels where ldapServerId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>LDAPServerAttributeRelModelImpl</code>.
	 * </p>
	 *
	 * @param ldapServerId the ldap server ID
	 * @param start the lower bound of the range of ldap server attribute rels
	 * @param end the upper bound of the range of ldap server attribute rels (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching ldap server attribute rels
	 */
	public java.util.List<LDAPServerAttributeRel> findByLdapServerId(
		long ldapServerId, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<LDAPServerAttributeRel>
			orderByComparator,
		boolean useFinderCache);

	/**
	 * Returns the first ldap server attribute rel in the ordered set where ldapServerId = &#63;.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching ldap server attribute rel
	 * @throws NoSuchServerAttributeRelException if a matching ldap server attribute rel could not be found
	 */
	public LDAPServerAttributeRel findByLdapServerId_First(
			long ldapServerId,
			com.liferay.portal.kernel.util.OrderByComparator
				<LDAPServerAttributeRel> orderByComparator)
		throws NoSuchServerAttributeRelException;

	/**
	 * Returns the first ldap server attribute rel in the ordered set where ldapServerId = &#63;.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching ldap server attribute rel, or <code>null</code> if a matching ldap server attribute rel could not be found
	 */
	public LDAPServerAttributeRel fetchByLdapServerId_First(
		long ldapServerId,
		com.liferay.portal.kernel.util.OrderByComparator<LDAPServerAttributeRel>
			orderByComparator);

	/**
	 * Returns the last ldap server attribute rel in the ordered set where ldapServerId = &#63;.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching ldap server attribute rel
	 * @throws NoSuchServerAttributeRelException if a matching ldap server attribute rel could not be found
	 */
	public LDAPServerAttributeRel findByLdapServerId_Last(
			long ldapServerId,
			com.liferay.portal.kernel.util.OrderByComparator
				<LDAPServerAttributeRel> orderByComparator)
		throws NoSuchServerAttributeRelException;

	/**
	 * Returns the last ldap server attribute rel in the ordered set where ldapServerId = &#63;.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching ldap server attribute rel, or <code>null</code> if a matching ldap server attribute rel could not be found
	 */
	public LDAPServerAttributeRel fetchByLdapServerId_Last(
		long ldapServerId,
		com.liferay.portal.kernel.util.OrderByComparator<LDAPServerAttributeRel>
			orderByComparator);

	/**
	 * Returns the ldap server attribute rels before and after the current ldap server attribute rel in the ordered set where ldapServerId = &#63;.
	 *
	 * @param ldapServerAttributeRelId the primary key of the current ldap server attribute rel
	 * @param ldapServerId the ldap server ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next ldap server attribute rel
	 * @throws NoSuchServerAttributeRelException if a ldap server attribute rel with the primary key could not be found
	 */
	public LDAPServerAttributeRel[] findByLdapServerId_PrevAndNext(
			long ldapServerAttributeRelId, long ldapServerId,
			com.liferay.portal.kernel.util.OrderByComparator
				<LDAPServerAttributeRel> orderByComparator)
		throws NoSuchServerAttributeRelException;

	/**
	 * Removes all the ldap server attribute rels where ldapServerId = &#63; from the database.
	 *
	 * @param ldapServerId the ldap server ID
	 */
	public void removeByLdapServerId(long ldapServerId);

	/**
	 * Returns the number of ldap server attribute rels where ldapServerId = &#63;.
	 *
	 * @param ldapServerId the ldap server ID
	 * @return the number of matching ldap server attribute rels
	 */
	public int countByLdapServerId(long ldapServerId);

	/**
	 * Returns all the ldap server attribute rels where ldapServerId = &#63; and classNameId = &#63;.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @return the matching ldap server attribute rels
	 */
	public java.util.List<LDAPServerAttributeRel> findByL_C(
		long ldapServerId, long classNameId);

	/**
	 * Returns a range of all the ldap server attribute rels where ldapServerId = &#63; and classNameId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>LDAPServerAttributeRelModelImpl</code>.
	 * </p>
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @param start the lower bound of the range of ldap server attribute rels
	 * @param end the upper bound of the range of ldap server attribute rels (not inclusive)
	 * @return the range of matching ldap server attribute rels
	 */
	public java.util.List<LDAPServerAttributeRel> findByL_C(
		long ldapServerId, long classNameId, int start, int end);

	/**
	 * Returns an ordered range of all the ldap server attribute rels where ldapServerId = &#63; and classNameId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>LDAPServerAttributeRelModelImpl</code>.
	 * </p>
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @param start the lower bound of the range of ldap server attribute rels
	 * @param end the upper bound of the range of ldap server attribute rels (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching ldap server attribute rels
	 */
	public java.util.List<LDAPServerAttributeRel> findByL_C(
		long ldapServerId, long classNameId, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<LDAPServerAttributeRel>
			orderByComparator);

	/**
	 * Returns an ordered range of all the ldap server attribute rels where ldapServerId = &#63; and classNameId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>LDAPServerAttributeRelModelImpl</code>.
	 * </p>
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @param start the lower bound of the range of ldap server attribute rels
	 * @param end the upper bound of the range of ldap server attribute rels (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching ldap server attribute rels
	 */
	public java.util.List<LDAPServerAttributeRel> findByL_C(
		long ldapServerId, long classNameId, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<LDAPServerAttributeRel>
			orderByComparator,
		boolean useFinderCache);

	/**
	 * Returns the first ldap server attribute rel in the ordered set where ldapServerId = &#63; and classNameId = &#63;.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching ldap server attribute rel
	 * @throws NoSuchServerAttributeRelException if a matching ldap server attribute rel could not be found
	 */
	public LDAPServerAttributeRel findByL_C_First(
			long ldapServerId, long classNameId,
			com.liferay.portal.kernel.util.OrderByComparator
				<LDAPServerAttributeRel> orderByComparator)
		throws NoSuchServerAttributeRelException;

	/**
	 * Returns the first ldap server attribute rel in the ordered set where ldapServerId = &#63; and classNameId = &#63;.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching ldap server attribute rel, or <code>null</code> if a matching ldap server attribute rel could not be found
	 */
	public LDAPServerAttributeRel fetchByL_C_First(
		long ldapServerId, long classNameId,
		com.liferay.portal.kernel.util.OrderByComparator<LDAPServerAttributeRel>
			orderByComparator);

	/**
	 * Returns the last ldap server attribute rel in the ordered set where ldapServerId = &#63; and classNameId = &#63;.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching ldap server attribute rel
	 * @throws NoSuchServerAttributeRelException if a matching ldap server attribute rel could not be found
	 */
	public LDAPServerAttributeRel findByL_C_Last(
			long ldapServerId, long classNameId,
			com.liferay.portal.kernel.util.OrderByComparator
				<LDAPServerAttributeRel> orderByComparator)
		throws NoSuchServerAttributeRelException;

	/**
	 * Returns the last ldap server attribute rel in the ordered set where ldapServerId = &#63; and classNameId = &#63;.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching ldap server attribute rel, or <code>null</code> if a matching ldap server attribute rel could not be found
	 */
	public LDAPServerAttributeRel fetchByL_C_Last(
		long ldapServerId, long classNameId,
		com.liferay.portal.kernel.util.OrderByComparator<LDAPServerAttributeRel>
			orderByComparator);

	/**
	 * Returns the ldap server attribute rels before and after the current ldap server attribute rel in the ordered set where ldapServerId = &#63; and classNameId = &#63;.
	 *
	 * @param ldapServerAttributeRelId the primary key of the current ldap server attribute rel
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next ldap server attribute rel
	 * @throws NoSuchServerAttributeRelException if a ldap server attribute rel with the primary key could not be found
	 */
	public LDAPServerAttributeRel[] findByL_C_PrevAndNext(
			long ldapServerAttributeRelId, long ldapServerId, long classNameId,
			com.liferay.portal.kernel.util.OrderByComparator
				<LDAPServerAttributeRel> orderByComparator)
		throws NoSuchServerAttributeRelException;

	/**
	 * Removes all the ldap server attribute rels where ldapServerId = &#63; and classNameId = &#63; from the database.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 */
	public void removeByL_C(long ldapServerId, long classNameId);

	/**
	 * Returns the number of ldap server attribute rels where ldapServerId = &#63; and classNameId = &#63;.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @return the number of matching ldap server attribute rels
	 */
	public int countByL_C(long ldapServerId, long classNameId);

	/**
	 * Returns all the ldap server attribute rels where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the matching ldap server attribute rels
	 */
	public java.util.List<LDAPServerAttributeRel> findByC_C(
		long classNameId, long classPK);

	/**
	 * Returns a range of all the ldap server attribute rels where classNameId = &#63; and classPK = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>LDAPServerAttributeRelModelImpl</code>.
	 * </p>
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param start the lower bound of the range of ldap server attribute rels
	 * @param end the upper bound of the range of ldap server attribute rels (not inclusive)
	 * @return the range of matching ldap server attribute rels
	 */
	public java.util.List<LDAPServerAttributeRel> findByC_C(
		long classNameId, long classPK, int start, int end);

	/**
	 * Returns an ordered range of all the ldap server attribute rels where classNameId = &#63; and classPK = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>LDAPServerAttributeRelModelImpl</code>.
	 * </p>
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param start the lower bound of the range of ldap server attribute rels
	 * @param end the upper bound of the range of ldap server attribute rels (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching ldap server attribute rels
	 */
	public java.util.List<LDAPServerAttributeRel> findByC_C(
		long classNameId, long classPK, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<LDAPServerAttributeRel>
			orderByComparator);

	/**
	 * Returns an ordered range of all the ldap server attribute rels where classNameId = &#63; and classPK = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>LDAPServerAttributeRelModelImpl</code>.
	 * </p>
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param start the lower bound of the range of ldap server attribute rels
	 * @param end the upper bound of the range of ldap server attribute rels (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching ldap server attribute rels
	 */
	public java.util.List<LDAPServerAttributeRel> findByC_C(
		long classNameId, long classPK, int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<LDAPServerAttributeRel>
			orderByComparator,
		boolean useFinderCache);

	/**
	 * Returns the first ldap server attribute rel in the ordered set where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching ldap server attribute rel
	 * @throws NoSuchServerAttributeRelException if a matching ldap server attribute rel could not be found
	 */
	public LDAPServerAttributeRel findByC_C_First(
			long classNameId, long classPK,
			com.liferay.portal.kernel.util.OrderByComparator
				<LDAPServerAttributeRel> orderByComparator)
		throws NoSuchServerAttributeRelException;

	/**
	 * Returns the first ldap server attribute rel in the ordered set where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching ldap server attribute rel, or <code>null</code> if a matching ldap server attribute rel could not be found
	 */
	public LDAPServerAttributeRel fetchByC_C_First(
		long classNameId, long classPK,
		com.liferay.portal.kernel.util.OrderByComparator<LDAPServerAttributeRel>
			orderByComparator);

	/**
	 * Returns the last ldap server attribute rel in the ordered set where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching ldap server attribute rel
	 * @throws NoSuchServerAttributeRelException if a matching ldap server attribute rel could not be found
	 */
	public LDAPServerAttributeRel findByC_C_Last(
			long classNameId, long classPK,
			com.liferay.portal.kernel.util.OrderByComparator
				<LDAPServerAttributeRel> orderByComparator)
		throws NoSuchServerAttributeRelException;

	/**
	 * Returns the last ldap server attribute rel in the ordered set where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching ldap server attribute rel, or <code>null</code> if a matching ldap server attribute rel could not be found
	 */
	public LDAPServerAttributeRel fetchByC_C_Last(
		long classNameId, long classPK,
		com.liferay.portal.kernel.util.OrderByComparator<LDAPServerAttributeRel>
			orderByComparator);

	/**
	 * Returns the ldap server attribute rels before and after the current ldap server attribute rel in the ordered set where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param ldapServerAttributeRelId the primary key of the current ldap server attribute rel
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next ldap server attribute rel
	 * @throws NoSuchServerAttributeRelException if a ldap server attribute rel with the primary key could not be found
	 */
	public LDAPServerAttributeRel[] findByC_C_PrevAndNext(
			long ldapServerAttributeRelId, long classNameId, long classPK,
			com.liferay.portal.kernel.util.OrderByComparator
				<LDAPServerAttributeRel> orderByComparator)
		throws NoSuchServerAttributeRelException;

	/**
	 * Removes all the ldap server attribute rels where classNameId = &#63; and classPK = &#63; from the database.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 */
	public void removeByC_C(long classNameId, long classPK);

	/**
	 * Returns the number of ldap server attribute rels where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the number of matching ldap server attribute rels
	 */
	public int countByC_C(long classNameId, long classPK);

	/**
	 * Returns the ldap server attribute rel where ldapServerId = &#63; and classNameId = &#63; and classPK = &#63; or throws a <code>NoSuchServerAttributeRelException</code> if it could not be found.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the matching ldap server attribute rel
	 * @throws NoSuchServerAttributeRelException if a matching ldap server attribute rel could not be found
	 */
	public LDAPServerAttributeRel findByL_C_C(
			long ldapServerId, long classNameId, long classPK)
		throws NoSuchServerAttributeRelException;

	/**
	 * Returns the ldap server attribute rel where ldapServerId = &#63; and classNameId = &#63; and classPK = &#63; or returns <code>null</code> if it could not be found. Uses the finder cache.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the matching ldap server attribute rel, or <code>null</code> if a matching ldap server attribute rel could not be found
	 */
	public LDAPServerAttributeRel fetchByL_C_C(
		long ldapServerId, long classNameId, long classPK);

	/**
	 * Returns the ldap server attribute rel where ldapServerId = &#63; and classNameId = &#63; and classPK = &#63; or returns <code>null</code> if it could not be found, optionally using the finder cache.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param useFinderCache whether to use the finder cache
	 * @return the matching ldap server attribute rel, or <code>null</code> if a matching ldap server attribute rel could not be found
	 */
	public LDAPServerAttributeRel fetchByL_C_C(
		long ldapServerId, long classNameId, long classPK,
		boolean useFinderCache);

	/**
	 * Removes the ldap server attribute rel where ldapServerId = &#63; and classNameId = &#63; and classPK = &#63; from the database.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the ldap server attribute rel that was removed
	 */
	public LDAPServerAttributeRel removeByL_C_C(
			long ldapServerId, long classNameId, long classPK)
		throws NoSuchServerAttributeRelException;

	/**
	 * Returns the number of ldap server attribute rels where ldapServerId = &#63; and classNameId = &#63; and classPK = &#63;.
	 *
	 * @param ldapServerId the ldap server ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the number of matching ldap server attribute rels
	 */
	public int countByL_C_C(long ldapServerId, long classNameId, long classPK);

	/**
	 * Caches the ldap server attribute rel in the entity cache if it is enabled.
	 *
	 * @param ldapServerAttributeRel the ldap server attribute rel
	 */
	public void cacheResult(LDAPServerAttributeRel ldapServerAttributeRel);

	/**
	 * Caches the ldap server attribute rels in the entity cache if it is enabled.
	 *
	 * @param ldapServerAttributeRels the ldap server attribute rels
	 */
	public void cacheResult(
		java.util.List<LDAPServerAttributeRel> ldapServerAttributeRels);

	/**
	 * Creates a new ldap server attribute rel with the primary key. Does not add the ldap server attribute rel to the database.
	 *
	 * @param ldapServerAttributeRelId the primary key for the new ldap server attribute rel
	 * @return the new ldap server attribute rel
	 */
	public LDAPServerAttributeRel create(long ldapServerAttributeRelId);

	/**
	 * Removes the ldap server attribute rel with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param ldapServerAttributeRelId the primary key of the ldap server attribute rel
	 * @return the ldap server attribute rel that was removed
	 * @throws NoSuchServerAttributeRelException if a ldap server attribute rel with the primary key could not be found
	 */
	public LDAPServerAttributeRel remove(long ldapServerAttributeRelId)
		throws NoSuchServerAttributeRelException;

	public LDAPServerAttributeRel updateImpl(
		LDAPServerAttributeRel ldapServerAttributeRel);

	/**
	 * Returns the ldap server attribute rel with the primary key or throws a <code>NoSuchServerAttributeRelException</code> if it could not be found.
	 *
	 * @param ldapServerAttributeRelId the primary key of the ldap server attribute rel
	 * @return the ldap server attribute rel
	 * @throws NoSuchServerAttributeRelException if a ldap server attribute rel with the primary key could not be found
	 */
	public LDAPServerAttributeRel findByPrimaryKey(
			long ldapServerAttributeRelId)
		throws NoSuchServerAttributeRelException;

	/**
	 * Returns the ldap server attribute rel with the primary key or returns <code>null</code> if it could not be found.
	 *
	 * @param ldapServerAttributeRelId the primary key of the ldap server attribute rel
	 * @return the ldap server attribute rel, or <code>null</code> if a ldap server attribute rel with the primary key could not be found
	 */
	public LDAPServerAttributeRel fetchByPrimaryKey(
		long ldapServerAttributeRelId);

	/**
	 * Returns all the ldap server attribute rels.
	 *
	 * @return the ldap server attribute rels
	 */
	public java.util.List<LDAPServerAttributeRel> findAll();

	/**
	 * Returns a range of all the ldap server attribute rels.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>LDAPServerAttributeRelModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of ldap server attribute rels
	 * @param end the upper bound of the range of ldap server attribute rels (not inclusive)
	 * @return the range of ldap server attribute rels
	 */
	public java.util.List<LDAPServerAttributeRel> findAll(int start, int end);

	/**
	 * Returns an ordered range of all the ldap server attribute rels.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>LDAPServerAttributeRelModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of ldap server attribute rels
	 * @param end the upper bound of the range of ldap server attribute rels (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of ldap server attribute rels
	 */
	public java.util.List<LDAPServerAttributeRel> findAll(
		int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<LDAPServerAttributeRel>
			orderByComparator);

	/**
	 * Returns an ordered range of all the ldap server attribute rels.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent, then the query will include the default ORDER BY logic from <code>LDAPServerAttributeRelModelImpl</code>.
	 * </p>
	 *
	 * @param start the lower bound of the range of ldap server attribute rels
	 * @param end the upper bound of the range of ldap server attribute rels (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of ldap server attribute rels
	 */
	public java.util.List<LDAPServerAttributeRel> findAll(
		int start, int end,
		com.liferay.portal.kernel.util.OrderByComparator<LDAPServerAttributeRel>
			orderByComparator,
		boolean useFinderCache);

	/**
	 * Removes all the ldap server attribute rels from the database.
	 */
	public void removeAll();

	/**
	 * Returns the number of ldap server attribute rels.
	 *
	 * @return the number of ldap server attribute rels
	 */
	public int countAll();

}