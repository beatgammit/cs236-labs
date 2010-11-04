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
	 * Overrides Object's equals method.
	 * This method returns true only if the parameter has the same value and has equal Parameters.
	 * @param obj the Object to compare this instance to (must be a Predicate instance)
	 * @return true if they are equal, false if not
	 */
	@Override
	public boolean equals(Object obj){
		if(this.hashCode() == obj.hashCode()){
			return true;
		}
		return false;
	}

	/**
	 * Creates a hash of this Predicate.
	 * This really isn't used, but NetBeans likes it to be there if the equals is overridden.
	 * @return an integer hash
	 */
	@Override
	public int hashCode() {
		return this.value.hashCode() * 2 + super.hashCode();
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
