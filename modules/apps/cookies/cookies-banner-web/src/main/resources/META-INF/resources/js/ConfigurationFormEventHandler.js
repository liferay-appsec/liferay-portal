/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {delegate} from 'frontend-js-web';

export default function ({namespace}) {
	const delegateHandler = delegate(
		document.body,
		'change',
		'input[type="checkbox"]',
		(event) => {
			const consentRenewalPeriod = document.getElementById(
				`${namespace}consentRenewalPeriod`
			);

			const consentRenewalPeriodLabel = document.getElementById(
				`${namespace}consentRenewalPeriodLabel`
			);

			const consentRenewalPeriodTimeUnit = document.getElementById(
				`${namespace}consentRenewalPeriodTimeUnit`
			);

			const dissentRenewalPeriod = document.getElementById(
				`${namespace}dissentRenewalPeriod`
			);

			const dissentRenewalPeriodLabel = document.getElementById(
				`${namespace}dissentRenewalPeriodLabel`
			);

			const dissentRenewalPeriodTimeUnit = document.getElementById(
				`${namespace}dissentRenewalPeriodTimeUnit`
			);

			const explicitConsentMode = document.querySelector(
				`input[type='checkbox'][name='${namespace}explicitConsentMode']`
			);

			const floatingIconEnabled = document.querySelector(
				`input[type='checkbox'][name='${namespace}floatingIconEnabled']`
			);

			const floatingIcons = document.querySelectorAll(
				`input[type='radio'][name='${namespace}floatingIcon']`
			);

			const logoSelectorContainer = document.getElementById(
				`${namespace}logoSelectorContainer`
			);

			const storeConsent = document.querySelector(
				`input[type='checkbox'][name='${namespace}storeConsent']`
			);

			if (event.delegateTarget.id === `${namespace}enabled`) {
				const isChecked = event.delegateTarget.checked;

				const toggleElement = (element, disabled) => {
					if (!element) {
						return;
					}
					if (disabled) {
						element.classList.add('disabled');
						element.setAttribute('disabled', '');
					}
					else {
						element.classList.remove('disabled');
						element.removeAttribute('disabled');
					}
				};

				toggleElement(consentRenewalPeriod, !isChecked);
				toggleElement(consentRenewalPeriodTimeUnit, !isChecked);
				toggleElement(dissentRenewalPeriod, !isChecked);
				toggleElement(dissentRenewalPeriodTimeUnit, !isChecked);

				if (consentRenewalPeriod) {
					consentRenewalPeriod.required = isChecked;
				}

				if (dissentRenewalPeriod) {
					dissentRenewalPeriod.required = isChecked;
				}

				if (isChecked) {
					consentRenewalPeriodLabel?.classList.remove('disabled');
					dissentRenewalPeriodLabel?.classList.remove('disabled');
					explicitConsentMode?.removeAttribute('disabled');
				}
				else {
					consentRenewalPeriodLabel?.classList.add('disabled');
					dissentRenewalPeriodLabel?.classList.add('disabled');
					explicitConsentMode?.setAttribute('disabled', '');
				}

				if (Liferay.FeatureFlags['LPD-75027']) {
					if (isChecked) {
						floatingIconEnabled?.removeAttribute('disabled');
						logoSelectorContainer?.classList.remove('disabled');
					}
					else {
						floatingIconEnabled?.setAttribute('disabled', '');
						logoSelectorContainer?.classList.add('disabled');
					}

					floatingIcons.forEach((iconInput) => {
						if (isChecked) {
							iconInput.removeAttribute('disabled');
						}
						else {
							iconInput.setAttribute('disabled', '');
						}
						const label = document.querySelector(
							`label[for='${iconInput.id}']`
						);

						label?.classList.toggle('disabled', !isChecked);
					});

					logoSelectorContainer
						?.querySelectorAll('input, button')
						.forEach((element) => {
							if (isChecked) {
								element.removeAttribute('disabled');
							}
							else {
								element.setAttribute('disabled', '');
							}
						});
				}

				if (Liferay.FeatureFlags['LPD-75032'] && storeConsent) {
					if (isChecked) {
						storeConsent.removeAttribute('disabled');
					}
					else {
						storeConsent.checked = false;
						storeConsent.setAttribute('disabled', '');
					}
				}
			}
		}
	);

	return {
		dispose() {
			delegateHandler.dispose();
		},
	};
}
