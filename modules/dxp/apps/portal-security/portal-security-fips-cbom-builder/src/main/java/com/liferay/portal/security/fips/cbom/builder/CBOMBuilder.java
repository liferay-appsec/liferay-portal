/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.fips.cbom.builder;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.PropertiesUtil;
import com.liferay.portal.kernel.util.ReleaseInfo;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.nio.charset.StandardCharsets;

import java.security.Provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import org.cyclonedx.Version;
import org.cyclonedx.generators.BomGeneratorFactory;
import org.cyclonedx.generators.json.BomJsonGenerator;
import org.cyclonedx.model.Bom;
import org.cyclonedx.model.Component;
import org.cyclonedx.model.Evidence;
import org.cyclonedx.model.LifecycleChoice;
import org.cyclonedx.model.Lifecycles;
import org.cyclonedx.model.Metadata;
import org.cyclonedx.model.Property;
import org.cyclonedx.model.component.crypto.AlgorithmProperties;
import org.cyclonedx.model.component.crypto.CryptoProperties;
import org.cyclonedx.model.component.crypto.ProtocolProperties;
import org.cyclonedx.model.component.crypto.RelatedCryptoMaterialProperties;
import org.cyclonedx.model.component.crypto.enums.AssetType;
import org.cyclonedx.model.component.crypto.enums.CryptoFunction;
import org.cyclonedx.model.component.crypto.enums.Primitive;
import org.cyclonedx.model.component.crypto.enums.ProtocolType;
import org.cyclonedx.model.component.crypto.enums.RelatedCryptoMaterialType;
import org.cyclonedx.model.component.evidence.Occurrence;

/**
 * @author Manuele Castro
 */
public class CBOMBuilder {

	public static String build(
			String configurationName, List<CryptoUse> cryptoUses)
		throws Exception {

		String portalPropertiesContent = StringUtil.read(
			CBOMBuilder.class.getClassLoader(), "portal.properties");

		Properties portalProperties = PropertiesUtil.load(
			portalPropertiesContent);

		Bom bom = new Bom();

		bom.setComponents(_getComponents(cryptoUses, portalProperties));
		bom.setMetadata(_getMetadata(configurationName));
		bom.setSerialNumber(_getSerialNumber(configurationName));
		bom.setVersion(1);

		BomJsonGenerator bomJsonGenerator = BomGeneratorFactory.createJson(
			Version.VERSION_16, bom);

		ObjectMapper objectMapper = new ObjectMapper();

		JsonNode jsonNode = objectMapper.readTree(
			bomJsonGenerator.toJsonString(false));

		DefaultPrettyPrinter defaultPrettyPrinter = new DefaultPrettyPrinter();

		defaultPrettyPrinter.indentArraysWith(
			DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);

		ObjectWriter objectWriter = objectMapper.writer(defaultPrettyPrinter);

		return objectWriter.writeValueAsString(jsonNode);
	}

	private static void _addProperties(
		String name, List<Property> properties, Collection<String> values) {

		for (String value : values) {
			properties.add(new Property(name, value));
		}
	}

	private static AlgorithmProperties _getAlgorithmProperties(
		List<CryptoUse> cryptoUses) {

		AlgorithmProperties algorithmProperties = new AlgorithmProperties();

		CryptoUse cryptoUse = cryptoUses.get(0);

		algorithmProperties.setCryptoFunctions(_getCryptoFunctions(cryptoUses));
		algorithmProperties.setPrimitive(_getPrimitive(cryptoUses));

		if (cryptoUse.getAlgorithm() == null) {
			return algorithmProperties;
		}

		if (cryptoUse.getKeySize() > 0) {
			algorithmProperties.setParameterSetIdentifier(
				String.valueOf(cryptoUse.getKeySize()));
		}

		if (Objects.equals(cryptoUse.getJCAClassName(), "Cipher")) {
			CBOMUtil.setCipherProperties(
				algorithmProperties, cryptoUse.getAlgorithm());
		}

		return algorithmProperties;
	}

