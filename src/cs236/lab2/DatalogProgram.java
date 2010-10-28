/*
 * DatalogProgram.java
 *
 * Copyright (c) 2010, T. Jameson Little.
 *
 * This file is part of cs236.lab2.
 *
 * DatalogProgram is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DatalogProgram is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with cs236.lab2.  If not, see <http ://www.gnu.org/licenses/>.
 */

package cs236.lab2;

import cs236.lab1.Token;
import cs236.lab1.TokenType;
import cs236.lab3.QueryEvaluator;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Converts Tokens created by a Tokenizer into Datalog structures.
 * This can be run as a separate Thread to optimize for speed.
 * @author jameson
 */
public class DatalogProgram implements Runnable {
	private final long WAIT_TIME = 10; // keep this pretty small, we have fast processors

	private List<Scheme> schemes;
	private List<Fact> facts;
	private List<Rule> rules;
	private List<Query> queries;

	private SortedSet<String> domain;

	private Queue<Token> tTokenQueue;
	private Token offendingToken;
	private TokenizerServer tokenizerServer;

	/**
	 * Creates a new instance and initializes all important variables.
	 */
	public DatalogProgram(){
		this.schemes = new ArrayList<Scheme>();
		this.facts = new ArrayList<Fact>();
		this.rules = new ArrayList<Rule>();
		this.queries = new ArrayList<Query>();

		// we'll just leave this null until we actually need it
		this.domain = null;

		this.tTokenQueue = new ConcurrentLinkedQueue<Token>();
		this.offendingToken = null;
		this.tokenizerServer = null;
	}

	/**
	 * Set's the TokenizerServer so it can notify it when it's encountered an exception.
	 * @param ts the TokenizerServer that is adding tokens to this DatalogProgram
	 */
	public void setTokenizerServer(TokenizerServer ts){
		this.tokenizerServer = ts;
	}

	/**
	 * Gets the list of Fact objects associated with this DatalogProgram.
	 * This returns the reference to the List, not a copy.
	 * @return a reference to a List<Fact>
	 */
	public List<Fact> getFactList(){
		return this.facts;
	}

	/**
	 * Gets the list of Rule objects associated with this DatalogProgram.
	 * This returns the reference to the List, not a copy.
	 * @return a reference to a List<Rule>
	 */
	public List<Rule> getRuleList(){
		return this.rules;
	}

	public List<Query> getQueryList(){
		return this.queries;
	}

	/**
	 * Adds a Token to the Queue.  The Queue is Thread-safe, so there's no problem.
	 * @param tToken
	 */
	public void addToQueue(Token tToken){
		this.tTokenQueue.add(tToken);
	}

	/**
	 * Starts this thread.  This method represents <Datalog Program> in the grammar
	 */
	@Override
	public void run() {
		this.processDatalog();
		if(this.tokenizerServer != null)
			this.tokenizerServer.stopParsing();
	}

	private void throwError(Token tToken){
		this.offendingToken = tToken;
		throw new IllegalStateException();
	}

	/**
	 * Running this requires the queue to have tokens.
	 * It will wait until the queue has tokens, which could be a long time.
	 */
	public void processDatalog(){
		try{
			this.processSchemes();
			this.processFacts();
			this.processRules();
			this.processQueries();

			Token tToken = getNextToken();
			if(tToken.getTokenType() != TokenType.EOF){
				this.throwError(tToken);
			}
		}catch(IllegalStateException ex){
			// This error serves little more purpose than to stop parsing in case of an error.
		}
	}

	/**
	 * We need at least one Scheme, so we'll expect to get one or throw an error
	 */
	private void processSchemes(){
		Token tToken = this.getNextToken();
		if(tToken.getTokenType() == TokenType.SCHEMES){
			tToken = this.getNextToken();
			if(tToken.getTokenType() == TokenType.COLON){
				// we need at least one scheme
				processScheme();

				// we need at least one Scheme, but that doesn't mean that we can't accept more!
				while(this.peekNextToken().getTokenType() == TokenType.IDENT){
					processScheme();
				}
			}else{
				this.throwError(tToken);
			}
		}else{
			this.throwError(tToken);
		}
	}

