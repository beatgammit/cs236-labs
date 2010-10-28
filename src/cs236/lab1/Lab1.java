/*
 * Lab1.java
 *
 * Copyright (c) 2010, T. Jameson Little.
 *
 * This file is part of cs236.lab1.
 *
 * Lab1 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Lab1 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with cs236.lab1.  If not, see <http ://www.gnu.org/licenses/>.
 */

package cs236.lab1;

import java.io.FileNotFoundException;

/**
 * This class creates a Tokenizer and outputs a formatted list of those tokens.
 * @author jameson
 */
public class Lab1 {

	/**
	 * This method parses through the tokens in a file and outputs them to the console.
	 * @param args List of files to turn into Tokens.
	 */
	public static void main(String[] args) {
		if(args != null && args.length > 0){
			Tokenizer tTokenizer = new Tokenizer();
			for(String sFile : args){
				try{
					tTokenizer.setFile(sFile);
					int iNumTokens = 0;
					Token tToken = tTokenizer.getNextToken();
					while(tToken.getTokenType() != TokenType.EOF){
						iNumTokens++;
						System.out.println(tToken.toString());
						tToken = tTokenizer.getNextToken();
					}

					// print and tally the EOF Token
					iNumTokens++;
					System.out.println(tToken.toString());

					System.out.println("Total Tokens = " + iNumTokens);
				}catch(FileNotFoundException ex){
					System.out.println("File does not exist. File:\n" + sFile);
				}
			}
		}
	}
}
