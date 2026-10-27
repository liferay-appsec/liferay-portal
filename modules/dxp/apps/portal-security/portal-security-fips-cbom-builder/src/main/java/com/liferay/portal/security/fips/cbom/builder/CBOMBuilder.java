/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.fips.cbom.builder;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropertiesUtil;
import com.liferay.portal.kernel.util.ReleaseInfo;

import java.io.File;
import java.io.FileInputStream;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.cyclonedx.model.Component;

/**
 * @author Manuele Castro
 */
public class CBOMBuilder {

	public static String build(
			String configurationName, List<CryptoCall> cryptoCalls)
		throws Exception {

		List<Component> components = new ArrayList<>(
			CryptoAssetComponentUtil.toComponents(cryptoCalls));

		components.addAll(CryptoDependencyUtil.toComponents(cryptoCalls));

		components.sort(Comparator.comparing(Component::getBomRef));

		return CycloneDXCBOMWriter.write(
			components, configurationName,
			CryptoDependencyUtil.toDependencies(cryptoCalls),
			ReleaseInfo.getName(), ReleaseInfo.getVersionDisplayName(),
			CryptoDependencyUtil.toProvides(cryptoCalls));
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

		CryptoCallScanner cryptoCallScanner = new CryptoCallScanner(false);

		for (File file : _getFiles(new File(args[3]), new File(args[2]))) {
			cryptoCallScanner.scan(file);
		}

		List<CryptoCall> cryptoCalls = cryptoCallScanner.getCryptoCalls();

		Set<String> messages = new TreeSet<>();

		for (CryptoCall cryptoCall : cryptoCalls) {
			if (cryptoCall.isResolved()) {
				continue;
			}

			messages.add(
				StringBundler.concat(
					"The scan cannot read the algorithm of the ",
					cryptoCall.getJCAClassName(), " call in ",
					cryptoCall.getClassName(), ", which comes from ",
					cryptoCall.getUnresolvedReason()));
		}

		for (String message : messages) {
			System.out.println(message);
		}

		String cbomJSON = build(configurationDir.getName(), cryptoCalls);

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

	private static Properties _loadProperties(File file) throws Exception {
		try (FileInputStream fileInputStream = new FileInputStream(file)) {
			return PropertiesUtil.load(fileInputStream, StringPool.UTF8);
		}
	}

}