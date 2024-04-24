package de.siv.adoc.service;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.impl.AbstractProjectViewPane;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

import static de.siv.adoc.action.AdocSortAction.TOGGLE_STATE_KEY;

@Service(Service.Level.PROJECT)
public final class AdocSortService {
    private static final Logger cLOGGER = Logger.getInstance(AdocSortService.class);
    private final Project project;

    public AdocSortService(Project project) {
        this.project = project;
    }

    public void sortProjectByAdoc() {
        ProjectView instance = ProjectView.getInstance(this.project);
        AbstractProjectViewPane currentProjectViewPane = instance.getCurrentProjectViewPane();
        currentProjectViewPane.installComparator((o1, o2) -> {
            if (o1 instanceof PsiFileNode p1 && o2 instanceof PsiFileNode p2 && PropertiesComponent.getInstance().getBoolean(TOGGLE_STATE_KEY, false)) {
                Integer value1 = parseFile(p1.getValue().getVirtualFile().getPath());
                Integer value2 = parseFile(p2.getValue().getVirtualFile().getPath());

                if (value1 == null) {
                    return 1;
                }
                if (value2 == null) {
                    return -1;
                }
                return value1.compareTo(value2);

            }
            ProjectViewNode<?> n1 = (ProjectViewNode<?> ) o1;
            ProjectViewNode<?>  n2 = (ProjectViewNode<?> ) o2;
            return Objects.requireNonNull(n1.getName()).compareTo(Objects.requireNonNull(n2.getName()));
        });

        currentProjectViewPane.updateFromRoot(true);
    }


    private Integer parseFile(String path) {
        String key = ":siv-sortierkennzeichen:";
        String valueForKey = findValueForKey(path, key);
        if (valueForKey == null) {
            return null;
        }
        Integer value = Integer.valueOf(valueForKey);

        return value;
    }

    private String findValueForKey(String filePath, String key) {
        String value = null;
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(filePath));
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith(key)) {
                    // Extract the value after the key
                    value = line.substring(key.length()).trim();
                    break; // Stop searching after finding the key
                }
            }
        } catch (IOException e) {
            cLOGGER.error(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    cLOGGER.error(e);
                }
            }
        }

        return value;
    }
}