#!/bin/bash
set -e
if [[ -z $PATH_PROD_KEY ]]
  then
    echo "PATH_PROD_KEY not defined!"
    exit 1
fi
if [[ -z $PATH_DEV_KEY ]]
  then
    echo "PATH_DEV_KEY not defined!"
    exit 1
fi
echo "Registering new import-api provider, please type in provider information:"
read -p " identifier (brukt som medium): " identifier
read -p " email: " email
read -p " phone: " phone
json='{ "identifier": "'$identifier'", "email": "'$email'", "phone": "'$phone'" }'
echo -e "\nThis will create a new provider in test and production using the json file:\n $json \n"
read -p "Are you sure (Y/y to approve)? " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]
then
  curl -POST -H "Authorization: Bearer `cat $PATH_DEV_KEY`" -H "Content-Type: application/json" -d "$json"  "https://pam-import-api.dev.intern.nav.no/stillingsimport/internal/providers"
  curl -POST -H "Authorization: Bearer `cat $PATH_PROD_KEY`" -H "Content-Type: application/json" -d "$json"  "https://pam-import-api.intern.nav.no/stillingsimport/internal/providers"
else
  echo "Aborting, will not create new provider"
fi
