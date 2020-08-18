#!/bin/bash

echo "E-mail address:"
read email
echo "First name:"
read first
echo "Last name: "
read last
echo "UUN: "
read uun

# call DSpace create admin script
./dspace create-administrator -e $email -f $first -l $last -c en -p password

if [ $? = 0 ] ; then
    # now do DataShare specific stuff
    ./dspace dsrun uk.ac.edina.datashare.administer.CreateAdministrator -u $uun -e $email

    if [ $? != 0 ] ; then
        echo "Problem creating admin account."
    fi
else
    echo "Problem creating admin account."
fi

exit 0
