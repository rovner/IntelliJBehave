/*
 * Copyright 2011-12 Aman Kumar
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.kumaraman21.intellijbehave.codeInspector;

import com.github.kumaraman21.intellijbehave.highlighter.StorySyntaxHighlighter;
import com.github.kumaraman21.intellijbehave.parser.JBehaveGivenStory;
import com.intellij.codeInspection.*;
import com.intellij.codeInspection.ex.ProblemDescriptorImpl;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.EverythingGlobalScope;
import com.intellij.psi.search.FilenameIndex;
import org.jetbrains.annotations.NotNull;

public class UndefinedGivenStoryInspection extends LocalInspectionTool {

    @NotNull
    @Override
    public String getShortName() {
        return "UndefinedGivenStory";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {

            @Override
            public void visitElement(PsiElement psiElement) {
                super.visitElement(psiElement);

                if (!(psiElement instanceof JBehaveGivenStory)) {
                    return;
                }

                JBehaveGivenStory givenStory = (JBehaveGivenStory) psiElement;

                PsiFile[] psiFiles = FilenameIndex.getFilesByName(givenStory.getProject(), givenStory.getFilename(), EverythingGlobalScope.allScope(givenStory.getProject()));
//                PsiReference[] references = givenStory.getReferences();
//
//                if (references.length != 1 || !(references[0] instanceof StepPsiReference)) {
//                    return;
//                }
                if(psiFiles.length != 1) {
                    return;
                }
//
                PsiFile psiFile = psiFiles[0];
//                StepPsiReference reference = (StepPsiReference) references[0];
//                JavaStepDefinition definition = reference.resolveToDefinition();
//
//                if (definition == null) {
                if (psiFile == null) {
                    holder.registerProblem(givenStory, "Given story <code>#ref</code> is not defined");
                } else {
                    highlightParameters(givenStory, psiFile, holder);
                }
//                } else {
//                    highlightParameters(givenStory, holder);
//                }
            }
        };
    }


    private void highlightParameters(JBehaveGivenStory givenStory, PsiFile psiFile, ProblemsHolder holder) {
        String stepText = givenStory.getPath();

//        String annotationText = javaStepDefinition.getAnnotationTextFor(stepText);
//        ParametrizedString pString = new ParametrizedString(annotationText);

        int offset = givenStory.getPathOffset();
//        for (StringToken token : pString.tokenize(stepText)) {
//            int length = token.getValue().length();
//            if (token.isIdentifier()) {
                registerHiglighting(StorySyntaxHighlighter.TABLE_CELL, givenStory, TextRange.from(offset, stepText.length()), holder);
//            }
//            offset += length;
//        }
    }

    private static void registerHiglighting(TextAttributesKey attributesKey,
                                            JBehaveGivenStory givenStory,
                                            TextRange range,
                                            ProblemsHolder holder) {
        final ProblemDescriptor descriptor = new ProblemDescriptorImpl(
                givenStory, givenStory, "", LocalQuickFix.EMPTY_ARRAY,
                ProblemHighlightType.INFORMATION, false, range, false, null,
                holder.isOnTheFly());
        descriptor.setTextAttributes(attributesKey);
        holder.registerProblem(descriptor);
    }
}

