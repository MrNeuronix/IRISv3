package ru.iris.commons.database.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OrderBy;
import org.hibernate.annotations.UpdateTimestamp;
import ru.iris.commons.protocol.enums.ValueType;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "device_values")
@Getter
@Setter
@NoArgsConstructor
public class DeviceValue {
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    protected Date lastUpdated;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date date;
    @ManyToOne
    private Device device;

    private String name;
    private String units;
    private Boolean readOnly;

    private String currentValue;

    @Enumerated(EnumType.STRING)
    private ValueType type = ValueType.STRING;

    private String additionalData;

    // get only 15 history points by batch
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "deviceValue")
    @OrderBy(clause = "date desc")
    @BatchSize(size = 30)
    private List<DeviceValueChange> changes = new ArrayList<>();
}