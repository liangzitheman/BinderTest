// IOnNewBookArrivedListener.aidl
package com.example.ubuntu.bindertest;

import com.example.ubuntu.bindertest.Book;

// Declare any non-default types here with import statements

interface IOnNewBookArrivedListener {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void onNewBookArrived(in Book newbook);
}
