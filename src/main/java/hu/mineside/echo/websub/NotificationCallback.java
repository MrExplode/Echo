package hu.mineside.echo.websub;

import com.rometools.rome.feed.synd.SyndFeed;

@FunctionalInterface
public interface NotificationCallback {
    /**
     * The handle method is executed each time the original feed provides
     * updates. The parameter contains a {@link SyndFeed} with all new entries.
     *
     * @param feed
     */
    void handle(SyndFeed feed);
}
