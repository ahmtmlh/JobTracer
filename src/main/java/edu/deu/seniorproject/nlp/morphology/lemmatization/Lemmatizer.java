package edu.deu.seniorproject.nlp.morphology.lemmatization;

import java.util.List;

import edu.deu.seniorproject.nlp.morphology.pattern.PatternType;

public interface Lemmatizer {

	/**
	 * Lematizes given sentence, with the option of removing punctuation or not.
	 * While lemmatizing, pos tags of each token will be stored. This is done, in order
	 * to save the real meaning of words while extracting pos tags.
	 * 
	 * @param sentence Sentence to break to its lemmas
	 */

	void lemmatizeSentence(String sentence, boolean removePunc);
	
	/**
	 * Returns a list of pos tags from the lemmatization.
	 * @return List of Pos-Tags.
	 */

	List<PatternType> getPosTags();
	
	/**
	 * Returns the lemmatized sentence
	 * @return Lemmatized string
	 */

	String getLemmatizedSentence();
	
	/**
	 * Clear all used fields for re-use.
	 */
	void flush();

}