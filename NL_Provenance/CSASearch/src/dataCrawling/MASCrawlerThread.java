package dataCrawling;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

public class MASCrawlerThread extends Thread
{
	private String relation = ""; 
	private Statement statementUpdate; 
	private int round = 5; 
		
	public MASCrawlerThread(String relation, Statement statementUpdate)
	{
		this.relation = relation; 
		this.statementUpdate = statementUpdate; 
	}
	
	public void run()
	{
		while(true)
		{
			try
			{
				String url = MASCrawler.getAnID(relation); 
				if(url == null)
				{
					return; 
				}
				if(relation.equals("paperIDs"))
				{
					crawlPaperIDs(url); 
				}
				if(relation.equals("papers"))
				{
					crawlPapers(url); 
				}
				if(relation.equals("authors"))
				{
					crawlAuthors(url); 
				}
				if(relation.equals("conferences"))
				{
					crawlCJ(url); 
				}
				if(relation.equals("journals"))
				{
					crawlCJ(url); 
				}
				if(relation.equals("organizations"))
				{
					crawlOrganization(url); 
				}
				if(relation.equals("keywords"))
				{
					crawlKeywords(url); 
				}
				if(relation.equals("citations"))
				{
					crawlCitations(url); 
				}
				if(relation.equals("abstract"))
				{
					crawlAbstract(url); 
				}
			}
			catch(Exception e)
			{
				e.printStackTrace(); 
			}
		}
	}
	
	// Step 2. get all the pids for computer science papers; 
	public void crawlPaperIDs(String id) throws SQLException, IOException
	{
		Document doc; 
		String url = "http://academic.research.microsoft.com/RankList?entitytype=1&topDomainID=2&subDomainID=0&last=0&start=" + id + "&end=" + (id + 99); 
		MASCrawler.getIP(); 

		try
		{
	    	String cookie1 = "" + (int)(Math.random()*100); 
	    	String cookie2 = "" + (int)(Math.random()*200);
			doc = Jsoup.connect(url)
				.data("query", "Java")
				.userAgent("Mozilla") 
				.cookie(cookie1, cookie2)
				.timeout(30000)
				.post();
    		
			try
			{
				Elements pids = doc.getElementById("ctl00_MainContent_divRankList").getElementsByTag("tr");
				for(int i = 1; i < pids.size(); i++)
				{
					String pid = pids.get(i).getElementsByTag("a").attr("href"); 
					pid = pid.split("/")[2]; 
					try
					{
						statementUpdate.executeUpdate("INSERT INTO mas.ids VALUES (\"pid\", " + pid + ", 0); "); 
					}
					catch(Exception e) {}
				}
				
				if(pids.size() > 0)
				{
					statementUpdate.executeUpdate("UPDATE mas.pages SET done = 1 WHERE start = " + id + "; "); 
				}
			}
			catch(Exception e)
			{}
		}
		catch(Exception e)
		{
			try
			{
				MASCrawler.getIP(); 
			}
			catch(IOException ioe){}
			crawlPaperIDs(id); 
		}
	}
	
