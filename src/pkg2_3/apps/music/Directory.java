package pkg2_3.apps.music;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import pkg1.main_home_screen.R;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class Directory extends ListActivity {

	private Vector<String> directoriesOfMp3 = new Vector<String>();
	private ListView listdirectories;
	private String[] directories;
	private ArrayList<HashMap<String, String>> directoryList = new ArrayList<HashMap<String, String>>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.directory_list);

		String directory = "/sdcard";
		// searchInDirectory(directory);
		searchAllMp3Directory(directory);

		directories = new String[directoriesOfMp3.size()];

		for (int i = 0; i < directoriesOfMp3.size(); i++) {
			
			directories[i] = directoriesOfMp3.elementAt(i);
		}

		listdirectories = getListView();
		listdirectories.setChoiceMode(listdirectories.CHOICE_MODE_SINGLE);

		directories = sortAscending(directories);

		// Adding menuItems to ListView
		ArrayList<HashMap<String, String>> directoryListData = getDirectoryList();
		
		ListAdapter adapter = new SimpleAdapter(this, directoryListData,
				R.layout.directory_item, new String[] { "directoryTitle" },
				new int[] { R.id.directoryTitle });

		setListAdapter(adapter);

		// selecting single ListView item

		listdirectories.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				int itemPosition = position;

				
				HashMap<String, String> item = (HashMap<String, String>) listdirectories
						.getItemAtPosition(position);
				
				String itemValue = item.get("directoryPath");

				// Show Alert
				Toast.makeText(getApplicationContext(),
						"Vous avez choisi le répertoire: " + itemValue,
						Toast.LENGTH_SHORT).show();

				// recall to main activity to call playlist
				Intent i = new Intent(getApplicationContext(),
						AndroidBuildingMusicPlayerActivity.class);
				// here
				Bundle b = new Bundle();
				b.putString("folder", itemValue);
				i.putExtras(b);
				setResult(100, i);
				

				// Closing repertoires view
				finish();

			}
		});

	}

	private ArrayList<HashMap<String, String>> getDirectoryList() {

		for (int i = 0; i < directories.length; i++) {
			HashMap<String, String> directory = new HashMap<String, String>();
			// enlever "/sdcard/"
			directory.put("directoryTitle", directories[i].substring(8));
			directory.put("directoryPath", directories[i]);
			// Adding each directory to directoryList
			directoryList.add(directory);
		}
		// return directorys list array
		return directoryList;
	}

	// modified by Hien
	private void searchAllMp3Directory(String directory_root) {
		File dir = new File(directory_root);

		if (dir.canRead() && dir.exists() && dir.isDirectory()) {
			String[] filesInDirectory = dir.list();
			if (filesInDirectory != null) {
				boolean flag = true;

				for (int i = 0; i < filesInDirectory.length; i++) {

					File file = new File(directory_root + "/"
							+ filesInDirectory[i]);

					if (file.isFile()
							&& file.getAbsolutePath()
									.toLowerCase(Locale.getDefault())
									.endsWith(".mp3")) {
						// directoriesOfMp3.add(directory + "/" + filesInDirectory[i]);
						if (flag) {
							directoriesOfMp3.add(directory_root);
							flag = false;
						}

					}

					else if (file.isDirectory()) {
						searchAllMp3Directory(file.getAbsolutePath());
					}

				}
			}
		}
	}

	private String[] sortAscending(String[] input) {
		List<String> sortedList = Arrays.asList(input);
		Collections.sort(sortedList);

		String[] output = (String[]) sortedList.toArray();
		return output;
	}

}
