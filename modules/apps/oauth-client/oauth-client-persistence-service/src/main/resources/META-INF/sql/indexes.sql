create unique index IX_79511A2C on OAuthClientASMetadata (companyId, issuer[$COLUMN_LENGTH:128$]);
create index IX_3FC42679 on OAuthClientASMetadata (companyId, userId);

create unique index IX_977E7B9A on OAuthClientEntry (companyId, asMetadataIssuer[$COLUMN_LENGTH:128$], clientId[$COLUMN_LENGTH:128$]);
create index IX_3752448C on OAuthClientEntry (companyId, userId);