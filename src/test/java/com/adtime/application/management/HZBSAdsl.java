package com.adtime.application.management;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuanlubin on 2016/12/27.
 */
public class HZBSAdsl implements ApplicationUpgradeTest.ServerList {

    @Override
    public List<RemoteServer> list() {
        List<RemoteServer> remoteServerList = new ArrayList<>();
        try {
            InputStream is = HZBSAdsl.class.getClassLoader().getResourceAsStream("server_list.txt");
            List<String> ipList = IOUtils.readLines(is, Charset.forName("utf-8"));
            for (String ip : ipList) {
                RemoteServer server = new RemoteServer(ip, "appuser", "ad1oor00t");
                remoteServerList.add(server);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return remoteServerList;
    }

    @Override
    public String remotePath() {
        return "/home/appuser";
    }
}
