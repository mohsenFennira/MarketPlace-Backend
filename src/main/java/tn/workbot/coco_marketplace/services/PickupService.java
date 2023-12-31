package tn.workbot.coco_marketplace.services;

import com.google.maps.DistanceMatrixApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.Distance;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.TravelMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;
import tn.workbot.coco_marketplace.Api.OpenWeatherMapClient;
import tn.workbot.coco_marketplace.Api.PickupTwilio;
import tn.workbot.coco_marketplace.Api.ScraperEssence;
import tn.workbot.coco_marketplace.Dto.auth.NewPassword;
import tn.workbot.coco_marketplace.configuration.SessionService;
import tn.workbot.coco_marketplace.entities.*;
import tn.workbot.coco_marketplace.entities.enmus.*;
import tn.workbot.coco_marketplace.repositories.*;
import tn.workbot.coco_marketplace.services.interfaces.PickupIService;

import javax.transaction.Transactional;
import java.io.IOException;
import java.text.DecimalFormat;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PickupService implements PickupIService {
    @Autowired
    PickupRepository pr;
    @Autowired
    OrderRepository or;
    @Autowired
    StoreRepository sr;
    @Autowired
    UserrRepository ur;
    @Autowired
    AgencyBranchRepository abr;
    @Autowired
    RequestRepository rr;
    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    OpenWeatherMapClient openWeatherMapClient;
    @Autowired
    PickupTwilio pickupTwilio;
    @Autowired
    ScraperEssence se;
    @Autowired
    ShippingRepository shippingRepository;
    @Autowired
    SessionService sessionService;
    @Autowired
    ProductQuantityRepository pq;




    @Override
    public Pickup addPickup(Pickup pickup) {
        Random random = new Random(); //java.util.Random
        pickup.setStatusPickupSeller(StatusPickupSeller.valueOf("PICKED"));
        pickup.setStatusPickupBuyer(StatusPickupBuyer.valueOf("PLACED"));
        int randomNumber = random.nextInt(9000) + 1000;  // generates a random number betweeen 1000 and 9999
        String prefix = "216";
        String code = prefix + randomNumber;
        List<Pickup> pickups = (List<Pickup>) pr.findAll();
        for (Pickup p : pickups) {
            if (p.getCodePickup() != code) {
                pickup.setCodePickup(code);
            } else {
                int randomNumber1 = random.nextInt(100) + 100;
                String code1 = prefix + randomNumber + randomNumber1;
                pickup.setCodePickup(code1);//
            }
        }
        pickup.setCodePickup(code);
        pickup.setDateCreationPickup(LocalDateTime.now());
        return pr.save(pickup);
    }

    @Transactional
    @Override
    public void removePickup(Long id) {
        Pickup pickup = pr.findById(id).get();
        String PICKED = "PICKED";
        if (!pickup.getStatusPickupSeller().equals(StatusPickupSeller.PICKED)) {
            throw new IllegalStateException("Cannot remove pickup with status other than PICKED");
        }
        pr.deleteById(id);
    }

    @Override
    public Pickup RetrievePickup(Long id) {
        if (pr.findById(id).isPresent())
            return pr.findById(id).get();
        return new Pickup();
    }

    @Override
    public List<Pickup> RetrievePickups() {
        return (List<Pickup>) pr.findAll();
    }

    @Override
    public Pickup updatePickup(Pickup pickup,Long idPikup) {
        Pickup pickup1=pr.findById(idPikup).get();
        pickup.setOrder(pickup1.getOrder());
        pickup.setStore(pickup1.getStore());
        return pr.save(pickup);
    }

    @Override
    public List<Pickup> RetrievePickupsByGovernoratBetweenPickupAndStoreAndDeliveryAgencyMen(Long id) {
        return null;
    }

    @Override
    public Pickup AssignPickupByOder(Pickup pickup, Long id) {
        Pickup p = pr.save(pickup);
        Order order = or.findById(id).get();
        p.setOrder(order);
        return pr.save(pickup);
    }

    @Override
    public List<Pickup> RetrievePickupsByGovernoratBetweenStoreAndDeliveryMenFreelancer() {
        //HADHI session variable
        User u=sessionService.getUserBySession();

        double a = openWeatherMapClient.getWeather(u.getGovernorate());
        List<Store> store = (List<Store>) sr.findAll();
        List<Store> storesInSameGovernorate = sr.findByGovernorate(u.getGovernorate());

        List<Pickup> pickups = new ArrayList<>();
        //ELI 3ANDHOUM BIKE MATODHA7RELHOUM KEN EL PICKUPS ELI BECH TTLVRA FEN HOUMA W STORE YABDA FARD GOVERNORATE
        if (a > 2) {
            if ((u.getGear().equals("BIKE")) || (u.getGear().equals("MOTO"))) {
                for (Store s : store) {
                    if (u.getGovernorate().equals(s.getGovernorate())) {
                        return pr.findByGovernorate(s.getGovernorate());
                    }
                }
            }
        }
        if (u.getGear().equals("CAR")) {
            for (Store storee : storesInSameGovernorate) {

                for(Pickup pe:storee.getPickups() ){
                    boolean hasRequest=false;
                    for(Request re:pe.getRequests()){
                        if(re.getDeliveryman()!=null && re.getDeliveryman().getId().equals(u.getId())){
                            hasRequest=true;
                            break;
                        }
                    }
                    if(!hasRequest && ( pe.getStatusPickupSeller().equals(StatusPickupSeller.PICKED))){
                        pickups.add(pe);
                    }
                }
               // pickups.addAll(storee.getPickups());
            }
        }
        return pickups;
    }



    @Override
    public List<Pickup> RetrievePickupsbetweenAgencyBranchAndStoreInTheSomeGovernorat() {
        //sessionManager Variable
        User u = sessionService.getUserBySession();
        List<Store> stores = sr.findAll();
        List<AgencyBranch> agencyBranches = new ArrayList<>();
        Set<Pickup> pickups = new HashSet<>();
        List<Request> requests=pr.REQUESTofuser(u.getId());
        agencyBranches.addAll(u.getAgencyBranches());
        
        for (AgencyBranch ab : agencyBranches) {
            for (Store s : stores) {
                if (s.getGovernorate().equals(ab.getGovernorate())) {
                    // Filter pickups based on whether they have an associated request or not
                    for (Pickup pickup : s.getPickups()) {
                        boolean hasRequest = false;

                            for (Request request : pickup.getRequests()) {
                                if (request.getAgency() != null && request.getAgency().getId().equals(u.getId())) {
                                    hasRequest = true;
                                    break;
                                }
                            }

                        if (!hasRequest) {
                            if(pickup.getStatusPickupSeller().equals(StatusPickupSeller.PICKED)) {
                            pickups.add(pickup);
                        }
                        }
                    }
                }
            }
        }
        return new ArrayList<>(pickups);
    }

    @Override
    public Pickup AssignPickupByStoreAndOrder(Pickup pickup, Long id, Long IdSotre) {
        //Variable Of Session Manager
        User u=sessionService.getUserBySession();
        Store store2 = pr.storeoforder(IdSotre, id, u.getId());
        Store storeer = sr.findById(IdSotre).get();
        Pickup pickup1 = pr.save(pickup);
        Order order = or.findById(id).get();
        List<Pickup> pickups = (List<Pickup>) pr.findAll();
        Random random = new Random(); //java.util.Random
        int randomNumber = random.nextInt(9000) + 1000;  // generates a random number between 1000 and 9999
        String prefix = "216";
        String code = prefix + randomNumber;
        pr.countstoreorder(id);
        float totalPrice = 0;
        List<Product> productList = pr.productOfTheStoreById(IdSotre, id, u.getId());
        int storeofsomeUser = pr.countstoreofproductinorderOfSomeseller(IdSotre, id, u.getId());
        if (pr.countstoreorder(id) == 1) {

            pickup1.setStatusPickupSeller(StatusPickupSeller.valueOf("PICKED"));
            pickup1.setStatusPickupBuyer(StatusPickupBuyer.valueOf("PLACED"));
            ///
            //ken el code el random mawjoud y3awed yrandom code a5er hhhh
            for (Pickup p : pickups) {
                if (p.getCodePickup() != code) {
                    pickup1.setCodePickup(code);
                } else {
                    int randomNumber1 = random.nextInt(100) + 100;
                    String code1 = prefix + randomNumber + randomNumber1;
                    pickup1.setCodePickup(code1);
                }
            }
            pickup1.setCodePickup(code);
            //
            pickup1.setOrder(order);
            pickup1.setShippingStatus(order.getPayment().toString());
            pickup1.setDateCreationPickup(LocalDateTime.now());
            // pickup1.setOrderOfTheSomeSeller(true);
            pickup1.setStore(storeer);
            pickup1.setSum(order.getSum());
            if(order.getDeliveryPrice()>=1 && order.getDeliveryPrice()<=8){
                pickup.setPoints(4);
            } else {
                pickup1.setPoints(6);
            }
            if (order.getPayment().equals(PaymentType.BANK_CARD)) {
                pickup1.setPayed(true);
            } else {
                pickup1.setPayed(false);
            }
        } else {
            if (storeofsomeUser >= 1) {
                for (Product p : productList) {
                    totalPrice += p.getProductPrice();
                    pickup1.setStore(storeer);
                    pickup1.setOrder(order);

                    //ken el code el random mawjoud y3awed yrandom code a5er hhhh
                    for (Pickup pc : pickups) {
                        if (pc.getCodePickup() != code) {
                            pickup1.setCodePickup(code);
                        } else {
                            int randomNumber1 = random.nextInt(100) + 100;
                            String code1 = prefix + randomNumber + randomNumber1;
                            pickup1.setCodePickup(code1);
                        }
                    }
                    if(order.getDeliveryPrice()>=1 && order.getDeliveryPrice()<=8){
                        pickup.setPoints(4);
                    } else {
                        pickup1.setPoints(6);
                    }
                    pickup1.setShippingStatus(order.getPayment().toString());
                    pickup1.setCodePickup(code);
                    pickup1.setSum(totalPrice);
                }

            }
        }

        return pr.save(pickup1);
    }

    @Override
    public Pickup ModifyStatusOfPickupByDelivery(String Status, Long idPickup) {
        Pickup pickup = pr.findById(idPickup).get();
        if (Status.equals("TAKED")) {
            pickup.setStatusPickupBuyer(StatusPickupBuyer.valueOf("TAKED"));
            pickup.setStatusPickupSeller(StatusPickupSeller.valueOf("TAKED"));
            pr.save(pickup);
        }
        else if (Status.equals("ONTHEWAY")) {
            pickup.setStatusPickupBuyer(StatusPickupBuyer.valueOf("ONTHEWAY"));
            pickup.setStatusPickupSeller(StatusPickupSeller.valueOf("ONTHEWAY"));
            pr.save(pickup);
        }
        else if (Status.equals("DELIVERED")) {
            pickup.setStatusPickupBuyer(StatusPickupBuyer.valueOf("DELIVERED"));
            pickup.setStatusPickupSeller(StatusPickupSeller.valueOf("DELIVERED"));
            pr.save(pickup);
        } else if (Status.equals("RETURN")) {
            pickup.setStatusPickupBuyer(StatusPickupBuyer.valueOf("RETURN"));
            pickup.setStatusPickupSeller(StatusPickupSeller.valueOf("RETURN"));
            pr.save(pickup);
        } else {
            pickup.setStatusPickupBuyer(StatusPickupBuyer.valueOf("REFUNDED"));
            pickup.setStatusPickupSeller(StatusPickupSeller.valueOf("REFUNDED"));
            pr.save(pickup);
        }
        return null;
    }

    @Override
    public Duration calculateDeliveryTime(Long idPickup) throws IOException, InterruptedException, ApiException {

        Pickup pickup1 = pr.pickupprettolivred(idPickup);
        Request request1 = pr.Requestprettolivred(idPickup);
        if (request1.getRequestStatus().equals(RequestStatus.APPROVED)) {
            GeoApiContext context = new GeoApiContext.Builder()
                    .apiKey("AIzaSyBdVAHuNwlcMICaKUcx8RNGUb5dBiMYIIo")
                    .build();
            // Get the distance and travel time using the DistanceMatrixApi
            DistanceMatrixApiRequest request = new DistanceMatrixApiRequest(context)
                    .origins(pickup1.getGovernorate())
                    .destinations(pickup1.getStore().getGovernorate())
                    .mode(TravelMode.DRIVING);

            DistanceMatrix matrix = request.await();
            Distance distance = matrix.rows[0].elements[0].distance;
            if ((request1.getDeliveryman() != null && request1.getDeliveryman().getGear() != null && request1.getDeliveryman().getGear().equals("CAR"))
                    || (request1.getAgencyDeliveryMan() != null && request1.getAgencyDeliveryMan().getGearv() != null && request1.getAgencyDeliveryMan().getGearv().equals("CAR"))) {
                double averageSpeed = 60.0; // km/h
                double distanceInKm = distance.inMeters / 1000.0;
                double travelTimeInHours = distanceInKm / averageSpeed;
                // Return the estimated delivery time as a Duration object
                return Duration.ofHours((long) travelTimeInHours);
            } else if ((request1.getDeliveryman() != null && request1.getDeliveryman().getGear() != null && request1.getDeliveryman().getGear().equals("BIKE"))
                    || (request1.getAgencyDeliveryMan() != null && request1.getAgencyDeliveryMan().getGearv() != null && request1.getAgencyDeliveryMan().getGearv().equals("BIKE"))) {
                double averageSpeed = 10.0; // km/h
                double distanceInKm = distance.inMeters / 1000.0;
                double travelTimeInHours = distanceInKm / averageSpeed;
                // Return the estimated delivery time as a Duration object
                return Duration.ofHours((long) travelTimeInHours);
            } else if ((request1.getDeliveryman() != null && request1.getDeliveryman().getGear() != null && request1.getDeliveryman().getGear().equals("MOTO"))
                    || (request1.getAgencyDeliveryMan() != null && request1.getAgencyDeliveryMan().getGearv() != null && request1.getAgencyDeliveryMan().getGearv().equals("MOTO"))) {
                double averageSpeed = 30.0; // km/h
                double distanceInKm = distance.inMeters / 1000.0;
                double travelTimeInHours = distanceInKm / averageSpeed;
                // Return the estimated delivery time as a Duration object
                return Duration.ofHours((long) travelTimeInHours);
            } else {
                // Calculate the estimated travel time based on the gear information
                double averageSpeed = 60.0; // km/h
                double distanceInKm = distance.inMeters / 1000.0;
                double travelTimeInHours = distanceInKm / averageSpeed;
                // Return the estimated delivery time as a Duration object
                return Duration.ofHours((long) travelTimeInHours);
            }

        }
        return null;

    }

    @Override
    public int test(Long id) {
        User user = ur.findById(id).get();
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(user.getEmail());
        mailMessage.setSubject("subject");
        mailMessage.setText("body");
        javaMailSender.send(mailMessage);
        return 1;
    }

    @Override
    public Pickup trakingbyseller(String codePickup) {
        //session varaible
        User u=sessionService.getUserBySession();
        return pr.trakingS(codePickup, u.getId());
    }

    @Override
    public Pickup trakingbybuyer(String codePickup) {
        //idBuyer mel session manager
        User u=sessionService.getUserBySession();
        return pr.trakingB(codePickup, u.getId());
    }

    @Override
    public List<Pickup> retrievePickupByDeliveryMenFreelancer() {
        //session manager
        User u=sessionService.getUserBySession();
        return pr.pickupOfDeliveryMenFreelancer(u.getId());
    }

    @Override
    public List<Pickup> retrievePickupByAgence() {
        //session Manager Variable
        User u=sessionService.getUserBySession();
        return pr.pickupOfAgency(u.getId());
    }

    @Override
    public List<Pickup> retrievePickupByBranch(Long idbranch) {
        //session manager mt3 el agence
        User u=sessionService.getUserBySession();
        return pr.pickupOfBranch(u.getId(), idbranch);
    }

    @Override
    public List<Order> retrieveOrderByseller(Long idStore) {
        User u=sessionService.getUserBySession();
        List<Pickup> pickups = (List<Pickup>) pr.findAll();
        List<Order> orders = pr.orderOfstore(idStore, u.getId());
        List<Order> finalOrders = new ArrayList<>();
        List<Product>products=pr.ProductBysto(idStore);
        double sum=0;
        for (Order order : orders) {
            boolean hasPickup = false;
            for (Pickup pickup : pickups) {
                for(Product p:products) {
                    sum=p.getProductPrice()+sum;
                    if ((pickup.getOrder().getId().equals(order.getId())) && (pickup.getStore().getId()==idStore)) {
                        hasPickup = true;
                        break;
                    }
                }
            }
            if (!hasPickup) {
                finalOrders.add(order);
            }
        }
        return finalOrders;
    }
    @Override
    public List<Pickup> retrievePickupBysellerAttent() {
        /////session manager
        User u=sessionService.getUserBySession();
        return pr.PickupBySeller(u.getId());
    }

    ///////stat
    @Override
    public int countPickupSellerPendingToday() {
        //Session Manager idSeller
        User u=sessionService.getUserBySession();
        return pr.countPickupSellerPendingToday(u.getId());
    }

    @Override
    public int countPickupSelleronTheWayToday() {
        //Session Manager idSeller
        User u=sessionService.getUserBySession();
        return pr.countPickupSelleronTheWayToday(u.getId());
    }

    @Override
    public int countPickupSellerDeliveredToday() {
        //Session Manager idSeller
        User u=sessionService.getUserBySession();
        return pr.countPickupSellerDeliveredToday(u.getId());
    }

    @Override
    public int countPickupSellerReturnToday() {
        //Session Manager idSeller
        User u=sessionService.getUserBySession();
        return pr.countPickupSellerReturnToday(u.getId());
    }

    @Override
    public int countPickupSellerRefundedToday() {
        //Session Manager idSeller
        User u=sessionService.getUserBySession();
        return pr.countPickupSellerRefundedToday(u.getId());
    }

    @Override
    public int countPickupDeliveryManFreelancerPendingToday() {
        //Session Manager ManFreelancer
        User u=sessionService.getUserBySession();
        return pr.countPickupDeliveryManFreelancerPendingToday(u.getId());
    }

    @Override
    public int countPickupAgencyToday() {
        //Session Manager Agency
        User u=sessionService.getUserBySession();
        return pr.countPickupAgencyToday(u.getId());
    }

    @Override
    public int countRequestRejectedDeliveryManFreelancerToday() {
        //Session Manager DeliveryManFreelancer
        User u=sessionService.getUserBySession();
        return pr.countRequestRejectedDeliveryManFreelancerToday(u.getId());
    }

    @Override
    public int countRequestApprovedDeliveryManFreelancerToday() {
        //Session Manager DeliveryManFreelancer
        User u=sessionService.getUserBySession();
        return pr.countRequestApprovedDeliveryManFreelancerToday(u.getId());
    }

    @Override
    public int countRequestRejectedAgencyToday() {
        //Session Manager Agency
        User u=sessionService.getUserBySession();
        return pr.countRequestRejectedAgencyToday(u.getId());
    }

    @Override
    public int countRequestApprovedAgencyToday() {
        //Session Manager Agency
        User u=sessionService.getUserBySession();
        return pr.countRequestApprovedAgencyToday(u.getId());
    }

    @Override
    public Float SumPricePickupDeliveredByFreelancerToday() {
        //Session Manager Agency
        User u=sessionService.getUserBySession();
        List<Pickup> pickups = new ArrayList<>();
        pickups.addAll(pr.SumPricePickupDeliveredByFreelancerToday(u.getId()));
        Float sum = Float.valueOf(0);
        for (Pickup p : pickups) {
            sum = p.getOrder().getDeliveryPrice() + sum;
        }
        return sum;
    }

    @Override
    public Float SumPricePickupDeliveredByAgencyToday() {
        //Session Manager Agency
        User u=sessionService.getUserBySession();
        List<Pickup> pickups = new ArrayList<>();
        pickups.addAll(pr.SumPricePickupDeliveredByAgencyToday(u.getId()));
        Float sum = Float.valueOf(0);
        for (Pickup p : pickups) {
            sum = p.getSum() + sum;
        }
        return sum;
    }

    @Override
    public Float SumPriceDeliveryPickupisDeliveredByFreelancerToday() {
        return null;
    }

    @Override
    public Float SumPriceDeliveryPickupisDeliveredByAgencyToday() {
        return null;
    }

    @Override
    public List<Product> RetrieveProductByPickup(Long idPickup) {
        List<Product> s = pr.ProductBystorebyPickup(idPickup);
        return s;
    }

    ///////////////stat Administrator
    @Override
    public int countAgencyAdministrator() {
        return pr.countAgencyAdministrator();
    }

    @Override
    public int countDeliveryFreelancerAdministrator() {
        return pr.countDeliveryFreelancerAdministrator();
    }

    @Override
    public int countPickupDeliveredTodayAdministrator() {
        return pr.countPickupDeliveredTodayAdministrator();
    }

    @Override
    public int countOfPickupOnTheWayTodayAdministrator() {
        return pr.countOfPickupOnTheWayTodayAdministrator();
    }

    @Override
    public int countOfPickupReturnedTodayAdministrator() {
        return pr.countOfPickupReturnedTodayAdministrator();
    }

    @Override
    public int countOfPickupDeliveredweekAdministrator() {
        return pr.countOfPickupDeliveredweekAdministrator();
    }

    @Override
    public int countOfPickupOnTheWayweekAdministrator() {
        return pr.countOfPickupOnTheWayweekAdministrator();
    }

    @Override
    public int countOfPickupReturnedweekAdministrator() {
        return pr.countOfPickupReturnedweekAdministrator();
    }

    @Override
    public Float sumOfPickupDeliveredTodayAdministrator() {
        List<Pickup> pickups = new ArrayList<>();
        pickups.addAll(pr.sumOfPickupDeliveredTodayAdministrator());
        Float sum = Float.valueOf(0);
        for (Pickup p : pickups) {
            sum = p.getSum() + sum;
        }
        return sum;
    }

    @Override
    public Float sumOfPickupOnTheWayTodayAdministrator() {
        List<Pickup> pickups = new ArrayList<>();
        pickups.addAll(pr.sumOfPickupOnTheWayTodayAdministrator());
        Float sum = Float.valueOf(0);
        for (Pickup p : pickups) {
            sum = p.getSum() + sum;
        }
        return sum;
    }

    @Override
    public Float sumOfPickupReturnedTodayAdministrator() {
        List<Pickup> pickups = new ArrayList<>();
        pickups.addAll(pr.sumOfPickupReturnedTodayAdministrator());
        Float sum = Float.valueOf(0);
        for (Pickup p : pickups) {
            sum = p.getSum() + sum;
        }
        return sum;
    }

    @Override
    public Float sumOfPickupDeliveredweekAdministrator() {
        List<Pickup> pickups = new ArrayList<>();
        pickups.addAll(pr.sumOfPickupDeliveredweekAdministrator());
        Float sum = Float.valueOf(0);
        for (Pickup p : pickups) {
            sum = p.getSum() + sum;
        }
        return sum;
    }

    @Override
    public Float sumOfPickupOnTheWayweekAdministrator() {
        List<Pickup> pickups = new ArrayList<>();
        pickups.addAll(pr.sumOfPickupOnTheWayweekAdministrator());
        Float sum = Float.valueOf(0);
        for (Pickup p : pickups) {
            sum = p.getSum() + sum;
        }
        return sum;
    }

    @Override
    public Float sumOfPickupReturnedweekAdministrator() {
        List<Pickup> pickups = new ArrayList<>();
        pickups.addAll(pr.sumOfPickupReturnedweekAdministrator());
        Float sum = Float.valueOf(0);
        for (Pickup p : pickups) {
            sum = p.getSum() + sum;
        }
        return sum;
    }

    @Scheduled(cron = "* * * 1 * *")
    public Float kilometreTotalConsommerParFreelancerDelivery() throws Exception {
        //session manager idUser
        List<User> u1= (List<User>) ur.findAll();
        double priceEssnceliters = Double.parseDouble(se.scrapePage("https://fr.globalpetrolprices.com/Tunisia/gasoline_prices/"));
        float kiloSum = 0;
        double price = Float.valueOf(0);
        double co2kilo = Float.valueOf(0);
        double Co2Car = 2.55;
        //////
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey("AIzaSyBdVAHuNwlcMICaKUcx8RNGUb5dBiMYIIo")
                .build();
        // Get the distance and travel time using the DistanceMatrixApi
        for (User u: u1) {
            List<Pickup> pickups = pr.SumKilometreINCar(u.getId());
            List<Pickup> pickups1 = pr.AgencyINCar(u.getId());
            if (u.getRole().getType().equals(RoleType.DELIVERYMEN)) {
                for (Pickup p : pickups) {
                    DistanceMatrixApiRequest request = new DistanceMatrixApiRequest(context)
                            .origins(p.getCity())
                            .destinations(p.getStore().getCity())
                            .mode(TravelMode.DRIVING);

                    DistanceMatrix matrix = request.await();
                    Distance distance = matrix.rows[0].elements[0].distance;
                    double distanceInKm = distance.inMeters / 1000.0;
                    kiloSum = (float) distanceInKm + kiloSum;
                    if(u.getGear().equals("CAR")){
                        if (u.getGearAge() > 0 && u.getGearAge() < 5) {
                            price = kiloSum * (5.8 / 100) * priceEssnceliters;
                            co2kilo = kiloSum * (5.8 / 100) * Co2Car;
                        } else if (u.getGearAge() >= 5 && u.getGearAge() < 10) {
                            price = kiloSum * (6.9 / 100) * priceEssnceliters;
                            co2kilo = kiloSum * (6.9 / 100) * Co2Car;
                        } else if (u.getGearAge() >= 10) {
                            price = kiloSum * (7.8 / 100) * priceEssnceliters;
                            co2kilo = kiloSum * (7.8 / 100) * Co2Car;
                        }
                    }
                    DecimalFormat decimalFormat = new DecimalFormat("#.##");
                    String formattedNumber = decimalFormat.format(price);
                    u.setFraisEssance(formattedNumber);
                    u.setKilometreConsomer(kiloSum);
                    u.setCo2(co2kilo);
                    ur.save(u);
                }
            } else if (u.getRole().getType().equals(RoleType.DELIVERYAGENCY)) {
                for (Pickup p : pickups1) {
                    DistanceMatrixApiRequest request = new DistanceMatrixApiRequest(context)
                            .origins(p.getGovernorate())
                            .destinations(p.getStore().getGovernorate())
                            .mode(TravelMode.DRIVING);

                    DistanceMatrix matrix = request.await();
                    Distance distance = matrix.rows[0].elements[0].distance;
                    double distanceInKm = distance.inMeters / 1000.0;
                    kiloSum = (float) distanceInKm + kiloSum;
                    if(u.getGear().equals("CAR")){
                            price = kiloSum * (5.8 / 100) * priceEssnceliters;
                        co2kilo = kiloSum * (5.8 / 100) * Co2Car;
                    }
                    DecimalFormat decimalFormat = new DecimalFormat("#.##");
                    String formattedNumber = decimalFormat.format(price);
                    u.setFraisEssance(formattedNumber);
                    u.setKilometreConsomer(kiloSum);
                    u.setCo2(co2kilo);
                    ur.save(u);
                }
            }


        }

        return kiloSum;

    }

    @Override
    public String FraisEssenceTotal() throws Exception {
        return null;
    }


    @Override
    public double LimiteCo2() throws IOException, InterruptedException, ApiException {
        //sessionManager
        User user=sessionService.getUserBySession();
        double co2kilo = user.getCo2();
        if (co2kilo > 1200) {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(user.getEmail());
            mailMessage.setSubject("You Consume Your Limit Of CO2");
            mailMessage.setText("you must give the world a tree if you don't like to get a strike in COCO market");
            javaMailSender.send(mailMessage);
            //PickupTwilio.sendSMS("You Consume Your Limit Of CO2 ,you must give the world a tree if you don't like to get a strike in COCO market");
        }
        return co2kilo;
    }

    @Override
    public User UpdateTheCO2ConsoFreelancer() throws IOException, InterruptedException, ApiException {
        return null;
    }

    @Override
    public List<Pickup> RetrievePickupAgencyByRequestWithStatusRequestApproved() {
        //sessionManager
        User u=sessionService.getUserBySession();
        return pr.ListePickupByStatusAPPROVEDRequest(u.getId());
    }

    @Override
    public List<Pickup> RetrievePickupFreelancerByRequestWithStatusRequestApproved() {
        //sessionManager
        User u=sessionService.getUserBySession();
        return pr.ListePickupByStatusAPPROVEDRequestFreelancer(u.getId());
    }

    @Override
    public Set<Store> RetrieveStoreOfUser() {
        User u=sessionService.getUserBySession();
        List<Store> stores = sr.findAll();
        Set<Store> stores1 = new TreeSet<>((s1, s2) -> Long.compare(s1.getId(), s2.getId())); // Use TreeSet with custom Comparator
        for (Store store : stores) {
            if (store.getSeller().getId().equals(u.getId())) {
                stores1.add(store);
            }
        }
        return stores1;
    }

    @Override
    public Order GetOrderById(Long IdOrder) {
        Order order=or.findById(IdOrder).get();
        return order;
    }

    @Override
    public Shipping getShippingByOrder(Long IdOrder) {
        Order order=or.findById(IdOrder).get();
        Shipping shipping=order.getShipping();
        return shipping;
    }

    @Override
    public User getBuyerByOrder(Long IdOrder) {
        Order order=or.findById(IdOrder).get();
        User buyer=order.getBuyer();
        return buyer;
    }

    @Override
    public Order getOrderByPickupId(Long idPickup) {
        return pr.getOrderByPickupId(idPickup);
    }

    @Override
    public User getBuyerByPickupId(Long idPickup) {
        return pr.getUserByPickupId(idPickup);
    }

    @Override
    public Shipping getShippingByPickupId(Long idPickup) {
        return pr.getShippingByPickupId(idPickup);
    }

    @Override
    public User getUserNOw() {
        User u=sessionService.getUserBySession();
        return u;
    }

    @Override
    public int countOrderBySellerNoPickup(Long idStore) {
        User u=sessionService.getUserBySession();
        List<Pickup> pickups = (List<Pickup>) pr.findAll();
        List<Order> orders = pr.orderOfstore(idStore, u.getId());
        List<Order> finalOrders = new ArrayList<>();
        int nb=0;
        for (Order order : orders) {
            boolean hasPickup = false;
            for (Pickup pickup : pickups) {
                if ((pickup.getOrder().getId().equals(order.getId()))&&(pickup.getStore().getId()==idStore)) {
                    hasPickup = true;
                    break;
                }
            }
            if (!hasPickup) {
                finalOrders.add(order);
            }
        }
        for (Order o:finalOrders) {
            nb++;
        }
        return nb;
    }

    @Override
    public ResponseEntity<Map<Float, List<Product>>> getProduct(Long idOrder,Long idStore) {

            //Variable Of Session Manager
            User u=sessionService.getUserBySession();
            Store store2 = pr.storeoforder(idStore, idOrder, u.getId());
            Store storeer = sr.findById(idOrder).get();
            Order order = or.findById(idOrder).get();

        float totalPrice=0;
        int storeofsomeUser = pr.countstoreofproductinorderOfSomeseller(idStore, idOrder, u.getId());
        List<Product> productList = pr.productOfTheStoreById(idStore, idOrder, u.getId());


        if (pr.countstoreorder(idOrder) == 1) {
            totalPrice =order.getSum();
            } else {
                if (storeofsomeUser >= 1) {
                    for (Product p : productList) {
                         totalPrice = p.getProductPrice()+totalPrice;

                    }

                }
            }
        Map<Float, List<Product>> resultMap = new HashMap<>();
        resultMap.put(totalPrice, productList);

        return ResponseEntity.ok(resultMap);
    }

    @Override
    public List<Product> getListProductOfOrder(Long idOrder, Long idStore) {
        //Variable Of Session Manager
        User u=sessionService.getUserBySession();
        List<Product> productList = pr.productOfTheStoreById(idStore, idOrder, u.getId());
        return productList;
    }

    @Override
    public Float getSumPriceProductOfOrder(Long idOrder, Long idStore) {
        //Variable Of Session Manager
        User u=sessionService.getUserBySession();
        Store store2 = pr.storeoforder(idStore, idOrder, u.getId());
        Store storeer = sr.findById(idOrder).get();
        Order order = or.findById(idOrder).get();

        float totalPrice=0;
        int storeofsomeUser = pr.countstoreofproductinorderOfSomeseller(idStore, idOrder, u.getId());
        List<Product> productList = pr.productOfTheStoreById(idStore, idOrder, u.getId());


        if (pr.countstoreorder(idOrder) == 1) {
            totalPrice =order.getSum();
        } else {
            if (storeofsomeUser >= 1) {
                for (Product p : productList) {
                    totalPrice = p.getProductPrice()+totalPrice;

                }

            }
        }
        return totalPrice;
    }

    @Override
    public List<ProductQuantity> getAllProductQuantity() {
        return pq.findAll();
    }

    @Override
    public int countPickupDeliveredForAgency() {
        //Variable Of Session Manager
        User u=sessionService.getUserBySession();
        return pr.countPickupDeliveredForAgency(u.getId());
    }

    @Override
    public int countPickupReturnedForAgency() {
        //Variable Of Session Manager
        User u=sessionService.getUserBySession();
        return pr.countPickupReturnedForAgency(u.getId());
    }

    @Override
    public int countPickupOnTheWayForAgency() {
        //Variable Of Session Manager
        User u=sessionService.getUserBySession();
        return pr.countPickupOnTheWayForAgency(u.getId());
    }

    @Override
    public int countPickupRefundedForAgency() {
        //Variable Of Session Manager
        User u=sessionService.getUserBySession();
        return pr.countPickupRefundedForAgency(u.getId());
    }

    @Override
    public int countPickupAssignedForAgency() {
        User u=sessionService.getUserBySession();
        return pr.countPickupAssignedForAgency(u.getId());
    }

    @Override
    public int countPickupTakedForAgency() {
        User u=sessionService.getUserBySession();
        return pr.countPickupTakedForAgency(u.getId());
    }

    @Override
    public int countPickupDeliveredForfreelancer() {
        User u=sessionService.getUserBySession();
        return pr.countPickupDeliveredForFreelancer(u.getId());
    }

    @Override
    public int countPickupReturnedForfreelancer() {
        User u=sessionService.getUserBySession();
        return pr.countPickupReturnedForFreelancer(u.getId());
    }

    @Override
    public int countPickupOnTheWayForfreelancer() {
        User u=sessionService.getUserBySession();
        return pr.countPickupOnTheWayForFreelancer(u.getId());
    }

    @Override
    public int countPickupRefundedForfreelancer() {
        User u=sessionService.getUserBySession();
        return pr.countPickupRefundedForFreelancer(u.getId());
    }

    @Override
    public int countPickupAssignedForFreelancer() {
        User u=sessionService.getUserBySession();
        return pr.countPickupAssignedForFreelancer(u.getId());
    }

    @Override
    public int countPickupTakedForFreelancer() {
        User u=sessionService.getUserBySession();
        return pr.countPickupTakedForFreelancer(u.getId());
    }

    @Override
    public int countPickupAssignedSeller() {
        User u=sessionService.getUserBySession();
        return pr.countpickupassignedSeller(u.getId());
    }

    @Override
    public int countPickupTakedSeller() {
        User u=sessionService.getUserBySession();
        return pr.countpickupTakedSeller(u.getId());
    }

    @Override
    public List<Pickup> RetrievePickupInProgress() {
        User u=sessionService.getUserBySession();
        List<Pickup> pickups=pr.retrievePickupInprogress(u.getId());
        List<Pickup> pickupsml=new ArrayList<>();
        for (Pickup p:pickups) {
            if(!p.getStatusPickupSeller().equals(StatusPickupSeller.PICKED)){
                pickupsml.add(p);
            }
        }
        return pickupsml;
    }

    @Override
    public int countProductQuantityInOrderProduct(Long idOrder, Long idProduct) {
        return pr.countProductQuantityByOrderAndProduct(idOrder,idProduct);
    }

    @Override
    public Store getStoreByPickup(Long idPickup) {
        return pr.getStoreByPickup(idPickup);
    }

    @Override
    public Double SumOfPricePickupDeliveredToday() {
            List<Pickup> pickups = pr.sumOfPickupDeliveredTodayAdministrator();
            double sum = 0;
            Set<Long> countedOrders = new HashSet<>();
            for (Pickup p : pickups) {
                Long orderId = p.getOrder().getId();
                if (!countedOrders.contains(orderId)) {
                    countedOrders.add(orderId);
                    sum += p.getOrder().getDeliveryPrice();
                }
            }
            return sum;
        }

    @Override
    public Map<StatusPickupSeller, Integer> getNumberOfPickupByStatus() {
        List<Pickup> pickup= (List<Pickup>) pr.findAll();
        Map<StatusPickupSeller, Integer> countMap = new HashMap<>();
        countMap.put(StatusPickupSeller.PICKED, 0);
        countMap.put(StatusPickupSeller.ASSIGNED, 0);
        countMap.put(StatusPickupSeller.DELIVERED, 0);
        countMap.put(StatusPickupSeller.TAKED, 0);
        countMap.put(StatusPickupSeller.RETURN, 0);
        countMap.put(StatusPickupSeller.REFUNDED, 0);
        for (Pickup p:pickup) {
            if (p.getStatusPickupSeller().equals(StatusPickupSeller.PICKED)) {
                countMap.put(StatusPickupSeller.PICKED, countMap.get(StatusPickupSeller.PICKED) + 1);
            } else  if (p.getStatusPickupSeller().equals(StatusPickupSeller.ASSIGNED)) {
                countMap.put(StatusPickupSeller.ASSIGNED, countMap.get(StatusPickupSeller.ASSIGNED) + 1);
            }
            else  if (p.getStatusPickupSeller().equals(StatusPickupSeller.DELIVERED)) {
                countMap.put(StatusPickupSeller.DELIVERED, countMap.get(StatusPickupSeller.DELIVERED) + 1);
            }
            else  if (p.getStatusPickupSeller().equals(StatusPickupSeller.TAKED)) {
                countMap.put(StatusPickupSeller.TAKED, countMap.get(StatusPickupSeller.TAKED) + 1);
            }
            else  if (p.getStatusPickupSeller().equals(StatusPickupSeller.RETURN)) {
                countMap.put(StatusPickupSeller.RETURN, countMap.get(StatusPickupSeller.RETURN) + 1);
            }
            else  if (p.getStatusPickupSeller().equals(StatusPickupSeller.REFUNDED)) {
                countMap.put(StatusPickupSeller.REFUNDED, countMap.get(StatusPickupSeller.REFUNDED) + 1);
            }
        }
        return countMap;
    }

    @Override
    public Map<StatusPickupSeller, Integer> getNumberOfPickupByStatusByMonthAndYearAndAll() {
        List<Pickup> pickup= (List<Pickup>) pr.findAll();
        Map<StatusPickupSeller, Integer> countMap = new HashMap<>();
        LocalDate d=LocalDate.now();
        System.out.println(d);
        countMap.put(StatusPickupSeller.PICKED, 0);
        countMap.put(StatusPickupSeller.ASSIGNED, 0);
        countMap.put(StatusPickupSeller.DELIVERED, 0);
        countMap.put(StatusPickupSeller.TAKED, 0);
        countMap.put(StatusPickupSeller.RETURN, 0);
        countMap.put(StatusPickupSeller.REFUNDED, 0);
        for (Pickup p:pickup) {
            if (p.getStatusPickupSeller().equals(StatusPickupSeller.PICKED)) {
                if((p.getDateCreationPickup().getMonth().equals(d.getMonth()))) {
                    countMap.put(StatusPickupSeller.PICKED, countMap.get(StatusPickupSeller.PICKED) + 1);
                }
            } else  if (p.getStatusPickupSeller().equals(StatusPickupSeller.ASSIGNED)) {
                if(p.getDateCreationPickup().getMonth().equals(d.getMonth())) {
                    countMap.put(StatusPickupSeller.ASSIGNED, countMap.get(StatusPickupSeller.ASSIGNED) + 1);
                }

            }
            else  if (p.getStatusPickupSeller().equals(StatusPickupSeller.DELIVERED)) {
                if((p.getDateCreationPickup().getMonth().equals(d.getMonth()))) {
                    countMap.put(StatusPickupSeller.DELIVERED, countMap.get(StatusPickupSeller.DELIVERED) + 1);
                }

            }
            else  if (p.getStatusPickupSeller().equals(StatusPickupSeller.TAKED)) {
                if((p.getDateCreationPickup().getMonth().equals(d.getMonth()))) {
                    countMap.put(StatusPickupSeller.TAKED, countMap.get(StatusPickupSeller.TAKED) + 1);
                }

            }
            else  if (p.getStatusPickupSeller().equals(StatusPickupSeller.RETURN)) {
                if((p.getDateCreationPickup().getMonth().equals(d.getMonth()))) {
                    countMap.put(StatusPickupSeller.RETURN, countMap.get(StatusPickupSeller.RETURN) + 1);
                }

            }
            else  if (p.getStatusPickupSeller().equals(StatusPickupSeller.REFUNDED)) {
                if((p.getDateCreationPickup().getMonth().equals(d.getMonth()))) {
                    countMap.put(StatusPickupSeller.REFUNDED, countMap.get(StatusPickupSeller.REFUNDED) + 1);
                }

            }
        }
        return countMap;
    }

    @Override
    public Double AllCo2User() {
        List<User> users= (List<User>) ur.findAll();
        double sum=0;
        for (User u:users) {
            sum=sum+u.getCo2();
        }
        return sum;
    }

    @Override
    public Map<String, Integer> getNumberPickupsInMonth() {
        Map<String, Integer> countMap = new HashMap<>();
        countMap.put("a",pr.PickupByMonth(1));
        countMap.put("b",pr.PickupByMonth(2));
        countMap.put("c",pr.PickupByMonth(3));
        countMap.put("d",pr.PickupByMonth(4));
        countMap.put("e",pr.PickupByMonth(5));
        countMap.put("f",pr.PickupByMonth(6));
        countMap.put("g",pr.PickupByMonth(7));
        countMap.put("h",pr.PickupByMonth(8));
        countMap.put("i",pr.PickupByMonth(9));
        countMap.put("j",pr.PickupByMonth(10));
        countMap.put("k",pr.PickupByMonth(11));
        countMap.put("m",pr.PickupByMonth(12));

        return countMap;
    }

    @Override
    public Map<String, Integer> getNumberRequestsInMonth() {
        Map<String, Integer> countMap = new HashMap<>();
        countMap.put("a",pr.RequestByMonth(1));
        countMap.put("b",pr.RequestByMonth(2));
        countMap.put("c",pr.RequestByMonth(3));
        countMap.put("d",pr.RequestByMonth(4));
        countMap.put("e",pr.RequestByMonth(5));
        countMap.put("f",pr.RequestByMonth(6));
        countMap.put("g",pr.RequestByMonth(7));
        countMap.put("h",pr.RequestByMonth(8));
        countMap.put("i",pr.RequestByMonth(9));
        countMap.put("j",pr.RequestByMonth(10));
        countMap.put("k",pr.RequestByMonth(11));
        countMap.put("m",pr.RequestByMonth(12));

        return countMap;
    }

    @Override
    public List<Pickup> RetrieveAllPickupsOfUser() {
        User u= sessionService.getUserBySession();
        return pr.getAllPickupsForUser(u.getId());
    }

    @Override
    public List<Pickup> RetrieveAllPickupsOfSeller() {
        User u=sessionService.getUserBySession();
        return pr.getPickupsOfSeller(u.getId());
    }

    @Override
    public User retrieveTheFreelancerOfPickup(Long idPickup) {
        Pickup pickup=pr.findById(idPickup).get();

        for (Request p:pickup.getRequests()) {
            if(p.getRequestStatus().equals(RequestStatus.APPROVED)){
                if(p.getAgency()!=null){
                    return p.getAgency();
                } else if (p.getDeliveryman()!=null) {
                    return p.getDeliveryman();
                }
            }
        }
        return null;
    }

    @Scheduled(cron = "* * * 27 * *")
    public void ModifyTheLevelOfDeliveryAgencyMonthly() {
        List<User> users = pr.ListOfDeliveryAgencywithStatusPickupDelivered();
        List<User> freelancer = pr.ListOfFreelancerwithStatusPickupDelivered();
        for (User u : users) {
            int uu = pr.countPickupdeliveredMonthlyByAgency(u.getId());
            if (uu >= 1 && uu < 100) {
                u.setLevelDelivery("Level 1");
                ur.save(u);
            } else if (uu >= 100 && uu < 500) {
                u.setLevelDelivery("Level 2");
                ur.save(u);
            } else if (uu >= 500) {
                u.setLevelDelivery("Top Rated Delivery");
                ur.save(u);
            }
        }
        for (User fr:freelancer) {

            int uu = pr.countPickupdeliveredMonthlyByFreelancer(fr.getId());
            if (uu > 0 && uu < 100) {
                fr.setLevelDelivery("Level 1");
                ur.save(fr);
            } else if (uu > 100 && uu < 500) {
                fr.setLevelDelivery("Level 2");
                ur.save(fr);
            } else if (uu > 500) {
                fr.setLevelDelivery("Top Rated Delivery");
                ur.save(fr);
            }

        }
    }

}