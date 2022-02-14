import com.cloudbees.plugins.credentials.Credentials
import com.cloudbees.plugins.credentials.CredentialsScope
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import com.cloudbees.plugins.credentials.SystemCredentialsProvider
import hudson.model.AdministrativeMonitor
import hudson.model.Node.Mode
import io.jenkins.plugins.orka.OrkaCloud
import io.jenkins.plugins.orka.AgentTemplate
import io.jenkins.plugins.orka.IdleTimeCloudRetentionStrategy
import io.jenkins.plugins.orka.DefaultVerificationStrategy
import java.util.ArrayList
import java.util.Collections
import jenkins.model.Jenkins

String cloudConfigName = "it-orka-cloud";
OrkaCloud currentCloud = Jenkins.instance.clouds.getByName(cloudConfigName);
if(currentCloud != null) {
    println("Skipping creation of cloud config `" + cloudConfigName + "` since it already exists.")
    return;
}

String orkaEndpoint = System.getenv()['ORKA_ENDPOINT'] ?: "http://10.221.188.100";

String orkaUsername = System.getenv()['ORKA_USERNAME'] ?: "it-jenkins@test.com";
String orkaPassword = System.getenv()['ORKA_PASSWORD'] ?: "123456";
String sshUsername =  System.getenv()['SSH_USERNAME'] ?: "admin";
String sshPassword =  System.getenv()['SSH_PASSWORD'] ?: "admin";

String vmConfigName = System.getenv()['VM_CONFIG_NAME'] ?: "it-orka-jenkins";
String agentLabel = System.getenv()['AGENT_LABEL'] ?: "it-orka-agent";
String agentPrefix = System.getenv()['AGENT_PREFIX'] ?: "it";

String remoteFs = System.getenv()['REMOTE_FS_ROOT'] ?: "/Users/admin";

String sshUserCredentialsId = java.util.UUID.randomUUID().toString();
String orkaUserCredentialsId = java.util.UUID.randomUUID().toString();

Credentials sshCredentials = (Credentials) new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, sshUserCredentialsId, "VM SSH credentials", sshUsername, sshPassword);
SystemCredentialsProvider.getInstance().getStore().addCredentials(Domain.global(), sshCredentials);

Credentials orkaCredentials = (Credentials) new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, orkaUserCredentialsId, "Orka credentials", orkaUsername, orkaPassword)
SystemCredentialsProvider.getInstance().getStore().addCredentials(Domain.global(), orkaCredentials)

AgentTemplate template = new AgentTemplate(sshUserCredentialsId, vmConfigName, false, null, 
    null, 1, 1, remoteFs, 
    Mode.NORMAL, agentLabel, agentPrefix, new IdleTimeCloudRetentionStrategy(30), new DefaultVerificationStrategy(), Collections.emptyList());

ArrayList<AgentTemplate> templates = new ArrayList<AgentTemplate>()
templates.add(template)

OrkaCloud cloud = new OrkaCloud(cloudConfigName, orkaUserCredentialsId, orkaEndpoint, "1", 600, false, null, templates)

Jenkins.instance.clouds.add(cloud)

Jenkins.instance.administrativeMonitors.each { x-> 
    String name = x.getClass().name
    if (name.contains("SecurityIsOffMonitor") ||
        name.contains("SecurityIsOffMonitor") ||
        name.contains("URICheckEncodingMonitor") ||
        name.contains("UpdateCenter") ||
        name.contains("UpdateSiteWarningsMonitor") ||
        name.contains("RootUrlNotSetMonitor") ||
        name.contains("CSRFAdministrativeMonitor")) {
        x.disable(true)
    }
}

Jenkins.instance.save()
