package edu.hogwarts.siesta.ircclient;



import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;

import java.net.Socket;

import java.util.Random;



class Irc implements Runnable {

	static final long serialVersionUID = 143445254L; 

	IrcClient ircClient=null;

	Thread thread=null;

	Socket socket=null;
	BufferedWriter ircWriter=null;
	BufferedReader ircReader=null;

	String usr=null,ins=null,src=null,dst=null,par=null,txt=null,tmp=null;



	Irc(IrcClient ircClient) {

		this.ircClient=ircClient;

		connect();

	}



	public void connect() {

		if(thread!=null) {
			cancel();
		}

		thread=new Thread(this);
		thread.start();

	} 

	

	public void run() {

		String line;

		try {

			ircClient.display("--> Connecting...");

			socket=new Socket(IrcClient.srv,IrcClient.prt);
			ircWriter=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			ircReader=new BufferedReader(new InputStreamReader(socket.getInputStream()));

			if(IrcClient.pss!=null) ircWrite("PASS "+IrcClient.pss);	
			ircWrite("NICK "+IrcClient.nck);		
			ircWrite("USER "+IrcClient.nck+" "+IrcClient.nck+" "+IrcClient.nck+" :"+IrcClient.nck);

			while(!Thread.interrupted()) {

				line=ircRead();

				if(line!=null) {

					if(line.charAt(0)==':') {

						tmp=line;

						tmp=skip(tmp,":");
						usr=cut(tmp,"!");
						tmp=skip(tmp," ");
						ins=cut(tmp," ");
						tmp=skip(tmp," ");
						dst=cut(tmp," ");
						dst=skip(dst,":");
						tmp=skip(tmp,":");
						txt=tmp;

						if(ins.equalsIgnoreCase("PRIVMSG")) {
							ircClient.display(dst+" <"+usr+"> "+txt);
						} else if(ins.equalsIgnoreCase("JOIN")) {
							ircClient.display("--> "+usr+" joins "+dst);
						} else if(ins.equalsIgnoreCase("PART")) {
							ircClient.display("--> "+usr+" leaves "+dst);
						} else if(ins.equalsIgnoreCase("QUIT")) {
							ircClient.display("--> "+usr+" quits "+txt);
						} else if(ins.equalsIgnoreCase("NICK")) {
							ircClient.display("--> "+usr+" is now known as "+txt);
						} else if(ins.equals("432")) {
							Random random=new Random();
							ircWrite("NICK "+String.format("unnamed-%04x", random.nextInt(65536)));
							ircWrite("PRIVMSG nickserv@services.dal.net :RELEASE "+IrcClient.nck+" "+IrcClient.pss);
							ircWrite("NICK "+IrcClient.nck);		
						} else if(ins.equals("433")) {
							Random random=new Random();
							ircWrite("NICK "+String.format("unnamed-%04x", random.nextInt(65536)));
							ircWrite("PRIVMSG nickserv@services.dal.net :GHOST "+IrcClient.nck+" "+IrcClient.pss);
							ircWrite("NICK "+IrcClient.nck);
						} else if(ins.equals("332")) {
							tmp=line;
							usr=cut(tmp,"!");
							tmp=skip(tmp," ");
							ins=cut(tmp," ");
							tmp=skip(tmp," ");
							src=cut(tmp," ");
							tmp=skip(tmp," ");
							dst=cut(tmp," ");
							tmp=skip(tmp,":");								
							txt=tmp;
							ircClient.display("--> TOPIC "+dst+": "+txt);
						} else {
							ircClient.display("--> "+line);
						}
					}
				
					if(line.toUpperCase().startsWith("PING")) {
						ircWrite("PONG "+line.substring(5));
					} else if(ins.equals("001")) {
						ircClient.display("--> Connected.");
						ircWrite("JOIN "+IrcClient.chn);
					}
				}
			}

			socket.close();

		} catch(Exception e) {
			ircClient.display("Error: "+e.toString());
		}
	}


	public void cancel() {
		thread.interrupt();
	}


	void parse(String line) {

		if(line.toUpperCase().startsWith(ircClient.PFX+"NICK")) {
			if(line.length()>=6) {
				IrcClient.nck=line.substring(6);
				ircWrite("NICK "+IrcClient.nck);
				ircClient.display("--> NICK is set to "+IrcClient.nck);
			}
		} else if(line.toUpperCase().startsWith(ircClient.PFX+"PASS")) {
			if(line.length()>=6) {
				IrcClient.pss=line.substring(6);
				ircWrite("PASS "+IrcClient.pss);
			}
		} else if(line.toUpperCase().startsWith(ircClient.PFX+"PORT")) {
			if(line.length()>=6) {
				IrcClient.prt=Integer.valueOf(line.substring(6));
				ircClient.display("--> PORT is set to "+IrcClient.prt);
			}
		} else if(line.toUpperCase().startsWith(ircClient.PFX+"TARGET")) {
			if(line.length()>=8) {
				ircClient.tgt=line.substring(8);
				ircClient.display("--> TARGET is set to "+ircClient.tgt);
			}
		} else if(line.equalsIgnoreCase(ircClient.PFX+"CONNECT")) {
			connect();
		} else if(line.toUpperCase().startsWith(ircClient.PFX+"JOIN")) {
			if(line.length()>=6) {
				ircClient.tgt=line.substring(6);
				ircWrite("JOIN "+ircClient.tgt);
			}
		} else if(line.toUpperCase().startsWith(ircClient.PFX+"MSG")) {
			if(line.length()>=7) {
				line=skip(line," ");
				dst=cut(line," ");
				txt=skip(line," ");
				ircClient.display(dst+" <"+IrcClient.nck+"> "+txt);
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
				ircClient.display(ircClient.tgt+" <"+IrcClient.nck+"> "+line);
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
			ircClient.display("Error: "+e.toString());
		}
	}



	String ircRead() {
		String line=null;
		try {
			line=ircReader.readLine();
//				ircClient.display("--> "+line);
		} catch(Exception e) {
			ircClient.display("Error: "+e.toString());
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
