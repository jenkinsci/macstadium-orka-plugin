# Troubleshooting

To troubleshoot any Jenkins problem, it is important to enable verbose logging for the plugin. To do that:

1. Go to `Manage Jenkins` -> `System Log`
1. Click `Add recorder` and provide a name
1. Click `Create` and `Add` for Loggers
1. Add the main Orka namespace `io.jenkins.plugins.orka`
1. Set Log Level to `All`

The logs will provide any relevant information for the plugin. Note, that enabling verbose logs may impact the Jenkins controller performance, so it should be disabled if not needed.

## Common Issues

# There are jobs waiting, but no agent is created

Agent scheduling is not our responsibility. The plugin is not responsible for agent creation. It is reponsbile for VM deployment/deletion.  
Jenkins decides when to spin up/down agents based on the current load. It ensures that agents are not overprovisioned.

However, there is a functionality in the plugin people can enable called `No Delay Provisioning`. Using it we are skipping Jenkins scheduling logic, which could lead to unexpected behavior, but spins up agents the moment a job requests them.

# The plugin cannot connect to the cluster

Ensure the host where the controller runs can connect to the cluster
