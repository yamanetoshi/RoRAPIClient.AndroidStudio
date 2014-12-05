package jp.shuri.yamanetoshi.rorapiclient;

import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import jp.shuri.yamanetoshi.json.JSONFunctions;
import android.os.Bundle;
import android.os.Handler;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class RoRClientActivity extends ListActivity {

	private final String TAG = "RoRClientActivity";
	
	private final String BASE_URL = "http://frozen-sands-2986.herokuapp.com/";
	private final String DELETE = "tasks/";
	private final String TASKS = "tasks.json";
    private final String PLEASE_WAIT = "please wait...";
    protected static final String MODE = "mode";
    protected static final String ID = "id";
    protected static final String NAME = "name";
	
	private DefaultHttpClient mHttpClient = new DefaultHttpClient();
	private Handler mHandler = new Handler();
	
	private ProgressDialog mProgressDialog;
	
	private JSONArray mList;
	private String[] mArray;

    MultiChoiceModeListener mActionModeCallback = new MultiChoiceModeListener() {

        private ActionMode mMode;
        private MyAdapter mAdapter;
        private SparseBooleanArray mCheckedItemPositions;

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.setTitle("対象を選択");
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return true;
        }
        
        private void requestDelete(ActionMode mode, MyAdapter adapter) {
			Log.d(TAG, "requestDelete() start");

			mMode = mode;
        	mAdapter = adapter;

            new Thread(new Runnable() {

				@Override
				public void run() {
					Log.d(TAG, "adapter.getCount() is " + mAdapter.getCount());
		        	for (int i = 0; i < mAdapter.getCount(); i++) {
		        		boolean checked = mCheckedItemPositions.get(i);
		        		if (checked) {
		        			Log.d(TAG, "posision (" + i + ") is checked");
		        			try {
		        				String id = mList.getJSONObject(i).getString("id");
		        				Log.d(TAG, "URL is " + BASE_URL + DELETE + id);
		    					JSONFunctions.DELETEfromURL(BASE_URL + DELETE + id, 
		    							mHttpClient);
		    				} catch (Exception e) {
		    					Log.d(TAG, "e is " + e);
		    				}
		        		}
		        	}
                	mHandler.post(new Runnable() {
						@Override
						public void run() {
							mMode.finish();
							mProgressDialog.dismiss();
							updateList();
						}
                	});
				}
        		
        	}).start();
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            SparseBooleanArray checkedItemPositions = getListView().getCheckedItemPositions();
            MyAdapter adapter = (MyAdapter) getListView().getAdapter();

            //mProgressDialog.show();
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    mProgressDialog.show();
                }

            });

            mCheckedItemPositions = new SparseBooleanArray();
            for(int i = 0; i < adapter.getCount(); i++) {
                mCheckedItemPositions.put(i, checkedItemPositions.get(i));
                Log.d(TAG, "mCheckedItemPositions.get(" + i + ") : " + mCheckedItemPositions.get(i));
            }

            requestDelete(mode, adapter);
        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
                boolean checked) {
        }
    };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ListView lv = getListView();
		lv.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
		lv.setMultiChoiceModeListener(mActionModeCallback);
//		lv.setBackgroundColor(R.color.white);
		
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> container, View view, int position,
					long id) {
	        	Intent i = new Intent(RoRClientActivity.this, RoRClientEdit.class);
	        	try {
	        		i.putExtra(NAME, mList.getJSONObject(position).getString("name"));
	        		i.putExtra(ID, mList.getJSONObject(position).getString("id"));
	        	} catch (JSONException e) {
	        		Log.e(TAG, "JSON Error : " + e);
	        	}
	        	i.setAction(Intent.ACTION_EDIT);
	        	int requestCode = 123;
	            startActivityForResult(i, requestCode);

				Toast.makeText(RoRClientActivity.this, 
						"position " + position + " id " + id, Toast.LENGTH_LONG).show();
			}
		});
		
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage(PLEASE_WAIT);
        
		mProgressDialog.show();
		
		updateList();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	 
		switch (requestCode) {
		case 123:
			if (resultCode == RESULT_OK) {
				updateList();
			}
		}
	}

	private void updateList() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					mList = new JSONArray(JSONFunctions.GETfromURL(BASE_URL + TASKS, mHttpClient));
				} catch (Exception e) {
					Log.d(TAG, "e is " + e);
				}
				mHandler.post(new Data2List());
			}
		}).start();	
	}
	
	private class Data2List implements Runnable {

		@Override
		public void run() {
			mArray = new String [mList.length()];
			for (int i = 0; i < mList.length(); i++) {
				try {
					mArray[i] = mList.getJSONObject(i).getString("name");
				} catch (JSONException e) {
					Log.e(TAG, e.toString());
				}
			}
			/*
	        ArrayAdapter<String> adapter = new ArrayAdapter<String>(RoRClientActivity.this,
	        		android.R.layout.simple_list_item_1, mArray);
	        		*/
			MyAdapter adapter = new MyAdapter(RoRClientActivity.this, R.layout.row, mArray);
			setListAdapter(adapter);
			
			mProgressDialog.dismiss();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.list_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.add:
//        	Toast.makeText(this, "add", Toast.LENGTH_LONG).show();
        	Intent i = new Intent(this, RoRClientEdit.class);
            i.putExtra(NAME, "");
            i.putExtra(ID, "");
        	i.setAction(Intent.ACTION_INSERT);
        	int requestCode = 123;
            startActivityForResult(i, requestCode);
        	/*
            Intent intent = new Intent(this, PaintActivity.class);
            startActivity(intent);
            */

            return true;
        case R.id.delete:
        	Toast.makeText(this, "delete", Toast.LENGTH_LONG).show();

        	/*
            if (0 < mImages.getAdapter().getCount()) {
                mImages.startActionMode(mActionModeCalback);
                // 一つ選択しないと連続選択のモードにならないので
                mImages.setItemChecked(0, true);
            }
            */
            return true;
        default:
        	return super.onOptionsItemSelected(item);
        }
	}
	
    private class MyAdapter extends ArrayAdapter<String> {
        private String [] items;
        private LayoutInflater     inflater;
 
        public MyAdapter(Context context, int resourceId,
                String[] items) {
            super(context, resourceId, items);
            this.items = items;
            this.inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
 
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = inflater.inflate(R.layout.row, null);
            }
            String item = items[position];
            TextView textView = (TextView) view
                    .findViewById(R.id.row_textview);
            textView.setText(item);
            return view;
        }
    }
}
