package ru.iris.commons.database.dao;

import org.springframework.data.repository.CrudRepository;
import ru.iris.commons.database.model.Zone;

import javax.transaction.Transactional;

@Transactional
public interface ZoneDAO extends CrudRepository<Zone, Long> {
	Zone findByName(String name);
}