package de.siv.adoc.action;


import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareToggleAction;
import com.intellij.openapi.project.Project;

import de.siv.adoc.service.AdocSortService;
import org.jetbrains.annotations.NotNull;


public class AdocSortAction extends DumbAwareToggleAction {
    public static final String TOGGLE_STATE_KEY = "AdocSortAction.ToggleState";

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        sortProject(project);
        super.actionPerformed(e);

    }

    private void sortProject(Project project) {
        project.getService(AdocSortService.class).sortProjectByAdoc();
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return this.isToggled();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        this.setToggled(state);
    }

    private boolean isToggled() {
        return PropertiesComponent.getInstance().getBoolean(TOGGLE_STATE_KEY, false);
    }

    private void setToggled(boolean toggled) {
        PropertiesComponent.getInstance().setValue(TOGGLE_STATE_KEY, toggled, false);
    }

}
