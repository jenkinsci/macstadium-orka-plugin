package io.jenkins.plugins.orka.helpers;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Collections;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.model.Saveable;
import hudson.model.Descriptor.FormException;
import hudson.model.Node.Mode;
import hudson.slaves.RetentionStrategy;
import hudson.util.DescribableList;
import io.jenkins.plugins.orka.Constants;
import io.jenkins.plugins.orka.OrkaProvisionedAgent;

public class CapacityHandlerTest {
    @ClassRule
    public static JenkinsRule r = new JenkinsRule();

    @Before
    public void initialize() {
        r.getInstance().getNodes().forEach(n -> {
            try {
                r.getInstance().removeNode(n);
            } catch (IOException e) {
            }
        });
    }

    @Test
    public void when_reserve_two_instances_and_capacity_available_should_return_two() {
        int capacity = 5;
        CapacityHandler handler = new CapacityHandler("cloud", capacity);
        int capacityToReserve = 2;

        int actualReserved = handler.reserveCapacity(capacityToReserve, "provisionIdString");

        assertEquals(2, actualReserved);
    }

    @Test
    public void when_reserve_five_instances_and_capacity_available_should_return_five() {
        int capacity = 5;
        CapacityHandler handler = new CapacityHandler("cloud", capacity);
        int capacityToReserve = 5;

        int actualReserved = handler.reserveCapacity(capacityToReserve, "provisionIdString");

        assertEquals(5, actualReserved);
    }

    @Test
    public void when_reserve_six_instances_and_capacity_not_available_should_return_five() {
        int capacity = 5;
        CapacityHandler handler = new CapacityHandler("cloud", capacity);
        int capacityToReserve = 6;

        int actualReserved = handler.reserveCapacity(capacityToReserve, "provisionIdString");

        assertEquals(5, actualReserved);
    }

    @Test
    public void when_reserve_nine_instances_and_capacity_not_available_should_return_five() {
        int capacity = 5;
        CapacityHandler handler = new CapacityHandler("cloud", capacity);
        int capacityToReserve = 9;

        int actualReserved = handler.reserveCapacity(capacityToReserve, "provisionIdString");

        assertEquals(5, actualReserved);
    }

    @Test
    public void when_reserve_nine_instances_and_three_available_should_return_three() {
        int capacity = 5;
        CapacityHandler handler = new CapacityHandler("cloud", capacity);
        handler.reserveCapacity(2, "provisionIdString");
        handler.addRunningInstance();
        handler.addRunningInstance();
        int capacityToReserve = 9;

        int actualReserved = handler.reserveCapacity(capacityToReserve, "provisionIdString");

        assertEquals(3, actualReserved);
    }

    @Test
    public void when_reserve_nine_instances_and_three_available_with_one_pending_should_return_two() {
        int capacity = 5;
        CapacityHandler handler = new CapacityHandler("cloud", capacity);
        handler.reserveCapacity(3, "provisionIdString");
        handler.addRunningInstance();
        handler.addRunningInstance();
        int capacityToReserve = 9;

        int actualReserved = handler.reserveCapacity(capacityToReserve, "provisionIdString");

        assertEquals(2, actualReserved);
    }

    @Test
    public void when_reserve_two_instances_and_three_available_should_return_two() {
        int capacity = 5;
        CapacityHandler handler = new CapacityHandler("cloud", capacity);
        handler.reserveCapacity(2, "provisionIdString");
        handler.addRunningInstance();
        handler.addRunningInstance();
        int capacityToReserve = 2;

        int actualReserved = handler.reserveCapacity(capacityToReserve, "provisionIdString");

        assertEquals(2, actualReserved);
    }

    @Test
    public void when_reserve_two_instances_and_three_available_with_one_pending_should_return_two() {
        int capacity = 5;
        CapacityHandler handler = new CapacityHandler("cloud", capacity);
        handler.reserveCapacity(3, "provisionIdString");
        handler.addRunningInstance();
        handler.addRunningInstance();
        int capacityToReserve = 2;

        int actualReserved = handler.reserveCapacity(capacityToReserve, "provisionIdString");

        assertEquals(2, actualReserved);
    }

