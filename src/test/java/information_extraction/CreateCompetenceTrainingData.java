package information_extraction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.Test;

import de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;
import de.uni_koeln.spinfo.classification.zoneAnalysis.data.ZoneClassifyUnit;
import de.uni_koeln.spinfo.classification.zoneAnalysis.helpers.SingleToMultiClassConverter;
import de.uni_koeln.spinfo.classification.zoneAnalysis.workflow.ZoneJobs;
import de.uni_koeln.spinfo.information_extraction.data.ExtractionUnit;
import de.uni_koeln.spinfo.information_extraction.data.IEType;
import de.uni_koeln.spinfo.information_extraction.data.InformationEntity;
import de.uni_koeln.spinfo.information_extraction.data.TextToken;
import de.uni_koeln.spinfo.information_extraction.db_io.IE_DBConnector;
import de.uni_koeln.spinfo.information_extraction.preprocessing.ExtractionUnitBuilder;

/**
 * @author geduldia
 * 
 *         A class to manually annotate competences and store in a database. The
 *         created database can be used as evaluation-corpus for the
 *         IE-workflows in src/test/java/information_extraction/evaluation/...
 * 
 *         You find an already annotated corpus in
 *         src/test/resources/information_extraction/trainingdata/
 *         TrainingData_Competences.db
 * 
 * 
 * 
 */
public class CreateCompetenceTrainingData {

	/////////////////////////////
	// APP-CONFIGURATION
	/////////////////////////////

	/**
	 * path to the output-DB for annotated data
	 */
	private static String outputFileName = "src/test/resources/information_extraction/trainingdata/myEvaluationData.db";

	/////////////////////////////
	// END
	/////////////////////////////

	private static File dataFile = new File("src/test/resources/classification/input/trainingData.csv");

	private static ZoneJobs jobs;

	private static Connection connection;

	private static File helperFile;

	/**
	 * prints ExtractionUnits on the console and expects annotations
	 * 
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws SQLException
	 */
	@Test
	public void annotate() throws ClassNotFoundException, IOException, SQLException {
		int alreadyAnnotated = prepare();
		List<ClassifyUnit> filtered = getClassifyUnits();
		System.out.println("\nread " + filtered.size() + " paragraphs from db for annotation\n");
		System.out.println(
				"YOU CAN PRINT 'stop' TO FINISH THIS ANNOTATION AT ANY TIME. \n(annotated data will be stored in "
						+ outputFileName + " - to continue at the same point just start this app again)\n\n ");

		Map<ExtractionUnit, Set<InformationEntity>> trainingData = new HashMap<ExtractionUnit, Set<InformationEntity>>();
		// annotate
		String answer = null;
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		int c = alreadyAnnotated;
		List<ExtractionUnit> before = new ArrayList<ExtractionUnit>();
		while (c < filtered.size()) {
			if (c < 0)
				c = 0;
			ClassifyUnit cu = filtered.get(c);
			System.out.println(
					"_____________________________________________NEW PARAGRAPH__________________________________________________");
			System.out.println("\npress 'y' to annotate this paragraph");
			System.out.println("press 'n' to skip this paragraph");
			System.out.println("press 'b' to go to back to the last paragraph\n");
			System.out.println(cu.getContent());
			while (true) {
				answer = in.readLine();
				if (answer.trim().toLowerCase().equals("n")) {
					before = null;
					c++;
					alreadyAnnotated++;
					break;
				}
				if (answer.trim().toLowerCase().equals("b")) {
					if (before != null) {
						for (ExtractionUnit ieUnit : before) {
							System.out.println("remove --> " + ieUnit.getSentence() + " <--  from TD");
							trainingData.remove(ieUnit);
						}
					}
					before = null;
					alreadyAnnotated--;
					c--;
					break;
				}
				if (answer.toLowerCase().equals("stop")) {
					writeTempFile(alreadyAnnotated);
					saveData(trainingData, connection);
					System.out.println("saved annotated TD in Database");
					return;
				}
				if (answer.equals("y")) {
					System.out.println("...splits paragraph into sentences...");
					List<ExtractionUnit> ieUnits = ExtractionUnitBuilder.initializeIEUnits(cu);
					before = ieUnits;
					int e = 0;
					while (e < ieUnits.size()) {
						ExtractionUnit ieUnit = ieUnits.get(e);
						InformationEntity lastEntity = null;
						Set<InformationEntity> iesForUnit = new HashSet<InformationEntity>();
						List<TextToken> tokens = ieUnit.getTokenObjects();
						printIeUnit(ieUnit);
						System.out.println("\n- press 'enter' if sentence doesn't contain a competence or ");
						System.out.println("- annotate a competence by specifying its indices e.g. 2 or 4-6\n");
						while (true) {
							answer = in.readLine();
							if (answer.equals("")) {
								trainingData.put(ieUnit, iesForUnit);
								e++;
								break;
							}
							if (answer.equals("b")) {
								if (lastEntity != null) {
									System.out.println("removed entities for current sentence");
									iesForUnit.remove(lastEntity);
								}
								break;
							}
							InformationEntity ie;
							String[] fromTo = answer.split("-");
							try {
								// interpret answer
								int from = Integer.parseInt(fromTo[0]) + 1;
								if (fromTo.length == 1) {
									ie = new InformationEntity(tokens.get(from).getLemma(), true);
								} else {
									int to = Integer.parseInt(fromTo[1]) + 1;
									ie = new InformationEntity(tokens.get(from).getLemma(), false);
									List<String> exp = new ArrayList<String>();
									for (int i = from; i <= to; i++) {
										exp.add(tokens.get(i).getLemma());
									}
									ie.setExpression(exp);
								}
								System.out.println("add: " + ie);
								iesForUnit.add(ie);
								lastEntity = ie;
							} catch (Exception exp) {
								if (answer.equals("stop")) {
									System.out.println("!!!please finish the current paragrap first!!!");
									break;
								}
								System.out.println("invalid answer");
								break;
							}
							System.out.println("\n- annotate next competence or");
							System.out.println("- press 'enter' to go to the next sentence or");
							System.out.println("- press 'b' to reset all annotations of this sentence");
						}
						trainingData.put(ieUnit, iesForUnit);
					}
					alreadyAnnotated++;
				} else {
					System.out.println("invalid answer...");
					c--;
				}
				c++;
				break;
			}

		}
	}

