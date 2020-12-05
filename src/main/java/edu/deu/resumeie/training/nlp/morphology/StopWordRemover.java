package edu.deu.resumeie.training.nlp.morphology;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StopWordRemover {

	private final List<String> stopWords;
	private boolean error = false;
	private Exception errorException;
	private final Locale locale;
	
	public StopWordRemover(String fileName) {
		locale = new Locale("tr");
		stopWords = new ArrayList<>();
		errorException = null;
		try (BufferedReader br = new BufferedReader(
				new FileReader(new File(fileName)))) {
			String line = "";
			while((line = br.readLine()) != null) {
				stopWords.add(line);
			}
		}catch (IOException e) {
			errorException = e;
			error = true;
		}
	}
	
	public boolean isErrorFree() {
		return !error;
	}
	
	/*
	 * FOR DEBUG PURPOSES  
	 */
	public Exception getError() {
		return errorException;
	}
	
	public String removeStopWords(String sentence) {
		//Pre-process
		String lowerCaseSentence = sentence.toLowerCase(locale).trim();
		String[] lowerCaseWords = lowerCaseSentence.split(" ");
		String[] words = sentence.split(" ");
		List<String> newWords = new ArrayList<>();
		for (int i = 0; i < words.length; i++) {
			String word = words[i];
			String lowerCaseWord = lowerCaseWords[i];
			if(!isStopWord(word)) {
				newWords.add(isAllUpperCase(word) ? word : lowerCaseWord);
			}
		}
		StringBuilder sb = new StringBuilder();
		for (String word : newWords) {
			sb.append(word);
			sb.append(" ");
		}
		return sb.toString().trim();
	}
	
	private boolean isAllUpperCase(String str) {
		String upperCase = str.toUpperCase(locale);
		return upperCase.equals(str);
	}
	
	
	private boolean isStopWord(String word) {
		for (String stopWord : stopWords) {
			//Remove punctuation before checking
			word = word.replaceAll("\\p{P}", "");
			if(word.equalsIgnoreCase(stopWord)) {
				return true;
			}
		}
		return false;
	}

}
