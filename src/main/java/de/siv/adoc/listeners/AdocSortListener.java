package de.siv.adoc.listeners;

import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;

import de.siv.adoc.service.AdocSortService;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class AdocSortListener implements BulkFileListener {


    @Override
    public void after(@NotNull List<? extends VFileEvent> events) {
        ProjectManager instance = ProjectManager.getInstance();
        for (VFileEvent event : events) {
            Arrays.stream(instance.getOpenProjects()).forEach(project -> project.getService(AdocSortService.class).sortProjectPane(event.getFile()));
        }
    }
}
