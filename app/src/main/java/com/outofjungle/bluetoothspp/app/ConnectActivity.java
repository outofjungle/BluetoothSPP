package com.outofjungle.bluetoothspp.app;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Set;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

public class ConnectActivity extends Activity {

  private ListView deviceListView;
  private BluetoothAdapter bluetoothAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_connect);
    setTitle(R.string.title_activity_connect);

    deviceListView = (ListView) findViewById(R.id.deviceListView);
    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    if (bluetoothAdapter == null) {
      makeText(getApplicationContext(), "Bluetooth Not Found", LENGTH_SHORT).show();
    } else if (!bluetoothAdapter.isEnabled()) {
      Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      startActivityForResult(enableBluetooth, "BT_ENABLE_REQ");
    } else {
      new DeviceSearch().execute();
    }

    deviceListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
      @Override
      public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

        BluetoothDevice device = (BluetoothDevice) adapterView.getItemAtPosition(i);

        Intent intent = new Intent(getApplicationContext(), ConsoleActivity.class);
        intent.putExtra("DEVICE", device);
        startActivity(intent);

        return true;
      }
    });
  }

  private void startActivityForResult(Intent enableBluetooth, String bt_enable_req) {
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    if (id == R.id.action_settings) {
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private class DeviceSearch extends AsyncTask<Void, Void, ArrayList<BluetoothDevice>> {

    @Override
    protected ArrayList<BluetoothDevice> doInBackground(Void... params) {
      Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
      ArrayList<BluetoothDevice> listDevices = new ArrayList<BluetoothDevice>();
      for (BluetoothDevice device : pairedDevices) {
        Log.d("DEBUG", device.getName());
        listDevices.add(device);
      }
      return listDevices;
    }

    @Override
    protected void onPostExecute(ArrayList<BluetoothDevice> listDevices) {
      super.onPostExecute(listDevices);
      if (listDevices.size() > 0) {
        DeviceAdapter adapter = new DeviceAdapter(getBaseContext(), listDevices);
        deviceListView.setAdapter(adapter);
      } else {
        makeText(getApplicationContext(), "Device Not Found", LENGTH_SHORT).show();
      }
    }
  }
}
