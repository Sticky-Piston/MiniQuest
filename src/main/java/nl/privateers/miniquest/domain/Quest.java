package nl.privateers.miniquest.domain;

import java.util.Optional;

public interface Quest {
    QuestID id();
    String name();
    String description();
    String questRegionType();
    String questType();
    Optional<Schematic> schematic();
    Long duration();
}
