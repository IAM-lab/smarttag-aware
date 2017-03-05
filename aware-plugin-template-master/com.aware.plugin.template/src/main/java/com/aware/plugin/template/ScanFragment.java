package com.aware.plugin.template;

/**
 * Created by aayushchadha on 05/03/17.
 */

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

public class ScanFragment extends Fragment {
    public static final int REQUEST_ENABLE_ST = 1;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mLeScanner;
    private BluetoothAdapter.LeScanCallback mLeScanCallback;
    private ScanCallback mScanCallback;
    private OnStatusListener mListener;
    private DeviceRecylerViewAdapter mRecyclerViewAdapter;

    public ScanFragment() {

    }

    // Method to return new instance of the Scan Fragment class
    public static ScanFragment newInstance() {
        return new ScanFragment();
    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= 21) {
            mScanCallback = new ScanCallback() {
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);

                    // Add to list of devices that were found
                    mRecyclerViewAdapter.addDevice(result.getDevice().getAddress());
                }
            };
        } else {
            mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
                public void onLeScan(BluetoothDevice BluetoothDevice, init i, byte[] bytes) {
                    mRecyclerViewAdapter.addDevice(bluetoothDevice.getAddress());

                }
            };
        }

        BluetoothManager manager = (BluetoothManager) getActivty().
                getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();
    }

    public void onResume() {
        super.onResume();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_ST);
        }
        else {
            startScan();
        }
    }

    /**
     * Initiates the scan for BLE devices according to the API level.
     */
    private void startScan() {
        if (mRecyclerViewAdapter.getSize() == 0) mListener.onShowProgress();
        if (Build.VERSION.SDK_INT < 21) {
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            // request BluetoothLeScanner if it hasn't been initialized yet
            if (mLeScanner == null) mLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            // start scan in low latency mode
            mLeScanner.startScan(new ArrayList<ScanFilter>(), new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(), mScanCallback);
        }
    }

    /**
     * Stops the scan using the proper functions for the API level.
     */
    private void stopScan() {
        if (mBluetoothAdapter.isEnabled()) {
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } else {
                mLeScanner.stopScan(mScanCallback);
            }
        }
    }

    @Override
    public void onPause() {
        stopScan();
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan, container, false);
        Context context = view.getContext();
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setHasFixedSize(true);
        mRecyclerViewAdapter = new DeviceRecyclerViewAdapter(mListener);
        recyclerView.setAdapter(mRecyclerViewAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), null, true));
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnStatusListener) {
            mListener = (OnStatusListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnStatusListener");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ScanFragment.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getActivity(), R.string.state_off, Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}


