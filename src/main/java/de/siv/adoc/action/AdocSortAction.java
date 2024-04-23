package de.siv.adoc.action;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.impl.AbstractProjectViewPane;
import com.intellij.ide.projectView.impl.ProjectViewState;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.ide.util.treeView.AbstractTreeStructure;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

import org.jetbrains.annotations.NotNull;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class AdocSortAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }


        ProjectView instance = ProjectView.getInstance(project);
        AbstractProjectViewPane currentProjectViewPane = instance.getCurrentProjectViewPane();
        currentProjectViewPane.installComparator((o1, o2) -> {
            if (o1 instanceof PsiFileNode && o2 instanceof PsiFileNode) {
                PsiFileNode p1 = (PsiFileNode) o1;
                Integer value1 = parseFile(p1.getValue().getVirtualFile().getPath());
                PsiFileNode p2 = (PsiFileNode) o2;
                Integer value2 = parseFile(p2.getValue().getVirtualFile().getPath());

                if (value1 == null) {
                    return 1;
                }
                if (value2 == null) {
                    return -1;
                }
                return value1.compareTo(value2);

            }
            ProjectViewNode n1 = (ProjectViewNode) o1;
            ProjectViewNode n2 = (ProjectViewNode) o2;
            return n1.getName().compareTo(n2.getName());
        });


        currentProjectViewPane.updateFromRoot(true);
        System.out.println();

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
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return value;
    }
}
