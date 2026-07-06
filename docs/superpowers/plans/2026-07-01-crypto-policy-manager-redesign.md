# CryptoPolicyManager Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a `CryptoPolicyManager` OSGi service that answers "is this cryptographic algorithm / key size FIPS-approved?" from a curated, NIST-cited catalog, enforcing in FIPS mode and passing through otherwise.

**Architecture:** Two modules under `modules/apps/portal-security/` — a public `-api` (interface, `ServiceType` enum, exception) and an `-impl` holding the hardcoded catalog and logic. No JCE provider introspection: the approved set is static data keyed by JCA service type. `fips.enabled` gates both enforcement and filtering: when off, `checkAlgorithm` and the query filters pass their input through unchanged; when on, `checkAlgorithm` throws on a non-approved value and the query filters drop non-approved candidates.

**Tech Stack:** Java, OSGi Declarative Services (`org.osgi.service.component.annotations`), Liferay `PropsValues`, JUnit + `LiferayUnitTestRule`, Gradle (`gw`).

**Design spec:** `docs/superpowers/specs/2026-07-01-crypto-policy-manager-redesign.md`

## Global Constraints

- Modules live under `modules/apps/portal-security/` and are named `portal-security-crypto-policy-api` and `portal-security-crypto-policy-impl`.
- **Curated catalog only** — no `java.security.Security.getProviders()` introspection anywhere; the impl imports no `java.security.*` / `javax.crypto.*` engine classes.
- `PropsValues.FIPS_ENABLED` is the sole enforcement gate; `isFIPSEnabled()` is `protected` so tests override it without mutating props.
- Every Java file starts with the SPDX header exactly: `SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com` / `SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06`, and carries `@author Manuele Castro`.
- Bundle-Version is `1.0.0`.
- The impl is a single `@Component(service = CryptoPolicyManager.class)`.
- Algorithm matching is **case-insensitive**; Cipher transformation reduction (`AES/GCM/NoPadding` → `AES`) applies **only** to `ServiceType.CIPHER` (never to other types — `SHA-512/224` legitimately contains `/`).
- Run the `format-source` skill before every commit; commit messages start with `LPD-90319 `.
- Commands use `gw` (works from any module directory).

---

## File Structure

```
modules/apps/portal-security/
  portal-security-crypto-policy-api/
    .lfrbuild-portal                     # empty marker
    bnd.bnd                              # bundle metadata + Export-Package
    build.gradle                         # api dependencies
    src/main/java/com/liferay/portal/security/crypto/policy/
      CryptoPolicyManager.java           # public interface
      ServiceType.java                   # enum: 8 JCA service types
      exception/CryptoPolicyException.java
    src/main/resources/com/liferay/portal/security/crypto/policy/
      packageinfo                        # version 1.0.0
      exception/packageinfo              # version 1.0.0
  portal-security-crypto-policy-impl/
    .lfrbuild-portal
    bnd.bnd
    build.gradle
    src/main/java/com/liferay/portal/security/crypto/policy/internal/
      CryptoPolicyManagerImpl.java       # catalog + logic
    src/test/java/com/liferay/portal/security/crypto/policy/internal/
      CryptoPolicyManagerImplTest.java   # full unit suite
```

Note: these two module directories currently contain only stale `build/` artifacts (from a prior checkout) and no source. Run `gw clean` in each before building so stale classes do not interfere.

---

## Task 1: API module — `ServiceType`, `CryptoPolicyException`, `CryptoPolicyManager`

**Files:**
- Create: `modules/apps/portal-security/portal-security-crypto-policy-api/.lfrbuild-portal`
- Create: `modules/apps/portal-security/portal-security-crypto-policy-api/bnd.bnd`
- Create: `modules/apps/portal-security/portal-security-crypto-policy-api/build.gradle`
- Create: `.../crypto/policy/ServiceType.java`
- Create: `.../crypto/policy/exception/CryptoPolicyException.java`
- Create: `.../crypto/policy/CryptoPolicyManager.java`
- Create: `.../resources/.../crypto/policy/packageinfo`
- Create: `.../resources/.../crypto/policy/exception/packageinfo`

