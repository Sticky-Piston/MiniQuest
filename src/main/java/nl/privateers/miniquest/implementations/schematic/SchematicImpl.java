package nl.privateers.miniquest.implementations.schematic;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.buffer.ExtentBuffer;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import nl.privateers.miniquest.MiniQuest;
import nl.privateers.miniquest.domain.QuestRegion;
import nl.privateers.miniquest.domain.Schematic;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SchematicImpl implements Schematic {
    private final MiniQuest plugin;
    private final String filename;
    private final String regionType;
    private final boolean isDefault;

    public SchematicImpl(MiniQuest plugin, String filename, String regionType, boolean isDefault) {
        this.plugin = plugin;
        this.filename = filename;
        this.regionType = regionType;
        this.isDefault = isDefault;
    }

    @Override
    public String filename() {
        return this.filename;
    }

    @Override
    public String regionType() {
        return this.regionType;
    }

    @Override
    public boolean isDefault() {
        return this.isDefault;
    }

    private int nextChunk(int current, int max) {
        if (current > max) {
            return current + 16;
        } else {
            return current - 16;
        }
    }

    @Override
    public void load(QuestRegion region, boolean isReset) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, bukkitTask -> {
            final var file = new File(
                    String.join("/", "plugins/MiniQuest/schematics", this.regionType, this.filename));

            if (!file.exists()) {
                plugin.getLogger().warning("The schematic: " + file.getAbsolutePath() + " doesn't exist.");
                return;
            }

            Clipboard clipboard;
            ClipboardFormat format = ClipboardFormats.findByFile(file);

            if (format == null) {
                plugin.getLogger().warning("Unknown schematic format.");
                return;
            }

            try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
                clipboard = reader.read();
                clipboard.setOrigin(clipboard.getRegion().getCenter().toBlockPoint());

                Bukkit.getScheduler().runTask(plugin, () -> {
                    try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(region.world()))) {
                        ClipboardHolder holder = new ClipboardHolder(clipboard);

                        if (clipboard.getRegion().getVolume() == region.volume()) {

                            Operation operation = holder.createPaste(editSession).to(region.regionCenter()).build();
                            Operations.complete(operation);

                        } else {
                            System.out.println("Schematic volume doesn't match region volume.");
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
