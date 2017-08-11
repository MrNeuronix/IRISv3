package ru.iris.commons.model.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OkStatus implements BackendAnswer {
    private String text;

    public String getStatus() {
        return "OK";
    }
}