**Interfaces:**
- Produces: `enum ServiceType { CIPHER, KEY_GENERATOR, KEY_PAIR_GENERATOR, KEY_STORE, MAC, MESSAGE_DIGEST, SECRET_KEY_FACTORY, SIGNATURE }` with `String getServiceTypeName()`; `class CryptoPolicyException extends RuntimeException` with `CryptoPolicyException(String)`; `interface CryptoPolicyManager` with `String checkAlgorithm(String, ServiceType)`, `String checkAlgorithm(String, int, ServiceType)`, `List<String> getAllowedAlgorithms(ServiceType, List<String>)`, `List<Integer> getAllowedKeySizes(String, List<Integer>)`, `boolean isFIPSMode()`.

- [ ] **Step 1: Create the empty build marker**

`.lfrbuild-portal` is an empty file:

```bash
: > modules/apps/portal-security/portal-security-crypto-policy-api/.lfrbuild-portal
```

- [ ] **Step 2: Create `bnd.bnd`**

```
Bundle-Name: Liferay Portal Security Crypto Policy API
Bundle-SymbolicName: com.liferay.portal.security.crypto.policy.api
Bundle-Version: 1.0.0
Export-Package:\
	com.liferay.portal.security.crypto.policy,\
	com.liferay.portal.security.crypto.policy.exception
```

- [ ] **Step 3: Create `build.gradle`**

```
dependencies {
	compileOnly group: "com.liferay.portal", name: "com.liferay.portal.kernel", version: "default"
	compileOnly group: "org.osgi", name: "org.osgi.annotation.versioning", version: "1.1.0"
}
```

- [ ] **Step 4: Create `ServiceType.java`**

```java
/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.crypto.policy;

/**
 * Maps 1:1 to Java Security (JCA) engine-class service type strings.
 *
 * @author Manuele Castro
 */
public enum ServiceType {

	CIPHER("Cipher"), KEY_GENERATOR("KeyGenerator"),
	KEY_PAIR_GENERATOR("KeyPairGenerator"), KEY_STORE("KeyStore"), MAC("Mac"),
	MESSAGE_DIGEST("MessageDigest"), SECRET_KEY_FACTORY("SecretKeyFactory"),
	SIGNATURE("Signature");

	public String getServiceTypeName() {
		return _serviceTypeName;
	}

	private ServiceType(String serviceTypeName) {
		_serviceTypeName = serviceTypeName;
	}

	private final String _serviceTypeName;

}
```

- [ ] **Step 5: Create `exception/CryptoPolicyException.java`**

```java
/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.crypto.policy.exception;

/**
 * Thrown when a non-approved algorithm or key size is requested in FIPS mode.
 *
 * @author Manuele Castro
 */
public class CryptoPolicyException extends RuntimeException {

	public CryptoPolicyException(String message) {
		super(message);
	}

}
```

- [ ] **Step 6: Create `CryptoPolicyManager.java`**

```java
/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.crypto.policy;

import com.liferay.portal.security.crypto.policy.exception.CryptoPolicyException;

import java.util.List;

/**
 * Exposes the set of FIPS-approved cryptographic algorithms and key sizes from
 * a curated, NIST-cited catalog. Features consult it to build
 * algorithm-selection UIs, validate configuration, and - when FIPS mode is
 * enabled - refuse non-approved algorithms at runtime.
 *
 * @author Manuele Castro
 */
public interface CryptoPolicyManager {

	/**
	 * In non-FIPS mode returns the algorithm unchanged. In FIPS mode returns
	 * the algorithm when it is approved for the service type, otherwise throws
	 * {@link CryptoPolicyException}.
	 */
	public String checkAlgorithm(String algorithm, ServiceType serviceType)
		throws CryptoPolicyException;

	/**
	 * In non-FIPS mode returns the algorithm unchanged. In FIPS mode returns
	 * the algorithm when it is approved and the key size is approved (or the
	 * algorithm has no key-size constraint), otherwise throws {@link
	 * CryptoPolicyException}.
	 */
	public String checkAlgorithm(
			String algorithm, int keySize, ServiceType serviceType)
		throws CryptoPolicyException;

	/**
	 * Filters the caller's candidate algorithms to those usable in the current
	 * environment. In non-FIPS mode returns the candidates unchanged. In FIPS
	 * mode returns only the candidates approved for the service type, preserving
	 * the caller's order. The result is always a subset of the candidates -
	 * never a superset - so an approved algorithm the caller did not offer is
	 * never added.
	 */
	public List<String> getAllowedAlgorithms(
		ServiceType serviceType, List<String> candidateAlgorithms);

	/**
	 * Filters the caller's candidate key sizes to those approved for the
	 * algorithm. In non-FIPS mode returns the candidates unchanged. In FIPS mode
	 * returns only the approved candidate key sizes, preserving order; when the
	 * algorithm has no key-size constraint the candidates are returned unchanged
	 * (an empty catalog set means "unconstrained," not "nothing allowed"). Pass
	 * the base algorithm name (for example {@code AES}, {@code RSA}, {@code
	 * EC}); no service type is supplied, so Cipher transformation names are not
	 * normalized.
	 */
	public List<Integer> getAllowedKeySizes(
		String algorithm, List<Integer> candidateKeySizes);

	public boolean isFIPSMode();

}
```

