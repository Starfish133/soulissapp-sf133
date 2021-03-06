package it.angelic.soulissclient;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.angelic.soulissclient.drawer.DrawerMenuHelper;
import it.angelic.soulissclient.drawer.NavDrawerAdapter;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.net.Constants;
import it.angelic.soulissclient.preferences.BroadcastSettingsFragment;
import it.angelic.soulissclient.preferences.BroadcastSettingsPreferenceListener;
import it.angelic.soulissclient.preferences.DbPreferenceListener;
import it.angelic.soulissclient.preferences.DbSettingsFragment;
import it.angelic.soulissclient.preferences.IpChangerListener;
import it.angelic.soulissclient.preferences.NetSettingsFragment;
import it.angelic.soulissclient.preferences.ServicePreferenceListener;
import it.angelic.soulissclient.preferences.ServiceSettingsFragment;
import it.angelic.soulissclient.preferences.SetHtmlRootListener;
import it.angelic.soulissclient.preferences.VisualSettingsFragment;
import it.angelic.soulissclient.preferences.WebServerPreferenceListener;

import static it.angelic.soulissclient.Constants.TAG;

public class PreferencesActivity extends PreferenceActivity {
    protected TextView info1;
    protected TextView info2;
    protected DrawerMenuHelper dmh;
    protected DrawerLayout mDrawerLayout;
    protected ActionBarDrawerToggle mDrawerToggle;
    protected ListView mDrawerList;
    protected NavDrawerAdapter mDrawermAdapter;
    SoulissPreferenceHelper opzioni;
    // private String strVersionCode;
    private String strVersionName;
    private String currentScreen;
    // Aggiorna la schermata
    private BroadcastReceiver macacoRawDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                Bundle extras = intent.getExtras();
                ArrayList<Short> vers = (ArrayList<Short>) extras.get("MACACO");
                Log.w(TAG, "RAW DATA: " + vers);
                switch (vers.get(0)) {
                    case Constants.Souliss_UDP_function_typreq_resp:
                        // fallthrought x refresh dicitura tipici
                    case Constants.Souliss_UDP_function_db_struct_resp:
                        Log.w(TAG, "DB STRUCT: " + currentScreen);
                        //if (currentScreen != null && currentScreen.equals("db_setup")) {
                        Intent inten = PreferencesActivity.this.getIntent();
                        inten.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        PreferencesActivity.this.finish();
                        PreferencesActivity.this.overridePendingTransition(0, 0);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                            inten.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, DbSettingsFragment.class.getName());
                        inten.setAction("db_setup");
                        inten.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        Toast.makeText(PreferencesActivity.this,
                                PreferencesActivity.this.getResources().getString(R.string.dbstruct_req),
                                Toast.LENGTH_SHORT).show();
                        PreferencesActivity.this.startActivity(inten);
                        //}
                        break;
                /*case Constants.Souliss_UDP_function_ping_resp:// restart
					if (currentScreen != null && currentScreen.equals("network_setup")) {
						Intent intend = PreferencesActivity.this.getIntent();
						intend.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
						PreferencesActivity.this.finish();
						PreferencesActivity.this.overridePendingTransition(0, 0);
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
							AlertDialogHelper.setExtra(intend, NetSettingsFragment.class.getName());
						// preferencesActivity.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS,com);
						intend.setAction("network_setup");

						intend.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

						PreferencesActivity.this.overridePendingTransition(0, 0);
						PreferencesActivity.this.startActivity(intend);
					}
					break;*/
                    default:
                        break;
                }
            } catch (Exception e) {
                Log.e(TAG, "EMPTY RAW dATA !!");
            }
        }
    };
    private Toolbar bar;
    private LinearLayout mDrawerLinear;

	/*
	 * @TargetApi(11) protected void AddResourceApi11AndGreater() {
	 * getFragmentManager().beginTransaction().replace(android.R.id.content, new
	 * PrefsFragment()).commit(); }
	 */

    @TargetApi(11)
    @Override
    public void onBuildHeaders(List<Header> target) {
        Log.i(TAG, "PreferenceActivityonBuildHeaders()");
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    @SuppressWarnings("deprecation")
    protected void AddResourceApiLessThan11(String action) {
        if (action != null && action.equals("network_setup")) {
            addPreferencesFromResource(R.xml.settings_net);
        } else if (action != null && action.equals("db_setup")) {
            addPreferencesFromResource(R.xml.settings_db);
        } else if (action != null && action.equals("service_setup")) {
            addPreferencesFromResource(R.xml.settings_dataservice);
        } else if (action != null && action.equals("visual_setup")) {
            addPreferencesFromResource(R.xml.settings_visual);
        } else if (action != null && action.equals("bcast_setup")) {
            addPreferencesFromResource(R.xml.settings_broadcast);
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            // Load the legacy preferences headers
            addPreferencesFromResource(R.xml.preferences_legacy);
        }
    }


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void fit(View v) {
        v.setFitsSystemWindows(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        opzioni = SoulissClient.getOpzioni();
        if (opzioni.isLightThemeSelected()) {
            setTheme(R.style.LightThemeSelector);
        } else
            setTheme(R.style.DarkThemeSelector);
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        try {
            getClass().getMethod("getFragmentManager");
            // non serve, ci sono headers
            // AddResourceApi11AndGreater();
        } catch (NoSuchMethodException e) { // Api < 11
            String action = getIntent().getAction();
            AddResourceApiLessThan11(action);
        }

        ListView v = getListView();
        v.setCacheColorHint(0);
        //LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        //bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.custom_actionbar, root, false);
        //root.addView(bar, 0); // insert at top
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            fit(v);
        }

    }

    protected void setActionBarInfo(String title) {
        try {
            //actionBar = getSupportActionBar();
            View ds = bar.getRootView();

            ImageButton online = (ImageButton) ds.findViewById(R.id.action_starred);
            TextView statusOnline = (TextView) ds.findViewById(R.id.online_status);
            TextView actionTitle = (TextView) ds.findViewById(R.id.actionbar_title);
            actionTitle.setText(title);

            if (!opzioni.isSoulissReachable()) {
                online.setBackgroundResource(R.drawable.red);
                statusOnline.setTextColor(getResources().getColor(R.color.std_red));
                statusOnline.setText(R.string.offline);

            } else {
                online.setBackgroundResource(R.drawable.green);
                statusOnline.setTextColor(getResources().getColor(R.color.std_green));
                statusOnline.setText(R.string.Online);
            }
        } catch (Exception e) {
            Log.e(it.angelic.soulissclient.Constants.TAG, "null bar? " + e.getMessage());
        }
    }

    @SuppressLint("NewApi")
    @Override
    protected void onStart() {
        super.onStart();
        setActionBarInfo(getString(R.string.app_opt));
        currentScreen = getIntent().getAction();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Log.d(TAG, "Going thru preference onStart(), screeN: " + currentScreen);
            return;
        }
        opzioni.initializePrefs();
		/*
		 * arrivo dai fragments, evido code Dup SCHERMATA NETWORK
		 */
        if (currentScreen != null && currentScreen.equals("network_setup")) {
            Preference privateIP = (Preference) findPreference("edittext_IP");
            // Vedi se e` gia` settato l'IP PUBBLICO
            Preference publicIP = (Preference) findPreference("edittext_IP_pubb");
            String summar = getResources().getString(R.string.summary_edittext_IP);
            privateIP.setSummary(opzioni.getPrefIPAddress().compareToIgnoreCase("") == 0 ? summar : opzioni
                    .getPrefIPAddress());

            String summarP = getResources().getString(R.string.summary_edittext_IP_pubb);
            publicIP.setSummary(opzioni.getIPPreferencePublic().compareToIgnoreCase("") == 0 ? summarP : opzioni
                    .getIPPreferencePublic());
			/* aggiorna il sommario PUBBLICO una volta modificato */
            OnPreferenceChangeListener ipChanger = new IpChangerListener(this);
            privateIP.setOnPreferenceChangeListener(ipChanger);
            publicIP.setOnPreferenceChangeListener(ipChanger);
            final Preference userIdx = (Preference) findPreference("userindexIC");
            final Preference nodeIndex = (Preference) findPreference("nodeindexIC");
            final Preference bCast = (Preference) findPreference("advbroadcastKey");
            bCast.setOnPreferenceClickListener(new BroadcastSettingsPreferenceListener(this));

            String strMeatFormat = getString(R.string.opt_nodeindex_desc);
            nodeIndex.setSummary(String.format(strMeatFormat, opzioni.getNodeIndex()));

            String stdrMeatFormat = getString(R.string.opt_userindex_desc);
            userIdx.setSummary(String.format(stdrMeatFormat, opzioni.getUserIndex()));

            userIdx.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Log.w(Constants.TAG, "CHANGING USER INDEX:" + newValue);
                    try {
                        String ics = (String) newValue;
                        Integer rete = Integer.parseInt(ics);
                        // enforce 0 < x < 0xfe
                        if (rete >= it.angelic.soulissclient.Constants.MAX_USER_IDX || rete < 1)
                            throw new IllegalArgumentException();
                        opzioni.setUserIndex(rete);
                        String stdrMeatFormat = getString(R.string.opt_userindex_desc);
                        userIdx.setSummary(String.format(stdrMeatFormat, opzioni.getUserIndex()));
                    } catch (Exception e) {
                        Toast.makeText(PreferencesActivity.this, getString(R.string.useridxhint), Toast.LENGTH_SHORT)
                                .show();
                    }
                    return true;
                }
            });
            nodeIndex.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Log.w(Constants.TAG, "CHANGING NODE INDEX:" + newValue);
                    try {
                        String ics = (String) newValue;
                        if (Integer.parseInt(ics) >= it.angelic.soulissclient.Constants.MAX_NODE_IDX
                                || Integer.parseInt(ics) < 1)// enforce 1 < x <
                            // 100
                            throw new IllegalArgumentException();
                        opzioni.setNodeIndex(Integer.parseInt(ics));
                        String strMeatFormat = getString(R.string.opt_nodeindex_desc);
                        nodeIndex.setSummary(String.format(strMeatFormat, opzioni.getNodeIndex()));
                    } catch (Exception e) {
                        Toast.makeText(PreferencesActivity.this, getString(R.string.nodeidxhint), Toast.LENGTH_SHORT)
                                .show();
                    }
                    return true;
                }
            });

        } else if (currentScreen != null && currentScreen.equals("db_setup")) {// SCHERMATA
            // DBINFO
            Preference createDbPref = (Preference) findPreference("createdb");
            Preference dropDbPref = (Preference) findPreference("dropdb");
            Preference exportDBPref = (Preference) findPreference("dbexp");
            Preference imortDBPref = (Preference) findPreference("dbimp");
            Preference dbinfopref = (Preference) findPreference("dbinfo");

            exportDBPref.setOnPreferenceClickListener(new DbPreferenceListener(this));
			/* Scegli il file e importa il DB */
            imortDBPref.setOnPreferenceClickListener(new DbPreferenceListener(this));
            // richiesta creazione nodi
            createDbPref.setOnPreferenceClickListener(new DbPreferenceListener(this));
            // dialogo drop DB
            dropDbPref.setOnPreferenceClickListener(new DbPreferenceListener(this));

            String strMeatFormat = getString(R.string.opt_dbinfo_desc);
            final String strMeatMsg = opzioni.getCustomPref().getInt("numNodi", 0) == 0 ? getString(R.string.dialog_disabled_db)
                    : String.format(strMeatFormat, opzioni.getCustomPref().getInt("numNodi", 0), opzioni
                    .getCustomPref().getInt("numTipici", 0));
            dbinfopref.setSummary(strMeatMsg);
        } else if (currentScreen != null && currentScreen.equals("service_setup")) {
            final Preference serviceActive = (Preference) findPreference("checkboxService");
            final Preference webserviceActive = (Preference) findPreference("webserverEnabled");
            final Preference setHomeLocation = (Preference) findPreference("setHomeLocation");
            final Preference setHtmlRoot = (Preference) findPreference("setHtmlRoot");
            final LocationManager locationManager;
            // EXPORT

            final String provider;
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            // criteria.setAccuracy(Criteria.ACCURACY_FINE);
            provider = locationManager.getBestProvider(criteria, true);
            // datasource = new SoulissDBHelper(getActivity());

			/* START STOP SoulissDataService */
            serviceActive.setOnPreferenceChangeListener(new ServicePreferenceListener(this));

			/* START STOP SoulissDataService */
            webserviceActive.setOnPreferenceChangeListener(new WebServerPreferenceListener(this));

            // Setta home location
            setHomeLocation.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    try {
                        Location luogo = locationManager.getLastKnownLocation(provider);
                        opzioni.setHomeLatitude(luogo.getLatitude());
                        opzioni.setHomeLongitude(luogo.getLongitude());
                        opzioni.reload();
                        Intent inten = PreferencesActivity.this.getIntent();
                        PreferencesActivity.this.overridePendingTransition(0, 0);
                        PreferencesActivity.this.finish();
                        inten.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                            AlertDialogHelper.setExtra(inten, ServiceSettingsFragment.class.getName());
                        inten.setAction("service_setup");

                        PreferencesActivity.this.overridePendingTransition(0, 0);
                        PreferencesActivity.this.startActivity(inten);
                    } catch (Exception e) {
                        Toast.makeText(PreferencesActivity.this, "Error getting current position", Toast.LENGTH_SHORT)
                                .show();
                    }
                    return true;
                }
            });

            setHtmlRoot.setOnPreferenceClickListener(new SetHtmlRootListener(this));

            String loc = null;
            if (opzioni.getHomeLatitude() != 0) {

                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> list;
                try {
                    list = geocoder.getFromLocation(opzioni.getHomeLatitude(), opzioni.getHomeLongitude(), 1);
                    if (list != null && list.size() > 0) {
                        Address address = list.get(0);
                        loc = address.getLocality();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "LOCATION ERR:" + e.getMessage());
                }

            }
            setHomeLocation.setSummary("Home location set to: " + (loc == null ? "" : loc) + " ("
                    + opzioni.getHomeLatitude() + " : " + opzioni.getHomeLongitude() + ")");

        } else if (currentScreen != null && currentScreen.equals("visual_setup")) {

            final Preference restoreWarns = (Preference) findPreference("restoredialogs");
            restoreWarns.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    opzioni.setDontShowAgain(getResources().getString(R.string.dialog_disabled_db), false);
                    opzioni.setDontShowAgain(getResources().getString(R.string.dialog_disabled_service), false);
                    Toast.makeText(PreferencesActivity.this,
                            SoulissClient.getAppContext().getString(R.string.opt_dialog_restored), Toast.LENGTH_SHORT)
                            .show();
                    return true;
                }
            });
        } else if (currentScreen != null && currentScreen.equals("bcast_setup")) {
            Toast.makeText(PreferencesActivity.this,
                    "Function not supported on Android 2, sorry", Toast.LENGTH_LONG)
                    .show();

        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            try {
                PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                // strVersionCode = "Version Code: "
                // + String.valueOf(packageInfo.versionCode);
                strVersionName = packageInfo.versionName;

            } catch (NameNotFoundException e) {
                Log.e(TAG, "Cannot load Version!", e);
                strVersionName = "Cannot load Version!";
            }
            Preference creditsPref = (Preference) findPreference("credits");
            creditsPref.setTitle(getResources().getString(R.string.app_name) + " Version " + strVersionName);
            // Rimette i dialogs

        }

    }

    @Override
    protected void onResume() {
        // IDEM, serve solo per reporting
        IntentFilter filtere = new IntentFilter();
        filtere.addAction(it.angelic.soulissclient.net.Constants.CUSTOM_INTENT_SOULISS_RAWDATA);
        registerReceiver(macacoRawDataReceiver, filtere);

        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(macacoRawDataReceiver);
        super.onPause();
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        if (DbSettingsFragment.class.getName().equals(fragmentName))
            return true;
        if (NetSettingsFragment.class.getName().equals(fragmentName))
            return true;
        if (ServiceSettingsFragment.class.getName().equals(fragmentName))
            return true;
        if (VisualSettingsFragment.class.getName().equals(fragmentName))
            return true;
        return BroadcastSettingsFragment.class.getName().equals(fragmentName);

    }



}
