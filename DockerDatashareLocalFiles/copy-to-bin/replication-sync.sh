#!/usr/bin/env bash

do_sync()
{
    RSYNC=/usr/bin/rsync
    SSH=/usr/bin/ssh
    RHOST=undefined
    RUSER=datashare
    FILES_DIR=/dspace/assetstore/

    $RSYNC -az -e $SSH $FILES_DIR $RUSER@$RHOST:$FILES_DIR
    exit $?
}


if [ "dev" = "prime" ] ; then
    LIVE=`/usr/bin/wget http://datashare.is.ed.ac.uk/healthcheck/ -q -O -`

    if [ "$LIVE" == "dlib-sidlaws-ubuntu.ucs.ed.ac.uk" ] ; then
        # on prime only sync if live
        do_sync
    else
        #echo "DataShare not live, not syncing"
        exit 1
    fi
elif [ "dev" = "backup" ] ; then
    # on backup machine just do it
    do_sync
else
    echo "Nowhere to sync to for dev"
    exit 1
fi
