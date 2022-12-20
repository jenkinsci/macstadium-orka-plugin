package io.jenkins.plugins.orka;

import hudson.Extension;
import hudson.XmlFile;
import hudson.model.Saveable;
import hudson.model.listeners.SaveableListener;

import jenkins.model.Jenkins;

@Extension
public final class CloudSaveListener extends SaveableListener {
    @Override
    public void onChange(Saveable o, XmlFile file) {
        if (o instanceof Jenkins) {
            OrkaVersionChecker.updateOrkaVersion();
        }
    }
}
