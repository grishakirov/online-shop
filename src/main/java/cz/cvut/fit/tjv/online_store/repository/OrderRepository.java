package cz.cvut.fit.tjv.online_store.repository;

import cz.cvut.fit.tjv.online_store.domain.Order;
import cz.cvut.fit.tjv.online_store.domain.OrderStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends CrudRepository<Order, Long> {
    @Query("SELECT COUNT(o) > 0 FROM Order o WHERE o.user.id = :userId AND o.status IN :statuses")
    boolean existsByUserIdAndStatusIn(@Param("userId") Long userId, @Param("statuses") List<OrderStatus> statuses);
}
