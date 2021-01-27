# # Edwin Meck - QA
  # Coding Challenge - Marvel API - v1 (Part One)

## Running The Tests
1- RestAssured framework has been used to create the api test due to its easy of use and rich features". 
2- Private and public keys that are required for authentication must be placed in the file "/src/test/resources/test.properties" as key value pairs.

The Tests can be run in two ways:

### IDE
The first is through the IDE; in Intellij, this can be done by many ways, such as pressing the Green Arrow next to the Test, or right clicking the Test in the Project Structure.

### Command Line
The second way of running is via the Command Line; If you don't have Maven Installed, this needs to be installed on your machine, and the location set as an Environment Variable on your machine. In addition, you must be using a JDK (as opposed to JRE), and have your JAVA_HOME Environment Variable pointing to that location. Maven will download all the required dependencies

Once done, the tests can be run simply via the following command from the Project Root Directory:

````````````
mvn test
````````````
