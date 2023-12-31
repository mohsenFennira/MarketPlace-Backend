package tn.workbot.coco_marketplace.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.workbot.coco_marketplace.entities.SupplierRequest;

import java.util.List;

@Repository
public interface SupplierRequestRepository extends JpaRepository<SupplierRequest,Long> {

    List<SupplierRequest> findSupplierRequestsByProductId(Long id);

}
