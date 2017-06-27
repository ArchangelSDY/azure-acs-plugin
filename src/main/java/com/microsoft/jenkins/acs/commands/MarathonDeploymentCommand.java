/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.jenkins.acs.commands;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.microsoft.jenkins.acs.exceptions.AzureCloudException;
import com.microsoft.jenkins.acs.util.JsonHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

public class MarathonDeploymentCommand implements ICommand<MarathonDeploymentCommand.IMarathonDeploymentCommandData> {
    @Override
    public void execute(MarathonDeploymentCommand.IMarathonDeploymentCommandData context) {
        String host = context.getMgmtFQDN();
        String sshFile = context.getSshKeyFileLocation();
        String filePassword = context.getSshKeyFilePassword();
        String linuxAdminUsername = context.getLinuxAdminUsername();
        String marathonConfigFile = context.getMarathonConfigFile();

        Session session = null;
        try {
            JSch jsch = new JSch();

            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            jsch.addIdentity(sshFile, filePassword);
            session = jsch.getSession(linuxAdminUsername, host, 2200);
            session.setConfig(config);
            session.connect();

            ChannelSftp channel = null;
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            String appId = JsonHelper.getId(marathonConfigFile);
            String deployedFilename = "acsDep" + Calendar.getInstance().getTimeInMillis() + ".json";
            context.logStatus("Copying marathon file to remote file: " + deployedFilename);
            try {
                channel.put(marathonConfigFile, deployedFilename);
            } catch (SftpException e) {
                context.logError("Error creating remote file:", e);
                return;
            }
            channel.disconnect();

            //ignore if app does not exist
            context.logStatus(String.format("Deleting application with appId: '%s' if it exists", appId));
            this.executeCommand(session, "curl -X DELETE http://localhost/marathon/v2/apps/" + appId, context);
            context.logStatus(String.format("Deploying file '%s' with appId to marathon.", deployedFilename, appId));
            this.executeCommand(session, "curl -i -H 'Content-Type: application/json' -d@" + deployedFilename + " http://localhost/marathon/v2/apps", context);
            context.setDeploymentState(DeploymentState.Success);
        } catch (InterruptedException ie) {
            throw new RuntimeException(ie);
        } catch (Exception e) {
            context.logError("Error deploying application to marathon:", e);
        } finally {
            if (session != null) {
                session.disconnect();
            }
        }
    }

    private void executeCommand(Session session, String command, IBaseCommandData context)
            throws IOException, JSchException, AzureCloudException, InterruptedException {
        ChannelExec execChnl = (ChannelExec) session.openChannel("exec");
        execChnl.setCommand(command);

        context.logStatus("==> exec: " + command);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];

        execChnl.connect();
        InputStream in = execChnl.getInputStream();

        try {
            while (true) {
                do {
                    // blocks on IO
                    int len = in.read(buffer, 0, buffer.length);
                    if (len < 0) {
                        break;
                    }
                    output.write(buffer, 0, len);
                } while (in.available() > 0);

                if (execChnl.isClosed()) {
                    if (in.available() > 0) {
                        continue;
                    }
                    if (execChnl.getExitStatus() < 0) {
                        throw new AzureCloudException("Error building or running docker image. Process exected with status: " +
                                execChnl.getExitStatus());
                    }
                    context.logStatus("<== exit status: " + execChnl.getExitStatus());
                    break;
                }
            }
            String serverOutput = output.toString("UTF-8");
            context.logStatus("<== " + serverOutput);
        } finally {
            execChnl.disconnect();
        }
    }

    public interface IMarathonDeploymentCommandData extends IBaseCommandData {
        String getMgmtFQDN();

        String getSshKeyFileLocation();

        String getSshKeyFilePassword();

        String getLinuxAdminUsername();

        String getMarathonConfigFile();
    }
}
