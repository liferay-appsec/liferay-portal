# CryptoPolicyManager Redesign

## Context

Liferay Portal needs a runtime service that exposes which cryptographic algorithms
and key sizes are approved for use, so that features (SAML, OAuth2, LDAP, password
encryption, digital signatures, MFA, etc.) can consult a single authority when
building algorithm-selection UIs, validating configuration inputs, and — in a FIPS
environment — refusing non-approved algorithm attempts at runtime.

A prior implementation exists on the `FIPS-Refinement-1` branch (identical to
`me/LPD-94806`) under tickets LPD-82902 / LPD-94806. It is **not on `master`**. This
redesign supersedes it and is a deliberate, ground-up replacement of the mechanism
that decides "approved." This redesign is tracked under LPD-90319.

### Why the prior implementation was replaced

The prior `CryptoPolicyManagerImpl` derived the approved set by enumerating the
installed JCE providers at activation:

```java
buildAlgorithmMap(Security.getProviders());   // every algorithm every provider implements
buildKeySizeMap(Security.getProviders());      // probe KeyGenerator/KeyPairGenerator sizes
```

This is FIPS-incorrect by construction. "Implemented by a registered provider" is not
"approved":

- ISO/IEC 19790 §7.4.3 states a module "may provide other services … both approved and
  non-approved."
- SP 800-140Cr2 §6 "precludes the use of all other security functions."

Concretely, `FIPSModeValidator` keeps companion providers registered in FIPS mode —
`SUN`, `SunJCE`, and others are whitelisted in both the BCFIPS and Amazon Corretto
lists (`FIPSModeValidator.java:155-164`). `SUN` unconditionally registers MD5 and
SHA-1. Enumerating `Security.getProviders()` therefore surfaces MD5 and SHA-1 even with
`fips.enabled=true`, so the prior gate's approved set for `MESSAGE_DIGEST` would contain
MD5 and `checkAlgorithm("MD5", MESSAGE_DIGEST)` would not throw — defeating the gate and
contradicting the prior spec's own integration assertions. The prior unit test hid this
by injecting a single clean mock provider, never exercising the real multi-provider
path.

## FIPS Scope: What This Gate Does and Does Not Decide

This is the most important framing for the design, and it is deliberately narrow.

FIPS imposes two *separate* kinds of requirement, and this service addresses only the
first:

- **Regime 1 — Approved cryptographic functions (Annex C / SP 800-140C).** A module
  running in approved-only mode may only invoke approved security functions. This is a
  hard, enumerable, primitive-level requirement (is `AES` approved? is `MD5`?). **This
  gate implements Regime 1.**

- **Regime 2 — Authentication-mechanism and protocol policy (Annex E / SP 800-140E →
  SP 800-63B, SP 800-131A usage rules).** Whether a *construction* is adequate for a
  given purpose — e.g. whether a password verifier must use a salted, iterated KDF, or
  whether a key size is approved for a specific direction (sign vs verify) — is a policy
  layer above the primitive. **This gate does not and cannot implement Regime 2.**

The distinction has direct consequences that must not be blurred:

- ISO/IEC 19790:2025 Annex E, E.4: *"No approved password protection standards exist at
  this time."* There is no FIPS-approved password-hashing algorithm list.
- SP 800-132 approves PBKDF2 as a PBKDF for *deriving keys to protect stored data*
  ("The MK shall not be used for other purposes"); it does not require a PBKDF for
  storing password verifiers.
