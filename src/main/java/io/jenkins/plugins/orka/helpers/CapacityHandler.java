package io.jenkins.plugins.orka.helpers;

import hudson.model.Node;

import io.jenkins.plugins.orka.OrkaProvisionedAgent;

import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import jenkins.model.Jenkins;

public class CapacityHandler {
    private static final Logger logger = Logger.getLogger(CapacityHandler.class.getName());
    private static final int INITIAL_RUNNING_INSTANCES = Integer.MIN_VALUE;

    private transient ReentrantLock instanceLock = new ReentrantLock();

    private String cloudId;
    private int instanceCap;

    private int runningInstances;
    private int plannedInstances;

    public CapacityHandler(String cloudId, int instanceCap) {
        this.cloudId = cloudId;
        this.instanceCap = instanceCap;
        this.runningInstances = INITIAL_RUNNING_INSTANCES;
    }

    public int reserveCapacity(int requestedInstances, String provisionIdString) {
        try {
            instanceLock.lock();

            logger.fine(String.format("%s. Reserving capacity for: %s", provisionIdString, requestedInstances));

            this.ensureRunningInstancesInitialized();

            logger.fine(String.format("%s. Current instances: %s, planned instances: %s: ", provisionIdString,
                    this.runningInstances, this.plannedInstances));

            int totalInstances = this.runningInstances + this.plannedInstances;

            logger.fine(String.format("%s. Total instances: %s, instance cap: %s: ", provisionIdString, totalInstances,
                    this.instanceCap));

            int availableInstances = Math.min(this.instanceCap - totalInstances, requestedInstances);
            int reservedCapacity = Math.max(availableInstances, 0);
            this.plannedInstances += reservedCapacity;

            return reservedCapacity;
        } finally {
            instanceLock.unlock();
        }
    }

    private boolean belongsToCloud(Node node) {
        if (node instanceof OrkaProvisionedAgent) {
            OrkaProvisionedAgent orkaProvisionedAgent = (OrkaProvisionedAgent) node;
            return orkaProvisionedAgent.getCloudId().equals(this.cloudId);
        }
        return false;
    }

    private void ensureRunningInstancesInitialized() {
        if (this.runningInstances == INITIAL_RUNNING_INSTANCES) {
            this.runningInstances = (int) Jenkins.getInstance().getNodes().stream().filter(n -> this.belongsToCloud(n))
                    .count();
            logger.fine("Initial running instances count: " + this.runningInstances);
        }
    }

    public void addRunningInstance() {
        logger.fine("Adding running instance...");
        try {
            instanceLock.lock();
            this.ensureRunningInstancesInitialized();

            this.plannedInstances--;
            this.runningInstances++;

            logger.fine(String.format("New planned instances: %s. New running instances: %s", this.plannedInstances,
                    this.runningInstances));
        } finally {
            instanceLock.unlock();
        }
    }

    public void removeRunningInstance() {
        logger.fine("Adding running instance...");
        try {
            instanceLock.lock();
            this.ensureRunningInstancesInitialized();
            this.runningInstances = this.runningInstances > 0 ? this.runningInstances - 1 : 0;

            logger.fine(String.format("New running instances: %s", this.runningInstances));
        } finally {
            instanceLock.unlock();
        }
    }

    public void removeFailedPlannedInstance() {
        logger.fine("Removing planned instance...");
        try {
            instanceLock.lock();
            this.plannedInstances = this.plannedInstances > 0 ? this.plannedInstances - 1 : 0;


            logger.fine(String.format("New planned instances: %s", this.plannedInstances));
        } finally {
            instanceLock.unlock();
        }
    }
}