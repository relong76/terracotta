TERRACOTTA
========

[![Quality gate](https://sonarcloud.io/api/project_badges/measure?project=terracotta-education_terracotta&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=terracotta-education_terracotta)

Build
-----
This will produce a terracotta-X.X.jar file in the *target* directory

    mvn install 

note, to avoid some test errors if those happen, try

    mvn install -DskipTests=true

to skip the yarn build, -Dskip.yarn can be used

Quick Run
---------
You can run the app in place to try it out without having to install and deploy a servlet container.

    mvn clean install -DskipTests=true spring-boot:run

Then go to the following default URL:

    https://localhost:9090/

NOTE: To run it and connect it to real LMSs it is recommended to run it in an accessible server 
with a valid certificate launching the jar file with the right configuration files.

Customizing
-----------
Use the application.yml to control various aspects of the Spring Boot application (like setting up your own database
connection). The example file has some sections with self-explanatory titles. It is recommended to 
use a properties/YAML file external to the jar to avoid storing sensitive values in your code: 

```--spring.config.location=/home/yourhomefolder/application-local.properties```


Adding Terracotta to Canvas
---------

A Canvas admin can add Terracotta in two ways, both built from Terracotta's own address
(`application.url`), name (`application.name`) and description (`application.description`):

- **1.3 JSON URL** (or **1.3 by Paste JSON Code**): create an LTI developer key and give Canvas
  `https://YOUR_TERRACOTTA_HOST/lti/canvas/config.json`, or paste what that address shows. As today, an
  admin then adds the platform deployment in Terracotta.
- **1.3 Dynamic Registration**: give Canvas `https://YOUR_TERRACOTTA_HOST/lti/canvas/registration`.
  Terracotta registers itself and saves the platform deployment. Its launches work right away when
  the Canvas's address belongs to an approved account in `lms_approved_account`. From any other
  address, the registration is kept but waits: Terracotta adds an unapproved account for it, and
  refuses its launches until the account is approved.

Either way, Terracotta's access to the Canvas API (a separate Canvas API developer key) is still set
up by hand.

### Approved LMS accounts

An account lists every address its LMS is reached at (`lms_approved_account_host`), so a school's own
domain and its `instructure.com` one both work. Manage accounts through the admin API, with the
`terracotta.admin` login:

- `GET /config/lms-approved-accounts` lists them, waiting ones included.
- `POST /config/lms-approved-accounts` adds one:

    ```
    { "name": "Indiana University", "lmsConnector": "CANVAS", "approved": true, "hosts": ["iu.instructure.com", "canvas.iu.edu"] }
    ```

- `PUT /config/lms-approved-accounts/{uuid}` changes one, given its `name`. Set `approved` to `true` to approve a
  waiting account, or back to `false` to stop its launches. Leave out `hosts` to keep the ones it has.

The first launch after a registration records the LMS's own ID for the account (`platform_guid`,
Canvas's `tool_platform` guid), which stays the same when its address changes. Platform deployments
set up by hand have no account, and launch as before.

Creating the database
---------
Connect to your mysql server and use your values on xxDATABASENAMExxx, xxxuserNamexxx, xxxPasswordxxx Set the right
values in the properties file.

mysql> create database xxDATABASENAMExxx DEFAULT CHARACTER SET utf8 ; Query OK, 1 row affected (0.00 sec)

mysql> create user 'xxxuserNamexxx'@'%' identified by 'xxxPasswordxxx'; Query OK, 0 rows affected (0.00 sec)

mysql> grant all on terracotta.* to 'terracotta'@'localhost'; Query OK, 0 rows affected (0.00 sec)


Creating database migration scripts
---------

Use the following steps to mostly automate creation of a liquibase database migration script. You will need [Liquibase](https://www.liquibase.com/) installed. The following was tested with Liquibase version 4.6.2.

1. Create a fresh MySQL database using Docker.

    ```
    docker run --name mysql57 -p 3406:3306 -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=terracotta -d mysql:5.7
    ```

2. Configure your local application.yml file to connect to this Docker MySQL database. Also set the `ddl-auto` setting to `update`.

    application-local.properties
    ```
    ...
    spring.jpa.hibernate.ddl-auto=update
    # spring.jpa.hibernate.ddl-auto=validate

    # Local docker instance
    spring.datasource.url=jdbc:mysql://localhost:3406/terracotta
    spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver
    spring.datasource.username=root
    spring.datasource.password=root
    ```

3. Assuming you've already made your code modifications to the JPA entities, start up your Terracotta application locally.

    ```
    java -jar target/terracotta-0.1.jar --spring.config.location=../application-local.properties
    ```

   This will bring your Docker MySQL database up-to-date with your JPA mappings.

   Once Terracotta has successfully started up, you'll see logged to the
   console:

   > `Started Terracotta in XX.XXX seconds`

   You can go ahead and shut it down now.

4. Create a liquibase configuration file that connects to your development environment database and uses your Docker MySQL database as the reference database.

    liquibase.properties:
    ```
    # DEV database
    url:  jdbc:mysql://localhost:3309/terracotta
    username: terracotta
    password: YOUR_TERRACOTTA_USER_PASSWORD
    classpath:  mysql-connector-java-8.0.27.jar

    # docker database, created with:
    #       docker run --name mysql57 -p 3406:3306 -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=terracotta -d mysql:5.7
    referenceUrl: jdbc:mysql://localhost:3406/terracotta
    referenceUsername: root
    referencePassword: root

    liquibase.hub.mode=off
    ```

    Note: above I used SSH port forwarding to forward port 3309 locally to the
    development database server and port. Also, you'll need to [download a
    mysql-connector-java
    jar](https://mvnrepository.com/artifact/mysql/mysql-connector-java) file.

5. Run liquibase to generate a diff between your development database and the Docker MySQL database.

    For example:

    ```
    liquibase --changeLogFile=terracotta/src/main/resources/db/changelog/2022.05/26-01-changelog.xml diffChangeLog
    ```

    The naming format is to put the changelog file in a directory named
    `YYYY.MM` and name it `DD-NN-changelog.xml`, where `YYYY` is the 4 digit
    year, `MM` is the two digit month, `DD` is the two digit day, and `NN` is a
    two digit incrementing counter for the changelog, starting with `01`, in
    case there are multiple changelogs generated in a given day. In the above
    example, the changelog was the first one generated on May 26, 2022.

6. Inspect the generated changelog file. You will likely need to fix up a few things, such as:

    - to help make the scripts database independent, use the following generic data types:
        - BOOLEAN
        - CURRENCY
        - UUID
        - CLOB
        - BLOB
        - DATE
        - DATETIME
        - TIME
        - BIGINT
    - give foreign key and other constraints proper names 

7. Add the changelog file to changelog-master.xml.

8. To verify your migration script, stop and remove your Docker MySQL container
   and create a fresh new one. Also, set `ddl-auto` to `validate`, then start up
   your application.

    ```
    docker stop mysql57
    docker rm mysql57
    docker run --name mysql57 -p 3406:3306 -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=terracotta -d mysql:5.7
    ```

    application-local.properties:
    ```
    ...
    # spring.jpa.hibernate.ddl-auto=update
    spring.jpa.hibernate.ddl-auto=validate

    # Local docker instance
    spring.datasource.url=jdbc:mysql://localhost:3406/terracotta
    spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver
    spring.datasource.username=root
    spring.datasource.password=root
    ```

    ```
    java -jar target/terracotta-0.1.jar --spring.config.location=../application-local.properties
    ```

    A successful application start will indicate that the migration scripts are
    complete and up-to-date with the JPA mappings.
