package cz.cvut.fit.tjv.online_store.service.mapper;

import java.util.List;

public interface CustomMapper<Entity, Dto> {
    public Dto convertToDto(Entity entity);
    Entity convertToEntity(Dto dto);
    List<Dto> converManyToDto(List<Entity> entities);
}