	// Step 3. get all the papers according to the pids; 
	public void crawlPapers(String pid) throws SQLException, IOException
	{
		try
		{
			MASCrawler.getIP(); 
		}
		catch(IOException ioe){}

		Document doc; 
		String url = "http://academic.research.microsoft.com/Publication/" + pid; 
		
		try
		{
	    	String cookie1 = "" + (int)(Math.random()*100); 
	    	String cookie2 = "" + (int)(Math.random()*200);
			doc = Jsoup.connect(url)
				.data("query", "Java")
				.userAgent("Mozilla") 
				.cookie(cookie1, cookie2)
				.timeout(30000)
				.post();
		}
		catch(IOException e)
		{
			crawlPapers(pid); 
			return; 
		}
		
		Elements elements = doc.getElementsByClass("title-span"); 
		if(elements.size() == 0)
		{
			return; 
		}
		
		statementUpdate.executeUpdate("UPDATE mas.ids SET exist = 1 WHERE relation = \"pid\" AND id = " + pid); 
		
		String title = elements.get(0).ownText().replace("\"", "");
		title = title.replace("\\", "");
		if(title.length() > 195)
		{
			title = title.substring(0, 195) + "..."; 
		}

		String paper_abstract = ""; 
		elements = doc.getElementsByClass("paper-card").get(0).getElementsByClass("abstract"); 
		if(elements.size() > 0 && elements.get(0).children().size() > 0)
		{
			paper_abstract = elements.get(0).child(0).text().replace("\"", "");
			if(paper_abstract.length() > 1995)
			{
				paper_abstract = paper_abstract.substring(0, 1995); 
				paper_abstract += "..."; 
			}
		}
		
		String cid = "0"; 
		String jid = "0"; 
		Element conference = doc.getElementById("ctl00_MainContent_PaperItem_HLConference"); 
		if(conference != null)
		{
			cid = conference.attr("href").split("/")[2]; 
		}
		Element journal = doc.getElementById("ctl00_MainContent_PaperItem_HLJournal"); 
		if(journal != null)
		{
			jid = journal.attr("href").split("/")[2]; 
		}
		
		String year = "0"; 
		elements = doc.getElementsByClass("paper-card").get(0).getElementsByClass("year"); 
		if(elements.size() > 0)
		{
			year = elements.get(0).ownText(); 
			try
			{
		    	year = year.substring(year.length()-4); 
		    	Integer.parseInt(year); 
			}
			catch(Exception e)
			{
				year = "0"; 
			}
		}
		else
		{
			try
			{
				year = doc.getElementById("ctl00_MainContent_PaperItem_lblYear").ownText(); 
				year = year.substring(year.length()-5, year.length()-1); 
			}
			catch(Exception e)
			{
				year = "0"; 
			}
		}
		String references = "0"; 
		try
		{
			references =  doc.getElementById("ctl00_MainContent_PaperList_ctl00_HeaderLink").child(0).ownText().replace("(", "");
			references = references.replace(")", ""); 
		    Integer.parseInt(references); 
		}
		catch(Exception e)
		{
			references = "0"; 
		}
		
		String citations = "0"; 
		elements = doc.getElementsByClass("paper-card").get(0).getElementsByClass("citation"); 
		if(elements.size() > 0 && elements.get(0).children().size() > 0 && elements.get(0).child(0).children().size() > 0)
		{
			citations = elements.get(0).child(0).child(0).ownText().replace("Citations: ", "");
			try
			{
		    	Integer.parseInt(citations); 
			}
			catch(Exception e)
			{
				citations = "0"; 
			}
		}
		
		String doi = ""; 
		Element doi_element = doc.getElementById("ctl00_MainContent_PaperItem_hypDOIText"); 
		if(doi_element != null)
		{
			doi = doi_element.ownText(); 
		}
		
		String command = "INSERT INTO mas.publication VALUES (" + pid + ", \"" + title + "\", \"" + paper_abstract + "\", "
			+ year + ", " + cid + ", " + jid + ", " + references + ", " + citations + ", \"" + doi + "\")"; 
		try
		{
			statementUpdate.executeUpdate(command); 
		}
		catch(Exception e)
		{}

		elements = doc.getElementsByClass("paper-card").get(0).getElementsByClass("author-name-tooltip"); 
		for(int i = 0; i < elements.size(); i++)
		{
			String author = ""; 
			try
			{
				author = elements.get(i).attr("href"); 
				int author_start = author.indexOf("r/") + 2; 
				int author_end = author.indexOf("/", author_start); 
				author = author.substring(author_start, author_end); 
			}
			catch(Exception e)
			{
				continue; 
			}
			if(!author.isEmpty())
			{
				command = "INSERT INTO mas.writes VALUES (" + author + ", " + pid + ")"; 
				try
				{
					statementUpdate.executeUpdate(command); 
				}
				catch(Exception e)
				{}
			}
		}
		
		elements = doc.getElementsByClass("section-wrapper"); 
		{
			if(elements.size() > 0)
			{
				elements = elements.get(0).getElementsByTag("a");  
				for(int i = 0; i < elements.size(); i++)
				{
					String keyword = elements.get(i).attr("href"); 
					if(!keyword.contains("/Keyword/"))
					{
						break; 
					}
					keyword = keyword.replace("/Keyword/", ""); 
					keyword = keyword.substring(0, keyword.indexOf("/")); 
					
					try
					{
				    	Integer.parseInt(keyword); 
						command = "INSERT INTO mas.publication_keyword VALUES (" + pid + ", " + keyword + ")"; 
						statementUpdate.executeUpdate(command); 
					}
					catch(Exception e)
					{}
				}
			}
		} 
	}	

