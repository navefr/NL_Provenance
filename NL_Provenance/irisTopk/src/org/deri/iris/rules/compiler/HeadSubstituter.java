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
import Top1.EquationTopK2;
import TopKBasics.KeyMap2;
import org.deri.iris.Configuration;
import org.deri.iris.EvaluationException;
import org.deri.iris.RuleUnsafeException;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.IStringTerm;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.api.terms.concrete.IIntegerTerm;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.deri.iris.utils.TermMatchingAndSubstitution;

import java.util.*;

/**
 * A compiled rule element representing the substitution of variable bindings in to the rule head.
 */
public class HeadSubstituter extends RuleElement
{
	/**
	 * Constructor.
	 * @param variables The variables from the rule body.
	 * @param headTuple The tuple from the rule head.
	 * @throws EvaluationException If unbound variables occur.
	 */
    public HeadSubstituter(List<IVariable> variables, ITuple headTuple, LinkedHashMap<RuleElement, ILiteral> elementsToLiteral,
                            Configuration configuration ) throws EvaluationException {
		assert variables != null;
		assert headTuple != null;
		assert configuration != null;
        assert elementsToLiteral != null;
		
		mConfiguration = configuration;
		
		mHeadTuple = headTuple;

        mElementsToLiteral = elementsToLiteral;

		// Work out the indices of variables in substitution order
		List<IVariable> variablesToSubstitute = TermMatchingAndSubstitution.getVariables( mHeadTuple, false );
		mIndices = new int[ variablesToSubstitute.size() ];

		int i = 0;
		for( IVariable variable : variablesToSubstitute )
		{
			int index = variables.indexOf( variable );
			if( index < 0 )
				throw new RuleUnsafeException( "Unbound variable in rule head: " + variable );
			mIndices[ i++ ] = index;
		}
	}
	
	@Override
	public IRelation process( CompiledRule r, IRelation inputRelation, Set<DerivationTree2> treesWaiting )
	{
		assert inputRelation != null;

		IRelation result = mConfiguration.relationFactory.createRelation();
		
		int [][] indices = new int[r.getElements().size() - 2][];
		for( int i = 0; i < r.getElements().size() - 2; ++i )
		{
			RuleElement element = r.getElements().get( i );
			indices[i] = element.getIndices();
		}
		
		
		for( int i = 0; i < inputRelation.size(); ++i )
		{
			ITuple inputTuple = inputRelation.get( i );
			
			ITuple outputTuple = TermMatchingAndSubstitution.substituteVariablesInToTuple( mHeadTuple, inputTuple, mIndices );
			
			result.add( outputTuple );
			
			//Amir added
			ITuple headTuple = getHead(r, outputTuple);
			if (false == headTuple.isTop1Found()) 
			{
				getAtomBody(r, inputTuple, indices, treesWaiting, headTuple);
			}
			
		}
		
		return result;
	}
	
	
	/*************************************************************************************************************/
	/** Title: getHead																					
	/** Description: Get the head of the rule - the fact that is derived. 									
	/*************************************************************************************************************/
	
	public ITuple getHead (CompiledRule r, ITuple tuple)
	{
		ITuple head;
		String name = r.getPredicate().getPredicateSymbol();
		boolean added = KeyMap2.getInstance().StringUpdate( name, tuple );
		
		if ( false == added )
		{
			head = KeyMap2.getInstance().Get(name, tuple);
		}
		
		else
		{
			head = tuple;
		}
		
		head.setCurRuleWeight(r.getWeight());
		return head;
	}
	
	
	/*************************************************************************************************************/
	/** Title: getAtomBody																					
	/** Description: Get the body of the rule and find the top-1 tree candidate for this fact. 									
	/*************************************************************************************************************/
	
	public void getAtomBody (CompiledRule r, ITuple tuple, int [][] indices, Set<DerivationTree2> treesWaiting, ITuple head)
	{
		List<ITuple> body = new ArrayList<ITuple>();
		List<DerivationTree2.Condition> conditions = new ArrayList<DerivationTree2.Condition>();
		List<ILiteral> literals = new ArrayList<ILiteral>();
		boolean added = false;

        Map<IVariable, Collection<RuleElement>> varToElementsMap = new HashMap<IVariable, Collection<RuleElement>>();

		for( int i = 0; i < r.getElements().size() - 2; ++i )
		{
			RuleElement element = r.getElements().get( i );

			String name = element.getPredicate().getPredicateSymbol();//.intern();
			ITuple relevantTuple = TupleByIndices(indices[i], tuple, element);
            relevantTuple.setPredicate(name);
			added = KeyMap2.getInstance().StringUpdate(name, relevantTuple);
			
			if ( false == added )
			{
				/*
				relevantTuple = KeyMap2.getInstance().Get(name, relevantTuple);
				
				if (relevantTuple.equals(head)) 
				{
					relevantTuple.setFact(true);
				}
				
				body.add( relevantTuple );
				*/
				body.add( KeyMap2.getInstance().Get(name, relevantTuple) );
			}
			
			else //can only happen for facts in the first iteration
			{
				relevantTuple.setFact(true);
				body.add(relevantTuple);
			}

            if (element instanceof Builtin) {
                conditions.add(new DerivationTree2.Condition(name, element.getView()));
            } else{
                conditions.add(null);
            }

            literals.add(mElementsToLiteral.get(element));

            if (!(element instanceof Builtin)) {
                for (IVariable variable : element.getView().getAllVariables()) {
                    Collection<RuleElement> elements = varToElementsMap.get(variable);
                    if (elements == null) {
                        elements = new ArrayList<>();
                        varToElementsMap.put(variable, elements);
                    }
                    elements.add(element);
                }
            }
        }

        addJoinConditions(tuple, body, conditions, literals, varToElementsMap);


        HandleTop1Scenario(head, body, conditions, literals, r, treesWaiting);
	}

