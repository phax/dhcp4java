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
package org.dhcpcluster.config.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.dhcpcluster.config.xml.data.Filter;
import org.dhcpcluster.config.xml.data.TypeFilterRoot;
import org.dhcpcluster.filter.AlwaysFalseFilter;
import org.dhcpcluster.filter.AlwaysTrueFilter;
import org.dhcpcluster.filter.AndFilter;
import org.dhcpcluster.filter.NotFilter;
import org.dhcpcluster.filter.NumOptionFilter;
import org.dhcpcluster.filter.OrFilter;
import org.dhcpcluster.filter.RequestFilter;
import org.dhcpcluster.filter.StringOptionFilter;
import org.dhcpcluster.filter.StringOptionFilter.CompareMode;

/**
 * 
 * @author Stephan Hadinger
 * @version 0.71
 */
public class XmlFilterFactory {

	private static final Logger logger = Logger.getLogger(XmlFilterFactory.class.getName().toLowerCase());

	private XmlFilterFactory() {
		throw new UnsupportedOperationException();
	}

    public static RequestFilter makeFilterRoot(Filter filter) {
    	if (filter == null) {
    		return ALWAYS_TRUE_FILTER;
    	}
    	return makeFilter(filter.getFilterGroup());
    }
    
    public static RequestFilter makeFilter(TypeFilterRoot filter) {
    	if (filter == null) {
    		throw new NullPointerException();
    	}
    	if (filter instanceof Filter.FilterAlwaysTrue) {
    		return ALWAYS_TRUE_FILTER;
    	} else if (filter instanceof Filter.FilterAlwaysFalse) {
    		return ALWAYS_FALSE_FILTER;
    	} else if (filter instanceof Filter.FilterAnd) {
    		List<TypeFilterRoot> list = ((Filter.FilterAnd)filter).getFilterGroup();
    		return new AndFilter(makeFilterList(list));
    	} else if (filter instanceof Filter.FilterOr) {
    		List<TypeFilterRoot> list = ((Filter.FilterOr)filter).getFilterGroup();
    		return new OrFilter(makeFilterList(list));
    	} else if (filter instanceof Filter.FilterNot) {
    		return new NotFilter(makeFilter(((Filter.FilterNot)filter).getFilterGroup()));
    	} else if (filter instanceof Filter.FilterNumOption) {
    		return makeNumOptionFilter((Filter.FilterNumOption)filter);
    	} else if (filter instanceof Filter.FilterStringOption) {
    		return makeStringFilter((Filter.FilterStringOption)filter);
    	}
    	throw new IllegalStateException("Unknown filter type: "+filter);
    }
    
    public static List<RequestFilter> makeFilterList(List<TypeFilterRoot> filterList) {
    	ArrayList<RequestFilter> resList = new ArrayList<RequestFilter>(filterList.size());
    	for (TypeFilterRoot elt : filterList) {
    		resList.add(makeFilter(elt));
    	}
    	return resList;
    }
    
    public static RequestFilter makeNumOptionFilter(Filter.FilterNumOption filter) {
		NumOptionFilter.CompareOp op;
		switch (filter.getOp()) {
			case EQ:
				op = NumOptionFilter.CompareOp.EQ;
				break;
			case NE:
				op = NumOptionFilter.CompareOp.NE;
				break;
			case LE:
				op = NumOptionFilter.CompareOp.LE;
				break;
			case GE:
				op = NumOptionFilter.CompareOp.GE;
				break;
			case LT:
				op = NumOptionFilter.CompareOp.LT;
				break;
			case GT:
				op = NumOptionFilter.CompareOp.GT;
				break;
			default:
				logger.severe("Unrecognized operation: "+filter.getOp().value() + ", default to ALWAYS_FALSE");
				return ALWAYS_FALSE_FILTER;
		}
		return new NumOptionFilter(filter.getCode(), filter.getValueInt(), op);
    }
    
    public static RequestFilter makeStringFilter(Filter.FilterStringOption filter) {
    	StringOptionFilter.CompareMode mode;
    	switch (filter.getMode()) {
    		case EXACT:
    			mode = CompareMode.EXACT;
    			break;
    		case CASE_INSENSITIVE:
    			mode = CompareMode.CASE_INSENSITIVE;
    			break;
    		case REGEX:
    			mode = CompareMode.REGEX;
    			break;
    		default:
				logger.severe("Unrecognized operation: "+filter.getMode().value() + ", default to ALWAYS_FALSE");
				return ALWAYS_FALSE_FILTER;
    	}
    	return new StringOptionFilter(filter.getCode(), filter.getValueString(), mode);
    }
    
    private static final RequestFilter ALWAYS_TRUE_FILTER = new AlwaysTrueFilter();
    private static final RequestFilter ALWAYS_FALSE_FILTER = new AlwaysFalseFilter();
}
