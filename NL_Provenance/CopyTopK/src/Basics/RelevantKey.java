package Basics;

public class RelevantKey extends Atom 
{

	public RelevantKey (Atom atom)
	{
		super(atom);
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isFact ? 1231 : 1237);
		result = prime * result + (isFullyInst ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		for (Proton p : params) 
		{
			result = prime * result + ((p instanceof Constant) ? p.hashCode() : p.category.hashCode());
		}
		
		return result;
	}
	

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		Atom other = (Atom) obj;
		/*if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;*/
		if (!((Atom) this).FittsPartialInst(other))
			return false;
		/*if (params == null) {
			if (other.params != null)
				return false;
		} else if (!params.equals(other.params))
			return false;*/
		return true;
	}
	
	
}
