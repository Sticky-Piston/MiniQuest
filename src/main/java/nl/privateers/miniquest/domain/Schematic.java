package nl.privateers.miniquest.domain;

import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.World;

public interface Schematic {
    String filename();
    String regionType();
    boolean isDefault();
    void load(QuestRegion region, boolean isReset);
}
