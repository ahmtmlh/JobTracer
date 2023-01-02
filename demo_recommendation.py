import json
import sys
from typing import Union

def get_nearest_skill(skill: str, centrality: dict) -> Union[str, tuple, None]:
    skill_index = -1
    centrality_keys = list(centrality.keys())
    for i, key in enumerate(centrality_keys):
        if key == skill:
            skill_index = i
            break
    
    if skill_index == -1:
        return None

    if skill_index == 0:
        return centrality_keys[1]
    elif skill_index == len(centrality_keys) -1:
        return centrality_keys[skill_index-1]
    else:
        current_score = centrality[centrality_keys[skill_index]]
        prev_score = centrality[centrality_keys[skill_index-1]]
        next_score = centrality[centrality_keys[skill_index+1]]

        prev_diff = abs(prev_score - current_score)
        next_diff = abs(next_score - current_score)

        if prev_diff > next_diff:
            first_recommendation_idx = skill_index + 1
            second_recommendation_idx = skill_index - 1
        else:
            first_recommendation_idx = skill_index - 1
            second_recommendation_idx = skill_index + 1

        return (centrality_keys[first_recommendation_idx], centrality_keys[second_recommendation_idx])

def get_skill_str(result):
    if isinstance(result, str):
        return result
    elif isinstance(result, tuple):
        return f'First: {result[0]}, Second: {result[1]}'

with open('pagerank_result.json', 'r', encoding='utf-8') as f:
    pagerank: dict = json.loads(f.read())

with open('eigenvector_centrality_result.json', 'r', encoding='utf-8') as f:
    eigenvector: dict = json.loads(f.read())

with open('closeness_centrality_result.json', 'r', encoding='utf-8') as f:
    closeness: dict = json.loads(f.read())

with open('katz_centrality_result.json', 'r', encoding='utf-8') as f:
    katz: dict = json.loads(f.read())

with open('nodes.txt', 'r', encoding='utf-8') as f:
    nodes = f.read()[1:-1].split(',')

while True:

    try: 
        token = input('Enter skill name (or phrase): ')
    except KeyboardInterrupt:
        print()
        print('Exiting...')
        sys.exit(0)

    found_token = None
    tokens = token.strip().split(' ')

    for t in tokens:
        if t in nodes:
            found_token = t
            break

    if not found_token:
        print(f'Skill \'{token}\' can\'t be found')
        sys.exit(1)

    nearest_skill_via_pagerank = get_nearest_skill(found_token, pagerank)
    nearest_skill_via_eigenvector = get_nearest_skill(found_token, eigenvector)
    nearest_skill_via_katz = get_nearest_skill(found_token, katz)
    nearest_skill_via_closeness = get_nearest_skill(found_token, closeness)

    print(f'Recommendation via PageRank: {get_skill_str(nearest_skill_via_pagerank)}')
    print(f'Recommendation via Eigen Vector Centralization: {get_skill_str(nearest_skill_via_eigenvector)}')
    print(f'Recommendation via Katz Centralization: {get_skill_str(nearest_skill_via_katz)}')
    print(f'Recommendation via Closeness Centrality: {get_skill_str(nearest_skill_via_closeness)}')
