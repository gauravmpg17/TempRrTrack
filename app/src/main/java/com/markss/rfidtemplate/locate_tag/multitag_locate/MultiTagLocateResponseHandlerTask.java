package com.markss.rfidtemplate.locate_tag.multitag_locate;

import android.content.Context;
import android.os.AsyncTask;

import androidx.fragment.app.Fragment;

import com.zebra.rfid.api3.TagData;
import com.markss.rfidtemplate.application.Application;
import com.markss.rfidtemplate.common.hextoascii;
import com.markss.rfidtemplate.home.MainActivity;
import com.markss.rfidtemplate.locate_tag.LocateOperationsFragment;
import com.markss.rfidtemplate.rfid.RFIDController;

/**
 * Async Task, which will handle multi-tag locationing tag data response from reader.
 */
public class MultiTagLocateResponseHandlerTask extends AsyncTask<Void, Void, Boolean> {
    private Context mContext;
    private TagData tagData;
    private Fragment fragment;

    public MultiTagLocateResponseHandlerTask(Context context, TagData tagData, Fragment fragment) {
        mContext = context;
        this.tagData = tagData;
        this.fragment = fragment;

    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        boolean success = false;
        try {
            String asciiTag = hextoascii.convert(tagData.getTagID());
            if(RFIDController.asciiMode) {
                if(Application.multiTagLocateTagListMap.containsKey(asciiTag)) {
                    Application.multiTagLocateTagListMap.get(asciiTag).setProximityPercent(tagData.MultiTagLocateInfo.getRelativeDistance());
                    int readCount = Application.multiTagLocateTagListMap.get(asciiTag).getReadCount() + tagData.getTagSeenCount();
                    Application.multiTagLocateTagListMap.get(asciiTag).setReadCount(readCount);
                    success = true;
                    // beep on each tag read
                    ((MainActivity) mContext).startbeepingTimer();
                }
            } else if (Application.multiTagLocateTagListMap.containsKey(tagData.getTagID())) {
                Application.multiTagLocateTagListMap.get(tagData.getTagID()).setProximityPercent(tagData.MultiTagLocateInfo.getRelativeDistance());
                int readCount = Application.multiTagLocateTagListMap.get(tagData.getTagID()).getReadCount() + tagData.getTagSeenCount();
                Application.multiTagLocateTagListMap.get(tagData.getTagID()).setReadCount(readCount);
                success = true;
                // beep on each tag read
                ((MainActivity)mContext).startbeepingTimer();
            }
        }
        catch(Exception e){
            success = false;
        }
        return success;
    }


    @Override
    protected void onPostExecute(Boolean result) {
        cancel(true);
        if (result) {
            if (fragment instanceof LocateOperationsFragment) {
                ((LocateOperationsFragment) fragment).handleLocateTagResponse();
            }
            tagData = null;
        }
    }
}