- The "salt + iterate for memorized secrets" requirement lives in SP 800-63B, which
  SP 800-140E references as a framework for **operator** authentication ("should …
  provide justification whenever SP 800-63B requirements cannot be met").

Therefore: this gate answers "is this *primitive* approved for this JCA service type?"
It intentionally does **not** answer "is this an adequate password scheme?" or "is this
key size approved for this direction?" A caller that receives `SHA-256` as an approved
`MESSAGE_DIGEST` must not read that as "SHA-256 is a valid standalone password hash" —
that Regime 2 judgment is out of scope.

### `ServiceType` coverage

The eight `ServiceType` values cover every Liferay call site that selects a FIPS-relevant
algorithm. A survey of the other JCA engine classes (per the *Java Security Standard
Algorithm Names* spec) against actual Liferay usage found nothing else to gate:

- **`KeyAgreement`** (ECDH / DH): no call sites — key agreement runs inside TLS / JSSE, not
  the portal layer. Add a `KEY_AGREEMENT` value if a direct consumer ever appears.
- **`SecureRandom`**: no `getInstance("<prng>")` call sites; all usages are
  `new SecureRandom()`, which defers to the highest-priority provider's DRBG — the FIPS DRBG
  in approved mode. There is no algorithm string to gate, and the default behavior is
  already FIPS-correct.
- **`KeyFactory`**: key-spec ↔ `Key` conversion only, no key size; approval is decided when
  the key is generated (`KEY_PAIR_GENERATOR`) or used (`SIGNATURE`), so gating the
  conversion adds nothing.
- **`SSLContext`, `KeyManagerFactory`**: the TLS layer. Protocol and cipher-suite approval
  is governed by JSSE / BCJSSE configuration, not an algorithm-name gate.
- **`AlgorithmParameters`, `CertificateFactory`**: no algorithm-selecting call sites.

`KEY_STORE` is included despite not being an approved *function* — see the catalog note.

## Design Decisions

1. **Curated catalog as the sole source of truth.** The approved set is an explicit,
   hardcoded, NIST-cited table. No `Security.getProviders()` introspection. Non-approved
   algorithms (MD5, bare SHA-1, DES, Blowfish, …) are simply absent from the catalog, so
   the leak that motivated the redesign cannot occur.

1. **Point-in-time model.** The catalog encodes the current "approved for new use" set:
   algorithms and key sizes per `ServiceType`. It does not model transition dates or
   usage direction. Transitions are handled by editing the catalog over time (see
   Maintenance).

1. **Hardcoded, type-safe representation.** The catalog is `static final` data in the
   impl, each entry commented with its NIST basis, reviewable against NIST in the diff.
   No external resource file, no parsing, no operator override — a compliance authority
   should have one auditable definition with no runtime failure modes.

1. **`fips.enabled` gates both enforcement and filtering.** Every method passes its input
   through unchanged outside FIPS mode and enforces inside it. `checkAlgorithm` asserts a
   single value: passthrough outside FIPS, throw on a non-approved value inside. The
   `getAllowedAlgorithms` / `getAllowedKeySizes` filters take the caller's candidate list
   and return it unchanged outside FIPS, or its approved subset (order preserved) inside —
   so a selection UI feeds the algorithms it offers and gets back what it may show, and a
   validator feeds the configured values and gets back the acceptable ones.

1. **Reuse the existing module and API surface.** Keep the `portal-security-crypto-policy-api`
   / `-impl` modules and the `CryptoPolicyManager` / `ServiceType` / `CryptoPolicyException`
   API so existing call-site wiring maps over; the implementation is fresh.

## Module Structure

```
modules/apps/portal-security/
  portal-security-crypto-policy-api/
    .lfrbuild-portal, bnd.bnd, build.gradle
    src/main/java/com/liferay/portal/security/crypto/policy/
      CryptoPolicyManager.java
      ServiceType.java
      exception/CryptoPolicyException.java
  portal-security-crypto-policy-impl/
    .lfrbuild-portal, bnd.bnd, build.gradle
    src/main/java/com/liferay/portal/security/crypto/policy/internal/
      CryptoPolicyManagerImpl.java
    src/test/java/.../internal/CryptoPolicyManagerImplTest.java
```

No SPI layer — there is no pluggable behavior; the catalog is a single authority.

## API

### `ServiceType`

Maps 1:1 to Java Security service-type strings, scoping a query to one kind of
operation. The service-type string is the JCA canonical name.

```java
public enum ServiceType {
    CIPHER,              // "Cipher"
    KEY_GENERATOR,       // "KeyGenerator"
    KEY_PAIR_GENERATOR,  // "KeyPairGenerator"
    KEY_STORE,           // "KeyStore"
    MESSAGE_DIGEST,      // "MessageDigest"
    MAC,                 // "Mac"
    SIGNATURE,           // "Signature"
    SECRET_KEY_FACTORY   // "SecretKeyFactory"
}
```

### `CryptoPolicyException`

```java
public class CryptoPolicyException extends RuntimeException {
    public CryptoPolicyException(String message) { super(message); }
}
```

Unchecked; thrown only in FIPS mode when a non-approved algorithm or key size is
requested.

### `CryptoPolicyManager`

```java
public interface CryptoPolicyManager {

    // Filters the caller's candidate algorithms.
    // Non-FIPS: returns the candidates unchanged.
    // FIPS: returns only the approved candidates for the service type, preserving the
    // caller's order. Always a SUBSET of the candidates, never a superset — an approved
    // algorithm the caller did not offer is never added.
    List<String> getAllowedAlgorithms(
        ServiceType serviceType, List<String> candidateAlgorithms);

    // Filters the caller's candidate key sizes for the algorithm.
    // Non-FIPS: returns the candidates unchanged.
    // FIPS: returns only the approved candidate key sizes, preserving order; when the
    // algorithm has no key-size constraint the candidates are returned unchanged (an
    // empty catalog set means "unconstrained," not "nothing allowed"). Pass the BASE
    // algorithm name (AES, RSA, EC) — no service type is supplied, so Cipher
    // transformation names are not normalized.
    List<Integer> getAllowedKeySizes(
        String algorithm, List<Integer> candidateKeySizes);

    // Non-FIPS: returns algorithm unchanged.
    // FIPS: returns algorithm if approved for the service type; else throws.
    String checkAlgorithm(String algorithm, ServiceType serviceType);

    // Non-FIPS: returns algorithm unchanged.
    // FIPS: returns algorithm if the algorithm is approved AND (the key size is approved
    // OR the algorithm has no key-size constraint); else throws.
    String checkAlgorithm(String algorithm, int keySize, ServiceType serviceType);

    boolean isFIPSMode();
}
```

## The Catalog

The single source of truth. Membership reflects the "approved for new use" set as of the
stamp date in Maintenance, grounded in NIST sources.

| ServiceType | Approved algorithms | Approved key sizes | NIST basis |
|---|---|---|---|
| `CIPHER` | `AES` | 128, 192, 256 | FIPS 197. Triple-DES excluded (three-key TDES is decrypt-only legacy since 2024-01-01, SP 800-140C §6.2.2.2). |
| `KEY_GENERATOR` | `AES`; HMAC key generators mirroring `MAC` (`HmacSHA1`, `HmacSHA224/256/384/512`) | AES: 128, 192, 256 | FIPS 197; FIPS 198-1. |
| `KEY_PAIR_GENERATOR` | `RSA`, `EC`, `Ed25519`, `Ed448` | RSA: 2048, 3072, 4096; EC: 256, 384, 521; EdDSA: fixed (no size) | FIPS 186-5; SP 800-131A (RSA ≥ 2048). |
| `KEY_STORE` | `PKCS12`, `BCFKS` | — | Keystore-format suitability, **not** an SP 800-140C function — see note below. `JKS` and `JCEKS` excluded. |
| `MESSAGE_DIGEST` | `SHA-224`, `SHA-256`, `SHA-384`, `SHA-512`, `SHA-512/224`, `SHA-512/256`, `SHA3-224`, `SHA3-256`, `SHA3-384`, `SHA3-512` | — | FIPS 180-4; FIPS 202. Bare SHA-1 excluded — see note below. |
| `MAC` | `HmacSHA1`, `HmacSHA224`, `HmacSHA256`, `HmacSHA384`, `HmacSHA512`, `HmacSHA512/224`, `HmacSHA512/256`, `KMAC128`, `KMAC256`, `AESCMAC` | — | FIPS 198-1; SP 800-185; SP 800-38B. |
| `SIGNATURE` | `SHA256withRSA`, `SHA384withRSA`, `SHA512withRSA`, `RSASSA-PSS`, `SHA256withECDSA`, `SHA384withECDSA`, `SHA512withECDSA`, `Ed25519`, `Ed448` | — | FIPS 186-5. `SHA1with*` excluded (SHA-1 signature generation disallowed). |
| `SECRET_KEY_FACTORY` | `PBKDF2WithHmacSHA1`, `PBKDF2WithHmacSHA256`, `PBKDF2WithHmacSHA384`, `PBKDF2WithHmacSHA512` | — | PBKDF2 primitives (SP 800-132). See advisory note below. |

**Deliberately excluded:** MD2, MD5, bare SHA-1, DES, DESede / Triple-DES, Blowfish, RC*,
DSA (signature generation, withdrawn under FIPS 186-5), all `SHA1with*` signatures, and the
`JKS` / `JCEKS` keystore formats.

### `KEY_STORE` is a scoped exception to the Annex C framing

`KeyStore` is a JCA service type, so it fits the mechanism exactly (no key size →
unconstrained), but a keystore *format* is not an SP 800-140C "security function," so this
row has no clean Annex C citation. It is included deliberately because the format decides
which crypto runs inside: `JKS` uses a SHA-1-based integrity MAC and a proprietary
key-protection scheme, and `JCEKS` uses `PBEWithMD5AndTripleDES` — neither is FIPS-suitable,
and loading them can fail outright in approved-only mode. `PKCS12` (with approved interior
algorithms) and `BouncyCastle`'s `BCFKS` are the FIPS-appropriate formats. This is the one
place the gate reaches slightly beyond "approved functions" into "approved formats"; the
maintainer should treat this row as policy-driven rather than Annex-C-derived.

### SHA-1 handling

SHA-1 is use-dependent, and the `ServiceType` keying captures the distinction:

- **Included** where SHA-1 is currently acceptable for non-collision-resistance use:
  `HmacSHA1` (`MAC`, `KEY_GENERATOR`) and `PBKDF2WithHmacSHA1` (`SECRET_KEY_FACTORY`).
  SP 800-131A Rev 2 Table 7 rates HMAC-based KDFs "Acceptable," and Table 8 rates SHA-1
  "acceptable for non-digital-signature applications that do not require collision
  resistance." This also keeps two real call sites working under FIPS: TOTP MFA
  (`Mac.getInstance("HmacSHA1")`) and the default password encryptor
  (`PBKDF2WithHmacSHA1`).
- **Excluded** from `SIGNATURE` — SHA-1 signature generation is disallowed (SP 800-131A
  Rev 2 Table 8).
- **Excluded** from `MESSAGE_DIGEST` — a *deliberate conservative policy choice*, not a
  strict Annex C requirement. Bare SHA-1 is technically "acceptable for
  non-collision-resistance applications," but the point-in-time / no-direction model
  cannot distinguish a collision-resistant use from a benign one, so the catalog omits
  it. **Consequence:** `SSHA` password verification, which calls
  `MessageDigest.getInstance("SHA-1")`, is rejected in FIPS mode (see Call-Site Wiring).
  This choice is revisitable by the catalog maintainer.

### `SECRET_KEY_FACTORY` is advisory in practice

`PBKDF2WithHmac*` are listed because their primitives are approved functions, which is
useful for selection UIs and config validation. Two caveats, both from the FIPS scope
framing above:

- The gate does **not** encode the Regime 2 rule that passwords must use a salted,
  iterated KDF. Presence of `PBKDF2WithHmacSHA256` in the catalog means "the primitive
  is approved," not "this is a compliant password scheme."
- Liferay's default `PBKDF2PasswordEncryptor` drives BouncyCastle's low-level
  `PKCS5S2ParametersGenerator` directly and never calls a JCA `SecretKeyFactory`, so the
  gate cannot enforce it without a refactor. Rerouting `PBKDF2PasswordEncryptor` through
  the gate (or through JCA) is out of scope for this ticket and left as an open item.

## Behavior and Semantics

### FIPS detection

```java
public boolean isFIPSMode() { return isFIPSEnabled(); }

// Protected so tests can override the gate without touching PropsValues.
protected boolean isFIPSEnabled() { return PropsValues.FIPS_ENABLED; }
```

### Algorithm-name normalization

Callers pass strings that do not always match a catalog key verbatim. Normalization is
applied consistently before every lookup (in both `checkAlgorithm` overloads and in
`getAllowedKeySizes`):

1. **Cipher transformation reduction (`CIPHER` only).** For `ServiceType.CIPHER`, a
   transformation is reduced to its base algorithm: text before the first `/`.
   `"AES/GCM/NoPadding"` → `"AES"`, `"AES/CBC/PKCS5Padding"` → `"AES"`. This stripping is
   applied **only** to `CIPHER` — never to other service types, because some canonical
   JCA names legitimately contain `/` (e.g. the `MESSAGE_DIGEST` names `SHA-512/224` and
   `SHA-512/256`, and the `MAC` names `HmacSHA512/224` and `HmacSHA512/256`). Blindly
   stripping after `/` would mangle those.

1. **Case-insensitive comparison.** Matching against the catalog is case-insensitive.
   The catalog stores canonical JCA names; input case does not matter.

1. **No cross-convention rewriting.** Normalization does **not** unify dashed vs
   undashed forms. `SHA-256` (a `MessageDigest` name) and `SHA256withRSA` (a `Signature`
   name) live under different service types and are stored in their canonical JCA form;
   lookups are scoped by `ServiceType`, so there is no cross-contamination.

The same normalized base algorithm is used for the key-size lookup, so
`checkAlgorithm("AES/GCM/NoPadding", 256, CIPHER)` normalizes to `"AES"`, confirms `AES`
is approved, then checks 256 against the catalog's approved sizes for `AES`.

### `checkAlgorithm` — key-size semantics

The catalog only defines key sizes for algorithms that *have* a configurable size (AES,
RSA, EC). For every other algorithm `getAllowedKeySizes` returns the empty set. The
3-arg overload must treat an empty allowed-size set as **"no key-size constraint —
accept,"** not as "reject everything":

```java
public String checkAlgorithm(String algorithm, int keySize, ServiceType serviceType) {
    checkAlgorithm(algorithm, serviceType);   // algorithm-level check first

    if (isFIPSEnabled()) {

        // Reads the catalog directly. The public getAllowedKeySizes is a
        // mode-aware candidate filter, so checkAlgorithm cannot delegate to it.

        Set<Integer> approvedKeySizes = _getApprovedKeySizes(
            _baseAlgorithm(algorithm, serviceType));

        if (!approvedKeySizes.isEmpty() && !approvedKeySizes.contains(keySize)) {
            throw new CryptoPolicyException(
                "Key size " + keySize + " for algorithm \"" + algorithm +
                    "\" is not approved in FIPS mode");
        }
    }

    return algorithm;
}
```

This is safe precisely because the catalog is static: "empty" unambiguously means
"unconstrained." (The old provider-probing design could not distinguish "unconstrained"
from "probing failed.") Without this rule, `checkAlgorithm("HmacSHA256", 256,
KEY_GENERATOR)` — or any size-less algorithm routed through the 3-arg overload — would
falsely throw.

### Query methods (candidate filters)

`getAllowedAlgorithms` and `getAllowedKeySizes` are mode-aware filters over the caller's
candidate list, not catalog dumps:

- **Non-FIPS:** early-exit — the caller's list is returned **as-is** (the same instance;
  no copy, no filtering). A `null` candidate list is returned unchanged.
