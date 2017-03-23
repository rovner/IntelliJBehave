package com.github.kumaraman21.intellijbehave.parser;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

import static org.apache.commons.lang.StringUtils.trim;

/**
 * Created by Stefan on 3/19/2017.
 */
public class JBehaveGivenStory extends ASTWrapperPsiElement {
    public JBehaveGivenStory(@NotNull ASTNode node) {
        super(node);
    }

    public String getFilename() {
        String[] pathElements = getPath().split("/");
        String fileNameWithMeta = pathElements[pathElements.length-1];

        return fileNameWithMeta.split("#")[0];
    }
    
    public String getPath() {
        String path = getText();

        return trim(path);
    }

    public int getPathOffset() {
        return 0;
//        return givenStoriesPrefix.length() + 1;
    }
}
