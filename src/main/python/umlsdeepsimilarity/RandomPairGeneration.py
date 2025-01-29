# Unique Random Pair Generation for comparing UMLS similarities between algorithms
# Hint : https://www.quora.com/How-do-you-create-random-nonrepetitive-pairs-from-a-list-in-Python

import random
import itertools

# For an integer list with a size of N, there will be N * (N + 1) / 2 possible pairs.
def get_random_pairs(numbers):
    # Generate all possible non-repeating pairs
    pairs = list(itertools.combinations(numbers,2))
    # pairs = list(itertools.permutations(numbers,2)) for obtain pairwise set
    # Even pairs : itertools.combinations_with_replacement.

    # Randomly shuffle these pairs
    random.shuffle(pairs)
    return pairs

# an algorithm with runtime complexity (almost just) linear in output size with the use of random.sample and np.random.choice:
def pair_generator(concepts):
    """Return an iterator of random pairs from a list of numbers."""
    # Keep track of already generated pairs
    used_pairs = set()

    while True:
        pair = random.sample(concepts, 2)
        # Avoid generating both (1, 2) and (2, 1)
        pair = tuple(sorted(pair))
        if pair not in used_pairs:
            used_pairs.add(pair)
            yield pair



def main():
    # Example
    # A relatively long list
    #numbers = list(range(1000000))
    #gen = pair_generator(numbers)

    # Get 10 pairs:
    #for i in xrange(10):
    #    pair = gen.next()
    #    print(pair)


    #(1) Load UMLS concepts from Database, then convert column to List
    import pandas as pd
    import pymysql
    from sqlalchemy import create_engine

    engine = create_engine('mysql+pymysql://umls:bislaprom3!@chrome.kaist.ac.kr:3307/umls')

    df = pd.read_sql_query('SELECT CUI FROM (SELECT DISTINCT CUI FROM MRCONSO) a', engine)
    df.head()
    dfConceptList = df['CUI'].tolist()

    #(2) Generate Random 70 pairs * 100 set then save each pair set to file
    gen = pair_generator(dfConceptList)

    pairs = pd.DataFrame(columns=('sno','CUI1', 'CUI2'))
    for i in xrange(100):
        #pairs = pd.DataFrame(columns=('sno','CUI1', 'CUI2'))
        print(pairs.head())
        for j in xrange(70):
            pair = gen.next()
            pairs = pairs.append({'sno':str(j),'CUI1':pair[0], 'CUI2':pair[1]},ignore_index=True)

        #Save pairs to file
        #pairs.to_csv("randomPair70Sets_"+str(i)+".csv", sep=',', encoding='utf-8')

        #Print Dataframe
        #print(pairs.head())
    pairs.to_csv("randomPair70_100Set.csv", sep=',', encoding='utf-8')

if __name__ == "__main__":
    main()