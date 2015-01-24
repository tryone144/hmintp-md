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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import de.busse_apps.hmintpmd.R;
import de.busse_apps.hmintpmd.common.WSIKMCalculator;

public class InputFragment extends Fragment {

	private MainActivity mActivity;
	private Button mButtonCalculate;
	
	private EditText mFieldLasttime;
	private EditText mFieldMasseaten;
	private EditText mFieldBodymass;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_input, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mActivity = (MainActivity) getActivity();
		
		mButtonCalculate = (Button) getView().findViewById(R.id.input_button_calculate);
		mButtonCalculate.setOnClickListener(new onButtonCalculateClickListener());
		
		mFieldLasttime = (EditText) getView().findViewById(R.id.input_field_lasttime);
		mFieldMasseaten = (EditText) getView().findViewById(R.id.input_field_masseaten);
		mFieldBodymass = (EditText) getView().findViewById(R.id.input_field_bodymass);
	}
	
	private boolean checkInput() {
		String lastTime = mFieldLasttime.getText().toString();
		String masseaten = mFieldMasseaten.getText().toString();
		String bodymass = mFieldBodymass.getText().toString();
		
		if (lastTime.equals("")) {
			mFieldLasttime.requestFocus();
			Toast.makeText(getActivity(), R.string.msg_error_missing_input, Toast.LENGTH_SHORT).show();
			return false;
		} else if (masseaten.equals("")) {
			mFieldMasseaten.requestFocus();
			Toast.makeText(getActivity(), R.string.msg_error_missing_input, Toast.LENGTH_SHORT).show();
			return false;
		} else if (bodymass.equals("") || Double.valueOf(bodymass) == 0.0) {
			mFieldBodymass.requestFocus();
			Toast.makeText(getActivity(), R.string.msg_error_missing_input, Toast.LENGTH_SHORT).show();
			return false;
		} else {
			return true;
		}
	}

	private double evaluateInput() {
		double lastTime = Double.valueOf(mFieldLasttime.getText().toString());
		double massEaten = Double.valueOf(mFieldMasseaten.getText().toString());
		double bodymass = Double.valueOf(mFieldBodymass.getText().toString());
		return WSIKMCalculator.calculate(lastTime, massEaten, bodymass);
	}
	
	private class onButtonCalculateClickListener implements View.OnClickListener {
		@Override
        public void onClick(View v) {
			if (checkInput()) {
				double value = evaluateInput();
				mActivity.openResultFragment(value);
			}
		}
	}

}
