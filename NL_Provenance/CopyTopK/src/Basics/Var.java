package Basics;


public class Var extends Proton
{
	boolean isBounded = false;
	
	public Var () {}
	
	
	public Var (String iname, String icategory) 
	{
		super(iname, icategory);
	}
	
	
	
	public Var (boolean bound)
	{
		this.isBounded = bound;
	}
	
    
	
	public boolean isBounded() 
    {
		return isBounded;
	}
    
    
    
	public void setBounded(boolean isBounded) 
	{
		this.isBounded = isBounded;
	}
	
	
	/*@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		//result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((category == null) ? 0 : category.hashCode());
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
		if (category == null) {
			if (other.category != null)
				return false;
		} else if (!category.equals(other.category))
			return false;
		return true;
	}*/
}
