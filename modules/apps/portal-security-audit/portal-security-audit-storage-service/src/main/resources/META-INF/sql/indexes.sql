create index IX_8FE31EDF on Audit_AuditEvent (companyId);

create unique index IX_F5F25FC on Audit_AuditPseudonym (companyId, contextName[$COLUMN_LENGTH:75$], fieldCategory[$COLUMN_LENGTH:75$], valueHash[$COLUMN_LENGTH:75$]);