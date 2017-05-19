package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import  java.io.Serializable;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 *
 * @author stevko
 *
 */

class MessageParam implements Comparable<MessageParam>{
    String message;
    int msgid;
    boolean status;
    int a_seq;
    String myport;

    @Override
    public int compareTo(MessageParam ob) {
        if(this.a_seq < ob.a_seq){
            return -1;
        }

        if(this.a_seq > ob.a_seq) {
            return 1;
        }

        if(this.a_seq == ob.a_seq){
            if(Integer.parseInt(this.myport)< Integer.parseInt(ob.myport)){
                return -1;
            }
            else{
                return 1;
            }
        }

        return 0;
    }

    public boolean equals(Object o){
        MessageParam ob = (MessageParam)o;
        return this.message.equals(ob.message)&&this.msgid==ob.msgid;
    }
}



public class GroupMessengerActivity extends Activity{
    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final String REMOTE_PORT2 = "11116";
    static final String REMOTE_PORT3 = "11120";
    static final String REMOTE_PORT4 = "11124";
    static final int SERVER_PORT = 10000;
    static int dbKey = -1;
    static String errport = "0";

    public static int seqnum =-1;

    //hashmap to store the message as priority queue
    //HashMap<Integer, MessageParam> map = new HashMap<Integer, MessageParam>();

    MessageParam param = new MessageParam();

    PriorityQueue<MessageParam> queue  = new PriorityQueue<MessageParam>();

    private final Uri mUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");
    private ContentResolver mContentResolver;
    private ContentValues mContentValues ;




