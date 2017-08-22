package ru.iris.facade.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.bus.EventBus;
import ru.iris.commons.database.model.DeviceValueChange;
import ru.iris.commons.protocol.enums.SourceProtocol;
import ru.iris.commons.registry.DeviceRegistry;
import ru.iris.facade.model.HistoryRequest;
import ru.iris.facade.model.dto.DeviceValueChangeDTO;
import ru.iris.commons.model.status.ErrorStatus;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@Profile("facade")
@Slf4j
public class ValuesHistoryFacade {

    @Autowired
    private DeviceRegistry registry;

    @Autowired
    private EventBus r;

    /**
     * Return history of device value
     *
     * @param request request
     * @return list of changes
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/api/history", method = RequestMethod.POST)
    public List<Object> getHistory(@RequestBody HistoryRequest request) {

        if (request.getLabel() == null || request.getLabel().isEmpty())
            return Collections.singletonList(new ErrorStatus("label field is empty or null"));

        if (request.getChannel() == null)
            return Collections.singletonList(new ErrorStatus("channel is null"));

        if (request.getSource() == null || request.getSource() == null)
            return Collections.singletonList(new ErrorStatus("source field is empty or null"));

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        Date startDate;
        Date stopDate = null;
        try {
            if (request.getStartDate() != null && !request.getStartDate().isEmpty())
                startDate = format.parse(request.getStartDate());
            else
                return Collections.singletonList(new ErrorStatus("No start date specified"));
            if (request.getEndDate() != null && !request.getEndDate().isEmpty())
                stopDate = format.parse(request.getEndDate());
        } catch (ParseException e) {
            return Collections.singletonList(new ErrorStatus("Date parse error. Use date in format: yyyy-MM-dd HH:mm:ss"));
        }

        List<DeviceValueChange> dbList = registry.getHistory(request.getSource(), request.getChannel(), request.getLabel(),
                startDate, stopDate);

        List<Object> ret = new ArrayList<>();
        dbList.forEach(element -> {
            DeviceValueChangeDTO dto = new DeviceValueChangeDTO();
            dto.setId(element.getId());
            dto.setDate(element.getDate());
            dto.setValue(element.getValue());
            dto.setAdditionalData(element.getAdditionalData());

            ret.add(dto);
        });

        return ret;
    }

}
