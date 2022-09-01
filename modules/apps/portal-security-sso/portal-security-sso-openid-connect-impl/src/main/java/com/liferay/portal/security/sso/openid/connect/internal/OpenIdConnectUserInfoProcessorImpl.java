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

package com.liferay.portal.security.sso.openid.connect.internal;

import com.liferay.oauth.client.persistence.model.OAuthClientEntry;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.UserEmailAddressException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Contact;
import com.liferay.portal.kernel.model.Country;
import com.liferay.portal.kernel.model.ListType;
import com.liferay.portal.kernel.model.Region;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.service.AddressLocalService;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.ContactLocalService;
import com.liferay.portal.kernel.service.CountryLocalService;
import com.liferay.portal.kernel.service.ListTypeLocalService;
import com.liferay.portal.kernel.service.PhoneLocalService;
import com.liferay.portal.kernel.service.RegionLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.Props;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.security.sso.openid.connect.OpenIdConnectServiceException;
import com.liferay.portal.security.sso.openid.connect.internal.exception.StrangersNotAllowedException;

import com.nimbusds.openid.connect.sdk.claims.UserInfo;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import net.minidev.json.JSONArray;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Michael C. Han
 */
@Component(immediate = true, service = OpenIdConnectUserInfoProcessor.class)
public class OpenIdConnectUserInfoProcessorImpl
	implements OpenIdConnectUserInfoProcessor {

	@Override
	public long processUserInfo(
			UserInfo userInfo, long companyId, String issuer, String mainPath,
			OAuthClientEntry oAuthClientEntry, String portalURL)
		throws PortalException {

		JSONObject userInfoMapperJSONObject = JSONFactoryUtil.createJSONObject(
			oAuthClientEntry.getOidcUserInfoMapperJSON());

		JSONObject userMapperJSONObject =
			userInfoMapperJSONObject.getJSONObject("user");

		User user = _userLocalService.fetchUserByEmailAddress(
			companyId,
			userInfo.getStringClaim(
				userMapperJSONObject.getString("emailAddress")));

		if (user != null) {
			return user.getUserId();
		}

		ServiceContext serviceContext = new ServiceContext();

		serviceContext.setPathMain(mainPath);
		serviceContext.setPortalURL(portalURL);

		user = _generateUser(
			companyId, issuer, serviceContext, userInfo,
			userInfoMapperJSONObject.getJSONObject("user_contact"),
			userMapperJSONObject,
			userInfoMapperJSONObject.getJSONObject("user_roles"));

		_addAddress(
			userInfoMapperJSONObject.getJSONObject("user_address"),
			serviceContext, user, userInfo);

		_addPhone(
			userInfoMapperJSONObject.getJSONObject("user_phone"),
			serviceContext, user, userInfo);

		return user.getUserId();
	}

	private void _addAddress(
		JSONObject userAddressMapperJSONObject, ServiceContext serviceContext,
		User user, UserInfo userInfo) {

		if (userAddressMapperJSONObject == null) {
			return;
		}

		try {
			String city = userInfo.getStringClaim(
				userAddressMapperJSONObject.getString("city"));

			ListType contactAddressListType = _listTypeLocalService.getListType(
				userInfo.getStringClaim(
					userAddressMapperJSONObject.getString("addressType")),
				Contact.class.getName() + ".address");

			if (contactAddressListType == null) {

				// Type is not a must by contract, but required by Liferay

				List<ListType> contactAddressListTypes =
					_listTypeLocalService.getListTypes(
						Contact.class.getName() + ".address");

				contactAddressListType = contactAddressListTypes.get(0);
			}

			Country country = _countryLocalService.getCountryByName(
				user.getCompanyId(),
				userInfo.getStringClaim(
					userAddressMapperJSONObject.getString("country")));

			Region region = _regionLocalService.getRegion(
				country.getCountryId(),
				userInfo.getStringClaim(
					userAddressMapperJSONObject.getString("region")));

			String street = userInfo.getStringClaim(
				userAddressMapperJSONObject.getString("street"));

			String[] streetParts = street.split("\n");

			String zip = userInfo.getStringClaim(
				userAddressMapperJSONObject.getString("zip"));

			_addressLocalService.addAddress(
				null, user.getUserId(), Contact.class.getName(),
				user.getContactId(), null, null,
				(streetParts.length > 0) ? streetParts[0] : null,
				(streetParts.length > 1) ? streetParts[1] : null,
				(streetParts.length > 2) ? streetParts[2] : null, city, zip,
				region.getRegionId(), country.getCountryId(),
				contactAddressListType.getListTypeId(), false, false, null,
				serviceContext);
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to add address for user: " + user.getUserId(),
					exception);
			}
		}
	}

	private void _addPhone(
		JSONObject userPhoneMapperJSONObject, ServiceContext serviceContext,
		User user, UserInfo userInfo) {

		if (userPhoneMapperJSONObject == null) {
			return;
		}

		try {
			ListType contactPhoneListType = _listTypeLocalService.getListType(
				userInfo.getStringClaim(
					userPhoneMapperJSONObject.getString("phoneType")),
				Contact.class.getName() + ".phone");

			if (contactPhoneListType == null) {

				// Type is not a must by contract, but required by Liferay

				List<ListType> contactPhoneListTypes =
					_listTypeLocalService.getListTypes(
						Contact.class.getName() + ".phone");

				contactPhoneListType = contactPhoneListTypes.get(0);
			}

			String phone = userInfo.getStringClaim(
				userPhoneMapperJSONObject.getString("phone"));

			_phoneLocalService.addPhone(
				user.getUserId(), Contact.class.getName(), user.getContactId(),
				phone, null, contactPhoneListType.getListTypeId(), false,
				serviceContext);
		}
		catch (PortalException portalException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to add phone for user: " + user.getUserId(),
					portalException);
			}
		}
	}

	private void _checkAddUser(long companyId, String emailAddress)
		throws PortalException {

		Company company = _companyLocalService.getCompany(companyId);

		if (!company.isStrangers()) {
			throw new StrangersNotAllowedException(companyId);
		}

		if (!company.isStrangersWithMx() &&
			company.hasCompanyMx(emailAddress)) {

			throw new UserEmailAddressException.MustNotUseCompanyMx(
				emailAddress);
		}
	}

	private User _generateUser(
			long companyId, String issuer, ServiceContext serviceContext,
			UserInfo userInfo, JSONObject userContactMapperJSONObject,
			JSONObject userMapperJSONObject,
			JSONObject userRolesMapperJSONObject)
		throws PortalException {

		String emailAddress = userInfo.getStringClaim(
			userMapperJSONObject.getString("emailAddress"));
		String firstName = userInfo.getStringClaim(
			userMapperJSONObject.getString("firstName"));
		String lastName = userInfo.getStringClaim(
			userMapperJSONObject.getString("lastName"));

		if (Validator.isNull(firstName) || Validator.isNull(lastName) ||
			Validator.isNull(emailAddress)) {

			throw new OpenIdConnectServiceException.UserMappingException(
				StringBundler.concat(
					"Unable to map OpenId Connect user to the portal, missing ",
					"or invalid profile information: {emailAddresss=",
					emailAddress, ", firstName=", firstName, ", lastName=",
					lastName, "}"));
		}

		_checkAddUser(companyId, emailAddress);

		long creatorUserId = 0;
		boolean autoPassword = true;
		String password1 = null;
		String password2 = null;
		String screenName = userInfo.getStringClaim(
			userMapperJSONObject.getString("screenName"));
		long prefixId = 0;
		long suffixId = 0;
		String middleName = userInfo.getStringClaim(
			userMapperJSONObject.getString("middleName"));
		int[] birthday = _getBirthday(userContactMapperJSONObject, userInfo);
		String jobTitle = userInfo.getStringClaim(
			userContactMapperJSONObject.getString("jobTitle"));
		long[] groupIds = null;
		long[] organizationIds = null;
		long[] userGroupIds = null;
		boolean sendEmail = false;

		User user = _userLocalService.addUser(
			creatorUserId, companyId, autoPassword, password1, password2,
			Validator.isNull(screenName), screenName, emailAddress,
			_getLocale(companyId, userInfo, userMapperJSONObject), firstName,
			middleName, lastName, prefixId, suffixId,
			_isMale(userInfo, userMapperJSONObject), birthday[1], birthday[2],
			birthday[0], jobTitle, groupIds, organizationIds,
			_getRoleIds(companyId, issuer, userInfo, userRolesMapperJSONObject),
			userGroupIds, sendEmail, serviceContext);

		return _userLocalService.updatePasswordReset(user.getUserId(), false);
	}

	private int[] _getBirthday(
		JSONObject userContactMapperJSONObject, UserInfo userInfo) {

		int[] birthday = new int[3];

		birthday[0] = 1970;
		birthday[1] = Calendar.JANUARY;
		birthday[2] = 1;

		String birthdate = userInfo.getStringClaim(
			userContactMapperJSONObject.getString("birthdate"));

		if (Validator.isNull(birthdate)) {
			return birthday;
		}

		try {
			String[] birthdateParts = birthdate.split("-");

			if (!birthdateParts[0].equals("0000")) {
				birthday[0] = Integer.parseInt(birthdateParts[0]);
			}

			if (birthdateParts.length == 3) {
				birthday[1] = Integer.parseInt(birthdateParts[1]);
				birthday[2] = Integer.parseInt(birthdateParts[2]);
			}
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) {
				_log.warn("Unable to parse user birthday, use default value");
			}
		}

		return birthday;
	}

	private Locale _getLocale(
			long companyId, UserInfo userInfo, JSONObject userMapperJSONObject)
		throws PortalException {

		String languageId = userInfo.getStringClaim(
			userMapperJSONObject.getString("languageId"));

		if (Validator.isNotNull(languageId)) {
			return new Locale(languageId);
		}

		Company company = _companyLocalService.getCompany(companyId);

		return company.getLocale();
	}

	private long[] _getRoleIds(long companyId, String issuer) {
		if (Validator.isNull(issuer) ||
			!Objects.equals(
				issuer,
				_props.get(
					"open.id.connect.user.info.processor.impl.issuer"))) {

			return null;
		}

		String roleName = _props.get(
			"open.id.connect.user.info.processor.impl.regular.role");

		if (Validator.isNull(roleName)) {
			return null;
		}

		Role role = _roleLocalService.fetchRole(companyId, roleName);

		if (role == null) {
			return null;
		}

		if (role.getType() == RoleConstants.TYPE_REGULAR) {
			return new long[] {role.getRoleId()};
		}

		if (_log.isInfoEnabled()) {
			_log.info("Role " + roleName + " is not a regular role");
		}

		return null;
	}

	private long[] _getRoleIds(
		long companyId, String issuer, UserInfo userInfo,
		JSONObject userRolesMapperJSONObject) {

		if ((userRolesMapperJSONObject == null) ||
			(userRolesMapperJSONObject.length() < 1)) {

			// Current workaround for LXC,
			// can be removed when LXC migrates to new approach

			return _getRoleIds(companyId, issuer);
		}

		try {
			JSONArray rolesJSONArray = (JSONArray)userInfo.getClaim(
				userRolesMapperJSONObject.getString("roles"));

			long[] roleIds = new long[rolesJSONArray.size()];

			for (int i = 0; i < rolesJSONArray.size(); ++i) {
				Role role = _roleLocalService.fetchRole(
					companyId, (String)rolesJSONArray.get(i));

				roleIds[i] = role.getRoleId();
			}

			return roleIds;
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) {
				_log.warn("Unable to assign roles " + exception);
			}

			return null;
		}
	}

	private boolean _isMale(
		UserInfo userInfo, JSONObject userMapperJSONObject) {

		String gender = userInfo.getStringClaim(
			userMapperJSONObject.getString("gender"));

		if (Validator.isNull(gender) || gender.equals("male")) {
			return true;
		}

		return false;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		OpenIdConnectUserInfoProcessorImpl.class);

	@Reference
	private AddressLocalService _addressLocalService;

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private ContactLocalService _contactLocalService;

	@Reference
	private CountryLocalService _countryLocalService;

	@Reference
	private ListTypeLocalService _listTypeLocalService;

	@Reference
	private PhoneLocalService _phoneLocalService;

	@Reference
	private Props _props;

	@Reference
	private RegionLocalService _regionLocalService;

	@Reference
	private RoleLocalService _roleLocalService;

	@Reference
	private UserLocalService _userLocalService;

}