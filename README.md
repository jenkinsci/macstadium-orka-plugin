# MacStadium Orka Jenkins Plugin

Jenkins Plugin working with MacStadium Orka.

The plugin provides two functionalities:  

* Provision agents in MacStadium per request - This is done via `Manage Jenkins` -> `Manage Nodes`
* Provision agents in MacStadium on-demand - Done via `Manage Jenkins` -> `Configure System` -> `Add Cloud`

## Build Requirements

* [Maven 3][maven]
* JDK 8

## Building, packaging and testing the plugin

To build the plugin use:  

    mvn install

This will run [checkstyle][checkstyle] validation and will build the plugin.

To package the plugin use:

    mvn package

This will run checkstyle validation, build and package the plugin.
It will produce an `hpi` file located in the `target` folder.

To run tests use:

    mvn test

If you want to run checkstyle only run:

    mvn validate

## Running the plugin locally

The easiest way to use Jenkins with the latest version of the plugin is to run:

    mvn hpi:run

This will boot a Jenkins master, package the plugin and install it. To run the Jenkins master open http://localhost:8080.

[maven]: http://maven.apache.org/
[checkstyle]: http://checkstyle.sourceforge.net/
