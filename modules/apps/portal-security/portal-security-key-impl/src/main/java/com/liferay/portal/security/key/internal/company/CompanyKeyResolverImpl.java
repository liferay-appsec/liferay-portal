/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.key.internal.company;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.encryptor.CompanyKeyResolver;
import com.liferay.portal.kernel.exception.CompanyKeyResolutionException;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.security.key.KeyReference;
import com.liferay.portal.security.key.crypto.CryptoManager;
import com.liferay.portal.security.key.crypto.CryptoServiceResult;
import com.liferay.portal.security.key.crypto.exception.CryptoException;
import com.liferay.portal.security.key.internal.profile.configuration.KeyManagerConfiguration;
import com.liferay.portal.security.key.spi.profile.KeyManagerProfile;
import com.liferay.portal.security.key.spi.profile.KeyManagerProfileRegistry;

import java.security.Key;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.spec.SecretKeySpec;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Christopher Kian
 */
@Component(
	configurationPid = "com.liferay.portal.security.key.internal.profile.configuration.KeyManagerConfiguration",
	service = CompanyKeyResolver.class
)
public class CompanyKeyResolverImpl implements CompanyKeyResolver {

	@Override
	public Key deserializeKey(long companyId, String serializedKey) {
		CompanyKeyCacheEntry companyKeyCacheEntry = _getCompanyKeyCacheEntry(
			companyId, serializedKey);

		if (companyKeyCacheEntry != null) {
			byte[] keyBytes = companyKeyCacheEntry.getKeyBytes();

			if (keyBytes != null) {
				try {
					return _createKey(keyBytes);
				}
				finally {
					Arrays.fill(keyBytes, (byte)0);
				}
			}
		}

		WrappedCompanyKey wrappedCompanyKey = WrappedCompanyKey.parse(
			companyId, serializedKey);

		byte[] keyBytes = null;

		try {
			CryptoServiceResult<byte[]> cryptoServiceResult =
				_cryptoManager.decrypt(
					wrappedCompanyKey.getCiphertext(), companyId,
					new KeyReference(
						wrappedCompanyKey.getIdentifier(),
						wrappedCompanyKey.getProviderId(),
						KeyReference.Type.CRYPTO));

			keyBytes = cryptoServiceResult.getValue();

			if (ArrayUtil.isEmpty(keyBytes)) {
				throw new CompanyKeyResolutionException(
					StringBundler.concat(
						"Decrypting the wrapped key returned no key material ",
						"for company ", companyId));
			}

			_putCompanyKeyCacheEntry(companyId, keyBytes, serializedKey);

			return _createKey(keyBytes);
		}
		catch (CryptoException cryptoException) {
			throw new CompanyKeyResolutionException(
				"Unable to decrypt the wrapped key for company " + companyId,
				cryptoException);
		}
		finally {
			if (keyBytes != null) {
				Arrays.fill(keyBytes, (byte)0);
			}
		}
	}

	@Override
	public boolean isEnabled() {
		if (Validator.isNull(_getCompanyKEKIdentifier()) ||
			Validator.isNull(_getCompanyKEKProviderId())) {

			return false;
		}

		return true;
	}

	@Override
	public String serializeKey(long companyId, Key key) {
		String companyKEKIdentifier = _getCompanyKEKIdentifier();

		if (Validator.isNull(companyKEKIdentifier)) {
			throw new CompanyKeyResolutionException(
				"KEK identifier is not configured for company " + companyId);
		}

		String companyKEKProviderId = _getCompanyKEKProviderId();

		if (Validator.isNull(companyKEKProviderId)) {
			throw new CompanyKeyResolutionException(
				"KEK provider is not configured for company " + companyId);
		}

		byte[] keyBytes = key.getEncoded();

		if (ArrayUtil.isEmpty(keyBytes)) {
			throw new CompanyKeyResolutionException(
				"Key has no encoded key material for company " + companyId);
		}

		try {
			CryptoServiceResult<byte[]> cryptoServiceResult =
				_cryptoManager.encrypt(
					companyId,
					new KeyReference(
						companyKEKIdentifier, companyKEKProviderId,
						KeyReference.Type.CRYPTO),
					keyBytes);

			byte[] ciphertext = cryptoServiceResult.getValue();

			if (ArrayUtil.isEmpty(ciphertext)) {
				throw new CompanyKeyResolutionException(
					"Encrypting the key returned no ciphertext for company " +
						companyId);
			}

			WrappedCompanyKey wrappedCompanyKey = new WrappedCompanyKey(
				ciphertext, companyKEKIdentifier, companyKEKProviderId);

			String serializedKey = wrappedCompanyKey.serialize();

			_putCompanyKeyCacheEntry(companyId, keyBytes, serializedKey);

			return serializedKey;
		}
		catch (CryptoException cryptoException) {
			throw new CompanyKeyResolutionException(
				"Unable to encrypt the key for company " + companyId,
				cryptoException);
		}
		finally {
			Arrays.fill(keyBytes, (byte)0);
		}
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		_keyAlgorithm = PropsUtil.get(PropsKeys.COMPANY_ENCRYPTION_ALGORITHM);
		_keyManagerConfiguration = ConfigurableUtil.createConfigurable(
			KeyManagerConfiguration.class, properties);

		_clearCompanyKeyCacheEntries();
	}

