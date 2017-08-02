package com.example.ubuntu.bindertest;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Created by ubuntu on 17-7-31.
 */

public class BookManagerService extends Service {
    private static final String TAG = "BMS";

    private AtomicBoolean mIsServiceDestroyed = new AtomicBoolean(false);

    private CopyOnWriteArrayList<Book> mBookList = new CopyOnWriteArrayList<Book>();
    private CopyOnWriteArrayList<IOnNewBookArrivedListener> mListenerList
            = new CopyOnWriteArrayList<IOnNewBookArrivedListener>();

    private Binder mBinder = new IBookManager.Stub(){
        public List<Book> getBookList() throws RemoteException{
            return mBookList;
        }

        public void addBook(Book book) throws RemoteException{
            mBookList.add(book);
        }

        public void registerListener(IOnNewBookArrivedListener listener)throws RemoteException
        {
            if(!mListenerList.contains(listener)){
                mListenerList.add(listener);
            }else {
                Log.d(TAG,"listener already exists");
            }
            Log.d(TAG,"registered listener sizes:"+ mListenerList.size());
        }

        public void unregisterListener(IOnNewBookArrivedListener listener)throws RemoteException
        {
            if(mListenerList.contains(listener)){
                mListenerList.remove(listener);
                Log.d(TAG,"unregistered listener succeed");
            }else {
                Log.d(TAG,"listener not found");
            }
            Log.d(TAG,"unregistered listener succeed,current size:"+ mListenerList.size());
        }
    };

    public void onCreate(){
        super.onCreate();
        mBookList.add(new Book(1,"android"));
        mBookList.add(new Book(2,"ios"));
        new Thread(new ServiceWorker()).start();
    }

    public IBinder onBind(Intent intent){
        return mBinder;
    }

    public void onDestroy(){
        mIsServiceDestroyed.set(true);
        super.onDestroy();
    }

    private void onNewBookArrived(Book book) throws RemoteException{
        mBookList.add(book);
        Log.d(TAG,"onNewbookarrived,listener:"+ mListenerList.size());
        for (int i = 0;i<mListenerList.size();i++){
            IOnNewBookArrivedListener listener = mListenerList.get(i);
            listener.onNewBookArrived(book);
        }
    }

    private class ServiceWorker implements Runnable{
        public void run(){
            while (!mIsServiceDestroyed.get()){
                try{
                    Thread.sleep(5000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                int bookId = mBookList.size()+1;
                Book newbook = new Book(bookId,"newbook#"+bookId);
                try {
                    onNewBookArrived(newbook);
                }catch (RemoteException e){
                    e.printStackTrace();
                }
            }
        }
    }


}