- **FIPS:** returns a fresh `List` containing only the approved candidates, in the
  caller's order. `getAllowedAlgorithms` matches case-insensitively and applies
  `CIPHER`-only transformation reduction to each candidate before the catalog test, but
  returns the caller's original strings. `getAllowedKeySizes` returns the candidates
  unchanged when the algorithm has no key-size constraint (empty catalog set =
  "unconstrained").

The result is always a **subset** of the candidates, never a superset — an approved value
the caller did not offer is never introduced. The full approved catalog is not itself
externally queryable (see Maintenance).

## Call-Site Wiring and Compatibility Consequences

Each call site takes `@Reference CryptoPolicyManager` and wraps its algorithm string
before `getInstance(...)`. Ported from the prior wiring:

| Module | Call | Wiring |
|---|---|---|
| `portal-encryptor` | `Cipher.getInstance(algorithm)` | `checkAlgorithm(algorithm, KEY_SIZE, CIPHER)` |
| `portal-encryptor` | `KeyGenerator.getInstance(algorithm)` | `checkAlgorithm(algorithm, KEY_SIZE, KEY_GENERATOR)` |
| `portal-security-password-encryptor-impl` (`SSHAPasswordEncryptor`) | `MessageDigest.getInstance("SHA-1")` | `checkAlgorithm("SHA-1", MESSAGE_DIGEST)` |
| `portal-crypto-hash-provider-message-digest` | `MessageDigest.getInstance(algorithm)` | `checkAlgorithm(algorithm, MESSAGE_DIGEST)` |
| `digital-signature-impl` (`DSHttp`) | `Signature.getInstance(...)` | `checkAlgorithm(..., SIGNATURE)` |
| `multi-factor-authentication-timebased-otp-web` | `Mac.getInstance("HmacSHA1")` | `checkAlgorithm("HmacSHA1", MAC)` |
| `analytics-settings-impl` (`AnalyticsSecurityAuthVerifier`) | `Signature.getInstance("DSA")` | `checkAlgorithm("DSA", SIGNATURE)` |
| `saml-opensaml-integration` (`CertificateToolImpl`) | `KeyPairGenerator.getInstance(algorithm).initialize(keySize)` | `checkAlgorithm(algorithm, keySize, KEY_PAIR_GENERATOR)` |
| `saml-web` (`UpdateCertificateMVCActionCommand`, `UpdateCertificateMVCResourceCommand`) | `KeyStore.getInstance("PKCS12")` | `checkAlgorithm("PKCS12", KEY_STORE)` |
| `portal-json-web-service-client` (`KeyStoreLoader`) | `KeyStore.getInstance("jks")` | `checkAlgorithm("jks", KEY_STORE)` |

