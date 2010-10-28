/*
 * Scheme.java
 *
 * Copyright (c) 2010, T. Jameson Little.
 *
 * This file is part of cs236.lab2.
 *
 * Scheme is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Scheme is distributed in the hope that it will be useful,
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
 * A Scheme is a Predicate that has only Identifiers as Parameters.
 * @author jameson
 */
public class Scheme extends Predicate{
	/**
	 * Creates a new Scheme with a name and a list of Parameters (parameters should be identifiers)
	 * @param tName the name of this Scheme
	 * @param tPredicateList the list of Parameters
	 */
	public Scheme(String tName, List<Parameter> tPredicateList){
		super(tName);
		this.addAll(tPredicateList);
	}
}
