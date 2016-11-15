package ru.iris.facade.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import reactor.bus.EventBus;
import ru.iris.commons.registry.DeviceRegistry;
import ru.iris.facade.model.HistoryRequest;

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
		return registry.getHistory(request.getSource(), request.getChannel(), request.getLabel(), request.getStartDate(),
		                          request.getEndDate());
	}

}
