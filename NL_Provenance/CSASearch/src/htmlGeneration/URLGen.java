package htmlGeneration;

public class URLGen 
{
	private static String authorHompagePrefix = "http://localhost:8080/csasearch/mainBlock.jsp?type=author&par="; 
	private static String paperHomepagePrefix = "http://localhost:8080/csasearch/mainBlock.jsp?type=publication&par="; 
	private static String conferenceHompagePrefix = "http://localhost:8080/csasearch/mainBlock.jsp?type=conference&par="; 
	private static String journalHomepagePrefix = "http://localhost:8080/csasearch/mainBlock.jsp?type=journal&par="; 
	private static String organizationHomepagePrefix = "http://localhost:8080/csasearch/mainBlock.jsp?type=organization&par="; 
	private static String domainHompagePrefix = "http://localhost:8080/csasearch/mainBlock.jsp?type=domain&par="; 
	private static String keywordHompagePrefix = "http://localhost:8080/csasearch/mainBlock.jsp?type=keyword&par="; 
	private static String viewListHompagePrefix = "http://localhost:8080/csasearch/mainBlock.jsp?type=viewlist";
	private static String keywordViewMorePrefix = "http://localhost:8080/csasearch/mainBlock.jsp?type=keywordViewMore";
	
	// add a link; 
	public static String addLinkage(String entityType, int id, String name)
	{
		String linkage = "<a href=\"" + URLGen.getURL(entityType, id) + "\">" + name + "</a>"; 
		return linkage; 
	}
	
	// find the url of an entity; 
	public static String getURL(String type, int id)
	{
		if(type.equals("author") || type.equals("coauthor") || type.equals("conferenceAuthor") || type.equals("journalAuthor") || type.equals("keywordAuthor"))
		{
			return URLGen.authorHompagePrefix + id; 
		}
		else if(type.equals("publication"))
		{
			return URLGen.paperHomepagePrefix + id; 
		}
		else if(type.equals("conference") || type.equals("authorConference"))
		{
			return URLGen.conferenceHompagePrefix + id; 
		}
		else if(type.equals("journal") || type.equals("authorJournal"))
		{
			return URLGen.journalHomepagePrefix + id; 
		}
		else if(type.equals("organization") || type.equals("conferenceOrganization") || type.equals("journalOrganization"))
		{
			return URLGen.organizationHomepagePrefix + id; 
		}
		else if(type.equals("domain"))
		{
			return URLGen.domainHompagePrefix + id; 
		}
		else if(type.equals("keyword") || type.equals("authorKeyword") || type.equals("conferenceKeyword") || type.equals("journalKeyword"))
		{
			return URLGen.keywordHompagePrefix + id; 
		}
		else 
		{
			return ""; 
		}
	}

	public static String keywordViewMoreURL(String content, String type, String keywords)
	{
		String linkage = "<a href=\"" + keywordViewMorePrefix + "&" + type + "&" + keywords + "\" style=\"font-size:18px\">" + content + "</a>"; 
		return linkage; 
	}
	
	public static String viewListURL(String content, String targetEntity, String page, String filter1)
	{
		String linkage = "<a href=\"" + viewListHompagePrefix + "&" + targetEntity + "&" + page + "&" + filter1 + "\">" + content + "</a>"; 
		return linkage; 
	}

	public static String viewListURL(String content, String type, int id1, int id2, String page)
	{
		String targetEntity = "type=papers"; 
		String filter1 = ""; 
		String filter2 = ""; 
		page = "page=" + page; 
		if(type.equals("authorConference"))
		{
			filter1 = "aid=" + id1; 
			filter2 = "cid=" + id2; 
		}
		else if(type.equals("authorJournal"))
		{
			filter1 = "aid=" + id1; 
			filter2 = "jid=" + id2; 
		}
		else if(type.equals("authorKeyword"))
		{
			filter1 = "aid=" + id1; 
			filter2 = "kid=" + id2; 
		}
		else if(type.equals("coauthor"))
		{
			filter1 = "aid=" + id1; 
			filter2 = "aid=" + id2; 
		}
		else if(type.equals("conferenceAuthor"))
		{
			filter1 = "cid=" + id1; 
			filter2 = "aid=" + id2; 
		}
		else if(type.equals("conferenceOrganization"))
		{
			filter1 = "oid=" + id2; 
			filter2 = "cid=" + id1; 
		}
		else if(type.equals("conferenceKeyword"))
		{
			filter1 = "cid=" + id1; 
			filter2 = "kid=" + id2; 
		}
		else if(type.equals("journalAuthor"))
		{
			filter1 = "jid=" + id1; 
			filter2 = "aid=" + id2; 
		}
		else if(type.equals("journalOrganization"))
		{
			filter1 = "oid=" + id2; 
			filter2 = "jid=" + id1; 
		}
		else if(type.equals("journalKeyword"))
		{
			filter1 = "jid=" + id1; 
			filter2 = "kid=" + id2; 
		}
		else if(type.equals("keywordAuthor"))
		{
			filter1 = "kid=" + id1; 
			filter2 = "aid=" + id2; 
		}
		
		String linkage = "<a href=\"" + viewListHompagePrefix + "&" + targetEntity + "&" + page + "&" + filter1 + "&" + filter2 + "\">" + content + "</a>"; 
		return linkage; 
	}
}
