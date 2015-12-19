package dataStructure;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import rdbms.Edge;
import rdbms.SchemaElement;
import rdbms.SchemaGraph;

import java.util.*;

public class Block 
{
	int blockID = 0;

	public ParseTreeNode blockRoot; 
	public Block outerBlock; 
	public ArrayList<Block> innerBlocks = new ArrayList<Block>(); 

	public ArrayList<ParseTreeNode> allNodes = new ArrayList<ParseTreeNode>(); 
	public ArrayList<Edge> edges = new ArrayList<Edge>(); 

	public ArrayList<SQLElement> selectElements = new ArrayList<SQLElement>(); 
	public ArrayList<Object> fromElements = new ArrayList<Object>(); 
	public ArrayList<String> conditions = new ArrayList<String>(); 
	public ArrayList<SQLElement> groupElements = new ArrayList<SQLElement>();
	
	public String SQL = "";

	public String DATALOGQuery = "";
	public Map<String, Pair<String, Map<String, String>>> DATALOGRuleSQL = new HashMap<>();
    private Map<String, String> elementToVariable = new HashMap<>();
    private Map<String, ParseTreeNode> conditionToNode = new HashMap<>();
    public Map<ParseTreeNode, String> nodeToLiteral = new HashMap<>();

	public Block(int blockID, ParseTreeNode blockRoot)
	{
		this.blockID = blockID; 
		this.blockRoot = blockRoot; 
	}
	
	public void nodeEdgeGen(Block mainBlock, ParseTree queryTree, SchemaGraph graph)
	{
		for(int i = 0; i < this.innerBlocks.size(); i++)
		{
			this.innerBlocks.get(i).nodeEdgeGen(mainBlock, queryTree, graph); 
		}
		
		ArrayList<ParseTreeNode> list = new ArrayList<ParseTreeNode>(); 
		list.add(this.blockRoot); 
		while(!list.isEmpty())
		{
			ParseTreeNode node = list.remove(0); 
			this.allNodes.add(node); 
			list.addAll(node.children); 
		}
		for(int i = 0; i < this.innerBlocks.size(); i++)
		{
			this.allNodes.removeAll(this.innerBlocks.get(i).allNodes); 
		}
		
		for(int i = 0; i < allNodes.size(); i++)
		{
			ParseTreeNode node = allNodes.get(i); 
			if(!node.mappedElements.isEmpty())
			{
				SchemaElement left = node.mappedElements.get(node.choice).schemaElement.relation; 
				boolean containsLeft = false; 
				for(int j = 0; j < this.fromElements.size(); j++)
				{
					if(((SchemaElement)this.fromElements.get(j)).elementID == left.elementID)
					{
						containsLeft = true; 
						break; 
					}
				}
				if(!containsLeft)
				{
					this.fromElements.add(left); 
				}
				
				SchemaElement right = null; 
				if(!node.parent.mappedElements.isEmpty())
				{
					right = node.parent.mappedElements.get(node.parent.choice).schemaElement.relation; 
				}
				else if(node.parent.tokenType.equals("OT") && node.parent.parent != null && !node.parent.parent.mappedElements.isEmpty())
				{
					right = node.parent.parent.mappedElements.get(node.parent.parent.choice).schemaElement.relation; 
				}
				if(right != null)
				{
					edges.addAll(graph.getJoinPath(left, right)); 
				}
			}
		}
	}
	