Decided consequences to document, not discover:

- **`SSHA` breaks in FIPS mode.** With bare SHA-1 excluded from `MESSAGE_DIGEST`,
  `checkAlgorithm("SHA-1", MESSAGE_DIGEST)` throws. This is intended — `SSHA` is not an
  adequate password scheme — but it is a breaking change for existing `SSHA` password
  hashes on a portal switched into FIPS mode, and it generalizes to the
  `passwords.encryption.algorithm.legacy` upgrade-on-login path (verifying an old MD5 /
  SHA / SSHA hash invokes a non-approved digest). Migration to `PBKDF2WithHmac*` is the
  remedy.
- **`analytics-settings-impl` uses `DSA`,** which is excluded from `SIGNATURE`; it will
  throw in FIPS mode and must migrate to an approved signature algorithm.
- **HMAC in `KEY_GENERATOR`.** HMAC key generators are mirrored from `MAC` so that
  generating an HMAC key (including `HmacSHA1`) via `KeyGenerator` in FIPS mode is not
  falsely rejected.
- **`KeyStoreLoader` uses `jks`.** `portal-json-web-service-client` loads a `JKS`
  keystore, which is excluded, so `checkAlgorithm("jks", KEY_STORE)` throws in FIPS mode
  (case-insensitive normalization matches the excluded `JKS`). Intended — the client must
  migrate to `PKCS12` / `BCFKS`. The two SAML paths already use `PKCS12` and pass.

