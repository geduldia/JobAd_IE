package de.uni_koeln.spinfo.information_extraction.extraction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;
import de.uni_koeln.spinfo.classification.jasc.data.JASCClassifyUnit;
import de.uni_koeln.spinfo.classification.zoneAnalysis.helpers.SingleToMultiClassConverter;
import de.uni_koeln.spinfo.information_extraction.data.ExtractionPattern;
import de.uni_koeln.spinfo.information_extraction.data.ExtractionUnit;
import de.uni_koeln.spinfo.information_extraction.data.IEType;
import de.uni_koeln.spinfo.information_extraction.data.InformationEntity;
import de.uni_koeln.spinfo.information_extraction.db_io.IE_DBConnector;
import de.uni_koeln.spinfo.information_extraction.preprocessing.ExtractionUnitBuilder;

/**
 * @author geduldia
 * 
 *         The main Extractor for the BIBB-Database. Has methods to extract Information and to store
 *         them in a new Database
 *			
 */
public class Extractor {

	private IEJobs jobs;
	private IEType type;
	private Set<String> knownEntities;
	private Set<String> noEntities;
	private File entitiesFile;
	private File noEntitiesFile;
	/**
	 * Constructor for competence-extraction
	 * @param outputConnection
	 * 				connection to output-db
	 * @param competences
	 *            file of already known/extracted competences
	 * @param noCompetences
	 *            file of known typical mistakes
	 * @param contexts
	 *            file of context-patterns for comp.-extraction
	 * @param modifierTerms
	 *            file of modifiers
	 * @param modifierTerms
	 *            file of importance-terms
	 * @param type
	 *            type of information (usually competences)
	 * @throws IOException
	 * @throws SQLException
	 */
	public Extractor(Connection outputConnection, File competences, File noCompetences, File contexts, File modifierTerms, IEType type) throws IOException, SQLException {	
		this.entitiesFile = competences;
		this.noEntitiesFile = noCompetences;
		this.type = type;
		this.jobs = new IEJobs(competences, noCompetences, modifierTerms, contexts, type);
		if(outputConnection != null){
			knownEntities = IE_DBConnector.readAnnotatedEntities(outputConnection, 1, type);
			noEntities = IE_DBConnector.readAnnotatedEntities(outputConnection, 0, type);
			jobs.addKnownEntities(knownEntities);
			jobs.addNoEntities(noEntities);
		}	
		initialize();
	}

	/**
	 * Constructor for tool-extraction
	 * 
	 * @param outputConnection
	 * 				connection to output-db
	 * @param tools
	 *            file of already known/extracted tools
	 * @param noTools
	 *            file of known typical mistakes
	 * @param contexts
	 *            file of context-patterns for comp.-extraction
	 * @param type
	 *            type of information (usually tools)
	 * @throws IOException
	 * @throws SQLException 
	 */
	public Extractor(Connection outputConnection, File tools, File noTools, File contexts, IEType type) throws IOException, SQLException {
		this.entitiesFile = tools;
		this.noEntitiesFile = noTools;
		this.type = type;
		this.jobs = new IEJobs(tools, noTools, contexts, type);
		if(outputConnection != null){
			knownEntities = IE_DBConnector.readAnnotatedEntities(outputConnection, 1, type);
			noEntities = IE_DBConnector.readAnnotatedEntities(outputConnection, 0, type);
			jobs.addKnownEntities(knownEntities);
			jobs.addNoEntities(noEntities);
		}		
		initialize();
	}

	private void initialize() {
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
	}