	public void translate(Block mainBlock, ParseTree queryTree)
	{
		for(int i = 0; i < this.innerBlocks.size(); i++)
		{
			this.innerBlocks.get(i).translate(mainBlock, queryTree); 
		}

		// SELECT
		if(this.blockRoot.tokenType.equals("NT"))
		{
			SQLElement sqlElement = new SQLElement(this, this.blockRoot); 
			selectElements.add(sqlElement); 
		}
		else if(this.blockRoot.tokenType.equals("FT"))
		{
			if(this.blockRoot.children.size() == 1 && this.blockRoot.children.get(0).tokenType.equals("NT"))
			{
				SQLElement sqlElement = new SQLElement(this, this.blockRoot.children.get(0)); 
				selectElements.add(sqlElement); 
			}
			else if(this.blockRoot.children.size() == 1 && this.blockRoot.children.get(0).tokenType.equals("FT"))
			{
				if(this.blockRoot.children.get(0).children.size() == 1 && this.blockRoot.children.get(0).children.get(0).tokenType.equals("NT"))
				{
					SQLElement sqlElement = new SQLElement(this.innerBlocks.get(0), this.blockRoot.children.get(0).children.get(0)); 
					selectElements.add(sqlElement); 
				}
			}
		}
		for(int i = 0; i < this.innerBlocks.size(); i++)
		{
			if(!this.blockRoot.equals(this.innerBlocks.get(i).blockRoot.parent))
			{
				this.selectElements.add(this.innerBlocks.get(i).selectElements.get(0)); 
			}
		}
		if(this.outerBlock != null && this.outerBlock.equals(mainBlock))
		{
			ParseTreeNode relatedInnerNode = this.findRelatedNodeFromSelf(mainBlock); 
			if(relatedInnerNode != null)
			{
				SQLElement sqlElement = new SQLElement(this, relatedInnerNode); 
				selectElements.add(sqlElement); 
			}
		}
		for(int i = 0; i < this.allNodes.size(); i++)
		{
			if(this.allNodes.get(i).QT.equals("each"))
			{
				SQLElement sqlElement = new SQLElement(this, this.allNodes.get(i)); 
				selectElements.add(sqlElement); 
			}
		}
		if(queryTree.root.children.size() > 1 && queryTree.root.children.get(1).children.size() == 2 && queryTree.root.children.get(1).children.get(1).function.equals("max"))
		{
			ParseTreeNode node  = queryTree.root.children.get(1).children.get(1).children.get(0); 
			SQLElement sqlElement = new SQLElement(this, node); 
			selectElements.add(sqlElement); 
		}
	
		// FROM
		for(int i = 0; i < this.edges.size(); i++)
		{
			boolean left = false; 
			boolean right = false; 
			for(int j = 0; j < this.fromElements.size(); j++)
			{
				if(((SchemaElement)this.fromElements.get(j)).elementID == this.edges.get(i).left.relation.elementID)
				{
					left = true; 
					break; 
				}
			}
			for(int j = 0; j < this.fromElements.size(); j++)
			{
				if(((SchemaElement)this.fromElements.get(j)).elementID == this.edges.get(i).right.relation.elementID)
				{
					right = true; 
					break; 
				}
			}
			if(!left)
			{
				this.fromElements.add(this.edges.get(i).left.relation); 
			}
			if(!right)
			{
				this.fromElements.add(this.edges.get(i).right.relation); 
			}
		}
		this.fromElements.addAll(this.innerBlocks); 
		
		// WHERE
		if(this.equals(mainBlock) && queryTree.root.children.size() > 1 && this.innerBlocks.size() > 0)
		{
			for(int i = 1; i < queryTree.root.children.size(); i++)
			{
				ParseTreeNode complexCondition = queryTree.root.children.get(i); 
				ParseTreeNode right = complexCondition.children.get(1); 

				String condition = ""; 
				condition += this.innerBlocks.get(0).selectElements.get(0).toString(this, ""); 
				condition += " " + complexCondition.function + " "; 
				if(this.innerBlocks.size() > 1)
				{
					condition += this.innerBlocks.get(1).selectElements.get(0).toString(this, ""); 
				}
				else
				{
					condition += right.label; 
				}
				this.conditions.add(condition);

                // TODO NAVE - does the complexCondition node is the value?
                conditionToNode.put(condition, complexCondition);
			}
		}
		for(int i = 0; i < this.allNodes.size(); i++)
		{
			ParseTreeNode curNode = this.allNodes.get(i); 
			if(!curNode.tokenType.equals("NT") && !curNode.mappedElements.isEmpty())
			{
				String condition = ""; 
				condition += curNode.mappedElements.get(curNode.choice).schemaElement.relation.name + "." + curNode.mappedElements.get(curNode.choice).schemaElement.name; 
				if(curNode.parent.tokenType.equals("OT"))
				{
					condition += " " + curNode.parent.function + " "; 
				}
				else if(curNode.mappedElements.get(curNode.choice).choice == -1)
				{
					condition += " LIKE \"%";
				}
				else
				{
					condition += " = "; 
				}
				if(curNode.tokenType.equals("VTNUM"))
				{
					condition += curNode.label; 
				}
				else
				{
					if(curNode.mappedElements.get(curNode.choice).choice == -1)
					{
						condition += curNode.label + "%\"";
					}
					else
					{
						condition += "\"" + curNode.mappedElements.get(curNode.choice).mappedValues.get(curNode.mappedElements.get(curNode.choice).choice) + "\"";
					}
				}
				this.conditions.add(condition);

                conditionToNode.put(condition, curNode);
			}
		}
		if(this.equals(mainBlock))
		{
			for(int i = 0; i < this.innerBlocks.size(); i++)
			{
				ParseTreeNode innerRelated = this.innerBlocks.get(i).findRelatedNodeFromSelf(mainBlock); 
				if(innerRelated != null && innerBlocks.get(i).allNodes.contains(innerRelated))
				{
					SQLElement left = new SQLElement(mainBlock, innerRelated); 
					SQLElement right = new SQLElement(innerBlocks.get(i), innerRelated); 
					String condition = left.toString(mainBlock, "") + " = " + right.toString(mainBlock, innerRelated.mappedElements.get(innerRelated.choice).schemaElement.name); 
					conditions.add(condition);

                    conditionToNode.put(condition, innerRelated);
				}
				else if(innerRelated != null)
				{
					SQLElement left = new SQLElement(mainBlock, innerRelated); 
					SQLElement right = new SQLElement(innerBlocks.get(i).innerBlocks.get(0), innerRelated); 
					String condition = left.toString(mainBlock, "") + " = " + right.toString(mainBlock, innerRelated.mappedElements.get(innerRelated.choice).schemaElement.name); 	
					conditions.add(condition);

                    conditionToNode.put(condition, innerRelated);
				}
			}
		}
		for(int i = 0; i < edges.size(); i++)
		{
			this.conditions.add(edges.get(i).edgeToString()); 
		}
		
		// GROUP BY
		if(this.outerBlock != null && this.outerBlock.equals(mainBlock))
		{
			for(int i = 0; i < this.allNodes.size(); i++)
			{
				for(int j = 0; j < this.outerBlock.allNodes.size(); j++)
				{
					if(this.allNodes.get(i).nodeID == this.outerBlock.allNodes.get(j).nodeID)
					{
						SQLElement element = new SQLElement(this, this.allNodes.get(i)); 
						this.groupElements.add(element); 
					}
				}
			}
		}
		for(int i = 0; i < this.allNodes.size(); i++)
		{
			if(this.allNodes.get(i).QT.equals("each"))
			{
				SQLElement sqlElement = new SQLElement(this, this.allNodes.get(i)); 
				this.groupElements.add(sqlElement); 
			}
		}
		
		SQLGen();
        try {
            DATALOGQueryGen();
            DATALOGRuleGen();
        } catch (Exception ignored) {}
	}
	
