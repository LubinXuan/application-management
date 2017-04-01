package com.adtime.application.management;

import org.testng.annotations.Test;

/**
 * Created by xuanlubin on 2016/12/27.
 */
public class BullbatApplicationUpgrade extends ApplicationUpgradeTest {

    @Override
    ServerList serverList() {
        return new HZBSAdsl();
    }

    @Test
    public void upload() {
        String fileName = "bullbat_tmp";
        String[] builder = new String[]{
                "rm bullbat -rf",
                "mv bullbat_tmp bullbat",
                "chmod +x bullbat/start.sh",
        };
        super.uploadAndExec(fileName, builder);

        startApp();
    }


    @Test
    public void uploadApp() {
        String fileName = "bullbat_tmp/bullbat-client-3.0.jar";
        StringBuilder builder = new StringBuilder();
        builder.append("mv bullbat-client-3.0.jar bullbat/bullbat-client-3.0.jar;");
        super.uploadAndExec(fileName, builder.toString());
    }

    @Test
    public void uploadRsa() {
        String fileName = "id_rsa.pub";
        StringBuilder builder = new StringBuilder();
        builder.append("mkdir .ssh && chmod 0700 .ssh;");//chmod 0700 .ssh && chmod 600 .ssh/authorized_keys
        builder.append("cp id_rsa.pub .ssh/authorized_keys;");
        builder.append("chmod 600 .ssh/authorized_keys;");
        builder.append("rm id_rsa id_rsa.pub;");
        super.uploadAndExec(fileName, builder.toString());
    }

    @Test
    public void startApp() {
        String builder = "~/bullbat/start.sh;";
        upgrade.exec(builder);
    }

    @Test
    public void checkStatus() {
        String builder = "ps -ef|grep bullbat-client-3.0.jar|grep -v grep";
        upgrade.exec(builder);
    }


    @Test
    public void forceKill() {
        //ps -ef|grep wubaer|grep -v grep|cut -c 9-15|xargs kill -9
        String builder = "ps -ef|grep bullbat-client-3.0.jar|grep -v grep|cut -c 9-15|xargs kill -9;rm .yq_crawler_61234/crawlTaskPersist* -rf";
        upgrade.exec(builder);
    }

    @Test
    public void checkDisk() {
        //String builder = "du -h --max=2 ecCrawler/";
        String builder = "du -h --max=3 |grep crawled_store";
        upgrade.exec(builder);
    }



    @Test
    public void rmDir() {
        //String builder = "du -h --max=2 ecCrawler/";
        String builder = "rm ecCrawler/all/ec_task_persist.db.* -rf";
        upgrade.exec(builder);
    }



    @Test
    public void checkDisk2() {
        //String builder = "du -h --max=2 ecCrawler/";
        String builder = "df -h";
        upgrade.exec(builder);
    }

    @Test
    public void rmFile() {
        String builder = "rm bullbat_tmp -rf";
        //String builder = "ls -l -h .yq_crawler_61234";
        upgrade.exec(builder);
    }


    @Test
    public void rmLog() {
        String builder = "find .yq_crawler_61234/logs/* -mtime +0 -type f -exec rm {} \\;";
        //String builder = "ls -l -h .yq_crawler_61234";
        upgrade.exec(builder);
    }

    @Test
    public void gc() {
        String builder = "/opt/jdk8/bin/jstat -gc `ps -ef|grep bullbat-client-3.0.jar|grep -v grep|cut -c 9-15`";
        //String builder = "ls -l -h .yq_crawler_61234";
        upgrade.exec(builder);
    }


    @Test
    public void checkPPP() {
        //ifconfig ppp0 | awk '/inet/ {print $2}' | cut -f2 -d ":"
        String builder = "/sbin/ifconfig ppp0 | awk '/inet/ {print $2}'";
        //String builder = "ls -l -h .yq_crawler_61234";
        upgrade.exec(builder);
    }

    @Test
    public void checkProcess() {
        //ifconfig ppp0 | awk '/inet/ {print $2}' | cut -f2 -d ":"
        String builder = "ps -ef|grep appEcCrawler/run.properties|grep -v grep";
        //String builder = "ls -l -h .yq_crawler_61234";
        upgrade.exec(builder);
    }
}