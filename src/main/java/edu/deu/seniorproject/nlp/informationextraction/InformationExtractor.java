package edu.deu.seniorproject.nlp.informationextraction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import edu.deu.seniorproject.nlp.morphology.StopWordRemover;
import edu.deu.seniorproject.nlp.morphology.lemmatization.Lemmatizer;
import edu.deu.seniorproject.nlp.morphology.lemmatization.TurkishLemmatizer;
import edu.deu.seniorproject.nlp.morphology.pattern.LinguisticPatternMatcher;
import edu.deu.seniorproject.nlp.morphology.pattern.Pattern;
import edu.deu.seniorproject.nlp.morphology.pattern.PatternMatcher;
import edu.deu.seniorproject.nlp.morphology.pattern.PatternType;
import edu.deu.seniorproject.nlp.morphology.token.NGramTokenizer;
import edu.deu.seniorproject.nlp.morphology.token.Tokenizer;

import static edu.deu.seniorproject.nlp.morphology.pattern.PatternType.*;

public class InformationExtractor {
	
	private String fileName;
	private String saveFileName;
	
	// LIST THAT ARE USED FOR PATTERN MATCHING
	// Priority = CompleteMatch > SingleMatch > MultipleMatch
	private List<Pattern> completeMatchList;
	private List<Pattern> singleMatchList;
	private List<Pattern> multipleMatchList;

	// NLP TOOLS
	private Lemmatizer lemmatizer;
	private StopWordRemover stopWordRemover;
	private Tokenizer tokenizer;
	private PatternMatcher pm;

	// PATTERN MATCHING INFORMATION
	private final int minMatchCount;
	private final int maxTokenMatch;

	// RESULT LISTS / SETS
	private Set<String> result;
	private Set<String> lemmatizedResult;
	private List<ListItem> extraInfoResult;

	public InformationExtractor(int minMatchCount) {
		init();
		this.minMatchCount = minMatchCount;
		this.maxTokenMatch = 5;
		this.fileName = "";
		this.saveFileName = "";
	}

	private void init() {
		initLists();
		lemmatizer = new TurkishLemmatizer();
		stopWordRemover = new StopWordRemover("stop-words2.tr.txt");
		tokenizer = new NGramTokenizer(5);
		pm = new LinguisticPatternMatcher();
		// HashSet is used for retaining the insertion order, and eliminating duplications.
		result = new LinkedHashSet<>();
		lemmatizedResult = new LinkedHashSet<>();
		extraInfoResult = new ArrayList<>();
	}

	private void initLists() {
		// COMPLETE MATCH
		completeMatchList = new ArrayList<>();
		completeMatchList.add(new Pattern(NOUN, VERB));
		// SINGLE MATCH
		singleMatchList = new ArrayList<>();
		singleMatchList.add(new Pattern(NUM, TIME, VERB));
		singleMatchList.add(new Pattern(NUM, TIME, NOUN));
		// Özel isim durumları.
		singleMatchList.add(new Pattern(SPECIAL, VERB));
		singleMatchList.add(new Pattern(SPECIAL, NOUN));
		// 2 dil bilen?
		singleMatchList.add(new Pattern(NUM, NOUN, VERB)); 
		// MULTIPLE MATCH
		multipleMatchList = new ArrayList<>();
		//multipleMatchList.add(new Pattern(VERB, NOUN));
		multipleMatchList.add(new Pattern(NOUN, NOUN, ADJ));
		multipleMatchList.add(new Pattern(NOUN, NOUN, VERB));
		multipleMatchList.add(new Pattern(ADJ, NOUN, VERB));
		multipleMatchList.add(new Pattern(NOUN, VERB, ADJ));
		multipleMatchList.add(new Pattern(ADJ, VERB, NOUN));
		//multipleMatchList.add(new Pattern(ADJ, NOUN, VERB));
		//multipleMatchList.add(new Pattern(NOUN, NOUN, NOUN));
		multipleMatchList.add(new Pattern(ADJ, VERB, NOUN, NOUN));
		multipleMatchList.add(new Pattern(NOUN, NOUN, NOUN, VERB));
		multipleMatchList.add(new Pattern(VERB, VERB, NOUN, NOUN));
		multipleMatchList.add(new Pattern(NOUN, ADJ, NOUN, ADJ));
		multipleMatchList.add(new Pattern(ADJ, VERB, NOUN, VERB));
		multipleMatchList.add(new Pattern(NOUN, VERB, NOUN, NOUN));
		multipleMatchList.add(new Pattern(ADJ, ADJ, NOUN, VERB));
		multipleMatchList.add(new Pattern(NOUN, ADJ, NOUN, VERB));
		multipleMatchList.add(new Pattern(VERB, NOUN, NOUN, VERB));
		multipleMatchList.add(new Pattern(NOUN, NOUN, NOUN, VERB, VERB));
		multipleMatchList.add(new Pattern(NOUN, NOUN, NOUN, NOUN, VERB));
		multipleMatchList.add(new Pattern(ADJ, VERB, NOUN, NOUN, VERB));
		/*
		multipleMatchList.add(new Pattern(NOUN, VERB, NOUN, NOUN, VERB));
		multipleMatchList.add(new Pattern(ADJ, NOUN, NOUN, NOUN, VERB));
		multipleMatchList.add(new Pattern(ADJ, NOUN, VERB, NOUN, VERB));
		multipleMatchList.add(new Pattern(ADJ, NOUN, ADJ, VERB, VERB));
		multipleMatchList.add(new Pattern(NOUN, ADJ, ADJ, NOUN, VERB));
		multipleMatchList.add(new Pattern(ADJ, NOUN, NOUN, NOUN, NOUN, VERB));
		*/
	}

