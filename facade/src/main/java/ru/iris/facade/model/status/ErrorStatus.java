package ru.iris.facade.model.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ErrorStatus {
    private String text;

    public String getStatus() {
        return "ERROR";
    }
}
