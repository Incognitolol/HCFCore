package rip.alpha.hcf.team.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.mcscrims.libraries.util.BalanceUtil;
import net.mcscrims.libraries.util.CC;
import net.mcscrims.libraries.util.RoundingUtil;
import net.mcscrims.libraries.util.SimpleText;
import net.mcscrims.libraries.util.TimeUtil;
import net.minecraft.util.io.netty.util.internal.ConcurrentSet;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.alpha.hcf.HCF;
import rip.alpha.hcf.profile.TeamProfile;
import rip.alpha.hcf.profile.statistics.ProfileStatTypes;
import rip.alpha.hcf.pvpclass.PvPClass;
import rip.alpha.hcf.pvpclass.impl.MinerClass;
import rip.alpha.hcf.team.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Getter
@Setter
public class PlayerTeam extends Team {

    private final Map<UUID, TeamMember> memberMap;
    private final Set<TeamInviteEntry> inviteEntries;

    private UUID leaderUUID;
    private String leaderName;

    private Location rally;
    private String previousRallyWorldId;

    private String announcement = null;
    private int balance = 0;

    private UUID focusedTeamId;
    private Set<UUID> focusedPlayers = new HashSet<>();

    private int kothsCapped = 0;
    private int points = 0;
    private int invites = HCF.getInstance().getConfiguration().getMaxInvites();

    private double dtr = 1.1;
    private long regen = System.currentTimeMillis(), rallyTime = -1, renameTime = -1;

    public PlayerTeam(UUID id, String name, UUID leaderUUID, String leaderName) {
        super(id, name, CC.YELLOW);

        this.memberMap = new HashMap<>();
        this.inviteEntries = new ConcurrentSet<>();

        this.leaderUUID = leaderUUID;
        this.leaderName = leaderName;
        if (leaderUUID != null) {
            this.addMember(leaderUUID, leaderName, TeamMember.TEAM_LEADER);
        }
    }

    public boolean isFull() {
        return this.memberMap.size() >= HCF.getInstance().getConfiguration().getTeamSize();
    }

    public void addInvite(UUID target) {
        this.getInviteEntries().add(new TeamInviteEntry(target));
    }

    public void removeInvite(UUID target) {
        this.getInviteEntries().iterator().forEachRemaining(teamInviteEntry -> {
            if (teamInviteEntry.getUuid().equals(target)) {
                this.getInviteEntries().remove(teamInviteEntry);
            }
        });
    }

    public void removeInviteCount() {
        if (HCF.getInstance().getConfiguration().isUseMaxInvites() && !HCF.getInstance().getConfiguration().isKitmap()) {
            this.invites--;
        }
    }

    public boolean isFocused(UUID uuid) {
        if (this.focusedPlayers.contains(uuid)) {
            return true;
        }
        if (this.focusedTeamId != null) {
            PlayerTeam focusedTeam = HCF.getInstance().getTeamHandler().getPlayerTeamById(this.focusedTeamId);
            if (focusedTeam != null) {
                return focusedTeam.getMember(uuid) != null;
            } else {
                this.focusedTeamId = null;
            }
        }
        return false;
    }

    public int getAmountInClass(UUID ignore, Class<? extends PvPClass> toCompare) {
        int i = 0;

        for (TeamMember member : this.getOnlineMembers()) {
            if (member.getUuid().equals(ignore)) {
                continue;
            }
            TeamProfile profile = HCF.getInstance().getProfileHandler().getProfile(member.getUuid());
            if (profile == null) {
                continue;
            }
            Class<? extends PvPClass> clazz = profile.getEquipPvPClass();
            if (clazz != null && clazz == toCompare) {
                i++;
            }
        }

        return i;
    }

    public boolean isRaidable() {
        return this.dtr <= 0;
    }

    public void setLeader(UUID uuid, String name) {
        TeamMember newLeader = this.getMember(uuid);
        TeamMember oldLeader = this.getMember(this.leaderUUID);
        if (newLeader == null || oldLeader == null) {
            return;
        }
        this.updateMemberRole(oldLeader, TeamMember.TEAM_COLEADER);
        this.updateMemberRole(newLeader, TeamMember.TEAM_LEADER);
        newLeader.setName(name);
        this.leaderUUID = uuid;
        this.leaderName = name;
    }

