package de.siv.adoc;

import com.intellij.DynamicBundle;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public class Bundle extends DynamicBundle {
    @NonNls
    private static final String BUNDLE = "messages.Bundle";

    private static final Bundle INSTANCE = new Bundle();

    private Bundle() {
        super(BUNDLE);
    }

    public static String message(@PropertyKey(resourceBundle = BUNDLE) @NotNull String key, Object... params) {
        return INSTANCE.getMessage(key, params);
    }
}