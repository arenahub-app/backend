package com.arenahub.domain.teamformation;

import java.math.BigDecimal;
import java.util.UUID;

public class TeamPlayer {

    private UUID id;
    private UUID teamId;
    private UUID groupId;
    private UUID memberId;
    private UUID guestId;
    private String userName;
    private BigDecimal skill;
    private String position;

    private TeamPlayer() {}

    public static TeamPlayer create(UUID teamId, UUID groupId, UUID memberId,
                                    String userName, BigDecimal skill, String position) {
        TeamPlayer p = new TeamPlayer();
        p.id = UUID.randomUUID();
        p.teamId = teamId;
        p.groupId = groupId;
        p.memberId = memberId;
        p.guestId = null;
        p.userName = userName;
        p.skill = skill;
        p.position = position;
        return p;
    }

    public static TeamPlayer createForGuest(UUID teamId, UUID groupId, UUID guestId,
                                             String userName, BigDecimal skill, String position) {
        TeamPlayer p = new TeamPlayer();
        p.id = UUID.randomUUID();
        p.teamId = teamId;
        p.groupId = groupId;
        p.memberId = null;
        p.guestId = guestId;
        p.userName = userName;
        p.skill = skill;
        p.position = position;
        return p;
    }

    public static TeamPlayer reconstitute(UUID id, UUID teamId, UUID groupId, UUID memberId,
                                          UUID guestId, String userName, BigDecimal skill, String position) {
        TeamPlayer p = new TeamPlayer();
        p.id = id;
        p.teamId = teamId;
        p.groupId = groupId;
        p.memberId = memberId;
        p.guestId = guestId;
        p.userName = userName;
        p.skill = skill;
        p.position = position;
        return p;
    }

    public UUID getId() { return id; }
    public UUID getTeamId() { return teamId; }
    public UUID getGroupId() { return groupId; }
    public UUID getMemberId() { return memberId; }
    public UUID getGuestId() { return guestId; }
    public String getUserName() { return userName; }
    public BigDecimal getSkill() { return skill; }
    public String getPosition() { return position; }
}
