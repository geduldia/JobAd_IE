package de.uni_koeln.spinfo.information_extraction.extraction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uni_koeln.spinfo.information_extraction.data.ExtractionPattern;
import de.uni_koeln.spinfo.information_extraction.data.ExtractionUnit;
import de.uni_koeln.spinfo.information_extraction.data.IEType;
import de.uni_koeln.spinfo.information_extraction.data.InformationEntity;
import de.uni_koeln.spinfo.information_extraction.data.PatternToken;
import de.uni_koeln.spinfo.information_extraction.data.TextToken;
import de.uni_koeln.spinfo.information_extraction.data.Token;

/**
 * @author geduldia
 * 
 *         This class contains all main methods for the information extraction
 *
 */
public class IEJobs {

	/**
	 * Extraction-Type (e.g. competences or tools)
	 */
	IEType type;

	/**
	 * list of modifiers (e.g. 'wünschenswert' or 'zwingend erforderlich')
	 * sorted by their first token in a map
	 */
	private Map<String, Set<List<String>>> modifiers;

	/**
	 * list of already extracted entities (sorted by their first token in map)
	 */
	public Map<String, Set<InformationEntity>> entities;

	/**
	 * list of known typical mistakes (only used in BIBB-Applications) sorted by
	 * their first token in a map
	 */
	private Map<String, Set<List<String>>> negExamples;

	/**
	 * number of already extracted entities
	 */
	public int knownEntities;

	/**
	 * list of ExtractionPatterns
	 */
	public List<ExtractionPattern> extractionPatterns;

	/**
	 * Constructor for competence-extraction
	 * 
	 * @param modifiers
	 *            File to the modifier-list
	 * @param extractionPatterns
	 *            pattern-file
	 * @param type
	 *            IE-type (competences)
	 * @throws IOException
	 */
	public IEJobs(File modifiers, File extractionPatterns, IEType type) throws IOException {
		this.type = type;
		initialize(null, null, modifiers, extractionPatterns);
	}

	/**
	 * Constructor for tool-extraction
	 * 
	 * @param extractionPatterns
	 *            pattern-file
	 * @param type
	 *            IE-type (tools)
	 * @throws IOException
	 */
	public IEJobs(File extractionPatterns, IEType type) throws IOException {
		this.type = type;
		initialize(null, null, extractionPatterns);
	}

	/**
	 * Constructor for Competence-Extraction in BIBB
	 * 
	 * @param competences
	 *            File to already known competences
	 * @param noCompetences
	 *            File to knwon typical mistakes
	 * @param modifiers
	 *            File to the modifier-list
	 * @param extractionPatterns
	 *            pattern-file
	 * @param type
	 *            IE-type (competences)
	 * @throws IOException
	 */
	public IEJobs(File competences, File noCompetences, File modifiers, File extractionPatterns, IEType type)
			throws IOException {
		this.type = type;
		initialize(competences, noCompetences, modifiers, extractionPatterns);
	}

	/**
	 * Constructor for tool-Extraction in BIBB
	 * 
	 * @param tools
	 *            File to already known tools
	 * @param noTools
	 *            File to knwon typical mistakes
	 * @param patterns
	 *            patterns-file
	 * @param type
	 *            IE-type (tools)
	 * @throws IOException
	 */
	public IEJobs(File tools, File noTools, File patterns, IEType type) throws IOException {
		this.type = type;
		initialize(tools, noTools, patterns);
	}

	/**
	 * annotate tokens of the given ExtractionUnits as entity, modifier or
	 * noEntity
	 * 
	 * @param extractionUnits
	 * @throws IOException
	 */
	public void annotateTokens(List<ExtractionUnit> extractionUnits) throws IOException {
		for (ExtractionUnit currentExtractionUnit : extractionUnits) {
			List<TextToken> tokens = currentExtractionUnit.getTokenObjects();
			if (!entities.isEmpty()) {
				annotateEntities(tokens);
			}
			if (negExamples != null) {
				annotateNegativeExamples(tokens);
			}
			if (type.equals(IEType.COMPETENCE)) {
				annotateModifiers(tokens);
			}
		}
	}

