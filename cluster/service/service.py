import socket
import json
import threading
import signal


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

#Get JSON from client, return another JSON that contains clustering information
def process_data(data):
    count, messages, vectorizer = parseJson(data)
    responses = []
    if count > 0:
        if vectorizer == 'count':
            responses = predictCnt(messages)
        else:
            responses = predictTfidf(messages)
        responses = [{"value" : int(response)} for response in responses]
    jsonResponse = {"count" : count, "clusters" : responses}
    return createResponse(jsonResponse)


def print_thread(data):
    print(data)

def handle_request(connection, address):
    with connection:
        print("Connection by", address)
        while True:
            data = connection.recv(16384)
            if not data:
                break
            data = data.decode('utf-8')
#            threading.Thread(target=print_thread, args=(data,)).start()
            response = process_data(data)
#            threading.Thread(target=print_thread, args=(response,)).start()
            connection.sendall(response.encode('utf-8'))  

def handle_connection():
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.bind((HOST, PORT))
        s.listen() 
        while True:
            connection, address = s.accept()
            handle_request(connection, address)
        s.close()

def start_connection():
    while True:
        try:
            handle_connection()
        except ConnectionResetError:
            print("Connection has been reset by remote client. Restarting...")
            pass
    print("No longer listening for connection")       

if __name__ == '__main__':
    #Init pickle modules
    init_clustering()
    signal.signal(signal.SIGINT, signal.SIG_DFL)
    #Start listening for connections
    start_connection()