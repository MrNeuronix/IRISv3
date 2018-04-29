package ru.iris.models.web;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;
import ru.iris.models.protocol.enums.SourceProtocol;

@Getter
@Setter
@NoArgsConstructor
public class HistoryRequest {
    @NotEmpty
    private SourceProtocol source;
    @NotEmpty
    private String channel;
    private String startDate;
    private String endDate;
    @NotEmpty
    private String label;

    private boolean desc = true;
}
