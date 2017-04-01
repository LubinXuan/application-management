package com.adtime.application.management;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuanlubin on 2016/12/27.
 */
public class VPS implements ApplicationUpgradeTest.ServerList {

    @Override
    public List<RemoteServer> list() {
        List<RemoteServer> remoteServerList = new ArrayList<>();
        remoteServerList.add(new RemoteServer("xz2.cncmcc.com", "root", "ad1oor00t", 13099));
        //remoteServerList.add(new RemoteServer("183.134.1.194", "root", "ad1oor00t", 20082));
        return remoteServerList;
    }

    @Override
    public String remotePath() {
        return "/root";
    }
}
