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
package org.deri.iris.rules.compiler;

import org.apache.commons.collections15.IteratorUtils;
import org.deri.iris.Configuration;
import org.deri.iris.EvaluationException;
import org.deri.iris.api.basics.*;
import org.deri.iris.api.builtins.IBuiltinAtom;
import org.deri.iris.api.terms.IConstructedTerm;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.facts.IFacts;
import org.deri.iris.rules.RuleHeadEquality;
import org.deri.iris.storage.IRelation;
import org.deri.iris.utils.equivalence.IEquivalentTerms;
import org.deri.iris.utils.equivalence.IgnoreTermEquivalence;

import java.util.*;

/**
 * A rule compiler for creating objects that compute new facts using
 * forward-chaining techniques.
 */
public class RuleCompiler {

	/**
	 * Constructor.
	 * 
	 * @param facts The facts that will be used by the compiled rules.
	 */
	public RuleCompiler(IFacts facts, Configuration configuration) {
		this(facts, new IgnoreTermEquivalence(), configuration);
	}

	/**
	 * Creates a new RuleCompiler.
	 * 
	 * @param facts The facts that will be used by the compiled rules.
	 * @param equivalentTerms The equivalent terms.
	 * @param configuration The configuration.
	 */
	public RuleCompiler(IFacts facts, IEquivalentTerms equivalentTerms,
			Configuration configuration) {
		mFacts = facts;
		mConfiguration = configuration;
		mEquivalentTerms = equivalentTerms;
	}

	/**
	 * Compile a rule. No optimisations of any kind are attempted.
	 * 
	 * @param rule The rule to be compiled
	 * @return The compiled rule, ready to be evaluated
	 * @throws EvaluationException If the query can not be compiled for any
	 *             reason.
	 */
	public ICompiledRule compile(IRule rule) throws EvaluationException {
        LinkedHashMap<RuleElement, ILiteral> elementsToLiteral = compileBody(rule.getBody());
        List<RuleElement> elements = IteratorUtils.toList(elementsToLiteral.keySet().iterator());

		List<IVariable> variables;

		if (elements.size() == 0)
			variables = new ArrayList<IVariable>();
		else {
			RuleElement lastElement = elements.get(elements.size() - 1);
			variables = lastElement.getOutputVariables();
		}

        ILiteral headLiteral = rule.getHead().get(0);
		IAtom headAtom = headLiteral.getAtom();
		ITuple headTuple = headAtom.getTuple();

		HeadSubstituter substituter;

		// We create a special head substituter for rules with head
		// equality, that establishes equivalence relation between terms in the
		// equivalent terms data-structure.
		if (RuleHeadEquality.hasRuleHeadEquality(rule)) {
			substituter = new RuleHeadEqualitySubstituter(variables, headTuple, elementsToLiteral,
					mEquivalentTerms, mConfiguration);
		} else {
			substituter = new HeadSubstituter(variables, headTuple, elementsToLiteral,
					mConfiguration);
		}

		elements.add(substituter);

		//Amir added
		for (int e = 0; e < elements.size() - 2; e++) 
		{
			RuleElement elt = elements.get(e);
			elt.FindIndices(elements);
		}
		
		
		return new CompiledRule(elements, headAtom.getPredicate(), headLiteral,
				mConfiguration);
	}

	/**
	 * Compile a query. No optimisations of any kind are attempted.
	 * 
	 * @param query The query to be compiled
	 * @return The compiled query, ready to be evaluated
	 * @throws EvaluationException If the query can not be compiled for any
	 *             reason.
	 */
	public ICompiledRule compile(IQuery query) throws EvaluationException {
		List<RuleElement> elements = IteratorUtils.toList(compileBody(query.getLiterals()).keySet().iterator());

		return new CompiledRule(elements, null, null, mConfiguration);
	}

	/**
	 * Compile a rule body (or query). The literals are compiled in the order
	 * given. However, if one literal can not be compiled, because one or more
	 * of its variables are not bound from the proceeding literal, then it is
	 * skipped an re-tried later.
	 * 
	 * @param bodyLiterals The list of literals to compile
	 * @return The compiled rule elements.
	 * @throws EvaluationException If a rule construct can not be compiled (e.g.
	 *             a built-in has constructed terms)
	 */
	private LinkedHashMap<RuleElement, ILiteral> compileBody(Collection<ILiteral> bodyLiterals)
			throws EvaluationException {
		List<ILiteral> literals = new ArrayList<ILiteral>(bodyLiterals);

        LinkedHashMap<RuleElement, ILiteral> elements = new LinkedHashMap<RuleElement, ILiteral>();

		List<IVariable> previousVariables = new ArrayList<IVariable>();

		while (elements.size() < bodyLiterals.size()) {
			EvaluationException lastException = null;

			boolean added = false;
			for (int l = 0; l < literals.size(); ++l) {
				ILiteral literal = literals.get(l);
				IAtom atom = literal.getAtom();
				boolean positive = literal.isPositive();

				RuleElement element;

				try {
					if (atom instanceof IBuiltinAtom) {
						IBuiltinAtom builtinAtom = (IBuiltinAtom) atom;

						boolean constructedTerms = false;
						for (ITerm term : atom.getTuple()) {
							if (term instanceof IConstructedTerm) {
								constructedTerms = true;
								break;
							}
						}

						if (constructedTerms)
							element = new BuiltinForConstructedTermArguments(
									previousVariables, builtinAtom, positive,
									mEquivalentTerms, mConfiguration);
						else
							element = new Builtin(atom.getPredicate(), atom.getTuple(),
                                    previousVariables, builtinAtom, positive,
                                    mEquivalentTerms, mConfiguration);
					} else {
						IPredicate predicate = atom.getPredicate();
						IRelation relation = mFacts.get(predicate);
						ITuple viewCriteria = atom.getTuple();

						if (positive) {
							if (previousVariables.size() == 0) {
								// First sub-goal
								element = new FirstSubgoal(predicate, relation,
										viewCriteria, mEquivalentTerms,
										mConfiguration);
							} else {
								element = new Joiner(previousVariables,
										predicate, relation, viewCriteria,
										mEquivalentTerms,
										mConfiguration.indexFactory,
										mConfiguration.relationFactory);
							}
						} else {
							// This *is* allowed to be the first literal for
							// rules such as:
							// p('a') :- not q('b')
							// or even:
							// p('a') :- not q(?X)
							element = new Differ(previousVariables, relation,
									viewCriteria, mEquivalentTerms,
									mConfiguration);
						}
					}
					previousVariables = element.getOutputVariables();

					elements.put(element, literals.get(l));

					literals.remove(l);
					added = true;
					break;
				} catch (EvaluationException e) {
					// Oh dear. Store the exception and try the next literal.
					lastException = e;
				}
			}
			if (!added) {
				// No more literals, so the last error really was serious.
				throw lastException;
			}
		}

		if (elements.size() > 0) {
			RuleElement lastElement = null;
            for (RuleElement element : elements.keySet()) {
                lastElement = element;
            }

            assert lastElement != null;
            RuleElement element = new EquivalenceResolver(lastElement.getOutputVariables(), mEquivalentTerms, mConfiguration);
			elements.put(element, null);
		}

		return elements;
	}

	/** The equivalent terms. */
	private IEquivalentTerms mEquivalentTerms;

	/** The knowledge-base facts used to attach to the compiled rule elements. */
	private final IFacts mFacts;

	/** The knowledge-base configuration. */
	private final Configuration mConfiguration;
}
