package ru.iris.models.database;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OrderBy;
import org.hibernate.annotations.UpdateTimestamp;
import ru.iris.models.protocol.enums.ValueType;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Entity
@Table(name = "device_values")
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
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
    @JsonBackReference
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
    @JsonIgnore
    private List<DeviceValueChange> changes = new CopyOnWriteArrayList<>();
}