#!/bin/bash

mvn clean install

if [[ $# -eq 1 ]] && [[ "$1" = "stups_evaluations" ]]
then
    echo "Running SWRL STUPS evaluation"
    for filename in ../stups_evaluation/*; do
        echo Processing $filename
        mvn exec:java -Dexec.mainClass="com.sebastienguillemin.Benchmark" -Dexec.args="$filename stups_evaluations"
    done
else
    for filename in ../evaluation/KB/*; do
        for i in $(seq 1 10); do
            echo Processing $filename
            mvn exec:java -Dexec.mainClass="com.sebastienguillemin.Benchmark" -Dexec.args="$filename"
        done
    done
fi