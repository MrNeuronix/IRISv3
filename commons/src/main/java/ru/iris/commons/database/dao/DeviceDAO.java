package ru.iris.commons.database.dao;

import org.springframework.data.repository.CrudRepository;
import ru.iris.commons.database.model.Device;

import javax.transaction.Transactional;

@Transactional
public interface DeviceDAO extends CrudRepository<Device, Long> {

}