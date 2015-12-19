package dataCrawling;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class DBLPCrawlerThread extends Thread
{
	private String relation = ""; 
	private Statement statementUpdate; 
		
	public DBLPCrawlerThread(String relation, Statement statementUpdate)
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
				String url = DBLPCrawler.getAnID(relation); 
				if(url == null)
				{
					return; 
				}
				if(relation.equals("authorID"))
				{
					crawlAuthorID(url); 
				}
				if(relation.equals("author"))
				{
					crawlAuthor(url); 
				}
				if(relation.equals("conference"))
				{
					crawlConference(url); 
				}
				if(relation.equals("journal"))
				{
					crawlJournal(url); 
				}
			}
			catch(Exception e)
			{
				e.printStackTrace(); 
			}
		}
	}
	
	// Step 2. crawl the ids for each author; 
	public void crawlAuthorID(String id) throws SQLException, IOException
	{
		Document doc; 
		String url = "http://dblp.uni-trier.de/pers?pos=" + id; 
		DBLPCrawler.getIP(); 

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
    		
			Elements elements = doc.getElementById("browse-person-output").getElementsByTag("li"); 		
			for(int i = 0; i < elements.size(); i++)
			{
				String author_url = elements.get(i).getElementsByAttribute("href").get(0).attr("href"); 
		    	String command = "INSERT INTO dblp.author VALUES (\"" + author_url + "\", \"\", \"\", \"\", \"\", \"\"); "; 
		    	
		    	try
		    	{
		    		statementUpdate.executeUpdate(command); 
		    		System.out.println(command);
		    	}
		    	catch(Exception e)
		    	{
		    		continue; 
		    	}
			}
			statementUpdate.executeUpdate("UPDATE dblp.page SET exist = 1 WHERE page_id = " + id); 
		}
		catch(IOException e)
		{
			System.out.println(System.getProperty("http.proxyHost") + " " + System.getProperty("http.proxyPort"));
			try
			{
				DBLPCrawler.getIP(); 
			}
			catch(IOException ioe){}
			crawlAuthorID(id); 
		}
	}
	
	// Step 3. crawl authors, together with their publications; 
	public void crawlAuthor(String url) throws SQLException, IOException
	{
		Document doc; 
		DBLPCrawler.getIP(); 

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
    		
			String authorName = doc.getElementsByTag("h1").get(0).child(0).ownText(); 
			String alias = ""; 
			String person_info = ""; 
			String homepage = ""; 
			String bib_url = ""; 

			try
			{
				if(doc.getElementsByTag("h1").get(0).child(0).children().size() > 0)
				{
					alias = doc.getElementsByTag("h1").get(0).child(0).children().get(0).ownText(); 
				}
				
				if(doc.getElementsByAttributeValue("class", "profile hideable").size() > 0
					&& doc.getElementsByAttributeValue("class", "profile hideable").get(0).getElementsByTag("li").size() > 0)
				{
					person_info = doc.getElementsByAttributeValue("class", "profile hideable").get(0).getElementsByTag("li").get(0).ownText();
				}
				
				if(doc.getElementsByAttributeValue("src", "http://dblp.uni-trier.de/img/home.dark.16x16.png").size() > 0)
				{
					homepage = doc.getElementsByAttributeValue("src", "http://dblp.uni-trier.de/img/home.dark.16x16.png").get(0).parentNode().attr("href"); 
				}
				
				if(doc.getElementsByAttributeValue("src", "http://dblp.uni-trier.de/img/bibtex.dark.16x16.png").size() > 0)
				{
					bib_url = doc.getElementsByAttributeValue("src", "http://dblp.uni-trier.de/img/bibtex.dark.16x16.png").get(0).parentNode().attr("href"); 
				}
				
				if(authorName.length() < 100 && alias.length() < 100 && person_info.length() < 200 && homepage.length() < 200 && bib_url.length() < 200)
				{
					if(authorName.contains("\""))
					{
						authorName = authorName.replaceAll("\"", ""); 
					}
					if(person_info.contains("\""))
					{
						person_info = person_info.replaceAll("\"", ""); 
					}
					
					String commandAuthor = "UPDATE dblp.author SET name = \"" + authorName + "\", alias = \"" + alias + "\", person_info = \"" + person_info
						+ "\", homepage = \"" + homepage + "\", bib_url = \"" + bib_url + "\" WHERE dblp_id = \"" + url + "\"; "; 	
					statementUpdate.executeUpdate(commandAuthor); 
					
					Elements publications = doc.getElementsByAttributeValue("itemtype", "http://schema.org/ScholarlyArticle"); 
					for(int i = 0; i < publications.size(); i++)
					{
						String paper_id = ""; 
						String paper_title = ""; 
						int year = 0; 
						String raw_c = ""; 
						String raw_j = ""; 
						String doi = ""; 
						
						if(publications.get(i).getElementsByAttributeValue("src", "http://dblp.uni-trier.de/img/paper.dark.hollow.16x16.png").size() > 0)
						{
							doi = publications.get(i).getElementsByAttributeValue("src", "http://dblp.uni-trier.de/img/paper.dark.hollow.16x16.png").get(0).parent().attr("href"); 
						}
						if(publications.get(i).getElementsByAttributeValue("src", "http://dblp.uni-trier.de/img/download.dark.hollow.16x16.png").size() > 0)
						{
							paper_id = publications.get(i).getElementsByAttributeValue("src", "http://dblp.uni-trier.de/img/download.dark.hollow.16x16.png").get(0).parent().attr("href"); 
						}
						if(publications.get(i).getElementsByAttributeValue("class", "title").size() > 0)
						{
							paper_title = publications.get(i).getElementsByAttributeValue("class", "title").get(0).text(); 
							paper_title = paper_title.replaceAll("\"", ""); 
						}
						if(publications.get(i).getElementsByAttributeValue("itemprop", "datePublished").size() > 0)
						{
							try
							{
								year = Integer.parseInt(publications.get(i).getElementsByAttributeValue("itemprop", "datePublished").get(0).ownText()); 
							}
							catch(Exception e){}
						}
						if(publications.get(i).getElementsByAttributeValueStarting("href", "http://dblp.uni-trier.de/db/journals/").size() > 0)
						{
							raw_j = publications.get(i).getElementsByAttributeValueStarting("href", "http://dblp.uni-trier.de/db/journals/").get(0).attr("href").split("#")[0]; 
						}
						else if(publications.get(i).getElementsByAttributeValueStarting("href", "http://dblp.uni-trier.de/db/conf/").size() > 0)
						{
							raw_c = publications.get(i).getElementsByAttributeValueStarting("href", "http://dblp.uni-trier.de/db/conf/").get(0).attr("href").split("#")[0]; 
						}
						
						String commandPaper = "INSERT INTO dblp.publication VALUES (\"" + paper_id + "\", \"" + paper_title + "\", " + year + ", \"" + raw_c + "\", \"" + raw_j + "\", \"" + doi + "\"); "; 
						try
						{
							statementUpdate.executeUpdate(commandPaper); 
						}
						catch(Exception e){}

						Elements authors = publications.get(i).getElementsByAttributeValue("itemprop", "author"); 
						for(int j = 0; j < authors.size(); j++)
						{
							String authorURL = ""; 
							if(authors.get(j).getElementsByAttributeValue("class", "this-person").size() > 0)
							{
								authorURL = url; 
							}
							else
							{
								authorURL = authors.get(j).child(0).attr("href"); 
							}
							
							String commandWrites = "INSERT INTO dblp.writes VALUES (\"" + authorURL + "\", \"" + paper_id + "\", " + (j+1) + "); "; 
							try
							{
								statementUpdate.executeUpdate(commandWrites); 
							}
							catch(Exception e){}
						}
					}
				}			
			}
			catch(Exception e)
			{
				System.out.println(url);
				return; 
			}
		}
		catch(Exception e)
		{
			try
			{
				DBLPCrawler.getIP(); 
			}
			catch(Exception ioe){}
		}
	}

	// Step 6. crawl the conferences; 
	public void crawlConference(String url) throws SQLException, IOException
	{
		Document doc; 
		DBLPCrawler.getIP(); 
		String full_name = ""; 

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
    		full_name = doc.getElementsByTag("h1").get(0).ownText(); 
    		full_name = full_name.replaceAll("\"", ""); 
    		String command = "INSERT INTO dblp.conference VALUES (\"" + url + "\", \"" + full_name + "\", \"\"); "; 
    		try
    		{
    			statementUpdate.executeUpdate(command); 
    		}
    		catch(Exception e)
    		{
    			System.out.println(command);
    		}
		}
		catch(IOException e)
		{
			System.out.println(System.getProperty("http.proxyHost") + " " + System.getProperty("http.proxyPort"));
			try
			{
				DBLPCrawler.getIP(); 
			}
			catch(IOException ioe){}
		}
	}
	
	// Step 7. crawl the journals; 
	public void crawlJournal(String url) throws SQLException, IOException
	{
		Document doc; 
		DBLPCrawler.getIP(); 
		String full_name = ""; 

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
    		full_name = doc.getElementsByTag("h1").get(0).ownText(); 
    		full_name = full_name.replaceAll("\"", ""); 
    		String command = "INSERT INTO dblp.journal VALUES (\"" + url + "\", \"" + full_name + "\", \"\"); "; 
       		try
       		{
       			statementUpdate.executeUpdate(command); 
       		}
       		catch(Exception e)
       		{
       			System.out.println(command);
       		}
		}
		catch(IOException e)
		{
			System.out.println(System.getProperty("http.proxyHost") + " " + System.getProperty("http.proxyPort"));
			try
			{
				DBLPCrawler.getIP(); 
			}
			catch(IOException ioe){}
		}
	}

}