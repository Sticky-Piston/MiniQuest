package nl.privateers.miniquest.implementations.quests.repository;

import nl.privateers.miniquest.MiniQuest;
import nl.privateers.miniquest.domain.Quest;
import nl.privateers.miniquest.domain.QuestID;
import nl.privateers.miniquest.implementations.quests.QuestImpl;
import nl.privateers.miniquest.usecases.repositories.QuestRetrievable;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.*;
import java.util.stream.Collectors;

public class QuestRepository implements QuestRetrievable {
    private final Map<QuestID, Quest> quests = new HashMap<>();

    public QuestRepository(MiniQuest plugin, YamlConfiguration configuration) {
        for (String key : configuration.getKeys(false)) {
            final var questConfiguration = configuration.getConfigurationSection(key);
            if (questConfiguration == null) {
                plugin.getLogger().warning("No configuration found for quest ID: " + key);
                continue;
            }

            final var questID = new QuestID(key);
            quests.put(questID, new QuestImpl(plugin, questID, questConfiguration));
        }
    }

    @Override
    public Optional<Quest> getQuest(QuestID questID) {
        return Optional.ofNullable(quests.get(questID));
    }

    @Override
    public Optional<Quest> getRandomQuest(String regionType) {
        final var filteredQuests = this.quests.entrySet().stream()
                .filter(quest -> regionType.equals(quest.getValue().questRegionType()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Random generator = new Random();
        Set<QuestID> entries = filteredQuests.keySet();

        if (entries.size() > 0) {
            return Optional.ofNullable(filteredQuests.get(entries.stream().toList().get(generator.nextInt(entries.size()))));
        }
        return Optional.empty();
    }
}
