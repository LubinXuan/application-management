package com.adtime.application.management;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuanlubin on 2016/12/27.
 */
public class EcApplicationUpgrade extends ApplicationUpgradeTest {

    @Override
    ServerList serverList() {
        return new HZBSAdsl();
    }

    @Test
    public void upload() {
        upgrade.exec("rm ec_crawler_tmp -rf");
        String fileName = "ec_crawler_tmp";
        StringBuilder builder = new StringBuilder();
        builder.append("rm appEcCrawler -rf;");
        builder.append("mv ec_crawler_tmp appEcCrawler;");
        builder.append("chmod +x appEcCrawler/start.sh;");
        builder.append("chmod +x appEcCrawler/start_car.sh;");
        super.uploadAndExec(fileName, builder.toString());
    }

    @Test
    public void uploadApp() {
        String fileName = "ec_crawler_tmp/bullbat-ec-client-1.0.jar";
        StringBuilder builder = new StringBuilder();
        builder.append("mv bullbat-ec-client-1.0.jar appEcCrawler/bullbat-ec-client-1.0.jar;");
        super.uploadAndExec(fileName, builder.toString());
    }

    @Test
    public void uploadShell() {
        String fileName = "ec_crawler_tmp/start.sh";
        StringBuilder builder = new StringBuilder();
        builder.append("mv start.sh appEcCrawler/start.sh;");
        builder.append("chmod +x appEcCrawler/start.sh;");
        super.uploadAndExec(fileName, builder.toString());
    }

    private void startApp(String type, int port) {
        StringBuilder builder = new StringBuilder();
        builder.append("cd ~;");
        builder.append("mkdir ecCrawler/" + type + " -p;");
        if ("single".equals(type)) {
            builder.append("~/appEcCrawler/start.sh ecCrawler/" + type + " " + port + " " + getQueue(type) + " false;");
        } else {
            builder.append("~/appEcCrawler/start.sh ecCrawler/" + type + " " + port + " " + getQueue(type) + " false;");
        }


        upgrade.exec(builder.toString());
    }

    @Test
    public void startList() {
        startApp("list", 51111);
    }

    @Test
    public void startRate() {
        startApp("rate", 51112);
    }

    @Test
    public void startSingle() {
        startApp("single", 51113);
    }

    @Test
    public void startAll() {
        StringBuilder builder = new StringBuilder();
        builder.append("cd ~;");
        builder.append("mkdir ecCrawler/all -p;");
        builder.append("~/appEcCrawler/start.sh ecCrawler/all 51114 false;");
        upgrade.exec(builder.toString());
    }

    private String getQueue(String... types) {
        String[] medias = new String[]{"ali", "gome", "jd", "suning", "vip"};
        List<String> queueList = new ArrayList<>();
        for (String media : medias) {
            for (String type : types) {

                if ("vip".equals(media) && "rate".equals(type)) {
                    continue;
                }
                queueList.add(("EC_CRAWL_" + media + "_" + type + "_QUEUE").toUpperCase());
            }
        }
        return String.join(",", queueList);
    }

    @Test
    public void queue() {
        System.out.println(getQueue("list", "rate", "single"));
    }


    @Test
    public void checkStatus() {
        String builder = "ps -ef|grep com.adtime.bullbat.eb.StartJMS|grep -v grep";//|grep _QUEUE_TMP |cut -c 9-15 | xargs kill 9
        upgrade.exec(builder);
    }


    @Test
    public void tailLog() {
        String builder = "tail ecCrawler/single/logs/eb.log";
        upgrade.exec(builder);
    }

    @Test
    public void rm() {
        String builder = "rm ec_crawler_tmp -rf";
        upgrade.exec(builder);
    }
}