package ru.iris.commons.database.dao;

import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;

import ru.iris.commons.database.model.DeviceValue;

@Transactional
public interface DeviceValueDAO extends CrudRepository<DeviceValue, Long> {
}