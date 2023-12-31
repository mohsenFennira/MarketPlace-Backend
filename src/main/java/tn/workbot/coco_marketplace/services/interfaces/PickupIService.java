package tn.workbot.coco_marketplace.services.interfaces;

import com.google.maps.errors.ApiException;
import org.springframework.http.ResponseEntity;
import tn.workbot.coco_marketplace.entities.*;
import tn.workbot.coco_marketplace.entities.enmus.StatusPickupSeller;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface PickupIService {
    public Pickup addPickup(Pickup pickup);
    public void removePickup(Long id);
    public Pickup RetrievePickup(Long id);
    public List<Pickup> RetrievePickups();
    public Pickup updatePickup(Pickup pickup,Long idPikup);
    public List<Pickup> RetrievePickupsByGovernoratBetweenPickupAndStoreAndDeliveryAgencyMen(Long id);
    public Pickup AssignPickupByOder(Pickup pickup,Long id);
    public List<Pickup> RetrievePickupsByGovernoratBetweenStoreAndDeliveryMenFreelancer();
    /*public Pickup AssignPickupBySeller(Pickup pickup);*/
    public List<Pickup> RetrievePickupsbetweenAgencyBranchAndStoreInTheSomeGovernorat();
    public Pickup AssignPickupByStoreAndOrder(Pickup pickup,Long id,Long IdSotre);
    public  Pickup ModifyStatusOfPickupByDelivery(String Status,Long idPickup);
    public Duration calculateDeliveryTime(Long idPickup) throws IOException, InterruptedException, ApiException;
    public int test(Long id);
    public Pickup trakingbyseller(String codePickup);
    public Pickup trakingbybuyer(String codePickup);
    public List<Pickup> retrievePickupByDeliveryMenFreelancer();
    public List<Pickup> retrievePickupByAgence();
    public List<Pickup> retrievePickupByBranch(Long idbranch);
    public List<Order> retrieveOrderByseller(Long idStore);
    public List<Pickup> retrievePickupBysellerAttent();
    ///////////stat Seller
    public int countPickupSellerPendingToday();
    public int countPickupSelleronTheWayToday();
    public int countPickupSellerDeliveredToday();
    public int countPickupSellerReturnToday();
    public int countPickupSellerRefundedToday();
    ///////////stat Delivery
    public int countPickupDeliveryManFreelancerPendingToday();
    public int countPickupAgencyToday();
    public int countRequestRejectedDeliveryManFreelancerToday();
    public int countRequestApprovedDeliveryManFreelancerToday();
    public int countRequestRejectedAgencyToday();
    public int countRequestApprovedAgencyToday();

    public Float SumPricePickupDeliveredByFreelancerToday();
    public Float SumPricePickupDeliveredByAgencyToday();
    public Float SumPriceDeliveryPickupisDeliveredByFreelancerToday();
    public Float SumPriceDeliveryPickupisDeliveredByAgencyToday();

    public List<Product>RetrieveProductByPickup(Long idPickup);
    /////////////stat Administrator
    public int countAgencyAdministrator();
    public int countDeliveryFreelancerAdministrator();
    public int countPickupDeliveredTodayAdministrator();
    public int countOfPickupOnTheWayTodayAdministrator();
    public int countOfPickupReturnedTodayAdministrator();
    public int countOfPickupDeliveredweekAdministrator();
    public int countOfPickupOnTheWayweekAdministrator();
    public int countOfPickupReturnedweekAdministrator();
    public Float sumOfPickupDeliveredTodayAdministrator();
    public Float sumOfPickupOnTheWayTodayAdministrator();
    public Float sumOfPickupReturnedTodayAdministrator();
    public Float sumOfPickupDeliveredweekAdministrator();
    public Float sumOfPickupOnTheWayweekAdministrator();
    public Float sumOfPickupReturnedweekAdministrator();

    ///////////Gear Delivery Alers (Kilometre || ESSENCE)
    public Float kilometreTotalConsommerParFreelancerDelivery() throws Exception;
    public String FraisEssenceTotal() throws Exception;

    //////////Envoyer Un sms si vous avez cconsoumer ton limite  CO2  ,
    public double LimiteCo2() throws IOException, InterruptedException, ApiException;
    public User UpdateTheCO2ConsoFreelancer() throws IOException, InterruptedException, ApiException;

    public List<Pickup> RetrievePickupAgencyByRequestWithStatusRequestApproved();
    public List<Pickup> RetrievePickupFreelancerByRequestWithStatusRequestApproved();
    public Set<Store> RetrieveStoreOfUser();
    public Order GetOrderById(Long IdOrder);
    public Shipping getShippingByOrder(Long IdOrder);
    public User getBuyerByOrder(Long IdOrder);
    public Order getOrderByPickupId(Long idPickup);
    public User getBuyerByPickupId(Long idPickup);
    public Shipping getShippingByPickupId(Long idPickup);
    public User getUserNOw();
    public int countOrderBySellerNoPickup(Long idStore);
    public ResponseEntity<Map<Float, List<Product>>> getProduct(Long idOrder,Long idStore) ;
    public List<Product> getListProductOfOrder(Long idOrder,Long idStore);
    public Float  getSumPriceProductOfOrder(Long idOrder,Long idStore);
    public List<ProductQuantity> getAllProductQuantity();
    //stat agency
    public int countPickupDeliveredForAgency();
    public int countPickupReturnedForAgency();
    public int countPickupOnTheWayForAgency();
    public int countPickupRefundedForAgency();
    public int countPickupAssignedForAgency();
    public int countPickupTakedForAgency();
    ///stat freelancer
    public int countPickupDeliveredForfreelancer();
    public int countPickupReturnedForfreelancer();
    public int countPickupOnTheWayForfreelancer();
    public int countPickupRefundedForfreelancer();
    public int countPickupAssignedForFreelancer();
    public int countPickupTakedForFreelancer();
    ////seller
    public int countPickupAssignedSeller();
    public int countPickupTakedSeller();
    public List<Pickup> RetrievePickupInProgress();
    public int countProductQuantityInOrderProduct(Long idOrder,Long idProduct);
    public Store getStoreByPickup(Long idPickup);
    public Double SumOfPricePickupDeliveredToday();
    public Map<StatusPickupSeller,Integer> getNumberOfPickupByStatus();
    public Map<StatusPickupSeller, Integer> getNumberOfPickupByStatusByMonthAndYearAndAll();
    public Double AllCo2User();
    public Map<String,Integer> getNumberPickupsInMonth();
    public Map<String,Integer> getNumberRequestsInMonth();
    public List<Pickup> RetrieveAllPickupsOfUser();
    public List<Pickup> RetrieveAllPickupsOfSeller();
    public User retrieveTheFreelancerOfPickup(Long idPickup);
    }
