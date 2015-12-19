package nalirComponents;

import java.util.ArrayList;

import nalirDBMS.SchemaElement;
import nalirDataStructure.EntityPair;
import nalirDataStructure.ParseTree;
import nalirDataStructure.ParseTreeNode;
import nalirDataStructure.Query;
import nalirTools.BasicFunctions;

public class EntityResolution 
{
	public static void entityResolute(Query query)
	{
		query.adjustedParseTree = (ParseTree) BasicFunctions.depthClone(query.parseTree); 

		ArrayList<ParseTreeNode> nodes = query.adjustedParseTree.allNodes; 
				
		for(int i = 0; i < nodes.size(); i++)
		{
			ParseTreeNode left = nodes.get(i); 
			if(left.getChoiceMap() == null)
			{
				continue; 
			}
			SchemaElement leftMap = left.getChoiceMap().schemaElement; 

			for(int j = i+1; j < nodes.size(); j++)
			{
				ParseTreeNode right = nodes.get(j); 
				if(right.getChoiceMap() == null)
				{
					continue; 
				}
				SchemaElement rightMap = right.getChoiceMap().schemaElement; 
				
				if(leftMap.equals(rightMap))
				{
					if(left.tokenType.equals("VTTEXT") && right.tokenType.equals("VTTEXT"))
					{
						if(left.label.equals(right.label))
						{
							EntityPair entityPair = new EntityPair(left, right);
							query.entities.add(entityPair); 
						}
						else
						{
							continue; 
						}
					}
					
					if((left.tokenType.equals("VTTEXT") && right.tokenType.equals("NT"))
						||(left.tokenType.equals("NT") && right.tokenType.equals("VTTEXT"))
						||(left.tokenType.equals("NT") && right.tokenType.equals("NT")))
					{
						if(Math.abs(left.wordOrder - right.wordOrder) > 2)
						{
							continue; 
						}
						else
						{
							if(left.parent.tokenType.equals("CMT") || left.parent.tokenType.equals("FT") 
								|| right.parent.tokenType.equals("CMT") || right.parent.tokenType.equals("FT"))
							{
								continue; 
							}
							EntityPair entityPair = new EntityPair(left, right);
							query.entities.add(entityPair); 
						}
					}
				}
			}		
		}
	}
}
