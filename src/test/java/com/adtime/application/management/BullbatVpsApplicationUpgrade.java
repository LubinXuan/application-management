package com.adtime.application.management;

import org.testng.annotations.Test;

/**
 * Created by xuanlubin on 2016/12/27.
 */
public class BullbatVpsApplicationUpgrade extends BullbatApplicationUpgrade {

    @Override
    ServerList serverList() {
        return new VPS();
    }

    @Test
    public void upload() {
        String fileName = "bullbat_tmp";
        String[] builder = new String[]{
                "rm bullbat -rf",
                "mv bullbat_tmp bullbat",
                "chmod +x bullbat/start_public.sh",
        };
        /*String[] builder = new String[]{
                "mv bullbat-client-3.0.jar bullbat/bullbat-client-3.0.jar"
        };*/
        super.uploadAndExec(fileName, builder);
    }


    @Override
    public void uploadRsa() {
        super.uploadRsa();
    }

    @Override
    public void startApp() {
        String builder = "~/bullbat/start_public.sh;";
        //builder = "ps -ef|grep bullbat-client-3.0.jar|grep -v grep|cut -c 9-15|xargs kill";
        upgrade.exec(builder);
    }


    @Override
    public void checkStatus() {
        super.checkStatus();
    }

    @Override
    public void forceKill() {
        super.forceKill();
    }

    @Override
    public void checkDisk() {
        super.checkDisk();
    }
}