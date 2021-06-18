package hu.mineside.echo.websub;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Handler;

/**
 * A handler interface for subscriptions. This handler will be used for
 * processing incoming notifications by the hub.
 *
 * @author Benjamin Erb
 */
public interface SubscriptionHandler extends Handler {
    /**
     * Process a verification request by the hub.
     *
     * @param request
     * @param response
     * @param subscription
     * @throws IOException
     * @throws ServletException
     */
    public void handleVerify(HttpServletRequest request, HttpServletResponse response, Subscription subscription) throws IOException, ServletException;

    /**
     * Process a notification request by the hub.
     *
     * @param request
     * @param response
     * @param subscription
     * @throws IOException
     * @throws ServletException
     */
    public void handleNotify(HttpServletRequest request, HttpServletResponse response, Subscription subscription) throws IOException, ServletException;
}
