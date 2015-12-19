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
package org.deri.iris.api.graph;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.graph.LabeledEdge;

/**
 * <p>
 * A graph to determine the dependencies of rules and predicates to each other.
 * </p>
 * <p>
 * $Id$
 * </p>
 * 
 * @author Richard Pöttler (richard dot poettler at deri dot org)
 * @version $Revision$
 */
public interface IPredicateGraph {
	/**
	 * Adds a rule to the graph.
	 * 
	 * @param rule
	 *            the rule to add
	 * @throws NullPointerException
	 *             if the rule is {@code null}
	 */
	public abstract void addRule(final IRule rule);

	/**
	 * Determines whether the rules are recursive.
	 * 
	 * @return {@code true} if they are recursive, otherwise {@code false}
	 */
	public abstract boolean detectCycles();

	/**
	 * Determines the vertexes contained in the cycle.
	 * 
	 * @return a Set containing all the vertexes
	 */
	public abstract Set<IPredicate> findVertexesForCycle();

	/**
	 * Determines the edges contained in the cycle.
	 * 
	 * @return a Set containing all the vertexes
	 */
	public abstract Set<LabeledEdge<IPredicate, Boolean>> findEdgesForCycle();

	/**
	 * Returns the number of negative Literals in the cycle.
	 * 
	 * @return the number of negative literals
	 */
	public abstract int countNegativesForCycle();

	/**
	 * Adds a collection of rules to the graph.
	 * 
	 * @param r
	 *            the collection of rules
	 * @throws NullPointerException
	 *             if the collections is or contains {@code null}
	 */
	public abstract void addRule(final Collection<IRule> r);

	/**
	 * Returns a set of predicates the given one depends on.
	 * 
	 * @param p
	 *            predicate for which to check for dependencies
	 * @return the predicates the predicate depends on
	 * @throws NullPointerException
	 *             if the predicate is {@code null}
	 */
	public abstract Set<IPredicate> getDepends(final IPredicate p);

	/**
	 * <p>
	 * Returns a compareator which compares two predicates depending on their
	 * dependencies of their rules.
	 * </p>
	 * <p>
	 * If one of the compared predicate isn't in the graph, or there isn't a
	 * path from one predicate to the other {@code 0} will be returned. If the
	 * first predicate depends on the second one, the first one will be
	 * determined to be bigger, and vice versa.
	 * </p>
	 * 
	 * @return the comparator
	 */
	public abstract Comparator<IPredicate> getPredicateComparator();

	/**
	 * <p>
	 * Returs a comparator which compares two rules depending on their
	 * dependencies of each other.
	 * </p>
	 * <p>
	 * The rules will compared according to their headpredicates.
	 * </p>
	 * 
	 * @return the comparator
	 * @see IPredicateGraph#getPredicateComparator()
	 */
	public abstract Comparator<IRule> getRuleComparator();
}