	public void SQLGen()
	{
		this.SQL += "SELECT ";
		if(this.outerBlock == null)
		{
			this.SQL += "DISTINCT "; 
		}
		for(int i = 0; i < this.selectElements.size(); i++)
		{
			if(i != 0)
			{
				this.SQL += ", "; 
			}
			if(this.selectElements.get(i).block.equals(this) && this.selectElements.get(i).node.parent.tokenType.equals("FT"))
			{
				this.SQL += this.selectElements.get(i).node.parent.function + "("; 
			}
			else if(this.selectElements.get(i).block.outerBlock != null && this.selectElements.get(i).block.outerBlock.equals(this)
				&& this.selectElements.get(i).node.parent.parent != null && this.selectElements.get(i).node.parent.parent.tokenType.equals("FT"))
			{
				this.SQL += this.selectElements.get(i).node.parent.parent.function + "("; 
			}
			this.SQL += this.selectElements.get(i).toString(this, ""); 
			if(this.selectElements.get(i).block.equals(this) && this.selectElements.get(i).node.parent.tokenType.equals("FT"))
			{
				this.SQL += ")"; 
			}
			else if(this.selectElements.get(i).block.outerBlock != null && this.selectElements.get(i).block.outerBlock.equals(this)
					&& this.selectElements.get(i).node.parent.parent != null && this.selectElements.get(i).node.parent.parent.tokenType.equals("FT"))
			{
				this.SQL += ")"; 
			}
			
			if(i == 0 && this.outerBlock != null)
			{
				this.SQL += " as " + this.blockRoot.function; 
			}
		}
		
		if(this.outerBlock == null)
		{
			this.SQL += "\n"; 
		}
		else
		{
			this.SQL += " "; 
		}
		this.SQL += "FROM "; 
		for(int i = 0; i < this.fromElements.size(); i++)
		{
			if(i != 0)
			{
				this.SQL += ", "; 
				if(this.fromElements.get(i-1).getClass().equals(this.getClass()))
				{
					this.SQL += "\n"; 
				}
			}
			
			if(this.fromElements.get(i).getClass().equals(this.getClass()))
			{
				this.SQL += "("; 
				this.SQL += ((Block) this.fromElements.get(i)).SQL; 
				this.SQL += ") block_";
				this.SQL += ((Block) this.fromElements.get(i)).blockID; 
			}
			else 
			{
				this.SQL += ((SchemaElement) this.fromElements.get(i)).name; 
			}
		}
		
		if(!this.conditions.isEmpty())
		{
			if(this.outerBlock == null)
			{
				this.SQL += "\n"; 
			}
			else
			{
				this.SQL += " "; 
			}

			this.SQL += "WHERE "; 
			for(int i = 0; i < this.conditions.size(); i++)
			{
				if(i != 0)
				{
					this.SQL += " AND "; 
				}
				this.SQL += conditions.get(i); 
			}
		}
		
		if(!this.groupElements.isEmpty())
		{
			if(this.outerBlock == null)
			{
				this.SQL += "\n"; 
			}
			else
			{
				this.SQL += " "; 
			}

			this.SQL += "GROUP BY "; 
			for(int i = 0; i < this.groupElements.size(); i++)
			{
				if(i != 0)
				{
					this.SQL += ", "; 
				}
				this.SQL += this.groupElements.get(i).toString(this, ""); 
			}
		}
	}

