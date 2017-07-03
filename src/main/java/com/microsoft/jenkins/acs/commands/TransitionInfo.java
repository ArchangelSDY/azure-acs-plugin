/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.jenkins.acs.commands;

public class TransitionInfo implements INextCommandAware {
    private ICommand<IBaseCommandData> command;
    private Class success;
    private Class fail;

    public ICommand<IBaseCommandData> getCommand() {
        return this.command;
    }

    @Override
    public Class getSuccess() {
        return this.success;
    }

    @Override
    public Class getFail() {
        return this.fail;
    }

    public TransitionInfo(final ICommand command, final Class success, final Class fail) {
        this.command = command;
        this.success = success;
        this.fail = fail;
    }
}
