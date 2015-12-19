package nalirTools;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import nalirDataStructure.ParseTree;
import nalirDataStructure.ParseTreeNode;
import nalirDataStructure.Query;

public class PrintForCheck 
{
	public static JSONObject jsonOutput(Query query)
	{
		JSONObject tokenizedParseTree = nodeToJSON(query.adjustedParseTree.root); 
		return tokenizedParseTree; 
	}
	
	@SuppressWarnings("unchecked")
	public static JSONObject nodeToJSON(ParseTreeNode node)
	{
		JSONObject jsonNode = new JSONObject();
		JSONArray attributes = new JSONArray(); 
		
		JSONObject id = new JSONObject(); 
		id.put("node_id", node.nodeID); 
		attributes.add(id); 

		JSONObject label = new JSONObject(); 
		label.put("label", node.label); 
		attributes.add(label); 
		
		JSONObject type = new JSONObject(); 
		type.put("type", node.tokenType);  
		attributes.add(type); 

		JSONObject function = new JSONObject(); 
		if(!node.function.equals("NA") && !node.function.isEmpty())
		{
			function.put("function", node.function); 
		}
		else
		{
			function.put("function", "NA"); 
		}
		attributes.add(function); 

		JSONObject quantifier = new JSONObject(); 
		if(!node.QT.equals("NA") && !node.QT.isEmpty())
		{
			quantifier.put("quantifier", node.QT); 
		}
		else
		{
			quantifier.put("quantifier", "NA"); 
		}
		attributes.add(quantifier); 

		JSONObject mappedSchemaElement = new JSONObject(); 
		JSONObject mappedValue = new JSONObject(); 
		if(node.mappedElements.size() > 0 && node.choice >= 0)
		{
			mappedSchemaElement.put("mappedSchemaElement", node.mappedElements.get(node.choice).schemaElement.printForCheck()); 
			if(node.mappedElements.get(node.choice).mappedValues.size() > 0 && node.mappedElements.get(node.choice).choice >= 0)
			{
				String value = node.mappedElements.get(node.choice).mappedValues.get(node.mappedElements.get(node.choice).choice); 
				if(value.contains(" (#"))
				{
					value = value.substring(0, value.indexOf(" (#")); 
				}
				mappedValue.put("mappedValue", value); 
			}
			else
			{
				mappedValue.put("mappedValue", "NA"); 
			}
		}
		else
		{
			mappedSchemaElement.put("mappedSchemaElement", "NA"); 
			mappedValue.put("mappedValue", "NA"); 
		}
		attributes.add(mappedSchemaElement); 
		attributes.add(mappedValue); 
		
		JSONObject child = new JSONObject(); 
		JSONArray children = new JSONArray(); 
		for(int i = 0; i < node.children.size(); i++)
		{
			children.add(nodeToJSON(node.children.get(i))); 
		}
		if(!children.isEmpty())
		{
			child.put("children", children); 
		}
		else
		{
			child.put("children", "NA"); 
		}
		attributes.add(child); 
		jsonNode.put("attributes", attributes); 
		
		return jsonNode; 
	}
	
	public static String allParseTreeNodePrintForCheck(ParseTree parseTree)
	{
		String results = ""; 
		for(int i = 0; i < parseTree.allNodes.size(); i++)
		{
			ParseTreeNode node = parseTree.allNodes.get(i); 
			String result = "";
			result += node.nodeID + ". "; 
			result += node.label + ": "; 
			result += node.tokenType + "; "; 
			result += node.function + "; "; 
			result += node.QT + "; ";
			result += "(" + node.choice + ") "; 

			if(node.mappedElements.size() > 0)
			{
				for(int j = 0; j < node.mappedElements.size() && j < 5; j++)
				{
					result += node.mappedElements.get(j).printForCheck() + "| "; 
				}
			}
			
			results += result +"\n"; 
		}
		
		return results; 
	}
}
