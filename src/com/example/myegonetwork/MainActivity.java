package com.example.myegonetwork;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Request.Callback;
import com.facebook.Response;
import com.facebook.Session;

public class MainActivity extends FragmentActivity {

	private static final String TAG = "myEgoNetwork";
	private final Long initialTime = Long.valueOf(1000);
	private Long time = initialTime;
	/* FQL queries */
	private static final String queryEgoFriends = "select uid1, uid2 from friend where uid1 = me()";
	private static final String queryMutualFriends = "select uid1, uid2 from friend where uid1 in (select uid1 from friend where uid2 = me()) and uid2 in (select uid2 from friend where uid1 = me())";
	private static final String queryPosts = "select created_time from stream where actor_id = %d and source_id = %d limit 1000000";
	private static final String queryUserReceivedPosts = "select source_id, actor_id, created_time, now() from stream where source_id = %d and actor_id in (%s) limit 100000000";
	private MainFragment mainFragment;
	private Long egoId;
	private Hashtable<Long, Hashtable<Long, RelationshipVariables>> users2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		users2 = new Hashtable<Long, Hashtable<Long,RelationshipVariables>>();

		if (savedInstanceState == null) {
			// Add the fragment on initial activity setup
			mainFragment = new MainFragment();
			getSupportFragmentManager()
			.beginTransaction()
			.add(android.R.id.content, mainFragment)
			.commit();
		} else {
			// Or set the fragment from restored state info
			mainFragment = (MainFragment) getSupportFragmentManager()
					.findFragmentById(android.R.id.content);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}

	/* method to process the download */
	public void downloadFriendsAsync(View view){
		// download relationships of the user (list of friendships)
		sendFqlRequest(queryEgoFriends, new Request.Callback(){ 
			public void onCompleted(Response response) {
				processFriendList(response, false);
			}
		});
	}
	private void DownloadPostsAsync(){
		// for each relationship download communication data
		//final Iterator<Map.Entry<RelationshipKey, RelationshipVariables>> it = users.entrySet().iterator();
		final Iterator<Map.Entry<Long, Hashtable<Long, RelationshipVariables>>> it = users2.entrySet().iterator();
		if(it.hasNext()){
			Map.Entry<Long, Hashtable<Long, RelationshipVariables>> entry = (Map.Entry<Long, Hashtable<Long, RelationshipVariables>>) it.next();
			Long userId = entry.getKey();
			Set<Long> friendIds = entry.getValue().keySet();
			String friendIdsString = "";
			Iterator<Long> fi = friendIds.iterator();
			while (fi.hasNext()){
				friendIdsString += fi.next();
				if (fi.hasNext()){
					friendIdsString += ",";
				}
			}
			final Long localUid = userId;
			final String localFriendIdsString = friendIdsString;
			sendFqlRequest(String.format(queryUserReceivedPosts, userId, friendIdsString), new Request.Callback(){ 
				public void onCompleted(Response response) {
					processPosts(response, localUid, localFriendIdsString, it);
				}
			});
		}
	}

