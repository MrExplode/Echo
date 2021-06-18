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

package hu.mineside.echo;

import hu.mineside.echo.command.CommandHandler;
import hu.mineside.echo.config.Config;
import hu.mineside.echo.listeners.MemeListener;
import hu.mineside.echo.listeners.MiscListener;
import hu.mineside.echo.handlers.ConsoleHandler;
import hu.mineside.echo.handlers.PlayerCountHandler;
import hu.mineside.echo.listeners.SwearListener;
import hu.mineside.echo.punishment.MuteManager;
import hu.mineside.echo.utils.JsonLoader;
import hu.mineside.echo.utils.sql.HikariConnection;
import hu.mineside.echo.youtube.YoutubeHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
@Getter
@SuppressWarnings("all")
public class Echo {
    @Getter private static Echo instance;
    private final long startTime;
    private final Config config;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    private JDA jda;
    private final CommandHandler commandHandler;
    private final PlayerCountHandler playerCountHandler;
    private final HikariConnection discordDB;
    private final MuteManager muteManager;
    private final YoutubeHandler youtubeHandler;

    public Echo() {
        startTime = System.currentTimeMillis();
        instance = this;
        log.info("Loading config...");
        config = JsonLoader.loadOrDefault("config.json", Config.class);
        log.info("Loading terminal...");
        new Thread(() -> new ConsoleHandler().start()).start();
        log.info("Starting bot instance...");
        Set<GatewayIntent> intents = EnumSet.of(
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_BANS,
                GatewayIntent.GUILD_EMOJIS,
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.DIRECT_MESSAGES
        );
        val builder = JDABuilder.create(config.getToken(), intents);
        builder.disableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS);
        builder.setEventManager(new AnnotatedEventManager());
        builder.addEventListeners(new MiscListener());
        builder.addEventListeners(new SwearListener());
        builder.addEventListeners(new MemeListener());

        try {
            jda = builder.build().awaitReady();
        } catch (LoginException | InterruptedException e) {
            log.error("Failed to log in" + e);
        }

        commandHandler = new CommandHandler();
        playerCountHandler = new PlayerCountHandler();
        discordDB = new HikariConnection(config.getDiscordDB(), 3);
        muteManager = new MuteManager();
        youtubeHandler = new YoutubeHandler();
        log.info("§aEcho started!");

        Runtime.getRuntime().addShutdownHook(new Thread(this::onShutdown));
    }

    private void onShutdown() {
        log.info("Shutting down JDA...");
        jda.shutdown();
        log.info("Closing Discord DB...");
        discordDB.disconnect();
        log.info("Shutting down YouTube integration...");
        youtubeHandler.shutdown();
    }

    public static EmbedBuilder embed() {
        return new EmbedBuilder().setColor(Constants.Colors.DEFAULT).setFooter("MineSide", "https://cdn.sunstorm.rocks/files/echo/m_icon.png");
    }

    public static void main(String... args) {
        new Echo();
    }
}
