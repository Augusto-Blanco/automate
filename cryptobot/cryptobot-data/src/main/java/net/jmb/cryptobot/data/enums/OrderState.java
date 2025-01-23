package net.jmb.cryptobot.data.enums;

public enum OrderState {
	PENDING("Attente exécution"), COMPLETE("Exécuté"), CANCELLED("Annulé"), PARTIAL("Partiellement exécuté");
	
	private String libelle;
	
	private OrderState() {
		this(null);
	}
	
	private OrderState(String libelle) {
		this.libelle = libelle;
	}
	
	public static OrderState get(String val) {
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
