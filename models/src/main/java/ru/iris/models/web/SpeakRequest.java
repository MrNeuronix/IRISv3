package ru.iris.models.web;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SpeakRequest {
    private String text;
    private String zone;
}
