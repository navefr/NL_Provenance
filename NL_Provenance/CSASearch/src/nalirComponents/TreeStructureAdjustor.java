package nalirComponents;

import nalirDBMS.RDBMS;
import nalirDataStructure.ParseTree;
import nalirDataStructure.ParseTreeNode;
import nalirDataStructure.Query;

public class TreeStructureAdjustor 
{
	public static void treeStructureAdjust(Query query, RDBMS db)
	{
		ParseTree tree = query.adjustedParseTree; 
		
		// put the leaf MAX, MIN level up; 
		for(int i = 0; i < tree.allNodes.size(); i++)
		{
			ParseTreeNode curNode = tree.allNodes.get(i); 
			if(curNode.tokenType.contains("FT") && curNode.children.isEmpty())
			{
				ParseTreeNode parent = curNode.parent; 
				
				if(parent.tokenType.equals("FT"))
				{
					tree.moveSubTree(curNode, parent); 
				}
				else
				{
					while(!parent.tokenType.contains("NT"))
					{
						parent = parent.parent; 
					}
					tree.moveSubTree(curNode, parent); 
				}				
			}
		}	
	
		// if an FT has more than 1 children, put its children to its parent; 
		for(int i = 0; i < tree.allNodes.size(); i++)
		{
			ParseTreeNode curNode = tree.allNodes.get(i); 
			if(curNode.tokenType.equals("FT") && curNode.children.size() > 1)
			{
				while(curNode.children.size() > 1)
				{
					tree.moveSubTree(curNode.children.get(0), curNode.children.get(1)); 
				}
			}
		}

		// move the child of VT to its NT parent; 
		for(int i = 0; i < tree.allNodes.size(); i++)
		{
			ParseTreeNode curNode = tree.allNodes.get(i); 
			if(curNode.tokenType.contains("VT") && !curNode.children.isEmpty())
			{
				if(curNode.children.get(0).tokenType.equals("NT"))
				{
					ParseTreeNode child = curNode.children.get(0); 
					if(curNode.choice < 0 || child.choice < 0)
					{
						tree.moveSubTree(curNode.parent, child); 
					}
					else if(curNode.mappedElements.get(curNode.choice).schemaElement.relation.equals(child.mappedElements.get(child.choice).schemaElement.relation))
					{
						tree.moveSubTree(curNode.parent, child); 
						tree.moveSubTree(child, curNode); 						
					}
					else
					{
						tree.moveSubTree(curNode.parent, child); 
					}
				}
				else
				{
					ParseTreeNode parent = curNode.parent; 
					while(!parent.tokenType.contains("NT"))
					{
						parent = parent.parent; 
					}					
					for(int j = 0; j < curNode.children.size(); j++)
					{
						tree.moveSubTree(parent, curNode.children.get(0)); 
					}					
				}
			}
		}
		
		for(int i = 0; i < tree.allNodes.size(); i++)
		{
			ParseTreeNode curNode = tree.allNodes.get(i); 
			if(curNode.tokenType.contains("OT") && curNode.children.isEmpty())
			{
				if(curNode.parent.tokenType.contains("NT"))
				{
					if(curNode.parent.children.get(0).tokenType.contains("VTNUM"))
					{
						tree.moveSubTree(curNode, curNode.parent.children.get(0)); 
					}
				}
			}
			if(curNode.tokenType.equals("OT") && curNode.children.size() > 1 && curNode.children.get(0).tokenType.contains("VT"))
			{
				while(curNode.children.size() > 1)
				{
					tree.moveSubTree(curNode.parent, curNode.children.get(1)); 
				}
			}
		}	
		
		// move the node that is too deep; 
		for(int i = 0; i < tree.allNodes.size(); i++)
		{
			ParseTreeNode curNode = tree.allNodes.get(i); 
			if(curNode.parent != null && curNode.parent.parent != null)
			{
				if(curNode.parent.parent.tokenType.contains("NT") && curNode.parent.tokenType.contains("NT") && curNode.QT.equals("NA") && curNode.parent.QT.equals("NA")
					&& (curNode.tokenType.contains("NT") || curNode.tokenType.equals("FT") || (curNode.tokenType.equals("OT") && !curNode.children.isEmpty() && curNode.children.get(0).choice >= 0)
					|| (!curNode.parent.mappedElements.get(curNode.parent.choice).schemaElement.equals(curNode.parent.parent.mappedElements.get(curNode.parent.parent.choice).schemaElement) 
					&& curNode.tokenType.contains("VT") && curNode.choice >= 0 
					&& !curNode.parent.mappedElements.get(curNode.parent.choice).schemaElement.relation.equals(curNode.mappedElements.get(curNode.choice).schemaElement.relation))))
				{
					tree.moveSubTree(curNode.parent.parent, curNode); 
				}
			}
		}
	}
}
