#!/usr/bin/env python
# coding: utf-8

# In[1]:


import pandas as pd
import random
import numpy as np
from pathlib import Path


random.seed(0)


# In[2]:


# Keep breed name in the "Breed" column
df = pd.read_csv("../evaluation/dogs_dataset.csv")
df["Breed"] = df["Name"]
df.drop(columns=["Name", "Grooming Needs", "Shedding Level"], inplace=True)

# Rename columns
df = df.rename(columns={"Origin" : "origin", "Type" : "type", "Friendly Rating (1-10)" : "hasFriendlyRating", "Life Span" : "hasLifeSpan", "Size" : "hasSize",  "Exercise Requirements (hrs/day)" : "needsHoursOfExercicePerDay", "Intelligence Rating (1-10)" : "hasIntelligenceRating", "Health Issues Risk" : "hasHealthIssuesRisk", "Average Weight (kg)" : "hasAverageWeight", "Training Difficulty (1-10)" : "hasTrainingDifficulty", "Breed" : "breed"})
df.columns = df.columns.str.replace(' ', '_')

# Clear data
selected_dogs = df[((df["origin"] == "France") & (df["type"] == "Toy")) | ((df["origin"] == "England") & (df["type"] == "Hound"))]
selected_dogs.loc[selected_dogs["breed"] == "Poodle (Toy)", "breed"] = "Poodle"
selected_dogs.loc[selected_dogs["breed"] == "English Foxhound", "breed"] = "English_Foxhound"
selected_dogs = selected_dogs.drop(columns=["breed"])   # Dropping Breed as it is the target


# In[3]:


column_values = {}

column_values["hasHealthIssuesRisk"] = ["Low", "Moderate", "High"]
column_values["hasSize"] = ["Small-Medium", "Medium", "Large", "Small", "Toy"]

column_values["hasFriendlyRating"] = list(range(1, 11)) 
column_values["hasLifeSpan"] = list(range(5, 21))
column_values["hasIntelligenceRating"] = list(range(1, 11))
column_values["hasTrainingDifficulty"] = list(range(1, 11))

column_values["needsHoursOfExercicePerDay"] = np.linspace(0, 24, 49)

column_values["origin"] = ["France", "England"]
column_values["type"] = ["Hound", "Toy"]

columns_names = ["hasName", "origin", "type", "hasFriendlyRating", "hasLifeSpan", "hasSize", "needsHoursOfExercicePerDay", "hasIntelligenceRating", "hasHealthIssuesRisk", "hasAverageWeight", "hasTrainingDifficulty"]


# In[4]:


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

    return pd.DataFrame(data=data, columns=columns_names)


# In[ ]:


Path("./dataset").mkdir(parents=True, exist_ok=True)
Path("./KB").mkdir(parents=True, exist_ok=True)


n = 50
dog_df = None
for i in range(14):
    dog_df = generate_dogs(n)
    
    dog_df.to_csv(f"./dataset/dogs_{n}.csv")
    print(f"{n} dogs generated and saved in file dogs_{n}.ttl")
    print()

    n *= 2

