package net.jmb.cryptobot.data.bean;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

public class CommonQO {

	private DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

	private Long id;
	private Date dateDebut;
	private Date dateFin;
	private Boolean avecAno;
	private Boolean notTraiteParVacation;

	public String getDateDebut() {
		if (dateDebut != null) {
			return df.format(dateDebut);
		}
		return null;
	}

	public void setDateDebut(String dateDebut) {
		if (StringUtils.isNotBlank(dateDebut)) {
			try {
				this.dateDebut = df.parse(dateDebut);
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public String getDateFin() {
		if (dateFin != null) {
			return df.format(dateFin);
		}
		return null;
	}

	public void setDateFin(String dateFin) {
		if (StringUtils.isNotBlank(dateFin)) {
			try {
				this.dateFin = df.parse(dateFin);
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public boolean isAvecAno() {
		return avecAno != null && avecAno;
	}

	public void setAvecAno(boolean avecAno) {
		this.avecAno = avecAno;
	}


	public void setDDateDebut(Date dateDebut) {
		this.dateDebut = dateDebut;
	}

	public void setDDateFin(Date dateFin) {
		this.dateFin = dateFin;
	}

	public Date getDDateDebut() {
		return this.dateDebut;
	}

	public Date getDDateFin() {
		return this.dateFin;
	}

	public CommonQO dateDebut(Date dateDebut) {
		this.dateDebut = dateDebut;
		return this;
	}

	public CommonQO dateFin(Date dateFin) {
		this.dateFin = dateFin;
		return this;
	}

	public CommonQO avecAno(Boolean avecAno) {
		this.avecAno = avecAno;
		return this;
	}
	
	
	public boolean isNotTraiteParVacation() {
		return notTraiteParVacation != null && notTraiteParVacation;
	}

	public void setNotTraiteParVacation(boolean notTraiteParVacation) {
		this.notTraiteParVacation = notTraiteParVacation;
	}
	
	public CommonQO notTraiteParVacation(Boolean notTraiteParVacation) {
		this.notTraiteParVacation = notTraiteParVacation;
		return this;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CommonQO other = (CommonQO) obj;
		return Objects.equals(avecAno, other.avecAno) && Objects.equals(dateDebut, other.dateDebut)
				&& Objects.equals(id, other.id) && Objects.equals(dateFin, other.dateFin)
				&& Objects.equals(notTraiteParVacation, other.notTraiteParVacation);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public CommonQO id(Long id) {
		setId(id);
		return this;
	}


}
