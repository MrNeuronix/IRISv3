package ru.iris.commons.database.dao;

import org.springframework.data.repository.CrudRepository;

import ru.iris.commons.database.model.Device;
import ru.iris.commons.protocol.enums.SourceProtocol;

import java.util.List;

import javax.transaction.Transactional;

@Transactional
public interface DeviceDAO extends CrudRepository<Device, Long> {
		List<Device> findBySource(SourceProtocol source);
}