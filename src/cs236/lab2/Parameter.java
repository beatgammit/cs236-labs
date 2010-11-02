/*
 * Parameter.java
 *
 * Copyright (c) 2010, T. Jameson Little.
 *
 * This file is part of cs236.lab2.
 *
 * Parameter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Parameter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with cs236.lab2.  If not, see <http ://www.gnu.org/licenses/>.
 */

package cs236.lab2;

import cs236.lab1.TokenType;

/**
 * A parameter can either be an Identifier or a String.
 * If it's an Identifier, this class can also be used to store temporary values in evaluation.
 * @author jameson
 */
public class Parameter implements Comparable<Parameter>{
	private String value;
	private String name;

	private Parameter(String sName, String sValue){
		this.name = sName;
		this.value = sValue;
	}

	/**
	 * Creates a new Parameter with a certain value (name) and a TokenType (either STRING or IDENT)
	 * @param sValue the value (name) of this parameter
	 * @param tTokenType this Parameter's TokenType
	 */
	public Parameter(String sValue, TokenType tTokenType){
		if(tTokenType == TokenType.STRING){
			this.value = sValue;
			this.name = null;
		}else{
			this.name = sValue;
			this.value = null;
		}
	}

	/**
	 * Gets the value of this Parameter.
	 * Should be used for STRING types. Otherwise it will return what was sent in with setValue().
	 * @return the value of this Parameter
	 */
	public String getValue(){
		return this.value;
	}

	/**
	 * Gets the name of this Parameter.  This will be null for a STRING type.
	 * @return the name if it exists or null otherwise
	 */
	public String getName(){
		return this.name;
	}

	/**
	 * Sets the value of this Parameter.
	 * Should only be used to hold a placeholder in evaluating Queries.
	 * @param sValue the value to assign to this Parameter.
	 */
	public void setValue(String sValue){
		this.value = sValue;
	}

	/**
	 * Gets the TokenType associated with this Parameter.
	 * @return TokenType.STRING or TokenType.IDENT
	 */
	public TokenType getTokenType(){
		if(this.getName() != null){
			return TokenType.IDENT;
		}else{
			return TokenType.STRING;
		}
	}

	/**
	 * Duplicates this Parameter.  Note that this is a deep duplicate, so we create a new instance.
	 * @return a duplicate of this Parameter
	 */
	public Parameter duplicate(){
		String tName = null;
		String tValue = null;
		if(this.getName() != null)
			tName = new String(this.getName());
		if(this.getValue() != null)
			tValue = new String(this.getValue());
		return new Parameter(tName, tValue);
	}

	/**
	 * Overrides Object's equals for Parameter objects.  It returns false if it's not a Parameter.
	 * 
	 * If this Parameter is a constant, then it will compare the values of both Parameters.
	 * If this Parameter is a variable, then it will compare the names of both Parameters.
	 * @param obj the Object to be compared
	 * @return true if they are the same value (not reference) or false otherwise
	 */
	@Override
	public boolean equals(Object obj){
		if(obj instanceof Parameter){
			Parameter tParam = (Parameter)obj;

			if(tParam.getTokenType() == TokenType.STRING){
				return tParam.getValue().equals(this.getValue());
			}
			else if(tParam.getTokenType() == TokenType.IDENT){
				return tParam.getName().equals(this.getName());
			}
		}
		return false;
	}

	/**
	 * Generate hash code for this object.  This uses the value and name fields to generate it.
	 * @return hash code as an int
	 */
	@Override
	public int hashCode() {
		final int SALT = 79;
		final int HASH_DEFAULT = 3;
		int hash = HASH_DEFAULT;
		hash = SALT * hash + (this.value != null ? this.value.hashCode() : 0);
		hash = SALT * hash + (this.name != null ? this.name.hashCode() : 0);
		return hash;
	}

	/**
	 * Formats this Parameter with this pattern.  It puts single quotes around Strings.
	 * @return a formatted String with this pattern- 'value' for String, value for identifier
	 */
	@Override
	public String toString(){
		if(this.getValue() != null)
			return String.format("'%s'", this.getValue());
		else
			return this.getName();
	}

	/**
	 * Compares two Parameter objects.
	 *
	 * If both Parameters have a value or both have only a name, String's compareTo method is used.
	 * If the Parameters don't have corresponding data, then the IDENT is preferred over the STRING.
	 * @param o the Parameter to compare to
	 * @return 1 if this is greater than the o, -1 if this is less than o
	 */
	public int compareTo(Parameter o) {
		if(this.getValue() != null && o.getValue() != null){
			return this.getValue().compareTo(o.getValue());
		}else if(this.getName() != null && o.getName() != null){
			return this.getName().compareTo(o.getName());
		}else{
			return this.getTokenType() == TokenType.STRING ? -1 : 1;
		}
	}
}
