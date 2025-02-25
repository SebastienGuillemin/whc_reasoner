# Evaluation using the Drog breed knowledge bases
## Generating knowledge bases
The dog breed knowledge bases (KB) can be reconstructed using the following command. The resulting KB are placed in folder "./evaluation/KB". 

```
/.run.sh dataset
```

Note that this command requires Python and Java to be installed. Moreover, the KB generation will also create CSV files that can be found in "evaluation/dataset". 

## Running the quantitative evaluation
You can run the quantitative evaluation using th efollowing command.

```
./run.sh quantitative_evaluation
```
This will produced files named "whc_result" at the root folder containing the run times and the number of inferred axioms per KB by the WHC reasoner.


## Running the qualitative evaluation
You can run the qualitative evaluation using th efollowing command.

```
./run.sh qualitative_evaluation
```

This will produced files named "inferred_axioms_[KB name]" at the root folder containing the inferred axioms for a given KB.

# SWRL benchmark

You can run the SWRL benchmark using the following command.

```
./run.sh swrl_evaluation
```

This command will produce the file "./swrl-benchmark/swrl_result.csv" coanting the Drool engine performances per KB.