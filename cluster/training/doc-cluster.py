import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import pickle
import sqlite3
from sklearn.cluster import DBSCAN
from sklearn.cluster import KMeans
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.feature_extraction.text import TfidfVectorizer

# Binary serialisation
def dumpObjectToFile(obj, filename):
    file = open(filename, "wb")
    pickle.dump(obj, file)
    file.close()

'''
UNUSED

This function receives K-Means Cluster results (fit_transform) as well as other jobAd information (extra_info)
This will save the final result or generate debugging information, decided by the verbose parameter
If verbose parameter is given as 'True', it will require additional parameters such as:

matrix: Word Vectorizer Matrix. Either CountVectorizer or TF-IDF vectorizer
wordsLemmatized: List of lemmatized words. Input from the Information Extraction
figure1_desc: Description for the first figure. This figure will include K-MEANS clustering information
figure1_filename: Filename for the figure1 to save it. File extension is up to user, indicate desired file type (ex: png, jpeg, bmp). Matplotlib is used to save to file.
figure2_desc: Description for the second figure. This figure will include DBSCAN clustering information
figure2_filename: Filename for the figure2 to save it. File extension is up to user, indicate desired file type (ex: png, jpeg, bmp). Matplotlib is used to save to file.
save_xlsx: Save results in a xlsx file in non-verbose mode. This function is valid only if @param verbose == False

Numpy is required for this operation to work
If this function is run in verbose mode, it will also save the file information in csv as well as xlsx. This is done to improve accesibility
If this function was run in non-verbose mode, it will save an xslx file according to save_xlsx parameter. Default value is True
Filename convention is as follows:
    
    [Verbose = False]
        if save_xlsx = True
        cluster_dataset_$(name).xlsx
    cluster_dataset_$(name).db

    [Verbose = True]
    post_cluster_$(name).xlsx
    post_cluster_$(name).csv
        
If this function is run in verbose mode, contents of saved files will be different. Below example shows the difference in both modes.
Example:
    
    [Verbose = False]
    ID     | Clusters | Extra INFO...
    255000 | 1-3-5
    255001 | 2-6
    ...

    [Verbose = True]
    ID     | Clusters(K-Means & DBSCAN) | Lemmatized Word (Result from IE)
    255000 | 1                          | Text
    255000 | 3                          | Text
    255000 | 5                          | Text
    255001 | 2                          | Text
    255001 | 6                          | Text
    ...
    Extra Info such as experience, jobInfo, educationStatus, etc. will not be saved in order to reduce disk space and run-time. Job-ID seems enough for this purpose

If this function is not run in verbose mode, return value will be the same clustering information that is received.

This function will not serialize vectorizer and cluster
'''
def finalize(name, verbose, extra_info, clusters_k_means, save_xlsx = True, matrix = None, wordsLemmatized = None, figure1_desc = None, figure1_filename = None, figure2_desc = None, figure2_filename = None):
    # Number words (list item) in total. Lenght of this
    # Each count in the list indicates the total number of words for a specific Job Advertisement.
    counts = extra_info[6]
    if not verbose:
        clusters_k_means = clusters_as_string(clusters_k_means, counts)
        data = {'id': extra_info[0], 'exp': extra_info[1], 'max_exp': extra_info[2], 'position': extra_info[3], 'cities': extra_info[4], 'ed_status': extra_info[5], 'clusters': clusters_k_means}
        
        frame = pd.DataFrame(data)

        if save_xlsx:
            frame.to_excel('cluster_dataset_'+ str(name) + '.xlsx', encoding='utf-8', index=False)

        conn = sqlite3.connect('cluster_dataset_' + str(name) + '.db')
        c = conn.cursor()
        c.execute(
        ''' 
            DROP TABLE IF EXISTS DATA
        ''')
        c.execute(
        ''' 
            CREATE TABLE DATA(
                id INTEGER PRIMARY KEY,
                exp INTEGER NOT NULL,
                max_exp INTEGER NOT NULL,
                position TEXT NOT NULL,
                cities TEXT NOT NULL,
                ed_status INTEGER NOT NULL,
                clusters TEXT NOT NULL
            ) WITHOUT ROWID
        ''')
        c.executescript(
        '''
            CREATE INDEX exp_index ON DATA(exp);
            CREATE INDEX max_exp_index ON DATA(max_exp);
            CREATE INDEX position_index ON DATA(position);
            CREATE INDEX ed_status_index ON DATA(ed_status);
        ''')
        frame.to_sql('DATA', conn, if_exists='append', index=False)
        conn.close()

    else:
        #DBSCAN Clustering
        clusters_dbscan = DBSCAN(eps = 0.25, min_samples = 5).fit_predict(matrix)
        ids = extra_info[0]
        new_ids = []
        i = 0
        for count in counts:
            for j in range(count):
                new_ids.append(ids[i])
            i = i+1
        # For debug purposes only. This data is not meant to be parsed by the IE Matcher service
        data = {'ID': new_ids, "Lemmatized Text" : wordsLemmatized ,'Clusters(K Means)': clusters_k_means, 'Clusters(DBSCAN)': clusters_dbscan}
        frame = pd.DataFrame(data)
        frame.to_csv('post_cluster_' + str(name) +'.csv', encoding = 'utf-8', index = False)
        frame.to_excel('post_cluster_' + str(name) + '.xlsx', encoding='utf-8', index = False)

        unique, counts = np.unique(clusters_k_means, return_counts=True)
        dict(zip(unique, counts))

        plt.bar(x=unique,height=counts)
        plt.xticks(np.arange(len(unique)),unique)
        plt.ylabel('Num of Sentence')
        plt.title(figure1_desc)
        plt.savefig(figure1_filename)

        unique, counts = np.unique(clusters_dbscan, return_counts=True)
        dict(zip(unique, counts))

        plt.bar(x=unique,height=counts)
        plt.xticks(np.arange(len(unique)),unique)
        plt.ylabel('Num of Sentence')
        plt.title(figure2_desc)
        plt.savefig(figure2_filename)

