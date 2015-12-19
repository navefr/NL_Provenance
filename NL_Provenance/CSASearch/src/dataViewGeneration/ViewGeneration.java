package dataViewGeneration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ViewGeneration 
{
	public static void main(String [] args) throws SQLException, ClassNotFoundException
	{
		String driver = "com.mysql.jdbc.Driver"; 
		String db_url = "jdbc:mysql://127.0.0.1:3306/";
		String user = "root";
		String password = "";
		Class.forName(driver);

		Connection conn = DriverManager.getConnection(db_url, user, password);		
		Statement statement = conn.createStatement();
		
		Connection connUpdate = DriverManager.getConnection(db_url, user, password);	
		Statement statementUpdate = connUpdate.createStatement();
		
//		getPublicationDomain(statement, statementUpdate); 
//		authorNumberPaper(statement, statementUpdate); 
//		paperCitation(statement, statementUpdate); 		
//		paperReferences(statement, statementUpdate); 
//		authorCitations(statement, statementUpdate); 

//		conferenceNumberPaper(statement, statementUpdate); 
//		journalNumberPaper(statement, statementUpdate); 
//		conferenceCitations(statement, statementUpdate); 
//		journalCitations(statement, statementUpdate); 
		
//		authorDomain(statement, statementUpdate); 		
//		organizationNumberAuthor(statement, statementUpdate); 
//		organizationNumberPaper(statement, statementUpdate); 
//		topAuthors(statement, statementUpdate); 
//		authorOrganizationPaperCJ(statement, statementUpdate); 
//		publicationOrganization(statement, statementUpdate); 
		organizationCitation(statement, statementUpdate); 
	}
	
	// Step 1. get the publication_domain: 
	public static void getPublicationDomain(Statement statement, Statement statementUpdate) throws SQLException
	{
		ResultSet publication_domain = statement.executeQuery("SELECT pid, did FROM dblp_plus.publication, dblp_plus.domain_conference"
			+ " WHERE publication.cid = domain_conference.cid AND publication.cid <> 0 AND did <> 0; "); 
		while(publication_domain.next())
		{
			String insert = "INSERT INTO dblp_plus.domain_publication VALUES (" + publication_domain.getInt(1) + ", " + publication_domain.getInt(2) + "); "; 
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
		
		publication_domain = statement.executeQuery("SELECT pid, did FROM dblp_plus.publication, dblp_plus.domain_journal WHERE publication.jid = domain_journal.jid AND publication.jid <> 0 AND did <> 0; "); 
		while(publication_domain.next())
		{
			String insert = "INSERT INTO dblp_plus.domain_publication VALUES (" + publication_domain.getInt(1) + ", " + publication_domain.getInt(2) + "); "; 
			try
			{
				statementUpdate.executeUpdate("INSERT INTO dblp_plus.domain_publication VALUES (" + publication_domain.getInt(1) + ", " + publication_domain.getInt(2) + "); "); 
			}
			catch(Exception e)
			{
				System.out.println(insert);
			}
		}
		publication_domain.close(); 
	}
	
	// Step 2. count the number of papers written by each author: 
	public static void authorNumberPaper(Statement statement, Statement statementUpdate) throws ClassNotFoundException, SQLException
	{
		ResultSet author_papers = statement.executeQuery("SELECT aid, count(pid) FROM dblp_plus.writes GROUP BY aid; "); 
		while(author_papers.next())
		{
			String update = "UPDATE dblp_plus.author SET paper_count = " + author_papers.getInt(2) + " WHERE aid = " + author_papers.getInt(1); 
			statementUpdate.executeUpdate(update); 
		}
		author_papers.close(); 
	}
	
	// Step 3. count the citations for each paper; 
	public static void paperCitation(Statement statement, Statement statementUpdate) throws ClassNotFoundException, SQLException
	{
		ResultSet paper_citations = statement.executeQuery("SELECT cited, count(citing) FROM dblp_plus.cite GROUP BY cited; "); 
		for(int i = 0; paper_citations.next(); i++)
		{
			if(i % 10000 == 0)
			{
				System.out.println(i);
			}
			String update = "UPDATE dblp_plus.publication SET citation_count = " + paper_citations.getInt(2) + " WHERE pid = " + paper_citations.getInt(1); 
			statementUpdate.executeUpdate(update); 
		}
		paper_citations.close(); 
	}
	
	// Step 4. count the references for each paper; 
	public static void paperReferences(Statement statement, Statement statementUpdate) throws ClassNotFoundException, SQLException
	{
		ResultSet paper_references = statement.executeQuery("SELECT citing, count(cited) FROM dblp_plus.cite GROUP BY citing; "); 
		for(int i = 0; paper_references.next(); i++)
		{
			if(i % 10000 == 0)
			{
				System.out.println(i);
			}
			String update = "UPDATE dblp_plus.publication SET reference_count = " + paper_references.getInt(2) + " WHERE pid = " + paper_references.getInt(1); 
			statementUpdate.executeUpdate(update); 
		}
		paper_references.close(); 
	}
	
	// Step 5. count the total citations for each author; 
	public static void authorCitations(Statement statement, Statement statementUpdate) throws ClassNotFoundException, SQLException
	{
		ResultSet author_citations = statement.executeQuery("SELECT aid, sum(citation_count) FROM dblp_plus.publication, dblp_plus.writes "
			+ "WHERE writes.pid = publication.pid GROUP BY aid; "); 
		for(int i = 0; author_citations.next(); i++)
		{
			if(i % 10000 == 0)
			{
				System.out.println(i);
			}
			String update = "UPDATE dblp_plus.author SET citation_count = " + author_citations.getInt(2)*1.8 + " WHERE aid = " + author_citations.getInt(1); 
			statementUpdate.executeUpdate(update); 
		}
		author_citations.close(); 
	}

	// Step 6. count the total papers for each conference; 
	public static void conferenceNumberPaper(Statement statement, Statement statementUpdate) throws ClassNotFoundException, SQLException
	{
		ResultSet conference_papers = statement.executeQuery("SELECT cid, count(pid) FROM dblp_plus.publication WHERE cid > 0 GROUP BY cid; "); 
		while(conference_papers.next())
		{
			String update = "UPDATE dblp_plus.conference SET paper_count = " + conference_papers.getInt(2) + " WHERE cid = " + conference_papers.getInt(1); 
			statementUpdate.executeUpdate(update); 
		}
		conference_papers.close(); 
	}

	// Step 7. count the total papers for each journal; 
	public static void journalNumberPaper(Statement statement, Statement statementUpdate) throws ClassNotFoundException, SQLException
	{
		ResultSet journal_papers = statement.executeQuery("SELECT jid, count(pid) FROM dblp_plus.publication WHERE jid > 0 GROUP BY jid; "); 
		while(journal_papers.next())
		{
			String update = "UPDATE dblp_plus.journal SET paper_count = " + journal_papers.getInt(2) + " WHERE jid = " + journal_papers.getInt(1); 
			statementUpdate.executeUpdate(update); 
		}
		journal_papers.close(); 
	}
	
	// Step 8. count the total citations for each conference; 
	public static void conferenceCitations(Statement statement, Statement statementUpdate) throws ClassNotFoundException, SQLException
	{
		ResultSet conference_papers = statement.executeQuery("SELECT cid, sum(citation_count) FROM dblp_plus.publication WHERE cid > 0 GROUP BY cid; "); 
		while(conference_papers.next())
		{
			String update = "UPDATE dblp_plus.conference SET citation_count = " + conference_papers.getInt(2)*1.5 + " WHERE cid = " + conference_papers.getInt(1); 
			statementUpdate.executeUpdate(update); 
		}
		conference_papers.close(); 
	}

	// Step 9. count the total citations for each journal; 
	public static void journalCitations(Statement statement, Statement statementUpdate) throws ClassNotFoundException, SQLException
	{
		ResultSet journal_papers = statement.executeQuery("SELECT jid, sum(citation_count) FROM dblp_plus.publication WHERE jid > 0 GROUP BY jid; "); 
		while(journal_papers.next())
		{
			String update = "UPDATE dblp_plus.journal SET citation_count = " + journal_papers.getInt(2)*1.5 + " WHERE jid = " + journal_papers.getInt(1); 
			statementUpdate.executeUpdate(update); 
		}
		journal_papers.close(); 
	}
	
	// Step 10. estimate the domain for each author; 
	public static void authorDomain(Statement statement, Statement statementUpdate) throws ClassNotFoundException, SQLException
	{
		statement.execute("use dblp_plus"); 
		statementUpdate.execute("use dblp_plus"); 
		
		int pre_aid = 0; 
		int pre_count = 0; 
		int pre_rank = 0; 
		
		ResultSet author_domain = statement.executeQuery("SELECT aid, did, count(*) num FROM writes, domain_publication WHERE writes.pid = domain_publication.pid GROUP BY aid, did ORDER BY aid ASC, num DESC; "); 
		for(int num = 0; author_domain.next(); num++)
		{
			if(num % 10000 == 0)
			{
				System.out.println(num); 
			}

			int aid = author_domain.getInt(1); 
			int did = author_domain.getInt(2); 
			int count = author_domain.getInt(3);
			
			if(aid != pre_aid)
			{
				pre_aid = aid;
				pre_count = count; 
				pre_rank = 1; 
				statementUpdate.executeUpdate("INSERT INTO domain_author VALUES (" + aid + ", " + did + ", " + count + ", 1); "); 
			}
			else if(pre_rank < 3 && (count > pre_count/2 || (count > 5 && count > pre_count/3) || (count > 10 && count > pre_count/4) || (count > 20 && count > pre_count/5)))
			{
				pre_rank++; 
				statementUpdate.executeUpdate("INSERT INTO domain_author VALUES (" + aid + ", " + did + ", " + count + ", " + pre_rank + "); "); 
			}
		}
		author_domain.close(); 
	}

	// Step 11. count the number of authors in each organization; 
	public static void organizationNumberAuthor(Statement statement, Statement statementUpdate) throws ClassNotFoundException, SQLException
	{
		ResultSet author_papers = statement.executeQuery("SELECT oid, count(aid) FROM dblp_plus.author GROUP BY oid; "); 
		while(author_papers.next())
		{
			String update = "UPDATE dblp_plus.organization SET author_count = " + author_papers.getInt(2) + " WHERE oid = " + author_papers.getInt(1); 
			statementUpdate.executeUpdate(update); 
		}
		author_papers.close(); 
	}

	// Step 11. count the number of papers in each organization; 
	public static void organizationNumberPaper(Statement statement, Statement statementUpdate) throws ClassNotFoundException, SQLException
	{
		ResultSet author_papers = statement.executeQuery("select a.oid, count(p.pid) from dblp_plus.publication p, dblp_plus.writes w, dblp_plus.author a where p.pid = w.pid and w.aid = a.aid group by a.oid; "); 
		while(author_papers.next())
		{
			String update = "UPDATE dblp_plus.organization SET paper_count = " + author_papers.getInt(2) + " WHERE oid = " + author_papers.getInt(1); 
			statementUpdate.executeUpdate(update); 
		}
		author_papers.close(); 
	}

	// Step 12. generate the top authors; 
	public static void topAuthors(Statement statement, Statement statementUpdate) throws ClassNotFoundException, SQLException
	{
		statement.execute("use dblp_plus; "); 
		for(int i = 1; i < 25; i++)
		{
			String query = "SELECT author.aid, author.photo, citation_count FROM author, domain_author WHERE author.aid = domain_author.aid AND rank = 1 AND did = " + i
				+ " ORDER BY citation_count DESC LIMIT 0, 10;"; 
			ResultSet rs = statement.executeQuery(query); 
			for(int j = 1; rs.next(); j++)
			{
				String update = "INSERT INTO dblp_plus.top_authors VALUES (" + i + ", " + rs.getInt(1) + ", " + j + ", \"" + rs.getString(2) + "\"); "; 
				statementUpdate.executeUpdate(update); 
			}
			rs.close(); 
			
			System.out.println(i);
		}
	}

	// Step 13. assign organization to author, conference/journal to paper; 
	public static void authorOrganizationPaperCJ(Statement statement, Statement statementUpdate) throws ClassNotFoundException, SQLException
	{
		ResultSet rs = statement.executeQuery("SELECT oid, name FROM dblp_plus.organization; "); 
		while(rs.next())
		{
			int oid = rs.getInt(1); 
			String name = rs.getString(2); 
			statementUpdate.executeUpdate("UPDATE dblp_plus.author SET organization = \"" + name + "\" WHERE oid = " + oid); 
		}
		rs.close();
		
		rs = statement.executeQuery("SELECT cid, name FROM dblp_plus.conference; "); 
		while(rs.next())
		{
			int cid = rs.getInt(1); 
			String name = rs.getString(2); 
			statementUpdate.executeUpdate("UPDATE dblp_plus.publication SET conference_journal = \"" + name + "\" WHERE cid = " + cid); 
		}
		rs.close();

		rs = statement.executeQuery("SELECT jid, name FROM dblp_plus.journal; "); 
		while(rs.next())
		{
			int jid = rs.getInt(1); 
			String name = rs.getString(2); 
			statementUpdate.executeUpdate("UPDATE dblp_plus.publication SET conference_journal = \"" + name + "\" WHERE jid = " + jid); 
		}
		rs.close();
	}

	// Step 14. publication_organization; 
	public static void publicationOrganization(Statement statement, Statement statementUpdate) throws ClassNotFoundException, SQLException
	{
		ResultSet author_papers = statement.executeQuery("select DISTINCT writes.pid, author.oid from dblp_plus.writes, dblp_plus.author where writes.aid = author.aid; "); 
		while(author_papers.next())
		{
			String update = "INSERT INTO dblp_plus.publication_organization VALUES (" + author_papers.getInt(1) + ", " + author_papers.getInt(2) + "); "; 
			statementUpdate.executeUpdate(update); 
		}
		author_papers.close(); 
	}
	
	// Step 15. organization_citation; 
	public static void organizationCitation(Statement statement, Statement statementUpdate) throws ClassNotFoundException, SQLException
	{
		ResultSet author_papers = statement.executeQuery("select o.oid, sum(p.citation_count) from dblp_plus.organization o, dblp_plus.publication_organization po, dblp_plus.publication p where o.oid = po.oid and po.pid = p.pid group by o.oid; "); 
		while(author_papers.next())
		{
			String update = "UPDATE dblp_plus.organization SET citation_count = " + author_papers.getInt(2) + " WHERE oid = " + author_papers.getInt(1) + "; "; 
			statementUpdate.executeUpdate(update); 
		}
		author_papers.close(); 
	}
}