package ru.iris.commons.database.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import ru.iris.commons.protocol.enums.DeviceType;
import ru.iris.commons.protocol.enums.SourceProtocol;
import ru.iris.commons.protocol.enums.State;

import javax.persistence.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "devices")
@Getter
@Setter
@NoArgsConstructor
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    private Short channel;

    private String humanReadable;
    private String manufacturer;
    private String productName;

    @Transient
    private State state;

    @Enumerated(EnumType.STRING)
    private SourceProtocol source;

    @Enumerated(EnumType.STRING)
    private DeviceType type;

    @ManyToOne
    private Zone zone;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "device")
    @OrderBy("name ASC")
    @MapKey(name = "name")
    private Map<String, DeviceValue> values = new HashMap<>();

    // State will be get in runtime runtime
}