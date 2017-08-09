package ru.iris.commons.database.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "zones")
@Getter
@Setter
@NoArgsConstructor
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
}