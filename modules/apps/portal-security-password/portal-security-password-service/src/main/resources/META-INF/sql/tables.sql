create table PasswordEntry (
	mvccVersion LONG default 0 not null,
	uuid_ VARCHAR(75) null,
	passwordEntryId LONG not null primary key,
	userId LONG,
	companyId LONG,
	createDate DATE null,
	hash VARCHAR(75) null
);

create table PasswordHashGenerator (
	mvccVersion LONG default 0 not null,
	uuid_ VARCHAR(75) null,
	passwordHashGeneratorId LONG not null primary key,
	companyId LONG,
	createDate DATE null,
	hashGeneratorName VARCHAR(75) null,
	hashGeneratorMeta VARCHAR(75) null
);

create table PasswordMeta (
	mvccVersion LONG default 0 not null,
	uuid_ VARCHAR(75) null,
	passwordMetaId LONG not null primary key,
	companyId LONG,
	createDate DATE null,
	passwordEntryId LONG,
	salt VARCHAR(75) null,
	pepperId VARCHAR(75) null
);