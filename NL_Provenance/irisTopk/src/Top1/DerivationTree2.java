package Top1;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.factory.Factory;
import org.deri.iris.rules.compiler.ICompiledRule;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


public class DerivationTree2 implements Comparable<DerivationTree2>
{
	private ITuple derivedFact;

    //Vector<Atom> factPointers;
	
	private ICompiledRule ruleUsed;

    private Condition condition;

    private ILiteral literal;

	//Body bodyInProv;
	
	private List<DerivationTree2> children; 
	
	private List<DerivationTree2> parents; 
	
	//PatternNode node;
	
	private double weight;
	
	
	
	/*************************************************************************************************************/
	/** Title: DerivationTree2																				
	/** Description: Constructor.   			
	/*************************************************************************************************************/
	
	public DerivationTree2 () {}
	
	
	/*************************************************************************************************************/
    /** Title: DerivationTree2
	/** Description: Recursive copy constructor.   			
	/*************************************************************************************************************/
	
	public DerivationTree2 (DerivationTree2 tree)
	{
		this();
		weight = tree.getWeight();
		derivedFact = Factory.BASIC.createTuple(tree.getDerivedFact().getTerms().clone());
		derivedFact.setPredicate( tree.getDerivedFact().getPredicate() );
		derivedFact.addTree(this);
		derivedFact.setTop1Found(true);
		derivedFact.setFact(true);
		if (null != tree.getChildren()) 
		{
			derivedFact.setFact(false);
			ruleUsed = tree.getRulePointer();
			children = new ArrayList<DerivationTree2>();
			for (DerivationTree2 child : tree.getChildren()) 
			{
				DerivationTree2 newChild = new DerivationTree2(child);
				newChild.addParent(this);
				children.add( newChild );
			}
		}
	}
	
	
	
	
	/*public Body getBodyInProv() 
	{
		return bodyInProv;
	}



	public void setBodyInProv(Body bodyInProv) 
	{
		this.bodyInProv = bodyInProv;
	}*/



	public List<DerivationTree2> getChildren() 
	{
		return children;
	}

	
	
	public List<DerivationTree2> getParents() 
	{
		return parents;
	}

	
	
	public void setParents(List<DerivationTree2> parents) 
	{
		this.parents = parents;
	}
	
	
	
	public void addParent(DerivationTree2 parent) 
	{
		if (null == this.parents) 
		{
			 this.parents = new ArrayList<DerivationTree2>();
		}
		
		this.parents.add(parent);
	}

	
	
	public void setChildren(List<DerivationTree2> children) 
	{
		this.children = children;
	}
	
	
	public void copyChildren(List<DerivationTree2> children) 
	{
		for (DerivationTree2 child : children) 
		{
			this.children.add( new DerivationTree2(child) );
		}
	}
	
	
	public void addChild(DerivationTree2 child) 
	{
		if (null == this.children) 
		{
			 this.children = new ArrayList<DerivationTree2>();
		}
		
		this.children.add(child);
	}

	
	
	public ITuple getDerivedFact() 
	{
		return derivedFact;
	}

	
	
	public void setDerivedFact(ITuple derivedFact) 
	{
		this.derivedFact = derivedFact;
	}
	
	
	
	
	/*public Vector<Atom> getFactPointers() 
	{
		return factPointers;
	}
	
	
	
	public void setFactPointers(Vector<Atom> factPointers) 
	{
		this.factPointers = factPointers;
	}
	
	
	
	public void addFactPointer(Atom factPointer) 
	{
		if (null == this.factPointers) 
		{
			this.factPointers = new Vector<Atom>();
		}
		
		this.factPointers.add(factPointer);
	}*/
	
	
	
	public ICompiledRule getRulePointer() 
	{
		return ruleUsed;
	}
	
	
	
	public void setRulePointer(ICompiledRule rulePointer) 
	{
		this.ruleUsed = rulePointer;
	}
	
	
	
	public double getWeight() 
	{
		return weight;
	}
	
	
	
	public void setWeight(double weight) 
	{
		this.weight = weight;
	}

	
	
	/*public PatternNode getNode() 
	{
		return node;
	}



	public void setNode(PatternNode node) 
	{
		this.node = node;
	}*/
	
	/*************************************************************************************************************/
	/** Title: getBodyOfRoot																				
	/** Description: constructs the body of the rule that derived the root of the tree.   			
	/*************************************************************************************************************/
	
	public List<ITuple> getBodyOfRoot ()
	{
		List<ITuple> body = new ArrayList<ITuple>();
		for (DerivationTree2 child : children) 
		{
			body.add(child.getDerivedFact());
		}
		
		return body;
	}
	
	
	/*************************************************************************************************************/
	/** Title: collectFactsInTree																				
	/** Description: Collects all the facts of the tree.   			
	/*************************************************************************************************************/
	
	public List<ITuple> collectFactsInTree ()
	{
		List<ITuple> facts = new ArrayList<ITuple>();
		facts.add(derivedFact);
		if (children != null) 
		{
			for (DerivationTree2 child : children) 
			{
				facts.addAll( child.collectFactsInTree() );
			}
		}
		
		return facts;
	}
	
	
	/*************************************************************************************************************/
	/** Title: isPathWithDuplicateFact																				
	/** Description: Checks if there is a path in the tree that contains the same fact twice.   			
	/*************************************************************************************************************/
	
