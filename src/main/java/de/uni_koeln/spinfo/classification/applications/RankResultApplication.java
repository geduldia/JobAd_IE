package de.uni_koeln.spinfo.classification.applications;

import java.io.File;
import java.io.IOException;

import de.uni_koeln.spinfo.classification.zoneAnalysis.evaluation.EvaluationValue;
import de.uni_koeln.spinfo.classification.zoneAnalysis.evaluation.ResultComparator;

/**
 * @author geduldia
 * 
 *         CAUTION: This Application needs data which is not stored on github.
 *         You find executable classes in src/test/java/...
 * 
 *         ranks all results in the given Folder ("toRank") and stores a ranking
 *         file for each evaluation measure in the given outpuFolder ("output")
 *
 */
public class RankResultApplication {

	/////////////////////////////
	// APP-CONFIGURATION
	/////////////////////////////

	static String toRank = "classification/output/defaultResults/07_12_15_preClassified";
	static String output = "classification/output/rankings/defaultResults/07_12_15_preClassified";

	/////////////////////////////
	// END
	/////////////////////////////

	public static void main(String[] args) throws ClassNotFoundException, IOException {

		File inputFolder = new File(toRank);
		File outputFolder = new File(output);
		if (!outputFolder.exists()) {
			outputFolder.mkdirs();
		}
		ResultComparator rc_filtered = new ResultComparator();
		rc_filtered.addResults(inputFolder);
		;
		if (!outputFolder.exists()) {
			outputFolder.mkdirs();
		}
		getRankings(rc_filtered, outputFolder.getAbsolutePath());
	}

	private static void getRankings(ResultComparator rc, String folderName) throws IOException {

		int numberOfClasses = rc.getAllResults().iterator().next().getNumberOfClasses();

		for (EvaluationValue v : EvaluationValue.values()) {
			for (int classID = 0; classID <= numberOfClasses; classID++) {
				rc.rankAll(v, classID, folderName);
			}
		}
	}

}
