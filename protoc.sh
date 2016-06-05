#!/bin/bash

command -v protoc >/dev/null 2>&1 || { echo >&2 "I require protoc but it's not installed. Aborting."; exit; }

export script_directory="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

set -x

pushd ${script_directory}
  mkdir -p amber-java-common/src/generated-sources/java
  cd ./amber-java-common/src/main/resources
  protoc drivermsg.proto --java_out=./../../../../amber-java-common/src/generated-sources/java
  cd ./../../../../

  for submodule in $(ls | grep amber-java | grep -v common | grep -v iml); do
    mkdir -p ${submodule}/src/generated-sources/java
    for proto_file in $(find ${submodule}/src/main/resources/ -name "*.proto"); do
        protoc --proto_path=./amber-java-common/src/main/resources --proto_path=${submodule}/src/main/resources/ ${proto_file} --java_out=${submodule}/src/generated-sources/java
    done
  done
popd
