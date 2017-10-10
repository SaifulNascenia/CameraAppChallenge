package com.nascenia.cameraappchallenge.fragment;


import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.nascenia.cameraappchallenge.R;
import com.nascenia.cameraappchallenge.activity.CameraHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnItemSelected;

/**
 * A simple {@link Fragment} subclass.
 */
public class CameraFragment extends Fragment {


    public CameraFragment() {
        // Required empty public constructor
    }

    private View initView;


    @BindView(R.id.first_resulation_spinner)
    Spinner firstResulationSpinner;

    @BindView(R.id.second_resulation_spinner)
    Spinner secondResulationSpinner;
    @BindView(R.id.first_switch_btn)
    SwitchCompat general1CenterPointSwitchBtn;

    private List<String> resulationList = new ArrayList<>();

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor prefEditor;

    private List<Camera.Size> listDeviceSupportedVideoResulationSizes;
    private List<String> listMaxSupportedVideoResulationSizes = new ArrayList<String>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        initView = inflater.inflate(R.layout.fragment_camera, container, false);
        ButterKnife.bind(this, initView);

        sharedPreferences = getActivity().getSharedPreferences("CAMERA_RESULATION_PREF", Context.MODE_PRIVATE);
        prefEditor = sharedPreferences.edit();

        resulationList.add("720p");
        resulationList.add("1080p");
        resulationList.add("1280p");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, resulationList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        getCameraPerMisssion();

        if (sharedPreferences.getBoolean("GENERAL_1_CENTER_POINT_STATUS", false))
            general1CenterPointSwitchBtn.setChecked(true);

        // firstResulationSpinner.setAdapter(dataAdapter);
        secondResulationSpinner.setAdapter(dataAdapter);

        return initView;
    }

    private void getCameraPerMisssion() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.CAMERA)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                requestPermissions(
                        new String[]{Manifest.permission.CAMERA,
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            setCameraResulation1();
        }
    }

    @OnItemSelected(R.id.first_resulation_spinner)
    void getSelectedCameraResulation(View view) {
        if (!listMaxSupportedVideoResulationSizes.isEmpty()) {
            /* String itemValue = firstResulationSpinner.getSelectedItem().toString();
            String width = itemValue.substring(itemValue.lastIndexOf(" ") + 1, itemValue.length());
            String height = itemValue.substring(0, itemValue.indexOf(" "));*/

            prefEditor.putInt("RESULATION_WIDTH", Integer.valueOf(
                    firstResulationSpinner.getSelectedItem().toString().
                            substring(0,
                                    firstResulationSpinner.getSelectedItem().toString().indexOf(" "))));
            prefEditor.commit();

            prefEditor.putInt("RESULATION_HEIGHT", Integer.valueOf(
                    firstResulationSpinner.getSelectedItem().toString().
                            substring(firstResulationSpinner.getSelectedItem().toString().lastIndexOf(" ") + 1,
                                    firstResulationSpinner.getSelectedItem().toString().length())));
            prefEditor.commit();
        }
    }

    @OnCheckedChanged(R.id.first_switch_btn)
    public void setFirstSwitchBtnStatus(boolean isChecked) {

        if (isChecked) {
            Log.i("checked", isChecked + "");
            prefEditor.putBoolean("GENERAL_1_CENTER_POINT_STATUS", isChecked);
            prefEditor.commit();

        } else {
            Log.i("checked", isChecked + "");
            prefEditor.putBoolean("GENERAL_1_CENTER_POINT_STATUS", isChecked);
            prefEditor.commit();
        }
    }

    private void setCameraResulation1() {
        Log.i("method_Call", "setCameraResulation1");
//https://stackoverflow.com/questions/14263521/android-getsupportedvideosizes-allways-returns-null/22022062#22022062
        Camera camera = Camera.open();
        Camera.Parameters cameraParameters = camera.getParameters();
        // List<Camera.Size> listSupportedPictureSizes = cameraParameters.getSupportedPictureSizes();
        listDeviceSupportedVideoResulationSizes = getSupportedVideoSizes(camera);

        Camera.Size optimalSize = CameraHelper.getOptimalVideoSize(camera.getParameters().getSupportedVideoSizes(),
                camera.getParameters().getSupportedPreviewSizes(), getArguments().getInt("preview_width")
                , getArguments().getInt("preview_height"));


        for (int i = 0; i < listDeviceSupportedVideoResulationSizes.size(); i++) {

            if (listDeviceSupportedVideoResulationSizes.get(i).height <= optimalSize.height &&
                    listDeviceSupportedVideoResulationSizes.get(i).width <= optimalSize.width) {

                String strSize = String.valueOf(listDeviceSupportedVideoResulationSizes.get(i).width) + " x " +
                        String.valueOf(listDeviceSupportedVideoResulationSizes.get(i).height);
                listMaxSupportedVideoResulationSizes.add(strSize);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, listMaxSupportedVideoResulationSizes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        firstResulationSpinner.setAdapter(adapter);
        camera.release();


        if (listMaxSupportedVideoResulationSizes.size() > 0 && (sharedPreferences.contains("RESULATION_WIDTH"))) {

            for (int i = 0; i < listMaxSupportedVideoResulationSizes.size(); i++) {
                if ((sharedPreferences.getInt("RESULATION_WIDTH", 0) +
                        " x " + sharedPreferences.getInt("RESULATION_HEIGHT", 0))
                        .equals(listMaxSupportedVideoResulationSizes.get(i))) {
                    firstResulationSpinner.setSelection(i);
                    Log.i("method_Call", i + " if sett");
                    break;

                }
            }
        }

    }

    public List<Camera.Size> getSupportedVideoSizes(Camera camera) {
        if (camera.getParameters().getSupportedVideoSizes() != null) {
            return camera.getParameters().getSupportedVideoSizes();
        } else {
            // Video sizes may be null, which indicates that all the supported
            // preview sizes are supported for video recording.
            return camera.getParameters().getSupportedPreviewSizes();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            Log.i("method_Call", requestCode + " ");
            if (grantResults.length == 0
                    || grantResults[0] !=
                    PackageManager.PERMISSION_GRANTED) {

                Log.i("method_Call", "Permission has been denied by user");
            } else {

                setCameraResulation1();
                Log.i("method_Call", "Permission has been denied by user");
            }
        } else {
            Log.i("method_Call", requestCode + " ");
        }

    }
}
