# Orka by MacStadium Jenkins Plugin

[![Jenkins](https://ci.jenkins.io/job/Plugins/job/macstadium-orka-plugin/job/master/badge/icon)](https://ci.jenkins.io/job/Plugins/job/macstadium-orka-plugin/job/master/)
[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/macstadium-orka.svg)](https://plugins.jenkins.io/macstadium-orka)

This is a Jenkins plugin to support both permanent and ephemeral Jenkins agents on [Orka by MacStadium][orka].

## Prerequisites

- [Orka by MacStadium][orka] 3.0+ environment - Used to deploy new VMs used as Jenkins agents. If you are running Orka 2.x, use version 1.35 of the plugin.
- VPN connection to the Orka environment - Each Orka environment is behind a firewall. Your Jenkins master must have visibility to the environment.

## Permanent Agents

The plugin allows you to create permanent agents, running on [Orka by MacStadium][orka]. The plugin first deploys a new VM in an Orka environment and then uses SSH to connect to it.

### Usage

To create a permanent agent:

- Go to `Manage Jenkins` → `Manage Nodes and Clouds`  
  **Note** If you are using a version earlier than Jenkins 2.205 go to `Manage Jenkins` → `Manage Nodes`
- Select `New Node`
- Provide a `Node name`
- Select `Agent running under Orka by MacStadium`
- Click `OK`

    <img src="images/new-agent.png" width="600"/>

- Configure the agent you want to launch

  Provide values for the following Orka properties:

  - Orka Token - The service account token used to connect to the Orka environment. Created by running orka3 sa <name> token
  - Orka Endpoint - The endpoint used by the plugin to connect to the Orka environment
  - Namespace - The namespace used to deploy VMs to
  - Node - The Orka node which the agent will be deployed on
  - Public Host (Optional) - Public node address as provided by the MacStadium team. Leave empty if you want to use the default node address. This addressed is used to connect to the Orka VM.  
    **Note** The public node addresses are provided by MacStadium.
  - Image - The image used to deploy a VM from
  - \# of CPUs - The number of CPUs of the VM
  - Memory - The memory of the VM
  - Tag (Optional) - When specified, the VM is preferred to be deployed to a node marked with this tag
  - Tag Required (Optiona) - When set to true, the VM is required to be deployed to a node marked with this tag
  - Use Net Boost (Optional) - When checked, improves the network performance of Intel-based VMs. Required for macOS Ventura Intel-based VMs. NOTE: Applicable only to macOS BigSur and later
  - Use GPU Passthrough - When checked, enables the VM to use the GPU available on the node. NOTE: GPU Passthrough must be enabled for the cluster
  - VM Credentials - The credentials used to SSH to the deployed VM
  - Name Prefix (Optional) - The deployed VM name starts with the specified prefix

- Click Save

## Ephemeral Agents

The plugin allows Jenkins to create ephemeral agents, running on [Orka by MacStadium][orka]. The plugin first deploys a new VM in an Orka environment and then uses SSH to connect to it.

A new agent is automatically created by Jenkins if the build load is too high and there are no available executors. Once the VM has been idle for a given amount of time, Jenkins terminates it automatically and all resources are cleaned up.

### Usage

To configure:

- Go to `Manage Jenkins` → `Manage Nodes and Clouds` → `Configure Clouds` → `Add a new cloud`  
  **Note** If you are using a version earlier than Jenkins 2.205 go to `Manage Jenkins` → `Configure System` → `Add a new cloud`
- Select `Orka Cloud`
- Configure the cloud by providing values for:
  - Name of this Cloud - The name of the cloud
  - Orka Token - The service account token used to connect to the Orka environment. Created by running orka3 sa <name> token
  - Orka Endpoint - The endpoint used by the plugin to connect to the Orka environment
  - Click `Advanced` to configure also:
    - Max Jenkins Agents Limit - The maximum number of Orka VMs that can be created by that cloud instance. This allows you to better manage your Orka resources.
    - Deployment Timeout (sec) - The time after which the request for new Orka VM will timeout. Defaults to 600 seconds (10 minutes).
  - Node Mappings (Optional) - Overwrite the default host address used to connect to an Orka VM. By default, the plugin uses the private node address. Provide a mapping to a public host address if you wish to change this behavior. This option is available by clicking `Advanced`
    **Note** The public node addresses are provided by MacStadium.
- Click Add Orka Template. An Orka template is the agent template, Jenkins will use to create a new agent.
- Fill the following values:

  - Image - The image used to deploy a VM from
  - \# of CPUs - The number of CPUs of the VM
  - Memory - The memory of the VM
  - Tag (Optional) - When specified, the VM is preferred to be deployed to a node marked with this tag
  - Tag Required (Optiona) - When set to true, the VM is required to be deployed to a node marked with this tag
  - Use Net Boost (Optional) - When checked, improves the network performance of Intel-based VMs. Required for macOS Ventura Intel-based VMs. NOTE: Applicable only to macOS BigSur and later
  - Use GPU Passthrough - When checked, enables the VM to use the GPU available on the node. NOTE: GPU Passthrough must be enabled for the cluster
  - Scheduler - The scheduler used to deploy the VM
  - Namespace - The namespace used to deploy VMs to
  - Name Prefix (Optional) - The deployed VM name starts with the specified prefix
  - VM Credentials - The credentials used to SSH to the deployed VM

### Migrating from Orka 2.x and Jenkins 1.xx

#### For both permananet and cloud (ephemeral) agents

If you are migrating from Orka 2.x and Jenkins 1.xx, you need to replace the Orka Credentials with Orka token. You can obtain one by:

- Creating a service account
- Getting a JWT token for that account

#### For cloud (ephemeral) agents

If you previously used existing VM config to deploy VMs, you will see a radio group with two options in your agent template:

- Orka 3.x Deployment - The new way of deploying VMs. Does not require a VM config. If you previously has `Create a new VM config` enabled, your settings have been automatically migrated to this option
- Orka 2.x Deployment - The legacy way of deploying VMs. If you previously used an existing. If you previously used an existing config, your settings have been automatically migrated to this option. All settings are `read-only`. If you need to make changes, you need to migrate to the `Orka 3.x Deployment` option

## Changelog

After version `1.4` [here][changelog].

Prior to version `1.4` [here][old-changelog].

[orka]: https://www.macstadium.com/orka
[changelog]: https://github.com/jenkinsci/macstadium-orka-plugin/releases
[old-changelog]: https://wiki.jenkins.io/display/JENKINS/Orka+Change+Log
