# SwordfishSync


## Compiling from source

```
git clone https://github.com/jsloane/SwordfishSync
cd SwordfishSync
mvn clean package
```

## Installation

### Prerequisites

Tomcat
MySql/MariaDB

### MySQL/MariaDB

Create database user

```
CREATE USER 'swordfishsync' IDENTIFIED BY 'swordfishsync';
GRANT ALL ON swordfishsyncdb.* TO 'swordfishsync';
flush privileges;
```

### Tomcat WAR deployment

Copy the sfs-client.war and sfs-server.war files to the tomcat webapps directory, or upload to tomcat using the tomcat manager.
Refer to https://tomcat.apache.org/tomcat-8.0-doc/manager-howto.html

Access the service at <host>:<port>/sfs-client