'''
UNUSED
'''
def tfIdfVectorizedClustering(wordsLemmatized, stopwords, extra_info, verbose = True, save_xlsx = True):
    # tf-idf vectorizing
    tfidf_vectorizer = TfidfVectorizer(stop_words = stopwords)
    tfidf_matrix = tfidf_vectorizer.fit_transform(wordsLemmatized)

    # KMeans Clustering
    num_clusters = 40
    cluster_k_means = KMeans(n_clusters=num_clusters).fit(tfidf_matrix)
    clusters_k_means = cluster_k_means.predict(tfidf_matrix)

    print("Len Clusters_k_means:", len(clusters_k_means))
    
    finalize('tfidf', verbose, extra_info, clusters_k_means, save_xlsx = save_xlsx, matrix=tfidf_matrix, wordsLemmatized=wordsLemmatized, figure1_desc='K-Means Clusters', figure1_filename='KMeans_clusters_tfidf.png', figure2_desc='DBSCAN Clusters', figure2_filename='DBSCAN_cluters_tfidf.png')
        
    # Save vectorizer and clustering objects 
    dumpObjectToFile(tfidf_vectorizer, "tfidf_vectorizer_dump.pickle")
    dumpObjectToFile(cluster_k_means, "tfidf-cluster-kmeans_dump.pickle")

'''
UNUSED
'''
def countVectorizerClustering(wordsLemmatized, stopwords, extra_info, verbose = True, save_xlsx = True):
    
    count_vect = CountVectorizer(stop_words=stopwords)
    matrix = count_vect.fit_transform(wordsLemmatized)

    # KMeans Clustering
    num_clusters = 40
    cluster_k_means = KMeans(n_clusters=num_clusters).fit(matrix)
    clusters_k_means = cluster_k_means.predict(matrix)
 
    finalize('cnt', verbose, extra_info, clusters_k_means, matrix=matrix, save_xlsx = save_xlsx, wordsLemmatized=wordsLemmatized, figure1_desc='K-Means Clusters', figure1_filename='KMeans_clusters_cnt.png', figure2_desc='DBSCAN Clusters', figure2_filename='DBSCAN_cluters_cnt.png')

    # Save vectorizer and clustering objects 
    dumpObjectToFile(count_vect, "count_vect_dump.pickle")
    dumpObjectToFile(cluster_k_means, "count-cluster-kmeans_dump.pickle")


