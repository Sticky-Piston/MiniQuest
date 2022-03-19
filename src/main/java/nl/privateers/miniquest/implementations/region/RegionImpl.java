package nl.privateers.miniquest.implementations.region;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.BlockVector3Imp;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import io.lumine.mythic.api.exceptions.InvalidMobTypeException;
import io.lumine.mythic.api.mobs.MobManager;
import io.lumine.mythic.bukkit.MythicBukkit;

import nl.privateers.miniquest.MiniQuest;
import nl.privateers.miniquest.domain.ActiveQuest;
import nl.privateers.miniquest.domain.QuestRegion;
import nl.privateers.miniquest.domain.QuestRegionID;
import nl.privateers.miniquest.domain.Schematic;
import nl.privateers.miniquest.implementations.schematic.SchematicImpl;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;

import java.time.LocalDateTime;
import java.util.*;

public class RegionImpl implements QuestRegion {
    private final MiniQuest plugin;
    private boolean enabled = false;
    private final QuestRegionID regionID;

    private final World world;
    private final ProtectedRegion region;

    private final ConfigurationSection configurationSection;
    private ActiveQuest activeQuest;

    private final Set<Chunk> chunks = new HashSet<>();

    private LocalDateTime lastQuestTime;

    public RegionImpl(
            MiniQuest plugin,
            QuestRegionID regionID,
            World world,
            ProtectedRegion region,
            ConfigurationSection configurationSection) {
        this.plugin = plugin;
        this.regionID = regionID;
        this.world = world;
        this.region = region;
        this.configurationSection = configurationSection;

        // Get chunks belonging to region
        for (int cx = minimumPoint().getBlockX(); cx <= maximumPoint().getBlockX() + 16; cx = cx + 16) {
            for (int cz = minimumPoint().getBlockZ(); cz <= maximumPoint().getBlockZ() + 16; cz = cz + 16) {
                Chunk currentChunk = world.getChunkAt(cx >> 4, cz >> 4);
                this.chunks.add(currentChunk);
            }
        }
    }

    public static Optional<RegionImpl> from(MiniQuest plugin, QuestRegionID regionID, ConfigurationSection configurationSection) {
        if (configurationSection.getString("region_type") == null) {
            plugin.getLogger().warning("Missing field: 'region_type' in region: " + regionID);
            return Optional.empty();
        }

        final var worldName = configurationSection.getString("world");
        if (worldName == null) {
            plugin.getLogger().warning("Missing field: 'world' in region: " + regionID);
            return Optional.empty();
        }
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("World: '" + worldName + "' not found for region: '" + regionID + "'");
            return Optional.empty();
        }

        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
        if (regionManager == null) {
            plugin.getLogger().warning("World Guard RegionManager error for region: " + regionID.questRegionID());
            return Optional.empty();
        }
        ProtectedRegion region = regionManager.getRegion(regionID.questRegionID());
        if (region == null) {
            plugin.getLogger().warning("Region: '" + regionID.questRegionID() + "' doesn't exist.");
            return Optional.empty();
        }

        if (configurationSection.getString("cool_down") == null) {
            configurationSection.addDefault("cool_down", 5f);
        }

        return Optional.of(new RegionImpl(plugin, regionID, world, region, configurationSection));
    }

    @Override
    public QuestRegionID id() {
        return this.regionID;
    }

    @Override
    public boolean enabled() {
        return this.enabled;
    }

    @Override
    public Optional<ActiveQuest> activeQuest() {
        return Optional.ofNullable(this.activeQuest);
    }

    @Override
    public void assign(ActiveQuest activeQuest) {
        this.activeQuest = activeQuest;

        final var schematic = this.activeQuest.quest().schematic();
        schematic.ifPresent(value -> value.load(this, false));

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Chunk regionChunk : this.chunks) {
                for (BlockState tileEntity : regionChunk.getTileEntities()) {
                    if (tileEntity instanceof Sign) {
                        var signLocation = tileEntity.getLocation();
                        if (contains(signLocation)) {
                            try {
                                MythicBukkit.inst().getAPIHelper().spawnMythicMob("SkeletalKnight", signLocation.toBlockLocation());
                            } catch (InvalidMobTypeException e) {
                                e.printStackTrace();
                            }
                            Sign sign = (Sign) tileEntity;
                            signLocation.getBlock().setType(Material.AIR);
                        }
                    }
                }
            }
        }, 50);

        // Filthy blob to find signs
//        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
//            for (int cx = minimumPoint().getBlockX(); cx <= maximumPoint().getBlockX() + 16; cx = cx + 16) {
//                for (int cz = minimumPoint().getBlockZ(); cz <= maximumPoint().getBlockZ() + 16; cz = cz + 16) {
//                    Chunk currentChunk = world.getChunkAt(cx >> 4, cz >> 4);
//                    Bukkit.getScheduler().runTask(plugin, () -> {
//                        for (BlockState tileEntity : currentChunk.getTileEntities()) {
//                            if (tileEntity instanceof Sign) {
//                                final var signLocation = tileEntity.getLocation();
//                                if (contains(signLocation)) {
//                                    Sign sign = (Sign) tileEntity;
//                                    signLocation.getBlock().setType(Material.RED_CONCRETE);
//                                    //tileEntity.setType(Material.RED_CONCRETE);
//                                    //plugin.getLogger().info("FOUND SIGN: " + sign.lines().get(0));
//                                }
//                            }
//                        }
//                    });
//                }
//            }
//        }, 100);
    }

    @Override
    public void reset() {
        this.activeQuest = null;
        this.lastQuestTime = LocalDateTime.now();

        final var schematic = this.defaultSchematic();
        schematic.ifPresentOrElse(
                value -> value.load(this, true),
                () -> {
                    // TODO: Set air

                    this.plugin.getLogger().warning("No default schematic found for region: " + regionID.questRegionID());
                });
    }

    @Override
    public boolean canAssign() {
        if (this.lastQuestTime == null) {
            return true;
        }

        return LocalDateTime.now().isAfter(lastQuestTime.plusMinutes(this.configurationSection.getLong("cool_down")));
    }

    @Override
    public String regionType() {
        return this.configurationSection.getString("region_type");
    }

    @Override
    public Optional<Schematic> defaultSchematic() {
        final var schematicFile = this.configurationSection.getString("schematic");
        if (schematicFile == null) {
            return Optional.empty();
        }

        return Optional.of(new SchematicImpl(this.plugin, schematicFile, regionType(), true));
    }

    @Override
    public World world() {
        return this.world;
    }

    @Override
    public int volume() {
        return region.volume();
    }

    @Override
    public boolean contains(Location location) {
        return region.contains(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
    }

    @Override
    public BlockVector3 minimumPoint() {
        return region.getMinimumPoint();
    }

    @Override
    public BlockVector3 maximumPoint() {
        return region.getMaximumPoint();
    }

    @Override
    public BlockVector3 regionCenter() {
        final var minimumPoint = region.getMinimumPoint().toVector3();
        final var maximumPoint = region.getMaximumPoint().toVector3();

        return minimumPoint.add(maximumPoint).divide(2).toBlockPoint();
    }
}
