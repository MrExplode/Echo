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

package hu.mineside.echo.punishment;

import hu.mineside.echo.Echo;
import hu.mineside.echo.punishment.type.Mute;
import hu.mineside.echo.utils.Utils;
import hu.mineside.echo.utils.sql.HikariConnection;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

@Slf4j
@Getter
public class MuteManager {
    private final List<Mute> mutes = new CopyOnWriteArrayList<>();

    public MuteManager() {
        log.info("Loading active mutes...");
        Echo.getInstance().getJda().addEventListener(this);
        Echo.getInstance().getDiscordDB().query(false,"SELECT `id`, `target`, `punisher`, `executedOn`, `reason`, `until` FROM `discord_mutes` WHERE active = 1")
                .thenAccept(rs -> {
                    try {
                        while (rs.next()) {
                            Mute mute = new Mute(rs.getString("target"), rs.getString("punisher"), rs.getString("reason"), rs.getLong("until"));
                            mute.setActive(true);
                            mute.setId(rs.getInt("id"));
                            mute.setExecutedOn(rs.getString("executedOn"));
                            mute.setUnPunishTask(Echo.getInstance().getScheduler().schedule(() -> {
                                mute.unPunish();
                                log.info("Auto-removed mute for " + Echo.getInstance().getJda().getUserById(mute.getTarget()).getName());
                            }, Duration.between(LocalDateTime.now(), LocalDateTime.ofInstant(Instant.ofEpochMilli(mute.getUntil()), TimeZone.getDefault().toZoneId())).getSeconds(), TimeUnit.SECONDS));
                            mutes.add(mute);
                        }
                    } catch (SQLException e) {
                        log.error("Exception while loading mute", e);
                    }
                });
        log.info("Cached " + mutes.size() + " mutes");
    }

    public void addMute(Mute mute) {
        mutes.add(mute);
        Echo.getInstance().getDiscordDB().query("SELECT `id` FROM `discord_mutes` WHERE `target` = ? AND `punisher` = ? AND `executedOn` = ? AND `until` = ?", mute.getTarget(), mute.getPunisher(), mute.getExecutedOn(), mute.getUntil())
                .thenAccept(rs -> {
                    try {
                        if (rs.next()) {
                            mute.setId(rs.getInt("id"));
                        }
                    } catch (SQLException e) {
                        log.error("Failed to fetch id for mute", e);
                    }
                });
    }

    public void muteExpired(Mute mute) {
        mutes.remove(mute);
    }

    @SubscribeEvent
    void onRankRemove(GuildMemberRoleRemoveEvent e) {
        if (e.getGuild().getRoleById(Mute.MUTE_ROLE_ID) != null && e.getRoles().contains(e.getGuild().getRoleById(Mute.MUTE_ROLE_ID))) {
            Mute mute = mutes.stream().filter(m -> m.getTarget().equals(e.getMember().getId())).findFirst().orElse(null);
            if (mute != null) {
                mute.unPunishBackend();
                if (mute.getUnPunishTask() != null)
                    mute.getUnPunishTask().cancel(true);
                mutes.remove(mute);
                log.info("Someone removed mute rank from " + Utils.combinedName(e.getMember()));
            }
        }
    }
}
