package projekt.budgetdroid;


import android.graphics.DashPathEffect;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidplot.util.PixelUtils;
import com.androidplot.xy.CatmullRomInterpolator;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class SummaryFragment extends Fragment {


    public SummaryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_summary, container, false);

        final List<String> dates = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        List<Number> positions = new ArrayList<>();
        XYPlot plot = (XYPlot) view.findViewById(R.id.plot);
        TextView mTxtViewSaldo = (TextView) view.findViewById(R.id.text_view_balance);
        TextView mTxtViewMin = (TextView) view.findViewById(R.id.textView_min);
        TextView mTxtViewMax = (TextView) view.findViewById(R.id.textView_max);
        final DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        List<TransactionModel> transactionsList = ((MainActivity)getActivity()).manager.getAllTransactions();
        Collections.sort(transactionsList, new Comparator<TransactionModel>() {
            @Override
            public int compare(TransactionModel o1, TransactionModel o2) {
                Date date1 = null, date2 = null;

                try {
                    date1 = (Date)formatter.parse(o1.getDate());
                    date2 = (Date)formatter.parse(o2.getDate());
                }
                catch (Exception ex) {  }
                return date1.compareTo(date2);
            }
        });

        for (int i = 0; i< transactionsList.size();i++) {
            try {
                dates.add(formatter.parse(transactionsList.get(i).getDate()).toString());
            } catch (ParseException e) { }
            if(i>0) {
                values.add(values.get(i - 1) + transactionsList.get(i).getConvertedValue());
            }
            else
            {
                values.add(transactionsList.get(i).getConvertedValue());
            }
            positions.add(i);
        }

        //XYSeries series1 = new SimpleXYSeries(positions, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Data");
        XYSeries series1 = new SimpleXYSeries(values, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Stan konta");

        // create formatters to use for drawing a series using LineAndPointRenderer
        // and configure them from xml:
        LineAndPointFormatter series1Format =
                new LineAndPointFormatter(getActivity(), R.xml.line_point_formatter);

        // add an "dash" effect to the series2 line:
//        series2Format.getLinePaint().setPathEffect(new DashPathEffect(new float[] {
//
//                // always use DP when specifying pixel sizes, to keep things consistent across devices:
//                PixelUtils.dpToPix(20),
//                PixelUtils.dpToPix(15)}, 0));

        // just for fun, add some smoothing to the lines:
        // see: http://androidplot.com/smooth-curves-and-androidplot/
        series1Format.setInterpolationParams(
                new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));

//        series2Format.setInterpolationParams(
//                new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));

        // add a new series' to the xyplot:
        plot.addSeries(series1, series1Format);

        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                int i = Math.round(((Number) obj).floatValue());
                return toAppendTo.append(dates.get(i));
            }
            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;
            }
        });

        Double min = 0.00;
        Double max = values.get(0);

        for(Double i: values) {
            if(i < min) min = i;
            if(i > max) max = i;
        }

        mTxtViewSaldo.setText(String.format("%.2f",values.get(values.size()-1)));
        mTxtViewMax.setText(String.format("%.2f",max));
        mTxtViewMin.setText(String.format("%.2f",min));

        return view;
    }

}
