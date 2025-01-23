package net.jmb.cryptobot.data.bean;

import java.util.Date;
import java.util.Objects;

public class AssetQO extends CommonQO {

	private Boolean nonTerminee;


	public boolean isNonTerminee() {
		return nonTerminee != null && nonTerminee;
	}

	public void setNonTerminee(boolean nonTerminee) {
		this.nonTerminee = nonTerminee;
	}

	public boolean isEmpty() {
		return equals(new AssetQO());
	}

	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			AssetQO other = (AssetQO) obj;
			return Objects.equals(nonTerminee, other.nonTerminee);
		}
		return false;
	}


	public AssetQO nonTerminee(Boolean nonTerminee) {
		this.nonTerminee = nonTerminee;
		return this;
	}

	@Override
	public AssetQO dateDebut(Date dateDebut) {
		return (AssetQO) super.dateDebut(dateDebut);
	}

	@Override
	public AssetQO dateFin(Date dateFin) {
		return (AssetQO) super.dateFin(dateFin);
	}

	@Override
	public AssetQO avecAno(Boolean avecAno) {
		return (AssetQO) super.avecAno(avecAno);
	}
	
	

}
