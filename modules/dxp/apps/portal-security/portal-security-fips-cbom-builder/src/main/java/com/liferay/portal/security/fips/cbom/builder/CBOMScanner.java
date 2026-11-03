/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.fips.cbom.builder;

import com.liferay.petra.string.CharPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.cyclonedx.model.component.crypto.enums.CryptoFunction;
import org.cyclonedx.model.component.crypto.enums.RelatedCryptoMaterialType;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceInterpreter;
import org.objectweb.asm.tree.analysis.SourceValue;

/**
 * @author Manuele Castro
 */
public class CBOMScanner {

	public CBOMScanner(CBOMData cbomData) {
		this(null, cbomData);
	}

	public CBOMScanner(Set<String> bundleSymbolicNames, CBOMData cbomData) {
		_bundleSymbolicNames = bundleSymbolicNames;
		_cbomData = cbomData;
	}

	public void scan(File file) throws IOException {
		String fileName = file.getName();

		if (fileName.endsWith(".lpkg")) {
			try (ZipFile zipFile = new ZipFile(file)) {
				for (ZipEntry zipEntry : Collections.list(zipFile.entries())) {
					String name = zipEntry.getName();

					if (!name.endsWith(".jar")) {
						continue;
					}

					try (JarInputStream jarInputStream = new JarInputStream(
							zipFile.getInputStream(zipEntry))) {

						_scan(jarInputStream, name);
					}
				}
			}

			return;
		}

		try (JarInputStream jarInputStream = new JarInputStream(
				new FileInputStream(file))) {

			_scan(jarInputStream, fileName);
		}
	}

