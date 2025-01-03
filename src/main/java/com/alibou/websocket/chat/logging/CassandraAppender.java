package com.alibou.websocket.chat.logging;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;

import java.io.Serializable;

@Plugin(name = "CassandraAppender", category = "Core", elementType = Appender.ELEMENT_TYPE, printObject = true)
public class CassandraAppender extends AbstractAppender {

    private final CqlSession session;
    private final String insertQuery;

    // Constructor
    protected CassandraAppender(String name, Layout<? extends Serializable> layout, CqlSession session,
            String insertQuery) {
        super(name, null, layout);
        this.session = session;
        this.insertQuery = insertQuery;
    }

    @Override
    public void append(LogEvent event) {
        try {

            // Log de debug pour vérifier si cette méthode est appelée
            System.out.println("Appel de l'Appender Cassandra : " + event.getMessage().getFormattedMessage());

            // Préparez l'insertion du log dans la table Cassandra
            String logMessage = new String(getLayout().toByteArray(event));
            session.execute(insertQuery, logMessage, event.getLevel().toString(), event.getLoggerName());
        } catch (Exception e) {
            throw new AppenderLoggingException("Error writing log to Cassandra", e);
        }
    }

    // Plugin Factory pour la configuration dans le fichier XML
    @PluginFactory
    public static CassandraAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginAttribute("session") CqlSession session,
            @PluginAttribute("insertQuery") String insertQuery) {

        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }

        return new CassandraAppender(name, layout, session, insertQuery);
    }
}
