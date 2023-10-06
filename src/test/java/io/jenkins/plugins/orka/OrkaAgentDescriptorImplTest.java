package io.jenkins.plugins.orka;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

public class OrkaAgentDescriptorImplTest {
    @ClassRule
    public static JenkinsRule r = new JenkinsRule();

    @Before
    public void initialize() throws IOException {
        SystemCredentialsProvider.getInstance().getCredentials().clear();
    }

    @Test
    public void when_do_fill_orka_credentials_id_items_with_no_credentials_should_return_empty_credentials()
            throws IOException {
        OrkaAgent.DescriptorImpl descriptor = new OrkaAgent.DescriptorImpl();

        ListBoxModel model = descriptor.doFillOrkaCredentialsIdItems();
        assertEquals(1, model.size());
        assertEquals("", model.get(0).value);
    }

    @Test
    public void when_do_fill_orka_credentials_id_items_with_one_credential_should_return_two_credentials()
            throws IOException {
        OrkaAgent.DescriptorImpl descriptor = new OrkaAgent.DescriptorImpl();
        UsernamePasswordCredentialsImpl credentials = new UsernamePasswordCredentialsImpl(CredentialsScope.SYSTEM,
                "uniqueId", "description", "foo", "bar");
        SystemCredentialsProvider.getInstance().getCredentials().add(credentials);
        SystemCredentialsProvider.getInstance().save();

        ListBoxModel model = descriptor.doFillOrkaCredentialsIdItems();
        assertEquals(2, model.size());
        assertEquals("", model.get(0).value);
        assertEquals(credentials.getId(), model.get(1).value);
    }

    @Test
    public void when_do_fill_vm_credentials_id_items_with_no_credentials_should_return_empty_credentials()
            throws IOException {
        OrkaAgent.DescriptorImpl descriptor = new OrkaAgent.DescriptorImpl();

        ListBoxModel model = descriptor.doFillVmCredentialsIdItems();
        assertEquals(1, model.size());
        assertEquals("", model.get(0).value);
    }

    @Test
    public void when_do_fill_vm_credentials_id_items_with_one_credential_should_return_two_credentials()
            throws IOException {
        OrkaAgent.DescriptorImpl descriptor = new OrkaAgent.DescriptorImpl();
        UsernamePasswordCredentialsImpl credentials = new UsernamePasswordCredentialsImpl(CredentialsScope.SYSTEM,
                "uniqueId", "description", "foo", "bar");
        SystemCredentialsProvider.getInstance().getCredentials().add(credentials);
        SystemCredentialsProvider.getInstance().save();

        ListBoxModel model = descriptor.doFillVmCredentialsIdItems();
        assertEquals(2, model.size());
        assertEquals("", model.get(0).value);
        assertEquals(credentials.getId(), model.get(1).value);
    }
}
