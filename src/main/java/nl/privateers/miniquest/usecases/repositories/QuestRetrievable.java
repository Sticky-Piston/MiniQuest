package nl.privateers.miniquest.usecases.repositories;

import nl.privateers.miniquest.domain.Quest;
import nl.privateers.miniquest.domain.QuestID;

import java.util.Optional;

public interface QuestRetrievable {
    Optional<Quest> getQuest(QuestID questID);
    Optional<Quest> getRandomQuest(String regionType);
}
