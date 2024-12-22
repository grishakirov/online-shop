package cz.cvut.fit.tjv.online_store.service.mapper;
import cz.cvut.fit.tjv.online_store.domain.User;
import cz.cvut.fit.tjv.online_store.controller.dto.UserDto;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class UserMapper implements CustomMapper<User, UserDto> {
    private final ModelMapper modelMapper;

    public UserMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public UserDto convertToDto(User user) {
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public User convertToEntity(UserDto userDto) {
        return modelMapper.map(userDto, User.class);
    }

    @Override
    public List<UserDto> convertManyToDto(List<User> users) {
        return users.stream()
                .map(this::convertToDto)
                .toList();
    }
}
