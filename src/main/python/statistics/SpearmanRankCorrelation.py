import psycopg2
import pandas as pd

conn = psycopg2.connect(host="localhost", port="5441", database="hpotemp", user="hpotemp", password="corus2018!")

cur = conn.cursor()

print('PostgreSQL database version:')
cur.execute('SELECT version()')

db_version = cur.fetchone()
print(db_version)

#df = pd.read_sql_query('select * from hpo_source', conn)
#df_result = pd.read_sql_query('select * from hpo_result', conn)

df_joinset = pd.read_sql_query('select a.cui_l, a.cui_r, '
                               'a.score hpo_score, a.rank hpo_rank, a.rank_wnan hpo_nrank, '
                               'b.score result_score, b.rank result_rank, b.rank_wnan result_nrank '
                               'from hpo_source a, hpo_result b '
                               'where (a.cui_l=b.cui_l and a.cui_r=b.cui_r) ', conn)

hpo_nranks = df_joinset["hpo_nrank"].values
result_nranks = df_joinset["result_nrank"].values

df_joinset_wnan = pd.read_sql_query('select a.cui_l, a.cui_r, '
                                    'a.score hpo_score, a.rank hpo_rank, a.rank_wnan hpo_nrank, '
                                    'b.score result_score, b.rank result_rank, b.rank_wnan result_nrank '
                                    'from hpo_source a, hpo_result b '
                                    'where (a.cui_l=b.cui_l and a.cui_r=b.cui_r) and b.score != \'NaN\' ', conn)

import scipy.stats

scipy.stats.spearmanr(hpo_nranks,result_nranks)

cur.close()