- [ ] **Step 7: Create the two `packageinfo` files**

Each file contains exactly one line: `version 1.0.0`

```bash
printf 'version 1.0.0' > modules/apps/portal-security/portal-security-crypto-policy-api/src/main/resources/com/liferay/portal/security/crypto/policy/packageinfo
printf 'version 1.0.0' > modules/apps/portal-security/portal-security-crypto-policy-api/src/main/resources/com/liferay/portal/security/crypto/policy/exception/packageinfo
```

- [ ] **Step 8: Compile the API module**

Run: `cd modules/apps/portal-security/portal-security-crypto-policy-api && gw clean compileJava`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 9: Format and commit**

Run the `format-source` skill, then:

```bash
git add modules/apps/portal-security/portal-security-crypto-policy-api
git commit -m "LPD-90319 Add the crypto policy API module"
```

---

## Task 2: Impl module — `CryptoPolicyManagerImpl` and its unit suite (TDD)

**Files:**
- Create: `modules/apps/portal-security/portal-security-crypto-policy-impl/.lfrbuild-portal`
- Create: `modules/apps/portal-security/portal-security-crypto-policy-impl/bnd.bnd`
- Create: `modules/apps/portal-security/portal-security-crypto-policy-impl/build.gradle`
- Create: `.../internal/CryptoPolicyManagerImpl.java`
- Test: `.../internal/CryptoPolicyManagerImplTest.java`

**Interfaces:**
- Consumes: `CryptoPolicyManager`, `ServiceType`, `CryptoPolicyException` from Task 1.
- Produces: `@Component` `CryptoPolicyManagerImpl` with a `protected boolean isFIPSEnabled()` seam for tests.

- [ ] **Step 1: Create the impl module scaffold**

```bash
: > modules/apps/portal-security/portal-security-crypto-policy-impl/.lfrbuild-portal
```

`bnd.bnd`:

```
Bundle-Name: Liferay Portal Security Crypto Policy Implementation
Bundle-SymbolicName: com.liferay.portal.security.crypto.policy.impl
Bundle-Version: 1.0.0
```

`build.gradle`:

```
dependencies {
	compileOnly group: "com.liferay.portal", name: "com.liferay.portal.kernel", version: "default"
	compileOnly group: "org.osgi", name: "org.osgi.service.component.annotations", version: "1.4.0"
	compileOnly group: "org.osgi", name: "osgi.core", version: "6.0.0"
	compileOnly project(":apps:portal-security:portal-security-crypto-policy-api")
}
```

(JUnit and `LiferayUnitTestRule` are provided on the unit-test classpath by the Liferay Gradle plugin — no explicit test dependency is needed, matching sibling `-impl` modules. If `gw test` cannot resolve them, add `testImplementation group: "com.liferay.portal", name: "com.liferay.portal.test", version: "default"` and re-run.)

- [ ] **Step 2: Write the failing unit test**

Create `.../internal/CryptoPolicyManagerImplTest.java`:

