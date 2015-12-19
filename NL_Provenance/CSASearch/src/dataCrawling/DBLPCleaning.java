package dataCrawling;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Normalizer;
import java.util.Hashtable;
import java.util.regex.Pattern;

public class DBLPCleaning 
{
	public static void main(String [] args) throws SQLException, ClassNotFoundException
	{
		String driver = "com.mysql.jdbc.Driver"; 
		String db_url = "jdbc:mysql://127.0.0.1:3306/";
		String user = "root";
		String password = "";
		Class.forName(driver);
		Connection conn = DriverManager.getConnection(db_url, user, password);
		Connection connUpdate = DriverManager.getConnection(db_url, user, password);
		
		Statement statement = conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY);
		statement.setFetchSize(Integer.MIN_VALUE);
		Statement statementUpdate = connUpdate.createStatement(); 
		
//		cleanAuthor(statement, statementUpdate); 
//		cleanConference(statement, statementUpdate); 
//		cleanJournal(statement, statementUpdate); 
//		cleanPublication(statement, statementUpdate); 
//		cleanWrites(statement, statementUpdate); 
		cleanCJName(statement, statementUpdate); 
	}
	
	// clean authors; 
	public static void cleanAuthor(Statement statement, Statement statementUpdate) throws SQLException
	{
		ResultSet result = statement.executeQuery("SELECT dblp_id, name, alias, person_info, homepage FROM dblp_raw.author"); 
		for(int i = 1; result.next(); i++)
		{
			String dblp_id = result.getString(1); 
			dblp_id = subURLAuthor(dblp_id); 
			
			String name = result.getString(2); 
			name = deAccent(name); 
			
			String alias = result.getString(3); 
			String person_info = result.getString(4); 
			String homepage = result.getString(5); 
			
			String insert = "INSERT INTO dblp.author VALUES (" + i + ", \"" + name + "\", \"" + alias + "\", \"" + person_info + "\", \"" + homepage + "\", \"" + dblp_id + "\"); "; 
			statementUpdate.executeUpdate(insert); 
			
			if(i % 100000 == 0)
			{
				System.out.println(i);
			}
		}
		result.close();
	}
	
	// clean conferences; 
	public static void cleanConference(Statement statement, Statement statementUpdate) throws SQLException
	{
		ResultSet result = statement.executeQuery("SELECT * FROM dblp_raw.conference"); 
		for(int i = 1; result.next(); i++)
		{
			String dblp_id = result.getString(1).substring(7); 
			String full_name = result.getString(2); 
			String name = dblp_id.substring(26).toUpperCase(); 
			
			String insert = "INSERT INTO dblp.conference VALUES (" + i + ", \"" + name + "\", \"" + full_name + "\", \"" + dblp_id + "\"); "; 
			statementUpdate.executeUpdate(insert); 
		}
		result.close();
	}
	
	// clean journals; 
	public static void cleanJournal(Statement statement, Statement statementUpdate) throws SQLException
	{
		ResultSet result = statement.executeQuery("SELECT * FROM dblp_raw.journal"); 
		for(int i = 1; result.next(); i++)
		{
			String dblp_id = result.getString(1).substring(7); 
			String full_name = result.getString(2); 
			String name = dblp_id.substring(30).toUpperCase(); 
			
			String insert = "INSERT INTO dblp.journal VALUES (" + i + ", \"" + name + "\", \"" + full_name + "\", \"" + dblp_id + "\"); "; 
			statementUpdate.executeUpdate(insert); 
		}
		result.close();
	}

	// clean publications; 
	public static void cleanPublication(Statement statement, Statement statementUpdate) throws SQLException
	{
		Hashtable<String, Integer> conferences = new Hashtable<String, Integer>(); 
		ResultSet result = statement.executeQuery("SELECT cid, dblp_url FROM dblp.conference"); 
		while(result.next())
		{
			conferences.put(result.getString(2), result.getInt(1)); 
		}
		result.close();

		Hashtable<String, Integer> journals = new Hashtable<String, Integer>(); 
		result = statement.executeQuery("SELECT jid, dblp_url FROM dblp.journal"); 
		while(result.next())
		{
			journals.put(result.getString(2), result.getInt(1)); 
		}
		result.close();

		result = statement.executeQuery("SELECT dblp_id, title, year, conference, journal, doi FROM dblp_raw.publication"); 
		for(int i = 1; result.next(); i++)
		{
			String dblp_id = result.getString(1).substring(36); 
			String title = result.getString(2); 
			int year = result.getInt(3); 

			String conference = result.getString(4); 
			if(!conference.isEmpty())
			{
				conference = conference.substring(7); 
			}

			String journal = result.getString(5); 
			if(!journal.isEmpty())
			{
				journal = journal.substring(7); 
			}
			
			String doi = result.getString(6); 
			if(doi.startsWith("https"))
			{
				doi = doi.substring(8); 
			}
			else if(doi.startsWith("ftp"))
			{
				doi = ""; 
			}
			else if(doi.startsWith("http"))
			{
				doi = doi.substring(7); 
			}
			
			int cid = 0; 
			int jid = 0; 
			if(conferences.containsKey(conference))
			{
				cid = conferences.get(conference); 
			}
			if(journals.containsKey(journal))
			{
				jid = journals.get(journal); 
			}
			
			String insert = "INSERT INTO dblp.publication VALUES (" + i + ", \"" + title + "\", " + year + ", " + cid + ", " + jid + ", \"" + doi + "\", \"" + dblp_id + "\"); "; 
			statementUpdate.executeUpdate(insert); 
			
			if(i % 100000 == 0)
			{
				System.out.println(i);
			}
		}
		result.close();
	}

	// clean writes; 
	public static void cleanWrites(Statement statement, Statement statementUpdate) throws SQLException
	{
		Hashtable<String, Integer> authors = new Hashtable<String, Integer>();
		ResultSet result = statement.executeQuery("SELECT aid, dblp_url FROM dblp.author; "); 
		while(result.next())
		{
			authors.put(result.getString(2), result.getInt(1)); 
		}
		result.close(); 
		
		Hashtable<String, Integer> papers = new Hashtable<String, Integer>(); 
		result = statement.executeQuery("SELECT pid, dblp_url FROM dblp.publication; "); 
		while(result.next())
		{
			papers.put(result.getString(2), result.getInt(1)); 
		}
		result.close(); 

		result = statement.executeQuery("SELECT * FROM dblp_raw.writes; "); 
		while(result.next())
		{
			String aid = subURLAuthor(result.getString(1)); 
			String pid = subURLPaper(result.getString(2)); 
			int order = result.getInt(3); 
			
			if(authors.containsKey(aid) && papers.containsKey(pid))
			{
				statementUpdate.executeUpdate("INSERT INTO dblp.writes VALUES (" + authors.get(aid) + ", " + papers.get(pid) + ", " + order + "); "); 
			}
		}
		result.close(); 
	}
	
	// clean conference/journal full name; 
	public static void cleanCJName(Statement statement, Statement statementUpdate) throws SQLException
	{
		ResultSet rs = statement.executeQuery("SELECT cid, full_name FROM dblp.conference; "); 
		while(rs.next())
		{
			int id = rs.getInt(1); 
			String name = rs.getString(2); 
			if(name.contains(" ("))
			{
				name = name.substring(0, name.indexOf(" (")); 
				statementUpdate.executeUpdate("UPDATE dblp.conference SET full_name = \"" + name + "\" WHERE cid = " + id); 
			}
		}
		rs.close(); 
		
		rs = statement.executeQuery("SELECT jid, full_name FROM dblp.journal; "); 
		while(rs.next())
		{
			int id = rs.getInt(1); 
			String name = rs.getString(2); 
			if(name.contains(" ("))
			{
				name = name.substring(0, name.indexOf(" (")); 
				statementUpdate.executeUpdate("UPDATE dblp.journal SET full_name = \"" + name + "\" WHERE jid = " + id); 
			}
		}
		rs.close(); 
	}
	
	// transform from url to id (author); 
	public static String subURLAuthor(String url)
	{
		url = url.substring(33); 
		return url; 
	}
	
	// transform from id to url (author); 
	public static String supURLAuthor(String url)
	{
		url = "http://dblp.uni-trier.de/pers/hd/" + url; 
		return url; 
	}
	
	// transform from url to id (paper); 
	public static String subURLPaper(String url)
	{
		url = url.substring(36); 
		return url; 
	}
	
	// transform from id to url (paper); 
	public static String supURLPaper(String url)
	{
		url = "http://dblp.uni-trier.de/rec/bibtex/" + url; 
		return url; 
	}	
	
	// transforming French, German,...,  characters into English
	public static String deAccent(String str) 
	{
	    String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD); 
	    Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
	    return pattern.matcher(nfdNormalizedString).replaceAll("");
	}

}
