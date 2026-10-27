/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.fips.cbom.builder;

import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.cyclonedx.model.Component;
import org.cyclonedx.model.Evidence;
import org.cyclonedx.model.Property;
import org.cyclonedx.model.component.crypto.AlgorithmProperties;
import org.cyclonedx.model.component.crypto.CryptoProperties;
import org.cyclonedx.model.component.crypto.ProtocolProperties;
import org.cyclonedx.model.component.crypto.RelatedCryptoMaterialProperties;
import org.cyclonedx.model.component.crypto.enums.AssetType;
import org.cyclonedx.model.component.crypto.enums.CryptoFunction;
import org.cyclonedx.model.component.crypto.enums.Mode;
import org.cyclonedx.model.component.crypto.enums.Padding;
import org.cyclonedx.model.component.crypto.enums.ProtocolType;
import org.cyclonedx.model.component.crypto.enums.RelatedCryptoMaterialType;
import org.cyclonedx.model.component.evidence.Occurrence;

/**
 * @author Manuele Castro
 */
public class CryptoAssetComponentUtil {

	public static String getBOMRef(CryptoCall cryptoCall) {
		StringBundler sb = new StringBundler(6);

		sb.append("crypto:");
		sb.append(cryptoCall.getJCAClassName());
		sb.append(StringPool.COLON);

		if (cryptoCall.isConfigurable()) {
			List<String> configuredBy = cryptoCall.getConfiguredBy();

			sb.append(configuredBy.get(0));

			return sb.toString();
		}

		if (cryptoCall.getAlgorithm() == null) {
			sb.append(cryptoCall.getClassName());

			return sb.toString();
		}

		sb.append(cryptoCall.getAlgorithm());

		if (cryptoCall.getKeySize() > 0) {
			sb.append(StringPool.COLON);
			sb.append(cryptoCall.getKeySize());
		}

		return sb.toString();
	}

	public static List<Component> toComponents(List<CryptoCall> cryptoCalls) {
		Map<String, List<CryptoCall>> cryptoCallsMap = new TreeMap<>();

		for (CryptoCall cryptoCall : cryptoCalls) {
			List<CryptoCall> bomRefCryptoCalls = cryptoCallsMap.computeIfAbsent(
				getBOMRef(cryptoCall), bomRef -> new ArrayList<>());

			bomRefCryptoCalls.add(cryptoCall);
		}

		return TransformUtil.transform(
			cryptoCallsMap.entrySet(),
			entry -> _toComponent(
				entry.getKey(), entry.getValue(), cryptoCalls));
	}

	private static AlgorithmProperties _getAlgorithmProperties(
		List<CryptoCall> cryptoCalls) {

		AlgorithmProperties algorithmProperties = new AlgorithmProperties();

		CryptoCall cryptoCall = cryptoCalls.get(0);

		algorithmProperties.setCryptoFunctions(
			_getCryptoFunctions(cryptoCalls));
		algorithmProperties.setPrimitive(cryptoCall.getPrimitive());

		if (cryptoCall.getAlgorithm() == null) {
			return algorithmProperties;
		}

		if (cryptoCall.getKeySize() > 0) {
			algorithmProperties.setParameterSetIdentifier(
				String.valueOf(cryptoCall.getKeySize()));
		}

		if (Objects.equals(cryptoCall.getJCAClassName(), "Cipher")) {
			_setCipherProperties(
				algorithmProperties, cryptoCall.getAlgorithm());
		}

		return algorithmProperties;
	}

	private static String _getAlgorithmRef(
		List<CryptoCall> cryptoCalls, List<CryptoCall> keyCryptoCalls) {

		TreeSet<String> algorithmRefs = new TreeSet<>();

		for (CryptoCall keyCryptoCall : keyCryptoCalls) {
			if (!keyCryptoCall.isResolved()) {
				continue;
			}

			List<CryptoCall> candidateCryptoCalls = new ArrayList<>();
			List<CryptoCall> matchingCryptoCalls = new ArrayList<>();

			for (CryptoCall cryptoCall : cryptoCalls) {
				if (!_isKeyCandidate(cryptoCall, keyCryptoCall)) {
					continue;
				}

				candidateCryptoCalls.add(cryptoCall);

				if (!_isKeyAlgorithm(cryptoCall, keyCryptoCall)) {
					continue;
				}

				matchingCryptoCalls.add(cryptoCall);
			}

			if (matchingCryptoCalls.isEmpty() &&
				(candidateCryptoCalls.size() == 1)) {

				matchingCryptoCalls = candidateCryptoCalls;
			}

			for (CryptoCall matchingCryptoCall : matchingCryptoCalls) {
				algorithmRefs.add(getBOMRef(matchingCryptoCall));
			}
		}

		if (algorithmRefs.size() != 1) {
			return null;
		}

		return algorithmRefs.first();
	}

