/*
 * QueryEvaluatorWithRules.java
 * 
 * Copyright (c) 2010, T. Jameson Little.
 * 
 * This file is part of cs236.lab4.
 * 
 * QueryEvaluatorWithRules is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * QueryEvaluatorWithRules is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with cs236.lab4.  If not, see <http://www.gnu.org/licenses/>.
 */

package cs236.lab4;

import cs236.lab2.DatalogProgram;
import cs236.lab2.Parameter;
import cs236.lab2.Predicate;
import cs236.lab2.Query;
import cs236.lab2.Rule;
import cs236.lab3.QueryEvaluator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author T. Jameson Little
 */
public class QueryEvaluatorWithRules extends QueryEvaluator {
	// this way we don't have to evaluate the same rules over and over again
	private Set<Predicate> pendingPredicates = new HashSet<Predicate>();
	private Set<Predicate> truePredicates = new TreeSet<Predicate>();
	private Set<Predicate> falsePredicates = new TreeSet<Predicate>();
	/**
	 * Calls the super constructor in QueryEvaluator.
	 * @param tQuery the Query to be evaluated
	 * @param dp the DatalogProgram to evaluate the query against
	 */
	public QueryEvaluatorWithRules(Query tQuery, DatalogProgram dp){
		super(tQuery, dp);
	}
	/**
	 * Evaluates a query by using the rules.  This is over-ridden from QueryEvaluator.
	 * @param tQuery the Query to validate
	 * @return whether we a Rule exists that validates the Query
	 */
	@Override
	public boolean validateUsingRules(Predicate tQuery){
		boolean bReturn = false;
		for(Rule tRule : this.getRuleList()){
			// duplicate the Rule so we don't do anything stupid
			Rule tDuplicate = tRule.duplicate();
			// check to see if we actually need to process this predicate
			if(unify(tQuery, tDuplicate)){// this will assign values to all identifiers in the Rule
				tDuplicate.propagateBoundVariables();

				// now let's worry about the predicates
				Set<Parameter> allFree = tDuplicate.getSetOfUnboundParameters();
				if(recurse(0, new ArrayList<Parameter>(allFree), tDuplicate.getPredicateList())){
					bReturn = true;
					break;
				}
			}
		}
		return bReturn;
	}

	/**
	 * Recurses through the List of Parameters that represents all free variables in a Rule.
	 * The recursion will produce every possible solution set (set of parameters).
	 *
	 * This method could be slow, but we will stop recursing if we find an acceptable solution set.
	 * A solution set is acceptable if every Predicate in tSet can be evaluated to true.
	 * @param i our position in the List of free variables
	 * @param freeVars all possible free variables in tSet
	 * @param tSet all Predicates to evaluate as a group
	 * @return
	 */
	private boolean recurse(int i, List<Parameter> freeVars, List<Predicate> tSet){
		boolean bReturn = false;
		if(i == freeVars.size()){
			Set<Parameter> tSolSet = new HashSet<Parameter>();
			for(Parameter tParam : freeVars){
				tSolSet.add(tParam);
			}

			bReturn = true;
			for(Predicate p : tSet){
				if(!canResolve(tSolSet, p)){
					bReturn = false;
					break;
				}
			}
		}else{
			bReturn = false;
			for(String s : this.getDomain()){
				Parameter tQueryParam = freeVars.get(i);
				tQueryParam.setValue(s);

				if(recurse(i + 1, freeVars, tSet)){
					bReturn = true;
				}
			}
		}
		return bReturn;
	}

	/**
	 * Checks to see if a Predicate can resolve using a set of Parameters.
	 * @param tSolution the set of Parameters to bind to the Predicate
	 * @param tPredicate the Predicate to test
	 * @return true if it can resolve, false if not
	 */
	private boolean canResolve(Set<Parameter> tSolution, Predicate tPredicate){
		for(Parameter p : tSolution){
			tPredicate.bind(p.getName(), p.getValue());
		}

		boolean bReturn;
		// if we've already evaluated this Predicate and it's true, don't evaluate it again
		if(truePredicates.contains(tPredicate)){
			bReturn = true;
		}else if(falsePredicates.contains(tPredicate)){
			bReturn = false;
		}else{
			boolean bPass = false;

			// detect infinite recursion
			if(!pendingPredicates.contains(tPredicate)){
				// add the working predicate to the list of pending predicates
				pendingPredicates.add(tPredicate.duplicate());

				bPass = factExists(tPredicate) || validateUsingRules(tPredicate);

				// remove it so we don't get screwed up down the line
				pendingPredicates.remove(tPredicate);
			}
			
			if(bPass){
				truePredicates.add(tPredicate.duplicate());
			}else{
				falsePredicates.add(tPredicate.duplicate());
			}

			bReturn = bPass;
		}
		return bReturn;
	}

	/**
	 * Unifies a Predicate with a Rule.  If it fails, then it will return false.
	 *
	 * To successfully unify a Predicate with a Rule, the following conditions must be met:
	 * - the name of both Predicates must be equal
	 * - the number of Parameters must be equal
	 * - each Parameter in the Query must be able to unify with each Parameter in the Rule's head
	 *
	 * A Parameter from the Query can unify with the Parameter with the Rule if:
	 * - their values are equal (this uses the bound value) OR
	 * - the Parameter in the Rule is an Identifier, in which case it will be bound
	 *
	 * This method will assume that bound variables in the Rule are there on purpose.
	 * If you don't know what this means, then pass in a deep copy of an unmodified Rule every time.
	 * @param tQuery the Query to unify (won't be modified)
	 * @param tRule the Rule to unify (will be modified)
	 * @return true if successful, false if unsuccessful
	 */
	private boolean unify(Predicate tQuery, Rule tRule){
		// simple unify test; if this fails, we won't bother checking the Parameters
		boolean bReturn = tQuery.size() == tRule.size() && tQuery.getValue().equals(tRule.getValue());
		for(int i = 0; bReturn && i < tQuery.size(); i++){
			Parameter tRuleParam = tRule.get(i);
			Parameter tQueryParam = tQuery.get(i);

			// if the Parameter has a value, compare it to the Query's Parameter, otherwise bind it
			if(tRuleParam.getValue() != null){
				bReturn = tRuleParam.getValue().equals(tQueryParam.getValue());
			}else if(tRuleParam.getName() != null){
				tRule.bind(tRuleParam.getName(), tQueryParam.getValue());
			}
		}
		return bReturn;
	}

	/**
	 * Gets the List of Rules from the DatalogProgram submitted using the constructor.
	 * This is a convenience method for getDatalogProgram().getRuleList().
	 * @return a List of Rules
	 */
	protected List<Rule> getRuleList(){
		return this.getDatalogProgram().getRuleList();
	}
}
