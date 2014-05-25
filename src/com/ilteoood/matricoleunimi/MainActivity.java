package com.ilteoood.matricoleunimi;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Html;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class MainActivity extends Activity 
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Button cerca=(Button)findViewById(R.id.cerca);
		Button cacheM=(Button)findViewById(R.id.cacheM);
		final TextView mailMatricola=(TextView)findViewById(R.id.mailMatricola);
		final MultiAutoCompleteTextView campoMatricola=(MultiAutoCompleteTextView)findViewById(R.id.campoMatrico);
		inizializzaMatricole();
		campoMatricola.setOnLongClickListener(new OnLongClickListener()
		{
			public boolean onLongClick(View v) 
			{
				campoMatricola.setText("");
				return false;
			}
			
		});
		campoMatricola.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) 
			{
				campoMatricola.setText(campoMatricola.getText().toString().replace(",","").trim());
			}
		});
		campoMatricola.setOnEditorActionListener(new OnEditorActionListener() 
		{
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) 
			{
				cerca();
				return false;
			}
		});
		mailMatricola.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0) 
			{
				if(mailMatricola.getText().toString()!="")
				{
					Intent i = new Intent(Intent.ACTION_SEND);
					i.setType("message/rcf882");
					i.putExtra(Intent.EXTRA_EMAIL, new String[]{mailMatricola.getText().toString().trim()});
					i.putExtra(Intent.EXTRA_SUBJECT, "");
					i.putExtra(Intent.EXTRA_TEXT, "");
					startActivity(Intent.createChooser(i, "E-Mail:"));
				}
			}
			
		});
		cerca.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v) 
			{
				cerca();
			}
		});
		cacheM.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v) 
			{
				cancellaDati();
				inizializzaMatricole();
				Toast.makeText(MainActivity.this,"La cronologia delle ultime ricerche è stata cancellata!",Toast.LENGTH_SHORT).show();
			}
		});
	}
	public void cerca()
	{
		final MultiAutoCompleteTextView campoMatricola=(MultiAutoCompleteTextView)findViewById(R.id.campoMatrico);
		if(campoMatricola.length()==0)
		{
			Toast.makeText(getApplicationContext(), "Nessuna matricola inserita!", Toast.LENGTH_SHORT).show();
		}
		else
		{
			aggiungiMatricola(campoMatricola.getText().toString().trim());
			inizializzaMatricole();
			CercaMatricola cm = new CercaMatricola();
			cm.execute();
		}
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
	public void inizializzaMatricole()
	{
		MultiAutoCompleteTextView campoMatricola=(MultiAutoCompleteTextView)findViewById(R.id.campoMatrico);
		SharedPreferences sp=getSharedPreferences("matricole",Context.MODE_PRIVATE);
		int n=sp.getInt("n", 0);
		String[] s=new String[n];
		for(int i=0;i<n;i++)
			s[i]=sp.getString(Integer.toString(i), "");
		ArrayAdapter<String> adapter=new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_dropdown_item_1line,s);
		campoMatricola.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
		campoMatricola.setAdapter(adapter);
	}
	public void aggiungiMatricola(String s)
	{
		boolean trovato=false;
		String matri;
		SharedPreferences sp=getSharedPreferences("matricole",Context.MODE_PRIVATE);
		int n=sp.getInt("n", 0);
		SharedPreferences.Editor pf=sp.edit();
		for(int i=0;i<n;i++)
		{
			matri=sp.getString(Integer.toString(i), "");
			if(s.equals(matri))
			{
				trovato=true;
				break;
			}
		}
		if(trovato==false)
		{
			pf.putString(Integer.toString(n),s);
			pf.putInt("n", ++n);
		}
		pf.commit();
	}
	public void cancellaDati()
	{
		SharedPreferences sp=getSharedPreferences("matricole",Context.MODE_PRIVATE);
		SharedPreferences.Editor pf=sp.edit();
		pf.clear();
		pf.commit();
	}
	class CercaMatricola extends AsyncTask <Void,Void,Void>
	{
		ProgressDialog dialog=new ProgressDialog(MainActivity.this);
		final EditText campoMatricola=(EditText)findViewById(R.id.campoMatrico);
		final TextView nomeMatricola=(TextView)findViewById(R.id.campoNome);
		final TextView mailMatricola=(TextView)findViewById(R.id.mailMatricola);
		Document doc;
		URL urli;
		int risposta;
		String parametri,s,risultato;
		Elements parsa;
		String[] formattazioneDati;
		protected void onPreExecute()
		{
			dialog.setMessage("Caricamento in corso...");
			dialog.show();
		}
		protected Void doInBackground(Void... params) 
		{
			try
			{
				urli=new URL("http://www.divtlc.unimi.it/Studenti/cercastud.php");
				HttpURLConnection http=(HttpURLConnection) urli.openConnection();
				http.setRequestMethod("POST");
				http.setUseCaches(false);
				parametri="matricola="+campoMatricola.getText().toString();
				DataOutputStream wr = new DataOutputStream(http.getOutputStream());
				wr.writeBytes(parametri);
				wr.flush();
				wr.close();
				risposta=http.getResponseCode();
				if(risposta==HttpURLConnection.HTTP_NOT_FOUND)
					Toast.makeText(MainActivity.this, "Errore nel raggiungimento della pagina, contattare lo sviluppatore", Toast.LENGTH_LONG).show();
				else if(risposta==HttpURLConnection.HTTP_OK)
				{
					BufferedReader br=new BufferedReader(new InputStreamReader(http.getInputStream()));
					while((s=br.readLine())!=null)
						if(!s.equals(""))
							risultato+=s+"\n";
				}
			}
			catch(MalformedURLException e)
			{} 
			catch (IOException e) {}
			doc=Jsoup.parse(risultato);
			parsa=doc.getElementsByClass("txtb");
			formattazioneDati=parsa.toString().split("</td>");
			return null;
		}
		protected void onPostExecute(Void result)
		{
			try
			{
				dialog.dismiss();
				if(formattazioneDati.length>3)
				{
					nomeMatricola.setText(formattazioneDati[4].replace("<td class=\"txtb\">", "").trim());
					mailMatricola.setText(Html.fromHtml("<u>"+formattazioneDati[5].replace("<td class=\"txtb\">", "").trim()+"</u>"));
				}
				else
				{
					parsa=doc.getElementsByTag("b");
					String ris=parsa.toString();
					if(ris.contains("Mi dispiace, ma hai gi&agrave; fatto troppe richieste, prova pi&uacute; tardi"))
						Toast.makeText(getApplicationContext(), "Hai fatto troppe richieste al server.\nAttendi qualche minuto!", Toast.LENGTH_LONG).show();
					else
						Toast.makeText(getApplicationContext(), "Nessuna matricola trovata!", Toast.LENGTH_LONG).show();
					nomeMatricola.setText("");
					mailMatricola.setText("");
				}
			}
			catch(NullPointerException e)
			{
				Toast.makeText(MainActivity.this, "Nessuna connesione ad internet!", Toast.LENGTH_SHORT).show();
			}
		}
	}

}