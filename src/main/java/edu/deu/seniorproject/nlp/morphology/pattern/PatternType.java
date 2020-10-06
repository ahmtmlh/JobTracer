package edu.deu.seniorproject.nlp.morphology.pattern;

public enum PatternType{
	
	// İsim
	NOUN,
	// Özel isim
	PROP, 
	// Sıfat
	ADJ,
	// Zarf
	ADV,
	// Bağlaç
	CONJ,
	// Fiil
	VERB,
	// Zamir
	PRON,
	// Edat
	PART,
	// Numerik
	NUM,
	// Noktalama
	PUNC,
	// İkileme (yineleme) -> fokur fokur
	DUP,
	// Zaman
	TIME,
	// Özel -> Meslek isimleri, kullanılan araçlar (Örn: C#)
	SPECIAL,
	UNKNOWN
	
}
