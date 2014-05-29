package com.outofjungle.bluetoothspp.app;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class DeviceAdapter extends ArrayAdapter<BluetoothDevice> {
  public DeviceAdapter(Context context, ArrayList<BluetoothDevice> devices) {
    super(context, R.layout.device_item, devices);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    if (convertView == null) {
      convertView = LayoutInflater.from(getContext()).inflate(R.layout.device_item, parent, false);
    }

    BluetoothDevice device = getItem(position);
    TextView deviceTextView = (TextView) convertView.findViewById(R.id.deviceTextView);
    deviceTextView.setText(device.getName() + "\n   " + device.getAddress());
    return convertView;
  }
}