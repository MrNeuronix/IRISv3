package ru.iris.commons.database.dao;

import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;

import ru.iris.commons.database.model.DeviceValueChange;

@Transactional
public interface DeviceValueChangeDAO extends CrudRepository<DeviceValueChange, Long> {
}