package edu.deu.seniorproject.nlp.morphology.lemmatization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.deu.seniorproject.nlp.morphology.pattern.PatternMatcher;
import edu.deu.seniorproject.nlp.morphology.pattern.PatternType;
import zemberek.core.turkish.PrimaryPos;
import zemberek.morphology.TurkishMorphology;
import zemberek.morphology.analysis.SingleAnalysis;
import zemberek.morphology.analysis.WordAnalysis;
import zemberek.morphology.lexicon.RootLexicon;

public class TurkishLemmatizer implements Lemmatizer {

	private final Map<String, PatternType> edgeCaseWords;
	private final TurkishMorphology morphology;
	private final List<PatternType> posTags;
	private final StringBuilder sb;

	public TurkishLemmatizer() {
		this.morphology = TurkishMorphology.builder().setLexicon(RootLexicon.getDefault()).useInformalAnalysis()
				.build();
		this.posTags = new ArrayList<>();
		this.sb = new StringBuilder();
		edgeCaseWords = new HashMap<>();
		edgeCaseWords.put("ekip", PatternType.NOUN);
		edgeCaseWords.put("dil", PatternType.NOUN);
		edgeCaseWords.put("karar", PatternType.NOUN);
		edgeCaseWords.put("olan", PatternType.VERB);
		edgeCaseWords.put("olmayan", PatternType.VERB);
	}

	@Override
	public void lemmatizeSentence(String sentence, boolean removePunctuation) {
		sentence = sentence.replace("·", "");
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
				if(isAllUpperCase(current.getInput())) {
					posTags.add(PatternType.SPECIAL);
				}
				sb.append(current.getInput()).append(" ");
			}
		}
	}
	
	
	private boolean isAllUpperCase(String str) {
		String upperCase = str.toUpperCase();
		return upperCase.equals(str);
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
				.fromString(current.getAnalysisResults().get(0).getDictionaryItem().primaryPos.shortForm);
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
					.fromString(current.getAnalysisResults().get(0).getDictionaryItem().secondaryPos.shortForm);
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
					.fromString(singleAnalysis.getDictionaryItem().primaryPos.shortForm) == type
					|| PatternMatcher.fromString(singleAnalysis.getDictionaryItem().secondaryPos.shortForm) == type)) {
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
