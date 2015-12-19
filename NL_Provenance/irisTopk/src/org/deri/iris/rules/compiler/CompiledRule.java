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

import Top1.DerivationTree2;
import org.deri.iris.Configuration;
import org.deri.iris.EvaluationException;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.factory.Factory;
import org.deri.iris.facts.IFacts;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.simple.SimpleRelationFactory;

import java.util.*;

/**
 * A compiled rule.
 */
public class CompiledRule implements ICompiledRule
{

    public Set<DerivationTree2> evaluatedProvenanceTrees = new HashSet<>();

	/**
	 * Constructor.
	 * @param elements The rule elements produced by the rule compiler.
	 * @param headPredicate The head predicate of the original rule.
	 */
	public CompiledRule( List<RuleElement> elements, IPredicate headPredicate, ILiteral headLiteral, Configuration configuration/*, double weight*/ )
	{
		assert elements.size() > 0;
		assert configuration != null;
		
		mConfiguration = configuration;
		
		mHeadPredicate = headPredicate;
		mHeadLiteral = headLiteral;

		mElements = elements;
		
		//Amir added
		mWeight = 1;//Math.random();// temporary...
	}

	/**
	 * Evaluate the rule.
	 * Each element is called in turn to produce tuples to pass on to the next rule element.
	 * If any rule element outputs an empty relation, then stop.
	 * @throws EvaluationException 
	 */
	public IRelation evaluate(boolean firstIteration, Set<DerivationTree2> treesWaiting) throws EvaluationException
	{
		// The first literal receives the starting relation (which has one zero length tuple in it). */
		IRelation output = mStartingRelation;
	
		//int numOfElts = mElements.size();
		for( RuleElement element : mElements )
		{
			output = element.process( this, output, treesWaiting );
			
			// Must always get some output relation, even if it is empty.
			assert output != null;
			
			// All literals are conjunctive, so if any literal produces no results,
			// then the whole rule produces no results.
			if( output.size() == 0 )
				break;
		}

        for (DerivationTree2 derivationTree : treesWaiting) {
            evaluatedProvenanceTrees.add(derivationTree);
        }

        //treesWaiting = FindTop1(firstIteration, treesWaiting);
		
		//System.out.println("size of keyMap: " + KeyMap2.getInstance().Size ());;
		//System.out.println("size of treesWaiting: " + treesWaiting.size ());;
		//KeyMap2.getInstance().ChackAllAtomsHaveTop1();
		return getTuplesForDB(treesWaiting);
		//return output;
	}
	
	public IRelation evaluateIteratively( IFacts deltas, Set<DerivationTree2> treesWaiting ) throws EvaluationException
	{
		IRelation union = mConfiguration.relationFactory.createRelation();
	
		/*
		for each literal (rule element) for which there exists a delta substitution
			substitute the rule element with the delta
			evaluate the whole rule
			store the results
		combine all the results and return
		*/
		
		for( int r = 0; r < mElements.size(); ++r )
		{
			RuleElement original = mElements.get( r );
			
			RuleElement substitution = original.getDeltaSubstitution( deltas );
			
			if( substitution != null )
			{
				//Amir added
				substitution.setIndices(original.getIndices());
				
				mElements.set( r, substitution );
	
				// Now just evaluate the modified rule
				IRelation output = evaluate(false, treesWaiting);
				
				for( int t = 0; t < output.size(); ++t )
					union.add( output.get( t ) );
				
				// Put the original rule element back the way it was
				mElements.set( r, original );
			}
		}
		
		return union;
	}
	
	
	
	public IPredicate headPredicate()
    {
	    return mHeadPredicate;
    }

	public double getWeight()
	{
		return mWeight;
	}
	
	public void setWeight(double w)
	{
		mWeight = w;
	}
	
	
	public List<IVariable> getVariablesBindings()
	{
		if( mElements.size() > 0 )
			return mElements.get( mElements.size() - 1 ).getOutputVariables();
		else
			return new ArrayList<IVariable>();
	}
	
	//Amir added
	public List<RuleElement> getElements()
	{
		return mElements;
	}
	
	
	//Amir added
	public IPredicate getPredicate()
	{
		return mHeadPredicate;
	}