	/**
	 * extract Entities by Patterns (manually written or auto-generated)
	 * 
	 * @param extractionUnits
	 *            ExtractionUnits to extract from
	 * @return A map of the given ExtractionUnits and the identified Entities in
	 *         this ExtractionUnit (together with the matched patterns)
	 */

	public Map<ExtractionUnit, Map<InformationEntity, List<ExtractionPattern>>> extractByPatterns(
			List<ExtractionUnit> extractionUnits) {
		Map<ExtractionUnit, Map<InformationEntity, List<ExtractionPattern>>> toReturn = new HashMap<ExtractionUnit, Map<InformationEntity, List<ExtractionPattern>>>();
		for (ExtractionUnit extractionUnit : extractionUnits) {
			List<TextToken> tokens = extractionUnit.getTokenObjects();
			for (ExtractionPattern pattern : extractionPatterns) {
				// check if extractionUnit matches with current pattern
				for (int t = 0; t <= tokens.size() - (pattern.getSize() - pattern.getOptionalTokens()); t++) {
					boolean match = false;
					int entityIndex = 0;
					int requiredToCompleteModifier = 0;
					int requiredToCompleteEntity = 0;
					int skip = 0;
					for (int p = 0; p < pattern.getSize(); p++) {
						if (p + skip >= pattern.getSize()) {
							break;
						}
						// check if current TextToken matches current
						// PatternToken
						int i = t + requiredToCompleteModifier + requiredToCompleteEntity;
						if (i + p >= tokens.size()) {
							continue;
						}
						TextToken textToken = tokens.get(i + p);
						PatternToken patternToken = pattern.getTokenAt(p + skip);
						match = textToken.isEqualsPatternToken(patternToken);
						if (!match) {
							// check if PatternToken is only optional
							if (pattern.getTokens().size() > p + skip + 1) {
								PatternToken nextToken = (PatternToken) pattern.getTokenAt(p + skip + 1);
								if (((PatternToken) patternToken).isOptional()
										&& ((TextToken) textToken).isEqualsPatternToken(nextToken)) {
									match = true;
									skip++;
								}
							}
						}
						if (!match) {
							break;
						}
						if (pattern.getExtractionPointer().get(0) == p) {
							entityIndex = i + p - skip;
						}
						if (patternToken.isInformationEntity()) {
							requiredToCompleteEntity = ((TextToken) textToken).getTokensToCompleteInformationEntity();
						}
						if (patternToken.isModifier()) {
							requiredToCompleteModifier = ((TextToken) textToken).getTokensToCompleteModifier();
						}
					}
					// current sentence matches with current pattern!
					if (match) {
						TextToken entityToken = tokens.get(entityIndex);
						String entityLemma = normalizeLemma(entityToken.getLemma());
						int entitySize = pattern.getExtractionPointer().size();
						InformationEntity newEntity;
						if (entitySize == 1) {
							if (entityLemma.length() > 1 && !(entityToken.isNoEntity())) {
								newEntity = new InformationEntity(normalizeLemma(entityToken.getLemma()), true);
							} else {
								// found entity is in blacklist (negExamples)
								newEntity = null;
								continue;
							}
						} else {
							newEntity = new InformationEntity(normalizeLemma(entityToken.getLemma()), false);
							// set rest of entity
							List<String> fullEntity = new ArrayList<String>();
							for (int s = 0; s < pattern.getExtractionPointer().size(); s++) {
								String expression = normalizeLemma(tokens.get(entityIndex + s).getLemma());
								if (!expression.trim().equals("") && !expression.trim().equals("--")) {
									fullEntity.add(normalizeLemma(tokens.get(entityIndex + s).getLemma()));
								}
							}
							if (fullEntity.size() > 1) {
								newEntity.setExpression(fullEntity);
							} else if (fullEntity.size() < 1) {
								newEntity = null;
								continue;
							} else {
								newEntity = new InformationEntity(normalizeLemma(entityToken.getLemma()), true);
							}
						}
						// check if full Entity is on blacklist (negExamples)
						boolean isNoEntity = false;
						if (negExamples.containsKey(newEntity.getToken())) {
							Set<List<String>> set = negExamples.get(newEntity.getToken());
							if (set.contains(newEntity.getTokens())) {
								isNoEntity = true;
							}
						}
						if (isNoEntity) {
							newEntity = null;
							continue;
						}
						// annotate token
						entityToken.setInformationEntity(true);
						entityToken.setTokensToCompleteInformationEntity(entitySize - 1);
						// add to return-map
						Map<InformationEntity, List<ExtractionPattern>> map = toReturn.get(extractionUnit);
						if (map == null)
							map = new HashMap<InformationEntity, List<ExtractionPattern>>();
						List<ExtractionPattern> list = map.get(newEntity);
						if (list == null)
							list = new ArrayList<ExtractionPattern>();
						list.add(pattern);
						map.put(newEntity, list);
						toReturn.put(extractionUnit, map);
					}
				}
			}
		}
		return toReturn;
	}

