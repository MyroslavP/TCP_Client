package com.example.tcpclient;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;

public class MainActivity extends AppCompatActivity {

    ImageView im1,im2;
    public static final int Server_Port = 5000;
    public static final String Server_ip = "192.168.0.75";
    public static final int Buffer_Size = 9;
    public static final int Magic = 0xABBABABA;

    private Socket socket = null;
    private InputStream reader;
    private boolean mRun = false;
    public byte[] buffer;
    public byte[] data;
    public int Sum;
    public int Img;
    public int tImg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        im1 = (ImageView) findViewById(R.id.Image);
        im2 = (ImageView) findViewById(R.id.image2);

        thread.start();

    }

    Thread thread = new Thread(new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @SuppressLint("SetTextI18n")
        @Override
        public void run() {

            mRun = true;

            try {
                Socket socket = new Socket(Server_ip, Server_Port);

                try {

                    reader = socket.getInputStream();

                    while (mRun) {

                        try {

                            buffer = new byte[Buffer_Size];
                            int charsRead = 0;

                            while (true) {

                                int off = 0, len = 9;
                                while (len > 0) {
                                    charsRead = reader.read(buffer, off, len);
                                    if (charsRead > 0) {
                                        off += charsRead;
                                        len -= charsRead;
                                    }
                                }
                                if (len > 0) break;

                                Sum = ((buffer[0] & 0xFF) << 24) |
                                        ((buffer[1] & 0xFF) << 16) |
                                        ((buffer[2] & 0xFF) << 8) |
                                        (buffer[3] & 0xFF);

                                Img = ((buffer[4] & 0xFF) << 24) |
                                        ((buffer[5] & 0xFF) << 16) |
                                        ((buffer[6] & 0xFF) << 8) |
                                        (buffer[7] & 0xFF);

                                tImg = buffer[8];


                                System.out.println(" Magic " + Integer.toHexString(Sum) + " Size " + Img + " Type " + (char) tImg);


                                if (Sum == Magic) {
                                    System.out.println("OK");

                                    if (Img > 9) {
                                        Img -= 9;
                                        data = new byte[Img];
                                        off = 0;
                                        len = Img;
                                        while (len > 0) {
                                            charsRead = reader.read(data, off, len);
                                            if (charsRead > 0) {
                                                off += charsRead;
                                                len -= charsRead;
                                            }
                                        }
                                        if (len > 0) break;

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {

                                                if (tImg == 'a')
                                                {
                                                    Bitmap bitmap1 = BitmapFactory.decodeByteArray(data, 0, data.length);
                                                    im1.setImageBitmap(Bitmap.createScaledBitmap(bitmap1, im1.getMeasuredWidth(), im1.getMeasuredHeight(), false));

                                                }
                                                if (tImg == 'b')
                                                {
                                                    Bitmap bitmap1 = BitmapFactory.decodeByteArray(data, 0, data.length);
                                                    im2.setImageBitmap(Bitmap.createScaledBitmap(bitmap1, im1.getMeasuredWidth(), im1.getMeasuredHeight(), false));

                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {

                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    });
}
