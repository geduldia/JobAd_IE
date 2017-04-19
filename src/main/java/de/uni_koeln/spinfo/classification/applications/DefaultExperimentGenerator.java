package de.uni_koeln.spinfo.classification.applications;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_koeln.spinfo.classification.core.data.ExperimentConfiguration;
import de.uni_koeln.spinfo.classification.core.data.FeatureUnitConfiguration;
import de.uni_koeln.spinfo.classification.core.distance.Distance;
import de.uni_koeln.spinfo.classification.core.featureEngineering.featureWeighting.AbsoluteFrequencyFeatureQuantifier;
import de.uni_koeln.spinfo.classification.core.featureEngineering.featureWeighting.AbstractFeatureQuantifier;
import de.uni_koeln.spinfo.classification.core.featureEngineering.featureWeighting.LogLikeliHoodFeatureQuantifier;
import de.uni_koeln.spinfo.classification.core.featureEngineering.featureWeighting.RelativeFrequencyFeatureQuantifier;
import de.uni_koeln.spinfo.classification.core.featureEngineering.featureWeighting.TFIDFFeatureQuantifier;
import de.uni_koeln.spinfo.classification.zoneAnalysis.classifier.ZoneAbstractClassifier;
import de.uni_koeln.spinfo.classification.zoneAnalysis.classifier.ZoneKNNClassifier;
import de.uni_koeln.spinfo.classification.zoneAnalysis.classifier.ZoneNaiveBayesClassifier;
import de.uni_koeln.spinfo.classification.zoneAnalysis.classifier.ZoneRocchioClassifier;
import de.uni_koeln.spinfo.classification.zoneAnalysis.classifier.svm.SVMClassifier;
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
 *         executes a lot of experiments and stores the experiment results
 *         classification/output/defaultResults (the results can be ranked with
 *         RankResultsApplication.java)
 * 
 */
public class DefaultExperimentGenerator {

	/////////////////////////////
	// APP-CONFIGURATION
	/////////////////////////////

	static File trainingDataFile = new File("classification/data/trainingSets/TrainingDataScrambled.csv");

	static File resultsOutputFile = new File("classification/output/defaultResults/preClassified");

	static boolean preClassify = true;

	static String outputFolder = "classification/output";

	/////////////////////////////
	// END
	/////////////////////////////

	public static void main(String[] args) throws IOException, ClassNotFoundException {

		List<Integer> evaluationCategories = new ArrayList<Integer>();
		evaluationCategories.add(1);
		evaluationCategories.add(2);
		evaluationCategories.add(3);
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

		List<ZoneAbstractClassifier> classifiers = new ArrayList<ZoneAbstractClassifier>();
		classifiers.add(new ZoneNaiveBayesClassifier());
		classifiers.add(new ZoneKNNClassifier(false, 5, Distance.COSINUS));
		classifiers.add(new ZoneKNNClassifier(false, 6, Distance.COSINUS));
		classifiers.add(new ZoneKNNClassifier(false, 4, Distance.COSINUS));
		classifiers.add(new ZoneKNNClassifier(false, 3, Distance.COSINUS));
		classifiers.add(new ZoneRocchioClassifier(false, Distance.EUKLID));
		classifiers.add(new ZoneRocchioClassifier(false, Distance.COSINUS));
		// classifiers.add(new SVMClassifier());

		List<AbstractFeatureQuantifier> quantifiers = new ArrayList<AbstractFeatureQuantifier>();
		quantifiers.add(new LogLikeliHoodFeatureQuantifier());
		quantifiers.add(new TFIDFFeatureQuantifier());
		quantifiers.add(new AbsoluteFrequencyFeatureQuantifier());
		quantifiers.add(new RelativeFrequencyFeatureQuantifier());

		for (ZoneAbstractClassifier classifier : classifiers) {

			for (AbstractFeatureQuantifier fq : quantifiers) {

				List<ExperimentResult> results = new ArrayList<ExperimentResult>();
				ExperimentResult result = null;

				for (int suffixTrees = 0; suffixTrees <= 1; suffixTrees++) {

					for (int norm = 0; norm <= 1; norm++) {

						for (int stem = 0; stem <= 1; stem++) {

							for (int stopwords = 0; stopwords <= 1; stopwords++) {

								for (int n = 0; n <= 4; n++) {

									int[] nGrams = null;
									switch (n) {
									case 1: {
										nGrams = new int[] { 2 };
										break;
									}
									case 2: {
										nGrams = new int[] { 3 };
										break;
									}
									case 3: {
										nGrams = new int[] { 2, 3 };
										break;
									}
									case 4: {
										nGrams = new int[] { 3, 4 };
										break;
									}
									default:
										break;
									}
									FeatureUnitConfiguration fuc = new FeatureUnitConfiguration(toBool(norm),
											toBool(stem), toBool(stopwords), nGrams, false, 0, toBool(suffixTrees));
									ExperimentConfiguration expConfig = new ExperimentConfiguration(fuc, fq, classifier,
											trainingDataFile, outputFolder);
									System.out.println("expConfig: " + expConfig.toString());

									result = ZoneSingleExperimentExecutor.crossValidate(expConfig, jobs,
											trainingDataFile, 4, 6, translations, preClassify, evaluationCategories);
									results.add(result);
								}
							}
						}
					}
				}
				// write Results...
				jobs.persistExperimentResults(results, resultsOutputFile);

				if (classifier instanceof ZoneNaiveBayesClassifier) {
					break;
				}
			}
		}
	}

	public static boolean toBool(int i) {
		if (i != 0) {
			return true;
		}
		return false;
	}

}