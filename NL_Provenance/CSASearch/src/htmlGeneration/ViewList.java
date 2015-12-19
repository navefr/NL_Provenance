package htmlGeneration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class ViewList 
{
	public static String viewList(Statement statement, String url) throws SQLException
	{
		String html = ""; 
		String targetEntity = ""; 
		String filterEntity1 = ""; 
		String filterEntity2 = ""; 
		int page = 1; 
		
		String [] phrases = url.split("&"); 
		if(phrases.length > 1)
		{
			targetEntity = phrases[1]; 
		}
		if(phrases.length > 2)
		{
			try
			{
				page = Integer.parseInt(phrases[2].split("=")[1]); 
			}
			catch (Exception e)
			{
				return "wrong parameters..."; 
			}
		}
		if(phrases.length > 3)
		{
			filterEntity1 = phrases[3]; 
		}
		if(phrases.length > 4)
		{
			filterEntity2 = phrases[4]; 
		}
		
		if(filterEntity2.isEmpty())
		{
			if(targetEntity.endsWith("authors")) // 1. top authors lists; 
			{
				String id = filterEntity1.split("=")[1]; 
				String entity = ""; 

				String query = ""; 
				ArrayList<Integer> aids = new ArrayList<Integer>(); 
				String queryAuthors = ""; 
				String filterLink = ""; 

				if(filterEntity1.startsWith("oid")) // 1.1 top authors in an organization; 
				{
					query = "SELECT name FROM dblp_plus.organization WHERE oid = " + id; 
					ResultSet entityName = statement.executeQuery(query); 
					if(entityName.next())
					{
						entity = entityName.getString(1); 
					}

					queryAuthors = "SELECT aid FROM dblp_plus.author WHERE oid = " + id + " ORDER BY citation_count DESC LIMIT " + ((page-1)*100) + ", " + 100; 
					filterLink = "<p><b>Top Authors in the " + URLGen.addLinkage("organization", Integer.parseInt(id), entity) + "</b></p>"; 
					
					if(!query.isEmpty() && !queryAuthors.isEmpty())
					{
						ResultSet result = statement.executeQuery(queryAuthors); 
						while(result.next())
						{
							aids.add(result.getInt("aid")); 
						}
						result.close(); 
						
						if(aids.size() > 0)
						{
							html += filterLink; 
						}
						for(int i = 0; i < aids.size(); i++)
						{
							if(i > 0)
							{
								html += "<hr>"; 
							}
							html += "<p>" + AuthorHomepage.authorBrief(statement, aids.get(i)) + "</p>"; 
						}
					}				
				}
				else if(filterEntity1.startsWith("did")) // 1.2 top authors in a domain; 
				{
					query = "SELECT name FROM dblp_plus.domain WHERE did = " + id; 
					ResultSet entityName = statement.executeQuery(query); 
					if(entityName.next())
					{
						entity = entityName.getString(1); 
					}

					queryAuthors = "SELECT author.aid FROM author, domain_author WHERE author.aid = domain_author.aid AND rank = 1 AND did = " + id
							+ " ORDER BY citation_count DESC LIMIT " + ((page-1)*100) + ", " + 100; 
					filterLink = "<p><b>Top Authors in the Domain of " + URLGen.addLinkage("domain", Integer.parseInt(id), entity) + "</b></p>"; 

					if(!query.isEmpty() && !queryAuthors.isEmpty())
					{
						ResultSet result = statement.executeQuery(queryAuthors); 
						while(result.next())
						{
							aids.add(result.getInt("aid")); 
						}
						result.close(); 
						
						if(aids.size() > 0)
						{
							html += filterLink; 
						}
						for(int i = 0; i < aids.size(); i++)
						{
							if(i > 0)
							{
								html += "<hr>"; 
							}
							html += "<p>" + AuthorHomepage.authorBrief(statement, aids.get(i)) + "</p>"; 
						}
					}				
				}
				else if(filterEntity1.startsWith("aid")) // 1.3. authors cooperated with an author; 
				{
					query = "SELECT name FROM dblp_plus.author WHERE aid = " + id; 
					ResultSet entityName = statement.executeQuery(query); 
					if(entityName.next())
					{
						entity = entityName.getString(1); 
					}

					ArrayList<String> coauthors = TopGen.topNamesGen(statement, "coauthor", Integer.parseInt(id), 10000); 
					if(coauthors.size() > 0)
					{
						html += "<h3><b>Authors cooperated with " + URLGen.addLinkage("author", Integer.parseInt(id), entity) + ": </b></h3>"; 
					}
					for(int i = 0; i < coauthors.size(); i++)
					{
						html += "<p>" + (i+1) + ". " + coauthors.get(i) + "</p>"; 
					}
				}
				if(filterEntity1.startsWith("cid")) // 1.4 top authors in a conference; 
				{
					query = "SELECT name FROM dblp_plus.conference WHERE cid = " + id; 
					ResultSet entityName = statement.executeQuery(query); 
					if(entityName.next())
					{
						entity = entityName.getString(1); 
					}

					ArrayList<String> keywords = TopGen.topNamesGen(statement, "conferenceAuthor", Integer.parseInt(id), page*100); 
					if(keywords.size() > 0)
					{
						html += "<h3><b>Top Authors in " + URLGen.addLinkage("conference", Integer.parseInt(id), entity) + ": </b></h3>"; 
					}
					for(int i = 0; i < keywords.size(); i++)
					{
						if(i > (page-1)*100-1)
						{
							html += "<p>" + (i+1) + ". " + keywords.get(i) + "</p>"; 
						}	
					}
				}
				if(filterEntity1.startsWith("jid")) // 1.5 top authors in a journal; 
				{
					query = "SELECT name FROM dblp_plus.journal WHERE jid = " + id; 
					ResultSet entityName = statement.executeQuery(query); 
					if(entityName.next())
					{
						entity = entityName.getString(1); 
					}

					ArrayList<String> keywords = TopGen.topNamesGen(statement, "journalAuthor", Integer.parseInt(id), page*100); 
					if(keywords.size() > 0)
					{
						html += "<h3><b>Top Authors in " + URLGen.addLinkage("journal", Integer.parseInt(id), entity) + ": </b></h3>"; 
					}
					for(int i = 0; i < keywords.size(); i++)
					{
						if(i > (page-1)*100-1)
						{
							html += "<p>" + (i+1) + ". " + keywords.get(i) + "</p>"; 
						}
					}
				}
				if(filterEntity1.startsWith("kid")) // 1.5 top authors on a topic; 
				{
					query = "SELECT keyword FROM dblp_plus.keyword WHERE kid = " + id; 
					ResultSet entityName = statement.executeQuery(query); 
					if(entityName.next())
					{
						entity = entityName.getString(1); 
					}

					ArrayList<String> keywords = TopGen.topNamesGen(statement, "keywordAuthor", Integer.parseInt(id), page*100); 
					if(keywords.size() > 0)
					{
						html += "<h3><b>Top Authors on the topic of " + URLGen.addLinkage("keyword", Integer.parseInt(id), entity) + ": </b></h3>"; 
					}
					for(int i = 0; i < keywords.size(); i++)
					{
						if(i > (page-1)*100-1)
						{
							html += "<p>" + (i+1) + ". " + keywords.get(i) + "</p>"; 
						}
					}
				}
			}
			
			if(targetEntity.endsWith("papers")) // 2. top papers lists; 
			{
				String id = filterEntity1.split("=")[1]; 
				String entity = ""; 
				String query = ""; 
				ArrayList<Integer> pids = new ArrayList<Integer>(); 

				if(filterEntity1.startsWith("aid")) // 2.1 top papers for an author;  
				{
					query = "SELECT name FROM dblp_plus.author WHERE aid = " + id; 
					ResultSet filter = statement.executeQuery(query); 
					if(filter.next())
					{
						entity = filter.getString(1); 
					}
					
					String queryPapers = "SELECT publication.pid FROM dblp_plus.publication, dblp_plus.writes WHERE writes.pid = publication.pid AND aid = " + id
						+ " ORDER BY publication.year DESC LIMIT " + ((page-1)*100) + ", " + 100; 
					ResultSet result = statement.executeQuery(queryPapers); 
					while(result.next())
					{
						pids.add(result.getInt("pid")); 
					}
					result.close(); 
					
					if(pids.size() > 0)
					{
						html += "<p><b>All Papers by " + URLGen.addLinkage("author", Integer.parseInt(id), entity) + ": </b></p>"; 
					}
					for(int i = 0; i < pids.size(); i++)
					{
						html += "<p>" + PaperHomepage.paperBrief(statement, pids.get(i), ((page-1)*100+i+1)) + "</p>"; 
					}
				}
				if(filterEntity1.startsWith("cid")) // 2.2 top papers for a conference;  
				{
					query = "SELECT name FROM dblp_plus.conference WHERE cid = " + id; 
					ResultSet filter = statement.executeQuery(query); 
					if(filter.next())
					{
						entity = filter.getString(1); 
					}
					
					String queryPapers = "SELECT publication.pid FROM dblp_plus.publication WHERE cid = " + id + " ORDER BY publication.year DESC LIMIT " + ((page-1)*100) + ", " + 100; 
					ResultSet result = statement.executeQuery(queryPapers); 
					while(result.next())
					{
						pids.add(result.getInt("pid")); 
					}
					result.close(); 
					
					if(pids.size() > 0)
					{
						html += "<p><b>All Papers in " + URLGen.addLinkage("conference", Integer.parseInt(id), entity) + ": </b></p>"; 
					}
					for(int i = 0; i < pids.size(); i++)
					{
						html += "<p>" + PaperHomepage.paperBrief(statement, pids.get(i), ((page-1)*100+i+1)) + "</p>"; 
					}
				}
				if(filterEntity1.startsWith("jid")) // 2.3 top papers for a journal;  
				{
					query = "SELECT name FROM dblp_plus.journal WHERE jid = " + id; 
					ResultSet filter = statement.executeQuery(query); 
					if(filter.next())
					{
						entity = filter.getString(1); 
					}
					
					String queryPapers = "SELECT publication.pid FROM dblp_plus.publication WHERE jid = " + id + " ORDER BY publication.year DESC LIMIT " + ((page-1)*100) + ", " + 100; 
					ResultSet result = statement.executeQuery(queryPapers); 
					while(result.next())
					{
						pids.add(result.getInt("pid")); 
					}
					result.close(); 
					
					if(pids.size() > 0)
					{
						html += "<p><b>All Papers in " + URLGen.addLinkage("journal", Integer.parseInt(id), entity) + ": </b></p>"; 
					}
					for(int i = 0; i < pids.size(); i++)
					{
						html += "<p>" + PaperHomepage.paperBrief(statement, pids.get(i), ((page-1)*100+i+1)) + "</p>"; 
					}
				}
				if(filterEntity1.startsWith("kid")) // 2.4 top papers containing a keyword;  
				{
					query = "SELECT keyword FROM dblp_plus.keyword WHERE kid = " + id; 
					ResultSet filter = statement.executeQuery(query); 
					if(filter.next())
					{
						entity = filter.getString(1); 
					}
					
					String queryPapers = "SELECT publication.pid FROM dblp_plus.publication, dblp_plus.publication_keyword"
						+ " WHERE publication_keyword.pid = publication.pid AND kid = " + id + " ORDER BY publication.year DESC LIMIT " + ((page-1)*100) + ", " + 100; 
					ResultSet result = statement.executeQuery(queryPapers); 
					while(result.next())
					{
						pids.add(result.getInt("pid")); 
					}
					result.close(); 
					
					if(pids.size() > 0)
					{
						html += "<p><b>All Papers on the topic of " + URLGen.addLinkage("keyword", Integer.parseInt(id), entity) + ": </b></p>"; 
					}
					for(int i = 0; i < pids.size(); i++)
					{
						html += "<p>" + PaperHomepage.paperBrief(statement, pids.get(i), ((page-1)*100+i+1)) + "</p>"; 
					}
				}
			}
			
			if(targetEntity.endsWith("conferences")) // 3. top conference lists; 
			{
				String id = filterEntity1.split("=")[1]; 
				String entity = ""; 
				String query = ""; 
				ArrayList<Integer> cids = new ArrayList<Integer>(); 

				if(filterEntity1.startsWith("did")) // 3.1 top conferences in a domain;  
				{
					query = "SELECT name FROM dblp_plus.domain WHERE did = " + id; 
					ResultSet filter = statement.executeQuery(query); 
					if(filter.next())
					{
						entity = filter.getString(1); 
					}
					
					String topConferences = TopGen.SQLGen("domainConference", Integer.parseInt(id), 10000); 
					ArrayList<String> conferences = new ArrayList<String>(); 
					ResultSet result = statement.executeQuery(topConferences); 
					while(result.next())
					{
						cids.add(result.getInt("id")); 
						String conferenceName = result.getString("name"); 
						if(!result.getString("full").isEmpty())
						{
							conferenceName += " - " + result.getString("full"); 
						}
						conferences.add(conferenceName); 
					}
					result.close();

					if(conferences.size() > 0)
					{
						html += "<p><b>Top Conferences in " + URLGen.addLinkage("domain", Integer.parseInt(id), entity) + " Area: </b></p>"; 
					}
					for(int i = 0; i < conferences.size(); i++)
					{
						html += "<p>" + (i+1) + ". " + URLGen.addLinkage("conference", cids.get(i), conferences.get(i)) + "</p>"; 
					}
				}
				else if(filterEntity1.startsWith("aid")) // 3.2 top conferences related to an author; 
				{
					query = "SELECT name FROM dblp_plus.author WHERE aid = " + id; 
					ResultSet filter = statement.executeQuery(query); 
					if(filter.next())
					{
						entity = filter.getString(1); 
					}

					ArrayList<String> conferences = TopGen.topNamesGen(statement, "authorConference", Integer.parseInt(id), 10000); 
					if(conferences.size() > 0)
					{
						html += "<h3><b>" + URLGen.addLinkage("author", Integer.parseInt(id), entity) + "'s Papers Distributions: </b></h3>"; 
					}
					for(int i = 0; i < conferences.size(); i++)
					{
						html += "<p>" + (i+1) + ". " + conferences.get(i) + "</p>"; 
					}
				}
			}

			if(targetEntity.endsWith("journals")) // 4. top journals in a domain; 
			{
				String id = filterEntity1.split("=")[1]; 
				String entity = ""; 
				String query = ""; 
				ArrayList<Integer> jids = new ArrayList<Integer>(); 

				if(filterEntity1.startsWith("did")) // 4.1 top journal in a domain;  
				{
					query = "SELECT name FROM dblp_plus.domain WHERE did = " + id; 
					ResultSet filter = statement.executeQuery(query); 
					if(filter.next())
					{
						entity = filter.getString(1); 
					}
					
					String topJournals = TopGen.SQLGen("domainJournal", Integer.parseInt(id), 10000); 
					ArrayList<String> journals = new ArrayList<String>(); 
					ResultSet result = statement.executeQuery(topJournals); 
					while(result.next())
					{
						jids.add(result.getInt("id")); 
						String journalName = result.getString("name"); 
						if(!result.getString("full").isEmpty())
						{
							journalName += " - " + result.getString("full"); 
						}
						journals.add(journalName); 
					}
					result.close();

					if(journals.size() > 0)
					{
						html += "<p><b>Top Journals in " + URLGen.addLinkage("domain", Integer.parseInt(id), entity) + " Area: </b></p>"; 
					}
					for(int i = 0; i < journals.size(); i++)
					{
						html += "<p>" + (i+1) + ". " + URLGen.addLinkage("journal", jids.get(i), journals.get(i)) + "</p>"; 
					}
				}
				else if(filterEntity1.startsWith("aid")) // 4.2 top journals related to an author; 
				{
					query = "SELECT name FROM dblp_plus.author WHERE aid = " + id; 
					ResultSet filter = statement.executeQuery(query); 
					if(filter.next())
					{
						entity = filter.getString(1); 
					}

					ArrayList<String> journals = TopGen.topNamesGen(statement, "authorJournal", Integer.parseInt(id), 10000); 
					if(journals.size() > 0)
					{
						html += "<h3><b>" + URLGen.addLinkage("author", Integer.parseInt(id), entity) + "'s Papers Distributions: </b></h3>"; 
					}
					for(int i = 0; i < journals.size(); i++)
					{
						html += "<p>" + (i+1) + ". " + journals.get(i) + "</p>"; 
					}
				}
			}
			
			if(targetEntity.endsWith("citations")) // 5. citations of a paper; 
			{
				String id = filterEntity1.split("=")[1]; 
				String entity = ""; 
				String query = ""; 
				
				query = "SELECT title FROM dblp_plus.publication WHERE pid = " + id; 
				ResultSet entityName = statement.executeQuery(query); 
				if(entityName.next())
				{
					entity = entityName.getString(1); 
				}

				html += "<p><b>Citations of " + URLGen.addLinkage("publication", Integer.parseInt(id), entity) + "</b></p>"; 
				ArrayList<Integer> citations = new ArrayList<Integer>(); 				
				ResultSet results = statement.executeQuery("SELECT citing FROM cite WHERE cited = " + id + " LIMIT " + (page-1)*100 + ", 100; "); 
				while(results.next())
				{
					citations.add(results.getInt("citing")); 
				}
				results.close();
				for(int i = 0; i < citations.size(); i++)
				{
					html += PaperHomepage.paperBrief(statement, citations.get(i), (page-1)*100+(i+1)); 
				}
			}
			
			if(targetEntity.endsWith("references")) // 6. references of a paper; 
			{
				String id = filterEntity1.split("=")[1]; 
				String entity = ""; 
				String query = ""; 

				query = "SELECT title FROM dblp_plus.publication WHERE pid = " + id; 
				ResultSet entityName = statement.executeQuery(query); 
				if(entityName.next())
				{
					entity = entityName.getString(1); 
				}

				html += "<p><b>References of " + URLGen.addLinkage("publication", Integer.parseInt(id), entity) + "</b></p>"; 
				ArrayList<Integer> references = new ArrayList<Integer>(); 				
				ResultSet results = statement.executeQuery("SELECT cited FROM cite WHERE citing = " + id + " LIMIT " + (page-1)*100 + ", 100; "); 
				while(results.next())
				{
					references.add(results.getInt("cited")); 
				}
				results.close();
				for(int i = 0; i < references.size(); i++)
				{
					html += PaperHomepage.paperBrief(statement, references.get(i), (page-1)*100+(i+1)); 
				}
			}
			
			if(targetEntity.endsWith("keywords")) // 7. top keywords; 
			{
				String id = filterEntity1.split("=")[1]; 
				String entity = ""; 
				String query = ""; 
				
				if(filterEntity1.startsWith("aid")) // 7.1. keywords of an author; 
				{
					query = "SELECT name FROM dblp_plus.author WHERE aid = " + id; 
					ResultSet entityName = statement.executeQuery(query); 
					if(entityName.next())
					{
						entity = entityName.getString(1); 
					}

					ArrayList<String> keywords = TopGen.topNamesGen(statement, "authorKeyword", Integer.parseInt(id), page*100); 
					if(keywords.size() > 0)
					{
						html += "<h3><b>Keywords related to " + URLGen.addLinkage("author", Integer.parseInt(id), entity) + ": </b></h3>"; 
					}
					for(int i = 0; i < keywords.size(); i++)
					{
						if(i > (page-1)*100-1)
						{
							html += "<p>" + (i+1) + ". " + keywords.get(i) + "</p>"; 
						}
					}
				}
				else if(filterEntity1.startsWith("cid")) // 7.2. keywords of a conference; 
				{
					query = "SELECT name FROM dblp_plus.conference WHERE cid = " + id; 
					ResultSet entityName = statement.executeQuery(query); 
					if(entityName.next())
					{
						entity = entityName.getString(1); 
					}

					ArrayList<String> keywords = TopGen.topNamesGen(statement, "conferenceKeyword", Integer.parseInt(id), page*100); 
					if(keywords.size() > 0)
					{
						html += "<h3><b>Keywords related to " + URLGen.addLinkage("conference", Integer.parseInt(id), entity) + ": </b></h3>"; 
					}
					for(int i = 0; i < keywords.size(); i++)
					{
						if(i > (page-1)*100-1)
						{
							html += "<p>" + (i+1) + ". " + keywords.get(i) + "</p>"; 
						}	
					}
				}
				else if(filterEntity1.startsWith("jid")) // 7.3. keywords of a journal; 
				{
					query = "SELECT name FROM dblp_plus.journal WHERE jid = " + id; 
					ResultSet entityName = statement.executeQuery(query); 
					if(entityName.next())
					{
						entity = entityName.getString(1); 
					}

					ArrayList<String> keywords = TopGen.topNamesGen(statement, "journalKeyword", Integer.parseInt(id), page*100); 
					if(keywords.size() > 0)
					{
						html += "<h3><b>Keywords related to " + URLGen.addLinkage("journal", Integer.parseInt(id), entity) + ": </b></h3>"; 
					}
					for(int i = 0; i < keywords.size(); i++)
					{
						if(i > (page-1)*100-1)
						{
							html += "<p>" + (i+1) + ". " + keywords.get(i) + "</p>"; 
						}
					}
				}
			}
			
			if(targetEntity.endsWith("organizations")) // 8. top organizations; 
			{
				String id = filterEntity1.split("=")[1]; 
				String entity = ""; 
				String query = ""; 
				
				if(filterEntity1.startsWith("cid")) // 8.1. top organizations in a conference; 
				{
					query = "SELECT name FROM dblp_plus.conference WHERE cid = " + id; 
					ResultSet entityName = statement.executeQuery(query); 
					if(entityName.next())
					{
						entity = entityName.getString(1); 
					}

					ArrayList<String> keywords = TopGen.topNamesGen(statement, "conferenceOrganization", Integer.parseInt(id), page*100); 
					if(keywords.size() > 0)
					{
						html += "<h3><b>Top Organizations in " + URLGen.addLinkage("organization", Integer.parseInt(id), entity) + ": </b></h3>"; 
					}
					for(int i = 0; i < keywords.size(); i++)
					{
						if(i > (page-1)*100-1)
						{
							html += "<p>" + (i+1) + ". " + keywords.get(i) + "</p>"; 
						}
					}
				}
				else if(filterEntity1.startsWith("jid")) // 8.2. top organizations in a journal; 
				{
					query = "SELECT name FROM dblp_plus.journal WHERE jid = " + id; 
					ResultSet entityName = statement.executeQuery(query); 
					if(entityName.next())
					{
						entity = entityName.getString(1); 
					}

					ArrayList<String> keywords = TopGen.topNamesGen(statement, "journalOrganization", Integer.parseInt(id), page*100); 
					if(keywords.size() > 0)
					{
						html += "<h3><b>Top Organizations in " + URLGen.addLinkage("organization", Integer.parseInt(id), entity) + ": </b></h3>"; 
					}
					for(int i = 0; i < keywords.size(); i++)
					{
						if(i > (page-1)*100-1)
						{
							html += "<p>" + (i+1) + ". " + keywords.get(i) + "</p>"; 
						}
					}
				}
			}
			
			int previousPage = 1; 
			int nextPage = page+1; 
			if(page == 1)
			{
				previousPage = 1; 
			}
			else
			{
				previousPage = page-1; 
			}
			html += URLGen.viewListURL("<u style = \"margin-left:380px\">prev</u>", targetEntity, "page=" + previousPage, filterEntity1); 
			html += URLGen.viewListURL("<u style = \"margin-left:50px\">next</u>", targetEntity, "page=" + nextPage, filterEntity1); 
		}
		
		
		if(!filterEntity2.isEmpty())
		{
			if(targetEntity.endsWith("papers"))
			{
				if((filterEntity1.startsWith("kid") && (filterEntity2.startsWith("aid") || filterEntity2.startsWith("cid") || filterEntity2.startsWith("jid")))
					|| ((filterEntity1.startsWith("cid") || filterEntity1.startsWith("jid")) && filterEntity2.startsWith("aid")))
				{
					String temp = filterEntity1; 
					filterEntity1 = filterEntity2; 
					filterEntity2 = temp; 
				}
				
				ArrayList<Integer> pids = new ArrayList<Integer>(); 
				String queryPapers = ""; 
				String from1 = ""; 
				String from2 = ""; 
				String where1 = ""; 
				String where2 = ""; 
				
				String filter1 = filterEntity1.split("=")[0]; 
				String filter2 = filterEntity2.split("=")[0]; 
				String id1 = filterEntity1.split("=")[1]; 
				String id2 = filterEntity2.split("=")[1]; 
				String type1 = ""; 
				String type2 = ""; 
				String entity1 = ""; 
				String entity2 = ""; 
				String prop1 = ""; 
				String prop2 = ""; 
				
				if(filter1.equals("aid"))
				{
					String query = "SELECT name FROM dblp_plus.author WHERE aid = " + id1; 
					ResultSet rs = statement.executeQuery(query); 
					if(rs.next())
					{
						type1 = "author"; 
						entity1 = rs.getString(1); 
						prop1 = "by"; 
						from1 = "dblp_plus.writes w1"; 
						where1 = "w1.pid = p1.pid AND w1.aid = " + id1; 
					}
				}
				else if(filter1.equals("cid"))
				{
					String query = "SELECT name FROM dblp_plus.conference WHERE cid = " + id1; 
					ResultSet rs = statement.executeQuery(query); 
					if(rs.next())
					{
						type1 = "conference"; 
						entity1 = rs.getString(1); 
						prop1 = "in"; 
						where1 = "p1.cid = " + id1; 
					}
				}
				else if(filter1.equals("jid"))
				{
					String query = "SELECT name FROM dblp_plus.journal WHERE jid = " + id1; 
					ResultSet rs = statement.executeQuery(query); 
					if(rs.next())
					{
						type1 = "journal"; 
						entity1 = rs.getString(1); 
						prop1 = "in"; 
						where1 = "p1.jid = " + id1; 
					}
				}
				else if(filter1.equals("kid"))
				{
					String query = "SELECT name FROM dblp_plus.keyword WHERE kid = " + id1; 
					ResultSet rs = statement.executeQuery(query); 
					if(rs.next())
					{
						type1 = "keyword"; 
						entity1 = rs.getString(1); 
						prop1 = "containing the Keyword"; 
						from1 = "dblp_plus.publication_keyword pk1"; 
						where1 = "pk1.pid = p1.pid AND pk1.kid = " + id1; 
					}
				}
				else if(filter1.equals("oid"))
				{
					String query = "SELECT name FROM dblp_plus.organization WHERE oid = " + id1; 
					ResultSet rs = statement.executeQuery(query); 
					if(rs.next())
					{
						type1 = "organization"; 
						entity1 = rs.getString(1); 
						prop1 = "by researchers in"; 
						from1 = "dblp_plus.writes w1, dblp_plus.author a1"; 
						where1 = "w1.pid = p1.pid AND w1.aid = a1.aid AND a1.oid = " + id1; 
					}
				}

				if(filter2.equals("aid"))
				{
					String query = "SELECT name FROM dblp_plus.author WHERE aid = " + id2; 
					ResultSet rs = statement.executeQuery(query); 
					if(rs.next())
					{
						type2 = "author"; 
						entity2 = rs.getString(1); 
						prop2 = "and"; 
						from2 = "dblp_plus.writes w2"; 
						where2 = "w2.pid = p1.pid AND w2.aid = " + id2; 
					}
				}
				else if(filter2.equals("cid"))
				{
					String query = "SELECT name FROM dblp_plus.conference WHERE cid = " + id2; 
					ResultSet rs = statement.executeQuery(query); 
					if(rs.next())
					{
						type2 = "conference"; 
						entity2 = rs.getString(1); 
						prop2 = "in"; 
						where2 = "p1.cid = " + id2; 
					}
				}
				else if(filter2.equals("jid"))
				{
					String query = "SELECT name FROM dblp_plus.journal WHERE jid = " + id2; 
					ResultSet rs = statement.executeQuery(query); 
					if(rs.next())
					{
						type2 = "journal"; 
						entity2 = rs.getString(1); 
						prop2 = "in"; 
						where2 = "p1.jid = " + id2; 
					}
				}
				else if(filter2.equals("kid"))
				{
					String query = "SELECT keyword FROM dblp_plus.keyword WHERE kid = " + id2; 
					ResultSet rs = statement.executeQuery(query); 
					if(rs.next())
					{
						type2 = "keyword"; 
						entity2 = rs.getString(1); 
						prop2 = "containing the Keyword"; 
						from2 = "dblp_plus.publication_keyword pk2"; 
						where2 = "pk2.pid = p1.pid AND pk2.kid = " + id2; 
					}
				}

				queryPapers = "SELECT DISTINCT p1.pid FROM dblp_plus.publication p1"; 
				if(!from1.isEmpty())
				{
					queryPapers += ", " + from1; 
				}
				if(!from2.isEmpty())
				{
					queryPapers += ", " + from2; 
				}
				queryPapers += " WHERE " + where1 + " AND " + where2 + " ORDER BY p1.year DESC LIMIT " + ((page-1)*100) + ", " + 100; 
				
				ResultSet result = statement.executeQuery(queryPapers); 
				while(result.next())
				{
					pids.add(result.getInt("pid")); 
				}
				result.close(); 
				
				if(pids.size() > 0)
				{
					html += "<h3>Papers "; 
					html += prop1 + " " + URLGen.addLinkage(type1, Integer.parseInt(id1), entity1) + " " + prop2 + " " + URLGen.addLinkage(type2, Integer.parseInt(id2), entity2); 
					html += "</h3>"; 
				}
				for(int i = 0; i < pids.size(); i++)
				{
					html += "<p>" + PaperHomepage.paperBrief(statement, pids.get(i), ((page-1)*100+i+1)) + "</p>"; 
				}				
			}
			
			int previousPage = 1; 
			int nextPage = page+1; 
			if(page == 1)
			{
				previousPage = 1; 
			}
			else
			{
				previousPage = page-1; 
			}
			html += URLGen.viewListURL("<u style = \"margin-left:380px\">prev</u>", targetEntity, Integer.parseInt(filterEntity1.split("=")[1]), Integer.parseInt(filterEntity2.split("=")[1]), previousPage+""); 
			html += URLGen.viewListURL("<u style = \"margin-left:50px\">next</u>", targetEntity, Integer.parseInt(filterEntity1.split("=")[1]), Integer.parseInt(filterEntity2.split("=")[1]), nextPage+""); 
		}
		
		return html; 
	}
}
