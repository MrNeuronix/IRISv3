package ru.iris.facade.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.iris.commons.helpers.SpeakHelper;
import ru.iris.models.web.SpeakRequest;
import ru.iris.models.status.ErrorStatus;
import ru.iris.models.status.OkStatus;

@RestController
@Profile("facade")
@Slf4j
public class SpeakFacade {

    private final SpeakHelper helper;

    @Autowired
    public SpeakFacade(
            SpeakHelper helper
    ) {
        this.helper = helper;
    }

    /**
     * Say something on specified zone (or at all zones)
     *
     * @param request request
     * @return ok or error status
     */
    @RequestMapping(value = "/api/speak", method = RequestMethod.POST)
    public Object sayAtZone(@RequestBody SpeakRequest request) {

        if (request.getText() != null && !request.getText().isEmpty()) {
            helper.say(request.getText());
        } else {
            return new ErrorStatus("empty text passed");
        }

        return new OkStatus("Saying: " + request.getText());
    }
}
