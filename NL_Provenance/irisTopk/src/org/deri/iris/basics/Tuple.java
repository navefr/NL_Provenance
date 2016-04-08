/*
 * Integrated Rule Inference System (IRIS):
 * An extensible rule inference system for datalog with extensions.
 * 
 * Copyright (C) 2008 Semantic Technology Institute (STI) Innsbruck, 
 * University of Innsbruck, Technikerstrasse 21a, 6020 Innsbruck, Austria.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 */
package org.deri.iris.basics;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.IConstructedTerm;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.api.terms.IVariable;

import Top1.DerivationTree2;

/**
 * <p>
 * A simple tuple implementation. This implementation is thread-safe.
 * </p>
 * <p>
 * $Id: Tuple.java,v 1.20 2007-11-07 16:14:44 nathaliest Exp $
 * </p>
 * @author Darko Anicic, DERI Innsbruck
 * @author Richard Pöttler (richard dot poettler at deri dot at)
 * @version $Revision: 1.20 $
 */
public class Tuple extends AbstractList<ITerm> implements ITuple {

	/** The terms stored in this tuple. */
	private final ITerm[] terms;
	
	private boolean isFact;
	
	private boolean isTopKUpdated;
	
	private boolean isTop1Found;
	
	private Collection<DerivationTree2> trees = new ArrayList<>();
	
	private double curRuleWeight;
	
	private String predicate;
	
	/**
	 * Creates a tuple defined by the list of terms.
	 * 
	 * @param terms list of terms that create a tuple
	 * @throws NullPointerException if terms is <code>null</code>
	 */
	public Tuple(final Collection<ITerm> t){
		if (t == null) {
			throw new NullPointerException("Input argument must not be null");
		}
		terms = t.toArray(new ITerm[t.size()]);
	}
	
	public int size() {
		return terms.length;
	}

	public ITerm get(final int i) {
		if (i < 0) {
			throw new IllegalArgumentException("The index must be positive, but was " + i);
		}
		if (i >= terms.length) {
			throw new IllegalArgumentException(
					"The index must not be greater or equal to the size (" + 
					size() + "), but was " + i);
		}
		return terms[i];
	}

	public ITuple append(final Collection<? extends ITerm> t) {
		if (t == null) {
			throw new IllegalArgumentException("The term list must not be null");
		}

		if (t.isEmpty()) {
			return this;
		}

		final List<ITerm> res = new LinkedList<ITerm>(this);
		for (final ITerm term : t) {
			res.add(term);
		}
		return new Tuple(res);
	}

	public boolean isGround() {
		for (final ITerm t : terms){
			if(!t.isGround()) {
				return false;
			}
		}
		return true;
	}

	public String toString(){
		if (terms.length <= 0) {
			return "()";
		}
		final StringBuilder buffer = new StringBuilder();
		buffer.append('(');
		boolean first = true;
		for (final ITerm t : terms) {
			if( first )
				first = false;
			else
				buffer.append(", ");
			buffer.append(t);
		}
		buffer.append(')');
		return buffer.toString();
	}

	public int compareTo(final ITuple t) {
		if (t == null) {
			throw new NullPointerException("Cannot compare with null");
		}
		
		int res = 0;
		for (int i = 0; i < Math.min(terms.length, t.size()); i++) {
			if ((res = terms[i].compareTo(t.get(i))) != 0) {
				return res;
			}
		}
		return terms.length - t.size();
	}

	public boolean equals(final Object o) {
		if (!(o instanceof Tuple)) {
			return false;
		}
		return super.equals(o);
	}

	public Set<IVariable> getVariables() {
		final Set<IVariable> variables = new HashSet<IVariable>();
		for (final ITerm term : terms) {
			if(term instanceof IVariable) {
				variables.add((IVariable) term);
			}
			if(term instanceof IConstructedTerm) {
				variables.addAll(getVariables((IConstructedTerm) term));
			}
		}
		return variables;
	}
	
	private Set<IVariable> getVariables(final IConstructedTerm t) {
		assert t != null: "The conscructed term must not be null";

		final Set<IVariable> variables = new HashSet<IVariable>();
		for (final ITerm term : t.getValue()) {
			if(term instanceof IVariable) {
				variables.add((IVariable) term);
			}
			if(term instanceof IConstructedTerm) {
				variables.addAll(getVariables((IConstructedTerm) term));
			}
		}
		return variables;
	}

	public List<IVariable> getAllVariables() {
		final List<IVariable> variables = new ArrayList<IVariable>();
		for (final ITerm term : terms) {
			if (term instanceof IVariable) {
				variables.add((IVariable) term);
			}
			if (term instanceof IConstructedTerm) {
				variables.addAll(getAllVariables((IConstructedTerm) term));
			}
		}
		return variables;
	}
	
	private List<IVariable> getAllVariables(final IConstructedTerm t) {
		assert t != null: "The conscructed term must not be null";

		final List<IVariable> variables = new ArrayList<IVariable>();
		for(final ITerm term : t.getValue()){
			if (term instanceof IVariable) {
				variables.add((IVariable) term);
			}
			if (term instanceof IConstructedTerm) {
				variables.addAll(getAllVariables((IConstructedTerm) term));
			}
		}
		return variables;
	}

	public boolean isFact() {
		return isFact;
	}

	public void setFact(boolean isFact) {
		this.isFact = isFact;
	}

	public boolean isTopKUpdated() {
		return isTopKUpdated;
	}

	public void setTopKUpdated(boolean isTopKUpdated) {
		this.isTopKUpdated = isTopKUpdated;
	}

	public Collection<DerivationTree2> getTrees() {
		return trees;
	}

	public void addTree(DerivationTree2 tree) {
        this.trees.add(tree);
	}

	public boolean isTop1Found() {
		return isTop1Found;
	}

	public void setTop1Found(boolean isTop1Found) {
		this.isTop1Found = isTop1Found;
	}

	public double getCurRuleWeight() {
		return curRuleWeight;
	}

	public void setCurRuleWeight(double curRuleWeight) {
		this.curRuleWeight = curRuleWeight;
	}
	
	
	public String getPredicate() 
	{
		return predicate;
	}


	public void setPredicate(String predicate) 
	{
		this.predicate = predicate;
	}
	
	
	public ITerm [] getTerms ()
	{
		return terms;
	}
	
	
	public boolean isFullyInst ()
	{
		boolean retVal = true;
		for (ITerm iTerm : terms) 
		{
			if (iTerm instanceof IVariable) 
			{
				retVal = false;
				break;
			}
		}
		
		return retVal;
	}
	
}
