
package com.schwinglive.aerialfishingimprovements;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class MolchPearlChimePluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(MolchPearlChimePlugin.class);
		RuneLite.main(args);
	}
}
