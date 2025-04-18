#!/usr/bin/env python
# coding: utf-8

# In[ ]:

import pandas as pd
import random
import numpy as np
from pathlib import Path


random.seed(0)
np.random.seed(100)


# In[2]:


column_values = {}

column_values["hasHealthIssuesRisk"] = ["Low", "Moderate", "High"]
column_values["hasSize"] = ["Large", "Medium", "Small", "Toy"]

column_values["hasFriendlyRating"] = list(range(1, 11))
column_values["hasLifeSpan"] = list(range(5, 21))
column_values["hasIntelligenceRating"] = list(range(1, 11))
column_values["hasTrainingDifficulty"] = list(range(1, 11))

column_values["needsHoursOfExercicePerDay"] = np.linspace(0, 24, 49)

column_values["origin"] = ["Germany", "England", "Australia", "France", "Switzerland"]
column_values["type"] = ["Toy", "Hound", "Terrier", "Working", "Non-Sporting", "Herding", "Sporting", "Standard"]

columns_names = ["hasName", "origin", "type", "hasFriendlyRating", "hasLifeSpan", "hasSize", "needsHoursOfExercicePerDay", "hasIntelligenceRating", "hasHealthIssuesRisk", "hasAverageWeight", "hasTrainingDifficulty"]
data_columns_names = ["origin", "type", "hasFriendlyRating", "hasLifeSpan", "hasSize", "needsHoursOfExercicePerDay", "hasIntelligenceRating", "hasHealthIssuesRisk", "hasAverageWeight", "hasTrainingDifficulty"]


# In[ ]:


def remove_per_cent_missing_data(dataframe, p=5.0):
    # Calculate the number of values to remove
    total_values = dataframe.size
    num_values_to_remove = int(total_values * (p / 100.0))

    # Flatten the subset DataFrame and get the index and column information
    flattened_subset = dataframe.stack()

    # Randomly select the indices (row, column) to remove
    random_indices = np.random.choice(flattened_subset.index, size=num_values_to_remove, replace=False)

    # Set the randomly selected values to NaN in the original DataFrame
    for row, col in random_indices:
        dataframe.at[row, col] = np.nan

    return dataframe

def generate_dogs(n=50):
    print(f"Generating {n} new dogs !")
    
    data = {}
    data['hasName'] = []
    data['origin'] = []
    data['type'] = []
    data['hasFriendlyRating'] = []
    data['hasLifeSpan'] = []
    data['hasSize'] = []
    data['needsHoursOfExercicePerDay'] = []
    data['hasIntelligenceRating'] = []
    data['hasHealthIssuesRisk'] = []
    data['hasAverageWeight'] = []
    data['hasTrainingDifficulty'] = []

    
    for i in range(n):
        data['hasName'].append(f"dog_{i}")
        data['origin'].append(random.choices(column_values["origin"])[0])
        data['type'].append(random.choices(column_values["type"])[0])
        data['hasFriendlyRating'].append(random.choices(column_values["hasFriendlyRating"])[0])        
        data['hasLifeSpan'].append(random.choices(column_values["hasLifeSpan"])[0])        
        data['hasSize'].append(random.choices(column_values["hasSize"])[0])        
        data['needsHoursOfExercicePerDay'].append(random.choices(column_values["needsHoursOfExercicePerDay"])[0])        
        data['hasIntelligenceRating'].append(random.choices(column_values["hasIntelligenceRating"])[0])        
        data['hasHealthIssuesRisk'].append(random.choices(column_values["hasHealthIssuesRisk"])[0])        
        data['hasAverageWeight'].append(round(np.random.uniform(low=5, high=35), 2))        
        data['hasTrainingDifficulty'].append(random.choices(column_values["hasTrainingDifficulty"])[0])

    df = pd.DataFrame(data=data, columns=columns_names)

    df[data_columns_names] = remove_per_cent_missing_data(df[data_columns_names])
        
        
    return df


# In[ ]:


Path("./dataset").mkdir(parents=True, exist_ok=True)
Path("./KB").mkdir(parents=True, exist_ok=True)

n = 50
dog_df = None
for i in range(14):
    dog_df = generate_dogs(n)

    missing_values = dog_df.isnull().sum().sum()

    dog_df.to_csv(f"./dataset/dogs_{n}.csv")
    print(f"{n} dogs generated and saved in file dogs_{n}.csv. Missing values : {missing_values} ({100 * missing_values / (n * 10)}%)")
    print()

    n *= 2

