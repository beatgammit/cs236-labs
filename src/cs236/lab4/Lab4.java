/*
 * Lab4.java
 *
 * Copyright (c) 2010, T. Jameson Little.
 *
 * This file is part of cs236.lab3.
 *
 * Lab4 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Lab4 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with cs236.lab3.  If not, see <http ://www.gnu.org/licenses/>.
 */

package cs236.lab4;

import cs236.lab1.Tokenizer;
import cs236.lab2.DatalogProgram;
import cs236.lab2.Query;
import cs236.lab2.TokenizerServer;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class parses through Datalog and evaluates the Queries against the Facts in each file.
 * @author jameson
 */
public class Lab4 {
	private final static long WAIT_TIME = 10; // keep this pretty small, we have fast processors

	/**
	 * This parses through files containing Datalog and evaluates the Queries against the Facts.
	 * This prints out the results to the console.
	 * @param args files containing Datalog
	 */
	public static void main(String[] args) {
		if(args != null && args.length > 0){
			for(String sFile : args){
				try{
					Tokenizer tTokenizer = new Tokenizer();
					tTokenizer.setFile(sFile);

					TokenizerServer tServer = new TokenizerServer(tTokenizer);
					DatalogProgram dp = tServer.run();

					System.out.println(evaluateQueries(dp));
				}catch(FileNotFoundException ex){
					System.out.println("File does not exist. File:");
					System.out.println(sFile);
				}
			}
		}
	}

	/**
	 * Evaluates the Queries.  Must be called after it's done parsing.
	 * @return a formatted String of the results of evaluation
	 */
	public static String evaluateQueries(DatalogProgram dp){
		List<QueryEvaluatorWithRules> qeList = new ArrayList<QueryEvaluatorWithRules>();
		for(Query q : dp.getQueryList()){
			QueryEvaluatorWithRules qe = new QueryEvaluatorWithRules(q, dp);
			qeList.add(qe);
			qe.run();

			// start the thread so we can move on with life
//			Thread tThread = new Thread(qe);
//			tThread.start();
		}

		StringBuilder sb = new StringBuilder();
		for(QueryEvaluatorWithRules qe : qeList){
			// this only makes sense if we're multithreading, which might not actually buy us much
			while(!qe.isFinished()){
				try {
					Thread.sleep(WAIT_TIME);
				} catch (InterruptedException ex) {
					Logger.getLogger(DatalogProgram.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
			sb.append(qe.toString());
		}

		return sb.toString();
	}
}
