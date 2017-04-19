package de.uni_koeln.spinfo.information_extraction.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author geduldia
 * 
 *         represents a single Entity (e.g. a tool or a competence) An Entity
 *         can consist of one ore more tokens/strings
 *
 */
public class InformationEntity {

	/**
	 * first token of the entity
	 */
	private String firstToken;
	/**
	 * is true if this InformationEntity consist of one token only
	 */
	private boolean complete;
	/**
	 * ordered list of all tokens/strings of this entity
	 */
	private List<String> tokens;

	/**
	 * modifier of this Entity (e.g. 'wünschenswert' or 'zwingend erforderlich')
	 */
	private String modifier;

	/**
	 * Constructor for InformationEntities
	 * 
	 * @param token
	 *            first token of this IE
	 * @param complete
	 *            InformationEntity consists of this token only
	 */
	public InformationEntity(String token, boolean complete) {
		this.firstToken = token;
		this.complete = complete;
		if (complete) {
			tokens = Arrays.asList(firstToken);
		}
	}

	/**
	 * @param modifier
	 */
	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	/**
	 * @return modifier
	 */
	public String getModifier() {
		return modifier;
	}

	/**
	 * @return full expression of this IE as one String
	 */
	public String getFullExpression() {
		if (tokens == null)
			return null;
		StringBuffer sb = new StringBuffer();
		for (String token : tokens) {
			sb.append(token + " ");
		}
		return sb.toString().trim();
	}

	/**
	 * @return firstToken
	 */
	public String getToken() {
		return firstToken;
	}

	/**
	 * @param token
	 *            first token
	 */
	public void setFirstToken(String token) {
		this.firstToken = token;
	}

	/**
	 * @return complete
	 */
	public boolean isComplete() {
		return complete;
	}

	/**
	 * @param isComplete
	 */
	public void setComplete(boolean isComplete) {
		this.complete = isComplete;
	}

	/**
	 * @return all tokens
	 */
	public List<String> getTokens() {
		return tokens;
	}

	/**
	 * 
	 * appends a new token to the expression
	 * 
	 * @param lemma
	 */
	public void addToExpression(String lemma) {
		if (tokens == null) {
			tokens = new ArrayList<String>();
		}
		tokens.add(lemma);
	}

	/**
	 * @param expression
	 *            full expression of this entity
	 */
	public void setExpression(List<String> expression) {
		this.tokens = expression;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder(3, 17).append(firstToken).append(complete).append(tokens)
				/* .append(importance) */.toHashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		InformationEntity am = (InformationEntity) o;
		return new EqualsBuilder().append(firstToken, am.firstToken).append(complete, am.complete)
				.append(tokens, am.tokens)
				./* append(importance, am.importance). */isEquals();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.getFullExpression();
	}

}
