package com.github.kumaraman21.intellijbehave.run.configuration;

import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.application.ApplicationConfigurationType;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.JavaRunConfigurationModule;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.openapi.project.Project;

public class JBehaveRunConfiguration extends ApplicationConfiguration {
    public JBehaveRunConfiguration(String name, Project project, ApplicationConfigurationType applicationConfigurationType) {
        super(name, project, applicationConfigurationType);
    }

    protected JBehaveRunConfiguration(String name, Project project, ConfigurationFactory factory) {
        super(name, project, factory);
    }
}
