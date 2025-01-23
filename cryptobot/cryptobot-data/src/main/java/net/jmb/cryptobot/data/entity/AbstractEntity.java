package net.jmb.cryptobot.data.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.TableGenerator;


@TableGenerator(
	name = AbstractEntity.GENERATOR,
	allocationSize = AbstractEntity.ALLOCATION_SIZE
)
@MappedSuperclass
@JsonInclude(Include.NON_NULL)
public abstract class AbstractEntity {
	
	public static final String GENERATOR = "key_sequence";	
	public static final int ALLOCATION_SIZE = 1;
	
//	@Version
//	private Long version;
	
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = GENERATOR)
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public AbstractEntity id(Long id) {
		this.id = id;
		return this;
	}
	
	public boolean isSameAs(AbstractEntity entity) {
		return entity != null 
				&& this.getClass().isAssignableFrom(entity.getClass())
				&& this.getId() != null
				&& this.getId().equals(entity.getId());
	}

//	public Long getVersion() {
//		return version;
//	}
//
//	public void setVersion(Long version) {
//		this.version = version;
//	}

}