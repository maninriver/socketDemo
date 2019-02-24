package com.example.feng.asynctaskdemo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;


public class TCPServerService extends Service {

    private boolean mIsServiceDestoryed = false;
    private String[] mDefinedMessages = new String[]{
            "你好啊，哈哈",
            "请问你叫什么名字",
            "今天北京天气不错，shy",
            "你知道吗，我可以和很多人聊天的哦",
            "给你讲个笑话把：据说爱笑的人运气不会太差，不知道真假。"
    };

    @Override
    public void onCreate() {
        new Thread(new TcpServer()).start();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        mIsServiceDestoryed = true;
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class TcpServer implements Runnable {

        @Override
        public void run() {
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(8688);

            } catch (IOException e) {
                System.out.println("server failed,port:8688");
                e.printStackTrace();
                return;
            }

            while (!mIsServiceDestoryed) {
                try {
                    //接受客户端请求
                    final Socket client = serverSocket.accept();
                    System.out.println("accept");
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                responseClient(client);
                            } catch (IOException error) {
                                error.printStackTrace();
                                System.out.println("error 73");
                            }
                        }
                    }.start();


                } catch (IOException error) {
                    System.out.println("server :" + error.getMessage());

                }
            }
        }


    }

    private void responseClient(Socket client) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        PrintWriter out = new PrintWriter( client.getOutputStream(), true);
        System.out.println("欢迎来到聊天室");
        out.print("欢迎来到聊天室!");
        client.getOutputStream().write(("欢迎峰哥"+"\n").getBytes("utf-8"));
        System.out.println("mIsServiceDestoryed :" + mIsServiceDestoryed);
        while (!mIsServiceDestoryed) {

            String str = in.readLine();
            System.out.println("msg from client" + str);
            System.out.println("-------------");
            if (str == null) {
                //客户端断开连接
                System.out.println("game over");
                break;
            }
            int i = new Random().nextInt(mDefinedMessages.length);
            String msg = mDefinedMessages[i];
            out.println(msg);
            System.out.println("send :" + msg);

        }

        System.out.println("client quit ");
        out.close();
        MyUtils.close(out);
        MyUtils.close(in);

    }


}
