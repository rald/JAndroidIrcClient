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

import java.net.Socket;

import java.util.Random;

public class IrcClient extends Activity {

	String srv = "sakura.jp.as.dal.net";
	String nck = "siesta";
	String chn = "#pantasya";
	String pss = null;
	int prt = 6667;

	final String PFX = "/"; 

	String tgt=chn;

	EditText cmd;
	ScrollView scl;	
	TextView dpy;
	Button btn;

	Irc irc = null;

	Thread thread;

	Socket socket;
	BufferedWriter ircWriter;
	BufferedReader ircReader;

	String usr=null,ins=null,dst=null,par=null,txt=null;

	Random random=new Random();



	private final Handler myTextHandler = new Handler(new Handler.Callback() {
	    @Override
	    public boolean handleMessage(Message stringMessage) {
			dpy.append((String)stringMessage.obj);

			scl.postDelayed(new Runnable() {
			    @Override
			    public void run() {
			        scl.smoothScrollTo(0,dpy.getBottom());
			    }
			}, 1000);

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

			socket = new Socket(srv,prt);
			
			ircWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

			ircReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		} catch(Exception e) {
			display("Error: "+e.toString());
		} 

		irc=new Irc();
		thread=new Thread(irc);

		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				String line=cmd.getText().toString();

				if(line.toUpperCase().startsWith(PFX+"NICK")) {
					if(line.length()>=6) {
						nck=line.substring(6);
						irc.ircWrite("NICK "+nck);
						display("NICK is set to "+nck);
					}
				} else if(line.toUpperCase().startsWith(PFX+"PASS")) {
					if(line.length()>=6) {
						pss=line.substring(6);
						irc.ircWrite("PASS "+pss);
					}
				} else if(line.toUpperCase().startsWith(PFX+"PORT")) {
					if(line.length()>=6) {
						prt=Integer.valueOf(line.substring(6));
						display("PORT is set to "+prt);
					}
				} else if(line.toUpperCase().startsWith(PFX+"TARGET")) {
					if(line.length()>=8) {
						tgt=line.substring(8);
						display("TARGET is set to "+tgt);
					}
				} else if(line.equalsIgnoreCase(PFX+"CONNECT")) {
					thread.start();
				} else if(line.toUpperCase().startsWith(PFX+"JOIN")) {
					if(line.length()>=6) {
						irc.ircWrite("JOIN "+line.substring(6));
					}
				} else if(line.toUpperCase().startsWith(PFX+"MSG")) {
					if(line.length()>=7) {
						line=skip(line," ");
						tgt=cut(line," ");
						txt=skip(line," ");
						irc.ircWrite("PRIVMSG "+tgt+" :"+txt);
					}
				} else if(line.toUpperCase().startsWith(PFX+"QUIT")) {
					String msg=null; 
					if(line.length()>=6) {
						msg=line.substring(6);
						irc.ircWrite("QUIT :"+msg);						
					} else {
						irc.ircWrite("QUIT");
					}
				} else if(line.toUpperCase().startsWith(PFX+"RAW")) {
					if(line.length()>=5) {
						irc.ircWrite(line.substring(5));
					}
				} else {
					if(line.length()>0) {
						display(tgt+" <"+nck+"> "+line);
						irc.ircWrite("PRIVMSG "+tgt+" :"+line);
					}
				}
				
				cmd.setText("");
			}
		});


	}



	public void display(String line) {
		Message stringMessage = Message.obtain(myTextHandler);
		stringMessage.obj = line+"\r\n";
		stringMessage.sendToTarget();
	}



	class Irc implements Runnable {

		@Override
		public void run() {

			try {

				String line;

				display("--> Connecting...");

				irc.ircWrite("USER "+nck+" "+nck+" "+nck+" :"+nck);

				while((line=ircRead())!=null) {

					if(line.charAt(0)==':') {
						line=skip(line,":");
						usr=cut(line,"!");
						line=skip(line," ");
						ins=cut(line," ");
						line=skip(line," ");
						dst=cut(line," ");
						dst=skip(dst,":");
						line=skip(line,":");
						txt=line;

						if(ins.equalsIgnoreCase("PRIVMSG")) {
							display(dst+" <"+usr+"> "+txt);
						} else if(ins.equalsIgnoreCase("JOIN")) {
							display("--> "+usr+" joins "+dst);
						} else if(ins.equalsIgnoreCase("PART")) {
							display("--> "+usr+" leaves "+dst);
						} else if(ins.equalsIgnoreCase("QUIT")) {
							display("--> "+usr+" quits "+txt);
						} else if(ins.equalsIgnoreCase("353")) {
							display("--> "+txt);
						} else if(ins.equalsIgnoreCase("433")) {
							display("--> "+txt);
						} else if(ins.equalsIgnoreCase("432")) {
							ircWrite("NICK "+String.format("unnamed-%04x", random.nextInt(65536)));
							ircWrite("PRIVMSG nickserv@services.dal.net :RELEASE "+nck+" "+pss);
							ircWrite("NICK "+nck);		
						
						} else if(ins.equalsIgnoreCase("474")) {
							display("--> "+txt);
						}
					}
				
					if(line.toUpperCase().startsWith("PING")) {
						ircWrite("PONG "+line.substring(5));
					} else if(ins.equals("001")) {
						display("--> Connected.");
						ircWrite("JOIN "+chn);
					}
				}

				socket.close();

			} catch(Exception e) {
				display("Error: "+e.toString());
			}
		}



		void ircWrite(String line) {
			try {
//				display("<-- "+line);
				ircWriter.write(line+"\r\n");
				ircWriter.flush();
			} catch(Exception e) {
				display("Error: "+e.toString());
			}
		}



		String ircRead() {
			String line=null;
			try {
				line=ircReader.readLine();
//				display("--> "+line);
			} catch(Exception e) {
				display("Error: "+e.toString());
			}
			return line;
		}

	}



	String cut(String str1,String str2) {
		int pos=str1.indexOf(str2);
		if(pos!=-1) {
			return str1.substring(0,pos);
		}
		return str1;
	}
	


	String skip(String str1,String str2) {
		int pos=str1.indexOf(str2);
		if(pos!=-1) {
			return str1.substring(pos+str2.length());
		}
		return str1;
	}



}
