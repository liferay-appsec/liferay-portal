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
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.cyclonedx.model.component.crypto.enums.CryptoFunction;
import org.cyclonedx.model.component.crypto.enums.RelatedCryptoMaterialType;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceInterpreter;
import org.objectweb.asm.tree.analysis.SourceValue;

/**
 * @author Manuele Castro
 */
public class CBOMScanner {

	public CBOMScanner(
		CBOMData cbomData, String defaultSecureRandomAlgorithm,
		boolean fipsEnabled) {

		this(null, cbomData, defaultSecureRandomAlgorithm, fipsEnabled);
	}

	public CBOMScanner(
		Set<String> bundleSymbolicNames, CBOMData cbomData,
		String defaultSecureRandomAlgorithm, boolean fipsEnabled) {

		_bundleSymbolicNames = bundleSymbolicNames;
		_cbomData = cbomData;
		_defaultSecureRandomAlgorithm = defaultSecureRandomAlgorithm;
		_fipsEnabled = fipsEnabled;
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

	private CryptoUse _getAlgorithmCryptoUse(
		Frame<SourceValue>[] frames, InsnList insnList,
		MethodInsnNode methodInsnNode) {

		Type[] argumentTypes = Type.getArgumentTypes(methodInsnNode.desc);

		if (Objects.equals(methodInsnNode.owner, _CLASS_NAME_DIGESTER_UTIL) &&
			((argumentTypes.length < 2) ||
			 !Objects.equals(argumentTypes[0], Type.getType(String.class)))) {

			CryptoUse cryptoUse = new CryptoUse();

			cryptoUse.addReference(
				_cbomData.addCallReference(
					Collections.emptyList(),
					_getMethodKey(
						_CLASS_NAME_DIGESTER_UTIL, "_getDefaultAlgorithm",
						"()Ljava/lang/String;")));

			return cryptoUse;
		}

		String fixedAlgorithm = _getFixedAlgorithm(methodInsnNode);

		if (fixedAlgorithm != null) {
			CryptoUse cryptoUse = new CryptoUse();

			cryptoUse.addValue(fixedAlgorithm);

			return cryptoUse;
		}

		int index = 0;

		if (Objects.equals(methodInsnNode.owner, _CLASS_NAME_SECRET_KEY_SPEC)) {
			index = argumentTypes.length - 1;
		}

		return _getArgumentCryptoUse(
			frames, insnList, methodInsnNode, index, argumentTypes.length);
	}

	private SourceValue _getArgument(
		Frame<SourceValue> frame, int index, int count) {

		return frame.getStack(frame.getStackSize() - count + index);
	}

	private CryptoUse _getArgumentCryptoUse(
		Frame<SourceValue>[] frames, InsnList insnList,
		AbstractInsnNode abstractInsnNode, int index, int count) {

		return _getCryptoUse(
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

	private List<MethodInsnNode> _getConstructorMethodInsnNodes(
		Frame<SourceValue>[] frames, InsnList insnList,
		TypeInsnNode typeInsnNode) {

		List<MethodInsnNode> methodInsnNodes = new ArrayList<>();

		for (AbstractInsnNode abstractInsnNode : insnList) {
			if (abstractInsnNode.getOpcode() != Opcodes.INVOKESPECIAL) {
				continue;
			}

			MethodInsnNode methodInsnNode = (MethodInsnNode)abstractInsnNode;

			Frame<SourceValue> frame = frames[insnList.indexOf(methodInsnNode)];

			if ((frame == null) ||
				!Objects.equals(methodInsnNode.owner, typeInsnNode.desc) ||
				!Objects.equals(methodInsnNode.name, "<init>")) {

				continue;
			}

			SourceValue sourceValue = _getReceiver(
				frame, Type.getArgumentCount(methodInsnNode.desc));

			if (!sourceValue.insns.contains(typeInsnNode)) {
				continue;
			}

			methodInsnNodes.add(methodInsnNode);
		}

		return methodInsnNodes;
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
		ClassNode classNode, MethodNode methodNode) {

		List<MethodInsnNode> methodInsnNodes = new ArrayList<>();

		if (Objects.equals(classNode.name, _CLASS_NAME_DIGESTER_UTIL) ||
			Objects.equals(classNode.name, _CLASS_NAME_LIFERAY_SECURE_RANDOM) ||
			Objects.equals(classNode.name, _CLASS_NAME_SECURE_RANDOM_UTIL)) {

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

	private CryptoUse _getCryptoUse(
		Frame<SourceValue>[] frames, InsnList insnList,
		AbstractInsnNode abstractInsnNode,
		Set<AbstractInsnNode> visitedAbstractInsnNodes) {

		CryptoUse cryptoUse = new CryptoUse();

		int opcode = abstractInsnNode.getOpcode();

		if (abstractInsnNode instanceof ParameterInsnNode) {
			ParameterInsnNode parameterInsnNode =
				(ParameterInsnNode)abstractInsnNode;

			cryptoUse.addReference(parameterInsnNode.getParameterKey());
		}
		else if (abstractInsnNode instanceof LdcInsnNode) {
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
		else if ((opcode == Opcodes.GETFIELD) ||
				 (opcode == Opcodes.GETSTATIC)) {

			cryptoUse.addReference(
				_getFieldKey((FieldInsnNode)abstractInsnNode));
		}
		else if (opcode == Opcodes.CHECKCAST) {
			cryptoUse.addAll(
				_getCryptoUse(
					frames, insnList,
					_getArgument(
						frames[insnList.indexOf(abstractInsnNode)], 0, 1),
					visitedAbstractInsnNodes));
		}
		else if (abstractInsnNode instanceof MethodInsnNode) {
			cryptoUse.addAll(
				_getReturnValueCryptoUse(
					frames, insnList, (MethodInsnNode)abstractInsnNode,
					visitedAbstractInsnNodes));
		}
		else if ((opcode == Opcodes.NEW) &&
				 _isSecretKeySpec((TypeInsnNode)abstractInsnNode)) {

			for (MethodInsnNode methodInsnNode :
					_getConstructorMethodInsnNodes(
						frames, insnList, (TypeInsnNode)abstractInsnNode)) {

				cryptoUse.addAll(
					_getAlgorithmCryptoUse(frames, insnList, methodInsnNode));
			}
		}
		else if ((abstractInsnNode instanceof InvokeDynamicInsnNode) &&
				 _isBootstrappedBy(
					 (InvokeDynamicInsnNode)abstractInsnNode,
					 "java/lang/invoke/StringConcatFactory")) {

			InvokeDynamicInsnNode invokeDynamicInsnNode =
				(InvokeDynamicInsnNode)abstractInsnNode;

			Frame<SourceValue> frame =
				frames[insnList.indexOf(abstractInsnNode)];
			Type[] argumentTypes = Type.getArgumentTypes(
				invokeDynamicInsnNode.desc);

			CryptoUse computedCryptoUse = new CryptoUse();

			for (int i = 0; i < argumentTypes.length; i++) {
				if (!Objects.equals(
						argumentTypes[i], Type.getType(String.class))) {

					continue;
				}

				computedCryptoUse.addAll(
					_getCryptoUse(
						frames, insnList,
						_getArgument(frame, i, argumentTypes.length),
						visitedAbstractInsnNodes));
			}

			cryptoUse.addReference(
				_cbomData.addComputedReference(computedCryptoUse));
		}
		else if (opcode != Opcodes.ACONST_NULL) {
			cryptoUse.addUnresolvedReason("a value the code computes");
		}

		return cryptoUse;
	}

	private CryptoUse _getCryptoUse(
		Frame<SourceValue>[] frames, InsnList insnList, SourceValue sourceValue,
		Set<AbstractInsnNode> visitedAbstractInsnNodes) {

		CryptoUse cryptoUse = new CryptoUse();

		for (AbstractInsnNode abstractInsnNode : sourceValue.insns) {
			if (!visitedAbstractInsnNodes.add(abstractInsnNode)) {
				continue;
			}

			cryptoUse.addAll(
				_getCryptoUse(
					frames, insnList, abstractInsnNode,
					visitedAbstractInsnNodes));
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

	private String _getFixedAlgorithm(MethodInsnNode methodInsnNode) {
		String key =
			methodInsnNode.owner + StringPool.PERIOD + methodInsnNode.name;

		if (key.equals("java/security/SecureRandom.<init>") ||
			key.equals(_CLASS_NAME_LIFERAY_SECURE_RANDOM + ".<init>") ||
			(Objects.equals(
				methodInsnNode.owner, _CLASS_NAME_SECURE_RANDOM_UTIL) &&
			 methodInsnNode.name.startsWith("next"))) {

			return _defaultSecureRandomAlgorithm;
		}

		return _fixedAlgorithms.get(key);
	}

	private String _getJCAClassName(MethodInsnNode methodInsnNode) {
		String fixedAlgorithmClassName = _fixedAlgorithmClassNames.get(
			methodInsnNode.owner + StringPool.PERIOD + methodInsnNode.name);

		if (fixedAlgorithmClassName != null) {
			return fixedAlgorithmClassName;
		}

		if ((methodInsnNode.getOpcode() == Opcodes.INVOKESTATIC) &&
			Objects.equals(
				methodInsnNode.owner, _CLASS_NAME_SECURE_RANDOM_UTIL) &&
			methodInsnNode.name.startsWith("next")) {

			return "SecureRandom";
		}

		if (methodInsnNode.getOpcode() == Opcodes.INVOKESTATIC) {
			if (Objects.equals(
					methodInsnNode.owner, _CLASS_NAME_DIGESTER_UTIL) &&
				methodInsnNode.name.startsWith("digest")) {

				return "MessageDigest";
			}

			if (Objects.equals(methodInsnNode.name, "getInstance") &&
				methodInsnNode.desc.startsWith("(Ljava/lang/String;")) {

				return CBOMUtil.fetchJCAClassName(methodInsnNode.owner);
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

	private CryptoUse _getMethodCryptoUse(
		ClassNode classNode, MethodNode methodNode) {

		CryptoUse cryptoUse = new CryptoUse();

		String methodKey = _getMethodKey(
			classNode.name, methodNode.name, methodNode.desc);

		Type[] argumentTypes = Type.getArgumentTypes(methodNode.desc);

		for (int i = 0; i < argumentTypes.length; i++) {
			if (!Objects.equals(argumentTypes[i], Type.getType(String.class))) {
				continue;
			}

			cryptoUse.addReference(methodKey + StringPool.POUND + i);
		}

		return cryptoUse;
	}

	private String _getMethodKey(
		String internalName, String name, String desc) {

		return StringBundler.concat(
			_getKeyName(internalName), StringPool.PERIOD, name, desc);
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

	private CryptoUse _getReturnValueCryptoUse(
		Frame<SourceValue>[] frames, InsnList insnList,
		MethodInsnNode methodInsnNode,
		Set<AbstractInsnNode> visitedAbstractInsnNodes) {

		CryptoUse cryptoUse = new CryptoUse();

		String propertyKey = CBOMUtil.fetchJDKPropertyKey(
			methodInsnNode.owner + StringPool.PERIOD + methodInsnNode.name);

		if (propertyKey != null) {
			cryptoUse.addPropertyKey(propertyKey);

			return cryptoUse;
		}

		Frame<SourceValue> frame = frames[insnList.indexOf(methodInsnNode)];
		int count = Type.getArgumentCount(methodInsnNode.desc);

		if (methodInsnNode.owner.endsWith("/PropsUtil") &&
			Objects.equals(methodInsnNode.name, "get") && (count > 0)) {

			CryptoUse keyCryptoUse = _getCryptoUse(
				frames, insnList, _getArgument(frame, 0, count),
				visitedAbstractInsnNodes);

			for (String value : keyCryptoUse.getValues()) {
				cryptoUse.addPropertyKey(value);
			}

			if (!keyCryptoUse.isConstant()) {
				cryptoUse.addUnresolvedReason(
					"a property whose name the code computes");
			}

			return cryptoUse;
		}

		if (CBOMUtil.isKeyClassName(methodInsnNode.owner) &&
			Objects.equals(methodInsnNode.name, "getAlgorithm") &&
			(count == 0)) {

			return _getCryptoUse(
				frames, insnList, _getReceiver(frame, count),
				visitedAbstractInsnNodes);
		}

		String jcaClassName = CBOMUtil.fetchJCAClassName(methodInsnNode.owner);

		if ((jcaClassName != null) &&
			CBOMUtil.isKeyMethodName(methodInsnNode.name)) {

			for (AbstractInsnNode abstractInsnNode :
					_getReceiver(frame, count).insns) {

				if ((abstractInsnNode instanceof MethodInsnNode) &&
					(_getJCAClassName((MethodInsnNode)abstractInsnNode) !=
						null)) {

					cryptoUse.addAll(
						_getAlgorithmCryptoUse(
							frames, insnList,
							(MethodInsnNode)abstractInsnNode));
				}
				else {
					cryptoUse.addUnresolvedReason(
						"the algorithm of a " + jcaClassName +
							" that the code does not create");
				}
			}

			return cryptoUse;
		}

		Function<String, String> function = _stringFunctions.get(
			methodInsnNode.owner + StringPool.PERIOD + methodInsnNode.name);

		if ((function == null) &&
			_computingClassNames.contains(methodInsnNode.owner) &&
			Objects.equals(
				Type.getReturnType(methodInsnNode.desc),
				Type.getType(String.class))) {

			CryptoUse computedCryptoUse = new CryptoUse();

			if ((methodInsnNode.getOpcode() != Opcodes.INVOKESTATIC) &&
				Objects.equals(methodInsnNode.owner, "java/lang/String")) {

				computedCryptoUse.addAll(
					_getCryptoUse(
						frames, insnList, _getReceiver(frame, count),
						visitedAbstractInsnNodes));
			}

			Type[] argumentTypes = Type.getArgumentTypes(methodInsnNode.desc);

			for (int i = 0; i < argumentTypes.length; i++) {
				if (!Objects.equals(
						argumentTypes[i], Type.getType(String.class))) {

					continue;
				}

				computedCryptoUse.addAll(
					_getCryptoUse(
						frames, insnList, _getArgument(frame, i, count),
						visitedAbstractInsnNodes));
			}

			cryptoUse.addReference(
				_cbomData.addComputedReference(computedCryptoUse));

			return cryptoUse;
		}

		if (function == null) {
			List<CryptoUse> argumentCryptoUses = new ArrayList<>();

			Type[] argumentTypes = Type.getArgumentTypes(methodInsnNode.desc);

			for (int i = 0; i < argumentTypes.length; i++) {
				if (!_isTracked(argumentTypes[i])) {
					argumentCryptoUses.add(null);

					continue;
				}

				argumentCryptoUses.add(
					_getCryptoUse(
						frames, insnList, _getArgument(frame, i, count),
						new HashSet<>(visitedAbstractInsnNodes)));
			}

			cryptoUse.addReference(
				_cbomData.addCallReference(
					argumentCryptoUses,
					_getMethodKey(
						methodInsnNode.owner, methodInsnNode.name,
						methodInsnNode.desc)));

			return cryptoUse;
		}

		SourceValue sourceValue = null;

		if (methodInsnNode.getOpcode() == Opcodes.INVOKESTATIC) {
			sourceValue = _getArgument(frame, 0, count);
		}
		else {
			sourceValue = _getReceiver(frame, count);
		}

		CryptoUse wrappedCryptoUse = _getCryptoUse(
			frames, insnList, sourceValue, visitedAbstractInsnNodes);

		wrappedCryptoUse.transformValues(function);

		return wrappedCryptoUse;
	}

	private boolean _isBootstrappedBy(
		InvokeDynamicInsnNode invokeDynamicInsnNode, String owner) {

		Handle handle = invokeDynamicInsnNode.bsm;

		return Objects.equals(handle.getOwner(), owner);
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

	private boolean _isScanned(String bundleSymbolicName) {
		if ((_bundleSymbolicNames == null) ||
			_bundleSymbolicNames.contains(bundleSymbolicName)) {

			return true;
		}

		return false;
	}

	private boolean _isSecretKeySpec(TypeInsnNode typeInsnNode) {
		return Objects.equals(typeInsnNode.desc, _CLASS_NAME_SECRET_KEY_SPEC);
	}

	private boolean _isTracked(Type type) {
		if (Objects.equals(type, Type.INT_TYPE) ||
			Objects.equals(type, Type.getType(String.class)) ||
			CBOMUtil.isKeyClassName(type.getInternalName())) {

			return true;
		}

		return false;
	}

	private boolean _isXMLFileName(String name) {
		if (name.startsWith("META-INF/") && name.endsWith(".xml")) {
			return true;
		}

		return false;
	}

	private void _scan(
		ClassNode classNode, String bundleSymbolicName, String featureName) {

		String className = _getClassName(classNode.name);

		List<String> interfaceNames = new ArrayList<>();

		for (String interfaceName : classNode.interfaces) {
			interfaceNames.add(_getKeyName(interfaceName));
		}

		String superClassName = null;

		if (classNode.superName != null) {
			superClassName = _getKeyName(classNode.superName);
		}

		_cbomData.addClass(
			_getKeyName(classNode.name), interfaceNames, superClassName);

		for (MethodNode methodNode : classNode.methods) {
			_cbomData.addMethod(
				methodNode.access,
				_getMethodKey(
					classNode.name, methodNode.name, methodNode.desc));
		}

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
					CryptoUse cryptoUse = new CryptoUse(
						bundleSymbolicName, className, featureName,
						_getJCAClassName(methodInsnNode));

					cryptoUse.addUnresolvedReason(
						"bytecode that the scan cannot analyze: " +
							analyzerException.getMessage());

					_cbomData.addCryptoUse(
						cryptoUse, new CryptoUse(), new CryptoUse());
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
					_getKeySizeCryptoUse(frames, insnList, methodInsnNode),
					_getMethodCryptoUse(classNode, methodNode));
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

			if (_isClassFileName(jarEntryName)) {
				scan(bundleSymbolicName, featureName, jarInputStream);
			}
			else if (_isXMLFileName(jarEntryName)) {
				_scanSpringBeans(jarInputStream);
			}
		}
	}

	private void _scanAssignments(
		ClassNode classNode, Frame<SourceValue>[] frames,
		MethodNode methodNode) {

		String methodKey = _getMethodKey(
			classNode.name, methodNode.name, methodNode.desc);

		boolean returnValueTracked = _isTracked(
			Type.getReturnType(methodNode.desc));

		InsnList insnList = methodNode.instructions;

		for (AbstractInsnNode abstractInsnNode : insnList) {
			if (frames[insnList.indexOf(abstractInsnNode)] == null) {
				continue;
			}

			int opcode = abstractInsnNode.getOpcode();

			if ((opcode == Opcodes.PUTFIELD) || (opcode == Opcodes.PUTSTATIC)) {
				FieldInsnNode fieldInsnNode = (FieldInsnNode)abstractInsnNode;

				if (_isTracked(Type.getType(fieldInsnNode.desc))) {
					_cbomData.addReferenceCryptoUse(
						_getArgumentCryptoUse(
							frames, insnList, abstractInsnNode, 0, 1),
						_getFieldKey(fieldInsnNode));
				}
			}
			else if (((opcode == Opcodes.ARETURN) ||
					  (opcode == Opcodes.IRETURN)) &&
					 returnValueTracked) {

				_cbomData.addReferenceCryptoUse(
					_getArgumentCryptoUse(
						frames, insnList, abstractInsnNode, 0, 1),
					methodKey);
			}
			else if ((abstractInsnNode instanceof InvokeDynamicInsnNode) &&
					 _isBootstrappedBy(
						 (InvokeDynamicInsnNode)abstractInsnNode,
						 "java/lang/invoke/LambdaMetafactory")) {

				InvokeDynamicInsnNode invokeDynamicInsnNode =
					(InvokeDynamicInsnNode)abstractInsnNode;

				Handle handle = (Handle)invokeDynamicInsnNode.bsmArgs[1];

				int offset = 1;

				if ((handle.getTag() == Opcodes.H_INVOKESTATIC) ||
					(handle.getTag() == Opcodes.H_NEWINVOKESPECIAL)) {

					offset = 0;
				}

				String lambdaMethodKey = _getMethodKey(
					handle.getOwner(), handle.getName(), handle.getDesc());

				Type[] argumentTypes = Type.getArgumentTypes(
					invokeDynamicInsnNode.desc);

				for (int i = offset; i < argumentTypes.length; i++) {
					if (!_isTracked(argumentTypes[i])) {
						continue;
					}

					_cbomData.addReferenceCryptoUse(
						_getArgumentCryptoUse(
							frames, insnList, invokeDynamicInsnNode, i,
							argumentTypes.length),
						lambdaMethodKey + StringPool.POUND + (i - offset));
				}
			}
			else if (abstractInsnNode instanceof MethodInsnNode) {
				MethodInsnNode methodInsnNode =
					(MethodInsnNode)abstractInsnNode;

				Type[] argumentTypes = Type.getArgumentTypes(
					methodInsnNode.desc);

				for (int i = 0; i < argumentTypes.length; i++) {
					if (!_isTracked(argumentTypes[i])) {
						continue;
					}

					_cbomData.addReferenceCryptoUse(
						_getArgumentCryptoUse(
							frames, insnList, methodInsnNode, i,
							argumentTypes.length),
						StringBundler.concat(
							_getMethodKey(
								methodInsnNode.owner, methodInsnNode.name,
								methodInsnNode.desc),
							StringPool.POUND, i));
				}
			}
		}
	}

	private void _scanSpringBeans(InputStream inputStream) throws IOException {
		XMLInputFactory xmlInputFactory = XMLInputFactory.newDefaultFactory();

		xmlInputFactory.setProperty(
			XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
		xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);

		try {
			XMLStreamReader xmlStreamReader =
				xmlInputFactory.createXMLStreamReader(
					new ByteArrayInputStream(inputStream.readAllBytes()));

			int eventType = xmlStreamReader.next();

			while (eventType != XMLStreamConstants.START_ELEMENT) {
				eventType = xmlStreamReader.next();
			}

			if (!Objects.equals(xmlStreamReader.getLocalName(), "beans")) {
				return;
			}

			Deque<Map<Integer, String>> argumentsDeque = new ArrayDeque<>();
			Deque<String> classNames = new ArrayDeque<>();

			while (xmlStreamReader.hasNext()) {
				eventType = xmlStreamReader.next();

				if ((eventType == XMLStreamConstants.END_ELEMENT) &&
					Objects.equals(xmlStreamReader.getLocalName(), "bean")) {

					_cbomData.addSpringBean(
						argumentsDeque.pop(), classNames.pop());

					continue;
				}

				if (eventType != XMLStreamConstants.START_ELEMENT) {
					continue;
				}

				String localName = xmlStreamReader.getLocalName();

				if (localName.equals("bean")) {
					argumentsDeque.push(new TreeMap<>());
					classNames.push(
						GetterUtil.getString(
							xmlStreamReader.getAttributeValue(null, "class")));
				}
				else if (localName.equals("constructor-arg") &&
						 !argumentsDeque.isEmpty()) {

					Map<Integer, String> arguments = argumentsDeque.peek();

					arguments.put(
						GetterUtil.getInteger(
							xmlStreamReader.getAttributeValue(null, "index"),
							arguments.size()),
						xmlStreamReader.getAttributeValue(null, "value"));
				}
			}
		}
		catch (XMLStreamException xmlStreamException) {
			throw new IOException(xmlStreamException);
		}
	}

	private static final String _CLASS_NAME_DIGESTER_UTIL =
		"com/liferay/portal/kernel/util/DigesterUtil";

	private static final String _CLASS_NAME_LIFERAY_SECURE_RANDOM =
		"com/liferay/portal/kernel/security/SecureRandom";

	private static final String _CLASS_NAME_SECRET_KEY_SPEC =
		"javax/crypto/spec/SecretKeySpec";

	private static final String _CLASS_NAME_SECURE_RANDOM_UTIL =
		"com/liferay/portal/kernel/security/SecureRandomUtil";

	private static final Set<String> _computingClassNames = SetUtil.fromArray(
		"com/liferay/petra/string/StringBundler",
		"com/liferay/petra/string/StringUtil",
		"com/liferay/portal/kernel/util/StringUtil", "java/lang/String");
	private static final Map<String, String> _fixedAlgorithmClassNames =
		HashMapBuilder.put(
			_CLASS_NAME_LIFERAY_SECURE_RANDOM + ".<init>", "SecureRandom"
		).put(
			"java/security/SecureRandom.<init>", "SecureRandom"
		).put(
			"jodd/util/BCrypt.hashpw", "BCrypt"
		).put(
			"org/vps/crypt/Crypt.crypt", "Crypt"
		).build();
	private static final Map<String, String> _fixedAlgorithms =
		HashMapBuilder.put(
			"jodd/util/BCrypt.hashpw", "BCRYPT"
		).put(
			"org/vps/crypt/Crypt.crypt", "UFC-CRYPT"
		).build();
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

	private final Set<String> _bundleSymbolicNames;
	private final CBOMData _cbomData;
	private final String _defaultSecureRandomAlgorithm;
	private final boolean _fipsEnabled;

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

}