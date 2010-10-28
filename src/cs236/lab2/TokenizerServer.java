/*
 * TokenizerServer.java
 *
 * Copyright (c) 2010, T. Jameson Little.
 *
 * This file is part of cs236.lab2.
 *
 * TokenizerServer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TokenizerServer is distributed in the hope that it will be useful,
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
import cs236.lab1.Tokenizer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class creates a new thread to parse Tokens into a Datalog program.
 * This class used to be a separate thread, but I decided to make it a regular class.
 * @author jameson
 */
public class TokenizerServer{
	private Tokenizer tokenizer;
	private AtomicBoolean bStop = new AtomicBoolean(false);

	private DatalogProgram dp;

	/**
	 * Creates a new instance with a Tokenizer to run.
	 * @param tTokenizer
	 */
	public TokenizerServer(Tokenizer tTokenizer){
		this.tokenizer = tTokenizer;
		this.dp = new DatalogProgram();
		this.dp.setTokenizerServer(this);
	}

	/**
	 * Stops parsing tokens from Tokenizer.
	 */
	public void stopParsing(){
		this.bStop.set(true);
	}

	/**
	 * Passes Tokens received from Tokenizer to a queue in DatalogProgram to parse.
	 * DatalogProgram parses in it's own Thread while this passes in new Tokens.
	 * @return the complete DatalogProgram
	 */
	public DatalogProgram run(){
		final long SLEEP_TIME = 50;
		Token tToken = this.tokenizer.getNextToken();

		Thread tDatalogThread = new Thread(dp);
		tDatalogThread.start();

		while(tToken.getTokenType() != TokenType.EOF){
			if(bStop.get()){
				this.tokenizer.cleanUp();
				return dp; // we have an error, let's stop and output the error
			}
			dp.addToQueue(tToken);

			tToken = this.tokenizer.getNextToken();
		}
		tokenizer.cleanUp();
		dp.addToQueue(tToken); // make sure the datalog program knows when to end
		while (!bStop.get()) {
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException ex) {
				Logger.getLogger(TokenizerServer.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		return dp;
	}
}
