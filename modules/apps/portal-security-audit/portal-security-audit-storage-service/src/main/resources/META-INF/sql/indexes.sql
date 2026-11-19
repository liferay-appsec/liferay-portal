create index IX_8FE31EDF on Audit_AuditEvent (companyId);

create unique index IX_763E9FCF on Audit_AuditEventPseudonymField (companyId, contextName[$COLUMN_LENGTH:75$], name[$COLUMN_LENGTH:75$], valueHash[$COLUMN_LENGTH:75$]);

create index IX_21090D24 on Audit_EventAccountEntryRel (companyId);