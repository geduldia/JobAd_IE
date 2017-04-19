package de.uni_koeln.spinfo.classification.core.helpers.crossvalidation;

import java.util.List;

import de.uni_koeln.spinfo.classification.jasc.data.JASCClassifyUnit;

/**
 * Class to bundle trainings and test sets applicable 
 * to machine learning techniques
 * @author jhermes
 *
 * @param <T>
 */
public class TrainingTestSets<T>{
	private List<T> training;
	private List<T> test;
	
	public TrainingTestSets(List<T> training, List<T> test) {
		super();
		this.training = training;
		this.test = test;
	}
	
	public List<T> getTrainingSet() {
		return training;
	}
	
	public List<T> getTestSet() {
		return test;
	}
	
	public int[] getMembersInTestSet(){
		JASCClassifyUnit c = (JASCClassifyUnit) test.get(0);
		int numberOfClasses = c.getClassIDs().length;
		int[] toReturn = new int[numberOfClasses];
		for(int i = 0; i < test.size(); i++){
			JASCClassifyUnit cu = (JASCClassifyUnit) test.get(i);
			boolean[] classIDs = cu.getClassIDs();
			for(int j = 0; j < classIDs.length; j++){
				if(classIDs[j]){
					toReturn[j]++;
				}
			}
		}
		return toReturn;
	}
	
}
