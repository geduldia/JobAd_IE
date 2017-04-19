package de.uni_koeln.spinfo.information_extraction.data;

/**
 * @author geduldia
 * 
 * Abstract class for a single Token.
 * Consists of string, lemma, ad posTag.
 * A Token can be marked as (first token of) an InformationEntity (e.g. Competence) or
 * a modifier (e.g. 'wünschenswert' or 'zwingend erforderlich')
 * classes extending this class:
 *  - PatternToken (Part of an ExtractionPattern)
 *  - TextToken (Part of an ExtractionUnit)
 */

public abstract class Token {
	

	protected String posTag;
	protected String lemma;	
	protected String string;
	
	/**
	 * token is (start of) a known InformationEntity (e.g. competence or tool)
	 */
	protected boolean informationEntity;
	
	/**
	 * token is (start of) a modifier-expression (e.g. 'wünschenswert' or 'zwingend erforderlich')
	 */
	protected boolean modifierTerm;
	
	/**
	 * @param string
	 * @param lemma
	 * @param posTag
	 */
	public Token(String string, String lemma, String posTag) {
		this(string, lemma, posTag, false);
	}

	/**
	 * @param string
	 * @param lemma
	 * @param posTag
	 * @param isInformationEntity
	 */
	public Token(String string, String lemma, String posTag, boolean isInformationEntity) {
		this.posTag = posTag;
		this.string = string;
		this.lemma = lemma;
		if (string != null && string.toLowerCase().equals("pc")) {
			this.lemma = "pc";
		}
		this.informationEntity =isInformationEntity;
	}
	
	/**
	 * @return returns true if this token is (start of) a known InformationEntity
	 */
	public boolean isInformationEntity() {
		return informationEntity;
	}

	/**
	 * @param isInformationEntity
	 */
	public void setInformationEntity(boolean isInformationEntity) {
		this.informationEntity = isInformationEntity;
	}

	/**
	 * @return returns true if this token is a modifier
	 */
	public boolean isModifier() {
		return modifierTerm;
	}

	/**
	 * @param isModifier
	 */
	public void setModifier(boolean isModifier) {
		this.modifierTerm = isModifier;
	}

	/**
	 * @return posTag
	 */
	public String getPosTag() {
		return posTag;
	}

	/**
	 * @return lemma
	 */
	public String getLemma() {
		return lemma;
	}

	/**
	 * @return string
	 */
	public String getString() {
		return string;
	}

	
	

}
