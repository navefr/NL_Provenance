package Basics;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;


public class Program 
{
	public Vector<Rule> Rules = new Vector<Rule>();
	
	
	
	public Program (Rule ... rules) 
	{
		for (Rule r : rules) 
		{
			Rules.add(r);
		}
	}
	
	
	
	public Program (Vector<Rule> rules) 
	{
		this.Rules = rules;
	}
	
	
	
	public Program (List<Rule> rules) 
	{
		this.Rules = new Vector<Rule>(rules);
	}
	
	
	
	public Program (HashSet<Rule> rules) 
	{
		this.Rules = new Vector<Rule> (rules);
	}
	
	
	public Vector<Rule> getRules() 
	{
		return Rules;
	}

	
	
	public void setRules(Rule ... rules) 
	{
		for (Rule r : rules) 
		{
			Rules.add(r);
		}
	}
	
	
	public void addRules(Vector<Rule> rules) 
	{
		Rules.addAll(rules);
	}
	
	
	public void addRule(Rule rule) 
	{
		Rules.add(rule);
	}
	
	
	public String toString ()
	{
		String retVal = "";
		for (Rule rule : this.Rules) 
		{
			retVal += rule.toString() + "\n";
		}
		
		return retVal;
	}
}
