package TopKBasics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.IStringTerm;
import org.deri.iris.api.terms.ITerm;

public class KeyMap2 
{
	public static KeyMap2  map = null;
	
	Map<String, Map<String, ITuple>> keyMap;
	
	
	public KeyMap2 () 
	{
		this.keyMap = new HashMap<String, Map<String, ITuple>>();
	}
	
	
	public static KeyMap2 getInstance() 
	{
		if(map == null) 
		{
			map = new KeyMap2();
		}
		
		return map;
	}
	
	
	
	public Map<String, Map<String, ITuple>> getMap() 
	{
		return keyMap;
	}
	
    
    
    /*************************************************************************************************************/
    /** Title: StringUpdate                                                                                                                                                               
    /** Description: add new atom to keyMap                     
    /*************************************************************************************************************/
    
    public boolean StringUpdate (String atomName, ITuple atomTuple)
    {
    	boolean added = false;
    	String stringTuple = atomTuple.toString();//.intern();
    	if (false == this.keyMap.containsKey( atomName )) 
    	{
    		this.keyMap.put(atomName, new HashMap<String, ITuple>());
    		this.keyMap.get(atomName).put(stringTuple, atomTuple);
    		added = true;
    	}
    	
    	else if (false == this.keyMap.get(atomName).containsKey( stringTuple ))
    	{
    		this.keyMap.get(atomName).put(stringTuple, atomTuple);
    		added = true;
    	}
    	
    	return added;
    }
    
    
    
    /*************************************************************************************************************/
    /** Title: StringUpdate                                                                                                                                                               
    /** Description: add new atom to keyMap                     
    /*************************************************************************************************************/
    
    public boolean StringUpdate (String atomName, ITuple atomTuple, String stringTuple)
    {
    	boolean added = false;
    	if (false == this.keyMap.containsKey( atomName )) 
    	{
    		this.keyMap.put(atomName, new HashMap<String, ITuple>());
    		this.keyMap.get(atomName).put(stringTuple, atomTuple);
    		added = true;
    	}
    	
    	else if (false == this.keyMap.get(atomName).containsKey( stringTuple ))
    	{
    		this.keyMap.get(atomName).put(stringTuple, atomTuple);
    		added = true;
    	}
    	
    	return added;
    }
    
    
    
    /*************************************************************************************************************/
    /** Title: StringUpdate                                                                                                                                                               
    /** Description: add new atom to keyMap                     
    /*************************************************************************************************************/
    
    public boolean CheckTupleExists (String atomName, String atomTuple)
    {
    	return keyMap.containsKey(atomName) && keyMap.get(atomName).containsKey(atomTuple);
    }
    
    
    
    
    /*************************************************************************************************************/
    /** Title: Contains                                                                                                                                                               
    /** Description:                      
    /*************************************************************************************************************/
    
    public boolean Contains (String atomName, ITuple atomTuple)
    {
    	return this.keyMap.containsKey(atomName) && this.keyMap.get(atomName).containsKey(atomTuple.toString());
    }
    
    
    /*************************************************************************************************************/
    /** Title: Contains                                                                                                                                                               
    /** Description:                      
    /*************************************************************************************************************/
    
