package de.uni_koeln.spinfo.classification.zoneAnalysis.data;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import de.uni_koeln.spinfo.classification.zoneAnalysis.evaluation.EvaluationValue;

/**
 * @author geduldia
 * 
 * an object for the overall-result of an experiment
 *
 */
public class ExperimentResult implements Serializable {
	
	List<CategoryResult> categoryEvaluations;
	
	private UUID experimentID;
	
	private String experimentConfiguration;
	
	private int numberOfClasses;

	public int getNumberOfClasses() {
		return numberOfClasses;
	}



	public void setNumberOfClasses(int numberOfClasses) {
		this.numberOfClasses = numberOfClasses;
	}

	private static final long serialVersionUID = 1L;

	private double recall;
	
	private double precision;
	
	private double accuracy;
	
	private double F1Measure;
	
	
	private int TN;
	private int TP;
	private int FN;
	private int FP;

	
	public ExperimentResult(){
		this.experimentID = UUID.randomUUID();
	}



	/**
	 * @return true-negatives
	 */
	public int getTN() {
		return TN;
	}

	/**
	 * @param tN true-negatives
	 */
	public void setTN(int tN) {
		TN = tN;
	}

	/**
	 * @return true-positives
	 */
	public int getTP() {
		return TP;
	}

	/**
	 * @param tP true-positives
	 */
	public void setTP(int tP) {
		TP = tP;
	}

	/**
	 * @return false-negatives
	 */
	public int getFN() {
		return FN;
	}

	/**
	 * @param fN false-negatives
	 */
	public void setFN(int fN) {
		FN = fN;
	}

	/**
	 * @return false-positives
	 */
	public int getFP() {
		return FP;
	}

	/**
	 * @param fP false-positives
	 */
	public void setFP(int fP) {
		FP = fP;
	}

	
	/**
	 * @return experimentID
	 */
	public UUID getID(){
		return experimentID;
	}
	

	
	/**
	 * @param ev the evaluation type
	 * @return the evaluation value
	 */
	public double getEvaluationValue(EvaluationValue ev){
		switch(ev){
		case PRECISION: return getPrecision(); 
		case RECALL: return getRecall(); 
		case FSCORE: return getF1Measure(); 
		case ACCURACY: return getAccuracy(); 
		}
		return 0.0;
	}
	
	
	
	/**
	 * @return list of category-evaluation-objects
	 */
	public List<CategoryResult> getCategoryEvaluations() {
		return categoryEvaluations;
	}

	/**
	 * @param catEv List of category-evaluations
	 */
	public void setCategoryEvaluations(List<CategoryResult> catEv) {
		this.categoryEvaluations = catEv;;
	}

	/**
	 * @return overall recall of experiment
	 */
	public double getRecall() {
		return recall;
	}

	/**
	 * @param recall
	 */
	public void setRecall(double recall) {
		this.recall = recall;
	}

	/**
	 * @return overall precision of experiment 
	 */
	public double getPrecision() {
		return precision;
	}

	/**
	 * @param precision
	 */
	public void setPrecision(double precision) {
		this.precision = precision;
	}

	/**
	 * @return overall accuracz of experiment
	 */
	public double getAccuracy() {
		return accuracy;
	}

	/**
	 * @param accuracy
	 */
	public void setAccuracy(double accuracy) {
		this.accuracy = accuracy;
	}

	/**
	 * @return overall f1/measure of experiment
	 */
	public double getF1Measure() {
		return F1Measure;
	}

	/**
	 * @param f1Measure
	 */
	public void setF1Measure(double f1Measure) {
		F1Measure = f1Measure;
	}

	
	@Override
	public int hashCode() {
		String hash = this.experimentConfiguration+accuracy+F1Measure;
		return hash.hashCode();
	}
	

	@Override
	public boolean equals(Object obj) {
		ExperimentResult r = (ExperimentResult) obj;
		if(this.experimentConfiguration.equals(r.experimentConfiguration)){
			return true;
		}
		return false;
	}
	

	public String getExperimentConfiguration() {
		return experimentConfiguration;
	}

	public void setExperimentConfiguration(String experimentConfiguration) {
		this.experimentConfiguration = experimentConfiguration;
	}
}