```java
/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.crypto.policy.internal;

import com.liferay.portal.security.crypto.policy.ServiceType;
import com.liferay.portal.security.crypto.policy.exception.CryptoPolicyException;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Manuele Castro
 */
public class CryptoPolicyManagerImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testEmptyKeySizeSetAccepts() {
		CryptoPolicyManagerImpl cryptoPolicyManagerImpl = _create(true);

		Assert.assertEquals(
			"HmacSHA256",
			cryptoPolicyManagerImpl.checkAlgorithm(
				"HmacSHA256", 256, ServiceType.KEY_GENERATOR));
	}

	@Test
	public void testFIPSApprovedAlgorithmReturned() {
		CryptoPolicyManagerImpl cryptoPolicyManagerImpl = _create(true);

		Assert.assertEquals(
			"SHA-256",
			cryptoPolicyManagerImpl.checkAlgorithm(
				"SHA-256", ServiceType.MESSAGE_DIGEST));
	}

	@Test(expected = CryptoPolicyException.class)
	public void testFIPSNonApprovedAlgorithmThrows() {
		CryptoPolicyManagerImpl cryptoPolicyManagerImpl = _create(true);

		cryptoPolicyManagerImpl.checkAlgorithm(
			"MD5", ServiceType.MESSAGE_DIGEST);
	}

	@Test
	public void testGetAllowedAlgorithmsExcludesLegacyPreservingOrder() {
		CryptoPolicyManagerImpl cryptoPolicyManagerImpl = _create(true);

		Assert.assertEquals(
			Arrays.asList("SHA-256", "SHA-512"),
			cryptoPolicyManagerImpl.getAllowedAlgorithms(
				ServiceType.MESSAGE_DIGEST,
				Arrays.asList("MD5", "SHA-256", "SHA-1", "SHA-512")));
	}

	@Test
	public void testGetAllowedAlgorithmsReturnsApprovedSubsetOfCandidates() {
		CryptoPolicyManagerImpl cryptoPolicyManagerImpl = _create(true);

		List<String> allowedAlgorithms =
			cryptoPolicyManagerImpl.getAllowedAlgorithms(
				ServiceType.KEY_PAIR_GENERATOR, Arrays.asList("RSA", "DSA"));

		// "RSA" is offered and approved

		Assert.assertEquals(Arrays.asList("RSA"), allowedAlgorithms);

		// "EC" is approved but was not offered, so it is never added

		Assert.assertFalse(allowedAlgorithms.contains("EC"));

		// "DSA" is offered but not approved, so it is dropped

		Assert.assertFalse(allowedAlgorithms.contains("DSA"));
	}

	@Test
	public void testGetAllowedKeySizesFiltersToApproved() {
		CryptoPolicyManagerImpl cryptoPolicyManagerImpl = _create(true);

		Assert.assertEquals(
			Arrays.asList(2048, 3072, 4096),
			cryptoPolicyManagerImpl.getAllowedKeySizes(
				"RSA", Arrays.asList(1024, 2048, 3072, 4096)));
	}

	@Test
	public void testGetAllowedKeySizesUnconstrainedReturnsAllCandidates() {
		CryptoPolicyManagerImpl cryptoPolicyManagerImpl = _create(true);

		List<Integer> candidateKeySizes = Arrays.asList(128, 256);

		Assert.assertEquals(
			candidateKeySizes,
			cryptoPolicyManagerImpl.getAllowedKeySizes(
				"SHA-256", candidateKeySizes));
	}

	@Test
	public void testGetAllowedNonFIPSReturnsCandidatesUnchanged() {
		CryptoPolicyManagerImpl cryptoPolicyManagerImpl = _create(false);

		List<String> candidateAlgorithms = Arrays.asList("MD5", "SHA-256");

		Assert.assertSame(
			candidateAlgorithms,
			cryptoPolicyManagerImpl.getAllowedAlgorithms(
				ServiceType.MESSAGE_DIGEST, candidateAlgorithms));

		List<Integer> candidateKeySizes = Arrays.asList(56, 128);

		Assert.assertSame(
			candidateKeySizes,
			cryptoPolicyManagerImpl.getAllowedKeySizes(
				"AES", candidateKeySizes));
	}

	@Test(expected = CryptoPolicyException.class)
	public void testKeySizeConstraintRejectsBadSize() {
		CryptoPolicyManagerImpl cryptoPolicyManagerImpl = _create(true);

		cryptoPolicyManagerImpl.checkAlgorithm("AES", 200, ServiceType.CIPHER);
	}

	@Test
	public void testKeySizeConstraintAcceptsApprovedSize() {
		CryptoPolicyManagerImpl cryptoPolicyManagerImpl = _create(true);

		Assert.assertEquals(
			"AES",
			cryptoPolicyManagerImpl.checkAlgorithm(
				"AES", 192, ServiceType.CIPHER));
	}

	@Test(expected = CryptoPolicyException.class)
	public void testKeyStoreRejectsJKSCaseInsensitive() {
		CryptoPolicyManagerImpl cryptoPolicyManagerImpl = _create(true);

		cryptoPolicyManagerImpl.checkAlgorithm("jks", ServiceType.KEY_STORE);
	}

	@Test
	public void testKeyStoreAcceptsPKCS12() {
		CryptoPolicyManagerImpl cryptoPolicyManagerImpl = _create(true);

		Assert.assertEquals(
			"PKCS12",
			cryptoPolicyManagerImpl.checkAlgorithm(
				"PKCS12", ServiceType.KEY_STORE));
	}

	@Test
	public void testNonFIPSPassesThroughUnchanged() {
		CryptoPolicyManagerImpl cryptoPolicyManagerImpl = _create(false);

		Assert.assertEquals(
			"MD5",
			cryptoPolicyManagerImpl.checkAlgorithm(
				"MD5", ServiceType.MESSAGE_DIGEST));
		Assert.assertEquals(
			"DES",
			cryptoPolicyManagerImpl.checkAlgorithm(
				"DES", 56, ServiceType.CIPHER));
	}

	@Test
	public void testNormalizationReducesCipherTransformation() {
		CryptoPolicyManagerImpl cryptoPolicyManagerImpl = _create(true);

		Assert.assertEquals(
			"AES/GCM/NoPadding",
			cryptoPolicyManagerImpl.checkAlgorithm(
				"AES/GCM/NoPadding", 256, ServiceType.CIPHER));
	}

	@Test(expected = CryptoPolicyException.class)
	public void testNormalizationStillRejectsNonApprovedBase() {
		CryptoPolicyManagerImpl cryptoPolicyManagerImpl = _create(true);

		cryptoPolicyManagerImpl.checkAlgorithm(
			"DES/CBC/PKCS5Padding", ServiceType.CIPHER);
	}

	@Test
	public void testSHA1AllowedInHmacAndPBKDF2() {
		CryptoPolicyManagerImpl cryptoPolicyManagerImpl = _create(true);

		Assert.assertEquals(
			"HmacSHA1",
			cryptoPolicyManagerImpl.checkAlgorithm(
				"HmacSHA1", ServiceType.MAC));
		Assert.assertEquals(
			"PBKDF2WithHmacSHA1",
			cryptoPolicyManagerImpl.checkAlgorithm(
				"PBKDF2WithHmacSHA1", ServiceType.SECRET_KEY_FACTORY));
	}

	@Test(expected = CryptoPolicyException.class)
	public void testSHA1DisallowedForSignature() {
		CryptoPolicyManagerImpl cryptoPolicyManagerImpl = _create(true);

		cryptoPolicyManagerImpl.checkAlgorithm(
			"SHA1withRSA", ServiceType.SIGNATURE);
	}

	@Test
	public void testSHA512SlashDigestIsApproved() {
		CryptoPolicyManagerImpl cryptoPolicyManagerImpl = _create(true);

		Assert.assertEquals(
			"SHA-512/256",
			cryptoPolicyManagerImpl.checkAlgorithm(
				"SHA-512/256", ServiceType.MESSAGE_DIGEST));
	}

	private CryptoPolicyManagerImpl _create(boolean fipsEnabled) {
		return new CryptoPolicyManagerImpl() {

			@Override
			protected boolean isFIPSEnabled() {
				return fipsEnabled;
			}

		};
	}

}
```

