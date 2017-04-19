package de.uni_koeln.spinfo.classification.zoneAnalysis.data;

import java.io.Serializable;

import de.uni_koeln.spinfo.classification.zoneAnalysis.evaluation.EvaluationValue;

/**
 * 
 * an object for the category-specific result of an experiment
 *
 */
/**
 * @author geduldia
 *
 */
public class CategoryResult implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param categoryID
	 */
	public CategoryResult(int categoryID){
		this.categoryID = categoryID;
	}
	

	private int categoryID;
	
	/*true-positive*/
	private int tp = 0;
	/*true-negative*/
	private int tn = 0;
	/*false-negative*/
	private int fn = 0;
	/*false-positive*/
	private int fp = 0;
	

	public void raiseTP(){
		this.tp++;
	}

	public void raiseTN() {
		this.tn++;
	}
	public void raiseFP(){
		this.fp++;
	}

	public void raiseFN() {
		this.fn++;
	}
	
	
	/**
	 * @param ev - evaluationvalue type
	 * @return evaluation value
	 */
	public double getEvaluationValue(EvaluationValue ev){
		switch(ev){
		case PRECISION: return getPrecision(); 
		case RECALL: return getRecall(); 
		case FSCORE: return getF1Score(); 
		case ACCURACY: return getAccuracy(); 
		}
		return 0.0;
	}
	

	/**
	 * @return recall of category
	 */
	public double getRecall(){
		double toReturn = ((double) tp)/(tp+fn);
		return toReturn;
	}
	
	
	/**
	 * @return precision of category
	 */
	public double getPrecision(){
		double toReturn = ((double)tp)/(tp+fp);
		return toReturn;
	}
	
	/**
	 * @return accuracy of category
	 */
	public double getAccuracy(){
		double toReturn = ((double)tp+tn)/(tp+tn+fp+fn);
		return toReturn;
	}
	
	/**
	 * @return f1-score of category
	 */
	public double getF1Score(){
		double toReturn = ((double)2*tp) / (2*tp+fp+fn);
		return toReturn;
	}
	
	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("result of class "+ categoryID+": \n   recall: "+ getRecall()+"\n   precision: " + getPrecision()+ "\n   accuracy: "+ getAccuracy()+"\n   F1Score "+ getF1Score()+ "\n\n");
		return sb.toString();
	}
}
