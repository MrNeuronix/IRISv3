package ru.iris.models.web;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpeakRequest {
    private String text;
    private String zone;
}
