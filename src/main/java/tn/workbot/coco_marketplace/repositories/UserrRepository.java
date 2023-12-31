package tn.workbot.coco_marketplace.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.workbot.coco_marketplace.entities.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import tn.workbot.coco_marketplace.entities.enmus.RoleType;

import java.util.List;
import java.util.Map;

@Repository
public interface UserrRepository extends CrudRepository<User,Long> {

    List<User> findUserByRoleType(RoleType roleType);

    User findUserByEmail(String email);
    User findByResetToken(String resetToken);



    @Query("SELECT concat(o.FirstName,' ',o.LastName,' ',count(o)) from User  o where o.role='SELLER' group by o.city order by count (o) desc ")
    List<String> getSellersGroupedByCityName();



 @Query("select u from User u where u.email=:v1 and u.password=:v2")
    public User getUserByEmailAndPassword(@Param("v1")String Email,@Param("v2")String pdw);
    @Query("select u from User u where u.email=:v1 ")
    public User getUserByEmail(@Param("v1")String Email);

    @Query("select u.role.type as role ,count(u) as nb from User u group by u.role")
    List<Map<String,Integer>> statsUsersByRole();

}
