package edu.deu.resumeie.training.nlp.morphology;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

public class StopWordRemover {

	private final Set<String> stopWords;
	private boolean error = false;
	private Exception errorException;

	public StopWordRemover(String fileName) {
		stopWords = new HashSet<>();
		errorException = null;
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			String line;
			while((line = br.readLine()) != null) {
				stopWords.add(line.toLowerCase());
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
		String lowerCaseSentence = sentence.toLowerCase().trim();
		String[] lowerCaseWords = lowerCaseSentence.split(" ");
		String[] words = sentence.split(" ");
		List<String> newWords = new ArrayList<>();
		for (int i = 0; i < words.length; i++) {
			String word = words[i];
			String lowerCaseWord = lowerCaseWords[i];
			if(!isStopWord(lowerCaseWord)) {
				newWords.add(word);
			}
		}
		StringBuilder sb = new StringBuilder();
		for (String word : newWords) {
			sb.append(word).append(" ");
		}
		return sb.toString().trim();
	}

	private boolean isStopWord(String word) {
		word = word.replaceAll("\\p{P}", "");
		return stopWords.contains(word);
	}
}
