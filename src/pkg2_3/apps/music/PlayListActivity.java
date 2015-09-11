package pkg2_3.apps.music;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import pkg1.main_home_screen.*;

public class PlayListActivity extends ListActivity {
	// Songs list
	private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
	String folder;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playlist);

		ArrayList<HashMap<String, String>> songsListData = new ArrayList<HashMap<String, String>>();
		
		// here
		Bundle bundle = getIntent().getExtras();
		folder = bundle.getString("folder");

		SongsManager plm = new SongsManager();
		plm.setFolder(folder);
		// get all songs from sdcard
		this.songsList = plm.getPlayList();

		// looping through playlist
		for (int i = 0; i < songsList.size(); i++) {
			// creating new HashMap
			HashMap<String, String> song = songsList.get(i);

			// adding HashList to ArrayList
			songsListData.add(song);
		}

		// Adding menuItems to ListView
		ListAdapter adapter = new SimpleAdapter(this, songsListData,
				R.layout.playlist_item, new String[] { "songTitle" }, new int[] {
						R.id.songTitle });

		setListAdapter(adapter);

		// selecting single ListView item
		ListView lv = getListView();
		//lv.setBackgroundColor(Color.BLUE);
		
		// listening to single listitem click
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// getting listitem index
				int songIndex = position;
				
				// Starting new intent to recall main activity to play music
				Intent in = new Intent(getApplicationContext(),
						AndroidBuildingMusicPlayerActivity.class);
				// Sending songIndex to PlayerActivity
				in.putExtra("songIndex", songIndex);
				in.putExtra("folder", folder);
				Log.i("Folder in playlist; ", folder);
				setResult(101, in);
				
				finish();
			}
		});

	}
	
	public void onBackPressed() {
		Intent in = new Intent(getApplicationContext(),
				AndroidBuildingMusicPlayerActivity.class);
		setResult(102, in); // to recall main activity to call directory
		
		finish();
		
	}
}
