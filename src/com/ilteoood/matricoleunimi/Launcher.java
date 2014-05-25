package com.ilteoood.matricoleunimi;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class Launcher extends Activity 
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launcher);
		TextView testoMatricola=(TextView)findViewById(R.id.testoMatricola);
		TextView testoNome=(TextView)findViewById(R.id.testoNome);
		ImageView imgMatricola=(ImageView)findViewById(R.id.imgMatricola);
		ImageView imgNome=(ImageView)findViewById(R.id.imgNome);
		imgMatricola.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v) 
			{
				avviaMatricola();
			}
		});
		testoMatricola.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v) 
			{
				avviaMatricola();
			}
		});
		imgNome.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v) 
			{
				avviaNome();
			}
		});
		testoNome.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v) 
			{
				avviaNome();
			}
		});
	}
	public void avviaMatricola()
	{
		Intent intento=new Intent(Launcher.this,MainActivity.class);
		startActivity(intento);
	}
	public void avviaNome()
	{
		Intent intento=new Intent(Launcher.this,TrovaDaNome.class);
		startActivity(intento);
	}
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		menu.add("Contattami").setOnMenuItemClickListener(new OnMenuItemClickListener()
		{
			public boolean onMenuItemClick(MenuItem item) 
			{
				Intent intento=new Intent(Intent.ACTION_SEND);
				intento.setType("message/rcf882");
				intento.putExtra(Intent.EXTRA_EMAIL, new String[]{"matteopietro.dazzi@gmail.com"});
				intento.putExtra(Intent.EXTRA_SUBJECT, "Assistenza per matricole UniMi");
				startActivity(Intent.createChooser(intento, "E-Mail:"));
				return false;
			}
			
		});
		menu.add("FAQ").setOnMenuItemClickListener(new OnMenuItemClickListener()
		{

			@Override
			public boolean onMenuItemClick(MenuItem item) 
			{
				Intent FAQ=new Intent(getApplicationContext(),FAQ.class);
				startActivity(FAQ);
				return false;
			}
			
		});
		return true;
	}
}
