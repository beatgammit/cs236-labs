/*
 * Rule.java
 *
 * Copyright (c) 2010, T. Jameson Little.
 *
 * This file is part of cs236.lab2.
 *
 * Rule is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rule is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with cs236.lab2.  If not, see <http ://www.gnu.org/licenses/>.
 */

package cs236.lab2;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A Rule is a Predicate with a list of Predicates.
 * A Rule is used to evaluate a Query when there are no Facts that directly satisfy a Query.
 * @author jameson
 */
public class Rule extends Predicate {
	private List<Predicate> predicateList;
	/**
	 * Creates a new Rule with a name and a list of Parameters.
	 * @param tName the name of this Rule
	 * @param paramList the list of Parameters associated with this Rule
	 */
	public Rule(String tName, List<Parameter> paramList){
		super(tName);
		this.addAll(paramList);
		this.predicateList = new ArrayList<Predicate>();
	}

	/**
	 * Adds a Predicate to this Rule
	 * @param tPredicate
	 */
	public void addPredicate(Predicate tPredicate){
		this.predicateList.add(tPredicate);
	}

	/**
	 * Gets the List of Predicates that this Rule contains.
	 * @return a List of Predicates
	 */
	public List<Predicate> getPredicateList(){
		return this.predicateList;
	}

	/**
	 * Returns a SortedSet of all of the free variables in this Rule.
	 * @return a SortedSet of Parameters
	 */
	@Override
	public SortedSet<Parameter> getSetOfUnboundParameters(){
		SortedSet<Parameter> tSet = new TreeSet<Parameter>();
		// for lab4 this isn't ever necessary since they're all bound when this method is called
		//tSet.addAll(super.getSetOfUnboundParameters());
		for(Predicate p : this.getPredicateList()){
			tSet.addAll(p.getSetOfUnboundParameters());
	}
		return tSet;
	}

	/**
	 * This will propagate all of the bound variables to the other predicates in this Rule.
	 * This calls Predicate's bind method for each Parameter in this Rule.
	 */
	public void propagateBoundVariables(){
		for(Parameter p : this){
			for(Predicate tPred : this.predicateList){
				tPred.bind(p.getName(), p.getValue());
			}
		}
	}

	@Override
	public Rule duplicate(){
		Rule tRule = new Rule(new String(this.getValue()), new ArrayList<Parameter>());
		for(Parameter tParam : this){
			tRule.add(tParam.duplicate());
		}
		for(Predicate tPredicate : this.predicateList){
			tRule.addPredicate(tPredicate.duplicate());
		}
		return tRule;
	}

	/**
	 * Override's Object's equals method.
	 *
	 * A Rule is equal to another Rule if:
	 * - Predicate's equals method returns true
	 * - they have the same number of Parameters
	 * - each Parameter is equal to its counterpart through Parameter's equals method
	 * @param obj
	 * @return true if they are equal or false if they are not
	 */
	@Override
	public boolean equals(Object obj){
		boolean bReturn = obj instanceof Rule;
		// make sure we have a valid Rule instance and that the heads match
		if(bReturn && super.equals(obj))
		{
			Rule tRule = (Rule)obj;
			if(tRule.predicateList.size() == this.predicateList.size()){
				for(int i = 0; bReturn && i < this.predicateList.size(); i++){
					bReturn = tRule.predicateList.get(i).equals(this.predicateList.get(i));
				}
			}else{
				bReturn = false;
			}
		}
		return bReturn;
	}

	/**
	 * Checks to see if there are any parameters that have the same name.  If so, we return the value.
	 * @param name the name to compare
	 * @return the value of the matched Parameter or null if none found
	 */
	public String findValue(String name){
		String sReturn = null;
		for(int i = 0; i < this.size(); i++){
			Parameter p = this.get(i);
			if(p.getName() != null && p.getName().equals(name)){
				sReturn = p.getValue();
				break;
			}
		}
		return sReturn;
	}
	/**
	 * Override's Object's hashCode method.
	 * This really isn't used but NetBeans complains if it is not overridden and equals is.
	 * @return an integer hashCode
	 */
	@Override
	public int hashCode() {
		return super.hashCode();
	}

	/**
	 * Returns a formatted String with this pattern- Name(Param,Param...) :- Predicate,Predicate...
	 * @return a formatted String
	 */
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString()).append(" :- ");
		
		boolean bFirst = true; // this prevents adding a comma after the last predicate
		for(Predicate p : this.predicateList){
			if(!bFirst){
				sb.append(',');
			}
			sb.append(p.toString());
			bFirst = false;

		}
		return sb.toString();
	}
}
