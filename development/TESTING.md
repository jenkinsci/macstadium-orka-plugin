# Testing Guide

Prior to releasing a new version, ensure:

1. **Fresh Installation**: The plugin works correctly in a fresh Jenkins installation.
2. **Upgrade Compatibility**: The plugin works after an upgrade, and all user settings are preserved. This is crucial due to how Jenkins [stores and loads configurations](DEVELOPING.md#persisting-state). Common steps:
   1. Install the official version of the plugin
   1. Create a new cloud
   1. Upgrade the plugin
   1. Ensure the cloud settings are the same

## Running Unit Tests

Run:

```bash
mvn test
```

## Running Integration Tests Locally

**Note**: These tests are currently deprecated.

Please refer to the [integration tests README](integration-tests/README.md) for more information.
