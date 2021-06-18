package hu.mineside.echo.websub.impl;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndLink;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import hu.mineside.echo.websub.Subscriber;
import hu.mineside.echo.websub.Subscription;
import hu.mineside.echo.websub.SubscriptionHandler;
import lombok.Getter;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.eclipse.jetty.server.Server;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Basic {@link Subscriber} implementation. Uses Jetty as an internal webserver
 * for subscriber endpoints. Each {@link Subscription} is registered with an
 * auto-generated ID and verification token.
 *
 * @author Benjamin Erb
 */
public class SubscriberImpl implements Subscriber {
    enum SubscriptionMode {
        SUBSCRIBE,
        UNSUBSCRIBE
    }

    @Getter private final int port;
    @Getter private final String host;
    private final Server server;
    private final SubscriptionHandler subscriptionHandler;
    private final Map<String, Subscription> subscriptions = new HashMap<>();
    private final Set<Map.Entry<URI, String>> subscribeIntents = new HashSet<>();
    private final Set<Map.Entry<URI, String>> unsubscribeIntents = new HashSet<>();

    private final ExecutorService executors = Executors.newFixedThreadPool(16);

    /**
     * Creates a new subscriber.
     *
     * @param host The external hostname of this subscriber
     * @param port The port this subscriber is listening to.
     */
    public SubscriberImpl(String host, int port) {
        this.host = host;
        this.port = port;
        this.server = new Server(port);
        this.subscriptionHandler = new SubscriptionHandlerImpl(this);

        server.setHandler(subscriptionHandler);

        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Subscription subscribe(URI feedTopicUri) {

        SyndFeedInput input = new SyndFeedInput();
        URI hubUri = null;
        SyndFeed feed;
        try {
            feed = input.build(new XmlReader(feedTopicUri.toURL()));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        for (SyndLink s : feed.getLinks()) {
            if (s != null) {
                if (s.getRel().equals("hub")) {
                    hubUri = URI.create(s.getHref());
                    break;
                }
            }
        }

        if (hubUri == null) {
            throw new IllegalArgumentException("Feed does not contain a hub relation");
        }

        Subscription subscription = new SubscriptionImpl(feedTopicUri, hubUri, this);

        //verify intent locally
        addSubscribeIntent(feedTopicUri, subscription.getVerifyToken());
        removeUnsubscribeIntent(subscription.getFeedTopicUri(), subscription.getVerifyToken());

        subscriptions.put(subscription.getInternalId(), subscription);

        //subscribe to hub
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(hubUri);
        post.addHeader("Content-Type", "application/x-www-form-urlencoded");
        List<NameValuePair> params = new ArrayList<>();
        generateParams(params, subscription, SubscriptionMode.SUBSCRIBE);
        try {
            post.setEntity(new UrlEncodedFormEntity(params));
            HttpResponse response = client.execute(post);
            if (response.getStatusLine().getStatusCode() != 204) {
                throw new RuntimeException();
            }
        } catch (Exception e) {
            subscriptions.remove(subscription.getInternalId());
            post.abort();
            throw new RuntimeException(e);
        } finally {
            client.getConnectionManager().shutdown();
        }
        return subscription;
    }

    private void generateParams(List<NameValuePair> params, Subscription subscription, SubscriptionMode mode) {
        params.add(new BasicNameValuePair("hub.callback", host + "/" + subscription.getInternalId()));
        params.add(new BasicNameValuePair("hub.mode", mode.name()));
        params.add(new BasicNameValuePair("hub.topic", subscription.getFeedTopicUri().toString()));
        params.add(new BasicNameValuePair("hub.verify", "sync"));
        params.add(new BasicNameValuePair("hub.verify_token", subscription.getVerifyToken()));
    }

    @Override
    public void unsubscribe(Subscription subscription) {
        removeSubscribeIntent(subscription.getFeedTopicUri(), subscription.getVerifyToken());
        addUnsubscribeIntent(subscription.getFeedTopicUri(), subscription.getVerifyToken());

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(subscription.getHubUri());
        post.addHeader("Content-Type", "application/x-www-form-urlencoded");
        List<NameValuePair> params = new ArrayList<>();
        generateParams(params, subscription, SubscriptionMode.UNSUBSCRIBE);
        try {
            post.setEntity(new UrlEncodedFormEntity(params));
            HttpResponse response = client.execute(post);
            System.out.println(response.getStatusLine().getStatusCode());
        } catch (Exception e) {
            subscriptions.remove(subscription.getInternalId());
            post.abort();
        } finally {
            client.getConnectionManager().shutdown();
            subscriptions.remove(subscription.getInternalId());
        }
    }

    @Override
    public void shutdown() throws Exception {
        server.stop();
    }

    @Override
    public Subscription getSubscriptionById(String id) {
        return subscriptions.get(id);
    }

    @Override
    public void addSubscribeIntent(URI feedTopicUri, String verifyToken) {
        subscribeIntents.add(new AbstractMap.SimpleImmutableEntry<>(feedTopicUri, verifyToken));
    }

    @Override
    public boolean verifySubscribeIntent(URI feedTopicUri, String verifyToken) {
        return subscribeIntents.contains(new AbstractMap.SimpleImmutableEntry<>(feedTopicUri, verifyToken));
    }

    @Override
    public void removeSubscribeIntent(URI feedTopicUri, String verifyToken) {
        subscribeIntents.remove(new AbstractMap.SimpleImmutableEntry<>(feedTopicUri, verifyToken));
    }

    @Override
    public void addUnsubscribeIntent(URI feedTopicUri, String verifyToken) {
        unsubscribeIntents.add(new AbstractMap.SimpleImmutableEntry<>(feedTopicUri, verifyToken));
    }

    @Override
    public boolean verifyUnsubscribeIntent(URI feedTopicUri, String verifyToken) {
        return unsubscribeIntents.contains(new AbstractMap.SimpleImmutableEntry<>(feedTopicUri, verifyToken));
    }

    @Override
    public void removeUnsubscribeIntent(URI feedTopicUri, String verifyToken) {
        unsubscribeIntents.remove(new AbstractMap.SimpleImmutableEntry<>(feedTopicUri, verifyToken));
    }

    @Override
    public void executeCallback(Runnable runnable) {
        executors.execute(runnable);
    }

}
