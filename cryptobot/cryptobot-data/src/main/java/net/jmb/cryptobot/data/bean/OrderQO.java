package net.jmb.cryptobot.data.bean;

import java.util.Date;
import java.util.Objects;

public class OrderQO extends CommonQO {

	private String batchEnAno;
	private Boolean anoNiv1;
	private String tradeRef;
	private String siret;
	
	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			OrderQO other = (OrderQO) obj;
			return Objects.equals(batchEnAno, other.batchEnAno)
				&& Objects.equals(anoNiv1, other.anoNiv1)
				&& Objects.equals(tradeRef, other.tradeRef)
				&& Objects.equals(siret, other.siret)
			;
		}
		return false;
	}
	
	public boolean isEmpty() {
		return equals(new OrderQO());
	}

	public OrderQO batchEnAno(String batchEnAno) {
		this.batchEnAno = batchEnAno;
		return this;
	}

	public String getBatchEnAno() {
		return batchEnAno;
	}

	public void setBatchEnAno(String batchEnAno) {
		this.batchEnAno = batchEnAno;
	}

	@Override
	public OrderQO dateDebut(Date dateDebut) {
		return (OrderQO) super.dateDebut(dateDebut);
	}

	@Override
	public OrderQO dateFin(Date dateFin) {
		return (OrderQO) super.dateFin(dateFin);
	}

	@Override
	public OrderQO avecAno(Boolean avecAno) {
		return (OrderQO) super.avecAno(avecAno);
	}

	public Boolean isAnoNiv1() {
		return anoNiv1 != null && anoNiv1;
	}
	
	public Boolean getAnoNiv1() {
		return anoNiv1 != null && anoNiv1;
	}

	public void setAnoNiv1(Boolean anoNiv1) {
		this.anoNiv1 = anoNiv1;
	}
	
	public OrderQO anoNiv1(Boolean anoNiv1) {
		this.anoNiv1 = anoNiv1;
		return this;
	}

	public String getTradeRef() {
		return tradeRef;
	}

	public void setTradeRef(String tradeRef) {
		this.tradeRef = tradeRef;
	}

	public String getSiret() {
		return siret;
	}

	public void setSiret(String siret) {
		this.siret = siret;
	}


	public OrderQO tradeRef(String tradeRef) {
		this.tradeRef = tradeRef;
		return this;
	}

	public OrderQO siret(String siret) {
		this.siret = siret;
		return this;
	}

}