	private void saveDataset(){
		//save the obtained dataset to file
		String dirName = "FBdatasets";
		String dsName = "egods.dat";
		if (isExternalStorageWritable()){
			PrintWriter writer = null;
			try {
				writer = new PrintWriter(new BufferedWriter(new FileWriter(getDatasetStorageDir(dirName).getAbsolutePath() + File.separator + dsName)));
				//file header
				writer.println("UID1" + "\t" + "UID2" + "\t" + "NUM_POSTS" + "\t" + "DURATION");
				Iterator<Map.Entry<Long, Hashtable<Long, RelationshipVariables>>> it = users2.entrySet().iterator();
				while(it.hasNext()){
					Map.Entry<Long, Hashtable<Long, RelationshipVariables>> entry = (Map.Entry<Long, Hashtable<Long, RelationshipVariables>>) it.next();
					Iterator<Map.Entry<Long, RelationshipVariables>> it2 = entry.getValue().entrySet().iterator();
					while(it2.hasNext()){
						Map.Entry<Long, RelationshipVariables> subEntry = it2.next();
						writer.println(entry.getKey() + "\t" + subEntry.getKey() + "\t" + subEntry.getValue().getPosts() + "\t" + (subEntry.getValue().getDownloadDate() - subEntry.getValue().getFirstPost()));
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally{
				if (writer != null){
					writer.close();
				}
			}
		}
		else{
			// TODO notify failure
		}
	}

	private void sendFqlRequest(String fqlQuery, Callback callback){
		Bundle params = new Bundle();
		params.putString("q", fqlQuery);

		Session session = Session.getActiveSession();
		Request request = new Request(session, 
				"/fql", 
				params, 
				HttpMethod.GET,
				callback
				);
		Request.executeBatchAsync(request);
	}

	private void processFriendList(Response response, boolean finished) {
		try {
			JSONArray friendships = new JSONArray(response.getGraphObject().getProperty("data").toString());
			Long userId = null;
			for (int i = 0; i < friendships.length(); i++) {
				userId = Long.parseLong(friendships.getJSONObject(i).get("uid1").toString());
				Long friendId = Long.parseLong(friendships.getJSONObject(i).get("uid2").toString());
				//RelationshipKey rk = new RelationshipKey(userId, friendId);
				//users.put(rk, new RelationshipVariables());
				if (!users2.containsKey(userId)){
					users2.put(userId, new Hashtable<Long, RelationshipVariables>());
				}
				users2.get(userId).put(friendId, new RelationshipVariables());
			}
			if (!finished && userId != null){
				egoId = userId;
			}
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (!finished){
			// download the list of friendships amongst user's friends
			sendFqlRequest(queryMutualFriends, new Request.Callback(){ 
				public void onCompleted(Response response) {
					processFriendList(response, true);
				}
			});

		}
		else{
			Log.d(TAG, users2.size() +"");
			Log.d(TAG, egoId + "");
			//add egoid to each friend before downloading posts
			Iterator<Map.Entry<Long, Hashtable<Long, RelationshipVariables>>> it = users2.entrySet().iterator();
			while(it.hasNext()){
				Map.Entry<Long, Hashtable<Long, RelationshipVariables>> entry = (Map.Entry<Long, Hashtable<Long, RelationshipVariables>>) it.next();
				if (!entry.getKey().equals(egoId)){
					entry.getValue().put(egoId, new RelationshipVariables());
				}
			}
			Log.d(TAG, "Finished building relationships, now downloading posts");
			DownloadPostsAsync();
		}
	}

	static int totalPosts = 0;
	static int friendNo = 0;
	private void processPosts(Response response, Long uid, String fIds, Iterator<Map.Entry<Long, Hashtable<Long, RelationshipVariables>>> it) {
		final Iterator<Map.Entry<Long, Hashtable<Long, RelationshipVariables>>> localIt = it;
		try{
			if (response.getError() != null){
				Log.e(TAG, "Got error from FB: " + response.getError());
				try {
					Thread.sleep(time);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				time *= 2;
				//throw new FBErrorException(response.getError().getErrorMessage());
			}
			if (response.getError() == null){
				JSONArray posts = new JSONArray(response.getGraphObject().getProperty("data").toString());
				totalPosts += posts.length();
				friendNo += 1;
				Log.d(TAG, "-> " + friendNo + ": saving posts received by user: total "+totalPosts);
				for (int i = 0; i < posts.length(); i++) {
					Long userId = Long.parseLong(posts.getJSONObject(i).get("source_id").toString());
					Long friendId = Long.parseLong(posts.getJSONObject(i).get("actor_id").toString());
					users2.get(userId).get(friendId).incPosts();
					Long actual = Long.parseLong(posts.getJSONObject(i).get("created_time").toString());
					Long oldest = users2.get(userId).get(friendId).getFirstPost();
					if (oldest == 0 || actual < oldest){
						users2.get(userId).get(friendId).setFirstPost(actual);
					}
					users2.get(userId).get(friendId).setDownloadDate(Long.parseLong(posts.getJSONObject(i).get("anon").toString()));
				}
			}
			if (localIt.hasNext()){
				Map.Entry<Long, Hashtable<Long, RelationshipVariables>> entry = (Map.Entry<Long, Hashtable<Long, RelationshipVariables>>) localIt.next();
				Long userId = entry.getKey();
				Set<Long> friendIds = entry.getValue().keySet();
				String friendIdsString = "";
				Iterator<Long> fi = friendIds.iterator();
				while (fi.hasNext()){
					friendIdsString += fi.next();
					if (fi.hasNext()){
						friendIdsString += ",";
					}
				}
				final Long localUid = userId;
				final String localFriendIdsString = friendIdsString;
				sendFqlRequest(String.format(queryUserReceivedPosts, userId, friendIdsString), new Request.Callback(){ 
					public void onCompleted(Response response) {
						processPosts(response, localUid, localFriendIdsString, localIt);
					}
				});
			}
			else{
				saveDataset();
			}
/*
		} catch (FBErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			final Long localUid = uid;
			final String localFriendIdsString = fIds;
			sendFqlRequest(String.format(queryUserReceivedPosts, localUid, localFriendIdsString), new Request.Callback(){ 
				public void onCompleted(Response response) {
					processPosts(response, localUid, localFriendIdsString, localIt);
				}
			});
			time = initialTime;
*/		} catch (JSONException e1){
			e1.printStackTrace();
		}
	}

	/* Checks if external storage is available for read and write */
	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	public File getDatasetStorageDir(String dirName) {
		// Get the directory for the user's public pictures directory. 
		File file = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_DOWNLOADS), dirName);
		if (!file.mkdirs()) {
			Log.e(TAG, "Directory not created");
		}
		return file;
	}


}