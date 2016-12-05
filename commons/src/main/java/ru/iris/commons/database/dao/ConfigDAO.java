package ru.iris.commons.database.dao;

import javax.transaction.Transactional;

import org.springframework.data.repository.CrudRepository;
import ru.iris.commons.database.model.Config;

@Transactional
public interface ConfigDAO extends CrudRepository<Config, Long> {
	Config findByParam(String param);
}