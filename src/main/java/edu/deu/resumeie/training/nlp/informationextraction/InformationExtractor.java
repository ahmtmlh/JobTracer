package edu.deu.resumeie.training.nlp.informationextraction;

import edu.deu.resumeie.training.nlp.morphology.StopWordRemover;
import edu.deu.resumeie.training.nlp.morphology.lemmatization.Lemmatizer;
import edu.deu.resumeie.training.nlp.morphology.lemmatization.TurkishLemmatizer;
import edu.deu.resumeie.training.nlp.morphology.pattern.LinguisticPatternMatcher;
import edu.deu.resumeie.training.nlp.morphology.pattern.Pattern;
import edu.deu.resumeie.training.nlp.morphology.pattern.PatternMatcher;
import edu.deu.resumeie.training.nlp.morphology.pattern.PatternType;
import edu.deu.resumeie.training.nlp.morphology.token.NGramTokenizer;
import edu.deu.resumeie.training.nlp.morphology.token.Tokenizer;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

import static edu.deu.resumeie.training.nlp.morphology.pattern.PatternType.*;

public class InformationExtractor {

	public static final String VERSION = "V1.3.2";

	private static final int COMPLETE_LIST = 0x01;
	private static final int SINGLE_LIST = 0x02;
	private static final int MULTI_LIST = 0x04;

	private static final String LIST_FILE = ".data/match_list.conf";

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

	private Map<ListItem, List<String>> lemmatizedResult;
	private Map<ListItem, List<String>> result;

	public InformationExtractor(int minMatchCount) {
		init();
		this.minMatchCount = minMatchCount;
		this.maxTokenMatch = 5;
		this.saveFileName = "";
	}

	private void init() {
		// If match_list.conf is found in the system, those values will be used.
		// If not, default values will be used
		initLists();
		lemmatizer = new TurkishLemmatizer();
		stopWordRemover = new StopWordRemover("stop-words2.tr.txt");
		tokenizer = new NGramTokenizer(6);
		pm = new LinguisticPatternMatcher();
		// HashSet is used for retaining the insertion order, and eliminating duplications.
		result = new HashMap<>();
		lemmatizedResult = new HashMap<>();
	}

	private PatternType getTypeFromString(String str){
		try{
			return PatternType.valueOf(str.toUpperCase());
		} catch(IllegalArgumentException e){
			return UNKNOWN;
		}
	}

	private void initLists(){

		completeMatchList = new ArrayList<>();
		singleMatchList = new ArrayList<>();
		multipleMatchList = new ArrayList<>();

		int current = 0;
		try(BufferedReader br = new BufferedReader(new FileReader(LIST_FILE))){
			String line;
			String identifier = null;
			boolean itemValid;
			while((line = br.readLine()) != null){
				itemValid = true;
				if("[complete]".equalsIgnoreCase(line)){
					current = COMPLETE_LIST;
					identifier = line;
				} else if ("[single]".equalsIgnoreCase(line)){
					current = SINGLE_LIST;
					identifier = line;
				} else if ("[multi]".equalsIgnoreCase(line)){
					current = MULTI_LIST;
					identifier = line;
				} else {
					if (current == 0){
						System.err.printf("Identifier \"%s\" is not known.%n", identifier);
						continue;
					}
					String[] temp = line.split(",");
					if(temp.length == 0) continue;

					PatternType[] types = new PatternType[temp.length];
					for (int i = 0; i < temp.length; i++) {
						PatternType tempType = getTypeFromString(temp[i]);
						if(tempType == UNKNOWN){
							itemValid = false;
							break;
						}
						types[i] = tempType;
					}
					if (!itemValid){
						System.err.printf("Item: \"%s\" is not valid%n", line);
						continue;
					}
					switch(current){
						case COMPLETE_LIST:
							completeMatchList.add(new Pattern(types));
							break;
						case SINGLE_LIST:
							singleMatchList.add(new Pattern(types));
							break;
						case MULTI_LIST:
							multipleMatchList.add(new Pattern(types));
							break;
					}
					current = 0;
				}
			}
		} catch (IOException e){
			System.out.println("Config file not found. Using default values...");
			initListsDefaultValues();
		}
	}