	private void processScheme(){
		Predicate tPredicate = processPredicate();
		Scheme tScheme = new Scheme(tPredicate.getValue(), tPredicate);
		this.schemes.add(tScheme);
	}

	/**
	 * Gets the Facts.  There could be zero facts, or there could be tons, we don't know.
	 */
	private void processFacts(){
		Token tToken = this.getNextToken();
		if(tToken.getTokenType() == TokenType.FACTS){
			tToken = this.getNextToken();
			if(tToken.getTokenType() == TokenType.COLON){
				// we don't care how many Facts we get
				while(this.peekNextToken().getTokenType() == TokenType.IDENT){
					processFact();
				}
			}else{
				this.throwError(tToken);
			}
		}else{
			this.throwError(tToken);
		}
	}

	private void processFact(){
		Predicate tPredicate = processPredicate();

		// Facts end in a period, so we will throw an exception if it doesn't
		Token tToken = this.getNextToken();
		if(tToken.getTokenType() == TokenType.PERIOD){
			Fact tFact = new Fact(tPredicate.getValue(), tPredicate);
			this.facts.add(tFact);
		}else{
			this.throwError(tToken);
		}
	}

	private void processRules(){
		Token tToken = this.getNextToken();
		if(tToken.getTokenType() == TokenType.RULES){
			tToken = this.getNextToken();
			if(tToken.getTokenType() == TokenType.COLON){
				// we don't care how many Rules we get
				while(this.peekNextToken().getTokenType() == TokenType.IDENT){
					processRule();
				}
			}else{
				this.throwError(tToken);
			}
		}else{
			this.throwError(tToken);
		}
	}

	private void processRule(){
		Predicate tPredicate = processPredicate();

		Rule tRule = new Rule(tPredicate.getValue(), tPredicate);

		// Make sure we have a colon-dash, again, kind of a checkstyle hack
		Token tToken = this.getNextToken();
		if(tToken.getTokenType() != TokenType.COLON_DASH){
			this.throwError(tToken);
		}
		processPredicateList(tRule);

		// Rules end in a period, so we will throw an exception if it doesn't
		tToken = this.getNextToken();
		if(tToken.getTokenType() == TokenType.PERIOD){
			this.rules.add(tRule);
		}else{
			this.throwError(tToken);
		}
	}

	private void processQueries(){
		Token tToken = this.getNextToken();
		if(tToken.getTokenType() == TokenType.QUERIES){
			tToken = this.getNextToken();
			if(tToken.getTokenType() == TokenType.COLON){
				processQuery();

				while(this.peekNextToken().getTokenType() == TokenType.IDENT){
					processQuery();
				}
			}else{
				this.throwError(tToken);
			}
		}else{
			this.throwError(tToken);
		}
	}

	private void processQuery(){
		Predicate tPredicate = processPredicate();
		Query tQuery = new Query(tPredicate.getValue(), tPredicate);

		// Queries end in a question mark, so we will throw an exception if it doesn't
		Token tToken = this.getNextToken();
		if(tToken.getTokenType() == TokenType.Q_MARK){
			// we passed all of the checks on this Query, let's add it to the list
			this.queries.add(tQuery);
		}else{
			this.throwError(tToken);
		}
	}

	private void processPredicateList(Rule tRule){
		tRule.addPredicate(processPredicate());
		while(peekNextToken().getTokenType() == TokenType.COMMA){
			this.getNextToken(); // burn one so we're on track for the next predicate
			tRule.addPredicate(processPredicate());
		}
	}

	private Predicate processPredicate(){
		Token tToken = this.getNextToken();
		if(tToken.getTokenType() == TokenType.IDENT){
			Predicate tPredicate = new Predicate(tToken.getValue());
			tToken = this.getNextToken();

			boolean bReturnMe = false;
			if(tToken.getTokenType() == TokenType.LEFT_PAREN){
				processParamList(tPredicate);
				tToken = this.getNextToken();

				// ugly checkstyle hack... why can't I just get 1 more bloody if??!?
				bReturnMe = tToken.getTokenType() == TokenType.RIGHT_PAREN;
			}
			if(bReturnMe){
				return tPredicate;
			}

		}
		this.throwError(tToken);
		return null;
	}

