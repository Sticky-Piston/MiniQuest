package nl.privateers.miniquest.implementations.region.repository;

import nl.privateers.miniquest.MiniQuest;
import nl.privateers.miniquest.domain.Quest;
import nl.privateers.miniquest.domain.QuestID;
import nl.privateers.miniquest.domain.QuestRegion;
import nl.privateers.miniquest.domain.QuestRegionID;
import nl.privateers.miniquest.implementations.region.RegionImpl;
import nl.privateers.miniquest.usecases.repositories.QuestRegionRetrievable;
import nl.privateers.miniquest.usecases.repositories.QuestRegionStreamable;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class RegionRepository implements QuestRegionRetrievable, QuestRegionStreamable {
    private final MiniQuest plugin;
    private final Map<QuestRegionID, QuestRegion> questRegions = new HashMap<>();
    private final LocalDateTime lastQuest = LocalDateTime.now();

    public RegionRepository(MiniQuest plugin, YamlConfiguration configuration) {
        this.plugin = plugin;

        for (String key : configuration.getKeys(false)) {
            final var regionConfiguration = configuration.getConfigurationSection(key);

            if (regionConfiguration != null) {
                final var regionID = new QuestRegionID(key);
                //final var region = new RegionImpl(plugin, regionID, regionConfiguration);

                final var region = RegionImpl.from(plugin, regionID, regionConfiguration);
                region.ifPresent(value -> questRegions.put(new QuestRegionID(key), value));
            } else {
                plugin.getLogger().warning("Failed to find configuration section for: " + key);
            }
        }
    }

    @Override
    public Optional<QuestRegion> getQuestRegion(QuestRegionID questRegionID) {
        return Optional.ofNullable(questRegions.get(questRegionID));
    }

    @Override
    public Stream<QuestRegion> getAllQuestRegions() {
        return questRegions.values().stream();
    }
}
