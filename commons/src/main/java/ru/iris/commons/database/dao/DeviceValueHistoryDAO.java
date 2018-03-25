package ru.iris.commons.database.dao;

import org.springframework.data.repository.CrudRepository;
import ru.iris.models.database.DeviceValueChange;

import javax.transaction.Transactional;

@Transactional
public interface DeviceValueHistoryDAO extends CrudRepository<DeviceValueChange, Long> {
}