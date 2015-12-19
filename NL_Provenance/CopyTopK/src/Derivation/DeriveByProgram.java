package Derivation;

import java.util.Vector;

import Basics.Program;
import Basics.Rule;

public class DeriveByProgram 
{
	Program p;
	
	Vector<Vector<Rule>> derivationsVec = new Vector<Vector<Rule>>();
	
	
	
	public DeriveByProgram (Rule ... irs)
	{
		this.p = new Program(irs);
	}
	
	
	
	public DeriveByProgram (Program p)
	{
		this.p = p;
	}



	public Vector<Vector<Rule>> getDerivationsVec() 
	{
		return derivationsVec;
	}
	
	
	
	public Program getP() 
	{
		return p;
	}



	public void setP(Program p) 
	{
		this.p = p;
	}



	/*************************************************************************************************************/
	/** Title: Reset																				
	/** Description: resets the parameters for next step of the program			
	/*************************************************************************************************************/
	
	public void Reset ()
	{
		this.derivationsVec.clear();
	}
}
