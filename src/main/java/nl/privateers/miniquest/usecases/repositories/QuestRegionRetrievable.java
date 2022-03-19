package nl.privateers.miniquest.usecases.repositories;

import nl.privateers.miniquest.domain.QuestRegion;
import nl.privateers.miniquest.domain.QuestRegionID;

import java.util.Optional;

public interface QuestRegionRetrievable {
    Optional<QuestRegion> getQuestRegion(QuestRegionID questRegionID);
}
