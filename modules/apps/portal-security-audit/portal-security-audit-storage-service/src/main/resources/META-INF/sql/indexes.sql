create index IX_8FE31EDF on Audit_AuditEvent (companyId);

create unique index IX_A9E9724E on Audit_AuditPseudonym (companyId, contextName[$COLUMN_LENGTH:75$], fieldCategory[$COLUMN_LENGTH:75$], value[$COLUMN_LENGTH:255$]);