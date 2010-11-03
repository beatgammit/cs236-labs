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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author T. Jameson Little
 */
public class QueryEvaluatorWithRules extends QueryEvaluator {
	// this way we don't have to evaluate the same rules over and over again
	private Set<Predicate> pendingPredicates = new HashSet<Predicate>();
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
		for(Rule tRule : this.getRuleList()){
			// duplicate the Rule so we don't do anything stupid
			Rule tDuplicate = tRule.duplicate();
			// check to see if we actually need to process this predicate
			if(unify(tQuery, tDuplicate)){// this will assign values to all identifiers in the Rule
				tDuplicate.propagateBoundVariables();

				// now let's worry about the predicates
				Set<Parameter> freeVariables = tDuplicate.getSetOfUnboundParameters();
				Parameter[] tParamArray = freeVariables.toArray(new Parameter[freeVariables.size()]);
				if(allResolve(0, tParamArray, tDuplicate.getPredicateList())){
					return true;
				}
			}
		}
		return false;
	}

	private boolean allResolve(int iPos, Parameter[] freeVars, List<Predicate> predList){
		if(iPos == freeVars.length){
			// add it to some kind of a list
			boolean bPass = true;
			for(Predicate predicate : predList){
				// bind everything
				for(Parameter p : freeVars){
					predicate.bind(p.getName(), p.getValue());
				}
				
				// detect infinite recursion
				if(pendingPredicates.contains(predicate)){
					bPass = false;
					// we want to keep this in the list until we're ready to remove it
					break;
				}

				// add the working predicate to the list of pending predicates
				pendingPredicates.add(predicate.duplicate());

				bPass = factExists(predicate) || validateUsingRules(predicate);
				
				// remove it so we don't get screwed up down the line
				pendingPredicates.remove(predicate);
				
				if(!bPass)
					return false;
			}
			return bPass;
		}else{
			for(String s : this.getDomain()){
				Parameter tQueryParam = freeVars[iPos];
				tQueryParam.setValue(s);
				if(allResolve(iPos + 1, freeVars, predList)){
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Unifies a Predicate with a Rule.  If it fails, then it will return false.
	 * @param tQuery the Query to unify (won't be modified)
	 * @param tRule the Rule to unify (will be modified)
	 * @return true if successful, false if unsuccessful
	 */
	private boolean unify(Predicate tQuery, Rule tRule){
		if(tQuery.size() != tRule.size() || !tQuery.getValue().equals(tRule.getValue())){
			return false;
		}
		for(int i = 0; i < tQuery.size(); i++){
			Parameter tRuleParam = tRule.get(i);
			Parameter tQueryParam = tQuery.get(i);

			// if we have an identifier, check to see if we have any other similar identifiers
			if(tRuleParam.getName() != null){
				String tValue = tRule.findValue(tRuleParam.getName());
				// if we have a similar Parameter, but the value doesn't match the Query param, discard
				if(tValue != null && !tValue.equals(tQueryParam.getValue())){
					return false;
				}
				tRuleParam.setValue(tQueryParam.getValue());
			}else{
				if(!tRuleParam.getValue().equals(tQueryParam.getValue()))
					return false;
			}
		}
		return true;
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