	/**
	 * merge entities if one fully contains the other
	 * 
	 * @param extractions
	 *            map of extractionUnits and extracted entities
	 * @return merged extractions
	 */
	public Map<ExtractionUnit, Map<InformationEntity, List<ExtractionPattern>>> mergeInformationEntities(
			Map<ExtractionUnit, Map<InformationEntity, List<ExtractionPattern>>> extractions) {
		for (ExtractionUnit ieunit : extractions.keySet()) {

			Map<InformationEntity, List<ExtractionPattern>> merged = new HashMap<InformationEntity, List<ExtractionPattern>>();

			List<InformationEntity> iesForUnit = new ArrayList<>(extractions.get(ieunit).keySet());
			InformationEntity containingIE = null;
			for (int i = 0; i < iesForUnit.size(); i++) {
				InformationEntity currentIE = iesForUnit.get(i);
				boolean isPartOfOtherIE = false;
				for (int j = 0; j < iesForUnit.size(); j++) {
					if (j == i)
						continue;
					InformationEntity otherIE = iesForUnit.get(j);
					// check if currentIE is in otherIE
					isPartOfOtherIE = containsList(otherIE.getTokens(), currentIE.getTokens());
					if (isPartOfOtherIE) {
						containingIE = otherIE;
						break;
					}
				}
				if (!isPartOfOtherIE) {
					merged.put(currentIE, extractions.get(ieunit).get(currentIE));
				} else {
					if (containingIE.getTokens().contains("und") || containingIE.getTokens().contains("oder")) {
						merged.put(currentIE, extractions.get(ieunit).get(currentIE));
					}
				}
			}
			extractions.put(ieunit, merged);
		}
		return extractions;
	}

	/**
	 * updates the lists of known entities with the given extractions
	 * 
	 * @param extractions
	 */
	public void updateEntitiesList(Map<ExtractionUnit, Map<InformationEntity, List<ExtractionPattern>>> extractions) {
		for (ExtractionUnit ieUnit : extractions.keySet()) {
			Set<InformationEntity> ies = extractions.get(ieUnit).keySet();
			Set<InformationEntity> emptyIEs = new HashSet<InformationEntity>();
			for (InformationEntity ie : ies) {
				if (!containsLetter(ie.getFullExpression())) {
					emptyIEs.add(ie);
					continue;
				}
				Set<InformationEntity> set = entities.get(ie.getToken());
				if (set == null) {
					set = new HashSet<InformationEntity>();
				}
				boolean isNew = set.add(ie);
				if (isNew) {
					knownEntities++;
				}
				entities.put(ie.getToken(), set);
			}
			ies.removeAll(emptyIEs);
		}
	}

