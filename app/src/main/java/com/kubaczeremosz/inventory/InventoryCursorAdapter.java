package com.kubaczeremosz.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Kuba on 2017-07-16.
 */

public class InventoryCursorAdapter extends CursorAdapter {

    private static Context mContext;

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        TextView nameTextView = (TextView) view.findViewById(R.id.item_name);
        TextView priceTextView = (TextView) view.findViewById(R.id.item_price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.item_quantity);
        Button sell = (Button) view.findViewById(R.id.item_sell);

        final int itemId = cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry._ID));
        int nameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRICE);
        final int quantity = cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_QUANTITY));


        String itemName = cursor.getString(nameColumnIndex);
        String itemQuantity = cursor.getString(quantityColumnIndex);
        String itemPrice = cursor.getString(priceColumnIndex);

        nameTextView.setText(itemName);
        quantityTextView.setText(itemQuantity);
        priceTextView.setText(itemPrice);
        sell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri itemUri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, itemId);
                sell(context, itemUri, quantity);
            }
        });
    }

    private void sell(Context context, Uri itemUri, int quantity) {

        if (quantity > 0) {
            quantity--;
        } else {
            Toast.makeText(context, context.getString(R.string.outofstock),
                    Toast.LENGTH_SHORT).show();
        }
        ContentValues values = new ContentValues();
        values.put(InventoryContract.InventoryEntry.COLUMN_QUANTITY, quantity);
        context.getContentResolver().update(itemUri, values, null, null);
    }
}