- [ ] **Step 3: Run the test to verify it fails**

Run: `cd modules/apps/portal-security/portal-security-crypto-policy-impl && gw clean test --tests CryptoPolicyManagerImplTest`
Expected: FAIL — compilation error, `CryptoPolicyManagerImpl` does not exist.

- [ ] **Step 4: Write `CryptoPolicyManagerImpl.java`**

```java
/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.security.crypto.policy.internal;

import com.liferay.portal.kernel.util.PropsValues;
import com.liferay.portal.security.crypto.policy.CryptoPolicyManager;
import com.liferay.portal.security.crypto.policy.ServiceType;
import com.liferay.portal.security.crypto.policy.exception.CryptoPolicyException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.osgi.service.component.annotations.Component;

/**
 * Answers cryptographic-policy queries from a curated, NIST-cited catalog of
 * approved algorithms and key sizes. Does not introspect the installed JCE
 * providers. When {@code fips.enabled=true} the check methods enforce the
 * catalog; otherwise they pass the algorithm through unchanged.
 *
 * @author Manuele Castro
 */
@Component(service = CryptoPolicyManager.class)
public class CryptoPolicyManagerImpl implements CryptoPolicyManager {

	@Override
	public String checkAlgorithm(String algorithm, ServiceType serviceType) {
		if (!isFIPSEnabled()) {
			return algorithm;
		}

		Set<String> approvedAlgorithms = _getApprovedAlgorithms(serviceType);

		if (!approvedAlgorithms.contains(
				_baseAlgorithm(algorithm, serviceType))) {

			throw new CryptoPolicyException(
				"Algorithm \"" + algorithm + "\" is not approved in FIPS mode");
		}

		return algorithm;
	}

	@Override
	public String checkAlgorithm(
		String algorithm, int keySize, ServiceType serviceType) {

		checkAlgorithm(algorithm, serviceType);

		if (isFIPSEnabled()) {
			Set<Integer> approvedKeySizes = _getApprovedKeySizes(
				_baseAlgorithm(algorithm, serviceType));

			if (!approvedKeySizes.isEmpty() &&
				!approvedKeySizes.contains(keySize)) {

				throw new CryptoPolicyException(
					"Key size " + keySize + " for algorithm \"" + algorithm +
						"\" is not approved in FIPS mode");
			}
		}

		return algorithm;
	}

	@Override
	public List<String> getAllowedAlgorithms(
		ServiceType serviceType, List<String> candidateAlgorithms) {

		if (!isFIPSEnabled() || (candidateAlgorithms == null)) {
			return candidateAlgorithms;
		}

		Set<String> approvedAlgorithms = _getApprovedAlgorithms(serviceType);

		List<String> allowedAlgorithms = new ArrayList<>();

		for (String candidateAlgorithm : candidateAlgorithms) {
			if (approvedAlgorithms.contains(
					_baseAlgorithm(candidateAlgorithm, serviceType))) {

				allowedAlgorithms.add(candidateAlgorithm);
			}
		}

		return allowedAlgorithms;
	}

	@Override
	public List<Integer> getAllowedKeySizes(
		String algorithm, List<Integer> candidateKeySizes) {

		if (!isFIPSEnabled() || (candidateKeySizes == null)) {
			return candidateKeySizes;
		}

		Set<Integer> approvedKeySizes = _getApprovedKeySizes(algorithm);

		if (approvedKeySizes.isEmpty()) {
			return candidateKeySizes;
		}

		List<Integer> allowedKeySizes = new ArrayList<>();

		for (Integer candidateKeySize : candidateKeySizes) {
			if (approvedKeySizes.contains(candidateKeySize)) {
				allowedKeySizes.add(candidateKeySize);
			}
		}

		return allowedKeySizes;
	}

	@Override
	public boolean isFIPSMode() {
		return isFIPSEnabled();
	}

	// Protected so tests can override the gate without mutating PropsValues.

	protected boolean isFIPSEnabled() {
		return PropsValues.FIPS_ENABLED;
	}

	private String _baseAlgorithm(String algorithm, ServiceType serviceType) {
		if (algorithm == null) {
			return "";
		}

		algorithm = algorithm.trim();

		if (serviceType == ServiceType.CIPHER) {
			int index = algorithm.indexOf('/');

			if (index >= 0) {
				return algorithm.substring(0, index);
			}
		}

		return algorithm;
	}

	private Set<String> _getApprovedAlgorithms(ServiceType serviceType) {
		return _approvedAlgorithms.getOrDefault(
			serviceType, Collections.emptySet());
	}

	private Set<Integer> _getApprovedKeySizes(String algorithm) {
		if (algorithm == null) {
			return Collections.emptySet();
		}

		return _approvedKeySizes.getOrDefault(
			algorithm.trim(), Collections.emptySet());
	}

	private static Set<Integer> _keySizes(int... keySizes) {
		Set<Integer> set = new TreeSet<>();

		for (int keySize : keySizes) {
			set.add(keySize);
		}

		return Collections.unmodifiableSet(set);
	}

	private static Set<String> _names(String... names) {
		Set<String> set = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

		Collections.addAll(set, names);

		return Collections.unmodifiableSet(set);
	}

	private static final Map<ServiceType, Set<String>> _approvedAlgorithms;

	private static final Map<String, Set<Integer>> _approvedKeySizes;

	static {
		Map<ServiceType, Set<String>> approvedAlgorithms = new EnumMap<>(
			ServiceType.class);

		// FIPS 197. Triple-DES excluded (decrypt-only legacy since 2024-01-01).

		approvedAlgorithms.put(ServiceType.CIPHER, _names("AES"));

		// FIPS 197; FIPS 198-1. HMAC entries mirror MAC.

		approvedAlgorithms.put(
			ServiceType.KEY_GENERATOR,
			_names(
				"AES", "HmacSHA1", "HmacSHA224", "HmacSHA256", "HmacSHA384",
				"HmacSHA512"));

		// FIPS 186-5; SP 800-131A (RSA >= 2048).

		approvedAlgorithms.put(
			ServiceType.KEY_PAIR_GENERATOR,
			_names("EC", "Ed25519", "Ed448", "RSA"));

		// Keystore-format suitability (not an SP 800-140C function). JKS/JCEKS
		// excluded.

		approvedAlgorithms.put(ServiceType.KEY_STORE, _names("BCFKS", "PKCS12"));

		// FIPS 198-1; SP 800-185; SP 800-38B.

		approvedAlgorithms.put(
			ServiceType.MAC,
			_names(
				"AESCMAC", "HmacSHA1", "HmacSHA224", "HmacSHA256", "HmacSHA384",
				"HmacSHA512", "HmacSHA512/224", "HmacSHA512/256", "KMAC128",
				"KMAC256"));

		// FIPS 180-4; FIPS 202. Bare SHA-1 excluded.

		approvedAlgorithms.put(
			ServiceType.MESSAGE_DIGEST,
			_names(
				"SHA-224", "SHA-256", "SHA-384", "SHA-512", "SHA-512/224",
				"SHA-512/256", "SHA3-224", "SHA3-256", "SHA3-384", "SHA3-512"));

		// PBKDF2 primitives (SP 800-132).

		approvedAlgorithms.put(
			ServiceType.SECRET_KEY_FACTORY,
			_names(
				"PBKDF2WithHmacSHA1", "PBKDF2WithHmacSHA256",
				"PBKDF2WithHmacSHA384", "PBKDF2WithHmacSHA512"));

		// FIPS 186-5. SHA1with* excluded.

		approvedAlgorithms.put(
			ServiceType.SIGNATURE,
			_names(
				"Ed25519", "Ed448", "RSASSA-PSS", "SHA256withECDSA",
				"SHA256withRSA", "SHA384withECDSA", "SHA384withRSA",
				"SHA512withECDSA", "SHA512withRSA"));

		_approvedAlgorithms = Collections.unmodifiableMap(approvedAlgorithms);

		Map<String, Set<Integer>> approvedKeySizes = new TreeMap<>(
			String.CASE_INSENSITIVE_ORDER);

		approvedKeySizes.put("AES", _keySizes(128, 192, 256));
		approvedKeySizes.put("EC", _keySizes(256, 384, 521));
		approvedKeySizes.put("RSA", _keySizes(2048, 3072, 4096));

		_approvedKeySizes = Collections.unmodifiableMap(approvedKeySizes);
	}

}
```

