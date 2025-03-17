#!/bin/bash

CURRENT_DIR_NAME=$(dirname ${BASH_SOURCE[0]})

echo CURRENT_DIR_NAME=${CURRENT_DIR_NAME}

source ${CURRENT_DIR_NAME}/../../../env/common.sh

function ldap_set_up {
	###########################################################################
	# Start: steps can be removed with next CI release
	###########################################################################

	local ciSlapdLdifLocation="/usr/local/etc/openldap/slapd.ldif"
#	local modifiedSlapdLdif="${CURRENT_DIR_NAME}/modifiedSlapd.ldif"

#	cp ${modifiedSlapdLdif} ${ciSlapdLdifLocation}

	echo "displaying slapd.ldif:"
	echo "$(<ciSlapdLdifLocation)"
	echo or
	cat ${ciSlapdLdifLocation}

	if [ -d /usr/local/etc/slapd.d ]
	then
		echo "slapd.d exists"
	else
		echo "slapd.d does not exist, making dir now"

		mkdir /usr/local/etc/slapd.d

		echo "Performing slapadd:"
		/usr/local/sbin/slapadd -n 0 -F /usr/local/etc/slapd.d -l ${ciSlapdLdifLocation}
	fi

	###########################################################################
	# End: steps can be removed with next CI release
	###########################################################################

	echo "Starting slapd:"
	/usr/local/libexec/slapd -F /usr/local/etc/slapd.d

	if [ $? -ne 0 ]; then
		echo "Command failed with exit status $?"
	else
		echo "Command succeeded"
	fi

	local exampleCompanyLdif="${CURRENT_DIR_NAME}/exampleCompany.ldif"

	ldapadd -cx -D "cn=admin,dc=example,dc=com" -w "secret" -f ${exampleCompanyLdif}

	if [ $? -ne 0 ]; then
		echo "Command failed with exit status $?.  Let's try removing the *.default files first"

		rm -f ./usr/local/etc/openldap/*.default

		ldapadd -cx -D "cn=admin,dc=example,dc=com" -w "secret" -f ${exampleCompanyLdif}

			if [ $? -ne 0 ]; then
				echo "didn't work that time either"
			else
				echo "Command succeeded that time"
			fi
	else
		echo "Command succeeded"
	fi

	local adminLdif="${CURRENT_DIR_NAME}/admin.ldif"

	ldapadd -cx -D "cn=admin,dc=example,dc=com" -w "secret" -f ${adminLdif}

	if [ $? -ne 0 ]; then
		echo "Command failed with exit status $?"
	else
		echo "Command succeeded"
	fi

	local addUsersLdif="${CURRENT_DIR_NAME}/addUsers.ldif"

	ldapadd -cx -D "cn=admin,dc=example,dc=com" -w "secret" -f ${addUsersLdif}

	if [ $? -ne 0 ]; then
		echo "Command failed with exit status $?"
	else
		echo "Command succeeded"
	fi

	local addGroupsLdif="${CURRENT_DIR_NAME}/addGroups.ldif"

	ldapadd -cx -D "cn=admin,dc=example,dc=com" -w "secret" -f ${addGroupsLdif}

	if [ $? -ne 0 ]; then
		echo "Command failed with exit status $?"
	else
		echo "Command succeeded"
	fi
}

function main {
	default_set_up

	ldap_set_up
}

main "${@}"