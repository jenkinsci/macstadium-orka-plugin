package io.jenkins.plugins.orka;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import hudson.Extension;
import hudson.model.Label;
import hudson.model.LoadStatistics;
import hudson.slaves.Cloud;
import hudson.slaves.NodeProvisioner;
import jenkins.model.Jenkins;

@Extension(ordinal = 200)
public class NoDelayProvisionerStrategy extends NodeProvisioner.Strategy {
    private static final Logger logger = Logger.getLogger(NoDelayProvisionerStrategy.class.getName());

    @Override
    public NodeProvisioner.StrategyDecision apply(NodeProvisioner.StrategyState strategyState) {
        final Label label = strategyState.getLabel();

        LoadStatistics.LoadStatisticsSnapshot snapshot = strategyState.getSnapshot();
        int availableCapacity = snapshot.getAvailableExecutors() // live executors
            + snapshot.getConnectingExecutors() // executors present but not yet connected
            + strategyState.getPlannedCapacitySnapshot() // capacity added by previous strategies from previous rounds
            + strategyState.getAdditionalPlannedCapacity(); // capacity added by previous strategies _this round_

        int currentDemand = snapshot.getQueueLength();
        logger.log(Level.FINE, "Available capacity={0}, currentDemand={1}",
                new Object[] { availableCapacity, currentDemand });

        if (availableCapacity < currentDemand) {
            Jenkins jenkinsInstance = Jenkins.get();
            for (Cloud cloud : jenkinsInstance.clouds) {
                if (cloud instanceof OrkaCloud && cloud.canProvision(label)) {

                    OrkaCloud orka = (OrkaCloud) cloud;
                    if (orka.getNoDelayProvisioning()) {
                        Collection<NodeProvisioner.PlannedNode> plannedNodes = cloud.provision(label,
                                currentDemand - availableCapacity);
                        logger.log(Level.FINE, "Planned {0} new nodes", plannedNodes.size());
                        strategyState.recordPendingLaunches(plannedNodes);
                        availableCapacity += plannedNodes.size();
                        logger.log(Level.FINE, "After provisioning, available capacity={0}, currentDemand={1}",
                                new Object[] { availableCapacity, currentDemand });
                        break;
                    }
                }
            }
        }
        if (availableCapacity >= currentDemand) {
            logger.log(Level.FINE, "Provisioning completed");
            return NodeProvisioner.StrategyDecision.PROVISIONING_COMPLETED;
        } else {
            logger.log(Level.FINE, "Provisioning not complete, consulting remaining strategies");
            return NodeProvisioner.StrategyDecision.CONSULT_REMAINING_STRATEGIES;
        }
    }

}
