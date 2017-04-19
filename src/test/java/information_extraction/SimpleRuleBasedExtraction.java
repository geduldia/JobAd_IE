package information_extraction;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;
import de.uni_koeln.spinfo.classification.jasc.data.JASCClassifyUnit;
import de.uni_koeln.spinfo.classification.zoneAnalysis.helpers.SingleToMultiClassConverter;
import de.uni_koeln.spinfo.information_extraction.data.ExtractionPattern;
import de.uni_koeln.spinfo.information_extraction.data.ExtractionUnit;
import de.uni_koeln.spinfo.information_extraction.data.IEType;
import de.uni_koeln.spinfo.information_extraction.data.InformationEntity;
import de.uni_koeln.spinfo.information_extraction.db_io.IE_DBConnector;
import de.uni_koeln.spinfo.information_extraction.extraction.IEJobs;
import de.uni_koeln.spinfo.information_extraction.preprocessing.ExtractionUnitBuilder;

/**
 * @author geduldia
 * 
 *         Class to execute a rulebased extraction on the output of the
 *         classification-app
 *         (src/test/java/classification/ClassifyJobAdsIntoParagraphs.java)
 *         
 *         Selects all class 3 paragraphs (=classified as applicants profile),
 *         splits them into sentences and extract competences by using the
 *         extraction-patterns in file 'competencePatterns.txt'
 *
 */
public class SimpleRuleBasedExtraction {
	
	/////////////////////////////
	// APP-CONFIGURATION
	/////////////////////////////

	/**
	 * path to the input database of classifyUnits
	 */
	private static String inputData = "src/test/resources/classification/output/CorrectableClassifiedParagraphs.db";

	/**
	 * path to the output database for extracted Competences
	 */
	private static String outputDB = "src/test/resources/information_extraction/output/SimpleRuleBasedExtractions.db";

	/**
	 * path to the modifiers-files
	 */
	static File importanceTerms = new File("src/test/resources/information_extraction/input/modifiers.txt");

	/**
	 * path to the patterns-file
	 */
	static File contextPatterns = new File("src/test/resources/information_extraction/input/competencePatterns.txt");

	/**
	 * first paragraph to read from input database
	 */
	static int startPos = 0;

	/**
	 * max number of read paragraphs (reads all if maxCount = -1)
	 */
	static int maxCount = -1;
	
	/////////////////////////////
	// END
	/////////////////////////////

	/**
	 * paragraphs to extract from
	 */
	static List<ClassifyUnit> paragraphs;
	/**
	 * connection to input-db
	 */
	static Connection inputConnection;
	/**
	 * connection to output-db
	 */
	static Connection outputConnection;

	/**
	 * - set class-translations - connect to input- and output-db - check
	 * validity of count- and startPos-values - read classifyUnits from input-db
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	@BeforeClass
	public static void initialize() throws ClassNotFoundException, SQLException {
		// set class translations
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
		JASCClassifyUnit.setNumberOfCategories(stmc.getNumberOfCategories(), stmc.getNumberOfClasses(),
				stmc.getTranslations());

		// connect to input database
		if (!new File(inputData).exists()) {
			System.out.println("Database don't exists " + inputData + "\nPlease change configuration and start again.");
			System.exit(0);
		} else {
			inputConnection = IE_DBConnector.connect(inputData);
		}
		// connect to output database
		outputConnection = IE_DBConnector.connect(outputDB);
		IE_DBConnector.createOutputTable(outputConnection, IEType.COMPETENCE, false);

		// check if count and startPos are valid
		String query = "SELECT COUNT(*) FROM ClassifiedParagraphs;";
		Statement stmt = inputConnection.createStatement();
		ResultSet countResult = stmt.executeQuery(query);
		int tableSize = countResult.getInt(1);
		stmt.close();
		if (tableSize <= startPos) {
			System.out.println("startPosition (" + startPos + ")is greater than tablesize (" + tableSize + ")");
			System.out.println("please select a new startPosition and try again");
			System.exit(0);
		}
		if (maxCount > tableSize - startPos) {
			maxCount = tableSize - startPos;
		}
		// read ClassigfyUnits from db
		paragraphs = IE_DBConnector.getClassifyUnitsUnitsFromDB(maxCount, startPos, inputConnection, IEType.COMPETENCE);
		System.out.println("\n");
	}

	/**
	 * executes rulebased-extraction on the selected classifyUnits
	 * 
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws SQLException
	 */
	@Test
	public void extract() throws SQLException, ClassNotFoundException, IOException {
		List<ExtractionUnit> extractionUnits = ExtractionUnitBuilder.initializeIEUnits(paragraphs);
		IEJobs jobs = new IEJobs(null, null, importanceTerms, contextPatterns, IEType.COMPETENCE);
		jobs.annotateTokens(extractionUnits);
		Map<ExtractionUnit, Map<InformationEntity, List<ExtractionPattern>>> patternExtractions = jobs
				.extractByPatterns(extractionUnits);
		patternExtractions = jobs.mergeInformationEntities(patternExtractions);
		jobs.updateEntitiesList(patternExtractions);
		jobs.setModifiers(patternExtractions);
		IE_DBConnector.writeCompetences(patternExtractions, outputConnection, false);
		System.out.println("\n");
		System.out.println("finished RuleBasedExtraction");
		System.out.println("extracted " + jobs.knownEntities + " competence-types");
		System.out.println("Extractions are stored in DB: " + outputDB);
	}

}
