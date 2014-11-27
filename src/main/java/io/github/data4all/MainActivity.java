package io.github.data4all;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class MainActivity extends Activity {	

	
	Button start;
	Dialog matchText;
	List<String> matchesText;
	ListView textList;
	private static final int REQUEST_CODE = 1234;
	/**
	 * Called when the activity is first created.
	 * 
	 * @param savedInstanceState
	 *            If the activity is being re-initialized after previously being
	 *            shut down then this Bundle contains the data it most recently
	 *            supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it
	 *            is null.</b>
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		start = (Button) findViewById(R.id.speech);
		start.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
		        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		        startActivityForResult(intent, REQUEST_CODE);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	 @Override
	 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	     if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
		     matchText = new Dialog(MainActivity.this);
		     matchText.setContentView(R.layout.dialog_matches);
		     matchText.setTitle(R.string.selectTag);
		     textList = (ListView)matchText.findViewById(R.id.list);
		     matchesText = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
		     matchesTag();
		     ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, matchesText);
		     textList.setAdapter(adapter);
		     textList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		    	 @Override
		    	 public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		    		 start.setText(matchesText.get(0));   		 
		    		 matchText.hide();
		    		
	     }
	 });
		     matchText.show();
	     }
	     super.onActivityResult(requestCode, resultCode, data);
	    }
	 
	 private void matchesTag(){
		List<String> list = new ArrayList<String>();
		List<String> copyData = new ArrayList<String>();
		list.add("Hallo");
		list.add("motorway");
		for (String tag : list) {
			for (String matches : matchesText) {
				if(tag.equals(matches)){
					copyData.add(tag);
				}
			}
		}	
			matchesText = (ArrayList<String>) copyData;
			
	 }
}
