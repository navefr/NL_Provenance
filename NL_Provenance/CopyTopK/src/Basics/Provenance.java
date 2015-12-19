package Basics;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class Provenance 
{
	public static Provenance  prov = null;
	
	Map<Atom, HashSet<Body>> provenance;
	
	
	public Provenance () 
	{
		provenance = new HashMap<Atom, HashSet<Body>>();
	}
	
	
	public static Provenance getInstance() 
	{
		if(prov == null) 
		{
			prov = new Provenance();
		}
		
		return prov;
	}
	
	
	
	
	public Map<Atom, HashSet<Body>> getProvenance() 
	{
		return provenance;
	}


	public void setProvenance(Map<Atom, HashSet<Body>> provenance) 
	{
		this.provenance = provenance;
	}


	/*************************************************************************************************************/
	/** Title: AddRuleToProv																				
	/** Description: 				
	/*************************************************************************************************************/
	
	public void AddRuleToProv (Rule rule, int level)
	{
		/*if (false == this.provenance.containsKey(rule.getHead()))
		{
			FoolsUpdate(rule.getHead());
			KeyMap.getInstance().FoolsUpdate(rule.getHead());
		}
		
		if (false == head.didFindTop1())
		{
			Body pointerBody = new Body();
			pointerBody.setDerivedInlevel(level);
			for (Atom atom : rule.getBody().getAtoms())
			{
				if (true == atom.isFact())
				{
					Update(atom);
					KeyMap.getInstance().Update(atom);
				}
				
				pointerBody.getAtoms().add(KeyMap.getInstance().getMap().get(atom.toString()));
			}
			
			pointerBody.setRuleUsed(rule.getBody().getRuleUsed());
			
			this.provenance.get(rule.getHead()).add(pointerBody);
		}*/
		
		Atom head = KeyMap.getInstance().Get(rule.getHead());
		if (false == this.provenance.containsKey(rule.getHead()))
		{
			FoolsUpdate(rule.getHead());
			KeyMap.getInstance().FoolsUpdate(rule.getHead());
			AddBodyToProvBottomUp(rule.getHead(), rule.getBody(), level);
		}
		
		else if (false == head.didFindTop1())
		{
			AddBodyToProvBottomUp(head, rule.getBody(), level);
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: AddBodyToProvBottomUp																				
	/** Description: 				
	/*************************************************************************************************************/
	
	public void AddBodyToProvBottomUp (Atom key, Body body, int level)
	{
		//System.out.println("size of prov of atom " + key + " before is " + this.provenance.get(key).size());
		Body pointerBody = new Body();
		pointerBody.setDerivedInlevel(level);
		pointerBody.setRuleUsed(body.getRuleUsed());
		for (Atom atom : body.getAtoms())
		{
			if (true == atom.isFact())
			{
				Update(atom);
				KeyMap.getInstance().Update(atom);
			}
			
			pointerBody.getAtoms().add(KeyMap.getInstance().getMap().get(atom.toString()));
		}
		
		this.provenance.get(key).add(pointerBody);
		//System.out.println(this.provenance.get(key));
		//System.out.println("size of prov of atom " + key + " after is " + this.provenance.get(key).size());
	}
	
	
	
	
	/*************************************************************************************************************/
	/** Title: AddBodyToProvTopDown																				
	/** Description: 				
	/*************************************************************************************************************/
	
	public void AddBodyToProvTopDown (Atom key, Body body, Set<Atom> needToDerive)
	{
		Body pointerBody = new Body();
		for (Atom atom : body.getAtoms()) 
		{
			Update(atom, needToDerive); //update pointers for atoms
			KeyMap.getInstance().Update(atom);

			pointerBody.getAtoms().add(KeyMap.getInstance().Get(atom)); //add atom to pointer body
			
			KeyMap.getInstance().Get(atom).setFact(atom.isFact()); //update atoms fields
			KeyMap.getInstance().Get(atom).setStable(atom.isStable());
		}

		this.provenance.get(key).add(pointerBody);
		pointerBody.setRuleUsed(body.getRuleUsed());
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: GetPointerBody																				
	/** Description: Finds the pointer body in the prov. given a key and a body				
	/*************************************************************************************************************/
	
	public Body GetPointerBody (Atom key, Body body)
	{
		Body retVal = null;
		for (Body b : this.provenance.get(key)) 
		{
			if (b.IdenticalAtoms(body)) 
			{
				retVal = b;
				break;
			}
		}
		
		return retVal;
	}
	
	
	/*************************************************************************************************************/
	/** Title: Update																				
	/** Description: 				
	/*************************************************************************************************************/
	
	public void Update (Atom atom)
	{
		if (false == this.provenance.containsKey(atom)) 
		{
			this.provenance.put(atom, new HashSet<Body>());
		}
	}
	
	
	
	/*************************************************************************************************************/
    /** Title: FoolsUpdate                                                                                                                                                               
    /** Description: add new atom to keyMap without checking whether it exists already                    
    /*************************************************************************************************************/
    
    public void FoolsUpdate (Atom atom)
    {
    	this.provenance.put(atom, new HashSet<Body>());
    }
	
	
	/*************************************************************************************************************/
	/** Title: Update																				
	/** Description: 				
	/*************************************************************************************************************/
	
	public void Update (Atom atom, Set<Atom> needToDerive)
	{
		if (false == this.provenance.containsKey(atom)) 
		{
			this.provenance.put(atom, new HashSet<Body>());
			if (false == atom.isFact()) 
			{
				needToDerive.add(atom);
			}
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: Update																				
	/** Description: 				
	/*************************************************************************************************************/
	
	public void UpdateDerivs (Atom atom, HashSet<Body> derivs)
	{
		this.provenance.put(atom, derivs);
	}
	
	
	/*************************************************************************************************************/
	/** Title: Update																				
	/** Description: 				
	/*************************************************************************************************************/
	
	public void AddDeriv (Atom atom, Body body)
	{
		if (false == this.provenance.get(atom).contains(body)) 
		{
			this.provenance.get(atom).add(body);
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: Get																				
	/** Description: 				
	/*************************************************************************************************************/
	
	public HashSet<Body> Get (Atom key)
	{
		return this.provenance.get(key);
	}
	
	
	/*************************************************************************************************************/
	/** Title: KeySet																				
	/** Description: 				
	/*************************************************************************************************************/
	
	public Set<Atom> KeySet ()
	{
		return this.provenance.keySet();
	}
	
	
	/*************************************************************************************************************/
    /** Title: Values                                                                                                                                                               
    /** Description:                      
    /*************************************************************************************************************/
    
    public Collection<HashSet<Body>> Values ()
    {
    	return this.provenance.values();
    }
	
	
	/*************************************************************************************************************/
	/** Title: Reset																				
	/** Description: Empty all provenance info			
	/*************************************************************************************************************/

	public void Reset ()
	{
		this.provenance.clear();
		prov = new Provenance();
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: GetProvSize																				
	/** Description: 				
	/*************************************************************************************************************/
	
	public int GetProvSize ()
	{
		int size = 0;
		for (HashSet<Body> set : Provenance.getInstance().Values()) 
		{
			size += set.size() + 1;
		}
		
		return size;
	}
	
	
	/*************************************************************************************************************/
	/** Title: Print																				
	/** Description: 				
	/*************************************************************************************************************/
	
	public void Print ()
	{
		for (Atom key : Provenance.getInstance().KeySet()) 
		{
			System.out.println(key + " = " + Provenance.getInstance().Get(key));
		}
	}
	
	
	/*************************************************************************************************************/
	/** Title: Memory																				
	/** Description: 				
	/*************************************************************************************************************/
	
	public long Memory ()
	{
		long retVal = 0;
		long tupleSize = 107;
		for (HashSet<Body> map : this.provenance.values()) 
		{
			for (Body tuple : map) 
			{
				for (Atom atom : tuple.getAtoms()) 
				{
					if (null != atom) 
					{
						long childrenSize = (null == atom.getTrees() || null == atom.getTrees().get(0).getChildren()) ? 0 : atom.getTrees().get(0).getChildren().size();
						long ParentsSize = (null == atom.getTrees() || null == atom.getTrees().get(0).getParents()) ? 0 : atom.getTrees().get(0).getParents().size();
						retVal += tupleSize + ( tupleSize + ( childrenSize + ParentsSize ) * tupleSize  ) ;//booleans + weight of rule + terms + tree
					}
				}
			}
		}
		
		return retVal / 1024L;
	}
}