## Error Handling

- `CryptoPolicyException` (unchecked) only in FIPS mode, only for a non-approved
  algorithm or a constrained-but-non-approved key size.
- Unknown `ServiceType` or unknown algorithm → the FIPS filter drops every candidate (an
  empty result); an algorithm without a configurable key size → the FIPS key-size filter
  returns the candidates unchanged and the 3-arg `checkAlgorithm` does not throw on size
  (empty = unconstrained).
- The query filters never throw and never return `null` in FIPS mode (a `null` candidate
  list is returned unchanged only through the non-FIPS early exit).
- Non-FIPS mode → `checkAlgorithm` never throws and the filters return their input
  unchanged.

## Testing

Pure, deterministic unit tests against the static catalog. No provider mocking is needed
or wanted — the catalog is fixed data, which is the whole point of the redesign. FIPS
mode is toggled by overriding `isFIPSEnabled()` in a test subclass.

Named tests:

- `testNonFIPSPassesThroughUnchanged` — every `checkAlgorithm` returns its input verbatim
  when FIPS is off, including non-approved algorithms.
- `testGetAllowedNonFIPSReturnsCandidatesUnchanged` — when FIPS is off, both filters
  return the caller's candidate list as the same instance, non-approved entries included.
- `testFIPSApprovedAlgorithmReturned` — `checkAlgorithm("SHA-256", MESSAGE_DIGEST)`
  returns `"SHA-256"`.
