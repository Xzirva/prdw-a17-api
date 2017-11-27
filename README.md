Prerequisites for this code sample:
- Java 1.6 (+)
- Apache Maven (http://maven.apache.org)

Before running the sample, client_secrets.json must be populated with a
client ID and client secret. You can create an ID/secret pair at:

  https://code.google.com/apis/console

How to fetch data for tagging process
------------------------------------------------------------------------------
To team members (`huilingSHAOCH` and `louisste`):
To collect data for the text mining and tagging process of the videos:
  - Create a file named `api.log` at project root 
  - Change the data on your `api.log` file to `23-11-10 07:30:01` to fetch data on videos published since 2010
  - Set up the entire project environment : Teradata Studio, Aster VMs, activate cluster, (optional, IntelliJ/NoteBeans)
  - Open 2 Git Bash command line consoles or Mac OS command line consoles and run :
    -- `./prdw-a17-api.sh` on one of them
    -- `./prdw-a17-api-second.sh` on the other one
    
The process should take about an hour, resulting in about 5000 videos fetched and inserted into the database.

General purpose instructions to run project
------------------------------------------------------------------------------
To build this code sample from the command line, type:

  <code>mvn compile</code>

To run a code sample from the command line, enter the following:

  <code>mvn exec:java -Dexec.mainClass="FULL_CLASS_NAME"</code>

For samples that require arguments, also specify -Dexec.args, e.g.:

  <code>mvn exec:java -Dexec.mainClass="FULL_CLASS_NAME" -Dexec.args="arg1 arg2"</code>

We only use 'data' package and AsterDatabaseInterface to connect to the database
