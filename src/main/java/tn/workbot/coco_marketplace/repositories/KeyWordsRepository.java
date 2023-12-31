package tn.workbot.coco_marketplace.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.workbot.coco_marketplace.entities.KeyWords;

@Repository
public interface KeyWordsRepository extends JpaRepository<KeyWords,Long> {

    KeyWords findByWord(String ch);
}
