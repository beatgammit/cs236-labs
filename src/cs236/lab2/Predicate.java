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
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A Predicate is the super class for the four Datalog types- Fact, Scheme, Rule, and Query.
 * A predicate consists of a name and a list of Parameters.
 * @author jameson
 */
public class Predicate extends ArrayList<Parameter> implements Comparable<Predicate>{
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

	/**
	 * Binds all variables with the given name to the given value.
	 * @param tName the name of the Parameter to bind
	 * @param tValue the value to bind to the Parameter
	 */
	public void bind(String tName, String tValue){
		if(tName != null){
			for(Parameter p : this){
				if(p.getName() != null && p.getName().equals(tName)){
					p.setValue(tValue);
				}
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
		boolean bReturn = false;
		for(Parameter p : this){
			if(p.getTokenType() == TokenType.IDENT){
				bReturn = true;
				break;
			}
		}
		return bReturn;
	}

	/**
	 * Checks to see if there are any parameters that are the same (using the equals method).
	 * This checks to make sure the reference isn't the same.
	 * @param tParam the Parameter to compare
	 * @return the index of the similar Parameter or -1 if nothing was found.
	 */
	public int findSimilar(Parameter tParam){
		int iReturn = -1;
		for(int i = 0; i < this.size(); i++){
			Parameter p = this.get(i);
			if(tParam != p && p.equals(tParam)){
				iReturn = i;
				break;
			}
		}
		return iReturn;
	}

	/**
	 * Returns a SortedSet of all of the free variables in this Predicate.
	 * @return a SortedSet of Parameters
	 */
	public SortedSet<Parameter> getSetOfUnboundParameters(){
		SortedSet<Parameter> tSet = new TreeSet<Parameter>();
		for(Parameter tParam : this){
			if(tParam.getTokenType() == TokenType.IDENT && tParam.getValue() == null){
				tSet.add(tParam.duplicate());
			}
		}
		return tSet;
	}

	/**
	 * Compares this Predicate to another Predicate.
	 *
	 * A Predicate is greater than another Predicate if:
	 * - the value of one is greater than another according to String's compareTo method
	 * - a Parameter of one is greater than its counterpart using Parameter's compareTo method
	 * - a Predicate has more parameters the other
	 * @param o the Predicate to compare this Predicate to
	 * @return 1 if this is greater, -1 if it is less than, or 0 if they are equal
	 */
	public int compareTo(Predicate o) {
		int iReturn = this.getValue().compareTo(o.getValue());
		// if the names are the same, we'll have to be more clever
		if(iReturn == 0){
			for(int i = 0; i < this.size() && i < o.size(); i++){
				Parameter pThis = this.get(i);
				Parameter pThat = o.get(i);
				if(pThis.compareTo(pThat) != 0){
					return pThis.compareTo(pThat);
				}
			}

			// if the Parameters are all the same, we still have to differentiate them
			if(this.size() == o.size()){
				iReturn = 0;
			}else{
				iReturn = this.size() > o.size() ? 1 : -1;
			}
		}
		return iReturn;
	}

	/**
	 * Overrides Object's equals method.
	 * This method returns true only if the parameter has the same value and has equal Parameters.
	 * @param obj the Object to compare this instance to (must be a Predicate instance)
	 * @return true if they are equal, false if not
	 */
	@Override
	public boolean equals(Object obj){
		boolean bReturn = false;
		if(obj instanceof Predicate){
			if(this.hashCode() == obj.hashCode()){
				bReturn = true;
			}
		}
		return bReturn;
	}

	/**
	 * Creates a hash of this Predicate.
	 * This really isn't used, but NetBeans likes it to be there if the equals is overridden.
	 * @return an integer hash
	 */
	@Override
	public int hashCode() {
		int hash = this.value.hashCode() * 2 + super.hashCode();
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
			if(!bFirst){
				sb.append(',');
			}
			sb.append(p.toString());
			bFirst = false;
		}
		sb.append(')');
		return sb.toString();
	}
}
