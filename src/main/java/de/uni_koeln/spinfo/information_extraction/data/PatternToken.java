package de.uni_koeln.spinfo.information_extraction.data;

/**
 * @author geduldia
 * 
 * Subclass of Token
 * Represents a single Token of an ExtractionPattern.
 * The attributes string, lemma and posTag can be null, if values are not specified in pattern
 * A PatternToken can be marked as optional (= token is not required to match the pattern)
 *
 */
public class PatternToken extends Token {

	/**
	 * is true when PatternToken is optional in the containing pattern
	 */
	private boolean optional;

	/**
	 * @param string
	 * @param lemma
	 * @param posTag
	 */
	public PatternToken(String string, String lemma, String posTag) {
		super(string, lemma, posTag);
	}
	
	/**
	 * @param string
	 * @param lemma
	 * @param posTag
	 * @param isInformationEntity
	 */
	public PatternToken(String string, String lemma, String posTag, boolean isInformationEntity) {
		super(string, lemma, posTag, isInformationEntity);
	}
	
	/**
	 * @param lemma
	 */
	public void setLemma(String lemma){
		this.lemma = lemma;
	}
	
	/**
	 * @param posTag
	 */
	public void setPosTag(String posTag){
		this.posTag = posTag;
	}
	/**
	 * @param string
	 */
	public void setString(String string){
		this.string = string;
	}
	
	/* (non-Javadoc)
	 * @see de.uni_koeln.spinfo.information_extraction.data.Token#getLemma()
	 */
	public String getLemma(){
		if(isModifier()) return "IMPORTANCE";
		return this.lemma;
	}
	
	/**
	 * @return returns true if this token is optional
	 */
	/**
	 * @return isOptional
	 */
	public boolean isOptional() {
		return optional;
	}

	/**
	 * @param isOptional
	 */
	public void setOptional(boolean isOptional) {
		this.optional = isOptional;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(string + "\t" + lemma + "\t" + posTag + "\t");
		if(this.informationEntity){
			sb.append("isInformsationEntitiy"+"\t");
		}
		if(this.optional){
			sb.append("isNoEntity"+"\t");
		}
		if(this.modifierTerm){
			sb.append("is (start of) importanceTerm"+"\t");
		}
		return sb.toString();
	}
}