    public boolean hasInvite(UUID target) {
        for (TeamInviteEntry entry : this.getInviteEntries()) {
            if (entry.isExpired()) {
                continue;
            }
            if (target.equals(entry.getUuid())) {
                return true;
            }
        }
        return false;
    }

    public void addMember(UUID uuid, TeamMember teamMember) {
        this.memberMap.put(uuid, teamMember);
        HCF.getInstance().getTeamPlayerCache().cacheUUID(uuid, this);
    }

    public void addMember(UUID uuid, String name, int role) {
        if (uuid == null) {
            return;
        }

        if (name == null) {
            return;
        }

        if (role < 0) {
            role = TeamMember.TEAM_MEMBER;
        }

        TeamMember member = new TeamMember(uuid, name);
        member.setRole(role);
        this.addMember(uuid, member);
    }

    public void updateName(UUID uuid, String name) {
        if (this.isLeader(uuid)) {
            this.leaderName = name;
        }
        TeamMember member = this.getMember(uuid);
        member.setName(name);
    }

    public boolean isLeader(UUID uuid) {
        return this.leaderUUID.equals(uuid);
    }

    public void broadcast(String message) {
        this.broadcast("&2[Team]&r ", message, TeamMember.TEAM_MEMBER);
    }

    public void broadcast(String prefix, String message, int requiredRank) {
        message = CC.translate(CC.translate(prefix) + message);

        for (TeamMember member : this.getOnlineMembers()) {
            if (!member.isHigherOrEqual(requiredRank)) {
                continue;
            }
            Player player = member.toPlayer();
            if (player == null) {
                continue;
            }
            ;
            player.sendMessage(message);
        }
    }

    public void updateMemberRole(TeamMember member, int rank) {
        if (member == null) {
            return;
        }
        if (rank > TeamMember.TEAM_LEADER) {
            return;
        }
        if (rank < TeamMember.TEAM_MEMBER) {
            return;
        }
        member.setRole(rank);
    }

    public void promoteMember(TeamMember member) {
        this.updateMemberRole(member, member.getRole() + 1);
    }

    public void demoteMember(TeamMember member) {
        this.updateMemberRole(member, member.getRole() - 1);
    }

    public void addMember(UUID uuid, String name) {
        this.addMember(uuid, name, TeamMember.TEAM_MEMBER);
    }

    public void removeMember(UUID uuid) {
        this.memberMap.remove(uuid);
        HCF.getInstance().getTeamPlayerCache().uncacheUUID(uuid);
    }

    public void setRegen(int hours, int mins, int secs) {
        long time = System.currentTimeMillis();
        if (hours > 0) {
            time += TimeUnit.HOURS.toMillis(hours);
        }
        if (mins > 0) {
            time += TimeUnit.MINUTES.toMillis(mins);
        }
        if (secs > 0) {
            time += TimeUnit.SECONDS.toMillis(secs);
        }
        this.setRegen(time);
    }

    public boolean hasDTRFreeze() {
        return this.regen - System.currentTimeMillis() > 0;
    }

    public boolean hasMaxDTR() {
        return this.dtr >= this.getMaxDTR();
    }

    public void incrementDTR() {
        double maxDTR = this.getMaxDTR();
        if (this.dtr == maxDTR) {
            return;
        }

        if (this.dtr > maxDTR) {
            this.dtr = maxDTR;
            return;
        }

        this.dtr += 0.1;

        if (this.dtr > maxDTR) {
            this.dtr = maxDTR;
            return;
        }

        this.setSave(true);
    }

    public double getMaxDTR() {
        int index = this.getTeamSize();
        double[] maxDTRs = HCF.getInstance().getConfiguration().getMaxDTR();
        if (index <= 1) {
            return maxDTRs[0];
        }
        if (index > maxDTRs.length) {
            return maxDTRs[maxDTRs.length - 1];
        }
        return maxDTRs[index - 1];
    }

    public String getDTRSymbol() {
        if (this.hasDTRFreeze()) {
            return CC.RED + "■";
        }
        if (!this.hasMaxDTR()) {
            return CC.AQUA + "▲";
        }
        return CC.GREEN + "▶";
    }

    public String formatRegen() {
        if (!this.hasDTRFreeze()) {
            return CC.RED + "Team is not on freeze";
        }
        return TimeUtil.formatTime(this.regen - System.currentTimeMillis());
    }

    public boolean isMember(UUID uuid) {
        return this.memberMap.containsKey(uuid);
    }

