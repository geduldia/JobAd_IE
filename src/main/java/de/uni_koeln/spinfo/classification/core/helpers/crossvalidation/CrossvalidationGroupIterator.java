package de.uni_koeln.spinfo.classification.core.helpers.crossvalidation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Iterates over TrainingsTestSets applicable for cross validation
 * @author jhermes
 *
 * @param <T>
 */
public class CrossvalidationGroupIterator<T> implements Iterator<TrainingTestSets<T>>{

	private List<List<T>> elements;
	private int current;

	protected CrossvalidationGroupIterator(List<List<T>> elements){
		this.elements = elements;
		current = 0;
	}
	
	
	public boolean hasNext() {
		return current < elements.size();
	}

	
	public TrainingTestSets<T> next() {
		List<T> trainingSet = new ArrayList<T>();
		List<T> testSet = new ArrayList<T>();
		//If there is only one group, training and test set 
		// contain the same elements.
		if(elements.size()==1){
			testSet = elements.get(0);
			trainingSet = elements.get(0);			
		}
		else{
			for (int i=0; i<elements.size(); i++){
				if(current==i){
					testSet = elements.get(i);
				}
				else{
					trainingSet.addAll(elements.get(i));
				}
			}
		}
		current+=1;
		return new TrainingTestSets<T>(trainingSet, testSet);
	}

	
	public void remove() {
		current++;
	}
	
}