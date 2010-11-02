/*
 * QueryEvaluator.java
 * 
 * Copyright (c) 2010, T. Jameson Little.
 * 
 * This file is part of cs236.lab3.
 * 
 * QueryEvaluator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * QueryEvaluator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with cs236.lab3.  If not, see <http ://www.gnu.org/licenses/>.
 */

package cs236.lab3;

import cs236.lab1.TokenType;
import cs236.lab2.DatalogProgram;
import cs236.lab2.Fact;
import cs236.lab2.Parameter;
import cs236.lab2.Predicate;
import cs236.lab2.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A QueryEvaluator can be used to evaluate a single Query against the Facts.
 * QueryEvaluator is only separate from the DatalogProgram to ease multithreading.
 * @author T. Jameson Little
 */
public class QueryEvaluator implements Runnable{
	private DatalogProgram datalog;

	private Query query;
	private List<Predicate> solutions;

	private AtomicBoolean bFinished;

	/**
	 * This should never be used.  It is required for subclasses to be able to inherit.
	 */
	protected QueryEvaluator(){
		this.datalog = null;
		this.query = null;
		this.solutions = null;
		this.bFinished = null;
	}
	/**
	 * Creates a new QueryEvaluator object.  This will not change the Query or the DatalogProgram.
	 * 
	 * @param tQuery a reference to the Query that we are evaluating
	 * @param dp the DatalogProgram we are evaluating the Query against.
	 */
	public QueryEvaluator(Query tQuery, DatalogProgram dp) {
		this.query = tQuery;
		this.datalog = dp;

		this.bFinished = new AtomicBoolean(false);

		this.solutions = new ArrayList<Predicate>();
	}

	/**
	 * Returns the List of Predicates that successfully unified with the Facts.
	 * This is done through recursively modifying the parameters in the Query until a match is found.
	 * @param tQuery
	 * @return
	 */
	protected List<Predicate> evaluateQuery(Predicate tQuery){
		evaluateQuery(0, tQuery);
		return this.solutions;
	}
	/**
	 * Evaluates the Query by recursively modifying the Parameters.
	 * Please pass in a duplicate if you don't want me to modify your data.
	 * @param iPos the position in the list of Parameters of the Query
	 * @param tQuery a reference to a Query object we can manipulate
	 */
	private void evaluateQuery(int iPos, Predicate tQuery){
		if(iPos == query.size()){
			// add it to some kind of a list
			if(factExists(tQuery) || validateUsingRules(tQuery)){
				addSolution(tQuery.duplicate());
			}
		}else{
			Parameter tParam = tQuery.get(iPos);
			if(tParam.getTokenType() == TokenType.STRING){
				// nothing changed in our builder Query, so go to the next one
				evaluateQuery(iPos + 1, tQuery);
				return;
			}
			// have we processed this identifier already?
			int iTemplatePos = tQuery.findSimilar(tParam);
			if(iTemplatePos >= 0 && iTemplatePos < iPos){
				// check to see if we should mimic an earlier Identifier (eg we're the same)
				Parameter tQueryParam = tQuery.get(iTemplatePos);
				tParam.setValue(tQueryParam.getValue());
				evaluateQuery(iPos + 1, tQuery);
				tParam.setValue(null);
			}else{
				for(String s : this.getDomain()){
					Parameter tQueryParam = tQuery.get(iPos);
					tQueryParam.setValue(s);
					evaluateQuery(iPos + 1, tQuery);
				}
			}
		}
	}

	/**
	 * Checks the rules to see if we can validate this query using the rules.
	 * @param tQuery the Query to evaluate
	 * @return false (we don't evaluate rules in this class)
	 */
	public boolean validateUsingRules(Predicate tQuery){
		return false;
	}

	/**
	 * Adds a Predicate to the list of positive solutions to this Query.
	 * @param tPred
	 */
	protected void addSolution(Predicate tPred){
		this.solutions.add(tPred);
	}

	/**
	 * Checks a Predicate (Query) against the Facts to determine if we've found a match.
	 * @param tQuery the Predicate to check against the Facts
	 * @return true if there is a match, false otherwise
	 */
	public boolean factExists(Predicate tQuery){
		for(Fact tFact : this.getFactList()){
			if(tFact.getValue().equals(tQuery.getValue()) && tFact.size() == tQuery.size()){
				boolean tMatch = true;
				for(int i = 0; i < tFact.size(); i++){
					Parameter factParam = tFact.get(i);
					Parameter tParam = tQuery.get(i);
					if(!factParam.getValue().equals(tParam.getValue())){
						tMatch = false;
						break;
					}
				}
				if(tMatch){
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns whether we have finished evaluating this Query against the Facts.
	 * This is only useful if you are using multithreading.
	 * @return true if we're finished, false otherwise
	 */
	public boolean isFinished(){
		return this.bFinished.get();
	}

	/**
	 * Runs the evaluation of the Query.  When this finishes, isFinished will evaluate to true.
	 */
	public void run() {
		this.evaluateQuery(0, this.query.duplicate());
		this.bFinished.set(true);
	}

	/**
	 * Prints a Predicate's solution set.
	 * A solution set consists of every unique identifier that has a value in left-right order.
	 *
	 * @param tPredicate a Predicate who has been used as a solution set
	 * @return a formatted String of solutions
	 */
	private String printSolutionSet(Predicate tPredicate){
		StringBuilder sb = new StringBuilder();
		List<String> tProcessed = new ArrayList<String>();
		for(Parameter tParam : tPredicate){
			// we got an identifier that was given a value
			if(tParam.getName() != null && tParam.getValue() != null){
				// if it's not empty, then we need to add a comma before adding the next one
				if(!tProcessed.isEmpty() && !tProcessed.contains(tParam.getName())){
					sb.append(", ");
				}
				if(!tProcessed.contains(tParam.getName())){
					tProcessed.add(tParam.getName());
					sb.append(String.format("%s='%s'", tParam.getName(), tParam.getValue()));
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Gets the DatalogProgram that was submitted using the constructor.
	 * @return the DatalogProgram or null if none was set
	 */
	protected DatalogProgram getDatalogProgram(){
		return this.datalog;
	}

	/**
	 * Gets the list of Facts from the DatalogProgram submitted using the constructor.
	 * This is a convenience method for getDatalogProgram().getFactList().
	 * @return a List of Facts
	 */
	protected List<Fact> getFactList(){
		return this.datalog.getFactList();
	}

	/**
	 * Gets the domain from the DatalogProgram submitted using the constructor.
	 * This is a convenience method for getDatalogProgram().getDomain()
	 * @return a SortedSet with the values in the domain
	 */
	protected SortedSet<String> getDomain(){
		return this.datalog.getDomain();
	}

	/**
	 * Formats the results of the evaluation according to Lab3 specifications.
	 * @return a formatted String representing the results of the evaluation
	 */
	@Override
	public String toString(){
		final String NEWLINE = System.getProperty("line.separator");

		StringBuilder sb = new StringBuilder();
		sb.append(this.query.toString()).append("? ");
		if(this.solutions.size() > 0){
			sb.append(String.format("Yes(%d)", this.solutions.size())).append(NEWLINE);
			for(Predicate tPred : this.solutions){
				// just in case the query had no identifiers...
				if(tPred.hasIdentifiers()){
					sb.append("  ").append(this.printSolutionSet(tPred)).append(NEWLINE);
				}
			}
		}else{
			sb.append("No").append(NEWLINE);
		}
		return sb.toString();
	}
}
