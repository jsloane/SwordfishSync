#!/bin/bash

#################
# Configuration 
#################

SEARCH_DIR_STRING="/directory"
REPLACE_DIR_STRING="smb://mediahost/directory"
KODI_JSON_RPC="http://kodihost/jsonrpc"

#################

for i in "$@"
do
	case $i in
		# get directory argument
		directory=*)
		DIRECTORY="${i#*=}"
		shift
		;;
		*)
		# unknown option
		;;
	esac
done

if [ -z $DIRECTORY ]; then
	echo "Required directory argument missing."
	echo
	echo "Usage: ./updateKodiLibrary.sh directory=/directory/to/update"
	echo
	exit 2
fi

echo "Directory supplied:  ${DIRECTORY}"

DIRECTORY=${DIRECTORY/$SEARCH_DIR_STRING/$REPLACE_DIR_STRING}

echo "Directory to update: ${DIRECTORY}"
echo
echo "Kodi response:"

curl --data-binary "{ \"jsonrpc\": \"2.0\", \"method\": \"VideoLibrary.Scan\", \"id\": \"updateKodiLibrary.sh\", \"params\": {\"directory\": \"${DIRECTORY}\"}}" -H 'content-type: application/json;' $KODI_JSON_RPC

echo