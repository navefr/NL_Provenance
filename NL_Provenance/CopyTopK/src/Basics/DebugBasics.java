package Basics;


public class DebugBasics 
{
	public static void main (String [] args) throws ClassNotFoundException
	{
		Var a = new Var("a", "Country");
		Var b = new Var("b", "Country");
		Var c = new Var("c", "Product");
		Var f = new Var("f", "Country");
		
		Atom dealsWith1_1 = new Atom("dealWith", a,b);
		Atom dealsWith1_2 = new Atom("dealWith", a,b);
		Atom dealsWith1_3 = new Atom("dealWith", a,b);
		Atom dealsWith2 = new Atom("dealWith", a,f);
		Atom dealsWith3 = new Atom("dealWith", f,b);
		Atom dealsWith4 = new Atom("dealWith", b,a);
		Atom exported = new Atom("export", b,c);
		Atom imported = new Atom("import", a,c);
		
		Rule r1 = new Rule (dealsWith1_1, 1, imported, exported);
		Rule r2 = new Rule (dealsWith1_2, 1, dealsWith2, dealsWith3);
		Rule r3 = new Rule (dealsWith1_3, 1, dealsWith4);
		
		Constant Cuba = new Constant("Cuba", "Country");
		Constant France = new Constant("France", "Country");
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
		
		/*DB db = DB.getInstance();
		db.Update(imPw);
		db.Update(exIw);
		db.Update(exFw);
		db.Update(exCc);
		db.Update(imCw);
		db.Update(imMw);
		db.Update(imMc);
		db.Update(imFc);*/
		//System.out.println(db.GetAllConstantsInCategory(Cuba)); 
		//System.out.println(db.categoryMap);
		//System.out.println(db.tableNames);
		/*System.out.println(db.Size());;
		db.DropTableIfExists("import");
		db.DropTableIfExists("export");
		db.DropTableIfExists("dealWith");*/
		Atom dw1 = new Atom("dealWith", a,Israel);
		Atom dw2 = new Atom("dealWith", c,Israel);
		
		RelevantKey key = new RelevantKey(dw1);
		RelevantKey key_2 = new RelevantKey(dw2);
		System.out.println(dw1);
		
	}
}
