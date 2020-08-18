#!/bin/bash

# remove uploads older than 1 week
for i in `find /dspace/upload/* -maxdepth 0 -type d -mtime +7 -print 2>/dev/null`;
    do echo -e "Deleting directory $i";
    rm -r $i;
done


DATASTORE=/var/datastore

# any log older than 2 weeks moved to archive on datastore
if [ -d "$DATASTORE" ]; then
    for i in `find /dspace/log/dspace.log* /dspace/log/checker.log* /dspace/log/solr.log* -type f -mtime +14 -print 2>/dev/null`;
    do
        mv $i $DATASTORE/datashare/logs/dspace/
    done

    for i in `find /home/vagrant/tomcat7/logs/localhost_access_log\.* -type f -mtime +14 -print 2>/dev/null`;
    do
        mv $i $DATASTORE/datashare/logs/tomcat/
    done

    for i in `find /var/log/httpd/error_log.* /var/log/httpd/access_log.* -type f -mtime +14 -print 2>/dev/null`;
    do
        cp $i $DATASTORE/datashare/logs/httpd/
        rm -f $i
    done
fi

exit 0
