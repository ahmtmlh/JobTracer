package edu.deu.resumeie.training.nlp.morphology.token;

import java.util.ArrayList;
import java.util.List;

public abstract class Tokenizer {

	private int n;
	protected final List<String> tokens;
	
	public Tokenizer(int n) {
		this.n = n;
		this.tokens = new ArrayList<>();
	}
	
	public List<String> getTokens(){
		return tokens;
	}	

	public int getN() {
		return n;
	}
	
	public void setN(int n) {
		this.n = n;
	}
	
	public void clearTokens() {
		tokens.clear();
	}
	
	/**
	 * Tokenize the text with the given N value.
	 * @param str String to tokenize.
	 */
	public abstract void tokenize(String str);
}
