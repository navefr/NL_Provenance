package application;


public class TradeGoods 
{	
	private String country;
	private String product;

	
	public TradeGoods(String inCountry, String inProduct) 
	{		
		this.setCountry(inCountry);
		this.setProduct(inProduct);
	}



	public String getCountry() {
		return country;
	}



	public void setCountry(String country) {
		this.country = country;
	}



	public String getProduct() {
		return product;
	}



	public void setProduct(String product) {
		this.product = product;
	}

}
