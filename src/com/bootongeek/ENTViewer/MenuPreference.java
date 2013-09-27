package com.bootongeek.ENTViewer;

import com.bootongeek.ENTViewer.R;

import android.app.AlertDialog;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;

public class MenuPreference extends PreferenceActivity {

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.preferences);
		
		EditTextPreference edtWidthCol = (EditTextPreference) findPreference("width_col");
		edtWidthCol.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		
		final EditTextPreference edtOffset = (EditTextPreference) findPreference("offset_week");
		edtOffset.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
		edtOffset.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				try{
					int intTmp = Math.abs(Integer.valueOf(((String) newValue)));
					if(intTmp > 52)
					{
						alertIncorrectValue();
					}
					else
					{
						return true;
					}
					
					return false;
				}
				catch(NumberFormatException e){
					alertIncorrectValue();
					return false;
				}
			}
			
			private void alertIncorrectValue(){
				AlertDialog.Builder b = new AlertDialog.Builder(MenuPreference.this);
				b.setTitle("Erreur de saisie");
				b.setMessage("La valeur entrée est incorrecte !\nLa valeur doit être comprise entre -52 et 52");
				b.setPositiveButton("Valider", null);
				b.show();
			}
		});
	}
}