    //Nave added
    public ILiteral getLiteral()
    {
        return mHeadLiteral;
    }
	
	
	//Amir added
	/*
	public Set<DerivationTree2> FindTop1 (boolean firstIteration, Set<DerivationTree2> treesWaiting) throws EvaluationException
	{
		//long startTime = System.currentTimeMillis();
		List<ITuple> heads = ((HeadSubstituter)mElements.get(mElements.size() - 1)).getHeads();
		List<List<ITuple>> bodies = ((HeadSubstituter)mElements.get(mElements.size() - 1)).getBodies();
		
		//System.out.println("size of treesWaiting before iteration: " + treesWaiting.size());
		if (heads != null) 
		{
			for (int i = 0; i < heads.size(); i++) 
			{
				if (false == heads.get( i ).isTop1Found()) 
				{
					HandleTop1Scenario(heads.get( i ), bodies.get( i ), treesWaiting);
				}
			}
			
			bodies.clear();
			heads.clear();
		}
		
		//System.out.println("	size of treesWaiting after  iteration: " + treesWaiting.size());
		else
		{
			System.out.println("nothing was derived");
		}
		long endTime = System.currentTimeMillis();
		long durationFullProv = (endTime - startTime);
		if (durationFullProv > 5000) System.out.println("durartion FindTop1(): " + durationFullProv);
		return treesWaiting;
	}
	
	
	//Amir added
	public void HandleTop1Scenario (ITuple ihead, List<ITuple> ibody, Set<DerivationTree2> treesWaiting)
	{
		if (false == ihead.isTop1Found()) 
		{
			EquationTopK2.UpdateTop1WhileSemiNaive(ihead, ibody);
			ihead.getTree().setRulePointer(this);
			treesWaiting.add( ihead.getTree() );
		//}
		
		for (ITuple bodyAtom : ibody) 
		{
			if (true == bodyAtom.isFact() && false == bodyAtom.isTopKUpdated()) 
			{
				EquationTopK2.SetTreeForFact(bodyAtom);
			}
		}
	}
	*/
	
	private IRelation getTuplesForDB (Set<DerivationTree2> treesWaiting)
	{
		//long startTime = System.currentTimeMillis();
		List<DerivationTree2> treeList = new ArrayList<DerivationTree2>(treesWaiting);
		Collections.sort(treeList);
		double bestWeight = treeList.isEmpty() ? 0 : treeList.get(0).getWeight();
		IRelation forDB = mConfiguration.relationFactory.createRelation();
		int startIndex = 0;
		
		for (DerivationTree2 tree : treeList)
		{
			if ( tree.getWeight() == bestWeight && false == tree.getDerivedFact().isTop1Found() )
			{
				tree.getDerivedFact().setTop1Found(true);
				forDB.add( tree.getDerivedFact() );
			}
			
			else if ( tree.getWeight() < bestWeight )
			{
				startIndex = treeList.indexOf(tree);
				break;
			}
		}
		
		treesWaiting.clear();
		
		if (0 < startIndex)
			treesWaiting.addAll(treeList.subList(startIndex, treeList.size()));
		/*
		if (0 == startIndex) 
			treesWaiting.clear();
		else
		{
			treesWaiting.clear();
			treesWaiting.addAll(treeList.subList(startIndex, treeList.size()));
		}
		*/
		/*long endTime = System.currentTimeMillis();
		long durationFullProv = (endTime - startTime);
		if (durationFullProv > 5000) System.out.println("durartion getTuplesForDB(): " + durationFullProv);*/
		return forDB;
	}
	
	
	/** The starting relation for evaluating each sub-goal. */
	private static final IRelation mStartingRelation = new SimpleRelationFactory().createRelation();

	static
	{
		// Start the evaluation with a single, zero-length tuple.
		mStartingRelation.add( Factory.BASIC.createTuple() );
	}
	
	/** The rule elements in order. */
	private final List<RuleElement> mElements;
	
	/** The head predicate. */ 
	private final IPredicate mHeadPredicate;

    /** The head predicate. */
    private final ILiteral mHeadLiteral;
	
	private final Configuration mConfiguration;
	
	//Amir added
	private double mWeight;
}
