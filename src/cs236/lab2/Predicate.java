/*
 * Predicate.java
 *
 * Copyright (c) 2010, T. Jameson Little.
 *
 * This file is part of cs236.lab2.
 *
 * Predicate is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Predicate is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with cs236.lab2.  If not, see <http ://www.gnu.org/licenses/>.
 */

package cs236.lab2;

import cs236.lab1.TokenType;
import java.util.ArrayList;

/**
 * A Predicate is the super class for the four Datalog types- Fact, Scheme, Rule, and Query.
 * A predicate consists of a name and a list of Parameters.
 * @author jameson
 */
public class Predicate extends ArrayList<Parameter>{
	private String value;

	/**
	 * Creates a new Predicate with a certain value.
	 * @param sValue the name of this Predicate
	 */
	public Predicate(String sValue){
		super();
		this.value = sValue;
	}

	/**
	 * Gets the value of this Predicate.
	 * @return the value as a String
	 */
	public String getValue(){
		return this.value;
	}

	public void bind(String tName, String tValue){
		for(Parameter p : this){
			if(p.getName().equals(tName)){
				p.setValue(tValue);
			}
		}
	}

	/**
	 * Duplicates this Predicate.  This is a deep duplication so it creates new objects.
	 * @return a copy of this Predicate
	 */
	public Predicate duplicate(){
		Predicate p = new Predicate(new String(this.getValue()));
		for(Parameter tParam : this){
			p.add(tParam.duplicate());
		}
		return p;
	}

	/**
	 * Gets the number of identifiers in this Predicate's list of Parameters.
	 * @return the number of Identifiers in this Predicate
	 */
	public boolean hasIdentifiers(){
		for(Parameter p : this){
			if(p.getTokenType() == TokenType.IDENT){
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks to see if there are any parameters that are the same (using the equals method).
	 * This checks to make sure the reference isn't the same.
	 * @param tParam the Parameter to compare
	 * @return the index of the similar Parameter or -1 if nothing was found.
	 */
	public int findSimilar(Parameter tParam){
		for(int i = 0; i < this.size(); i++){
			Parameter p = this.get(i);
			if(tParam != p && p.equals(tParam)){
				return i;
			}
		}
		return -1;
	}

	@Override
	public boolean equals(Object obj){
		if(obj instanceof Predicate){
			Predicate tPredicate = (Predicate)obj;
			if(!this.getValue().equals(tPredicate.getValue()) || this.size() != tPredicate.size()){
				return false;
			}
			
			for(int i = 0; i < tPredicate.size(); i++){
				if(!tPredicate.get(i).equals(this.get(i))){
					return false;
				}
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 83 * hash + (this.value != null ? this.value.hashCode() : 0);
		return hash;
	}

	/**
	 * Formats this Predicate according to Datalog's guidelines.
	 * @return a formatted Strin
	 */
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(this.getValue()).append('(');
		boolean bFirst = true;
		for(Parameter p : this){
			if(!bFirst)
				sb.append(',');
			sb.append(p.toString());
			bFirst = false;
		}
		sb.append(')');
		return sb.toString();
	}
}
