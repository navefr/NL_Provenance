package htmlGeneration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class TopGen 
{
	public static ArrayList<String> topNamesGen(Statement statement, String type, int id, int k) throws SQLException
	{
		ArrayList<String> entities = new ArrayList<String>(); 
		ResultSet results = statement.executeQuery(SQLGen(type, id, k)); 
		while(results.next())
		{
			String entity = "<a href=\"" + URLGen.getURL(type, results.getInt("id")) + "\">" + results.getString("name") + "</a>"
				+ " (" + URLGen.viewListURL(results.getInt("num")+"", type, id, results.getInt("id"), "1") + ")"; 
			entities.add(entity); 
		}
		
		return entities; 
	}
	
	public static String SQLGen(String type, int id, int k)
	{
		String SQLcommand = ""; 

		// author: 
		if(type.equals("authorDomain")) // domains of an author; 
		{
			SQLcommand = "SELECT domain.did AS id, domain.name AS name, domain_author.paper_count AS num FROM domain, domain_author WHERE domain_author.did = domain.did "
				+ " AND domain_author.aid = " + id + " GROUP BY domain.did ORDER BY num DESC LIMIT 0, " + k; 
		}
		else if(type.equals("authorConference")) // most relevant conferences of an author; 
		{
			SQLcommand = "SELECT conference.cid AS id, conference.name AS name, count(*) AS num FROM conference, publication, writes WHERE conference.cid = publication.cid "
				+ "AND publication.pid = writes.pid AND writes.aid = " + id + " GROUP BY conference.cid ORDER BY num DESC LIMIT 0, " + k; 
		}
		else if(type.equals("authorJournal")) // most relevant conferences of an author; 
		{
			SQLcommand = "SELECT journal.jid AS id, journal.name AS name, count(*) AS num FROM journal, publication, writes WHERE journal.jid = publication.jid "
				+ "AND publication.pid = writes.pid AND writes.aid = " + id + " GROUP BY journal.jid ORDER BY num DESC LIMIT 0, " + k; 
		}
		else if(type.equals("authorKeyword")) // most relevant keywords of an author; 
		{
			SQLcommand = "SELECT keyword.kid AS id, keyword.keyword AS name, count(*) AS num FROM keyword, publication_keyword, writes"
				+ " WHERE keyword.kid = publication_keyword.kid AND publication_keyword.pid = writes.pid AND writes.aid = " + id
				+ " GROUP BY keyword.kid ORDER BY num DESC LIMIT 0, " + k; 
		}
		else if(type.equals("coauthor"))
		{
			SQLcommand = "SELECT author.aid AS id, author.name AS name, count(*) AS num FROM author, writes w1, writes w2 WHERE author.aid = w1.aid "
				+ "AND w1.pid = w2.pid AND w1.aid <> w2.aid AND w2.aid = " + id + " GROUP BY id ORDER BY num DESC LIMIT 0, " + k; 
		}

		// conference: 
		else if(type.equals("conferenceAuthor")) // top authors in a specific conference; 
		{
			SQLcommand = "SELECT author.aid AS id, author.name AS name, count(*) AS num FROM author, writes, publication WHERE author.aid = writes.aid AND writes.pid = publication.pid"
					+ " AND cid = " + id + " GROUP BY author.aid ORDER BY num DESC LIMIT 0, " + k; 
		}
		else if(type.equals("conferenceOrganization")) // top authors in a specific conference; 
		{
			SQLcommand = "SELECT organization.oid AS id, organization.name AS name, count(DISTINCT publication.pid) AS num FROM organization, author, writes, publication WHERE organization.oid = author.oid"
					+ " AND author.aid = writes.aid AND writes.pid = publication.pid AND cid = " + id + " GROUP BY organization.oid ORDER BY num DESC LIMIT 0, " + k; 
		}
		else if(type.equals("conferenceKeyword")) // top keywords in a specific conference; 
		{
			SQLcommand = "SELECT keyword.kid AS id, keyword.keyword AS name, count(*) AS num FROM keyword, publication_keyword, publication WHERE keyword.kid = publication_keyword.kid"
					+ " AND publication_keyword.pid = publication.pid AND publication.cid = " + id + " GROUP BY keyword.kid ORDER BY num DESC LIMIT 0, " + k; 
		}
		else if(type.equals("conferencePublication")) // top publications in a specific conference; 
		{
			SQLcommand = "SELECT publication.pid AS id, publication.title AS name, citation_count AS num FROM publication WHERE"
					+ " publication.cid = " + id + " ORDER BY num DESC LIMIT 0, " + k; 
		}
		
		// journal: 
		else if(type.equals("journalAuthor")) // top authors in a specific journal; 
		{
			SQLcommand = "SELECT author.aid AS id, author.name AS name, count(*) AS num FROM author, writes, publication WHERE author.aid = writes.aid AND writes.pid = publication.pid"
					+ " AND jid = " + id + " GROUP BY author.aid ORDER BY num DESC LIMIT 0, " + k; 
		}
		else if(type.equals("journalOrganization")) // top authors in a specific journal; 
		{
			SQLcommand = "SELECT organization.oid AS id, organization.name AS name, count(DISTINCT publication.pid) AS num FROM organization, author, writes, publication WHERE organization.oid = author.oid"
					+ " AND author.aid = writes.aid AND writes.pid = publication.pid AND jid = " + id + " GROUP BY organization.oid ORDER BY num DESC LIMIT 0, " + k; 
		}
		else if(type.equals("journalKeyword")) // top keywords in a specific journal; 
		{
			SQLcommand = "SELECT keyword.kid AS id, keyword.keyword AS name, count(*) AS num FROM keyword, publication_keyword, publication WHERE keyword.kid = publication_keyword.kid"
					+ " AND publication_keyword.pid = publication.pid AND publication.jid = " + id + " GROUP BY keyword.kid ORDER BY num DESC LIMIT 0, " + k; 
		}
		else if(type.equals("journalPublication")) // top publications in a specific journal; 
		{
			SQLcommand = "SELECT publication.pid AS id, publication.title AS name, citation_count AS num FROM publication WHERE"
					+ " publication.jid = " + id + " ORDER BY num DESC LIMIT 0, " + k; 
		}

		// domain: 
		else if(type.equals("domainConference")) // top conferences in a domain
		{
			SQLcommand = "SELECT conference.cid AS id, conference.name AS name, conference.full_name AS full, conference.citation_count*conference.citation_count/conference.paper_count AS num FROM conference, domain_conference"
				+ " WHERE conference.cid = domain_conference.cid AND domain_conference.did = " + id + " ORDER BY num DESC LIMIT 0, " + k; 
		}
		else if(type.equals("domainJournal")) // top journals in a domain
		{
			SQLcommand = "SELECT journal.jid AS id, journal.name AS name, journal.full_name AS full, journal.citation_count*journal.citation_count/journal.paper_count AS num FROM journal, domain_journal "
				+ "WHERE journal.jid = domain_journal.jid AND domain_journal.did = " + id + " ORDER BY num DESC LIMIT 0, " + k; 
		}

		// keyword: 
		else if(type.equals("keywordAuthor")) // top authors on a topic 
		{
			SQLcommand = "SELECT author.aid AS id, author.name AS name, count(*) AS num FROM author, writes, publication_keyword"
				+ " WHERE author.aid = writes.aid AND writes.pid = publication_keyword.pid AND publication_keyword.kid = " + id + " GROUP BY id ORDER BY num DESC LIMIT 0, " + k; 
		}
		else if(type.equals("keywordPublication")) // top papers on a topic 
		{
			SQLcommand = "SELECT publication.pid AS id, publication.title AS name, citation_count AS num FROM publication, publication_keyword WHERE"
				+ " publication.pid = publication_keyword.pid AND publication_keyword.kid = " + id + " ORDER BY num DESC LIMIT 0, " + k; 
		}
		
		return SQLcommand; 
	}
}
