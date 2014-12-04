package jp.shuri.yamanetoshi.rorapiclient;

import java.util.ArrayList;
import java.util.List;

import jp.shuri.yamanetoshi.json.JSONFunctions;

import org.apache.http.NameValuePair;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class RoRClientEdit extends Activity {
	private final String TAG = "RoRClientEdit";
	
	private String mode;
	private String id;
	private String name;
	
	private String URL = "http://shrouded-tundra-4125.herokuapp.com/";
	private DefaultHttpClient mDefaultHttpClient = new DefaultHttpClient();
	private String POST = "tasks";
	private String PUT = "tasks/";
	private String PLEASE_WAIT = "please wait...";
	
	private EditText mEditText;
	private ProgressDialog mProgressDialog;
	
	private List<NameValuePair> mParams;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		id = getIntent().getStringExtra(RoRClientActivity.ID);
		name = getIntent().getStringExtra(RoRClientActivity.NAME);
		mode = getIntent().getAction();
		
		Log.d(TAG, "id is " + id);
		Log.d(TAG, "name is " + name);
		Log.d(TAG, "mode is " + mode);
		
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage(PLEASE_WAIT);
		
		mEditText = (EditText)findViewById(R.id.edittext);
		mEditText.setText(name);
		
		Button btn = (Button)findViewById(R.id.button);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
                mParams = new ArrayList<NameValuePair>();
                SpannableStringBuilder sb = (SpannableStringBuilder)mEditText.getText();
                String str = sb.toString();
                mParams.add(new BasicNameValuePair("[task][name]", str));

                mProgressDialog.show();
            	                
				if (mode.equals(Intent.ACTION_INSERT)) {
                    new Thread(new Runnable() {

            			@Override
            			public void run() {
                            try {
                             	JSONFunctions.POSTfromURL(URL + POST, mDefaultHttpClient, mParams);
                				setResult(RESULT_OK, new Intent());
                				mProgressDialog.dismiss();
                				finish();
                            } catch (Exception e) {
                               	Log.e(TAG, "Exception + " + e);
                            }
            			}
                    		
                    }).start();
				} else if (mode.equals(Intent.ACTION_EDIT)) {
                    new Thread(new Runnable() {

            			@Override
            			public void run() {
                            try {
                             	JSONFunctions.PUTfromURL(URL + PUT + id, mDefaultHttpClient, mParams);
                				setResult(RESULT_OK, new Intent());
                				mProgressDialog.dismiss();
                				finish();
                            } catch (Exception e) {
                               	Log.e(TAG, "Exception + " + e);
                            }
            			}
                    		
                    }).start();
					
				}
			}
			
		});
	}
}