    ServerSocket serverSocket;
    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);

        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));

        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            Log.e(TAG, "Can't create a ServerSocket: "+e);
            return;
        }


        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));

        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */

        final EditText text = (EditText)findViewById(R.id.editText1);

        findViewById(R.id.button4).setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        //message id
                        param.msgid++;
                        String message = text.getText().toString() +":"+ param.msgid;
                        Log.d(TAG, "sample message:");
                        text.setText("");
                        new ClientTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message, myPort);
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }

    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {



        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            /*
             * TODO: Fill in your server code that receives messages and passes them
             * to onProgressUpdate().
             */
            try {

                while(true) {
                    Socket socket = serverSocket.accept();
                    //Log.d(TAG, sockets[0].toString());

                    Log.d(TAG, "servertask is here");

                    //receive the message from clienttask
                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String[] msg = br.readLine().split(":");
//

                    Log.d(TAG,"msgarray: "+msg[0]+":"+msg[1]+":"+msg[2]+":"+msg[3]+":"+msg[4]);

                    if (msg[0]==null || msg[0].isEmpty()) {
                        errport = msg[4];
                        Log.e(TAG, "null message encountered");
                        throw new Exception();
                    }

                    MessageParam obj = new MessageParam();
                    obj.message = msg[0];
                    obj.msgid = Integer.parseInt(msg[1]);
                    obj.status = Boolean.valueOf(msg[2]);
                    obj.a_seq = Integer.parseInt(msg[3]);;
                    obj.myport = msg[4];

                    Log.d(TAG,"Object values: "+obj.message+":"+obj.msgid+":"+obj.status+":"+obj.a_seq);

                    if(!obj.status){
                        seqnum++;
                        obj.a_seq = seqnum;
                        synchronized (queue){
                            queue.add(obj);
                        }

                        Log.d(TAG, "Servertask seqnum: "+seqnum);

                        PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
                        pw.println(obj.msgid+":"+Integer.toString(seqnum));
                        pw.flush();
//
                    }

                    else{

                        seqnum = Math.max(seqnum,obj.a_seq)+1;
                                //Log.d(TAG, "queue content: "+obj.message+":"+obj.msgid+":"+obj.myport);
                                    if(queue.contains(obj)){
                                        Log.i(TAG, "Match found in queue: "+obj.msgid);
                                        //Log.e(TAG, "is object removed: "+b);
                                        synchronized (queue){
                                             queue.remove(obj);
                                            queue.add(obj);}

                                        //Log.e(TAG, "is object added: "+b);
                                    }


                                while (queue.size() != 0) {
                                    //pop the values until it hits the end or a value with false status
                                    synchronized(queue){
                                        MessageParam ob = queue.peek();
                                        Log.i(TAG, "popping queue: " + ":" + ob.message + ":" + ob.msgid + ":" + ob.status + ":" + ob.a_seq + ":" + ob.myport);
                                        if (ob.status) {
                                            ob = queue.poll();
                                            dbKey++;
                                            Log.e(TAG, "object popped: " + dbKey);
                                            String[] publishmsg = {ob.message, Integer.toString(dbKey)};
                                            publishProgress(publishmsg);
                                        } else {
                                            Log.d(TAG, "message undeliverable:" + ob.message + ":" + dbKey);
                                            break;
                                        }
                                    }
                                }

                    }

                    Log.d(TAG, "out of serversocket loop: " + obj.message);

                    socket.close();
                }
            } catch (Exception e) {
                Log.d(TAG, "Unable to recieve message from Socket");
                if(!errport.equals("0")){
                        Iterator<MessageParam> ir = queue.iterator();
                        while(ir.hasNext()){
                            MessageParam ob = ir.next();
                            if(ob.myport.equals(errport)&&ob.status==false){
                                synchronized (queue){queue.remove(ob);}

                            }
                        }

                }
            }


            return null;
        }

        protected void onProgressUpdate(String...strings) {
            /*
             * The following code displays what is received in doInBackground().
             */
            String message = strings[0];
            String msgseq = strings[1];
            Log.d(TAG, "Message Recieved in Progress Update: "+message+": "+msgseq);


                    mContentValues = new ContentValues();
                    mContentValues.put("key", msgseq);
                    mContentValues.put("value", message);

                    mContentResolver = getContentResolver();
                    mContentResolver.insert(mUri, mContentValues);

            TextView remoteTextView = (TextView) findViewById(R.id.textView1);
            remoteTextView.append(message +": "+msgseq+ "\t\n");


            /*
             * The following code creates a file in the AVD's internal storage and stores a file.
             *
             * For more information on file I/O on Android, please take a look at
             * http://developer.android.com/training/basics/data-storage/files.html
             */

            return;
        }

    }

    private class ClientTask extends AsyncTask<String, Void, Void>{

        @Override
        protected Void doInBackground(String... msgs) {
            int itr = 0;
            int a_seq = 0;


            String[] msg1 = msgs[0].split(":");
            //received message, messageid and sending process port number on send buttonclick
            String message = msg1[0].trim();
            int msgid = Integer.parseInt(msg1[1]);
            String sendingport = msgs[1];
            boolean status = false;
            String remotePort= "0";
            //Log.e(TAG, "msg: "+msgs[0]+"msg1: "+msgs[1]);

            for(int i=11108;i<=11124;i+=4) {
                try {
                    remotePort = Integer.toString(i);
                    if(remotePort.equals(errport)){
                        Log.e(TAG,"err found: "+errport);
                        if(i==11124){break;}
                        i+=4;
                        remotePort = Integer.toString(i);
                    }
                    //multicasting the message by writing message on the server port.


                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(remotePort));

                    //prepare the message to send
//
                    String msgtosend = message + ":" + Integer.toString(msgid) + ":" + Boolean.toString(status) + ":" + Integer.toString(-1) + ":" + sendingport;
                    Log.e(TAG, "msgstoSend from clienttask: " + msgtosend);
                /*
                 * TODO: Fill in your client code that sends out a message.
                 */

                    PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
                    pw.println(msgtosend);
                    pw.flush();

                    //socket.setSoTimeout(500);
                    if(socket.getInputStream()==null){
                        Log.e(TAG, "null socket exception");
                        throw new Exception();}
                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String[] rmsg = br.readLine().split(":");




                    int rmsgid = Integer.parseInt(rmsg[0]);
                    int p_seq = Integer.parseInt(rmsg[1]);

                    Log.i(TAG, "received msgid and seqnum and itr: "+rmsgid+" : "+p_seq+" : "+itr);


                    itr++;
                    a_seq = Math.max(a_seq, p_seq);
                    int compare = 5;
                    if(!errport.equals("0")){compare = 4;}

                    if (itr == compare) {
                        Log.d(TAG, "agreed seq number is: " + a_seq);
                        status = true;
                        String newmsg = message + ":" + rmsgid + ":" + Boolean.toString(status) + ":" + Integer.toString(a_seq) + ":" + sendingport;
                        Log.d(TAG, "itr is 5 now: " + newmsg);
                        for (int j=11108;j<=11124;j+=4) {
                            String port = Integer.toString(j);
                            if(port.equals(errport)){
                                Log.e(TAG, "skipped errport: "+errport);
                                if(j==11124){break;}
                                j+=4;
                                port = Integer.toString(j);
                            }
                            //Log.d(TAG, "sending messages back to server");
                            Socket s = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                    Integer.parseInt(port));
                            PrintWriter p = new PrintWriter(s.getOutputStream(), true);
                            p.println(newmsg);
                            p.flush();
                            s.close();
                        }
                    }


                    socket.close();

                } catch (UnknownHostException e) {
                    Log.e(TAG, "ClientTask UnknownHostException");
                    return null;
                } catch (IOException e) {
                    Log.e(TAG, "ClientTask socket IOException" + e);
                    return null;
                }
                catch (Exception e) {
                    errport = remotePort;
                    Log.e(TAG, "Socket exception met:"+e+":"+remotePort+":"+errport);
                    synchronized (queue){
                        Iterator<MessageParam> it = queue.iterator();
                        List<MessageParam> list = new ArrayList<MessageParam>();
                        while(it.hasNext()){
                            Log.e(TAG, "Log2");
                            MessageParam ob = it.next();
                            Log.e(TAG,"log3: "+ob.myport+":"+ob.status);
                            if(errport.equals(ob.myport) && (!ob.status)){
                                Log.d(TAG, "message removed:"+errport+":"+ob.message);
                                //queue.remove(ob);
                                list.add(ob);
                            }
                        }
                        //queue.remove(list);
                        queue.removeAll(list);

                    }

                }

            }

            return null;
        }
    }

}

