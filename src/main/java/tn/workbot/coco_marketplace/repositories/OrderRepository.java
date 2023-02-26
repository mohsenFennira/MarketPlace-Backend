package tn.workbot.coco_marketplace.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.workbot.coco_marketplace.entities.Order;
import tn.workbot.coco_marketplace.entities.User;

import java.util.List;
import java.util.Map;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
  @Query("SELECT concat(o.buyer.FirstName,' & ',o.buyer.LastName,' ',count(o)) from Order o where o.status='ACCEPTED_PAYMENT' group by o.buyer order by count (o) desc ")
  List<String> RankUsersByOrdersAcceptedPayement();

  int findByBuyerId(Long id);

  @Query("SELECT o from Order o where o.buyer.id=:id and o.status='BASKET'")
  Order BasketExistance(@Param("id") Long id);


  @Query("SELECT concat(o.shipping.governorate,' ',COUNT(o)) FROM Order o where o.status='ACCEPTED_PAYMENT' group by o.shipping.governorate order by count(o) desc ")
  List<String> RankGouvernoratByNbOrders();

}

