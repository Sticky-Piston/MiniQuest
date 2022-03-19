package nl.privateers.miniquest.usecases.repositories;

import nl.privateers.miniquest.domain.Schematic;

import java.util.Optional;

public interface SchematicRetrievable {
    Optional<Schematic> getSchematic(String filename);
}
