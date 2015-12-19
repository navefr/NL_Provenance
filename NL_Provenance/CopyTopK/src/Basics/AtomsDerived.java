package Basics;

import java.util.HashSet;

public class AtomsDerived 
{
	public static AtomsDerived  set = null;
	
	HashSet<Atom> atomSet;
	
	
	
	public AtomsDerived () 
	{
		this.atomSet = new HashSet<Atom> ();
	}
	
	
	
	public static AtomsDerived getInstance() 
	{
		if(set == null) 
		{
			set = new AtomsDerived();
		}
		
		return set;
	}


	
	public HashSet<Atom> getAtomSet() 
	{
		return atomSet;
	}


	
	public void setAtomSet(HashSet<Atom> atomSet) 
	{
		this.atomSet = atomSet;
	}


	
	/*************************************************************************************************************/
	/** Title: Reset																				
	/** Description: Empty all keyMap info			
	/*************************************************************************************************************/

	public void Reset ()
	{
		this.atomSet.clear();
		set = null;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: ClearMap																				
	/** Description: Empty all keyMap info			
	/*************************************************************************************************************/

	public void ClearSet ()
	{
		this.atomSet.clear();
	}
	
	
	/*************************************************************************************************************/
    /** Title: Update                                                                                                                                                               
    /** Description: add new atom to keyMap                     
    /*************************************************************************************************************/
    
    public void Update (Atom atom)
    {
    	this.atomSet.add(atom);
    }
	
	
}
