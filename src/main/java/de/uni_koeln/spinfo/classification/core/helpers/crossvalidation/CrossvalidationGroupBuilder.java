package de.uni_koeln.spinfo.classification.core.helpers.crossvalidation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/** Builds a specified number of training- and test-Sets 
 * for specified elements for crossvalidation.   
 *
 * @author jhermes
 *
 * @param <T>
 */
public class CrossvalidationGroupBuilder<T> implements Iterable<TrainingTestSets<T>>{
	
	private List<T> overallElements;
	private List<List<T>> groups;
	private int k;
	
	
	/** Builds k crossvalidation groups for specified elements.
	 * @param overallElements Elements that should be divided into training and test groups.
	 * @param k Number of crossvaldiation groups.
	 */
	public CrossvalidationGroupBuilder(List<T> overallElements, int k) {
		super();
		this.overallElements = overallElements;
		this.k = k;
		groups = new ArrayList<List<T>>();
		Collections.shuffle(overallElements, new Random(0));
		buildCrossValidationGroups();
	}
	

	public Iterator<TrainingTestSets<T>> iterator() {
		return new CrossvalidationGroupIterator<T>(groups);
	}	


	private void buildCrossValidationGroups(){
		int groupsize = overallElements.size() / k;
		int position = 0;
		for(int i=0; i<k; i++){
			List<T> nextGroup = new ArrayList<T>(groupsize);
			for(int j=0; j<groupsize; j++){
				nextGroup.add(overallElements.get(position));
				position += 1;
			}
			groups.add(nextGroup);
		}
	}

	
}