	// Step 4. get all the authors according to the aids; 
	public void crawlAuthors(String aid) throws SQLException, IOException
	{
		try
		{
			MASCrawler.getIP(); 
		}
		catch(IOException ioe){}

		Document doc; 
		String url = "http://academic.research.microsoft.com/Author/" + aid; 
		
		try
		{
	    	String cookie1 = "" + (int)(Math.random()*100); 
	    	String cookie2 = "" + (int)(Math.random()*200);
			doc = Jsoup.connect(url)
				.data("query", "Java")
				.userAgent("Mozilla") 
				.cookie(cookie1, cookie2)
				.timeout(30000)
				.post();
		}
		catch(IOException e)
		{
			try
			{
				MASCrawler.getIP(); 
			}
			catch(Exception ioe){}

			crawlAuthors(aid); 
			return; 
		}
		
		String name = ""; 
		try
		{
			name = doc.getElementById("ctl00_MainContent_AuthorItem_authorName").ownText(); 
		}
		catch(Exception e)
		{
			return; 
		}
		
		
		try
		{
			Elements elements = doc.getElementsByClass("line-height-small"); 
			for(int j = 0; j < elements.size(); j++)
			{
				Elements ets = elements.get(j).getElementsByTag("a");				 
				for(int i = 0; i < ets.size(); i++)
				{
					String domain = ets.get(i).attr("href"); 
					if(domain.contains("subDomain"))
					{
						int top_start = domain.indexOf("topDomainID"); 
						top_start = domain.indexOf("=", top_start) + 1; 
						int top_end = domain.indexOf("&", top_start); 
						String top_domain = domain.substring(top_start, top_end);
						if(top_domain.equals("2"))
						{
							int sub_start = domain.indexOf("=", top_end) + 1; 
							int sub_end = domain.indexOf("&", sub_start); 
							String sub_domain = domain.substring(sub_start, sub_end); 

							String command = "INSERT INTO mas.domain_author VALUES (" + aid + ", " + sub_domain + ")"; 
							try
							{
								statementUpdate.executeUpdate(command); 
							}
							catch(Exception e)
							{}
						}
					}
					else
					{
						break; 
					}
				}
			}
		}
		catch(Exception e){}
		
		String oid = "0"; 
		try
		{
			Element organization = doc.getElementById("ctl00_MainContent_AuthorItem_affiliation");
			String organization_name = organization.attr("href"); 
			int o_start = organization_name.indexOf("Organization/") + 13; 
			int o_end = organization_name.indexOf("/", o_start); 
			oid = organization_name.substring(o_start, o_end); 
		}
		catch(Exception e) {}
		
		String homePage = ""; 
		Element element = doc.getElementById("ctl00_MainContent_AuthorItem_imgHomePageLink");
		if(element != null)
		{
			homePage = element.attr("href"); 
			if(homePage.length() > 195)
			{
				homePage = ""; 
			}
		}
		
		String pic = ""; 
		try
		{
			pic = doc.getElementById("ctl00_MainContent_AuthorItem_imgAuthorPhoto").attr("title"); 
			if(pic.length() > 195)
			{
				pic = ""; 
			}
		}
		catch(Exception e)
		{}
		
		String command = "INSERT INTO mas.author VALUES (" + aid + ", \"" + name + "\", " + oid + ", \"" + homePage + "\", \"" + pic + "\")"; 
		try
		{
			statementUpdate.executeUpdate(command); 
		}
		catch(Exception e)
		{
			System.out.println(command);
		}
	}
	
