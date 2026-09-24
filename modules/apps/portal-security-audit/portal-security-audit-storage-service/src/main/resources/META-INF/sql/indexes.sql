create index IX_8FE31EDF on Audit_AuditEvent (companyId);

create unique index IX_9C77F5E on Audit_AuditPseudonym (companyId, contextName[$COLUMN_LENGTH:75$], fieldCategory[$COLUMN_LENGTH:75$], identityValueHash[$COLUMN_LENGTH:75$]);