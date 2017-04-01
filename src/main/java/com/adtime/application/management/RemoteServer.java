package com.adtime.application.management;

import com.adtime.application.ssh.SSHClient;
import com.jcraft.jsch.JSchException;

/**
 * Created by xuanlubin on 2016/12/27.
 */
public class RemoteServer {
    private String ip;
    private String user;
    private String pwd;
    private int port = 22;
    private SSHClient sshClient;


    public RemoteServer(String ip, String user, String pwd) {
        this.ip = ip;
        this.user = user;
        this.pwd = pwd;
    }

    public RemoteServer(String ip, String user, String pwd, int port) {
        this.ip = ip;
        this.user = user;
        this.pwd = pwd;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public String getUser() {
        return user;
    }

    public String getPwd() {
        return pwd;
    }

    public int getPort() {
        return port;
    }

    public SSHClient getSshClient() {
        return sshClient;
    }

    public void connect() throws JSchException {
        this.sshClient = SSHClient.connect(this.getIp(), this.getUser(), this.getPwd(), this.getPort());
    }
}
