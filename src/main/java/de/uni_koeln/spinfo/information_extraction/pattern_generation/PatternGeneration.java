package de.uni_koeln.spinfo.information_extraction.pattern_generation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.uni_koeln.spinfo.information_extraction.data.ExtractionPattern;
import de.uni_koeln.spinfo.information_extraction.data.PatternToken;
import de.uni_koeln.spinfo.information_extraction.data.ExtractionUnit;
import de.uni_koeln.spinfo.information_extraction.data.InformationEntity;
import de.uni_koeln.spinfo.information_extraction.data.TextToken;

/**
 * A class for automatic generation of patterns
 * 
 * @author geduldia
 *
 */
public class PatternGeneration {

	/**
	 * @param stringMatchExtractions
	 *            Occurrences of known entities found by stringmatch
	 * @param left
	 *            left context-size
	 * @param right
	 *            right context-size
	 * @param rules
	 *            generalization-rules
	 * @return A list of auto-generated patterns
	 */
	public static List<ExtractionPattern> generatePatterns(
			Map<ExtractionUnit, Map<InformationEntity, List<ExtractionPattern>>> stringMatchExtractions, int left, int right,
			boolean[] rules) {

		List<ExtractionPattern> toReturn = new ArrayList<ExtractionPattern>();

		for (ExtractionUnit eUnit : stringMatchExtractions.keySet()) {
			if (eUnit.getTokenObjects().size() <= 4) {
				// sentence is to short to build pattern from
				continue;
			}

			for (InformationEntity ie : stringMatchExtractions.get(eUnit).keySet()) {
				// Create pattern for current context
				ExtractionPattern c = new ExtractionPattern();
				c.setDescription("auto-context: " + eUnit.getSentence() + "   (" + ie.getFullExpression() + ")");
				c.setId(-1);
				int skip = 0;
				List<Integer> extractionPointer = new ArrayList<Integer>();
				for (int t = 0; t < eUnit.getTokenObjects().size(); t++) {
					if (t + skip >= eUnit.getTokenObjects().size())
						break;
					TextToken tt = eUnit.getTokenObjects().get(t + skip);
					if (!extractionPointer.isEmpty()) {
						if (t + skip > extractionPointer.get(extractionPointer.size() - 1) + right)
							break;
					}
					PatternToken ct = new PatternToken(null, tt.getLemma(), tt.getPosTag());
					if (extractionPointer.contains(t + skip)) {
						ct.setLemma(null);
					}
					if (tt.getLemma().equals(ie.getToken())) {
						ct.setLemma(null);
						for (int i = 0; i < ie.getTokens().size(); i++) {
							extractionPointer.add(t + i);
						}
					}
					// apply generalization-rules
					if (rules[0]) {
						if (tt.isModifier()) {
							ct.setLemma("MODIFIER");
							ct.setPosTag(null);
							ct.setModifier(true);
							skip += tt.getTokensToCompleteModifier();
						}
					}
					if (rules[1]) {
						if (tt.isInformationEntity() && !(tt.getLemma().equals(ie.getToken()))) {
							ct.setLemma(null);
							ct.setPosTag(null);
							ct.setInformationEntity(true);
							skip += tt.getTokensToCompleteInformationEntity();
						}
					}
					if (rules[2]) {
						if (isStopWord(tt)) {
							ct.setLemma(null);
						}
					}
					if (rules[3]) {
						if (ct.getPosTag() != null) {
							if (ct.getPosTag().equals("<root-POS>") || ct.getPosTag().equals("XY")) {
								ct.setPosTag("<root-POS>|XY|$,|KON");

							}
							if (ct.getPosTag().equals("<end-POS>") || ct.getPosTag().equals("$.")) {
								ct.setPosTag("<end-POS>|$.|$,|KON");

							} else if (ct.getPosTag().equals("$,") || ct.getPosTag().equals("KON")) {
								ct.setPosTag("$,|KON");
							}
						}
					}
					c.addToken(ct);

				}

				if (extractionPointer.isEmpty())
					break;
				if (c.getTokens().isEmpty())
					break;
				// set left context-size
				if (left > -1) {
					int leftIndex = extractionPointer.get(0) - left;
					int firstEUIndex = extractionPointer.get(0);
					int euSize = extractionPointer.size();
					for (int i = 0; i < leftIndex; i++) {
						c.getTokens().remove(0);
						firstEUIndex--;
					}
					extractionPointer = new ArrayList<Integer>();
					for (int k = 0; k < euSize; k++) {
						extractionPointer.add(firstEUIndex + k);
					}
				}
				c.setEUPointer(extractionPointer);
				toReturn.add(c);
			}
		}
		return toReturn;
	}

	private static boolean isStopWord(TextToken t) {
		if (!t.getPosTag().startsWith("V") && !(t.getPosTag().startsWith("N")) && !(t.getPosTag().startsWith("AD"))) {
			return true;
		}
		return false;
	}

}