	private static List<ClassifyUnit> getClassifyUnits() throws IOException {
		// read and pre-filter ClassifyUnits
		List<ClassifyUnit> cus = jobs.getCategorizedParagraphsFromFile(dataFile, false);
		// cus.addAll(jobs.getCategorizedParagraphsFromFile(dataFile2, false));
		List<ClassifyUnit> filtered = new ArrayList<ClassifyUnit>();
		for (ClassifyUnit cu : cus) {
			boolean[] classes = ((ZoneClassifyUnit) cu).getClassIDs();
			if (classes[2] && !classes[0] && !classes[1] && !classes[3]) {
				filtered.add(cu);
			}
		}
		// scramble classifyUnits
		Random random = new Random(0);
		Collections.shuffle(filtered, random);
		return filtered;
	}

	private static int prepare() throws IOException, ClassNotFoundException, SQLException {
		File dbFile = new File(outputFileName);
		String dbName = dbFile.getName().substring(0, dbFile.getName().length() - 3);
		helperFile = new File("src/test/resources/information_extraction/trainingdata/helperFile_" + dbName);
		int alreadyAnnotated = readNumberOfAnnotetdClassifyUnits(helperFile);
		if (!dbFile.exists()) {
			connection = IE_DBConnector.connect(outputFileName);
			IE_DBConnector.createOutputTable(connection, IEType.COMPETENCE, false);
		} else {
			connection = IE_DBConnector.connect(outputFileName);
		}
		// set Translations
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
		jobs = new ZoneJobs(stmc);
		return alreadyAnnotated;
	}

	private static void writeTempFile(int annotated) throws IOException {
		PrintWriter out = new PrintWriter(new FileWriter(helperFile));
		out.write(" " + annotated);
		out.flush();
		out.close();
	}

	private static int readNumberOfAnnotetdClassifyUnits(File helperFile) throws IOException {
		if (!helperFile.exists())
			return 0;
		BufferedReader in = new BufferedReader(new FileReader(helperFile));
		String line = in.readLine();
		if (line == null) {
			in.close();
			return 0;
		}
		int annotated = Integer.parseInt(line.trim());
		if (annotated < 0) {
			annotated = 0;
		}
		in.close();
		return annotated;
	}

	private static void saveData(Map<ExtractionUnit, Set<InformationEntity>> trainingData, Connection connection)
			throws ClassNotFoundException, SQLException {

		for (ExtractionUnit ieunt : trainingData.keySet()) {
			List<InformationEntity> list = new ArrayList<InformationEntity>(trainingData.get(ieunt));
			IE_DBConnector.writeCompetenceTrainingData(ieunt, list, connection);
		}
	}

	private static void printIeUnit(ExtractionUnit ieUnit) {
		List<TextToken> tokens = ieUnit.getTokenObjects();
		System.out.println();
		for (int i = 1; i < tokens.size() - 1; i++) {
			System.out.print(tokens.get(i).getString() + "(" + (i - 1) + ") ");
		}
		System.out.println();
	}

}
