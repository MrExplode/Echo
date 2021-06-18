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

package hu.mineside.echo.command;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import hu.mineside.echo.Constants;
import hu.mineside.echo.Echo;
import hu.mineside.echo.GuildType;
import hu.mineside.echo.command.impl.player.HelpCommand;
import hu.mineside.echo.command.impl.player.PlayersCommand;
import hu.mineside.echo.command.impl.staff.InfoCommand;
import hu.mineside.echo.command.impl.staff.UptimeCommand;
import hu.mineside.echo.command.impl.staff.punishment.BanCommand;
import hu.mineside.echo.command.impl.staff.punishment.KickCommand;
import hu.mineside.echo.command.impl.staff.punishment.MuteCommand;
import hu.mineside.echo.permission.BotPermission;
import hu.mineside.echo.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static hu.mineside.echo.Echo.embed;

@Slf4j
public class CommandHandler {
    private final Map<String, Command> commands = new HashMap<>();
    private final Cache<String, Boolean> interactionCache = CacheBuilder.newBuilder().expireAfterWrite(3, TimeUnit.SECONDS).build();

    public CommandHandler() {
        Echo.getInstance().getJda().addEventListener(this);
        Echo.getInstance().getScheduler().scheduleAtFixedRate(interactionCache::cleanUp, 1, 1, TimeUnit.SECONDS);
        //staff commands
        registerCommand(new KickCommand());
        registerCommand(new BanCommand());
        registerCommand(new MuteCommand());

        registerCommand(new UptimeCommand());
        registerCommand(new InfoCommand());

        //player commands
        registerCommand(new HelpCommand());
        registerCommand(new PlayersCommand());
    }

    public void registerCommand(Command command) {
        log.debug("Registering command: " + command.getName());
        commands.put(command.getLabel(), command);
    }

    @SubscribeEvent
    void onMessage(GuildMessageReceivedEvent e) {
        Message msg = e.getMessage();
        String content = msg.getContentDisplay();
        GuildType guildType = GuildType.getType(msg.getGuild().getId());
        if (content.startsWith(Constants.COMMAND_PREFIX) && guildType != GuildType.UNKNOWN) {
            content = content.substring(Constants.COMMAND_PREFIX.length());
            //the message contains only the command prefix
            if (content.length() == 0)
                return;
            String cmdLabel = content.split(" ")[0].toLowerCase();
            if (commands.containsKey(cmdLabel)) {
                Command command = commands.get(cmdLabel);
                if (BotPermission.hasPermission(msg.getMember(), command.getPermission())) {
                    //rate limit command usage for players
                    if (!BotPermission.hasPermission(msg.getMember(), BotPermission.MODERATOR)) {
                        if (interactionCache.getIfPresent(msg.getAuthor().getId()) != null) {
                            msg.addReaction("\uD83D\uDE44").queue();
                            interactionCache.put(msg.getAuthor().getId(), true);
                            return;
                        }
                    }
                    //allowed server
                    if (!command.getGuildTypes().contains(guildType)) {
                        msg.getChannel().sendMessage(embed().setColor(Constants.Colors.ERROR).setDescription("Ezt a parancsot nem használhatod ezen a szerveren!").build()).queue();
                        return;
                    }
                    List<String> args;
                    if (content.length() <= cmdLabel.length() + 1)
                        args = Collections.emptyList();
                    else
                        args = Arrays.asList(content.substring(cmdLabel.length() + 1).split(" "));
                    try {
                        //execute command
                        log.info(Utils.combinedName(msg.getMember()) + " issued command: " + cmdLabel);
                        command.onCommand(msg, cmdLabel, args);
                    } catch (Exception ex) {
                        log.error("Failed to handle command: " + cmdLabel + " from " + msg.getMember().getEffectiveName(), ex);
                    }
                } else {
                    msg.addReaction("\uD83D\uDE2C").queue();
                }
            }
        }
    }
}
