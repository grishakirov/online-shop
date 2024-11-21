package cz.cvut.fit.tjv.online_store.service.mapper;

import cz.cvut.fit.tjv.online_store.controller.dto.BonusCardDto;
import cz.cvut.fit.tjv.online_store.domain.BonusCard;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BonusCardMapper {

    private final ModelMapper modelMapper;

    public BonusCardMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public BonusCardDto convertToDto(BonusCard bonusCard) {
        return modelMapper.map(bonusCard, BonusCardDto.class);
    }

    public BonusCard convertToEntity(BonusCardDto bonusCardDto) {
        return modelMapper.map(bonusCardDto, BonusCard.class);
    }

    public List<BonusCardDto> converManyToDto(List<BonusCard> bonusCards) {
        return bonusCards.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}