	private static String _getAlgorithmRef(
		List<CryptoUse> cryptoUses, Map<String, String> jcaClassNames,
		List<CryptoUse> keyCryptoUses) {

		TreeSet<String> algorithmRefs = new TreeSet<>();

		for (CryptoUse keyCryptoUse : keyCryptoUses) {
			if (!keyCryptoUse.isResolved()) {
				continue;
			}

			List<CryptoUse> candidateCryptoUses = new ArrayList<>();
			List<CryptoUse> matchingCryptoUses = new ArrayList<>();

			for (CryptoUse cryptoUse : cryptoUses) {
				if (!_isKeyCandidate(cryptoUse, keyCryptoUse)) {
					continue;
				}

				candidateCryptoUses.add(cryptoUse);

				if (!keyCryptoUse.isConfigurable() &&
					!CBOMUtil.isKeyAlgorithm(
						cryptoUse.getAlgorithm(), cryptoUse.getJCAClassName(),
						keyCryptoUse.getAlgorithm())) {

					continue;
				}

				matchingCryptoUses.add(cryptoUse);
			}

			if (matchingCryptoUses.isEmpty() &&
				(candidateCryptoUses.size() == 1)) {

				matchingCryptoUses = candidateCryptoUses;
			}

			for (CryptoUse matchingCryptoUse : matchingCryptoUses) {
				algorithmRefs.add(_getBOMRef(matchingCryptoUse, jcaClassNames));
			}
		}

		if (algorithmRefs.size() != 1) {
			return null;
		}

		return algorithmRefs.first();
	}

	private static String _getBOMRef(
		CryptoUse cryptoUse, Map<String, String> jcaClassNames) {

		if (cryptoUse.isConfigurable()) {
			List<String> configuredBy = cryptoUse.getConfiguredBy();

			String propertyKey = configuredBy.get(0);

			return StringBundler.concat(
				"crypto:", jcaClassNames.get(propertyKey), StringPool.COLON,
				propertyKey);
		}

		StringBundler sb = new StringBundler(6);

		sb.append("crypto:");
		sb.append(cryptoUse.getJCAClassName());
		sb.append(StringPool.COLON);

		if (cryptoUse.getAlgorithm() == null) {
			sb.append(cryptoUse.getClassName());

			return sb.toString();
		}

		sb.append(cryptoUse.getAlgorithm());

		if (cryptoUse.getKeySize() > 0) {
			sb.append(StringPool.COLON);
			sb.append(cryptoUse.getKeySize());
		}

		return sb.toString();
	}

	private static List<Component> _getComponents(
		List<CryptoUse> cryptoUses, Properties portalProperties) {

		List<CryptoUse> sortedCryptoUses = new ArrayList<>(cryptoUses);

		sortedCryptoUses.sort(Comparator.comparing(CryptoUse::getJCAClassName));

		Map<String, String> jcaClassNames = new HashMap<>();

		for (CryptoUse cryptoUse : sortedCryptoUses) {
			if (!cryptoUse.isConfigurable()) {
				continue;
			}

			List<String> configuredBy = cryptoUse.getConfiguredBy();

			jcaClassNames.putIfAbsent(
				configuredBy.get(0), cryptoUse.getJCAClassName());
		}

		Map<String, List<CryptoUse>> cryptoUsesMap = new TreeMap<>();

		for (CryptoUse cryptoUse : sortedCryptoUses) {
			List<CryptoUse> bomRefCryptoUses = cryptoUsesMap.computeIfAbsent(
				_getBOMRef(cryptoUse, jcaClassNames),
				bomRef -> new ArrayList<>());

			bomRefCryptoUses.add(cryptoUse);
		}

		return TransformUtil.transform(
			cryptoUsesMap.entrySet(),
			entry -> _toComponent(
				entry.getKey(), entry.getValue(), cryptoUses, jcaClassNames,
				portalProperties));
	}

	private static List<CryptoFunction> _getCryptoFunctions(
		List<CryptoUse> cryptoUses) {

		Set<CryptoFunction> cryptoFunctions = new TreeSet<>();

		for (CryptoUse cryptoUse : cryptoUses) {
			cryptoFunctions.addAll(cryptoUse.getCryptoFunctions());
		}

		return new ArrayList<>(cryptoFunctions);
	}

