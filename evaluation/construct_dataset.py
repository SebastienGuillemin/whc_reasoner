#!/usr/bin/env python
# coding: utf-8

# In[ ]:


import pandas as pd
import random
import numpy as np
from pathlib import Path


random.seed(0)


# In[ ]:


column_values = {}

column_values["hasHealthIssuesRisk"] = ["Low", "Moderate", "High"]
column_values["hasSize"] = ["Small-Medium", "Medium", "Large", "Small", "Toy"]

column_values["hasFriendlyRating"] = list(range(1, 11)) 
column_values["hasLifeSpan"] = list(range(5, 21))
column_values["hasIntelligenceRating"] = list(range(1, 11))
column_values["hasTrainingDifficulty"] = list(range(1, 11))

column_values["needsHoursOfExercicePerDay"] = np.linspace(0, 24, 49)

column_values["origin"] = ["Germany", "Afghanistan", "England", "Japan", "USA", "Australia", "CentralAfrica", "France", "Scotland", "Switzerland"]
column_values["type"] = ["Toy", "Hound", "Terrier", "Working", "Non-Sporting", "Herding", "Sporting", "Standard"]

columns_names = ["hasName", "origin", "type", "hasFriendlyRating", "hasLifeSpan", "hasSize", "needsHoursOfExercicePerDay", "hasIntelligenceRating", "hasHealthIssuesRisk", "hasAverageWeight", "hasTrainingDifficulty"]


# In[ ]:


rand_range = [x / 100.0 for x in range(0, 10000)]
percent_missing_val = 5.0

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
        mask = random.sample(rand_range, 10)
        data['hasName'].append(f"dog_{i}")
        data['origin'].append(random.choices(column_values["origin"])[0] if mask[0] >= percent_missing_val else None)
        data['type'].append(random.choices(column_values["type"])[0] if mask[1] >= percent_missing_val else None)
        data['hasFriendlyRating'].append(random.choices(column_values["hasFriendlyRating"])[0] if mask[2] >= percent_missing_val else None)        
        data['hasLifeSpan'].append(random.choices(column_values["hasLifeSpan"])[0] if mask[3] >= percent_missing_val else None)        
        data['hasSize'].append(random.choices(column_values["hasSize"])[0] if mask[4] >= percent_missing_val else None)        
        data['needsHoursOfExercicePerDay'].append(random.choices(column_values["needsHoursOfExercicePerDay"])[0] if mask[5] >= percent_missing_val else None)        
        data['hasIntelligenceRating'].append(random.choices(column_values["hasIntelligenceRating"])[0] if mask[6] >= percent_missing_val else None)        
        data['hasHealthIssuesRisk'].append(random.choices(column_values["hasHealthIssuesRisk"])[0] if mask[7] >= percent_missing_val else None)        
        data['hasAverageWeight'].append(round(np.random.uniform(low=5, high=35), 2) if mask[8] >= percent_missing_val else None)        
        data['hasTrainingDifficulty'].append(random.choices(column_values["hasTrainingDifficulty"])[0] if mask[9] >= percent_missing_val else None)
        
    return pd.DataFrame(data=data, columns=columns_names)


# In[ ]:


Path("./dataset").mkdir(parents=True, exist_ok=True)
Path("./KB").mkdir(parents=True, exist_ok=True)

n = 50
dog_df = None
for i in range(14):
    dog_df = generate_dogs(n)

    dog_df.to_csv(f"./dataset/dogs_{n}.csv")
    print(f"{n} dogs generated and saved in file dogs_{n}.csv")
    print()

    n *= 2

