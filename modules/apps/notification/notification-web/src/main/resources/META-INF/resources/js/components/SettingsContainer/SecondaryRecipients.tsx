/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayPanel from '@clayui/panel';
import {
	Input,
	MultiSelectItem,
	MultipleSelect,
	SingleSelect,
} from '@liferay/object-js-components-web';
import {
	ILearnResourceContext,
	LearnMessage,
	LearnResourcesContext,
} from 'frontend-js-components-web';
import React, {useEffect, useState} from 'react';

import {
	getCheckedChildren,
	handleMultiSelectItemsChange,
	uncheckMultiSelectItemChildrens,
} from './multiSelectUtil';

interface SecondaryRecipientsProps {
	emailNotificationRoles: MultiSelectItem[];
	emailNotificationUserGroups: MultiSelectItem[];
	learnResources: ILearnResourceContext;
	recipientOptions: LabelValueObject[];
	setValues: (values: Partial<NotificationTemplate>) => void;
	values: NotificationTemplate;
}

export function resetRecipientTypeValue(newRecipientTypeValue: string) {
	if (newRecipientTypeValue === 'email') {
		return '';
	}

	return [];
}

export function SecondaryRecipient({
	emailNotificationRoles,
	emailNotificationUserGroups,
	learnResources,
	recipientOptions,
	setValues,
	values,
}: SecondaryRecipientsProps) {
	const [bccRolesList, setBCCRolesList] = useState<MultiSelectItem[]>([]);
	const [bccUserGroupsList, setBCCUserGroupsList] = useState<
		MultiSelectItem[]
	>([]);
	const [ccRolesList, setCCRolesList] = useState<MultiSelectItem[]>([]);
	const [ccUserGroupsList, setCCUserGroupsList] = useState<MultiSelectItem[]>(
		[]
	);
	const [recipient] = values.recipients as EmailRecipients[];

	const handleRecipientItemChange = (
		items: MultiSelectItem[],
		recipientKey: 'cc' | 'bcc',
		setItemList: (value: MultiSelectItem[]) => void
	) => {
		const newRecipients = handleMultiSelectItemsChange(items);

		setValues({
			...values,
			recipients: [
				{
					...(values.recipients[0] as EmailRecipients),
					[recipientKey]: newRecipients,
				},
			],
		});

		setItemList(items);
	};

	const handleRecipientTypeChange = (
		newRecipientTypeValue: string,
		recipientKey: 'cc' | 'bcc',
		roleList: MultiSelectItem[],
		recipientTypeKey: 'ccType' | 'bccType',
		setRoleList: (value: MultiSelectItem[]) => void,
		setUserGroupList: (value: MultiSelectItem[]) => void,
		userGroupList: MultiSelectItem[]
	) => {
		if (newRecipientTypeValue !== 'role') {
			const newRoleList = uncheckMultiSelectItemChildrens(roleList);
			setRoleList(newRoleList);
		}
		if (newRecipientTypeValue !== 'user-group') {
			const newUserGroupList =
				uncheckMultiSelectItemChildrens(userGroupList);
			setUserGroupList(newUserGroupList);
		}
		setValues({
			...values,
			recipients: [
				{
					...recipient,
					[recipientKey]: resetRecipientTypeValue(
						newRecipientTypeValue
					),
					[recipientTypeKey]: newRecipientTypeValue as string,
				},
			],
		});
	};

	useEffect(() => {
		if (emailNotificationRoles.length && !ccRolesList.length) {
			setCCRolesList(emailNotificationRoles);
		}

		if (emailNotificationUserGroups.length && !ccUserGroupsList.length) {
			setCCUserGroupsList(emailNotificationUserGroups);
		}

		if (Array.isArray(recipient.cc) && !!recipient.cc.length) {
			if (
				recipient.ccType === 'role' &&
				(!!ccRolesList.length || !!emailNotificationRoles.length)
			) {
				const baseRoleList = ccRolesList.length
					? ccRolesList
					: emailNotificationRoles;

				setCCRolesList(
					baseRoleList.map((baseRoleElement) => {
						return {
							...baseRoleElement,
							children: getCheckedChildren(
								recipient.cc as EmailNotificationRecipients[],
								baseRoleElement.children
							),
						};
					})
				);
			}
			else if (
				recipient.ccType === 'user-group' &&
				(!!ccUserGroupsList.length ||
					!!emailNotificationUserGroups.length)
			) {
				const baseUserGroupList = ccUserGroupsList.length
					? ccUserGroupsList
					: emailNotificationUserGroups;

				setCCUserGroupsList(
					baseUserGroupList.map((baseUserGroupElement) => {
						return {
							...baseUserGroupElement,
							children: getCheckedChildren(
								recipient.cc as EmailNotificationRecipients[],
								baseUserGroupElement.children
							),
						};
					})
				);
			}

			return;
		}

		// eslint-disable-next-line react-hooks/exhaustive-deps
	}, [emailNotificationRoles, emailNotificationUserGroups, recipient.cc]);

	useEffect(() => {
		if (emailNotificationRoles.length && !bccRolesList.length) {
			setBCCRolesList(emailNotificationRoles);
		}

		if (emailNotificationUserGroups.length && !bccUserGroupsList.length) {
			setBCCUserGroupsList(emailNotificationUserGroups);
		}

		if (Array.isArray(recipient.bcc) && !!recipient.bcc.length) {
			if (
				recipient.bccType === 'role' &&
				(!!bccRolesList.length || !!emailNotificationRoles.length)
			) {
				const baseRoleList = bccRolesList.length
					? bccRolesList
					: emailNotificationRoles;

				setBCCRolesList(
					baseRoleList.map((baseRoleElement) => {
						return {
							...baseRoleElement,
							children: getCheckedChildren(
								recipient.bcc as EmailNotificationRecipients[],
								baseRoleElement.children
							),
						};
					})
				);
			}
			else if (
				recipient.bccType === 'user-group' &&
				(!!bccUserGroupsList.length ||
					!!emailNotificationUserGroups.length)
			) {
				const baseUserGroupList = bccUserGroupsList.length
					? bccUserGroupsList
					: emailNotificationUserGroups;

				setBCCUserGroupsList(
					baseUserGroupList.map((baseUserGroupElement) => {
						return {
							...baseUserGroupElement,
							children: getCheckedChildren(
								recipient.bcc as EmailNotificationRecipients[],
								baseUserGroupElement.children
							),
						};
					})
				);
			}

			return;
		}

		// eslint-disable-next-line react-hooks/exhaustive-deps
	}, [emailNotificationRoles, emailNotificationUserGroups, recipient.bcc]);

	return (
		<>
			<ClayPanel
				displayTitle={Liferay.Language.get('cc')}
				displayType="unstyled"
			>
				<ClayPanel.Body>
					<div className="row">
						<div className="col-lg-6">
							<SingleSelect<LabelValueObject>
								disabled={values.system}
								id="secondaryRecipientTypeCC"
								items={recipientOptions}
								label={Liferay.Language.get('type')}
								onSelectionChange={(value) => {
									handleRecipientTypeChange(
										value as string,
										'cc',
										ccRolesList,
										'ccType',
										setCCRolesList,
										setCCUserGroupsList,
										ccUserGroupsList
									);
								}}
								selectedKey={recipient.ccType}
							/>
						</div>

						<div className="col-lg-6">
							{recipient.ccType === 'email' ? (
								<Input
									disabled={values.system}
									feedbackMessage={Liferay.Language.get(
										'you-can-use-a-comma-to-enter-multiple-users'
									)}
									id="secondaryRecipientsCC"
									label={Liferay.Language.get('recipients')}
									name="secondaryRecipientsCC"
									onChange={({target}) =>
										setValues({
											...values,
											recipients: [
												{
													...values.recipients[0],
													cc: target.value,
												},
											],
										})
									}
									placeholder={Liferay.Language.get(
										'type-email-address'
									)}
									value={
										(
											values
												.recipients[0] as EmailRecipients
										).cc as string
									}
								/>
							) : (
								<div className="lfr__notification-template-email-notification-settings-multiple-select">
									<MultipleSelect
										disabled={values.system}
										id={
											recipient.ccType === 'role'
												? 'secondaryRecipientRolesCC'
												: 'secondaryRecipientUserGroupsCC'
										}
										label={
											recipient.ccType === 'role'
												? Liferay.Language.get('role')
												: Liferay.Language.get(
														'user-group'
													)
										}
										options={
											recipient.ccType === 'role'
												? ccRolesList
												: ccUserGroupsList
										}
										placeholder={
											recipient.ccType === 'role'
												? Liferay.Language.get(
														'select-role'
													)
												: Liferay.Language.get(
														'select-user-group'
													)
										}
										search
										searchPlaceholder={
											recipient.ccType === 'role'
												? Liferay.Language.get(
														'search-for-a-role'
													)
												: Liferay.Language.get(
														'search-for-a-user-group'
													)
										}
										selectAllOption
										setOptions={(items) => {
											handleRecipientItemChange(
												items,
												'cc',
												recipient.ccType === 'role'
													? setCCRolesList
													: setCCUserGroupsList
											);
										}}
									/>

									{recipient.toType === 'role' && (
										<LearnResourcesContext.Provider
											value={learnResources}
										>
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
						</div>
					</div>
				</ClayPanel.Body>
			</ClayPanel>

			<ClayPanel
				displayTitle={Liferay.Language.get('bcc')}
				displayType="unstyled"
			>
				<ClayPanel.Body>
					<div className="row">
						<div className="col-lg-6">
							<SingleSelect<LabelValueObject>
								disabled={values.system}
								id="secondaryRecipientTypeBCC"
								items={recipientOptions}
								label={Liferay.Language.get('type')}
								onSelectionChange={(value) => {
									handleRecipientTypeChange(
										value as string,
										'bcc',
										bccRolesList,
										'bccType',
										setBCCRolesList,
										setBCCUserGroupsList,
										bccUserGroupsList
									);
								}}
								selectedKey={recipient.bccType}
							/>
						</div>

						<div className="col-lg-6">
							{recipient.bccType === 'email' ? (
								<Input
									disabled={values.system}
									feedbackMessage={Liferay.Language.get(
										'you-can-use-a-comma-to-enter-multiple-users'
									)}
									id="secondaryRecipientsBCC"
									label={Liferay.Language.get('recipients')}
									name="secondaryRecipientsBCC"
									onChange={({target}) =>
										setValues({
											...values,
											recipients: [
												{
													...values.recipients[0],
													bcc: target.value,
												},
											],
										})
									}
									placeholder={Liferay.Language.get(
										'type-email-address'
									)}
									value={
										(
											values
												.recipients[0] as EmailRecipients
										).bcc as string
									}
								/>
							) : (
								<div className="lfr__notification-template-email-notification-settings-multiple-select">
									<MultipleSelect
										disabled={values.system}
										id={
											recipient.bccType === 'role'
												? 'secondaryRecipientRolesBCC'
												: 'secondaryRecipientUserGroupsBCC'
										}
										label={
											recipient.bccType === 'role'
												? Liferay.Language.get('role')
												: Liferay.Language.get(
														'user-group'
													)
										}
										options={
											recipient.bccType === 'role'
												? bccRolesList
												: bccUserGroupsList
										}
										placeholder={
											recipient.bccType === 'role'
												? Liferay.Language.get(
														'select-role'
													)
												: Liferay.Language.get(
														'select-user-group'
													)
										}
										search
										searchPlaceholder={
											recipient.bccType === 'role'
												? Liferay.Language.get(
														'search-for-a-role'
													)
												: Liferay.Language.get(
														'search-for-a-user-group'
													)
										}
										selectAllOption
										setOptions={(items) => {
											handleRecipientItemChange(
												items,
												'bcc',
												recipient.bccType === 'role'
													? setBCCRolesList
													: setBCCUserGroupsList
											);
										}}
									/>

									{recipient.toType === 'role' && (
										<LearnResourcesContext.Provider
											value={learnResources}
										>
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
						</div>
					</div>
				</ClayPanel.Body>
			</ClayPanel>
		</>
	);
}
