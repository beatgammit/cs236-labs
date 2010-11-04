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
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
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
	private String errorString;
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
		this.errorString = null;
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
	 * @return a reference to a List of Rule objects
	 */
	public List<Rule> getRuleList(){
		return this.rules;
	}

	/**
	 * Gets the List of Query objects associated with this DatalogProgram.
	 * This returns the reference to the List, not a copy.
	 * @return a reference to a List of Query objects
	 */
	public List<Query> getQueryList(){
		return this.queries;
	}

	/**
	 * Gets the List of Scheme objects associated with this DatalogProgram.
	 * This returns the reference to the List, not a copy.
	 * @return a reference to a List of Scheme objects
	 */
	public List<Scheme> getSchemeList(){
		return this.schemes;
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
		if(this.tokenizerServer != null){
			this.tokenizerServer.stopParsing();
		}
	}

	private String getErrorMessage(Token tToken){
		return String.format("Failure!\n  %s\n", tToken.toString());
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
				throw new IllegalStateException(getErrorMessage(tToken));
			}
		}catch(IllegalStateException ex){
			// This error serves little more purpose than to stop parsing in case of an error.
			this.errorString = ex.getMessage();
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
				throw new IllegalStateException(getErrorMessage(tToken));
			}
		}else{
			throw new IllegalStateException(getErrorMessage(tToken));
		}
	}

	/**
	 * Parses a Scheme and adds it to the List.  A Scheme has this schema:
	 * <Predicate>
	 */
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
				throw new IllegalStateException(getErrorMessage(tToken));
			}
		}else{
			throw new IllegalStateException(getErrorMessage(tToken));
		}
	}

	/**
	 * Parses a Fact and adds it to the List.  A Fact follows this schema:
	 * <Predicate>.
	 */
	private void processFact(){
		Predicate tPredicate = processPredicate();

		// Facts end in a period, so we will throw an exception if it doesn't
		Token tToken = this.getNextToken();
		if(tToken.getTokenType() == TokenType.PERIOD){
			Fact tFact = new Fact(tPredicate.getValue(), tPredicate);
			this.facts.add(tFact);
		}else{
			throw new IllegalStateException(getErrorMessage(tToken));
		}
	}

	/**
	 * Gets the Rules.  There must be a Rules section, but there doesn't have to be any Rules.
	 */
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
				throw new IllegalStateException(getErrorMessage(tToken));
			}
		}else{
			throw new IllegalStateException(getErrorMessage(tToken));
		}
	}

	/**
	 * Parses a Rule and adds it to the List.  A Rule follows this schema:
	 * <Predicate>:-<PredicateList>.
	 */
	private void processRule(){
		Predicate tPredicate = processPredicate();

		Rule tRule = new Rule(tPredicate.getValue(), tPredicate);

		// Make sure we have a colon-dash, again, kind of a checkstyle hack
		Token tToken = this.getNextToken();
		if(tToken.getTokenType() != TokenType.COLON_DASH){
			throw new IllegalStateException(getErrorMessage(tToken));
		}
		processPredicateList(tRule);

		// Rules end in a period, so we will throw an exception if it doesn't
		tToken = this.getNextToken();
		if(tToken.getTokenType() == TokenType.PERIOD){
			this.rules.add(tRule);
		}else{
			throw new IllegalStateException(getErrorMessage(tToken));
		}
	}

	/**
	 * Gets the Queries.  There must be a Queries section and at least one Query.
	 */
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
				this.getErrorMessage(tToken);
			}
		}else{
			this.getErrorMessage(tToken);
		}
	}

	/**
	 * Parses a Query and adds it to the List.  A Query follows this schema:
	 * <Predicate>?
	 */
	private void processQuery(){
		Predicate tPredicate = processPredicate();
		Query tQuery = new Query(tPredicate.getValue(), tPredicate);

		// Queries end in a question mark, so we will throw an exception if it doesn't
		Token tToken = this.getNextToken();
		if(tToken.getTokenType() == TokenType.Q_MARK){
			// we passed all of the checks on this Query, let's add it to the list
			this.queries.add(tQuery);
		}else{
			this.getErrorMessage(tToken);
		}
	}

	/**
	 * Gets a List of Predicates and adds them to the Rule.  There must be at least one Predicate.
	 * @param tRule
	 */
	private void processPredicateList(Rule tRule){
		tRule.addPredicate(processPredicate());
		while(peekNextToken().getTokenType() == TokenType.COMMA){
			this.getNextToken(); // burn one so we're on track for the next predicate
			tRule.addPredicate(processPredicate());
		}
	}

	/**
	 * Parses and returns a Predicate.  A Predicate follows this schema:
	 * <Identifier>(<ParameterList>)
	 * @return the Predicate
	 */
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
		throw new IllegalStateException(getErrorMessage(tToken));
	}

	/**
	 * Gets the List of Parameters and adds them to the List.  There must be at least one Parameter.
	 * A Parameter must be either an Identifier or a String.
	 * @param targetList the List to add the Parameters to
	 */
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
			throw new IllegalStateException(getErrorMessage(tToken));
		}
	}

	/**
	 * Gets the next Token.  This will pop it from the Queue.  This method is Thread safe.
	 * @return the next Token
	 */
	private synchronized Token getNextToken(){
		Token tReturn = null;
		try {
			while(this.tTokenQueue.isEmpty()){
				// wait for a notify()
				Thread.sleep(WAIT_TIME);
			}
			tReturn = this.tTokenQueue.poll();
		} catch (InterruptedException ex) {
			Logger.getLogger(DatalogProgram.class.getName()).log(Level.SEVERE, null, ex);
		}
		return tReturn;
	}

	/**
	 * Peeks at the next token in the List.  This will not remove the Token from the List.
	 *
	 * This is synchronized, so it is Thread safe.
	 * @return the next Token
	 */
	private synchronized Token peekNextToken(){
		Token tReturn = null;
		try {
			while(this.tTokenQueue.isEmpty()){
				// wait for a notify()
				this.wait(WAIT_TIME);
			}
			tReturn = this.tTokenQueue.peek();
		} catch (InterruptedException ex) {
			Logger.getLogger(DatalogProgram.class.getName()).log(Level.SEVERE, null, ex);
		}
		return tReturn;
	}

	/**
	 * Returns a formatted String representing all of the data in this DatalogProgram.
	 * @return the formatted String
	 */
	@Override
	public String toString(){
		String sReturn = null;
		if(this.errorString != null){
			sReturn = this.errorString;
		}else{
			final String NEWLINE = System.getProperty("line.separator");

			StringBuilder sb = new StringBuilder();
			sb.append("Success!").append(NEWLINE);

			sb.append(String.format("Schemes(%d):", this.getSchemeList().size())).append(NEWLINE);
			for(Scheme tScheme : this.getSchemeList()){
				sb.append("  ").append(tScheme.toString()).append(NEWLINE);
			}

			sb.append(String.format("Facts(%d):", this.getFactList().size())).append(NEWLINE);
			for(Fact tFact : this.getFactList()){
				sb.append("  ").append(tFact.toString()).append(NEWLINE);
			}

			sb.append(String.format("Rules(%d):", this.getRuleList().size())).append(NEWLINE);
			for(Rule tRule : this.getRuleList()){
				sb.append("  ").append(tRule.toString()).append(NEWLINE);
			}

			sb.append(String.format("Queries(%d):", this.getQueryList().size())).append(NEWLINE);
			for(Query tQuery : this.getQueryList()){
				sb.append("  ").append(tQuery.toString()).append(NEWLINE);
			}

			sb.append(String.format("Domain(%d):", this.getDomain().size())).append(NEWLINE);
			for(String s : this.getDomain()){
				sb.append("  ").append('\'').append(s).append('\'').append(NEWLINE);
			}
			sReturn = sb.toString();
		}
		return sReturn;
	}

	/**
	 * Gets the domain of this DatalogProgram (every String parameter in Rules, Queries, and Facts).
	 * This does not return duplicates and returns them in alphabetical order.
	 * @return a sorted list of the Domain (no quotes)
	 */
	public SortedSet<String> getDomain(){
		if(this.domain == null){
			this.domain = new TreeSet<String>();
			for(Fact tFact : this.getFactList()){
				fillDomain(tFact, this.domain);
			}

			for(Rule tRule : this.getRuleList()){
				fillDomain(tRule, this.domain);
			}

			for(Query tQuery : this.getQueryList()){
				fillDomain(tQuery, this.domain);
			}
		}
		return this.domain;
	}

	/**
	 * Adds the values of all Parameters with String values to the domain.
	 * This method doesn't force you to use a SortedSet for your domain, but you probably should.
	 * @param tPredicate the Predicate to extract domain values from
	 * @param tDomain a reference to a Set representing the domain
	 */
	private void fillDomain(Predicate tPredicate, Set<String> tDomain){
		for(Parameter p : tPredicate){
			if(p.getTokenType() == TokenType.STRING){
				tDomain.add(p.getValue());
			}
		}
	}
}