"""
    This function receives clusters in a list (each cluster as an item in list), and counts of each cluster in an JobAd
    Uses counts to transform clusters to a comma seperated string.
    Example:
        clusters: [1,2,3,4,5,6,7,8,9,10]
        counts = [3,2,5]
        Arrays will produce a final list looking like:
        final_clusters = ['1,2,3', '4,5', '6,7,8,9,10']
"""
def clusters_as_string(clusters, counts):
    final_clusters = []
    i = 0
    # For each Job Ad, create a cluster string. This data will be saved to one single database, rather than a relation of jobs and clusters
    for count in counts:
        c = count
        cluster_str = ''
        while c > 0:
            cluster_str = cluster_str + ',' + str(clusters[i])
            c = c-1
            i = i+1
        final_clusters.append(cluster_str[1:])
    return final_clusters

def create_job_cities_table(conn, cities):
    c = conn.cursor()
    c.executescript(
    '''
        DROP TABLE IF EXISTS CITIES;
        CREATE TABLE CITIES(
            id INTEGER PRIMARY KEY,
            name TEXT NOT NULL
        );
        CREATE INDEX city_name_index ON CITIES(name);
        DROP TABLE IF EXISTS JOB_CITIES;
        CREATE TABLE JOB_CITIES(
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            job_id INTEGER NOT NULL,
            city_id INTEGER NOT NULL,
            FOREIGN KEY(job_id) REFERENCES JOB_DATA(id),
            FOREIGN KEY(city_id) REFERENCES CITIES(id)
        );
        CREATE INDEX jc_job_id_index ON JOB_CITIES(job_id);
        CREATE INDEX jc_city_id_index ON JOB_CITIES(city_id);
    ''')
    
    cities.to_sql('CITIES', conn, if_exists='append', index=False)

def populate_job_cities_table(conn, cities, df):
    count = 0
    c = conn.cursor()
    for index, row in df.iterrows():
        temp = str(row['cities']).split(',')
        job_id = row['job_id']
        for city in temp:
            city_id = cities[cities['name']==city].index.values[0]
            query = 'INSERT INTO JOB_CITIES(job_id, city_id) VALUES({}, {})'.format(job_id, city_id)
            c.execute(query)
            count = count + 1
    conn.commit()
    print('Total rows inserted: ', count)

def create_university_tables(conn):
    c = conn.cursor()
    c.executescript(
    '''
        DROP TABLE IF EXISTS UNIVERSITIES;
        CREATE TABLE UNIVERSITIES(
            id INTEGER PRIMARY KEY,
            name TEXT NOT NULL
        );
        DROP TABLE IF EXISTS FACULTIES;
        CREATE TABLE FACULTIES(
            id INTEGER PRIMARY KEY,
            uni_id INTEGER NOT NULL,
            name TEXT NOT NULL,
            FOREIGN KEY(uni_id) REFERENCES UNIVERSITIES(id)
        );
        CREATE INDEX fac_uni_id_index ON FACULTIES(uni_id);
        DROP TABLE IF EXISTS DEPARTMENTS;
        CREATE TABLE DEPARTMENTS(
            id INTEGER PRIMARY KEY,
            fac_id INTEGER NOT NULL,
            name TEXT NOT NULL,
            FOREIGN KEY(fac_id) REFERENCES FACULTIES(id)
        );
        CREATE INDEX dep_fac_id_index ON DEPARTMENTS(fac_id);
    '''
    )
    
    uni_df = pd.read_excel('../../data/universite.xls')
    uni_df.drop(['status'], axis = 1, inplace = True)
    uni_df.columns = ['id', 'name']
    uni_df.to_sql('UNIVERSITIES', conn, if_exists='append', index = False)

    fac_df = pd.read_excel('../../data/fakulte.xls')
    fac_df.drop(['status'], axis = 1, inplace = True)
    fac_df.columns = ['id', 'uni_id', 'name']
    fac_df.to_sql('FACULTIES', conn, if_exists='append', index = False)

    dep_df = pd.read_excel('../../data/bolum.xlsx')
    dep_df.drop(['status', 'universite_id'], axis = 1, inplace = True)
    dep_df.columns = ['fac_id', 'name']
    dep_df['id'] = np.arange(1,dep_df['name'].size + 1)
    dep_df.to_sql('DEPARTMENTS', conn, if_exists='append', index = False)