	private static Evidence _getEvidence(List<CryptoUse> cryptoUses) {
		Set<String> classNames = new TreeSet<>();

		for (CryptoUse cryptoUse : cryptoUses) {
			classNames.add(cryptoUse.getClassName());
		}

		Evidence evidence = new Evidence();

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

	private static Metadata _getMetadata(String configurationName) {
		Metadata metadata = new Metadata();

		Component component = new Component();

		component.setName(ReleaseInfo.getName());
		component.setType(Component.Type.APPLICATION);
		component.setVersion(ReleaseInfo.getVersionDisplayName());

		LifecycleChoice lifecycleChoice = new LifecycleChoice();

		lifecycleChoice.setPhase(LifecycleChoice.Phase.POST_BUILD);

		Lifecycles lifecycles = new Lifecycles();

		lifecycles.setLifecycleChoice(
			Collections.singletonList(lifecycleChoice));

		metadata.setComponent(component);
		metadata.setLifecycles(lifecycles);
		metadata.setProperties(
			Arrays.asList(
				new Property("liferay:cbom:configuration", configurationName),
				new Property("liferay:cbom:schema-version", "1.0")));
		metadata.setTimestamp(null);

		return metadata;
	}

	private static String _getName(CryptoUse cryptoUse) {
		if (cryptoUse.isConfigurable()) {
			List<String> configuredBy = cryptoUse.getConfiguredBy();

			return configuredBy.get(0);
		}

		if (cryptoUse.getAlgorithm() == null) {
			return cryptoUse.getJCAClassName();
		}

		return CBOMUtil.getCycloneDXName(
			cryptoUse.getAlgorithm(), cryptoUse.getJCAClassName(),
			cryptoUse.getKeySize());
	}

	private static Primitive _getPrimitive(List<CryptoUse> cryptoUses) {
		TreeSet<Primitive> primitives = new TreeSet<>();

		for (CryptoUse cryptoUse : cryptoUses) {
			primitives.add(
				CBOMUtil.getPrimitive(
					cryptoUse.getAlgorithm(), cryptoUse.getJCAClassName()));
		}

		if (primitives.size() == 1) {
			return primitives.first();
		}

		return Primitive.UNKNOWN;
	}

	private static List<Property> _getProperties(
		List<CryptoUse> cryptoUses, Properties portalProperties) {

		Set<String> configuredBy = new TreeSet<>();
		Set<String> featureNames = new TreeSet<>();
		Set<String> jcaClassNames = new TreeSet<>();
		Set<String> moduleNames = new TreeSet<>();
		Set<String> providerNames = new TreeSet<>();
		Set<String> unresolvedReasons = new TreeSet<>();

		for (CryptoUse cryptoUse : cryptoUses) {
			configuredBy.addAll(cryptoUse.getConfiguredBy());
			featureNames.add(cryptoUse.getFeatureName());
			jcaClassNames.add(cryptoUse.getJCAClassName());

			if (cryptoUse.getBundleSymbolicName() != null) {
				moduleNames.add(cryptoUse.getBundleSymbolicName());
			}

			if (cryptoUse.getAlgorithm() != null) {
				Provider provider = CBOMUtil.fetchProvider(
					cryptoUse.getAlgorithm(), cryptoUse.getJCAClassName());

				if (provider != null) {
					providerNames.add(provider.getName());
				}
			}

			unresolvedReasons.addAll(cryptoUse.getUnresolvedReasons());
		}

		List<Property> properties = new ArrayList<>();

		_addProperties("liferay:cbom:configured-by", properties, configuredBy);

		List<String> defaultValues = TransformUtil.transform(
			configuredBy,
			propertyKey -> StringBundler.concat(
				propertyKey, StringPool.EQUAL,
				portalProperties.getProperty(propertyKey, StringPool.BLANK)));

		_addProperties("liferay:cbom:default-value", properties, defaultValues);

		_addProperties("liferay:cbom:feature", properties, featureNames);
		_addProperties("liferay:cbom:jca-class", properties, jcaClassNames);
		_addProperties("liferay:cbom:module", properties, moduleNames);
		_addProperties("liferay:cbom:provider", properties, providerNames);
		_addProperties(
			"liferay:cbom:unresolved-reason", properties, unresolvedReasons);

		return properties;
	}

	private static ProtocolProperties _getProtocolProperties(
		CryptoUse cryptoUse) {

		ProtocolProperties protocolProperties = new ProtocolProperties();

		if (cryptoUse.getAlgorithm() == null) {
			protocolProperties.setType(ProtocolType.TLS);

			return protocolProperties;
		}

		String algorithm = cryptoUse.getAlgorithm();

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
			List<CryptoUse> cryptoUses, Map<String, String> jcaClassNames,
			List<CryptoUse> keyCryptoUses) {

		RelatedCryptoMaterialProperties relatedCryptoMaterialProperties =
			new RelatedCryptoMaterialProperties();

		CryptoUse keyCryptoUse = keyCryptoUses.get(0);

		if (Objects.equals(keyCryptoUse.getJCAClassName(), "KeyStore")) {
			if (keyCryptoUse.getAlgorithm() != null) {
				relatedCryptoMaterialProperties.setFormat(
					keyCryptoUse.getAlgorithm());
			}

			relatedCryptoMaterialProperties.setType(
				RelatedCryptoMaterialType.OTHER);

			return relatedCryptoMaterialProperties;
		}

		relatedCryptoMaterialProperties.setAlgorithmRef(
			_getAlgorithmRef(cryptoUses, jcaClassNames, keyCryptoUses));

		if (Objects.equals(keyCryptoUse.getJCAClassName(), "SecretKeySpec")) {
			relatedCryptoMaterialProperties.setType(
				RelatedCryptoMaterialType.SECRET_KEY);

			return relatedCryptoMaterialProperties;
		}

		TreeSet<RelatedCryptoMaterialType> relatedCryptoMaterialTypes =
			new TreeSet<>();

		for (CryptoUse cryptoUse : keyCryptoUses) {
			relatedCryptoMaterialTypes.addAll(
				cryptoUse.getRelatedCryptoMaterialTypes());
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

	private static String _getSerialNumber(String configurationName) {
		String name =
			ReleaseInfo.getVersionDisplayName() + StringPool.POUND +
				configurationName;

		return "urn:uuid:" +
			UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8));
	}

	private static boolean _isKeyCandidate(
		CryptoUse cryptoUse, CryptoUse keyCryptoUse) {

		if (!_keyAlgorithmJCAClassNames.contains(cryptoUse.getJCAClassName()) ||
			!Objects.equals(
				cryptoUse.getClassName(), keyCryptoUse.getClassName()) ||
			!cryptoUse.isResolved() ||
			(cryptoUse.isConfigurable() != keyCryptoUse.isConfigurable())) {

			return false;
		}

		if (!keyCryptoUse.isConfigurable()) {
			return true;
		}

		return !Collections.disjoint(
			cryptoUse.getConfiguredBy(), keyCryptoUse.getConfiguredBy());
	}

	private static Component _toComponent(
		String bomRef, List<CryptoUse> bomRefCryptoUses,
		List<CryptoUse> cryptoUses, Map<String, String> jcaClassNames,
		Properties portalProperties) {

		Component component = new Component();
		CryptoProperties cryptoProperties = new CryptoProperties();

		CryptoUse cryptoUse = bomRefCryptoUses.get(0);

		AssetType assetType = CBOMUtil.getAssetType(
			cryptoUse.getJCAClassName());

		if (assetType == AssetType.ALGORITHM) {
			cryptoProperties.setAlgorithmProperties(
				_getAlgorithmProperties(bomRefCryptoUses));
		}
		else if (assetType == AssetType.PROTOCOL) {
			cryptoProperties.setProtocolProperties(
				_getProtocolProperties(cryptoUse));
		}
		else {
			cryptoProperties.setRelatedCryptoMaterialProperties(
				_getRelatedCryptoMaterialProperties(
					cryptoUses, jcaClassNames, bomRefCryptoUses));
		}

		cryptoProperties.setAssetType(assetType);

		component.setBomRef(bomRef);
		component.setCryptoProperties(cryptoProperties);
		component.setEvidence(_getEvidence(bomRefCryptoUses));
		component.setName(_getName(cryptoUse));
		component.setProperties(
			_getProperties(bomRefCryptoUses, portalProperties));
		component.setType(Component.Type.CRYPTOGRAPHIC_ASSET);

		return component;
	}

	private static final Set<String> _keyAlgorithmJCAClassNames =
		SetUtil.fromArray("Cipher", "KeyAgreement", "Mac", "Signature");

}