package de.siv.adoc.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.impl.AbstractProjectViewPane;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import static de.siv.adoc.action.AdocSortAction.TOGGLE_STATE_KEY;

@Service(Service.Level.PROJECT)
public final class AdocSortService {
    private static final Logger cLOGGER = Logger.getInstance(AdocSortService.class);
    private final Project project;
    private final Cache<String, Integer> cache;

    public AdocSortService(Project project) {
        this.project = project;

        this.cache = CacheBuilder.newBuilder().maximumSize(10000).build();
    }

    public void sortProjectPane() {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        FileEditor selectedEditor = fileEditorManager.getSelectedEditor();
        if (selectedEditor != null) {
            VirtualFile file = selectedEditor.getFile();
            sortProjectPane(file);
        }
    }

    public void sortProjectPane(VirtualFile pFile) {
        ProjectView instance = ProjectView.getInstance(this.project);
        AbstractProjectViewPane currentProjectViewPane = instance.getCurrentProjectViewPane();
        if (currentProjectViewPane == null || !pFile.isValid()) {
            return;
        }
        PsiManager psiManager = PsiManager.getInstance(project);
        PsiFile file = psiManager.findFile(pFile);
        cache.invalidateAll();
        if (file != null && file.getParent() != null && file.getParent().getParent() != null) {
            currentProjectViewPane.updateFrom(file.getParent().getParent(), true, true);
        }
    }


    public void sortProjectByAdoc() {
        this.cache.invalidateAll();
        ProjectView instance = ProjectView.getInstance(this.project);
        AbstractProjectViewPane currentProjectViewPane = instance.getCurrentProjectViewPane();

        currentProjectViewPane.installComparator((o1, o2) -> {
            if (PropertiesComponent.getInstance().getBoolean(TOGGLE_STATE_KEY, false)) {
                Integer value1 = null;
                Integer value2 = null;

                if (o1 instanceof PsiFileNode p1) {
                    value1 = parseFile(p1.getValue().getVirtualFile().getPath());
                }
                if (o2 instanceof PsiFileNode p2) {
                    value2 = parseFile(p2.getValue().getVirtualFile().getPath());
                }

                if (o1 instanceof PsiDirectoryNode d1) {
                    if (!(o2 instanceof PsiDirectoryNode) && value2 == null) {
                        return -1;
                    }
                    Optional<? extends AbstractTreeNode<?>> description = getDescription(d1);
                    if (description.isPresent()) {
                        value1 = parseFile(Objects.requireNonNull(((PsiFileNode) description.get()).getVirtualFile()).getPath());
                    }
                }

                if (o2 instanceof PsiDirectoryNode d2) {
                    if (!(o1 instanceof PsiDirectoryNode) && value1 == null) {
                        return 1;
                    }
                    Optional<? extends AbstractTreeNode<?>> description = getDescription(d2);
                    if (description.isPresent()) {
                        value2 = parseFile(Objects.requireNonNull(((PsiFileNode) description.get()).getVirtualFile()).getPath());
                    }
                }


                if (value1 == null && value2 != null) {
                    return 1;
                }
                if (value2 == null && value1 != null) {
                    return -1;
                }
                if (value1 != null && value2 != null) {
                    return value1.compareTo(value2);
                }
            }

            ProjectViewNode<?> n1 = (ProjectViewNode<?>) o1;
            ProjectViewNode<?> n2 = (ProjectViewNode<?>) o2;
            return Objects.requireNonNull(n1.getName()).compareTo(Objects.requireNonNull(n2.getName()));
        });

        currentProjectViewPane.updateFromRoot(true);
    }

    private @NotNull Optional<? extends AbstractTreeNode<?>> getDescription(PsiDirectoryNode d1) {
        return d1.getChildren().stream()
                .filter(c -> c instanceof PsiFileNode)
                .filter(c -> {
                    if (((PsiFileNode) c).getVirtualFile() != null) {
                        return ((PsiFileNode) c).getVirtualFile().getName().replace(".adoc", "").equals(d1.getName());
                    }
                    return false;
                })
                .findFirst();
    }


    private Integer parseFile(String path) {

        Integer returnValue = this.cache.getIfPresent(path);
        if (returnValue == null) {
            String key = ":siv-sortierkennzeichen:";
            String valueForKey = findValueForKey(path, key);
            if (valueForKey == null) {
                return null;
            }
            returnValue = Integer.valueOf(valueForKey);
            cache.put(path, returnValue);
        }
        return returnValue;
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