	/**
	 * finds (and extracts) occurrences of known entities via stringmatching
	 * 
	 * @param extractionUnits
	 * @param patternExtractions
	 *            entities extracted by patterns
	 * @return map of ExtractionUnits and their containing Entities (only
	 *         ExtractionUnits which don't match a pattern)
	 * 
	 */
	public Map<ExtractionUnit, Map<InformationEntity, List<ExtractionPattern>>> extractByStringMatch(
			List<ExtractionUnit> extractionUnits,
			Map<ExtractionUnit, Map<InformationEntity, List<ExtractionPattern>>> patternExtractions) {
		Map<ExtractionUnit, Map<InformationEntity, List<ExtractionPattern>>> extractions = new HashMap<ExtractionUnit, Map<InformationEntity, List<ExtractionPattern>>>();
		List<ExtractionPattern> patternList = new ArrayList<ExtractionPattern>();
		for (ExtractionUnit extractionUnit : extractionUnits) {
			List<TextToken> tokens = extractionUnit.getTokenObjects();
			int skip = 0;
			for (int t = 0; t < tokens.size(); t++) {
				if (t + skip >= tokens.size())
					break;
				Token token = tokens.get(t + skip);
				if (token.isInformationEntity()) {
					skip += ((TextToken) token).getTokensToCompleteInformationEntity();
				}
				String lemma = normalizeLemma(token.getLemma());
				if (entities.keySet().contains(lemma)) {
					for (InformationEntity ie : entities.get(lemma)) {
						if (ie.isComplete()) {
							token.setInformationEntity(true);
							if (patternExtractions.get(extractionUnit) == null
									|| !(patternExtractions.get(extractionUnit).containsKey(ie))) {

								InformationEntity newIE = new InformationEntity(ie.getToken(), true);
								Map<InformationEntity, List<ExtractionPattern>> iesForUnit = extractions
										.get(extractionUnit);
								if (iesForUnit == null)
									iesForUnit = new HashMap<InformationEntity, List<ExtractionPattern>>();
								iesForUnit.put(newIE, patternList);
								extractions.put(extractionUnit, iesForUnit);
							}
							continue;
						}
						boolean matches = false;
						for (int c = 1; c < ie.getTokens().size(); c++) {
							if (tokens.size() <= t + c) {
								matches = false;
								break;
							}
							matches = ie.getTokens().get(c).equals(normalizeLemma(tokens.get(t + c).getLemma()));
							if (!matches) {
								break;
							}
						}
						// Token is start of entity
						if (matches) {
							token.setInformationEntity(true);
							((TextToken) token).setTokensToCompleteInformationEntity(ie.getTokens().size() - 1);
							if (patternExtractions.get(extractionUnit) == null
									|| !(patternExtractions.get(extractionUnit).containsKey(ie))) {
								InformationEntity newIE = new InformationEntity(ie.getToken(), false);
								newIE.setExpression(ie.getTokens());
								Map<InformationEntity, List<ExtractionPattern>> iesForUnit = extractions
										.get(extractionUnit);
								if (iesForUnit == null)
									iesForUnit = new HashMap<InformationEntity, List<ExtractionPattern>>();
								iesForUnit.put(newIE, patternList);
								extractions.put(extractionUnit, iesForUnit);
							}
						}
					}
				}
			}
		}
		return extractions;
	}

	/**
	 * searches for modifiers in the given ExtractionUnits and sets
	 * modifier-attributes to the InformationEntities
	 * 
	 * @param extractions
	 */
	public void setModifiers(Map<ExtractionUnit, Map<InformationEntity, List<ExtractionPattern>>> extractions) {
		for (ExtractionUnit extractionUnit : extractions.keySet()) {
			String longestModifier = null;
			int length = 0;
			List<TextToken> tokens = extractionUnit.getTokenObjects();
			int skip = 0;
			for (int t = 0; t < tokens.size(); t++) {
				if (t + skip >= tokens.size())
					break;
				Token token = tokens.get(t + skip);
				String lemma = normalizeLemma(token.getLemma());
				// check if token is (start of)  expression
				if (token.isModifier() && ((TextToken) token).getTokensToCompleteModifier() == 0) {
					if (longestModifier == null) {
						longestModifier = lemma;
						length = 1;
					}
				}
				if (token.isModifier() && ((TextToken) token).getTokensToCompleteModifier() > 0) {
					if (((TextToken) token).getTokensToCompleteModifier() > length) {
						StringBuffer sb = new StringBuffer();
						for (int i = 0; i < ((TextToken) token).getTokensToCompleteModifier() + 1; i++) {
							sb.append(normalizeLemma(tokens.get(t + skip + i).getLemma()) + " ");
						}
						longestModifier = sb.toString().trim();
						length = ((TextToken) token).getTokensToCompleteModifier();
					}
				}
			}
			if (longestModifier != null) {
				for (InformationEntity ie : extractions.get(extractionUnit).keySet()) {
					ie.setModifier(longestModifier);
				}
			}
		}
	}
	/**
	 * adds a list of new auto-generated patterns to the list of all patterns
	 * 
	 * @param autoPatterns
	 */
	public void addPatterns(List<ExtractionPattern> autoPatterns) {
		for (ExtractionPattern pattern : autoPatterns) {
			extractionPatterns.add(pattern);
		}
	}

