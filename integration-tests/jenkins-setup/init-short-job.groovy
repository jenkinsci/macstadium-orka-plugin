import jenkins.model.Jenkins
import hudson.model.FreeStyleProject

String jobName = "orka-it-short-job";

FreeStyleProject job = Jenkins.instance.getItem(jobName);

if(job != null) {
  println("Skipping creation of job `" + jobName + "` since it already exists.")
  return;
}

Jenkins.instance.createProjectFromXML(jobName, new ByteArrayInputStream("""\
<?xml version='1.1' encoding='UTF-8'?>
<project>
  <actions/>
  <description>This is Jenkins integration tests short job used for testing Keep Idle retention strategy.</description>
  <keepDependencies>false</keepDependencies>
  <properties/>
  <scm class="hudson.scm.NullSCM"/>
  <assignedNode>it-orka-agent</assignedNode>
  <canRoam>false</canRoam>
  <disabled>false</disabled>
  <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
  <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
  <triggers/>
  <concurrentBuild>false</concurrentBuild>
  <builders>
    <hudson.tasks.Shell>
      <command>echo &apos;Jenkins Integration Test&apos;</command>
      <configuredLocalRules/>
    </hudson.tasks.Shell>
  </builders>
  <publishers/>
  <buildWrappers/>
</project>
""".bytes))