"""
    This functions works like finalize, but in non-verbose mode only. This function also creates a table
    in the database for original job advertisement texts, if original dataset_filename is given.
"""
def finalizeCompact(extra_info, clusters_kmeans_tfidf, clusters_kmeans_cnt, dataset = None):
    data_df = pd.read_excel(dataset)
    diff = set(data_df[data_df.columns[2]].values.tolist()) - set(extra_info[0])
    data_df = data_df[data_df[data_df.columns[2]].apply(lambda x: False if x in diff else True)]

    # Create and key value pair for index read from original data file, 
    # key: indexes, value: indexing of the actual list. [0, len(index))
    ids_df = data_df[data_df.columns[2]].values.tolist()
    ids_df = {k:v for v, k in enumerate(ids_df)}
    text_df = data_df[data_df.columns[7]].values.tolist()
    text_cleared_df = data_df[data_df.columns[9]].values.tolist()
    
    text = []
    text_cleared = [] 

    # Find the correct text from the index placement of extra_info, since all the
    # other information are included in that pack aswell.
    for id in extra_info[0]:
        # Get the array index of 'id' from extra_info pack.
        # Use that index to find the text and text_cleared from the data list
        idx = ids_df[id]
        text.append(text_df[idx])
        text_cleared.append(text_cleared_df[idx])

    print('Data Size(id): {}, Size: {}'.format(len(ids), len(extra_info[0])))
    print('Data Size(text): {}, Size: {}'.format(len(text), len(extra_info[0])))
    
    conn = sqlite3.connect('../../data/data.db')
    c = conn.cursor()

    data = {'id': extra_info[0], 'exp': extra_info[1], 'max_exp': extra_info[2], 'position': extra_info[3],
            'ed_status': extra_info[5], 'text' : text, 'text_clear': text_cleared}

    frame = pd.DataFrame(data)

    c.executescript(
    '''
        DROP TABLE IF EXISTS JOB_DATA;
        CREATE TABLE JOB_DATA(
            id INTEGER PRIMARY KEY,
            exp INTEGER NOT NULL,
            max_exp INTEGER NOT NULL,
            position TEXT NOT NULL,
            ed_status INTEGER NOT NULL,
            text TEXT NOT NULL,
            text_clear TEXT NOT NULL
        );

        CREATE INDEX job_exp_index ON JOB_DATA(exp);
        CREATE INDEX job_max_exp_index ON JOB_DATA(max_exp);
        CREATE INDEX job_position_index ON JOB_DATA(position);
        CREATE INDEX job_ed_status_index ON JOB_DATA(ed_status);
    ''')

    frame.to_sql('JOB_DATA', conn, if_exists='append', index=False)
    
    counts = extra_info[6]
    clusters_kmeans_tfidf = clusters_as_string(clusters_kmeans_tfidf, counts)
    clusters_kmeans_cnt = clusters_as_string(clusters_kmeans_cnt, counts)

    data1 = {'job_id': extra_info[0], 'clusters_tfidf': clusters_kmeans_tfidf, 'clusters_cnt': clusters_kmeans_cnt}
        
    frame1 = pd.DataFrame(data1)

    c.executescript(
    ''' 
        DROP TABLE IF EXISTS CLUSTER_DATA;
        CREATE TABLE CLUSTER_DATA(
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            job_id INTEGER NOT NULL,
            clusters_tfidf TEXT NOT NULL,
            clusters_cnt TEXT NOT NULL,
            FOREIGN KEY(job_id) REFERENCES JOB_DATA(id)
        );
        CREATE INDEX cl_job_id_index ON CLUSTER_DATA(job_id);
    ''')

    frame1.to_sql('CLUSTER_DATA', conn, if_exists='append', index=False)
    
    cities = extra_info[4]
    cities_set = set()
    for city in cities:
        _cities = city.split(',')
        for c in _cities:
            cities_set.add(c)

    cities_unique = list(cities_set)
    cities_df = pd.DataFrame({'id':np.arange(len(cities_unique)), 'name': cities_unique})

    create_job_cities_table(conn, cities_df)
    populate_job_cities_table(conn, cities_df, pd.DataFrame({'job_id': extra_info[0], 'cities': extra_info[4]}))

    create_university_tables(conn)

    conn.close()


