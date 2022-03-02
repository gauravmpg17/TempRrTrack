package com.markss.rfidtemplate.rapidread;

import static android.app.Activity.RESULT_OK;
import static com.markss.rfidtemplate.application.Application.TAG_LIST_LOADED;
import static com.markss.rfidtemplate.application.Application.bookDao;
import static com.markss.rfidtemplate.common.Constants.SUCCESS;
import static com.markss.rfidtemplate.home.MainActivity.TAG_CONTENT_FRAGMENT;
import static com.markss.rfidtemplate.rfid.RFIDController.ActiveProfile;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.markss.rfidtemplate.R;
import com.markss.rfidtemplate.application.Application;
import com.markss.rfidtemplate.common.Constants;
import com.markss.rfidtemplate.common.ResponseHandlerInterfaces;
import com.markss.rfidtemplate.home.MainActivity;
import com.markss.rfidtemplate.inventory.InventoryListItem;
import com.markss.rfidtemplate.rfid.RFIDController;
import com.shashank.sony.fancytoastlib.FancyToast;
import com.zebra.rfid.api3.RFIDResults;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import asset.trak.database.daoModel.BookAndAssetData;
import asset.trak.database.entity.Inventorymaster;
import asset.trak.database.entity.LocationMaster;
import asset.trak.database.entity.ScanTag;
import asset.trak.model.AssetScanApi;
import asset.trak.model.AssetSyncRequestDataModel;
import asset.trak.model.InventoryMasterApi;
import asset.trak.views.inventory.ReconcileAssetsFragment;
import asset.trak.views.module.InventoryViewModel;
import okhttp3.MediaType;
import okhttp3.RequestBody;


public class RapidReadFragment extends Fragment implements ResponseHandlerInterfaces.ResponseTagHandler, ResponseHandlerInterfaces.TriggerEventHandler, ResponseHandlerInterfaces.BatchModeEventHandler, ResponseHandlerInterfaces.ResponseStatusHandler {
    MatchModeProgressView progressView;
    private TextView tagReadRate;
    private TextView uniqueTags;
    private TextView totalTags;
    private FloatingActionButton inventoryButton;
    private TextView timeText;
    private TextView uniqueTagTitle;
    private TextView totalTagTitle;
    private FloatingActionButton btnScan;
    private FloatingActionButton btnScanpause;

    private LinearLayout invtoryData;
    private FrameLayout batchModeRR;
    boolean batchModeEventReceived = false;
    private LocationMaster locationData;
    private ConstraintLayout foundLocParent;
    private ConstraintLayout foundForDifferentParent;
    private TextView tvFoundLocCount;
    private TextView tvNotFoundLocCount;
    private TextView tvDiffLocationCount;
    private TextView tvNotRegisteredCount;
    private TextView tvILastRecordDate;
    private Button btnReconcile;
    private Button btnInventoryRecord;
    private LinearLayout llBottomParent;
    private ProgressBar progressBar;
    private HashSet<String> listInventoryList = new HashSet<>();
    private HashSet<String> scannedList = new HashSet<>();

    private List<Inventorymaster> pendingInventoryScan;
    private InventoryViewModel inventoryViewModel;
    private int totalRegisteredCount = 0;
    private int countFoundCurrentLocation = 0;
    private int countNotFoundCurrentLocation = 0;
    private int countFoundDifferentLoc = 0;
    private int countNotRegistered = 0;

