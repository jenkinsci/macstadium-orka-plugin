package io.jenkins.plugins.orka;

import hudson.Extension;
import hudson.model.AsyncPeriodicWork;
import hudson.model.TaskListener;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Extension
public class OrkaVersionMonitor extends AsyncPeriodicWork {
    public OrkaVersionMonitor() {
        super("Orka version monitor");
    }

    @Override
    public long getRecurrencePeriod() {
        return TimeUnit.HOURS.toMillis(1);
    }

    @Override
    protected void execute(TaskListener listener) throws IOException, InterruptedException {
        OrkaVersionChecker.updateOrkaVersion();
    }
}
