# SwordfishSync

SwordfishSync is a web application that adds torrents from RSS feeds to transmission bittorrent service.

## Installation

### Prerequisites

Tomcat

MySql/MariaDB


CentOS 7
```
sudo yum install tomcat mariadb-server
```
Ubuntu
```
sudo apt-get install tomcat mariadb-server
```

### MySQL/MariaDB

Database setup

```
sudo mysql_secure_installation
```

Log in with the root account to configure the database.

```
sudo mysql -uroot -p
```

Create database, user, flush privileges and exit

```
CREATE DATABASE swordfishsyncdb;
CREATE USER 'swordfishsync'@'localhost' IDENTIFIED BY 'swordfishsync';
GRANT ALL ON swordfishsyncdb.* TO 'swordfishsync'@'localhost';
flush privileges;
exit
```

### Compiling from source

Install development tools

CentOS 7
```
sudo yum install -y java-1.8.0-openjdk-devel
#sudo yum install maven
# note - need to manually install maven 3.3.3 or later, as maven 3.0.5 is too old
```
Ubuntu
```
sudo apt-get install openjdk-8-jdk
sudo apt-get install maven
```

Download source and compile

```
git clone https://github.com/jsloane/SwordfishSync
cd SwordfishSync
mvn clean package
```

### Tomcat WAR deployment

Copy the sfs-client/target/sfs-client.war and sfs-server/target/sfs-server.war files to the tomcat webapps directory, or upload to tomcat using the tomcat manager.
Refer to https://tomcat.apache.org/tomcat-8.0-doc/manager-howto.html

```
TODO
```

Access the service at \<host\>:\<port\>/sfs-client
