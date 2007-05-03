/*
 *	This file is part of dhcp4java, a DHCP API for the Java language.
 *	(c) 2006 Stephan Hadinger
 *
 *	This library is free software; you can redistribute it and/or
 *	modify it under the terms of the GNU Lesser General Public
 *	License as published by the Free Software Foundation; either
 *	version 2.1 of the License, or (at your option) any later version.
 *
 *	This library is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *	Lesser General Public License for more details.
 *
 *	You should have received a copy of the GNU Lesser General Public
 *	License along with this library; if not, write to the Free Software
 *	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.dhcpcluster.backend;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.72
 */
public class QueryRunner2 extends QueryRunner {
	
	private static final Map<String, String>				queryCache = new HashMap<String, String>();
	private static final Map<String, Map<String, int[]>> mapCache = new HashMap<String, Map<String, int[]>>();

	/* (non-Javadoc)
	 * @see org.apache.commons.dbutils.QueryRunner#query(java.sql.Connection, java.lang.String, java.lang.Object[], org.apache.commons.dbutils.ResultSetHandler)
	 */
	public Object queryNamedParams(Connection conn, String sql, Map<String, Object> params, ResultSetHandler rsh) throws SQLException {
		return super.query(conn, sql, namedParametersToArray(sql, params), rsh);
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.dbutils.QueryRunner#update(java.sql.Connection, java.lang.String, java.lang.Object[])
	 */
	public int updateNamesParams(Connection conn, String sql, Map<String, Object> params) throws SQLException {
		return super.update(conn, sql, namedParametersToArray(sql, params));
	}
	
	private final synchronized Object[] namedParametersToArray(String query, Map<String, Object> params) {
		Map<String, int[]> paramMap = mapCache.get(query);
		String parsedQuery = queryCache.get(query);
		if ((paramMap == null) || (parsedQuery == null)) {
			paramMap = new HashMap<String, int[]>();
			parsedQuery = parse(query, paramMap);
			queryCache.put(query, parsedQuery);
			mapCache.put(query, paramMap);
		}
		int paramNumber = paramMap.get("")[0];
		Object[] args = new Object[paramNumber];
		for (Entry<String, Object> paramPair : params.entrySet()) {
			String paramName = paramPair.getKey().toLowerCase();
			Object paramValue = paramPair.getValue();
			int[] indexArray = paramMap.get(paramName);
			if (indexArray == null) {
				throw new IllegalArgumentException("Parameter "+paramName+" is not supported for request: "+query);
			}
			for (int i : indexArray) {
				args[i] = paramValue;
			}
		}
		return args;
	}

    /**
     * Parses a query with named parameters.  The parameter-index mappings are put into the map, and the
     * parsed query is returned.  DO NOT CALL FROM CLIENT CODE.  This method is non-private so JUnit code can
     * test it.
     * @param query    query to parse
     * @param paramMap map to hold parameter-index mappings
     * @return the parsed query
     */
    private static final String parse(String query, Map<String, int[]> paramMap) {
    	// I was originally using regular expressions, but they didn't work well for ignoring
    	// parameter-like strings inside quotes.
    	int length = query.length();
    	StringBuffer parsedQuery = new StringBuffer(length);
    	boolean inSingleQuote = false;
    	boolean inDoubleQuote = false;
    	int index=1;
    	Map<String, List<Integer>> tempMap = new HashMap<String, List<Integer>>();
    	
    	for(int i=0; i<length; i++) {
    		char c=query.charAt(i);
    		if (inSingleQuote) {
    			if (c=='\'') {
    				inSingleQuote=false;
    			}
    		} else if(inDoubleQuote) {
    			if(c=='"') {
    				inDoubleQuote=false;
    			}
    		} else {
    			if(c=='\'') {
    				inSingleQuote=true;
    			} else if(c=='"') {
    				inDoubleQuote=true;
    			} else if(c==':' && i+1<length && Character.isJavaIdentifierStart(query.charAt(i+1))) {
    				int j = i+2;
    				while (j<length && Character.isJavaIdentifierPart(query.charAt(j))) {
    					j++;
    				}
    				String name=query.substring(i+1, j).toLowerCase();
    				c='?'; // replace the parameter with a question mark
    				i += name.length(); // skip past the end if the parameter
    				
    				List<Integer> indexList = tempMap.get(name);
    				if (indexList==null) {
    					indexList = new LinkedList<Integer>();
    					tempMap.put(name, indexList);
    				}
    				indexList.add(new Integer(index-1));
    				
    				index++;
    			}
    		}
    		parsedQuery.append(c);
    	}

    	// replace the lists of Integer objects with arrays of ints
    	for (Map.Entry<String, List<Integer>> entry : tempMap.entrySet()) {
    		List<Integer> list = entry.getValue();
    		int[] indexes = new int[list.size()];
    		int i=0;
    		for (Integer x : list) {
    			indexes[i++] = x;
    		paramMap.put(entry.getKey(), indexes);
    		}
    	}
    	int[] lengthArray = new int[1];
    	lengthArray[0] = index-1;
    	paramMap.put("", lengthArray);		// insert total number of parameters
    	
    	return parsedQuery.toString();
    }
    
}
