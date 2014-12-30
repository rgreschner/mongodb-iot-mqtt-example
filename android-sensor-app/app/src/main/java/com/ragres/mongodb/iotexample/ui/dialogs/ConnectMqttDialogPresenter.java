package com.ragres.mongodb.iotexample.ui.dialogs;


import android.os.AsyncTask;

import com.ragres.mongodb.iotexample.controllers.ConnectivityController;

import rx.subjects.BehaviorSubject;

public class ConnectMqttDialogPresenter {

    /**
     * Observable for server address text.
     */
    private BehaviorSubject<String> serverAddressTextObservable = BehaviorSubject.create();

    /**
     * Observable for dialog dismiss.
     */
    private BehaviorSubject dismissDialogObservable = BehaviorSubject.create();

    /**
     * Public constructor.
     */
    public ConnectMqttDialogPresenter(ConnectivityController connectivityController) {
        this.connectivityController = connectivityController;
    }

    /**
     * Connectivity controller.
     */
    private ConnectivityController connectivityController;

    /**
     * Get observable for server address text.
     */
    public BehaviorSubject<String> getServerAddressTextObservable() {
        return serverAddressTextObservable;
    }

    /**
     * On dialog create.
     */
    public void onCreateDialog() {
        String serverAddress = connectivityController.getServerAddress();
        serverAddressTextObservable.onNext(serverAddress);
    }

    /**
     * Connect to server.
     * @param serverAddress Server to connect to.
     */
    public void connectToServer(final String serverAddress) {
        AsyncTask connectTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {

                connectivityController.connectToServer(serverAddress);
                return null;
            }
        };
        connectTask.execute();
    }

    /**
     * Cancel dialog.
     */
    public void cancel() {
        dismissDialogObservable.onNext(null);
    }

    /**
     * Get observable for dialog dismiss.
     */
    public BehaviorSubject getDismissDialogObservable() {
        return dismissDialogObservable;
    }
}
