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

import cs236.lab1.TokenType;
import cs236.lab2.DatalogProgram;
import cs236.lab2.Parameter;
import cs236.lab2.Query;
import cs236.lab2.Rule;
import cs236.lab3.QueryEvaluator;
import java.util.List;

/**
 *
 * @author T. Jameson Little
 */
public class QueryEvaluatorWithRules extends QueryEvaluator {
	public QueryEvaluatorWithRules(Query tQuery, DatalogProgram dp){
		super(tQuery, dp);
	}
	/**
	 * Evaluates a query by using the rules.  This is over-ridden from QueryEvaluator.
	 * @param tQuery the Query to validate
	 * @return whether we a Rule exists that validates the Query
	 */
	@Override
	public boolean validateUsingRules(Query tQuery){
		for(Rule tRule : this.getRuleList()){
			Rule tDuplicate = tRule.duplicate();
			if(unify(tQuery, tDuplicate)){
				return true;
			}
		}
		return false;
	}

	private boolean unify(Query tQuery, Rule tRule){
		if(tQuery.size() == tRule.size() && tRule.getValue().equals(tRule.getValue())){
			for(int i = 0; i < tQuery.size(); i++){
				Parameter tRuleParam = tRule.get(i);
				Parameter tQueryParam = tQuery.get(i);
				// if the Rule parameter is a variable, but the Query parameter is a constant, substitute
				if(tRuleParam.getTokenType() == TokenType.IDENT &&
						tQueryParam.getTokenType() == TokenType.STRING){
					tRuleParam.setValue(tQueryParam.getValue());
				}else if(tRuleParam.getTokenType() == TokenType.STRING &&
						tQueryParam.getTokenType() == TokenType.STRING &&
						tRuleParam.getValue().equals(tQueryParam.getValue())){
						continue;
				}else{
					return false;
				}
			}
			return true;
		}
		return false;
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
