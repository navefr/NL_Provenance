package nalirDataStructure;

import nalirDBMS.SchemaElement;

public class SQLTemplate 
{
	public int templateID; 
	public String SQL; 
	public String explanation; 
	
	public SQLTemplate(int id, String sql, String explanation)
	{
		this.templateID = id;
		this.SQL = sql; 
		this.explanation = explanation; 
	}

	public static String getSQL(SQLTemplate T, ParseTree tree)
	{
		String sql = T.SQL; 
		for(int turn = 0; turn < 2 && sql.contains("#"); turn++)
		{
			for(int i = 0; i < tree.allNodes.size(); i++)
			{
				ParseTreeNode node = tree.allNodes.get(i); 
				if(node.tokenType.equals("VTNUM"))
				{
					if(node.choice == -1)
					{
						sql = sql.replaceFirst("#NUM", node.label); 
					}
					else
					{
						SchemaElement element = node.mappedElements.get(node.choice).schemaElement; 
						sql = sql.replaceFirst("#"+element.relation.name+"."+element.name, node.label); 
					}
				}
				else if(node.tokenType.equals("VTTEXT"))
				{
					String label = node.mappedElements.get(node.choice).mappedValues.get(node.mappedElements.get(node.choice).choice); 
					if(label.contains(" (#"))
					{
						label = label.substring(0, label.indexOf(" (#")); 
					}
					label = "\"" + label + "\""; 
					SchemaElement element = node.mappedElements.get(node.choice).schemaElement; 
					sql = sql.replaceFirst("#"+element.relation.name, label); 
				}
			}			
		}
		
		return sql; 
	}
	
	public static String getNL(SQLTemplate T, ParseTree tree)
	{
		String explanation = T.explanation; 
		for(int turn = 0; turn < 2 && explanation.contains("#"); turn++)
		{
			for(int i = 0; i < tree.allNodes.size(); i++)
			{
				ParseTreeNode node = tree.allNodes.get(i); 
				if(node.tokenType.equals("VTNUM"))
				{
					if(node.choice == -1)
					{
						if(turn == 0 && !explanation.contains("#NUM"))
						{
							return ""; 
						}
						
						explanation = explanation.replaceFirst("#NUM", node.label); 
					}
					else
					{
						SchemaElement element = node.mappedElements.get(node.choice).schemaElement; 
						
						if(turn == 0 && !explanation.contains("#"+element.relation.name+"."+element.name))
						{
							return ""; 
						}

						explanation = explanation.replaceFirst("#"+element.relation.name+"."+element.name, node.label); 
					}
				}
				else if(node.tokenType.equals("VTTEXT"))
				{
					String label = node.mappedElements.get(node.choice).mappedValues.get(node.mappedElements.get(node.choice).choice); 
					if(label.contains(" (#"))
					{
						label = label.substring(0, label.indexOf(" (#")); 
					}
					label = "\"" + label + "\""; 
					SchemaElement element = node.mappedElements.get(node.choice).schemaElement; 
					
					if(turn == 0 && !explanation.contains("#"+element.relation.name))
					{
						return ""; 
					}

					explanation = explanation.replaceFirst("#"+element.relation.name, label); 
				}
			}
		}
		return explanation; 
	}
}
