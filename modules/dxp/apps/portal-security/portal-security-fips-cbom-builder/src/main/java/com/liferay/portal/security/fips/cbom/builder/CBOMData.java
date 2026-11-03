/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.fips.cbom.builder;

import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Supplier;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author Manuele Castro
 * @author Caio Farias
 */
public class CBOMData {

	public String addCallReference(
		List<CryptoUse> argumentCryptoUses, String methodKey) {

		String reference = _CALL_REFERENCE_PREFIX + _callReferenceCount++;

		_callSites.put(reference, new CallSite(argumentCryptoUses, methodKey));

		return reference;
	}

	public void addClass(
		String className, List<String> interfaceNames, String superClassName) {

		_interfaceNames.put(className, interfaceNames);

		if (superClassName != null) {
			_superClassNames.put(className, superClassName);
		}
	}

	public String addComputedReference(CryptoUse cryptoUse) {
		String reference =
			_COMPUTED_REFERENCE_PREFIX + _computedReferenceCount++;

		_referenceCryptoUses.put(reference, cryptoUse);

		return reference;
	}

	public void addCryptoUse(
		CryptoUse cryptoUse, CryptoUse keySizeCryptoUse,
		CryptoUse methodCryptoUse) {

		_cryptoUsesSuppliers.add(
			() -> _toCryptoUses(cryptoUse, keySizeCryptoUse, methodCryptoUse));
	}

	public void addMethod(int access, String methodKey) {
		_methodAccesses.put(methodKey, access);
	}

	public void addReferenceCryptoUse(CryptoUse cryptoUse, String reference) {
		CryptoUse referenceCryptoUse = _referenceCryptoUses.get(reference);

		if (referenceCryptoUse == null) {
			_referenceCryptoUses.put(reference, cryptoUse);
		}
		else {
			referenceCryptoUse.addAll(cryptoUse);
		}
	}

	public void addSpringBean(
		Map<Integer, String> arguments, String className) {

		if (arguments.isEmpty() || Validator.isNull(className)) {
			return;
		}

		List<List<String>> argumentsList =
			_springConstructorArguments.computeIfAbsent(
				className, key -> new ArrayList<>());

		argumentsList.add(new ArrayList<>(arguments.values()));
	}

	public Set<String> getBundleSymbolicNames() {
		Set<String> bundleSymbolicNames = new HashSet<>(
			_portalBundleSymbolicNames);

		bundleSymbolicNames.addAll(_settingBundleSymbolicNames);

		return bundleSymbolicNames;
	}

	public List<CryptoUse> getCryptoUses() {
		_scopeCryptoUses();

		return _cryptoUses;
	}

	public Set<String> getMessages() {
		_scopeCryptoUses();

		Set<String> messages = new TreeSet<>(_messages);

		for (String propertyKey : _unmatchedPropertyKeys) {
			messages.add(
				"The scan found no crypto call for \"" + propertyKey + "\"");
		}

		return messages;
	}

	public List<CryptoUse> getScannedCryptoUses() {
		List<CryptoUse> cryptoUses = new ArrayList<>();

		for (Supplier<List<CryptoUse>> cryptoUsesSupplier :
				_cryptoUsesSuppliers) {

			cryptoUses.addAll(cryptoUsesSupplier.get());
		}

		return cryptoUses;
	}

	public static class ResolutionContext {

		public ResolutionContext(
			List<CryptoUse> argumentCryptoUses, int depth, int id,
			String methodKey, ResolutionContext parentResolutionContext) {

			_argumentCryptoUses = argumentCryptoUses;
			_depth = depth;
			_id = id;
			_methodKey = methodKey;
			_parentResolutionContext = parentResolutionContext;
		}

		public List<CryptoUse> getArgumentCryptoUses() {
			return _argumentCryptoUses;
		}

		public int getDepth() {
			return _depth;
		}

		public int getId() {
			return _id;
		}

		public String getMethodKey() {
			return _methodKey;
		}

		public ResolutionContext getParentResolutionContext() {
			return _parentResolutionContext;
		}

		private final List<CryptoUse> _argumentCryptoUses;
		private final int _depth;
		private final int _id;
		private final String _methodKey;
		private final ResolutionContext _parentResolutionContext;

	}

	private void _addImplementationMethodKey(
		String methodKey, Set<String> methodKeys) {

		Integer access = _methodAccesses.get(methodKey);

		if ((access != null) && ((access & Opcodes.ACC_ABSTRACT) == 0)) {
			methodKeys.add(methodKey);
		}
	}