- `testFIPSNonApprovedAlgorithmThrows` — `checkAlgorithm("MD5", MESSAGE_DIGEST)` throws.
- `testGetAllowedAlgorithmsReturnsApprovedSubsetOfCandidates` — in FIPS,
  `getAllowedAlgorithms(KEY_PAIR_GENERATOR, ["RSA", "DSA"])` returns exactly `["RSA"]`:
  `DSA` (offered, not approved) is dropped, and `EC` (approved, not offered) is never
  added. This is the defining subset-not-superset contract of the redesign.
- `testGetAllowedAlgorithmsExcludesLegacyPreservingOrder` — in FIPS,
  `getAllowedAlgorithms(MESSAGE_DIGEST, ["MD5", "SHA-256", "SHA-1", "SHA-512"])` returns
  `["SHA-256", "SHA-512"]` (legacy dropped, order preserved).
- `testKeySizeConstraintEnforced` — `checkAlgorithm("AES", 192, CIPHER)` returns;
  `checkAlgorithm("AES", 200, CIPHER)` throws.
- `testGetAllowedKeySizesFiltersToApproved` — in FIPS,
  `getAllowedKeySizes("RSA", [1024, 2048, 3072, 4096])` returns `[2048, 3072, 4096]`.
- `testGetAllowedKeySizesUnconstrainedReturnsAllCandidates` — in FIPS,
  `getAllowedKeySizes("SHA-256", [128, 256])` returns `[128, 256]` unchanged (no size
  constraint → empty catalog set → all candidates pass).
