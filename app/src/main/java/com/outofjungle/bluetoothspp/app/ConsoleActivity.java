package com.outofjungle.bluetoothspp.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.outofjungle.bluetoothspp.app.models.Message;
import com.outofjungle.bluetoothspp.app.models.Writer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.UUID;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

public class ConsoleActivity extends Activity {

  private ProgressDialog progressDialog;
  private UUID androidUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
  private BluetoothDevice bluetoothDevice;
  private BluetoothSocket bluetoothSocket;
  private boolean bluetoothConnected = false;
  private RxRunner rxRunner = null;

  private Button txButton;
  private EditText txEditText;

  private MessageAdapter messageListAdapter;
  private ArrayList<Message> messageList = new ArrayList<Message>();
  private ListView messageListView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_console);

    Intent intent = getIntent();
    Bundle b = intent.getExtras();
    bluetoothDevice = b.getParcelable("DEVICE");

    setTitle(String.format(bluetoothDevice.getName()));

    messageListAdapter = new MessageAdapter(getBaseContext(), messageList);
    messageListView = (ListView) findViewById(R.id.messageListView);
    messageListView.setAdapter(messageListAdapter);

    txEditText = (EditText) findViewById(R.id.txText);
    txButton = (Button) findViewById(R.id.txButton);

    txButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View arg0) {
        try {
          final String txText = txEditText.getText().toString();
          bluetoothSocket.getOutputStream().write(txText.getBytes());
          txEditText.setText("");

          messageListView.post(new Runnable() {
            @Override
            public void run() {
              Log.d("TX", txText);
              Message message = new Message(txText, Writer.ANDROID);
              messageList.add(message);
              messageListAdapter.notifyDataSetChanged();
              messageListView.setSelection(messageListAdapter.getCount() - 1);
            }
          });
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
  }

  @Override
  protected void onPause() {
    if (bluetoothSocket != null && bluetoothConnected) {
      new BluetoothDisconnect().execute();
    }
    super.onPause();
  }

  @Override
  protected void onResume() {
    if (bluetoothSocket == null || !bluetoothConnected) {
      new BluetoothConnect().execute();
    }
    super.onResume();
  }

  @Override
  protected void onStop() {
    super.onStop();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.console, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.action_settings) {
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private class BluetoothConnect extends AsyncTask<Void, Void, Void> {
    private boolean connected = false;

    @Override
    protected void onPreExecute() {
      progressDialog = ProgressDialog.show(ConsoleActivity.this, bluetoothDevice.getName(), "Connecting...");
    }

    @Override
    protected Void doInBackground(Void... devices) {
      try {
        if (bluetoothSocket == null || !bluetoothConnected) {
          bluetoothSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(androidUUID);
          BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
          bluetoothSocket.connect();
        }
        connected = true;
      } catch (IOException e) {
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void result) {
      super.onPostExecute(result);

      if (!connected) {
        makeText(getApplicationContext(), String.format("Could not connect to %s", bluetoothDevice.getName()), Toast.LENGTH_LONG).show();
        finish();
      } else {
        makeText(getApplicationContext(), String.format("Connected to %s", bluetoothDevice.getName()), LENGTH_SHORT).show();
        bluetoothConnected = true;
        rxRunner = new RxRunner();
      }
      progressDialog.dismiss();
    }
  }

  private class BluetoothDisconnect extends AsyncTask<Void, Void, Void> {

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected Void doInBackground(Void... params) {

      if (rxRunner != null) {
        rxRunner.stop();
        while (rxRunner.isRunning()) ;
        rxRunner = null;
      }

      try {
        bluetoothSocket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }

      return null;
    }

    @Override
    protected void onPostExecute(Void result) {
      super.onPostExecute(result);
      bluetoothConnected = false;
    }
  }

  private class RxRunner implements Runnable {
    private boolean halt = false;
    private Thread rxThread;

    public RxRunner() {
      rxThread = new Thread(this, "Input Thread");
      rxThread.start();
    }

    public boolean isRunning() {
      return rxThread.isAlive();
    }

    @Override
    public void run() {
      InputStream rxStream;

      try {
        rxStream = bluetoothSocket.getInputStream();
        while (!halt) {
          byte[] buffer = new byte[256];
          if (rxStream.available() > 0) {
            rxStream.read(buffer);

            int i;
            for (i = 0; (i < buffer.length) && (0 != buffer[i]); i++) {
            }
            final String rxText = new String(buffer, 0, i);

            messageListView.post(new Runnable() {
              @Override
              public void run() {
                Log.d("RX", rxText);
                Message message = new Message(rxText, Writer.ARDUINO);
                messageList.add(message);
                messageListAdapter.notifyDataSetChanged();
                messageListView.setSelection(messageListAdapter.getCount() - 1);
              }
            });
          }
          Thread.sleep(500);
        }
      } catch (IOException e) {
        e.printStackTrace();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    public void stop() {
      halt = true;
    }
  }
}
