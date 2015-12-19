package keywordSearch;

import java.util.ArrayList;

import functions.SimilarityFunctions;

public class ItemList 
{
	public String type = ""; 
	public ArrayList<Item> items = new ArrayList<Item>(); 
	
	public ItemList(String type)
	{
		this.type = type; 
	}
	
	public void addItem(int id, String keywords, String content, double importance)
	{
		Item item = new Item(id, keywords, content, importance); 
		for(int i = 0; i < items.size(); i++)
		{
			if(items.get(i).id == id)
			{
				if(item.relevance > items.get(i).relevance)
				{
					items.get(i).relevance = item.relevance; 
				}
				item = items.remove(i); 
			}
		}
		
		for(int i = 0; i < items.size(); i++)
		{
			if(item.relevance > items.get(i).relevance)
			{
				items.add(i, item);
				return; 
			}
		}
		items.add(item); 
	}
	
	public class Item
	{
		public int id = 0; 
		public double relevance = 0; 
		
		public Item(int id, String keywords, String content, double importance)
		{
			this.id = id; 
			importance = importance / 21; 
			double similarity = SimilarityFunctions.jaccardQGram(keywords, content, 3); 
			relevance = 4*importance*similarity + importance + similarity; 
		}
	}
}