    public boolean Contains (String atomName, String atomTuple)
    {
    	return this.keyMap.containsKey(atomName) && this.keyMap.get(atomName).containsKey(atomTuple);
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
	
	public ITuple Get (String atomName, ITuple atomTuple)
	{
		return this.keyMap.get(atomName).get( atomTuple.toString().intern() );
	}
	
	
	/*************************************************************************************************************/
	/** Title: Get																				
	/** Description: 				
	/*************************************************************************************************************/
	
	public ITuple Get (String atomName, String atomTuple)
	{
		return this.keyMap.get(atomName).get(atomTuple);
	}
	
	
	/*************************************************************************************************************/
	/** Title: Get																				
	/** Description: 				
	/*************************************************************************************************************/
	
	public Collection<ITuple> Get (String atomName)
	{
		if (this.keyMap.containsKey(atomName)) 
		{
			return this.keyMap.get(atomName).values();
		}
		
		else
		{
			System.out.println("KeyMap does not contain the key: " + atomName);
			return null;
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: GetAll																				
	/** Description: 				
	/*************************************************************************************************************/
	
	public void GetAll (String atomName, ITuple atomTuple, List<List<ITuple>> bodies)
	{
		Map<String, ITuple> relMap = this.keyMap.get(atomName);
		if (relMap != null) 
		{
			if (atomTuple.isFullyInst() && relMap.containsKey( atomTuple.toString() )) 
			{
				List<ITuple> tuples = new ArrayList<ITuple>();
				tuples.add( relMap.get( atomTuple.toString() ) );
				bodies.add( tuples );
			}
			
			else if (false == atomTuple.isFullyInst())
			{
				for (ITuple tuple : relMap.values()) 
				{
					if ( TuplesHaveSameConsts(atomTuple, tuple) ) 
					{
						List<ITuple> tuples = new ArrayList<ITuple>();
						tuples.add(tuple);
						bodies.add( tuples );
					}
				}
			}
		}
	}
	

	
	/*************************************************************************************************************/
	/** Title: TuplesHaveSameConsts																				
	/** Description: 				
	/*************************************************************************************************************/
	
	private boolean TuplesHaveSameConsts(ITuple tuple1, ITuple tuple2 ) 
	{
		boolean retVal = true;
		for (int i = 0; i < tuple1.size(); i++) 
		{
			ITerm term = tuple1.get(i);
			if (term instanceof IStringTerm && !term.equals(tuple2.get(i))) 
			{
				retVal = false;
				break;
			}
		}
		
		return retVal;
	}
	
	
	/*************************************************************************************************************/
	/** Title: Size																				
	/** Description: 				
	/*************************************************************************************************************/
	
	public int Size ()
	{
		int size = 0;
		for (String key : this.keyMap.keySet()) 
		{
			size += this.keyMap.get(key).size();
		}
		
		return size;
	}
    
	
	
	/*************************************************************************************************************/
	/** Title: Print																				
	/** Description: 				
	/*************************************************************************************************************/
	
	public void Print ()
	{
		for (String key : this.keyMap.keySet()) 
		{
			System.out.println(key + "=" + this.keyMap.get(key));
		}
	}
	
	
    
    /*************************************************************************************************************/
	/** Title: Reset																				
	/** Description: Empty all keyMap info			
	/*************************************************************************************************************/

	public void Reset ()
	{
		this.keyMap.clear();
		map = new KeyMap2();
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
	/** Title: AllAtomsHaveTop1																				
	/** Description: 				
	/*************************************************************************************************************/
	
	public void ChackAllAtomsHaveTop1 ()
	{
		int i = 0;
		for (Map<String, ITuple> map : this.keyMap.values()) 
		{
			for (ITuple tuple : map.values()) 
			{				
				/*if (false == tuple.isTop1Found()) 
				{
					System.out.println("Did not find top-1 for " + tuple);
					//System.out.println("Bodies are: " + Provenance.getInstance().Get(atom));
					i++;
				}*/
				
				if (tuple.getTrees() == null || tuple.getTrees().isEmpty()) {
					System.out.println(tuple + " has no trees!!!");
				}
				
				else {
					System.out.println(tuple + "tree size: " + tuple.getTrees().iterator().next().size());
                }
			}
		}
		
		//System.out.println("num not found top 1: " + i);
	}
	
	
	/*************************************************************************************************************/
	/** Title: AllAtomsHaveTop1																				
	/** Description: 				
	/*************************************************************************************************************/
	
	public String [] FindLargestTree ()
	{
		String [] atom = new String [2];
		int maxSize = 0;
		ITuple max = null;
		for (String key : this.keyMap.keySet()) 
		{
			for (ITuple tuple : this.keyMap.get(key).values()) 
			{	
				if (tuple.getTrees().iterator().next().size() > maxSize)
				{
					atom[1] = tuple.toString();
					atom[0] = key;
					maxSize = tuple.getTrees().iterator().next().size();
				}
			}
		}
		
		return atom;
	}
	
	/*************************************************************************************************************/
	/** Title: Memory																				
	/** Description: 				
	/*************************************************************************************************************/
	
	public long Memory ()
	{
		long retVal = 0;
		long tupleSize = 107;
		for (Map<String, ITuple> map : this.keyMap.values()) 
		{
			for (ITuple tuple : map.values()) 
			{	
				long childrenSize = (null == tuple.getTrees().iterator().next().getChildren()) ? 0 : tuple.getTrees().iterator().next().getChildren().size();
				long ParentsSize = (null == tuple.getTrees().iterator().next().getParents()) ? 0 : tuple.getTrees().iterator().next().getParents().size();
				retVal += tupleSize + ( tupleSize + ( childrenSize + ParentsSize ) * tupleSize  ) ;//booleans + weight of rule + terms + tree 
			}
		}
		
		return retVal / 1024L;
	}
}
