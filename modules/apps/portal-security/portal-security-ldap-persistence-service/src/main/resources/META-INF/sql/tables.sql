create table LDAPServerAttributeRel (
	mvccVersion LONG default 0 not null,
	ldapServerAttributeRelId LONG not null primary key,
	companyId LONG,
	ldapServerId LONG,
	classNameId LONG,
	classPK LONG
);