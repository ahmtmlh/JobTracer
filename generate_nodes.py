import pandas as pd
from itertools import combinations
import json

def parse_skills(item):
    new_list = []
    counts = []
    items = item.split("|")
    items = [it for it in items if it]
    new_list.extend(items)
    counts.append(len(items))    
    return new_list, counts

def search_token(s: str, token: str):
	words = [ss.lower() for ss in s.split(' ')]
	token = [ss.lower() for ss in token.split(' ')]
	return set(token).issubset(set(words))

def get_hard_skills(skill_list, hard_skills):
	found_hard_skills = []
	for s in skill_list:
		for hard_skill in hard_skills:
			if search_token(s, hard_skill):
				found_hard_skills.append(hard_skill)

	return found_hard_skills

def normalize_skill(s: str):
	s = s.strip()
	if len(s) > 1:
		return s.lower()
	
	return s


df = pd.read_csv("ie-result.lm.txt", encoding="utf-8", header=None, sep=";")

with open('./data/hard-skills.txt', 'r', encoding='utf-8') as f:
	hard_skills = f.readlines()

hard_skills = [normalize_skill(s) for s in hard_skills if s]

skills = df[df.columns[-1]].values.tolist()
edges = {}
nodes = set()

for skill in skills:
	s, c = parse_skills(skill)
	hard_skill_list = get_hard_skills(s, hard_skills)
	nodes.update(hard_skill_list)
	possible_edges = list(combinations(hard_skill_list, 2))

	for _edge in possible_edges:
		edge = f'{_edge[0]},{_edge[1]}'
		reverse_edge = f'{_edge[1]},{_edge[0]}'
		
		if edge == reverse_edge:
			continue

		if reverse_edge in edges:
			edges[reverse_edge] += 1
		elif edge in edges:
			edges[edge] += 1
		else:
			edges[edge] = 1

node_list = f"[{','.join(nodes)}]"

with open('nodes.txt', 'w', encoding='utf-8') as f:
	f.write(node_list)

edges = {k: v for k, v in sorted(edges.items(), key=lambda item: -int(item[1]))}

with open('edges.json', 'w', encoding='utf-8') as f:
	json.dump(edges, f, indent=4)