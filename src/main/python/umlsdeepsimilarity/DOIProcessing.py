import pandas as pd

df_doid = pd.read_csv("data/DOID.csv", encoding = "ISO-8859-1")
df_gset = pd.read_excel("data/journal.pone.0099415.s005.XLSX")


def getCUI(doid):
    row = df_doid.loc[df_doid['core_notation'] == doid]
    cui = row.iloc[0]['database_cross_reference']
    cui_split = cui.split("|")
    cui_split_idx = [cui_split.index(i) for i in cui_split if 'UMLS_CUI' in i][0]

    return cui_split[cui_split_idx].split(":")[1]


df_gset['CUI1'] = df_gset.apply(lambda row: getCUI(row.DOID_1), axis=1)
df_gset['CUI2'] = df_gset.apply(lambda row: getCUI(row.DOID_2), axis=1)

df_gset.to_csv('data/ournal.pone.0099415.s005WithCUI.csv',sep=',')