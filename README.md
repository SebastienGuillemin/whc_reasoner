# Evaluation using the Drog breed knowledge bases
## Generating knowledge bases
The following command can reconstruct the dog breed knowledge bases (KB). The resulting KB are in the folder [evaluation/KB](evaluation/KB/). 

```
./run.sh dataset
```

Note that this command requires Python and Java (8 or above) to be installed. Moreover, the KB generation will also create CSV files in the folder [evaluation/dataset](evaluation/dataset/).
**KBs generation may take some time**

The generation of KB is a two-stage process. First, CSV files are generated using Python script (see [this file](evaluation/README.md) for further information). Then, a [Java program](./core/src/main/java/com/sebastienguillemin/whcreasoner/dataset/ConstructKB.java) will convert each CSV file into a Turtle file (i.e., a KB).

## Running the quantitative evaluation
You can run the quantitative evaluation using the following command.

```
./run.sh quantitative_evaluation
```
This will produce files named "whc_result.csv" (placed in the root folder of this repository) containing the run times and the number of inferred axioms per KB by the WHC reasoner.


## Running the qualitative evaluation
You can run the qualitative evaluation using the following command.

```
./run.sh qualitative_evaluation
```

This will produce files named "inferred_axioms_[KB name].txt" (placed in the root folder of this repository) containing the inferred axioms for a given KB.

# SWRL benchmark

You can run the SWRL benchmark using the following command.

```
./run.sh swrl_evaluation
```

This command will produce the file "swrl_result.csv" (in the folder [swrl-benchmark](swrl-benchmark/)) containing the Drool engine performances per KB.