    @Test
    public void when_reserve_eight_instances_and_four_available_should_return_four() {
        int capacity = 5;
        CapacityHandler handler = new CapacityHandler("cloud", capacity);
        handler.reserveCapacity(2, "provisionIdString");
        handler.addRunningInstance();
        handler.removeRunningInstance();
        handler.addRunningInstance();
        int capacityToReserve = 8;

        int actualReserved = handler.reserveCapacity(capacityToReserve, "provisionIdString");

        assertEquals(4, actualReserved);
    }

    @Test
    public void when_reserve_eight_instances_and_three_available_with_one_pending_should_return_three() {
        int capacity = 5;
        CapacityHandler handler = new CapacityHandler("cloud", capacity);
        handler.reserveCapacity(3, "provisionIdString");
        handler.addRunningInstance();
        handler.removeRunningInstance();
        handler.addRunningInstance();
        int capacityToReserve = 8;

        int actualReserved = handler.reserveCapacity(capacityToReserve, "provisionIdString");

        assertEquals(3, actualReserved);
    }

    @Test
    public void when_reserve_eight_instances_and_no_running_should_return_eight() {
        int capacity = 8;
        CapacityHandler handler = new CapacityHandler("cloud", capacity);
        handler.reserveCapacity(3, "provisionIdString");
        handler.addRunningInstance();
        handler.removeRunningInstance();
        handler.addRunningInstance();
        handler.removeRunningInstance();
        handler.addRunningInstance();
        handler.removeRunningInstance();
        handler.removeRunningInstance();
        handler.removeRunningInstance();
        handler.removeRunningInstance();
        int capacityToReserve = 8;

        int actualReserved = handler.reserveCapacity(capacityToReserve, "provisionIdString");

        assertEquals(8, actualReserved);
    }

    @Test
    public void when_reserve_three_instances_and_four_available_should_return_three() {
        int capacity = 5;
        CapacityHandler handler = new CapacityHandler("cloud", capacity);
        handler.reserveCapacity(2, "provisionIdString");
        handler.addRunningInstance();
        handler.removeRunningInstance();
        handler.addRunningInstance();
        int capacityToReserve = 3;

        int actualReserved = handler.reserveCapacity(capacityToReserve, "provisionIdString");

        assertEquals(3, actualReserved);
    }

    @Test
    public void when_reserve_three_instances_and_four_available_with_one_pending_should_return_three() {
        int capacity = 5;
        CapacityHandler handler = new CapacityHandler("cloud", capacity);
        handler.reserveCapacity(3, "provisionIdString");
        handler.addRunningInstance();
        handler.removeRunningInstance();
        handler.addRunningInstance();
        int capacityToReserve = 3;

        int actualReserved = handler.reserveCapacity(capacityToReserve, "provisionIdString");

        assertEquals(3, actualReserved);
    }

    @Test
    public void when_reserve_three_instances_and_four_available_and_one_failed_should_return_three() {
        int capacity = 5;
        CapacityHandler handler = new CapacityHandler("cloud", capacity);
        handler.reserveCapacity(2, "provisionIdString");
        handler.addRunningInstance();
        handler.removeFailedPlannedInstance();
        int capacityToReserve = 3;

        int actualReserved = handler.reserveCapacity(capacityToReserve, "provisionIdString");

        assertEquals(3, actualReserved);
    }

    @Test
    public void when_reserve_seven_instances_and_four_available_with_one_failed_should_return_four() {
        int capacity = 5;
        CapacityHandler handler = new CapacityHandler("cloud", capacity);
        handler.reserveCapacity(2, "provisionIdString");
        handler.addRunningInstance();
        handler.removeFailedPlannedInstance();
        int capacityToReserve = 7;

        int actualReserved = handler.reserveCapacity(capacityToReserve, "provisionIdString");

        assertEquals(4, actualReserved);
    }

    @Test
    public void when_reserve_two_instances_with_one_initial_should_return_two() throws IOException, FormException {
        int capacity = 5;
        CapacityHandler handler = new CapacityHandler("cloud", capacity);
        r.getInstance()
                .addNode(new OrkaProvisionedAgent("cloud", "vmId", "node", "host", 2, Constants.DEFAULT_NAMESPACE,
                        "vmCredentialsId", 5, "remoteFS",
                        Mode.NORMAL, "labelString", RetentionStrategy.NOOP,
                        new DescribableList<>(Saveable.NOOP, Collections.emptyList()), null));
        int capacityToReserve = 2;

        int actualReserved = handler.reserveCapacity(capacityToReserve, "provisionIdString");

        assertEquals(2, actualReserved);
    }

