package com.ilteoood.matricoleunimi;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class TrovaDaNome extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trova_da_nome);
		final TextView mailMatricola=(TextView)findViewById(R.id.mailMatricola);
		final EditText campoNome=(EditText)findViewById(R.id.campoNome);
		final MultiAutoCompleteTextView campoCognome=(MultiAutoCompleteTextView)findViewById(R.id.campoCognome);
		final Spinner campoMatricola=(Spinner)findViewById(R.id.matricola);
		Button cerca=(Button)findViewById(R.id.cerca);
		Button cache=(Button)findViewById(R.id.cacheM);
		inizializzaCognomi();
		campoCognome.setOnLongClickListener(new OnLongClickListener()
		{
			public boolean onLongClick(View v) 
			{
				campoCognome.setText("");
				return false;
			}
		}
		);
		campoCognome.setOnEditorActionListener(new TextView.OnEditorActionListener() 
		{
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) 
			{
				cerca();
				return false;
			}
		});
		campoNome.setOnLongClickListener(new OnLongClickListener()
		{
			public boolean onLongClick(View v) 
			{
				campoNome.setText("");
				return false;
			}
		}
		);
		campoNome.setOnEditorActionListener(new OnEditorActionListener() 
		{
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) 
			{
				cerca();
				return false;
			}
		});
		campoCognome.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) 
			{
				campoCognome.setText(campoCognome.getText().toString().replace(",", "").trim());
				campoNome.setText(trovaNomi(campoCognome.getText().toString()));
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
		campoMatricola.setOnItemSelectedListener(new OnItemSelectedListener()
		{
			public void onItemSelected(AdapterView<?> arg0, View arg1,int arg2, long arg3) 
			{
				Bundle x=getIntent().getExtras();
				mailMatricola.setText(x.getString(Integer.toString(arg2+1)));
			}
			public void onNothingSelected(AdapterView<?> arg0) 
			{}
		});
		cerca.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v) 
			{
					cerca();	
			}
		});
		cache.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v) 
			{
				cancellaDati();
				inizializzaCognomi();
				Toast.makeText(TrovaDaNome.this,"La cronologia delle ultime ricerche è stata cancellata!",Toast.LENGTH_SHORT).show();
			}
		});
	}
	public void cerca()
	{
		final MultiAutoCompleteTextView campoCognome=(MultiAutoCompleteTextView)findViewById(R.id.campoCognome);
		final EditText campoNome=(EditText)findViewById(R.id.campoNome);
		if(campoNome.getText().length()==0)
			Toast.makeText(getApplicationContext(), "Nessun nome inserito!", Toast.LENGTH_SHORT).show();
		else if(campoCognome.getText().length()==0)
		{
			Toast.makeText(getApplicationContext(), "Nessun cognome inserito!", Toast.LENGTH_SHORT).show();
		}
		else
		{
			aggiornaDati(campoNome.getText().toString().trim(),campoCognome.getText().toString().trim());
			inizializzaCognomi();
			CercaMatricola cm = new CercaMatricola();
			cm.execute();
		}
	}
	public void inizializzaCognomi()
	{
		final MultiAutoCompleteTextView campoCognome=(MultiAutoCompleteTextView)findViewById(R.id.campoCognome);
		SharedPreferences sp=getSharedPreferences("cognomiSalvati",Context.MODE_PRIVATE);
		int n=sp.getInt("n", 0);
		String[] cognomi=new String[n];
		for (int i=0;i<n;i++)
			cognomi[i]=(sp.getString(Integer.toString(i), ""));
		ArrayAdapter<String> set=new ArrayAdapter<String>(TrovaDaNome.this,android.R.layout.simple_dropdown_item_1line,cognomi);
		campoCognome.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
		campoCognome.setAdapter(set);
	}
	public String trovaNomi(String x)
	{
		SharedPreferences sp=getSharedPreferences("cognomiSalvati",Context.MODE_PRIVATE);
		SharedPreferences sp1=getSharedPreferences("nomiSalvati",Context.MODE_PRIVATE);
		int n=sp.getInt("n", 0);
		for(int i=0;i<n;i++)
		{
			String cogn=sp.getString(Integer.toString(i), "").trim();
			if(cogn.equals(x))
				return sp1.getString(Integer.toString(i), "");
		}
		return "";
	}
	public void aggiornaDati(String nome, String cognome)
	{
		boolean trovato=false;
		String cogn;
		SharedPreferences sp=getSharedPreferences("cognomiSalvati",Context.MODE_PRIVATE);
		SharedPreferences sp1=getSharedPreferences("nomiSalvati",Context.MODE_PRIVATE);
		int n=sp.getInt("n", 0);
		for (int i=0;i<n;i++)
		{
			cogn=sp.getString(Integer.toString(i), "").trim();
			if(cognome.equals(cogn))
				if(nome.equals(sp1.getString(Integer.toString(i), "").trim()))
				{
					trovato=true;
					break;
				}
		}
		if(trovato==false)
		{
			salvaDati(n,cognome,nome);
		}
	}
	public void salvaDati(int n,String cognome, String nome)
	{
		SharedPreferences sp=getSharedPreferences("cognomiSalvati",Context.MODE_PRIVATE);
		SharedPreferences sp1=getSharedPreferences("nomiSalvati",Context.MODE_PRIVATE);
		SharedPreferences.Editor pf=sp.edit();
		SharedPreferences.Editor pf1=sp1.edit();
		pf.putString(Integer.toString(n), cognome);
		pf1.putString(Integer.toString(n), nome);
		pf.putInt("n", ++n);
		pf.commit();
		pf1.commit();
	}
	public void cancellaDati()
	{
		SharedPreferences sp=getSharedPreferences("cognomiSalvati",Context.MODE_PRIVATE);
		SharedPreferences sp1=getSharedPreferences("nomiSalvati",Context.MODE_PRIVATE);
		SharedPreferences.Editor pf=sp.edit();
		SharedPreferences.Editor pf1=sp1.edit();
		pf.clear();
		pf1.clear();
		pf.commit();
	}
	class CercaMatricola extends AsyncTask <Void,Void,Void>
	{
		ProgressDialog dialog=new ProgressDialog(TrovaDaNome.this);
		final EditText campoNome=(EditText)findViewById(R.id.campoNome);
		final EditText campoCognome=(EditText)findViewById(R.id.campoCognome);
		final Spinner campoMatricola=(Spinner)findViewById(R.id.matricola);
		final TextView mailMatricola=(TextView)findViewById(R.id.mailMatricola);
		final ArrayAdapter<String> matricole=new ArrayAdapter<String>(TrovaDaNome.this, R.drawable.spinner);
		Document doc;
		Elements parsa;
		String parametri,s,risultato;
		String[] formattazioneDati;
		int risposta;
		protected void onPreExecute()
		{
			dialog.setMessage("Caricamento in corso...");
			dialog.show();
		}
		protected Void doInBackground(Void... params) 
		{
			try
			{
				URL urli=new URL("http://www.divtlc.unimi.it/Studenti/cercastud.php");
				HttpURLConnection http=(HttpURLConnection) urli.openConnection();
				http.setRequestMethod("POST");
				http.setUseCaches(false);
				parametri="cognome=\""+campoCognome.getText().toString()+"\"&nome=\""+campoNome.getText().toString()+"\"";
				DataOutputStream dos=new DataOutputStream(http.getOutputStream());
				dos.writeBytes(parametri);
				dos.flush();
				dos.close();
				risposta=http.getResponseCode();
				if(risposta==HttpURLConnection.HTTP_NOT_FOUND)
					Toast.makeText(TrovaDaNome.this, "Errore nel raggiungimento della pagina, contattare lo sviluppatore", Toast.LENGTH_LONG).show();
				else if(risposta==HttpURLConnection.HTTP_OK)
				{
					BufferedReader br=new BufferedReader(new InputStreamReader(http.getInputStream()));
					while((s=br.readLine())!=null)
						if(!s.equals(""))
							risultato+=s+"\n";
				}
			}
			catch(Exception e){}
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
					int i;
					Bundle ex=new Bundle();
					for(i=1;i<formattazioneDati.length/3;i++)
					{
						matricole.add(formattazioneDati[(3*i)+1].replace("<td class=\"txtb\">", "").trim());
						ex.putString(Integer.toString(i),formattazioneDati[(3*i)+3].replace("<td class=\"txtb\">", "").trim());
					}
					if(i>2)
					{
						AlertDialog.Builder adb=new AlertDialog.Builder(TrovaDaNome.this);
						adb.setTitle("Attenzione!");
						adb.setMessage("Sono state trovate più matricole per lo stesso nome!\nClicca sulla matricola per vedere tutte quelle disponibili.");
						adb.setIcon(android.R.drawable.ic_dialog_alert);
						adb.setNeutralButton("Ok",null);
						adb.show();
					}
					campoMatricola.setAdapter(matricole);
					getIntent().putExtras(ex);
					mailMatricola.setText(Html.fromHtml("<u>"+formattazioneDati[6].replace("<td class=\"txtb\">", "").trim()+"</u>"));
				}
				else
				{
					parsa=doc.getElementsByTag("b");
					String ris=parsa.toString();
					if(ris.contains("Mi dispiace, ma hai gi&agrave; fatto troppe richieste, prova pi&uacute; tardi"))
						Toast.makeText(getApplicationContext(), "Hai fatto troppe richieste al server.\nAttendi qualche minuto!", Toast.LENGTH_LONG).show();
					else
						Toast.makeText(getApplicationContext(), "Nessuno studente trovato!", Toast.LENGTH_LONG).show();
					matricole.clear();
					campoMatricola.setAdapter(matricole);
					mailMatricola.setText("");
				}
			}
			catch(NullPointerException e)
			{
				Toast.makeText(TrovaDaNome.this, "Nessuna connesione ad internet!", Toast.LENGTH_SHORT).show();
			}
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

	
