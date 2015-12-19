package Derivation;

import TopK.DerivationTree;
import TopK.EquationTopK;
import Basics.*;

public class SemiNaive 
{
	Program p;
	
	ParseResultsBottomUp parser;
	
	EquationTopK topk;
	
	Atom patternRoot;
	
	DerivationTree bestTreeThisStep;
	
	public SemiNaive (Rule ... irs)
	{
		this.p = new Program(irs);
		this.parser = new ParseResultsBottomUp(this.p);
	}
	
	
	public SemiNaive (int ik, Atom root, Rule ... irs) //for online
	{
		this.p = new Program(irs);
		this.topk = new EquationTopK (ik, true);
		this.patternRoot = root;
		this.parser = new ParseResultsBottomUp(this.p, this.topk);
	}
	
	
	
	public SemiNaive (Program irs)
	{
		this.p = irs;
		this.parser = new ParseResultsBottomUp(this.p);
	}
	
	
	public SemiNaive (int ik, Program irs, Atom root) //for online
	{
		this.p = irs;
		this.topk = new EquationTopK (ik, true);
		this.patternRoot = root;
		this.parser = new ParseResultsBottomUp(this.p, this.topk);
	}
	
	
	public Program getP() 
	{
		return p;
	}


	public void setP(Program p) 
	{
		this.p = p;
	}


	
	public ParseResultsBottomUp getParser() 
	{
		return parser;
	}

	

	public void setHelper(ParseResultsBottomUp iparser)
	{
		this.parser = iparser;
	}

	
	

	public DerivationTree getBestTreeThisStep() 
	{
		return bestTreeThisStep;
	}


	/*************************************************************************************************************/
	/** Title: SemiNaiveIteration																				
	/** Description: performs one iteration of semi-naive algorithm 			
	/*************************************************************************************************************/
	
	public boolean SemiNaiveIteration (boolean debug, boolean forIntersection, boolean online)
	{
		//long startTime = System.currentTimeMillis();
		boolean retVal = parser.ParseResults(forIntersection, online);
		/*long end = System.currentTimeMillis();
		if ((end-startTime) > 500) 
		{
			System.out.println("SemiNaiveIteration:: time to update DB + parse results: " + (end-startTime));
			//System.out.println("SemiNaiveIteration:: bodies of dealsWith1(Canada,Andorra) are: " + this.parser.prov.get(this.parser.keyMap.get("dealsWith1(Canada,Andorra)")));
		}*/
		
		
		//DEBUG
		if (true == debug) 
		{
			System.out.println("atoms added at this step: " + parser.getAddedInstAtoms());
			//System.out.println("provenance at this step: " + parser.getProv());
			System.out.println("size of provenance at this step: " + Provenance.getInstance().GetProvSize());
		}
		/*startTime = System.currentTimeMillis();
		if (true == online) 
		{
			//this.topk.setTreesUpdatedLastIter(this.parser.getProgramDeriver().getAddedInstAtomsInThisIteration());
			this.topk.TopK();
		}
		end = System.currentTimeMillis();
		if ((end-startTime) > 1000) 
		{
			System.out.println("SemiNaiveIteration:: time for top-k: " + (end-startTime));
		}*/
		
		// for top-1
		/*if (true == online && null != KeyMap.getInstance().Get(this.patternRoot) && true == KeyMap.getInstance().Get(this.patternRoot).didFindTop1()) 
		{
			retVal = true;
		}*/
		
		parser.Reset();
		return retVal;
	}
	
	
	
	
	/*************************************************************************************************************/
	/** Title: Run																			
	/** Description: SemiNaiveAlgorithm 			
	/*************************************************************************************************************/
	
	public void Run (boolean debug, boolean forIntersection, boolean online)
	{
		this.parser.programDeriver.bestTreeThisStep = null;
		while (false == SemiNaiveIteration(debug, forIntersection, online));
		
		this.bestTreeThisStep = this.parser.bestTreeThisStep;
		/*if (true == online) 
		{
			KeyMap.getInstance().ChackAllAtomsHaveTop1 ();
		}*/
		//MemDB.getInstance().Print();
		//this.parser.getC().SanityCheck();
	}
	
}
