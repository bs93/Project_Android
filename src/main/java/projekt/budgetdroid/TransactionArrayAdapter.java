package projekt.budgetdroid;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Szmolke on 2017-04-06.
 */

public class TransactionArrayAdapter extends ArrayAdapter<TransactionModel> implements Filterable{

    private List<TransactionModel> list;
    private List<TransactionModel> oryginalList;
    private final Activity context;

    public TransactionArrayAdapter(Activity context, List<TransactionModel> list) {
        super(context, R.layout.list_layout, list);
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View view = ((Activity)getContext()).getLayoutInflater().inflate(R.layout.list_layout, null);
        TextView label = (TextView) view.findViewById(R.id.label);
        TextView sub = (TextView) view.findViewById(R.id.sub);
        TextView trans_val = (TextView) view.findViewById(R.id.trans_val);

        TransactionModel currentItem = list.get(position);

        label.setText(currentItem.getName());
        label.setTextColor(Color.BLACK);
        sub.setText(currentItem.getDate());
        sub.setTextColor(Color.GRAY);
        trans_val.setText(String.format("%s %s", currentItem.getValue(), currentItem.getCurrency()));

        if (Double.parseDouble(currentItem.getValue()) > 0) {
                trans_val.setTextColor(Color.parseColor("#0c905c"));
        }
        else {
            trans_val.setTextColor(Color.RED);
        }

        return view;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,FilterResults results) {

                list = (List<TransactionModel>) results.values; // has the filtered values
                notifyDataSetChanged();  // notifies the data with new filtered values
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                List<TransactionModel> FilteredArrList = new ArrayList<>();

                if (oryginalList == null) {
                    oryginalList = new ArrayList<TransactionModel>(list); // saves the original data in mOriginalValues
                }

                /********
                 *
                 *  If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
                 *  else does the Filtering and returns FilteredArrList(Filtered)
                 *
                 ********/
                if (constraint == null || constraint.length() == 0) {

                    // set the Original result to return
                    results.count = oryginalList.size();
                    results.values = oryginalList;
                } else {
                    String[] params = constraint.toString().split("_");
                    String currency = params[0];
                    Double minVal = new Double(params[1]);
                    Double maxVal = new Double(params[2]);

                    for (int i = 0; i < oryginalList.size(); i++) {
                        TransactionModel data = oryginalList.get(i);
                        if (data.getCurrency().equals(currency) && data.getConvertedValue() >= minVal && data.getConvertedValue() <= maxVal) {
                            FilteredArrList.add(data);
                        }
                    }
                    // set the Filtered result to return
                    results.count = FilteredArrList.size();
                    results.values = FilteredArrList;
                }
                return results;
            }
        };
        return filter;
    }

}

