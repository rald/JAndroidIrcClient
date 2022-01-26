package edu.hogwarts.siesta.ircclient;

import android.app.Service;

import android.content.Intent;

import android.os.Bundle;
import android.os.IBinder;



class IrcService extends Service {

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Bundle bundle = intent.getExtras();

		if(bundle!=null) {

			Irc irc = (Irc) bundle.getSerializable("irc");		

			Thread thread=new Thread(irc);

			thread.start();

		}
		
		return START_STICKY;
    }
  
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