	/**
	 * Used tto check if given POS tag of a sentence passes the pattern
	 * matching test. Success depends on total number of tokens as well.
	 * @param posTags POS Tag list of a sentence
	 * @param n Number of tokens
	 * @return True if passes the test, false if not.
	 */
	private boolean matchPattern(List<PatternType> posTags, int n) {
		boolean completeMatch = false;
		boolean singleMatch = false;
		boolean multipleMatch = false;
		// Check for complete pattern matches
		for (Pattern pattern : completeMatchList) {
			completeMatch |= pm.completePatternMatch(posTags, pattern);
		}
		if (!completeMatch) {
			// Check for single pattern matches
			for (Pattern pattern : singleMatchList) {
				singleMatch |= pm.checkPattern(posTags, pattern);
			}
			if (!singleMatch) {
				// Check for multiple pattern matches
				int count = 0;
				for (Pattern pattern : multipleMatchList) {
					if (pm.checkPattern(posTags, pattern)) {
						count++;
					}
				}
				// Token sayısı ve en az kabul edilen match sayısından en küçüğü ile karşılaştır
				multipleMatch = (count >= Math.min(n, minMatchCount));
			}
		}
		return completeMatch || singleMatch || multipleMatch;
	}

	/**
	 * Check if tokens passes the pattern matching test
	 * Success depends on total number of tokens
	 * @param tokens List of tokens
	 * @return True if tokens pass, false if not.
	 */
	private boolean checkTokensMatching(List<String> tokens) {
		int count = 0;
		for (String token : tokens) {
			token = removePunctuation(token);
			lemmatizer.flush();
			lemmatizer.lemmatizeSentence(token, true);
			if (matchPattern(lemmatizer.getPosTags(), tokens.size())) {
				count++;
			}
		}
		/*
		 * If token size <= 6 : half of the token size (example: token size=5, check for 2)
		 * If token size > 6: Check for 6
		 *
		 * Check if count is bigger than min(maxTokenMatch, check_from_above)
		 * If true, tokens pass the patternMatching rulebook
		 */
		return count > Math.min(maxTokenMatch, tokens.size() <= 6 ? tokens.size() / 2 : 6);
	}

	/**
	 * Check if comma separated parts of a sentence passes the test.
	 * @param commaSeparated List of comma separated parts of a sentence.
	 * @return True if each comma separated part of a sentence passes the test,
	 * 		   false if even one of them fails.
	 */
	private boolean checkSeparatedStrings(String[] commaSeparated) {
		if (commaSeparated.length < 2) {
			return false;
		}
		boolean flag = true;
		for (String commaSeparatedSentence : commaSeparated) {
			tokenizer.clearTokens();
			tokenizer.tokenize(commaSeparatedSentence);
			flag &= checkTokensMatching(tokenizer.getTokens());
		}
		return flag;
	}

	/**
	 * Extract information from a sentence
	 * @param item A sentence in ListItem form. Contains extra information like ID and experience
	 */
	public void extractFromString(ListItem item) {
		String sentence = item.text;
		// If sentence is blank, do nothing.
		if(sentence.trim().isEmpty()) {
			return;
		}
		// Remove stopwords.
		if (stopWordRemover.isErrorFree()) {
			sentence = stopWordRemover.removeStopWords(sentence);
		}
		String[] commaSeparated = sentence.split(",");
		if (checkSeparatedStrings(commaSeparated)) {
			// All comma separated sentences are matched by pattern rules. Include them
			// as different sentences.
			for (String commaSeparatedSentence : commaSeparated) {
				addToLists(commaSeparatedSentence, item);
			}
		} else {
			// At least ONE of the comma separated sentence doesn't match pattern matching rules.
			// Check for the original sentence, and add it to list if it matches rules.
			tokenizer.clearTokens();
			tokenizer.tokenize(sentence);
			if (checkTokensMatching(tokenizer.getTokens())) {
				addToLists(sentence, item);
			}
		}
	}
	
