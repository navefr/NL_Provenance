package Basics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import Parsing.ParseDbRules;

public class MemDB 
{
	public static MemDB  db = null;
	
	public Map<String, HashSet<Atom>> facts;// = new HashMap<String, HashSet<Atom>>();
    
    public Map<String, HashSet<Proton>> contsCategories;// = new HashMap<String, HashSet<Proton>>();
    
    public Map<String, String []> tableCategoriesMap;// = new HashMap<String, String []>();
    
    
    
    public MemDB () 
    {
    	Init();
    }

    
    
    public MemDB (Map<String, HashSet<Atom>> inFacts)
    {
    	Init();
    	this.facts = inFacts;
    	for (String key : this.facts.keySet())//set all facts to stable 
    	{
    		for (Atom fact : this.facts.get(key)) 
    		{
    			fact.setFact(true);
    			fact.setStable(true);
    		}
    	}
    }
    
    
    
    public Map<String, HashSet<Atom>> getFacts() 
    {
    	return this.facts;
    }



    public void setFacts(Map<String, HashSet<Atom>> facts) 
    {
    	this.facts = facts;
    }
    
    
    
    public void Print() 
    {
    	String retVal = "";
    	for (String key : this.facts.keySet()) 
    	{
    		/*if (key.equals("dealsWith")) 
    		{*/
	    		retVal = key + ": ";
	    		for (Atom atom : this.facts.get(key)) 
	    		{
	    			//retVal += atom.toString() + ", ";
	    			System.out.println(atom);
	    		}

    		//}	//System.out.println(retVal);
    	}
    }
    
    
    
    public static MemDB getInstance() 
	{
		if(db == null) 
		{
			db = new MemDB();
		}
		
		return db;
	}
    
    
    /*************************************************************************************************************/
    /** Title: Init                                                                                                                                                               
    /** Description: Update tableCategoriesMap                     
    /*************************************************************************************************************/
    
    private void Init ()
    {
    	/*this.tableCategoriesMap.put("exports", new ArrayList<String>(Arrays.asList("Country", "Product")));
    	this.tableCategoriesMap.put("imports", new ArrayList<String>(Arrays.asList("Country", "Product")));
    	this.tableCategoriesMap.put("dealsWith", new ArrayList<String>(Arrays.asList("Country", "Country")));
    	*/
    	facts = new HashMap<String, HashSet<Atom>>();
    	contsCategories = new HashMap<String, HashSet<Proton>>();
    	ParseDbRules helper = new ParseDbRules();
    	tableCategoriesMap = new HashMap<String, String []>(helper.getRelTocat());
    }
    
    
    /*************************************************************************************************************/
    /** Title: Update                                                                                                                                                               
    /** Description: add new fact to DB                     
    /*************************************************************************************************************/
    
    public void Update (Atom atom)
    {
    	if (null == this.facts.get(atom.getName()))
    	{
    		this.facts.put(atom.getName(), new HashSet<Atom>());
    	}

    	this.facts.get(atom.getName()).add(atom);
    	atom.isFullyInst();
    	atom.setStable(true);
    	
    	for (Proton p : atom.getParams()) // add to contsCategories
    	{
    		if (null == this.contsCategories.get(p.getCategory()))
        	{
        		this.contsCategories.put(p.getCategory(), new HashSet<Proton>());
        	}
    		
    		this.contsCategories.get(p.getCategory()).add(p);
		}
    }
    
    
    /*************************************************************************************************************/
    /** Title: Update                                                                                                                                                               
    /** Description: add new fact to DB                     
    /*************************************************************************************************************/
    
