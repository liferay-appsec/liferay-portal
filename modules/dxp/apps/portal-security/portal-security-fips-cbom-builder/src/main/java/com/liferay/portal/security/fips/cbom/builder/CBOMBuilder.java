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
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropertiesUtil;
import com.liferay.portal.kernel.util.ReleaseInfo;

import java.io.File;
import java.io.FileInputStream;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import java.security.Provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
import org.cyclonedx.model.component.crypto.CryptoProperties;
import org.cyclonedx.model.component.crypto.enums.AssetType;
import org.cyclonedx.model.component.evidence.Occurrence;

/**
 * @author Manuele Castro
 */
public class CBOMBuilder {

	public static String build(
			String configurationName, List<CryptoUse> cryptoUses)
		throws Exception {

		Bom bom = new Bom();

		bom.setComponents(_getComponents(cryptoUses));
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

	public static void main(String[] args) throws Exception {
		File configurationDir = new File(args[0]);

		Properties portalExtProperties = _loadProperties(
			new File(configurationDir, "portal-ext.properties"));

		if (GetterUtil.getBoolean(
				portalExtProperties.getProperty("fips.enabled"))) {

			throw new UnsupportedOperationException(
				"Unable to build the CBOM of a FIPS mode configuration");
		}

		CBOMData cbomData = new CBOMData();

		CBOMScanner cbomScanner = new CBOMScanner(
			cbomData.getBundleSymbolicNames(), cbomData);

		for (File file : _getFiles(new File(args[3]), new File(args[2]))) {
			cbomScanner.scan(file);
		}

		String cbomJSON = build(
			configurationDir.getName(), cbomData.getCryptoUses());

		File file = new File(args[1]);

		File parentDir = file.getParentFile();

		parentDir.mkdirs();

		Files.write(file.toPath(), cbomJSON.getBytes(StandardCharsets.UTF_8));

		System.out.println("Wrote " + file.getAbsolutePath());
	}

	private static void _addFiles(
		File dir, File excludedDir, List<File> files) {

		File[] childFiles = dir.listFiles();

		if (childFiles == null) {
			return;
		}

		for (File childFile : childFiles) {
			String fileName = childFile.getName();

			if (childFile.isDirectory()) {
				if (!childFile.equals(excludedDir)) {
					_addFiles(childFile, excludedDir, files);
				}
			}
			else if (fileName.endsWith(".jar") || fileName.endsWith(".lpkg")) {
				files.add(childFile);
			}
		}
	}

	private static void _addProperties(
		String name, List<Property> properties, Collection<String> values) {

		for (String value : values) {
			properties.add(new Property(name, value));
		}
	}

	private static String _getBOMRef(CryptoUse cryptoUse) {
		StringBundler sb = new StringBundler(4);

		sb.append("crypto:");
		sb.append(cryptoUse.getJCAClassName());
		sb.append(StringPool.COLON);
		sb.append(cryptoUse.getAlgorithm());

		return sb.toString();
	}

	private static List<Component> _getComponents(List<CryptoUse> cryptoUses) {
		List<CryptoUse> sortedCryptoUses = new ArrayList<>(cryptoUses);

		sortedCryptoUses.sort(Comparator.comparing(CryptoUse::getJCAClassName));

		Map<String, List<CryptoUse>> cryptoUsesMap = new TreeMap<>();

		for (CryptoUse cryptoUse : sortedCryptoUses) {
			List<CryptoUse> bomRefCryptoUses = cryptoUsesMap.computeIfAbsent(
				_getBOMRef(cryptoUse), bomRef -> new ArrayList<>());

			bomRefCryptoUses.add(cryptoUse);
		}

		return TransformUtil.transform(
			cryptoUsesMap.entrySet(),
			entry -> _toComponent(entry.getKey(), entry.getValue()));
	}

	private static Evidence _getEvidence(List<CryptoUse> cryptoUses) {
		Evidence evidence = new Evidence();

		Set<String> classNames = new TreeSet<>();

		for (CryptoUse cryptoUse : cryptoUses) {
			classNames.add(cryptoUse.getClassName());
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

	private static List<File> _getFiles(
		File appServerPortalDir, File liferayHomeDir) {

		List<File> files = new ArrayList<>();

		File osgiDir = new File(liferayHomeDir, "osgi");

		_addFiles(osgiDir, new File(osgiDir, "test"), files);

		_addFiles(new File(appServerPortalDir, "WEB-INF/lib"), null, files);
		_addFiles(
			new File(appServerPortalDir, "WEB-INF/shielded-container-lib"),
			null, files);

		files.sort(null);

		return files;
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

	private static List<Property> _getProperties(List<CryptoUse> cryptoUses) {
		Set<String> featureNames = new TreeSet<>();
		Set<String> jcaClassNames = new TreeSet<>();
		Set<String> moduleNames = new TreeSet<>();
		Set<String> providerNames = new TreeSet<>();

		for (CryptoUse cryptoUse : cryptoUses) {
			featureNames.add(cryptoUse.getFeatureName());
			jcaClassNames.add(cryptoUse.getJCAClassName());

			if (cryptoUse.getBundleSymbolicName() != null) {
				moduleNames.add(cryptoUse.getBundleSymbolicName());
			}

			Provider provider = CBOMUtil.fetchProvider(
				cryptoUse.getAlgorithm(), cryptoUse.getJCAClassName());

			if (provider == null) {
				continue;
			}

			providerNames.add(provider.getName());
		}

		List<Property> properties = new ArrayList<>();

		_addProperties("liferay:cbom:feature", properties, featureNames);
		_addProperties("liferay:cbom:jca-class", properties, jcaClassNames);
		_addProperties("liferay:cbom:module", properties, moduleNames);
		_addProperties("liferay:cbom:provider", properties, providerNames);

		return properties;
	}

	private static String _getSerialNumber(String configurationName) {
		String name =
			ReleaseInfo.getVersionDisplayName() + StringPool.POUND +
				configurationName;

		return "urn:uuid:" +
			UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8));
	}

	private static Properties _loadProperties(File file) throws Exception {
		try (FileInputStream fileInputStream = new FileInputStream(file)) {
			return PropertiesUtil.load(fileInputStream, StringPool.UTF8);
		}
	}

	private static Component _toComponent(
		String bomRef, List<CryptoUse> bomRefCryptoUses) {

		CryptoProperties cryptoProperties = new CryptoProperties();

		cryptoProperties.setAssetType(AssetType.ALGORITHM);

		CryptoUse cryptoUse = bomRefCryptoUses.get(0);

		Component component = new Component();

		component.setBomRef(bomRef);
		component.setCryptoProperties(cryptoProperties);
		component.setEvidence(_getEvidence(bomRefCryptoUses));
		component.setName(cryptoUse.getAlgorithm());
		component.setProperties(_getProperties(bomRefCryptoUses));
		component.setType(Component.Type.CRYPTOGRAPHIC_ASSET);

		return component;
	}

}