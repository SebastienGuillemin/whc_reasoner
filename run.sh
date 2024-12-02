#!/bin/bash

function print_big_message {
    echo " "
    echo "---------------------------------------------"
    echo $1
    echo "---------------------------------------------"
    echo " "

}

if [[ $# -eq 0 ]] || [[ "$1" = "core" ]]
then
    print_big_message "Installing modules in .m2 repository and running the core example"

    mvn clean install exec:java -Dexec.mainClass="com.sebastienguillemin.whcreasoner.core.Example" -f core/pom.xml
elif [[ "$1" = "dataset" ]]
then
    print_big_message "Creating dataset"

    cd evaluation
    jupyter nbconvert --to script construct_dataset.ipynb
    python construct_dataset.py
    cd ..

    mvn clean install -f core/pom.xml
    for filename in ./evaluation/dataset/*; do
        echo Processing $filename
        mvn exec:java -Dexec.mainClass="com.sebastienguillemin.whcreasoner.core.ConstructKB" -Dexec.args="$filename" -f core/pom.xml
    done
elif [[ "$1" = "eval_quanti" ]]
then
    print_big_message "Running quantitative evaluation"
    
    mvn clean install -f core/pom.xml
    for filename in ./evaluation/KB/*; do
        for i in $(seq 1 10); do
            echo Processing $filename
            mvn exec:java -Dexec.mainClass="com.sebastienguillemin.whcreasoner.core.Eval" -Dexec.args="$filename" -f core/pom.xml
        done
    done
elif [[ "$1" = "eval_quali" ]]
then
    print_big_message "Running qualitative evaluation"

    declare -a KBs=("./evaluation/KB/dogs_50.ttl" "./evaluation/KB/dogs_100.ttl" "./evaluation/KB/dogs_200.ttl")

    mvn clean install -f core/pom.xml
    for kb in "${KBs[@]}"
    do
        print_big_message "Processing $kb"
        mvn exec:java -Dexec.mainClass="com.sebastienguillemin.whcreasoner.core.Eval" -Dexec.args="$kb save_inferred_axioms" -f core/pom.xml
    done

elif [[ "$1" = "swrl-eval" ]]
then
    print_big_message "Running evaluation for SWRL"
    
    cd swrl-benchmark
    ./run.sh
    cd ..
else
    print_big_message "Unknown argument(s)"
fi