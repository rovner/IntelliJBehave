package com.github.kumaraman21.intellijbehave.resolver;

import com.github.kumaraman21.intellijbehave.parser.JBehaveGivenStory;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.ResolveResult;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Stefan on 3/23/2017.
 */
public class GivenStoryPsiReference implements PsiPolyVariantReference {
    private final JBehaveGivenStory myGivenStory;
    private final TextRange myRange;

    public GivenStoryPsiReference(JBehaveGivenStory element, TextRange range) {
        myGivenStory = element;
        myRange = range;
    }

    @Override
    public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
        return myGivenStory;
    }

    @NotNull
    @Override
    public String getCanonicalText() {
        return myGivenStory.getText();
    }

    @Override
    public JBehaveGivenStory getElement() {
        return myGivenStory;
    }

    @Override
    public TextRange getRangeInElement() {
        return myRange;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return ArrayUtil.EMPTY_OBJECT_ARRAY;
    }

    @Override
    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        return myGivenStory;
    }

    @Override
    public boolean isReferenceTo(PsiElement element) {
        ResolveResult[] resolvedResults = multiResolve(false);

        for (ResolveResult resolveResult : resolvedResults) {
            if (getElement().getManager().areElementsEquivalent(resolveResult.getElement(), element)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isSoft() {
        return false;
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        return ResolveResult.EMPTY_ARRAY;
    }

    @Override
    public PsiElement resolve() {
        ResolveResult[] result = multiResolve(true);
        return result.length == 1 ? result[0].getElement() : null;
    }
}
