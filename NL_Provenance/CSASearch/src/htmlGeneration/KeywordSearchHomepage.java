package htmlGeneration;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import keywordSearch.ItemList;
import keywordSearch.KeywordSearch;

public class KeywordSearchHomepage 
{
	public static String resultHTMLGen(Statement statement, String keywords) throws SQLException
	{
		String html = ""; 
		ArrayList<ItemList> listList = KeywordSearch.search(keywords, statement); 
		
		for(int i = 0; i < listList.size(); i++)
		{
			if(i > 0)
			{
				html += "<hr>"; 
			}
			
			html += resultBlockGen(keywords, statement, listList.get(i), 5); 
		}
		
		return html; 
	}
	
	public static String viewMore(Statement statement, String keywords, String type) throws SQLException
	{
		String html = ""; 
		ArrayList<ItemList> listList = KeywordSearch.search(keywords, statement); 
		
		for(int i = 0; i < listList.size(); i++)
		{
			if(listList.get(i).type.equals(type))
			{
				html += resultBlockGen(keywords, statement, listList.get(i), 100); 
			}
		}
		
		return html; 
	}
	
	public static String resultBlockGen(String keywords, Statement statement, ItemList itemList, int size) throws SQLException
	{
		String html = ""; 
		String viewMore = ""; 
		if(itemList.items.size() > size && size < 100)
		{
			viewMore = "(" + URLGen.keywordViewMoreURL("view more", "type=" + itemList.type, "keywords=" + keywords) + ")"; 
		}
		
		html += "<div style=\"font-size:20px\"><b>Mapped " + itemList.type + "s: </b>" + viewMore + "</div>"; 
		for(int i = 0; i < size && i < itemList.items.size(); i++)
		{
			if(itemList.type.equals("author"))
			{
				html += "<p>" + (i+1) + ". " + AuthorHomepage.authorShort(statement, itemList.items.get(i).id) + "</p>"; 
			}
			else if(itemList.type.equals("publication"))
			{
				html += PaperHomepage.paperBrief(statement, itemList.items.get(i).id, (i+1)); 
			}
			else if(itemList.type.equals("organization"))
			{
				html += "<p>" + (i+1) + ". " + OrganizationHomepage.organizationShort(statement, itemList.items.get(i).id)  + "</p>"; 
			}
			else if(itemList.type.equals("conference"))
			{
				html += "<p>" + (i+1) + ". " + ConferenceHomepage.conferenceShort(statement, itemList.items.get(i).id) + "</p>"; 
			}
			else if(itemList.type.equals("journal"))
			{
				html += "<p>" + (i+1) + ". " + JournalHomepage.journalShort(statement, itemList.items.get(i).id) + "</p>"; 
			}
			else if(itemList.type.equals("keyword"))
			{
				html += "<p>" + (i+1) + ". " + KeywordHomepage.keywordShort(statement, itemList.items.get(i).id) + "</p>"; 
			}
			else if(itemList.type.equals("domain"))
			{
				html += "<p>" + (i+1) + ". " + DomainHomepage.domainShort(statement, itemList.items.get(i).id) + "</p>"; 
			}
		}
		
		return html; 
	}
}
