#!/bin/bash

if [ "$1" = "" ]
then
    >&2 echo "no keystore name specified"
    >&2 echo "usage: $0 <alias>"
    exit 1
fi

STORE_TYPE="pkcs12"
EXTENSION="jks"
STORE_PWD="${1}store"
KEY_PWD="${1}key"
KEY_ALIAS="$1"

if [ $STORE_TYPE = "pkcs12" ]
then
    EXTENSION="p12"
    KEY_PWD="$STORE_PWD" # same for pkcs12 created via keytool
fi

STORE="${1}.${EXTENSION}"

# generate certificate
keytool -genkeypair -alias "$KEY_ALIAS" -keyalg RSA -keysize 4096 -dname "CN=$1,O=POPJAVA" -storepass "$STORE_PWD" -keypass "$KEY_PWD" -storetype $STORE_TYPE -keystore "$STORE" -validity 9000


echo -e "KeyStore password is \e[1m$STORE_PWD\e[0m change it with \e[2mkeytool -keystore $STORE -storepasswd -new <newpass>\e[0m"
echo -e "Private Key password is \e[1m$KEY_PWD\e[0m change it with \e[2mkeytool -keystore $STORE -alias $KEY_ALIAS -keypasswd -new <newpass>\e[0m"


# extact certificate
keytool -export -alias "$KEY_ALIAS" -keystore "$STORE" -file "$KEY_ALIAS.cer" -rfc

echo "Information dump:"
echo " KEY_STORE = `readlink -e "$STORE"`"
echo " KEY_STORE_FORMAT = $STORE_TYPE"
echo " KEY_STORE_PWD = $STORE_PWD"
echo " KEY_STORE_PK_ALIAS = $KEY_ALIAS"
echo " KEY_STORE_PK_PWD = $KEY_PWD"
