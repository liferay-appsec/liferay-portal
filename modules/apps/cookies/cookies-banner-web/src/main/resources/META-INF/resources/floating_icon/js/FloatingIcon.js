/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {openCookieConsentModal} from '../../cookies_banner/js/CookiesBanner';

export default function () {
	const cookieBanner = document.querySelector('.cookies-banner');
	const floatingIconButton = document.getElementById('floatingIconButton');
	const floatingIconLabel = document.getElementById('floatingIconLabel');

	if (!floatingIconLabel || !floatingIconButton) {
		return;
	}

	const toggleIconVisibility = () => {
		let isBannerVisible = false;

		if (cookieBanner) {
			isBannerVisible =
				cookieBanner.style.display !== 'none' &&
				!cookieBanner.classList.contains('d-none');
		}

		if (isBannerVisible) {
			floatingIconLabel.classList.remove('d-inline-flex');
			floatingIconLabel.classList.add('d-none');
		}
		else {
			floatingIconLabel.classList.remove('d-none');
			floatingIconLabel.classList.add('d-inline-flex');
		}
	};

	toggleIconVisibility();

	if (cookieBanner) {
		const observer = new MutationObserver(() => {
			toggleIconVisibility();
		});

		observer.observe(cookieBanner, {
			attributeFilter: ['style', 'class'],
			attributes: true,
		});
	}

	floatingIconButton.addEventListener('click', () => {
		openCookieConsentModal({});
	});
}