	/*
	 * Adding successful strings to result lists.
	 */
	private void addToLists(String str, ListItem item) {
		str = removePunctuation(str);
		lemmatizer.flush();
		lemmatizer.lemmatizeSentence(str, true);
		if(!lemmatizedResult.contains(lemmatizer.getLemmatizedSentence().trim()) && !result.contains(str)) {
			lemmatizedResult.add(lemmatizer.getLemmatizedSentence().trim());
			result.add(str);
			extraInfoResult.add(item);
		}
	}
	
	/**
	 * Remove the punctuation part of the string, to add to the list
	 * 
	 * This is used to change some of the punctuation characters, rather than changing them all
	 * with a Regex command.
	 * 
	 * @param str String to remove punctuation from
	 * @return Punctuation removed new string.
	 */
	private String removePunctuation(String str) {
		// "\\p{P}" -> This removes punctuation. 
		return str.replace("•", "").replace("...", "")
				.replace("(", "").replace(")", "")
				.replace("{", "").replace("}", "")
				.replace("[", "").replace("]", "")
				.replace("!", "").replace("?", "")
				.replace("\"", "").replace("'", "")
				.replace(":", "").replace(";", "")
				.replace("%", "").replace("/", "")
				.replace("\\", "").replace("·", "")
				.replace("_", "").replace("‘", "")
				.replace("’", "").replace("“", "")
				.replace("”", "").replace("*", "")
				.trim();
	}
	
	/**
	 * Extract information from list of sentences.
	 * This function ultimately calls the "extractFromString" function.
	 * @param items List of sentences, with extra information.
	 */
	public void extractFromList(List<ListItem> items) {
		for (ListItem item : items) {
			extractFromString(item);
			//System.out.printf("Line: %d/%d%n", i, n);
		}
	}
	
	/**
	 * Extract information from a file. This function extracts lines to a list
	 * and calls the "extractFromList" function.
	 * @param filename Name of the file.
	 */
	public void extractFromFile(String filename) {
		this.fileName = filename;
		List<ListItem> lines = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(new File(filename)))) {
			String line;
			while ((line = br.readLine()) != null) {
				lines.add(ListItem.parse(line));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		extractFromList(lines);
	}
	
	/**
	 * Save the extracted information strings (both original and 
	 * lemmatized sentences) to a file. 
	 * @param filename Name of the file to save to.
	 */
	public void saveToFile(String filename) {
		this.saveFileName = filename;
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(new File(filename+".txt")))){
			for (String res : result) {
				bw.write(res);
				bw.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Lemmatized result will also contain exp and id information
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(new File(filename+".lm.txt")))){
			int i = 0;
			for (String res : lemmatizedResult) {
				ListItem item = extraInfoResult.get(i++);
				res = item.id + ";" + item.exp + ";" + item.maxExp + ";" + item.jobInfo + ";" + res;
				bw.write(res);
				bw.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void saveToFile() {
		saveToFile("ie-result");
	}

	public List<String> getLemmatizedResults(){
		return new ArrayList<>(this.lemmatizedResult);
	}

	/**
	 * Delete temporary files that have been used by this tool.
	 * Files can't recover after deletion.
	 */
	public void deleteTempFiles() throws IOException{
		java.nio.file.Files.delete(Paths.get(this.saveFileName + ".txt"));
		java.nio.file.Files.delete(Paths.get(this.saveFileName + ".lm.txt"));
		java.nio.file.Files.delete(Paths.get(this.fileName));
	}


	private static class ListItem{

		private static final String DELIMITER = "-_-";

		String id;
		int exp;
		int maxExp;
		String text;
		String jobInfo;

		ListItem(String id, int exp, int maxExp, String jobInfo, String text){
			this.id = id;
			this.exp = exp;
			this.maxExp = maxExp;
			this.text = text;
			this.jobInfo = jobInfo;
		}

		public static ListItem parse(String str){
			String[] parts = str.split(DELIMITER);
			return new ListItem(parts[0].trim(), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]),
								parts[3].trim(), parts[4].trim());
		}

		@Override
		public String toString() {
			return "{ Text: " + text + ", ID: " + id + ", EXP|MAX_EXP: " + exp +"|"+maxExp  +  "}";
		}
	}
	
}
