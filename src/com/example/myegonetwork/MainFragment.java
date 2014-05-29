/**
 * 
 */
package com.example.myegonetwork;

import java.util.Arrays;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;

/**
 * @author Valerio Arnaboldi (valerio.arnaboldi@gmail.com)
 *
 */
public class MainFragment extends Fragment {

	//private static final String TAG = "MainFragment"; 
	private Button downloadButton;
	private UiLifecycleHelper uiHelper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		uiHelper = new UiLifecycleHelper(getActivity(), callback);
		uiHelper.onCreate(savedInstanceState);
	}

	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
		if (state.isOpened()){
			downloadButton.setEnabled(true);
		}
		else if (state.isClosed()){
			downloadButton.setEnabled(false);
		}
	}
	
	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, 
			ViewGroup container, 
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_main, container, false);
		
		LoginButton authButton = (LoginButton) view.findViewById(R.id.authButton);
		authButton.setFragment(this);
		authButton.setReadPermissions(Arrays.asList("user_location", "user_birthday", "user_likes", "user_friends", "user_activities", "user_birthday", "user_education_history", "user_events", "user_groups", "user_hometown", "user_interests", "user_likes", "user_location", "user_photos", "user_relationships", "user_relationship_details", "user_religion_politics", "user_status", "user_videos", "user_website", "user_work_history", "read_friendlists", "read_stream", "read_insights", "read_mailbox", "friends_about_me", "friends_actions.news", "friends_birthday", "friends_events", "friends_hometown", "friends_location", "friends_photo_video_tags", "friends_relationship_details", "friends_status", "friends_website", "friends_actions.video", "friends_checkins", "friends_games_activity", "friends_interests", "friends_notes", "friends_photos", "friends_relationships", "friends_subscriptions", "friends_work_history", "friends_actions.music", "friends_activities", "friends_education_history", "friends_groups", "friends_likes", "friends_online_presence", "friends_questions", "friends_religion_politics", "friends_videos"));
		downloadButton = (Button) view.findViewById(R.id.download_button);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();

		// For scenarios where the main activity is launched and user
		// session is not null, the session state change notification
		// may not be triggered. Trigger it if it's open/closed.
		Session session = Session.getActiveSession();
		if (session != null &&
				(session.isOpened() || session.isClosed()) ) {
			onSessionStateChange(session, session.getState(), null);
		}

		uiHelper.onResume();
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}
}
