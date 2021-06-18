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

package hu.mineside.echo.youtube;

import hu.mineside.echo.Echo;
import hu.mineside.echo.websub.Subscriber;
import hu.mineside.echo.websub.impl.SubscriberImpl;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;

@Slf4j
public class YoutubeHandler {
    private final long DEFAULT_LEASE = 60 * 60 * 24 * 1000;
    private final Subscriber webSubscriber;

    public YoutubeHandler() {
        log.info("Loading YouTube integration...");
        webSubscriber = new SubscriberImpl("https://youtube.mineside.hu", 7290);
        Echo.getInstance().getDiscordDB().query("SELECT `id`, `last_sub`, `lease` FROM `youtube_watcher`").thenAccept(rs -> {
            try {
                while (rs.next()) {
                    //todo complete
                }
            } catch (SQLException e) {
                log.error("akurvaszádat", e);
            }
        });
    }

    public void shutdown() {
        try {
            webSubscriber.shutdown();
        } catch (Exception e) {
            log.error("Failed to shutdown YouTube integration", e);
        }
    }
}
