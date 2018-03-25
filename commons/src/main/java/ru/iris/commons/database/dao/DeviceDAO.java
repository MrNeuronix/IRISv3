package ru.iris.commons.database.dao;

import org.springframework.data.repository.CrudRepository;
import ru.iris.models.database.Device;
import ru.iris.models.protocol.enums.SourceProtocol;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
public interface DeviceDAO extends CrudRepository<Device, Long> {
    List<Device> findAll();
    List<Device> findBySource(SourceProtocol source);
}