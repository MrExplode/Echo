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

package hu.mineside.echo.listeners;

import lombok.val;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

public class MiscListener {

    @SubscribeEvent
    void onGuildMessage(GuildMessageReceivedEvent e) {
        if (e.getMessage().getContentDisplay().toLowerCase().contains("mineside") || e.getMessage().getContentDisplay().toLowerCase().contains("mine side")) {
            //mineside emote
            val emote = e.getGuild().getEmoteById("807426946417819708");
            if (emote != null)
                e.getMessage().addReaction(emote).queue();
        }
    }

    @SubscribeEvent
    void onPrivateMessage(PrivateMessageReceivedEvent e) {
//        if (!e.getAuthor().isBot())
//            e.getChannel().sendMessage(Echo.embed().setTitle("Szia!").build()).queue(__ -> {}, __ ->{});
    }
}