	public void scan(
			String bundleSymbolicName, String featureName,
			InputStream inputStream)
		throws IOException {

		if (!_isScanned(bundleSymbolicName)) {
			return;
		}

		byte[] bytes = inputStream.readAllBytes();

		ClassNode classNode = new ClassNode();

		ClassReader classReader = new ClassReader(bytes);

		classReader.accept(
			classNode, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

		_scan(classNode, bundleSymbolicName, featureName);
	}

	private Frame<SourceValue>[] _analyze(
			ClassNode classNode, MethodNode methodNode)
		throws AnalyzerException {

		Analyzer<SourceValue> analyzer = new Analyzer<>(
			new OriginInterpreter());

		return analyzer.analyze(classNode.name, methodNode);
	}

	private CryptoUse _getAlgorithmCryptoUse(
		Frame<SourceValue>[] frames, InsnList insnList,
		MethodInsnNode methodInsnNode) {

		Type[] argumentTypes = Type.getArgumentTypes(methodInsnNode.desc);

		return _getArgumentCryptoUse(
			frames, insnList, methodInsnNode, 0, argumentTypes.length);
	}

	private SourceValue _getArgument(
		Frame<SourceValue> frame, int index, int count) {

		return frame.getStack(frame.getStackSize() - count + index);
	}

	private CryptoUse _getArgumentCryptoUse(
		Frame<SourceValue>[] frames, InsnList insnList,
		AbstractInsnNode abstractInsnNode, int index, int count) {

		return _getCryptoUse(
			_getArgument(
				frames[insnList.indexOf(abstractInsnNode)], index, count));
	}

	private String _getBundleSymbolicName(String fileName, Manifest manifest) {
		if (manifest != null) {
			Attributes attributes = manifest.getMainAttributes();

			String bundleSymbolicName = attributes.getValue(
				"Bundle-SymbolicName");

			if (Validator.isNotNull(bundleSymbolicName)) {
				int index = bundleSymbolicName.indexOf(CharPool.SEMICOLON);

				if (index != -1) {
					bundleSymbolicName = bundleSymbolicName.substring(0, index);
				}

				return StringUtil.trim(bundleSymbolicName);
			}
		}

		return _getFileName(fileName);
	}

	private String _getClassName(String internalName) {
		String className = StringUtil.replace(
			internalName, CharPool.SLASH, CharPool.PERIOD);

		int index = className.indexOf(CharPool.DOLLAR);

		if (index != -1) {
			className = className.substring(0, index);
		}

		return className;
	}

	private List<CryptoFunction> _getCryptoFunctions(
		Frame<SourceValue>[] frames, InsnList insnList, String jcaClassName,
		MethodInsnNode methodInsnNode) {

		CryptoFunction cryptoFunction = CBOMUtil.fetchCryptoFunction(
			jcaClassName);

		if (cryptoFunction != null) {
			return Collections.singletonList(cryptoFunction);
		}

		Set<CryptoFunction> cryptoFunctions = new TreeSet<>();

		for (MethodInsnNode linkedMethodInsnNode :
				_getLinkedMethodInsnNodes(frames, insnList, methodInsnNode)) {

			if (Objects.equals(linkedMethodInsnNode.name, "initSign")) {
				cryptoFunctions.add(CryptoFunction.SIGN);
			}
			else if (Objects.equals(linkedMethodInsnNode.name, "initVerify")) {
				cryptoFunctions.add(CryptoFunction.VERIFY);
			}
			else if (Objects.equals(linkedMethodInsnNode.name, "init") &&
					 linkedMethodInsnNode.desc.startsWith("(I")) {

				CryptoUse cryptoUse = _getArgumentCryptoUse(
					frames, insnList, linkedMethodInsnNode, 0,
					Type.getArgumentCount(linkedMethodInsnNode.desc));

				for (String value : cryptoUse.getValues()) {
					cryptoFunction = CBOMUtil.fetchCipherCryptoFunction(value);

					if (cryptoFunction == null) {
						continue;
					}

					cryptoFunctions.add(cryptoFunction);
				}
			}
		}

		if (cryptoFunctions.isEmpty()) {
			return Collections.singletonList(CryptoFunction.UNKNOWN);
		}

		return new ArrayList<>(cryptoFunctions);
	}

	private List<MethodInsnNode> _getCryptoMethodInsnNodes(
		MethodNode methodNode) {

		List<MethodInsnNode> methodInsnNodes = new ArrayList<>();

		for (AbstractInsnNode abstractInsnNode : methodNode.instructions) {
			if (!(abstractInsnNode instanceof MethodInsnNode)) {
				continue;
			}

			MethodInsnNode methodInsnNode = (MethodInsnNode)abstractInsnNode;

			if (_getJCAClassName(methodInsnNode) == null) {
				continue;
			}

			methodInsnNodes.add(methodInsnNode);
		}

		return methodInsnNodes;
	}

	private CryptoUse _getCryptoUse(AbstractInsnNode abstractInsnNode) {
		CryptoUse cryptoUse = new CryptoUse();

		int opcode = abstractInsnNode.getOpcode();

		if (abstractInsnNode instanceof LdcInsnNode) {
			LdcInsnNode ldcInsnNode = (LdcInsnNode)abstractInsnNode;

			cryptoUse.addValue(String.valueOf(ldcInsnNode.cst));
		}
		else if ((opcode >= Opcodes.ICONST_M1) &&
				 (opcode <= Opcodes.ICONST_5)) {

			cryptoUse.addValue(String.valueOf(opcode - Opcodes.ICONST_0));
		}
		else if ((opcode == Opcodes.BIPUSH) || (opcode == Opcodes.SIPUSH)) {
			IntInsnNode intInsnNode = (IntInsnNode)abstractInsnNode;

			cryptoUse.addValue(String.valueOf(intInsnNode.operand));
		}

		return cryptoUse;
	}

	private CryptoUse _getCryptoUse(SourceValue sourceValue) {
		CryptoUse cryptoUse = new CryptoUse();

		for (AbstractInsnNode abstractInsnNode : sourceValue.insns) {
			cryptoUse.addAll(_getCryptoUse(abstractInsnNode));
		}

		return cryptoUse;
	}

	private String _getFeatureName(String fileName, Manifest manifest) {
		if (manifest != null) {
			Attributes attributes = manifest.getMainAttributes();

			String bundleName = attributes.getValue("Bundle-Name");

			if (Validator.isNotNull(bundleName)) {
				return bundleName;
			}
		}

		return _getFileName(fileName);
	}

	private String _getFileName(String fileName) {
		String name = fileName.substring(
			fileName.lastIndexOf(CharPool.SLASH) + 1);

		int index = name.lastIndexOf(CharPool.PERIOD);

		if (index > 0) {
			name = name.substring(0, index);
		}

		return name;
	}

	private String _getJCAClassName(MethodInsnNode methodInsnNode) {
		if ((methodInsnNode.getOpcode() == Opcodes.INVOKESTATIC) &&
			Objects.equals(methodInsnNode.name, "getInstance") &&
			methodInsnNode.desc.startsWith("(Ljava/lang/String;")) {

			return CBOMUtil.fetchJCAClassName(methodInsnNode.owner);
		}

		return null;
	}

	private CryptoUse _getKeySizeCryptoUse(
		Frame<SourceValue>[] frames, InsnList insnList,
		MethodInsnNode methodInsnNode) {

		CryptoUse cryptoUse = new CryptoUse();

		String methodName = CBOMUtil.fetchKeySizeMethodName(
			methodInsnNode.owner);

		if (methodName == null) {
			return cryptoUse;
		}

		for (MethodInsnNode linkedMethodInsnNode :
				_getLinkedMethodInsnNodes(frames, insnList, methodInsnNode)) {

			if (!Objects.equals(linkedMethodInsnNode.name, methodName) ||
				!linkedMethodInsnNode.desc.startsWith("(I")) {

				continue;
			}

			cryptoUse.addAll(
				_getArgumentCryptoUse(
					frames, insnList, linkedMethodInsnNode, 0,
					Type.getArgumentCount(linkedMethodInsnNode.desc)));
		}

		return cryptoUse;
	}

	private List<MethodInsnNode> _getLinkedMethodInsnNodes(
		Frame<SourceValue>[] frames, InsnList insnList,
		MethodInsnNode methodInsnNode) {

		List<MethodInsnNode> linkedMethodInsnNodes = new ArrayList<>();

		for (AbstractInsnNode abstractInsnNode : insnList) {
			if (abstractInsnNode.getOpcode() != Opcodes.INVOKEVIRTUAL) {
				continue;
			}

			MethodInsnNode curMethodInsnNode = (MethodInsnNode)abstractInsnNode;

			Frame<SourceValue> frame =
				frames[insnList.indexOf(curMethodInsnNode)];

			if ((frame == null) ||
				!Objects.equals(
					curMethodInsnNode.owner, methodInsnNode.owner)) {

				continue;
			}

			SourceValue sourceValue = _getReceiver(
				frame, Type.getArgumentCount(curMethodInsnNode.desc));

			if (!sourceValue.insns.contains(methodInsnNode)) {
				continue;
			}

			linkedMethodInsnNodes.add(curMethodInsnNode);
		}

		return linkedMethodInsnNodes;
	}

	private SourceValue _getReceiver(Frame<SourceValue> frame, int count) {
		return frame.getStack(frame.getStackSize() - count - 1);
	}

	private List<RelatedCryptoMaterialType> _getRelatedCryptoMaterialTypes(
		Frame<SourceValue>[] frames, InsnList insnList, String jcaClassName,
		MethodInsnNode methodInsnNode) {

		if (!Objects.equals(jcaClassName, "KeyFactory")) {
			return Collections.emptyList();
		}

		Set<RelatedCryptoMaterialType> relatedCryptoMaterialTypes =
			new TreeSet<>();

		for (MethodInsnNode linkedMethodInsnNode :
				_getLinkedMethodInsnNodes(frames, insnList, methodInsnNode)) {

			RelatedCryptoMaterialType relatedCryptoMaterialType =
				CBOMUtil.fetchKeyFactoryRelatedCryptoMaterialType(
					linkedMethodInsnNode.name);

			if (relatedCryptoMaterialType == null) {
				continue;
			}

			relatedCryptoMaterialTypes.add(relatedCryptoMaterialType);
		}

		return new ArrayList<>(relatedCryptoMaterialTypes);
	}

	private boolean _isClassFileName(String name) {
		if (name.startsWith("com/liferay/") && name.endsWith(".class")) {
			return true;
		}

		return false;
	}

	private boolean _isScanned(String bundleSymbolicName) {
		if ((_bundleSymbolicNames == null) ||
			_bundleSymbolicNames.contains(bundleSymbolicName)) {

			return true;
		}

		return false;
	}

	private void _scan(
			ClassNode classNode, String bundleSymbolicName, String featureName)
		throws IOException {

		String className = _getClassName(classNode.name);

		for (MethodNode methodNode : classNode.methods) {
			if (methodNode.instructions.size() == 0) {
				continue;
			}

			List<MethodInsnNode> methodInsnNodes = _getCryptoMethodInsnNodes(
				methodNode);

			Frame<SourceValue>[] frames = null;

			try {
				frames = _analyze(classNode, methodNode);
			}
			catch (AnalyzerException analyzerException) {
				throw new IOException(analyzerException);
			}

			InsnList insnList = methodNode.instructions;

			for (MethodInsnNode methodInsnNode : methodInsnNodes) {
				if (frames[insnList.indexOf(methodInsnNode)] == null) {
					continue;
				}

				String jcaClassName = _getJCAClassName(methodInsnNode);

				CryptoUse cryptoUse = new CryptoUse(
					bundleSymbolicName, className, featureName, jcaClassName);

				cryptoUse.setCryptoFunctions(
					_getCryptoFunctions(
						frames, insnList, jcaClassName, methodInsnNode));
				cryptoUse.setRelatedCryptoMaterialTypes(
					_getRelatedCryptoMaterialTypes(
						frames, insnList, jcaClassName, methodInsnNode));

				cryptoUse.addAll(
					_getAlgorithmCryptoUse(frames, insnList, methodInsnNode));

				_cbomData.addCryptoUse(
					cryptoUse,
					_getKeySizeCryptoUse(frames, insnList, methodInsnNode));
			}
		}
	}

	private void _scan(JarInputStream jarInputStream, String name)
		throws IOException {

		Manifest manifest = jarInputStream.getManifest();

		String bundleSymbolicName = _getBundleSymbolicName(name, manifest);

		if (!_isScanned(bundleSymbolicName)) {
			return;
		}

		String featureName = _getFeatureName(name, manifest);

		JarEntry jarEntry = null;

		while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
			String jarEntryName = jarEntry.getName();

			if (!_isClassFileName(jarEntryName)) {
				continue;
			}

			scan(bundleSymbolicName, featureName, jarInputStream);
		}
	}

	private final Set<String> _bundleSymbolicNames;
	private final CBOMData _cbomData;

	private static class OriginInterpreter extends SourceInterpreter {

		public OriginInterpreter() {
			super(Opcodes.ASM9);
		}

		@Override
		public SourceValue copyOperation(
			AbstractInsnNode abstractInsnNode, SourceValue sourceValue) {

			return sourceValue;
		}

	}

}