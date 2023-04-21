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
echo "Generating tokens for provider"
read -p " providerId: " id
echo -e "\nThis will generate tokens for provider: $id \n"
read -p "Are you sure (Y/y to approve)? " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]
then
  echo -e "dev:"
  curl -XPUT -H "Authorization: Bearer `cat $PATH_DEV_KEY`" -H "Content-Type: application/json"  "https://pam-import-api.intern.dev.nav.no/stillingsimport/internal/providers/$id/token"
  echo -e "\nprod:"
  curl -XPUT -H "Authorization: Bearer `cat $PATH_PROD_KEY`" -H "Content-Type: application/json"  "https://pam-import-api.intern.nav.no/stillingsimport/internal/providers/$id/token"
  echo -e "\n"
else
  echo "Aborting, will not generate tokens for provider: $id"
fi
