package ru.iris.commons.database.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "device_values_change")
@Getter
@Setter
@NoArgsConstructor
public class DeviceValueChange {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @ManyToOne
    private DeviceValue deviceValue;

    private String value;
    private String additionalData;
}