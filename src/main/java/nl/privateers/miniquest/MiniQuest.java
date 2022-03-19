package nl.privateers.miniquest;

import nl.privateers.miniquest.implementations.quests.repository.QuestRepository;
import nl.privateers.miniquest.implementations.region.repository.RegionRepository;
import nl.privateers.miniquest.scheduler.Scheduler;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class MiniQuest extends JavaPlugin {
    @Override
    public void onEnable() {
        // Plugin startup logic

        // Open config
        final var questConfigPath = new File("plugins/MiniQuest/");
        final var questConfigFile = new File(questConfigPath, "quests.yml");

        questConfigPath.mkdirs();
        try {
            if (questConfigFile.createNewFile()) {
                System.out.println("Created new config file");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        final var questConfig = YamlConfiguration.loadConfiguration(questConfigFile);

        final var questRegionConfigPath = new File("plugins/MiniQuest/");
        final var questRegionConfigFile = new File(questConfigPath, "regions.yml");

        questRegionConfigPath.mkdirs();
        try {
            if (questRegionConfigFile.createNewFile()) {
                System.out.println("Created new config file");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        final var questRegionConfig = YamlConfiguration.loadConfiguration(questRegionConfigFile);

        // Initialize quest repository
        QuestRepository questRepository = new QuestRepository(this, questConfig);

        // Initialize region repository
        RegionRepository regionRepository = new RegionRepository(this, questRegionConfig);

        // Start the event manager
        new Scheduler(this, questRepository, regionRepository).runTaskTimerAsynchronously(this, 0, 50);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