    public static com.markss.rfidtemplate.rapidread.RapidReadFragment newInstance() {
        return new com.markss.rfidtemplate.rapidread.RapidReadFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        return inflater.inflate(R.layout.fragment_rr, container, false);
        //

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_rr, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        inventoryViewModel = ViewModelProviders.of(requireActivity()).get(InventoryViewModel.class);

        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        inventoryButton = mainActivity.findViewById(R.id.rr_inventoryButton);
        uniqueTags = mainActivity.findViewById(R.id.uniqueTagContent);
        uniqueTagTitle = mainActivity.findViewById(R.id.uniqueTagTitle);
        totalTags = mainActivity.findViewById(R.id.totalTagContent);
        totalTagTitle = mainActivity.findViewById(R.id.totalTagTitle);
        tagReadRate = getActivity().findViewById(R.id.readRateContent);
        batchModeRR = getActivity().findViewById(R.id.batchModeRR);
        invtoryData = getActivity().findViewById(R.id.inventoryDataLayout);
        btnScan = getActivity().findViewById(R.id.btnScan123);

        foundLocParent = getActivity().findViewById(R.id.foundLocParent);
        foundForDifferentParent = getActivity().findViewById(R.id.foundForDifferentParent);
        tvFoundLocCount = getActivity().findViewById(R.id.tvFoundLocCount);
        tvNotFoundLocCount = getActivity().findViewById(R.id.tvNotFoundLocCount);
        tvDiffLocationCount = getActivity().findViewById(R.id.tvDiffLocationCount);
        tvNotRegisteredCount = getActivity().findViewById(R.id.tvNotRegisteredCount);
        tvILastRecordDate = getActivity().findViewById(R.id.tvILastRecord12);
        btnReconcile = getActivity().findViewById(R.id.btnReconcile);
        btnInventoryRecord = getActivity().findViewById(R.id.btnInventoryRecord);
        llBottomParent = getActivity().findViewById(R.id.llBottomParent1);
        progressBar = getActivity().findViewById(R.id.progressBar);
        TextView tvRegisteredCount = getActivity().findViewById(R.id.tvRegisteredCountrr);
        TextView tvLocation = getActivity().findViewById(R.id.tvLocation);
        ImageView ivBack = getActivity().findViewById(R.id.ivBackButtonrr);
        locationData = getArguments().getParcelable("LocationData");
        totalRegisteredCount = getArguments().getInt("totalRegistered");
        tvRegisteredCount.setText(String.valueOf(bookDao.getCountLocationId(locationData.getId())));

        //   tvRegisteredCount.setText(String.valueOf(totalRegisteredCount));

        tvLocation.setText(locationData.getLocationName());
        listInventoryList = new HashSet<>();
        scannedList = new HashSet<>();

        pendingInventoryScan = bookDao.getPendingInventoryScan(locationData.getId());


        if (!pendingInventoryScan.isEmpty()) {
            List<String> tags = bookDao.getScanRfid(locationData.getId(), pendingInventoryScan.get(0).getScanID());
            for (String tag : tags) {
                scannedList.add(tag);
            }
            //format and set the date here
            if (pendingInventoryScan.get(0).getScanOn() == null || TextUtils.isEmpty(pendingInventoryScan.get(0).getScanOn())) {
                tvILastRecordDate.setText(getString(R.string.last_scanned) + " NA ");
            } else {
                SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                SimpleDateFormat changedFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
                try {

                    Date latestDate = formater.parse(pendingInventoryScan.get(0).getScanOn());
                    tvILastRecordDate.setText(getString(R.string.last_scanned) +" "+ changedFormat.format(latestDate));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }


        }
        if(scannedList.size() < 10 && scannedList.size()>0)
        {
            uniqueTags.setText("0"+scannedList.size());
        }
        else
        {
            uniqueTags.setText(Integer.toString(scannedList.size()));
        }


        if (RFIDController.mIsInventoryRunning) {
            inventoryButton.setBackgroundResource(R.drawable.ic_play_stop);
        } else {
            inventoryButton.setBackgroundResource(android.R.drawable.ic_media_play);
        }
        if (RFIDController.isBatchModeInventoryRunning != null && RFIDController.isBatchModeInventoryRunning) {
            invtoryData.setVisibility(View.GONE);
            batchModeRR.setVisibility(View.VISIBLE);
        } else {
            invtoryData.setVisibility(View.VISIBLE);
            batchModeRR.setVisibility(View.GONE);
        }
        if (RFIDController.mRRStartedTime == 0)
            Application.TAG_READ_RATE = 0;
        else
            Application.TAG_READ_RATE = (int) (Application.TOTAL_TAGS / (RFIDController.mRRStartedTime / (float) 1000));
        tagReadRate.setText(Application.TAG_READ_RATE + Constants.TAGS_SEC);
        timeText = (TextView) getActivity().findViewById(R.id.readTimeContent);
        if (timeText != null) {
            String min = String.format("%d", TimeUnit.MILLISECONDS.toMinutes(RFIDController.mRRStartedTime));
            String sec = String.format("%d", TimeUnit.MILLISECONDS.toSeconds(RFIDController.mRRStartedTime) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(RFIDController.mRRStartedTime)));
            if (min.length() == 1) {
                min = "0" + min;
            }
            if (sec.length() == 1) {
                sec = "0" + sec;
            }
            timeText.setText(min + ":" + sec);
        }
        progressView = getActivity().findViewById(R.id.MatchModeView);
        if (Application.TAG_LIST_MATCH_MODE) {
            progressView.setVisibility(View.VISIBLE);
        } else {
            progressView.setVisibility(View.GONE);
        }
        if (Application.missedTags > 9999) {
            //orignal size is 60sp - reduced size 45sp
            uniqueTags.setTextSize(45);
        }
        updateTexts();
        getActivity().findViewById(R.id.tv_prefilter_enabled).setVisibility(
                RFIDController.getInstance().isPrefilterEnabled() ? View.VISIBLE : View.INVISIBLE);
        Button bt_clear = getActivity().findViewById(R.id.bt_clear);
        bt_clear.setVisibility(ActiveProfile.id.equals("1") ? View.VISIBLE : View.INVISIBLE);
        bt_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (RFIDController.mIsInventoryRunning) {
                    Toast.makeText(getContext(), "Inventory is running", Toast.LENGTH_SHORT).show();
                } else {
                    RFIDController.getInstance().clearAllInventoryData();
                    resetTagsInfo();
                    TAG_LIST_LOADED = false;

                 /* final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Clear inventory");
                    builder.setMessage("Do you want to clear data?");
                    builder.setPositiveButton("Yes", new Dia`logInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            RFIDController.getInstance().clearInventoryData();
                            resetTagsInfo();
                            TAG_LIST_LOADED = false;

                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.create().show();

                    return true;*/
                }

            }
        });

        ivBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStackImmediate();
        });


        btnScan.setOnClickListener(v -> {
            if (btnScan.getTag().equals("1")) {
                btnScan.setTag("0");
                llBottomParent.setVisibility(View.GONE);
                btnScan.setImageResource(android.R.drawable.ic_media_pause);
                listInventoryList = new HashSet<>();
            } else {
                btnScan.setTag("1");
                llBottomParent.setVisibility(View.VISIBLE);
                btnScan.setImageResource(android.R.drawable.ic_media_play);
                addDataToScanTag();
                showCountFound();
                updateCountInDb();

//                btnScan.setTag("1");
//                llBottomParent.setVisibility(View.GONE);
//                btnScan.setImageResource(android.R.drawable.ic_media_play);
//                listInventoryList = new HashSet<>();
            }
        });

        btnReconcile.setOnClickListener(v -> {
//            Bundle bundle = new Bundle();
//            bundle.putInt("locationId", locationData.getId());
//            bundle.putParcelable("LocationData", locationData);
//            ReconcileAssetsFragment fragInfo = new ReconcileAssetsFragment();
//            fragInfo.setArguments(bundle);
//            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content_frame,
//                    fragInfo, TAG_CONTENT_FRAGMENT).addToBackStack(null).commit();
        });
        if (scannedList.isEmpty() == false) {
            btnScan.setTag("1");
            llBottomParent.setVisibility(View.VISIBLE);
            btnScan.setImageResource(android.R.drawable.ic_media_play);
            addDataToScanTag();
            showCountFound();
        }

    }

    public void updateTexts() {
        if (Application.TAG_LIST_MATCH_MODE) {
            if (uniqueTags != null && totalTags != null) {
                totalTags.setText(String.valueOf(Application.matchingTags));
                //uniqueTags.setText(String.valueOf(Application.missedTags));
            }
            if (totalTagTitle != null && uniqueTagTitle != null) {
                totalTagTitle.setText(R.string.rr_total_tag_title_MM);
                uniqueTagTitle.setText(R.string.rr_unique_tags_title_MM);
            }
            updateProgressView();
        } else {
            if (uniqueTags != null)
                //uniqueTags.setText(String.valueOf(Application.UNIQUE_TAGS));
                if (totalTags != null)
                    totalTags.setText(String.valueOf(Application.TOTAL_TAGS));
            if (totalTagTitle != null && uniqueTagTitle != null) {
                totalTagTitle.setText(R.string.rr_total_tag_title);
                uniqueTagTitle.setText(R.string.rr_unique_tags_title);
            }
        }
    }

    private void updateProgressView() {
        if (Application.missedTags != 0) {
            progressView.mSweepAngle = 360 * Application.matchingTags / (Application.missedTags + Application.matchingTags);
        } else if (Application.matchingTags != 0 && Application.missedTags == 0 && RFIDController.mIsInventoryRunning) {
            progressView.bCompleted = true;
        } else {
            progressView.mSweepAngle = 0;
        }
        if (progressView.mSweepAngle >= 360) {
            progressView.mSweepAngle = 0;
        }
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressView.invalidate();
                    progressView.requestLayout();
                }
            });
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * method to reset tags info on the screen before starting inventory operation
     */
    public void resetTagsInfo() {
//        uniqueTags.setText(String.valueOf(RFIDController.UNIQUE_TAGS));
//        totalTags.setText(String.valueOf(RFIDController.TOTAL_TAGS));
        updateTexts();
        progressView.bCompleted = false;
        tagReadRate.setText(Application.TAG_READ_RATE + Constants.TAGS_SEC);
        timeText.setText(Constants.ZERO_TIME);
    }

    /**
     * method to start inventory operation on trigger press event received
     */
    public void triggerPressEventRecieved() {
        if (!RFIDController.mIsInventoryRunning && getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainActivity activity = (MainActivity) getActivity();
                    if (activity != null) {
                        activity.inventoryStartOrStop();
                    }
                }
            });
        }
    }

    /**
     * method to stop inventory operation on trigger release event received
     */
    public void triggerReleaseEventRecieved() {
        if ((RFIDController.mIsInventoryRunning == true) && getActivity() != null) {
            //RFIDController.mInventoryStartPending = false;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainActivity activity = (MainActivity) getActivity();
                    if (activity != null) {
                        activity.inventoryStartOrStop();
                    }
                }
            });
        }
    }

    public void handleStatusResponse(final RFIDResults results) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (results.equals(RFIDResults.RFID_BATCHMODE_IN_PROGRESS)) {
                    if (uniqueTags != null) {
                        invtoryData.setVisibility(View.GONE);
                        batchModeRR.setVisibility(View.VISIBLE);
                    }
                } else if (!results.equals(RFIDResults.RFID_API_SUCCESS)) {
                    RFIDController.mIsInventoryRunning = false;
                    if (inventoryButton != null) {
                        inventoryButton.setBackgroundResource(android.R.drawable.ic_media_play);
                    }
                    RFIDController.isBatchModeInventoryRunning = false;
                }
            }
        });
    }


    /**
     * method to update inventory details on the screen on operation end summary received
     */
    public void updateInventoryDetails() {
        updateTexts();
        tagReadRate.setText(Application.TAG_READ_RATE + Constants.TAGS_SEC);
    }

    /**
     * method to reset inventory operation status on the screen
     */
    public void resetInventoryDetail() {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!ActiveProfile.id.equals("1")) {
                        if (inventoryButton != null && !RFIDController.mIsInventoryRunning &&
                                (RFIDController.isBatchModeInventoryRunning == null || !RFIDController.isBatchModeInventoryRunning)) {
                            inventoryButton.setBackgroundResource(android.R.drawable.ic_media_play);
                        }
                        if (uniqueTags != null) {
                            invtoryData.setVisibility(View.VISIBLE);
                        }
                        if (Application.TAG_LIST_MATCH_MODE)
                            progressView.setVisibility(View.VISIBLE);

                        if (batchModeRR != null) {
                            batchModeRR.setVisibility(View.GONE);
                        }

                        if (Application.TAG_LIST_MATCH_MODE && Application.matchingTags != 0 && Application.missedTags == 0) {
                            progressView.bCompleted = true;
                        }
                    }
                }
            });
    }

    @Override
    public void batchModeEventReceived() {
        batchModeEventReceived = true;
        if (inventoryButton != null)
            inventoryButton.setBackgroundResource(R.drawable.ic_play_stop);
        if (uniqueTags != null) {
            invtoryData.setVisibility(View.GONE);
            progressView.setVisibility(View.GONE);
        }
        if (batchModeRR != null) {
            batchModeRR.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void handleTagResponse(InventoryListItem inventoryListItem, boolean isAddedToList) {
        updateTexts();

        listInventoryList.add(inventoryListItem.getTagID());
        scannedList.add(inventoryListItem.getTagID());
        uniqueTags.setText(Integer.toString(scannedList.size()));
        if (tagReadRate != null) {
            if (RFIDController.mRRStartedTime == 0)
                Application.TAG_READ_RATE = 0;
            else
                Application.TAG_READ_RATE = (int) (Application.TOTAL_TAGS / (RFIDController.mRRStartedTime / (float) 1000));
            tagReadRate.setText(Application.TAG_READ_RATE + Constants.TAGS_SEC);
        }
    }


    private void addDataToScanTag() {
        if (locationData.getId() == null) {

        } else {
            if (pendingInventoryScan.isEmpty()) {

            } else {
                Inventorymaster lastItem = pendingInventoryScan.get(0);

                listInventoryList.add("000000000000000000001271");
                listInventoryList.add("300833B2DDD9014000000000");
                listInventoryList.add("10011011003");
                listInventoryList.add("10011011005");
                listInventoryList.add("122110110053434");
//
//                scannedList.add("000000000000000000001271");
//                scannedList.add("300833B2DDD9014000000000");
//                scannedList.add("10011011003");
//                scannedList.add("10011011005");
//                scannedList.add("122110110053434");


                for (String inventoryTag : listInventoryList) {
                    ScanTag scanTag = new ScanTag();
                    scanTag.setScanId(lastItem.getScanID());
                    scanTag.setLocationId(locationData.getId());
                    scanTag.setRfidTag(inventoryTag);

                    Integer getCountOfTagAlready = bookDao.getCountOfTagAlready(scanTag.getRfidTag(), scanTag.getScanId());
                    if (getCountOfTagAlready == 0)
                        bookDao.addScanTag(scanTag);
                }
            }
        }

    }

    private  void updateCountInDb(){
        Inventorymaster inventoryMaster = pendingInventoryScan.get(0);
        inventoryMaster.setFoundOnLocation(countFoundCurrentLocation);
        inventoryMaster.setNotFound(countNotFoundCurrentLocation);
        inventoryMaster.setFoundOfDiffLocation(countFoundDifferentLoc);
        inventoryMaster.setNotRegistered(countNotRegistered);
        inventoryMaster.setRegistered(bookDao.getCountLocationId(locationData.getId()));
        //inventoryMaster.setStatus(asset.trak.utils.Constants.InventoryStatus.COMPLETED);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
        Calendar cal = Calendar.getInstance();
        String dateFormat = sdf.format(cal.getTime());
        inventoryMaster.setScanOn(dateFormat);


        bookDao.updateInventoryItem(inventoryMaster);

        try {
                Date latestDate = sdf.parse(dateFormat);
                SimpleDateFormat changedFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
                tvILastRecordDate.setText(getString(R.string.last_scanned) +" "+ changedFormat.format(latestDate));


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showCountFound() {
        foundLocParent.setVisibility(View.VISIBLE);
        foundForDifferentParent.setVisibility(View.VISIBLE);
        llBottomParent.setVisibility(View.VISIBLE);

        if (locationData.getId() == null) {
            tvFoundLocCount.setText(String.valueOf(countFoundCurrentLocation));
            tvNotFoundLocCount.setText(String.valueOf(countNotFoundCurrentLocation));
            tvDiffLocationCount.setText(String.valueOf(countFoundDifferentLoc));
            tvNotRegisteredCount.setText(String.valueOf(countNotRegistered));
        } else {

            Inventorymaster inventoryMaster = pendingInventoryScan.get(0);

            /*Get Count*/
            countFoundCurrentLocation = bookDao.getCountOfTagsFound(inventoryMaster.getScanID(), locationData.getId());
            countNotFoundCurrentLocation = bookDao.getCountOfTagsNotFound(locationData.getId(), inventoryMaster.getScanID());
            countFoundDifferentLoc = bookDao.getCountFoundDifferentLoc(inventoryMaster.getScanID(), locationData.getId());
            countNotRegistered = bookDao.getCountNotRegistered(inventoryMaster.getScanID());
            List<BookAndAssetData> bookAndAssetData = bookDao.getFoundAtLocation(inventoryMaster.getScanID(), locationData.getId());

            Log.e("data", "" + new Gson().toJson(bookAndAssetData));


            tvFoundLocCount.setText(String.valueOf(countFoundCurrentLocation));
            tvNotFoundLocCount.setText(String.valueOf(countNotFoundCurrentLocation));
            tvDiffLocationCount.setText(String.valueOf(countFoundDifferentLoc));
            tvNotRegisteredCount.setText(String.valueOf(countNotRegistered));


//            /*Update entry in Inventory master to completed*/
//            inventoryMaster.setFoundOnLocation(countFoundCurrentLocation);
//            inventoryMaster.setNotFound(countNotFoundCurrentLocation);
//            inventoryMaster.setFoundOfDiffLocation(countFoundDifferentLoc);
//            inventoryMaster.setNotRegistered(countNotRegistered);
//            inventoryMaster.setRegistered(bookDao.getCountLocationId(locationData.getId()));
//            //inventoryMaster.setStatus(asset.trak.utils.Constants.InventoryStatus.COMPLETED);
//
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
//            Calendar cal = Calendar.getInstance();
//            String dateFormat = sdf.format(cal.getTime());
//            inventoryMaster.setScanOn(dateFormat);
//
//
//            bookDao.updateInventoryItem(inventoryMaster);





        }

        btnInventoryRecord.setOnClickListener(v -> {
            Log.d("RapidRead", countNotFoundCurrentLocation + " " + countFoundDifferentLoc + " " + countFoundDifferentLoc);

            if (countNotFoundCurrentLocation == 0 && countFoundDifferentLoc == 0 && countFoundDifferentLoc == 0) {
                progressBar.setVisibility(View.VISIBLE);
                btnInventoryRecord.setEnabled(false);
                btnInventoryRecord.setClickable(false);
                postAssetSync();
            } else {
               FancyToast.makeText(requireActivity(),"Please reconcile asset(s) to proceed.", FancyToast.LENGTH_LONG,FancyToast.WARNING,false).show();
             //   Toast.makeText(requireActivity(), "Please reconcile asset(s) to proceed.", Toast.LENGTH_SHORT).show();
            }

        });


    }


    private void postAssetSync() {
        disableUserInteraction(getActivity());
        List<BookAndAssetData> bookAndAssetData = new ArrayList<BookAndAssetData>();
        AssetSyncRequestDataModel assetSyncRequestDataModel = new AssetSyncRequestDataModel();

        Inventorymaster inventoryMaster = pendingInventoryScan.get(0);


//        if (inventoryMaster.getLocationId() == null) {
//            inventoryMaster.setLocationId(0);
//        }
//        if (inventoryMaster.getRegistered() == null) {
//            inventoryMaster.setRegistered(0);
//        }
//        if (inventoryMaster.getNotRegistered() == null) {
//            inventoryMaster.setNotRegistered(0);
//        }
//        if (inventoryMaster.getNotFound() == null) {
//            inventoryMaster.setNotFound(0);
//        }
//        if (inventoryMaster.getFoundOfDiffLocation() == null) {
//            inventoryMaster.setFoundOfDiffLocation(0);
//        }
        if (locationData.getId() == null) {
            locationData.setId(0);
        }

        assetSyncRequestDataModel.inventoryMaster.add(new InventoryMasterApi(inventoryMaster.getScanID(),
                inventoryMaster.getDeviceId(),
                inventoryMaster.getLocationId(),
                inventoryMaster.getScanOn(),
                inventoryMaster.getRegistered(),
                inventoryMaster.getNotRegistered(),
                inventoryMaster.getNotFound(),
                inventoryMaster.getFoundOfDiffLocation()));


        bookAndAssetData.addAll(bookDao.getFoundAtLocation(inventoryMaster.getScanID(), locationData.getId()));
        // bookAndAssetData=bookDao.getFoundAtLocation(inventoryMaster.getScanID(),locationData.getId());


//        ArrayList<Inventorymaster> inventoryList = new ArrayList<>();
//        inventoryList.add(inventoryMasterList.get(inventoryMasterList.size() - 1));


/*
      "scanId": "string",
      "deviceId": "string",
      "locationId": 0,
      "scanOn": "string",
      "registered": 0,
      "notRegistered": 0,
      "notFound": 0,
      "foundOfDiffLocation": 0*/

        Log.e("data", "" + new Gson().toJson(bookAndAssetData));
        //  ArrayList<AssetScanApi> assetScanList = new ArrayList<>();
        for (BookAndAssetData n : bookAndAssetData) {
            AssetScanApi scanTag = new AssetScanApi();
            scanTag.setRfidTag(n.getAssetCatalogue().getRfidTag());
            Log.d("newdata scan", "postAssetSync: " + n.getAssetCatalogue().getInventoryScanId());
            if (n.getAssetCatalogue().getInventoryScanId() == null) {
                n.getAssetCatalogue().setInventoryScanId("");
            }
            scanTag.setScanId(inventoryMaster.getScanID());
            scanTag.setNewLocationId(n.getAssetCatalogue().getLocationId());
            assetSyncRequestDataModel.assetScan.add(scanTag);
        }
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), assetSyncRequestDataModel.toString());
        Log.e("data", "" + new Gson().toJson(assetSyncRequestDataModel));
        inventoryViewModel.postAssetSync(body).observe(getViewLifecycleOwner(), response -> {
            if (response == SUCCESS) {
                Log.d("final", "postAssetSync: ");
                progressBar.setVisibility(View.GONE);
                btnInventoryRecord.setEnabled(true);
                btnInventoryRecord.setClickable(true);
                enableUserInteraction(getActivity());
                inventoryMaster.setStatus(asset.trak.utils.Constants.InventoryStatus.COMPLETED);
                bookDao.updateInventoryItem(inventoryMaster);
                bookDao.updateScanIdOfReconciledAssets(inventoryMaster.getScanID(), inventoryMaster.getLocationId());

                Toast.makeText(getContext(), getString(R.string.data_sync_success), Toast.LENGTH_SHORT).show();

            } else {
                Log.d("final", "Failure: ");
                progressBar.setVisibility(View.GONE);
                btnInventoryRecord.setEnabled(true);
                btnInventoryRecord.setClickable(true);
                enableUserInteraction(getActivity());
                Toast.makeText(getContext(), getString(R.string.error_data_sync), Toast.LENGTH_SHORT).show();

            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d("scantagcheck", "onResume: " + btnScan.getTag());
        if (btnScan != null) {


            //btnScan.setTag("1");
            //llBottomParent.setVisibility(View.VISIBLE);
            //btnScan.setImageResource(android.R.drawable.ic_media_play);
            //addDataToScanTag();
            //showCountFound();
        }


    }

    public void enableUserInteraction(FragmentActivity requireActivity) {
        requireActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void disableUserInteraction(FragmentActivity requireActivity) {
        requireActivity.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }


}
