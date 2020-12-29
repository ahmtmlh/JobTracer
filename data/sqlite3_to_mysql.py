import sqlite3
from mysql.connector import (connection)
import sys

def exec_mysql(c, sql):
    c.execute(sql)

def create_tables(mysqlDb):
    c = mysqlDb.cursor()
    exec_mysql(c, "DROP TABLE IF EXISTS JOB_CITIES")
    exec_mysql(c, "DROP TABLE IF EXISTS CITIES")
    exec_mysql(c, "DROP TABLE IF EXISTS CLUSTER_DATA")
    exec_mysql(c, "DROP TABLE IF EXISTS JOB_DATA")
    exec_mysql(c, "DROP TABLE IF EXISTS DEPARTMENTS")
    exec_mysql(c, "DROP TABLE IF EXISTS FACULTIES")
    exec_mysql(c, "DROP TABLE IF EXISTS UNIVERSITIES")
    exec_mysql(c, "DROP TABLE IF EXISTS LANGUAGES")
    exec_mysql(c, "DROP TABLE IF EXISTS DRIVING_LICENSES")

    exec_mysql(c, "CREATE TABLE JOB_DATA("+
            "id INT(11) PRIMARY KEY," +
            "exp INT(11) NOT NULL," +
            "max_exp INT(11) NOT NULL,"+
            "position nvarchar(255) NOT NULL,"+
            "ed_status INT(11) NOT NULL,"+
            "text TEXT NOT NULL,"+
            "text_clear TEXT NOT NULL)" )
    exec_mysql(c, "CREATE TABLE CLUSTER_DATA(" +
            "id INT(11) PRIMARY KEY AUTO_INCREMENT," +
            "job_id INT(11) NOT NULL UNIQUE," +
            "clusters_tfidf nvarchar(1024) NOT NULL," +
            "clusters_cnt nvarchar(1024) NOT NULL," +
            "FOREIGN KEY(job_id) REFERENCES JOB_DATA(id)" +
            ")")
    exec_mysql(c, "CREATE TABLE CITIES("+
                  "id INT(11) PRIMARY KEY,"+
                  "name nvarchar(255) NOT NULL"+
                  ")")
    exec_mysql(c, "CREATE TABLE JOB_CITIES("+
            "id INT(11) PRIMARY KEY AUTO_INCREMENT,"+
            "job_id INT(11) NOT NULL,"+
            "city_id INT(11) NOT NULL,"+
            "FOREIGN KEY(job_id) REFERENCES JOB_DATA(id),"+
            "FOREIGN KEY(city_id) REFERENCES CITIES(id)"+
        ")")
    exec_mysql(c, "CREATE TABLE UNIVERSITIES("+
            "id INT(11) PRIMARY KEY,"+
            "name nvarchar(255) NOT NULL"+
        ")")
    exec_mysql(c, "CREATE TABLE FACULTIES("+
            "id INT(11) PRIMARY KEY,"+
            "uni_id INT(11) NOT NULL,"+
            "name nvarchar(255) NOT NULL,"+
            "FOREIGN KEY(uni_id) REFERENCES UNIVERSITIES(id)"+
        ")")

    exec_mysql(c, "CREATE TABLE DEPARTMENTS("+
            "id INT(11) PRIMARY KEY,"+
            "fac_id INT(11) NOT NULL,"+
            "name nvarchar(255) NOT NULL,"+
            "FOREIGN KEY(fac_id) REFERENCES FACULTIES(id)"+
        ")")

    exec_mysql(c, "CREATE TABLE LANGUAGES("+
            "id INT(11) PRIMARY KEY,"+
            "name nvarchar(255) NOT NULL"+
        ")")
    
    exec_mysql(c, "CREATE TABLE DRIVING_LICENSES("+
            "id INT(11) PRIMARY KEY,"+
            "type nvarchar(255) NOT NULL"+
        ")")
    
    mysqlDb.commit()
    c.close()

def create_indexes(mysqlDb):
    c = mysqlDb.cursor()
    
    exec_mysql(c, "CREATE INDEX job_exp_index ON JOB_DATA(exp)")
    exec_mysql(c, "CREATE INDEX job_max_exp_index ON JOB_DATA(max_exp)")
    exec_mysql(c, "CREATE INDEX job_pos_index ON JOB_DATA(position)")
    exec_mysql(c, "CREATE INDEX job_ed_stat_index ON JOB_DATA(ed_status)")
    exec_mysql(c, "CREATE INDEX cl_job_id_index ON CLUSTER_DATA(job_id)")    
    exec_mysql(c, "CREATE INDEX city_name_index ON CITIES(name)")
    exec_mysql(c, "CREATE INDEX jc_job_id_index ON JOB_CITIES(job_id)")
    exec_mysql(c, "CREATE INDEX jc_city_id_index ON JOB_CITIES(city_id)")
    exec_mysql(c, "CREATE INDEX fac_uni_id_index ON FACULTIES(uni_id)")
    exec_mysql(c, "CREATE INDEX dep_fac_id_index ON DEPARTMENTS(fac_id)")

    mysqlDb.commit()
    c.close()
    


