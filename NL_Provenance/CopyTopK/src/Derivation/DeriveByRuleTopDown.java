package Derivation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import Basics.*;

public class DeriveByRuleTopDown extends DeriveByRule
{

	Map<String, HashSet<Proton>> relevantsFound = new HashMap<String, HashSet<Proton>>();
	
	Set<Atom> atomsToDerive;
	
	public DeriveByRuleTopDown (Rule ir, Program ip, Set<Atom> iprov)
	{
		super(ir, ip);
		this.atomsToDerive = iprov;
	}
	
	
	public DeriveByRuleTopDown (Rule ir, Program ip)
	{
		super(ir, ip);
	}
	
	
	/*************************************************************************************************************/
	/** Title: FindDerivationsForRule																				
	/** Description: Finds all Derivations for the rule according to db 				
	/*************************************************************************************************************/
	
	public void FindDerivationsForRuleTopDown () 
	{
		List<Rule> instRuleVector = new CopyOnWriteArrayList<Rule>();
		instRuleVector.add(new Rule(this.r));
		
		while (false == instRuleVector.isEmpty())
		{	
			instRuleVector = InstIterationTopDown(instRuleVector);
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: FindDerivationsForRule																				
	/** Description: Finds all Derivations for the rule according to vhildren's db For Intersection				
	/*************************************************************************************************************/
	
	public void FindDerivationsForRuleTopDownForIntersection (Map<String, Vector<Atom>> childrenDB) 
	{
		List<Rule> instRuleVector = new CopyOnWriteArrayList<Rule>();
		instRuleVector.add(new Rule(this.r));
		
		if (true == IsFullyInstList(instRuleVector)) 
		{
			this.derivations.addAll(instRuleVector);
		}
		
		while (false == IsFullyInstList(instRuleVector))
		{			
			instRuleVector = InstIterationTopDownForIntersection(instRuleVector, childrenDB);
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: InstIterationTopDown																				
	/** Description: One iteration to update the instRuleVector in partly inst rules  				
	/*************************************************************************************************************/
	
	public List<Rule> InstIterationTopDown (List<Rule> instRuleVector)
	{
		HashSet<Proton> optionsForProton;
		for (Rule rule : instRuleVector)
		{
			if (false == rule.isFullyInst()) 
			{
				Set<Proton> varsInRule = GetListOfVarsInRule(rule);
				for (Proton var : varsInRule) 
				{
					optionsForProton = new HashSet<Proton>(FindRelevantConst(var));
					if (null != optionsForProton) 
					{
						for (Proton c : optionsForProton) 
						{
							Rule copy = new Rule(rule);
							copy.SwapVarInRule(var, c);
							if (true == copy.isFullyInst())
							{
								if (true == IsRuleDerivationPhysable(copy)) 
								{
									copy.getBody().setRuleUsed(this.r);
									Provenance.getInstance().AddBodyToProvTopDown(copy.getHead(), copy.getBody(), this.atomsToDerive);
								}
							}
							
							else
							{
								instRuleVector.add(copy);
							}
						}
					}
				}
			}
			
			else if (true == IsRuleDerivationPhysable(rule))
			{
				rule.getBody().setRuleUsed(this.r);
				Provenance.getInstance().AddBodyToProvTopDown(rule.getHead(), rule.getBody(), this.atomsToDerive);
			}
			
			instRuleVector.remove(rule);
		}
		
		return instRuleVector;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: FindRelevantConst																				
	/** Description: One iteration to update the instRuleVector in partly inst rules  				
	/*************************************************************************************************************/
	
	public HashSet<Proton> FindRelevantConst (Proton p)
	{
		HashSet<Proton> optionsForProton;
		if (true == relevantsFound.containsKey(p.getCategory())) 
		{
			optionsForProton = relevantsFound.get(p.getCategory());
		}
		else
		{
			optionsForProton = MemDB.getInstance().GetAllConstantsInCategory(p);;//DB.getInstance().GetAllConstantsInCategory(p);
			relevantsFound.put(p.getCategory(), optionsForProton);
		}
		
		return optionsForProton;
	}
	
	
	
	
	/*************************************************************************************************************/
	/** Title: InstIterationTopDownForIntersection																				
	/** Description: One iteration to update the instRuleVector in partly inst rules For Intersection 				
	/*************************************************************************************************************/
	
	public List<Rule> InstIterationTopDownForIntersection (List<Rule> instRuleVector, Map<String, Vector<Atom>> childrenDB)
	{
		Vector<Proton> optionsForProton = null;
		for (Rule rule : instRuleVector)
		{
			Set<Proton> varsInRule = GetListOfVarsInRule(rule);
			for (Proton var : varsInRule) 
			{
				optionsForProton = GetAllConstantsInCategory(var, childrenDB);
				if (null != optionsForProton) 
				{
					for (Proton c : optionsForProton) 
					{
						Rule copy = new Rule(rule);
						copy.SwapVarInRule(var, c);
						if (copy.isFullyInst()) 
						{	
							this.derivations.add(copy);
						}
						
						else
						{
							instRuleVector.add(copy);
						}
					}
				}
			}
			
			instRuleVector.remove(rule);
		}
		
		return instRuleVector;
	}
	
	
	
	
	/*************************************************************************************************************/
	/** Title: GetListOfVarsInRule																				
	/** Description: Gets a List Of Vars In a Rule  				
	/*************************************************************************************************************/
	
	public Set<Proton> GetListOfVarsInRule (Rule rule)
	{
		Set<Proton> varsInRule = new HashSet<Proton> ();
		for (Atom atom : rule.getBody().getAtoms()) 
		{
			if (false == atom.isFullyInst()) 
			{
				for (Proton p : atom.getParams()) 
				{
					if (p instanceof Var)
					{
						varsInRule.add(p);
					}
				}
			}
		}
		
		return varsInRule;
	}
	
	
	/*************************************************************************************************************/
	/** Title: IsFullyInstList																				
	/** Description: Check if the list of rules contains only fully inst rules  				
	/*************************************************************************************************************/
	
	public boolean IsFullyInstList (List<Rule> instRuleVector)
	{
		boolean fullyInstList = true;
		for (Rule rule : instRuleVector) 
		{
			if (false == rule.isFullyInst()) 
			{
				fullyInstList = false;
				break;
			}
		}
		
		return fullyInstList;
	}
	
	
	
	/*************************************************************************************************************/
    /** Title: GetAllConstantsInCategory                                                                                                                                                            
    /** Description: Gets all the atoms in the DB by proton category                        
    /*************************************************************************************************************/
    
    public Vector<Proton> GetAllConstantsInCategory (Proton p, Map<String, Vector<Atom>> childrenDB)
    {
            Vector<Proton> relevants = new Vector<Proton>();
            for (String key : childrenDB.keySet()) 
            {
                    for (Atom atom : childrenDB.get(key)) 
                    {
                            for (Proton proton : atom.getParams()) 
                            {
                                    if (true == proton.getCategory().equals(p.getCategory()) && false == relevants.contains(proton)) 
                                    {
                                            relevants.add(proton);
                                    }
                            }
                    }
            }
            
            return relevants;
    }
    
    
    
    /*************************************************************************************************************/
	/** Title: IsNotDeriveble																				
	/** Description: Checks if atom cannot be derived  				
	/*************************************************************************************************************/
	
	/*public boolean IsNotDerivable (Atom atom)
	{
		return !MemDB.getInstance().ContainedInTable(atom) && atom.IsAtomRelationEdb(this.p);//!DB.getInstance().ContainedInTable(atom)
	}*/
	
	
	
	/*************************************************************************************************************/
	/** Title: IsRuleDerivationPhysable																				
	/** Description: Checks if rule can have stable derivation  				
	/*************************************************************************************************************/
	
	public boolean IsRuleDerivationPhysable (Rule rule)
	{
		boolean partOfDB;
		boolean retVal = true;
		boolean headIsStable = true;
		for (Atom bodyAtom : rule.getBody().getAtoms()) 
		{
			partOfDB = MemDB.getInstance().ContainedInTable(bodyAtom);//DB.getInstance().ContainedInTable(bodyAtom);
			if (false == partOfDB)  
			{
				headIsStable = false;
				if (bodyAtom.IsAtomRelationEdb(this.p))
				{
					retVal = false;
					break;
				}
			}
			
			else 
			{
				bodyAtom.setFact(true);
				bodyAtom.setStable(true);
			}
		}
		
		if (true == headIsStable) //all body atoms are facts in DB
		{
			KeyMap.getInstance().Get(rule.getHead()).setStable(true);
			//MemDB.getInstance().Update(rule.getHead());//DB.getInstance().Update(rule.getHead());
		}
		
		return retVal;
	}
}
