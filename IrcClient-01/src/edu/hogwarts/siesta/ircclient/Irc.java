package edu.hogwarts.siesta.ircclient;


import android.content.Intent;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.Serializable;

import java.net.Socket;

import java.util.Random;



class Irc implements Serializable,Runnable {

	static final long serialVersionUID = 143445254L; 

	transient IrcClient ircClient=null;

	transient Socket socket=null;
	transient BufferedWriter ircWriter=null;
	transient BufferedReader ircReader=null;

	transient String usr=null,ins=null,dst=null,par=null,txt=null;



	Irc(IrcClient ircClient) {
		this.ircClient=ircClient;
	}

	

	public void run() {

		try {

			String line;

			ircClient.display("--> Connecting...");

			ircWrite("USER "+ircClient.nck+" "+ircClient.nck+" "+ircClient.nck+" :"+ircClient.nck);

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
						ircClient.display(dst+" <"+usr+"> "+txt);
					} else if(ins.equalsIgnoreCase("JOIN")) {
						ircClient.display("--> "+usr+" joins "+dst);
					} else if(ins.equalsIgnoreCase("PART")) {
						ircClient.display("--> "+usr+" leaves "+dst);
					} else if(ins.equalsIgnoreCase("QUIT")) {
						ircClient.display("--> "+usr+" quits "+txt);
					} else if(ins.equalsIgnoreCase("353")) {
						ircClient.display("--> "+txt);
					} else if(ins.equalsIgnoreCase("433")) {
						ircClient.display("--> "+txt);
					} else if(ins.equalsIgnoreCase("432")) {
						Random random=new Random();
						ircWrite("NICK "+String.format("unnamed-%04x", random.nextInt(65536)));
						ircWrite("PRIVMSG nickserv@services.dal.net :RELEASE "+ircClient.nck+" "+ircClient.pss);
						ircWrite("NICK "+ircClient.nck);		
					
					} else if(ins.equalsIgnoreCase("474")) {
						ircClient.display("--> "+txt);
					}
				}
			
				if(line.toUpperCase().startsWith("PING")) {
					ircWrite("PONG "+line.substring(5));
				} else if(ins.equals("001")) {
					ircClient.display("--> Connected.");
					ircWrite("JOIN "+ircClient.chn);
				}
			}

			socket.close();

		} catch(Exception e) {
			ircClient.display("Error: "+IrcClient.getStackTrace(e));
		}
	}



	void parse(String line) {

		if(line.toUpperCase().startsWith(ircClient.PFX+"NICK")) {
			if(line.length()>=6) {
				ircClient.nck=line.substring(6);
				ircWrite("NICK "+ircClient.nck);
				ircClient.display("NICK is set to "+ircClient.nck);
			}
		} else if(line.toUpperCase().startsWith(ircClient.PFX+"PASS")) {
			if(line.length()>=6) {
				ircClient.pss=line.substring(6);
				ircWrite("PASS "+ircClient.pss);
			}
		} else if(line.toUpperCase().startsWith(ircClient.PFX+"PORT")) {
			if(line.length()>=6) {
				ircClient.prt=Integer.valueOf(line.substring(6));
				ircClient.display("PORT is set to "+ircClient.prt);
			}
		} else if(line.toUpperCase().startsWith(ircClient.PFX+"TARGET")) {
			if(line.length()>=8) {
				ircClient.tgt=line.substring(8);
				ircClient.display("TARGET is set to "+ircClient.tgt);
			}
		} else if(line.equalsIgnoreCase(ircClient.PFX+"CONNECT")) {

			try {
				Intent intent=new Intent(ircClient,IrcService.class);
				intent.putExtra("irc",this);
				ircClient.startService(intent);
			} catch(Exception e) {
				ircClient.display("Error: "+IrcClient.getStackTrace(e));
			}

		} else if(line.toUpperCase().startsWith(ircClient.PFX+"JOIN")) {
			if(line.length()>=6) {
				ircWrite("JOIN "+line.substring(6));
			}
		} else if(line.toUpperCase().startsWith(ircClient.PFX+"MSG")) {
			if(line.length()>=7) {
				line=skip(line," ");
				ircClient.tgt=cut(line," ");
				txt=skip(line," ");
				ircWrite("PRIVMSG "+ircClient.tgt+" :"+txt);
			}
		} else if(line.toUpperCase().startsWith(ircClient.PFX+"QUIT")) {
			String msg=null; 
			if(line.length()>=6) {
				msg=line.substring(6);
				ircWrite("QUIT :"+msg);						
			} else {
				ircWrite("QUIT");
			}
		} else if(line.toUpperCase().startsWith(ircClient.PFX+"RAW")) {
			if(line.length()>=5) {
				ircWrite(line.substring(5));
			}
		} else {
			if(line.length()>0) {
				ircClient.display(ircClient.tgt+" <"+ircClient.nck+"> "+line);
				ircWrite("PRIVMSG "+ircClient.tgt+" :"+line);
			}
		}
	}



	void ircWrite(String line) {
		try {
//				ircClient.display("<-- "+line);
			ircWriter.write(line+"\r\n");
			ircWriter.flush();
		} catch(Exception e) {
			ircClient.display("Error: "+IrcClient.getStackTrace(e));
		}
	}



	String ircRead() {
		String line=null;
		try {
			line=ircReader.readLine();
//				ircClient.display("--> "+line);
		} catch(Exception e) {
			ircClient.display("Error: "+IrcClient.getStackTrace(e));
		}
		return line;
	}


	
	static String cut(String str1,String str2) {
		int pos=str1.indexOf(str2);
		if(pos!=-1) {
			return str1.substring(0,pos);
		}
		return str1;
	}
	


	static String skip(String str1,String str2) {
		int pos=str1.indexOf(str2);
		if(pos!=-1) {
			return str1.substring(pos+str2.length());
		}
		return str1;
	}



}
