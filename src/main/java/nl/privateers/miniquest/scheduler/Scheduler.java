package nl.privateers.miniquest.scheduler;

import nl.privateers.miniquest.MiniQuest;
import nl.privateers.miniquest.implementations.quests.ActiveQuestImpl;
import nl.privateers.miniquest.implementations.quests.repository.QuestRepository;
import nl.privateers.miniquest.implementations.region.repository.RegionRepository;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDateTime;

public class Scheduler extends BukkitRunnable {
    private final MiniQuest plugin;
    private final QuestRepository questRepository;
    private final RegionRepository regionRepository;

    public Scheduler(MiniQuest plugin, QuestRepository questRepository, RegionRepository regionRepository) {
        this.plugin = plugin;
        this.questRepository = questRepository;
        this.regionRepository = regionRepository;
    }

    @Override
    public void run() {
        this.regionRepository.getAllQuestRegions().forEach(questRegion -> {
            if (questRegion.activeQuest().isEmpty()) {
                if (questRegion.canAssign()) {
                    this.plugin.getLogger().info("Found a region without an event");

                    final var randomQuest = this.questRepository.getRandomQuest(questRegion.regionType());
                    if (randomQuest.isPresent()) {

                        final var now = LocalDateTime.now();
                        final var endsAt = now.plusMinutes(randomQuest.get().duration());

                        ActiveQuestImpl activeQuest = new ActiveQuestImpl(randomQuest.get(), now, endsAt);

                        Bukkit.getScheduler().runTask(plugin, bukkitTask -> {
                            randomQuest.ifPresent(quest -> questRegion.assign(activeQuest));
                        });
                    }
                }
            } else {
                // Should the event expire?
                final var now = LocalDateTime.now();
                final var activeQuest = questRegion.activeQuest();
                if (activeQuest.isPresent()) {
                    if (now.isAfter(activeQuest.get().endsAt())) {
                        // Resetting quest event
                        this.plugin.getLogger().info("Resetting quests on region: " + questRegion.id());
                        Bukkit.getScheduler().runTask(plugin, bukkitTask -> {
                            questRegion.reset();
                        });
                    }
                }
            }
        });
    }
}
