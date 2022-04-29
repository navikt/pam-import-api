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
echo "Register new IMPORT-API provider, please type in correct information"
read -p "identifier (brukt som medium):" identifier
read -p "email:" email
read -p "phone:" phone
json='{ "identifier": "'$identifier'", "email": "'$email'", "phone": "'$phone'" }'
echo -e "this will create a new provider on both test and production using the json file:\n $json \n"
read -p "Are you sure? " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]
then
  curl -POST -H "Authorization: Bearer `cat $PATH_DEV_KEY`" -H "Content-Type: application/json" -d "$json"  "https://pam-import-api.dev-gcp.nais.io/stillingsimport/internal/providers"
  curl -POST -H "Authorization: Bearer `cat $PATH_PROD_KEY`" -H "Content-Type: application/json" -d "$json"  "https://pam-import-api.prod-gcp.nais.io/stillingsimport/internal/providers"
fi