	/**
	 * (only for BIBB-Applications to add manually annotated entities) adds the
	 * given set of entities to the entities-list
	 * 
	 * @param entities
	 *            set of annotated entities
	 * @throws IOException
	 */
	public void addKnownEntities(Set<String> entities) throws IOException {
		for (String comp : entities) {
			// create inform.-entity and add to posExample list
			String[] split = comp.split(" ");
			String keyword;
			try {
				keyword = normalizeLemma(split[0]);
			} catch (ArrayIndexOutOfBoundsException e) {
				continue;
			}
			Set<InformationEntity> iesForKeyword = this.entities.get(keyword);
			if (iesForKeyword == null)
				iesForKeyword = new HashSet<InformationEntity>();
			InformationEntity ie = new InformationEntity(keyword, split.length == 1);
			if (!ie.isComplete()) {
				for (String string : split) {
					ie.addToExpression(normalizeLemma(string));

				}
			}
			boolean isnew = iesForKeyword.add(ie);
			if (isnew) {
				knownEntities++;
			}
			this.entities.put(keyword, iesForKeyword);
		}
	}

	/**
	 * (only for BIBB-Applications to add manually annotated mistakes) adds the
	 * given set of wrong entities to the negExamples-list
	 * 
	 * @param noEntities
	 *            set of annotated wrong entities
	 */
	public void addNoEntities(Set<String> noEntities) {

		for (String line : noEntities) {
			String keyword;
			String[] split = line.split(" ");
			try {
				keyword = normalizeLemma(split[0]);
			} catch (ArrayIndexOutOfBoundsException e) {
				continue;
			}
			Set<List<String>> expressionsForKeyword = negExamples.get(keyword);
			if (expressionsForKeyword == null) {
				expressionsForKeyword = new HashSet<List<String>>();
			}
			List<String> expression = Arrays.asList(split);
			expressionsForKeyword.add(expression);
			negExamples.put(keyword, expressionsForKeyword);

		}
	}



	private void initialize(File tools, File noTools, File patternFile) throws IOException {
		initialize(tools, noTools, null, patternFile);
	}

	private void initialize(File knownEntities, File negativeEntities, File modifierFile, File patternFile)
			throws IOException {
		entities = new HashMap<String, Set<InformationEntity>>();
		this.knownEntities = 0;
		if (knownEntities != null) {
			readKnownEntitiesFromFile(knownEntities);
		}
		negExamples = new HashMap<String, Set<List<String>>>();
		modifiers = new HashMap<String, Set<List<String>>>();
		if (negativeEntities != null) {
			readWordList(negativeEntities, negExamples);
		}
		if (modifierFile != null) {
			readWordList(modifierFile, modifiers);
		}
		extractionPatterns = new ArrayList<ExtractionPattern>();
		readPatterns(extractionPatterns, patternFile);
	}

