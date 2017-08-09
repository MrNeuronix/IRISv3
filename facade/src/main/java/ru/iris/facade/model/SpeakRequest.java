package ru.iris.facade.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpeakRequest {
    private String text;
    private String zone;
}