	private Set<String> _getCallerMethodKeys(String methodKey) {
		int index = methodKey.lastIndexOf(
			CharPool.PERIOD, methodKey.indexOf(CharPool.OPEN_PARENTHESIS));

		String signature = methodKey.substring(index);

		Integer access = _methodAccesses.get(methodKey);

		if ((access == null) || signature.startsWith(".<init>(") ||
			((access & (Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC)) != 0)) {

			return Collections.singleton(methodKey);
		}

		String className = methodKey.substring(0, index);

		Set<String> methodKeys = new TreeSet<>();

		for (String relatedClassName : _getSubtypeNames(className)) {
			methodKeys.add(relatedClassName + signature);
		}

		for (String relatedClassName : _getSupertypeNames(className)) {
			methodKeys.add(relatedClassName + signature);
		}

		return methodKeys;
	}

	private Set<String> _getImplementationMethodKeys(String methodKey) {
		int index = methodKey.lastIndexOf(
			CharPool.PERIOD, methodKey.indexOf(CharPool.OPEN_PARENTHESIS));

		String signature = methodKey.substring(index);

		Integer access = _methodAccesses.get(methodKey);

		if ((access != null) &&
			(signature.startsWith(".<init>(") ||
			 ((access & (Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC)) != 0))) {

			return Collections.singleton(methodKey);
		}

		String className = methodKey.substring(0, index);

		Set<String> methodKeys = new TreeSet<>();

		for (String subtypeName : _getSubtypeNames(className)) {
			_addImplementationMethodKey(subtypeName + signature, methodKeys);
		}

		String superClassName = _superClassNames.get(className);

		while (superClassName != null) {
			String superMethodKey = superClassName + signature;

			if (_methodAccesses.containsKey(superMethodKey)) {
				_addImplementationMethodKey(superMethodKey, methodKeys);

				break;
			}

			superClassName = _superClassNames.get(superClassName);
		}

		return methodKeys;
	}

	private CryptoUse _getParameterCryptoUse(String methodKey, String index) {
		CryptoUse cryptoUse = null;

		for (String callerMethodKey : _getCallerMethodKeys(methodKey)) {
			CryptoUse referenceCryptoUse = _referenceCryptoUses.get(
				callerMethodKey + StringPool.POUND + index);

			if (referenceCryptoUse == null) {
				continue;
			}

			if (cryptoUse == null) {
				cryptoUse = new CryptoUse();
			}

			cryptoUse.addAll(referenceCryptoUse);
		}

		CryptoUse springCryptoUse = _getSpringCryptoUse(
			methodKey + StringPool.POUND + index);

		if (springCryptoUse == null) {
			return cryptoUse;
		}

		if (cryptoUse != null) {
			springCryptoUse.addAll(cryptoUse);
		}

		return springCryptoUse;
	}

	private CryptoUse _getReferenceCryptoUse(String reference) {
		int index = reference.indexOf(CharPool.POUND);

		if (index != -1) {
			return _getParameterCryptoUse(
				reference.substring(0, index), reference.substring(index + 1));
		}

		return _referenceCryptoUses.get(reference);
	}

	private CryptoUse _getSpringCryptoUse(String reference) {
		int index = reference.indexOf(".<init>(");

		if (index == -1) {
			return null;
		}

		List<List<String>> argumentsList = _springConstructorArguments.get(
			reference.substring(0, index));

		if (argumentsList == null) {
			return null;
		}

		int poundIndex = reference.indexOf(CharPool.POUND);

		int argumentCount = Type.getArgumentCount(
			reference.substring(index + 7, poundIndex));
		int parameterIndex = GetterUtil.getInteger(
			reference.substring(poundIndex + 1));

		CryptoUse cryptoUse = null;

		for (List<String> arguments : argumentsList) {
			if (arguments.size() != argumentCount) {
				continue;
			}

			if (cryptoUse == null) {
				cryptoUse = new CryptoUse();
			}

			String value = arguments.get(parameterIndex);

			if (value == null) {
				cryptoUse.addUnresolvedReason(
					CBOMUtil.getUnresolvedReason(reference));
			}
			else {
				cryptoUse.addValue(value);
			}
		}

		return cryptoUse;
	}

