package cz.cvut.fit.tjv.online_store.service;

import cz.cvut.fit.tjv.online_store.controller.dto.BonusCardDto;
import cz.cvut.fit.tjv.online_store.domain.BonusCard;
import cz.cvut.fit.tjv.online_store.domain.User;
import cz.cvut.fit.tjv.online_store.repository.BonusCardRepository;
import cz.cvut.fit.tjv.online_store.repository.UserRepository;
import cz.cvut.fit.tjv.online_store.service.mapper.BonusCardMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BonusCardService {

    private final BonusCardRepository bonusCardRepository;
    private final UserRepository userRepository;
    private final BonusCardMapper bonusCardMapper;

    public BonusCardService(BonusCardRepository bonusCardRepository, UserRepository userRepository, BonusCardMapper bonusCardMapper) {
        this.bonusCardRepository = bonusCardRepository;
        this.userRepository = userRepository;
        this.bonusCardMapper = bonusCardMapper;
    }

    public BonusCardDto save(BonusCardDto bonusCardDto) {
        User user = userRepository.findById(bonusCardDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        BonusCard bonusCard = bonusCardMapper.convertToEntity(bonusCardDto);
        bonusCard.setUser(user);
        BonusCard savedCard = bonusCardRepository.save(bonusCard);
        return bonusCardMapper.convertToDto(savedCard);
    }

    public List<BonusCardDto> findAll() {
        List<BonusCard> bonusCards = (List<BonusCard>) bonusCardRepository.findAll();
        return bonusCardMapper.converManyToDto(bonusCards);
    }

    public BonusCardDto findById(Long id) {
        BonusCard bonusCard = bonusCardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bonus card not found"));
        return bonusCardMapper.convertToDto(bonusCard);
    }

    public void delete(Long id) {
        if (!bonusCardRepository.existsById(id)) {
            throw new IllegalArgumentException("Bonus card not found");
        }
        bonusCardRepository.deleteById(id);
    }

    public BonusCardDto findByCardNumber(String cardNumber) {
        BonusCard bonusCard = bonusCardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new IllegalArgumentException("Bonus card not found"));
        return bonusCardMapper.convertToDto(bonusCard);
    }

    public BonusCardDto addBalance(Long cardId, Double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        BonusCard bonusCard = bonusCardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Bonus card not found"));

        bonusCard.setBalance(bonusCard.getBalance() + amount);
        BonusCard updatedCard = bonusCardRepository.save(bonusCard);
        return bonusCardMapper.convertToDto(updatedCard);
    }

    public BonusCardDto deductBalance(Long cardId, Double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        BonusCard bonusCard = bonusCardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Bonus card not found"));

        if (bonusCard.getBalance() < amount) {
            throw new IllegalArgumentException("Insufficient balance on the bonus card");
        }

        bonusCard.setBalance(bonusCard.getBalance() - amount);
        BonusCard updatedCard = bonusCardRepository.save(bonusCard);
        return bonusCardMapper.convertToDto(updatedCard);
    }
}