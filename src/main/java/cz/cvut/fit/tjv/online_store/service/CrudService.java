package cz.cvut.fit.tjv.online_store.service;


import jakarta.validation.Valid;

public interface CrudService <EntityDto, ID> {
    EntityDto save (@Valid EntityDto entity);
    Iterable<EntityDto> findAll();
    EntityDto findById(ID id);
    EntityDto update(ID id, EntityDto entity);
    void delete(ID id);

}