    public void Update (Atom atom, boolean isLoading)
    {
    	if (null == this.facts.get(atom.getName()))
    	{
    		this.facts.put(atom.getName(), new HashSet<Atom>());
    	}

    	atom.isFullyInst();
    	atom.setStable(true);
    	atom.setFact(isLoading);
    	this.facts.get(atom.getName()).add(atom);
    	
    	for (Proton p : atom.getParams()) // add to contsCategories
    	{
    		if (null == this.contsCategories.get(p.getCategory()))
        	{
        		this.contsCategories.put(p.getCategory(), new HashSet<Proton>());
        	}
    		
    		this.contsCategories.get(p.getCategory()).add(p);
		}
    }
    
    
    
    /*************************************************************************************************************/
    /** Title: Contains                                                                                                                                                             
    /** Description: check if atom is contained in DB                       
    /*************************************************************************************************************/
    
    public boolean ContainedInTable (Atom atom)
    {
    	boolean retVal = false;
    	if (null != this.getFacts().get(atom.getName()))
    	{
    		retVal = this.facts.get(atom.getName()).contains(atom);
    	}

    	return retVal;
    }
    
    
    
    /*************************************************************************************************************/
    /** Title: GetAllConstantsInCategory                                                                                                                                                            
    /** Description: Gets all the atoms in the DB by proton category                        
    /*************************************************************************************************************/
    
    public HashSet<Proton> GetAllConstantsInCategory (Proton p)
    {
    	return this.contsCategories.get(p.getCategory());
    }
    
    
    
    /*************************************************************************************************************/
    /** Title: GetRelevantFactsFromDB                                                                                                                                                            
    /** Description: Finds all facts that are compatible with the inst. of partlyInstAtom 	                        
    /*************************************************************************************************************/
    
    public Vector<Atom> GetRelevantFactsFromDB (Atom partlyInstAtom, Program p)
	{
    	Vector<Atom> relevants = new Vector<Atom>();
    	if (null != this.facts.get(partlyInstAtom.getName().toString())) 
    	{
    		for (Atom fact : this.facts.get(partlyInstAtom.getName().toString())) 
        	{
    			if (true == partlyInstAtom.FittsPartialInst(fact)) 
    			{

    		    	if (true == fact.IsAtomRelationEdb(p)) 
    				{
    		    		fact.setFact(true);
    				}
    		    	
    				relevants.add(fact);
    			}
    		}
		}
    	
    	return relevants;
	}
    
    
    
    /*************************************************************************************************************/
	/** Title: Size																				
	/** Description: Returns the size of the entire DB 			
	/*************************************************************************************************************/
	
	public int Size()
	{
		int size = 0;
		for (String name : this.facts.keySet()) 
		{
			size += this.facts.get(name).size();
		}
		
		return size;
	}
	
	
  
    
    /*************************************************************************************************************/
	/** Title: NumOfFactsWithName																				
	/** Description: Returns the size of the entire DB 			
	/*************************************************************************************************************/
	
	public int NumOfFactsWithName(String name)
	{
		return this.facts.get(name).size();
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: InsertToTable																				
	/** Description: inserts values to table in DB			
	/*************************************************************************************************************/
	
	public void InsertToTable (String tableName, List<String> values)
	{
		/*if (this.tableCategoriesMap.get(tableName) == null) 
		{
			System.out.println("relation " + tableName + " is not recognized in tableCategoriesMap in MemDB");
		}*/
		
		Atom toInsert = new Atom (tableName);
		if (false == values.contains("People's_Republic_of_China") && this.tableCategoriesMap.get(tableName) != null) //make it the same as mysql DB numbers
		{
			for (String val : values) 
			{
				toInsert.AddParam(new Constant(val, this.tableCategoriesMap.get(tableName)[values.indexOf(val)]));//get(values.indexOf(val))));
			}
			
			toInsert.isFullyInst();
			toInsert.setStable(true);
			toInsert.setFact(true);
			
			Update(toInsert);
		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: Reset																				
	/** Description: Empty all DB info			
	/*************************************************************************************************************/

	public void Reset ()
	{
		this.facts.clear();
		this.contsCategories.clear();
		this.tableCategoriesMap.clear();
		db = new MemDB();
	}
}
