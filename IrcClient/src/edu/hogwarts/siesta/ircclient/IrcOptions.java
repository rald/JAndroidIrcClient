package edu.hogwarts.siesta.ircclient;



import android.app.Activity;

import android.content.Intent;

import android.graphics.Typeface;

import android.os.Bundle;

import android.widget.EditText;
import android.widget.Button;

import android.view.View;



public class IrcOptions extends Activity {



	EditText edt2;
	EditText edt3;
	EditText edt4;
	EditText edt5;
	EditText edt6;

	Button btn3;
	Button btn4;



	@Override
	public void onCreate(Bundle bundle) {

		super.onCreate(bundle);    

		setContentView(R.layout.ircoptions);

		edt2 = (EditText) findViewById(R.id.editText2);
		edt3 = (EditText) findViewById(R.id.editText3);
		edt4 = (EditText) findViewById(R.id.editText4);
		edt5 = (EditText) findViewById(R.id.editText5);
		edt6 = (EditText) findViewById(R.id.editText6);

		btn3 = (Button) findViewById(R.id.button3);
		btn4 = (Button) findViewById(R.id.button4);

		Typeface typeface = Typeface.createFromAsset(getAssets(),"font/Typori-Regular.ttf");

		edt2.setTypeface(typeface);
		edt3.setTypeface(typeface);
		edt4.setTypeface(typeface);
		edt5.setTypeface(typeface);
		edt6.setTypeface(typeface);

		btn3.setTypeface(typeface);
		btn4.setTypeface(typeface);

		edt2.setTextSize(24);
		edt3.setTextSize(24);
		edt4.setTextSize(24);
		edt5.setTextSize(24);
		edt6.setTextSize(24);

		btn3.setTextSize(24);
		btn4.setTextSize(24);

		edt2.setText(IrcClient.srv);
		edt3.setText(String.valueOf(IrcClient.prt));
		edt4.setText(IrcClient.nck);
		edt5.setText(IrcClient.pss);
		edt6.setText(IrcClient.chn);

		btn3.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				IrcClient.srv=edt2.getText().toString();
				IrcClient.prt=Integer.valueOf(edt3.getText().toString());
				IrcClient.nck=edt4.getText().toString();
				IrcClient.pss=edt5.getText().toString();
				IrcClient.chn=edt6.getText().toString();
			}
		});



		btn4.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(IrcOptions.this, IrcClient.class);
                startActivity(intent);		
      }
		});
	}


}
