package com.outofjungle.bluetoothspp.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.outofjungle.bluetoothspp.app.models.Message;

import java.util.ArrayList;

public class MessageAdapter extends ArrayAdapter<Message> {
  public MessageAdapter(Context context, ArrayList<Message> messages) {
    super(context, R.layout.message_item, messages);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    if (convertView == null) {
      convertView = LayoutInflater.from(getContext()).inflate(R.layout.message_item, parent, false);
    }

    Message message = getItem(position);


    TextView writerName = (TextView) convertView.findViewById(R.id.writerName);
    TextView messageText = (TextView) convertView.findViewById(R.id.messageText);

    writerName.setText(message.getWriter().toString());
    messageText.setText(message.getText());

    return convertView;
  }
}