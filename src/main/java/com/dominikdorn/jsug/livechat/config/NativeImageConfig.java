package com.dominikdorn.jsug.livechat.config;

import com.dominikdorn.jsug.livechat.domain.ChatMessage;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration
@ImportRuntimeHints(NativeImageConfig.AppRuntimeHints.class)
public class NativeImageConfig {

    static class AppRuntimeHints implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // Register domain classes for reflection (needed for JDBC mapping)
            hints.reflection().registerType(ChatMessage.class, MemberCategory.values());

            // Register Flyway migration resources
            hints.resources().registerPattern("db/migration/*");
        }
    }
}