- [ ] **Step 5: Run the test to verify it passes**

Run: `cd modules/apps/portal-security/portal-security-crypto-policy-impl && gw test --tests CryptoPolicyManagerImplTest`
Expected: PASS — all test methods green.

- [ ] **Step 6: Format and commit**

Run the `format-source` skill (it may reorder the enum constants, catalog entries, and test methods alphabetically — accept its output), then re-run the test to confirm still green, then:

```bash
git add modules/apps/portal-security/portal-security-crypto-policy-impl
git commit -m "LPD-90319 Add the crypto policy implementation and catalog"
```

---

## Task 3: Build and deploy verification

**Files:** none (verification only).

- [ ] **Step 1: Full clean build of both modules**

Run: `cd modules/apps/portal-security/portal-security-crypto-policy-api && gw clean build`
Then: `cd modules/apps/portal-security/portal-security-crypto-policy-impl && gw clean build`
Expected: `BUILD SUCCESSFUL` for both; the impl's `test` task runs the full suite green.

- [ ] **Step 2: Deploy to the running bundle**

Run: `cd modules/apps/portal-security/portal-security-crypto-policy-api && gw deploy`
Then: `cd modules/apps/portal-security/portal-security-crypto-policy-impl && gw deploy`
Expected: both jars deploy; no errors in `<bundles>/logs/liferay.<date>.log`.

