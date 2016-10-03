package ru.iris.commons.database.dao;

import javax.transaction.Transactional;

import org.springframework.data.repository.CrudRepository;
import ru.iris.commons.database.model.Config;

/**
 * A DAO for the entity User is simply created by extending the CrudRepository
 * interface provided by spring. The following methods are some of the ones
 * available from such interface: save, delete, deleteAll, findOne and findAll.
 * The magic is that such methods must not be implemented, and moreover it is
 * possible create new query methods working only by defining their signature!
 *
 * @author netgloo
 */
@Transactional
public interface ConfigDAO extends CrudRepository<Config, Long> {

	public Config findByParam(String param);

}