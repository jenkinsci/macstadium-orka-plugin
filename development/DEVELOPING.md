# Development Guide

This guide provides information about how to build, package, and run the plugin locally.

## Table of Contents

- [Build Requirements](#build-requirements)
- [Building, Packaging, and Testing](#building-packaging-and-testing)
- [Running the Plugin Locally](#running-the-plugin-locally)
- [Entry Points](#entry-points)
- [User Interface](#user-interface)
  - [UI Bindings](#ui-bindings)
- [Persisting State](#persisting-state)
- [Changing the Supported Jenkins Version](#changing-the-supported-jenkins-version)

## Build Requirements

- [Maven 3][maven]
- JDK 8 (the minimum Jenkins LTS we currently support requires JDK 11)

## Building, Packaging, and Testing

To **build** the plugin and run [Checkstyle][checkstyle] validation:

```bash
mvn install
```

To **package** the plugin (includes building and validation), producing an .hpi file in the `target` folder:

```bash
mvn package
```

To **run** unit tests:

```bash
mvn test
```

To **run** Checkstyle validation only:

```bash
mvn validate
```

## Running the Plugin Locally

To run the plugin locally with a Jenkins controller running the minimum supported Jenkins version (specified in [pom.xml](../pom.xml)):

```bash
mvn hpi:run
```

Access the Jenkins controller at http://localhost:8080.

## Entry Points

The plugin has two entry points corresponding to its functionalities:

Permanent Agents: [OrkaProvisionedAgent.java](../src/main/java/io/jenkins/plugins/orka/OrkaProvisionedAgent.java)
Ephemeral Agents (Clouds): [OrkaCloud.java](../src/main/java/io/jenkins/plugins/orka/OrkaCloud.java)

## User Interface

The UI is built using Jelly files. Each class with a UI has a folder in the resources section named after the class.

### UI Bindings

Proper naming of properties and methods is crucial, as bindings work by convention. Mismatches can cause bindings to fail.

Descriptors are the entry point for any UI troubleshooting. They are usually inner classes within the class they describe. See [OrkaCloud.java](../src/main/java/io/jenkins/plugins/orka/OrkaCloud.java) as an example.

Jenkins builds the UI using a combination of Descriptors and Describables:

1. If there is no describable (no cloud is yet created), default values are taken from the Descriptor.
1. If there is a describable, values are taken from it.

Common naming conventions:

- doCheckX: Validate property X when it changes.
- doFillXItems: Provide values for dropdowns, lists, etc.
- doTestX: Run a manual check (e.g., test connection) for property X.

**IMPORTANT**: Any Descriptor method that accepts credentials as parameters must be annotated with @POST to prevent security issues.

## Persisting State

Jenkins persists state by serializing classes (e.g., clouds, agents) to XML files and deserializing them on load. To exclude a property from serialization, mark it as transient.

## Changing the Supported Jenkins Version

**IMPORTANT**: Updating the minimum required Jenkins version is a breaking change. Ensure no customers are running an unsupported version (consult with Product).

We only support [Jenkins LTS versions][lts]. The minimum supported version is specified in the [pom.xml](pom.xml) file under `<jenkins.version>`. For example:

```xml
<jenkins.version>2.204.1</jenkins.version>
```

[lts]: https://www.jenkins.io/changelog-stable/
