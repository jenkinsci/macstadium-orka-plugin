#!/bin/bash

set -eux

CONTAINER_NAME=it-jenkins
docker container stop $CONTAINER_NAME