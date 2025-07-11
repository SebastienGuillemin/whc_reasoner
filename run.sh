#!/bin/bash

function print_big_message {
    echo " "
    echo -e "\e[1;32m--------$1--------\e[0m"
    echo $1
    echo " "

}

if [[ $# -eq 0 ]] || [[ "$1" = "stups_evaluations" ]]
then
    print_big_message "Running the stups WHC evaluation programs for Cannabis drug samples"

    l=100
        for i in $(seq 1 9);
        do
            mvn clean install exec:java -Dexec.mainClass="com.sebastienguillemin.whcreasoner.evaluation.Eval" -f core/pom.xml -Dexec.args="./stups_evaluation/evaluation_STUPS_$l.ttl stups_evaluation non_save_inferred_axioms"
            l=$((l * 2))
        done

elif [[ "$1" = "example" ]]
then
    print_big_message "Running the core example"

    mvn clean install exec:java -Dexec.mainClass="com.sebastienguillemin.whcreasoner.example.Example" -f core/pom.xml

elif [[ "$1" = "dataset" ]]
then
    print_big_message "Creating dataset"

    cd evaluation
    python construct_dataset.py
    cd ..

    mvn clean install -f core/pom.xml
    for filename in ./evaluation/dataset/*; do
        echo Processing $filename
        mvn exec:java -Dexec.mainClass="com.sebastienguillemin.whcreasoner.dataset.ConstructKB" -Dexec.args="$filename" -f core/pom.xml
    done

elif [[ "$1" = "quantitative_evaluation" ]]
then
    print_big_message "Running WHC quantitative evaluation"
    
    mvn clean install -f core/pom.xml
    for filename in ./evaluation/KB/*; do
        for i in $(seq 1 10); do
            echo Processing $filename
            mvn exec:java -Dexec.mainClass="com.sebastienguillemin.whcreasoner.evaluation.Eval" -Dexec.args="$filename non_stups_evaluation non_save_inferred_axioms" -f core/pom.xml
        done
    done

elif [[ "$1" = "qualitative_evaluation" ]]
then
    print_big_message "Running WHC qualitative evaluation"

    declare -a KBs=("./evaluation/KB/dogs_50.ttl" "./evaluation/KB/dogs_100.ttl" "./evaluation/KB/dogs_200.ttl" "./evaluation/KB/dogs_400.ttl" "./evaluation/KB/dogs_800.ttl")

    mvn clean install -f core/pom.xml
    for kb in "${KBs[@]}"
    do
        print_big_message "Processing $kb"
        mvn exec:java -Dexec.mainClass="com.sebastienguillemin.whcreasoner.evaluation.Eval" -Dexec.args="$kb non_stups_evaluation save_inferred_axioms" -f core/pom.xml
    done

elif [[ "$1" = "swrl_evaluation" ]]
then
    print_big_message "Running evaluation for SWRL"
    
    cd swrl-benchmark
    ./run.sh
    cd ..
elif [[ "$1" = "stups_swrl_evaluation" ]]
then
    print_big_message "Running STUPS evaluation for SWRL"
    
    cd swrl-benchmark
    ./run.sh stups_evaluations
    cd ..
else
    print_big_message "Unknown argument(s)"
fi