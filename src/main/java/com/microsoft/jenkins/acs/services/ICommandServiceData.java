/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.jenkins.acs.services;

import java.util.Hashtable;

import com.microsoft.jenkins.acs.commands.IBaseCommandData;
import com.microsoft.jenkins.acs.commands.ICommand;
import com.microsoft.jenkins.acs.commands.TransitionInfo;

public interface ICommandServiceData {
	public Class getStartCommandClass();
	public Hashtable<Class, TransitionInfo> getCommands(); 
	public IBaseCommandData getDataForCommand(ICommand command);
}