def populate_tables(sqliteDbFile, mysqlDb):
    conn = sqlite3.connect(sqliteDbFile)
    sqlite_cur = conn.cursor()
    mysql_cur = mysqlDb.cursor()
    # JOB_DATA      
    sqlite_cur.execute("SELECT * FROM JOB_DATA")
    rows = sqlite_cur.fetchall()
    for row in rows:
        try:
            mysql_cur.execute("INSERT INTO JOB_DATA VALUES (%s, %s, %s, %s, %s, %s, %s)", row)
        except Exception:
            pass        
    mysqlDb.commit()
    print("JOB_DATA Transfer completed")
    
    # CLUSTER_DATA
    sqlite_cur.execute("SELECT * FROM CLUSTER_DATA")
    rows = sqlite_cur.fetchall()
    for row in rows:
        try:
            mysql_cur.execute("INSERT INTO CLUSTER_DATA VALUES (%s, %s, %s, %s)", row)
        except Exception:
            pass        
    mysqlDb.commit()
    print("CLUSTER_DATA Transfer completed")

    # CITIES
    sqlite_cur.execute("SELECT * FROM CITIES")
    rows = sqlite_cur.fetchall()
    for row in rows:
        try:
            mysql_cur.execute("INSERT INTO CITIES VALUES (%s, %s)", row)
        except Exception:
            pass        
    mysqlDb.commit()
    print("CITIES Transfer completed")
        
    # JOB_CITIES
    sqlite_cur.execute("SELECT * FROM JOB_CITIES")
    rows = sqlite_cur.fetchall()
    for row in rows:
        try:
            mysql_cur.execute("INSERT INTO JOB_CITIES VALUES (%s, %s, %s)", row)
        except Exception:
            pass 
    mysqlDb.commit()
    print("JOB_CITIES Transfer completed")
        
    # UNIVERSITIES
    sqlite_cur.execute("SELECT * FROM UNIVERSITIES")
    rows = sqlite_cur.fetchall()
    for row in rows:
        try:
            mysql_cur.execute("INSERT INTO UNIVERSITIES VALUES (%s, %s)", row)
        except Exception:
            pass
    mysqlDb.commit()
    print("UNIVERSITIES Transfer completed")
        
    # FACULTIES
    sqlite_cur.execute("SELECT * FROM FACULTIES")
    rows = sqlite_cur.fetchall()
    for row in rows:
        try:
            mysql_cur.execute("INSERT INTO FACULTIES VALUES (%s, %s, %s)", row)
        except Exception:
            pass
    mysqlDb.commit()
    print("FACULTIES Transfer completed")
        
    # DEPARTMENTS
    sqlite_cur.execute("SELECT * FROM DEPARTMENTS")
    rows = sqlite_cur.fetchall()
    for row in rows:
        try:
            mysql_cur.execute("INSERT INTO DEPARTMENTS VALUES (%s, %s, %s)", row)
        except Exception:
            pass
    mysqlDb.commit()
    print("DEPARTMENTS Transfer completed")

    '''
    # LANGUAGES
    sqlite_cur.execute("SELECT * FROM LANGUAGES")
    rows = sqlite_cur.fetchall()
    for row in rows:
        try:
            mysql_cur.execute("INSERT INTO LANGUAGES VALUES (%s, %s)", row)
        except:
            pass        
    mysqlDb.commit()
    print("LANGUAGES Transfer completed")
        
    # DRIVING_LICENSES
    sqlite_cur.execute("SELECT * FROM DRIVING_LICENSES")
    rows = sqlite_cur.fetchall()
    for row in rows:
        try:
            mysql_cur.execute("INSERT INTO DRIVING_LICENSES VALUES (%s, %s)", row)
        except:
            pass
    mysqlDb.commit()
    print("DRIVING_LICENSES Transfer completed")
    '''
    mysql_cur.close()
    sqlite_cur.close()
    conn.close()


if __name__ == '__main__':
    mysql_db_name = "jobrecommend"
    sqlite_file_name = "data.db"
    if len(sys.argv) == 3:
        mysql_db_name = sys.argv[1]
        sqlite_file_name = sys.argv[2]
    elif len(sys.argv) != 1:
        print("Usage: python sqlite3_to_mysql.py [mysql database name] [sqlite database name]")
        print("[mysql database name] defaults to 'jobrecommend'")
        print("[sqlite database name] defaults to 'data.db'")
        exit(0)

    print("SQLITE Source: ", sqlite_file_name)
    print("MYSQL Destination: ", mysql_db_name) 

    mysqlDb = connection.MySQLConnection(
        user='root', 
        password='',
        host='localhost',
        database=mysql_db_name)
    
    create_tables(mysqlDb)
    populate_tables(sqlite_file_name, mysqlDb)
    create_indexes(mysqlDb)
    mysqlDb.close()
    print("End")