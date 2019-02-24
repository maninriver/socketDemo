package com.example.feng.asynctaskdemo;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TCPClientActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int MESSAGE_RECEIVE_NEW_MSG = 1;
    private static final int MESSAGE_SOCKET_CONNECTED = 2;
    private Button mSendButton;
    private TextView mMessageTextView;
    private EditText mMessageEditText;
    private PrintWriter mPrintWriter;
    private OutputStream mOutputstream;

    private Socket mClientSocket;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_RECEIVE_NEW_MSG:
                    System.out.println("MESSAGE_RECEIVE_NEW_MSG");
                    mMessageTextView.setText(mMessageTextView.getText() + (String) msg.obj);
                    break;
                case MESSAGE_SOCKET_CONNECTED:
                    mSendButton.setEnabled(true);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        if (mClientSocket != null) {
            try {
                mClientSocket.shutdownInput();
                mClientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcpclient);
        mMessageTextView = findViewById(R.id.msg_container);
        mSendButton = findViewById(R.id.send);
        mMessageEditText = findViewById(R.id.msg);
        mSendButton.setOnClickListener(this);
        Intent service = new Intent(this, TCPServerService.class);
        startService(service);
        new Thread() {
            @Override
            public void run() {
                connectTCPServer();
            }
        }.start();


    }


    private void connectTCPServer() {
        Socket socket = null;
        while (socket == null) {
            try {
                socket = new Socket("localhost", 8688);
                System.out.println("is  connect? " + socket.isConnected());
                mClientSocket = socket;
                mPrintWriter = new PrintWriter(socket.getOutputStream(), true);

                mOutputstream = socket.getOutputStream();
                mPrintWriter.print("connect server");
                socket.getOutputStream().write(("connect server" + "\n").getBytes("utf-8"));
                mHandler.sendEmptyMessage(MESSAGE_SOCKET_CONNECTED);
                System.out.println("connect server successful");

            } catch (IOException error) {
                SystemClock.sleep(1000);
                System.out.println("connect tcp service failed,retry ...");
            }
        }
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (!TCPClientActivity.this.isFinishing()) {
                String msg = bufferedReader.readLine();
                System.out.println("receive :" + msg);
                if (msg != null) {
                    String time = formatDateTime(System.currentTimeMillis());
                    final String showedMsg = "server" + time + ":" + msg + "\n";
                    mHandler.obtainMessage(MESSAGE_RECEIVE_NEW_MSG, showedMsg).sendToTarget();
                }
            }
            System.out.println("quit...");
            MyUtils.close(mPrintWriter);
            MyUtils.close(bufferedReader);
            socket.close();

        } catch (IOException error) {
            System.out.println(error.getMessage());
        }

    }

    private String formatDateTime(long time) {
        return new SimpleDateFormat("(HH:mm:ss)").format(new Date(time));
    }

    @Override
    public void onClick(View v) {
        if (v == mSendButton) {
            System.out.println("onclick");
            final String msg = mMessageEditText.getText().toString();
            if (!TextUtils.isEmpty(msg) && mOutputstream != null) {
            //    mPrintWriter.print(msg);//目前不清楚什么原因，printwriter是失败的
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            mOutputstream.write((msg+"\n").getBytes("utf-8"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
                mMessageEditText.setText("");
                String time = formatDateTime(System.currentTimeMillis());
                final String showMsg = "self" + time + ":" + msg + "\n";
                mMessageTextView.setText(mMessageTextView.getText() + showMsg);
            }

        }
    }
}
