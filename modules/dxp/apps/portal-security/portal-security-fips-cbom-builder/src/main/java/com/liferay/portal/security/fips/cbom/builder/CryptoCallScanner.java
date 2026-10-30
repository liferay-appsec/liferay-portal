/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.fips.cbom.builder;

import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.crypto.Cipher;

import org.cyclonedx.model.component.crypto.enums.CryptoFunction;
import org.cyclonedx.model.component.crypto.enums.RelatedCryptoMaterialType;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
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
public class CryptoCallScanner {

	public static boolean isJDKPropertyKey(String propertyKey) {
		return _jdkPropertyKeys.containsValue(propertyKey);
	}

	public CryptoCallScanner(boolean fipsEnabled) {
		_fipsEnabled = fipsEnabled;
	}

	public List<CryptoCall> getCryptoCalls() {
		List<CryptoCall> cryptoCalls = new ArrayList<>();

		for (ScannedCryptoCall scannedCryptoCall : _scannedCryptoCalls) {
			cryptoCalls.add(_toCryptoCall(scannedCryptoCall));
		}

		return cryptoCalls;
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

		byte[] bytes = inputStream.readAllBytes();

		if (!_hasCryptoReference(bytes)) {
			return;
		}

		ClassNode classNode = new ClassNode();

		ClassReader classReader = new ClassReader(bytes);

		classReader.accept(
			classNode, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

		_scan(classNode, bundleSymbolicName, featureName);
	}

	private Frame<SourceValue>[] _analyze(
			ClassNode classNode, MethodNode methodNode)
		throws AnalyzerException {

		_foldFIPSEnabled(methodNode.instructions);

		Analyzer<SourceValue> analyzer = new Analyzer<>(
			new OriginInterpreter(
				_getMethodKey(classNode.name, methodNode.name, methodNode.desc),
				methodNode));

		return analyzer.analyze(classNode.name, methodNode);
	}

	private void _foldFIPSEnabled(InsnList insnList) {
		for (AbstractInsnNode abstractInsnNode : insnList.toArray()) {
			if (!_isFIPSEnabled(abstractInsnNode)) {
				continue;
			}

			AbstractInsnNode nextAbstractInsnNode = abstractInsnNode.getNext();

			while ((nextAbstractInsnNode != null) &&
				   (nextAbstractInsnNode.getOpcode() == -1)) {

				nextAbstractInsnNode = nextAbstractInsnNode.getNext();
			}

			if ((nextAbstractInsnNode == null) ||
				((nextAbstractInsnNode.getOpcode() != Opcodes.IFEQ) &&
				 (nextAbstractInsnNode.getOpcode() != Opcodes.IFNE))) {

				continue;
			}

			JumpInsnNode jumpInsnNode = (JumpInsnNode)nextAbstractInsnNode;

			insnList.remove(abstractInsnNode);

			if ((jumpInsnNode.getOpcode() == Opcodes.IFEQ) != _fipsEnabled) {
				insnList.set(
					jumpInsnNode,
					new JumpInsnNode(Opcodes.GOTO, jumpInsnNode.label));
			}
			else {
				insnList.remove(jumpInsnNode);
			}
		}
	}

	private Origin _getAlgorithmOrigin(
		Frame<SourceValue>[] frames, InsnList insnList,
		MethodInsnNode methodInsnNode) {

		Type[] argumentTypes = Type.getArgumentTypes(methodInsnNode.desc);

		if (Objects.equals(methodInsnNode.owner, _CLASS_NAME_DIGESTER_UTIL) &&
			((argumentTypes.length < 2) ||
			 !Objects.equals(argumentTypes[0], Type.getType(String.class)))) {

			Origin origin = new Origin();

			origin.addReference(
				_getMethodKey(
					_CLASS_NAME_DIGESTER_UTIL, "_getDefaultAlgorithm",
					"()Ljava/lang/String;"));

			return origin;
		}

		int index = 0;

		if (Objects.equals(methodInsnNode.owner, _CLASS_NAME_SECRET_KEY_SPEC)) {
			index = argumentTypes.length - 1;
		}

		return _getArgumentOrigin(
			frames, insnList, methodInsnNode, index, argumentTypes.length);
	}

	private SourceValue _getArgument(
		Frame<SourceValue> frame, int index, int count) {

		return frame.getStack(frame.getStackSize() - count + index);
	}

	private Origin _getArgumentOrigin(
		Frame<SourceValue>[] frames, InsnList insnList,
		AbstractInsnNode abstractInsnNode, int index, int count) {

		return _getOrigin(
			frames, insnList,
			_getArgument(
				frames[insnList.indexOf(abstractInsnNode)], index, count),
			new HashSet<>());
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

		CryptoFunction cryptoFunction = _cryptoFunctions.get(jcaClassName);

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

				Origin origin = _getArgumentOrigin(
					frames, insnList, linkedMethodInsnNode, 0,
					Type.getArgumentCount(linkedMethodInsnNode.desc));

				for (String value : origin.getValues()) {
					cryptoFunction = _cipherCryptoFunctions.get(value);

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
		ClassNode classNode, MethodNode methodNode) {

		List<MethodInsnNode> methodInsnNodes = new ArrayList<>();

		if (Objects.equals(classNode.name, _CLASS_NAME_DIGESTER_UTIL)) {
			return methodInsnNodes;
		}

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

	private String _getFieldKey(FieldInsnNode fieldInsnNode) {
		return _getKeyName(fieldInsnNode.owner) + StringPool.PERIOD +
			fieldInsnNode.name;
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
		if (methodInsnNode.getOpcode() == Opcodes.INVOKESTATIC) {
			if (Objects.equals(
					methodInsnNode.owner, _CLASS_NAME_DIGESTER_UTIL) &&
				methodInsnNode.name.startsWith("digest")) {

				return "MessageDigest";
			}

			if (Objects.equals(methodInsnNode.name, "getInstance") &&
				methodInsnNode.desc.startsWith("(Ljava/lang/String;")) {

				return _jcaClassNames.get(methodInsnNode.owner);
			}

			return null;
		}

		if ((methodInsnNode.getOpcode() == Opcodes.INVOKESPECIAL) &&
			Objects.equals(methodInsnNode.owner, _CLASS_NAME_SECRET_KEY_SPEC) &&
			Objects.equals(methodInsnNode.name, "<init>") &&
			methodInsnNode.desc.endsWith("Ljava/lang/String;)V")) {

			return "SecretKeySpec";
		}

		return null;
	}

	private String _getKeyName(String internalName) {
		return StringUtil.replace(
			internalName, CharPool.SLASH, CharPool.PERIOD);
	}

	private Origin _getKeySizeOrigin(
		Frame<SourceValue>[] frames, InsnList insnList,
		MethodInsnNode methodInsnNode) {

		Origin origin = new Origin();

		String methodName = _keySizeMethodNames.get(methodInsnNode.owner);

		if (methodName == null) {
			return origin;
		}

		for (MethodInsnNode linkedMethodInsnNode :
				_getLinkedMethodInsnNodes(frames, insnList, methodInsnNode)) {

			if (!Objects.equals(linkedMethodInsnNode.name, methodName) ||
				!linkedMethodInsnNode.desc.startsWith("(I")) {

				continue;
			}

			origin.addAll(
				_getArgumentOrigin(
					frames, insnList, linkedMethodInsnNode, 0,
					Type.getArgumentCount(linkedMethodInsnNode.desc)));
		}

		return origin;
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

	private String _getMethodKey(
		String internalName, String name, String desc) {

		return StringBundler.concat(
			_getKeyName(internalName), StringPool.PERIOD, name, desc);
	}

	private Origin _getOrigin(
		Frame<SourceValue>[] frames, InsnList insnList,
		AbstractInsnNode abstractInsnNode,
		Set<AbstractInsnNode> visitedAbstractInsnNodes) {

		Origin origin = new Origin();

		int opcode = abstractInsnNode.getOpcode();

		if (abstractInsnNode instanceof ParameterInsnNode) {
			ParameterInsnNode parameterInsnNode =
				(ParameterInsnNode)abstractInsnNode;

			origin.addReference(parameterInsnNode.getParameterKey());
		}
		else if (abstractInsnNode instanceof LdcInsnNode) {
			LdcInsnNode ldcInsnNode = (LdcInsnNode)abstractInsnNode;

			origin.addValue(String.valueOf(ldcInsnNode.cst));
		}
		else if ((opcode >= Opcodes.ICONST_M1) &&
				 (opcode <= Opcodes.ICONST_5)) {

			origin.addValue(String.valueOf(opcode - Opcodes.ICONST_0));
		}
		else if ((opcode == Opcodes.BIPUSH) || (opcode == Opcodes.SIPUSH)) {
			IntInsnNode intInsnNode = (IntInsnNode)abstractInsnNode;

			origin.addValue(String.valueOf(intInsnNode.operand));
		}
		else if ((opcode == Opcodes.GETFIELD) ||
				 (opcode == Opcodes.GETSTATIC)) {

			origin.addReference(_getFieldKey((FieldInsnNode)abstractInsnNode));
		}
		else if (opcode == Opcodes.CHECKCAST) {
			origin.addAll(
				_getOrigin(
					frames, insnList,
					_getArgument(
						frames[insnList.indexOf(abstractInsnNode)], 0, 1),
					visitedAbstractInsnNodes));
		}
		else if (abstractInsnNode instanceof MethodInsnNode) {
			origin.addAll(
				_getReturnValueOrigin(
					frames, insnList, (MethodInsnNode)abstractInsnNode,
					visitedAbstractInsnNodes));
		}
		else if (opcode != Opcodes.ACONST_NULL) {
			origin.addUnresolvedReason("a value the code computes");
		}

		return origin;
	}

	private Origin _getOrigin(
		Frame<SourceValue>[] frames, InsnList insnList, SourceValue sourceValue,
		Set<AbstractInsnNode> visitedAbstractInsnNodes) {

		Origin origin = new Origin();

		for (AbstractInsnNode abstractInsnNode : sourceValue.insns) {
			if (!visitedAbstractInsnNodes.add(abstractInsnNode)) {
				continue;
			}

			origin.addAll(
				_getOrigin(
					frames, insnList, abstractInsnNode,
					visitedAbstractInsnNodes));
		}

		return origin;
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
				_keyFactoryRelatedCryptoMaterialTypes.get(
					linkedMethodInsnNode.name);

			if (relatedCryptoMaterialType == null) {
				continue;
			}

			relatedCryptoMaterialTypes.add(relatedCryptoMaterialType);
		}

		return new ArrayList<>(relatedCryptoMaterialTypes);
	}

	private Origin _getReturnValueOrigin(
		Frame<SourceValue>[] frames, InsnList insnList,
		MethodInsnNode methodInsnNode,
		Set<AbstractInsnNode> visitedAbstractInsnNodes) {

		Origin origin = new Origin();

		String propertyKey = _jdkPropertyKeys.get(
			methodInsnNode.owner + StringPool.PERIOD + methodInsnNode.name);

		if (propertyKey != null) {
			origin.addPropertyKey(propertyKey);

			return origin;
		}

		Frame<SourceValue> frame = frames[insnList.indexOf(methodInsnNode)];
		int count = Type.getArgumentCount(methodInsnNode.desc);

		if (methodInsnNode.owner.endsWith("/PropsUtil") &&
			Objects.equals(methodInsnNode.name, "get") && (count > 0)) {

			Origin keyOrigin = _getOrigin(
				frames, insnList, _getArgument(frame, 0, count),
				visitedAbstractInsnNodes);

			for (String value : keyOrigin.getValues()) {
				origin.addPropertyKey(value);
			}

			if (!keyOrigin.isConstant()) {
				origin.addUnresolvedReason(
					"a property whose name the code computes");
			}

			return origin;
		}

		if (_jcaClassNames.containsKey(methodInsnNode.owner) &&
			Objects.equals(methodInsnNode.name, "getAlgorithm") &&
			(count == 0)) {

			for (AbstractInsnNode abstractInsnNode :
					_getReceiver(frame, count).insns) {

				if ((abstractInsnNode instanceof MethodInsnNode) &&
					(_getJCAClassName((MethodInsnNode)abstractInsnNode) !=
						null)) {

					origin.addAll(
						_getAlgorithmOrigin(
							frames, insnList,
							(MethodInsnNode)abstractInsnNode));
				}
				else {
					origin.addUnresolvedReason(
						"the algorithm of a " +
							_jcaClassNames.get(methodInsnNode.owner) +
								" that the code does not create");
				}
			}

			return origin;
		}

		Function<String, String> function = _stringFunctions.get(
			methodInsnNode.owner + StringPool.PERIOD + methodInsnNode.name);

		if (function == null) {
			origin.addReference(
				_getMethodKey(
					methodInsnNode.owner, methodInsnNode.name,
					methodInsnNode.desc));

			return origin;
		}

		SourceValue sourceValue = null;

		if (methodInsnNode.getOpcode() == Opcodes.INVOKESTATIC) {
			sourceValue = _getArgument(frame, 0, count);
		}
		else {
			sourceValue = _getReceiver(frame, count);
		}

		Origin wrappedOrigin = _getOrigin(
			frames, insnList, sourceValue, visitedAbstractInsnNodes);

		wrappedOrigin.transformValues(function);

		return wrappedOrigin;
	}

	private String _getUnresolvedReason(String reference) {
		int index = reference.indexOf(CharPool.POUND);

		if (index != -1) {
			String methodName = reference.substring(
				0, reference.indexOf(CharPool.OPEN_PARENTHESIS));

			if (methodName.endsWith(".<init>")) {
				methodName =
					"the constructor of " +
						StringUtil.removeLast(methodName, ".<init>");
			}

			return StringBundler.concat(
				"parameter ",
				GetterUtil.getInteger(reference.substring(index + 1)) + 1,
				" of ", methodName);
		}

		index = reference.indexOf(CharPool.OPEN_PARENTHESIS);

		if (index != -1) {
			return "the return value of " + reference.substring(0, index) +
				"()";
		}

		return "the field " + reference;
	}

	private boolean _hasCryptoReference(byte[] bytes) {
		String classFileContent = new String(
			bytes, StandardCharsets.ISO_8859_1);

		for (String needle : _needles) {
			if (classFileContent.contains(needle)) {
				return true;
			}
		}

		return false;
	}

	private boolean _isClassFileName(String name) {
		if (name.startsWith("com/liferay/") && name.endsWith(".class")) {
			return true;
		}

		return false;
	}

	private boolean _isFIPSEnabled(AbstractInsnNode abstractInsnNode) {
		if (abstractInsnNode.getOpcode() != Opcodes.GETSTATIC) {
			return false;
		}

		FieldInsnNode fieldInsnNode = (FieldInsnNode)abstractInsnNode;

		if (fieldInsnNode.owner.endsWith("/PropsValues") &&
			Objects.equals(fieldInsnNode.name, "FIPS_ENABLED")) {

			return true;
		}

		return false;
	}

	private boolean _isResolvable(String reference) {
		int index = reference.indexOf(CharPool.POUND);

		if (index == -1) {
			return true;
		}

		return _privateMethodKeys.contains(reference.substring(0, index));
	}

	private boolean _isTracked(Type type) {
		if (Objects.equals(type, Type.INT_TYPE) ||
			Objects.equals(type, Type.getType(String.class))) {

			return true;
		}

		return false;
	}

	private Origin _resolve(Origin origin, Set<String> visitedReferences) {
		Origin resolvedOrigin = new Origin();

		resolvedOrigin.addAll(origin);

		resolvedOrigin.clearReferences();

		for (String reference : origin.getReferences()) {
			Origin referenceOrigin = _referenceOrigins.get(reference);

			if ((referenceOrigin == null) || !_isResolvable(reference) ||
				!visitedReferences.add(reference)) {

				resolvedOrigin.addUnresolvedReason(
					_getUnresolvedReason(reference));
			}
			else {
				resolvedOrigin.addAll(
					_resolve(referenceOrigin, visitedReferences));
			}
		}

		return resolvedOrigin;
	}

	private void _scan(
		ClassNode classNode, String bundleSymbolicName, String featureName) {

		String className = _getClassName(classNode.name);

		for (MethodNode methodNode : classNode.methods) {
			if (methodNode.instructions.size() == 0) {
				continue;
			}

			List<MethodInsnNode> methodInsnNodes = _getCryptoMethodInsnNodes(
				classNode, methodNode);

			Frame<SourceValue>[] frames = null;

			try {
				frames = _analyze(classNode, methodNode);
			}
			catch (AnalyzerException analyzerException) {
				for (MethodInsnNode methodInsnNode : methodInsnNodes) {
					Origin origin = new Origin();

					origin.addUnresolvedReason(
						"bytecode that the scan cannot analyze: " +
							analyzerException.getMessage());

					_scannedCryptoCalls.add(
						new ScannedCryptoCall(
							origin,
							new CryptoCall(
								bundleSymbolicName, className, featureName,
								_getJCAClassName(methodInsnNode)),
							new Origin()));
				}

				continue;
			}

			_scanAssignments(classNode, frames, methodNode);

			InsnList insnList = methodNode.instructions;

			for (MethodInsnNode methodInsnNode : methodInsnNodes) {
				if (frames[insnList.indexOf(methodInsnNode)] == null) {
					continue;
				}

				String jcaClassName = _getJCAClassName(methodInsnNode);

				CryptoCall cryptoCall = new CryptoCall(
					bundleSymbolicName, className, featureName, jcaClassName);

				cryptoCall.setCryptoFunctions(
					_getCryptoFunctions(
						frames, insnList, jcaClassName, methodInsnNode));
				cryptoCall.setRelatedCryptoMaterialTypes(
					_getRelatedCryptoMaterialTypes(
						frames, insnList, jcaClassName, methodInsnNode));

				_scannedCryptoCalls.add(
					new ScannedCryptoCall(
						_getAlgorithmOrigin(frames, insnList, methodInsnNode),
						cryptoCall,
						_getKeySizeOrigin(frames, insnList, methodInsnNode)));
			}
		}
	}

	private void _scan(JarInputStream jarInputStream, String name)
		throws IOException {

		Manifest manifest = jarInputStream.getManifest();

		String bundleSymbolicName = _getBundleSymbolicName(name, manifest);
		String featureName = _getFeatureName(name, manifest);

		JarEntry jarEntry = null;

		while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
			if (!_isClassFileName(jarEntry.getName())) {
				continue;
			}

			scan(bundleSymbolicName, featureName, jarInputStream);
		}
	}

	private void _scanAssignments(
		ClassNode classNode, Frame<SourceValue>[] frames,
		MethodNode methodNode) {

		String className = _getClassName(classNode.name);

		String methodKey = _getMethodKey(
			classNode.name, methodNode.name, methodNode.desc);

		if ((methodNode.access & Opcodes.ACC_PRIVATE) != 0) {
			_privateMethodKeys.add(methodKey);
		}

		boolean returnValueTracked = false;

		if (((methodNode.access & (Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC)) !=
				0) &&
			(Type.getArgumentCount(methodNode.desc) == 0) &&
			_isTracked(Type.getReturnType(methodNode.desc))) {

			returnValueTracked = true;
		}

		InsnList insnList = methodNode.instructions;

		for (AbstractInsnNode abstractInsnNode : insnList) {
			if (frames[insnList.indexOf(abstractInsnNode)] == null) {
				continue;
			}

			int opcode = abstractInsnNode.getOpcode();

			if ((opcode == Opcodes.PUTFIELD) || (opcode == Opcodes.PUTSTATIC)) {
				FieldInsnNode fieldInsnNode = (FieldInsnNode)abstractInsnNode;

				if (Objects.equals(fieldInsnNode.owner, classNode.name) &&
					_isTracked(Type.getType(fieldInsnNode.desc))) {

					_track(
						_getFieldKey(fieldInsnNode),
						_getArgumentOrigin(
							frames, insnList, abstractInsnNode, 0, 1));
				}
			}
			else if (((opcode == Opcodes.ARETURN) ||
					  (opcode == Opcodes.IRETURN)) &&
					 returnValueTracked) {

				_track(
					methodKey,
					_getArgumentOrigin(
						frames, insnList, abstractInsnNode, 0, 1));
			}
			else if (abstractInsnNode instanceof MethodInsnNode) {
				MethodInsnNode methodInsnNode =
					(MethodInsnNode)abstractInsnNode;

				if (!Objects.equals(
						_getClassName(methodInsnNode.owner), className)) {

					continue;
				}

				Type[] argumentTypes = Type.getArgumentTypes(
					methodInsnNode.desc);

				for (int i = 0; i < argumentTypes.length; i++) {
					if (!_isTracked(argumentTypes[i])) {
						continue;
					}

					_track(
						StringBundler.concat(
							_getMethodKey(
								methodInsnNode.owner, methodInsnNode.name,
								methodInsnNode.desc),
							StringPool.POUND, i),
						_getArgumentOrigin(
							frames, insnList, methodInsnNode, i,
							argumentTypes.length));
				}
			}
		}
	}

	private CryptoCall _toCryptoCall(ScannedCryptoCall scannedCryptoCall) {
		CryptoCall cryptoCall = scannedCryptoCall.getCryptoCall();

		Origin algorithmOrigin = _resolve(
			scannedCryptoCall.getAlgorithmOrigin(), new HashSet<>());

		Set<String> unresolvedReasons = algorithmOrigin.getUnresolvedReasons();

		if (!unresolvedReasons.isEmpty()) {
			cryptoCall.setUnresolvedReason(
				StringUtil.merge(unresolvedReasons, ", "));

			return cryptoCall;
		}

		Origin keySizeOrigin = _resolve(
			scannedCryptoCall.getKeySizeOrigin(), new HashSet<>());

		Set<String> propertyKeys = algorithmOrigin.getPropertyKeys();
		SortedSet<String> values = algorithmOrigin.getValues();

		if (values.isEmpty() && !propertyKeys.isEmpty()) {
			List<String> configuredBy = new ArrayList<>(propertyKeys);

			configuredBy.addAll(keySizeOrigin.getPropertyKeys());

			cryptoCall.setConfiguredBy(configuredBy);

			return cryptoCall;
		}

		if ((values.size() == 1) && propertyKeys.isEmpty()) {
			cryptoCall.setAlgorithm(
				CryptoProviderUtil.getAlgorithmName(
					values.first(), cryptoCall.getJCAClassName()));

			SortedSet<String> keySizes = keySizeOrigin.getValues();

			if (keySizeOrigin.isConstant() && (keySizes.size() == 1)) {
				cryptoCall.setKeySize(GetterUtil.getInteger(keySizes.first()));
			}

			return cryptoCall;
		}

		Set<String> candidates = new TreeSet<>(values);

		candidates.addAll(propertyKeys);

		if (candidates.isEmpty()) {
			cryptoCall.setUnresolvedReason("a value the code never sets");
		}
		else {
			cryptoCall.setUnresolvedReason(
				"one of several values: " + StringUtil.merge(candidates, ", "));
		}

		return cryptoCall;
	}

	private void _track(String reference, Origin origin) {
		Origin referenceOrigin = _referenceOrigins.get(reference);

		if (referenceOrigin == null) {
			_referenceOrigins.put(reference, origin);
		}
		else {
			referenceOrigin.addAll(origin);
		}
	}

	private static final String _CLASS_NAME_DIGESTER_UTIL =
		"com/liferay/portal/kernel/util/DigesterUtil";

	private static final String _CLASS_NAME_SECRET_KEY_SPEC =
		"javax/crypto/spec/SecretKeySpec";

	private static final Map<String, CryptoFunction> _cipherCryptoFunctions =
		HashMapBuilder.put(
			String.valueOf(Cipher.DECRYPT_MODE), CryptoFunction.DECRYPT
		).put(
			String.valueOf(Cipher.ENCRYPT_MODE), CryptoFunction.ENCRYPT
		).put(
			String.valueOf(Cipher.UNWRAP_MODE), CryptoFunction.DECRYPT
		).put(
			String.valueOf(Cipher.WRAP_MODE), CryptoFunction.ENCRYPT
		).build();
	private static final Map<String, CryptoFunction> _cryptoFunctions =
		HashMapBuilder.put(
			"KeyAgreement", CryptoFunction.KEYDERIVE
		).put(
			"KeyFactory", CryptoFunction.OTHER
		).put(
			"KeyGenerator", CryptoFunction.KEYGEN
		).put(
			"KeyManagerFactory", CryptoFunction.OTHER
		).put(
			"KeyPairGenerator", CryptoFunction.KEYGEN
		).put(
			"KeyStore", CryptoFunction.OTHER
		).put(
			"Mac", CryptoFunction.TAG
		).put(
			"MessageDigest", CryptoFunction.DIGEST
		).put(
			"SecretKeyFactory", CryptoFunction.KEYDERIVE
		).put(
			"SecretKeySpec", CryptoFunction.OTHER
		).put(
			"SSLContext", CryptoFunction.OTHER
		).put(
			"TrustManagerFactory", CryptoFunction.OTHER
		).build();
	private static final Map<String, String> _jcaClassNames =
		HashMapBuilder.put(
			"java/security/KeyFactory", "KeyFactory"
		).put(
			"java/security/KeyPairGenerator", "KeyPairGenerator"
		).put(
			"java/security/KeyStore", "KeyStore"
		).put(
			"java/security/MessageDigest", "MessageDigest"
		).put(
			"java/security/Signature", "Signature"
		).put(
			"javax/crypto/Cipher", "Cipher"
		).put(
			"javax/crypto/KeyAgreement", "KeyAgreement"
		).put(
			"javax/crypto/KeyGenerator", "KeyGenerator"
		).put(
			"javax/crypto/Mac", "Mac"
		).put(
			"javax/crypto/SecretKeyFactory", "SecretKeyFactory"
		).put(
			"javax/net/ssl/KeyManagerFactory", "KeyManagerFactory"
		).put(
			"javax/net/ssl/SSLContext", "SSLContext"
		).put(
			"javax/net/ssl/TrustManagerFactory", "TrustManagerFactory"
		).build();
	private static final Map<String, String> _jdkPropertyKeys =
		HashMapBuilder.put(
			"java/security/KeyStore.getDefaultType", "keystore.type"
		).put(
			"javax/net/ssl/KeyManagerFactory.getDefaultAlgorithm",
			"ssl.KeyManagerFactory.algorithm"
		).put(
			"javax/net/ssl/TrustManagerFactory.getDefaultAlgorithm",
			"ssl.TrustManagerFactory.algorithm"
		).build();
	private static final Map<String, RelatedCryptoMaterialType>
		_keyFactoryRelatedCryptoMaterialTypes = HashMapBuilder.put(
			"generatePrivate", RelatedCryptoMaterialType.PRIVATE_KEY
		).put(
			"generatePublic", RelatedCryptoMaterialType.PUBLIC_KEY
		).build();
	private static final Map<String, String> _keySizeMethodNames =
		HashMapBuilder.put(
			"java/security/KeyPairGenerator", "initialize"
		).put(
			"javax/crypto/KeyGenerator", "init"
		).build();
	private static final List<String> _needles = Arrays.asList(
		_CLASS_NAME_DIGESTER_UTIL, "/PropsUtil", "java/security/",
		"javax/crypto/", "javax/net/ssl/");
	private static final Map<String, Function<String, String>>
		_stringFunctions = HashMapBuilder.<String, Function<String, String>>put(
			"com/liferay/petra/string/StringUtil.toLowerCase",
			StringUtil::toLowerCase
		).put(
			"com/liferay/petra/string/StringUtil.toUpperCase",
			StringUtil::toUpperCase
		).put(
			"com/liferay/portal/kernel/util/GetterUtil.getInteger",
			Function.identity()
		).put(
			"com/liferay/portal/kernel/util/GetterUtil.getString",
			Function.identity()
		).put(
			"com/liferay/portal/kernel/util/StringUtil.toLowerCase",
			StringUtil::toLowerCase
		).put(
			"com/liferay/portal/kernel/util/StringUtil.toUpperCase",
			StringUtil::toUpperCase
		).put(
			"com/liferay/portal/kernel/util/StringUtil.trim", StringUtil::trim
		).put(
			"java/lang/String.toLowerCase", StringUtil::toLowerCase
		).put(
			"java/lang/String.toUpperCase", StringUtil::toUpperCase
		).put(
			"java/lang/String.trim", StringUtil::trim
		).build();

	private final boolean _fipsEnabled;
	private final Set<String> _privateMethodKeys = new HashSet<>();
	private final Map<String, Origin> _referenceOrigins = new HashMap<>();
	private final List<ScannedCryptoCall> _scannedCryptoCalls =
		new ArrayList<>();

	private static class Origin {

		public void addAll(Origin origin) {
			_propertyKeys.addAll(origin._propertyKeys);
			_references.addAll(origin._references);
			_unresolvedReasons.addAll(origin._unresolvedReasons);
			_values.addAll(origin._values);
		}

		public void addPropertyKey(String propertyKey) {
			_propertyKeys.add(propertyKey);
		}

		public void addReference(String reference) {
			_references.add(reference);
		}

		public void addUnresolvedReason(String unresolvedReason) {
			_unresolvedReasons.add(unresolvedReason);
		}

		public void addValue(String value) {
			_values.add(value);
		}

		public void clearReferences() {
			_references.clear();
		}

		public Set<String> getPropertyKeys() {
			return _propertyKeys;
		}

		public Set<String> getReferences() {
			return _references;
		}

		public Set<String> getUnresolvedReasons() {
			return _unresolvedReasons;
		}

		public SortedSet<String> getValues() {
			return _values;
		}

		public boolean isConstant() {
			if (_propertyKeys.isEmpty() && _references.isEmpty() &&
				_unresolvedReasons.isEmpty()) {

				return true;
			}

			return false;
		}

		public void transformValues(Function<String, String> function) {
			Set<String> values = new TreeSet<>();

			for (String value : _values) {
				values.add(function.apply(value));
			}

			_values.clear();

			_values.addAll(values);
		}

		private final Set<String> _propertyKeys = new TreeSet<>();
		private final Set<String> _references = new TreeSet<>();
		private final Set<String> _unresolvedReasons = new TreeSet<>();
		private final SortedSet<String> _values = new TreeSet<>();

	}

	private static class OriginInterpreter extends SourceInterpreter {

		public OriginInterpreter(String methodKey, MethodNode methodNode) {
			super(Opcodes.ASM9);

			_methodKey = methodKey;

			int local = 0;

			if ((methodNode.access & Opcodes.ACC_STATIC) == 0) {
				local++;
			}

			Type[] argumentTypes = Type.getArgumentTypes(methodNode.desc);

			for (int i = 0; i < argumentTypes.length; i++) {
				_parameterIndexes.put(local, i);

				local += argumentTypes[i].getSize();
			}
		}

		@Override
		public SourceValue copyOperation(
			AbstractInsnNode abstractInsnNode, SourceValue sourceValue) {

			return sourceValue;
		}

		@Override
		public SourceValue newParameterValue(
			boolean instanceMethod, int local, Type type) {

			Integer parameterIndex = _parameterIndexes.get(local);

			if (parameterIndex == null) {
				return super.newParameterValue(instanceMethod, local, type);
			}

			return new SourceValue(
				type.getSize(),
				new ParameterInsnNode(
					_methodKey + StringPool.POUND + parameterIndex));
		}

		private final String _methodKey;
		private final Map<Integer, Integer> _parameterIndexes = new HashMap<>();

	}

	private static class ParameterInsnNode extends AbstractInsnNode {

		public ParameterInsnNode(String parameterKey) {
			super(-1);

			_parameterKey = parameterKey;
		}

		@Override
		public void accept(MethodVisitor methodVisitor) {
		}

		@Override
		public AbstractInsnNode clone(Map<LabelNode, LabelNode> labelNodes) {
			return this;
		}

		public String getParameterKey() {
			return _parameterKey;
		}

		@Override
		public int getType() {
			return VAR_INSN;
		}

		private final String _parameterKey;

	}

	private static class ScannedCryptoCall {

		public ScannedCryptoCall(
			Origin algorithmOrigin, CryptoCall cryptoCall,
			Origin keySizeOrigin) {

			_algorithmOrigin = algorithmOrigin;
			_cryptoCall = cryptoCall;
			_keySizeOrigin = keySizeOrigin;
		}

		public Origin getAlgorithmOrigin() {
			return _algorithmOrigin;
		}

		public CryptoCall getCryptoCall() {
			return _cryptoCall;
		}

		public Origin getKeySizeOrigin() {
			return _keySizeOrigin;
		}

		private final Origin _algorithmOrigin;
		private final CryptoCall _cryptoCall;
		private final Origin _keySizeOrigin;

	}

}