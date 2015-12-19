package Derivation;

import java.util.HashSet;
import java.util.Set;

import Basics.Program;
import Basics.Rule;

public class DeriveByRule 
{
	Rule r;
	
	Program p;
	
	Set<Rule> derivations = new HashSet<Rule>(); 

	
	public DeriveByRule (Rule ir, Program ip)
	{
		this.r = ir;
		this.p = ip;
	}
	
	
	
	public Rule getR() 
	{
		return r;
	}



	public void setR(Rule r) 
	{
		this.r = r;
	}



	public Set<Rule> getDerivations() 
	{
		return derivations;
	}



	/*************************************************************************************************************/
	/** Title: Reset																				
	/** Description: resets the parameters for next step of the program			
	/*************************************************************************************************************/
	
	public void Reset ()
	{
		this.derivations.clear();
	}

}
