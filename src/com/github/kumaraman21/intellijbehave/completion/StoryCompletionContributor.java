package com.github.kumaraman21.intellijbehave.completion;

import com.github.kumaraman21.intellijbehave.highlighter.StoryTokenType;
import com.github.kumaraman21.intellijbehave.language.StoryFileType;
import com.github.kumaraman21.intellijbehave.parser.JBehaveStep;
import com.github.kumaraman21.intellijbehave.parser.JBehaveGivenStory;
import com.github.kumaraman21.intellijbehave.resolver.GivenStoryPsiReference;
import com.github.kumaraman21.intellijbehave.resolver.StepDefinitionAnnotation;
import com.github.kumaraman21.intellijbehave.resolver.StepDefinitionIterator;
import com.github.kumaraman21.intellijbehave.resolver.StepPsiReference;
import com.github.kumaraman21.intellijbehave.utility.LocalizedStorySupport;
import com.github.kumaraman21.intellijbehave.utility.ParametrizedString;
import com.github.kumaraman21.intellijbehave.utility.ScanUtils;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.*;
import com.intellij.util.Consumer;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.steps.StepType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class StoryCompletionContributor extends CompletionContributor {
    public StoryCompletionContributor() {
    }

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull final CompletionResultSet _result) {
        if (parameters.getCompletionType() == CompletionType.BASIC) {
            String prefix = CompletionUtil.findReferenceOrAlphanumericPrefix(parameters);
            CompletionResultSet result = _result.withPrefixMatcher(prefix);

            LocalizedKeywords keywords = lookupLocalizedKeywords(parameters);
            Consumer<LookupElement> consumer = newConsumer(result);

            addAllKeywords(result.getPrefixMatcher(), consumer, keywords);
            addAllSteps(parameters,
                    result.getPrefixMatcher(),
                    consumer,
                    keywords);
            addAllStories(parameters,
                    result.getPrefixMatcher(),
                    consumer,
                    keywords);
        }
    }

    private LocalizedKeywords lookupLocalizedKeywords(CompletionParameters parameters) {
        String locale = "en";
        ASTNode localeNode = parameters.getOriginalFile().getNode().findChildByType(StoryTokenType.COMMENT_WITH_LOCALE);
        if (localeNode != null) {
            String localeFound = LocalizedStorySupport.checkForLanguageDefinition(localeNode.getText());
            if (localeFound != null) {
                locale = localeFound;
            }
        }
        return new LocalizedStorySupport().getKeywords(locale);
    }

    private static Consumer<LookupElement> newConsumer(final CompletionResultSet result) {
        return new Consumer<LookupElement>() {
            @Override
            public void consume(LookupElement element) {
                 result.addElement(element);
            }
        };
    }

    private static void addAllKeywords(PrefixMatcher prefixMatcher,
                                       Consumer<LookupElement> consumer,
                                       LocalizedKeywords keywords) {
        addIfMatches(consumer, prefixMatcher, keywords.narrative());
        addIfMatches(consumer, prefixMatcher, keywords.asA());
        addIfMatches(consumer, prefixMatcher, keywords.inOrderTo());
        addIfMatches(consumer, prefixMatcher, keywords.iWantTo());
        //
        addIfMatches(consumer, prefixMatcher, keywords.givenStories());
        addIfMatches(consumer, prefixMatcher, keywords.ignorable());
        addIfMatches(consumer, prefixMatcher, keywords.scenario());
        addIfMatches(consumer, prefixMatcher, keywords.examplesTable());
        //
        addIfMatches(consumer, prefixMatcher, keywords.lifecycle());
        addIfMatches(consumer, prefixMatcher, keywords.before());
        addIfMatches(consumer, prefixMatcher, keywords.after());
        addIfMatches(consumer, prefixMatcher, keywords.outcome());
        addIfMatches(consumer, prefixMatcher, keywords.outcomeAny());
        addIfMatches(consumer, prefixMatcher, keywords.outcomeFailure());
        addIfMatches(consumer, prefixMatcher, keywords.outcomeSuccess());
        //
        addIfMatches(consumer, prefixMatcher, keywords.given());
        addIfMatches(consumer, prefixMatcher, keywords.when());
        addIfMatches(consumer, prefixMatcher, keywords.then());
        addIfMatches(consumer, prefixMatcher, keywords.and());
    }

    private static void addIfMatches(Consumer<LookupElement> consumer, PrefixMatcher prefixMatchers, String input) {
        addIfMatches(consumer, prefixMatchers, input, "");
    }

    private static void addIfMatches(Consumer<LookupElement> consumer, PrefixMatcher prefixMatchers, String input, String spacingBefore) {
        if (prefixMatchers.prefixMatches(input)) {
            LookupElementBuilder lookup = LookupElementBuilder.create(input);
            lookup = lookup.withInsertHandler(new InsertHandler<LookupElement>() {
                @Override
                public void handleInsert(InsertionContext context, LookupElement element) {
                    context.getDocument().insertString(context.getStartOffset(), spacingBefore);
                    context.getEditor().getCaretModel().moveToOffset(context.getTailOffset());
                }
            });

            consumer.consume(lookup);
        }
    }

    private static void addAllStories(CompletionParameters parameters,
                                      PrefixMatcher prefixMatcher,
                                      Consumer<LookupElement> consumer,
                                      LocalizedKeywords keywords) {
        JBehaveGivenStory givenStory = getGivenStoryPsiElement(parameters);
        if (givenStory == null) {
            return;
        }

        Module module = ModuleUtilCore.findModuleForPsiElement(givenStory);

        /**
         * Set searchscope
         * If module is null, get project scope, otherwise get module with dependencies and libraries scope (including test)
         */
        GlobalSearchScope searchScope;
        if (module != null) {
            searchScope = module.getModuleWithDependenciesAndLibrariesScope(true);
        } else {
            searchScope = GlobalSearchScope.projectScope(givenStory.getProject());
        }

        /**
         * Find all story files within defined searchScope
         * For this iteration it will just find all stories, without looking at Meta tags or anything
         * Spacing is added before the given story, placing the completion at the start position of
         * the caret. Afterwards the caret is moved to the end of the line, so one can easily type
         * a comma
         */
        Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(StoryFileType.INSTANCE, searchScope);

        for (VirtualFile virtualFile : virtualFiles) {
            String path = virtualFile.getPath();
            String url = virtualFile.getUrl();
            String givenStoryPath;

            if (url.startsWith("file://")) {
                givenStoryPath = path.substring(path.indexOf("resources/") + "resources/".length());
            } else if (url.startsWith("jar://")) {
                givenStoryPath = path.substring(path.indexOf(".jar!/") + ".jar!/".length());
            } else {
                givenStoryPath = "";
            }

            String toMatch = prefixMatcher.getPrefix();
            String toMatchWithoutLeadingSpaces = StringUtil.trimLeading(toMatch);
            String spacingBefore = toMatch.substring(0, (toMatch.length()-toMatchWithoutLeadingSpaces.length()));

            addIfMatches(consumer, prefixMatcher, givenStoryPath, spacingBefore);
        }
    }

    private static void addAllSteps(CompletionParameters parameters,
                                    PrefixMatcher prefixMatcher,
                                    Consumer<LookupElement> consumer,
                                    LocalizedKeywords keywords) {
        JBehaveStep step = getStepPsiElement(parameters);
        if (step == null) {
            return;
        }

        StepType stepType = step.getStepType();
        String actualStepPrefix = step.getActualStepPrefix();
        //
        String textBeforeCaret = CompletionUtil.findReferenceOrAlphanumericPrefix(parameters);

        // suggest only if at least the actualStepPrefix is complete
        if (isStepTypeComplete(keywords, textBeforeCaret)) {
            StepSuggester stepAnnotationFinder = new StepSuggester(prefixMatcher,
                    stepType,
                    actualStepPrefix,
                    textBeforeCaret,
                    consumer,
                    step.getProject());
            ScanUtils.iterateInContextOf(step, stepAnnotationFinder);
        }
    }

    private static boolean isStepTypeComplete(LocalizedKeywords keywords, String input) {
        return input.startsWith(keywords.given())
                || input.startsWith(keywords.when())
                || input.startsWith(keywords.then())
                || input.startsWith(keywords.and());
    }

    private static JBehaveGivenStory getGivenStoryPsiElement(CompletionParameters parameters) {
        PsiElement position = parameters.getPosition();
        PsiElement positionParent = position.getParent();
        if (positionParent instanceof JBehaveGivenStory) {
            return (JBehaveGivenStory) positionParent;
        } else if (position instanceof GivenStoryPsiReference) {
            return ((GivenStoryPsiReference) position).getElement();
        } else if (position instanceof JBehaveGivenStory) {
            return (JBehaveGivenStory) position;
        } else {
            return null;
        }
    }

    private static JBehaveStep getStepPsiElement(CompletionParameters parameters) {
        PsiElement position = parameters.getPosition();
        PsiElement positionParent = position.getParent();
        if (positionParent instanceof JBehaveStep) {
            return (JBehaveStep) positionParent;
        } else if (position instanceof StepPsiReference) {
            return ((StepPsiReference) position).getElement();
        } else if (position instanceof JBehaveStep) {
            return (JBehaveStep) position;
        } else {
            return null;
        }
    }

    private static class StepSuggester extends StepDefinitionIterator {

        private final PrefixMatcher prefixMatcher;
        private final String actualStepPrefix;
        private final String textBeforeCaret;
        private final Consumer<LookupElement> consumer;

        private StepSuggester(PrefixMatcher prefixMatcher,
                              StepType stepType,
                              String actualStepPrefix,
                              String textBeforeCaret,
                              Consumer<LookupElement> consumer,
                              Project project) {
            super(stepType, project);
            this.prefixMatcher = prefixMatcher;
            this.actualStepPrefix = actualStepPrefix;
            this.textBeforeCaret = textBeforeCaret;
            this.consumer = consumer;
        }

        @Override
        public boolean processStepDefinition(StepDefinitionAnnotation stepDefinitionAnnotation) {
            StepType annotationStepType = stepDefinitionAnnotation.getStepType();
            if (annotationStepType != getStepType()) {
                return true;
            }
            String annotationText = stepDefinitionAnnotation.getAnnotationText();
            String adjustedAnnotationText = actualStepPrefix + " " + annotationText;

            ParametrizedString pString = new ParametrizedString(adjustedAnnotationText);
            String complete = pString.complete(textBeforeCaret);
            if (StringUtil.isNotEmpty(complete)) {
                PsiAnnotation matchingAnnotation = stepDefinitionAnnotation.getAnnotation();
                consumer.consume(LookupElementBuilder.create(matchingAnnotation, textBeforeCaret + complete));
            } else if (prefixMatcher.prefixMatches(adjustedAnnotationText)) {
                PsiAnnotation matchingAnnotation = stepDefinitionAnnotation.getAnnotation();
                consumer.consume(LookupElementBuilder.create(matchingAnnotation, adjustedAnnotationText));
            }
            return true;
        }
    }
}
