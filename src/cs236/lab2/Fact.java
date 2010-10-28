/*
 * Fact.java
 *
 * Copyright (c) 2010, T. Jameson Little.
 *
 * This file is part of cs236.lab2.
 *
 * Fact is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Fact is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with cs236.lab2.  If not, see <http ://www.gnu.org/licenses/>.
 */

package cs236.lab2;

import java.util.List;

/**
 * A Fact consists of an Predicate whose parameters are all constants.
 * @author jameson
 */
public class Fact extends Predicate{
	/**
	 * Creates a new Fact using Predicate(String) and fills it with parameters from the list.
	 * @param sValue The name of this Fact
	 * @param tParamList The parameters associated with this Fact.
	 */
	public Fact(String sValue, List<Parameter> tParamList){
		super(sValue);
		this.addAll(tParamList);
	}
}
