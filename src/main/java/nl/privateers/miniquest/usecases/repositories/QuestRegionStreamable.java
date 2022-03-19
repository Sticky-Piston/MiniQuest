package nl.privateers.miniquest.usecases.repositories;

import nl.privateers.miniquest.domain.QuestRegion;

import java.util.stream.Stream;

public interface QuestRegionStreamable {
    Stream<QuestRegion> getAllQuestRegions();
}
