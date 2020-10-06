package edu.deu.seniorproject.nlp.informationextraction;

public class ProcessException extends Exception{
	
	private static final long serialVersionUID = -6681276367974821442L;

	public ProcessException(Throwable t) {
		super(t);
	}
	
	public ProcessException(String str) {
		super(str);
	}

}
