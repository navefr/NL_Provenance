package Measurements;

import Basics.Atom;
import Basics.Constant;
import Basics.KeyMap;
import Basics.MemDB;
import Basics.Rule;
import Basics.Var;
import Derivation.SemiNaive;

public class IrisTupleProgram 
{
	public static void main (String [] args)
	{
		/*p(’abcd0’).p(’abcd1’).p(’abcd2’).p(’abcd3’).p(’abcd4’).
	p(’abcd5’).p(’abcd6’).p(’abcd7’).p(’abcd8’).p(’abcd9’).
	p(’abcd10’).
	ra(?A,?B,?C,?D,?E) :- p(?A),p(?B),p(?C),p(?D),p(?E).
	rb(?A,?B,?C,?D,?E) :- p(?A),p(?B),p(?C),p(?D),p(?E).
	r(?A,?B,?C,?D,?E) :- ra(?A,?B,?C,?D,?E),rb(?A,?B,?C,?D,?E).
	q(?A) :- r(?A,?B,?C,?D,?E).
	q(?B) :- r(?A,?B,?C,?D,?E).
	q(?C) :- r(?A,?B,?C,?D,?E).
	q(?D) :- r(?A,?B,?C,?D,?E).
	q(?E) :- r(?A,?B,?C,?D,?E).
	?- q(?X).*/

		Constant abcd0 = new Constant("abcd0", "Country");
		Constant abcd1 = new Constant("abcd1", "Country");
		Constant abcd2 = new Constant("abcd2", "Product");
		Constant abcd3 = new Constant("abcd3", "Country");
		Constant abcd4 = new Constant("abcd4", "Country");
		Constant abcd5 = new Constant("abcd5", "Product");
		Constant abcd6 = new Constant("abcd6", "Country");
		Constant abcd7 = new Constant("abcd7", "Country");
		Constant abcd8 = new Constant("abcd8", "Country");
		Constant abcd9 = new Constant("abcd9", "Country");
		Constant abcd10 = new Constant("abcd10", "Country");
		Constant abcd11 = new Constant("abcd11", "Country");
		Constant abcd12 = new Constant("abcd12", "Country");
		Constant abcd13 = new Constant("abcd13", "Country");
		Constant abcd14 = new Constant("abcd14", "Country");
		Constant abcd15 = new Constant("abcd15", "Country");
		Constant abcd16 = new Constant("abcd16", "Country");
		Atom fact_0 = new Atom("p", abcd0);
		Atom fact_1 = new Atom("p", abcd1);
		Atom fact_2 = new Atom("p", abcd2);
		Atom fact_3 = new Atom("p", abcd3);
		Atom fact_4 = new Atom("p", abcd4);
		Atom fact_5 = new Atom("p", abcd5);
		Atom fact_6 = new Atom("p", abcd6);
		Atom fact_7 = new Atom("p", abcd7);
		Atom fact_8 = new Atom("p", abcd8);
		Atom fact_9 = new Atom("p", abcd9);
		Atom fact_10 = new Atom("p", abcd10);
		Atom fact_11 = new Atom("p", abcd11);
		Atom fact_12 = new Atom("p", abcd12);
		Atom fact_13 = new Atom("p", abcd13);
		Atom fact_14 = new Atom("p", abcd14);
		Atom fact_15 = new Atom("p", abcd15);
		Atom fact_16 = new Atom("p", abcd16);
		MemDB.getInstance().Reset();
		KeyMap.getInstance().Reset();
		
		Atom [] facts = new Atom [] {fact_0, fact_1, fact_2, fact_3, fact_4, fact_5, fact_6, fact_7, fact_8, fact_9, fact_10//};
		,fact_11, fact_12, fact_13, fact_14};// fact_15, fact_16
		for (Atom f : facts)
		{
			MemDB.getInstance().Update(f);
		}

		Var a = new Var("a", "Country");
		Var b = new Var("b", "Country");
		Var c = new Var("c", "Country");
		Var d = new Var("d", "Country");
		Var e = new Var("e", "Country");

		Atom p_1 = new Atom("p", a);
		Atom p_2 = new Atom("p", b);
		Atom p_3 = new Atom("p", c);
		Atom p_4 = new Atom("p", d);
		Atom p_5 = new Atom("p", e);
		Atom p_6 = new Atom("p", a);
		Atom p_7 = new Atom("p", b);
		Atom p_8 = new Atom("p", c);
		Atom p_9 = new Atom("p", d);
		Atom p_10 = new Atom("p", e);

		Atom ra = new Atom("ra", a,b, c, d, e);
		Atom rb = new Atom("rb", a,b, c, d, e);
		Atom r = new Atom("r", a,b, c, d, e);
		
		Atom q_1 = new Atom("q", a);
		Atom q_2 = new Atom("q", b);
		Atom q_3 = new Atom("q", c);
		Atom q_4 = new Atom("q", d);
		Atom q_5 = new Atom("q", e);

		Rule r1 = new Rule (ra, 1, p_1, p_2, p_3, p_4, p_5);
		Rule r2 = new Rule (rb, 1, p_6, p_7, p_8, p_9, p_10);
		Rule r3 = new Rule (r, 1, ra, rb);
		Rule r4 = new Rule (q_1, 1, r);
		Rule r5 = new Rule (q_2, 1, r);
		Rule r6 = new Rule (q_3, 1, r);
		Rule r7 = new Rule (q_4, 1, r);
		Rule r8 = new Rule (q_5, 1, r);
		
		
		SemiNaive sn = new SemiNaive(1, null, r1, r2, r3, r4, r5, r6, r7, r8);
		System.out.println("size of start DB: " + MemDB.getInstance().Size());
		long startTime = System.currentTimeMillis();
		sn.Run(false, false, true);
		long endTime = System.currentTimeMillis();
		
		double durationFullProv = (endTime - startTime);
		System.out.println("Time for top-1: " + durationFullProv);
		System.out.println("size of end DB: " + MemDB.getInstance().NumOfFactsWithName("r"));
	}
}
