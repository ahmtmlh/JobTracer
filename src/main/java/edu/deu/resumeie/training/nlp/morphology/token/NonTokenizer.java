package edu.deu.resumeie.training.nlp.morphology.token;

public class NonTokenizer extends Tokenizer{

    public NonTokenizer(int n) {
        super(n);
    }

    public NonTokenizer(){
        this(1);
    }

    @Override
    public void tokenize(String str) {
        tokens.add(str);
    }
}
