/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.jenkins.acs.commands;

import java.io.IOException;

import com.microsoft.jenkins.acs.exceptions.AzureCloudException;
import com.microsoft.jenkins.acs.util.NetworkResourceProviderHelper;

import com.microsoft.azure.management.network.NetworkResourceProviderClient;
import com.microsoft.windowsazure.exception.ServiceException;

public class GetPublicFQDNCommand implements ICommand<GetPublicFQDNCommand.IGetPublicFQDNCommandData> {
	public void execute(GetPublicFQDNCommand.IGetPublicFQDNCommandData context) {
		try {
	        context.logStatus("Getting management public FQDN.");
			String mgmtFQDN = NetworkResourceProviderHelper.getMgmtPublicIPFQDN(
					context.getNetworkClient(), 
					context.getDnsNamePrefix());
			context.logStatus("Management public FQDN: " + mgmtFQDN);
			context.setMgmtFQDN(mgmtFQDN);
			context.setDeploymentState(DeploymentState.Success);
		} catch (IOException | ServiceException | AzureCloudException e) {
			context.logError("Error deploying marathon service or enabling ports:", e);
		}
    }
	
	public interface IGetPublicFQDNCommandData extends IBaseCommandData {
		public String getDnsNamePrefix();
		public NetworkResourceProviderClient getNetworkClient();
		public void setMgmtFQDN(String mgmtFQDN);
	}
}
