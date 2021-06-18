package hu.mineside.echo.websub.impl;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import hu.mineside.echo.websub.Subscriber;
import hu.mineside.echo.websub.Subscription;
import hu.mineside.echo.websub.SubscriptionHandler;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.net.URI;

/**
 * Basic {@link SubscriptionHandler} implementation. Incoming requests will be
 * checked and forwarded to the appropriate processing methods.
 *
 * @author Benjamin Erb
 */
public class SubscriptionHandlerImpl extends AbstractHandler implements SubscriptionHandler {
    private final Subscriber subscriber;

    /**
     * Creates a new {@link SubscriptionHandler}.
     *
     * @param subscriber
     */
    public SubscriptionHandlerImpl(Subscriber subscriber) {
        this.subscriber = subscriber;
    }

    @Override
    public void handleNotify(HttpServletRequest request, HttpServletResponse response, final Subscription subscription) {
        try {
            InputSource source = new InputSource(request.getInputStream());
            SyndFeedInput feedInput = new SyndFeedInput();
            feedInput.setPreserveWireFeed(true);
            final SyndFeed feed;
            feed = feedInput.build(source);
            subscriber.executeCallback(() -> {
                if (subscription.getNotificationCallback() != null) {
                    subscription.getNotificationCallback().handle(feed);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            response.setStatus(200);
        }

    }

    @Override
    public void handleVerify(HttpServletRequest request, HttpServletResponse response, Subscription subscription) {
        if (request.getParameter("hub.mode") != null && request.getParameter("hub.topic") != null && request.getParameter("hub.challenge") != null && request.getParameter("hub.verify_token") != null) {
            URI feedTopicUri = URI.create(request.getParameter("hub.topic"));
            if (request.getParameter("hub.mode").equals("subscribe")) {
                if (subscriber.verifySubscribeIntent(feedTopicUri, request.getParameter("hub.verify_token"))) {
                    response.setStatus(200);
                    response.setContentType("text/plain");
                    try {
                        response.getWriter().write(request.getParameter("hub.challenge"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    response.setStatus(404);
                }
            } else if (request.getParameter("hub.mode").equals("unsubscribe")) {
                if (subscriber.verifyUnsubscribeIntent(feedTopicUri, request.getParameter("hub.verify_token"))) {
                    response.setStatus(200);
                    response.setContentType("text/plain");
                    try {
                        response.getWriter().write(request.getParameter("hub.challenge"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    response.setStatus(404);
                }
            }

        } else {
            response.setStatus(400);
        }

    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
        Subscription subscription = subscriber.getSubscriptionById(target.substring(1));
        if (null != subscription) {
            if (request.getMethod().equals("GET")) {
                handleVerify(request, response, subscription);
            } else if (request.getMethod().equals("POST")) {
                handleNotify(request, response, subscription);
            } else {
                response.setStatus(405);
            }
            baseRequest.setHandled(true);
        }
    }

}
