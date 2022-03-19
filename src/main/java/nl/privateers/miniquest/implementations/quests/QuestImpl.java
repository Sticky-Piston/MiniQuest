package nl.privateers.miniquest.implementations.quests;

import nl.privateers.miniquest.MiniQuest;
import nl.privateers.miniquest.domain.Quest;
import nl.privateers.miniquest.domain.QuestID;
import nl.privateers.miniquest.domain.Schematic;
import nl.privateers.miniquest.implementations.schematic.SchematicImpl;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Optional;

public class QuestImpl implements Quest {
    private final MiniQuest plugin;
    private final QuestID questID;
    private final ConfigurationSection configurationSection;
    private final Schematic schematic;

    public QuestImpl(MiniQuest plugin, QuestID questID, ConfigurationSection configurationSection) {
        this.plugin = plugin;
        this.questID = questID;

        if (configurationSection.getString("region_type") == null) {
            Bukkit.getLogger().warning("No region_type has been specified for event: " + this.questID);
            configurationSection.addDefault("region_type", "DEFAULT");
        }
        if (configurationSection.getString("schematic") == null) {
            Bukkit.getLogger().warning("No schematic specified, using default.");
            configurationSection.addDefault("schematic", "default.schem");
        }

        configurationSection.addDefault("name", "nameless event");
        configurationSection.addDefault("description", "");
        configurationSection.addDefault("quest_type", "DUNGEON");
        configurationSection.addDefault("duration", 5f);

        this.configurationSection = configurationSection;
        this.schematic = new SchematicImpl(
                plugin,
                this.configurationSection.getString("schematic"),
                this.configurationSection.getString("region_type"),
                false);

    }

    @Override
    public QuestID id() {
        return this.questID;
    }

    @Override
    public String name() {
        return this.configurationSection.getString("name");
    }

    @Override
    public String description() {
        return this.configurationSection.getString("description");
    }

    @Override
    public String questRegionType() {
        return this.configurationSection.getString("region_type");
    }

    @Override
    public String questType() {
        return this.configurationSection.getString("quest_type");
    }

    @Override
    public Optional<Schematic> schematic() {
        return Optional.ofNullable(this.schematic);
    }

    @Override
    public Long duration() {
        return this.configurationSection.getLong("duration");
    }
}
