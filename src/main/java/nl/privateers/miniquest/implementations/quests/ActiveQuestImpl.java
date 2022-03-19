package nl.privateers.miniquest.implementations.quests;

import nl.privateers.miniquest.domain.ActiveQuest;
import nl.privateers.miniquest.domain.Quest;

import java.time.LocalDateTime;

public class ActiveQuestImpl implements ActiveQuest {
    private final Quest event;

    private final LocalDateTime startedAt;
    private final LocalDateTime endsAt;

    public ActiveQuestImpl(Quest event, LocalDateTime startedAt, LocalDateTime endsAt) {
        this.event = event;
        this.startedAt = startedAt;
        this.endsAt = endsAt;
    }

    @Override
    public Quest quest() {
        return this.event;
    }

    @Override
    public LocalDateTime startedAt() {
        return this.startedAt;
    }

    @Override
    public LocalDateTime endsAt() {
        return this.endsAt;
    }

    @Override
    public boolean inProgress() {
        return false;
    }
}
