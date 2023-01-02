import networkx as nx
import json
import matplotlib.pyplot as plt
import operator

def calculate_and_save_centrality_measure(graph, centrality_func, filename=None):
    
    if not filename:
        filename = f'{centrality_func.__name__}_result.json'

    centrality_result: dict = centrality_func(graph)
    sorted_result = dict(sorted(centrality_result.items(), key=operator.itemgetter(1), reverse=True))

    with open(filename, 'w', encoding='utf-8') as f:
        json.dump(sorted_result, f, indent=4)

def draw_graph(graph):
    pos = nx.random_layout(graph)
    nx.draw_networkx(graph, pos)
    labels = nx.get_edge_attributes(graph, 'weight')
    nx.draw_networkx_edge_labels(graph, pos, edge_labels=labels)
    plt.show()


with open('edges.json', 'r', encoding='utf-8') as f:
    json_content = f.read()

edges: dict = json.loads(json_content)

with open('nodes.txt', 'r', encoding='utf-8') as f:
    nodes_content = f.read()

nodes = nodes_content[1:-1].split(',')

graph = nx.Graph()
graph.add_nodes_from(nodes)

for k,v in edges.items():
    node_names = k.split(',')
    graph.add_edge(node_names[0], node_names[1], weight=int(v))

"""
calculate_and_save_centrality_measure(graph, nx.eigenvector_centrality)
calculate_and_save_centrality_measure(graph, nx.katz_centrality),
calculate_and_save_centrality_measure(graph, nx.pagerank)
calculate_and_save_centrality_measure(graph, nx.closeness_centrality)
"""

draw_graph(graph)