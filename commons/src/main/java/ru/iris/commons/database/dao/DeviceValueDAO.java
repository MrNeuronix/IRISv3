package ru.iris.commons.database.dao;

import org.springframework.data.repository.CrudRepository;

import ru.iris.commons.database.model.DeviceValue;

public interface DeviceValueDAO extends CrudRepository<DeviceValue, Long> {
}