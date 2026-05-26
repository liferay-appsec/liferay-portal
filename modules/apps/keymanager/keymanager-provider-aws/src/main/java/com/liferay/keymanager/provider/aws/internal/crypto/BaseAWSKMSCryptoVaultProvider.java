/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.keymanager.provider.aws.internal.crypto;

import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.kms.model.AliasListEntry;
import com.amazonaws.services.kms.model.CreateAliasRequest;
import com.amazonaws.services.kms.model.CreateKeyRequest;
import com.amazonaws.services.kms.model.CreateKeyResult;
import com.amazonaws.services.kms.model.CustomerMasterKeySpec;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.kms.model.DecryptResult;
import com.amazonaws.services.kms.model.DeleteAliasRequest;
import com.amazonaws.services.kms.model.DescribeKeyRequest;
import com.amazonaws.services.kms.model.DescribeKeyResult;
import com.amazonaws.services.kms.model.EncryptRequest;
import com.amazonaws.services.kms.model.EncryptResult;
import com.amazonaws.services.kms.model.KeyMetadata;
import com.amazonaws.services.kms.model.KeyUsageType;
import com.amazonaws.services.kms.model.ListAliasesRequest;
import com.amazonaws.services.kms.model.ListAliasesResult;
import com.amazonaws.services.kms.model.NotFoundException;
import com.amazonaws.services.kms.model.ScheduleKeyDeletionRequest;

import com.liferay.keymanager.KeyReference;
import com.liferay.keymanager.crypto.CryptoKey;
import com.liferay.keymanager.crypto.CryptoManagerException;
import com.liferay.keymanager.provider.aws.internal.util.AWSArnResolver;
import com.liferay.keymanager.provider.aws.internal.util.AWSByteBufferUtil;
import com.liferay.keymanager.provider.aws.internal.util.AWSClientManager;
import com.liferay.keymanager.spi.crypto.CryptoVaultProvider;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;

import java.nio.ByteBuffer;

import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.osgi.service.component.annotations.Deactivate;

/**
 * @author Christopher Kian
 */
