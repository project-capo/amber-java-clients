#!/bin/bash

for submodule in $(ls | grep amber-java | grep -v common | grep -v iml); do
    rm -rf ${submodule}/src/generated-sources/java;
done
