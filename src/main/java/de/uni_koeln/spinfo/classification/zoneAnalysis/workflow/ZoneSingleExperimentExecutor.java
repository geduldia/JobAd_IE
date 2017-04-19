package de.uni_koeln.spinfo.classification.zoneAnalysis.workflow;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;
import de.uni_koeln.spinfo.classification.core.data.ExperimentConfiguration;
import de.uni_koeln.spinfo.classification.db_io.DbConnector;
import de.uni_koeln.spinfo.classification.zoneAnalysis.classifier.RegexClassifier;
import de.uni_koeln.spinfo.classification.zoneAnalysis.data.ExperimentResult;

public class ZoneSingleExperimentExecutor {

	public static ExperimentResult crossValidate(ExperimentConfiguration expConfig, ZoneJobs jobs,
			File trainingDataFile, int numCategories, int numClasses, Map<Integer, List<Integer>> translations,
			boolean preClassify, List<Integer> evaluationCategories) throws IOException {
		long before = System.nanoTime();
		// prepare classifyUnits...
		List<ClassifyUnit> paragraphs = null;
		paragraphs = jobs.getCategorizedParagraphsFromFile(expConfig.getDataFile(),
				expConfig.getFeatureConfiguration().isTreatEncoding());

		long after = System.nanoTime();
		// system.out.println("prepare CUs: " + (after - before)/1000000000d);
		before = System.nanoTime();
		paragraphs = jobs.initializeClassifyUnits(paragraphs);

		after = System.nanoTime();
		// system.out.println("initialize CUs: " + (after -
		// before)/1000000000d);
		before = System.nanoTime();
		paragraphs = jobs.setFeatures(paragraphs, expConfig.getFeatureConfiguration(), true);

		after = System.nanoTime();
		// system.out.println("setFeatures: " + (after - before)/1000000000d);
		before = System.nanoTime();
		paragraphs = jobs.setFeatureVectors(paragraphs, expConfig.getFeatureQuantifier(), null);
		after = System.nanoTime();
		// system.out.println("set Vectors: " + (after - before)/1000000000d);
		// preclassify

		Map<ClassifyUnit, boolean[]> preClassified = new HashMap<ClassifyUnit, boolean[]>();
		if (preClassify) {
			before = System.nanoTime();
			RegexClassifier rc = new RegexClassifier("classification/data/regex.txt");
			for (ClassifyUnit cu : paragraphs) {
				boolean[] classIDs = rc.classify(cu, null);
				preClassified.put(cu, classIDs);
			}
			after = System.nanoTime();
			// system.out.println("preClassify: " + (after -
			// before)/1000000000d);
		}

		// classify
		before = System.nanoTime();
		Map<ClassifyUnit, boolean[]> classified = jobs.crossvalidate(paragraphs, expConfig);
		after = System.nanoTime();
		// system.out.println("crossvalidate: " + (after - before)/1000000000d);

		// merge results
		if (preClassify) {
			before = System.nanoTime();
			classified = jobs.mergeResults(classified, preClassified);
			after = System.nanoTime();
			// system.out.println("merge: " + (after - before)/1000000000d);
		}

		// translate
		if (translations != null) {
			before = System.nanoTime();
			classified = jobs.translateClasses(classified);
			after = System.nanoTime();
			// system.out.println("translate: " + (after - before)/1000000000d);
		}

		// evaluate
		before = System.nanoTime();
		ExperimentResult result = jobs.evaluate(classified, evaluationCategories, expConfig);
		after = System.nanoTime();
		// system.out.println("evaluate: " + (after - before)/1000000000d);
		return result;
	}

}
