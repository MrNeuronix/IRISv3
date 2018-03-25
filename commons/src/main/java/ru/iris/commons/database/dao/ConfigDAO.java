package ru.iris.commons.database.dao;

import org.springframework.data.repository.CrudRepository;
import ru.iris.models.database.Config;

import javax.transaction.Transactional;

@Transactional
public interface ConfigDAO extends CrudRepository<Config, Long> {
    Config findByParam(String param);
}