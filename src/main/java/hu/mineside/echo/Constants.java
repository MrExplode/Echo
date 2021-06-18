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

import com.google.gson.Gson;
import lombok.experimental.UtilityClass;
import me.ahornyai.httpclient.HttpClient;

import java.awt.*;

@UtilityClass
public class Constants {
    public final Gson GSON = new Gson();
    public final HttpClient HTTP_CLIENT = new HttpClient("EchoBot");
    public final String COMMAND_PREFIX = "-";

    @UtilityClass
    public class Colors {
        public final Color DEFAULT = new Color(101, 198, 32);
        public final Color WARNING = new Color(245, 203, 0);
        public final Color ERROR = new Color(221, 12, 12);
    }

    @UtilityClass
    public class Ticket {
        public final String CATEGORY_ID = "";
        public final String OPEN_EMOTE = "\uD83D\uDCE9";
        public final String CLOSE_EMOTE = "\uD83D\uDD12";
        public final String REOPEN_EMOTE = "\uD83D\uDD13";
        public final String DELETE_EMOTE = "⛔";
        public final String CONFIRM_EMOTE = "✅";
        public final String DENY_EMOTE = "❎";
    }
}
