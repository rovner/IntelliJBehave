package com.github.kumaraman21.intellijbehave.run.configuration;

import com.intellij.execution.application.ApplicationConfigurationType;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class JBehaveConfigurationType extends ApplicationConfigurationType {
    @Nls
    @Override
    public String getDisplayName() {
        return "JBehave Story";
    }

    @Nls
    @Override
    public String getConfigurationTypeDescription() {
        return "Run JBehave Story file";
    }

    @Override
    public Icon getIcon() {
        return IconLoader.getIcon("../../../../../../fileTypes/bdd-jb-orange-red-green.png");
    }

    @NotNull
    @Override
    public String getId() {
        return "jbehave-story";
    }

    @Override
    public ConfigurationFactory[] getConfigurationFactories() {
        ConfigurationFactory factory = new JBehaveConfigurationFactory(this);
        return new ConfigurationFactory[] {factory};
    }
}
