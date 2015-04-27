###############################################
#
# Building and testing the BiSciCol code
#
###############################################

To build the main code (not the test code) and the .war file, run:
ant
-OR-
ant all.

To build and run the unit tests, run:
ant test.

###############################################
#
# Deploying from commandline:
#
###############################################

sudo cp dist/biscicol.war /usr/local/src/glassfish3/glassfish/domains/domain1/autodeploy/

###############################################
#
# Loading Virtuoso Data
#
###############################################
Servers running at:
http://chignik.berkeley.edu:8890/
http://flmnh-biscicol.flmnh.ufl.edu:8890/


# Load inferencing rules for "relies"
DB.DBA.ttlp ('
 @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
 <http://biscicol.org/terms/index#depends_on> rdfs:subPropertyOf <http://biscicol.org/terms/index#relies> .
 <http://biscicol.org/terms/index#alias_of> rdfs:subPropertyOf <http://biscicol.org/terms/index#relies> .
 <http://biscicol.org/terms/index#derives_from> rdfs:subPropertyOf <http://biscicol.org/terms/index#relies> .
 ', '', 'http://biscicol.org/inference/rules#');

rdfs_rule_set ('http://biscicol.org/inference/property_rules1', 'http://biscicol.org/inference/rules#');

define input:inference "http://biscicol.org/inference/property_rules1"

Copy biscicolsettings.template to the "classes" directory in your build directory.  Make necessary changes to the properties file (e.g., virtuoso username/password).

./runLoad.sh

###############################################
#
# System Installation
#
###############################################
# Installation for  CentOS
yum install ant
yum install svn 
yum install java-1.6.0
yum install unzip
yum install openssl-devel
yum install openssh-clients
yum install gcc
yum install redland-virtuoso virtuoso-opensource{,-utils}
yum install virtuoso-opensource-conductor
yum install virtuoso-opensource-apps

# Note on Java-- i had to download the Sun Java instead of the yum java to get it to work

# not sure if i actually need this or not
# leaving off for now
#yum install httpd 

# add ports
add 8890, 1111, 80, 4848, 8080 to /etc/sysconfig/iptables
# add the following line to forward 8080 to 80
-A PREROUTING -i eth0 -p tcp -m tcp --dport 80 -j DNAT --to-destination :8080
/sbin/service iptables restart


###############################################
#
#  Glassfish Stuff
#
###############################################
# Install Glassfish
download to /usr/local/src/
extract
/usr/local/src/glassfish3/glassfish/bin/asadmin change-admin-password
(other useful notes on glassfish=http://www.davidghedini.com/pg/entry/how_to_install_glassfish_3)

# updating glassfish
./pkg image-update

# bcid application is the context root
change contextroot of bcid Application in Glassfish console to "/"

#starting and stopping
cd {$glassfishRoot}/bin/stopserv
cd {$glassfishRoot}/bin/startserv

# change master password
./asadmin
  -> change-admin-password (admin/admin is default)
# enabling secure admin
./asadmin
  -> enable-secure-admin

###############################################
#
# Deploying and getting code
#
###############################################
# Getting Code:
mkdir code
cd code

#biscicol
svn checkout https://biscicol.googlecode.com/svn/trunk/ biscicol --username jdeck88@gmail.com
mkdir dist
ant
sudo cp dist/biscicol.war /usr/local/src/glassfish3/glassfish/domains/domain1/autodeploy/

#triplifier
svn checkout https://triplifier.googlecode.com/svn/trunk/ triplifier --username jdeck88@gmail.com
mkdir dist
ant
sudo cp dist/triplifier.war /usr/local/src/glassfish3/glassfish/domains/domain1/autodeploy/


###############################################
#
#  Start Up
#
###############################################
As user jdeck:
cd /var/lib/virtuoso/db
virtuoso-t -df &
sudo /usr/local/src/glassfish3/glassfish/bin/startserv &


###############################################
# Mysql Installation, if desired-- this is for testing for now
# JBD 4/30/2012
###############################################

yum install mysql
yum install mysql-server
yum install mysql-devel
chgrp -R mysql /var/lib/mysql
chmod -R 770 /var/lib/mysql
service mysqld start
chkconfig mysqld on && service mysqld restart && chkconfig --list | grep mysqld
mysqladmin -u root password NEWPASSWORD

