package edu.deu.seniorproject.nlp.morphology.pattern;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LinguisticPatternMatcher implements PatternMatcher {


	@Override
	public boolean checkPattern(List<PatternType> posTags, Pattern pattern) {
		if (pattern.isContinuous()) {
			// Return if the pattern contains the given postags in that order and
			// continuous.
			return this.checkContinuousMatch(posTags, pattern);
		} else {
			// Returns if the pattern contains the given postags in that order.
			return this.checkInOrderMatch(posTags, pattern);
		}
	}
	
	
	private boolean checkCompleteMatch(List<PatternType> posTags, Pattern pattern) {
		return posTags.equals(pattern.getTypes());
	}
	
	
	private boolean checkInOrderMatch(List<PatternType> posTags, Pattern pattern) {
		List<PatternType> typesCopy = new ArrayList<>(pattern.getTypes());
		for (int i = 0; i < posTags.size() && !typesCopy.isEmpty(); i++) {
			if(typesCopy.get(0) == posTags.get(i)) {
				// Delete the first occurance of that type in order to NOT check it again.
				typesCopy.remove(posTags.get(i));
			}
		}
		// If the copied list is empty, than that means every type in the pattern has been found
		// and deleted from the list.
		return typesCopy.isEmpty();
	}
	
	
	private boolean checkContinuousMatch(List<PatternType> posTags, Pattern pattern) {
		boolean found = false;
		for (int i = 0; i < posTags.size(); i++) {
			// If the first element of the types list is found in postags
			if(pattern.getTypes().get(0) == posTags.get(i)) {
				// Check if the remaining elements are also found in the same order
				boolean flag = true;
				for (int j = 1; j < pattern.getTypes().size(); j++) {
					if(!(i+j < posTags.size() && pattern.getTypes().get(j) == posTags.get(i+j))) {
						flag = false;
						break;
					}
				}
				//If found once, it is found nonetheless
				found |= flag;
			}
		}
		return found;
	}

	@Override
	public Map<String, Integer> matchPatterns(Map<String, List<PatternType>> allPosTags, List<Pattern> patterns) {
		Map<String, Integer> result = new LinkedHashMap<>();
		for (Map.Entry<String, List<PatternType>> posTagAndSentence : allPosTags.entrySet()) {
			int count = 0;
			for (Pattern p : patterns) {
				if (checkPattern(posTagAndSentence.getValue(), p)) {
					// Pattern match has been found, increment the counter.
					count++;
				}
			}
			// Insert sentence and count of matched patterns into the map
			result.put(posTagAndSentence.getKey(), count);
		}
		return result;
	}

	@Override
	public List<String> matchLeastNPatterns(Map<String, List<PatternType>> allPosTags, List<Pattern> patterns, int minCount) {
		Map<String, Integer> allMatches = matchPatterns(allPosTags, patterns);
		List<String> result = new ArrayList<>();
		for (Map.Entry<String, Integer> match : allMatches.entrySet()) {
			// Value of the <key,value> pair is the match count for the sentence.
			if(match.getValue() >= minCount) {
				result.add(match.getKey());
			}
		}
		return result;
	}


	@Override
	public boolean completePatternMatch(List<PatternType> posTags, Pattern pattern) {
		return checkCompleteMatch(posTags, pattern);
	}

}
