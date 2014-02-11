package it.angelic.soulissclient.fragments;

import static it.angelic.soulissclient.model.typicals.Constants.Souliss_T1n_OffCoil;
import static it.angelic.soulissclient.model.typicals.Constants.Souliss_T1n_OnCoil;
import static junit.framework.Assert.assertTrue;
import it.angelic.soulissclient.Constants;
import it.angelic.soulissclient.R;
import it.angelic.soulissclient.SoulissClient;
import it.angelic.soulissclient.db.SoulissDBHelper;
import it.angelic.soulissclient.helpers.AlertDialogHelper;
import it.angelic.soulissclient.helpers.SoulissPreferenceHelper;
import it.angelic.soulissclient.model.SoulissTypical;
import it.angelic.soulissclient.model.typicals.SoulissTypical19AnalogChannel;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.pheelicks.visualizer.VisualizerView;
import com.pheelicks.visualizer.renderer.BarGraphRenderer;

public class T19SingleChannelLedFragment extends AbstractMusicVisualizerFragment {
	private SoulissDBHelper datasource = new SoulissDBHelper(SoulissClient.getAppContext());
	private SoulissPreferenceHelper opzioni;

	private Button buttPlus;
	private Button buttMinus;

	private Button btOff;
	private Button btOn;
	private SoulissTypical19AnalogChannel collected;
	// private SoulissTypical related;

	private Button btFlash;
	private Button btSleep;

	private int intensity = 0;
	// Color change listener.
	private VisualizerView mVisualizerView;
	// private CheckBox checkMusic;

	private boolean continueIncrementing;
	// private Runnable senderThread;
	private boolean continueDecrementing;
	private ToggleButton togMulticast;
	private TableRow tableRowVis;
	private TableRow tableRowChannel;
	private View tableRowLamp;
	private Spinner modeSpinner;
	private SeekBar seekChannelIntensity;
	private TextView redChanabel;
	private View buttLamp;

	/**
	 * Serve per poter tenuto il bottone brightness
	 * 
	 * @param cmd
	 */
	private void startIncrementing(final Short cmd) {
		setIsIncrementing(true);
		new Thread(new Runnable() {
			public void run() {
				while (isIncrementing()) {
					issueIrCommand(cmd, intensity, 0, 0, togMulticast.isChecked());
				}
			}
		}).start();
	}

	synchronized private void stopIncrementing() {
		setIsIncrementing(false);
	}

	synchronized private boolean isIncrementing() {
		return continueIncrementing;
	}

	/**
	 * Serve per poter tenuto il bottone brightness
	 * 
	 * @param cmd
	 */
	private void startDecrementing(final Short cmd) {
		setIsDecrementing(true);
		new Thread(new Runnable() {
			public void run() {
				while (isDecrementing()) {
					issueIrCommand(cmd, intensity, 0, 0, togMulticast.isChecked());
				}
			}
		}).start();
	}

	synchronized private void stopDecrementing() {
		setIsDecrementing(false);
	}

	/**
	 * Per gestire tasto premuto
	 * 
	 * @param newSetting
	 */
	synchronized void setIsIncrementing(boolean newSetting) {
		continueIncrementing = newSetting;
	}

	synchronized private boolean isDecrementing() {
		return continueDecrementing;
	}

	synchronized void setIsDecrementing(boolean newSetting) {
		continueDecrementing = newSetting;
	}

	/**
	 * Interface describing a intensity change listener.
	 */
	public interface OnColorChangedListener {
		/**
		 * Method colorChanged is called when a new intensity is selected.
		 * 
		 * @param intensity
		 *            new intensity.
		 */
		void colorChanged(int color);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		opzioni = SoulissClient.getOpzioni();
		// tema
		if (opzioni.isLightThemeSelected())
			getActivity().setTheme(R.style.LightThemeSelector);
		else
			getActivity().setTheme(R.style.DarkThemeSelector);
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		if (!opzioni.isDbConfigured()) {
			AlertDialogHelper.dbNotInitedDialog(getActivity());
		}

	}

	@SuppressLint("NewApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (container == null)
			return null;
		opzioni = SoulissClient.getOpzioni();
		View ret = inflater.inflate(R.layout.frag_t19_singlechannel, container, false);
		datasource = new SoulissDBHelper(getActivity());
		datasource.open();

		Bundle extras = getActivity().getIntent().getExtras();
		if (extras != null && extras.get("TIPICO") != null) {
			collected = (SoulissTypical19AnalogChannel) extras.get("TIPICO");
		} else if (getArguments() != null) {
			collected = (SoulissTypical19AnalogChannel) getArguments().get("TIPICO");
		} else {
			try {
				// try emergency
				Log.w(Constants.TAG, "Attempting emergency load");
				collected = (SoulissTypical19AnalogChannel) datasource.getSoulissTypical(collected.getTypicalDTO()
						.getNodeId(), collected.getTypicalDTO().getSlot());
			} catch (Exception e) {
				Log.e(Constants.TAG, "Error retriving node:" + e.getMessage());
				return ret;
			}
		}
		
		super.setCollected(collected);
		super.actionBar = ((SherlockFragmentActivity) getActivity()).getSupportActionBar();
		super.actionBar.setCustomView(R.layout.custom_actionbar); // load
		super.actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM); // show
		super.actionBar.setDisplayHomeAsUpEnabled(true);
		refreshStatusIcon();
		
