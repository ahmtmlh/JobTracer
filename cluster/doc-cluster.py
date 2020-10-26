from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.cluster import KMeans
from sklearn.cluster import DBSCAN
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import pickle

def Diff(li1, li2): 
    li_dif = [i for i in li1 + li2 if i not in li1 or i not in li2] 
    return li_dif 

# Binary serialisation
def dumpObjectToFile(obj, filename):
    file = open(filename, "wb")
    pickle.dump(obj, file)
    file.close()

def tfIdfVectorizedClustering(words, wordsLemmatized, stopwords, extra_info):
    # tf-idf vectorizing
    tfidf_vectorizer = TfidfVectorizer(max_df = 0.8, min_df = 0.05, stop_words = stopwords)
    tfidf_matrix = tfidf_vectorizer.fit_transform(wordsLemmatized)

    # KMeans Clustering
    num_clusters = 40
    clusters_k_means = KMeans(n_clusters = num_clusters).fit_predict(tfidf_matrix)

    #DBSCAN Clustering
    clusters_dbscan = DBSCAN(eps = 0.25, min_samples = 5).fit_predict(tfidf_matrix)

    data = {'ID': extra_info[0], 'Experience': extra_info[1], 'Max Experience': extra_info[2] , 'Job Info' : extra_info[3] ,'Text': words, "Lemmatized Text" : wordsLemmatized ,'Clusters(K Means)': clusters_k_means, 'Clusters(DBSCAN)': clusters_dbscan}
    frame = pd.DataFrame(data)
    frame.to_csv('post_cluster_kmeans_tfidf.csv', encoding = 'utf-8', index = False)
    frame.to_excel('post_cluster_kmeans_tfidf.xlsx', encoding='utf-8', index = False)

    unique, counts = np.unique(clusters_k_means, return_counts=True)
    dict(zip(unique, counts))

    plt.bar(x=unique,height=counts)
    plt.xticks(np.arange(len(unique)),unique)
    plt.ylabel('Num of Sentence')
    plt.title('K-Means clusters')
    plt.savefig("KMeans_Clust_tfidf.png")

    unique, counts = np.unique(clusters_dbscan, return_counts=True)
    dict(zip(unique, counts))

    plt.bar(x=unique,height=counts)
    plt.xticks(np.arange(len(unique)),unique)
    plt.ylabel('Num of Sentence')
    plt.title('DBSCAN clusters')
    plt.savefig("DBScan_Clust_tfidf.png")

    # Save vectorizer and clustering objects 
    dumpObjectToFile(tfidf_vectorizer, "tfidf_vectorizer_dump.pickle")
    dumpObjectToFile(clusters_k_means, "tfidf-cluster-kmeans_dump.pickle")


def countVectorizerClustering(words, wordsLemmatized, stopwords, extra_info):
    
    count_vect = CountVectorizer(stop_words=stopwords)
    matrix = count_vect.fit_transform(wordsLemmatized)

    # KMeans Clustering
    num_clusters = 40
    clusters_k_means = KMeans(n_clusters = num_clusters).fit_predict(matrix)

    #DBSCAN Clustering
    clusters_dbscan = DBSCAN(eps = 0.25, min_samples = 10).fit_predict(matrix)

    data = {'ID': extra_info[0], 'Experience': extra_info[1], 'Max Experience': extra_info[2] , 'Job Info' : extra_info[3], 'Text': words, "Lemmatized Text" : wordsLemmatized ,'Clusters(K Means)': clusters_k_means, 'Clusters(DBSCAN)': clusters_dbscan}
    frame = pd.DataFrame(data)
    frame.to_csv('post_cluster_kmeans_cnt.csv', encoding = 'utf-8', index = False)
    frame.to_excel('post_cluster_kmeans_cnt.xlsx', encoding='utf-8', index = False)

    unique, counts = np.unique(clusters_k_means, return_counts=True)
    dict(zip(unique, counts))

    plt.bar(x=unique,height=counts)
    plt.xticks(np.arange(len(unique)),unique)
    plt.ylabel('Num of Sentence')
    plt.title('K-Means clusters')
    plt.savefig("KMeans_Clust_cnt.png")

    unique, counts = np.unique(clusters_dbscan, return_counts=True)
    dict(zip(unique, counts))

    plt.bar(x=unique,height=counts)
    plt.xticks(np.arange(len(unique)),unique)
    plt.ylabel('Num of Sentence')
    plt.title('DBSCAN clusters')
    plt.savefig("DBScan_Clust_cnt.png")

    # Save vectorizer and clustering objects 
    dumpObjectToFile(count_vect, "count_vect_dump.pickle")
    dumpObjectToFile(clusters_k_means, "count-cluster-kmeans_dump.pickle")


# Main driver
if __name__ == '__main__':
    # read file
    df = pd.read_csv("ie-result.lm.txt", encoding="utf-8", header=None, sep=';')
    wordsLemmatized = df[df.columns[-1]].values.tolist()
    extra_info = [ df[df.columns[0]].values.tolist(), df[df.columns[1]].values.tolist(), df[df.columns[2]].values.tolist(), df[df.columns[3]].values.tolist() ]
    f = open("ie-result.txt", "r", encoding= "utf-8")
    words = f.read().split("\n")
    f.close()

    #Remove empty strings
    words = [w for w in words if w]
    wordsLemmatized = [wl for wl in wordsLemmatized if wl]

    #List of stop-words. These won't be calculated during vectorizing
    stopwords = ['tercihen', 'universite', 'universitelerin', 'üniversite', 'üniversitelerin']

    tfIdfVectorizedClustering(words, wordsLemmatized, stopwords, extra_info)
    countVectorizerClustering(words, wordsLemmatized, stopwords, extra_info)