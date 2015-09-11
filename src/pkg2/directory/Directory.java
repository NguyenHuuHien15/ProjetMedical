package pkg2.directory;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import pkg1.main_home_screen.*;
import android.R.anim;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class Directory extends Activity {

	TextView txtPatients;

	ListView listPatients;
	String[] patients = new String[] { "Hien", "Hiep", "Antoine", "Lucas",
			"Antony", "Brieuc", "Thomas", "Luis", "Lucie", "Alexendre",
			"Jérôme", "Eric", "Michel", "Francois", "Thiery", "Claude",
			"Alexandra", "Vincent" };

	ImageButton btnCallToJava;
	ImageButton btnBack;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.directory);

		DisplayMetrics display = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(display);

		int widthOfScreen = display.widthPixels;
		int heightOfScreen = display.heightPixels;

		txtPatients = (TextView) findViewById(R.id.txtPatients);
		txtPatients.setMinimumWidth(widthOfScreen);

		listPatients = (ListView) findViewById(R.id.list_Patients);
		ArrayAdapter<String> adapter_List = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_checked, patients);

		listPatients.setMinimumHeight(heightOfScreen - 200);
		listPatients.setChoiceMode(listPatients.CHOICE_MODE_SINGLE);

		listPatients.setAdapter(adapter_List);
		patients = sortAscending(patients);

		adapter_List.setNotifyOnChange(true);

		btnCallToJava = (ImageButton) findViewById(R.id.btnCall);
		btnCallToJava.setMinimumWidth(widthOfScreen / 3);
		btnCallToJava.setMaxWidth(widthOfScreen / 3);
		btnCallToJava.setVisibility(View.INVISIBLE);
		btnCallToJava.setEnabled(false);

		listPatients.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				btnCallToJava.setVisibility(View.VISIBLE);
				btnCallToJava.setEnabled(true);

				CheckedTextView item = (CheckedTextView) view;

				// ListView Clicked item index
				int itemPosition = position;

				// ListView Clicked item value
				String itemValue = (String) listPatients
						.getItemAtPosition(position);

				// Show Alert
				Toast.makeText(getApplicationContext(),
						"Vous avez choisi le patient: " + itemValue,
						Toast.LENGTH_SHORT).show();

			}
		});

		btnCallToJava.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(
						"pkg3.view_preview.Main_View_Preview_AfterSendACall"));
				btnCallToJava.setVisibility(View.INVISIBLE);
				btnCallToJava.setEnabled(false);
			}
		});

		btnBack = (ImageButton) findViewById(R.id.btnBack);
		btnBack.setMinimumWidth(widthOfScreen / 3);
		btnBack.setMaxWidth(widthOfScreen / 3);

		btnBack.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish(); // aller au main activite
			}
		});

	}

	private String[] sortAscending(String[] input) {
		List<String> sortedList = Arrays.asList(input);
		Collections.sort(sortedList);

		String[] output = (String[]) sortedList.toArray();
		return output;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onRestart()
	 */
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return super.onOptionsItemSelected(item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return super.onContextItemSelected(item);
	}

}
