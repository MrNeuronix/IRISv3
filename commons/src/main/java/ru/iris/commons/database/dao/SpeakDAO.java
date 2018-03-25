package ru.iris.commons.database.dao;

import org.springframework.data.repository.CrudRepository;
import ru.iris.models.database.Speaks;

import javax.transaction.Transactional;

@Transactional
public interface SpeakDAO extends CrudRepository<Speaks, Long> {
    Speaks findByCache(Integer cache);
}