/*
 * Lab2.java
 *
 * Copyright (c) 2010, T. Jameson Little.
 *
 * This file is part of cs236.lab2.
 *
 * Lab2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Lab2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with cs236.lab2.  If not, see <http ://www.gnu.org/licenses/>.
 */

package cs236.lab2;

import cs236.lab1.Tokenizer;
import java.io.FileNotFoundException;

/**
 * Creates a DatalogProgram from a file passed in and outputs it and its domain.
 * @author jameson
 */
public class Lab2 {
	/**
	 * Parses through the files given.
	 * This method outputs a String to the console according to DatalogProgram's toString() method.
	 * @param args List of files to parse.
	 */
	public static void main(String[] args){
		if(args != null && args.length > 0){
			for(String sFile : args){
				try{
					Tokenizer tTokenizer = new Tokenizer();
					tTokenizer.setFile(sFile);

					TokenizerServer tServer = new TokenizerServer(tTokenizer);
					DatalogProgram dp = tServer.run();
					System.out.println(dp.toString());
				}catch(FileNotFoundException ex){
					System.out.println("File does not exist. File:");
					System.out.println(sFile);
				}
			}
		}
	}
}
