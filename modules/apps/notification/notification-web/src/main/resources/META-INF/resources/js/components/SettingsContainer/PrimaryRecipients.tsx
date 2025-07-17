/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayForm, {ClayCheckbox} from '@clayui/form';
import ClayIcon from '@clayui/icon';
import {ClayTooltipProvider} from '@clayui/tooltip';
import {
	FormError,
	Input,
	MultiSelectItem,
	MultipleSelect,
	SingleSelect,
} from '@liferay/object-js-components-web';
import {
	ILearnResourceContext,
	InputLocalized,
	LearnMessage,
	LearnResourcesContext,
} from 'frontend-js-components-web';
import React, {useEffect, useState} from 'react';

import {NotificationTemplateError} from '../EditNotificationTemplate';
import {
	getCheckedChildren,
	handleMultiSelectItemsChange,
	uncheckMultiSelectItemChildrens,
} from './multiSelectUtil';

interface PrimaryRecipientProps {
	emailNotificationRoles: MultiSelectItem[];
	emailNotificationUserGroups: MultiSelectItem[];
	errors: FormError<NotificationTemplate & NotificationTemplateError>;
	learnResources: ILearnResourceContext;
	recipientOptions: LabelValueObject[];
	selectedLocale: Locale;
	setValues: (values: Partial<NotificationTemplate>) => void;
	values: NotificationTemplate;
}

export function getSubscribersDefaultRole() {
	return '[%EMAIL_RECIPIENT_ADDRESS%]';
}

export function resetRecipientValue(value: React.Key) {
	return value === 'subscribers' ? getSubscribersDefaultRole() : [];
}

