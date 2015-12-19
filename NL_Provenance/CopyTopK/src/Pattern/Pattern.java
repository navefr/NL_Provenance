package Pattern;

import java.util.Vector;

import TopK.DerivationTree;

public class Pattern 
{
	Vector<Vector<PatternNode>> patternVec = new Vector<Vector<PatternNode>>();
	
	
	public Pattern () {}
	
	
	/*************************************************************************************************************/
	/** Title: Pattern																				
	/** Description: copy constructor  			
	/*************************************************************************************************************/
	
	public Pattern (Pattern pattern) 
	{
		for (Vector<PatternNode> vector : pattern.getPatternVec()) 
		{
			patternVec.add( new Vector<PatternNode>(vector) );
		}
	}
	
	
	
	public Pattern (PatternNode ipatternRoot)
	{
		Vector<PatternNode> root = new Vector<PatternNode>();
		root.add(ipatternRoot);
		this.patternVec.add(root);
	}
	
	
	
	public Pattern (Vector<Vector<PatternNode>> ipatternVec)
	{
		this.patternVec = ipatternVec;
	}
	
	
	
	public Vector<Vector<PatternNode>> getPatternVec()
	{
		return patternVec;
	}

	

	public void setPatternVec(Vector<PatternNode> ... ipattern) 
	{
		for (Vector<PatternNode> vector : ipattern) 
		{
			this.patternVec.add(vector);
		}
	}
	
	
	/*************************************************************************************************************/
	/** Title: getRoot																				
	/** Description: get the root of the pattern  			
	/*************************************************************************************************************/
	
	public PatternNode getRoot ()
	{
		return patternVec.get(0).get(0);
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: AddLayer																				
	/** Description: Add lower Layer to pattern  			
	/*************************************************************************************************************/
	
	public void AddLayer (Vector<PatternNode> layer)
	{
		this.patternVec.add(layer);
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: getSize																				
	/** Description:  			
	/*************************************************************************************************************/
	
	public int getSize() 
	{
		int size = 0;
		for (Vector<PatternNode> vector : this.patternVec) 
		{
			size += vector.size();
		}
		
		return size;
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: toString																				
	/** Description:  			
	/*************************************************************************************************************/
	
	public String toString ()
	{
		String retVal = "";
		int i = 1;
		for (Vector<PatternNode> patternVec : this.patternVec) 
		{
			retVal += "Layer " + i++ + ": " + patternVec.toString() + "\n"; 
		}
		
		return retVal;
	}
	
	
	/*************************************************************************************************************/
	/** Title: Arrange																				
	/** Description:  			
	/*************************************************************************************************************/
	
	public void Arrange ()
	{
		Vector<PatternNode> leaves = new Vector<PatternNode>();
		for (int i = 0; i < patternVec.size()-1; i++) 
		{
			Vector<PatternNode> vector = patternVec.get(i);
			Vector<PatternNode> toRemove = new Vector<PatternNode>();
			for (PatternNode patternNode : vector) 
			{
				if (patternNode.isLeaf()) 
				{
					leaves.add(patternNode);
					toRemove.add(patternNode);
				}
			}
			
			for (PatternNode patternNode : toRemove) 
			{
				vector.remove(patternNode);
			}
			
		}
		
		patternVec.lastElement().addAll(leaves);
	}
	
	
	/*************************************************************************************************************/
	/** Title: Renaming																				
	/** Description: renames all atoms in the pattern 			
	/*************************************************************************************************************/
	
	public void Renaming (boolean intersection)
	{
		int cnt = 1;
		for (Vector<PatternNode> vector : patternVec) 
		{
			if (true == intersection && true == patternVec.lastElement().equals(vector)) 
			{
				for (PatternNode PatternNode : vector) 
				{
					PatternNode.setNewName(PatternNode.getName());
				}
			}
			
			else
			{
				for (PatternNode PatternNode : vector) 
				{
					if (true == intersection) 
					{
						PatternNode.setNewName(PatternNode.getName() + cnt++);
					}
					
					else
					{
						PatternNode.setNewName("_p_" + cnt);
						cnt++;
					}
				}
			}
		}
	}
	
	
	/*************************************************************************************************************/
	/** Title: RenamingTrans																				
	/** Description: renames all atoms in the pattern. Used for pattern with transitive children 			
	/*************************************************************************************************************/
	
	public void RenamingTrans ()
	{
		int cnt = 1;
		for (Vector<PatternNode> vector : patternVec) 
		{
			for (PatternNode patternNode : vector) 
			{
				if (true == patternNode.isTransChild()) 
				{
					patternNode.setNewName("_pt_" + cnt);
				}
				
				else
				{
					patternNode.setNewName("_p_" + cnt);
				}
				
				cnt++;
			}

		}
	}
	
	
	
	/*************************************************************************************************************/
	/** Title: ConvertTreeToPattern																				
	/** Description: renames all atoms in the pattern 			
	/*************************************************************************************************************/
	
	public void ConvertTreeToPattern (DerivationTree tree)
	{
		Vector<PatternNode> curLayer = new Vector<PatternNode>();
		Vector<DerivationTree> workVec = new Vector<DerivationTree>();
		Vector<DerivationTree> nextWorkVec = new Vector<DerivationTree>();
		workVec.add(tree);
		tree.setNode(new PatternNode(tree.getDerivedFact()));
		while (false == workVec.isEmpty())
		{
			DerivationTree curTree = workVec.remove(0);
			PatternNode curRoot = curTree.getNode();//new PatternNode(curTree.getDerivedFact());
			if (null != curTree.getChildren()) 
			{
				for (DerivationTree child : curTree.getChildren()) 
				{
					PatternNode c = new PatternNode(child.getDerivedFact());
					curRoot.AddPatternChild(c);
					c.AddParent(curRoot);
					child.setNode(c);
					nextWorkVec.add(child);
				}
			}
			
			//finished with curRoot
			curLayer.add(curRoot);
			
			if (workVec.isEmpty()) 
			{
				workVec = new Vector<DerivationTree>(nextWorkVec);
				this.patternVec.add(new Vector<PatternNode>(curLayer));
				curLayer.clear();
				nextWorkVec.clear();
			}
		}
	}
	
	
	/*************************************************************************************************************/
	/** Title: Reset																				
	/** Description:  			
	/*************************************************************************************************************/
	
	public void Reset ()
	{
		this.patternVec.clear();
	}

}
