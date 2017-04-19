package de.uni_koeln.spinfo.classification.applications;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_koeln.spinfo.classification.core.data.ExperimentConfiguration;
import de.uni_koeln.spinfo.classification.core.data.FeatureUnitConfiguration;
import de.uni_koeln.spinfo.classification.core.distance.Distance;
import de.uni_koeln.spinfo.classification.core.featureEngineering.featureWeighting.AbstractFeatureQuantifier;
import de.uni_koeln.spinfo.classification.core.featureEngineering.featureWeighting.TFIDFFeatureQuantifier;
import de.uni_koeln.spinfo.classification.zoneAnalysis.classifier.ZoneAbstractClassifier;
import de.uni_koeln.spinfo.classification.zoneAnalysis.classifier.ZoneKNNClassifier;
import de.uni_koeln.spinfo.classification.zoneAnalysis.data.ExperimentResult;
import de.uni_koeln.spinfo.classification.zoneAnalysis.helpers.SingleToMultiClassConverter;
import de.uni_koeln.spinfo.classification.zoneAnalysis.workflow.ZoneJobs;
import de.uni_koeln.spinfo.classification.zoneAnalysis.workflow.ZoneSingleExperimentExecutor;

/**
 * @author geduldia
 * 
 *         CAUTION: This Application needs data which is not stored on github.
 *         You find executable classes in src/test/java/...
 * 
 *         Application to execute a single Experiment for the specified
 *         experiment-parameters. Stores the result in specified "outputFolder"
 * 
 */

public class SingleExperimentExecution {

	/////////////////////////////
	// APP-CONFIGURATION
	/////////////////////////////

	static File inputFile = new File("classification/data/trainingSets/trainingDataScrambled.csv");
	static String outputFolder = "classification/output";
	////////////////////////////////////////
	/////// experiment parameters
	///////////////////////////////////////

	static boolean preClassify = true;
	static File resultOutputFolder = new File("classification/output/singleResults/preClassified");
	static int knnValue = 4;
	static boolean ignoreStopwords = false;
	static boolean normalizeInput = false;
	static boolean useStemmer = false;
	static boolean suffixTrees = false;
	static int[] nGrams = null;
	static int miScoredFeaturesPerClass = 0;
	static Distance distance = Distance.COSINUS;
	static ZoneAbstractClassifier classifier = new ZoneKNNClassifier(false, knnValue, distance);
	static AbstractFeatureQuantifier quantifier = new TFIDFFeatureQuantifier();
	static List<Integer> evaluationCategories = Arrays.asList(new Integer[] { 1, 2, 3 });
	//////////////////////////////////////
	///////// END///
	//////////////////////////////////////

	/////////////////////////////
	// END
	/////////////////////////////

	public static void main(String[] args) throws ClassNotFoundException, IOException {

		// Translations
		Map<Integer, List<Integer>> translations = new HashMap<Integer, List<Integer>>();
		List<Integer> categories = new ArrayList<Integer>();
		categories.add(1);
		categories.add(2);
		translations.put(5, categories);
		categories = new ArrayList<Integer>();
		categories.add(2);
		categories.add(3);
		translations.put(6, categories);
		SingleToMultiClassConverter stmc = new SingleToMultiClassConverter(6, 4, translations);
		ZoneJobs jobs = new ZoneJobs(stmc);

		FeatureUnitConfiguration fuc = new FeatureUnitConfiguration(normalizeInput, useStemmer, ignoreStopwords, nGrams,
				false, miScoredFeaturesPerClass, suffixTrees);
		ExperimentConfiguration expConfig = new ExperimentConfiguration(fuc, quantifier, classifier, inputFile,
				outputFolder);
		ExperimentResult result = ZoneSingleExperimentExecutor.crossValidate(expConfig, jobs, inputFile, 4, 6,
				translations, preClassify, evaluationCategories);

		System.out.println("F Measure: \t" + result.getF1Measure());
		System.out.println("Precision: \t" + result.getPrecision());
		System.out.println("Recall: \t" + result.getRecall());
		System.out.println("Accuracy: \t" + result.getAccuracy());

		// store result
		jobs.persistExperimentResult(result, resultOutputFolder);
	}

}
