package com.ragres.mongodb.iotexample.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.ragres.mongodb.iotexample.AndroidApplication;
import com.ragres.mongodb.iotexample.R;
import com.ragres.mongodb.iotexample.controllers.ConnectivityController;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * MQTT Broker Connection Dialog.
 */
public class ConnectMqttDialogFragment extends DialogFragment {

    /**
     * Connect to server button.
     */
    @InjectView(R.id.inputServerAddress)
    EditText inputServerAddress;


    /**
     * Get application instance.
     *
     * @return Application instance.
     */
    private AndroidApplication getAndroidApplication() {
        AndroidApplication application = (AndroidApplication) this.getActivity().getApplication();
        return application;
    }

    /**
     * Get connectivity controller instance.
     *
     * @return Connectivity controller instance.
     */
    private ConnectivityController getConnectivityController() {
        ConnectivityController connectivityController = getAndroidApplication().
                getConnectivityController();
        return connectivityController;
    }

    /**
     * Create dialog.
     *
     * @param savedInstanceState Saved state.
     * @return Created dialog.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_connect_mqtt_title)
                .setPositiveButton(android.R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                onConnectToServerBtnClick();
                            }
                        }
                )
                .setNegativeButton(android.R.string.no,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                onCancelBtnClick();
                            }
                        }
                );

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_connect_mqtt, null);
        ButterKnife.inject(this, view);

        inputServerAddress.setText(getConnectivityController().getServerAddress());

        builder.setView(view);

        AlertDialog dialog = builder.create();
        return dialog;
    }

    /**
     * Handle click on cancel button.
     */
    private void onCancelBtnClick() {
        this.getDialog().dismiss();
    }


    /**
     * Handle click on connect button.
     */
    public void onConnectToServerBtnClick() {
        final String serverAddress = inputServerAddress.getText().toString();

        AsyncTask connectTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                getConnectivityController().connectToServer(serverAddress);
                return null;
            }
        };
        connectTask.execute();
    }

    /**
     * Create fragment instance.
     *
     * @return New fragment instance.
     */
    public static DialogFragment newInstance() {
        ConnectMqttDialogFragment fragment = new ConnectMqttDialogFragment();
        return fragment;
    }
}
