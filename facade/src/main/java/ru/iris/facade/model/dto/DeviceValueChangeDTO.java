package ru.iris.facade.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class DeviceValueChangeDTO {
    private long id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date date;
    private String value;
    private String additionalData;
}
