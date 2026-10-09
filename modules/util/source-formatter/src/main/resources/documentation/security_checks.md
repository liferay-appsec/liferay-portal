# Security Checks

Check | File Extensions | Description
----- | --------------- | -----------
CryptoGetInstanceCallCheck | .java | Finds JCA classes instantiated with `getInstance` outside `CryptoUtil`. |
CryptoSecretKeySpecCheck | .java | Finds secret keys built with `new SecretKeySpec` instead of `CryptoUtil.createSecretKey`. |
FIPSTLSVerificationCheck | .java | Finds outbound TLS verification bypasses that are not guarded by `PropsValues.FIPS_ENABLED`, see LPD-93649. |
JSPXSSVulnerabilitiesCheck | .jsp, .jspf, .jspx, .tag, .tpl, or .vm | Finds xss vulnerabilities. |
JavaDeserializationSecurityCheck | .java | Finds Java serialization vulnerabilities. |
JavaXMLSecurityCheck | .java | Finds possible XXE or Quadratic Blowup security vulnerabilities. |
SecretComparisonCheck | .java | Finds secrets compared with a method that returns at the first differing byte. |