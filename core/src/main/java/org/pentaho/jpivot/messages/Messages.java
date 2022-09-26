package org.pentaho.jpivot.messages;

import org.pentaho.platform.util.messages.MessagesBase;

import java.util.concurrent.Callable;

/**
 * @author Petr Procházka (petrprochy)
 * @since 6.1.0.1-17
 */
public class Messages extends MessagesBase {
  private static final Messages instance = new Messages();

  public static Messages getInstance() {
    return instance;
  }


  @Override
  public String getString(String key) {
    return fallback(key);
  }

  @Override
  public String getString(String key, Object... params) {
    return fallback(key, params);
  }

  @Override
  public String getErrorString(String key) {
    return fallbackError(key);
  }

  @Override
  public String getErrorString(String key, Object... params) {
    return fallbackError(key, params);
  }

  private String fallback(String key) {
    final String value = getOrNull(key, () -> super.getString(key));
    return value != null ? value : org.pentaho.platform.web.servlet.messages.Messages.getInstance().getString(key);
  }

  private String fallback(String key, Object... params) {
    final String value = getOrNull(key, () -> super.getString(key, params));
    return value != null ? value : org.pentaho.platform.web.servlet.messages.Messages.getInstance().getString(key, params);
  }

  private String fallbackError(String key) {
    final String value = getOrNull(key, () -> super.getErrorString(key));
    return value != null ? value : org.pentaho.platform.web.servlet.messages.Messages.getInstance().getErrorString(key);
  }

  private String fallbackError(String key, Object... params) {
    final String value = getOrNull(key, () -> super.getErrorString(key, params));
    return value != null ? value : org.pentaho.platform.web.servlet.messages.Messages.getInstance().getErrorString(key, params);
  }

  private String getOrNull(String key, Callable<String> callable) {
    try {
      return getBundle().containsKey(key) ? callable.call() : null;
    } catch (Exception e) {
      return null;
    }
  }
}
