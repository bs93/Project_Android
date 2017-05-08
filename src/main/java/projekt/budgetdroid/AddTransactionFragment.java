package projekt.budgetdroid;


import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddTransactionFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    EditText dateEdit;
    EditText descriptionEdit;
    EditText valueEdit;
    EditText nameEdit;
    Spinner currSpinner;
    Button btnAdd;

    public AddTransactionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_add_transaction, container, false);
        View view = inflater.inflate(R.layout.fragment_add_transaction, container, false);
        nameEdit = (EditText) view.findViewById(R.id.transName_EditText);
        currSpinner = (Spinner) view.findViewById(R.id.currency_spinner);

        SharedPreferences preferences = this.getActivity().getSharedPreferences("PREFERENCE",0);
        currSpinner.setSelection(preferences.getInt("defaultCurrencyIndex",0));

        btnAdd = (Button) view.findViewById(R.id.AddButton);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!validateInput())
                {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Uzupełnij wszystkie pola", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });

                    return;
                }

                boolean isInserted = true;
                try {
                    ((MainActivity) getActivity()).manager.insertTransaction(nameEdit.getText().toString(),
                            descriptionEdit.getText().toString(),
                            currSpinner.getSelectedItem().toString(),
                            dateEdit.getText().toString(),
                            Double.parseDouble(valueEdit.getText().toString()));
                }
                catch (Exception ex)
                {
                    isInserted = false;
                }
                finally {
                    ((MainActivity)getActivity()).setFragment(new SummaryFragment());

                    if(isInserted)
                    {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Dodano nowy wpis", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        });

                    }
                    else
                    {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Operacja zakończona niepowodzeniem", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        });

                    }
                }
            }
        });
        descriptionEdit = (EditText)view.findViewById(R.id.description_EditText);
        descriptionEdit.setHorizontallyScrolling(false);
        descriptionEdit.setMaxLines(Integer.MAX_VALUE);
        valueEdit = (EditText) view.findViewById(R.id.transValue_TextEdit);
        dateEdit = (EditText)view.findViewById(R.id.dateEditText);
        dateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomDatePicker(v);
            }
        });

        dateEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus)
                {
                    showCustomDatePicker(v);
                }

            }
        });
        return  view;
    }

    private boolean validateInput() {
        if (nameEdit.getText().toString() == "" ||
        descriptionEdit.getText().toString() == "" ||
        currSpinner.getSelectedItem().toString() == "" ||
        dateEdit.getText().toString() == "")
        {
            return false;
        }
        return true;
    }

    private void showCustomDatePicker(View v) {
        InputMethodManager imm =  (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

        DialogFragment newFragment = new DatePickerFragment(); // creating DialogFragment which creates DatePickerDialog
        newFragment.setTargetFragment(AddTransactionFragment.this,0);  // Passing this fragment DatePickerFragment.
        // As i figured out this is the best way to keep the reference to calling activity when using FRAGMENT.
        newFragment.show(getActivity().getFragmentManager(), "datePicker");
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) { // what should be done when a date is selected
        StringBuilder sb = new StringBuilder().append(dayOfMonth).append("/").append(monthOfYear + 1).append("/").append(year);
        String formattedDate = sb.toString();
        dateEdit.setText(formattedDate);

        valueEdit.requestFocusFromTouch();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }
}
