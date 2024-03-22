<%--
/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/init.jsp" %>

<liferay-layout:render-layout-utility-page-entry
	type="<%= LayoutUtilityPageEntryConstants.TYPE_CREATE_ACCOUNT %>"
>
	<c:choose>
		<c:when test='<%= SessionMessages.contains(request, "userAdded") %>'>
			<div class="alert alert-success">
				<liferay-ui:message key="thank-you-for-creating-an-account" />
			</div>
		</c:when>
		<c:when test="<%= SessionErrors.contains(request, ContactNameException.MustHaveFirstName.class.getName()) %>">
			<clay:alert
				displayType="danger"
				message="the-field-value-is-invalid"
			/>

			<liferay-ui:message key="please-enter-a-valid-first-name" />
		</c:when>
		<c:when test="<%= SessionErrors.contains(request, ContactNameException.MustHaveLastName.class.getName()) %>">
			<clay:alert
				displayType="danger"
				message="the-field-value-is-invalid"
			/>

			<liferay-ui:message key="please-enter-a-valid-last-name" />
		</c:when>
		<c:when test="<%= SessionErrors.contains(request, ContactNameException.MustHaveValidFullName.class.getName()) %>">
			<clay:alert
				displayType="danger"
				message="the-field-value-is-invalid"
			/>

			<liferay-ui:message key="please-enter-a-valid-first-middle-and-last-name" />
		</c:when>
		<c:when test="<%= SessionErrors.contains(request, EmailAddressException.class.getName()) %>">
			<clay:alert
				displayType="danger"
				message="the-field-value-is-invalid"
			/>

			<liferay-ui:message key="please-enter-a-valid-email-address" />
		</c:when>
		<c:when test="<%= SessionErrors.contains(request, UserEmailAddressException.MustNotBeDuplicate.class.getName()) %>">
			<clay:alert
				displayType="danger"
				message="the-field-value-is-invalid"
			/>

			<liferay-ui:message key="the-email-address-you-requested-is-already-taken" />
		</c:when>
		<c:when test="<%= SessionErrors.contains(request, UserEmailAddressException.MustNotBeNull.class.getName()) %>">
			<clay:alert
				displayType="danger"
				message="the-field-value-is-invalid"
			/>

			<liferay-ui:message key="please-enter-an-email-address" />
		</c:when>
		<c:when test="<%= SessionErrors.contains(request, UserEmailAddressException.MustNotBePOP3User.class.getName()) %>">
			<clay:alert
				displayType="danger"
				message="the-field-value-is-invalid"
			/>

			<liferay-ui:message key="the-email-address-you-requested-is-reserved" />
		</c:when>
		<c:when test="<%= SessionErrors.contains(request, UserEmailAddressException.MustNotBeReserved.class.getName()) %>">
			<clay:alert
				displayType="danger"
				message="the-field-value-is-invalid"
			/>

			<liferay-ui:message key="the-email-address-you-requested-is-reserved" />
		</c:when>
		<c:when test="<%= SessionErrors.contains(request, UserEmailAddressException.MustNotUseCompanyMx.class.getName()) %>">
			<clay:alert
				displayType="danger"
				message="the-field-value-is-invalid"
			/>

			<liferay-ui:message key="the-email-address-you-requested-is-not-valid-because-its-domain-is-reserved" />
		</c:when>
		<c:when test="<%= SessionErrors.contains(request, UserEmailAddressException.MustValidate.class.getName()) %>">
			<clay:alert
				displayType="danger"
				message="the-field-value-is-invalid"
			/>

			<liferay-ui:message key="please-enter-a-valid-email-address" />
		</c:when>
		<c:when test="<%= SessionErrors.contains(request, UserScreenNameException.MustNotBeDuplicate.class.getName()) %>">
			<clay:alert
				displayType="danger"
				message="the-field-value-is-invalid"
			/>

			<liferay-ui:message key="the-screen-name-you-requested-is-already-taken" />
		</c:when>
		<c:when test="<%= SessionErrors.contains(request, UserScreenNameException.MustNotBeNull.class.getName()) %>">
			<clay:alert
				displayType="danger"
				message="the-field-value-is-invalid"
			/>

			<liferay-ui:message key="the-screen-name-cannot-be-blank" />
		</c:when>
		<c:when test="<%= SessionErrors.contains(request, UserScreenNameException.MustNotBeNumeric.class.getName()) %>">
			<clay:alert
				displayType="danger"
				message="the-field-value-is-invalid"
			/>

			<liferay-ui:message key="the-screen-name-cannot-contain-only-numeric-values" />
		</c:when>
		<c:when test="<%= SessionErrors.contains(request, UserScreenNameException.MustNotBeReserved.class.getName()) %>">
			<clay:alert
				displayType="danger"
				message="the-field-value-is-invalid"
			/>

			<liferay-ui:message key="the-screen-name-you-requested-is-reserved" />
		</c:when>
		<c:when test="<%= SessionErrors.contains(request, UserScreenNameException.MustNotBeReservedForAnonymous.class.getName()) %>">
			<clay:alert
				displayType="danger"
				message="the-field-value-is-invalid"
			/>

			<liferay-ui:message key="the-screen-name-you-requested-is-reserved-for-the-anonymous-user" />
		</c:when>
		<c:when test="<%= SessionErrors.contains(request, UserScreenNameException.MustNotBeUsedByGroup.class.getName()) %>">
			<clay:alert
				displayType="danger"
				message="the-field-value-is-invalid"
			/>

			<liferay-ui:message key="the-screen-name-you-requested-is-already-taken-by-a-site" />
		</c:when>
		<c:when test="<%= SessionErrors.contains(request, UserScreenNameException.MustProduceValidFriendlyURL.class.getName()) %>">
			<clay:alert
				displayType="danger"
				message="the-field-value-is-invalid"
			/>

			<liferay-ui:message key="the-screen-name-you-requested-must-produce-a-valid-friendly-url" />
		</c:when>
		<c:when test="<%= SessionErrors.contains(request, UserScreenNameException.MustValidate.class.getName()) %>">
			<clay:alert
				displayType="danger"
				message="the-field-value-is-invalid"
			/>

			<%
			Object errorException = SessionErrors.get(request, UserScreenNameException.MustValidate.class.getName());

			UserScreenNameException.MustValidate usne = (UserScreenNameException.MustValidate)errorException;
			%>

			<liferay-ui:message key="<%= usne.screenNameValidator.getDescription(locale) %>" />
		</c:when>
		<c:when test="<%= SessionErrors.contains(request, UserPasswordException.MustBeLonger.class.getName()) %>">
			<clay:alert
				displayType="danger"
				message="the-field-value-is-invalid"
			/>

			<%
			Object errorException = SessionErrors.get(request, UserScreenNameException.MustValidate.class.getName());

			UserPasswordException.MustBeLonger upe = (UserPasswordException.MustBeLonger)errorException;
			%>

			<liferay-ui:message arguments="<%= String.valueOf(upe.minLength) %>" key="that-password-is-too-short" translateArguments="<%= false %>" />
		</c:when>
		<c:when test="<%= SessionErrors.contains(request, UserPasswordException.MustComplyWithModelListeners.class.getName()) %>">
			<clay:alert
				displayType="danger"
				message="the-field-value-is-invalid"
			/>

			<liferay-ui:message key="that-password-is-invalid-please-enter-a-different-password" />
		</c:when>
		<c:when test="<%= SessionErrors.contains(request, UserPasswordException.MustComplyWithRegex.class.getName()) %>">
			<clay:alert
				displayType="danger"
				message="the-field-value-is-invalid"
			/>

			<%
			Object errorException = SessionErrors.get(request, UserScreenNameException.MustValidate.class.getName());

			UserPasswordException.MustComplyWithRegex upe = (UserPasswordException.MustComplyWithRegex)errorException;
			%>

			<liferay-ui:message arguments="<%= HtmlUtil.escape(upe.regex) %>" key="that-password-does-not-comply-with-the-regular-expression" translateArguments="<%= false %>" />
		</c:when>
		<c:when test="<%= SessionErrors.contains(request, UserPasswordException.MustHaveMoreNumbers.class.getName()) %>">
			<clay:alert
				displayType="danger"
				message="the-field-value-is-invalid"
			/>

			<%
			Object errorException = SessionErrors.get(request, UserScreenNameException.MustValidate.class.getName());

			UserPasswordException.MustHaveMoreNumbers upe = (UserPasswordException.MustHaveMoreNumbers)errorException;
			%>

			<liferay-ui:message arguments="<%= String.valueOf(upe.minNumbers) %>" key="that-password-must-contain-at-least-x-numbers" translateArguments="<%= false %>" />
		</c:when>
		<c:when test="<%= SessionErrors.contains(request, UserPasswordException.MustHaveMoreSymbols.class.getName()) %>">
			<clay:alert
				displayType="danger"
				message="the-field-value-is-invalid"
			/>

			<%
			Object errorException = SessionErrors.get(request, UserScreenNameException.MustValidate.class.getName());

			UserPasswordException.MustHaveMoreSymbols upe = (UserPasswordException.MustHaveMoreSymbols)errorException;
			%>

			<liferay-ui:message arguments="<%= String.valueOf(upe.minSymbols) %>" key="that-password-must-contain-at-least-x-symbols" translateArguments="<%= false %>" />
		</c:when>
		<c:when test="<%= SessionErrors.contains(request, UserPasswordException.MustHaveMoreUppercase.class.getName()) %>">
			<clay:alert
				displayType="danger"
				message="the-field-value-is-invalid"
			/>

			<%
			Object errorException = SessionErrors.get(request, UserScreenNameException.MustValidate.class.getName());

			UserPasswordException.MustHaveMoreUppercase upe = (UserPasswordException.MustHaveMoreUppercase)errorException;
			%>

			<liferay-ui:message arguments="<%= String.valueOf(upe.minUppercase) %>" key="that-password-must-contain-at-least-x-uppercase-characters" translateArguments="<%= false %>" />
		</c:when>
		<c:when test="<%= SessionErrors.contains(request, UserPasswordException.MustMatch.class.getName()) %>">
			<clay:alert
				displayType="danger"
				message="the-field-value-is-invalid"
			/>

			<liferay-ui:message key="the-passwords-you-entered-do-not-match" />
		</c:when>
		<c:when test="<%= SessionErrors.contains(request, UserPasswordException.MustNotBeNull.class.getName()) %>">
			<clay:alert
				displayType="danger"
				message="the-field-value-is-invalid"
			/>

			<liferay-ui:message key="the-password-cannot-be-blank" />
		</c:when>
		<c:when test="<%= SessionErrors.contains(request, UserPasswordException.MustNotBeTrivial.class.getName()) %>">
			<clay:alert
				displayType="danger"
				message="the-field-value-is-invalid"
			/>

			<liferay-ui:message key="that-password-uses-common-words-please-enter-a-password-that-is-harder-to-guess-i-e-contains-a-mix-of-numbers-and-letters" />
		</c:when>
		<c:when test="<%= SessionErrors.contains(request, UserPasswordException.MustNotContainDictionaryWords.class.getName()) %>">
			<clay:alert
				displayType="danger"
				message="the-field-value-is-invalid"
			/>

			<liferay-ui:message key="that-password-uses-common-dictionary-words" />
		</c:when>
	</c:choose>
</liferay-layout:render-layout-utility-page-entry>