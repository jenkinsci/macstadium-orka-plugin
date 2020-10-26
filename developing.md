# Developing the plugin

This article provides information about how to build, package, or run the plugin locally. For usage information, see the Jenkins plugin [tutorial][tutorial].

The plugin provides two functionalities:

- Provision permanent agents in MacStadium - Via `Manage Jenkins` -> `Manage Nodes`
- Provision ephemeral agents in MacStadium - Via `Manage Jenkins` -> `Configure System` -> `Add a new cloud`

## Build requirements

- [Maven 3][maven]
- JDK 8

## Building, packaging and testing the plugin

To build the plugin, run:

    mvn install

This runs [checkstyle][checkstyle] validation and builds the plugin.

To package the plugin, run:

    mvn package

This runs checkstyle validation, build and package the plugin.
It produces an `hpi` file located in the `target` folder.

To run tests, run:

    mvn test

To run checkstyle, run:

    mvn validate

## Running the plugin locally

To use the plugin locally, run:

    mvn hpi:run

This boots a Jenkins master, package the plugin and install it. To run the Jenkins master open http://localhost:8080.

## Running integration tests locally

Please, refer to the [integration tests README](https://github.com/jenkinsci/macstadium-orka-plugin/tree/master/integration-tests).

[maven]: http://maven.apache.org/
[checkstyle]: http://checkstyle.sourceforge.net/
[tutorial]: https://wiki.jenkins.io/display/JENKINS/Plugin+tutorial
