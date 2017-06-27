/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.jenkins.acs;

import com.microsoft.jenkins.acs.commands.DeploymentState;
import com.microsoft.jenkins.acs.commands.IBaseCommandData;
import com.microsoft.jenkins.acs.commands.ICommand;
import com.microsoft.jenkins.acs.commands.TransitionInfo;
import com.microsoft.jenkins.acs.services.ICommandServiceData;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;

import javax.annotation.Nonnull;
import java.util.Hashtable;

public abstract class AbstractBaseContext implements ICommandServiceData {
    private transient Run<?, ?> run;
    private transient FilePath workspace;
    private transient Launcher launcher;
    private transient TaskListener listener;
    private transient DeploymentState deployState = DeploymentState.Unknown;
    private transient Hashtable<Class, TransitionInfo> commands;
    private transient Class startCommandClass;

    protected void configure(
            @Nonnull final Run<?, ?> run,
            @Nonnull final FilePath workspace,
            @Nonnull final Launcher launcher,
            @Nonnull final TaskListener listener,
            Hashtable<Class, TransitionInfo> commands,
            Class startCommandClass) {
        this.run = run;
        this.workspace = workspace;
        this.launcher = launcher;
        this.listener = listener;
        this.commands = commands;
        this.startCommandClass = startCommandClass;
    }

    @Override
    public Hashtable<Class, TransitionInfo> getCommands() {
        return commands;
    }

    @Override
    public Class getStartCommandClass() {
        return startCommandClass;
    }

    @Override
    public abstract IBaseCommandData getDataForCommand(ICommand command);

    public void setDeploymentState(DeploymentState deployState) {
        this.deployState = deployState;
    }

    public DeploymentState getDeploymentState() {
        return this.deployState;
    }

    public boolean getHasError() {
        return this.deployState.equals(DeploymentState.HasError);
    }

    public boolean getIsFinished() {
        return this.deployState.equals(DeploymentState.HasError) ||
                this.deployState.equals(DeploymentState.Done);
    }

    public Run<?, ?> getRun() {
        return run;
    }

    public FilePath getWorkspace() {
        return workspace;
    }

    public Launcher getLauncher() {
        return launcher;
    }

    public TaskListener getListener() {
        return this.listener;
    }

    public void logStatus(String status) {
        listener.getLogger().println(status);
    }

    public void logError(Exception ex) {
        this.logError("Error: ", ex);
    }

    public void logError(String prefix, Exception ex) {
        this.listener.error(prefix + ex.getMessage());
        ex.printStackTrace();
        this.deployState = DeploymentState.HasError;
    }

    public void logError(String message) {
        this.listener.error(message);
        this.deployState = DeploymentState.HasError;
    }
}
