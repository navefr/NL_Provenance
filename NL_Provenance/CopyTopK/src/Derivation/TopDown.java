package Derivation;

import Basics.*;
import Circuit.*;

public class TopDown 
{	
	Program p;
	
	ParseResultsTopDown parser;
	
	Circuit c = new Circuit();
	
	
	
	public TopDown () {}
	
	
	public TopDown (Program ip)
	{
		this.p = ip;
		this.parser =  new ParseResultsTopDown(ip);
	}
	
	
	
	public TopDown (Rule ... irs)
	{
		this.p = new Program(irs);
		this.parser = new ParseResultsTopDown(this.p);
	}


	public Program getP() 
	{
		return p;
	}


	public void setP(Program p) 
	{
		this.p = p;
	}


	public ParseResultsTopDown getParser() 
	{
		return parser;
	}


	public void setParser(ParseResultsTopDown parser) 
	{
		this.parser = parser;
	}
	
	
	
	
	public Circuit getC() 
	{
		return c;
	}


	/*************************************************************************************************************/
	/** Title: Run																				
	/** Description: Finds all possible ways to derive an atom			
	/*************************************************************************************************************/
	
	public void Run (Atom root)
	{
		this.parser.getAtomsToDerive().add(root);
		
		long startTime = System.currentTimeMillis();
		while (false == this.parser.getAtomsToDerive().isEmpty())
		{
			this.parser.ParseResults();
		}
		
		long endTime = System.currentTimeMillis();
		double intersectionTime = (endTime - startTime);
		/*if (intersectionTime > 1000) 
		{*/
			System.out.println("Run:: time for derive iteration in milliseconds: " + intersectionTime);
			System.out.println("Run:: size before cleaning: " + Provenance.getInstance().GetProvSize());
			//System.out.println("Run:: prov beforw cleaning: " + Provenance.getInstance().getProvenance());
		//}
		this.parser.LeaveOnlyStableDerivations();
		
		for (Atom key : Provenance.getInstance().KeySet()) 
		{
			if (!key.isStable()) {
				System.out.println("Run:: key " + key + " is not stable");
			}
			for (Body b : Provenance.getInstance().Get(key)) 
			{
				if (!b.IsBodyStable()) {
					System.out.println("Run:: body " + b + " is not stable");
				}
			}
		}
		
		System.out.println("Run:: size after cleaning: " + Provenance.getInstance().GetProvSize());
		//System.out.println("size after Leaving Only Possible Derivations: " + this.parser.getProvenance().size());
		//this.parser.LeaveOnlyRelevantDerivations(root);
		//c.BuildCicuitTopDown(root, this.parser.getProvenance());
		//System.out.println("size after cleaning: " + this.parser.getProvenance().size());
	}

}
 