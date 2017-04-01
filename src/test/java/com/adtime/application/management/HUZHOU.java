package com.adtime.application.management;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuanlubin on 2016/12/27.
 */
public class HUZHOU implements ApplicationUpgradeTest.ServerList {

    @Override
    public List<RemoteServer> list() {
        List<RemoteServer> remoteServerList = new ArrayList<>();
        remoteServerList.add(new RemoteServer("172.16.8.27", "appuser", "ad1oor00t"));
        remoteServerList.add(new RemoteServer("172.16.8.28", "appuser", "ad1oor00t"));
        return remoteServerList;
    }

    @Override
    public String remotePath() {
        return "/home/appuser";
    }
}
