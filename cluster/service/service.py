import socket
import json

from cluster_predict import predictCnt, predictTfidf, init_clustering

HOST = "localhost"
PORT = 65432

def parseJson(jsonString):
    root = json.loads(jsonString)
    messages = root['jobInfo']
    messages = [m['value'] for m in messages]
    count = len(messages)
    v = root['vectorizer']
    return count, messages, v

def createResponse(data_dict):
    return json.dumps(data_dict)


def acceptConnection(s):
    connection, address = s.accept()
    with connection:
        print("Connection by", address)
        data = connection.recv(4096)
        if not data:
            return
        return connection, data
            

#Get JSON from client, return another JSON that contains clustering information
def process_data(data):
    count, messages, vectorizer = parseJson(data)
    print("Using vectorizer:", vectorizer)
    if vectorizer == 'count':
        responses = predictCnt(messages)
    else:
        responses = predictTfidf(messages)
    responses = [{"value" : int(response)} for response in responses]
    jsonResponse = {"count" : count, "clusters" : responses}
    return createResponse(jsonResponse)


# This doesn't work as intented. Somehow processing data in a seperate thread makes thing collapse
def process_connection(connection, data):
    response = process_data(data)
    connection.sendall(response.encode('utf-8'))

# For debug. Reflects incoming data
def reflect_thread(connection, data):
    connection.sendall(data.encode('utf-8'))


def start_connection():
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.bind((HOST, PORT))
        s.listen() 
        while True:
            connection, address = s.accept()
            with connection:
                print("Connection by", address)
                data = connection.recv(4096)
                if not data:
                    break
                data = data.decode('utf-8')
                response = process_data(data)
                connection.sendall(response.encode('utf-8'))
        s.close()
    print("No longer listening for connection")       

if __name__ == '__main__':
    #Init pickle modules
    init_clustering()
    #Start listening for connections
    start_connection()