    private Pair<String, String> getJoinCondition(String condition) {
        String[] split = condition.split(" |=");
        String left = split[0].trim();
        String right = split[split.length - 1].trim();
        if (elementToVariable.containsKey(left) && elementToVariable.containsKey(right)) {
            return new ImmutablePair<>(left, right);
        } else {
            return null;
        }
    }

    private Collection<String> initializeVariablesAndGetFilteredCondition() {
        for (Object fromElement : this.fromElements) {
            String elementName = ((SchemaElement) fromElement).name;
            for (SchemaElement attribute : ((SchemaElement) fromElement).attributes) {
                String attributeName = attribute.name;
                getVariable(elementName + "." + attributeName);
            }
        }

        Collection<String> filteredCondition = new ArrayList<>();
        for (String condition : this.conditions) {
            if (condition.matches(".*=.*")) {
                Pair<String, String> joinCondition = getJoinCondition(condition);
                if (joinCondition != null)  {
                    String left = joinCondition.getLeft();
                    String right = joinCondition.getRight();
                    elementToVariable.put(right, elementToVariable.get(left));
                } else {
                    String newCondition = replaceVariablesInCondition(condition);
                    filteredCondition.add(newCondition);

                    nodeToLiteral.put(conditionToNode.get(condition), newCondition);
                }
            } else {
                String newCondition = replaceVariablesInCondition(condition);
                filteredCondition.add(newCondition);

                nodeToLiteral.put(conditionToNode.get(condition), newCondition);
            }
        }

        return filteredCondition;
    }

    private String replaceVariablesInCondition(String condition) {
        String newCondition = "";
        String[] split = condition.split(" ");
        boolean isFirst = true;
        for (String s : split) {
            if (isFirst) {
                isFirst = false;
            } else {
                newCondition += " ";
            }

            String variable = elementToVariable.get(s);
            if (variable == null) {
                newCondition += s;
            } else {
                newCondition += variable;
            }
        }

        return newCondition;
    }

    private String getVariable(String element) {
        if (elementToVariable.containsKey(element)) {
            return elementToVariable.get(element);
        }

        String variable = generateVariableName(element);
        elementToVariable.put(element, variable);
        return variable;
    }

    private String generateVariableName(String element) {
        return "?" + element.replaceAll("\\.|_", "");
    }

