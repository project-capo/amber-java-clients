#!/bin/bash

export __dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

pushd ${__dir}
    __current_branch=$(git branch | grep \* | sed 's/\*\s//g')

    mvn clean
    ${__dir}/protoc.sh
    mvn install
    mvn deploy

    rm -rf ${__dir}/repo
    mkdir -p ${__dir}/repo
    for path in $(find . -type d -name mvn-repo)
    do
        cp --verbose -r ${__dir}/${path}/ ${__dir}/repo/
    done

    git checkout mvn-repo
    cp --verbose -r ${__dir}/repo/mvn-repo/pl ${__dir}/
    git add --all
    git commit -m "Update mvn-repo"
    git pull -s recursive -X ours --no-edit origin mvn-repo
    git push origin mvn-repo

    git checkout ${__current_branch}
popd