    @Test
    public void when_reserve_five_instances_with_one_initial_should_return_four() throws IOException, FormException {
        int capacity = 5;
        CapacityHandler handler = new CapacityHandler("cloud", capacity);
        r.getInstance()
                .addNode(new OrkaProvisionedAgent("cloud", "vmId", "node", "host", 2, Constants.DEFAULT_NAMESPACE,
                        "vmCredentialsId", 5, "remoteFS",
                        Mode.NORMAL, "labelString", RetentionStrategy.NOOP,
                        new DescribableList<>(Saveable.NOOP, Collections.emptyList()), null));
        int capacityToReserve = 5;

        int actualReserved = handler.reserveCapacity(capacityToReserve, "provisionIdString");

        assertEquals(4, actualReserved);
    }

    @Test
    public void when_reserve_nine_instances_with_one_initial_should_return_four() throws IOException, FormException {
        int capacity = 5;
        CapacityHandler handler = new CapacityHandler("cloud", capacity);
        r.getInstance()
                .addNode(new OrkaProvisionedAgent("cloud", "vmId", "node", "host", 2, Constants.DEFAULT_NAMESPACE,
                        "vmCredentialsId", 5, "remoteFS",
                        Mode.NORMAL, "labelString", RetentionStrategy.NOOP,
                        new DescribableList<>(Saveable.NOOP, Collections.emptyList()), null));
        int capacityToReserve = 9;

        int actualReserved = handler.reserveCapacity(capacityToReserve, "provisionIdString");

        assertEquals(4, actualReserved);
    }

    public void when_reserve_two_instances_with_one_initial_for_another_cloud_should_return_two()
            throws IOException, FormException {
        int capacity = 5;
        CapacityHandler handler = new CapacityHandler("cloud", capacity);
        r.getInstance()
                .addNode(new OrkaProvisionedAgent("another", "vmId", "node", "host", 2, Constants.DEFAULT_NAMESPACE,
                        "vmCredentialsId", 5,
                        "remoteFS", Mode.NORMAL, "labelString", RetentionStrategy.NOOP,
                        new DescribableList<>(Saveable.NOOP, Collections.emptyList()), null));
        int capacityToReserve = 2;

        int actualReserved = handler.reserveCapacity(capacityToReserve, "provisionIdString");

        assertEquals(2, actualReserved);
    }

    @Test
    public void when_reserve_five_instances_with_one_initial_for_another_cloud_should_return_five()
            throws IOException, FormException {
        int capacity = 5;
        CapacityHandler handler = new CapacityHandler("cloud", capacity);
        r.getInstance()
                .addNode(new OrkaProvisionedAgent("another", "vmId", "node", "host", 2, Constants.DEFAULT_NAMESPACE,
                        "vmCredentialsId", 5,
                        "remoteFS", Mode.NORMAL, "labelString", RetentionStrategy.NOOP,
                        new DescribableList<>(Saveable.NOOP, Collections.emptyList()), null));
        int capacityToReserve = 5;

        int actualReserved = handler.reserveCapacity(capacityToReserve, "provisionIdString");

        assertEquals(5, actualReserved);
    }

    @Test
    public void when_reserve_nine_instances_with_one_initial_for_another_cloud_should_return_five()
            throws IOException, FormException {
        int capacity = 5;
        CapacityHandler handler = new CapacityHandler("cloud", capacity);
        r.getInstance()
                .addNode(new OrkaProvisionedAgent("another", "vmId", "node", "host", 2, Constants.DEFAULT_NAMESPACE,
                        "vmCredentialsId", 5,
                        "remoteFS", Mode.NORMAL, "labelString", RetentionStrategy.NOOP,
                        new DescribableList<>(Saveable.NOOP, Collections.emptyList()), null));
        int capacityToReserve = 9;

        int actualReserved = handler.reserveCapacity(capacityToReserve, "provisionIdString");

        assertEquals(5, actualReserved);
    }
}
