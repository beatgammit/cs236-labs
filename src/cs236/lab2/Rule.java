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

import cs236.lab1.TokenType;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A Rule is a Predicate with a list of Predicates.
 * A Rule is used to evaluate a Query when there are no Facts that directly satisfy a Query.
 * @author jameson
 */
public class Rule extends Predicate implements Comparable<Predicate>{
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
	public SortedSet<Parameter> getSetOfUnboundParameters(){
		SortedSet<Parameter> tSet = new TreeSet<Parameter>();
		for(Predicate p : this.getPredicateList()){
			for(Parameter tParam : p){
				if(tParam.getTokenType() == TokenType.IDENT && tParam.getValue() == null){
					tSet.add(tParam.duplicate());
				}
			}
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
		if(obj instanceof Rule && super.equals(obj))
		{
			Rule tRule = (Rule)obj;
			if(tRule.predicateList.size() != this.predicateList.size()){
				return false;
			}
			
			for(int i = 0; i < this.predicateList.size(); i++){
				if(!tRule.predicateList.get(i).equals(this.predicateList.get(i))){
					return false;
				}
			}
		}
		return false;
	}

	/**
	 * Override's Object's hashCode method.
	 * This really isn't used but NetBeans complains if it is not overridden and equals is.
	 * @return
	 */
	@Override
	public int hashCode() {
		int hash = 5;
		hash = 59 * hash + (this.predicateList != null ? this.predicateList.hashCode() : 0);
		return hash;
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
			if(!bFirst)
				sb.append(',');
			sb.append(p.toString());
			bFirst = false;

		}
		return sb.toString();
	}

	/**
	 * Override's Predicate's compareTo method unless the parameter passed really is a Predicate.
	 * This allows both Rule and Predicate to have unique compareTo methods.
	 * This method just calls compareTo(Rule) if the Predicate passed is a Rule reference.
	 * @param o
	 * @return
	 */
	@Override
	public int compareTo(Predicate o){
		if(o instanceof Rule){
			return compareTo((Rule)o);
		}
		return super.compareTo(o);
	}

	/**
	 * Compares this Rule instance to another Rule instance.
	 *
	 * A Rule is greater than another Rule if:
	 * - it is greater than the other according to Predicate's compareTo method
	 * - a Predicates is greater than it's corresponding Predicate according to Predicate's compareTo
	 * @param o the Rule to compare against this instance
	 * @return 1 if this instance is greater, -1 if it is lesser, and 0 if they are equal
	 */
	public int compareTo(Rule o) {
		if(super.compareTo(o) == 0){
			int i = 0;
			List<Predicate> pThisList = this.getPredicateList();
			List<Predicate> pThatList = o.getPredicateList();
			for(; i < pThisList.size() && i < pThatList.size(); i++){
				Predicate pThis = pThisList.get(i);
				Predicate pThat = pThatList.get(i);
				if(!pThis.equals(pThat)){
					return pThis.compareTo(pThat);
				}
			}
			if(pThisList.size() > i){
				return 1;
			}else if(pThatList.size() > i){
				return -1;
			}
			return 0;
		}else{
			return super.compareTo(o);
		}
	}
}
