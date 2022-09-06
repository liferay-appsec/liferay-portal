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

package com.liferay.headless.commerce.admin.account.internal.graphql.servlet.v1_0;

import com.liferay.headless.commerce.admin.account.internal.graphql.mutation.v1_0.Mutation;
import com.liferay.headless.commerce.admin.account.internal.graphql.query.v1_0.Query;
import com.liferay.headless.commerce.admin.account.internal.resource.v1_0.AccountAddressResourceImpl;
import com.liferay.headless.commerce.admin.account.internal.resource.v1_0.AccountGroupResourceImpl;
import com.liferay.headless.commerce.admin.account.internal.resource.v1_0.AccountMemberResourceImpl;
import com.liferay.headless.commerce.admin.account.internal.resource.v1_0.AccountOrganizationResourceImpl;
import com.liferay.headless.commerce.admin.account.internal.resource.v1_0.AccountResourceImpl;
import com.liferay.headless.commerce.admin.account.internal.resource.v1_0.UserResourceImpl;
import com.liferay.headless.commerce.admin.account.resource.v1_0.AccountAddressResource;
import com.liferay.headless.commerce.admin.account.resource.v1_0.AccountGroupResource;
import com.liferay.headless.commerce.admin.account.resource.v1_0.AccountMemberResource;
import com.liferay.headless.commerce.admin.account.resource.v1_0.AccountOrganizationResource;
import com.liferay.headless.commerce.admin.account.resource.v1_0.AccountResource;
import com.liferay.headless.commerce.admin.account.resource.v1_0.UserResource;
import com.liferay.portal.kernel.util.ObjectValuePair;
import com.liferay.portal.vulcan.graphql.servlet.ServletData;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentServiceObjects;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;

/**
 * @author Alessio Antonio Rendina
 * @generated
 */
@Component(enabled = false, service = ServletData.class)
@Generated("")
public class ServletDataImpl implements ServletData {

	@Activate
	public void activate(BundleContext bundleContext) {
		Mutation.setAccountResourceComponentServiceObjects(
			_accountResourceComponentServiceObjects);
		Mutation.setAccountAddressResourceComponentServiceObjects(
			_accountAddressResourceComponentServiceObjects);
		Mutation.setAccountGroupResourceComponentServiceObjects(
			_accountGroupResourceComponentServiceObjects);
		Mutation.setAccountMemberResourceComponentServiceObjects(
			_accountMemberResourceComponentServiceObjects);
		Mutation.setAccountOrganizationResourceComponentServiceObjects(
			_accountOrganizationResourceComponentServiceObjects);
		Mutation.setUserResourceComponentServiceObjects(
			_userResourceComponentServiceObjects);

		Query.setAccountResourceComponentServiceObjects(
			_accountResourceComponentServiceObjects);
		Query.setAccountAddressResourceComponentServiceObjects(
			_accountAddressResourceComponentServiceObjects);
		Query.setAccountGroupResourceComponentServiceObjects(
			_accountGroupResourceComponentServiceObjects);
		Query.setAccountMemberResourceComponentServiceObjects(
			_accountMemberResourceComponentServiceObjects);
		Query.setAccountOrganizationResourceComponentServiceObjects(
			_accountOrganizationResourceComponentServiceObjects);
	}

	public String getApplicationName() {
		return "Liferay.Headless.Commerce.Admin.Account";
	}

	@Override
	public Mutation getMutation() {
		return new Mutation();
	}

	@Override
	public String getPath() {
		return "/headless-commerce-admin-account-graphql/v1_0";
	}

	@Override
	public Query getQuery() {
		return new Query();
	}

	public ObjectValuePair<Class<?>, String> getResourceMethodPair(
		String methodName, boolean mutation) {

		if (mutation) {
			return _resourceMethodPairs.get("mutation#" + methodName);
		}

		return _resourceMethodPairs.get("query#" + methodName);
	}

	private static final Map<String, ObjectValuePair<Class<?>, String>>
		_resourceMethodPairs = new HashMap<>();

