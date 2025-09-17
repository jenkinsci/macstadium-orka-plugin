# Troubleshooting

When troubleshooting Jenkins issues, start by enabling verbose logging for the plugin by following these steps: 

1. Go to `Manage Jenkins` -> `System Log`
1. Click `Add recorder` and provide a name
1. Click `Create` and `Add` for Loggers
1. Add the main Orka namespace `io.jenkins.plugins.orka`
1. Set Log Level to `All`

The logs will provide any relevant information. Note that enabling verbose logs may impact the Jenkins controller performance, so this should be disabled if not needed.

## Common Issues

### There are jobs waiting, but no agent is created

The plugin handles VM deployment and deletion, but agent scheduling remains under Jenkins control. Jenkins manages the provisioning or termination of agents based on current workload demands and prevents over-provisioning based on load analysis.

The plugin offers an optional `No Delay Provisioning` feature that bypasses Jenkins standard scheduling logic. While this feature immediately spins up agents when a job requests them, enabling this feature may result in unexpected behavior.

### The plugin cannot connect to the cluster

Ensure the host the controller is running on can connect to the cluster
