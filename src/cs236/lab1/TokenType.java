/*
 * TokenType.java
 *
 * Copyright (c) 2010, T. Jameson Little.
 *
 * This file is part of cs236.lab1.
 *
 * TokenType is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TokenType is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with cs236.lab1.  If not, see <http ://www.gnu.org/licenses/>.
 */

package cs236.lab1;

/**
 * A TokenType describes a Token and gives meaning to its value.
 * @author jameson
 */
public enum TokenType {
	/**
	 * Datalog Schemes- follows form- Schemes
	 */
	SCHEMES,
	/**
	 * Datalog Facts- follows form- Facts
	 */
	FACTS,
	/**
	 * Datalog Rules- follows form- Rules
	 */
	RULES,
	/**
	 * Datalog Queries- follows form- Queries
	 */
	QUERIES,
	/**
	 * Datalog Identifier- must start with a letter, only letters and numbers allowed
	 */
	IDENT,
	/**
	 * Datalog Identifier- anything encapsulated by two double quotes
	 */
	STRING,
	/**
	 * Left parenthesis
	 */
	LEFT_PAREN,
	/**
	 * Right parenthesis
	 */
	RIGHT_PAREN,
	/**
	 * Comma
	 */
	COMMA,
	/**
	 * Period
	 */
	PERIOD,
	/**
	 * Question mark
	 */
	Q_MARK,
	/**
	 * Colon
	 */
	COLON,
	/**
	 * A colon with a dash
	 */
	COLON_DASH,
	/**
	 * End of file
	 */
	EOF,
	/**
	 * Anything that is not valid according to the Datalog grammar
	 */
	UNDEFINED
}
