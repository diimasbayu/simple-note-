package com.example.note;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class NoteEdit extends Activity{

	public static int numTitle = 1;	
	public static String curDate = "";
	public static String curTime = "";
	public static String curText = "";	
    private EditText mTitleText;
    private EditText mBodyText;
    private TextView mDateText;
    private Long mRowId;
    private Cursor note;
    private NotesDbAdapter mDbHelper;
    

    private int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_CAMERA = 2;

    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_GALERY = 2;

    public static final String IMAGE_DIRECTORY_NAME = "image";

    private Uri fileUri;
    public  String filePath;

    private Bitmap bitmap;

    ProgressDialog pDialog;
    Button btnkamera;
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_edit);
        setTitle(R.string.app_name);
        //check kamera
        if (!isDeviceSupportCamera()) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! Your device doesn't support camera",
                    Toast.LENGTH_LONG).show();
            finish();
        }
        
        //button camera
        btnkamera = (Button) findViewById(R.id.btnkamera1);
        btnkamera.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				alertDialog();
				
			}
		});        
        
        
        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();        
        
        

        mTitleText = (EditText) findViewById(R.id.title);
        mBodyText = (EditText) findViewById(R.id.body);
        mDateText = (TextView) findViewById(R.id.notelist_date);

        long msTime = System.currentTimeMillis();  
        Date curDateTime = new Date(msTime);
 	
        SimpleDateFormat formatter = new SimpleDateFormat("d'/'M'/'y");  
        curDate = formatter.format(curDateTime);        
        
        mDateText.setText(""+curDate);
        

        mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(NotesDbAdapter.KEY_ROWID);
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = extras != null ? extras.getLong(NotesDbAdapter.KEY_ROWID)
                                    : null;
        }
        populateFields();
        
       }
	
	
	  public static class LineEditText extends EditText{
			public LineEditText(Context context, AttributeSet attrs) {
				super(context, attrs);
					mRect = new Rect();
			        mPaint = new Paint();
			        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
			        mPaint.setColor(Color.BLUE);
			}
			private Rect mRect;
		    private Paint mPaint;	    
		    
		    @Override
		    protected void onDraw(Canvas canvas) {
		  
		        int height = getHeight();
		        int line_height = getLineHeight();

		        int count = height / line_height;

		        if (getLineCount() > count)
		            count = getLineCount();

		        Rect r = mRect;
		        Paint paint = mPaint;
		        int baseline = getLineBounds(0, r);

		        for (int i = 0; i < count; i++) {

		            canvas.drawLine(r.left, baseline + 1, r.right, baseline + 1, paint);
		            baseline += getLineHeight();

		        super.onDraw(canvas);
		    }
		}
	  }
	  
	  @Override
	    protected void onSaveInstanceState(Bundle outState) {
	        super.onSaveInstanceState(outState);
	        saveState();
	        outState.putSerializable(NotesDbAdapter.KEY_ROWID, mRowId);
	        outState.putParcelable("file_uri", fileUri);
	    }
	  @Override
	    protected void onRestoreInstanceState(Bundle savedInstanceState) {
	        super.onRestoreInstanceState(savedInstanceState);

	        fileUri = savedInstanceState.getParcelable("file_uri");
	    }

	  
	  @Override
	    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	        super.onActivityResult(requestCode, resultCode, data);

	        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
	            fileUri = data.getData();
	            try {
	                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), fileUri);
	                
	                filePath = getPath(fileUri);

	                Toast.makeText(getApplication(), filePath, Toast.LENGTH_LONG).show();

	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }else if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
	            if (resultCode == RESULT_OK) {

	                filePath = fileUri.getPath();

	                
	                BitmapFactory.Options options = new BitmapFactory.Options();
	                options.inSampleSize = 8;
	                final Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
	                Toast.makeText(getApplication(), filePath, Toast.LENGTH_LONG).show();
	            }else if (resultCode == RESULT_CANCELED) {
	                Toast.makeText(getApplicationContext(),
	                        "User cancelled image capture", Toast.LENGTH_SHORT)
	                        .show();
	            } else {
	                Toast.makeText(getApplicationContext(),
	                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
	                        .show();
	            }
	        }
	    }

	  public String getPath(Uri uri) {
	        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
	        cursor.moveToFirst();
	        String document_id = cursor.getString(0);
	        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
	        cursor.close();

	        cursor = getContentResolver().query(
	                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
	                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
	        cursor.moveToFirst();
	        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
	        cursor.close();

	        return path;
	    }

	    @Override
	    protected void onPause() {
	        super.onPause();
	        saveState();
	    }
	    
	    @Override
	    protected void onResume() {
	        super.onResume();
	        populateFields();
	    }
	    
		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			getMenuInflater().inflate(R.menu.noteedit_menu, menu);
			return true;		
		}
		
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
		    switch (item.getItemId()) {
		    case R.id.menu_about:
		            	
		        AlertDialog.Builder dialog = new AlertDialog.Builder(NoteEdit.this);
		        dialog.setTitle("About");
		        dialog.setMessage("Program Notes App untuk UAS Mobile Programming" );
		        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					
		        	   @Override
		        	   public void onClick(DialogInterface dialog, int which) {
		        		   dialog.cancel();
						
		        	   }
		           });
		           dialog.show();	           
		           return true;
		    case R.id.menu_delete:
				if(note != null){
	    			note.close();
	    			note = null;
	    		}
	    		if(mRowId != null){
	    			mDbHelper.deleteNote(mRowId);
	    		}
	    		finish();
		    	
		        return true;
		    case R.id.menu_save:
	    		saveState();
	    		finish();	    	
		    default:
		    	return super.onOptionsItemSelected(item);
		    }
		}
	    
	    private void saveState() {
	        String title = mTitleText.getText().toString();
	        String body = mBodyText.getText().toString();

	        if(mRowId == null){
	        	long id = mDbHelper.createNote(title, body, curDate);
	        	if(id > 0){
	        		mRowId = id;
	        	}else{
	        		Log.e("saveState","failed to create note");
	        	}
	        }else{
	        	if(!mDbHelper.updateNote(mRowId, title, body, curDate)){
	        		Log.e("saveState","failed to update note");
	        	}
	        }
	    }
	    
	  
	    private void populateFields() {
	        if (mRowId != null) {
	            note = mDbHelper.fetchNote(mRowId);
	            startManagingCursor(note);
	            mTitleText.setText(note.getString(
	    	            note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)));
	            mBodyText.setText(note.getString(
	                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)));
	            curText = note.getString(
	                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY));
	        }
	    }
	    
	    //cek kamera device
	    private Boolean isDeviceSupportCamera() {
            if (getApplicationContext().getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_CAMERA)) {
                return true;
            } else {
                return false;
            }
        }
	    
	    private void captureImage() {
	        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

	        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
	        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

	        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
	    }

	    private void showGalery() {
	        Intent intent = new Intent();
	        intent.setType("image/*");
	        intent.setAction(Intent.ACTION_GET_CONTENT);
	        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
	    }
	    
	    public Uri getOutputMediaFileUri(int type) {
	        return Uri.fromFile(getOutputMediaFile(type));
	    }
	    
	    private static File getOutputMediaFile(int type) {

	        File mediaStorageDir = new File(
	                Environment
	                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
	                IMAGE_DIRECTORY_NAME);

	        if (!mediaStorageDir.exists()) {
	            if (!mediaStorageDir.mkdirs()) {

	                return null;
	            }
	        }

	        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
	                Locale.getDefault()).format(new Date());
	        File mediaFile;
	        if (type == MEDIA_TYPE_IMAGE) {
	            mediaFile = new File(mediaStorageDir.getPath() + File.separator
	                    + "IMG_" + timeStamp + ".jpg");
	        }else if (type == MEDIA_TYPE_GALERY) {
	            mediaFile = new File(mediaStorageDir.getPath() + File.separator
	                    + "IMG_" + timeStamp );
	        } else {
	            return null;
	        }
	        return mediaFile;
	    }

	    private void alertDialog() {
	        AlertDialog.Builder builder = new AlertDialog.Builder(NoteEdit.this);
	        builder.setTitle("alert");
	        builder.setMessage("Pilih dengan apa anda akan mengupload foto");

	        String galery = getString(R.string.galery);
	        builder.setNeutralButton(galery,
	                new DialogInterface.OnClickListener() {
	                    @Override
	                    public void onClick(DialogInterface dialog, int which) {
	                        showGalery();
	                    }
	                });

	        String kamera = getString(R.string.camera);
	        builder.setPositiveButton(kamera,
	                new DialogInterface.OnClickListener() {
	                    @Override
	                    public void onClick(DialogInterface dialog, int which) {
	                    captureImage();
	                    }
	                });
	        AlertDialog dialog = builder.create();
	        dialog.show();
	    }
}

