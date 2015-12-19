/*
 * Integrated Rule Inference System (IRIS):
 * An extensible rule inference system for datalog with extensions.
 * 
 * Copyright (C) 2009 Semantic Technology Institute (STI) Innsbruck, 
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.deri.iris.Configuration;
import org.deri.iris.EvaluationException;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.deri.iris.utils.TermMatchingAndSubstitution;
import org.deri.iris.utils.equivalence.IEquivalentTerms;

import Top1.DerivationTree2;
import TopKBasics.KeyMap2;

/**
 * This rule element creates all possible combinations of an input relation
 * using the equivalent terms, and adds these combinations to the output
 * relation.
 * 
 * @author Adrian Marte
 */
public class EquivalenceResolver extends RuleElement {

	private final IEquivalentTerms equivalentTerms;

	private final Configuration configuration;
	
	//private List<Body> bodies;
	private  List<List<ITuple>> bodies;

	public EquivalenceResolver(List<IVariable> inputVariables,
			IEquivalentTerms equivalentTerms, Configuration configuration) {
		this.equivalentTerms = equivalentTerms;
		this.configuration = configuration;

		// We do not make any changes to the input/output variables.
		mOutputVariables = inputVariables;
	}

	@Override
	public IRelation process(CompiledRule r, IRelation input, Set<DerivationTree2> treesWaiting) throws EvaluationException {
		// Create the output relation.
		IRelation relation = configuration.relationFactory.createRelation();
		
		//long startTime = System.currentTimeMillis();
		for (int i = 0; i < input.size(); i++) 
		{
			ITuple tuple = input.get(i);
			
			// Create all combinations using the equivalent terms.
			List<ITuple> combinations = Utils.createAllCombinations(tuple,
					equivalentTerms);

			// Add combinations to output relation.
			for (ITuple combination : combinations) 
			{
				relation.add(combination);
			}
		}
		/*long endTime = System.currentTimeMillis();
		long durationFullProv = (endTime - startTime);
		if (durationFullProv > 5000) System.out.println("durartion process() of bodies: " + durationFullProv);*/
		return relation;
	}
	
	
	public List<List<ITuple>> getBodies()
	{
		return bodies;
	}
	
	
	
	public void clearBodies ()
	{
		if (bodies != null) 
		{
			bodies.clear();
		}
	}

}
