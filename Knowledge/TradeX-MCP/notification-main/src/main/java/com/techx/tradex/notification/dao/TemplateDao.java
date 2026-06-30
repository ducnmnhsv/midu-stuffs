package com.techx.tradex.notification.dao;

public interface TemplateDao {
    String getTemplate(String name, String locale, Object data);
}
