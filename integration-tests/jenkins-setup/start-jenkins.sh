#!/bin/bash

set -ux

JENKINS_HOST=localhost:8080
JENKINS_URL=http://$JENKINS_HOST
JENKINS_HOME_VOLUME=~/docker/it/jenkins_home
JENKINS_HOME=/var/jenkins_home
JENKINS_DOCKER_IMAGE_NAME=orka/it-jenkins-setup:lts
CONTAINER_NAME=it-jenkins
JENKINS_PLUGIN_REPO_PATH=../..
WAIT_JENKINS_SEQ_MAX=24
WAIT_JENKINS_SLEEP_SECONDS=10
WAIT_JENKINS_MINUTES=$(($WAIT_JENKINS_SEQ_MAX/(60/$WAIT_JENKINS_SLEEP_SECONDS)))

function wait_for_jenkins_to_start() {
  echo "Restarting Jenkins..."

  # timeout in ~4min
  for i in `seq 1 $WAIT_JENKINS_SEQ_MAX`; do
    status_code=$(curl --write-out %{http_code} --silent --output /dev/null $JENKINS_HOST)
    if [ $status_code -eq 200 ];then
      echo "Jenkins started successfully"
      break
    fi
    sleep $WAIT_JENKINS_SLEEP_SECONDS
    if [ $i -eq $WAIT_JENKINS_SEQ_MAX ];then
      echo "Exit Jenkins setup since Jenkins hasn't started in the last ~($WAIT_JENKINS_SEQ_MAX/6) minutes."
      exit -1
    fi
  done
}

docker container stop $CONTAINER_NAME
docker image rm $JENKINS_DOCKER_IMAGE_NAME
docker build --build-arg JENKINS_TAG=lts --tag $JENKINS_DOCKER_IMAGE_NAME .
rm -rf $JENKINS_HOME_VOLUME

docker container run \
  --name $CONTAINER_NAME \
  --rm \
  --detach \
  --publish 8080:8080 \
  --publish 50000:50000 \
  --volume $JENKINS_HOME_VOLUME:$JENKINS_HOME \
  $JENKINS_DOCKER_IMAGE_NAME

wait_for_jenkins_to_start

echo "Building & installing macstadium-orka-plugin..."
cd $JENKINS_PLUGIN_REPO_PATH
rm -rf target && mvn package 
cd ./integration-tests/jenkins-setup

cp $JENKINS_PLUGIN_REPO_PATH/target/macstadium-orka.hpi $JENKINS_HOME_VOLUME/plugins
docker exec -i $(docker ps -aqf name=$CONTAINER_NAME) curl $JENKINS_URL/jnlpJars/jenkins-cli.jar -o $JENKINS_HOME/jenkins-cli.jar
docker exec -i $(docker ps -aqf name=$CONTAINER_NAME) java -jar $JENKINS_HOME/jenkins-cli.jar -s $JENKINS_URL/ install-plugin file://$JENKINS_HOME/plugins/macstadium-orka.hpi -restart

wait_for_jenkins_to_start