	// Step 5. get all the conferences (together with conference_domain) according to the cids in paper; 
	// Step 6. get all the journals (together with journal_domain) according to the jids in paper; 
	public void crawlCJ(String id) throws SQLException
	{
		Document doc; 
		String url = ""; 
		
		if(relation.equals("conferences"))
		{
			url = "http://academic.research.microsoft.com/Conference/" + id;
		}
		else if(relation.equals("journals"))
		{
			url = "http://academic.research.microsoft.com/Journal/" + id;
		}

		try
		{
	    	String cookie1 = "" + (int)(Math.random()*100); 
	    	String cookie2 = "" + (int)(Math.random()*200);
			doc = Jsoup.connect(url)
				.data("query", "Java")
				.userAgent("Mozilla") 
				.cookie(cookie1, cookie2)
				.timeout(30000)
				.post();
    		
			String name = doc.getElementsByTag("title").text();
			String full_name = ""; 
			if(name.contains(" - "))
			{
				full_name = name.split(" - ")[1]; 
				name = name.split(" - ")[0]; 
			}
			
    		Elements elements = doc.getElementsByAttributeValueContaining("href", "http://academic.research.microsoft.com/RankList?entitytype=");     		
    		
    		if(elements.size() > 0)
    		{
    			for(int i = 0; i < elements.size(); i++)
    			{
    				String domain = elements.get(i).attr("href"); 

    				int start_top = domain.indexOf("topDomainID=");
    				start_top = domain.indexOf("=", start_top) + 1; 
    				int end_top = domain.indexOf("&", start_top); 
    				String top_domain = domain.substring(start_top, end_top); 
    				if(!top_domain.equals("2"))
    				{
    					continue; 
    				}
    				
    				int start_sub = domain.indexOf("subDomainID="); 
    				start_sub = domain.indexOf("=", start_sub) + 1; 
    				int end_sub = domain.indexOf("&", start_sub); 
    				String sub_domain = domain.substring(start_sub, end_sub); 

    				if(relation.equals("conferences"))
    				{
        				String command = "insert into mas.domain_conference values (" + id + ", " + sub_domain + ")";
        				try
        				{        				
        					statementUpdate.executeUpdate(command);
        				}
        				catch(Exception e)
        				{}
    				}
    				else if(relation.equals("journals"))
    				{
        				String command = "insert into mas.domain_journal values (" + id + ", " + sub_domain + ")";
        				try
        				{        				
        					statementUpdate.executeUpdate(command);
        				}
        				catch(Exception e)
        				{}
    				}
    			}
    		}

    		String homePage = ""; 
    		elements = doc.getElementsMatchingOwnText("Homepage"); 
    		if(elements.size() > 0)
    		{
        		homePage = elements.get(0).attr("href"); 
    		}

			String command = ""; 
			if(relation.equals("conferences"))
			{
				command = "insert into mas.conference values (" + id + ", \"" + name + "\"" + ", \"" + full_name + "\", \"" + homePage + "\")";
				if(command.contains("Sign in") || command.contains("Waiting") || name.isEmpty())
				{
					try
					{
						MASCrawler.getIP(); 
					}
					catch(IOException ioe){}
					crawlCJ(id); 	
					return; 
				}
				try
				{        				
					statementUpdate.executeUpdate(command);
				}
				catch(Exception e)
				{}
				try
				{        				
					statementUpdate.executeUpdate(command);
				}
				catch(Exception e)
				{}
			}
			else if(relation.equals("journals"))
			{
				command = "insert into mas.journal values (" + id + ", \"" + name + "\"" + ", \"" + full_name + "\", \"" + homePage + "\")"; 
				if(command.contains("Sign in") || command.contains("Waiting") || name.isEmpty())
				{
					try
					{
						MASCrawler.getIP(); 
					}
					catch(IOException ioe){}
					crawlCJ(id); 	
					return; 
				}
				try
				{        				
					statementUpdate.executeUpdate(command);
				}
				catch(Exception e)
				{}
			}
			System.out.println(command); 
		}
		catch(IOException e)
		{
			try
			{
				MASCrawler.getIP(); 
			}
			catch(IOException ioe){}
			crawlCJ(id); 
		}
	}
	
