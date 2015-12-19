package application;

public class Trade
{	
	private String country1;
	private String country2;

	
	public Trade(String inCountry1, String inCountry2) 
	{		
		this.country1 = inCountry1;
		this.country2 = inCountry2;
	}



	public String getCountry1() {
		return country1;
	}



	public void setCountry1(String country) {
		this.country1 = country;
	}



	public String getCountry2() {
		return country2;
	}



	public void setCountry2(String product) {
		this.country2 = product;
	}

}