	private void initListsDefaultValues() {
		// COMPLETE MATCH
		completeMatchList.add(new Pattern(NOUN, VERB));
		// SINGLE MATCH
		singleMatchList.add(new Pattern(NUM, TIME, VERB));
		singleMatchList.add(new Pattern(NUM, TIME, NOUN));
		// Özel isim durumları.
		singleMatchList.add(new Pattern(SPECIAL));
		// 2 dil bilen?
		singleMatchList.add(new Pattern(NUM, NOUN, VERB));
		singleMatchList.add(new Pattern(ADJ, NOUN, NOUN));
		// MULTIPLE MATCH
		multipleMatchList.add(new Pattern(NOUN, NOUN, ADJ));
		multipleMatchList.add(new Pattern(NOUN, NOUN, VERB));
		multipleMatchList.add(new Pattern(ADJ, NOUN, VERB));
		multipleMatchList.add(new Pattern(NOUN, VERB, ADJ));
		multipleMatchList.add(new Pattern(ADJ, VERB, NOUN));
		multipleMatchList.add(new Pattern(ADV, VERB, NOUN));
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
		 * Check if count is bigger (or equal [NOT SURE ABOUT THIS]) than min(maxTokenMatch, check_from_above)
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
	 * Extract information from a list item.
	 * @param item A sentence in ListItem form. Contains extra information like ID and experience
	 */
	public void extractFromListItem(ListItem item) {
		for(String sentence : item.getTexts()){
			sentence = sentence.trim();

			// If sentence is blank, do nothing.
			if(sentence.isEmpty()) {
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
					addToResult(commaSeparatedSentence, item);
				}
			} else {
				// At least ONE of the comma separated sentence doesn't match pattern matching rules.
				// Check for the original sentence, and add it to list if it matches rules.
				tokenizer.clearTokens();
				tokenizer.tokenize(sentence);
				if (checkTokensMatching(tokenizer.getTokens())) {
					addToResult(sentence, item);
				}
			}
		}
	}
	/*
	 * Adding successful strings to result lists.
	 */
	private void addToResult(String str, ListItem item) {
		boolean newItem = false;
		str = removePunctuation(str);
		lemmatizer.flush();
		lemmatizer.lemmatizeSentence(str, true);

		List<String> lemmatizedList;
		List<String> regularList;
		if(lemmatizedResult.containsKey(item)){
			lemmatizedList = lemmatizedResult.get(item);
			regularList = result.get(item);
		} else {
			newItem = true;
			lemmatizedList = new ArrayList<>();
			regularList = new ArrayList<>();
		}

		lemmatizedList.add(lemmatizer.getLemmatizedSentence().trim());
		regularList.add(str);

		if (newItem){
			lemmatizedResult.put(item, lemmatizedList);
			result.put(item, regularList);
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
		// Threading?
//		Thread[] threadArr = new Thread[4];
//		for (int i = 0; i < 4; i++) {
//			int finalI = i;
//			threadArr[i] = new Thread(() -> {
//				int start = (items.size() / 4) * finalI;
//				int end = start + (items.size()/4);
//				while (start < end) {
//					extractFromListItem(items.get(start));
//					start++;
//				}
//			});
//		}
//		for(Thread thread : threadArr){
//			thread.start();
//		}
//
//		for(Thread thread : threadArr){
//			try {
//				thread.join();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}

		for (ListItem item : items) {
			extractFromListItem(item);
			//System.out.printf("Line: %d/%d%n", i, n);
		}
	}

	/**
	 * This function will internally call the extractFromList function. In order that to work,
	 * this function will call the given strings in a invalid ListItem form. Invalid ListItems
	 * will be detected by the extractFromString function, and additional information like id, exp, etc.
	 * will not be saved.
	 * @param list List of strings.
	 */
	public void extractFromStringList(List<String> list){
		if (list == null) return;
		for (String str : list){
			extractFromListItem(new ListItem(str));
		}
	}

	private String getFormattedString(ListItem item, List<String> list){
		String ret = item.id + ";" + item.exp + ";" + item.maxExp + ";" + item.jobInfo + ";" +
				item.cities + ";" + item.getEducationStatus() + ";";

		StringBuilder sb = new StringBuilder();
		for (String str : list){
			sb.append(str).append('|');
		}

		if (sb.length() > 0){
			//Get rid of the extra '|'
			sb.setLength(sb.length()-1);
			ret = ret + sb.toString();
		}

		return ret;
	}


	/**
	 * Save the extracted information strings (both original and 
	 * lemmatized sentences) to a file. 
	 * @param filename Name of the file to save to.
	 */
	public void saveToFile(String filename) {
		this.saveFileName = filename;
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(filename+".txt"))){
			for (Map.Entry<ListItem, List<String>> entry : result.entrySet()) {
				String res = getFormattedString(entry.getKey(), entry.getValue());
				bw.write(res);
				bw.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(filename+".lm.txt"))){

			for (Map.Entry<ListItem, List<String>> entry : lemmatizedResult.entrySet()){
				String res = getFormattedString(entry.getKey(), entry.getValue());
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
		List<String> ret = new ArrayList<>();
		lemmatizedResult.values().forEach(ret::addAll);
		return ret;
	}

	/**
	 * Delete temporary files that have been used by this tool.
	 * Files can't recover after deletion.
	 */
	public void deleteTempFiles() throws IOException{
		java.nio.file.Files.deleteIfExists(Paths.get(this.saveFileName + ".txt"));
		java.nio.file.Files.deleteIfExists(Paths.get(this.saveFileName + ".lm.txt"));
	}

	public void clear(){
		this.lemmatizedResult.clear();
		this.result.clear();
		this.lemmatizer.flush();
		this.tokenizer.clearTokens();
	}


	public static class ListItem{

		String id;
		int exp;
		int maxExp;
		final List<String> texts;
		String jobInfo;
		String cities;
		int educationStatus;

		private final boolean valid;

		public ListItem(String id, int exp, int maxExp, String jobInfo, String cities, int educationStatus){
			this.id = id;
			this.exp = exp;
			this.maxExp = maxExp;
			this.texts = new ArrayList<>();
			this.jobInfo = jobInfo;
			this.cities = cities;
			this.educationStatus = educationStatus;
			valid = checkIfValid();
		}

		public ListItem(String text){
			this.texts = new ArrayList<>();
			this.texts.add(text);
			valid = false;
		}

		private boolean checkIfValid(){
			return id != null && !id.isEmpty();
		}

		public boolean isValid(){
			return valid;
		}

		public String getId() {
			return id;
		}

		public int getExp() {
			return exp;
		}

		public int getMaxExp() {
			return maxExp;
		}

		public List<String> getTexts() {
			return texts;
		}

		public String getJobInfo() {
			return jobInfo;
		}

		public String getCities() {
			return cities;
		}

		public int getEducationStatus() {
			return educationStatus;
		}

		public void addText(String text){
			this.texts.add(text);
		}

		@Override
		public String toString() {
			return "{ Text: " + Arrays.toString(texts.toArray()) + ", ID: " + id + ", EXP|MAX_EXP: " + exp +"|"+maxExp  +  "}";
		}

		@Override
		public int hashCode() {
			if (this.id != null){
				return this.id.hashCode();
			} else {
				return this.texts.hashCode();
			}
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj){
				return true;
			}
			if (obj instanceof ListItem){
				if (this.id != null){
					return this.id.equals( ((ListItem)obj).id );
				} else {
					return this.texts.equals( ((ListItem)obj).texts );
				}
			}

			return false;
		}
	}
	
}
