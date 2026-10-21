package framework;

import jakarta.servlet.http.HttpSession;

public class MySession {
    private final HttpSession session;

    public MySession(HttpSession session) {
        this.session = session;
    }

    public Object get(String key) {
        return (session != null) ? session.getAttribute(key) : null;
    }

    public void set(String key, Object value) {
        if (session != null) {
            session.setAttribute(key, value);
        }
    }

    public void remove(String key) {
        if (session != null) {
            session.removeAttribute(key);
        }
    }

    public void invalidate() {
        if (session != null) {
            session.invalidate();
        }
    }

    public HttpSession getHttpSession() {
        return session;
    }
}