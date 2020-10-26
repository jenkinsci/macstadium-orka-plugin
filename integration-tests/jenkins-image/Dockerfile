ARG  JENKINS_TAG=lts
FROM jenkins/jenkins:${JENKINS_TAG}

# Skip initial setup
ENV JAVA_OPTS -Djenkins.install.runSetupWizard=false

COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt

COPY init-cloud.groovy /usr/share/jenkins/ref/init.groovy.d/init-cloud.groovy
COPY init-short-job.groovy /usr/share/jenkins/ref/init.groovy.d/init-short-job.groovy
COPY init-long-job.groovy /usr/share/jenkins/ref/init.groovy.d/init-long-job.groovy
