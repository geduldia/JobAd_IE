package de.uni_koeln.spinfo.information_extraction.preprocessing;

import java.io.IOException;
import java.util.List;

import de.uni_koeln.spinfo.information_extraction.data.ExtractionUnit;
import is2.data.SentenceData09;
import is2.io.CONLLWriter09;
import is2.lemmatizer.Lemmatizer;
import is2.tag.Tagger;

/**
 * @author geduldia
 * 
 * Annotates ExractionUnits with lexical data using MateTools
 *
 */
public class MateTagger {


	/**
	 * adds lexical data to the given ExtractionUnits (pos-tags, morph-tags, lemma)
	 * @param extractionUnits
	 * @throws IOException
	 */
	public static void setLexicalData(List<ExtractionUnit> extractionUnits) throws IOException {
		IETokenizer tokenizer = new IETokenizer();
		is2.tools.Tool lemmatizer = new Lemmatizer(
				"information_extraction/data/sentencedata_models/ger-tagger+lemmatizer+morphology+graph-based-3.6/lemma-ger-3.6.model");
		is2.mtag.Tagger morphTagger = null;
		is2.tools.Tool tagger = null;
		morphTagger = new is2.mtag.Tagger(
				"information_extraction/data/sentencedata_models/ger-tagger+lemmatizer+morphology+graph-based-3.6/morphology-ger-3.6.model");
		tagger = new Tagger(
				"information_extraction/data/sentencedata_models/ger-tagger+lemmatizer+morphology+graph-based-3.6/tag-ger-3.6.model");

		CONLLWriter09 writer = null;
		for (ExtractionUnit extractionUnit : extractionUnits) {
			SentenceData09 sd = new SentenceData09();
			sd.init(tokenizer.tokenizeSentence("<root> " + extractionUnit.getSentence()));
			lemmatizer.apply(sd);
			morphTagger.apply(sd);
			tagger.apply(sd);
			extractionUnit.setSentenceData(sd);

			if (writer != null) {
				writer.write(sd);
			}
		}
		lemmatizer = null;
		morphTagger = null;
		tagger = null;
	}

}
