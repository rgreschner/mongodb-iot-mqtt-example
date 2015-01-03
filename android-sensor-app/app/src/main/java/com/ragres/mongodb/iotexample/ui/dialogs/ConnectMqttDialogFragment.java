package com.ragres.mongodb.iotexample.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.ragres.mongodb.iotexample.AndroidApplication;
import com.ragres.mongodb.iotexample.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * MQTT Broker Connection Dialog.
 */
public class ConnectMqttDialogFragment extends DialogFragment {

    /**
     * Connect to server button.
     */
    @InjectView(R.id.input_server_address)
    EditText inputServerAddress;

    /**
     * Dialog presenter.
     */
    private ConnectMqttDialogPresenter connectMqttDialogPresenter;

    /**
     * Event subscription on getServerAddressTextObservable.
     */
    private Subscription getServerAddressTextObservableSubscription;

    /**
     * Event subscription on getDismissDialogObservableSubscription.
     */
    private Subscription getDismissDialogObservableSubscription;


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

        connectMqttDialogPresenter = getAndroidApplication().getObjectGraph()
                .get(ConnectMqttDialogPresenter.class);

        getServerAddressTextObservableSubscription = connectMqttDialogPresenter.getServerAddressTextObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String value) {
                        inputServerAddress.setText(value);
                    }
                });
        getDismissDialogObservableSubscription = connectMqttDialogPresenter.getDismissDialogObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1() {
                    @Override
                    public void call(Object value) {
                        getDialog().dismiss();
                    }
                });
        connectMqttDialogPresenter.onCreateDialog();

        builder.setView(view);

        AlertDialog dialog = builder.create();
        return dialog;
    }

    /**
     * Handle click on cancel button.
     */
    private void onCancelBtnClick() {
        connectMqttDialogPresenter.cancel();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != getServerAddressTextObservableSubscription) {
            getServerAddressTextObservableSubscription.unsubscribe();
            getServerAddressTextObservableSubscription = null;
        }
        if (null != getDismissDialogObservableSubscription) {
            getDismissDialogObservableSubscription.unsubscribe();
            getDismissDialogObservableSubscription = null;
        }
    }

    /**
     * Handle click on connect button.
     */
    public void onConnectToServerBtnClick() {
        final String serverAddress = inputServerAddress.getText().toString();
        connectMqttDialogPresenter.connectToServer(serverAddress);
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
