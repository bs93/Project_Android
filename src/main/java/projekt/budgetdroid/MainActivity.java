package projekt.budgetdroid;

import android.app.Activity;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.github.johnpersano.supertoasts.library.SuperToast;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    NavigationView navigationView = null;
    Toolbar toolbar = null;
    public DbManager manager;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //set the summary fragment
        setFragment(new SummaryFragment());

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        manager = new DbManager(this);

        updateCurrency();

        boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("isFirstRun", true);
        if (isFirstRun) {
            currencySetUp();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void updateCurrency() {
        progressDialog = ProgressDialog.show(MainActivity.this, "Proszę czekać ...",  "Aktualizacja kursów walut ...", true);
        progressDialog.setCancelable(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean updated = true;
                try {
                    HttpHandler sh = new HttpHandler();

                    // Making a request to url and getting response
                    String jsonStr = sh.makeServiceCall("http://api.fixer.io/latest");

                    if (jsonStr != null) {
                        JSONObject jsonObj = new JSONObject(jsonStr);
                        JSONObject ratesObj = jsonObj.getJSONObject("rates");

                        String date = jsonObj.getString("date");
                        String PLNrate = ratesObj.getString("PLN");
                        String USDrate = ratesObj.getString("USD");

                        manager.UpdateCurrencies(date, PLNrate, USDrate);
                        Thread.sleep(1000); // Let's wait for some time
                    }

                    else {
                        updated = false;
                    }

                } catch (Exception e) {
                    updated = false;
                }
                finally {
                    progressDialog.dismiss();


                    if (!updated)
                    {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast toast = Toast.makeText(getApplicationContext(), "Aktualizacja zakończona niepowodzeniem", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        });
                    }
                }

            }
        }).start();
    }

    public void setFragment(Fragment fragment)
    {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    public void currencySetUp() {
            final AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
            final View mView = getLayoutInflater().inflate(R.layout.dialog_first_run,null);
            final Spinner mSpinner = (Spinner) mView.findViewById(R.id.firstRun_sp_currency);
            Button mButton = (Button) mView.findViewById(R.id.firstRun_btn_ok);

            mSpinner.setSelection(getSharedPreferences("PREFERENCE", 0).getInt("defaultCurrencyIndex",0));
            mBuilder.setView(mView);
            final AlertDialog dialog = mBuilder.create();

            mButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                            .edit()
                            .putInt("defaultCurrencyIndex", mSpinner.getSelectedItemPosition())
                            .putBoolean("isFirstRun", false)
                            .apply();
                    dialog.dismiss();
                }
            });

            dialog.show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            currencySetUp();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_summary) {
            setFragment(new SummaryFragment());
        } else if (id == R.id.nav_addTransaction) {
            setFragment(new AddTransactionFragment());
        } else if (id == R.id.nav_list) {
            setFragment(new TransactionListView());
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