public abstract class BaseAWSKMSCryptoVaultProvider
	implements CryptoVaultProvider {

	@Override
	public byte[] decrypt(byte[] ciphertext, long companyId, String identifier)
		throws CryptoManagerException {

		_checkPermission(companyId);

		String keyArn = _resolveKeyArn(companyId, identifier);

		try {
			DecryptResult decryptResult = _awsClientManager.execute(
				awsKms -> awsKms.decrypt(
					new DecryptRequest(
					).withCiphertextBlob(
						ByteBuffer.wrap(ciphertext)
					).withKeyId(
						keyArn
					)));

			return AWSByteBufferUtil.consumeAndZero(
				decryptResult.getPlaintext());
		}
		catch (Exception exception) {
			throw new CryptoManagerException(
				StringBundler.concat(
					"Unable to decrypt with AWS KMS key ", keyArn, ": ",
					exception.getMessage()),
				exception);
		}
	}

	@Override
	public void deleteKey(long companyId, String identifier)
		throws CryptoManagerException {

		_checkPermission(companyId);

		String keyArn = _resolveKeyArn(companyId, identifier);

		String aliasName = _toAliasName(keyArn);

		try {
			_awsClientManager.execute(
				awsKms -> {
					awsKms.scheduleKeyDeletion(
						new ScheduleKeyDeletionRequest(
						).withKeyId(
							keyArn
						).withPendingWindowInDays(
							_KEY_DELETION_PENDING_WINDOW_DAYS
						));

					if (aliasName != null) {
						try {
							awsKms.deleteAlias(
								new DeleteAliasRequest(
								).withAliasName(
									aliasName
								));
						}
						catch (NotFoundException notFoundException) {
							if (_log.isDebugEnabled()) {
								_log.debug(
									"Alias " + aliasName + " already removed",
									notFoundException);
							}
						}
					}

					return null;
				});
		}
		catch (Exception exception) {
			throw new CryptoManagerException(
				StringBundler.concat(
					"Unable to schedule deletion for AWS KMS key ", keyArn,
					": ", exception.getMessage()),
				exception);
		}
	}

	@Override
	public byte[] encrypt(long companyId, String identifier, byte[] plaintext)
		throws CryptoManagerException {

		_checkPermission(companyId);

		String keyArn = _resolveKeyArn(companyId, identifier);

		try {
			EncryptResult encryptResult = _awsClientManager.execute(
				awsKms -> awsKms.encrypt(
					new EncryptRequest(
					).withKeyId(
						keyArn
					).withPlaintext(
						ByteBuffer.wrap(plaintext)
					)));

			return AWSByteBufferUtil.consumeAndZero(
				encryptResult.getCiphertextBlob());
		}
		catch (Exception exception) {
			throw new CryptoManagerException(
				StringBundler.concat(
					"Unable to encrypt with AWS KMS key ", keyArn, ": ",
					exception.getMessage()),
				exception);
		}
	}

	@Override
	public String generateAsymmetricKeyPair(
			String algorithmSpec, long companyId, String identifier)
		throws CryptoManagerException {

		_checkPermission(companyId);

		String aliasName = _toAliasName(_resolveKeyArn(companyId, identifier));

		try {
			return _awsClientManager.execute(
				awsKms -> {
					CreateKeyResult createKeyResult = awsKms.createKey(
						new CreateKeyRequest(
						).withCustomerMasterKeySpec(
							_toAsymmetricSpec(algorithmSpec)
						).withKeyUsage(
							_toAsymmetricKeyUsage(algorithmSpec)
						));

					KeyMetadata keyMetadata = createKeyResult.getKeyMetadata();

					if (aliasName != null) {
						awsKms.createAlias(
							new CreateAliasRequest(
							).withAliasName(
								aliasName
							).withTargetKeyId(
								keyMetadata.getKeyId()
							));

						return identifier;
					}

					return keyMetadata.getArn();
				});
		}
		catch (Exception exception) {
			throw new CryptoManagerException(
				"Unable to generate asymmetric key pair: " +
					exception.getMessage(),
				exception);
		}
	}

	@Override
	public String generateSecretKey(
			String algorithmSpec, long companyId, String identifier)
		throws CryptoManagerException {

		_checkPermission(companyId);

		String aliasName = _toAliasName(_resolveKeyArn(companyId, identifier));

		try {
			return _awsClientManager.execute(
				awsKms -> {
					CreateKeyResult createKeyResult = awsKms.createKey(
						new CreateKeyRequest(
						).withCustomerMasterKeySpec(
							CustomerMasterKeySpec.SYMMETRIC_DEFAULT
						).withKeyUsage(
							KeyUsageType.ENCRYPT_DECRYPT
						));

					KeyMetadata keyMetadata = createKeyResult.getKeyMetadata();

					if (aliasName != null) {
						awsKms.createAlias(
							new CreateAliasRequest(
							).withAliasName(
								aliasName
							).withTargetKeyId(
								keyMetadata.getKeyId()
							));

						return identifier;
					}

					return keyMetadata.getArn();
				});
		}
		catch (Exception exception) {
			throw new CryptoManagerException(
				"Unable to generate secret key: " + exception.getMessage(),
				exception);
		}
	}

	@Override
	public List<String> getKeyIdentifiers(long companyId)
		throws CryptoManagerException {

		_checkPermission(companyId);

		String aliasPrefix = _resolveAliasPrefix(companyId);

		if (Validator.isNull(aliasPrefix)) {
			return new ArrayList<>();
		}

		try {
			return _awsClientManager.execute(
				awsKms -> {
					List<String> aliasNames = new ArrayList<>();

					String marker = null;

					do {
						ListAliasesRequest listAliasesRequest =
							new ListAliasesRequest(
							).withLimit(
								100
							);

						if (marker != null) {
							listAliasesRequest.setMarker(marker);
						}

						ListAliasesResult listAliasesResult =
							awsKms.listAliases(listAliasesRequest);

						for (AliasListEntry aliasListEntry :
								listAliasesResult.getAliases()) {

							String aliasName = aliasListEntry.getAliasName();

							if ((aliasName != null) &&
								aliasName.startsWith(aliasPrefix)) {

								aliasNames.add(
									aliasName.substring(aliasPrefix.length()));
							}
						}

						marker = listAliasesResult.getNextMarker();
					}
					while (marker != null);

					return aliasNames;
				});
		}
		catch (Exception exception) {
			throw new CryptoManagerException(
				StringBundler.concat(
					"Unable to list AWS KMS aliases under ", aliasPrefix, ": ",
					exception.getMessage()),
				exception);
		}
	}

	@Override
	public CryptoKey getKeyMetadata(long companyId, String identifier)
		throws CryptoManagerException {

		_checkPermission(companyId);

		String keyArn = _resolveKeyArn(companyId, identifier);

		try {
			DescribeKeyResult describeKeyResult = _awsClientManager.execute(
				awsKms -> awsKms.describeKey(
					new DescribeKeyRequest(
					).withKeyId(
						keyArn
					)));

			KeyMetadata keyMetadata = describeKeyResult.getKeyMetadata();

			String algorithm = GetterUtil.getString(
				keyMetadata.getCustomerMasterKeySpec(), "SYMMETRIC_DEFAULT");
			String cipherSpec = GetterUtil.getString(
				keyMetadata.getKeyUsage(), "ENCRYPT_DECRYPT");

			long creationDate = 0;

			if (keyMetadata.getCreationDate() != null) {
				creationDate = keyMetadata.getCreationDate(
				).getTime();
			}

			return new CryptoKey(
				algorithm, cipherSpec, creationDate,
				new KeyReference(
					keyArn, _providerId, KeyReference.Type.CRYPTO));
		}
		catch (Exception exception) {
			throw new CryptoManagerException(
				StringBundler.concat(
					"Unable to describe AWS KMS key ", keyArn, ": ",
					exception.getMessage()),
				exception);
		}
	}

	@Override
	public String importSecretKey(
			String algorithmSpec, long companyId, String identifier,
			byte[] rawKeyMaterial)
		throws CryptoManagerException {

		throw new CryptoManagerException(
			"Importing raw key material into AWS KMS is not yet supported");
	}

	@Override
	public boolean isAllowedCompany(long companyId) {
		return false;
	}

	@Override
	public Key unwrap(
			long companyId, String identifier, String wrappedKeyAlgorithm,
			byte[] wrappedKeyBytes, int wrappedKeyCipherType)
		throws CryptoManagerException {

		_checkPermission(companyId);

		String keyArn = _resolveKeyArn(companyId, identifier);

		byte[] keyBytes = null;

		try {
			DecryptResult decryptResult = _awsClientManager.execute(
				awsKms -> awsKms.decrypt(
					new DecryptRequest(
					).withCiphertextBlob(
						ByteBuffer.wrap(wrappedKeyBytes)
					).withKeyId(
						keyArn
					)));

			keyBytes = AWSByteBufferUtil.consumeAndZero(
				decryptResult.getPlaintext());

			return _toKey(keyBytes, wrappedKeyAlgorithm, wrappedKeyCipherType);
		}
		catch (Exception exception) {
			throw new CryptoManagerException(
				StringBundler.concat(
					"Unable to unwrap with AWS KMS key ", keyArn, ": ",
					exception.getMessage()),
				exception);
		}
		finally {
			if (keyBytes != null) {
				Arrays.fill(keyBytes, (byte)0);
			}
		}
	}

	@Override
	public byte[] wrap(long companyId, Key keyToWrap, String identifier)
		throws CryptoManagerException {

		_checkPermission(companyId);

		byte[] encoded = keyToWrap.getEncoded();

		try {
			return encrypt(companyId, identifier, encoded);
		}
		finally {
			if (encoded != null) {
				Arrays.fill(encoded, (byte)0);
			}
		}
	}

	protected void activate(Map<String, Object> properties) {
		_companyId = GetterUtil.getLong(properties.get("companyId"));
		_keyArnTemplate = GetterUtil.getString(
			properties.get("keyArnTemplate"));
		_region = GetterUtil.getString(properties.get("awsRegion"));
		_accountId = GetterUtil.getString(properties.get("awsAccountId"));

		boolean useFipsEndpoint = GetterUtil.getBoolean(
			properties.get("useFipsEndpoint"));

		AWSClientManager<AWSKMS> existingAWSClientManager = _awsClientManager;

		if (existingAWSClientManager != null) {
			existingAWSClientManager.updateConfiguration(
				_region, useFipsEndpoint);
		}
		else {
			String fipsEndpointTemplate = "kms-fips.{region}.amazonaws.com";

			_awsClientManager = new AWSClientManager<>(
				_region, useFipsEndpoint, fipsEndpointTemplate,
				(endpointConfiguration, region, credentialsProvider) -> {
					AWSKMSClientBuilder awskmsClientBuilder =
						AWSKMSClientBuilder.standard(
						).withCredentials(
							credentialsProvider
						);

					if (endpointConfiguration != null) {
						awskmsClientBuilder.withEndpointConfiguration(
							endpointConfiguration);
					}
					else if (Validator.isNotNull(region)) {
						awskmsClientBuilder.withRegion(region);
					}

					return awskmsClientBuilder.build();
				});
		}

		if (_log.isInfoEnabled()) {
			_log.info(
				StringBundler.concat(
					"Activated ", _providerId, " for company ", _companyId,
					" in region ", _region));
		}
	}

	@Deactivate
	protected void deactivate() {
		if (_awsClientManager != null) {
			_awsClientManager.close();

			_awsClientManager = null;
		}
	}

	protected long getConfiguredCompanyId() {
		return _companyId;
	}

	protected void setProviderId(String providerId) {
		_providerId = providerId;
	}

	private void _checkPermission(long companyId)
		throws CryptoManagerException {

		if (!isAllowedCompany(companyId)) {
			throw new CryptoManagerException(
				StringBundler.concat(
					"Provider ", _providerId, " does not handle company ID ",
					companyId));
		}
	}

	private String _normalizeAsymmetricAlgorithm(String algorithm) {
		if (algorithm == null) {
			return algorithm;
		}

		int index = algorithm.indexOf('_');

		if (index <= 0) {
			return algorithm;
		}

		String prefix = algorithm.substring(0, index);

		if (prefix.equals("RSA")) {
			return "RSA";
		}

		if (prefix.equals("EC") || prefix.equals("ECC")) {
			return "EC";
		}

		return algorithm;
	}

	private String _resolveAliasPrefix(long companyId) {
		if (Validator.isNull(_keyArnTemplate)) {
			return null;
		}

		String resolved = AWSArnResolver.resolve(
			_keyArnTemplate, _region, _accountId, companyId, "");

		int index = resolved.indexOf("alias/");

		if (index < 0) {
			return null;
		}

		return resolved.substring(index);
	}

	private String _resolveKeyArn(long companyId, String identifier) {
		return AWSArnResolver.resolve(
			_keyArnTemplate, _region, _accountId, companyId, identifier);
	}

	private String _toAliasName(String keyArnOrAlias) {
		if (keyArnOrAlias == null) {
			return null;
		}

		int index = keyArnOrAlias.indexOf("alias/");

		if (index < 0) {
			return null;
		}

		return keyArnOrAlias.substring(index);
	}

	private KeyUsageType _toAsymmetricKeyUsage(String algorithmSpec) {
		if ((algorithmSpec == null) || !algorithmSpec.contains("|")) {
			return KeyUsageType.SIGN_VERIFY;
		}

		String usage = algorithmSpec.substring(algorithmSpec.indexOf('|') + 1);

		try {
			return KeyUsageType.fromValue(usage);
		}
		catch (IllegalArgumentException illegalArgumentException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unknown KMS key usage \"" + usage +
						"\"; falling back to SIGN_VERIFY",
					illegalArgumentException);
			}

			return KeyUsageType.SIGN_VERIFY;
		}
	}

	private CustomerMasterKeySpec _toAsymmetricSpec(String algorithmSpec) {
		if (algorithmSpec == null) {
			return CustomerMasterKeySpec.RSA_2048;
		}

		String spec = algorithmSpec;

		int pipeIndex = spec.indexOf('|');

		if (pipeIndex >= 0) {
			spec = spec.substring(0, pipeIndex);
		}

		try {
			return CustomerMasterKeySpec.fromValue(spec);
		}
		catch (IllegalArgumentException illegalArgumentException) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unknown KMS key spec \"" + spec +
						"\"; falling back to RSA_2048",
					illegalArgumentException);
			}

			return CustomerMasterKeySpec.RSA_2048;
		}
	}

	private Key _toKey(byte[] keyBytes, String algorithm, int cipherType)
		throws Exception {

		if (cipherType == Cipher.PUBLIC_KEY) {
			KeyFactory keyFactory = KeyFactory.getInstance(
				_normalizeAsymmetricAlgorithm(algorithm));

			return keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes));
		}

		if (cipherType == Cipher.PRIVATE_KEY) {
			KeyFactory keyFactory = KeyFactory.getInstance(
				_normalizeAsymmetricAlgorithm(algorithm));

			return keyFactory.generatePrivate(
				new PKCS8EncodedKeySpec(keyBytes));
		}

		return new SecretKeySpec(keyBytes, algorithm);
	}

	private static final int _KEY_DELETION_PENDING_WINDOW_DAYS = 7;

	private static final Log _log = LogFactoryUtil.getLog(
		BaseAWSKMSCryptoVaultProvider.class);

	private volatile String _accountId;
	private volatile AWSClientManager<AWSKMS> _awsClientManager;
	private volatile long _companyId;
	private volatile String _keyArnTemplate;
	private volatile String _providerId;
	private volatile String _region;

}