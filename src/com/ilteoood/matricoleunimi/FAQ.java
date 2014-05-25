package com.ilteoood.matricoleunimi;


import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FAQ extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_faq);
		ListView listaDomande=(ListView)findViewById(R.id.listaFAQ);
		ArrayAdapter<String> domande=new ArrayAdapter<String>(FAQ.this, R.drawable.listview);
		domande.add("Perchè a volte mi compare la scritta: \n\"Hai fatto troppe richieste al server...\"?");
		domande.add("Come posso evitarlo?");
		domande.add("Come posso contattare lo sviluppatore dell'app?");
		domande.add("Da dove provengono i dati che l'applicazione mostra?");
		listaDomande.setAdapter(domande);
		listaDomande.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) 
			{
				final AlertDialog.Builder risposta=new AlertDialog.Builder(FAQ.this);
				risposta.setTitle("Risposta:");
				switch(arg2)
				{
				case 0:
					risposta.setMessage("L'Università blocca per 15 minuti, circa, tutti coloro che effettuano troppe ricerche.\nViene fatto per evitare una congestione del server.");
					break;
				case 1:
					risposta.setMessage("In nessun modo, ma puoi raggirare il problema (se usi una connessione mobile) semplicemente effettuando una riconnessione.");
					break;
				case 2:
					risposta.setMessage("Nella sezione Contattami hai la possibilità di mandarmi una e-mail.");
					break;
				case 3:
					risposta.setMessage("I dati vengono presi da un sito che l'Università mette a disposizione di tutti.");
					break;
				}
				risposta.setPositiveButton("Ok", null);
				risposta.show();
			}
		});
		
	}
	/*public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.faq, menu);
		return true;
	}*/

}
