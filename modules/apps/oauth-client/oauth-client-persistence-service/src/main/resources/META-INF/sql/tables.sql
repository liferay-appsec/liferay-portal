create table OAuthClientASMetadata (
	mvccVersion LONG default 0 not null,
	oAuthClientASMetadataId LONG not null primary key,
	companyId LONG,
	userId LONG,
	createDate DATE null,
	modifiedDate DATE null,
	discoveryEndpoint VARCHAR(256) null,
	issuer VARCHAR(128) null,
	metadataJSON TEXT null
);

create table OAuthClientEntry (
	mvccVersion LONG default 0 not null,
	oAuthClientEntryId LONG not null primary key,
	companyId LONG,
	userId LONG,
	createDate DATE null,
	modifiedDate DATE null,
	asMetadataIssuer VARCHAR(128) null,
	clientId VARCHAR(128) null,
	infoJSON TEXT null,
	requestParamsJSON TEXT null
);