	// Step 7. get all the organizations according to the oids in authors; 
	public void crawlOrganization(String oid) throws SQLException
	{
		Document doc; 
		String url = "http://academic.research.microsoft.com/Organization/" + oid; 

		try
		{
	    	String cookie1 = "" + (int)(Math.random()*100); 
	    	String cookie2 = "" + (int)(Math.random()*200);
			doc = Jsoup.connect(url)
				.data("query", "Java")
				.userAgent("Mozilla") 
				.cookie(cookie1, cookie2)
				.timeout(30000)
				.post();
			
			String name = ""; 
			name = doc.getElementsByTag("title").get(0).text(); 
    		
    		String continent = ""; 
    		Elements elements = doc.getElementsByClass("continent"); 
    		if(elements.size() > 0)
    		{
    			elements = elements.get(0).getElementsByAttribute("href"); 
    			if(elements.size() > 0)
    			{
    				continent = elements.get(0).ownText(); 
    			}
    		}

    		String homePage = ""; 
    		elements = doc.getElementsMatchingOwnText("Homepage"); 
    		if(elements.size() > 0)
    		{
        		homePage = elements.get(0).attr("href"); 
    		}

			String command = "insert into mas.organization values(" + oid + ", \"" + name + "\"" + ", \"" + continent + "\", \"" + homePage + "\")";
			if(command.contains("Sign in"))
			{
				return; 
			}
			System.out.println(command);
			statementUpdate.executeUpdate(command); 
		}
		catch(IOException e)
		{
			try
			{
				MASCrawler.getIP(); 
			}
			catch(IOException ioe)
			{}
			crawlOrganization(oid); 
		}
	}	
	
	// Step 8. get all the keywords according to the kids in publicatin_keywords; 
	public void crawlKeywords(String kid) throws SQLException, IOException
	{
		try
		{
			MASCrawler.getIP(); 
		}
		catch(IOException ioe){}

		Document doc; 
		String url = "http://academic.research.microsoft.com/Keyword/" + kid; 
		
		try
		{
	    	String cookie1 = "" + (int)(Math.random()*100); 
	    	String cookie2 = "" + (int)(Math.random()*200);
			doc = Jsoup.connect(url)
				.data("query", "Java")
				.userAgent("Mozilla") 
				.cookie(cookie1, cookie2)
				.timeout(30000)
				.post();
		}
		catch(IOException e)
		{
			crawlKeywords(kid); 
			return; 
		}

		try
		{
			String keyword = doc.getElementById("ctl00_MainContent_KeywodItem_name").ownText();
			if(keyword.length() > 95)
			{
				return; 
			}
			
			String command = "INSERT INTO mas.keyword VALUES (" + kid + ", \"" + keyword + "\"); "; 
			try
			{
				statementUpdate.executeUpdate(command); 
			}
			catch(Exception e)
			{}
			
			try
			{
				String [] variations = doc.getElementById("ctl00_MainContent_KeywodItem_ltrStemmings").ownText().split(", "); 
				for(int i = 0; i < variations.length; i++)
				{
					String variation = variations[i]; 
					command = "INSERT INTO mas.keyword_variations VALUES (" + kid + ", \"" + variation + "\")"; 
					try
					{
						statementUpdate.executeUpdate(command); 
					}
					catch(Exception e)
					{}
				}
			}
			catch(Exception e)
			{}
		}
		catch(Exception e)
		{
			System.out.println(kid);
		}
	}	
	
