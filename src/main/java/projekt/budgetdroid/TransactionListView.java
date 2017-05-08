package projekt.budgetdroid;


import android.annotation.TargetApi;
import android.app.ListFragment;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.florescu.android.rangeseekbar.RangeSeekBar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class TransactionListView extends Fragment {

    private ArrayAdapter<TransactionModel> adapter;

    public TransactionListView() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){

        final View view = inflater.inflate(R.layout.fragment_list, container, false);
        ListView listView = (ListView)view.findViewById(R.id.fragmnet_listView);
        adapter = new TransactionArrayAdapter(getActivity(),  getModel());
        listView.setAdapter(adapter);

        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                view.findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_sort:
                                showSortDialog(inflater, view);
                                break;
                            case R.id.action_filter:
                                showFilterDialog(inflater, view);
                                break;
                        }
                        return true;
                    }
                });
        return view;
    }

    private void showFilterDialog(LayoutInflater inflater, View fragmentView) {
        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        final View mView = inflater.inflate(R.layout.filter_dialog,null);
        final RangeSeekBar<Double> mSeekBar = (RangeSeekBar<Double>) mView.findViewById(R.id.rangeSeekBar);
        final Spinner mCurrSpinner = (Spinner) mView.findViewById(R.id.spinner_currency);
        Button mButton = (Button) mView.findViewById(R.id.filtre_button);

        int currencyIndex = getActivity().getSharedPreferences("PREFERENCE", 0).getInt("defaultCurrencyIndex",0);
        final String currency = getActivity().getResources().getStringArray(R.array.currency_array)[currencyIndex];
        final Pair<Double,Double> range = ((MainActivity)getActivity()).manager.GetValuesRange(currency);
        mSeekBar.setTextAboveThumbsColor(Color.BLACK);
        mCurrSpinner.setSelection(currencyIndex);

        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();

        mButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                adapter.getFilter().filter(mCurrSpinner.getSelectedItem() + "_" + mSeekBar.getSelectedMinValue() + "_" + mSeekBar.getSelectedMaxValue());
                dialog.dismiss();
            }
        });
        mCurrSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Pair<Double,Double> range = ((MainActivity)getActivity()).manager.GetValuesRange((String)parent.getItemAtPosition(position));
                mSeekBar.setRangeValues(range.first, range.second +1);
                mSeekBar.resetSelectedValues();
                mSeekBar.setSelectedMaxValue(range.second+1);
                mSeekBar.setSelectedMinValue(range.first);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {  }
        } );

        dialog.show();
    }

    private void showSortDialog(LayoutInflater inflater, final View fragmentView) {
        final AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        final View mView = inflater.inflate(R.layout.sort_dialog,null);
        final Spinner mSortBySpinner = (Spinner) mView.findViewById(R.id.spinner_sort_by);
        final Spinner mOrderSpinner = (Spinner) mView.findViewById(R.id.spinner_order);
        Button mButton = (Button) mView.findViewById(R.id.sort_button);

        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();

        mButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                sort(fragmentView, mSortBySpinner.getSelectedItem().toString(), mOrderSpinner.getSelectedItem().toString());
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void sort(View view, String sortBy, String orderBy) {
        ListView listView = (ListView)view.findViewById(R.id.fragmnet_listView);
        final int factor = orderBy.equals("Rosnąco") ? 1 : -1;

        if(sortBy.equals("Wartość"))
        {
            adapter.sort(new Comparator<TransactionModel>() {
                @Override
                public int compare(TransactionModel o1, TransactionModel o2) {
                    return factor * o1.getValue().compareTo(o2.getValue());
                }
            });

        }
        else if(sortBy.equals("Data"))
        {
            adapter.sort(new Comparator<TransactionModel>() {
                @Override
                public int compare(TransactionModel o1, TransactionModel o2) {
                    Date date1 = null, date2 = null;
                    DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                    try {
                        date1 = (Date)formatter.parse(o1.getDate());
                        date2 = (Date)formatter.parse(o2.getDate());
                    }
                    catch (Exception ex) {  }
                    return factor * date1.compareTo(date2);
                }
            });
        }

        listView.setAdapter(adapter);
    }

    private List<TransactionModel> getModel() {
        return  ((MainActivity)getActivity()).manager.getAllTransactions();
    }


}
