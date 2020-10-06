package edu.deu.seniorproject.nlp.morphology.pattern;

import java.util.List;
import java.util.Map;

public interface PatternMatcher {

	/**
	 * Checks if the given sentence matches with the given pattern
	 * @param posTags Postags of an sentence to check
	 * @param pattern Pattern to check
	 * @return True if sentence matches, false if not.
	 */
	public boolean checkPattern(List<PatternType> posTags, Pattern pattern);
	
	/**
	 * Matches pattern for the given postags and sentences.
	 * @param allPosTags Map of sentences and their posTags.
	 * @param patterns List pof patterns to check
	 * @param continuous Option to check the patterns in continuous order or not.
	 * @return Map of sentences paired with how many patterns mathced.
	 */
	public Map<String, Integer> matchPatterns(Map<String, List<PatternType>> allPosTags,
			List<Pattern> patterns);
	
	/**
	 * Matches patterns and returns a list of sentencens that mathces at least N patterns.
	 * @param allPosTags Map of sentences and their posTags.
	 * @param patterns List pof patterns to check
	 * @param continuous Option to check the patterns in continuous order or not.
	 * @param minCount Minimum pattern match count for a sentence to be included in the list.
	 * @return List of sentences, with at least minCount matches
	 */
	public List<String> matchLeastNPatterns(Map<String, List<PatternType>> allPosTags,
			List<Pattern> patterns, int minCount);
	
	/**
	 * Checks if the given sentence is matching exactly as the given pattern
	 * @param posTags Postags of the sentence
	 * @param pattern Pattern to check
	 * @return True if pattern matches the sentence, false if not.
	 */
	public boolean completePatternMatch(List<PatternType> posTags, Pattern pattern);
	
	
	public static PatternType fromString(String str) {
		try {
			str = str.toUpperCase().replace('İ', 'I').replace('Ç', 'C')
					.replace('Ş', 'S').replace('Ğ', 'G').replace('Ü', 'U');
			return PatternType.valueOf(str);
		} catch (IllegalArgumentException e) {
			return PatternType.UNKNOWN;
		}
	}
	
}
