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

    mvn clean install exec:java -Dexec.mainClass="com.sebastienguillemin.whcreasoner.core.Example"
elif [[ "$1" = "dataset" ]]
then
    print_big_message "Creating dataset"

    jupyter nbconvert --to script ./evaluation/construct_dataset.ipynb
    cd evaluation
    python construct_dataset.py
    cd ..

    mvn clean install

    for filename in ./evaluation/dataset/*; do
        echo Processing $filename
        mvn exec:java -Dexec.mainClass="com.sebastienguillemin.whcreasoner.core.ConstructKB" -Dexec.args="$filename"
    done
elif [[ "$1" = "eval" ]]
then
    print_big_message "Running evaluation"
    
    mvn clean install

    for filename in ./evaluation/KB/*; do
        for i in $(seq 1 10); do
            echo Processing $filename
            mvn exec:java -Dexec.mainClass="com.sebastienguillemin.whcreasoner.core.Eval" -Dexec.args="$filename"
        done
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