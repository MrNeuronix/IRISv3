package ru.iris.commons.database.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "config")
@Getter
@Setter
@NoArgsConstructor
public class Config {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String param;
    private String value;

    public Config(String param, String value) {
        this.param = param;
        this.value = value;
    }
}