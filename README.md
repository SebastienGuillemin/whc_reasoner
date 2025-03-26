# Evaluation using the Drog breed knowledge bases
## Generating knowledge bases
The dog breeds knowledge bases (KB) can be reconstructed using the following command. The resulting KB are placed in folder [evaluation/KB](evaluation/KB/). 

```
./run.sh dataset
```

Note that this command requires Python and Java (8 or above) to be installed. Moreover, the KB generation will also create CSV files that can be found in folder [evaluation/dataset](evaluation/dataset/).
**KBs generation may take some time**

The generation of KB is a two stages process. Frist, CSV files are generated using Python script (see [this file](evaluation/README.md) for further information). Then, a [Java program](./core/src/main/java/com/sebastienguillemin/whcreasoner/dataset/ConstructKB.java) will convert each CSV file into a Turtle file (i.e., a KB).

## Running the quantitative evaluation
You can run the quantitative evaluation using the following command.

```
./run.sh quantitative_evaluation
```
This will produced files named "whc_result.csv" (placed at the root folder of this repository) containing the run times and the number of inferred axioms per KB by the WHC reasoner.


## Running the qualitative evaluation
You can run the qualitative evaluation using th efollowing command.

```
./run.sh qualitative_evaluation
```

This will produced files named "inferred_axioms_[KB name].txt" (placed at the root folder of this repository) containing the inferred axioms for a given KB.

# SWRL benchmark

You can run the SWRL benchmark using the following command.

```
./run.sh swrl_evaluation
```

This command will produce the file "swrl_result.csv" (in folder [swrl-benchmark](swrl-benchmark/)) contaning the Drool engine performances per KB.