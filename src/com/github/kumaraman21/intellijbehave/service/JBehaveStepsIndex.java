package com.github.kumaraman21.intellijbehave.service;

import com.github.kumaraman21.intellijbehave.kotlin.KotlinConfigKt;
import com.github.kumaraman21.intellijbehave.parser.JBehaveStep;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.impl.java.stubs.index.JavaFullClassNameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.QualifiedName;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public class JBehaveStepsIndex {
    public static JBehaveStepsIndex getInstance(Project project) {
        return ServiceManager.getService(project, JBehaveStepsIndex.class);
    }

    @NotNull
    public Collection<JavaStepDefinition> findStepDefinitions(@NotNull JBehaveStep step) {
        Module module = ModuleUtilCore.findModuleForPsiElement(step);

        if (module == null) {
            return emptyList();
        }

        Map<Class, JavaStepDefinition> definitionsByClass = new HashMap<Class, JavaStepDefinition>();
        List<JavaStepDefinition> stepDefinitions = loadStepsFor(module);
        String stepText = step.getStepText();

        for (JavaStepDefinition stepDefinition : stepDefinitions) {
            if (stepDefinition.matches(stepText) && stepDefinition.supportsStep(step)) {
                Integer currentHighestPriority = getPriorityByDefinition(definitionsByClass.get(stepDefinition.getClass()));
                Integer newPriority = getPriorityByDefinition(stepDefinition);

                if (newPriority > currentHighestPriority) {
                    definitionsByClass.put(stepDefinition.getClass(), stepDefinition);
                }
            }
        }

        return definitionsByClass.values();
    }

    @NotNull
    private static Integer getPriorityByDefinition(@Nullable JavaStepDefinition definition) {
        if (definition == null) {
            return -1;
        }

        return definition.getAnnotationPriority();
    }

    @NotNull
    public List<JavaStepDefinition> loadStepsFor(@NotNull Module module) {
        GlobalSearchScope dependenciesScope = module.getModuleWithDependenciesAndLibrariesScope(true);

        PsiClass givenAnnotationClass = findStepAnnotation(Given.class.getName(), module, dependenciesScope);
        PsiClass whenAnnotationClass = findStepAnnotation(When.class.getName(), module, dependenciesScope);
        PsiClass thenAnnotationClass = findStepAnnotation(Then.class.getName(), module, dependenciesScope);

        if (givenAnnotationClass == null || whenAnnotationClass == null || thenAnnotationClass == null) {
            return emptyList();
        }

        List<JavaStepDefinition> result = new ArrayList<JavaStepDefinition>();

        List<PsiClass> stepAnnotations = asList(givenAnnotationClass, whenAnnotationClass, thenAnnotationClass);
        for (PsiClass stepAnnotation : stepAnnotations) {
            Collection<PsiAnnotation> allStepAnnotations = getAllStepAnnotations(stepAnnotation, dependenciesScope);

            for (PsiAnnotation stepDefAnnotation : allStepAnnotations) {
                result.add(new JavaStepDefinition(stepDefAnnotation));
            }
        }

        return result;
    }

    @NotNull
    private static Collection<PsiAnnotation> getAllStepAnnotations(@NotNull final PsiClass annClass, @NotNull final GlobalSearchScope scope) {
        return ApplicationManager.getApplication().runReadAction(new Computable<Collection<PsiAnnotation>>() {
            @Override
            public Collection<PsiAnnotation> compute() {
                Project project = annClass.getProject();
                Collection<PsiAnnotation> psiAnnotations = new ArrayList<PsiAnnotation>();
                if (KotlinConfigKt.getPluginIsEnabled()) {
                    String qualifiedName = annClass.getQualifiedName();

                }
                psiAnnotations.addAll(JavaAnnotationIndex.getInstance().get(annClass.getName(), project, scope));
                return psiAnnotations;
            }
        });
    }

    @Nullable
    private PsiClass findStepAnnotation(String stepClass, Module module, GlobalSearchScope dependenciesScope) {
        Collection<PsiClass> stepDefAnnotationCandidates =
                JavaFullClassNameIndex.getInstance().get(stepClass.hashCode(), module.getProject(), dependenciesScope);

        for (PsiClass stepDefAnnotations : stepDefAnnotationCandidates) {
            if (stepClass.equals(stepDefAnnotations.getQualifiedName())) {
                return stepDefAnnotations;
            }
        }

        return null;
    }
}