- `testEmptyKeySizeSetAccepts` — `checkAlgorithm("HmacSHA256", 256, KEY_GENERATOR)`
  returns without throwing (no size constraint).
- `testNormalizationReducesCipherTransformation` — `checkAlgorithm("AES/GCM/NoPadding",
  256, CIPHER)` returns; a non-approved base still throws after normalization, e.g.
  `checkAlgorithm("DES/CBC/PKCS5Padding", CIPHER)`.
- `testSHA512SlashDigestIsApproved` — `checkAlgorithm("SHA-512/256", MESSAGE_DIGEST)`
  returns (the `/` in the digest name is not stripped).
- `testSHA1SplitByServiceType` — `checkAlgorithm("PBKDF2WithHmacSHA1",
  SECRET_KEY_FACTORY)` and `checkAlgorithm("HmacSHA1", MAC)` pass; `checkAlgorithm("SHA-1",
  MESSAGE_DIGEST)` and `checkAlgorithm("SHA1withRSA", SIGNATURE)` throw.
- `testKeyStoreFormatGated` — `checkAlgorithm("PKCS12", KEY_STORE)` returns;
  `checkAlgorithm("jks", KEY_STORE)` and `checkAlgorithm("JKS", KEY_STORE)` both throw
  (case-insensitive).

