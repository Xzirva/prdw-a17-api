Prerequisites for this code sample:
- Java 1.6
- Apache Maven (http://maven.apache.org)

Before running the sample, client_secrets.json must be populated with a
client ID and client secret. You can create an ID/secret pair at:

  https://code.google.com/apis/console

To build this code sample from the command line, type:

  <code>mvn compile</code>

To run a code sample from the command line, enter the following:

  <code>mvn exec:java -Dexec.mainClass="FULL_CLASS_NAME"</code>

For samples that require arguments, also specify -Dexec.args, e.g.:

  <code>mvn exec:java -Dexec.mainClass="FULL_CLASS_NAME" -Dexec.args="arg1 arg2"</code>

We only use 'data' package and AsterDatabaseInterface to connect to the database