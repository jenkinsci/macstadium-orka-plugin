# Release guide

This guide explains how to release the Orka Jenkins plugin.

## Table of Contents

- [Releasing the Plugin](#releasing-the-plugin)
  - [Future Release Improvements](#future-release-improvements)

## Releasing the Orka Jenkins plugin

The plugin is released using GitHub Actions and Maven.

### Release process

1. Do not update the plugin version manually unless you are incrementing the `major` version. `Minior` versions are handled automatically.
1. Remove Branch Protection: Temporarily disable branch protection in GitHub, as the release action automatically updates the version in ```pom.xml``` and pushes to the main branch.
1. Run the Release Action: Trigger the GitHub Action to release the plugin.
1. Restore Branch Protection: Re-enable branch protection after the release is successful.
1. Publish a new GitHub release:
   1. Use [Release Drafter] to automatically generate release notes
   1. Validate the release notes
   1. Validate the release version and tag
   1. Publish the release

## Future improvements

The Jenkins infrastructure allows for continuous integration and continuous deployment (CI/CD), which eliminates the need to manually change branch permissions. Learn more about this [here][cicd].

[cicd]: https://www.jenkins.io/doc/developer/publishing/releasing-cd/
[Release Drafter]: https://github.com/marketplace/actions/release-drafter