	private static List<CryptoFunction> _getCryptoFunctions(
		List<CryptoCall> cryptoCalls) {

		Set<CryptoFunction> cryptoFunctions = new TreeSet<>();

		for (CryptoCall cryptoCall : cryptoCalls) {
			cryptoFunctions.addAll(cryptoCall.getCryptoFunctions());
		}

		return new ArrayList<>(cryptoFunctions);
	}

	private static String _getDescription(List<CryptoCall> cryptoCalls) {
		Set<String> unresolvedReasons = new TreeSet<>();

		for (CryptoCall cryptoCall : cryptoCalls) {
			if (cryptoCall.isResolved()) {
				continue;
			}

			unresolvedReasons.add(cryptoCall.getUnresolvedReason());
		}

		if (unresolvedReasons.isEmpty()) {
			return null;
		}

		return StringBundler.concat(
			"The scan cannot read the algorithm, which comes from ",
			StringUtil.merge(unresolvedReasons, ", "), StringPool.PERIOD);
	}

	private static Evidence _getEvidence(List<CryptoCall> cryptoCalls) {
		Evidence evidence = new Evidence();

		Set<String> classNames = new TreeSet<>();

		for (CryptoCall cryptoCall : cryptoCalls) {
			classNames.add(cryptoCall.getClassName());
		}

		evidence.setOccurrences(
			TransformUtil.transform(
				classNames,
				className -> {
					Occurrence occurrence = new Occurrence();

					occurrence.setLocation(className);

					return occurrence;
				}));

		return evidence;
	}

	private static Mode _getMode(String name) {
		for (Mode mode : Mode.values()) {
			if (StringUtil.equalsIgnoreCase(mode.getName(), name)) {
				return mode;
			}
		}

		return Mode.OTHER;
	}

	private static String _getName(CryptoCall cryptoCall) {
		if (cryptoCall.isConfigurable()) {
			List<String> configuredBy = cryptoCall.getConfiguredBy();

			return configuredBy.get(0);
		}

		if (cryptoCall.getAlgorithm() == null) {
			return cryptoCall.getJCAClassName();
		}

		return CryptoAlgorithmNameUtil.getName(
			cryptoCall.getAlgorithm(), cryptoCall.getJCAClassName(),
			cryptoCall.getKeySize());
	}

	private static ProtocolProperties _getProtocolProperties(
		CryptoCall cryptoCall) {

		ProtocolProperties protocolProperties = new ProtocolProperties();

		if (cryptoCall.getAlgorithm() == null) {
			protocolProperties.setType(ProtocolType.TLS);

			return protocolProperties;
		}

		String algorithm = cryptoCall.getAlgorithm();

		if (algorithm.equals("TLS")) {
			protocolProperties.setType(ProtocolType.TLS);
		}
		else if (algorithm.startsWith("TLSv")) {
			protocolProperties.setType(ProtocolType.TLS);
			protocolProperties.setVersion(algorithm.substring(4));
		}
		else {
			protocolProperties.setType(ProtocolType.OTHER);
		}

		return protocolProperties;
	}

	private static RelatedCryptoMaterialProperties
		_getRelatedCryptoMaterialProperties(
			List<CryptoCall> cryptoCalls, List<CryptoCall> keyCryptoCalls) {

		RelatedCryptoMaterialProperties relatedCryptoMaterialProperties =
			new RelatedCryptoMaterialProperties();

		CryptoCall keyCryptoCall = keyCryptoCalls.get(0);

		if (Objects.equals(keyCryptoCall.getJCAClassName(), "KeyStore")) {
			if (keyCryptoCall.getAlgorithm() != null) {
				relatedCryptoMaterialProperties.setFormat(
					keyCryptoCall.getAlgorithm());
			}

			relatedCryptoMaterialProperties.setType(
				RelatedCryptoMaterialType.OTHER);

			return relatedCryptoMaterialProperties;
		}

		relatedCryptoMaterialProperties.setAlgorithmRef(
			_getAlgorithmRef(cryptoCalls, keyCryptoCalls));

		if (Objects.equals(keyCryptoCall.getJCAClassName(), "SecretKeySpec")) {
			relatedCryptoMaterialProperties.setType(
				RelatedCryptoMaterialType.SECRET_KEY);

			return relatedCryptoMaterialProperties;
		}

		TreeSet<RelatedCryptoMaterialType> relatedCryptoMaterialTypes =
			new TreeSet<>();

		for (CryptoCall cryptoCall : keyCryptoCalls) {
			relatedCryptoMaterialTypes.addAll(
				cryptoCall.getRelatedCryptoMaterialTypes());
		}

		if (relatedCryptoMaterialTypes.size() == 1) {
			relatedCryptoMaterialProperties.setType(
				relatedCryptoMaterialTypes.first());
		}
		else {
			relatedCryptoMaterialProperties.setType(
				RelatedCryptoMaterialType.KEY);
		}

		return relatedCryptoMaterialProperties;
	}

