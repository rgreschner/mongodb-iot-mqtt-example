package com.ragres.mongodb.iotexample.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;

import com.ragres.mongodb.iotexample.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class AboutDialogFragment extends DialogFragment {

    @InjectView(R.id.webview_about)
    WebView webView;

    /**
     * Create dialog.
     *
     * @param savedInstanceState Saved state.
     * @return Created dialog.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_about_title)
                .setPositiveButton(android.R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                onCancelBtnClick();
                            }
                        }
                );

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_about, null);
        ButterKnife.inject(this, view);
        webView.loadUrl("file:///android_asset/about.html");

        builder.setView(view);

        AlertDialog dialog = builder.create();
        return dialog;
    }

    /**
     * Handle click on cancel button.
     */
    private void onCancelBtnClick() {
        this.dismiss();
    }


    /**
     * Create fragment instance.
     *
     * @return New fragment instance.
     */
    public static DialogFragment newInstance() {
        AboutDialogFragment fragment = new AboutDialogFragment();
        return fragment;
    }

}
