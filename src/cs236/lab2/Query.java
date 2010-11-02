/*
 * Query.java
 *
 * Copyright (c) 2010, T. Jameson Little.
 *
 * This file is part of cs236.lab2.
 *
 * Query is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Query is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with cs236.lab2.  If not, see <http ://www.gnu.org/licenses/>.
 */

package cs236.lab2;

import java.util.ArrayList;
import java.util.List;

/**
 * A Query is a Predicate that can be evaluated against the Facts.
 * A Query can have all Strings as Parameters, all Identifiers, or a mixture of both.
 * @author jameson
 */
public class Query extends Predicate{
	/**
	 * Creates a new Query with a name and a list of Parameters.
	 * @param tName the name of this Query
	 * @param tParamList the list of Parameters to associate with this Query
	 */
	public Query(String tName, List<Parameter> tParamList){
		super(tName);
		this.addAll(tParamList);
	}

	@Override
	public Query duplicate(){
		Query p = new Query(new String(this.getValue()), new ArrayList<Parameter>());
		for(Parameter tParam : this){
			p.add(tParam.duplicate());
		}
		return p;
	}
}
