package ru.iris.commons.database.model;

import org.hibernate.annotations.CreationTimestamp;
import ru.iris.commons.protocol.enums.SourceProtocol;
import ru.iris.commons.protocol.enums.Type;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "v2_zones")
public class Zone {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date date;

	private String name;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "zone")
	private Set<Device> devices = new HashSet<>();

	public Zone() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}