	/**
	 * Extracts Information (e.g. competences) from the input-database of
	 * ClassifyUnits and writes them in a new output-database
	 * 
	 * @param startPos
	 *            db startpositon for extraction 
	 * @param count
	 *            max number of classifyUnits to extract from
	 * @param tablesize
	 *            tablesize of the input-db
	 * @param inputConnection
	 *            input-connection
	 * @param outputConnection
	 * 				output-connection
	 * @throws IOException
	 * @throws SQLException
	 */
	public void extract(int startPos, int count, int tablesize, Connection inputConnection, Connection outputConnection)
			throws IOException, SQLException {
		Map<ExtractionUnit, Map<InformationEntity, List<ExtractionPattern>>> allExtractions = new HashMap<ExtractionUnit, Map<InformationEntity, List<ExtractionPattern>>>();
		boolean finished = false;
		int readClassifyUnits = 0;
		int start = startPos;
		while (!finished) {
			// get Paragraphs from ClassesCorrectable-DB
			int maxCount = 100000;
			if (readClassifyUnits + maxCount > count) {
				maxCount = count - readClassifyUnits;
			}

			List<ClassifyUnit> classifyUnits = IE_DBConnector.getClassifyUnitsUnitsFromDB(maxCount, start,
					inputConnection, jobs.type);

			System.out.println("\nselected " + classifyUnits.size() + " classifyUnits from DB for " + jobs.type.name()
					+ "-detection\n");

			readClassifyUnits = readClassifyUnits + maxCount;
			if (readClassifyUnits >= count) {
				finished = true;
			}
			if (startPos + readClassifyUnits >= tablesize) {
				finished = true;
			}
			start = start + maxCount;

			List<ExtractionUnit> extractionUnits = ExtractionUnitBuilder.initializeIEUnits(classifyUnits);
			jobs.annotateTokens(extractionUnits);
			Map<ExtractionUnit, Map<InformationEntity, List<ExtractionPattern>>> extractions = jobs
					.extractByPatterns(extractionUnits);
			allExtractions.putAll(extractions);
		}
		// remove already known entities
		jobs.mergeInformationEntities(allExtractions);
		allExtractions = removeKnownEntities(allExtractions);
		if (allExtractions.isEmpty()) {
			System.out.println("\n no new " + jobs.type.name() + "s found\n");
		} else {
			IE_DBConnector.createOutputTable(outputConnection, type, true);
			if (type == IEType.COMPETENCE) {
				IE_DBConnector.writeCompetences(allExtractions, outputConnection, true);
			}
			if (type == IEType.TOOL) {
				IE_DBConnector.writeTools(allExtractions, outputConnection, true);
			}
			System.out.println("\n finished "+jobs.type.name()+"s extraction\n");
			System.out.println("\n see and edit results in database '"+outputConnection.getMetaData().getURL().substring(12));
		}
		writeOutputFile();
	}

	

	private Map<ExtractionUnit, Map<InformationEntity, List<ExtractionPattern>>> removeKnownEntities(
			Map<ExtractionUnit, Map<InformationEntity, List<ExtractionPattern>>> allExtractions) {
		Map<ExtractionUnit, Map<InformationEntity, List<ExtractionPattern>>> toReturn = new HashMap<ExtractionUnit, Map<InformationEntity, List<ExtractionPattern>>>();
		for (ExtractionUnit extractionUnit : allExtractions.keySet()) {

			Map<InformationEntity, List<ExtractionPattern>> ies = allExtractions.get(extractionUnit);
			Map<InformationEntity, List<ExtractionPattern>> filterdIes = new HashMap<InformationEntity, List<ExtractionPattern>>();
			for (InformationEntity ie : ies.keySet()) {
				Set<InformationEntity> knownIEs = jobs.entities.get(ie.getToken());
				if (knownIEs == null || (!knownIEs.contains(ie))) {
					filterdIes.put(ie, ies.get(ie));
				}
			}
			if (!filterdIes.isEmpty()) {
				toReturn.put(extractionUnit, filterdIes);
			}
		}
		return toReturn;
	}

	private void writeOutputFile() throws IOException {
	
		if (!entitiesFile.exists()) {
			entitiesFile.createNewFile();
		} else {
			BufferedReader in = new BufferedReader(new FileReader(entitiesFile));
			String line = in.readLine();
			while (line != null) {
				knownEntities.add(line);
				line = in.readLine();
			}
			in.close();
		}
		PrintWriter out = new PrintWriter(new FileWriter(entitiesFile));
		for (String string : knownEntities) {
			out.write(string + "\n");
		}
		out.close();
		
		if (!noEntitiesFile.exists()) {
			noEntitiesFile.createNewFile();
		} else {
			BufferedReader in = new BufferedReader(new FileReader(noEntitiesFile));
			String line = in.readLine();
			while (line != null) {
				noEntities.add(line);
				line = in.readLine();
			}
			in.close();
		}
		out = new PrintWriter(new FileWriter(noEntitiesFile));
		for (String string : noEntities) {
			out.write(string + "\n");
		}
		out.close();
	}
}
