# Security Checks

Check | File Extensions | Description
----- | --------------- | -----------
JSPXSSVulnerabilitiesCheck | .jsp, .jspf, .jspx, .tag, .tpl, or .vm | Finds xss vulnerabilities. |
JavaDeserializationSecurityCheck | .java | Finds Java serialization vulnerabilities. |
JavaTLSVerificationCheck | .java | Checks that outbound TLS verification bypasses are guarded by `FIPSModeValidator`, see LPD-93649. |
JavaXMLSecurityCheck | .java | Finds possible XXE or Quadratic Blowup security vulnerabilities. |