    public TeamMember getMember(UUID uuid) {
        return this.memberMap.get(uuid);
    }

    public Set<TeamMember> getMembers() {
        return new HashSet<>(this.memberMap.values());
    }

    public Set<TeamMember> getMembersByRole(int role) {
        return this.getMembers().stream().filter(member -> member.isEqual(role)).collect(Collectors.toSet());
    }

    public Set<TeamMember> getOnlineMembers() {
        return this.getMembers().stream().filter(TeamMember::isOnline).collect(Collectors.toSet());
    }

    public boolean isRallyExpired() {
        if (this.rally == null) {
            return true;
        }
        if (this.rallyTime == -1) {
            return true;
        }
        return this.rallyTime - System.currentTimeMillis() < 0;
    }

    public int getTeamSize() {
        return this.memberMap.size();
    }

    public void handleTeamChat(Player player, String message) {
        String prefix = "&2[Team] ";
        String formattedMessage = player.getName() + ": " + CC.YELLOW + message;
        this.broadcast(prefix, formattedMessage, TeamMember.TEAM_MEMBER);
    }

    public void handleCaptainChat(Player player, String message) {
        if (!this.getMember(player.getUniqueId()).isHigherOrEqual(TeamMember.TEAM_CAPTAIN)) {
            player.sendMessage(CC.RED + "You are not a captain in a team.");
            return;
        }

        String prefix = "&b[Captain] ";
        String formattedMessage = player.getName() + ": " + CC.PINK + message;
        this.broadcast(prefix, formattedMessage, TeamMember.TEAM_CAPTAIN);
    }

