package com.github.kumaraman21.intellijbehave.service;

import com.github.kumaraman21.intellijbehave.parser.JBehaveGivenStory;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static java.util.Collections.emptyList;

/**
 * Created by Stefan on 3/21/2017.
 */
public class JBehaveGivenStoriesIndex {
    public static JBehaveGivenStoriesIndex getInstance(Project project) {
        return ServiceManager.getService(project, JBehaveGivenStoriesIndex.class);
    }

    @NotNull
    public Collection<String> findGivenStoryDefinitions(@NotNull JBehaveGivenStory givenStory) {
        Module module = ModuleUtilCore.findModuleForPsiElement(givenStory);

        if (module == null) {
            return emptyList();
        }

        return emptyList();
    }
}