	private Set<String> _getSubtypeNames(String className) {
		Set<String> subtypeNames = new TreeSet<>();

		if (_subtypeNames == null) {
			_subtypeNames = new HashMap<>();

			for (Map.Entry<String, String> entry :
					_superClassNames.entrySet()) {

				Set<String> superClassSubtypeNames =
					_subtypeNames.computeIfAbsent(
						entry.getValue(), key -> new TreeSet<>());

				superClassSubtypeNames.add(entry.getKey());
			}

			for (Map.Entry<String, List<String>> entry :
					_interfaceNames.entrySet()) {

				for (String interfaceName : entry.getValue()) {
					Set<String> interfaceSubtypeNames =
						_subtypeNames.computeIfAbsent(
							interfaceName, key -> new TreeSet<>());

					interfaceSubtypeNames.add(entry.getKey());
				}
			}
		}

		Deque<String> classNames = new ArrayDeque<>();

		classNames.push(className);

		while (!classNames.isEmpty()) {
			String curClassName = classNames.pop();

			if (!subtypeNames.add(curClassName)) {
				continue;
			}

			classNames.addAll(
				_subtypeNames.getOrDefault(
					curClassName, Collections.emptySet()));
		}

		return subtypeNames;
	}

	private Set<String> _getSupertypeNames(String className) {
		Set<String> supertypeNames = new TreeSet<>();

		Deque<String> classNames = new ArrayDeque<>();

		classNames.push(className);

		while (!classNames.isEmpty()) {
			String curClassName = classNames.pop();

			if (!supertypeNames.add(curClassName)) {
				continue;
			}

			String superClassName = _superClassNames.get(curClassName);

			if (superClassName != null) {
				classNames.push(superClassName);
			}

			classNames.addAll(
				_interfaceNames.getOrDefault(
					curClassName, Collections.emptyList()));
		}

		return supertypeNames;
	}

	private CryptoUse _resolve(
		CryptoUse cryptoUse, ResolutionContext resolutionContext,
		Set<String> visitedReferences) {

		CryptoUse resolvedCryptoUse = new CryptoUse();

		resolvedCryptoUse.addAll(cryptoUse);

		resolvedCryptoUse.clearReferences();

		for (String reference : cryptoUse.getReferences()) {
			if (!visitedReferences.add(
					CBOMUtil.getVisitedKey(reference, resolutionContext))) {

				continue;
			}

			if (reference.startsWith(_CALL_REFERENCE_PREFIX)) {
				resolvedCryptoUse.addAll(
					_resolveCall(
						_callSites.get(reference), resolutionContext,
						visitedReferences));

				continue;
			}

			int index = reference.indexOf(CharPool.POUND);

			if (index != -1) {
				ResolutionContext boundResolutionContext =
					CBOMUtil.fetchBoundResolutionContext(
						reference.substring(0, index), resolutionContext);

				if (boundResolutionContext != null) {
					List<CryptoUse> argumentCryptoUses =
						boundResolutionContext.getArgumentCryptoUses();

					CryptoUse argumentCryptoUse = argumentCryptoUses.get(
						GetterUtil.getInteger(reference.substring(index + 1)));

					if (argumentCryptoUse == null) {
						resolvedCryptoUse.addUnresolvedReason(
							CBOMUtil.getUnresolvedReason(reference));
					}
					else {
						resolvedCryptoUse.addAll(
							_resolve(
								argumentCryptoUse,
								boundResolutionContext.
									getParentResolutionContext(),
								visitedReferences));
					}

					continue;
				}
			}

			CryptoUse referenceCryptoUse = _getReferenceCryptoUse(reference);

			if (referenceCryptoUse == null) {
				resolvedCryptoUse.addUnresolvedReason(
					CBOMUtil.getUnresolvedReason(reference));

				continue;
			}

			ResolutionContext referenceResolutionContext = null;

			if (reference.startsWith(_COMPUTED_REFERENCE_PREFIX)) {
				referenceResolutionContext = resolutionContext;
			}

			CryptoUse resolvedReferenceCryptoUse = _resolve(
				referenceCryptoUse, referenceResolutionContext,
				visitedReferences);

			if (reference.startsWith(_COMPUTED_REFERENCE_PREFIX)) {
				resolvedReferenceCryptoUse = CBOMUtil.toComputedCryptoUse(
					resolvedReferenceCryptoUse);
			}

			resolvedCryptoUse.addAll(resolvedReferenceCryptoUse);
		}

		return resolvedCryptoUse;
	}

