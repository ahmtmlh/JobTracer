package edu.deu.resumeie.training.nlp.morphology.lemmatization;

import edu.deu.resumeie.training.nlp.morphology.pattern.PatternMatcher;
import edu.deu.resumeie.training.nlp.morphology.pattern.PatternType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zemberek.core.turkish.PrimaryPos;
import zemberek.morphology.TurkishMorphology;
import zemberek.morphology.analysis.SingleAnalysis;
import zemberek.morphology.analysis.WordAnalysis;
import zemberek.morphology.lexicon.RootLexicon;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class TurkishLemmatizer implements Lemmatizer {

	private static final Logger logger = LogManager.getLogger(TurkishLemmatizer.class);

	private final Map<String, PatternType> edgeCaseWords;
	private final TurkishMorphology morphology;
	private final List<PatternType> posTags;
	private final Set<String> specialSet;
	private final StringBuilder sb;

	private final String edgeCaseWordsFilename;

	public TurkishLemmatizer() {
		this("");
	}

	public TurkishLemmatizer(String edgeCaseWordsFilename){
		this.morphology = TurkishMorphology.builder().setLexicon(RootLexicon.getDefault()).useInformalAnalysis()
				.build();
		this.posTags = new ArrayList<>();
		this.sb = new StringBuilder();
		this.edgeCaseWords = new HashMap<>();
		this.edgeCaseWordsFilename = edgeCaseWordsFilename;
		this.specialSet = new HashSet<>();
		initLists();
	}

	private void initLists(){
		if (this.edgeCaseWordsFilename != null && !this.edgeCaseWordsFilename.isEmpty()){
			initEdgeCaseWordsFromFile(this.edgeCaseWordsFilename);
		} else {
			edgeCaseWords.put("ekip", PatternType.NOUN);
			edgeCaseWords.put("dil", PatternType.NOUN);
			edgeCaseWords.put("karar", PatternType.NOUN);
			edgeCaseWords.put("olan", PatternType.VERB);
			edgeCaseWords.put("olmayan", PatternType.VERB);
			edgeCaseWords.put("sürücü", PatternType.NOUN);
			edgeCaseWords.put("seviye", PatternType.ADJ);
			edgeCaseWords.put("makine", PatternType.NOUN);
			edgeCaseWords.put("yazılım", PatternType.NOUN);
		}
	}

	private void initEdgeCaseWordsFromFile(String filename){
		try(BufferedReader br = new BufferedReader(new FileReader(filename))){
			String line;
			while((line = br.readLine()) != null){
				int commaIndex = line.indexOf(',');
				if (commaIndex > 0){
					String word = line.substring(0, commaIndex);
					String posTag = line.substring(commaIndex+1);
					edgeCaseWords.put(word, PatternMatcher.patternTypeFromString(posTag));
				}
			}
		} catch (IOException e){
			logger.error(e.getLocalizedMessage(), e);
		}
	}

	@Override
	public void lemmatizeSentence(String sentence, boolean removePunctuation) {
		List<WordAnalysis> analysis = morphology.analyzeSentence(sentence);
		for (int i = 0; i < analysis.size(); i++) {
			WordAnalysis current = analysis.get(i);

			// If there is analysis result and result is known by the api.
			// If not, just include the vanilla input.
			/*
			 * !current.getAnalysisResult.isEmpty() && !current.getAnalysisResult.get(0).isUnknown()
			 */
			if (current.isCorrect()) {
				// If an option for remove punctuation is given AND pos tag of the word is punctuation
				// don't include it.
				if (removePunctuation && current.getAnalysisResults().get(0).getPos() == PrimaryPos.Punctuation) {
					continue;
				}
				if (edgeCaseWords.containsKey(current.getNormalizedInput().toLowerCase())) {
					sb.append(current.getNormalizedInput().toLowerCase()).append(" ");
					posTags.add(edgeCaseWords.get(current.getNormalizedInput().toLowerCase()));
					continue;
				}
				sb.append(current.getAnalysisResults().get(0).getLemmas().get(0)).append(" ");
				WordAnalysis prev = i > 0 ? analysis.get(i - 1) : WordAnalysis.EMPTY_INPUT_RESULT;
				// WordAnalysis next = i < analysis.size()-1 ? analysis.get(i+1) : WordAnalysis.EMPTY_INPUT_RESULT;
				PatternType possibleFit = getBestPossibleFit(prev, current);
				// If returned type is not unknown, then add it to list.
				if (possibleFit != PatternType.UNKNOWN)
					posTags.add(possibleFit);
			} else {
				// If analyser can't find a result for the current word, check if it has been classified as SPECIAL.
				// SPECIAL identifiers WILL pass the pattern matching.
				if(isSpecial(current.getInput())) {
					posTags.add(PatternType.SPECIAL);
				}
				sb.append(current.getInput()).append(" ");
			}
		}
	}

	/**
	 * 	If string is:
	 * 		All uppercase AND
	 * 		More than 3 characters long AND
	 * 		Doesn't start with a digit
	 * 	Then it can be classified as special string. This approach only classifies the first appearance
	 * 	of the word.
	 * 	This function uses caching on words.
	 * @param str String to check if its special
	 * @return	True if string is classified as special.
	 */
	private boolean isSpecial(String str) {
		if (specialSet.contains(str.toUpperCase())){
			return true;
		}
		String upperCase = str.toUpperCase();
		boolean allUpperCase = upperCase.equals(str);
		if (allUpperCase && upperCase.length() >= 3 && !Character.isDigit(upperCase.charAt(0))){
			specialSet.add(upperCase);
			return true;
		}
		return false;
	}
	

	/*
	 * Get the best pos tag. Care for the edge cases.
	 */
	private PatternType getBestPossibleFit(WordAnalysis prev, WordAnalysis current) {
		// Check for validity
		if (current.isCorrect()) {
			// If previous word is not correct, return the current word pos tag.
			if (!prev.isCorrect()) {
				return analyseWord(current);
			}
			// If previous word is correct, analyse current with previous word in mind.
			else {
				return analyseByPrevWord(prev, current);
			}
		} else {
			return PatternType.UNKNOWN;
		}
	}

	/**
	 * Some words are analysed to be Unknowns but they might not be. In a Unknown
	 * case, look for possible pos tags in that word and determine the pos tag with
	 * the following order:
	 * 
	 * ADJECTIVE -> First choice
	 * Verb -> Second choice
	 * Noun -> Third choice
	 * Adverb -> Last choice
	 * If the given word is still unknown, keep it unknown.
	 * 
	 * If word is not Unknown in the first place, return the found pos tag.
	 */

	private PatternType analyseWord(WordAnalysis current) {
		PatternType type = PatternMatcher
				.patternTypeFromString(current.getAnalysisResults().get(0).getDictionaryItem().primaryPos.shortForm);
		if (type == PatternType.UNKNOWN) {
			if (checkIfContainsType(current, PatternType.ADJ)) {
				return PatternType.ADJ;
			} else if (checkIfContainsType(current, PatternType.VERB)) {
				return PatternType.VERB;
			} else if (checkIfContainsType(current, PatternType.NOUN)) {
				return PatternType.NOUN;
			} else if (checkIfContainsType(current, PatternType.ADV)) {
				return PatternType.ADV;
			} else {
				return PatternType.UNKNOWN;
			}
		} else {
			PatternType secondaryPos = PatternMatcher
					.patternTypeFromString(current.getAnalysisResults().get(0).getDictionaryItem().secondaryPos.shortForm);
			if (secondaryPos == PatternType.PROP) {
				return PatternType.NOUN;
			}
			return type;
		}

	}

	/*
	 * Analyse the current and previous word and return the best possible pos-tag
	 * for the current word.
	 */
	private PatternType analyseByPrevWord(WordAnalysis prev, WordAnalysis current) {
		// If current word might express time and previous word is something numerical,
		// analysis for the current word is probably TIME.
		if (checkIfContainsType(current, PatternType.TIME) && checkIfContainsType(prev, PatternType.NUM)) {
			return PatternType.TIME;
		}

		return analyseWord(current);
	}

	private boolean checkIfContainsType(WordAnalysis word, PatternType type) {
		for (SingleAnalysis singleAnalysis : word.getAnalysisResults()) {
			if (singleAnalysis.getDictionaryItem() != null && (PatternMatcher
					.patternTypeFromString(singleAnalysis.getDictionaryItem().primaryPos.shortForm) == type
					|| PatternMatcher.patternTypeFromString(singleAnalysis.getDictionaryItem().secondaryPos.shortForm) == type)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<PatternType> getPosTags() {
		return posTags;
	}

	@Override
	public String getLemmatizedSentence() {
		return sb.toString().trim();
	}

	@Override
	public void flush() {
		this.posTags.clear();
		this.sb.setLength(0);
	}

	@Override
	public String toString() {
		return String.format("Lemmatized Sentence: %s%nPostags : %s%n", getLemmatizedSentence(), getPosTags());
	}
}
