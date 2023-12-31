package tn.workbot.coco_marketplace.services.interfaces;

import tn.workbot.coco_marketplace.entities.AgencyBranch;
import tn.workbot.coco_marketplace.entities.AgencyDeliveryMan;
import tn.workbot.coco_marketplace.entities.Request;
import tn.workbot.coco_marketplace.entities.enmus.RoleType;

import java.util.List;
import java.util.Map;

public interface AgencyBranchIService {
    public AgencyBranch addAgencyBranch(AgencyBranch agencyBranch);
    public void removeAgencyBranch(Long id);
    public AgencyBranch RetrieveAgencyBranch(Long id);
    public List<AgencyBranch> retrievethebranchesofeachagency();
    public List<AgencyBranch> RetrieveAllAgencyBranch();
    public AgencyBranch updateAgencyBranch(AgencyBranch agencyBranch);
    public AgencyBranch AssignBranchManByDeliveryAgency(AgencyBranch agencyBranch);
    public List<Request> test(Long id);
    public int countAgencyBranchesInAgency();
    public int countDeliveryMenInAllAgencyBranchesForAgench();
    public List<AgencyBranch> retrieveAgencyBranchOfUser();
    public int countDeliveryMenInAgency(Long idBranch);
    public AgencyBranch updatebRANCHwithMAP(Long idBranch,AgencyBranch agencyBranch);
    public Map<RoleType, Integer> countAllAgencyAdmin();
}