- [ ] **Step 3: Confirm the component is active**

In the Gogo shell (or the logs), confirm `com.liferay.portal.security.crypto.policy.internal.CryptoPolicyManagerImpl` is registered and `active`. No FIPS runtime is required — the catalog is deterministic, so behavior is fully covered by the Task 2 unit tests; deployment only confirms the `@Component` activates.

- [ ] **Step 4: Commit (only if any fixes were needed)**

If Steps 1-3 required source fixes, run `format-source`, then:

```bash
git add modules/apps/portal-security/portal-security-crypto-policy-api modules/apps/portal-security/portal-security-crypto-policy-impl
git commit -m "LPD-90319 Fix crypto policy build and deployment issues"
```

---

## Phase 2 (Follow-up plan): Call-site wiring

Wiring the consumers is a **separate plan**, to be written and executed after the service above is built and deployed — each consumer is independent, needs its own current-source context, and can only be tested once the API bundle is deployed. Per the spec, the sites and their wiring are:

| Module | Call | Wiring |
|---|---|---|
| `portal-encryptor` (`EncryptorImpl`) | `Cipher.getInstance(algorithm)`, `KeyGenerator.getInstance(algorithm)` | `checkAlgorithm(algorithm, KEY_SIZE, CIPHER / KEY_GENERATOR)` |
| `portal-security-password-encryptor-impl` (`SSHAPasswordEncryptor`) | `MessageDigest.getInstance("SHA-1")` | `checkAlgorithm("SHA-1", MESSAGE_DIGEST)` — **throws in FIPS (intended)** |
| `portal-crypto-hash-provider-message-digest` | `MessageDigest.getInstance(algorithm)` | `checkAlgorithm(algorithm, MESSAGE_DIGEST)` |
| `digital-signature-impl` (`DSHttp`) | `Signature.getInstance(...)` | `checkAlgorithm(..., SIGNATURE)` |
| `multi-factor-authentication-timebased-otp-web` | `Mac.getInstance("HmacSHA1")` | `checkAlgorithm("HmacSHA1", MAC)` |
| `analytics-settings-impl` (`AnalyticsSecurityAuthVerifier`) | `Signature.getInstance("DSA")` | `checkAlgorithm("DSA", SIGNATURE)` — **throws in FIPS; must migrate** |
| `saml-opensaml-integration` (`CertificateToolImpl`) | `KeyPairGenerator.getInstance(algorithm).initialize(keySize)` | `checkAlgorithm(algorithm, keySize, KEY_PAIR_GENERATOR)` |
| `saml-web` (`UpdateCertificateMVCActionCommand`, `UpdateCertificateMVCResourceCommand`) | `KeyStore.getInstance("PKCS12")` | `checkAlgorithm("PKCS12", KEY_STORE)` |
| `portal-json-web-service-client` (`KeyStoreLoader`) | `KeyStore.getInstance("jks")` | `checkAlgorithm("jks", KEY_STORE)` — **throws in FIPS; must migrate to PKCS12/BCFKS** |

