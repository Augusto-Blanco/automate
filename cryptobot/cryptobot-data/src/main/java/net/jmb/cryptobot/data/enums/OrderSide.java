package net.jmb.cryptobot.data.enums;

public enum OrderSide {
	BUY("Achat"), SELL("Vente");
	
	private String libelle;
	
	private OrderSide() {
		this(null);
	}
	
	private OrderSide(String libelle) {
		this.libelle = libelle;
	}
	
	public static OrderSide get(String val) {
		try {
			return valueOf(val);
		} catch (Exception e) {
			return null;
		}
	}

	public String getLibelle() {
		return libelle != null ? libelle : name();
	}


}
