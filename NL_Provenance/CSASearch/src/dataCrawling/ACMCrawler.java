package dataCrawling;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class ACMCrawler 
{
	public static ArrayList<String> hosts = new ArrayList<String>(); 
	public static ArrayList<String> ports = new ArrayList<String>(); 
	public static int rand = 0; 
	public static Statement statement; 
	public static Statement statementUpdate; 
	public static ResultSet result; 
	
	public static int STARTING = 1; 
	public static int ENDING = 2754632; 

	public static void main(String [] args) throws IOException, ClassNotFoundException, SQLException
	{
		String driver = "com.mysql.jdbc.Driver"; 
		String db_url = "jdbc:mysql://127.0.0.1:3306/";
		String user = "root";
		String password = "";
		Class.forName(driver);
		Connection conn = DriverManager.getConnection(db_url, user, password);
		Connection connUpdate = DriverManager.getConnection(db_url, user, password);
		
		statement = conn.createStatement();
		statementUpdate = connUpdate.createStatement(); 
		
//		getPaperIDs(statement, statementUpdate); 
		crawlPapers(); 
	}
	
	// Step 1. add all the pids to paper_id; from STARTING to ENDING; 
	public static void getPaperIDs(Statement statement, Statement statementUpdate) throws SQLException
	{
		for(int i = STARTING; i < ENDING; i++)
		{
			statement.executeUpdate("INSERT INTO ACM.paper_id VALUES (" + i + ", 0)"); 
		}
	}
	
	// Step 2. crawl all publications, together with their citations and references; 
	public static void crawlPapers() throws SQLException, ClassNotFoundException
	{		
		try 
		{
			getIP();
		} 
		catch (IOException e) {} 
		
		String get_remain_set = "SELECT pid FROM ACM.paper_id WHERE done = 0; "; 		
		result = statement.executeQuery(get_remain_set); 

		System.out.println("well prepared! " + get_remain_set); 
		
		int threadNum = 500; 
		for(int i = 0; i < threadNum; i++)
		{
			new ACMCrawlerThread(statementUpdate).start(); 
		}
	}
	
	// get a page in a synchronized manner; 
	public synchronized static String getAnID() throws SQLException
	{
		if(result.next()) 
		{
			return "" + result.getInt(1); 
		}
		
		return null; 
	}
	
	// get a new ip; 
	public synchronized static void getIP() throws IOException, SQLException
	{
		if(hosts.size() == 0)
		{
			ResultSet ip = statementUpdate.executeQuery("SELECT * FROM dblp.ip"); 
			while(ip.next())
			{
				hosts.add(ip.getString(1)); 
				ports.add(ip.getString(2));
			}
		}

		rand = (int)(Math.random() * 997542); 
		System.setProperty("http.maxRedirects", "50");  
		System.getProperties().setProperty("proxySet", "true");  
		System.getProperties().setProperty("http.proxyHost", hosts.get(rand%hosts.size()));
		System.getProperties().setProperty("http.proxyPort", ports.get(rand%hosts.size()));
	}
}