	private void processParamList(List<Parameter> targetList){
		Token tToken = this.getNextToken();
		if(tToken.getTokenType() == TokenType.STRING || tToken.getTokenType() == TokenType.IDENT){
			targetList.add(new Parameter(tToken.getValue(), tToken.getTokenType()));
			if(this.peekNextToken().getTokenType() == TokenType.COMMA){
				// pop off the comma and let's get going!!
				tToken = this.getNextToken();

				processParamList(targetList);
			}
		}else{
			this.throwError(tToken);
		}
	}

	private synchronized Token getNextToken(){
		try {
			while(this.tTokenQueue.isEmpty()){
				// wait for a notify()
				Thread.sleep(WAIT_TIME);
			}
			return this.tTokenQueue.poll();
		} catch (InterruptedException ex) {
			Logger.getLogger(DatalogProgram.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}

	private synchronized Token peekNextToken(){
		try {
			while(this.tTokenQueue.isEmpty()){
				// wait for a notify()
				this.wait(WAIT_TIME);
			}
			return this.tTokenQueue.peek();
		} catch (InterruptedException ex) {
			Logger.getLogger(DatalogProgram.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}

	/**
	 * Returns a formatted String representing all of the data in this DatalogProgram.
	 * @return the formatted String
	 */
	@Override
	public String toString(){
		final String NEWLINE = System.getProperty("line.separator");

		StringBuilder sb = new StringBuilder();
		if(this.offendingToken == null){
			sb.append("Success!").append(NEWLINE);

			sb.append(String.format("Schemes(%d):", this.schemes.size())).append(NEWLINE);
			for(Scheme tScheme : this.schemes){
				sb.append("  ").append(tScheme.toString()).append(NEWLINE);
			}

			sb.append(String.format("Facts(%d):", this.facts.size())).append(NEWLINE);
			for(Fact tFact : this.facts){
				sb.append("  ").append(tFact.toString()).append(NEWLINE);
			}

			sb.append(String.format("Rules(%d):", this.rules.size())).append(NEWLINE);
			for(Rule tRule : this.rules){
				sb.append("  ").append(tRule.toString()).append(NEWLINE);
			}

			sb.append(String.format("Queries(%d):", this.queries.size())).append(NEWLINE);
			for(Query tQuery : this.queries){
				sb.append("  ").append(tQuery.toString()).append(NEWLINE);
			}

			sb.append(String.format("Domain(%d):", this.getDomain().size())).append(NEWLINE);
			for(String s : this.getDomain()){
				sb.append("  ").append('\'').append(s).append('\'').append(NEWLINE);
			}
		}else{
			sb.append("Failure!").append(NEWLINE);
			sb.append("  ").append(this.offendingToken.toString()).append(NEWLINE);
		}
		return sb.toString();
	}

	/**
	 * Gets the domain of this DatalogProgram (every String parameter in Rules, Queries, and Facts).
	 * This does not return duplicates and returns them in alphabetical order.
	 * @return a sorted list of the Domain (no quotes)
	 */
	public SortedSet<String> getDomain(){
		if(this.domain == null){
			this.domain = new TreeSet<String>();
			for(Fact tFact : this.facts){
				fillDomain(tFact, this.domain);
			}

			for(Rule tRule : this.rules){
				fillDomain(tRule, this.domain);
			}

			for(Query tQuery : this.queries){
				fillDomain(tQuery, this.domain);
			}
		}
		return this.domain;
	}

	private void fillDomain(Predicate tPredicate, SortedSet<String> tDomain){
		for(Parameter p : tPredicate){
			if(p.getTokenType() == TokenType.STRING){
				tDomain.add(p.getValue());
			}
		}
	}
}
