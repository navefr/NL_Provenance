package nalirDBMS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import nalirDataStructure.MappedSchemaElement;
import nalirDataStructure.ParseTreeNode;
import nalirDataStructure.SQLTemplates;

public class RDBMS 
{
	public SchemaGraph schemaGraph; 
	public SQLTemplates templates; 
	public Connection conn; 

	public ArrayList<String> history = new ArrayList<String>(); 

	public RDBMS(String database_name) throws Exception
	{
		String driver = "com.mysql.jdbc.Driver"; 
		String db_url = "jdbc:mysql://127.0.0.1:3306/";
		String user = "root";
		String password = "";
		Class.forName(driver);
		conn = DriverManager.getConnection(db_url, user, password);
		
		Statement statement = conn.createStatement(); 
		statement.execute("use " + database_name); 
		loadHistory(database_name); // load history; 
		templates = new SQLTemplates(); 

		schemaGraph = new SchemaGraph(database_name); // create the schema graph; 
	}
	
	public void loadHistory(String database) throws SQLException // load history; 
	{
		Statement statement = conn.createStatement(); 
		String query = "SELECT * FROM " + database + ".history; "; 
		ResultSet results = statement.executeQuery(query); 
		
		while(results.next())
		{
			history.add(results.getString(2)); 
		}
	}
	
	public boolean isSchemaExist(ParseTreeNode treeNode) throws Exception 
	{
		ArrayList<SchemaElement> attributes = schemaGraph.getElementsByType("text number trival_text"); 
		
		for(int i = 0; i < attributes.size(); i++)
		{
			MappedSchemaElement element = attributes.get(i).isSchemaExist(treeNode.label); 
			if(element != null)
			{
				treeNode.mappedElements.add(element); 
			}
		}
		if(!treeNode.mappedElements.isEmpty())
		{
			return true; 
		}
		else
		{
			return false;
		}
	}
	
	public boolean isTextExist(ParseTreeNode treeNode) throws Exception 
	{
		ArrayList<SchemaElement> entities = schemaGraph.getElementsByType("entity"); 
		for(int i = 0; i < entities.size(); i++)
		{
			MappedSchemaElement entity = entities.get(i).isTextExist(treeNode.label, conn); 
			if(entity != null)
			{
				
				treeNode.mappedElements.add(entity); 
			}
		}
		
		if(!treeNode.mappedElements.isEmpty())
		{
			return true; 
		}
		return false;
	}

	public boolean isNumExist(String operator, ParseTreeNode treeNode) throws Exception 
	{
		ArrayList<SchemaElement> textAtts = schemaGraph.getElementsByType("number"); 
		for(int i = 0; i < textAtts.size(); i++)
		{
			MappedSchemaElement textAtt = textAtts.get(i).isNumExist(treeNode.label, operator, conn); 
			if(textAtt != null)
			{
				treeNode.mappedElements.add(textAtt); 
			}
		}
		
		if(!treeNode.mappedElements.isEmpty())
		{
			return true; 
		}
		return false;
	}
	
	public ArrayList<ArrayList<String>> conductSQL(String query)
	{
		ArrayList<ArrayList<String>> finalResults = new ArrayList<ArrayList<String>>(); 
		try
		{
			Statement statement = conn.createStatement(); 

			ResultSet result = statement.executeQuery(query);
			while(result.next())
			{
				int columnSize = result.getMetaData().getColumnCount(); 
				ArrayList<String> row = new ArrayList<String>(); 
				for(int i = 0; i < columnSize; i++)
				{
					row.add(result.getString(i+1)); 
				}
				finalResults.add(row); 
			}
			
			return finalResults; 
		} catch(Exception e)
		{
			return new ArrayList<ArrayList<String>>(); 
		}
	}
}
