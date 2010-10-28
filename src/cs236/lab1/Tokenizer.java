/*
 * Tokenizer.java
 *
 * Copyright (c) 2010, T. Jameson Little.
 *
 * This file is part of cs236.lab1.
 *
 * Tokenizer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Tokenizer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with cs236.lab1.  If not, see <http ://www.gnu.org/licenses/>.
 */

package cs236.lab1;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

/**
 * Tokenizer parses a text file into individual Tokens as defined by the Datalog spec.
 * @author jameson
 */
public class Tokenizer {
	private final char START_OF_STRING = '\'';
	private final char NEW_LINE = '\n';
	private final char EOF = (char) -1;
	private final char COMMENT = '#';

	private final char SYM_LEFT_PAREN = '(';
	private final char SYM_RIGHT_PAREN = ')';
	private final char SYM_COMMA = ',';
	private final char SYM_PERIOD = '.';
	private final char SYM_QUESTION = '?';
	private final char SYM_COLON = ':';
	private final char SYM_DASH = '-';

	private static Map<TokenType, String> keywords = new EnumMap<TokenType, String>(TokenType.class);

	// emulates
	private char curChar;
	private char nextChar;
	private boolean bNextCharWorks = false;

	private int lineNumber;
	private String tokenData;

	private BufferedInputStream in = null;

	/**
	 * Only do this when you want to optimize for memory allocation.
	 * Call setFile() to start parsing.
	 */
	public Tokenizer(){
		keywords.put(TokenType.FACTS, "Facts");
		keywords.put(TokenType.QUERIES, "Queries");
		keywords.put(TokenType.RULES, "Rules");
		keywords.put(TokenType.SCHEMES, "Schemes");
		this.lineNumber = 1; // lineNumber is 1 based
	}

	/**
	 * Convenience method for the empty constructor and setFile().
	 * @param sFile
	 * @throws FileNotFoundException
	 */
	public Tokenizer(String sFile) throws FileNotFoundException {
		this();
		this.setFile(sFile);
	}

	/**
	 * Creates a new InputStream and sets the lineNumber to 0.
	 * This can be used with cleanUp to save memory allocation when working with multiple files.
	 *
	 * @param sFile
	 * @throws FileNotFoundException The file given is not valid
	 */
	public final void setFile(String sFile) throws FileNotFoundException{
		File tFile = new File(sFile);
		if(tFile.exists() && tFile.canRead()){
			this.in = new BufferedInputStream(new FileInputStream(tFile));
		}
	}

	/**
	 * Gets the character at the current location.
	 * @return
	 */
	private char getCurChar(){
		return this.curChar;
	}

	/**
	 * Closes the stream associated with the file being read.
	 */
	public void cleanUp(){
		try{
			this.in.close();
		}catch(IOException ex){
		}
	}

	/**
	 * Convenience method for getNextChar(true)
	 * @return
	 */
	private char pop(boolean bIgnoreWhitespace){
		return this.getNextChar(true, bIgnoreWhitespace);
	}

	/**
	 * Convenience method for getNextChar(false)
	 * @return
	 */
	private char peek(boolean bIgnoreWhitespace){
		return this.getNextChar(false, bIgnoreWhitespace);
	}

	/**
	 * Gets the next character (curPos + 1) or 0 (byte value, not ASCII) if at the end of the file
	 * @param bIncrement If false, acts like peek, if true, acts like a regular next.
	 * @return
	 */
	private char getNextChar(boolean bIncrement, boolean bIgnoreWhitespace){
		try{
			if(!bIncrement){
				if(!bNextCharWorks){
					this.nextChar = (char)in.read();
					this.bNextCharWorks = true;
				}
				return nextChar;
			}
			if(bNextCharWorks){
				this.curChar = this.nextChar;
				this.bNextCharWorks = false;
			}else{
				this.curChar = (char)in.read();
			}
			// hahahahaha, this was legacy code (used to spite checkstyle), so I'll keep it for fun
			this.lineNumber = (this.curChar == NEW_LINE) ? this.lineNumber + 1 : this.lineNumber;

			// if this is just whitespace, then let's get them something real
			if(Character.isWhitespace(this.curChar) && bIgnoreWhitespace){
				return getNextChar(bIncrement, bIgnoreWhitespace);
			}
		}catch(IOException ex){

		}
		return this.curChar; // about as close to null as we can get...
	}

