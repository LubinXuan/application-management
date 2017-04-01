package com.adtime.application.management;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.util.Strings;

import java.io.IOException;
import java.util.List;

/**
 * Created by xuanlubin on 2016/12/27.
 */
public abstract class ApplicationUpgradeTest {


    interface ServerList {
        List<RemoteServer> list();

        String remotePath();
    }

    ApplicationUpgrade upgrade;

    private ServerList serverList;

    abstract ServerList serverList();

    public ApplicationUpgradeTest() {
        this.serverList = serverList();
        this.upgrade = new ApplicationUpgrade(serverList.list());
    }


    void uploadAndExec(String localFile, String... cmd) {
        upgrade.upload(localFile, serverList.remotePath());
        upgrade.exec(String.join(";", cmd));
    }

    @AfterTest
    public void setDown() throws IOException {
        upgrade.close();
    }

}