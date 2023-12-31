package tn.workbot.coco_marketplace.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.workbot.coco_marketplace.entities.BadFeelings;
import tn.workbot.coco_marketplace.repositories.BadFeelingsRepository;
import tn.workbot.coco_marketplace.services.interfaces.BadFeelingsInterface;

import java.util.List;

@Service
public class BadFeelingsService implements BadFeelingsInterface {

   @Autowired
    BadFeelingsRepository badFeelingsRepository;
    @Override
    public void addNewBadFeelings(BadFeelings badFeelings) {
        badFeelingsRepository.save(badFeelings);

    }

    @Override
    public void updateBadFeelings(BadFeelings badFeelings) {
        badFeelingsRepository.save(badFeelings);
    }

    @Override
    public void deleteBadFeelings(Long id) {

        badFeelingsRepository.deleteById(id);
    }

    @Override
    public List<BadFeelings> getAllBadFeelings() {
        return badFeelingsRepository.findAll();
    }
}
