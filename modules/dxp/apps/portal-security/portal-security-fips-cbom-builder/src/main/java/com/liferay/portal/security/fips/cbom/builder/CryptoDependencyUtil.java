/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.fips.cbom.builder;

import com.liferay.petra.function.transform.TransformUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.cyclonedx.model.Component;
import org.cyclonedx.model.Dependency;
import org.cyclonedx.model.component.data.ComponentData;

/**
 * @author Manuele Castro
 */
public class CryptoDependencyUtil {

	public static List<Component> toComponents(List<CryptoCall> cryptoCalls) {
		Map<String, Component> componentsMap = new TreeMap<>();

		for (CryptoCall cryptoCall : cryptoCalls) {
			componentsMap.computeIfAbsent(
				"module:" + cryptoCall.getBundleSymbolicName(),
				bomRef -> _toLibraryComponent(
					bomRef, cryptoCall.getFeatureName()));

			String providerName = _fetchProviderName(cryptoCall);

			if (providerName != null) {
				componentsMap.computeIfAbsent(
					"provider:" + providerName,
					bomRef -> _toLibraryComponent(bomRef, providerName));
			}

			for (String propertyKey : cryptoCall.getConfiguredBy()) {
				componentsMap.computeIfAbsent(
					"setting:" + propertyKey,
					bomRef -> _toDataComponent(bomRef, propertyKey));
			}
		}

		return new ArrayList<>(componentsMap.values());
	}

	public static List<Dependency> toDependencies(
		List<CryptoCall> cryptoCalls) {

		Map<String, Set<String>> dependsOnMap = new TreeMap<>();

		for (CryptoCall cryptoCall : cryptoCalls) {
			String bomRef = CryptoAssetComponentUtil.getBOMRef(cryptoCall);

			Set<String> bomRefDependsOn = dependsOnMap.computeIfAbsent(
				bomRef, key -> new TreeSet<>());

			for (String propertyKey : cryptoCall.getConfiguredBy()) {
				bomRefDependsOn.add("setting:" + propertyKey);

				dependsOnMap.computeIfAbsent(
					"setting:" + propertyKey, key -> new TreeSet<>());
			}

			Set<String> moduleDependsOn = dependsOnMap.computeIfAbsent(
				"module:" + cryptoCall.getBundleSymbolicName(),
				key -> new TreeSet<>());

			moduleDependsOn.add(bomRef);

			String providerName = _fetchProviderName(cryptoCall);

			if (providerName == null) {
				continue;
			}

			dependsOnMap.computeIfAbsent(
				"provider:" + providerName, key -> new TreeSet<>());
		}

		return TransformUtil.transform(
			dependsOnMap.entrySet(),
			entry -> {
				Dependency dependency = new Dependency(entry.getKey());

				dependency.setDependencies(
					TransformUtil.transform(entry.getValue(), Dependency::new));

				return dependency;
			});
	}

	public static Map<String, Set<String>> toProvides(
		List<CryptoCall> cryptoCalls) {

		Map<String, Set<String>> provides = new TreeMap<>();

		for (CryptoCall cryptoCall : cryptoCalls) {
			String providerName = _fetchProviderName(cryptoCall);

			if (providerName == null) {
				continue;
			}

			Set<String> bomRefs = provides.computeIfAbsent(
				"provider:" + providerName, key -> new TreeSet<>());

			bomRefs.add(CryptoAssetComponentUtil.getBOMRef(cryptoCall));
		}

		return provides;
	}

	private static String _fetchProviderName(CryptoCall cryptoCall) {
		if (cryptoCall.getAlgorithm() == null) {
			return null;
		}

		return CryptoProviderUtil.fetchProviderName(
			cryptoCall.getAlgorithm(), cryptoCall.getJCAClassName());
	}

	private static Component _toDataComponent(
		String bomRef, String propertyKey) {

		ComponentData componentData = new ComponentData();

		if (CryptoCallScanner.isJDKPropertyKey(propertyKey)) {
			componentData.setName("java.security");
		}
		else {
			componentData.setName("portal.properties");
		}

		componentData.setType(ComponentData.ComponentDataType.CONFIGURATION);

		Component component = new Component();

		component.setBomRef(bomRef);
		component.setData(Collections.singletonList(componentData));
		component.setName(propertyKey);
		component.setType(Component.Type.DATA);

		return component;
	}

	private static Component _toLibraryComponent(String bomRef, String name) {
		Component component = new Component();

		component.setBomRef(bomRef);
		component.setName(name);
		component.setType(Component.Type.LIBRARY);

		return component;
	}

}