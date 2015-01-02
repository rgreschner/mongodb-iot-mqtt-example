package com.ragres.mongodb.iotexample.ui.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ragres.mongodb.iotexample.R;

/**
 * Fragment displaying MQTT connection information.
 */
public class MqttConnectivityInformationFragment extends Fragment {

    public MqttConnectivityInformationFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_mqtt_connectivity_information, container, false);
        return view;
    }


}
