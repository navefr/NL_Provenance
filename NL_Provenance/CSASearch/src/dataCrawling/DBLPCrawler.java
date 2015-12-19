package dataCrawling;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;

public class DBLPCrawler 
{
	public static ArrayList<String> hosts = new ArrayList<String>(); 
	public static ArrayList<String> ports = new ArrayList<String>(); 
	public static int rand = 0; 
	public static Statement statement; 
	public static Statement statementUpdate; 
	public static ResultSet result; 

	public static int AUTHORSIZE = 1562000; 
		
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
		
//		getAuthorList(statement, statementUpdate); 
//		crawlData("authorID"); 
//		crawlData("author"); 
//		cleanAuthor(statement, statementUpdate); 
//		cleanPublicationCJ(statement, statementUpdate); 
//		crawlData("conference"); 
		crawlData("journal"); 
	}
	
	// Step 1. compute the entire page list in http://dblp.uni-trier.de/pers?pos=1; 
	public static void getAuthorList(Statement statement, Statement statementUpdate) throws SQLException
	{
		for(int i = 1; i < AUTHORSIZE; i += 300)
		{
			String command = "INSERT INTO dblp.page VALUES (\"authorPageList\", " + i + ", 0)"; 
			statementUpdate.executeUpdate(command); 
		}
	}
	
	// Step 2. crawl the ids for each author; 
	// Step 3. crawl authors, together with their publications; 
	// Step 6. crawl the conferences; 
	// Step 7. crawl the journals; 
	public static void crawlData(String relation) throws SQLException, ClassNotFoundException
	{		
		try 
		{
			getIP();
		} 
		catch (IOException e) {} 
		
		String get_remain_set = ""; 
		if(relation.equals("authorID")) // Step 2. get the ids for each author; 
		{
			get_remain_set = "SELECT page_id FROM dblp.page WHERE relation = \"authorPageList\" AND exist = 0"; 
		}
		if(relation.equals("author")) // Step 3. get authors, together with their publications; 
		{
			get_remain_set = "SELECT dblp_id FROM dblp.author WHERE name = \"\""; 
		}
		if(relation.equals("conference")) // Step 6. get the conferences; 
		{
			get_remain_set = "SELECT DISTINCT conference FROM dblp.publication WHERE conference <> \"\" AND conference NOT IN (SELECT cid FROM dblp.conference); "; 
		}
		if(relation.equals("journal")) // Step 7. get the journals; 
		{
			get_remain_set = "SELECT DISTINCT journal FROM dblp.publication WHERE journal <> \"\" AND journal NOT IN (SELECT jid FROM dblp.journal); "; 
		}
		result = statement.executeQuery(get_remain_set); 

		System.out.println("well prepared! " + get_remain_set); 
		
		int threadNum = 500; 
		for(int i = 0; i < threadNum; i++)
		{
			new DBLPCrawlerThread(relation, statementUpdate).start(); 
		}
	}
	
	// Step 4. clean the authors, delete the duplicated ones; 
	public static void cleanAuthor(Statement statement, Statement statementUpdate) throws SQLException
	{
		int count = 0; 
		
		String deleteUseless = "DELETE FROM dblp.author WHERE bib_url = \"\""; 
		statementUpdate.executeUpdate(deleteUseless); 

		deleteUseless = "DELETE FROM dblp.author WHERE bib_url is NULL"; 
		statementUpdate.executeUpdate(deleteUseless); 
		
		Hashtable<String, String> bib_urls = new Hashtable<String, String>(); 
		
		String readAllAuthors = "SELECT dblp_id, bib_url FROM dblp.author; "; 
		ResultSet results = statement.executeQuery(readAllAuthors); 
		while(results.next())
		{
			String dblp_id = results.getString(1); 
			String bib_url = results.getString(2); 
			
			if(bib_urls.containsKey(bib_url))
			{
				String commandUpdate = "DELETE FROM dblp.author WHERE dblp_id = \"" + dblp_id + "\"; "; 
				statementUpdate.executeUpdate(commandUpdate); 
				
				commandUpdate = "UPDATE dblp.writes SET aid = \"" + bib_urls.get(bib_url) + "\" WHERE aid = \"" + dblp_id + "\"; ";  
				try
				{
					statementUpdate.executeUpdate(commandUpdate); 
				}
				catch(Exception e)
				{}
			}
			else
			{
				bib_urls.put(bib_url, dblp_id); 
			}
			
			count++; 
			if(count % 100000 == 0)
			{
				System.out.println(count);
			}
		}
	}
	
	// Step 5. clean publication's conference journal information; 
	public static void cleanPublicationCJ(Statement statement, Statement statementUpdate) throws SQLException
	{
		int count = 0; 
		
		String publicationConference = "SELECT DISTINCT rawC FROM dblp.publication WHERE rawC <> \"\"; "; 
		ResultSet results = statement.executeQuery(publicationConference); 
		while(results.next())
		{
			String raw_conference = results.getString(1); 
			String [] phrases = raw_conference.split("/"); 
			String conference = raw_conference.substring(0, raw_conference.length()-phrases[phrases.length-1].length()-1); 
			
			String commandUpdate = "UPDATE dblp.publication SET conference = \"" + conference + "\" WHERE rawC = \"" + raw_conference + "\"; ";  
			statementUpdate.executeUpdate(commandUpdate); 
			
			count++; 
			if(count % 1000 == 0)
			{
				System.out.println(count);
			}
		}
		results.close();
		
		String publicationJournal = "SELECT DISTINCT rawJ FROM dblp.publication WHERE rawJ <> \"\"; "; 
		results = statement.executeQuery(publicationJournal); 
		while(results.next())
		{
			String raw_Journal = results.getString(1); 
			String [] phrases = raw_Journal.split("/"); 
			String conference = raw_Journal.substring(0, raw_Journal.length()-phrases[phrases.length-1].length()-1); 
			
			String commandUpdate = "UPDATE dblp.publication SET journal = \"" + conference + "\" WHERE rawJ = \"" + raw_Journal + "\"; ";  
			statementUpdate.executeUpdate(commandUpdate); 
			
			count++; 
			if(count % 1000 == 0)
			{
				System.out.println(count);
			}
		}
		results.close();	
		
		statementUpdate.executeUpdate("UPDATE dblp.publication SET conference = \"\" WHERE conference is NULL"); 
		statementUpdate.executeUpdate("UPDATE dblp.publication SET journal = \"\" WHERE journal is NULL"); 
	}
	
	// get a page in a synchronized manner; 
	public synchronized static String getAnID(String relation) throws SQLException
	{
		if(result.next()) 
		{
			if(relation.equals("authorID"))
			{
				return ""+result.getInt(1); 
			}
			else 
			{
				return result.getString(1); 
			}
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