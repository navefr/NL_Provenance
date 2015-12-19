package system;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import htmlGeneration.AuthorHomepage;
import htmlGeneration.ConferenceHomepage;
import htmlGeneration.DomainHomepage;
import htmlGeneration.JournalHomepage;
import htmlGeneration.Homepage;
import htmlGeneration.KeywordHomepage;
import htmlGeneration.KeywordSearchHomepage;
import htmlGeneration.OrganizationHomepage;
import htmlGeneration.PaperHomepage;
import htmlGeneration.ViewList;

public class NavigationSystem 
{
	public Statement statement; 
	
	public NavigationSystem() throws SQLException, ClassNotFoundException
	{
		String driver = "com.mysql.jdbc.Driver"; 
		String db_url = "jdbc:mysql://127.0.0.1:3306/";
		String user = "root";
		String password = "";
		Class.forName(driver);
		Connection conn = DriverManager.getConnection(db_url, user, password);		
		this.statement = conn.createStatement();
		statement.execute("use dblp_plus; "); 
	}
	
	public static void main(String [] args) throws ClassNotFoundException, SQLException
	{
		String url = "http://localhost:8080/csasearch/mainBlock.jsp?type=author&par=14124"; 
//		url = "http://localhost:8080/csasearch/mainBlock.jsp?type=publication&par=2670755"; 
		url = "http://localhost:8080/csasearch/mainBlock.jsp?type=conference&par=1853"; 
//		url = "http://localhost:8080/csasearch/mainBlock.jsp?type=organization&par=405"; 
//		url = "http://35.2.81.4:8080/csasearch/mainBlock.jsp?type=domain&par=18"; 
		url = "http://35.2.81.4:8080/csasearch/mainBlock.jsp?type=viewlist&target=journals&page=1&did=14"; 
		url = "http://35.2.81.4:8080/csasearch/mainBlock.jsp?type=keyword&par=9084"; 
		url = "http://35.2.81.4:8080/csasearch/nalir.jsp"; 
		NavigationSystem system = new NavigationSystem(); 
				
		System.out.println(system.page(url)); 
	}
	
	@SuppressWarnings("unused")
	public String page(String url) throws SQLException
	{
		String html = ""; 

		String type = ""; 
		int argument1 = -1; 
		int argument2 = -1; 
		int argument3 = -1; 
		
		if(url.contains("csasearch/mainBlock.jsp"))
		{
			if(url.split(".jsp").length > 1)
			{
				url = url.split(".jsp")[url.split(".jsp").length-1]; 
				if(url.length() > 0)
				{
					url = url.substring(1); 
					
					if(url.startsWith("type="))
					{
						url = url.substring(5); 
					}
					
					try
					{
						String [] pars = url.split("&par="); 
						type = pars[0]; 
						if(pars.length > 1)
						{
							argument1 = Integer.parseInt(pars[1]); 
						}
						if(pars.length > 2)
						{
							argument2 = Integer.parseInt(pars[2]); 
						}
						if(pars.length > 3)
						{
							argument3 = Integer.parseInt(pars[3]); 
						}
					}
					catch(Exception e){}
				}
			}
			
			if(type.equalsIgnoreCase("author") && argument1 > 0)
			{
				html = AuthorHomepage.authorHomepage(statement, argument1); 
			}
			else if(type.equalsIgnoreCase("publication") && argument1 > 0)
			{
				html = PaperHomepage.paperHomepage(statement, argument1); 
			}
			else if(type.equalsIgnoreCase("conference") && argument1 > 0)
			{
				html = ConferenceHomepage.conferenceHomepage(statement, argument1); 
			}
			else if(type.equalsIgnoreCase("journal") && argument1 > 0)
			{
				html = JournalHomepage.journalHomepage(statement, argument1); 
			}
			else if(type.equalsIgnoreCase("organization") && argument1 > 0)
			{
				html = OrganizationHomepage.organizationHomepage(statement, argument1); 
			}
			else if(type.equalsIgnoreCase("domain") && argument1 > 0)
			{
				html = DomainHomepage.domainHomepage(statement, argument1); 
			}
			else if(type.equalsIgnoreCase("keyword") && argument1 > 0)
			{
				html = KeywordHomepage.keywordHomepage(statement, argument1); 
			}
			else if(url.startsWith("viewlist"))
			{
				html = ViewList.viewList(statement, url); 
			}
			else if(url.startsWith("keywordSearch"))
			{
				html = KeywordSearchHomepage.resultHTMLGen(statement, url.substring(14)); 
			}
			else if(url.startsWith("keywordViewMore"))
			{
				try
				{
					type = url.split("&keywords=")[0]; 
					String keywords = url.split("&keywords=")[1]; 
					
					html = KeywordSearchHomepage.viewMore(statement, keywords, type.substring(21)); 
				}
				catch(Exception e) {}
			}
			else
			{
				html = Homepage.homepage(statement); 
			}
		}

		return html; 
	}
}
