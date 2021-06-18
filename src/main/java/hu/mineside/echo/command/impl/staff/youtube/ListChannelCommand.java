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

package hu.mineside.echo.command.impl.staff.youtube;

import hu.mineside.echo.GuildType;
import hu.mineside.echo.command.Command;
import hu.mineside.echo.permission.BotPermission;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public class ListChannelCommand extends Command {

    public ListChannelCommand() {
        super("listChannels", "List Channels", "List watched channels", BotPermission.ADMIN, GuildType.STAFF);
    }

    @Override
    public void onCommand(Message message, String command, List<String> args) {

    }
}
