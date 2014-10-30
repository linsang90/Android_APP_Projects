package sl.rutgers.robustdownload;

import java.text.DecimalFormat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.DownloadManager;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;

/**
 * RobustDownload
 * 
 * @author Sang Lin 
 * @NetId: sl1010
 */

public class DownloadActivity extends Activity {

	public static final String     TAG = "SL";
	public static final String     DOWNLOAD_FOLDER_NAME = "/DownFile";
    public static final String     DOWNLOAD_FILE_NAME   = "Download.pdf";
	public static final String     APK_URL = "http://www.winlab.rutgers.edu/~janne/chi2011web.pdf";
	
	private Button      downloadButton ;
	private ProgressBar downloadProgress;
    private TextView    downloadSize;
    private TextView    downloadPrecent;
    private TextView    internet_status;
    private TextView    speed;
    private TextView    latency;
    
	DownloadManager    downloadManager;
	private long       downloadId = 0;
	
	private MyHandler              handler;
    private Handler                handlerSpeed;
    private Timer                  timer;
    private TimerTask              task;
    private DownloadChangeObserver downloadObserver;
	
	public boolean  network;
	public boolean  firstTime = true;
	public boolean  beginDownload;
	public boolean  finishDownload;
	
	public int      lastWifi;
	public int      bandwidth; 
    
	public long     lastByte = 0;
	public long     downloadTime = 0;
	public long     beginTime = 0;
	
