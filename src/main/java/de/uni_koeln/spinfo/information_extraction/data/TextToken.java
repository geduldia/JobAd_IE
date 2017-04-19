package de.uni_koeln.spinfo.information_extraction.data;

/**
 * @author geduldia
 * 
 * Subclass of Token (consist of String, Lemma and PosTag)
 * Represents a single Token of an ExtractionUnit (~ Sentence).
 * A TextToken can be marked as (start of) a known InformationEntity or modifier-expression.
 *
 */
public class TextToken extends Token {
	
	/**
	 * specifies the number of tokens to complete the InformationEntity (if
	 * token is start of InformationEntity)
	 */
	private int tokensToCompleteInformationEntity = 0;
	/**
	 * specifies the number of tokens to complete the modifier-expression (if token
	 * is start of modifier-expression)
	 */
	private int tokensToCompleteModifier = 0;
	
	/**
	 * token is a known mistake (only used in BIBB-Applications)
	 */
	private boolean noEntity;
	
	

	/**
	 * @param string
	 * @param lemma
	 * @param posTag
	 */
	public TextToken(String string, String lemma, String posTag) {
		super(string, lemma, posTag);
	}

	/**
	 * @return number of tokens to complete the InformationEntity (if token is start of one)
	 */
	public int getTokensToCompleteInformationEntity() {
		return tokensToCompleteInformationEntity;
	}

	
	/**
	 * @param tokensToCompleteInformationEntity
	 */
	public void setTokensToCompleteInformationEntity(int tokensToCompleteInformationEntity) {
		this.tokensToCompleteInformationEntity = tokensToCompleteInformationEntity;
	}

	/**
	 * @return number of tokens to complete modifier-expression (if token is start of one)
	 */
	public int getTokensToCompleteModifier() {
		return tokensToCompleteModifier;
	}

	/**
	 * @param tokensToCompleteModifier
	 */
	public void setTokensToCompleteModifier(int tokensToCompleteModifier) {
		this.tokensToCompleteModifier = tokensToCompleteModifier;
	}

	/**
	 * @return returns true if token is a known typical mistake
	 */
	public boolean isNoEntity() {
		return noEntity;
	}

	/**
	 * @param isNoEntity
	 */
	public void setNoEntity(boolean isNoEntity) {
		this.noEntity = isNoEntity;
	}

	/**
	 * compares this TextToken with the given PatternToken
	 * @param contextToken
	 * @return returns true, if this TextToken matches the given ContextToken
	 */
	public boolean isEqualsPatternToken(PatternToken contextToken) {
		// compare strings
		if (contextToken.getString() != null) {
			String[] strings = contextToken.getString().split("\\|");
			boolean match = false;
			for (String string : strings) {
				match = string.equals(this.string);
				if (match)
					break;
			}
			if (!match)
				return false;
		}
		// compare posTags
		if (contextToken.getPosTag() != null) {
			String[] tags = contextToken.getPosTag().split("\\|");
			if (tags[0].startsWith("-")) {
				for (String tag : tags) {
					tag = tag.substring(1);
					if (tag.equals(this.posTag)) {
						return false;
					}
				}
			} else {
				boolean match = false;
				for (String tag : tags) {
					if (tag.startsWith("-")) {
						match = !(tag.equals(this.posTag));
					} else {
						match = tag.equals(this.posTag);
					}
					if (match)
						break;
				}
				if (!match) {
					return false;
				}
			}

		}
		// compare lemmata
		if (contextToken.getLemma() != null) {
			if (contextToken.getLemma().toUpperCase().equals("IMPORTANCE")) {
				return this.modifierTerm;
			}
			else {
				String[] lemmas = contextToken.getLemma().split("\\|");
				boolean match = false;
				for (String lemma : lemmas) {
					if(lemma.startsWith("--")){
						match = this.getLemma().endsWith(lemma.substring(2));
					}
					else if (lemma.startsWith("-")) {
						match = this.getLemma().endsWith(lemma.substring(1));
						if (match) {
							match = !this.getLemma().startsWith(lemma.substring(1));
						}
					} else {
						match = this.lemma.equals(lemma);
					}

					if (match)
						break;
				}
				if (!match) {
					return false;
				}
			}
		}
		if (contextToken.isInformationEntity()) {
			return this.informationEntity;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(string + "\t" + lemma + "\t" + posTag + "\t");
		if (this.informationEntity) {
			sb.append("isInformsationEntitiy" + "\t");
		}
		if (this.noEntity) {
			sb.append("isNoEntity" + "\t");
		}
		if (this.modifierTerm) {
			sb.append("is (start of) modifier" + "\t");
		}
		return sb.toString();
	}

}
