package nalirDBMS;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import nalirDataStructure.MappedSchemaElement;
import nalirTools.SimFunctions;

@SuppressWarnings("serial")
public class SchemaElement implements Serializable
{
	public int elementID = 0; 
	public String name = ""; // relation_name or attribute_name
	public String type = ""; // entity, relationship, pk, fk, text, trival_text, number; 

	public SchemaElement relation; // for pk, fk, text, trival_text, number; 
	
	public ArrayList<SchemaElement> attributes = new ArrayList<SchemaElement>(); // for entity and relationship; 
	
	public SchemaElement pk; // for entity
	public SchemaElement defaultAttribute; 
	public ArrayList<SchemaElement> inElements = new ArrayList<SchemaElement>(); 
	
	public SchemaElement(int elementID, String name, String type)
	{
		this.elementID = elementID; 
		this.name = name; 
		this.type = type; 
	}
	
	public MappedSchemaElement isSchemaExist(String tag) throws Exception
	{
		if(this.equals(this.relation.defaultAttribute))
		{
			if(SimFunctions.ifSchemaSimilar(this.relation.name, tag) || SimFunctions.ifSchemaSimilar(name, tag))
			{
				MappedSchemaElement mappedSchemaElement = new MappedSchemaElement(this); 
				mappedSchemaElement.similarity = SimFunctions.similarity(this.relation.name, tag); 
				mappedSchemaElement.similarity = 1-(1-mappedSchemaElement.similarity)*(1-SimFunctions.similarity(name, tag)); 
				return mappedSchemaElement; 
			}			
		}
		else if(SimFunctions.ifSchemaSimilar(name, tag))
		{
			MappedSchemaElement mappedSchemaElement = new MappedSchemaElement(this); 
			mappedSchemaElement.similarity = SimFunctions.similarity(name, tag); 
			return mappedSchemaElement; 
		}
		return null; 
	}
	
	public MappedSchemaElement isTextExist(String value, Connection conn) throws Exception 
	{
		Statement statement = conn.createStatement(); 
		
		String SQL = ""; 
		if(name.equals("domain"))
		{
			SQL = "SELECT importance, name FROM dblp_plus.domain; "; 
		}
		else if(name.equals("conference"))
		{
			SQL = "SELECT importance, name, full_name FROM dblp_plus.conference; "; 
		}
		else if(name.equals("journal"))
		{
			SQL = "SELECT importance, name, full_name FROM dblp_plus.journal; "; 
		}
		else if(name.equals("organization"))
		{
			SQL = "SELECT importance, name, name_short FROM dblp_plus.organization WHERE name LIKE \"" + value + "\" OR name_short LIKE \"" + value + "\" LIMIT 0, 2000; ";
		}
		else if(name.equals("keyword"))
		{
			SQL = "SELECT importance, keyword, keyword_short FROM dblp_plus.keyword WHERE keyword LIKE \"" + value + "\" OR keyword_short LIKE \"" + value + "\" LIMIT 0, 2000; ";
		}
		else if(name.equals("publication"))
		{
			SQL = "SELECT importance, title FROM dblp_plus.publication WHERE MATCH(title) AGAINST ('" +  value + "') LIMIT 0, 2000"; 
		}
		else if(name.equals("author"))
		{
			SQL = "SELECT importance, name FROM dblp_plus.author WHERE MATCH(name) AGAINST ('" +  value + "') LIMIT 0, 2000"; 
		}
		ResultSet result = statement.executeQuery(SQL); 
		
		MappedSchemaElement mappedSchemaElement = new MappedSchemaElement(this);  
		while(result.next())
		{
			String text = result.getInt(1) + "###"; 
			text += result.getString(2); 
			try
			{
				text += "###" + result.getString(3); 
			}
			catch(Exception e)
			{}
			mappedSchemaElement.mappedValues.add(text); 
		}
		if(!mappedSchemaElement.mappedValues.isEmpty())
		{
			return mappedSchemaElement; 
		}
		
		return null;
	}
	
	public MappedSchemaElement isNumExist(String number, String operator, Connection conn) throws Exception 
	{
		Statement statement = conn.createStatement(); 
		String query = "SELECT " + this.name + " FROM " + this.relation.name + " WHERE " + this.name + operator + " " + number + " LIMIT 0, 5"; 
		
		ResultSet result = statement.executeQuery(query); 
		MappedSchemaElement mappedSchemaElement = new MappedSchemaElement(this);  
		while(result.next())
		{
			int mapNum = result.getInt(1); 
			String mapNumber = "" + mapNum;  
			mappedSchemaElement.mappedValues.add(mapNumber); 
		}
		if(!mappedSchemaElement.mappedValues.isEmpty())
		{
			return mappedSchemaElement; 
		}
		
		return null;
	}

	public String printForCheck() 
	{
		String result = ""; 
		if(type.equals("entity") || type.equals("relationship"))
		{
			result += relation.name; 
		}
		else
		{
			result += relation.name + "." + name; 
		}
		
		return result;
	}
}
