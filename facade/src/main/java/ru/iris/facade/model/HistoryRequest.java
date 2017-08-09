package ru.iris.facade.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

@Getter
@Setter
public class HistoryRequest {
    @NotEmpty
    private String source;
    @NotEmpty
    private Short channel;
    private String startDate;
    private String endDate;
    @NotEmpty
    private String label;
}
