create table OpenIdConnectSession (
	mvccVersion LONG default 0 not null,
	openIdConnectSessionId LONG not null primary key,
	companyId LONG,
	modifiedDate DATE null,
	accessToken TEXT null,
	idToken TEXT null,
	providerName VARCHAR(75) null,
	refreshToken TEXT null
);