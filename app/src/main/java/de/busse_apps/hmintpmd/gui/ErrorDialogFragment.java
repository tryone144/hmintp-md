package de.busse_apps.hmintpmd.gui;

/*
 * Copyright 2015 Bernd Busse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.busse_apps.hmintpmd.R;

public class ErrorDialogFragment extends DialogFragment {

    public static final String ARG_TITLE = "de.busse_apps.hmintpmd.gui.ErrorDialogFragment.title";
    public static final String ARG_MESSAGE = "de.busse_apps.hmintpmd.gui.ErrorDialogFragment.message";
    private static final String INFO_DIALOG_TAG = "de.busse_apps.hmintpmd.gui.InfoDialogFragment";

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(getArguments().getString(ARG_TITLE));
        builder.setMessage(getArguments().getString(ARG_MESSAGE));
        builder.setNegativeButton(R.string.dialog_close, new onButtonCloseClickListener());
        builder.setNeutralButton(R.string.dialog_info, new onButtonInfoClickListener());

        return builder.create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getDialog() != null) {
            getDialog().setCanceledOnTouchOutside(false);
            getDialog().setCancelable(false);
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /**
     * DialogInterface.OnClickListener for close button
     */
    private class onButtonCloseClickListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int id) {
            dialog.cancel();
            getActivity().finish();
        }
    }

    /**
     * DialogInterface.OnClickListener for info button
     */
    private class onButtonInfoClickListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int id) {
            // TODO: generate Information Dialog
            InfoDialogFragment mInfoDialogFragment = new InfoDialogFragment();
            mInfoDialogFragment.setCancelable(false);
            mInfoDialogFragment.show(getFragmentManager(), INFO_DIALOG_TAG);
        }
    }
}
