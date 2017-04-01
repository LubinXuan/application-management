package com.adtime.application.management;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuanlubin on 2016/12/27.
 */
public class CarApplicationUpgrade extends EcApplicationUpgrade {

    @Override
    ServerList serverList() {
        return new HZBSAdsl() {
            @Override
            public List<RemoteServer> list() {
                List<RemoteServer> remoteServerList = new ArrayList<>();
                String[] ips = new String[]{
                        "192.168.168.121",
                        "192.168.168.122",
                        "192.168.168.123",
                };
                for (String ip : ips) {
                    RemoteServer server = new RemoteServer(ip, "appuser", "ad1oor00t");
                    remoteServerList.add(server);
                }
                return remoteServerList;
            }
        };
    }

    @Test
    public void uploadShell2() {
        String fileName = "ec_crawler_tmp/start_car.sh";
        StringBuilder builder = new StringBuilder();
        builder.append("mv start_car.sh appEcCrawler/start_car.sh;");
        builder.append("chmod +x appEcCrawler/start_car.sh;");
        super.uploadAndExec(fileName, builder.toString());
    }

    @Test
    public void startESCar() {
        StringBuilder builder = new StringBuilder();
        builder.append("cd ~;");
        builder.append("mkdir ecCrawler/ershouche -p;");
        builder.append("~/appEcCrawler/start_car.sh ecCrawler/ershouche 45555;");

        upgrade.exec(builder.toString());
    }

    @Override
    public void checkStatus() {
        super.checkStatus();
    }




    @Test
    public void startESCar11() {
        StringBuilder builder = new StringBuilder();
        builder.append("tail -100 ecCrawler/ershouche/logs/eb.log");

        upgrade.exec(builder.toString());
    }
}