	static {
		_resourceMethodPairs.put(
			"mutation#createAccountGroupByExternalReferenceCodeAccount",
			new ObjectValuePair<>(
				AccountResourceImpl.class,
				"postAccountGroupByExternalReferenceCodeAccount"));
		_resourceMethodPairs.put(
			"mutation#deleteAccountGroupByExternalReferenceCodeAccount",
			new ObjectValuePair<>(
				AccountResourceImpl.class,
				"deleteAccountGroupByExternalReferenceCodeAccount"));
		_resourceMethodPairs.put(
			"mutation#createAccount",
			new ObjectValuePair<>(AccountResourceImpl.class, "postAccount"));
		_resourceMethodPairs.put(
			"mutation#createAccountBatch",
			new ObjectValuePair<>(
				AccountResourceImpl.class, "postAccountBatch"));
		_resourceMethodPairs.put(
			"mutation#deleteAccountByExternalReferenceCode",
			new ObjectValuePair<>(
				AccountResourceImpl.class,
				"deleteAccountByExternalReferenceCode"));
		_resourceMethodPairs.put(
			"mutation#patchAccountByExternalReferenceCode",
			new ObjectValuePair<>(
				AccountResourceImpl.class,
				"patchAccountByExternalReferenceCode"));
		_resourceMethodPairs.put(
			"mutation#createAccountByExternalReferenceCodeLogo",
			new ObjectValuePair<>(
				AccountResourceImpl.class,
				"postAccountByExternalReferenceCodeLogo"));
		_resourceMethodPairs.put(
			"mutation#deleteAccount",
			new ObjectValuePair<>(AccountResourceImpl.class, "deleteAccount"));
		_resourceMethodPairs.put(
			"mutation#deleteAccountBatch",
			new ObjectValuePair<>(
				AccountResourceImpl.class, "deleteAccountBatch"));
		_resourceMethodPairs.put(
			"mutation#patchAccount",
			new ObjectValuePair<>(AccountResourceImpl.class, "patchAccount"));
		_resourceMethodPairs.put(
			"mutation#createAccountLogo",
			new ObjectValuePair<>(
				AccountResourceImpl.class, "postAccountLogo"));
		_resourceMethodPairs.put(
			"mutation#deleteAccountAddressByExternalReferenceCode",
			new ObjectValuePair<>(
				AccountAddressResourceImpl.class,
				"deleteAccountAddressByExternalReferenceCode"));
		_resourceMethodPairs.put(
			"mutation#patchAccountAddressByExternalReferenceCode",
			new ObjectValuePair<>(
				AccountAddressResourceImpl.class,
				"patchAccountAddressByExternalReferenceCode"));
		_resourceMethodPairs.put(
			"mutation#deleteAccountAddress",
			new ObjectValuePair<>(
				AccountAddressResourceImpl.class, "deleteAccountAddress"));
		_resourceMethodPairs.put(
			"mutation#deleteAccountAddressBatch",
			new ObjectValuePair<>(
				AccountAddressResourceImpl.class, "deleteAccountAddressBatch"));
		_resourceMethodPairs.put(
			"mutation#patchAccountAddress",
			new ObjectValuePair<>(
				AccountAddressResourceImpl.class, "patchAccountAddress"));
		_resourceMethodPairs.put(
			"mutation#updateAccountAddress",
			new ObjectValuePair<>(
				AccountAddressResourceImpl.class, "putAccountAddress"));
		_resourceMethodPairs.put(
			"mutation#updateAccountAddressBatch",
			new ObjectValuePair<>(
				AccountAddressResourceImpl.class, "putAccountAddressBatch"));
		_resourceMethodPairs.put(
			"mutation#createAccountByExternalReferenceCodeAccountAddress",
			new ObjectValuePair<>(
				AccountAddressResourceImpl.class,
				"postAccountByExternalReferenceCodeAccountAddress"));
		_resourceMethodPairs.put(
			"mutation#createAccountIdAccountAddress",
			new ObjectValuePair<>(
				AccountAddressResourceImpl.class,
				"postAccountIdAccountAddress"));
		_resourceMethodPairs.put(
			"mutation#createAccountIdAccountAddressBatch",
			new ObjectValuePair<>(
				AccountAddressResourceImpl.class,
				"postAccountIdAccountAddressBatch"));
		_resourceMethodPairs.put(
			"mutation#createAccountGroup",
			new ObjectValuePair<>(
				AccountGroupResourceImpl.class, "postAccountGroup"));
		_resourceMethodPairs.put(
			"mutation#createAccountGroupBatch",
			new ObjectValuePair<>(
				AccountGroupResourceImpl.class, "postAccountGroupBatch"));
		_resourceMethodPairs.put(
			"mutation#deleteAccountGroupByExternalReferenceCode",
			new ObjectValuePair<>(
				AccountGroupResourceImpl.class,
				"deleteAccountGroupByExternalReferenceCode"));
		_resourceMethodPairs.put(
			"mutation#patchAccountGroupByExternalReferenceCode",
			new ObjectValuePair<>(
				AccountGroupResourceImpl.class,
				"patchAccountGroupByExternalReferenceCode"));
		_resourceMethodPairs.put(
			"mutation#deleteAccountGroup",
			new ObjectValuePair<>(
				AccountGroupResourceImpl.class, "deleteAccountGroup"));
		_resourceMethodPairs.put(
			"mutation#deleteAccountGroupBatch",
			new ObjectValuePair<>(
				AccountGroupResourceImpl.class, "deleteAccountGroupBatch"));
		_resourceMethodPairs.put(
			"mutation#patchAccountGroup",
			new ObjectValuePair<>(
				AccountGroupResourceImpl.class, "patchAccountGroup"));
		_resourceMethodPairs.put(
			"mutation#createAccountByExternalReferenceCodeAccountMember",
			new ObjectValuePair<>(
				AccountMemberResourceImpl.class,
				"postAccountByExternalReferenceCodeAccountMember"));
		_resourceMethodPairs.put(
			"mutation#deleteAccountByExternalReferenceCodeAccountMember",
			new ObjectValuePair<>(
				AccountMemberResourceImpl.class,
				"deleteAccountByExternalReferenceCodeAccountMember"));
		_resourceMethodPairs.put(
			"mutation#patchAccountByExternalReferenceCodeAccountMember",
			new ObjectValuePair<>(
				AccountMemberResourceImpl.class,
				"patchAccountByExternalReferenceCodeAccountMember"));
		_resourceMethodPairs.put(
			"mutation#createAccountIdAccountMember",
			new ObjectValuePair<>(
				AccountMemberResourceImpl.class, "postAccountIdAccountMember"));
		_resourceMethodPairs.put(
			"mutation#createAccountIdAccountMemberBatch",
			new ObjectValuePair<>(
				AccountMemberResourceImpl.class,
				"postAccountIdAccountMemberBatch"));
		_resourceMethodPairs.put(
			"mutation#deleteAccountIdAccountMember",
			new ObjectValuePair<>(
				AccountMemberResourceImpl.class,
				"deleteAccountIdAccountMember"));
		_resourceMethodPairs.put(
			"mutation#patchAccountIdAccountMember",
			new ObjectValuePair<>(
				AccountMemberResourceImpl.class,
				"patchAccountIdAccountMember"));
		_resourceMethodPairs.put(
			"mutation#createAccountByExternalReferenceCodeAccountOrganization",
			new ObjectValuePair<>(
				AccountOrganizationResourceImpl.class,
				"postAccountByExternalReferenceCodeAccountOrganization"));
		_resourceMethodPairs.put(
			"mutation#deleteAccountByExternalReferenceCodeAccountOrganization",
			new ObjectValuePair<>(
				AccountOrganizationResourceImpl.class,
				"deleteAccountByExternalReferenceCodeAccountOrganization"));
		_resourceMethodPairs.put(
			"mutation#createAccountIdAccountOrganization",
			new ObjectValuePair<>(
				AccountOrganizationResourceImpl.class,
				"postAccountIdAccountOrganization"));
		_resourceMethodPairs.put(
			"mutation#createAccountIdAccountOrganizationBatch",
			new ObjectValuePair<>(
				AccountOrganizationResourceImpl.class,
				"postAccountIdAccountOrganizationBatch"));
		_resourceMethodPairs.put(
			"mutation#deleteAccountIdAccountOrganization",
			new ObjectValuePair<>(
				AccountOrganizationResourceImpl.class,
				"deleteAccountIdAccountOrganization"));
		_resourceMethodPairs.put(
			"mutation#createAccountByExternalReferenceCodeAccountMemberCreateUser",
			new ObjectValuePair<>(
				UserResourceImpl.class,
				"postAccountByExternalReferenceCodeAccountMemberCreateUser"));
		_resourceMethodPairs.put(
			"query#accounts",
			new ObjectValuePair<>(
				AccountResourceImpl.class, "getAccountsPage"));
		_resourceMethodPairs.put(
			"query#accountByExternalReferenceCode",
			new ObjectValuePair<>(
				AccountResourceImpl.class,
				"getAccountByExternalReferenceCode"));
		_resourceMethodPairs.put(
			"query#account",
			new ObjectValuePair<>(AccountResourceImpl.class, "getAccount"));
		_resourceMethodPairs.put(
			"query#accountAddressByExternalReferenceCode",
			new ObjectValuePair<>(
				AccountAddressResourceImpl.class,
				"getAccountAddressByExternalReferenceCode"));
		_resourceMethodPairs.put(
			"query#accountAddress",
			new ObjectValuePair<>(
				AccountAddressResourceImpl.class, "getAccountAddress"));
		_resourceMethodPairs.put(
			"query#accountByExternalReferenceCodeAccountAddresses",
			new ObjectValuePair<>(
				AccountAddressResourceImpl.class,
				"getAccountByExternalReferenceCodeAccountAddressesPage"));
		_resourceMethodPairs.put(
			"query#accountIdAccountAddresses",
			new ObjectValuePair<>(
				AccountAddressResourceImpl.class,
				"getAccountIdAccountAddressesPage"));
		_resourceMethodPairs.put(
			"query#accountGroups",
			new ObjectValuePair<>(
				AccountGroupResourceImpl.class, "getAccountGroupsPage"));
		_resourceMethodPairs.put(
			"query#accountGroupByExternalReferenceCode",
			new ObjectValuePair<>(
				AccountGroupResourceImpl.class,
				"getAccountGroupByExternalReferenceCode"));
		_resourceMethodPairs.put(
			"query#accountGroup",
			new ObjectValuePair<>(
				AccountGroupResourceImpl.class, "getAccountGroup"));
		_resourceMethodPairs.put(
			"query#accountByExternalReferenceCodeAccountGroups",
			new ObjectValuePair<>(
				AccountGroupResourceImpl.class,
				"getAccountByExternalReferenceCodeAccountGroupsPage"));
		_resourceMethodPairs.put(
			"query#accountIdAccountGroups",
			new ObjectValuePair<>(
				AccountGroupResourceImpl.class,
				"getAccountIdAccountGroupsPage"));
		_resourceMethodPairs.put(
			"query#accountByExternalReferenceCodeAccountMembers",
			new ObjectValuePair<>(
				AccountMemberResourceImpl.class,
				"getAccountByExternalReferenceCodeAccountMembersPage"));
		_resourceMethodPairs.put(
			"query#accountByExternalReferenceCodeAccountMember",
			new ObjectValuePair<>(
				AccountMemberResourceImpl.class,
				"getAccountByExternalReferenceCodeAccountMember"));
		_resourceMethodPairs.put(
			"query#accountIdAccountMembers",
			new ObjectValuePair<>(
				AccountMemberResourceImpl.class,
				"getAccountIdAccountMembersPage"));
		_resourceMethodPairs.put(
			"query#accountIdAccountMember",
			new ObjectValuePair<>(
				AccountMemberResourceImpl.class, "getAccountIdAccountMember"));
		_resourceMethodPairs.put(
			"query#accountByExternalReferenceCodeAccountOrganizations",
			new ObjectValuePair<>(
				AccountOrganizationResourceImpl.class,
				"getAccountByExternalReferenceCodeAccountOrganizationsPage"));
		_resourceMethodPairs.put(
			"query#accountByExternalReferenceCodeAccountOrganization",
			new ObjectValuePair<>(
				AccountOrganizationResourceImpl.class,
				"getAccountByExternalReferenceCodeAccountOrganization"));
		_resourceMethodPairs.put(
			"query#accountIdAccountOrganizations",
			new ObjectValuePair<>(
				AccountOrganizationResourceImpl.class,
				"getAccountIdAccountOrganizationsPage"));
		_resourceMethodPairs.put(
			"query#accountIdAccountOrganization",
			new ObjectValuePair<>(
				AccountOrganizationResourceImpl.class,
				"getAccountIdAccountOrganization"));
	}

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<AccountResource>
		_accountResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<AccountAddressResource>
		_accountAddressResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<AccountGroupResource>
		_accountGroupResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<AccountMemberResource>
		_accountMemberResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<AccountOrganizationResource>
		_accountOrganizationResourceComponentServiceObjects;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	private ComponentServiceObjects<UserResource>
		_userResourceComponentServiceObjects;

}