	private void readWordList(File inputFile, Map<String, Set<List<String>>> map) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(inputFile));
		String line = in.readLine();
		while (line != null) {
			String keyword;
			String[] split = line.split(" ");
			try {
				keyword = normalizeLemma(split[0]);
			} catch (ArrayIndexOutOfBoundsException e) {
				line = in.readLine();
				continue;
			}
			Set<List<String>> expressionsForKeyword = map.get(keyword);
			if (expressionsForKeyword == null) {
				expressionsForKeyword = new HashSet<List<String>>();
			}

			List<String> expression = Arrays.asList(split);
			expressionsForKeyword.add(expression);
			map.put(keyword, expressionsForKeyword);
			line = in.readLine();
		}
		in.close();
	}

	private void readPatterns(List<ExtractionPattern> patterns, File patternFile) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(patternFile));
		String line = in.readLine();
		ExtractionPattern pattern = new ExtractionPattern();
		int lineNumber = 0;
		while (line != null) {
			lineNumber++;
			String[] split = line.split("\t");
			// set id
			try {
				if (line.startsWith("ID:")) {
					pattern.setId(Integer.parseInt(split[1]));
				}
				// set name
				if (line.startsWith("DESCRIPTION:")) {
					pattern.setDescription(split[1].trim());
				}
				// set Token
				if (line.startsWith("TOKEN:") || line.startsWith("[TOKEN:]")) {
					String string = split[1];
					if (string.equals("null"))
						string = null;
					String lemma = split[2];
					if (lemma.equals("null"))
						lemma = null;
					String posTag = split[3];
					if (posTag.equals("null"))
						posTag = null;
					Token token = new PatternToken(string, lemma, posTag, Boolean.parseBoolean(split[4]));
					if (lemma != null && lemma.toUpperCase().equals("MODIFIER")) {
						token.setModifier(true);
					}

					if (line.startsWith("[")) {
						((PatternToken) token).setOptional(true);
					}
					pattern.addToken((PatternToken) token);
				}
				// set extraction indices
				if (line.startsWith("EXTRACT:")) {
					List<Integer> euPointer = new ArrayList<Integer>();
					String[] ints = split[1].split(",");
					for (String string : ints) {
						euPointer.add(Integer.parseInt(string));
					}
					pattern.setEUPointer(euPointer);
					patterns.add(pattern);
					pattern = new ExtractionPattern();
				}
			} catch (Exception e) {
				System.out.println("Error in  pattern file (line " + lineNumber + ")");
			}
			line = in.readLine();
		}
		in.close();
	}

	private void annotateEntities(List<TextToken> tokens) {
		for (int t = 0; t < tokens.size(); t++) {
			Token token = tokens.get(t);
			String lemma = normalizeLemma(token.getLemma());
			// check if current token is posExample
			if (entities.keySet().contains(lemma)) {
				for (InformationEntity ie : entities.get(lemma)) {
					if (ie.isComplete()) {
						// token is complete entity
						token.setInformationEntity(true);
						continue;
					}
					boolean matches = false;
					for (int c = 1; c < ie.getTokens().size(); c++) {
						if (tokens.size() <= t + c) {
							matches = false;
							break;
						}
						matches = ie.getTokens().get(c).equals(normalizeLemma(tokens.get(t + c).getLemma()));
						if (!matches) {
							break;
						}
					}
					// Token is start of entity
					if (matches) {
						token.setInformationEntity(true);
						((TextToken) token).setTokensToCompleteInformationEntity(ie.getTokens().size() - 1);
					}
				}
			}
		}
	}

	private void annotateNegativeExamples(List<TextToken> tokens) {
		for (int t = 0; t < tokens.size(); t++) {
			Token token = tokens.get(t);
			String lemma = normalizeLemma(token.getLemma());
			// check if current token is negExample
			if (negExamples.keySet().contains(lemma)) {
				boolean match = false;
				for (List<String> expression : negExamples.get(lemma)) {
					for (int s = 0; s < expression.size(); s++) {
						String string = expression.get(s);
						try {
							match = string.equals(tokens.get(t + s).getLemma());
						} catch (ArrayIndexOutOfBoundsException e) {
							break;
						}
						if (!match)
							break;
					}
					if (match)
						break;
				}
				// current token is negative example
				if (match) {
					((TextToken) token).setNoEntity(true);
				}
			}
		}
	}

	private void annotateModifiers(List<TextToken> tokens) {
		int skip = 0;
		for (int t = 0; t < tokens.size(); t++) {
			if (t + skip >= tokens.size())
				break;
			Token token = tokens.get(t + skip);
			String lemma = normalizeLemma(token.getLemma());
			// check if token is 
			if (modifiers.keySet().contains(lemma)) {
				int required = -1;
				boolean match = false;
				for (List<String> expression : modifiers.get(lemma)) {
					for (int s = 0; s < expression.size(); s++) {
						String string = expression.get(s);
						try {
							match = string.equals(tokens.get(t + s).getLemma());
						} catch (ArrayIndexOutOfBoundsException e) {
							break;
						}
						if (!match)
							break;
					}
					if (match) {
						if (expression.size() > required) {
							required = expression.size() - 1;
						}
					}
				}
				// token is start of modifier
				token.setModifier(true);
				((TextToken) token).setTokensToCompleteModifier(required);
				skip +=required;
			}
		}
	}

	/**
	 * normalizes the given string
	 * 
	 * @param lemma
	 *            string to normalize
	 * @return normalized string
	 */
	private String normalizeLemma(String lemma) {
		lemma = lemma.trim();
		if (lemma.equals("--")) {
			return lemma;
		}
		if (lemma.startsWith("<")) {
			return lemma;
		}
		if (lemma.length() <= 1) {
			return lemma;
		}
		while (true) {
			lemma = lemma.trim();
			if (lemma.length() == 0) {
				break;
			}
			Character s = lemma.charAt(0);
			if (s == '_') {
				lemma = lemma.substring(1);
				lemma = lemma.trim();
			}
			if (lemma.length() == 0) {
				break;
			}
			if (!Character.isLetter(s) && !Character.isDigit(s) && !(s == '§')) {
				lemma = lemma.substring(1);
				lemma = lemma.trim();
			}
			if (lemma.length() == 0) {
				break;
			}
			Character e = lemma.charAt(lemma.length() - 1);
			if (e == '_') {
				lemma = lemma.substring(0, lemma.length() - 1);
				lemma = lemma.trim();
			}
			if (lemma.length() == 0) {
				break;
			}
			if (!Character.isLetter(e) && !Character.isDigit(e) && !(e == '+') && !(e == '#')) {
				lemma = lemma.substring(0, lemma.length() - 1);
				lemma = lemma.trim();
			} else {
				break;
			}
		}
		return lemma.trim();
	}

	private boolean containsList(List<String> a, List<String> b) {
		if (a.size() < b.size()) {
			return false;
		}
		for (int i = 0; i <= a.size() - b.size(); i++) {
			boolean match = false;
			for (int j = 0; j < b.size(); j++) {
				match = a.get(i + j).equals(b.get(j));
				if (!match)
					break;
			}
			if (match) {
				return true;
			}
		}
		return false;
	}

	private boolean containsLetter(String string) {
		Pattern p = Pattern.compile("[A_Z]|[a-z]");
		Matcher m = p.matcher(string);
		return m.find();
	}

	private void readKnownEntitiesFromFile(File entitiesFile) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(entitiesFile));
		String line = in.readLine();
		while (line != null) {
			// create inform.-entity and add to posExample list
			String[] split = line.split(" ");
			String keyword;
			try {
				keyword = normalizeLemma(split[0]);

			} catch (ArrayIndexOutOfBoundsException e) {
				line = in.readLine();
				continue;
			}
			Set<InformationEntity> iesForKeyword = entities.get(keyword);
			if (iesForKeyword == null)
				iesForKeyword = new HashSet<InformationEntity>();
			InformationEntity ie = new InformationEntity(keyword, split.length == 1);
			if (!ie.isComplete()) {
				for (String string : split) {
					ie.addToExpression(normalizeLemma(string));

				}
			}
			boolean isnew = iesForKeyword.add(ie);
			if (isnew) {
				knownEntities++;
			}
			entities.put(keyword, iesForKeyword);
			line = in.readLine();
		}
		in.close();
	}

}
