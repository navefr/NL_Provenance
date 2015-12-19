package dataCrawling;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ACMCrawlerThread extends Thread
{
	private Statement statementUpdate; 
		
	public ACMCrawlerThread(Statement statementUpdate)
	{
		this.statementUpdate = statementUpdate; 
	}
	
	public void run()
	{
		while(true)
		{
			try
			{
				String url = ACMCrawler.getAnID(); 
				if(url == null)
				{
					return; 
				}
				crawlPaper(url); 
			}
			catch(Exception e)
			{
				e.printStackTrace(); 
			}
		}
	}
	
	// crawl a paper; 
	public void crawlPaper(String pid) throws SQLException, IOException
	{
		String url = "http://dl.acm.org/citation.cfm?id=" + pid + "&preflayout=flat"; 

		Document doc; 
		ACMCrawler.getIP(); 

		try
		{
	    	String cookie1 = "" + (int)(Math.random()*100); 
	    	String cookie2 = "" + (int)(Math.random()*200);
			doc = Jsoup.connect(url)
				.data("query", "Java")
				.userAgent("Mozilla") 
				.cookie(cookie1, cookie2)
				.maxBodySize(0)
				.timeout(30000)
				.post();
			
			String title = ""; 
			String doi = "";
			String year = ""; 
			String paperAbstract = ""; 

			if(doc.getElementsByAttributeValue("name", "citation_title").size() > 0)
			{
				title = doc.getElementsByAttributeValue("name", "citation_title").get(0).attr("content"); 
				title = title.replaceAll("\"", ""); 
			}
			if(doc.getElementsByAttributeValue("name", "citation_doi").size() > 0)
			{
				doi = doc.getElementsByAttributeValue("name", "citation_doi").get(0).attr("content"); 
			}
			if(doc.getElementsByAttributeValue("name", "citation_date").size() > 0)
			{
				year = doc.getElementsByAttributeValue("name", "citation_date").get(0).attr("content"); 
			}

			if(doc.getElementsByAttributeValue("style", "margin-left:10px; margin-top:10px; margin-right:10px; margin-bottom: 10px;").size() > 0)
 			{
	 			Element paper_ab = doc.getElementsByAttributeValue("style", 
	 					"margin-left:10px; margin-top:10px; margin-right:10px; margin-bottom: 10px;").get(0); 
 				if(paper_ab.getElementsByAttributeValue("style", "display:inline").size() > 0)
 				{
 					paperAbstract = paper_ab.getElementsByAttributeValue("style", "display:inline").get(0).text(); 
 					if(paperAbstract.length() > 1995)
 					{
 						paperAbstract = paperAbstract.substring(0, 1995) + "..."; 
 						paperAbstract = paperAbstract.replaceAll("\"", ""); 
 					}
 				}
			}	
			
			// 0. author; 1. references; 2. citations; 
			Elements elements = doc.getElementsByAttributeValue("style", 
				"margin-left:10px; margin-top:0px; margin-right:10px; margin-bottom: 10px;"); 
			Elements refs = elements.get(1).getElementsByTag("tr"); 
			Elements cites = elements.get(2).getElementsByTag("tr"); 

			String command1 = "UPDATE acm.paper_id SET done = 1 WHERE pid = " + pid + "; "; 
			statementUpdate.executeUpdate(command1); 

			if(title.length() > 1 && (paperAbstract.length() > 30 || refs.size() > 0 || cites.size() > 0)
				&& title.length() < 200 && year.length() < 45 && doi.length() < 200 && paperAbstract.length() < 2000)
			{
				String command2 = "INSERT INTO acm.publication VALUES (" + pid + ", \"" + title + "\", \"" + year + "\", \"" + paperAbstract
					+ "\", \"" + doi + "\"); "; 
				statementUpdate.executeUpdate(command2); 
				
				for(int i = 0; i < refs.size(); i++)
				{
					Element reference = refs.get(i).getElementsByTag("td").get(2).getElementsByTag("div").get(0); 
					String reference_id = ""; 
					if(reference.getElementsByAttribute("href").size() > 0)
					{
						reference_id = reference.getElementsByAttribute("href").get(0).attr("href"); 
						reference_id = reference_id.substring(reference_id.indexOf("?id=") + 4); 
						reference_id = reference_id.substring(0, reference_id.indexOf("&")); 
					}
					if(reference_id.isEmpty())
					{
						String reference_title = reference.text(); 
						if(reference_title.length() < 400)
						{
							String command3 = "INSERT INTO acm.cite VALUES (" + pid + ", " + reference_title.hashCode() + "); "; 
							statementUpdate.executeUpdate(command3); 
							
							command3 = "INSERT INTO acm.raw_publication VALUES (" + reference_title.hashCode() + ", \"" + reference_title + "\"); "; 
							statementUpdate.executeUpdate(command3); 
						}
					}
					else
					{
						String command3 = "INSERT INTO acm.cite VALUES (" + pid + ", " + reference_id + "); "; 
						statementUpdate.executeUpdate(command3); 
					}
				}

				for(int i = 0; i < cites.size(); i++)
				{
					Element citation = cites.get(i).getElementsByTag("div").get(0); 
					String citation_id = ""; 
					
					if(citation.getElementsByAttribute("href").size() > 0)
					{
						citation_id = citation.getElementsByAttribute("href").get(0).attr("href"); 
						citation_id = citation_id.substring(citation_id.indexOf("?id=") + 4); 
						citation_id = citation_id.substring(0, citation_id.indexOf("&")); 
						
						String command3 = "INSERT INTO acm.cite VALUES (" + citation_id + ", " + pid + "); "; 
						statementUpdate.executeUpdate(command3); 
					}
				}
			}
		}
		catch(Exception e)
		{
			try
			{
				ACMCrawler.getIP(); 
			}
			catch(Exception ioe){}
		}
	}
}