		assertTrue("TIPICO NULLO", collected instanceof SoulissTypical19AnalogChannel);
		collected.setPrefs(opzioni);
		collected.setCtx(getActivity());
		if (Constants.versionNumber >= 11) {
			ActionBar actionBar = getActivity().getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setTitle(collected.getNiceName());
		}

		buttPlus = (Button) ret.findViewById(R.id.buttonPlus);
		buttMinus = (Button) ret.findViewById(R.id.buttonMinus);
		togMulticast = (ToggleButton) ret.findViewById(R.id.checkBoxMulticast);

		btOff = (Button) ret.findViewById(R.id.buttonTurnOff);
		btOn = (Button) ret.findViewById(R.id.buttonTurnOn);
		tableRowLamp = (TableRow) ret.findViewById(R.id.tableRowLamp);
		tableRowChannel = (TableRow) ret.findViewById(R.id.tableRowChannel);

		btFlash = (Button) ret.findViewById(R.id.flash);
		btSleep = (Button) ret.findViewById(R.id.sleep);
		modeSpinner = (Spinner) ret.findViewById(R.id.modeSpinner);
		tableRowVis = (TableRow) ret.findViewById(R.id.tableRowMusic);
		mVisualizerView = (VisualizerView) ret.findViewById(R.id.visualizerView);
		mVisualizerView.setOpz(opzioni);

		seekChannelIntensity = (SeekBar) ret.findViewById(R.id.channelRed);
		
		redChanabel = (TextView) ret.findViewById(R.id.channelRedLabel);
		buttLamp = (Button) ret.findViewById(R.id.buttonLamp);
		btOff.setTag(it.angelic.soulissclient.model.typicals.Constants.Souliss_T1n_OffCmd);
		btOn.setTag(it.angelic.soulissclient.model.typicals.Constants.Souliss_T1n_OnCmd);
		buttPlus.setTag(it.angelic.soulissclient.model.typicals.Constants.Souliss_T1n_BrightUp);
		buttMinus.setTag(it.angelic.soulissclient.model.typicals.Constants.Souliss_T1n_BrightDown);
		btFlash.setTag(it.angelic.soulissclient.model.typicals.Constants.Souliss_T1n_Flash);
		btSleep.setTag(it.angelic.soulissclient.model.typicals.Constants.Souliss_T_related);

		// CHANNEL Listeners
		seekChannelIntensity.setOnSeekBarChangeListener(new channelInputListener());

		final OnItemSelectedListener lib = new AdapterView.OnItemSelectedListener() {
			

			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				if (pos == 0) {// channels
					tableRowVis.setVisibility(View.GONE);
					mVisualizerView.setVisibility(View.GONE);
					tableRowLamp.setVisibility(View.VISIBLE);
					tableRowChannel.setVisibility(View.VISIBLE);
					mVisualizerView.setEnabled(false);
					// TODO questi non vanno
					seekChannelIntensity.setProgress(intensity);
				} else {// music
					if (Constants.versionNumber >= 9) {
						mVisualizerView.setFrag(T19SingleChannelLedFragment.this);
						mVisualizerView.link(togMulticast.isChecked());
						addBarGraphRenderers();
					} else {
						// TODO scrivere che non esiste
					}
					tableRowLamp.setVisibility(View.GONE);
					mVisualizerView.setVisibility(View.VISIBLE);
					tableRowVis.setVisibility(View.VISIBLE);
					mVisualizerView.setEnabled(true);
					mVisualizerView.link(togMulticast.isChecked());

					tableRowChannel.setVisibility(View.GONE);
				}
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		};
		// avoid auto call upon Creation with runnable
		// modeSpinner.post(new Runnable() {
		// public void run() {
		modeSpinner.setOnItemSelectedListener(lib);
		// }
		// });

		// Listener generico
		OnClickListener plus = new OnClickListener() {
			public void onClick(View v) {
				Short cmd = (Short) v.getTag();
				assertTrue(cmd != null);

				issueIrCommand(cmd, intensity, 0, 0, togMulticast.isChecked());
				return;
			}

		};

