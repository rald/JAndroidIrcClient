package edu.hogwarts.siesta.ircclient;



import android.app.Activity;

import android.content.Intent;

import android.graphics.Typeface;

import android.os.Bundle;
import android.os.StrictMode;
import android.os.Handler;
import android.os.Message;

import android.widget.EditText;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import android.view.View;

import java.io.PrintWriter;
import java.io.StringWriter;



public class IrcClient extends Activity {

	static String srv = "sakura.jp.as.dal.net";
	static String nck = "siesta";
	static String chn = "#pantasya";
	static String pss = null;
	static int prt = 6667;

	String tgt=chn;

	final String PFX = "."; 



	EditText cmd;
	ScrollView scl;	
	TextView dpy;
	Button btn1;
	Button btn2;



	public Irc irc=null;




	private final Handler myTextHandler = new Handler(new Handler.Callback() {
	    @Override
	    public boolean handleMessage(Message stringMessage) {
			dpy.append((String)stringMessage.obj);

			scl.postDelayed(new Runnable() {
			    @Override
			    public void run() {
			        scl.smoothScrollTo(0,dpy.getBottom());
			    }
			}, 250);

	        return true;
	    }
	});



	@Override
	public void onCreate(Bundle bundle) {

		super.onCreate(bundle);    
		
		setContentView(R.layout.ircclient);

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

		cmd = (EditText) findViewById(R.id.editText1);
		scl = (ScrollView) findViewById(R.id.scrollView1);
		dpy = (TextView) findViewById(R.id.textView1);
		btn1 = (Button) findViewById(R.id.button1);
		btn2 = (Button) findViewById(R.id.button2);

		Typeface typeface = Typeface.createFromAsset(getAssets(),"font/Typori-Regular.ttf");

		cmd.setTypeface(typeface);
		dpy.setTypeface(typeface);
		btn1.setTypeface(typeface);
		btn2.setTypeface(typeface);

		cmd.setTextSize(24);
		dpy.setTextSize(24);
		btn1.setTextSize(24);
		btn2.setTextSize(24);



		irc=new Irc(this);		



		btn1.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				String line=cmd.getText().toString();

				if(irc!=null) {
					irc.parse(line);
				}				

				cmd.setText("");
			}
		});



		btn2.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(IrcClient.this, IrcOptions.class);
	        	startActivity(intent);
			}
		});


	}



	void display(String line) {
		Message stringMessage = Message.obtain(myTextHandler);
		stringMessage.obj = line+"\r\n";
		stringMessage.sendToTarget();
	}



	static String getStackTrace(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String stackTrace = sw.toString();
		return stackTrace;
	}

}