def clustering(wordsLemmatized, stopwords, extra_info, dataset):
    # Tf-Idf Vectorizing
    tfidf_vectorizer = TfidfVectorizer(stop_words = stopwords)
    tfidf_matrix = tfidf_vectorizer.fit_transform(wordsLemmatized)
    # Count Vectorizing
    count_vect = CountVectorizer(stop_words=stopwords)
    matrix = count_vect.fit_transform(wordsLemmatized)

    # Clustering K-Means Only
    # Tf-IDF
    num_clusters = 40
    kmeans_tfidf = KMeans(n_clusters=num_clusters).fit(tfidf_matrix)
    clusters_kmeans_tfidf = kmeans_tfidf.predict(tfidf_matrix)
    # Count
    kmeans_cnt = KMeans(n_clusters=num_clusters).fit(matrix)
    clusters_kmeans_cnt = kmeans_cnt.predict(matrix)

    finalizeCompact(extra_info, clusters_kmeans_tfidf, clusters_kmeans_cnt, dataset = dataset)

    # Save vectorizer and clustering objects 
    dumpObjectToFile(tfidf_vectorizer, "../pickle/tfidf_vectorizer_dump.pickle")
    dumpObjectToFile(kmeans_tfidf, "../pickle/tfidf-cluster-kmeans_dump.pickle")
    dumpObjectToFile(count_vect, "../pickle/count_vect_dump.pickle")
    dumpObjectToFile(kmeans_cnt, "../pickle/count-cluster-kmeans_dump.pickle")


def parseList(_list):
    new_list = []
    counts = []
    for _item in _list:
        _item = _item[1:-1]
        items = _item.split('|')
        new_list.extend(items)
        counts.append(len(items))
    return new_list, counts


# Main driver
if __name__ == '__main__':
    # read file
    df = pd.read_csv("../../data/ie-result.lm.txt", encoding="utf-8", header=None, sep=';')
    wordsLemmatized = df[df.columns[-1]].values.tolist()
    wordsLemmatized, counts = parseList(wordsLemmatized)
    ids = df[df.columns[0]].values.tolist()
    exp = df[df.columns[1]].values.tolist()
    maxExp = df[df.columns[2]].values.tolist()
    jobInfo = df[df.columns[3]].values.tolist()
    edStatus = df[df.columns[5]].values.tolist()

    # Replace is done because IE is done with seperator '|', while the end result must receive ','
    cities = df[df.columns[4]].values.tolist()
    cities = [c.replace('|', ',') for c in cities]
    
    extra_info = [ ids, exp, maxExp, jobInfo, cities, edStatus, counts ]

    #Remove empty strings
    wordsLemmatized = [wl for wl in wordsLemmatized if wl]

    #List of stop-words. These won't be calculated during vectorizing, but they were needed by IE service before clustering
    stopwords = ['tercihen', 'universite', 'universitelerin', 'üniversite', 'üniversitelerin']

    #tfIdfVectorizedClustering(wordsLemmatized, stopwords, extra_info, verbose=False, save_xlsx = False)
    #countVectorizerClustering(wordsLemmatized, stopwords, extra_info, verbose = False, save_xlsx = False)

    clustering(wordsLemmatized, stopwords, extra_info, dataset='../../data/dataset.xlsx')