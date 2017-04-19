package de.uni_koeln.spinfo.information_extraction.data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author geduldia
 * 
 *         Represents a Pattern to identify InformationEntities (e.g.
 *         competences or tools). Consist of several required Tokens and a list
 *         with Token-Indices which has to be extracted in case of match
 *
 */
public class ExtractionPattern {

	/**
	 * required tokens
	 */
	private List<PatternToken> tokens = new ArrayList<PatternToken>();
	/**
	 * indices of entity-tokens
	 */
	private List<Integer> extractionPointer = new ArrayList<Integer>();
	/**
	 * a short description of this pattern
	 */
	private String description;
	/**
	 * pattern-id
	 */
	private int id;

	/**
	 * @return pattern-id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * adds a new token to the pattern
	 * 
	 * @param token
	 *            toAdd
	 */
	public void addToken(PatternToken token) {
		tokens.add(token);
	}

	/**
	 * @return number of tokens this pattern consists of
	 */
	public int getSize() {
		return tokens.size();
	}

	/**
	 * returns the Token at the given index
	 * 
	 * @param index
	 * @return token at index
	 */
	public PatternToken getTokenAt(int index) {
		return tokens.get(index);
	}

	/**
	 * @return list of all tokens in this pattern
	 */
	public List<PatternToken> getTokens() {
		return tokens;
	}

	/**
	 * @return list of extractionPointers
	 */
	public List<Integer> getExtractionPointer() {
		return extractionPointer;
	}

	/**
	 * 
	 * @param extractionPointer
	 */
	public void setEUPointer(List<Integer> extractionPointer) {
		this.extractionPointer = extractionPointer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("ID:\t" + id + "\n");
		sb.append("DESCRIPTION:\t" + description + "\n");
		for (int t = 0; t < tokens.size(); t++) {
			Token token = tokens.get(t);
			sb.append("TOKEN:\t");
			sb.append(token.getString() + "\t");
			sb.append(token.getLemma() + "\t");
			sb.append(token.getPosTag() + "\t");
			sb.append(token.isInformationEntity() + "\n");
		}
		sb.append("EXTRACT:\t");
		for (Integer i : extractionPointer) {
			sb.append(i + ",");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append("\n");
		return sb.toString();
	}

	/**
	 * @return number of optional tokens
	 */
	public int getOptionalTokens() {
		int optionalTokens = 0;
		for (PatternToken token : tokens) {
			if (token.isOptional()) {
				optionalTokens++;
			}
		}
		return optionalTokens;
	}
}
