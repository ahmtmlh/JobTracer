package edu.deu.resumeie.training.nlp.morphology.token;

public class NGramTokenizer extends Tokenizer{

	
	public NGramTokenizer(int n) {
		super(n);
	}
	
	public NGramTokenizer() {
		this(5);
	}
	
	@Override
	public void tokenize(String str) {
		str = str.trim();
		String[] words = str.split(" ");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < words.length; i++) {
			int count = 0;
			while(i + getN() <= words.length && count < getN()) {
				sb.append(words[i+count]);
				sb.append(" ");
				count++;
			}
			if(sb.length() > 0) {
				tokens.add(sb.toString().trim());
				// Clear the buffer
				sb.setLength(0);
			}
		}
		// If there there are no tokens with the length of n, 
		// Include the whole string.
		if(tokens.isEmpty()) {
			tokens.add(str);
		}	
	}
	
}
