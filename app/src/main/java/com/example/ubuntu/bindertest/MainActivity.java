package com.example.ubuntu.bindertest;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;


import java.util.List;



public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int MESSAGE_NEW_BOOK_ARRIVED = 1;

    private IBookManager mRemoteBookManager;

    private android.os.Handler mhandler = new android.os.Handler(){
        @Override
        public void handleMessage(Message message) {
            switch(message.what){
                case MESSAGE_NEW_BOOK_ARRIVED:
                    Log.d(TAG,"receive new book :"+message.obj);
                    break;
                default :
                    super.handleMessage(message);
            }

        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            IBookManager bookManager = IBookManager.Stub.asInterface(iBinder);
            try{
                mRemoteBookManager = bookManager;
                List<Book> list = bookManager.getBookList();
                Log.i(TAG,"query book list, list type is:"+list.getClass().getCanonicalName());
                Log.i(TAG,"query book list:"+list.toString());
                Book newbook = new Book(3,"android developer's art");
                bookManager.addBook(newbook);
                List<Book> newlist = bookManager.getBookList();
                Log.i(TAG,"query book newlist:"+newlist.toString());
                bookManager.registerListener(mOnNewBookArrivedListener);
            }catch (RemoteException e){
                e.printStackTrace();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mRemoteBookManager = null;
            Log.e(TAG,"binder died");

        }
    };

    private IOnNewBookArrivedListener mOnNewBookArrivedListener = new IOnNewBookArrivedListener.Stub(){
        public void onNewBookArrived(Book newbook) throws RemoteException{
            mhandler.obtainMessage(MESSAGE_NEW_BOOK_ARRIVED,newbook).
                    sendToTarget();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this,BookManagerService.class);
        bindService(intent,mConnection, Context.BIND_AUTO_CREATE);
    }

    protected void onDestroy(){
        if (mRemoteBookManager != null&&mRemoteBookManager.asBinder().isBinderAlive()){
            try{
                Log.i(TAG,"unregistered listener :"+mOnNewBookArrivedListener);
                mRemoteBookManager.
                        unregisterListener(mOnNewBookArrivedListener);
            }catch (RemoteException e){
                e.printStackTrace();
            }
        }
        unbindService(mConnection);
        super.onDestroy();
    }
}
