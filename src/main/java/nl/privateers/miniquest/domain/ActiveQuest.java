package nl.privateers.miniquest.domain;

import java.time.LocalDateTime;

public interface ActiveQuest {
    Quest quest();
    LocalDateTime startedAt();
    LocalDateTime endsAt();
    boolean inProgress();
}
