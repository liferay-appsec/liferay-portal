create index IX_86A4C3B7 on PasswordEntry (userId);
create index IX_F3037531 on PasswordEntry (uuid_[$COLUMN_LENGTH:75$], companyId);

create index IX_B566BA6E on PasswordHashGenerator (createDate);
create index IX_A63F69A4 on PasswordHashGenerator (uuid_[$COLUMN_LENGTH:75$], companyId);

create index IX_A48844E4 on PasswordMeta (createDate);
create index IX_717D8C5C on PasswordMeta (passwordEntryId);
create index IX_76B0C39A on PasswordMeta (uuid_[$COLUMN_LENGTH:75$], companyId);