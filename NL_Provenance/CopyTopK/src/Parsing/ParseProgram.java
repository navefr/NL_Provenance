package Parsing;

import Basics.*;

public class ParseProgram 
{	
	Program program;

	/*************************************************************************************************************/
	/** Title: ParseProgram																				
	/** Description: Takes a string and turns it into a Program. Format: dealsWith(a, b):- import(a, c), export(b, c) & 0.8			
	/*************************************************************************************************************/
	
	public ParseProgram (String str)
	{
		//this.db = new DBMethods();
		Program p = new Program();
		String[] rules = str.split("\n");
		for (String ruleStr : rules) 
		{
			p.addRule(ParseRule(ruleStr));
		}
		
		this.program = p;
	}
	
	
	/*************************************************************************************************************/
	/** Title: ParseAtom																				
	/** Description: Takes a string and turns it into an Atom 			
	/*************************************************************************************************************/
	
	public Atom ParseAtom (String str)
	{
		Atom atom = new Atom();
		String[] nameParams = str.split("\\(");
		String[] params = nameParams[1].split(",");
		Proton[] atomParams = new Proton[params.length];
		
		for (int i = 0; i < params.length; i++) 
		{
			String category = DB.getInstance().RetrieveProgramCategoryByParamIndex(nameParams[0], i);
			if (params[i].length() == 1) 
			{
				atomParams[i] = new Var (params[i], category);
			}
			else
			{
				atomParams[i] = new Constant (params[i], category);
			}
		}
		
		atom.setName(nameParams[0]);
		atom.setParams(atomParams);
		return atom;
	}
	
	
	
	
	/*************************************************************************************************************/
	/** Title: ParseRule																				
	/** Description: Takes a string and turns it into a Rule 			
	/*************************************************************************************************************/
	
	public Rule ParseRule (String str)
	{
		Rule rule = new Rule ();
		
		str = str.replaceAll("\\s","");
		String[] ruleWeight = str.split("&");
		String[] headBody = ruleWeight[0].split(":-");
		
		headBody[1] = headBody[1].substring(0, headBody[1].length()-1);
		headBody[0] = headBody[0].substring(0, headBody[0].length()-1); 
		
		String[] body = headBody[1].split("\\),");
		Atom[] ruleBody = new Atom[body.length];
		
		for (int i = 0; i < ruleBody.length; i++) 
		{
			ruleBody[i] = ParseAtom(body[i]);
		}
		
		rule.setWeight(Double.parseDouble(ruleWeight[1]));
		rule.setHead(ParseAtom(headBody[0]));
		rule.setBody(ruleBody);
		return rule;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: IsLegalInput																				
	/** Description: Checks that the user input is of legal form 			
	/*************************************************************************************************************/
	
	public boolean IsLegalInput (String input)
	{
		boolean retVal = true;
		
		
		return retVal;
	}


	/*public DBMethods getDb() 
	{
		return db;
	}


	public void setDb(DBMethods db) 
	{
		this.db = db;
	}*/


	public Program getProgram() 
	{
		return program;
	}


	public void setProgram(Program program) 
	{
		this.program = program;
	}
	
	
	
}
