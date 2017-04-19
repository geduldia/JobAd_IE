package information_extraction.evaluation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
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
import de.uni_koeln.spinfo.information_extraction.pattern_generation.PatternGeneration;
import de.uni_koeln.spinfo.information_extraction.preprocessing.MateTagger;

/**
 * @author geduldia
 * 
 * test-class to execute and evaluate the Bootstrapping Workflow
 * Stores a detailed evaluation result in src/test/resources/information_extraction/output/evaluation_files
 * 
 *
 */
public class EvaluateBootstrapExtraction {
	
	/////////////////////////////
	// APP-CONFIGURATION
	/////////////////////////////
	/**
	 * file with the manually written patterns
	 */
	private static File patternsFile = new File("src/test/resources/information_extraction/input/competencePatterns.txt");

	/**
	 * file with the list of modifiers (e.g. 'wünschenswert' or 'zwingend erforderlich')
	 */
	private static File modifiers = new File("src/test/resources/information_extraction/input/modifiers.txt");
	
	/**
	 * path to the manually annotated Training-DB
	 */
	private static String trainingData = "src/test/resources/information_extraction/trainingdata/TrainingData_Competences.db";

	/**
	 * file to store auto-generated patterns
	 */
	private static File autoContextsFile = new File("src/test/resources/information_extraction/output/autoPatterns.txt");
	
	/**
	 * file to store a detailed evaluation-result (list of all extracted competences, all true-positives, false-positives and false-negatives)
	 */
	private static File resultFile = new File("src/test/resources/information_extraction/output/evaluation_files/BootstrapExtraction.txt");
	
	/////////////////////////////
	// END
	/////////////////////////////

	/**
	 * executes and evaluates the Bootstrapping Workflow
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
		IEJobs jobs = new IEJobs(null, null, modifiers, patternsFile, IEType.COMPETENCE);
		Map<ExtractionUnit, Map<InformationEntity, List<ExtractionPattern>>> pattern_extractions = null;
		Map<ExtractionUnit, Map<InformationEntity, List<ExtractionPattern>>> stringmatchContexts = null;
		List<ExtractionPattern> allAutoContexts = new ArrayList<ExtractionPattern>();
	
		int knownEntities = 0;
		int round = 0;
		while (true) {
		
			round++;
			jobs.annotateTokens(ieUnits);
			pattern_extractions = jobs.extractByPatterns(ieUnits);
			pattern_extractions = jobs.mergeInformationEntities(pattern_extractions);
			jobs.updateEntitiesList(pattern_extractions);
			jobs.annotateTokens(ieUnits);
			stringmatchContexts = jobs.extractByStringMatch(ieUnits, pattern_extractions);
			stringmatchContexts = jobs.mergeInformationEntities(stringmatchContexts);
			//generate new patterns from contexts
			List<ExtractionPattern> autoContexts = PatternGeneration.generatePatterns(stringmatchContexts, 2, 3, new boolean[]{true,true,true,true});	
			allAutoContexts.addAll(autoContexts);
			jobs.addPatterns(autoContexts);
			if (jobs.knownEntities <= knownEntities) {
				break;
			}
			knownEntities = jobs.knownEntities;
		}
		writeAutoContextsFile(allAutoContexts, autoContextsFile);
		// merge Extractions
		Map<ExtractionUnit, Map<InformationEntity, List<ExtractionPattern>>> allExtractions = new HashMap<ExtractionUnit, Map<InformationEntity, List<ExtractionPattern>>>();
		allExtractions.putAll(pattern_extractions);
		for (ExtractionUnit extractionUnit : stringmatchContexts.keySet()) {
			Map<InformationEntity, List<ExtractionPattern>> map = allExtractions.get(extractionUnit);
			if (map == null)
				map = new HashMap<InformationEntity, List<ExtractionPattern>>();
			map.putAll(stringmatchContexts.get(extractionUnit));
			allExtractions.put(extractionUnit, map);
		}
		
		System.out.println("\nfinished Bootstrap-Extractions");
		
		// evaluate
		IEEvaluator evaluator = new IEEvaluator();
		EvaluationResult result = evaluator.evaluateIEResults(allExtractions, trainingdata);
		
		System.out.println(result.toString());
		System.out.println("Iterations: " + round+"\n");
		System.out.println("for detailed results read evaluation-file ("+ resultFile.getPath()+")");
		
		// write output file
		Set<InformationEntity> extractedCompetences = new HashSet<InformationEntity>();
		int totalNumberOfExtractions = 0;
		for (ExtractionUnit eUnit : allExtractions.keySet()) {
			for (InformationEntity ie : allExtractions.get(eUnit).keySet()) {
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

	private void writeAutoContextsFile(List<ExtractionPattern> contexts, File file) throws IOException {
		PrintWriter out = new PrintWriter(new FileWriter(file));
		for (ExtractionPattern context : contexts) {
			out.write(context.toString() + "\n\n");

		}
		out.close();
	}

}
