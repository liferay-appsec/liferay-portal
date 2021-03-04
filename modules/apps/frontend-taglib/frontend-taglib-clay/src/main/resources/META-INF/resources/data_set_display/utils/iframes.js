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

import {OPEN_MODAL, OPEN_MODAL_FROM_IFRAME} from './eventsDefinitions';

const iframeHandlerModalNamespace = 'iframe-handler-modal_';
let modalsCounter = 0;
const registeredModals = new Map();

Liferay.on(OPEN_MODAL_FROM_IFRAME, (payload) => {
	let firstAvailableModalId = null;

	// eslint-disable-next-line no-for-of-loops/no-for-of-loops
	for (const [modalId, modalRef] of registeredModals.entries()) {
		if (modalRef) {
			firstAvailableModalId = modalId;
			break;
		}
	}

	window.top.Liferay.fire(OPEN_MODAL, {
		...payload,
		id: firstAvailableModalId,
	});
});

Liferay.on('endNavigate', () => {
	registeredModals.clear();
});

export function subscribeModal(modalNode) {
	const id = `${iframeHandlerModalNamespace}${modalsCounter++}`;

	registeredModals.set(id, modalNode);

	return id;
}

export function unsubscribeModal(modalId) {
	registeredModals.delete(modalId);
}

export function isPageInIframe() {
	return window.location !== window.parent.location;
}
