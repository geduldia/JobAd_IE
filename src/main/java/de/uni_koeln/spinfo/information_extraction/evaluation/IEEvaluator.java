package de.uni_koeln.spinfo.information_extraction.evaluation;

import java.util.List;
import java.util.Map;

import de.uni_koeln.spinfo.information_extraction.data.ExtractionPattern;
import de.uni_koeln.spinfo.information_extraction.data.ExtractionUnit;
import de.uni_koeln.spinfo.information_extraction.data.InformationEntity;

/**
 * @author geduldia
 * 
 *         A Class to evaluate IE-results
 *
 */
public class IEEvaluator {

	/**
	 * compares the extracted Information Entities with the annotated Entities
	 * and returns the EvaluationResult
	 * 
	 * @param allExtractions
	 * @param trainingdata
	 * @return EvaluationResult
	 */
	public EvaluationResult evaluateIEResults(
			Map<ExtractionUnit, Map<InformationEntity, List<ExtractionPattern>>> allExtractions,
			Map<ExtractionUnit, List<String>> trainingdata) {

		EvaluationResult result = new EvaluationResult();

		for (ExtractionUnit ieunit : trainingdata.keySet()) {
			List<String> anchors = trainingdata.get(ieunit);
			if (allExtractions.keySet().contains(ieunit)) {
				for (InformationEntity ie : allExtractions.get(ieunit).keySet()) {
					boolean tp = false;
					for (String anchor : anchors) {
						if (ie.getFullExpression().contains(anchor)) {
							tp = true;
							break;
						}
					}
					if (tp) {
						result.addTP(ie, ieunit);
					} else {
						result.addFP(ie, ieunit);
					}
				}
				for (String anchor : anchors) {
					boolean fn = true;
					for (InformationEntity ie : allExtractions.get(ieunit).keySet()) {
						if (ie.getFullExpression().contains(anchor)) {
							fn = false;
							break;
						}
					}
					if (fn) {
						result.addFN(anchor, ieunit);
					}
				}
			} else {
				for (String string : anchors) {
					result.addFN(string, ieunit);
				}
			}
		}
		return result;
	}

}
