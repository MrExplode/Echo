/*
 *     Echo - Discord bot
 *     Copyright (C) 2021  SunStorm (aka. MrExplode)
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package hu.mineside.echo.punishment.type;

import hu.mineside.echo.Constants;
import hu.mineside.echo.Echo;
import hu.mineside.echo.GuildType;
import hu.mineside.echo.punishment.Punishment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Getter
@Setter
@RequiredArgsConstructor
public class Mute extends Punishment {
    public static String MUTE_ROLE_ID = "703678844116271205";
    private int id;
    private final String target;
    private final String punisher;
    private String executedOn;
    private final String reason;
    private final long until;
    @Nullable private String removedBy;
    @Nullable private String removedOn;
    private boolean active;
    @Nullable private ScheduledFuture<?> unPunishTask;

    @Override
    public void punish() {
        setExecutedOn(DATE_FORMAT.format(new Date(System.currentTimeMillis())));
        setActive(true);

        Guild guild = Echo.getInstance().getJda().getGuildById(GuildType.PUBLIC.getGuildId());
        if (guild.getMemberById(target) == null) {
            log.warn("User no longer in guild");
            return;
        }

        guild.addRoleToMember(target, guild.getRoleById(MUTE_ROLE_ID)).queue();
        setUnPunishTask(Echo.getInstance().getScheduler().schedule(() -> {
            unPunish();
            log.info("Auto-removed mute for " + Echo.getInstance().getJda().getUserById(target).getName());
        }, Duration.between(LocalDateTime.now(), LocalDateTime.ofInstant(Instant.ofEpochMilli(until), TimeZone.getDefault().toZoneId())).getSeconds(), TimeUnit.SECONDS));

        Echo.getInstance().getDiscordDB().update(false, "INSERT INTO `discord_mutes` (`target`, `punisher`, `executedOn`, `reason`, `until`, `active`) VALUES (?, ?, ?, ?, ?, ?)",
                target, punisher, executedOn, reason, until, active);
        Echo.getInstance().getMuteManager().addMute(this);
    }

    @Override
    public void unPunish() {
        Echo.getInstance().getMuteManager().muteExpired(this);
        Guild guild = Echo.getInstance().getJda().getGuildById(GuildType.PUBLIC.getGuildId());
        guild.removeRoleFromMember(target, guild.getRoleById(MUTE_ROLE_ID)).queue();
        unPunishBackend();
    }

    public void unPunishBackend() {
        setRemovedOn(DATE_FORMAT.format(new Date(System.currentTimeMillis())));
        Echo.getInstance().getDiscordDB().update(false, "UPDATE `discord_mutes` SET `active` = 0, `removedOn` = ?, `removedBy` = ? WHERE `target` = ? AND id = ?",
                getRemovedOn(), getRemovedBy() == null ? Punishment.BOT : getRemovedBy(), target, id);
    }
}