    public void DATALOGQueryGen()
    {
        Collection<String> filteredCondition = initializeVariablesAndGetFilteredCondition();
        this.DATALOGQuery += "query(";
        for(int i = 0; i < this.selectElements.size(); i++)
        {
            if(i != 0)
            {
                this.DATALOGQuery += ", ";
            }
            // TODO NAVE - Will it work?
            if(this.selectElements.get(i).block.equals(this) && this.selectElements.get(i).node.parent.tokenType.equals("FT"))
            {
                this.DATALOGQuery += this.selectElements.get(i).node.parent.function + "(";
            }
            else if(this.selectElements.get(i).block.outerBlock != null && this.selectElements.get(i).block.outerBlock.equals(this)
                    && this.selectElements.get(i).node.parent.parent != null && this.selectElements.get(i).node.parent.parent.tokenType.equals("FT"))
            {
                this.DATALOGQuery += this.selectElements.get(i).node.parent.parent.function + "(";
            }
            this.DATALOGQuery += getVariable(this.selectElements.get(i).toString(this, ""));
            if(this.selectElements.get(i).block.equals(this) && this.selectElements.get(i).node.parent.tokenType.equals("FT"))
            {
                this.DATALOGQuery += ")";
            }
            else if(this.selectElements.get(i).block.outerBlock != null && this.selectElements.get(i).block.outerBlock.equals(this)
                    && this.selectElements.get(i).node.parent.parent != null && this.selectElements.get(i).node.parent.parent.tokenType.equals("FT"))
            {
                this.DATALOGQuery += ")";
            }
        }
        this.DATALOGQuery += ")";

        // Todo nave - Im not sure it will be good in case there are multiple elements.
        for (SQLElement selectElement : this.selectElements) {
            nodeToLiteral.put(selectElement.node, this.DATALOGQuery);
        }
        this.DATALOGQuery += " :- ";


        for(int i = 0; i < this.fromElements.size(); i++)
        {
            if(i != 0)
            {
                this.DATALOGQuery += ", ";
            }

            this.DATALOGQuery += ((SchemaElement) this.fromElements.get(i)).name;
            this.DATALOGQuery += "(";

            for(int j = 0; j < ((SchemaElement) this.fromElements.get(i)).attributes.size(); j++)
            {
                if(j != 0)
                {
                    this.DATALOGQuery += ", ";
                }
                String elementName = ((SchemaElement) this.fromElements.get(i)).name;
                String attributeName = ((SchemaElement) this.fromElements.get(i)).attributes.get(j).name;
                this.DATALOGQuery += getVariable(elementName + "." + attributeName);
            }
            this.DATALOGQuery += ")";
        }

        for(String condition : filteredCondition)
        {
            this.DATALOGQuery += ", ";
            this.DATALOGQuery += condition;
        }

        // TODO NAVE - DONT KNOW
        if(!this.groupElements.isEmpty())
        {
            if(this.outerBlock == null)
            {
                this.DATALOGQuery += "\n";
            }
            else
            {
                this.DATALOGQuery += " ";
            }

            this.DATALOGQuery += "GROUP BY ";
            for(int i = 0; i < this.groupElements.size(); i++)
            {
                if(i != 0)
                {
                    this.DATALOGQuery += ", ";
                }
                this.DATALOGQuery += this.groupElements.get(i).toString(this, "");
            }
        }
    }

    public void DATALOGRuleGen() {
        for (Object fromElement : this.fromElements) {
            String elementName = ((SchemaElement) fromElement).name;
            Map<String, String> dependencies = new HashMap<>();
            String query = "SELECT ";
            boolean isFirst = true;
            for (SchemaElement attribute : ((SchemaElement) fromElement).attributes) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    query += ", ";
                }
                query += attribute.name;
            }
            query += " FROM ";

            query += elementName;

            boolean firstRelevantCondition = true;
            for (String condition : this.conditions) {
                Pair<String, String> joinCondition = getJoinCondition(condition);
                if (joinCondition == null) {
                    if (condition.contains(elementName)) {
                        if (firstRelevantCondition) {
                            query += " WHERE ";
                            firstRelevantCondition = false;
                        } else {
                            query += " AND ";
                        }
                        query += condition;
                    }
                } else {
                    String left = joinCondition.getLeft();
                    String right = joinCondition.getRight();
                    if (left.contains(elementName)) {
                        dependencies.put(left, right);
                    } else if (right.contains(elementName)) {
                        dependencies.put(right, left);
                    }
                }
            }
            this.DATALOGRuleSQL.put(elementName, new ImmutablePair<String, Map<String, String>>(query, dependencies));
        }
    }

    public ParseTreeNode findRelatedNodeFromSelf(Block mainBlock)
	{
		ParseTreeNode outerNT = mainBlock.blockRoot; 		
		if(mainBlock.blockRoot.equals("FT"))
		{
			outerNT = mainBlock.blockRoot.children.get(0); 
		}
		
		LinkedList<ParseTreeNode> nodeList = new LinkedList<ParseTreeNode>(); 
		nodeList.add(this.blockRoot); 
		while(!nodeList.isEmpty())
		{
			ParseTreeNode innerNT = nodeList.removeLast();; 
			if(innerNT.tokenType.equals("NT"))
			{
				if(innerNT.nodeID == outerNT.nodeID)
				{
					return innerNT; 
				}
			}
			nodeList.addAll(innerNT.children); 
		}
		
		return null; 
	}

	public void printForCheck()
	{
		System.out.print("block_" + blockID + " root: " + blockRoot.label); 
		if(outerBlock != null)
		{
			System.out.print("; outer: block_" + outerBlock.blockID); 
		}
		if(innerBlocks.size() > 0)
		{
			System.out.print("; inner: " ); 			
		}
		for(int i = 0; i < innerBlocks.size(); i++)
		{
			System.out.print("block_" + innerBlocks.get(i).blockID + " "); 
		}
		System.out.println(); 
		
		System.out.println(this.SQL); 		
		System.out.println(); 
	}
}