	/**
	 * 
	 * @return
	 * @throws Exception- Message is the String up until this point
	 */
	private Token readString() {
		this.tokenData += this.getCurChar();
		char tChar = this.peek(false); // don't pop it yet, because pop doesn't return new lines
		while(tChar != START_OF_STRING){
			if(tChar != NEW_LINE && tChar != EOF){
				this.tokenData += this.pop(false);
			}else{
				return new Token(TokenType.UNDEFINED, this.lineNumber, this.tokenData);
			}

			tChar = this.peek(false);
		}
		this.tokenData += this.pop(false);
		this.tokenData = this.tokenData.substring(1, this.tokenData.length() - 1);
		return new Token(TokenType.STRING, this.lineNumber, this.tokenData);
	}

	private Token readSymbol(){
		Token tReturn = null;

		char tChar = this.getCurChar();
		switch(tChar){
			case SYM_COLON:{
				this.tokenData += tChar;
				// satisfies the ':-" symbol
				if(this.peek(true) == SYM_DASH){
					this.tokenData += this.pop(true);
					tReturn = new Token(TokenType.COLON_DASH, this.lineNumber, this.tokenData);
				}else{
					tReturn = new Token(TokenType.COLON, this.lineNumber, this.tokenData);
				}
				break;
			}
			case SYM_COMMA:{
				this.tokenData += tChar;
				tReturn = new Token(TokenType.COMMA, this.lineNumber, this.tokenData);
				break;
			}
			case SYM_PERIOD:{
				this.tokenData += tChar;
				tReturn = new Token(TokenType.PERIOD, this.lineNumber, this.tokenData);
				break;
			}
			case SYM_QUESTION:{
				this.tokenData += tChar;
				tReturn = new Token(TokenType.Q_MARK, this.lineNumber, this.tokenData);
				break;
			}
			case SYM_LEFT_PAREN:{
				this.tokenData += tChar;
				tReturn = new Token(TokenType.LEFT_PAREN, this.lineNumber, this.tokenData);
				break;
			}
			case SYM_RIGHT_PAREN:{
				this.tokenData += tChar;
				tReturn = new Token(TokenType.RIGHT_PAREN, this.lineNumber, this.tokenData);
				break;
			}
			default:
				this.tokenData += tChar;
				tReturn = new Token(TokenType.UNDEFINED, this.lineNumber, this.tokenData);
				break;
		}
		return tReturn;
	}

	private Token readIdentifier(){
		char tChar = this.getCurChar();
		this.tokenData += tChar;
		if(!isValidIdentifierChar(tChar, true)){ // must start with a letter
			return new Token(TokenType.UNDEFINED, this.lineNumber, this.tokenData);
		}

		// identifiers can only be digits or letters
		// don't increment increment yet so we don't miss the next token
		while(isValidIdentifierChar(this.peek(false), false)){
			tChar = this.pop(false);
			this.tokenData += tChar;
			if(isKeyword(tokenData) && !isValidIdentifierChar(this.peek(false), false)){
				return createKeyword(tokenData, this.lineNumber);
			}
		}
		return new Token(TokenType.IDENT, this.lineNumber, this.tokenData);
	}

	private static boolean isValidIdentifierChar(char tChar, boolean bFirstChar){
		if(Character.isLetter(tChar)){
			return true;
		}else if(!bFirstChar && Character.isDigit(tChar)){
			return true;
		}
		return false;
	}

	private static boolean isKeyword(String sValue){
		return keywords.containsValue(sValue);
	}

	private static Token createKeyword(String sValue, int iLineNumber){
		for(TokenType t : keywords.keySet()){
			if(keywords.get(t).equals(sValue)){
				return new Token(t, iLineNumber, sValue);
			}
		}
		// should never get this far since we made the checks earlier
		return null;
	}

	private void readComment(){
		char tChar = this.getCurChar();
		while(this.peek(false) != NEW_LINE && this.peek(false) != EOF){
			tChar = this.pop(false);
		}
	}

	/**
	 * Gets the next token.  This reads from the stream created in the Constructor.
	 * @return the next Token in the file
	 */
	public Token getNextToken(){
		char tChar = this.pop(true);
		if(tChar == EOF){ // make sure we're not at the end
			return new Token(TokenType.EOF, this.lineNumber, "");
		}
		
		// let's start off with a clean slate; don't reallocate if we don't have to
		tokenData = new String();
		if(Character.isLetter(tChar)){ // Identifiers (or keywords) are complex, keep them separate
			return readIdentifier();
		}

		Token tReturn = null; // to avoid lexical complexity, let's make a return value
		switch(tChar){
			case START_OF_STRING:{
				tReturn = this.readString();
				break;
			}

			case COMMENT:{
				readComment();
				tReturn = this.getNextToken();
				break;
			}

			default:{ // we have to assume that it's a symbol, don't worry, we readSymbol cheecks
				tReturn = readSymbol();
				break;
			}
		}
		return tReturn;
	}
}
