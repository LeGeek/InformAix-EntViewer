package com.bootongeek.ENTViewer;

import com.bootongeek.ENTViewer.R;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.text.InputType;

public class MenuPreference extends PreferenceActivity {

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.preferences);
		
		EditTextPreference edt = (EditTextPreference) findPreference("width_col");
		edt.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
	}
}
