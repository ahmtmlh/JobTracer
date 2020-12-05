import pickle
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.cluster import KMeans
from sklearn.cluster import DBSCAN

count_vect = None
tfidf_vect = None
KMeans_count_vect = None
KMeans_tfidf_vect = None

def loadModule(filename):
    f = open(filename, "rb")
    temp = pickle.load(f)
    f.close()
    return temp

def init_clustering():
    global tfidf_vect, count_vect, KMeans_count_vect, KMeans_tfidf_vect
    count_vect = loadModule("../pickle/count_vect_dump.pickle")
    tfidf_vect = loadModule("../pickle/tfidf_vectorizer_dump.pickle")
    KMeans_count_vect = loadModule("../pickle/count-cluster-kmeans_dump.pickle")
    KMeans_tfidf_vect = loadModule("../pickle/tfidf-cluster-kmeans_dump.pickle")


def __predict(messages, vectorizer, cluster):
    matrix = vectorizer.transform(messages)
    clusters = cluster.predict(matrix)
    return clusters

def predictCnt(messages):
    return __predict(messages, count_vect, KMeans_count_vect)
    
def predictTfidf(messages):
    return __predict(messages, tfidf_vect, KMeans_tfidf_vect)