	public WifiInfo info;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download);
		
		//handler handles all message of progress bar, download status.
		handler = new MyHandler();
		
		//handlerSpeed handles only download speed, which is update every 500ms.
		handlerSpeed = new Handler()   {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if(msg.arg2 > 0 && (msg.arg1 != lastByte)) {
				 speed.setText("Speed: "+getAppSize((msg.arg1 - lastByte)*2)+"/s");
				 Log.i("SL","the throughput of the network is ["+getAppSize((msg.arg1 - lastByte)*2)+"/s]");
				 lastByte = msg.arg1;
				}
			}
		};
		
		//timer is used to schedule task for every 500ms.
		timer = new Timer();
		task = new TimerTask() {
			@Override
			public void run() {
				Message message = new Message();
				message.what = 1;
				int[] bytesAndStatus = getBytesAndStatus(downloadId);
				message.arg1 = bytesAndStatus[0];
				message.arg2 = bytesAndStatus[1];
				handlerSpeed.sendMessage(message);
			}
		};
		
		//initialize all layout components.
		downloadButton = (Button)findViewById(R.id.downBtn);
		downloadProgress = (ProgressBar) findViewById(R.id.progress_bar);
		downloadSize = (TextView)findViewById(R.id.download_size);
        downloadPrecent = (TextView)findViewById(R.id.download_percent);
        downloadObserver = new DownloadChangeObserver();
        internet_status = (TextView)findViewById(R.id.internet_status);
        speed = (TextView)findViewById(R.id.download_speed);
        latency = (TextView)findViewById(R.id.download_latency);
        WifiManager wifiManager = (WifiManager) getSystemService (Context.WIFI_SERVICE);
		info = wifiManager.getConnectionInfo ();
        
		downloadButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				beginTime = System.currentTimeMillis();
				Log.i("SL", "at ["+refFormatNowDate(beginTime)+ "]starts to check about wifi status and schedule a download");
				
                downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
                Uri downloadUri = Uri.parse(APK_URL);
                DownloadManager.Request request = new DownloadManager.Request(downloadUri);
                request.setDestinationInExternalPublicDir(DOWNLOAD_FOLDER_NAME,DOWNLOAD_FILE_NAME);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
                downloadId = downloadManager.enqueue(request);
                downloadObserver = new DownloadChangeObserver();
                
                //Get up-to-date status of downloading.
                updateView();
                
                //Set all flags.
                if(firstTime){
                	timer.schedule(task, 1, 500);
                }
                lastByte = 0;
                firstTime = false;
                beginDownload = false;
                finishDownload = false;
                lastWifi = -1;
			}
		});
	}
	
	class DownloadChangeObserver extends ContentObserver {
   	 
        public DownloadChangeObserver(){
            super(handler);
        }
     
        @Override
        public void onChange(boolean selfChange) {
            updateView();
        }
    }
     
    public void updateView() {
    	int Wifi = -1;
    	boolean isWifi = isNetworkAvailable(getApplicationContext());
    	if(isWifi == true) Wifi = 1;
    	else Wifi =0;
    	if(lastWifi == -1) {
    		lastWifi = Wifi;
    	}
    	else{
    		if(lastWifi != Wifi) {
    			long currentTime = System.currentTimeMillis();
    			if(Wifi == 1) {
    				//info.getBSSID ();
    				Log.i("SL", "at ["+refFormatNowDate(currentTime)+ "]wifi is on and ssid is ["+info.getBSSID ()+"]");
    			}
    			else Log.i("SL", "at ["+refFormatNowDate(currentTime)+ "]wifi is down");
    			lastWifi = Wifi;
    		}
    	}
        int[] bytesAndStatus = getBytesAndStatus(downloadId);
        handler.sendMessage(handler.obtainMessage(Wifi, bytesAndStatus[0], bytesAndStatus[1],bytesAndStatus[2]));
    }
    
    protected void onResume() {
        super.onResume();
        /** observer download change **/
        getContentResolver().registerContentObserver(Uri.parse("content://downloads/my_downloads"), true,downloadObserver);
    }
     
    @Override
    protected void onPause() {
        super.onPause();
        getContentResolver().unregisterContentObserver(downloadObserver);
    }
    
	public int[] getBytesAndStatus(long downloadId) {
	    int[] bytesAndStatus = new int[] { -1, -1, 0 };
	    DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
	    Cursor c = null;
	    try {
			c = downloadManager.query(query);
	        if (c != null && c.moveToFirst()) {
	            bytesAndStatus[0] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
	            bytesAndStatus[1] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
	            bytesAndStatus[2] = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
	        }
	    } finally {
	        if (c != null) {
	            c.close();
	        }
	    }
	    return bytesAndStatus;
	}
	
	private class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int Wifi = msg.what;
            if(Wifi == 1) {
            	if(!beginDownload && !finishDownload){
            		long WifiTime = System.currentTimeMillis();
            		Log.i("SL", "at ["+refFormatNowDate(WifiTime)+ "]wifi is available and ssid is ["+info.getBSSID ()+"]");
            	}
            	internet_status.setText("Wifi is on and ssid is ["+info.getBSSID ()+"]");
            }
            else {
            	internet_status.setText("Wifi is down");
            }
            
            switch (1) {
                case 1:
                    int status = (Integer)msg.obj;
                    if (isDownloading(status)) {
                        downloadProgress.setVisibility(View.VISIBLE);
                        downloadProgress.setMax(0);
                        downloadProgress.setProgress(0);
                        downloadButton.setVisibility(View.GONE);
                        downloadSize.setVisibility(View.VISIBLE);
                        downloadPrecent.setVisibility(View.VISIBLE);
                        if (msg.arg2 < 0) {
                            downloadProgress.setIndeterminate(true);
                            downloadPrecent.setText("0%");
                            downloadSize.setText("0M/0M");
                        } else {
                        	if(!beginDownload) {
                        		downloadTime = System.currentTimeMillis();
                        		Log.i("SL", "at ["+refFormatNowDate(downloadTime)+ "]wifi is available and download starts");
                        		latency.setText("Latency: "+new StringBuilder(16).append((downloadTime-beginTime))
                        				.toString()+"ms");
                        		beginDownload = true;
                        	}
                            downloadProgress.setIndeterminate(false);
                            downloadProgress.setMax(msg.arg2);
                            downloadProgress.setProgress(msg.arg1);
                            downloadPrecent.setText(getNotiPercent(msg.arg1, msg.arg2));
                            downloadSize.setText(getAppSize(msg.arg1) + "/" + getAppSize(msg.arg2));
                        }
                    } else {
                    	if(!finishDownload) {
                    		long downloadComplete = System.currentTimeMillis();
                    		Log.i("SL", "at ["+refFormatNowDate(downloadComplete)+ "] completes downloading");
                    		Log.i("SL", "the time used to download is ["+new StringBuilder(16).append((downloadComplete - downloadTime)).toString()+ "]ms");
                    		Toast.makeText(getApplicationContext(), "Successfully downloaded~", 1).show();
                    		finishDownload = true;
                    	}
                        downloadProgress.setVisibility(View.VISIBLE);
                        downloadProgress.setMax(100);
                        downloadProgress.setProgress(100);
                        downloadButton.setVisibility(View.VISIBLE);
                        downloadSize.setVisibility(View.VISIBLE);
                        downloadPrecent.setVisibility(View.VISIBLE);
                    }
                    break;
            }
        }
    }
	
	static final DecimalFormat DOUBLE_DECIMAL_FORMAT = new DecimalFormat("0.##");

    public static final int MB_2_BYTE = 1024 * 1024;
    public static final int KB_2_BYTE = 1024;
	
    //Change byte into M.
	public static CharSequence getAppSize(long size) {
        if (size <= 0) {
            return "0M";
        }

        if (size >= MB_2_BYTE) {
            return new StringBuilder(16).append(DOUBLE_DECIMAL_FORMAT.format((double)size / MB_2_BYTE)).append("M");
        } else if (size >= KB_2_BYTE) {
            return new StringBuilder(16).append(DOUBLE_DECIMAL_FORMAT.format((double)size / KB_2_BYTE)).append("K");
        } else {
            return size + "B";
        }
    }

    public static String getNotiPercent(long progress, long max) {
        int rate = 0;
        if (progress <= 0 || max <= 0) {
            rate = 0;
        } else if (progress > max) {
            rate = 100;
        } else {
            rate = (int)((double)progress / max * 100);
        }
        return new StringBuilder(16).append(rate).append("%").toString();
    }

    public static boolean isDownloading(int downloadManagerStatus) {
        return downloadManagerStatus == DownloadManager.STATUS_RUNNING
                || downloadManagerStatus == DownloadManager.STATUS_PAUSED
                || downloadManagerStatus == DownloadManager.STATUS_PENDING;
    }
    
    public boolean isNetworkAvailable(Context context) {
    	
    	ConnectivityManager mgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
    	NetworkInfo[] info = mgr.getAllNetworkInfo();  
    	if (info != null) {  
    		for (int i = 0; i < info.length; i++) {  
             	if (info[i].getState() == NetworkInfo.State.CONNECTED) {  
            	 return true;  
             	}  
         	}  
    	}  
    	return false;  
    } 
    
    //Change long into formed time which is good for display in Log.
    public String refFormatNowDate(long time) {
    	  Date nowTime = new Date(time);
    	  SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    	  String retStrFormatNowDate = sdFormatter.format(nowTime);
    	  return retStrFormatNowDate;
    	}
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.download, menu);
		return true;
	}
}