    @Override
    public void sendTeamInfo(CommandSender sender) {
        List<SimpleText> messages = new ArrayList<>();
        messages.add(new SimpleText("&7&m--------------------------"));

        String teamSizeColor = this.isFull() ? CC.RED : CC.GRAY;
        SimpleText teamSizeText = new SimpleText(teamSizeColor + " [" + this.getOnlineMembers().size() + "/" + this.getTeamSize() + "]");
        if (this.isFull()) {
            teamSizeText.hover(CC.RED + "This team is full.");
        }

        String hqString = this.getHome() == null ? (CC.GRAY + "None") : (CC.GOLD + this.getHome().getBlockX() + ", " + this.getHome().getBlockZ());
        SimpleText infoText = new SimpleText(this.getDisplayName(sender));
        infoText.add(teamSizeText);
        infoText.add(" &7- &eHQ:&f " + hqString);
        messages.add(infoText);

        //Leader message
        messages.add(new SimpleText("&eLeader: " + this.createTeamInfo(TeamMember.TEAM_LEADER)));

        //Other messages
        String message = this.createTeamInfo(TeamMember.TEAM_COLEADER);
        if (!message.isEmpty()) {
            messages.add(new SimpleText("&eCo-Leaders: " + message));
        }
        message = this.createTeamInfo(TeamMember.TEAM_CAPTAIN);
        if (!message.isEmpty()) {
            messages.add(new SimpleText("&eCaptains: " + message));
        }
        message = this.createTeamInfo(TeamMember.TEAM_MEMBER);
        if (!message.isEmpty()) {
            messages.add(new SimpleText("&eMembers: " + message));
        }

        boolean member = true;

        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.isOp() && player.getGameMode() == GameMode.SURVIVAL) {
                member = this.getMember(player.getUniqueId()) != null;
            }
        }

        if (member) {
            messages.add(new SimpleText("&eBalance: &2$&a" + BalanceUtil.formatBalance(this.balance)));

            if (HCF.getInstance().getConfiguration().isUseMaxInvites()) {
                messages.add(new SimpleText("&eInvites left: &6" + this.invites));
            }

            messages.add(new SimpleText("&eKoTH Captures: &6" + this.kothsCapped));
            messages.add(new SimpleText("&ePoints: &6" + this.points));
        }

        if (!HCF.getInstance().getConfiguration().isKitmap()) {
            messages.add(new SimpleText("&eDTR: " + this.getDTRSymbol() + this.getDTRColor() + RoundingUtil.round(this.dtr, 2)));
            if (this.hasDTRFreeze()) {
                messages.add(new SimpleText("&eTime Until Regen: &6" + this.formatRegen()));
            }
        }

        if (this.announcement != null) {
            if (member) {
                messages.add(new SimpleText("&eAnnouncement: &6" + this.announcement));
            }
        }

        messages.add(new SimpleText("&7&m--------------------------"));
        messages.forEach(message1 -> message1.send(sender));
    }

    public String getDTRColor() {
        return this.isRaidable() ? CC.RED : CC.GREEN;
    }

    private String createTeamInfo(int i) {
        StringJoiner builder = new StringJoiner(CC.YELLOW + ", ");

        for (TeamMember member : this.getMembersByRole(i)) {
            TeamProfile teamProfile;

            if (Bukkit.isPrimaryThread()) {
                teamProfile = HCF.getInstance().getProfileHandler().getProfile(member.getUuid());
            } else {
                teamProfile = HCF.getInstance().getProfileHandler().getProfileOrLoad(member.getUuid());
            }

            String color = CC.GREEN;
            int kills = 0;

            if (teamProfile != null) {
                kills = teamProfile.getStat(ProfileStatTypes.KILLS);
                if (!member.isOnline()) {
                    color = CC.GRAY;
                    if (teamProfile.isDeathban()) {
                        color = CC.RED;
                    }
                }
            }

            String statMessage = CC.YELLOW + "[" + CC.GREEN + kills + CC.YELLOW + "]";
            builder.add(color + member.getName() + statMessage);
        }

        return builder.toString();
    }

    @Override
    public String getDisplayName(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            UUID uuid = player.getUniqueId();
            TeamMember member = this.getMember(uuid);
            if (member != null) {
                return CC.DARK_GREEN + this.getName();
            }
        }
        return CC.YELLOW + this.getName();
    }

    public void addPoints(int points) {
        this.points += points;
        this.setSave(true);
    }

    public void removePoints(int points) {
        this.points -= points;

        if (this.points <= 0) {
            this.points = 0;
        }

        this.setSave(true);
    }

    public void fromDocument(Document document) {
        super.fromDocument(document);

        this.memberMap.clear();
        Document membersDocument = (Document) document.get("members");
        membersDocument.forEach((key, value) -> {
            UUID uuid = UUID.fromString(key);
            Document memberDocument = (Document) value;
            TeamMember member = new TeamMember(uuid, null);
            member.fromDocument(memberDocument);
            this.addMember(uuid, member);
        });

        this.leaderUUID = UUID.fromString(document.getString("leaderUUID"));
        this.leaderName = document.getString("leaderName");

        this.regen = document.get("regen", this.regen);
        this.dtr = document.get("dtr", this.dtr);
        this.balance = document.getInteger("balance", this.balance);
        this.kothsCapped = document.getInteger("kothsCapped", this.kothsCapped);
        this.points = document.getInteger("points", this.points);

        if (HCF.getInstance().getConfiguration().isUseMaxInvites()) {
            this.invites = document.getInteger("invites", this.invites);
        }

        if (document.containsKey("announcement")) {
            this.announcement = document.getString("announcement");
        }
    }

    public Document toDocument() {
        Document document = super.toDocument();
        Document membersDocument = new Document();

        this.memberMap.values().iterator().forEachRemaining(teamMember ->
                membersDocument.put(teamMember.getUuid().toString(), teamMember.toDocument()));
        document.put("members", membersDocument);

        document.put("leaderUUID", this.leaderUUID.toString());
        document.put("leaderName", this.leaderName);

        document.put("dtr", this.dtr);
        document.put("regen", this.regen);
        document.put("balance", this.balance);
        document.put("kothsCapped", this.kothsCapped);
        document.put("points", this.points);

        if (HCF.getInstance().getConfiguration().isUseMaxInvites()) {
            document.put("invites", this.invites);
        }

        if (this.announcement != null) {
            document.put("announcement", this.announcement);
        }
        return document;
    }

    @Getter
    @Setter
    public static final class TeamInviteEntry {
        private final UUID uuid;
        private final long timeCreated;

        public TeamInviteEntry(UUID uuid) {
            this.uuid = uuid;
            this.timeCreated = System.currentTimeMillis();
        }

        public boolean isExpired() {
            long currentTime = System.currentTimeMillis();
            return TimeUnit.MILLISECONDS.toMinutes(currentTime - timeCreated) >= 5;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof TeamInviteEntry) {
                TeamInviteEntry entry = (TeamInviteEntry) o;
                return entry.getUuid().equals(this.uuid);
            }
            return false;
        }
    }

    @Getter
    @Setter
    public static final class TeamMember {
        public static final int TEAM_LEADER = 3;
        public static final int TEAM_COLEADER = 2;
        public static final int TEAM_CAPTAIN = 1;
        public static final int TEAM_MEMBER = 0;

        private final UUID uuid;
        private String name;
        private int role;
        private TeamChatMode chatMode;

        public TeamMember(UUID uuid, String name) {
            this.uuid = uuid;
            this.name = name;
            this.role = TEAM_MEMBER;
            this.chatMode = TeamChatMode.PUBLIC;
        }

        public boolean isHigherOrEqual(int other) {
            return this.role >= other;
        }

        public boolean isEqual(int other) {
            return other == this.role;
        }

        public boolean isLowerOrEqual(int other) {
            return this.role <= other;
        }

        public boolean isLower(int other) {
            return this.role < other;
        }

        public boolean isOnline() {
            Player player = Bukkit.getPlayer(this.getUuid());
            return player != null && player.willBeOnline();
        }

        public String getPrefix() {
            if (this.role <= 0) {
                return "";
            }
            StringBuilder role = new StringBuilder();
            for (int i = 0; i < this.role; i++) {
                role.append("*");
            }
            return role.toString();
        }

        public String getRoleNameById() {
            return TeamMember.getRoleNameById(this.role);
        }

        public Player toPlayer() {
            return Bukkit.getPlayer(this.uuid);
        }

        public void fromDocument(Document document) {
            this.name = document.getString("name");
            this.role = document.getInteger("role");
            this.chatMode = TeamChatMode.getById(document.getInteger("chatMode", this.chatMode.id));
        }

        public Document toDocument() {
            Document document = new Document();
            document.put("uuid", this.uuid.toString());
            document.put("name", this.name);
            document.put("role", this.role);
            document.put("chatMode", this.chatMode.id);
            return document;
        }

        public static int getRoleIdByName(String name) {
            switch (name.toLowerCase()) {
                case "leader":
                    return TEAM_LEADER;
                case "co-leader":
                    return TEAM_COLEADER;
                case "captain":
                    return TEAM_CAPTAIN;
                case "member":
                    return TEAM_MEMBER;
                default:
                    return -1;
            }
        }

        public static String getRoleNameById(int role) {
            switch (role) {
                case TEAM_LEADER:
                    return "Leader";
                case TEAM_COLEADER:
                    return "Co-Leader";
                case TEAM_CAPTAIN:
                    return "Captain";
                case TEAM_MEMBER:
                    return "Member";
                default:
                    return null;
            }
        }
    }

    @Getter
    @RequiredArgsConstructor
    public enum TeamChatMode {
        PUBLIC(0, "Public", '!', new String[]{"p", "public", "pubchat"}),
        FACTION(1, "Faction", '@', new String[]{"f", "faction", "fac"}),
        CAPTAIN(2, "Captain", '^', new String[]{"officer", "o", "captain", "cap", "admin", "c"});

        private final int id;
        private final String name;
        private final Character chatPrefix;
        private final String[] commandPrefix;

        private static final Map<Integer, TeamChatMode> ID_TO_VALUES;
        private static final Map<Character, Integer> PREFIX_TO_ID;
        private static final Map<String, Integer> COMMAND_TO_ID;

        static {
            ID_TO_VALUES = new HashMap<>();
            PREFIX_TO_ID = new HashMap<>();
            COMMAND_TO_ID = new HashMap<>();

            for (TeamChatMode value : TeamChatMode.values()) {
                ID_TO_VALUES.put(value.id, value);
                PREFIX_TO_ID.putIfAbsent(value.chatPrefix, value.id);
                for (String commandPrefix : value.commandPrefix) {
                    COMMAND_TO_ID.putIfAbsent(commandPrefix, value.id);
                }
            }
        }

        public static TeamChatMode getById(int id) {
            if (id == -2) {
                return null;
            }
            return ID_TO_VALUES.get(id);
        }

        public static TeamChatMode getByCommandPrefix(String commandPrefix) {
            return getById(COMMAND_TO_ID.getOrDefault(commandPrefix, -1));
        }

        public static TeamChatMode getByChatPrefix(Character character) {
            return getById(PREFIX_TO_ID.getOrDefault(character, -2));
        }
    }
}
