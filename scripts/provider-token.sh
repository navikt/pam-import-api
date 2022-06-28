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
echo "Create tokens for provider"
read -p "provider-id:" id
echo -e "this will generate tokens for provider $id"
read -p "Are you sure? " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]
then
  echo -e "dev:"
  curl -XPUT -H "Authorization: Bearer `cat $PATH_DEV_KEY`" -H "Content-Type: application/json"  "https://pam-import-api.dev-gcp.nais.io/stillingsimport/internal/providers/$id/token"
  echo -e "\nprod:"
  curl -XPUT -H "Authorization: Bearer `cat $PATH_PROD_KEY`" -H "Content-Type: application/json"  "https://pam-import-api.prod-gcp.nais.io/stillingsimport/internal/providers/$id/token"
  echo -e "\n"
fi
