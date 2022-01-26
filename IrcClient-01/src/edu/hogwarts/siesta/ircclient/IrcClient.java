package edu.hogwarts.siesta.ircclient;



import android.app.Activity;

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

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.net.Socket;



public class IrcClient extends Activity {

	String srv = "sakura.jp.as.dal.net";
	String nck = "siesta";
	String chn = "#pantasya";
	String pss = null;
	int prt = 6667;

	String tgt=chn;

	final String PFX = "/"; 



	EditText cmd;
	ScrollView scl;	
	TextView dpy;
	Button btn;



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
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

		setContentView(R.layout.ircclient);

		cmd = (EditText) findViewById(R.id.editText1);
		scl = (ScrollView) findViewById(R.id.scrollView1);
		dpy = (TextView) findViewById(R.id.textView1);
		btn = (Button) findViewById(R.id.button1);

		Typeface typeface = Typeface.createFromAsset(getAssets(),"font/Typori-Regular.ttf");

		cmd.setTypeface(typeface);
		dpy.setTypeface(typeface);
		btn.setTypeface(typeface);

		cmd.setTextSize(24);
		dpy.setTextSize(24);
		btn.setTextSize(24);

		try {
			irc=new Irc(this);
			irc.socket=new Socket(srv,prt);
			irc.ircWriter=new BufferedWriter(new OutputStreamWriter(irc.socket.getOutputStream()));
			irc.ircReader=new BufferedReader(new InputStreamReader(irc.socket.getInputStream()));
		} catch(Exception e) {
			display("Error: "+getStackTrace(e));			
		}
		
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				String line=cmd.getText().toString();

				irc.parse(line);				

				cmd.setText("");
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
