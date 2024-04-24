package de.siv.adoc.activity;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import de.siv.adoc.service.AdocSortService;
import org.jetbrains.annotations.NotNull;

public class AdocSortActivity implements StartupActivity {
    @Override
    public void runActivity(@NotNull Project project) {
        project.getService(AdocSortService.class).sortProjectByAdoc();
    }
}