	private static boolean _isKeyAlgorithm(
		CryptoCall cryptoCall, CryptoCall keyCryptoCall) {

		if (keyCryptoCall.isConfigurable()) {
			return true;
		}

		String algorithm = StringUtil.toUpperCase(cryptoCall.getAlgorithm());
		String keyAlgorithm = StringUtil.toUpperCase(
			keyCryptoCall.getAlgorithm());

		if (Objects.equals(cryptoCall.getJCAClassName(), "Cipher")) {
			String[] transformationParts = StringUtil.split(
				algorithm, CharPool.SLASH);

			return transformationParts[0].equals(keyAlgorithm);
		}

		if (Objects.equals(cryptoCall.getJCAClassName(), "KeyAgreement")) {
			if (algorithm.equals(keyAlgorithm) ||
				(algorithm.equals("ECDH") && keyAlgorithm.equals("EC"))) {

				return true;
			}

			return false;
		}

		if (Objects.equals(cryptoCall.getJCAClassName(), "Signature")) {
			if (algorithm.endsWith("WITH" + keyAlgorithm) ||
				(algorithm.endsWith("WITHECDSA") &&
				 keyAlgorithm.equals("EC"))) {

				return true;
			}

			return false;
		}

		return algorithm.equals(keyAlgorithm);
	}

	private static boolean _isKeyCandidate(
		CryptoCall cryptoCall, CryptoCall keyCryptoCall) {

		if (!_keyAlgorithmJCAClassNames.contains(
				cryptoCall.getJCAClassName()) ||
			!Objects.equals(
				cryptoCall.getClassName(), keyCryptoCall.getClassName()) ||
			!cryptoCall.isResolved() ||
			(cryptoCall.isConfigurable() != keyCryptoCall.isConfigurable())) {

			return false;
		}

		if (!keyCryptoCall.isConfigurable()) {
			return true;
		}

		return !Collections.disjoint(
			cryptoCall.getConfiguredBy(), keyCryptoCall.getConfiguredBy());
	}

	private static void _setCipherProperties(
		AlgorithmProperties algorithmProperties, String transformation) {

		String[] transformationParts = StringUtil.split(
			StringUtil.toUpperCase(transformation), CharPool.SLASH);

		if (transformationParts.length != 3) {
			return;
		}

		algorithmProperties.setMode(_getMode(transformationParts[1]));

		String paddingName = transformationParts[2];

		if (paddingName.equals("NOPADDING")) {
			return;
		}

		if (paddingName.startsWith("OAEP")) {
			algorithmProperties.setPadding(Padding.OAEP);
		}
		else if (paddingName.equals("PKCS1PADDING")) {
			algorithmProperties.setPadding(Padding.PKCS1V15);
		}
		else if (paddingName.equals("PKCS5PADDING")) {
			algorithmProperties.setPadding(Padding.PKCS5);
		}
		else if (paddingName.equals("PKCS7PADDING")) {
			algorithmProperties.setPadding(Padding.PKCS7);
		}
		else {
			algorithmProperties.setPadding(Padding.OTHER);
		}
	}

	private static Component _toComponent(
		String bomRef, List<CryptoCall> bomRefCryptoCalls,
		List<CryptoCall> cryptoCalls) {

		Component component = new Component();
		CryptoProperties cryptoProperties = new CryptoProperties();

		CryptoCall cryptoCall = bomRefCryptoCalls.get(0);

		AssetType assetType = _assetTypes.getOrDefault(
			cryptoCall.getJCAClassName(), AssetType.ALGORITHM);

		if (assetType == AssetType.ALGORITHM) {
			cryptoProperties.setAlgorithmProperties(
				_getAlgorithmProperties(bomRefCryptoCalls));
		}
		else if (assetType == AssetType.PROTOCOL) {
			cryptoProperties.setProtocolProperties(
				_getProtocolProperties(cryptoCall));
		}
		else {
			cryptoProperties.setRelatedCryptoMaterialProperties(
				_getRelatedCryptoMaterialProperties(
					cryptoCalls, bomRefCryptoCalls));
		}

		cryptoProperties.setAssetType(assetType);

		component.setBomRef(bomRef);
		component.setCryptoProperties(cryptoProperties);
		component.setDescription(_getDescription(bomRefCryptoCalls));
		component.setEvidence(_getEvidence(bomRefCryptoCalls));
		component.setName(_getName(cryptoCall));
		component.setProperties(
			Collections.singletonList(
				new Property(
					"liferay:cbom:jca-class", cryptoCall.getJCAClassName())));
		component.setType(Component.Type.CRYPTOGRAPHIC_ASSET);

		return component;
	}

	private static final Map<String, AssetType> _assetTypes =
		HashMapBuilder.put(
			"KeyFactory", AssetType.RELATED_CRYPTO_MATERIAL
		).put(
			"KeyStore", AssetType.RELATED_CRYPTO_MATERIAL
		).put(
			"SecretKeySpec", AssetType.RELATED_CRYPTO_MATERIAL
		).put(
			"SSLContext", AssetType.PROTOCOL
		).build();
	private static final Set<String> _keyAlgorithmJCAClassNames =
		SetUtil.fromArray("Cipher", "KeyAgreement", "Mac", "Signature");

}