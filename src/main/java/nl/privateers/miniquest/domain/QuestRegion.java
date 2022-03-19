package nl.privateers.miniquest.domain;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Optional;

public interface QuestRegion {
    QuestRegionID id();
    boolean enabled();
    Optional<ActiveQuest> activeQuest();

    void assign(ActiveQuest activeQuest);
    void reset();

    boolean canAssign();
    String regionType();
    Optional<Schematic> defaultSchematic();
    World world();
    int volume();
    boolean contains(Location location);
    BlockVector3 minimumPoint();
    BlockVector3 maximumPoint();
    BlockVector3 regionCenter();
}
