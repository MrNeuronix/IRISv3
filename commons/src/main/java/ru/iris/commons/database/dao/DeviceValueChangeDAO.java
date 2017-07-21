package ru.iris.commons.database.dao;

import org.springframework.data.repository.CrudRepository;
import ru.iris.commons.database.model.DeviceValueChange;

import javax.transaction.Transactional;

@Transactional
public interface DeviceValueChangeDAO extends CrudRepository<DeviceValueChange, Long> {
}