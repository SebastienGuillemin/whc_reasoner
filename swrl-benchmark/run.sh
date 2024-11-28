#!/bin/bash

mvn clean install

for filename in ../evaluation/KB/*; do
    for i in $(seq 1 10); do
        echo Processing $filename
        mvn exec:java -Dexec.mainClass="com.sebastienguillemin.Benchmark" -Dexec.args="$filename"
    done
done
