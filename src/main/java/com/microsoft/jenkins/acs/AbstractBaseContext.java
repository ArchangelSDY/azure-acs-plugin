/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.jenkins.acs;

import java.util.Hashtable;

import com.microsoft.jenkins.acs.services.ICommandServiceData;
import com.microsoft.jenkins.acs.commands.ICommand;
import com.microsoft.jenkins.acs.commands.TransitionInfo;

import com.microsoft.jenkins.acs.commands.DeploymentState;
import com.microsoft.jenkins.acs.commands.IBaseCommandData;

import hudson.model.BuildListener;

public abstract class AbstractBaseContext implements ICommandServiceData {
	private BuildListener listener;
	private DeploymentState deployState = DeploymentState.Unknown;
	private Hashtable<Class, TransitionInfo> commands;
	private Class startCommandClass;
			
	protected void configure(BuildListener listener,
			Hashtable<Class, TransitionInfo> commands,
			Class startCommandClass) {
		this.listener = listener;
		this.commands = commands;
		this.startCommandClass = startCommandClass;
		
	}
	
	public Hashtable<Class, TransitionInfo> getCommands() {
		return commands;
	}
	
	public Class getStartCommandClass() {
		return startCommandClass;
	}
	
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

	public BuildListener getListener() {
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
