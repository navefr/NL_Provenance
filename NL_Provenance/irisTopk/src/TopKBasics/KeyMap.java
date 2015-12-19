package TopKBasics;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deri.iris.api.basics.ITuple;

public class KeyMap 
{
	public static KeyMap  map = null;
	
	Map<String, Atom> keyMap;
	
	
	public KeyMap () 
	{
		this.keyMap = new HashMap<String, Atom>();
	}
	
	
	public static KeyMap getInstance() 
	{
		if(map == null) 
		{
			map = new KeyMap();
		}
		
		return map;
	}
	
	
	
	public Map<String, Atom> getMap() 
	{
		return keyMap;
	}


	public void setKeyMap(Map<String, Atom> keyMap) 
	{
		this.keyMap = keyMap;
	}


	/*************************************************************************************************************/
    /** Title: Update                                                                                                                                                               
    /** Description: add new atom to keyMap                     
    /*************************************************************************************************************/
    
    public boolean Update (Atom atom)
    {
    	boolean retVal = false;
    	if (false == this.keyMap.containsKey(atom.toString())) 
    	{
    		this.keyMap.put(atom.toString(), atom);
    		retVal = true;
    	}
    	
    	return retVal;
    }
    
    
    /*************************************************************************************************************/
    /** Title: StringUpdate                                                                                                                                                               
    /** Description: add new atom to keyMap                     
    /*************************************************************************************************************/
    
    public Atom StringUpdate (String atomName, ITuple atomTuple)
    {
    	Atom retVal = null;
    	if (false == this.keyMap.containsKey(atomName + atomTuple + ".")) 
    	{
    		Atom atom = new Atom ( atomName, atomTuple );
    		this.keyMap.put(atom.toString(), atom);
    		retVal = atom;
    	}
    	
    	return retVal;
    }
    
    
    /*************************************************************************************************************/
    /** Title: FoolsUpdate                                                                                                                                                               
    /** Description: add new atom to keyMap without checking whether it exists already                    
    /*************************************************************************************************************/
    
    public void FoolsUpdate (Atom atom)
    {
    	this.keyMap.put(atom.toString(), atom);
    }
    
    
    /*************************************************************************************************************/
    /** Title: UpdateRule                                                                                                                                                               
    /** Description: add new atom to keyMap                     
    /*************************************************************************************************************/
    
    /*public void UpdateRule (Rule r)
    {
    	Update(r.getHead());
    	for (Atom atom : r.getBody().getAtoms()) 
    	{
    		if (true == atom.isFact())
			{
    			Update(atom);
			}
		}
    }*/
    
    
    public void AddAll (Set<Atom> set)
    {
    	for (Atom atom : set) 
    	{
			FoolsUpdate(atom);
		}
    }
    
    
    /*************************************************************************************************************/
    /** Title: Values                                                                                                                                                               
    /** Description:                      
    /*************************************************************************************************************/
    
    public Collection<Atom> Values ()
    {
    	return this.keyMap.values();
    }
    
    
    /*************************************************************************************************************/
    /** Title: Contains                                                                                                                                                               
    /** Description:                      
    /*************************************************************************************************************/
    
    public boolean Contains (Atom atom)
    {
    	return this.keyMap.values().contains(atom);
    }
    
    
    /*************************************************************************************************************/
	/** Title: KeySet																				
	/** Description: 				
	/*************************************************************************************************************/
	
	public Set<String> KeySet ()
	{
		return this.keyMap.keySet();
	}
	
	
	/*************************************************************************************************************/
	/** Title: Get																				
	/** Description: 				
	/*************************************************************************************************************/
	
	public Atom Get (String atomName, ITuple atomTuple)
	{
		return this.keyMap.get(atomName + atomTuple + ".");
	}
	
	
	/*************************************************************************************************************/
	/** Title: Get																				
	/** Description: 				
	/*************************************************************************************************************/
	
	public Atom Get (Atom atom)
	{
		return this.keyMap.get(atom.toString());
	}
    
	
	/*************************************************************************************************************/
	/** Title: Size																				
	/** Description: 				
	/*************************************************************************************************************/
	
	public int Size ()
	{
		return this.keyMap.keySet().size();
	}
    
	
	
    
    /*************************************************************************************************************/
	/** Title: Reset																				
	/** Description: Empty all keyMap info			
	/*************************************************************************************************************/

	public void Reset ()
	{
		this.keyMap.clear();
		map = new KeyMap();
	}
	
	
	/*************************************************************************************************************/
	/** Title: ClearMap																				
	/** Description: Empty all keyMap info			
	/*************************************************************************************************************/

	public void ClearMap ()
	{
		this.keyMap.clear();
	}
	
	
	/*************************************************************************************************************/
	/** Title: UpdateFactBody																				
	/** Description: 				
	/*************************************************************************************************************/
	
	public void UpdateFactBody (Body body)
	{
		for (Atom atom : body.getAtoms())
		{
			if (true == atom.isFact())
			{
				Update(atom);
			}
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: GetPointerBodyForTop1																				
	/** Description: Finds the pointer body in the prov. given a key and a body				
	/*************************************************************************************************************/
	
	public Body GetPointerBodyForTop1 (Body body)
	{
		Body retVal = new Body ();
		for (Atom atom : body.getAtoms()) 
		{
			retVal.getAtoms().add(Get(atom));
		}
		
		retVal.setRuleWeight(body.getRuleWeight());
		return retVal;
	}
	
	
	/*************************************************************************************************************/
	/** Title: GetPointerBodyForTop1																				
	/** Description: Finds the pointer body in the prov. given a key and a body				
	/*************************************************************************************************************/
	
	public Body GetPointerBodyForTop1 (List<String> body, double weight)
	{
		Body retVal = new Body ();
		for (String atom : body) 
		{
			retVal.getAtoms().add(this.keyMap.get(atom));
		}
		
		retVal.setRuleWeight(weight);
		return retVal;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: AllAtomsHaveTop1																				
	/** Description: 				
	/*************************************************************************************************************/
	
	public void ChackAllAtomsHaveTop1 ()
	{
		int i = 0;
		for (Atom atom : this.keyMap.values()) 
		{
			if (false == atom.foundTop1) 
			{
				System.out.println("Did not find top-1 for " + atom);
				//System.out.println("Bodies are: " + Provenance.getInstance().Get(atom));
				i++;
			}
			
			if (atom.getTree() == null) 
			{
				System.out.println(atom + " has no trees!!!");
			}
			
			/*if (Provenance.getInstance().Get(atom).size() > 1) 
			{
				System.out.println("Size of prov for " + atom + " is " + Provenance.getInstance().Get(atom).size() + " and should be 1");
			}*/
		}
		
		System.out.println("num not found top 1: " + i);
	}
}
