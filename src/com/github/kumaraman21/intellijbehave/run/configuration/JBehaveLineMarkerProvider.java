package com.github.kumaraman21.intellijbehave.run.configuration;

import com.google.gson.Gson;
import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.execution.Executor;
import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import org.jbehave.core.model.ExamplesTable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;
import java.util.Map;

import static com.github.kumaraman21.intellijbehave.highlighter.StoryTokenType.EXAMPLE_TYPE;
import static com.github.kumaraman21.intellijbehave.highlighter.StoryTokenType.SCENARIO_TEXT;
import static com.intellij.icons.AllIcons.RunConfigurations.TestState.Run;
import static com.intellij.icons.AllIcons.RunConfigurations.TestState.Run_run;
import static com.intellij.openapi.editor.markup.GutterIconRenderer.Alignment.CENTER;
import static com.intellij.psi.tree.TokenSet.WHITE_SPACE;
import static java.util.Collections.emptyList;

public class JBehaveLineMarkerProvider extends RelatedItemLineMarkerProvider {

    private static final Icon DEBUG = ((IconLoader.CachedImageIcon) IconLoader.getIcon("/actions/startDebugger_dark.png")).scale(0.7f);

    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo> result) {
        if (result.isEmpty()) {
            String path = getPath(element);
            result.add(new JBehaveRelatedItemLineMarkerInfo(
                    element,
                    Run_run,
                    String.format("Run Story \"%s\"", path),
                    handler(DefaultRunExecutor.getRunExecutorInstance(), path)
            ));
        }

        if (element instanceof LeafPsiElement) {
            IElementType elementType = ((LeafPsiElement) element).getElementType();
            String path = getPath(element);
            if (elementType.equals(SCENARIO_TEXT)) {
                String scenarioName = element.getText().trim();
                result.add(new JBehaveRelatedItemLineMarkerInfo(
                        element,
                        Run,
                        String.format("Run Scenario \"%s\"", scenarioName),
                        handler(DefaultRunExecutor.getRunExecutorInstance(), path, scenarioName)
                ));

                result.add(new JBehaveRelatedItemLineMarkerInfo(
                        element,
                        DEBUG,
                        String.format("Debug Scenario \"%s\"", scenarioName),
                        handler(DefaultDebugExecutor.getDebugExecutorInstance(), path, scenarioName)
                ));
                return;
            }

            if (elementType.equals(EXAMPLE_TYPE)) {
                String scenarioName = element.getParent().getParent().getNode().getFirstChildNode().getPsi().getNextSibling().getText().trim();
                ASTNode tableNode = element.getParent().getLastChild().getNode();
                ExamplesTable table = new ExamplesTable(tableNode.getPsi().getText());
                ASTNode[] lineBrakeNodes = tableNode.getChildren(WHITE_SPACE);

                for (int i = 0; i < lineBrakeNodes.length; i++) {
                    ASTNode node = lineBrakeNodes[i];
                    PsiElement el = node.getPsi().getNextSibling();
                    Map<String, String> row = table.getRow(i);
                    String rowJson = new Gson().toJson(row);
                    result.add(new JBehaveRelatedItemLineMarkerInfo(
                            el,
                            Run,
                            String.format("Run Scenario \"%s[%s]\"", scenarioName, row),
                            handler(DefaultRunExecutor.getRunExecutorInstance(), path, scenarioName, rowJson)
                    ));
                    result.add(new JBehaveRelatedItemLineMarkerInfo(
                            el,
                            DEBUG,
                            String.format("Debug Scenario \"%s[%s]\"", scenarioName, row),
                            handler(DefaultDebugExecutor.getDebugExecutorInstance(), path, scenarioName, rowJson)
                    ));
                }
            }
        }
    }

    @NotNull
    private String getPath(@NotNull PsiElement element) {
        return element.getContainingFile().getVirtualFile().getPath();
    }

    private GutterIconNavigationHandler<PsiElement> handler(Executor executor, String... args) {
        return (e, elt) -> {
            RunManagerImpl runManager = (RunManagerImpl) RunManager.getInstance(elt.getProject());
            JBehaveConfigurationType type = new JBehaveConfigurationType();
            StringBuilder sb = new StringBuilder();
            sb.append(args[0]);
            if(args.length > 1) {
                sb.append(": ").append(args[1]);
                if (args.length > 2) {
                    sb.append(" [").append(args[2]).append("]");
                }
            }
            RunnerAndConfigurationSettings runConfiguration = runManager.createRunConfiguration(sb.toString(), type.getConfigurationFactories()[0]);
            ((JBehaveRunConfiguration) runConfiguration.getConfiguration()).setProgramParameters("\"" + String.join("\" \"", args) + "\"");
            ProgramRunnerUtil.executeConfiguration(runConfiguration, executor);
        };
    }

    private class JBehaveRelatedItemLineMarkerInfo extends RelatedItemLineMarkerInfo<PsiElement> {

        JBehaveRelatedItemLineMarkerInfo(PsiElement element, Icon icon,
                                         String tooltip, GutterIconNavigationHandler<PsiElement> navHandler) {
            super(element, element.getTextRange(), icon, Pass.LINE_MARKERS, o -> tooltip, navHandler, CENTER, emptyList());
        }
    }
}
