package Parsing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Basics.Atom;
import Basics.MemDB;
import Basics.Rule;
import Basics.Var;


public class DebugParsing 
{
	public static void main (String [] args)
	{
		/*String input = "dealsWith(a, b):- import(a, c), export(b, c) & 0.8"
				+ " \n dealsWith(a, b):- dealsWith(a, f), dealsWith(f, b) & 0.5 "
				+ "\n dealsWith(a, b):- dealsWith(b, a) & 1";
		
		ParseProgram pp = new ParseProgram(input);*/
		
		//System.out.println(pp.program);
		
		ParseDbRules rfDB = new ParseDbRules ("C:\\Users\\amirgilad\\WORKSPACESVN\\CopyTopK\\TC\\prog.iris", 100);
		//System.out.println(rfDB.getProgram());
		//ParseDB rfDB2 = new ParseDB ("yago2core_facts.clean.notypes.tsv", 200);
		
		String regular1 = "(.+)\\s*\\(((.+),?)+\\)";
		String line = "q(?A,?B) :- r(?A,?B,?C,?D,?E).";
		String [] rule = line.split(":-");
		Pattern pattern1 = Pattern.compile(regular1);
		Matcher m1, m2;
		m1 = pattern1.matcher(line);
		if (m1.find()) 
		{
			for (int j = 0; j < m1.groupCount(); j++) 
			{
				System.out.println(m1.group(j).trim());
			}
			/*
			Var a = new Var(m1.group(1).trim(), "");
			Var b = new Var(m1.group(3).trim(), "");
			Var c = new Var(m1.group(4).trim(), "");
			Var d = new Var(m1.group(6).trim(), "");
			Var e = new Var(m1.group(7).trim(), "");
			Var f = new Var(m1.group(9).trim(), "");
			Atom bodyAtom_1 = new Atom(m1.group(2), a,b);
			Atom bodyAtom_2 = new Atom(m1.group(5).trim(), c,d);
			Atom head = new Atom(m1.group(8), e,f);
			System.out.println(Double.parseDouble(m1.group(10)));*/

		}
	}
}