    private void addJoinConditions(ITuple tuple, List<ITuple> body, List<DerivationTree2.Condition> conditions,
                                   List<ILiteral> literals, Map<IVariable, Collection<RuleElement>> varToElementsMap) {
        for (Map.Entry<IVariable, Collection<RuleElement>> varToElements : varToElementsMap.entrySet()) {
            IVariable joinVariable = varToElements.getKey();
            Collection<RuleElement> elements = varToElements.getValue();
            if (elements.size() > 1) {


                Collection<ITerm> terms = new ArrayList<ITerm>();
                for (RuleElement element : elements) {
                    ITuple currTuple = TupleByIndices(element.getIndices(), tuple, element);
                    terms.addAll(Arrays.asList(currTuple.getTerms()));
                }

                ITuple relevantTuple = Factory.BASIC.createTuple(terms.toArray(new ITerm[terms.size()]));
                relevantTuple.setFact(true);
                body.add(relevantTuple);
                conditions.add(new DerivationTree2.Condition("JOIN", Factory.BASIC.createTuple(joinVariable)));
                literals.add(null);
            }
        }
    }


    /*************************************************************************************************************/
	/** Title: HandleTop1Scenario																					
	/** Description: Uses UpdateTop1WhileSemiNaive method to find the candidate for the top-1 tree and sets it as
	/**  this fact's tree. 									
	/*************************************************************************************************************/
	
	public void HandleTop1Scenario (ITuple ihead, List<ITuple> ibody, List<DerivationTree2.Condition> conditions, List<ILiteral> literals,
                                    CompiledRule r, Set<DerivationTree2> treesWaiting)
	{
        List<DerivationTree2> derivationTrees = EquationTopK2.UpdateWhileSemiNaive(ihead, ibody, conditions, literals);
        for (DerivationTree2 derivationTree : derivationTrees) {
            derivationTree.setRulePointer(r);
            derivationTree.setRulePointer(r);
            derivationTree.setLiteral(r.getLiteral());
        }
		treesWaiting.addAll(derivationTrees);
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: TupleByIndices																					
	/** Description: Finds the tuple that fits the rule element. Divides it into two cases: 
	/** 1. having a const. value in the tuple (happens when using a pattern) 
	/** 2. there is no const. value in the tuple.
	/*************************************************************************************************************/
	
	public ITuple TupleByIndices (int [] indices, ITuple tuple, RuleElement element)
	{
		ITerm[] terms = new ITerm[element.getView().size()];
		int index = 0;

		if (indices.length < element.getView().size()) //there is a const value in the tuple
		{
			for (int i = 0; i < element.getView().size(); ++i) 
			{
				ITerm term = element.getView().get(i);
				if (term instanceof IStringTerm || term instanceof IIntegerTerm)
				{
					terms[i] = term;//index++
				}
			}
			
			//index = 0;
			for (int i = 0; i < terms.length; ++i)
			{
				if (null == terms[i])
				{
					terms[i] = tuple.get(indices[0]);
				}
			}
		}
		
		else
		{
			for (int i : indices) 
			{
                if (i != -1) {
                    terms[index++] = tuple.get(i);
                }
			}
		}

		return Factory.BASIC.createTuple(terms);
	}
	
	
	
	//Amir added
	/*public String StringTupleByIndices (int [] indices, ITuple tuple)
	{
		String retVal= "(";
		for (int i : indices) 
		{
			retVal += tuple.get(i).toString().intern() + ",";
		}
		
		retVal = retVal.substring(0, retVal.length() - 1) + ")";
		return retVal.intern();
	}*/
	/*
	public List<ITuple> getHeads ()
	{
		return heads;
	}
	
	public List<List<ITuple>> getBodies()
	{
		return bodies;
	}
	
	public void clearHeads ()
	{
		if (heads != null) 
		{
			heads.clear();
		}
	}
	*/
	//Amir added
	public int[] getIndices() 
	{
		return mIndices;
	}
	
	
	public ITuple getView()
	{
		return mHeadTuple;
	}

	/** The rule head tuple. */
	protected final ITuple mHeadTuple;
	
	/** The indices of variables in substitution order. */
	protected final int[] mIndices;

	/** The knowledge-base's configuration object. */
	protected final Configuration mConfiguration;

    /** The mapping from an element to the literal that caused his creation. */
    protected final LinkedHashMap<RuleElement, ILiteral> mElementsToLiteral;
	
	//private List<ITuple> heads;
	//private List<List<ITuple>> bodies;
}
