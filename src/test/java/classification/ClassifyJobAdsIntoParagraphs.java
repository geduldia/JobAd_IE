package classification;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Test;

import de.uni_koeln.spinfo.classification.db_io.DbConnector;
import de.uni_koeln.spinfo.classification.jasc.workflow.ConfigurableDatabaseClassifier;

/**
 * @author geduldia
 * 
 * A test-class to classify JobAds into four classes of paragraphs:
 * 
 * class 1: about the company
 * class 2: job-description
 * class 3: applicant profile
 * class 4: formalities 
 * 
 * you can use an existing Model (includes a classifier, distance-measere etc.) or build a new model
 *
 */
public class ClassifyJobAdsIntoParagraphs {

	/////////////////////////////
	// APP-CONFIGURATION
	/////////////////////////////

	/**
	 * Path to input database (A small demo of the BIBB-database)
	 */
	static String inputDB = "src/test/resources/classification/input/JobAdDB.db";

	/**
	 * path to the output folder for classified paragraphs
	 */
	static String outputFolder = "src/test/resources/classification/output/";

	/**
	 * Path to the trainingdata-file annotated by Spinfo (is used to train the
	 * classifiers)
	 */
	static String trainingdataFile = "src/test/resources/classification/input/trainingData.csv";

	/**
	 * correctable output database
	 */
	static String corrOutputDB = "CorrectableClassifiedParagraphs.db";

	/**
	 * original output database
	 */
	static String origOutputDB = "OriginalClassifiedParagraphs.db";

	/**
	 * overall results fetched from db, no limit: -1
	 */
	static int queryLimit = -1;

	/**
	 * start query from entry with id larger than startId (if there is a query
	 * limit)
	 */
	static int startId = 0;

	/**
	 * number of results fetched in one step
	 */
	static int fetchSize = 100;

	/////////////////////////////
	// END
	/////////////////////////////

	@Test
	public void classify() throws ClassNotFoundException, SQLException, IOException {

		// Connect to input database
		Connection inputConnection = null;
		if (!new File(inputDB).exists()) {
			System.out
					.println("Database '" + inputDB + "' don't exists \nPlease change configuration and start again.");
			System.exit(0);
		} else {
			inputConnection = DbConnector.connect(inputDB);
		}

		// Connect to output database
		Connection corrConnection = null;
		Connection origConnection = null;
		File corrDBFile = new File(outputFolder+corrOutputDB);
		File origDBFile = new File(outputFolder+origOutputDB);

		// if outputdatabase already exists
		if (corrDBFile.exists()) {

			// use or override current outputDatabase
			System.out.println("\noutput-database  already exists. "
					+ "\n - press 'o' to overwrite it (deletes all prior entries - annotated Trainingsparagraphs are saved in Trainingdatabase)"
					+ "\n - press 'u' to use it (adds and replaces entries)"
					+ "\n - press 'c' to create a new Output-Database");
			boolean answered = false;
			while (!answered) {
				BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
				String answer = in.readLine();
				if (answer.toLowerCase().trim().equals("o")) {
					corrConnection = DbConnector.connect(outputFolder+corrOutputDB);
					DbConnector.createClassificationOutputTables(corrConnection, true);
					origConnection = DbConnector.connect(outputFolder+origOutputDB);
					DbConnector.createClassificationOutputTables(origConnection, false);
					answered = true;
				} else if (answer.toLowerCase().trim().equals("u")) {
					corrConnection = DbConnector.connect(outputFolder+corrOutputDB);
					origConnection = DbConnector.connect(outputFolder+origOutputDB);
					if (!origDBFile.exists()) {
						DbConnector.createClassificationOutputTables(origConnection, false);
					}
					answered = true;
				} else if (answer.toLowerCase().trim().equals("c")) {
					System.out.println("Please enter the name of the new correctable Database. It will be stored in "
							+ outputFolder);
					BufferedReader ndIn = new BufferedReader(new InputStreamReader(System.in));
					corrOutputDB = ndIn.readLine();
					corrConnection = DbConnector.connect(outputFolder + corrOutputDB);
					DbConnector.createClassificationOutputTables(corrConnection, true);
					System.out.println(
							"Please enter the name of the new original Database. It will be stored in " + outputFolder);
					ndIn = new BufferedReader(new InputStreamReader(System.in));
					origOutputDB = ndIn.readLine();
					origConnection = DbConnector.connect(outputFolder + origOutputDB);
					DbConnector.createClassificationOutputTables(origConnection, false);
					answered = true;
				} else {
					System.out.println("C: invalid answer! please try again...");
					System.out.println();
				}
			}
		}

		// if output database does not exist
		else {
			// create output-database
			corrConnection = DbConnector.connect(outputFolder + corrOutputDB);
			DbConnector.createClassificationOutputTables(corrConnection, true);
			origConnection = DbConnector.connect(outputFolder + origOutputDB);
			DbConnector.createClassificationOutputTables(origConnection, false);
		}

		// create output-directory if not exists
		if (!new File("classification/output").exists()) {
			new File("classification/output").mkdirs();
		}

		// start classifying
		ConfigurableDatabaseClassifier dbClassfy = new ConfigurableDatabaseClassifier(inputConnection, corrConnection,
				origConnection, null, queryLimit, fetchSize,

				startId, false, true, trainingdataFile);
		try {
			StringBuffer sb = new StringBuffer();
			sb.append("\nstart classification....\n\n");
			sb.append("\ninput DB: " + inputDB.substring(inputDB.lastIndexOf("/") + 1));
			sb.append("\ncorrectable output DB: " + corrDBFile.getName());
			sb.append("\noriginal output DB: " + origDBFile.getName());
			sb.append("\nused Trainingdata: ");
			sb.append(trainingdataFile.substring(trainingdataFile.lastIndexOf("/") + 1));
			sb.append("\n\n");
			dbClassfy.classify(sb);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
