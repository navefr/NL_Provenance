package dataCrawling;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class MASCrawler 
{
	public static ArrayList<String> hosts = new ArrayList<String>(); 
	public static ArrayList<String> ports = new ArrayList<String>(); 
	public static int rand = 0; 
	public static Statement statement; 
	public static Statement statementUpdate; 
	public static ResultSet result; 
	public static int round = 5; 

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
		
//		getPidsCS(); 
//		crawlData("paperIDs"); 
//		crawlData("papers"); 
//		crawlData("authors"); 
//		crawlData("conferences"); 
//		crawlData("journals"); 
//		crawlData("organizations"); 
		
//		crawlData("keywords"); 
//		getPids(); 
//		crawlData("citations"); 
//		getPublicationDomain(); 
//		getKeywordDomain(); 	
//		refineAuthor(); 
		
		crawlData("abstract"); 
	}
	
	// Step 1. get all the pids for computer science papers; 
	public static void getPidsCS() throws SQLException 
	{
		for(int i = 0; i * 100 < 3539285; i++) 
		{
			statementUpdate.executeUpdate("INSERT INTO mas.pages VALUES (\"pid\", " + (i*100 + 1) + ", 0); "); 
		}
	}
	
	// Step 2. get all the pids for computer science papers; 
	// Step 3. get all the papers (together with writes/publication_keywords) according to the pids; 
	// Step 4. get all the authors (together with author_domain) according to the aids in writes; 
	// Step 5. get all the conferences (together with conference_domain) according to the cids in paper; 
	// Step 6. get all the journals (together with journal_domain) according to the jids in paper; 
	// Step 7. get all the organizations according to the oids in authors; 
	// Step 8. get all the keywords according to the kids in publicatin_keywords; 
	// Step 10. crawl the citation of a paper; 
	public static void crawlData(String relation) throws SQLException, ClassNotFoundException
	{		
		try 
		{
			getIP();
		} 
		catch (IOException e) {} 
		
		String get_remain_set = ""; 
		if(relation.equals("paperIDs")) // Step 2. get all the pids for computer science papers; 
		{
			get_remain_set = "SELECT start FROM mas.pages WHERE done = 0; "; 
		}
		if(relation.equals("papers")) // Step 3. get all the papers (together with writes/publication_keywords) according to the pids; 
		{
			get_remain_set = "SELECT id FROM mas.ids WHERE relation = \"pid\" AND exist = 0; "; 
		}
		if(relation.equals("authors")) // Step 4. get all the authors (together with author_domain) according to the aids in writes; 
		{
			get_remain_set = "SELECT DISTINCT aid FROM mas.writes WHERE aid NOT IN (SELECT aid FROM mas.author); "; 
		}
		if(relation.equals("conferences")) // Step 5. get all the conferences (together with conference_domain) according to the cids in paper; 
		{
			get_remain_set = "SELECT DISTINCT cid FROM mas.publication WHERE cid <> 0 AND cid NOT IN (SELECT cid FROM mas.conference); "; 
		}
		if(relation.equals("journals")) // Step 6. get all the journals (together with journal_domain) according to the jids in paper; 
		{
			get_remain_set = "SELECT DISTINCT jid FROM mas.publication WHERE jid <> 0 AND jid NOT IN (SELECT jid FROM mas.journal); ; "; 
		}
		if(relation.equals("organizations")) // Step 7. get all the organizations according to the oids in authors; 
		{
			get_remain_set = "SELECT DISTINCT oid FROM mas.author WHERE oid NOT IN (SELECT oid FROM mas.organization) AND oid <> 0; "; 
		}
		if(relation.equals("keywords")) // Step 8. get all the keywords according to the kids in publicatin_keywords; 
		{
			get_remain_set = "SELECT DISTINCT kid FROM mas.publication_keyword WHERE kid NOT IN (SELECT kid FROM mas.keyword);"; 
		}
		if(relation.equals("citations")) // Step 10. crawl the citation of a paper; 
		{
			if(round == 1)
			{
				get_remain_set = "SELECT id FROM mas.ids WHERE relation = \"citation\" AND exist = 0 AND id IN (SELECT mas_id FROM dblp.map_paper); "; 
			}
			else
			{
				get_remain_set = "SELECT id FROM mas.ids WHERE relation = \"citation\" AND exist = 0 AND id IN (SELECT mas_id FROM dblp.map_paper); "; 
			}
		}
		if(relation.equals("abstract")) 
		{
			get_remain_set = "SELECT id FROM mas.ids WHERE exist = 0 ORDER BY id DESC; "; 
		}

		result = statement.executeQuery(get_remain_set); 

		System.out.println("well prepared! " + get_remain_set); 
		
		int threadNum = 500; 
		for(int i = 0; i < threadNum; i++)
		{
			new MASCrawlerThread(relation, statementUpdate).start(); 
		}
	}
	
	// Step 9. get the pids for crawling citations; 
	public static void getPids() throws SQLException
	{
		String query = ""; 
		if(round == 1)
		{
			query = "SELECT pid FROM mas.publication WHERE reference_num > 0 AND pid NOT IN (SELECT DISTINCT citing FROM mas.cite); "; 
		}
		else 
		{
			query = "SELECT pid FROM mas.publication WHERE reference_num > " + (round-1)*100 + "; "; 
		}
		ResultSet pids = statement.executeQuery(query); 
		while(pids.next())
		{
			statementUpdate.execute("INSERT INTO mas.ids VALUES (\"citation\", " + pids.getInt(1) + ", " + 0 + "); "); 
		}
	}
	
	// Step 11. get the publication_domain: 
	public static void getPublicationDomain() throws SQLException
	{
		ResultSet publication_domain = statement.executeQuery("SELECT pid, did FROM mas.publication, mas.domain_conference WHERE publication.cid = domain_conference.cid AND publication.cid <> 0 AND did <> 0; "); 
		while(publication_domain.next())
		{
			String insert = "INSERT INTO mas.domain_publication VALUES (" + publication_domain.getInt(1) + ", " + publication_domain.getInt(2) + "); "; 
			try
			{
				statementUpdate.executeUpdate(insert); 
			}
			catch(Exception e)
			{
				System.out.println(insert);
			}
		}
		publication_domain.close(); 
		
		publication_domain = statement.executeQuery("SELECT pid, did FROM mas.publication, mas.domain_journal WHERE publication.jid = domain_journal.jid AND publication.jid <> 0 AND did <> 0; "); 
		while(publication_domain.next())
		{
			String insert = "INSERT INTO mas.domain_publication VALUES (" + publication_domain.getInt(1) + ", " + publication_domain.getInt(2) + "); "; 
			try
			{
				statementUpdate.executeUpdate("INSERT INTO mas.domain_publication VALUES (" + publication_domain.getInt(1) + ", " + publication_domain.getInt(2) + "); "); 
			}
			catch(Exception e)
			{
				System.out.println(insert);
			}
		}
		publication_domain.close(); 
	}
	
	// Step 12. get the keyword_domain; 
	public static void getKeywordDomain() throws SQLException
	{
		int pre_kid = 0; 
		int max = 0; 
		int order = 0; 
		ResultSet publication_keyword = statement.executeQuery("SELECT kid, did, count(*) num FROM mas.publication_keyword, mas.domain_publication WHERE publication_keyword.pid = domain_publication.pid"
			+ " GROUP BY kid, did ORDER BY kid ASC, num DESC; "); 
		while(publication_keyword.next())
		{
			int kid = publication_keyword.getInt(1); 
			int did = publication_keyword.getInt(2); 
			int num = publication_keyword.getInt(3); 
			
			if(kid != pre_kid)
			{
				pre_kid = kid; 
				max = num; 
				order = 1; 
				String insert = "INSERT INTO mas.domain_keyword VALUES (" + kid + ", " + did + ", " + order + "); "; 
				try
				{
					statementUpdate.executeUpdate(insert); 
				}
				catch(Exception e)
				{
					System.out.println(insert);
				}
				order++; 
			}
			else
			{
				if(num > max/3)
				{
					String insert = "INSERT INTO mas.domain_keyword VALUES (" + kid + ", " + did + ", " + order + "); "; 
					try
					{
						statementUpdate.executeUpdate(insert); 
					}
					catch(Exception e)
					{
						System.out.println(insert);
					}
					order++; 
				}
			}
		}
		publication_keyword.close(); 
	}
	
	// Step 13. refine the author name; 
	public static void refineAuthor() throws SQLException
	{
		ResultSet rs = statement.executeQuery("SELECT aid, name FROM mas.author WHERE name LIKE \"%(%\"");
		while(rs.next())
		{
			int aid = rs.getInt(1); 
			String name = rs.getString(2); 
			if(name.contains("(") && name.contains(")"))
			{
				name = name.substring(0, name.indexOf("(")); 
				while(name.endsWith(" "))
				{
					name = name.substring(0, name.length()-1); 
				}
				statementUpdate.executeUpdate("UPDATE mas.author SET name = \"" + name + "\" WHERE aid = " + aid); 
			}
		}
		rs.close(); 
		
		rs = statement.executeQuery("SELECT aid, name FROM mas.author");
		while(rs.next())
		{
			int aid = rs.getInt(1); 
			String name = rs.getString(2); 
			if(!name.equals(DBLPCleaning.deAccent(name)))
			{
				name = DBLPCleaning.deAccent(name); 
				statementUpdate.executeUpdate("UPDATE mas.author SET name = \"" + name + "\" WHERE aid = " + aid); 
			}
		}
		rs.close(); 
	}
	
	// get a page in a synchronized manner; 
	public synchronized static String getAnID(String relation) throws SQLException
	{
		if(result.next()) 
		{
			return result.getString(1); 
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