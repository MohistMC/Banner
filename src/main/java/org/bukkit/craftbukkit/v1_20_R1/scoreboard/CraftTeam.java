package org.bukkit.craftbukkit.v1_20_R1.scoreboard;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.minecraft.world.scores.PlayerTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Team;

final class CraftTeam extends CraftScoreboardComponent implements Team {
    private final PlayerTeam team;

    CraftTeam(CraftScoreboard scoreboard, PlayerTeam team) {
        super(scoreboard);
        this.team = team;
    }

    @Override
    public String getName() throws IllegalStateException {
        checkState();

        return team.getName();
    }

    @Override
    public String getDisplayName() throws IllegalStateException {
        checkState();

        return CraftChatMessage.fromComponent(team.getDisplayName());
    }

    @Override
    public void setDisplayName(String displayName) throws IllegalStateException {
        Preconditions.checkArgument(displayName != null, "Display name cannot be null");
        int lengthStripedDisplayName = ChatColor.stripColor(displayName).length();
        Preconditions.checkArgument(lengthStripedDisplayName <= 128, "Display name '%s' is longer than the limit of 128 characters (%s)", displayName, lengthStripedDisplayName);
        checkState();

        team.setDisplayName(CraftChatMessage.fromString(displayName)[0]); // SPIGOT-4112: not nullable
    }

    @Override
    public String getPrefix() throws IllegalStateException {
        checkState();

        return CraftChatMessage.fromComponent(team.getPlayerPrefix());
    }

    @Override
    public void setPrefix(String prefix) throws IllegalStateException, IllegalArgumentException {
        Preconditions.checkArgument(prefix != null, "Prefix cannot be null");
        int lengthStripedPrefix = ChatColor.stripColor(prefix).length();
        Preconditions.checkArgument(lengthStripedPrefix <= 64, "Prefix '%s' is longer than the limit of 64 characters (%s)", prefix, lengthStripedPrefix);
        checkState();

        team.setPlayerPrefix(CraftChatMessage.fromStringOrNull(prefix));
    }

    @Override
    public String getSuffix() throws IllegalStateException {
        checkState();

        return CraftChatMessage.fromComponent(team.getPlayerSuffix());
    }

    @Override
    public void setSuffix(String suffix) throws IllegalStateException, IllegalArgumentException {
        Preconditions.checkArgument(suffix != null, "Suffix cannot be null");
        int lengthStripedSuffix = ChatColor.stripColor(suffix).length();
        Preconditions.checkArgument(lengthStripedSuffix <= 64, "Suffix '%s' is longer than the limit of 64 characters (%s)", suffix, lengthStripedSuffix);

        team.setPlayerSuffix(CraftChatMessage.fromStringOrNull(suffix));
    }

    @Override
    public ChatColor getColor() throws IllegalStateException {
        checkState();

        return CraftChatMessage.getColor(team.getColor());
    }

    @Override
    public void setColor(ChatColor color) {
        Preconditions.checkArgument(color != null, "Color cannot be null");
        checkState();

        team.setColor(CraftChatMessage.getColor(color));
    }

    @Override
    public boolean allowFriendlyFire() throws IllegalStateException {
        checkState();

        return team.isAllowFriendlyFire();
    }

    @Override
    public void setAllowFriendlyFire(boolean enabled) throws IllegalStateException {
        checkState();

        team.setAllowFriendlyFire(enabled);
    }

    @Override
    public boolean canSeeFriendlyInvisibles() throws IllegalStateException {
        checkState();

        return team.canSeeFriendlyInvisibles();
    }

    @Override
    public void setCanSeeFriendlyInvisibles(boolean enabled) throws IllegalStateException {
        checkState();

        team.setSeeFriendlyInvisibles(enabled);
    }

    @Override
    public NameTagVisibility getNameTagVisibility() throws IllegalArgumentException {
        checkState();

        return notchToBukkit(team.getNameTagVisibility());
    }

    @Override
    public void setNameTagVisibility(NameTagVisibility visibility) throws IllegalArgumentException {
        checkState();

        team.setNameTagVisibility(bukkitToNotch(visibility));
    }

    @Override
    public Set<OfflinePlayer> getPlayers() throws IllegalStateException {
        checkState();

        ImmutableSet.Builder<OfflinePlayer> players = ImmutableSet.builder();
        for (String playerName : team.getPlayers()) {
            players.add(Bukkit.getOfflinePlayer(playerName));
        }
        return players.build();
    }

    @Override
    public Set<String> getEntries() throws IllegalStateException {
        checkState();

        ImmutableSet.Builder<String> entries = ImmutableSet.builder();
        for (String playerName : team.getPlayers()) {
            entries.add(playerName);
        }
        return entries.build();
    }

    @Override
    public int getSize() throws IllegalStateException {
        checkState();

        return team.getPlayers().size();
    }

