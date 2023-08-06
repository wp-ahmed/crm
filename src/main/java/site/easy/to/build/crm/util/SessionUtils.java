package site.easy.to.build.crm.util;

import jakarta.servlet.http.HttpSession;

public class SessionUtils {

    public static <T> T getSessionAttribute(HttpSession session, String attributeName, Class<T> wrapperClass) {
        Object attribute = session.getAttribute(attributeName);
        if (wrapperClass.isInstance(attribute)) {
            return wrapperClass.cast(attribute);
        }
        return null;
    }

}