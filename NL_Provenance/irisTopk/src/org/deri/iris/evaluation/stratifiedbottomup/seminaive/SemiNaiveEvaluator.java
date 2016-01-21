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
package org.deri.iris.evaluation.stratifiedbottomup.seminaive;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deri.iris.Configuration;
import org.deri.iris.EvaluationException;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.evaluation.stratifiedbottomup.IRuleEvaluator;
import org.deri.iris.facts.Facts;
import org.deri.iris.facts.IFacts;
import org.deri.iris.rules.compiler.ICompiledRule;
import org.deri.iris.storage.IRelation;

import Top1.DerivationTree2;

/**
 * Semi-naive evaluation. see Ullman, Vol. 1
 */
public class SemiNaiveEvaluator implements IRuleEvaluator
{
	public Map<ITuple, DerivationTree2> evaluateRules( List<ICompiledRule> rules, IFacts facts, Configuration configuration ) throws EvaluationException
	{
		IFacts deltas = new Facts( configuration.relationFactory );
		Set<DerivationTree2> treesWaiting = new HashSet<DerivationTree2>();
		// One pass with simple evaluation to generate deltas
		// For each rule in the collection (stratum)
		for (final ICompiledRule rule : rules )
		{
			IRelation delta = rule.evaluate(true, treesWaiting);

			if( delta != null && delta.size() > 0 )
			{
				//IPredicate predicate = rule.headPredicate();
				//deltas.get( predicate ).addAll( delta );
				addAllToRightPredicate(deltas, delta);
			}
		}
		
		// Update the facts
		addAll( facts, deltas );
		
		// Now do iterative evaluation (semi-naive)
		boolean newTuples;
		for(;;)
		{
			newTuples = false;
			
			IFacts previousDeltas = deltas;
			
			deltas = new Facts( configuration.relationFactory );
			
			for (final ICompiledRule rule : rules )
			{
				//IPredicate predicate = rule.headPredicate();

				IRelation delta = rule.evaluateIteratively( previousDeltas, treesWaiting );
				
				// Remove already known tuples
				if( delta != null && delta.size() > 0 )
				{
					//IRelation programFacts = facts.get( predicate );
					//delta = removeDeducedTuples( delta, programFacts, configuration );
					delta = removeDeducedTuples( delta, facts, configuration );
				}

				if( delta != null && delta.size() > 0)
				{
					newTuples = true;
					//deltas.get( predicate ).addAll( delta );
					addAllToRightPredicate(deltas, delta);
				}
			}
			
			if( ! newTuples )
				break;
			// Iterate new tuples in dP[i] and add to program
			addAll( facts, deltas );
		}

        return null;
	}

	/**
	 * Add all the tuples from each of the deltas to the target facts.
	 * @param target The facts to be added to.
	 * @param deltas The facts to be added.
	 */
	private static void addAll( IFacts target, IFacts deltas )
	{
		for( IPredicate predicate : deltas.getPredicates() )
			target.get( predicate ).addAll( deltas.get( predicate ) );
	}

	/**
	 * Helper to remove tuples from a delta that are already known or computed.
	 * @param predicate The predicate identifying the relation.
	 * @param delta The deltas produced by the last round of evaluation.
	 * @param programFacts The already known or computed facts.
	 * @return
	 */
	private static IRelation removeDeducedTuples(IRelation delta, IFacts facts, Configuration configuration )
	{
		// If there is nothing to take away from, or just nothing to take-away...
		/*
		if( delta.size() == 0 || programFacts.size() == 0 )
			return delta;
		*/
		
		if( delta.size() == 0 )
			return delta;
		
		IRelation result = configuration.relationFactory.createRelation();
		
		for( int t = 0; t < delta.size(); ++ t )
		{
			ITuple tuple = delta.get( t );
			
			//Amir added
			IRelation programFacts = facts.get( tuple.getTrees().iterator().next().getRulePointer().headPredicate() );
			if( programFacts.size() == 0 )
				return delta;
			////////////
			
			if( ! programFacts.contains( tuple ) )
			{
				result.add( tuple );
			}
		}
		
		return result;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: addAllToRightPredicate																					
	/** Description: Fix the "add to facts" stage in order to accommodate the top-1 algorithm. 									
	/*************************************************************************************************************/
	
	private static void addAllToRightPredicate( IFacts deltas, IRelation delta )
	{
		for ( int i = 0; i < delta.size(); i++ ) 
		{
			ITuple tuple = delta.get(i);
			IPredicate predicate = tuple.getTrees().iterator().next().getRulePointer().headPredicate();
			deltas.get(predicate).add(tuple);
		}
	}
}
