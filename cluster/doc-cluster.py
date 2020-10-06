from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.cluster import KMeans
from sklearn.cluster import DBSCAN
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

# read file
f = open("ie-result.lm.txt", "r", encoding="utf-8")
wordsLemmatized = f.read().split("\n")
f.close()
f = open("ie-result.txt", "r", encoding= "utf-8")
words = f.read().split("\n")
f.close()

#List of stop-words. These won't be calculated during Tf-Idf
stopwords = ['tercihen']

# tf-idf vectorizing
tfidf_vectorizer = TfidfVectorizer(max_df = 0.8, min_df = 0.05, stop_words = stopwords)
tfidf_matrix = tfidf_vectorizer.fit_transform(wordsLemmatized)

# KMeans Clustering
num_clusters = 6
clusters_k_means = KMeans(n_clusters = num_clusters).fit_predict(tfidf_matrix)

#DBSCAN Clustering
clusters_dbscan = DBSCAN(eps = 0.5, min_samples = 10).fit_predict(tfidf_matrix)

data = {'Text': words, "Lemmatized Text" : wordsLemmatized ,'Clusters(K Means)': clusters_k_means, 'Clusters(DBSCAN)': clusters_dbscan}
frame = pd.DataFrame(data)
frame.to_csv('post_cluster_kmeans.csv', encoding = 'utf-8', index = False)
frame.to_excel('post_cluster_kmeans.xlsx', encoding='utf-8', index = False)

unique, counts = np.unique(clusters_k_means, return_counts=True)
dict(zip(unique, counts))

plt.bar(x=unique,height=counts)
plt.xticks(np.arange(len(unique)),unique)
plt.ylabel('Num of Sentence')
plt.title('K-Means clusters')
plt.savefig("KMeans_Clust.png")

unique, counts = np.unique(clusters_dbscan, return_counts=True)
dict(zip(unique, counts))

plt.bar(x=unique,height=counts)
plt.xticks(np.arange(len(unique)),unique)
plt.ylabel('Num of Sentence')
plt.title('DBSCAN clusters')
plt.savefig("DBScan_Clust.png")