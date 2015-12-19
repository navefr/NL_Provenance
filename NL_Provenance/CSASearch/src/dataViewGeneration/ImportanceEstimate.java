package dataViewGeneration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class ImportanceEstimate 
{
	public static void main(String [] args) throws Exception
	{
		String driver = "com.mysql.jdbc.Driver"; 
		String db_url = "jdbc:mysql://127.0.0.1:3306";
		String user = "root";
		String password = "";
		Class.forName(driver);
		Connection conn = DriverManager.getConnection(db_url, user, password);
		Statement statement = conn.createStatement(); 
		Statement statementUpdate = conn.createStatement(); 
		statement.execute("use dblp_plus; "); 
		
		TupleGraph network = new TupleGraph(); 
		network.buildGraph(statement); 
		for(int i = 0; i < 15; i++)
		{
			network.weightUpdate(statement); 
		}

		network.writeDatabase(statement, statementUpdate); 
	}
}
