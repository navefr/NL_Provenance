package Basics;

public class Triplet 
{
	int idx;
	
	String programCategory;
	
	String tableName;
	
	
	
	public Triplet (int i, String iprogramCategory, String itableName)
	{
		this.idx = i;
		this.programCategory = iprogramCategory;
		this.tableName = itableName;
	}



	public int getIdx() {
		return idx;
	}



	public void setIdx(int idx) {
		this.idx = idx;
	}



	public String getProgramCategory() {
		return programCategory;
	}



	public void setProgramCategory(String programCategory) {
		this.programCategory = programCategory;
	}



	public String getTableName() {
		return tableName;
	}



	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	
	public String toString ()
	{
		return "(" + this.tableName + " ," + this.programCategory + " ," + this.idx + ")";
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + idx;
		result = prime * result
				+ ((programCategory == null) ? 0 : programCategory.hashCode());
		result = prime * result
				+ ((tableName == null) ? 0 : tableName.hashCode());
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
		Triplet other = (Triplet) obj;
		if (idx != other.idx)
			return false;
		if (programCategory == null) {
			if (other.programCategory != null)
				return false;
		} else if (!programCategory.equals(other.programCategory))
			return false;
		if (tableName == null) {
			if (other.tableName != null)
				return false;
		} else if (!tableName.equals(other.tableName))
			return false;
		return true;
	}
	
	
	
	
}
