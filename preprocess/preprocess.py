import os
import pandas as pd
import sys

filename = os.path.join("preprocess", "70K_IlanDatasi_wLi_230120.xlsx")
sheetName = "Sheet1"

f = open(os.path.join("preprocess", "il.txt"), encoding="utf-8")
cities = f.read()
cities = cities.split("\n")
f.close()

f = open(os.path.join("preprocess", "ülkeler.txt") , encoding="utf-8")
countries = f.read()
countries = countries.split("\n")
f.close()

education_keywords = ["Mezun", "Üniversite", "Lise", "Öğrenci", "Doktora", "Meslek", "Yüksekokul"]

#Functions to apply 
def check_city_allowed(cities, x):
    #First requirement
    res1 = False
    for city in cities:
        res1 = res1 or city in x or city.lower() in x.lower() or city.upper() in x.upper()
    #Second requirement
    res2 = ("türkiye" in x.lower())
    #Third requirement
    res3 = (x.strip() == "")
    return res1 or res2 or res3

def contains_country(countries, text):
    for country in countries:
        if country.lower() in text:
            return country
    return ""

def look_for_country(df, countries):
    rows_to_delete = []
    for index, row in df.iterrows():
        #If city is blank, try to fill it with the counrty info from the text.
        #If not, delete that entry.
        if row.iller == " ":
            country = contains_country(countries, row.metinTemiz.lower())
            if country != "":
                df.at[index, 'iller'] = country
            else:
                rows_to_delete.append(index)
    if(len(rows_to_delete) != 0):
        df.drop(rows_to_delete, inplace=True)
    return df

def validate_education_status(keywords, x):
    for keyword in keywords:
        if keyword.lower() in x.lower():
            return True
    return False

if __name__ == "__main__":
    if len(sys.argv) > 2:
        filename = os.path.join("preprocess", sys.argv[1])
    dfFirst = pd.read_excel(filename, sheet_name = sheetName)
    df = dfFirst.copy()
    #Fill NaN values as blanks
    df = df.fillna(' ')
    #Apply Rule 1
    df = df[df.jobrefno.apply(lambda x : False if type(x) == str else True)]
    df = df[df.Tecrube.apply(lambda x : False if type(x) == str else True)]
    df = df[df.maxTecrube.apply(lambda x : False if type(x) == str else True)]
    #Apply Rule 2
    df.Tecrube = df.Tecrube.apply(lambda x : 0 if x > 40 else x)
    #Apply Rule 3
    df.maxTecrube = df.maxTecrube.apply(lambda x : 99 if x == 0 or x >= 20 else x)
    df.maxTecrube = df.apply(lambda x : x.maxTecrube+3 if x.maxTecrube-x.Tecrube <= x.Tecrube else x.maxTecrube, axis=1)
    # Apply Rule 4
    df = df[df.iller.apply(lambda x : check_city_allowed(cities, x))]
    #Apply Rule 5
    df = look_for_country(df, countries)
    #Apply Rule 6 and 7
    df = df[df.EgitimDurumuUzun.apply(lambda x : x != " ")]
    df = df[df.EgitimDurumuUzun.apply(lambda x : validate_education_status(education_keywords, x))]
    
    df.to_excel("dataset.xlsx", sheet_name = sheetName)