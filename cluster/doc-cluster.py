from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.cluster import KMeans
from sklearn.cluster import DBSCAN
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import pickle
import sqlite3



# Binary serialisation
def dumpObjectToFile(obj, filename):
    file = open(filename, "wb")
    pickle.dump(obj, file)
    file.close()

'''
This function receives K-Means Cluster results (fit_transform) as well as other jobAd information (extra_info)
This will save the final result or generate debugging information, decided by the verbose parameter
If verbose parameter is given as 'True', it will require additional parameters such as:

matrix: Word Vectorizer Matrix. Either CountVectorizer or TF-IDF vectorizer
wordsLemmatized: List of lemmatized words. Input from the Information Extraction
figure1_desc: Description for the first figure. This figure will include K-MEANS clustering information
figure1_filename: Filename for the figure1 to save it. File extension is up to user, indicate desired file type (ex: png, jpeg, bmp). Matplotlib is used to save to file.
figure2_desc: Description for the second figure. This figure will include DBSCAN clustering information
figure2_filename: Filename for the figure2 to save it. File extension is up to user, indicate desired file type (ex: png, jpeg, bmp). Matplotlib is used to save to file.

Numpy is required for this operation to work
If this function is run in verbose mode, it will also save the file information in csv as well as xlsx. This is done to improve accesibility
Filename convention is as follows:
    
    [Verbose = True]
    cluster_dataset_$(name).xlsx
    cluster_dataset_$(name).db
    [Verbose = False]
    post_cluster_$(name).xlsx
    post_cluster_$(name).csv
        
If this function is run in verbose mode, contents of saved files will be different. Below example shows the difference in both modes.
Example:
    
    [Verbose = True]
    ID     | Clusters | Extra INFO...
    255000 | 1-3-5
    255001 | 2-6
    ...

    [Verbose = False]
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
    counts = extra_info[6]
    if not verbose:
        final_clusters_k_means = []
        i = 0
        for count in counts:
            c = count
            cluster_str = ''
            while c > 0:
                cluster_str = cluster_str + ',' + str(clusters_k_means[i])
                c = c-1
                i = i+1
            final_clusters_k_means.append(cluster_str[1:])
        clusters_k_means = final_clusters_k_means
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

def tfIdfVectorizedClustering(wordsLemmatized, stopwords, extra_info, verbose = True):
    # tf-idf vectorizing
    tfidf_vectorizer = TfidfVectorizer(max_df = 0.8, min_df = 0.05, stop_words = stopwords)
    tfidf_matrix = tfidf_vectorizer.fit_transform(wordsLemmatized)

    # KMeans Clustering
    num_clusters = 40
    cluster_k_means = KMeans(n_clusters=num_clusters).fit(tfidf_matrix)
    clusters_k_means = cluster_k_means.predict(tfidf_matrix)
    
    if not verbose:
        finalize('tfidf', verbose, extra_info, clusters_k_means)
    else:
        finalize('tfidf', verbose, extra_info, clusters_k_means, matrix=tfidf_matrix, wordsLemmatized=wordsLemmatized, figure1_desc='K-Means Clusters', figure1_filename='KMeans_clusters_tfidf.png', figure2_desc='DBSCAN Clusters', figure2_filename='DBSCAN_cluters_tfidf.png')

    # Save vectorizer and clustering objects 
    dumpObjectToFile(tfidf_vectorizer, "tfidf_vectorizer_dump.pickle")
    dumpObjectToFile(cluster_k_means, "tfidf-cluster-kmeans_dump.pickle")


def countVectorizerClustering(wordsLemmatized, stopwords, extra_info, verbose = True):
    
    count_vect = CountVectorizer(stop_words=stopwords)
    matrix = count_vect.fit_transform(wordsLemmatized)

    # KMeans Clustering
    num_clusters = 40
    cluster_k_means = KMeans(n_clusters=num_clusters).fit(matrix)
    clusters_k_means = cluster_k_means.predict(matrix)
    
    if not verbose:
        finalize('cnt', verbose, extra_info, clusters_k_means)
    else:
        finalize('cnt', verbose, extra_info, clusters_k_means, matrix=matrix, wordsLemmatized=wordsLemmatized, figure1_desc='K-Means Clusters', figure1_filename='KMeans_clusters_cnt.png', figure2_desc='DBSCAN Clusters', figure2_filename='DBSCAN_cluters_cnt.png')

    # Save vectorizer and clustering objects 
    dumpObjectToFile(count_vect, "count_vect_dump.pickle")
    dumpObjectToFile(cluster_k_means, "count-cluster-kmeans_dump.pickle")

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
    df = pd.read_csv("ie-result.lm.txt", encoding="utf-8", header=None, sep=';')
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

    tfIdfVectorizedClustering(wordsLemmatized, stopwords, extra_info)
    countVectorizerClustering(wordsLemmatized, stopwords, extra_info, verbose = False)