package edu.deu.seniorproject.nlp.morphology.pattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Pattern {

	private List<PatternType> types;
	private boolean continuous;
	
	public Pattern() {
		this.types = new ArrayList<>();
		this.continuous = false;
	}
	
	public Pattern(PatternType... types) {
		this();
		this.types.addAll(Arrays.asList(types));
	}
	
	public Pattern(boolean continuous, PatternType... types) {
		this(types);
		this.continuous = continuous;
	}

	public void addPatternType(PatternType type) {
		types.add(type);
	}
	
	public List<PatternType> getTypes() {
		return types;
	}
	
	protected boolean isContinuous() {
		return this.continuous;
	}
	
	/*
	 * THIS IS DONE BY THE PATTERN MATCHER
	 *
	protected boolean checkContinious(List<PatternType> posTags) {
		boolean found = false;
		for (int i = 0; i < posTags.size(); i++) {
			// If the first element of the types list is found in postags
			if(types.get(0) == posTags.get(i)) {
				// Check if the remaining elements are also found in the same order
				boolean flag = true;
				for (int j = 1; j < types.size(); j++) {
					if(!(i+j < posTags.size() && types.get(j) == posTags.get(i+j))) {
						flag = false;
						break;
					}
				}
				found = flag;
			}
		}
		return found;
	}
	
	protected boolean checkInOrder(List<PatternType> posTags) {
		List<PatternType> typesCopy = new ArrayList<>(types);
		for (int i = 0; i < posTags.size(); i++) {
			if(typesCopy.contains(posTags.get(i))) {
				// Delete the first occurance of that type in order to NOT
				// check it again.
				typesCopy.remove(posTags.get(i));
			}
		}
		// If the copied list is empty, than that means every type in the pattern has been found
		// and deleted from the list.
		return typesCopy.isEmpty();
	}
	
	*/
	
}
