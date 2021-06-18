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

package hu.mineside.echo.command.impl.staff.punishment;

import hu.mineside.echo.Constants;
import hu.mineside.echo.Echo;
import hu.mineside.echo.command.Command;
import hu.mineside.echo.permission.BotPermission;
import hu.mineside.echo.punishment.type.Mute;
import hu.mineside.echo.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import java.util.ArrayList;
import java.util.List;

import static hu.mineside.echo.Echo.embed;

@Slf4j
public class MuteCommand extends Command {

    public MuteCommand() {
        super("mute", "Mute", "Játékos némítása", BotPermission.MODERATOR);
    }

    @Override
    public void onCommand(Message message, String command, List<String> args) {
        args = new ArrayList<>(args);
        //preconditions
        if (args.size() < 2) {
            message.getChannel().sendMessage(embed().setTitle("Helytelen parancs!").setDescription("mute <player> <idő> [indok]").setColor(Constants.Colors.WARNING).build()).queue();
            return;
        }
        if (message.getMentionedMembers().size() != 1) {
            message.getChannel().sendMessage(embed().setTitle("Tageld meg akit némítani akarsz!").setColor(Constants.Colors.WARNING).build()).queue();
            return;
        }
        if (!message.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
            message.getChannel().sendMessage(embed().setTitle("Nincs jogom a rangok kezeléséhez!").setColor(Constants.Colors.ERROR).build()).queue();
            return;
        }
        Member target = message.getMentionedMembers().get(0);
        if (!message.getMember().canInteract(target)) {
            message.getChannel().sendMessage(embed().setTitle("Őt nem némíthatod!").setColor(Constants.Colors.ERROR).build()).queue();
            return;
        }
        if (Echo.getInstance().getMuteManager().getMutes().stream().anyMatch(m -> m.getTarget().equals(target.getId()))) {
            message.getChannel().sendMessage(embed().setTitle(target.getEffectiveName() + " már némítva van!").setColor(Constants.Colors.WARNING).build()).queue();
            return;
        }
        long length = Utils.parseTime(args.get(1));
        if (length == -1) {
            message.getChannel().sendMessage(embed().setTitle("Hibás időformátum!").setColor(Constants.Colors.WARNING).build()).queue();
            return;
        }

        //reason
        String reason = "";
        MessageAction messageAction;
        if (args.size() != 2) {
            args.remove(0);
            args.remove(0);
            reason = String.join(" ", args);
            messageAction = message.getChannel().sendMessage(embed().setTitle(target.getEffectiveName() + " némítva!").setDescription("Indok: " + reason).build()) ;
        } else {
            messageAction = message.getChannel().sendMessage(embed().setTitle(target.getEffectiveName() + " némítva!").build());
        }
        reason = reason.isEmpty() ? "Nincs megadva" : reason;
        //mute
        Mute mute = new Mute(target.getId(), message.getMember().getId(), reason, System.currentTimeMillis() + length);
        mute.punish();
        messageAction.queue();
        log.info((Utils.combinedName(message.getMember()) + " muted " + target.getEffectiveName() + ": " + reason));
    }
}
