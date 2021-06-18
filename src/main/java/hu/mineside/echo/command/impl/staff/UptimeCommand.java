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

package hu.mineside.echo.command.impl.staff;

import hu.mineside.echo.Echo;
import hu.mineside.echo.GuildType;
import hu.mineside.echo.command.Command;
import hu.mineside.echo.permission.BotPermission;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

import static hu.mineside.echo.Echo.embed;

public class UptimeCommand extends Command {

    public UptimeCommand() {
        super("uptime", "Uptime", "Bot uptime", BotPermission.OPERATOR, GuildType.PUBLIC, GuildType.STAFF, GuildType.DEV);
    }

    @Override
    public void onCommand(Message message, String command, List<String> args) {
        long seconds = (System.currentTimeMillis() - Echo.getInstance().getStartTime()) / 1000;
        message.getChannel().sendMessage(embed().addField("Bot Uptime", String.format("%d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, (seconds % 60)), false).build()).queue();
    }
}
