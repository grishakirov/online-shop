package cz.cvut.fit.tjv.online_store.repository;

import cz.cvut.fit.tjv.online_store.domain.BonusCard;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface BonusCardRepository extends CrudRepository<BonusCard, Long> {
    Optional<BonusCard> findByCardNumber(String cardNumber);
}