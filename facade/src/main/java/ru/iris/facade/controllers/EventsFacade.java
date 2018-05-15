package ru.iris.facade.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.bus.Event;
import reactor.bus.EventBus;
import ru.iris.models.bus.event.CommandEvent;
import ru.iris.models.status.OkStatus;
import ru.iris.models.web.CommandRequest;

@RestController
@Profile("facade")
@Slf4j
public class EventsFacade {

    @Autowired
    private EventBus r;

    @RequestMapping(value = "/api/command", method = RequestMethod.POST)
    public Object sayAtZone(@RequestBody CommandRequest request) {
        r.notify("command.run", Event.wrap(
                new CommandEvent(request.getCommand())
        ));

        return new OkStatus("Sent");
    }
}