## Maintenance and Known Limitations

- **As-of date: 2026-07-01.** The catalog is a point-in-time snapshot and must be
  reviewed against current NIST guidance periodically.
- **SHA-1 sunset — 2030-12-31.** `PBKDF2WithHmacSHA1`, `HmacSHA1`, and the `HmacSHA1`
  `KEY_GENERATOR` entry are acceptable now but disallowed after NIST's announced SHA-1
  retirement (being formalized in the SP 800-131A Rev 3 draft). A hardcoded catalog with
  no date awareness will silently be wrong on 2031-01-01; these entries must be removed
  at that transition, and the `SHA256`-based PBKDF2/HMAC variants preferred meanwhile.
- **No transition dates or usage direction.** By design (point-in-time model). The
  catalog cannot express date-based or direction-based approval (e.g. TDES
  decrypt-only), so it lists only "approved for new use."
- **No `KEY_AGREEMENT` service type.** ECDH / DH are not covered (no portal-layer call
  sites — see ServiceType coverage). Add the value if a direct consumer appears.
- **The full approved catalog is not externally queryable.** The query methods are
  candidate filters — they only ever narrow a caller-supplied list — so no consumer can
  enumerate the entire approved set through the API. This is a deliberate consequence of
  dropping the no-arg query forms in favor of the mode-aware filters. A maintainer who
  needs a compliance dump reads the `static` catalog table in `CryptoPolicyManagerImpl`
  directly.
- **`KEY_STORE` is policy-driven, not Annex-C-derived** (see the catalog note). Its
  approved set (`PKCS12`, `BCFKS`) reflects keystore-format suitability, so it is the one
  catalog row a maintainer updates on format guidance rather than on an SP 800-140C change.
- **Regime 1 only.** The gate decides approved *primitives* (Annex C). It does not judge
  password-scheme adequacy, protocol suitability, or per-direction key-size approval
  (Regime 2 / SP 800-63B / SP 800-131A usage rules). Consumers must not infer Regime 2
  compliance from a Regime 1 approval.
- **`SECRET_KEY_FACTORY` is advisory** for the default password path, which bypasses JCA
  (see catalog note). Enforcing it requires refactoring `PBKDF2PasswordEncryptor` — open
  item, out of scope here.

## References

- Local FIPS knowledge base: `NIST.SP.800-140C.md` (approved functions / Annex C),
  `NIST.SP.800-140D.md` (SP 800-132 as an Annex D method), `NIST.SP.800-140E.md` and
  `ISO_19790/annex_e_approved_authentication_mechanisms.md` (authentication mechanisms;
  E.4 "no approved password protection standards"),
  `ISO_19790/section_07_4_roles_services_and_authentication.md` (approved vs
  non-approved services).
- NIST SP 800-131A Rev 2 — SHA-1 / HMAC / KDF transition status (Tables 7, 8).
- NIST SP 800-132 — PBKDF2 (scope: deriving keys to protect stored data).
- NIST SP 800-63B-4 — memorized-secret verifier storage (§5.1.1.2).
- FIPS 197, 180-4, 202, 186-5, 198-1.