	private CryptoUse _resolveCall(
		CallSite callSite, ResolutionContext resolutionContext,
		Set<String> visitedReferences) {

		CryptoUse cryptoUse = new CryptoUse();

		int depth = 1;

		if (resolutionContext != null) {
			depth = resolutionContext.getDepth() + 1;
		}

		if (depth > _MAX_DEPTH) {
			cryptoUse.addUnresolvedReason(
				"a value that passes through more than " + _MAX_DEPTH +
					" nested calls");

			return cryptoUse;
		}

		String methodKey = callSite.getMethodKey();

		boolean found = false;

		for (String implementationMethodKey :
				_getImplementationMethodKeys(methodKey)) {

			ResolutionContext boundResolutionContext =
				CBOMUtil.fetchBoundResolutionContext(
					implementationMethodKey, resolutionContext);

			if (boundResolutionContext != null) {
				found = true;

				continue;
			}

			CryptoUse returnCryptoUse = _referenceCryptoUses.get(
				implementationMethodKey);

			if (returnCryptoUse == null) {
				continue;
			}

			found = true;

			cryptoUse.addAll(
				_resolve(
					returnCryptoUse,
					new ResolutionContext(
						callSite.getArgumentCryptoUses(), depth,
						_resolutionContextCount++, implementationMethodKey,
						resolutionContext),
					visitedReferences));
		}

		if (!found) {
			cryptoUse.addUnresolvedReason(
				CBOMUtil.getUnresolvedReason(methodKey));
		}

		return cryptoUse;
	}

	private void _scopeCryptoUses() {
		if (_cryptoUses != null) {
			return;
		}

		_cryptoUses = new ArrayList<>();

		for (CryptoUse cryptoUse : getScannedCryptoUses()) {
			if (!cryptoUse.isResolved()) {
				_cryptoUses.add(cryptoUse);

				_messages.add(
					StringBundler.concat(
						"The scan cannot read the algorithm of the ",
						cryptoUse.getJCAClassName(), " call in ",
						cryptoUse.getClassName(), ", which comes from ",
						StringUtil.merge(
							cryptoUse.getUnresolvedReasons(), ", ")));

				continue;
			}

			if (cryptoUse.isConfigurable()) {
				List<String> configuredBy = cryptoUse.getConfiguredBy();

				String propertyKey = configuredBy.get(0);

				if (!_portalBundleSymbolicNames.contains(
						cryptoUse.getBundleSymbolicName()) &&
					!_settingPropertyKeys.contains(propertyKey)) {

					continue;
				}

				cryptoUse.setFeatureName(propertyKey);

				_cryptoUses.add(cryptoUse);

				_unmatchedPropertyKeys.remove(propertyKey);

				continue;
			}

			String standardAlgorithmName = CBOMUtil.getStandardAlgorithmName(
				cryptoUse.getAlgorithm(), cryptoUse.getJCAClassName());

			cryptoUse.setAlgorithm(standardAlgorithmName);

			if (_portalBundleSymbolicNames.contains(
					cryptoUse.getBundleSymbolicName())) {

				cryptoUse.setFeatureName(
					StringBundler.concat(
						cryptoUse.getAlgorithm(), " in ",
						cryptoUse.getClassName()));

				_cryptoUses.add(cryptoUse);

				continue;
			}

			for (String propertyKey : cryptoUse.getSelectedBy()) {
				if (!_settingPropertyKeys.contains(propertyKey)) {
					continue;
				}

				CryptoUse featureCryptoUse = cryptoUse.createPart();

				featureCryptoUse.setAlgorithm(cryptoUse.getAlgorithm());
				featureCryptoUse.setFeatureName(propertyKey);
				featureCryptoUse.setKeySize(cryptoUse.getKeySize());

				_cryptoUses.add(featureCryptoUse);

				_unmatchedPropertyKeys.remove(propertyKey);
			}
		}
	}

