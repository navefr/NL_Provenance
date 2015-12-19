package Basics;


public class Proton 
{
	String name;
	
	String category;
	
	public Proton() {}
	
	
	public Proton(String iname, String icategory)
	{
		this.name = iname;
		this.category = icategory;
	}
	
	
	public Proton(Proton other)
	{
		this.name = other.name;
		this.category = other.category;
	}

	

	public String getName() 
	{
		return name;
	}

	

	public void setName(String name)
	{
		this.name = name;
	}
	

	public String getCategory() 
	{
		return category;
	}


	public void setCategory(String category) 
	{
		this.category = category;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		//result = prime * result + ((category == null) ? 0 : category.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Proton other = (Proton) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	
	public String toString ()
	{
		return "(" + this.name + " ," + this.category + ")";
	}
	
}
