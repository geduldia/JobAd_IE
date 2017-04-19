package information_extraction.evaluation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import de.uni_koeln.spinfo.information_extraction.data.ExtractionPattern;
import de.uni_koeln.spinfo.information_extraction.data.ExtractionUnit;
import de.uni_koeln.spinfo.information_extraction.data.IEType;
import de.uni_koeln.spinfo.information_extraction.data.InformationEntity;
import de.uni_koeln.spinfo.information_extraction.db_io.IE_DBConnector;
import de.uni_koeln.spinfo.information_extraction.evaluation.EvaluationResult;
import de.uni_koeln.spinfo.information_extraction.evaluation.IEEvaluator;
import de.uni_koeln.spinfo.information_extraction.extraction.IEJobs;
import de.uni_koeln.spinfo.information_extraction.preprocessing.MateTagger;

/**
 * @author geduldia
 * 
 *         test-class to execute and evaluate the simple rulebased workflow.
 *         Stores a detailed evaluation result in
 *         src/test/resources/information_extraction/output/evaluation_files
 *
 */
public class EvaluateSimpleRuleBasedExtraction {

	/////////////////////////////
	// APP-CONFIGURATION
	/////////////////////////////

	/**
	 * file with the manually written patterns
	 */
	private static File contextFile = new File("src/test/resources/information_extraction/input/competencePatterns.txt");
	/**
	 * file with the list of modifiers (e.g. 'wünschenswert' or 'zwingend
	 * erforderlich')
	 */
	private static File modifier = new File("src/test/resources/information_extraction/input/modifiers.txt");
	/**
	 * path to the manually annotated Training-DB
	 */
	private static String trainingData = "src/test/resources/information_extraction/trainingdata/TrainingData_Competences.db";
	/**
	 * file to store a detailed evaluation-result (list of all extracted
	 * competences, all true-positives, false-positives and false-negatives)
	 */
	private static File resultFile = new File(
			"src/test/resources/information_extraction/output/evaluation_files/SimpleRuleBasedExtraction.txt");

	/////////////////////////////
	// END
	/////////////////////////////

	/**
	 * executes and evaluates the simple rulebased extraction
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws IOException
	 */
	@Test
	public void evaluate() throws ClassNotFoundException, SQLException, IOException {

		// read Trainingdata from DB
		Connection connection = IE_DBConnector.connect(trainingData);
		Map<ExtractionUnit, List<String>> trainingdata = IE_DBConnector.readTrainingData(connection);

		// prepare ieUnits for IE
		List<ExtractionUnit> ieUnits = new ArrayList<ExtractionUnit>(trainingdata.keySet());
		MateTagger.setLexicalData(ieUnits);

		// extract competences
		IEJobs jobs = new IEJobs(null, null, modifier, contextFile, IEType.COMPETENCE);
		jobs.annotateTokens(ieUnits);
		Map<ExtractionUnit, Map<InformationEntity, List<ExtractionPattern>>> extractions = jobs
				.extractByPatterns(ieUnits);
		extractions = jobs.mergeInformationEntities(extractions);

		// evaluate
		IEEvaluator evaluator = new IEEvaluator();
		EvaluationResult result = evaluator.evaluateIEResults(extractions, trainingdata);

		System.out.println(result.toString());
		System.out.println("for detailed results read evaluation-file: " + resultFile.getPath());

		// write output file
		Set<InformationEntity> extractedCompetences = new HashSet<InformationEntity>();
		int totalNumberOfExtractions = 0;
		for (ExtractionUnit eUnit : extractions.keySet()) {
			for (InformationEntity ie : extractions.get(eUnit).keySet()) {
				extractedCompetences.add(ie);
				totalNumberOfExtractions++;
			}
		}
		if (!resultFile.exists())
			resultFile.createNewFile();
		PrintWriter out = new PrintWriter(new FileWriter(resultFile));
		out.write(result.toString() + "\n\n");
		out.write(
				"extracted Competences (" + extractedCompetences.size() + " / " + totalNumberOfExtractions + "):\n\n");
		for (InformationEntity ie : extractedCompetences) {
			out.println(ie + "\n");
		}
		out.write("\n\n\n");
		out.write(result.printFPs());
		out.write("__________________________________________________________________________________________\n");
		out.write(result.printFNs());
		out.write("__________________________________________________________________________________________\n");
		out.write(result.printTPs());
		out.flush();
		out.close();
	}

}
