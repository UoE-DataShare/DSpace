#!/usr/bin/env bash

LIVE=`/usr/bin/wget http://datashare.is.ed.ac.uk/healthcheck/ -q -O -`
LIVE_SSH=`/usr/bin/wget --no-check-certificate https://datashare.is.ed.ac.uk/healthcheck/ -q -O -`

if [ "$LIVE" != "$LIVE_SSH" ] ; then
    echo -e "HTTP: "$LIVE "\nHTTPS: "$LIVE_SSH | mailx -s "DataShare HTTP/HTTPS out of sync" george.hamilton@ed.ac.uk
fi

exit $?
