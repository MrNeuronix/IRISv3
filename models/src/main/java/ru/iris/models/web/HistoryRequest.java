package ru.iris.models.web;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;
import ru.iris.models.protocol.enums.SourceProtocol;

@Getter
@Setter
public class HistoryRequest {
    @NotEmpty
    private SourceProtocol source;
    @NotEmpty
    private String channel;
    private String startDate;
    private String endDate;
    @NotEmpty
    private String label;
}