export function PrimaryRecipient({
	emailNotificationRoles,
	emailNotificationUserGroups,
	errors,
	learnResources,
	recipientOptions,
	selectedLocale,
	setValues,
	values,
}: PrimaryRecipientProps) {
	const [recipient] = values.recipients as EmailRecipients[];
	const [toRolesList, setToRolesList] = useState<MultiSelectItem[]>([]);
	const [toUserGroupsList, setToUserGroupsList] = useState<MultiSelectItem[]>(
		[]
	);

	useEffect(() => {
		if (emailNotificationRoles.length && !toRolesList.length) {
			setToRolesList(emailNotificationRoles);
		}

		if (emailNotificationUserGroups.length && !toUserGroupsList.length) {
			setToUserGroupsList(emailNotificationUserGroups);
		}

		if (Array.isArray(recipient.to) && !!recipient.to.length) {
			if (
				recipient.toType === 'role' &&
				(!!toRolesList.length || !!emailNotificationRoles.length)
			) {
				const baseRoleList = toRolesList.length
					? toRolesList
					: emailNotificationRoles;

				setToRolesList(
					baseRoleList.map((baseRoleElement) => {
						return {
							...baseRoleElement,
							children: getCheckedChildren(
								recipient.to as EmailNotificationRecipients[],
								baseRoleElement.children
							),
						};
					})
				);
			}
			else if (
				recipient.toType === 'user-group' &&
				(!!toUserGroupsList.length ||
					!!emailNotificationUserGroups.length)
			) {
				const baseUserGroupList = toUserGroupsList.length
					? toUserGroupsList
					: emailNotificationUserGroups;

				setToUserGroupsList(
					baseUserGroupList.map((baseUserGroupElement) => {
						return {
							...baseUserGroupElement,
							children: getCheckedChildren(
								recipient.to as EmailNotificationRecipients[],
								baseUserGroupElement.children
							),
						};
					})
				);
			}

			return;
		}

		// eslint-disable-next-line react-hooks/exhaustive-deps
	}, [emailNotificationRoles, emailNotificationUserGroups, recipient.to]);

	return (
		<>
			<SingleSelect<LabelValueObject>
				disabled={values.system}
				id="primaryRecipientType"
				items={recipientOptions}
				label={Liferay.Language.get('type')}
				onSelectionChange={(value) => {
					if (value !== 'role') {
						const newToRoleList =
							uncheckMultiSelectItemChildrens(toRolesList);
						setToRolesList(newToRoleList);
					}

					if (value !== 'user-group') {
						const newToUserGroupList =
							uncheckMultiSelectItemChildrens(toUserGroupsList);
						setToUserGroupsList(newToUserGroupList);
					}
					setValues({
						...values,
						recipients: [
							{
								...recipient,
								to: resetRecipientValue(value),
								toType: value as string,
							},
						],
					});
				}}
				required
				selectedKey={recipient.toType}
			/>

			{recipient.toType === 'email' ? (
				<div className="lfr__notification-template-email-notification-settings-primary-recipient-input-localized">
					<InputLocalized
						disabled={values.system}
						error={errors.to}
						helpMessage={Liferay.Language.get(
							'you-can-use-a-comma-to-enter-multiple-users'
						)}
						id="primaryRecipients"
						label={Liferay.Language.get('recipients')}
						name="recipients"
						onChange={(translation) => {
							setValues({
								...values,
								recipients: [
									{
										...recipient,
										to: translation,
									},
								],
							});
						}}
						placeholder={Liferay.Language.get('type-email-address')}
						required
						selectedLocale={selectedLocale}
						translations={recipient.to as LocalizedValue<string>}
					/>
				</div>
			) : (
				<div className="lfr__notification-template-email-notification-settings-multiple-select">
					<MultipleSelect
						disabled={values.system}
						error={errors.to}
						id={
							recipient.toType === 'role'
								? 'primaryRecipientRoles'
								: 'primaryRecipientUserGroups'
						}
						label={
							recipient.toType === 'role'
								? Liferay.Language.get('role')
								: Liferay.Language.get('user-group')
						}
						options={
							recipient.toType === 'role'
								? toRolesList
								: toUserGroupsList
						}
						placeholder={
							recipient.toType === 'role'
								? Liferay.Language.get('select-role')
								: Liferay.Language.get('select-user-group')
						}
						required
						search
						searchPlaceholder={
							recipient.toType === 'role'
								? Liferay.Language.get('search-for-a-role')
								: Liferay.Language.get(
										'search-for-a-user-group'
									)
						}
						selectAllOption
						setOptions={(items) => {
							const newRecipients =
								handleMultiSelectItemsChange(items);

							setValues({
								...values,
								recipients: [
									{
										...recipient,
										to: newRecipients,
									},
								],
							});

							if (recipient.toType === 'role') {
								setToRolesList(items);
							}
							else {
								setToUserGroupsList(items);
							}
						}}
					/>

					{recipient.toType === 'role' && (
						<LearnResourcesContext.Provider value={learnResources}>
							<div className="lfr__notification-template-email-notification-settings-multiple-select-help-text">
								<span>
									{Liferay.Language.get(
										'account-roles-are-subject-to-account-restrictions'
									)}
								</span>
								&nbsp;
								<LearnMessage
									className="alert-link"
									resource="notification-web"
									resourceKey="general"
								/>
							</div>
						</LearnResourcesContext.Provider>
					)}
				</div>
			)}

			{recipient.toType === 'subscribers' && (
				<div className="lfr__notification-template-email-notification-settings-primary-recipient-input-not-localized">
					<Input
						disabled
						id="subscribersRecipients"
						label={Liferay.Language.get('recipients')}
						name="recipients"
						required
						value={getSubscribersDefaultRole()}
					/>
				</div>
			)}

			<>
				<ClayForm.Group className="ml-1 row">
					<div className="mr-2">
						<ClayCheckbox
							checked={recipient.singleRecipient}
							disabled={values.system}
							label={Liferay.Language.get(
								'send-emails-separately'
							)}
							onChange={({target: {checked}}) => {
								setValues({
									...values,
									recipients: [
										{
											...recipient,
											singleRecipient: checked,
										},
									],
								});
							}}
						/>
					</div>

					<ClayTooltipProvider>
						<span
							title={Liferay.Language.get(
								'each-to-recipient-will-receive-separate-emails'
							)}
						>
							<ClayIcon
								className="lfr__notification-template-email-notification-settings-tooltip-icon"
								symbol="question-circle-full"
							/>
						</span>
					</ClayTooltipProvider>
				</ClayForm.Group>
			</>
		</>
	);
}
