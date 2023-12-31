package tn.workbot.coco_marketplace.repositories;

import org.apache.logging.log4j.message.StringFormattedMessage;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.workbot.coco_marketplace.entities.Pickup;
import tn.workbot.coco_marketplace.entities.Request;
import tn.workbot.coco_marketplace.entities.User;

import java.util.List;

@Repository
public interface RequestRepository extends CrudRepository<Request,Long> {
    @Query("select count(r) from Request r,AgencyDeliveryMan adm where r.agencyDeliveryMan.id=:value and r.pickup.id=:v2")
    public int countrequestwithsomedeliverymen(@Param("value")Long deliveryManId,@Param("v2")Long pickupId);
    @Query("select count(r) from Request r where r.requestStatus='APPROVED' and r.pickup.id=:v2")
    public int  verifier(@Param("v2")Long pickupId);
    @Query("select r from Request r where r.requestStatus='PENDING' and r.pickup.id=:v2")
    public List<Request>  verifier2(@Param("v2")Long pickupId);
    @Query("select r from Request r,AgencyDeliveryMan ab,AgencyBranch aa where r.agencyDeliveryMan.id=:v1  ")
    public List<Request> ByDeliveryMen(@Param("v1")Long id);
    @Query("select count(r) from Request r,AgencyDeliveryMan ab,AgencyBranch aa where r.agencyDeliveryMan.agencyBranch.id=:v1 and r.requestStatus='APPROVED'")
    public int countApproved(@Param("v1")Long id);
    @Query("select count(r) from Request r,AgencyDeliveryMan ab,AgencyBranch aa where r.agencyDeliveryMan.id=:v1 and r.requestStatus='APPROVED'")
    public int countApprovedDeliveryAgence(@Param("v1")Long id);
    @Query("select r from Request r,Pickup p,Store s,User u  where r.pickup.id=p.id and p.store.id=s.id and s.seller.id=u.id and u.id=:v6")
    public List<Request> retrieveRequestBystore(@Param("v6")Long id);

    @Query("select r from Request r,Pickup p where r.pickup.id=p.id and p.id=:v7")
    public List<Request> retrieveRequestByPickup(@Param("v7")Long id);

    @Query("select distinct r from Request r where r.deliveryman.id=:v1")
    public List<Request> RetrieveRequestByFreelancer(@Param("v1") Long IdFreelancer);
    @Query("select distinct r from Request r where r.Agency.id=:v1")
    public List<Request> RetrieveRequestByAgency(@Param("v1") Long idAgency);
    @Query("select p from Pickup p,Request r where  r.pickup.id = p.id and r.id=:v1")
    public Pickup retrievePickupbyRequestId(@Param("v1") Long idRequest);

    @Query("select u from User u,Request r,Pickup p ,Store  s where u.id=r.deliveryman.id and r.pickup.id=p.id and p.store.seller.id=:v1 and r.id=:v2")
    public User retrieveFreelancerDeliveryByRequestAndStore(@Param("v1") long idS, @Param("v2") long idRequest );

    @Query("select count (distinct r) from Request  r,User u where r.Agency.id=:v1 and r.requestStatus='PENDING'")
    public int countRequestTotalOfAgency(@Param("v1") Long idAgency);
    @Query("select count (distinct r) from Request  r,User u where r.Agency.id=:v1 and r.requestStatus='APPROVED'")
    public int countRequestApprovedForAgency(@Param("v1") Long idAgency);
    @Query("select count (distinct r) from Request  r,User u where r.Agency.id=:v1 and r.requestStatus='REJECTED'")
    public int countRequestRejectForAgency(@Param("v1") Long idAgency);

    @Query("SELECT DISTINCT r FROM Request r ,Pickup p WHERE r.deliveryman.id = :v1 AND r.requestStatus = 'APPROVED' AND (r.pickup.statusPickupSeller = 'TAKED' OR r.pickup.statusPickupSeller = 'ONTHEWAY' OR r.pickup.statusPickupSeller = 'RETURN' OR r.pickup.statusPickupSeller = 'ASSIGNED')")
    public List<Request> RetrieveRequestApprovedByFreelancer(@Param("v1") Long IdFreelancer);
    @Query("SELECT DISTINCT r FROM Request r ,Pickup p WHERE r.Agency.id = :v1 AND r.requestStatus = 'APPROVED' AND (r.pickup.statusPickupSeller = 'TAKED' OR r.pickup.statusPickupSeller = 'ONTHEWAY' OR r.pickup.statusPickupSeller = 'RETURN' OR r.pickup.statusPickupSeller = 'ASSIGNED')")
    public List<Request> RetrieveRequestApprovedByAgency(@Param("v1") Long IdAgency);

    @Query("select distinct r from Request r ,Pickup p ,Store s,User u where r.pickup.id=p.id and r.pickup.store.seller.id=:v1 and r.requestStatus='PENDING' order by r.RequestDate desc ")
    public List<Request> getRequestByorderDesc(@Param("v1") Long idSeller);


    @Query("select distinct r from Request r,User u,Pickup p where r.pickup.id=p.id and r.deliveryman.id=:v1 and p.statusPickupSeller='ASSIGNED' order by r.RequestDate desc ")
    public List<Request> getRequestFreelancerAssignedLast5(@Param("v1") Long idUser);
   @Query("select distinct r from Request r,User u where r.deliveryman.id=:v1")
    public List<Request> getTheRequestOfFreelancer(@Param("v1") Long idFreelancer);
}