		// start thread x decremento
		OnTouchListener incListener = new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				Short cmd = (Short) v.getTag();
				assertTrue(cmd != null);
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					startIncrementing(cmd);
					break;
				case MotionEvent.ACTION_UP:
					stopIncrementing();
					break;
				}

				return true;
			}

		};
		OnTouchListener decListener = new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				Short cmd = (Short) v.getTag();
				assertTrue(cmd != null);
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					startDecrementing(cmd);
					break;
				case MotionEvent.ACTION_UP:
					stopDecrementing();
					break;
				}

				return true;
			}

		};
		buttPlus.setOnTouchListener(incListener);
		buttMinus.setOnTouchListener(decListener);
		btOff.setOnClickListener(plus);
		btOn.setOnClickListener(plus);
		btFlash.setOnClickListener(plus);
		btSleep.setOnClickListener(plus);

		return ret;
	}

	// Methods for adding renderers to visualizer
	private void addBarGraphRenderers() {
		Paint paint = new Paint();
		paint.setStrokeWidth(50f);
		paint.setAntiAlias(false);
		paint.setColor(Color.argb(255, 156, 138, 252));
		BarGraphRenderer barGraphRendererBottom = new BarGraphRenderer(32, paint, false);
		mVisualizerView.addRenderer(barGraphRendererBottom);

		// TOP
		Paint paint2 = new Paint();
		paint2.setStrokeWidth(12f);
		paint2.setAntiAlias(false);
		paint2.setColor(Color.argb(255, 181, 11, 233));
		BarGraphRenderer barGraphRendererTop = new BarGraphRenderer(4, paint2, true);
		mVisualizerView.addRenderer(barGraphRendererTop);
	}

	public static T19SingleChannelLedFragment newInstance(int index, SoulissTypical content) {
		T19SingleChannelLedFragment f = new T19SingleChannelLedFragment();

		// Supply index input as an argument.
		Bundle args = new Bundle();
		args.putInt("index", index);
		// Ci metto il nodo dentro
		if (content != null) {
			args.putSerializable("TIPICO", (SoulissTypical19AnalogChannel) content);
		}
		f.setArguments(args);

		return f;
	}


/*
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				NodeDetailFragment details = NodeDetailFragment.newInstance(collected.getTypicalDTO().getNodeId(),
						collected.getParentNode());
				// Execute a transaction, replacing any existing fragment
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				if (opzioni.isAnimationsEnabled())
					ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
				ft.replace(R.id.details, details);
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
				ft.commit();
			} else {
				getActivity().finish();
				if (opzioni.isAnimationsEnabled())
					getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
*/
	@Override
	public void onResume() {
		super.onResume();
		datasource.open();
		IntentFilter filtere = new IntentFilter();
		filtere.addAction(it.angelic.soulissclient.net.Constants.CUSTOM_INTENT_SOULISS_RAWDATA);
		getActivity().registerReceiver(datareceiver, filtere);
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(datareceiver);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// datasource.close();
		if (mVisualizerView != null)
			mVisualizerView.release();
	}

	// Aggiorna il feedback
	private BroadcastReceiver datareceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// SoulissNode coll = datasource.getSoulissNode();
			collected = (SoulissTypical19AnalogChannel) datasource.getSoulissTypical(collected.getTypicalDTO()
					.getNodeId(), collected.getTypicalDTO().getSlot());
			// Bundle extras = intent.getExtras();
			// Bundle vers = (Bundle) extras.get("NODES");
			// intensity = collected.getColor();
			Log.d(Constants.TAG, "Detected data arrival, intensity change to: " + collected.getIntensity());
			redChanabel.setText(getString(R.string.Souliss_T19_received) + " " + collected.getIntensity());

			if (collected.getTypicalDTO().getOutput() == Souliss_T1n_OnCoil) {
				buttLamp.setBackgroundResource(R.drawable.bulb_on);
				buttLamp.getBackground().setAlpha(collected.getIntensity());
			} else if (collected.getTypicalDTO().getOutput() == Souliss_T1n_OffCoil)
				buttLamp.setBackgroundResource(R.drawable.bulb_off);
			else {
				Log.w(Constants.TAG, "Unknown status");
			}
			refreshStatusIcon();
		}
	};

	/**
	 * Inner class representing the intensity Channels.
	 */
	private class channelInputListener implements SeekBar.OnSeekBarChangeListener {

		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			// curva quadratica
			float val = seekChannelIntensity.getProgress();
			int out = (int) (val * val * 255 / 100 / 100);
			if (out > 255)
				out = 255;
			intensity = out;
			issueIrCommand(it.angelic.soulissclient.model.typicals.Constants.Souliss_T1n_Set, intensity, 0, 0,
					togMulticast.isChecked());
			redChanabel.setText(getString(R.string.Souliss_T19_set) + " " + out);

		}

		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		// solo per sicurezza
		public void onStopTrackingTouch(SeekBar seekBar) {
			collected.issueRefresh();//
		}

	}

	/**
	 * Souliss RGB light command Souliss OUTPUT Data is:
	 * 
	 * 
	 * INPUT data 'read' from GUI
	 */
	public void issueIrCommand(final short val, final int r, final int g, final int b, final boolean multicast) {
		collected.issueAnalogCommand(val, r, multicast);
	}

}
