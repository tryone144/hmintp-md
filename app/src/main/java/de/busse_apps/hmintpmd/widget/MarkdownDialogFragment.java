package de.busse_apps.hmintpmd.widget;

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

import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import in.uncod.android.bypass.Bypass;

public abstract class MarkdownDialogFragment extends DialogFragment {

    protected CharSequence getMessageFromResource(int resId) {
        String rawMessage = "";

        Reader bReader = null;
        try {
            bReader = new BufferedReader(new InputStreamReader(getResources()
                    .openRawResource(resId), "UTF8"));

            int current;
            while ((current = bReader.read()) != -1) {
                rawMessage = rawMessage + String.valueOf((char) current);
            }
        } catch (Exception ignored) {
        } finally {
            if (bReader != null) {
                try {
                    bReader.close();
                } catch (IOException ignored) {
                }
            }
        }

        Bypass bypass = new Bypass(getActivity());
        CharSequence mdMessage = bypass.markdownToSpannable(rawMessage);

        return mdMessage;
    }

    public class onButtonCloseClickListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int id) {
            dialog.cancel();
        }
    }
}
