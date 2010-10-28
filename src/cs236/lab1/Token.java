/*
 * Token.java
 *
 * Copyright (c) 2010, T. Jameson Little.
 *
 * This file is part of cs236.lab1.
 *
 * Token is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Token is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with cs236.lab1.  If not, see <http ://www.gnu.org/licenses/>.
 */

package cs236.lab1;

/**
 * A Token represents one unit of a grammar.
 * @author jameson
 */
public class Token {
	private TokenType tokenType;
	private int lineNumber;
	private String value;

	/**
	 * Creates a token with all of the basic values.
	 *
	 * @param tTokenType The type of token to create
	 * @param iLineNumber The line number the token appears on
	 * @param sValue The value of this token
	 */
	public Token(TokenType tTokenType, int iLineNumber, String sValue){
		this.tokenType = tTokenType;
		this.lineNumber = iLineNumber;
		this.value = sValue;
	}

	/**
	 * The TokenType passed to the constructor.
	 * @return the TokenType
	 */
	public TokenType getTokenType(){
		return this.tokenType;
	}

	/**
	 * Gets the line number associated with this Token.
	 * @return the line number of this Token
	 */
	public int getLineNumber(){
		return this.lineNumber;
	}

	/**
	 * Gets the value of this Token.
	 * @return the value as a String
	 */
	public String getValue(){
		return this.value;
	}

	/**
	 * Formats this token according to this pattern- (TokenType,"value",lineNumber)
	 * @return the formatted String
	 */
	@Override
	public String toString(){
		return String.format("(%s,\"%s\",%d)", this.tokenType, this.value, this.lineNumber);
	}
}
