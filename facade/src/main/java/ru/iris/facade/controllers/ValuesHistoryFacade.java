package ru.iris.facade.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import reactor.bus.EventBus;
import ru.iris.commons.database.model.DeviceValueChange;
import ru.iris.commons.protocol.enums.SourceProtocol;
import ru.iris.commons.registry.DeviceRegistry;
import ru.iris.facade.model.HistoryRequest;
import ru.iris.facade.model.dto.DeviceValueChangeDTO;
import ru.iris.facade.model.status.ErrorStatus;

@RestController
@Profile("facade")
public class ValuesHistoryFacade {

	private final DeviceRegistry registry;
	private EventBus r;

	private static final Logger logger = LoggerFactory.getLogger(ValuesHistoryFacade.class);

	@Autowired(required = false)
	public ValuesHistoryFacade(
			DeviceRegistry registry
	)
	{
		this.registry = registry;
	}

	@Autowired
	public void setR(EventBus r) {
		this.r = r;
	}

	/**
	 * Return history of device value
	 *
	 * @param request request
	 * @return list of changes
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/api/history", method = RequestMethod.POST)
	public List<Object> getHistory(@RequestBody HistoryRequest request) {

		if(request.getLabel() == null || request.getLabel().isEmpty())
			return Collections.singletonList(new ErrorStatus("label field is empty or null"));

		if(request.getChannel() == null || request.getChannel() <= 0)
			return Collections.singletonList(new ErrorStatus("channel is null or <= 0"));

		SourceProtocol sourceProtocol;
		switch (request.getSource()) {
			case "zwave":
				sourceProtocol = SourceProtocol.ZWAVE;
				break;
			case "noolite":
				sourceProtocol = SourceProtocol.NOOLITE;
				break;
			default:
				return Collections.singletonList(new ErrorStatus("protocol unknown"));
		}

		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
		Date startDate;
		Date stopDate = null;
		try {
			if(request.getStartDate() != null && !request.getStartDate().isEmpty())
				startDate = format.parse(request.getStartDate());
			else
				return Collections.singletonList(new ErrorStatus("No start date specified"));
			if(request.getEndDate() != null && !request.getEndDate().isEmpty())
				stopDate = format.parse(request.getEndDate());
		} catch (ParseException e) {
			return Collections.singletonList(new ErrorStatus("Date parse error"));
		}

		List<DeviceValueChange> dbList = registry.getHistory(sourceProtocol, request.getChannel(), request.getLabel(),
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
