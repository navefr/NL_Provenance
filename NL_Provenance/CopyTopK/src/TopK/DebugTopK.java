package TopK;

import Basics.Atom;
import Basics.Constant;
import Basics.DB;
import Basics.Rule;
import Basics.Var;
import Derivation.SemiNaive;
import Derivation.TopDown;
import Parsing.ParseDB;

public class DebugTopK 
{
	public static void main (String [] args)
	{
		DB.getInstance().DropTableIfExists("imports");
		DB.getInstance().DropTableIfExists("exports");
		DB.getInstance().DropTableIfExists("dealsWith");
		DB.getInstance().DropTableIfExists("import");
		DB.getInstance().DropTableIfExists("export");
		DB.getInstance().DropTableIfExists("dealWith");
		
		Var a = new Var("a", "Country");
		Var b = new Var("b", "Country");
		Var c = new Var("c", "Product");
		Var f = new Var("f", "Country");

		Atom dealsWith1_1 = new Atom("dealsWith", a,b);
		Atom dealsWith1_2 = new Atom("dealsWith", a,b);
		Atom dealsWith1_3 = new Atom("dealsWith", a,b);
		Atom dealsWith2 = new Atom("dealsWith", a,f);
		Atom dealsWith3 = new Atom("dealsWith", f,b);
		Atom dealsWith4 = new Atom("dealsWith", b,a);
		Atom exported = new Atom("exports", b,c);
		Atom imported = new Atom("imports", a,c);

		Rule r1 = new Rule (dealsWith1_1, 1, imported, exported);
		Rule r2 = new Rule (dealsWith1_2, 1, dealsWith2, dealsWith3);
		Rule r3 = new Rule (dealsWith1_3, 1, dealsWith4);

		Constant Angola = new Constant("Angola", "Country");
		Constant Benin = new Constant("Benin", "Country");
		/*Constant France = new Constant("France", "Country");
		Constant Cuba = new Constant("Cuba", "Country");
		Constant Mexico = new Constant("Mexico", "Country");
		Constant cigars = new Constant("cigars", "Product");
		Constant wine = new Constant("wine", "Product");
		Constant weapon = new Constant("weapon", "Product");
		Constant Israel = new Constant("Israel", "Country");
		Constant Palestine = new Constant("Palestine", "Country");

		Atom imPw = new Atom ("import", true, true, Palestine, weapon);
		Atom exIw = new Atom ("export", true, true, Israel, weapon);
		Atom exFw = new Atom ("export", true, true, France, wine);
		Atom exCc = new Atom ("export", true, true, Cuba, cigars);
		Atom imCw = new Atom ("import", true, true, Cuba, wine);
		Atom imMw = new Atom ("import", true, true, Mexico, wine);
		Atom imMc = new Atom ("import", true, true, Mexico, cigars);
		Atom imFc = new Atom ("import", true, true, France, cigars);


		DB.getInstance().DropTableIfExists("import");
		DB.getInstance().DropTableIfExists("export");

		DB.getInstance().Update(imPw);
		DB.getInstance().Update(exIw);
		DB.getInstance().Update(exFw);
		DB.getInstance().Update(exCc);
		DB.getInstance().Update(imCw);
		DB.getInstance().Update(imMw);
		DB.getInstance().Update(imMc);
		DB.getInstance().Update(imFc);*/
		
		ParseDB rfDB = new ParseDB ("C:\\Users\\amirgilad\\Downloads\\yago2core_facts.clean.notypes.tsv", 100); // 100 rows in DB table
		Atom dealsWithMC = new Atom("dealsWith", Angola,Benin);
		//Atom dealsWithMC = new Atom("dealWith", Mexico,France);
		/*SemiNaive sn = new SemiNaive(r1, r2, r3);
		sn.Run(true);*/
		TopDown td = new TopDown (r1, r2, r3);
		td.Run(dealsWithMC);
		//int size = td.getParser().getProvenance().keySet().size();
		System.out.println(td.getC().getCircuit());
		TopKAlgo topk = new TopKAlgo();
		topk.RunTopKForAtom(dealsWithMC, 2, td.getC().getCircuit());
	}
}