	public boolean isPathWithDuplicateFact (List<ITuple> factsSoFar)
	{
		boolean retVal = false;
		factsSoFar.add(derivedFact);
		if (null == children) 
		{
			return factDuplicate(factsSoFar);
		}
		
		for (DerivationTree2 child : children) 
		{
			// there is a path from root to leaf with the same fact appearing twice
			List<ITuple> factForPath = new ArrayList<ITuple>(factsSoFar);
			if ( true == child.isPathWithDuplicateFact(factForPath) )
			{
				retVal = true;
				break;
			}
		}
		
		return retVal;
	}
	

	
	/*************************************************************************************************************/
	/** Title: factDuplicate																				
	/** Description:   			
	/*************************************************************************************************************/
	
	public boolean factDuplicate (List<ITuple> facts)
	{
		for (int j=0;j<facts.size();j++)
		{
			for (int k=j+1;k<facts.size();k++)
			{
				if ( k != j && facts.get(k).equals(facts.get(j)))
				{
					return true;
				}
			}
		}

		return false;
	}
	
	
	/*************************************************************************************************************/
	/** Title: size																				
	/** Description: Compute the number of nodes in the derivation tree.   			
	/*************************************************************************************************************/
	
	public int size ()
	{
		int size = 0;
		if (null == children) 
		{
			return 1;
		}
		
		for (DerivationTree2 child : children) 
		{
			size += child.size();
		}
		
		return size; 
	}
	

	
	public int compareTo(DerivationTree2 other) 
	{
		double w1= weight;
		double w2= other.getWeight();
	    int retVal;
	    if (w1 == w2) { retVal = 0; }	    	
	    else if (w1 > w2) { retVal = -1; }
	    else {retVal = 1;}
	    return retVal;
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		/*result = prime * result
				+ ((children == null) ? 0 : children.hashCode());*/
		result = prime * result
				+ ((derivedFact == null) ? 0 : derivedFact.hashCode());
		/*result = prime * result
				+ ((factPointers == null) ? 0 : factPointers.hashCode());
		result = prime * result
				+ ((rulePointer == null) ? 0 : rulePointer.hashCode());*/
		/*long temp;
		temp = Double.doubleToLongBits(weight);
		result = prime * result + (int) (temp ^ (temp >>> 32));*/
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DerivationTree2 other = (DerivationTree2) obj;
		
		if (children == null && other.children != null) 
		{
			return false;
		}
		else if (children != null && other.children == null)
		{
			return false;
		}
		else if (children != null && other.children != null)
		{
			if (children.size() != other.children.size())
			{
				return false;
			}
			else if (!children.equals(other.children))
			{
				return false;
			}
		}
		
		if (derivedFact == null) 
		{
			if (other.derivedFact != null)
			{
				return false;
			}
		} 
		else if (!derivedFact.equals(other.derivedFact))
		{
			return false;
		}
		/*if (factPointers == null) {
			if (other.factPointers != null)
				return false;
		} else if (!factPointers.equals(other.factPointers))
			return false;*/
		/*
		if (parents == null) {
			if (other.parents != null)
				return false;
		} else if (!parents.equals(other.parents))
			return false;
		*/
		if (ruleUsed == null) 
		{
			if (other.ruleUsed != null)
			{
				return false;
			}
		} 
		else if (!ruleUsed.equals(other.ruleUsed))
		{
			return false;
		}
		/*if (Double.doubleToLongBits(weight) != Double
				.doubleToLongBits(other.weight))
			return false;
		*/
		return true;
	}

    public Condition getCondition() {
        return this.condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public ILiteral getLiteral() {
        return literal;
    }

    public void setLiteral(ILiteral literal) {
        this.literal = literal;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Derived Fact: ").append(derivedFact).append("\n");
        if (condition != null) {
            sb.append("Condition: ").append(condition.toString()).append("\n");
        }
        if (literal != null) {
            sb.append("Literal: ").append(literal.toString()).append("\n");
        }
        if (children != null && children.size() > 0) {
            for (DerivationTree2 child : children) {
                sb.append("|\n");
                String childString = child.toString();
                StringTokenizer st = new StringTokenizer(childString, "\n");
                boolean isFirst = true;
                while (st.hasMoreTokens()) {
                    sb.append("|");
                    if (isFirst) {
                        sb.append("--- ");
                        isFirst = false;
                    } else {
                        sb.append("    ");
                    }
                    sb.append(st.nextToken()).append("\n");
                }
            }
        }

        return sb.toString();
    }
	
	public static class Condition {

        private String type;
        private ITuple tuple;

        public Condition(String type, ITuple tuple) {
            this.type = type;
            this.tuple = tuple;
        }

        public String getType() {
            return type;
        }

        public ITuple getTuple() {
            return tuple;
        }

        @Override
        public String toString() {
            return "Condition{" +
                    "type='" + type + '\'' +
                    ", tuple=" + tuple +
                    '}';
        }
    }
}