	@Deactivate
	protected void deactivate() {
		_clearCompanyKeyCacheEntries();

		_keyManagerConfiguration = null;
	}

	private void _clearCompanyKeyCacheEntries() {
		for (CompanyKeyCacheEntry companyKeyCacheEntry :
				_companyKeyCacheEntries.values()) {

			companyKeyCacheEntry.destroy();
		}

		_companyKeyCacheEntries.clear();
	}

	private Key _createKey(byte[] keyBytes) {
		return new SecretKeySpec(keyBytes, _keyAlgorithm);
	}

	private long _getCacheTTL() {
		KeyManagerConfiguration keyManagerConfiguration =
			_keyManagerConfiguration;

		if (keyManagerConfiguration == null) {
			return 0;
		}

		long cacheTTL = keyManagerConfiguration.companyKeyCacheTTL();

		return cacheTTL * 1000;
	}

	private String _getCompanyKEKIdentifier() {
		KeyManagerConfiguration keyManagerConfiguration =
			_keyManagerConfiguration;

		if (keyManagerConfiguration == null) {
			return null;
		}

		return keyManagerConfiguration.companyKEKIdentifier();
	}

	private String _getCompanyKEKProviderId() {
		KeyManagerProfile keyManagerProfile =
			_keyManagerProfileRegistry.getActiveKeyManagerProfile();

		if (keyManagerProfile == null) {
			return null;
		}

		return keyManagerProfile.getCompanyKEKProviderId();
	}

	private CompanyKeyCacheEntry _getCompanyKeyCacheEntry(
		long companyId, String serializedKey) {

		CompanyKeyCacheEntry companyKeyCacheEntry = _companyKeyCacheEntries.get(
			companyId);

		if (companyKeyCacheEntry == null) {
			return null;
		}

		if (companyKeyCacheEntry.isExpired(System.currentTimeMillis()) ||
			!Objects.equals(
				companyKeyCacheEntry.getSerializedKey(), serializedKey)) {

			if (_companyKeyCacheEntries.remove(
					companyId, companyKeyCacheEntry)) {

				companyKeyCacheEntry.destroy();
			}

			return null;
		}

		return companyKeyCacheEntry;
	}

	private void _putCompanyKeyCacheEntry(
		long companyId, byte[] keyBytes, String serializedKey) {

		long cacheTTL = _getCacheTTL();

		if (cacheTTL <= 0) {
			return;
		}

		CompanyKeyCacheEntry companyKeyCacheEntry = _companyKeyCacheEntries.put(
			companyId,
			new CompanyKeyCacheEntry(
				System.currentTimeMillis() + cacheTTL, keyBytes,
				serializedKey));

		if (companyKeyCacheEntry != null) {
			companyKeyCacheEntry.destroy();
		}
	}

	private final Map<Long, CompanyKeyCacheEntry> _companyKeyCacheEntries =
		new ConcurrentHashMap<>();

	@Reference
	private CryptoManager _cryptoManager;

	private volatile String _keyAlgorithm;
	private volatile KeyManagerConfiguration _keyManagerConfiguration;

	@Reference
	private KeyManagerProfileRegistry _keyManagerProfileRegistry;

}