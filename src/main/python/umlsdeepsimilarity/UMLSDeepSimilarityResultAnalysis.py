## Junseok Park, 2017-07-20
## For UMLS Deep Similarity Method Validation

import requests
import pandas as pd
import json

url = 'http://localhost:8001/umls-similarity/api/v1.47/getUMLSSimilarity'
data = '''{
    "measure":"path",
    "cui1":"C0010137",
    "cui2":"C0086511"
}'''


def loadPedersonExperimentData():
    df = pd.read_excel('files/conceptEmbeddingResults/Pederson_DataSet_WithMayoClinic.xlsx',header=0)
    return df


def getScore(data):
    response = requests.post(url,data=data,headers={"Content-Type":"application/json"})
    resultJson = response.json()['result']

    #for key in resultJson:
    #    print(key+":"+resultJson[key])
    return resultJson


def DataSetTest(df,methods):
    resultDict = {}

    for method in methods:
        resultList = []
        resultDict[method] = resultList
        #resultList = resultDict[method]
        for index, row in df.iterrows():
            print(method,":",row['CUI1'],row['CUI2'])
            data = json.JSONEncoder().encode({"measure":method,"cui1":row['CUI1'],"cui2":row['CUI2']})

            print(data)
            result = getScore(data)
            resultList.append(result['score'])
        resultDict[method] = resultList

    scoredf = pd.DataFrame(resultDict)

    resultdf = pd.concat([df,scoredf],axis=1)
    return resultdf

def loadCaviedesData():
    df = pd.read_excel('files/conceptEmbeddingResults/Caviedes_DataSet.xlsx')
    return df

def loadDataFromDB(query,columns):
    import mysql.connector as sql
    db_connection = sql.connect(host='chrome.kaist.ac.kr',port=3307,database='umls',user='umls',password='bislaprom3!')
    data = pd.read_sql(query, con=db_connection)
    return pd.DataFrame(data,columns=columns)

def generateRandomSet(numberOfRandomSet):
    query = "SELECT CUI as CUI1 FROM MRCONSO ORDER BY RAND() LIMIT "+str(numberOfRandomSet)
    df_cui1 = loadDataFromDB(query,["CUI1"])
    query = "SELECT CUI as CUI2 FROM MRCONSO ORDER BY RAND() LIMIT "+str(numberOfRandomSet)
    df_cui2 = loadDataFromDB(query,["CUI2"])
    resultdf = pd.concat([df_cui1,df_cui2],axis=1)
    return resultdf


def main():
    # Experiment 1 : Random Selection for coverage comparison
    #for i in range(0,10):
    #    df = generateRandomSet(1000)
    #    methods = ["vector"]
    #    resultdf = DataSetTest(df,methods)
    #    resultdf.to_csv('data/randomCUISet_'+str(i+1)+'_vector.csv',sep=',')
    #    df.to_csv('data/randomCUISet_'+str(i+1)+'.csv',sep=',')

    # Experiment 2


    # Experiment 2 : Pederson
    # Load Data
    #df = loadPedersonExperimentData()
    #methods=["path","batet","cdist","faith","jcn","lch","wup","lin","nam","pks","random","res","sanchez","upath","zhong","vector","lesk"]
    #resultdf = DataSetTest(df,methods)

    #print(resultdf.head())

    #resultdf.to_csv('files/conceptEmbeddingResults/Pederson_DataSet_WithMayoClinic_Scores_20170812.csv',sep=',')

    ## Experiment 3 : Caviedes
    #df = loadCaviedesData()
    #methods=["cdist","vector"]
    #resultdf = DataSetTest(df,methods)


    #import psycopg2
    #conn = psycopg2.connect(host="localhost", port="5441", database="hpotemp", user="hpotemp", password="corus2018!")
    #df= pd.read_sql_query('select cui_l as CUI1, cui_r as CUI2, score from hpo_source',conn)
    #df.columns = ['CUI1','CUI2','score']

    df = pd.read_csv('files/ournal.pone.0099415.s005WithCUI_compact.csv')
    methods=["lesk","vector"]
    resultdf= DataSetTest(df,methods)

    #resultdf.head()
    resultdf.to_csv('files/conceptEmbeddingResults/result_20180411-000/PlosOne_Lesk_Vector_Result.csv',sep=',')

    #conn.close()

if __name__ == "__main__":
    main()