	// Step 10. crawl the citation of a paper; 
	public void crawlCitations(String pid) throws SQLException
	{
		Document doc; 
		String url = "http://academic.research.microsoft.com/Detail?entitytype=1&searchtype=2&id=" + pid + "&start=" + (100*(round-1)+1) + "&end=" + (100*round); 
		
		try
		{
	    	String cookie1 = "" + (int)(Math.random()*100); 
	    	String cookie2 = "" + (int)(Math.random()*200);
			doc = Jsoup.connect(url)
				.data("query", "Java")
				.userAgent("Mozilla") 
				.cookie(cookie1, cookie2)
				.timeout(30000)
				.post();
		}
		catch(IOException e)
		{
			try
			{
				MASCrawler.getIP(); 
			}
			catch(IOException ioe)
			{}
			crawlCitations(pid); 
			
			return; 
		}
		
		Elements elements = doc.getElementsByClass("paper-item"); 
		if(elements.size() == 0)
		{
			return; 
		}
		
		for(int i = 0; i < elements.size(); i++)
		{
			Elements hrefs = elements.get(i).getElementsByAttribute("href"); 
			if(hrefs.size() > 0)
			{
				String href = hrefs.get(0).attr("href"); 
				if(href.startsWith("Publication/"))
				{
					href = href.replace("Publication/", ""); 
					href = href.substring(0, href.indexOf("/")); 
					String command = "INSERT INTO mas.cite VALUES (" + pid + ", " + Integer.parseInt(href) + ")"; 
					try
					{
						statementUpdate.executeUpdate(command); 
					}
					catch(Exception e)
					{
						System.out.println(command);
					}
				}
			}
		}
		
		statementUpdate.executeUpdate("UPDATE mas.ids SET exist = 1 WHERE relation = \"citation\" AND id = " + pid); 
	}	
	
	// Step 11. crawl the abstract of paper; 
	public void crawlAbstract(String pid) throws SQLException
	{
		try
		{
			MASCrawler.getIP(); 
		}
		catch(IOException ioe){}

		Document doc; 
		String url = "http://academic.research.microsoft.com/Publication/" + pid; 
		
		try
		{
	    	String cookie1 = "" + (int)(Math.random()*100); 
	    	String cookie2 = "" + (int)(Math.random()*200);
			doc = Jsoup.connect(url)
				.data("query", "Java")
				.userAgent("Mozilla") 
				.cookie(cookie1, cookie2)
				.timeout(30000)
				.post();
		}
		catch(IOException e)
		{
			crawlAbstract(pid); 
			return; 
		}
		
		Elements elements = doc.getElementsByClass("title-span"); 
		if(elements.size() == 0)
		{
			return; 
		}
		
		String paper_abstract = ""; 
		elements = doc.getElementsByClass("paper-card").get(0).getElementsByClass("abstract"); 
		if(elements.size() > 0 && elements.get(0).children().size() > 0)
		{
			paper_abstract = elements.get(0).child(0).text().replace("\"", "");
			if(paper_abstract.length() > 1995)
			{
				paper_abstract = paper_abstract.substring(0, 1995); 
				paper_abstract += "..."; 
			}
		}
		paper_abstract = paper_abstract.replaceAll("- ", ""); 
				
		statementUpdate.executeUpdate("UPDATE mas.ids SET exist = 1 WHERE relation = \"paper\" AND id = " + pid); 
		
		try
		{
			String command = "UPDATE mas.publication SET abstract = \"" + paper_abstract + "\" WHERE pid = " + pid; 
			statementUpdate.executeUpdate(command); 
			
			command = "UPDATE dblp_plus.publication SET abstract = \"" + paper_abstract + "\" WHERE mas_id = " + pid; 
			statementUpdate.executeUpdate(command); 
		}
		catch(Exception e)
		{}
	}	
}