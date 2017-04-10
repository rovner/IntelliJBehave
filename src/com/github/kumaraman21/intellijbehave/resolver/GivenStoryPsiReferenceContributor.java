package com.github.kumaraman21.intellijbehave.resolver;

import com.github.kumaraman21.intellijbehave.parser.JBehaveGivenStory;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class GivenStoryPsiReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(psiElement(JBehaveGivenStory.class), new JBehaveGivenStoryReferenceProvider());
    }
}