    @Override
    public void addPlayer(OfflinePlayer player) throws IllegalStateException, IllegalArgumentException {
        Preconditions.checkArgument(player != null, "OfflinePlayer cannot be null");
        addEntry(player.getName());
    }

    @Override
    public void addEntry(String entry) throws IllegalStateException, IllegalArgumentException {
        Preconditions.checkArgument(entry != null, "Entry cannot be null");
        CraftScoreboard scoreboard = checkState();

        scoreboard.board.addPlayerToTeam(entry, team);
    }

    @Override
    public boolean removePlayer(OfflinePlayer player) throws IllegalStateException, IllegalArgumentException {
        Preconditions.checkArgument(player != null, "OfflinePlayer cannot be null");
        return removeEntry(player.getName());
    }

    @Override
    public boolean removeEntry(String entry) throws IllegalStateException, IllegalArgumentException {
        Preconditions.checkArgument(entry != null, "Entry cannot be null");
        CraftScoreboard scoreboard = checkState();

        if (!team.getPlayers().contains(entry)) {
            return false;
        }

        scoreboard.board.removePlayerFromTeam(entry, team);
        return true;
    }

    @Override
    public boolean hasPlayer(OfflinePlayer player) throws IllegalArgumentException, IllegalStateException {
        Preconditions.checkArgument(player != null, "OfflinePlayer cannot be null");
        return hasEntry(player.getName());
    }

    @Override
    public boolean hasEntry(String entry) throws IllegalArgumentException, IllegalStateException {
        Preconditions.checkArgument(entry != null, "Entry cannot be null");
        checkState();

        return team.getPlayers().contains(entry);
    }

    @Override
    public void unregister() throws IllegalStateException {
        CraftScoreboard scoreboard = checkState();

        scoreboard.board.removePlayerTeam(team);
    }

    @Override
    public OptionStatus getOption(Option option) throws IllegalStateException {
        checkState();

        switch (option) {
            case NAME_TAG_VISIBILITY:
                return OptionStatus.values()[team.getNameTagVisibility().ordinal()];
            case DEATH_MESSAGE_VISIBILITY:
                return OptionStatus.values()[team.getDeathMessageVisibility().ordinal()];
            case COLLISION_RULE:
                return OptionStatus.values()[team.getCollisionRule().ordinal()];
            default:
                throw new IllegalArgumentException("Unrecognised option " + option);
        }
    }

    @Override
    public void setOption(Option option, OptionStatus status) throws IllegalStateException {
        checkState();

        switch (option) {
            case NAME_TAG_VISIBILITY:
                team.setNameTagVisibility(net.minecraft.world.scores.Team.Visibility.values()[status.ordinal()]);
                break;
            case DEATH_MESSAGE_VISIBILITY:
                team.setDeathMessageVisibility(net.minecraft.world.scores.Team.Visibility.values()[status.ordinal()]);
                break;
            case COLLISION_RULE:
                team.setCollisionRule(net.minecraft.world.scores.Team.CollisionRule.values()[status.ordinal()]);
                break;
            default:
                throw new IllegalArgumentException("Unrecognised option " + option);
        }
    }

    public static net.minecraft.world.scores.Team.Visibility bukkitToNotch(NameTagVisibility visibility) {
        switch (visibility) {
            case ALWAYS:
                return net.minecraft.world.scores.Team.Visibility.ALWAYS;
            case NEVER:
                return net.minecraft.world.scores.Team.Visibility.NEVER;
            case HIDE_FOR_OTHER_TEAMS:
                return net.minecraft.world.scores.Team.Visibility.HIDE_FOR_OTHER_TEAMS;
            case HIDE_FOR_OWN_TEAM:
                return net.minecraft.world.scores.Team.Visibility.HIDE_FOR_OWN_TEAM;
            default:
                throw new IllegalArgumentException("Unknown visibility level " + visibility);
        }
    }

    public static NameTagVisibility notchToBukkit(net.minecraft.world.scores.Team.Visibility visibility) {
        switch (visibility) {
            case ALWAYS:
                return NameTagVisibility.ALWAYS;
            case NEVER:
                return NameTagVisibility.NEVER;
            case HIDE_FOR_OTHER_TEAMS:
                return NameTagVisibility.HIDE_FOR_OTHER_TEAMS;
            case HIDE_FOR_OWN_TEAM:
                return NameTagVisibility.HIDE_FOR_OWN_TEAM;
            default:
                throw new IllegalArgumentException("Unknown visibility level " + visibility);
        }
    }

    @Override
    CraftScoreboard checkState() throws IllegalStateException {
        Preconditions.checkState(getScoreboard().board.getPlayerTeam(team.getName()) != null, "Unregistered scoreboard component");

        return getScoreboard();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + (this.team != null ? this.team.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CraftTeam other = (CraftTeam) obj;
        return !(this.team != other.team && (this.team == null || !this.team.equals(other.team)));
    }

}