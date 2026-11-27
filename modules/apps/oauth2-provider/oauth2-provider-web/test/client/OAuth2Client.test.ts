/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {
	FromParameters,
	FromUserAgentApplication,
	OAuth2Client,
} from '../../src/main/resources/META-INF/resources/js/client/OAuth2Client';

const fetchMock = fetch as any;

describe('FromUserAgentApplication', () => {
	it('creates a client for a readable application', async () => {
		fetchMock.mockResponseOnce(
			JSON.stringify({
				client_id: 'client-id',
				homePageURL: 'http://localhost:8080',
			})
		);

		await expect(FromUserAgentApplication('erc')).resolves.toBeInstanceOf(
			OAuth2Client
		);
	});

	it('encodes the external reference code in the lookup', async () => {
		fetchMock.mockResponseOnce(
			JSON.stringify({
				client_id: 'client-id',
				homePageURL: 'http://localhost:8080',
			})
		);

		await FromUserAgentApplication('a&b#c');

		expect(fetchMock).toHaveBeenCalledWith(
			expect.stringContaining('externalReferenceCode=a%26b%23c')
		);
	});

	it('names the cause when the application endpoint fails', async () => {
		fetchMock.mockResponseOnce('', {status: 500});

		await expect(FromUserAgentApplication('erc')).rejects.toThrow(
			'Unable to get the application erc because the request failed with status 500'
		);
	});

	it('names the cause when the application is not readable', async () => {
		fetchMock.mockResponseOnce('', {status: 400});

		await expect(FromUserAgentApplication('erc')).rejects.toThrow(
			'No Application User Agent profile found for erc'
		);
	});
});

function getTokenRequestBody(): URLSearchParams {
	const [, init] = fetchMock.mock.calls[fetchMock.mock.calls.length - 1];

	return init.body as URLSearchParams;
}

async function requestToken(oAuth2Client: OAuth2Client) {
	fetchMock.mockResponseOnce(JSON.stringify({access_token: 'token'}));

	await (oAuth2Client as any)._requestToken(
		'code-verifier',
		'code',
		'http://localhost/o/oauth2/redirect'
	);

	return getTokenRequestBody();
}

describe('resource', () => {
	const homePageURL = `http://${Math.random().toString(36).substring(2)}.example.com`;

	function mockApplication() {
		fetchMock.mockResponseOnce(
			JSON.stringify({client_id: 'client-id', homePageURL})
		);
	}

	it('defaults to the home page URL for a user agent application', async () => {
		mockApplication();

		const body = await requestToken(await FromUserAgentApplication('erc'));

		expect(body.getAll('resource')).toEqual([homePageURL]);
	});

	it('still accepts a debug flag as the second argument', async () => {
		mockApplication();

		const body = await requestToken(
			await FromUserAgentApplication('erc', true)
		);

		expect(body.getAll('resource')).toEqual([homePageURL]);
	});

	it('sends every requested resource', async () => {
		mockApplication();

		const mcpURL = `http://${Math.random().toString(36).substring(2)}.example.com/o/mcp`;

		const body = await requestToken(
			await FromUserAgentApplication('erc', {
				resource: [homePageURL, mcpURL],
			})
		);

		expect(body.getAll('resource')).toEqual([homePageURL, mcpURL]);
	});

	it('sends no resource for an empty list', async () => {
		mockApplication();

		const body = await requestToken(
			await FromUserAgentApplication('erc', {resource: []})
		);

		expect(body.has('resource')).toBe(false);
	});

	it('sends no resource from parameters unless requested', async () => {
		const body = await requestToken(
			FromParameters({
				authorizeURL: '/o/oauth2/authorize',
				clientId: 'client-id',
				homePageURL,
				tokenURL: '/o/oauth2/token',
			})
		);

		expect(body.has('resource')).toBe(false);
	});

	it('sends the resource from parameters', async () => {
		const body = await requestToken(
			FromParameters({
				authorizeURL: '/o/oauth2/authorize',
				clientId: 'client-id',
				homePageURL,
				resource: homePageURL,
				tokenURL: '/o/oauth2/token',
			})
		);

		expect(body.getAll('resource')).toEqual([homePageURL]);
	});
});

describe('state', () => {
	it('sends an opaque state instead of the session key', async () => {
		const homePageURL = `http://${Math.random().toString(36).substring(2)}.example.com`;

		const oAuth2Client = FromParameters({
			authorizeURL: '/o/oauth2/authorize',
			clientId: 'client-id',
			homePageURL,
			redirectURIs: [`${window.location.origin}/o/oauth2/redirect`],
			resource: homePageURL,
			tokenURL: '/o/oauth2/token',
		});

		const sessionKey = `client-id-token-${homePageURL} ${homePageURL}/o/mcp`;

		(oAuth2Client as any)._createIframe(
			{code_challenge: 'challenge', code_verifier: 'verifier'},
			sessionKey
		);

		const iframe = document.querySelector('iframe') as HTMLIFrameElement;

		const state = new URL(iframe.src).searchParams.get('state');

		expect(state).toMatch(/^[0-9a-f]{32}$/);
		expect(state).not.toBe(sessionKey);
	});
});