Wiring pattern per site: add `compileOnly project(":apps:portal-security:portal-security-crypto-policy-api")` to the consuming module's `build.gradle`; inject `@Reference private CryptoPolicyManager _cryptoPolicyManager;`; wrap the algorithm string with the matching `checkAlgorithm(...)` immediately before `getInstance(...)`.

---

## Self-Review

**Spec coverage:**
- API surface (`CryptoPolicyManager`, `ServiceType` incl. `KEY_STORE`, `CryptoPolicyException`) → Task 1. ✓
- Curated catalog, all 8 service types with NIST-cited membership incl. SHA-1 split → Task 2 `CryptoPolicyManagerImpl` static block. ✓
- `fips.enabled` gates enforcement and filtering; non-FIPS passes candidates through, FIPS filters → Task 2 (`isFIPSEnabled`, query methods). ✓
- Empty-key-size-set = accept → Task 2 3-arg `checkAlgorithm` + `testEmptyKeySizeSetAccepts`. ✓
- Normalization (CIPHER-only transformation reduction, case-insensitive) → `_baseAlgorithm` + `_names` case-insensitive set + `testNormalization*` / `testSHA512SlashDigestIsApproved`. ✓
- `KEY_STORE` gating incl. case-insensitive JKS → `testKeyStore*`. ✓
- Named unit tests from the spec → Task 2 (all present, plus `testSHA512SlashDigestIsApproved` guarding the `/`-in-name correctness). ✓
- Call-site wiring → Phase 2 (separate plan, per scope split). ✓ (deliberately deferred)

**Placeholder scan:** No TBD/TODO; all code blocks complete; the only forward-reference ("provided by the Liferay Gradle plugin") includes the concrete fallback dependency.

**Type consistency:** `checkAlgorithm`, `getAllowedAlgorithms`, `getAllowedKeySizes`, `isFIPSMode`, `isFIPSEnabled`, `ServiceType` constant names, and `_baseAlgorithm`/`_names`/`_keySizes` helpers are used identically across the interface, impl, and tests.

**Note for the spec:** the spec's normalization bullet says stripping after `/` is "a no-op for the other service types"; that is inaccurate for `SHA-512/224` and `SHA-512/256`. This plan implements CIPHER-only stripping; the spec's wording should be corrected to match.