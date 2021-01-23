from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.cluster import KMeans
from sklearn.cluster import DBSCAN
import pandas as pd
import numpy as np
import pickle
import sqlite3

def parseList(_list):
  new_list = []
  counts = []
  for _item in _list:
      _item = _item[1:-1]
      items = _item.split('|')
      new_list.extend(items)
      counts.append(len(items))
  return new_list, counts

# Binary serialisation
def dumpObjectToFile(obj, filename):
  file = open(filename, "wb")
  pickle.dump(obj, file)
  file.close()


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
  
  uni_df = pd.read_excel('"../../data/universiteler.xlsx')
  uni_df.columns = ['id', 'name']
  uni_df.to_sql('UNIVERSITIES', conn, if_exists='append', index = False)

  fac_df = pd.read_excel('"../../data/fakulteler.xlsx')
  fac_df.columns = ['id', 'uni_id', 'name']
  fac_df.to_sql('FACULTIES', conn, if_exists='append', index = False)

  dep_df = pd.read_excel('"../../data/bolumler.xlsx')
  dep_df.columns = ['id', 'fac_id', 'name']
  dep_df.to_sql('DEPARTMENTS', conn, if_exists='append', index = False)

def create_language_table(conn):
  c = conn.cursor()
  c.executescript(
  '''
    DROP TABLE IF EXISTS LANGUAGES;
    CREATE TABLE LANGUAGES(
      id INTEGER PRIMARY KEY,
      name TEXT NOT NULL
    );
  '''
  )

  languages_df = pd.read_excel('"../../data/diller.xlsx')
  languages_df.columns = ['name']
  languages_df['id'] = np.arange(1,len(languages_df.index)+1)
  languages_df.to_sql('LANGUAGES', conn, if_exists='append', index = False)

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


def finalizeCompact(extra_info, clusters_kmeans_tfidf, clusters_kmeans_cnt, dataset):
  data_df = pd.read_excel(dataset)
  diff = set(data_df[data_df.columns[2]].values.tolist()) - set(extra_info[0])
  data_df = data_df[data_df[data_df.columns[2]].apply(lambda x: False if x in diff else True)]

  # Create and key value pair for index read from original data file, 
  # key: job_id (indexes), value: index of the job_id in the list. [0, len(id))
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
  
  conn = sqlite3.connect('"../../data/data.db')
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
  create_language_table(conn)

  conn.close()

def clustering(wordsLemmatized, stopwords, extra_info, dataset):
  # Tf-Idf Vectorizing
  tfidf_vectorizer = TfidfVectorizer(stop_words = stopwords)
  tfidf_matrix = tfidf_vectorizer.fit_transform(wordsLemmatized)

  # Count Vectorizing
  count_vect = CountVectorizer(stop_words=stopwords)
  matrix = count_vect.fit_transform(wordsLemmatized)
  
  # Clustering K-Means Only
  # TF-IDF
  num_clusters = 120
  kmeans_tfidf = KMeans(n_clusters=num_clusters).fit(tfidf_matrix)
  clusters_kmeans_tfidf = kmeans_tfidf.predict(tfidf_matrix)
  
  # Count
  kmeans_cnt = KMeans(n_clusters=num_clusters).fit(matrix)
  clusters_kmeans_cnt = kmeans_cnt.predict(matrix)

  finalizeCompact(extra_info, clusters_kmeans_tfidf, clusters_kmeans_cnt, dataset)
  
  # Save vectorizer and clustering objects 
  dumpObjectToFile(tfidf_vectorizer, "../pickle/tfidf_vectorizer_dump.pickle")
  dumpObjectToFile(kmeans_tfidf, "../pickle/tfidf-cluster-kmeans_dump.pickle")
  dumpObjectToFile(count_vect, "../pickle/count_vect_dump.pickle")
  dumpObjectToFile(kmeans_cnt, "../pickle/count-cluster-kmeans_dump.pickle")

if __name__ == '__main__':
  #read file
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

  clustering(wordsLemmatized, stopwords, extra_info, dataset='../../data/dataset.xlsx')