	private List<CryptoUse> _toCryptoUses(
		CryptoUse cryptoUse, CryptoUse keySizeCryptoUse,
		CryptoUse methodCryptoUse) {

		CryptoUse resolvedAlgorithmCryptoUse = _resolve(
			cryptoUse, null, new HashSet<>());
		CryptoUse resolvedKeySizeCryptoUse = _resolve(
			keySizeCryptoUse, null, new HashSet<>());

		List<CryptoUse> cryptoUses = new ArrayList<>();

		SortedSet<String> keySizes = resolvedKeySizeCryptoUse.getValues();
		SortedSet<String> values = resolvedAlgorithmCryptoUse.getValues();

		Set<String> selectedBy = new TreeSet<>();

		if (!values.isEmpty()) {
			selectedBy.addAll(resolvedAlgorithmCryptoUse.getPropertyKeys());

			CryptoUse resolvedMethodCryptoUse = _resolve(
				methodCryptoUse, null, new HashSet<>());

			selectedBy.addAll(resolvedMethodCryptoUse.getPropertyKeys());
		}

		for (String value : values) {
			CryptoUse valueCryptoUse = cryptoUse.createPart();

			valueCryptoUse.setAlgorithm(value);
			valueCryptoUse.setSelectedBy(new ArrayList<>(selectedBy));

			if ((values.size() == 1) && resolvedKeySizeCryptoUse.isConstant() &&
				(keySizes.size() == 1)) {

				valueCryptoUse.setKeySize(
					GetterUtil.getInteger(keySizes.first()));
			}

			cryptoUses.add(valueCryptoUse);
		}

		for (String propertyKey :
				resolvedAlgorithmCryptoUse.getPropertyKeys()) {

			CryptoUse configuredCryptoUse = cryptoUse.createPart();

			List<String> configuredBy = new ArrayList<>();

			configuredBy.add(propertyKey);

			for (String keySizePropertyKey :
					resolvedKeySizeCryptoUse.getPropertyKeys()) {

				if (configuredBy.contains(keySizePropertyKey)) {
					continue;
				}

				configuredBy.add(keySizePropertyKey);
			}

			configuredCryptoUse.setConfiguredBy(configuredBy);

			cryptoUses.add(configuredCryptoUse);
		}

		Set<String> unresolvedReasons =
			resolvedAlgorithmCryptoUse.getUnresolvedReasons();

		if (!unresolvedReasons.isEmpty() || cryptoUses.isEmpty()) {
			CryptoUse unresolvedCryptoUse = cryptoUse.createPart();

			if (unresolvedReasons.isEmpty()) {
				unresolvedCryptoUse.setUnresolvedReasons(
					Collections.singletonList("a value the code never sets"));
			}
			else {
				unresolvedCryptoUse.setUnresolvedReasons(
					new ArrayList<>(unresolvedReasons));
			}

			cryptoUses.add(unresolvedCryptoUse);
		}

		return cryptoUses;
	}

	private static final String _CALL_REFERENCE_PREFIX = "call:";

	private static final String _COMPUTED_REFERENCE_PREFIX = "computed:";

	private static final int _MAX_DEPTH = 32;

	private static final Set<String> _portalBundleSymbolicNames =
		SetUtil.fromArray(
			"com.liferay.portal.impl", "com.liferay.portal.kernel");
	private static final Set<String> _settingBundleSymbolicNames =
		SetUtil.fromArray(
			"com.liferay.object.service", "com.liferay.portal.encryptor",
			"com.liferay.portal.security.key.impl",
			"com.liferay.portal.security.password.encryptor.impl");
	private static final Set<String> _settingPropertyKeys = SetUtil.fromArray(
		"auth.mac.algorithm", "company.encryption.algorithm",
		"object.encryption.algorithm", "passwords.encryption.algorithm",
		"passwords.encryption.algorithm.legacy",
		"tunneling.servlet.encryption.algorithm");

	private int _callReferenceCount;
	private final Map<String, CallSite> _callSites = new HashMap<>();
	private int _computedReferenceCount;
	private List<CryptoUse> _cryptoUses;
	private final List<Supplier<List<CryptoUse>>> _cryptoUsesSuppliers =
		new ArrayList<>();
	private final Map<String, List<String>> _interfaceNames = new HashMap<>();
	private final Set<String> _messages = new TreeSet<>();
	private final Map<String, Integer> _methodAccesses = new HashMap<>();
	private final Map<String, CryptoUse> _referenceCryptoUses = new HashMap<>();
	private int _resolutionContextCount = 1;
	private final Map<String, List<List<String>>> _springConstructorArguments =
		new HashMap<>();
	private Map<String, Set<String>> _subtypeNames;
	private final Map<String, String> _superClassNames = new HashMap<>();
	private final Set<String> _unmatchedPropertyKeys = new TreeSet<>(
		_settingPropertyKeys);

	private static class CallSite {

		public CallSite(List<CryptoUse> argumentCryptoUses, String methodKey) {
			_argumentCryptoUses = argumentCryptoUses;
			_methodKey = methodKey;
		}

		public List<CryptoUse> getArgumentCryptoUses() {
			return _argumentCryptoUses;
		}

		public String getMethodKey() {
			return _methodKey;
		}

		private final List<CryptoUse> _argumentCryptoUses;
		private final String _methodKey;

	}

}